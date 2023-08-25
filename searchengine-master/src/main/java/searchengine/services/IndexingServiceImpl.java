package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.model.SiteStatus;
import searchengine.repositories.PageModelRepository;
import searchengine.repositories.SiteModelRepository;
import searchengine.services.site_indexing.MappingSiteRecursiveCycle;
import searchengine.services.site_indexing.PageNode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;

import static searchengine.controllers.ApiController.isIndexingInProgress;

@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    @Value("${user-agent}")
    public static String userAgentName;

    private final SiteModelRepository siteModelRepository;
    private final PageModelRepository pageModelRepository;
    private final Environment environment;

    private final SitesList sites;
    private List<ForkJoinPool> forkList = new ArrayList<>();

    public static HashMap<String, CopyOnWriteArrayList<PageModel>> cashPagesMap = new HashMap<>();
    public static HashMap<String, CopyOnWriteArrayList<String>> cashPathMap = new HashMap<>();
    Logger logger = LoggerFactory.getLogger(IndexingServiceImpl.class);


    @Override
    @Transactional
    public void startIndexing() {
        isIndexingInProgress = true;
        logger.info(ServicesMessage.INDEXING_IN_PROGRESS);
        List<Site> sitesLists = sites.getSites();

        for(Site item : sitesLists) {
            startMappingSite(item);
        }
        isIndexingInProgress = false;
        logger.info(ServicesMessage.INDEXING_FINISHED);
    }

    private void startMappingSite(Site siteItem) {
        logger.info("Site name: " + siteItem.getName() );
        logger.info("Site url: " + siteItem.getUrl() );
        CopyOnWriteArrayList<PageModel> cashPages = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<String> cashPath = new CopyOnWriteArrayList<>();
        cashPagesMap.put(siteItem.getUrl(), cashPages);
        cashPathMap.put(siteItem.getUrl(), cashPath);

        clearDataBase(siteItem.getName());
        SiteModel site = setIndexingStatus(siteItem.getUrl(), siteItem.getName(), null);

        ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkList.add(forkJoinPool);
        SiteStatus status = goAllPages(site, forkJoinPool);

        site.setStatus(status);
        logger.info("The cached pages are being written to the database | size " + cashPages.size());


        /** Именно в новом потоке никак не хочет писать в таблицу Page. Предполагаю, потому что
         * она зависит от таблицы Site.
         * Но как решить?
         */
        writeCachePagesToBD(cashPages);
    }

    @Transactional
    public void clearDataBase(String name) {
        List<SiteModel> siteModelList = siteModelRepository.findByName(name);
        siteModelList.forEach(s -> {
            logger.info("Find in BD :" + s.getId() +" | " + s.getName() + " | deleting");
            siteModelRepository.delete(s);
        });
    }

    @Transactional
    private void writeCachePagesToBD(CopyOnWriteArrayList<PageModel> cashPages) {
        try {
            pageModelRepository.saveAll(cashPages);
//            cashPages.forEach(pageModelRepository::save);
        } catch (Exception ex) {
            logger.info("Error >> writeCachePagesToBD()" + ex);
        }
    }


    public SiteStatus goAllPages(SiteModel site, ForkJoinPool forkJoinPool) {
        logger.info("SetCurrentStatus: SiteStatus.INDEXING" + site.getUrl());

        try {
            PageNode root = new PageNode(site.getUrl());
            forkJoinPool.invoke(new MappingSiteRecursiveCycle(pageModelRepository, root, site));
            logger.info("SetCurrentStatus: SiteStatus.INDEXED" + site.getUrl());
            return SiteStatus.INDEXED;
        } catch (Exception ex) {
            logger.info("public void goAllPages Error" + ex);
            return SiteStatus.FAILED;
        }
    }


    private SiteModel setIndexingStatus(String source_root, String name, String error) {
        SiteModel row = new SiteModel();
        row.setName(name);
        row.setStatus(SiteStatus.INDEXING);
        row.setStatus_time(LocalDateTime.now());
        row.setUrl(source_root);
        row.setLast_error(error);
        siteModelRepository.save(row);
        SiteModel site = siteModelRepository.findByName(name).stream().findFirst().orElse(null);
        return site;
    }
}

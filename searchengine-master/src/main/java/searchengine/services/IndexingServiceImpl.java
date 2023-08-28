package searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
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
@Getter
@Setter
public class IndexingServiceImpl implements IndexingService {
    @Value("${user-agent}")
    public static String userAgentName = "Ya bot/12.01";

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
        logger.info("User-agent: " + userAgentName);
        List<Site> sitesLists = sites.getSites();

        for (Site item : sitesLists) {
            clearDataBase(item.getName());
            SiteModel site = new SiteModel();
            site.setUrl(item.getUrl());
            site.setName(item.getName());
            site.setStatus(SiteStatus.INDEXING);
            site.setStatus_time(LocalDateTime.now());
            site.setLast_error("");
            siteModelRepository.save(site);
            SiteStatus status = goAllPages(site);
            site.setStatus(status);
            logger.info("The cached pages are being written to the database | size " + cashPagesMap.get(site.getUrl()).size());
            writeCachePagesToBD(cashPagesMap.get(site.getUrl()));
        }
        isIndexingInProgress = false;
        logger.info(ServicesMessage.INDEXING_FINISHED);
    }

    @Transactional
    public void clearDataBase(String name) {
        List<SiteModel> siteModelList = siteModelRepository.findByName(name);
        siteModelList.forEach(s -> {
            logger.info("Find in BD :" + s.getId() + " | " + s.getName() + " | deleting");
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


    public SiteStatus goAllPages(SiteModel site) {
        logger.info("SetCurrentStatus: SiteStatus.INDEXING" + site.getUrl());
        CopyOnWriteArrayList<PageModel> cashPages = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<String> cashPath = new CopyOnWriteArrayList<>();
        cashPagesMap.put(site.getUrl(), cashPages);
        cashPathMap.put(site.getUrl(), cashPath);

        ForkJoinPool forkJoinPool = new ForkJoinPool();
        forkList.add(forkJoinPool);

        try {
            PageNode root = new PageNode(site.getUrl());
            forkJoinPool.invoke(new MappingSiteRecursiveCycle(root, site));
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

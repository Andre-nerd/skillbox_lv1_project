package searchengine.services;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
public class IndexingServiceImpl implements IndexingService {

    private final SiteModelRepository siteModelRepository;
    private final PageModelRepository pageModelRepository;
    private final Environment environment;

    //    @Value("#{${indexing-settings}}")
    private Map<String, String> sites = new HashMap<>();

    private List<ForkJoinPool> forkList = new ArrayList<>();

    public static CopyOnWriteArrayList<PageModel> cashPages;
    public static CopyOnWriteArrayList<String> cashPath;
    Logger logger = LoggerFactory.getLogger(IndexingServiceImpl.class);


    @Autowired
    public IndexingServiceImpl(SiteModelRepository siteModelRepository, PageModelRepository pageModelRepository, Environment environment) {
        this.siteModelRepository = siteModelRepository;
        this.pageModelRepository = pageModelRepository;
        this.environment = environment;
//        sites.put("https://www.lenta.ru", "Лента");
//        sites.put("https://www.skillbox.ru", "Skillbox");

        sites.put("https://volochek.life/", "Volochek");
//        sites.put("http://www.playback.ru/", "Playback");
    }


    @Override
    public void startIndexing() {
        isIndexingInProgress = true;
        logger.info(ServicesMessage.INDEXING_IN_PROGRESS);

        for (Map.Entry<String, String> item : sites.entrySet()) {
            cashPages = new CopyOnWriteArrayList<>();
            cashPath = new CopyOnWriteArrayList<>();
            clearDataBase(item.getValue());
            setCurrentStatus(item.getKey(), item.getValue(), SiteStatus.INDEXING, null);
            ForkJoinPool forkJoinPool = new ForkJoinPool();
            forkList.add(forkJoinPool);
            SiteStatus status = goAllPages(item.getKey(), item.getValue(), forkJoinPool);
            clearDataBase(item.getValue());
            setCurrentStatus(item.getKey(), item.getValue(), status, null);
            logger.info("The cached pages are being written to the database | size " + cashPages.size());
            writeCachePagesToBD();
        }
        //        forkJoinPool.shutdown();
//        forkJoinPool.shutdownNow();
        isIndexingInProgress = false;
        logger.info(ServicesMessage.INDEXING_FINISHED);
    }

    @Override
    @Transactional
    public void clearDataBase(String name) {
        List<SiteModel> siteModelList = siteModelRepository.findByName(name);
        siteModelList.forEach(s -> siteModelRepository.delete(s));
    }

    @Transactional
    private void writeCachePagesToBD() {
        try {
            cashPages.forEach(pageModelRepository::save);
        } catch (Exception ex){
            logger.info("Error >> writeCachePagesToBD()" + ex);
        }
    }

    @Transactional
    @Override
    public SiteStatus goAllPages(String source_root, String name, ForkJoinPool forkJoinPool) {
        logger.info("SetCurrentStatus: SiteStatus.INDEXING" + name);
        SiteModel site = siteModelRepository.findByName(name).stream().findFirst().orElse(null);

        try {
            PageNode root = new PageNode(source_root);
            forkJoinPool.invoke(new MappingSiteRecursiveCycle(pageModelRepository, root, site));
            logger.info("SetCurrentStatus: SiteStatus.INDEXED" + name);
            return SiteStatus.INDEXED;
        } catch (Exception ex) {
            logger.info("public void goAllPages Error" + ex);
            return SiteStatus.FAILED;
        }
    }


    private void setCurrentStatus(String source_root, String name, SiteStatus status, String error) {
        SiteModel row = new SiteModel();
        row.setName(name);
        row.setStatus(status);
        row.setStatus_time(LocalDateTime.now());
        row.setUrl(source_root);
        row.setLast_error(error);
        siteModelRepository.save(row);
    }

    @Override
    public void createNewRowIndexing(String root) {

    }

    @Override
    public void updateStatusTime(String root) {

    }

    @Override
    public void indexFinished(String root) {

    }

    @Override
    public void indexingFailed(String root) {

    }
}

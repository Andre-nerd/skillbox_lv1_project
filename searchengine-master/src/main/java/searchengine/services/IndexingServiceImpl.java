package searchengine.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import java.util.concurrent.ForkJoinPool;

import static searchengine.controllers.ApiController.isIndexingInProgress;

@Service
@Transactional
public class IndexingServiceImpl implements IndexingService {

    private final SiteModelRepository siteModelRepository;
    private final PageModelRepository pageModelRepository;
    private final Environment environment;

    //    @Value("#{${indexing-settings}}")
    private Map<String, String> sites = new HashMap<>();

    private List<ForkJoinPool> forkList = new ArrayList<>();
    Logger logger = LoggerFactory.getLogger(IndexingServiceImpl.class);


    @Autowired
    public IndexingServiceImpl(SiteModelRepository siteModelRepository, PageModelRepository pageModelRepository, Environment environment) {
        this.siteModelRepository = siteModelRepository;
        this.pageModelRepository = pageModelRepository;
        this.environment = environment;
        sites.put("https://www.lenta.ru", "Лента");
        sites.put("https://www.skillbox.ru", "Skillbox");
    }


    @Override
    public void startIndexing() {
        isIndexingInProgress = true;
        logger.info(ServicesMessage.INDEXING_IN_PROGRESS);

        for (Map.Entry<String, String> item : sites.entrySet()) {
            clearDataBase(item.getValue());
            ForkJoinPool forkJoinPool = new ForkJoinPool();
            forkList.add(forkJoinPool);
            goAllPages(item.getKey(), item.getValue(),forkJoinPool);
        }
        //        forkJoinPool.shutdown();
//        forkJoinPool.shutdownNow();
        isIndexingInProgress = false;
        logger.info(ServicesMessage.INDEXING_FINISHED);
    }

    @Override
    public void clearDataBase(String root) {
        List<SiteModel> siteModelList = siteModelRepository.findByName(root);
        siteModelList.forEach(s -> siteModelRepository.delete(s));
    }

    @Override
    public void goAllPages(String source_root, String name, ForkJoinPool forkJoinPool ) {
        SiteModel row = new SiteModel();
        row.setName(name);
        row.setStatus(SiteStatus.INDEXING);
        row.setStatus_time(LocalDateTime.now());
        row.setUrl(source_root);
        siteModelRepository.save(row);
        SiteModel site = siteModelRepository.findByName(name).stream().findFirst().orElse(null);

        try {
            PageNode root = new PageNode(source_root);
            forkJoinPool.invoke(new MappingSiteRecursiveCycle(pageModelRepository, root, site));
            row.setStatus_time(LocalDateTime.now());
            row.setStatus(SiteStatus.INDEXED);
            siteModelRepository.save(row);
        } catch (Exception ex) {
            row.setStatus_time(LocalDateTime.now());
            row.setLast_error(ex.getMessage());
            row.setStatus(SiteStatus.FAILED);
            siteModelRepository.save(row);
        }

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

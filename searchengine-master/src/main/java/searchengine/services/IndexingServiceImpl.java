package searchengine.services;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
import searchengine.services.site_indexing.TextParsing;

import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;

import static searchengine.controllers.ApiController.isIndexingInProgress;
import static searchengine.controllers.ResponseCode.ERROR_WHILE_CRAWLING;
import static searchengine.services.site_indexing.MappingSiteRecursiveCycle.TIME_OUT;

@Service
@RequiredArgsConstructor
@Getter
@Setter
public class IndexingServiceImpl implements IndexingService {

    @Value("${user-agent.name}")
    public static String userAgentName = "Ya bot/12.01";

    private final SiteModelRepository siteModelRepository;
    private final PageModelRepository pageModelRepository;
    private final Environment environment;

    private final SitesList sites;
    private List<ForkJoinPool> forkList = new ArrayList<>();
    private List<Thread> threads = new ArrayList<>();
    private HashMap<SiteModel, SiteStatus> siteStatuses = new HashMap<>();

    public static HashMap<String, CopyOnWriteArrayList<PageModel>> cashPagesMap = new HashMap<>();
    public static HashMap<String, CopyOnWriteArrayList<String>> cashPathMap = new HashMap<>();
    Logger logger = LoggerFactory.getLogger(IndexingServiceImpl.class);


    @Override
    @Transactional
    public void startIndexing() {
        isIndexingInProgress = true;
        logger.info(ServicesMessage.INDEXING_IN_PROGRESS);
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
            Thread thread = new Thread(() -> {
                SiteStatus status = goAllPages(site);
                siteStatuses.put(site, status);
            }
            );
            threads.add(thread);
        }

        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            try {
                t.join();
                logger.info("Thread : " + t + " join() successful");
            } catch (InterruptedException e) {
                logger.info("Exception when run thread " + t + " | " + e.getMessage());
            }
        }
        writeCachePagesToBD(cashPagesMap);
        for (Map.Entry<SiteModel, SiteStatus> item : siteStatuses.entrySet()) {
            setStatus(item.getKey(), item.getValue());
        }
        isIndexingInProgress = false;
        logger.info(ServicesMessage.INDEXING_FINISHED);
    }

    @Override
    public void stopIndexing() {
        isIndexingInProgress = false;
        forkList.forEach(ForkJoinPool::shutdownNow);
        threads.forEach(Thread::interrupt);
        forkList = new ArrayList<>();
        threads = new ArrayList<>();
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
    private void writeCachePagesToBD(HashMap<String, CopyOnWriteArrayList<PageModel>> cashPagesMap) {
        for (Map.Entry<String, CopyOnWriteArrayList<PageModel>> item : cashPagesMap.entrySet()) {
            logger.info("Write Cache Pages To BD() size" + item.getValue().size());
            try {
                pageModelRepository.saveAll(item.getValue());
            } catch (Exception ex) {
                logger.info("Error >> writeCachePagesToBD()" + ex);
            }
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


    @Transactional
    private void setStatus(SiteModel site, SiteStatus status) {
        try {
            SiteModel findSite = siteModelRepository.findByName(site.getName()).stream().findFirst().orElse(null);
            findSite.setStatus(status);
            findSite.setStatus_time(LocalDateTime.now());
            if (status.equals(SiteStatus.FAILED)) {
                findSite.setLast_error(ERROR_WHILE_CRAWLING);
            }
        } catch (Exception ex) {
            logger.info("private void setStatus Error" + ex);
        }
    }

    @Override
    public void indexOnePage(String path, SiteModel site) {
        try {
            PageModel page = new PageModel();
            page.setPath(path);
            page.setOwner(site);
            Connection connection = Jsoup.connect(path)
                    .userAgent(userAgentName)
                    .timeout(TIME_OUT);
            Document doc = connection.get();
            page.setCode(connection.response().statusCode());
            page.setContent(doc.html());
            TextParsing parser = new TextParsing();
            HashMap<String, Integer> map = parser.parsingOnePageText(doc.html());
            System.out.println(map);
        } catch (Exception ex) {
            logger.info("Error > IndexingService fun indexOnePage");
        }
    }

    @Override
    public boolean propertiesContainsHost(String host) {
        List<String> siteProperties = sites.getSites().stream().map(Site::getUrl).map(
                name -> {
                    try {
                        return new java.net.URI(name).getHost();
                    } catch (URISyntaxException e) {
                        return null;
                    }
                }
        ).toList();
        logger.info("Sites in properties: " + siteProperties);
        return (siteProperties.contains(host));
    }
}

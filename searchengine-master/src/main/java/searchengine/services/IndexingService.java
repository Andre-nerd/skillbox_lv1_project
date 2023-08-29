package searchengine.services;

import org.springframework.data.util.Pair;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.model.SiteStatus;

import java.util.concurrent.ForkJoinPool;

public interface IndexingService {
    void startIndexing();
    void stopIndexing();
    void indexOnePage(String path, SiteModel site);
    SiteModel propertiesContainsHost(String host);
}

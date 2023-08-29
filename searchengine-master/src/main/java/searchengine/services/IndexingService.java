package searchengine.services;

import searchengine.model.SiteModel;
import searchengine.model.SiteStatus;

import java.util.concurrent.ForkJoinPool;

public interface IndexingService {
    void startIndexing();
    void stopIndexing();
    void indexOnePage(String path, SiteModel site);
}

package searchengine.services;

import searchengine.model.SiteModel;
import searchengine.model.SiteStatus;

import java.util.concurrent.ForkJoinPool;

public interface IndexingService {
    void startIndexing();
    void clearDataBase(String root);

    void createNewRowIndexing(String root);

    SiteStatus goAllPages(SiteModel site, ForkJoinPool forkJoinPool);

    void updateStatusTime(String root);

    void indexFinished(String root);
    void indexingFailed(String root);
}

package searchengine.services;

import searchengine.model.SiteStatus;

import java.util.concurrent.ForkJoinPool;

public interface IndexingService {
    void startIndexing();
    void clearDataBase(String root);

    void createNewRowIndexing(String root);

    SiteStatus goAllPages(String source_root, String name, ForkJoinPool forkJoinPool);

    void updateStatusTime(String root);

    void indexFinished(String root);
    void indexingFailed(String root);
}

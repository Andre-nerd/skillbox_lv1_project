package searchengine.services;

import java.util.concurrent.ForkJoinPool;

public interface IndexingService {
    void startIndexing();
    void clearDataBase(String root);

    void createNewRowIndexing(String root);

    void goAllPages(String source_root, String name, ForkJoinPool forkJoinPool);

    void updateStatusTime(String root);

    void indexFinished(String root);
    void indexingFailed(String root);
}

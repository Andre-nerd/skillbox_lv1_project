package searchengine.services;

public interface IndexingService {
    void clearDataBase(String root);

    void createNewRowIndexing(String root);

    void goAllPages(String root);

    void updateStatusTime(String root);

    void indexFinished(String root);
    void indexingFailed(String root);
}

package searchengine.services;

import org.springframework.web.bind.annotation.RequestParam;

public interface SearchService {
    void search(String query, int offset, int limit, String siteUrl);
}

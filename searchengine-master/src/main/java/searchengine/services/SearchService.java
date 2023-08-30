package searchengine.services;

import org.springframework.web.bind.annotation.RequestParam;
import searchengine.dto.search.SearchResponse;

public interface SearchService {
    SearchResponse search(String query, int offset, int limit, String siteUrl);
}

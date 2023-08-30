package searchengine.services;

import org.springframework.web.bind.annotation.RequestParam;
import searchengine.dto.search.SearchResponse;

import java.util.List;

public interface SearchService {
    SearchResponse search(String query, int offset, int limit, List<String> siteUrl);
}

package searchengine.services;

import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.search.SiteSearchData;

import java.util.Arrays;

@Service
public class SearchServiceImpl implements SearchService{
    @Override
    public SearchResponse search(String query, int offset, int limit, String siteUrl) {
        SearchResponse response = new SearchResponse();
        response.setResult(true);
        response.setCount(900);
        response.setData(Arrays.asList(new SiteSearchData[]{}));
        return response;
    }
}

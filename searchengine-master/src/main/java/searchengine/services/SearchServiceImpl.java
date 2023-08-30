package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.search.SiteSearchData;
import searchengine.model.SiteModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);

    @Override
    public SearchResponse search(String query, int offset, int limit, List<String> siteUrl) {

        logger.info("Sites " + siteUrl.size() + " | " + siteUrl);
        SearchResponse response = new SearchResponse();
        response.setResult(true);
        response.setCount(900);
        response.setData(Arrays.asList(new SiteSearchData[]{}));
        return response;
    }
}

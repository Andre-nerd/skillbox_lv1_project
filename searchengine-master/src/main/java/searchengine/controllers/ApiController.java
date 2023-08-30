package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.config.SitesList;
import searchengine.dto.indexing.IndexingResponse;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.search.SiteSearchData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.SiteModel;
import searchengine.services.IndexingService;
import searchengine.services.SearchService;
import searchengine.services.StatisticsService;

import javax.servlet.http.HttpServletRequest;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static searchengine.controllers.Utils.ResponseCode.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {
    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    private final SearchService searchService;
    public final SitesList sites;
    public static boolean isIndexingInProgress = false;
    Logger logger = LoggerFactory.getLogger(ApiController.class);


    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingResponse> startIndexing() {
        IndexingResponse response = new IndexingResponse();
        if (isIndexingInProgress) {
            response.setResult(false);
            response.setError(INDEXING_ALREADY_STARTED);
        } else {
            response.setResult(true);
            isIndexingInProgress = true;
        }
        try {
            indexingService.startIndexing();
        } catch (Exception ex) {
            logger.info("Error >> indexingService.startIndexing(): " + ex);
        }
        ResponseEntity responseEntity = ResponseEntity.ok(response);
        logger.info("ApiController/startIndexing | response " + responseEntity);
        return responseEntity;
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexingResponse> stopIndexing() {
        IndexingResponse response = new IndexingResponse();
        if (!isIndexingInProgress) {
            response.setResult(false);
            response.setError(INDEXING_NOT_LAUNCH);
            return ResponseEntity.ok(response);
        }
        try {
            indexingService.stopIndexing();
            response.setResult(true);
        } catch (Exception ex) {
            response.setResult(false);
            response.setError(ex.getMessage());
        }
        ResponseEntity<IndexingResponse> responseEntity = ResponseEntity.ok(response);
        logger.info("ApiController/stopIndexing | response " + responseEntity);
        return responseEntity;
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexingResponse> indexPage(HttpServletRequest request, @RequestParam("url") String url) {
        IndexingResponse response = new IndexingResponse();
        try {
            String host = new java.net.URI(url).getHost();
            logger.info("ApiController/indexPage | indexing page " + url + " HOST: " + host);
            SiteModel site = indexingService.propertiesContainsHost(host);
            if (site != null) {
                logger.info("ApiController/indexPage | site included in properties ");
                indexingService.indexOnePage(url, site);
                response.setResult(true);
            } else {
                logger.info(URL_INVALIDATE);
                response.setResult(false);
                response.setError(URL_INVALIDATE);
            }
        } catch (Exception ex) {
            logger.info(URL_INVALIDATE);
            response.setResult(false);
            response.setError(URL_INVALIDATE);
        }

        ResponseEntity<IndexingResponse> responseEntity = ResponseEntity.ok(response);
        return responseEntity;
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(
            @RequestParam("query") String query,
            @RequestParam("offset") int offset,
            @RequestParam("limit") int limit,
            @RequestParam(value  = "site", required = false) String siteUrl
            ) {
        logger.info("\n" + "Query: " + query + " | " + siteUrl + "\n" +"offset: " + offset + " | " + "limit: " + limit +"\n");
        SearchResponse response = new SearchResponse();
        if(query == null || query.isBlank()){
            response.setError(EMPTY_SEARCH_TERM);
            return ResponseEntity.status(EMPTY_SEARCH_TERM_CODE).body(response);
        }
        List<String> sitesList = new ArrayList<>();
        if(siteUrl == null){
            sites.getSites().forEach(s -> sitesList.add(s.getUrl()));
        } else {
            try {
                String host = new java.net.URI(siteUrl).getHost();
                SiteModel siteModel = indexingService.propertiesContainsHost(host);
                if (siteModel == null) {
                    response.setError(PAGE_NOT_FOUND);
                    return ResponseEntity.status(PAGE_NOT_FOUND_CODE).body(response);
                } else {
                    sitesList.add(siteUrl);
                }
            } catch (URISyntaxException e) {
                logger.info("ApiController/search | " + PAGE_NOT_FOUND);
                response.setError(PAGE_NOT_FOUND);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
        }
        response = searchService.search(query,offset,limit, sitesList);
        return ResponseEntity.ok(response);
    }
}

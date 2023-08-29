package searchengine.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.statistics.IndexingResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.model.SiteModel;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;

import javax.servlet.http.HttpServletRequest;

import static searchengine.controllers.ResponseCode.*;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final StatisticsService statisticsService;
    private final IndexingService indexingService;
    public static boolean isIndexingInProgress = false;
    Logger logger = LoggerFactory.getLogger(ApiController.class);

    public ApiController(StatisticsService statisticsService, IndexingService indexingService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
    }

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
            if (indexingService.propertiesContainsHost(host)) {
                logger.info("ApiController/indexPage | site included in properties ");
                indexingService.indexOnePage(url, new SiteModel());
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
}

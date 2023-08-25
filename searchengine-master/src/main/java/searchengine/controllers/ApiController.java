package searchengine.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import searchengine.dto.statistics.IndexingResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.IndexingService;
import searchengine.services.StatisticsService;

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
    public ResponseEntity<IndexingResponse> startIndexing(){
        IndexingResponse response = new IndexingResponse();
        if (isIndexingInProgress){
            response.setResult(false);
            response.setError(INDEXING_ALREADY_STARTED);
        } else {
            response.setResult(true);
            isIndexingInProgress = true;
        }
        try{
            indexingService.startIndexing();
        } catch (Exception ex){
            logger.info("Error >> indexingService.startIndexing(): " + ex);
        }
        ResponseEntity responseEntity = ResponseEntity.ok(response);
        logger.info("ApiController | response " + responseEntity);
        return responseEntity;
    }
}

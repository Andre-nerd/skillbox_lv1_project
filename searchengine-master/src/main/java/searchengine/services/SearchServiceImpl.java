package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import searchengine.config.SitesList;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.search.SiteSearchData;
import searchengine.model.IndexModel;
import searchengine.model.Lemma;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageModelRepository;
import searchengine.repositories.SiteModelRepository;
import searchengine.services.site_indexing.TextParsing;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SiteModelRepository siteModelRepository;
    private final PageModelRepository pageModelRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);

    @Override
    public SearchResponse search(String query, int offset, int limit, List<String> siteUrl) {

        logger.info("Sites " + siteUrl.size() + " | " + siteUrl);
        List<String> listLemmas = TextParsing.splitTextIntoWords(query).stream()
                .filter(TextParsing::isNotServicePart)
                .map(TextParsing::normFormsWord)
                .toList();

        HashSet<String> setLemmas = new HashSet<>(listLemmas);
        logger.info("Lemmas " + setLemmas);
        List<Lemma> lemmaList = findLemmas(setLemmas);
        HashMap<PageModel, Integer> maxPageWeight = calculateRelevanceMaxPage(lemmaList);
        setLemmas.forEach(lemma ->{
            SiteSearchData site = new SiteSearchData();
            site.setSiteName(lemma);
        });

        SearchResponse response = new SearchResponse();
        response.setResult(true);
        response.setCount(900);
        response.setData(Arrays.asList(new SiteSearchData[]{}));
        return response;
    }

    private List<Lemma> findLemmas(HashSet<String> setLemmas) {
        List<Lemma> lemmaList = new ArrayList<>();
        setLemmas.forEach(lemmaName -> {
            Lemma lemma = lemmaRepository.findByLemma(lemmaName).stream().findAny().orElse(null);
            if (lemma != null) lemmaList.add(lemma);
        });
        Collections.sort(lemmaList);
        return lemmaList;
    }

    private List<PageModel> findPageByLemma(Lemma lemma) {
        List<IndexModel> indexModels = indexRepository.findByOwnerLemma(lemma).stream().toList();
        List<PageModel> pageModels = new ArrayList<>();
        indexModels.forEach(i -> {
            PageModel page = pageModelRepository.findById(i.getOwnerPage().getId()).stream().findAny().orElse(null);
            if (page != null) pageModels.add(page);
        });
        return pageModels;
    }

    private HashMap<PageModel, Integer> calculateRelevanceMaxPage(List<Lemma> lemmaList) {
        HashMap<PageModel, Integer> pageMap = new HashMap<>();
        lemmaList.forEach(lemma -> {
            List<PageModel> pagesByLemma = findPageByLemma(lemma);
            pagesByLemma.forEach(page -> {
                if (pageMap.containsKey(page)) {
                    pageMap.put(page, pageMap.get(page) + lemma.getFrequency());
                } else {
                    pageMap.put(page, lemma.getFrequency());
                }
            });
        });
        for (Map.Entry<PageModel,Integer> item : pageMap.entrySet()){
            logger.info("Page:" + item.getKey().getId() + " maxWeight " + item.getValue());
        }
        return pageMap;
    }

    private Double calculateRelevancePage(int pageMaxWeight, int lemmaFrequency){
        return (double) pageMaxWeight / lemmaFrequency;
    }
}

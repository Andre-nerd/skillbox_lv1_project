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
        findLemmas(setLemmas);

        SearchResponse response = new SearchResponse();
        response.setResult(true);
        response.setCount(900);
        response.setData(Arrays.asList(new SiteSearchData[]{}));
        return response;
    }

    private List<Lemma> findLemmas(HashSet<String> setLemmas){
        List<Lemma> lemmaList= new ArrayList<>();
        setLemmas.forEach(lemmaName ->{
            Lemma lemma = lemmaRepository.findByLemma(lemmaName).stream().findAny().orElse(null);
            if(lemma != null) lemmaList.add(lemma);
        });
        Collections.sort(lemmaList);
        lemmaList.forEach(l->{
            List<PageModel> pagesByLemma = findPageByLemma(l);
        });
        return lemmaList;
    }

    private List<PageModel> findPageByLemma(Lemma lemma){
        List<IndexModel> indexModels = indexRepository.findByOwnerLemma(lemma).stream().toList();
        indexModels.forEach(l->{
            logger.info("Index:" + l.getOwnerLemma().getId() + " | " + l.getOwnerPage().getId());
        });
        List<PageModel> pageModels = new ArrayList<>();
        indexModels.forEach(i->{
            PageModel page = pageModelRepository.findById(i.getOwnerPage().getId()).stream().findAny().orElse(null);
            if (page != null) pageModels.add(page);
        });
        return pageModels;
    }
}

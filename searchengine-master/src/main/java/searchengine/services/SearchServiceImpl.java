package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.search.SiteSearchData;
import searchengine.model.IndexModel;
import searchengine.model.Lemma;
import searchengine.model.PageModel;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageModelRepository;
import searchengine.repositories.SiteModelRepository;
import searchengine.services.site_indexing.TextParsing;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
        List<SiteSearchData> siteSearchDataList = new ArrayList<>();
        AtomicInteger count = new AtomicInteger();
        List<String> listLemmas = TextParsing.splitTextIntoWords(query).stream()
                .filter(TextParsing::isNotServicePart)
                .map(TextParsing::normFormsWord)
                .toList();

        HashSet<String> setLemmas = new HashSet<>(listLemmas);
        List<Lemma> lemmaList = findLemmas(setLemmas);
        HashMap<PageModel, Integer> maxPageWeight = calculateRelevanceMaxPage(lemmaList);
        lemmaList.forEach(lemma -> {
            SiteSearchData site = new SiteSearchData();
            site.setSite(lemma.getOwner().getUrl());
            site.setSiteName(lemma.getOwner().getName());
            IndexModel index = indexRepository.findByOwnerLemma(lemma).stream().findAny().orElse(null);
            if (index != null) {
                site.setUri(index.getOwnerPage().getPath());
                site.setTitle(getTitlePage(index.getOwnerPage()));
                Double relevance = calculateRelevancePage(maxPageWeight.get(index.getOwnerPage()), lemma.getFrequency());
                site.setRelevance(relevance);
                site.setSnippet(getSnippetPage(index.getOwnerPage(), lemma));
            }
            logger.info("Created new SiteInfo " + site);
            siteSearchDataList.add(site);
            count.getAndIncrement();
        });

        SearchResponse response = new SearchResponse();
        response.setResult(true);
        response.setCount(count.get());
        response.setData(siteSearchDataList);
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
        return pageMap;
    }

    private Double calculateRelevancePage(int pageMaxWeight, int lemmaFrequency) {
        return lemmaFrequency /(double) pageMaxWeight ;
    }

    private String getTitlePage(PageModel page) {
        Document document = Jsoup.parse(page.getContent(), page.getPath());
        Elements elements = document.select("title");
        StringBuilder title = new StringBuilder();
        for (Element a : elements) {
            logger.info("getTitlePage: " + a.html());
            title.append(a.html());
        }
        return title.toString();
    }

    private String getSnippetPage(PageModel page, Lemma lemma) {
        Document document = Jsoup.parse(page.getContent(), page.getPath());
        Elements elements = document.select("body").select("p");
        String snippet = null;
        for (Element a : elements) {
            List<String> normalText = TextParsing.splitTextIntoWords(a.html()).stream()
                    .filter(TextParsing::isNotServicePart)
                    .map(TextParsing::normFormsWord)
                    .toList();
            if (normalText.contains(lemma.getLemma())) {
                int maxLength = Math.min(a.html().length(), 1000);
                snippet = a.html().substring(0, maxLength);
                break;
            }
        }
        return snippet;
    }
}

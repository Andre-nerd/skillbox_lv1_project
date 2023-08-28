package searchengine.controllers.Utils;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.*;
import searchengine.repositories.IndexRepository;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageModelRepository;
import searchengine.repositories.SiteModelRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Transactional
public class WriteToBD {
    private final SiteModelRepository siteModelRepository;
    private final PageModelRepository pageModelRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;

    Logger logger = LoggerFactory.getLogger(WriteToBD.class);


    public void testIndex() {
        PageModel page = pageModelRepository.findById(2876).stream().findAny().orElse(null);
        logger.info("class WriteToBD page = " + page.getId());
        Lemma lemma = lemmaRepository.findById(1).stream().findAny().orElse(null);
        logger.info("class WriteToBD lemma = " + lemma.getId());
        IndexModel indexModel = new IndexModel();
        indexModel.setOwnerPage(page);
        indexModel.setOwnerLemma(lemma);
        indexModel.setRank(12);
        logger.info("class WriteToBD indexModel = " + indexModel);
        try {
            indexRepository.save(indexModel);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }

    public void testLemma() {
        SiteModel site = siteModelRepository.findById(308).stream().findAny().orElse(null);
        Lemma lemma = new Lemma();
        lemma.setLemma("New Lemma now");
        lemma.setOwner(site);
        lemma.setFrequency(5);
        try {
            lemmaRepository.save(lemma);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }
    public SiteModel testSiteSQL(String url, String name) {
        SiteModel site = new SiteModel();
        site.setStatus(SiteStatus.INDEXED);
        site.setUrl(url);
        site.setName(name);
        site.setStatus_time(LocalDateTime.now());
        site.setLast_error("No error OK");
        siteModelRepository.save(site);
        PageModel p = new PageModel();
        p.setOwner(site);
        List<PageModel> pages = new ArrayList<>();
        pages.add(p);
        site.setPages(pages);
        return site;
    }

    public void testPageSQL(SiteModel siteModel) {
        PageModel page = new PageModel();
        page.setOwner(siteModel);
        page.setPath("path/page/put");
        page.setCode(200);
        page.setContent("Thi is content");
        pageModelRepository.save(page);
    }

}

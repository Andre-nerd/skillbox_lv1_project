package searchengine.controllers;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import searchengine.config.SitesList;
import searchengine.model.Lemma;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.model.SiteStatus;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageModelRepository;
import searchengine.repositories.SiteModelRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Transactional
public class DefaultController {
    private final SiteModelRepository siteModelRepository;
    private final PageModelRepository pageModelRepository;
    private final LemmaRepository lemmaRepository;

    Logger logger = LoggerFactory.getLogger(DefaultController.class);

    /**
     * Метод формирует страницу из HTML-файла index.html,
     * который находится в папке resources/templates.
     * Это делает библиотека Thymeleaf.
     */
    @RequestMapping("/")
    public String index() {
        logger.info("Logger report about index()");
//        testSiteSQL("https://lenta", "Lemnta");

//
//        try {
//            new Thread(() -> {


//                }
//                ).start();
//        } catch (Exception ex) {
//            logger.info("Error >> " + ex);
//        }


//        if (siteModel != null) {
//            List<PageModel> pageModelList = siteModel.getPages();
//            pageModelList.forEach(System.out::println);
//        } else {
//            System.out.println(siteModel + "= null");
//        }

//        SiteModel site = testSiteSQL();
//        testPageSQL(site);
//        try {
//            List<SiteModel> siteModel = siteModelRepository.findAll().stream().toList();
//            System.out.println(siteModel.size());
//            testPageSQL(siteModel.get(0));
//        }catch (Exception ex){
//            System.out.println(ex);
//        }

//       Optional siteModel = siteModelRepository.findById(1);
//        System.out.println(siteModel.size());
        return "index";
    }


    private void testLemma() {
        Lemma lemma = new Lemma();
        lemma.setLemma("New Lemma now");
        try {
            lemmaRepository.save(lemma);
        } catch (Exception ex) {
            System.out.println(ex);
        }
    }


    private SiteModel testSiteSQL(String url, String name) {
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

    @Transactional
    private void testPageSQL(SiteModel siteModel) {
        PageModel page = new PageModel();
        page.setOwner(siteModel);
        page.setPath("path/page/put");
        page.setCode(200);
        page.setContent("Thi is content");
        logger.info("testPageSQL page " + page);
        pageModelRepository.save(page);
    }

}

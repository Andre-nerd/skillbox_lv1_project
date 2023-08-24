package searchengine.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import searchengine.model.Lemma;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.model.SiteStatus;
import searchengine.repositories.LemmaRepository;
import searchengine.repositories.PageModelRepository;
import searchengine.repositories.SiteModelRepository;

import java.time.LocalDate;
import java.util.List;

@Controller
public class DefaultController {
    private final SiteModelRepository siteModelRepository;
    private final PageModelRepository pageModelRepository;
    private final LemmaRepository lemmaRepository;

    Logger logger = LoggerFactory.getLogger(DefaultController.class);

    @Autowired
    public DefaultController(SiteModelRepository siteModelRepository, PageModelRepository pageModelRepository, LemmaRepository lemmaRepository) {
        this.siteModelRepository = siteModelRepository;
        this.pageModelRepository = pageModelRepository;
        this.lemmaRepository = lemmaRepository;
    }

    /**
     * Метод формирует страницу из HTML-файла index.html,
     * который находится в папке resources/templates.
     * Это делает библиотека Thymeleaf.
     */
    @RequestMapping("/")
    public String index() {
        logger.info("Logger report about index()");
//        testSiteSQL();
//        testLemma();
//
//        SiteModel siteModel = siteModelRepository.findById(1).stream().findAny().orElse(null);
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

    @Transactional
    private void testLemma(){
        Lemma lemma = new Lemma();
        lemma.setLemma("New Lemma now");
        try{
            lemmaRepository.save(lemma);
        } catch (Exception ex){
            System.out.println(ex);
        }
    }

    @Transactional
    private void testSiteSQL(){
        for (int i = 0; i<10; i++) {
            String url = "https://" + i + "-" + i;
            String name = String.valueOf(i);

            SiteModel site = new SiteModel();
            site.setStatus(SiteStatus.INDEXED);
            site.setUrl(url);
            site.setName(name);
            site.setStatus_time(LocalDate.now());
            site.setLast_error("No error OK");
            siteModelRepository.save(site);
        }
    }

    @Transactional
    private void testPageSQL(SiteModel siteModel){
        PageModel page = new PageModel();
        page.setOwner(siteModel);
        page.setPath("path/page/put");
        page.setCode(200);
        page.setContent("Thi is content");
        pageModelRepository.save(page);
    }

}

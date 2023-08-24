package searchengine.services.site_indexing;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.model.PageModel;
import searchengine.model.SiteModel;
import searchengine.repositories.PageModelRepository;
import searchengine.services.IndexingServiceImpl;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;
import static searchengine.services.ServicesMessage.URL_PARSING_ERROR;


public class MappingSiteRecursiveCycle extends RecursiveAction {

    //    private final SiteModelRepository siteModelRepository;
    private final PageModelRepository pageModelRepository;
    private PageNode node;
    private static final int TIME_OUT = 10000;
    private static final int SLEEP_TIME = 500;
    private static final Pattern patternNotFile = Pattern.compile("([^\\s]+(\\.(?i)(jpg|png|gif|bmp|pdf))$)");
    private static final Pattern patternNotAnchor = Pattern.compile("#([\\w\\-]+)?$");
    private final Pattern patternRoot;

    Logger logger = LoggerFactory.getLogger(MappingSiteRecursiveCycle.class);

    private final SiteModel site;

    @Autowired
    public MappingSiteRecursiveCycle(PageModelRepository pageModelRepository, PageNode node, SiteModel site) {
        this.pageModelRepository = pageModelRepository;
        this.node = node;
        this.site = site;
        patternRoot = Pattern.compile("^" + this.node.getUrl());
    }

    @Override
    protected void compute() {
        System.out.println("new void Compute launched __________________________" + node.getUrl());
        try {
            sleep(SLEEP_TIME);
            Connection connection = Jsoup.connect(node.getUrl())
                    .userAgent("FinderSearchBot/1.01 (Windows; U; WindowsNT)")
                    .timeout(TIME_OUT);

            Document page = connection.get();
            Elements elements = page.select("body").select("a");
            for (Element a : elements) {
                String childUrl = a.absUrl("abs:href");
                Optional<PageModel> sameUrl = pageModelRepository.findByPath(childUrl).stream().findAny();
                logger.info("Optional >?" + sameUrl);
                if (sameUrl.isPresent()) continue;
                try {
                    logger.info("newRow>>>");
                    PageModel newRow = createNewRow(childUrl, site);
                    logger.info("newRow = " + newRow);

                    /** Упорно не хочет делать запись в БД */
                    pageModelRepository.save(newRow);


                } catch (IOException exception){
                    logger.info(URL_PARSING_ERROR +": " + childUrl + " | " + exception);
                }

                System.out.println(childUrl);
                if (isUrlValid(childUrl)) {
                    childUrl = convertPath(childUrl);
                    node.addChild(new PageNode(childUrl));
                }
            }
        } catch (IOException | InterruptedException e) {
            System.out.println(e.toString());
        }

        for (PageNode child : node.getChildren()) {
            MappingSiteRecursiveCycle task = new MappingSiteRecursiveCycle(pageModelRepository, child, site);
            task.compute();
        }

    }

    public String convertPath(String url) {
        return url.replaceAll("\\?.+", "");
    }

    public boolean isUrlValid(String url) {
        return patternRoot.matcher(url).lookingAt() && !patternNotFile.matcher(url).find()
                && !patternNotAnchor.matcher(url).find();
    }

    private PageModel createNewRow(String path, SiteModel site) throws IOException {
        PageModel page = new PageModel();
        page.setPath(path);
        page.setOwner(site);
        logger.info("PageModel createNewRow site id" + site.getId());
        Connection connection = Jsoup.connect(path)
                .userAgent("FinderSearchBot/1.01 (Windows; U; WindowsNT)")
                .timeout(TIME_OUT);
        Document doc = connection.get();
        page.setCode(connection.response().statusCode());
        page.setContent(doc.html());
        return page;
    }
}

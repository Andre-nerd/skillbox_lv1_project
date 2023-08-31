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

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;
import static searchengine.services.IndexingServiceImpl.*;
import static searchengine.controllers.Utils.ServicesMessage.URL_PARSING_ERROR;


public class MappingSiteRecursiveCycle extends RecursiveAction {

    private PageNode node;
    public static final int TIME_OUT = 10000;
    public static final int SLEEP_TIME = 500;
    private static final Pattern patternNotFile = Pattern.compile("([^\\s]+(\\.(?i)(jpg|png|gif|bmp|pdf))$)");
    private static final Pattern patternNotAnchor = Pattern.compile("#([\\w\\-]+)?$");
    private final Pattern patternRoot;

    private final CopyOnWriteArrayList<PageModel> cashPages;
    private  final CopyOnWriteArrayList<String> cashPath;
    private static int maxCountCycle = 0;

    Logger logger = LoggerFactory.getLogger(MappingSiteRecursiveCycle.class);

    private final SiteModel site;

    @Autowired
    public MappingSiteRecursiveCycle(PageNode node, SiteModel site) {

        this.node = node;
        this.site = site;
        patternRoot = Pattern.compile("^" + this.node.getUrl());
        cashPages = cashPagesMap.get(site.getUrl());
        cashPath = cashPathMap.get(site.getUrl());
    }

    @Override
    protected void compute() {
        System.out.println("new void Compute launched __________________________" + node.getUrl());
        try {
            sleep(SLEEP_TIME);
            if (cashPath.contains(node.getUrl())) {
                logger.info("URL already indexed | skipped ");
                return;
            }
            Connection connection = Jsoup.connect(node.getUrl())
                    .userAgent(userAgentName)
                    .timeout(TIME_OUT);

            Document page = connection.get();
            Elements elements = page.select("body").select("a");
            for (Element a : elements) {
                String childUrl = a.absUrl("abs:href");

                System.out.println(childUrl);
                createNewRow(childUrl, site);

                if (isUrlValid(childUrl)) {
                    childUrl = convertPath(childUrl);
                    node.addChild(new PageNode(childUrl));
                }
            }
        } catch (IOException | InterruptedException e) {
            logger.info("void compute() Error" + e);
        }

        for (PageNode child : node.getChildren()) {
            MappingSiteRecursiveCycle task = new MappingSiteRecursiveCycle(child, site);
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


    private void createNewRow(String path, SiteModel site) {
        try {
            PageModel page = new PageModel();
            page.setPath(path);
            page.setOwner(site);
            Connection connection = Jsoup.connect(path)
                    .userAgent(userAgentName)
                    .timeout(TIME_OUT);
            Document doc = connection.get();
            page.setCode(connection.response().statusCode());
            page.setContent(doc.html());
            logger.info("PageModel createNewRow | code: " + page.getCode() + " | owner: " + page.getOwner());
            cashPath.add(path);
            cashPages.add(page);


        } catch (Exception exception) {
            logger.info(URL_PARSING_ERROR + ": " + path + " | " + exception);
        }
    }
}

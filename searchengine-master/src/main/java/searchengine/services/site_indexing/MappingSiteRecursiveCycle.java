package searchengine.services.site_indexing;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import searchengine.repositories.PageModelRepository;
import searchengine.repositories.SiteModelRepository;

import java.io.IOException;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;


public class MappingSiteRecursiveCycle extends RecursiveAction {

//    private final SiteModelRepository siteModelRepository;
    private final PageModelRepository pageModelRepository;
    private PageNode node;
    private static final int TIME_OUT = 10000;
    private static final int SLEEP_TIME = 500;
    private static final Pattern patternNotFile = Pattern.compile("([^\\s]+(\\.(?i)(jpg|png|gif|bmp|pdf))$)");
    private static final Pattern patternNotAnchor = Pattern.compile("#([\\w\\-]+)?$");
    private final Pattern patternRoot;

    @Autowired
    public MappingSiteRecursiveCycle(PageModelRepository pageModelRepository, PageNode node) {
        this.pageModelRepository = pageModelRepository;
        this.node = node;
        patternRoot = Pattern.compile("^" + this.node.getUrl());
    }
    @Override
    protected void compute() {
        System.out.println("new void Compute launched __________________________" + node.getUrl());
        try {
            sleep(SLEEP_TIME );
            Connection connection = Jsoup.connect(node.getUrl())
                    .userAgent("FinderSearchBot/1.01 (Windows; U; WindowsNT)")
                    .timeout(TIME_OUT);

            Document page = connection.get();
            Elements elements = page.select("body").select("a");
            for (Element a : elements) {
                String childUrl = a.absUrl("abs:href");
//                if (cash.contains(childUrl)) continue;
//                cash.add(childUrl);
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
            MappingSiteRecursiveCycle task = new MappingSiteRecursiveCycle(pageModelRepository, child);
            task.compute();
        }

    }
    public String convertPath(String url) {
        return url.replaceAll("\\?.+","");
    }

    public boolean isUrlValid(String url) {
        return patternRoot.matcher(url).lookingAt() && !patternNotFile.matcher(url).find()
                && !patternNotAnchor.matcher(url).find();
    }
}

package searchengine.services.site_indexing;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.concurrent.RecursiveAction;
import java.util.regex.Pattern;

import static java.lang.Thread.sleep;

public class MappingSiteRecursiveCycle extends RecursiveAction {
    private PageNode node;
    private static final int TIME_OUT = 10000;
    private static final int SLEEP_TIME = 170;
    private static final Pattern patternNotFile = Pattern.compile("([^\\s]+(\\.(?i)(jpg|png|gif|bmp|pdf))$)");
    private static final Pattern patternNotAnchor = Pattern.compile("#([\\w\\-]+)?$");
    private final Pattern patternRoot;
    public MappingSiteRecursiveCycle(PageNode node) {
        this.node = node;
        patternRoot = Pattern.compile("^" + this.node.getUrl());
    }
    @Override
    protected void compute() {
        System.out.println("new void Compute launched __________________________" + node.getUrl());
        try {
            sleep(SLEEP_TIME );
            Connection connection = Jsoup.connect(node.getUrl())
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
            MappingSiteRecursiveCycle task = new MappingSiteRecursiveCycle(child);
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

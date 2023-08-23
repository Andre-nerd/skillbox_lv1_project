package searchengine.services.site_indexing;

import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.CopyOnWriteArrayList;

@Getter
@Setter
public class PageNode {
    private volatile PageNode parent;
    public volatile int level;
    private String url;

    private volatile CopyOnWriteArrayList<PageNode> children;

    public PageNode(String url) {
        this.parent = null;
        this.level = 0;
        this.url = url;
        this.children = new CopyOnWriteArrayList<PageNode>();
    }

    public int findLevel() {
        return (parent == null) ? 0 : 1 + parent.findLevel();
    }

    public synchronized void addChild(PageNode element) {
        PageNode root = getRootElement();
        if(!root.containsUrl(element.getUrl())) {
            element.setParent(this);
            children.add(element);
        }
    }
    public PageNode getRootElement() {
        return parent == null ? this : parent.getRootElement();
    }

    public CopyOnWriteArrayList<PageNode> getChildren() {
        return children;
    }

    public boolean containsUrl(String url) {
        if (this.url.equals(url)) {
            return true;
        }
        for (PageNode child : children) {
            if(child.containsUrl(url))
                return true;
        }
        return false;
    }
}

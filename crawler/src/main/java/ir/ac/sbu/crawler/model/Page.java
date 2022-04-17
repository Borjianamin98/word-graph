package ir.ac.sbu.crawler.model;

import java.util.Set;

public class Page {

    private final String link;
    private final String linkContent;
    private final Set<String> anchors;

    public Page(String link, String linkContent, Set<String> anchors) {
        this.link = link;
        this.linkContent = linkContent;
        this.anchors = anchors;
    }

    public String getLink() {
        return link;
    }

    public String getLinkContent() {
        return linkContent;
    }

    public Set<String> getAnchors() {
        return anchors;
    }

    @Override
    public String toString() {
        return "Page{" +
                "link='" + link + '\'' +
                ", linkContent='" + linkContent + '\'' +
                ", anchors=" + anchors +
                '}';
    }
}

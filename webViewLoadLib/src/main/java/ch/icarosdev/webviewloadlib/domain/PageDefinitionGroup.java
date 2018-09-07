package ch.icarosdev.webviewloadlib.domain;

import java.util.ArrayList;
import java.util.List;

public class PageDefinitionGroup {

    public String groupName;
    public List<PageDefinition> pages;

    public PageDefinitionGroup() {
        this.pages = new ArrayList<PageDefinition>();
    }

    public PageDefinitionGroup(String name) {
        this.groupName = name;
        this.pages = new ArrayList<PageDefinition>();
    }

    public PageDefinition addPageEntry(String pageName, String url, int initialScale, String isImage) {
        return this.addPageEntry(pageName, url, "", "", initialScale, isImage);
    }

    public PageDefinition addPageEntry(String pageName, String url, String isImage) {
        return this.addPageEntry(pageName, url, "", "", 1, isImage);
    }

    public PageDefinition addPageEntry(String pageName, String url, String postUrl, String postArguments, int initialScale, String isImage) {
        PageDefinition definition = new PageDefinition(pageName, url, postUrl, postArguments, initialScale, isImage);
        this.pages.add(definition);

        return definition;
    }

    public void addPageEntry(PageDefinition definition) {
        this.pages.add(0, definition);
    }

    public void removePageEntry(PageDefinition page) {
        PageDefinition toRemove = null;
        for (PageDefinition pagePersisted : this.pages) {
            if (pagePersisted.getUrlToLoad().equals(page.getUrlToLoad())) {
                toRemove = pagePersisted;
                break;
            }
        }

        if (toRemove != null) {
            this.pages.remove(toRemove);
        }
    }
}

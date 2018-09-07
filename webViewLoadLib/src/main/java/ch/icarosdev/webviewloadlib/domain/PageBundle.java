package ch.icarosdev.webviewloadlib.domain;

import java.util.ArrayList;
import java.util.List;

public class PageBundle {

    public List<PageDefinitionGroup> definitionGroups;

    public PageBundle() {
        this.definitionGroups = new ArrayList<PageDefinitionGroup>();
    }

    public void mergeBundle(PageBundle customBundle) {
        if (customBundle != null) {
            for (PageDefinitionGroup definitionGroup : customBundle.definitionGroups) {
                PageDefinitionGroup originalDefinitionGroup = this.findExistingGroup(definitionGroup);
                if (originalDefinitionGroup != null) {
                    // insert all pages here
                    for (PageDefinition page : definitionGroup.pages) {
                        originalDefinitionGroup.addPageEntry(page);
                    }
                } else {
                    // insert the whole group into the list
                    this.definitionGroups.add(definitionGroup);
                }

            }
        }
    }

    private PageDefinitionGroup findExistingGroup(PageDefinitionGroup definitionGroup) {
        for (PageDefinitionGroup definitionGroupOrig : this.definitionGroups) {
            if (definitionGroup.groupName.equals(definitionGroupOrig.groupName)) {
                return definitionGroupOrig;
            }
        }

        return null;
    }

    public List<PageDefinition> getAllPageDefinitions() {
        List<PageDefinition> pages = new ArrayList<PageDefinition>();
        for (PageDefinitionGroup definitionGroup : this.definitionGroups) {
            for (PageDefinition page : definitionGroup.pages) {
                pages.add(page);
            }
        }

        return pages;
    }

    public PageDefinition getPage(int groupPosition, int childPosition) {
        return this.definitionGroups.get(groupPosition).pages.get(childPosition);
    }

    public void updatePageToBundle(PageDefinition page, String category, boolean deletePage) {
        PageDefinitionGroup groupToAddPage = null;

        for (PageDefinitionGroup definitionGroup : this.definitionGroups) {
            if (definitionGroup.groupName.equals(category)) {
                groupToAddPage = definitionGroup;
            }

            if (definitionGroup.pages.contains(page)) {
                definitionGroup.pages.remove(page);
            }
        }

        if (groupToAddPage != null && deletePage) {
            groupToAddPage.removePageEntry(page);
            return;
        }

        if (groupToAddPage == null) {
            groupToAddPage = new PageDefinitionGroup(category);
            groupToAddPage.groupName = category;
            this.definitionGroups.add(groupToAddPage);
        }

        groupToAddPage.pages.add(page);

    }

    public PageDefinitionGroup getDefinitionGroup(int groupPos) {
        return this.definitionGroups.get(groupPos);
    }
}

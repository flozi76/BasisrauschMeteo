package ch.icarosdev.webviewloadlib.custompages.ui;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import ch.icarosdev.webviewloadlib.R;
import ch.icarosdev.webviewloadlib.domain.PageDefinition;
import ch.icarosdev.webviewloadlib.domain.PageDefinitionGroup;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Florian
 * Date: 28.10.13
 * Time: 20:17
 * To change this template use File | Settings | File Templates.
 */
public class PageDefinitionArrayAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<PageDefinitionGroup> pageDefinitionGroups;
    private int grouplist_item;
    private int layoutResourceId;

    public PageDefinitionArrayAdapter(Context context, int grouplist_item, int layoutResourceId, List<PageDefinitionGroup> pageDefinitionGroups) {
        this.context = context;
        this.grouplist_item = grouplist_item;
        this.layoutResourceId = layoutResourceId;
        this.pageDefinitionGroups = pageDefinitionGroups;
    }

    @Override
    public int getGroupCount() {
        if (pageDefinitionGroups == null) {
            return 0;
        }
        return pageDefinitionGroups.size();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (pageDefinitionGroups == null || pageDefinitionGroups.size() < groupPosition) {
            return 0;
        }

        PageDefinitionGroup definitionGroup = pageDefinitionGroups.get(groupPosition);

        return definitionGroup.pages.size();  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object getGroup(int groupPosition) {
        PageDefinitionGroup definitionGroup = pageDefinitionGroups.get(groupPosition);

        return definitionGroup.groupName;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        PageDefinitionGroup definitionGroup = pageDefinitionGroups.get(groupPosition);

        PageDefinition pageDefinition = definitionGroup.pages.get(childPosition);

        return pageDefinition.urlToLoad;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getGroupId(int groupPosition) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean hasStableIds() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View row = convertView;
        PageDefinitionHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(grouplist_item, parent, false);

            holder = new PageDefinitionHolder();
            holder.pageName = (TextView) row.findViewById(R.id.listitem_page_category);

            row.setTag(holder);
        } else {
            holder = (PageDefinitionHolder) row.getTag();
        }

        PageDefinitionGroup group = this.pageDefinitionGroups.get(groupPosition);
        holder.pageName.setText(group.groupName);

        return row;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        View row = convertView;
        PageDefinitionHolder holder = null;

        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);

            holder = new PageDefinitionHolder();
            holder.pageName = (TextView) row.findViewById(R.id.listitem_page_name);
            holder.url = (TextView) row.findViewById(R.id.listitem_page_url);

            row.setTag(holder);
        } else {
            holder = (PageDefinitionHolder) row.getTag();
        }

        PageDefinitionGroup group = this.pageDefinitionGroups.get(groupPosition);
        PageDefinition page = group.pages.get(childPosition);
        holder.pageName.setText(page.pageName);
        holder.url.setText(page.urlToLoad);

        return row;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;  //To change body of implemented methods use File | Settings | File Templates.
    }

    static class PageDefinitionHolder {
        public TextView pageName;
        public TextView url;
    }
}

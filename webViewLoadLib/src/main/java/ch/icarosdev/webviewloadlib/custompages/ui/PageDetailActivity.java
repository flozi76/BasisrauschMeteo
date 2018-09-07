package ch.icarosdev.webviewloadlib.custompages.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.*;
import android.widget.*;
import ch.icarosdev.webviewloadlib.R;
import ch.icarosdev.webviewloadlib.domain.PageBundle;
import ch.icarosdev.webviewloadlib.domain.PageDefinition;
import ch.icarosdev.webviewloadlib.domain.PageDefinitionGroup;
import ch.icarosdev.webviewloadlib.xml.PageDefinitionRepository;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Florian
 * Date: 06.10.13
 * Time: 20:59
 * To change this template use File | Settings | File Templates.
 */
public class PageDetailActivity extends Activity {

    private static String TAG = "BASISRAUSCH_APP";
    private TextView pageUrl;
    private TextView pageName;
    private PageDefinition page;
    private Spinner category;
    private List<PageDefinitionGroup> categories;
    private PageBundle pageBundle;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            getActionBar().setDisplayHomeAsUpEnabled(true);
            setContentView(R.layout.page_detail_activityview);
            this.pageUrl = (TextView) findViewById(R.id.custompage_url);
            this.pageName = (TextView) findViewById(R.id.custompage_name);
            this.category = (Spinner) findViewById(R.id.custompage_spinner);
            Button button = (Button) findViewById(R.id.custompage_add_category_button);

            Bundle extras = getIntent().getExtras();

            PageDefinitionRepository repository = new PageDefinitionRepository(this);
            PageBundle pageBundleAllDefinitions = repository.readAllDefinitions();
            this.pageBundle = repository.readCustomDefinitions();

            this.categories = pageBundleAllDefinitions.definitionGroups;
            this.categories.remove(0);
            this.reloadCategories();

            if (extras != null) {
                boolean loadPage = extras.getBoolean("LOAD_PAGE");

                if (loadPage) {
                    int groupPos = extras.getInt("GROUP_POSITION");
                    int pagePos = extras.getInt("PAGE_POSITION");

                    this.page = pageBundle.getPage(groupPos, pagePos);
                    PageDefinitionGroup definitionGroup = pageBundle.getDefinitionGroup(groupPos);

                    int pos = 0;

                    for (PageDefinitionGroup group : categories) {
                        if (group.groupName.equals(definitionGroup.groupName)) {
                            break;
                        }
                        pos++;
                    }

                    this.category.setSelection(pos);

                    if (this.page != null) {
                        this.pageUrl.setText(this.page.urlToLoad);
                        this.pageName.setText(this.page.pageName);
                    }
                }
            }

            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    final EditText input = new EditText(PageDetailActivity.this);

                    new AlertDialog.Builder(PageDetailActivity.this)
                            .setTitle(getString(R.string.alert_title_category))
                            .setView(input)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    Editable editable = input.getText();
                                    PageDetailActivity.this.categories.add(new PageDefinitionGroup(editable.toString()));
                                    PageDetailActivity.this.reloadCategories();
                                    PageDetailActivity.this.category.setSelection(PageDetailActivity.this.categories.size() - 1);
                                    // deal with the editable
                                }
                            })
                            .setNegativeButton(getString(R.string.menu_abort), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    // Do nothing.
                                }
                            }).show();
                }

            });

        } catch (Exception ex) {
            Log.e(TAG, ex.toString(), ex);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                PageDetailActivity.this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem menuItemSave = menu.add(1, 1, 1, getString(R.string.menu_save));
        menuItemSave.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        MenuItem menuItemDelete = menu.add(1, 1, 2, getString(R.string.menu_delete));
        menuItemDelete.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);


        menuItemSave.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menuItemSave.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                PageDetailActivity.this.savePage(false);
                PageDetailActivity.this.finish();

                return true;
            }
        });

        menuItemDelete.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(PageDetailActivity.this);
                myAlertDialog.setTitle(PageDetailActivity.this.getString(R.string.title_delete_page_dialog));
                myAlertDialog.setMessage(PageDetailActivity.this.getString(R.string.message_delete_page_dialog));
                myAlertDialog.setPositiveButton(PageDetailActivity.this.getString(R.string.ok), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        PageDetailActivity.this.savePage(true);
                        PageDetailActivity.this.finish();
                    }});
                myAlertDialog.setNegativeButton(PageDetailActivity.this.getString(R.string.cancel), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        // do something when the Cancel button is clicked
                    }});
                myAlertDialog.show();


                return true;
            }
        });

        return true;
    }

    private void savePage(boolean delete) {
        try {
            PageDefinitionRepository repository = new PageDefinitionRepository(this);
            String newPageUrl = this.pageUrl.getText().toString();
            String newPageName = this.pageName.getText().toString();
            String categoryName = ((PageDefinitionGroup) this.category.getSelectedItem()).groupName;

            if (this.page == null) {
                this.page = new PageDefinition();
            }

            if (this.pageBundle == null) {
                this.pageBundle = new PageBundle();
            }

            this.page.setUrlToLoad(newPageUrl);
            this.page.pageName = newPageName;

            this.pageBundle.updatePageToBundle(page, categoryName, delete);
            repository.persistCustomDefinitions(this.pageBundle);

            this.tryPostUrl(this.page.urlToLoad, categoryName);
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
    }

    private void tryPostUrl(final String urlToLoad, final String category) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    HttpClient httpClient = new DefaultHttpClient();
                    HttpConnectionParams.setConnectionTimeout(httpClient.getParams(), 2000);
                    HttpConnectionParams.setSoTimeout(httpClient.getParams(), 2000);
                    HttpPost httpPost = new HttpPost("https://docs.google.com/forms/d/1a66MGp4qN_DEJA8Y0Xl4lvDjKyxWsgfPvOGHRR5coYc/formResponse");
                    List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
                    nameValuePairs.add(new BasicNameValuePair("entry.1317301163", urlToLoad));
                    nameValuePairs.add(new BasicNameValuePair("entry.161837820", category));

                    httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                    HttpResponse response = httpClient.execute(httpPost);

                    String resp = response.toString();

                } catch (Exception e) {
                    Log.e(TAG, e.toString(), e);
                }
            }
        }).start();
    }

    private void reloadCategories() {
        CategoryItemArrayAdapter categoryItemArrayAdapter = new CategoryItemArrayAdapter(this, R.layout.category_spinner_item, this.categories);
        this.category.setAdapter(categoryItemArrayAdapter);
    }

    static class PageDefinitionHolder {
        TextView pageName;
    }

    class CategoryItemArrayAdapter extends ArrayAdapter<PageDefinitionGroup> {
        private Context context;
        private int textViewResourceId;
        private List<PageDefinitionGroup> pageDefinitionGroups;

        public CategoryItemArrayAdapter(Context context, int textViewResourceId, List<PageDefinitionGroup> objects) {
            super(context, textViewResourceId, objects);
            this.context = context;
            this.textViewResourceId = textViewResourceId;
            pageDefinitionGroups = objects;
        }

        @Override
        public int getCount() {
            return this.pageDefinitionGroups.size();
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        public View getCustomView(int position, View convertView, ViewGroup parent) {

            View row = convertView;
            PageDefinitionHolder holder = null;
            try {
                if (row == null) {
                    LayoutInflater inflater = ((Activity) context).getLayoutInflater();
                    row = inflater.inflate(textViewResourceId, parent, false);

                    holder = new PageDefinitionHolder();
                    holder.pageName = (TextView) row.findViewById(R.id.spinner_item_text);

                    row.setTag(holder);
                } else {
                    holder = (PageDefinitionHolder) row.getTag();
                }

                PageDefinitionGroup group = this.pageDefinitionGroups.get(position);
                holder.pageName.setText(group.groupName);
            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }
            return row;
        }
    }
}
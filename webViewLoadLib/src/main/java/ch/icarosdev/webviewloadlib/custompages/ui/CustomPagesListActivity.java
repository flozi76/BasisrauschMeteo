package ch.icarosdev.webviewloadlib.custompages.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.Toast;
import ch.icarosdev.webviewloadlib.R;
import ch.icarosdev.webviewloadlib.domain.PageBundle;
import ch.icarosdev.webviewloadlib.xml.PageDefinitionRepository;
import com.actionbarsherlock.internal.widget.IcsAdapterView;

/**
 * Created with IntelliJ IDEA.
 * User: Florian
 * Date: 06.10.13
 * Time: 17:44
 */
public class CustomPagesListActivity extends Activity {

    private static String TAG = "BASISRAUSCH_APP";
    private ExpandableListView expListView;
    private PageDefinitionRepository repository;

    @Override
    protected void onResume() {
        super.onResume();
        try {

            expListView = (ExpandableListView) findViewById(R.id.custompages_list);

            this.initializePages();
            expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                    Intent intent = new Intent(CustomPagesListActivity.this, PageDetailActivity.class);
                    intent.putExtra("LOAD_PAGE", true);
                    intent.putExtra("GROUP_POSITION", groupPosition);
                    intent.putExtra("PAGE_POSITION", childPosition);
                    startActivity(intent);

                    return true;
                }
            });

        } catch (Exception ex) {
            Log.e(TAG, ex.toString(), ex);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custompages_listview);
        try {
            getActionBar().setDisplayHomeAsUpEnabled(true);

            expListView = (ExpandableListView) findViewById(R.id.custompages_list);
            this.repository = new PageDefinitionRepository(this);

            this.initializePages();
            expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

                public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                    Intent intent = new Intent(CustomPagesListActivity.this, PageDetailActivity.class);
                    intent.putExtra("LOAD_PAGE", true);
                    intent.putExtra("GROUP_POSITION", groupPosition);
                    intent.putExtra("PAGE_POSITION", childPosition);
                    startActivity(intent);

                    return true;
                }
            });

            expListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {

                    final Context context = view.getContext();
                    final long idt = id;
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);

//                    final CharSequence[] items = { "Delete", "Move Up", "Move Down" };
                    final CharSequence[] items = { context.getString(R.string.delete_list_item) };
                    builder.setTitle(context.getString(R.string.title_contextmenu_list));

                    builder.setItems(items, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int item) {
                            //cart = cartList.get(position);
                            //db.removeProductFromCart(context, cart);
                            final PageBundle pageBundle = repository.readCustomDefinitions();
                            //Toast.makeText(context, "Position : " + position + " id: " + idt, Toast.LENGTH_SHORT).show();

                            pageBundle.definitionGroups.remove(position);
                            repository.persistCustomDefinitions(pageBundle);

                            new AlertDialog.Builder(context)
                                    .setTitle(getString(R.string.success_remove_item_from_list))
                                    .setMessage(getString(R.string.item_removed))
                                    .setPositiveButton(getString(R.string.item_removed_done), new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            //Intent intent = new Intent(CartDetailsActivity.this, HomeScreen.class);
                                            //startActivity(intent);
                                        }
                                    })
                                    .show();

                            initializePages();

                        }

                    });

                    AlertDialog alert = builder.create();

                    alert.show();

                    return true;
                }
            });

        } catch (Exception ex) {
            Log.e(TAG, ex.toString(), ex);
        }

    }

    private void initializePages() {
        final PageBundle pageBundle = repository.readCustomDefinitions();
        PageDefinitionArrayAdapter adapter = new PageDefinitionArrayAdapter(this, R.layout.pagedefinition_grouplist_item, R.layout.pagedefinition_list_item, pageBundle.definitionGroups);
        expListView.setAdapter(adapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                CustomPagesListActivity.this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem menuItemPreferences = menu.add(1, 1, 1, getString(R.string.menu_add_page));
        MenuItem menuItemResetCustomPages = menu.add(1, 1, 2, getString(R.string.menu_delete_all_custom_pages));

        menuItemPreferences.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menuItemResetCustomPages.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        menuItemPreferences.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                Intent i = new Intent(CustomPagesListActivity.this, PageDetailActivity.class);
                startActivity(i);
                return true;
            }
        });

        menuItemResetCustomPages.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                AlertDialog.Builder myAlertDialog = new AlertDialog.Builder(CustomPagesListActivity.this);
                myAlertDialog.setTitle(CustomPagesListActivity.this.getString(R.string.title_delete_all_dialog));
                myAlertDialog.setMessage(CustomPagesListActivity.this.getString(R.string.message_delete_all_dialogentries));
                myAlertDialog.setPositiveButton(CustomPagesListActivity.this.getString(R.string.ok), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        PageDefinitionRepository repository = new PageDefinitionRepository(CustomPagesListActivity.this);
                        repository.deleteCustomDefinitions();
                        CustomPagesListActivity.this.finish();
                    }});
                myAlertDialog.setNegativeButton(CustomPagesListActivity.this.getString(R.string.cancel), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        // do something when the Cancel button is clicked
                    }});
                myAlertDialog.show();
                return true;
            }
        });

        return true;
    }
}
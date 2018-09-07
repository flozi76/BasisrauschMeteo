package ch.icarosdev.basisrauschmeteo;

import android.app.Activity;
import android.os.Bundle;
import android.preference.*;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Florian
 * Date: 06.10.13
 * Time: 14:46
 * To change this template use File | Settings | File Templates.
 */
public class BasisrauschPreferenceActivity extends PreferenceActivity {
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        // Add a button to the header list.
////        if (hasHeaders()) {
////            Button button = new Button(this);
////            button.setText("Some action");
////            setListFooter(button);
////        }
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        addPreferencesFromResource(R.xml.advanced_preferences);


        //PreferenceCategory targetCategory = (PreferenceCategory)findPreference("category_custompages");

        //CheckBoxPreference checkBoxPreference = new CheckBoxPreference(this);
        //make sure each key is unique
        //checkBoxPreference.setKey("keyName");
        //checkBoxPreference.setChecked(true);

        //targetCategory.addPreference(checkBoxPreference);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                BasisrauschPreferenceActivity.this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Populate the activity with the top-level headers.
     */
//    @Override
//         public void onBuildHeaders(List<Header> target) {
//        loadHeadersFromResource(R.xml.preference_headers, target);
//    }

    /**
     * This fragment shows the advanced_preferences for the first header.
     */
    public static class Prefs1Fragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Make sure default values are applied.  In a real app, you would
            // want this in a shared function that is used to retrieve the
            // SharedPreferences wherever they are needed.
            PreferenceManager.setDefaultValues(getActivity(), R.xml.advanced_preferences, false);

            // Load the advanced_preferences from an XML resource
            addPreferencesFromResource(R.xml.advanced_preferences);

        }
    }

    /**
     * This fragment contains a second-level set of preference that you
     * can get to by tapping an item in the first advanced_preferences fragment.
     */
    public static class Prefs1FragmentInner extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Can retrieve arguments from preference XML.
            PreferenceManager.setDefaultValues(getActivity(), R.xml.webpages_categories, false);

            // Load the advanced_preferences from an XML resource
            addPreferencesFromResource(R.xml.webpages_categories);

            //fetch the item where you wish to insert the CheckBoxPreference, in this case a PreferenceCategory with key "targetCategory"
            PreferenceCategory targetCategory = (PreferenceCategory)findPreference("webpages_categories");

            for (int i = 0; i < 10; i++)
            {
                //create one check box for each setting you need
                CheckBoxPreference checkBoxPreference = new CheckBoxPreference(this.getActivity());
                //make sure each key is unique
                checkBoxPreference.setKey("some Key" + i);
                checkBoxPreference.setTitle("Some Title " + i);
                checkBoxPreference.setChecked(true);

                targetCategory.addPreference(checkBoxPreference);
            }
        }
    }
}
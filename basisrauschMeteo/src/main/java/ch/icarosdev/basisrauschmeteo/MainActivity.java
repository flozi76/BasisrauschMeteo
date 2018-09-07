package ch.icarosdev.basisrauschmeteo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.*;
import ch.icarosdev.webviewloadlib.custompages.ui.ChangeLog;
import ch.icarosdev.webviewloadlib.xml.PageDefinitionRepository;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Window;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Tracker;
import com.viewpagerindicator.LinePageIndicator;

import ch.icarosdev.basisrauschmeteo.regtherm.RegthermDataWorkspace;
import ch.icarosdev.webviewloadlib.ImageViewFragment;
import ch.icarosdev.webviewloadlib.domain.PageDefinition;
import ch.icarosdev.webviewloadlib.domain.PageDefinitionGroup;
import ch.icarosdev.webviewloadlib.WebViewFragment;
import ch.icarosdev.webviewloadlib.xml.IDefinitionDownloadedListener;
import ch.icarosdev.webviewloadlib.domain.PageBundle;

import android.app.ActionBar.OnNavigationListener;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;
//import android.view.Menu;
//import android.view.MenuInflater;


public class MainActivity extends SherlockFragmentActivity implements  IDefinitionDownloadedListener,
		OnNavigationListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a
	 * {@link android.support.v4.app.FragmentPagerAdapter} derivative, which
	 * will keep every loaded fragment in memory. If this becomes too memory
	 * intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;
	private static String TAG = "BASISRAUSCH_APP";
	private Dictionary<String, Fragment> fragmentCache = new Hashtable<String, Fragment>(); 

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	private static String ARG_PAGEPOSITION = "arg_page";
	private static String ARG_TIMETICKS = "arg_timeticks";
	private static String ARG_NAVIGATIONPOSITION = "arg_navpos";
	private static String ARG_PAGEORIENTATION = "arg_orient";

	public int navigationPosition = 0;
	public int pagePosition = 0;

	private List<PageDefinitionGroup> pageGroups;

	private Tracker myTracker;

	// private PageDefinitionWorkspace pageDefinitionWorkspace;
	private int orientation;
	private boolean resetNavPosition;
	private long dateTimeTicks;
    private PageDefinitionRepository pageDefinitionRepository;
    private boolean forceReload;

    @Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance().activityStart(this);
        Log.i(TAG, "Basisrausch Meteo Application Started!" );
	}

	@Override
	public void onStop() {
		super.onStop();

		EasyTracker.getInstance().activityStop(this);
		RegthermDataWorkspace.resetHttpClient();
		this.dateTimeTicks = Calendar.getInstance().getTimeInMillis();
        Log.i(TAG, "Basisrausch Meteo Application Stopped!" );
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		long dateTimeTicksSaved = this.dateTimeTicks;
		long dateTimeTicksNow = Calendar.getInstance().getTimeInMillis();
		
		long difference = dateTimeTicksNow - dateTimeTicksSaved;
		
		if(difference > 50000 || this.forceReload){
			this.resetNavPosition = true;
		} else {
			this.resetNavPosition = false;	
		}
		
		if(this.resetNavPosition){
			this.navigationPosition = 0;
			this.pagePosition = 0;
			this.initializeMeteoPages();
			this.initializeSpinner();
            this.forceReload = false;
		}
	};
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		requestWindowFeature(Window.FEATURE_PROGRESS);

		super.onCreate(savedInstanceState);


        ChangeLog cl = new ChangeLog(this);
        //cl.getLogDialog().show();
        if (cl.firstRun()) {
            cl.getLogDialog().show();
        }


		setSupportProgressBarIndeterminateVisibility(true);
		setContentView(R.layout.activity_page_loader);

		if (savedInstanceState != null) {
//			int orient = savedInstanceState.getInt(ARG_PAGEORIENTATION);
//			if(orient != this.orientation)
//			{
//				this.orientation = orient;
				this.pagePosition = savedInstanceState.getInt(ARG_PAGEPOSITION);
				this.navigationPosition = savedInstanceState.getInt(ARG_NAVIGATIONPOSITION);
            this.dateTimeTicks = savedInstanceState.getLong(ARG_TIMETICKS);
//			}
		}

		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        try{
            this.pageGroups = new ArrayList<PageDefinitionGroup>();
            this.initializeMeteoPages();
            this.initializeSpinner();

            EasyTracker.getInstance().setContext(this);
            this.myTracker = EasyTracker.getTracker();
            this.myTracker.setStartSession(true);
        } catch (Exception ex) {
            Log.e(TAG, ex.toString(), ex);
        }
	}

	private void initializeSpinner() {
		String[] items = new String[this.pageGroups.size()];
		
		int i = 0;
		for (PageDefinitionGroup group : this.pageGroups) {
			items[i] = group.groupName;
			i++;
		}
		
		MyAdapter adapter = new MyAdapter(this, R.layout.spinner_row, items);

		adapter.setDropDownViewResource(R.layout.sherlock_spinner_dropdown_item);
		getActionBar().setListNavigationCallbacks(adapter, this);
		getActionBar().setSelectedNavigationItem(this.navigationPosition);
		
		//this.onNavigationItemSelected(this.navigationPosition, 0);

		//this.invalidateSectionPagesAdapter();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);

	    // Checks the orientation of the screen
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	        this.orientation = 0;
	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	    	this.orientation = 1;
	    }
	 }

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		
		outState.putInt(ARG_NAVIGATIONPOSITION, this.navigationPosition);
		outState.putInt(ARG_PAGEPOSITION, this.pagePosition);
		outState.putInt(ARG_PAGEORIENTATION, this.orientation);
		outState.putLong(ARG_TIMETICKS, this.dateTimeTicks);
	}

    @Override
    public void onBackPressed() {
        if(this.navigationPosition == 0 && this.pagePosition == 0) {
            super.onBackPressed();
        }
        else{
            this.navigationPosition = 0;
            this.pagePosition = 0;
            this.initializeMeteoPages();
            this.initializeSpinner();
        }
    }

	private void invalidateSectionPagesAdapter() {
		try {
			if (mSectionsPagerAdapter != null) {
				mSectionsPagerAdapter.notifyDataSetChanged();
				mViewPager.invalidate();
			}
			mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

			// Set up the ViewPager with the sections adapter.
			mViewPager = (ViewPager) findViewById(R.id.pager);
			mViewPager.setAdapter(mSectionsPagerAdapter);
			
			// Bind the title indicator to the adapter
			LinePageIndicator titleIndicator = (LinePageIndicator) findViewById(R.id.indicator);
			titleIndicator.setViewPager(mViewPager);
			titleIndicator.setOnPageChangeListener(new OnPageChangeListener() {

				@Override
				public void onPageScrollStateChanged(int arg0) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onPageScrolled(int arg0, float arg1, int arg2) {
					// TODO Auto-generated method stub

				}

				@Override
				public void onPageSelected(int arg0) {
					pagePosition = arg0;
				}
			});
			
			titleIndicator.setCurrentItem(0);
			//mViewPager.setCurrentItem(0, true);

		} catch (Exception ex) {
			Log.e(TAG, ex.toString(), ex);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem menuItemNext = menu.add(22, 11, 1, "Up").setIcon(R.drawable.navigation_expand);
		menuItemNext.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		
		menu.add(22, 21, 2, "Refr").setIcon(R.drawable.navigation_refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
		MenuItem menuItemPrevious = menu.add(22, 31, 3, "Down");
		menuItemPrevious.setIcon(R.drawable.navigation_collapse).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

		menuItemNext.setOnMenuItemClickListener(new OnMenuItemClickListener() {			
			@Override
			public boolean onMenuItemClick(MenuItem item) {

				try {
					if(navigationPosition < pageGroups.size()-1)
					{
						navigationPosition++;
						getActionBar().setSelectedNavigationItem(navigationPosition);
					}
					return true;
				} catch (Exception ex) {
					Log.e(TAG, ex.toString(), ex);
				}
				return false;
			}
		});
		
		menuItemPrevious.setOnMenuItemClickListener(new OnMenuItemClickListener() {			
			@Override
			public boolean onMenuItemClick(MenuItem item) {

				try {
					if(navigationPosition > 0)
					{
						navigationPosition--;
						getActionBar().setSelectedNavigationItem(navigationPosition);
					}
					return true;
				} catch (Exception ex) {
					Log.e(TAG, ex.toString(), ex);
				}
				return true;
			}
		});

             // Set MenuItem
        MenuItem menuItemPreferences = menu.add(1, 1, 1, R.string.menu_custom_settings).setIcon(R.drawable.navigation_expand);
        menuItemPreferences.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        menuItemPreferences.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {

                Intent i = new Intent(MainActivity.this, BasisrauschPreferenceActivity.class);
                startActivity(i);
                MainActivity.this.forceReload = true;

                return true;
            }
        });

        MenuItem menuItemInfo = menu.add(1, 2, 2, R.string.menu_appinfo).setIcon(R.drawable.navigation_expand);
        menuItemInfo.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        menuItemInfo.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                ChangeLog cl = new ChangeLog(MainActivity.this);
                cl.getLogDialog().show();
                return true;
            }
        });


		return true;
	}

	private String encodeUrl(String urlToEncode) {
		try {
			return URLEncoder.encode(urlToEncode, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return urlToEncode;
		}
	}

	private String replaceDateWithOffset(String basePage, int offset) {
		Calendar cal = new GregorianCalendar();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

		cal.add(Calendar.DATE, offset);

		dateFormat.setCalendar(cal);
		System.out.println(dateFormat.format(cal.getTime()));

		String dateToday = dateFormat.format(cal.getTime());

		basePage = basePage.replace("{XXXX_XXXX}", dateToday);

		return basePage;
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the primary sections of the app.
	 */
	public class SectionsPagerAdapter extends FragmentStatePagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int i) {
            MainActivity.this.pagePosition = i;

			PageDefinition definition = pageGroups.get(navigationPosition).pages.get(i);
			String urlToload = definition.getPreparedUrl();

			String id = "Id_"+navigationPosition+"_"+i;
			Fragment fragment = fragmentCache.get(id);
			if(fragment == null)
			{
				fragment = new WebViewFragment();
                if(definition.isImage != null){
                    if(definition.isImage.equals("true"))
                    {
                        fragment = new ImageViewFragment();
                    }
                    else if (definition.isImage.equals("isRegtherm"))
                    {
                        fragment = new RethermViewFragment();
                    }
                    else if (definition.isImage.equals("isRegthermMeteotest"))
                    {
                        fragment = new RethermMeteoTestViewFragment();
                    }
                }
				
				fragmentCache.put(id, fragment);
			}
			Bundle args = new Bundle();
			
			args.putString(WebViewFragment.ARG_URL_KEY, urlToload );
			args.putString(WebViewFragment.ARG_POSTURL_KEY, definition.postUrlToExecuteBefore);
			args.putString(WebViewFragment.ARG_POSTARGS_KEY, definition.postArguments);
			args.putInt(WebViewFragment.ARG_INITIALSCALE_KEY,definition.initialScale);
			args.putBoolean(WebViewFragment.ARG_SHOW_SOURCE_URL,definition.showsourceurl);

			fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
            if(pageGroups != null && pageGroups.size() > navigationPosition)
            {
			    return pageGroups.get(navigationPosition).pages.size();
            }
            return 0;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			String pageName = "";
			try {
				if(pageGroups.get(navigationPosition).pages.size()>position)
				{
					pageName = pageGroups.get(navigationPosition).pages.get(position).pageName;
				
					// Tracking ...
					myTracker.setStartSession(true);
					myTracker.trackView(pageName);
					myTracker.trackEvent(pageName, pageName, pageName, (long)position);
				}
				
			} catch (Exception ex) {
				Log.e(TAG, ex.toString(), ex);
			}

			return pageName;
		}
	}

	@Override
	public boolean onNavigationItemSelected(int position, long id) {
		try {
			this.setSupportProgressBarIndeterminateVisibility(false);
			this.navigationPosition = position;
			invalidateSectionPagesAdapter();
		} catch (Exception ex) {
			Log.e(TAG, ex.toString(), ex);
		}

		return false;
	}
	
	private void initializeMeteoPages() {		
		
		PageBundle bundle = null;

        this.pageDefinitionRepository = new PageDefinitionRepository(this);
        this.pageDefinitionRepository.initializeWorkspaces(this);

		// Flozi APPDATA
//		this.pageDefinitionWorkspace = PageDefinitionWorkspace.getInstance().initialize(getResources().getString(R.string.app_name), "https://dl.dropboxusercontent.com/s/q5d84q08dajleuu/PageDefinitions.xml?token_hash=AAEy7eUW49wsJdlPMPjcNrJi6eiFsJdjyFJ4n0W-ziXZMA&dl=1", "PageDefinitions.xml", this);

		// Basisrausch AppData
		//this.pageDefinitionWorkspace = PageDefinitionWorkspace.getInstance().initialize(getResources().getString(R.string.app_name), "https://dl.dropboxusercontent.com/s/9w79rg302fpsaj5/PageDefinitions.xml?token_hash=AAF54zKqCBEYX_TaTtPygV4nxNGMHi3d6cXWkVMLxS90lg&dl=1", "PageDefinitions.xml", this);



        bundle = this.pageDefinitionRepository.readAllDefinitions();

        PreferenceManager.setDefaultValues(this, R.xml.advanced_preferences, false);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (settings.getBoolean("checkbox_deactivate_defaultpages", false)) {
            bundle = this.pageDefinitionRepository.readCustomDefinitions();
        }

		if(bundle == null)
		{
			bundle = this.initializeMeteoPagesBundle();
			this.pageDefinitionRepository.persistCommonDefinitions(bundle);
		}
		
		this.pageGroups = bundle.definitionGroups;
	}
	
	private PageBundle initializeMeteoPagesBundle() {
		
		PageBundle bundle = new PageBundle();
		
		PageDefinitionGroup basisrausch = new PageDefinitionGroup("Basisrausch");
		bundle.definitionGroups.add(basisrausch);
		basisrausch.addPageEntry("Basisrausch", "http://www.basisrausch.ch", "false");

		PageDefinitionGroup segelflugPrognose = new PageDefinitionGroup("Segelflugprognose");
		bundle.definitionGroups.add(segelflugPrognose);
		segelflugPrognose.addPageEntry("Segelflugprognose", "http://www.shv-fsvl.ch/meteo/render_meteo_text.php?prod=PPSW40", "http://www.shv-fsvl.ch/member/login.php?lang=de&target=/member/login.php", "shvNr=40943&shvArt=a&password=sentinella&do=login", 98, "false");

		PageDefinitionGroup isobarenKarten = new PageDefinitionGroup("Isobarenkarten");
		bundle.definitionGroups.add(isobarenKarten);
		isobarenKarten.addPageEntry("Isobarenkarte Meteoschweiz", "http://www.meteoschweiz.admin.ch/web/de/wetter/allgemeine_lage.Par.0011.DownloadFile.ext.tmp/gross.png", 104, "true");
		isobarenKarten.addPageEntry("Isobarenkarte Metoffice", "http://www.metoffice.gov.uk/public/weather/surface-pressure/#?tab=surfacePressureColour&fcTime=1368702000", "false");
		isobarenKarten.addPageEntry("Isobarenkarte Meteocentrale", "http://www.meteocentrale.ch/de/wetter/unwetter-schweiz/warnlagebericht.html",105, "false");
		
		PageDefinitionGroup nzzSatellitenFilm = new PageDefinitionGroup("Satelitenbilder");
		bundle.definitionGroups.add(nzzSatellitenFilm);
		nzzSatellitenFilm.addPageEntry("Satellitenbilder", "http://www.meteoschweiz.admin.ch/web/de/wetter/aktuelles_wetter/satellitenbild.Par.0012.Loop.html", 70, "false");

		PageDefinitionGroup nzz = new PageDefinitionGroup("NZZ Meteo");
		bundle.definitionGroups.add(nzz);
		nzz.addPageEntry("NZZ Meteo", "http://mobile.nzz.ch/wetter/wetterbericht/", "false");

		PageDefinitionGroup regtherm = new PageDefinitionGroup("Regtherm");
		bundle.definitionGroups.add(regtherm);
		regtherm.addPageEntry("Regtherm", "https://shop.meteoswiss.ch/image/data/mez/eshop/aviatik/regtherm/Berner_Alpen.txt?import=true", "https://shop.meteoswiss.ch/login.html:#_#:https://shop.meteoswiss.ch/themeLayerView.html", "form.username=flozi76@bluewin.ch&form.password=soaringwetter1:#_#:type=ptsc&id=25&uid=79979328606211812", 1, "false");

		PageDefinitionGroup wind = new PageDefinitionGroup("Wind");
		bundle.definitionGroups.add(wind);
		wind.addPageEntry("Windwerte Meteoschweiz", "http://www.meteoschweiz.admin.ch/web/de/wetter/aktuelles_wetter.par0010.html?allStations=1", "false");
		wind.addPageEntry("Wind 1000m", "http://www.meteoblue.com/uploads/meteobluedata/pub/nmm4/maps/77WRH90.htm", "false");
		wind.addPageEntry("Wind 3000m", "http://www.meteoblue.com/uploads/meteobluedata/pub/nmm4/maps/77WRH70.htm", "false");

		PageDefinitionGroup pressure = new PageDefinitionGroup("Druckdifferenz");
		bundle.definitionGroups.add(pressure);
		pressure.addPageEntry("Druckdifferenz Nord-Süd", "http://www.shv-fsvl.ch/meteo/render_meteo_image.php?prod=VISG11", "http://www.shv-fsvl.ch/member/login.php?lang=de&target=/member/login.php", "shvNr=40943&shvArt=a&password=sentinella&do=login", 90, "false");
		pressure.addPageEntry("Druckdifferenz Ost-West", "http://www.shv-fsvl.ch/meteo/render_meteo_image.php?prod=VISG10", "http://www.shv-fsvl.ch/member/login.php?lang=de&target=/member/login.php", "shvNr=40943&shvArt=a&password=sentinella&do=login", 90, "false");

		PageDefinitionGroup radiosondierung = new PageDefinitionGroup("Radiosondierung");
		bundle.definitionGroups.add(radiosondierung);
		radiosondierung.addPageEntry("Payerne-Stuttgart 00Z", "http://www.meteoschweiz.admin.ch/web/de/klima/messsysteme/atmosphaere/radiosondierungen.Par.0013.DownloadFile.ext.tmp/paystu00utc.gif",103, "true");
		radiosondierung.addPageEntry("Payerne-Stuttgart 12Z", "http://www.meteoschweiz.admin.ch/web/de/klima/messsysteme/atmosphaere/radiosondierungen.Par.0014.DownloadFile.ext.tmp/paystu12utc.gif",103, "true");
		radiosondierung.addPageEntry("Schaenis-Soaring", "http://www.schaenis-soaring.ch/emagramm.cfm",103, "false");

		PageDefinitionGroup regenradar = new PageDefinitionGroup("Niederschlagsradar");
		bundle.definitionGroups.add(regenradar);
		regenradar.addPageEntry("Niederschlagsradar", "http://www.meteoschweiz.admin.ch/web/de/wetter/aktuelles_wetter/radarbild.Par.0005.Loop.html",103, "false");

		PageDefinitionGroup dabs = new PageDefinitionGroup("Dabs & Notam");
		bundle.definitionGroups.add(dabs);
		dabs.addPageEntry("Dabs Heute", "https://docs.google.com/gview?embedded=true&url=" + this.replaceDateWithOffset("http://www.skyguide.ch/fileadmin/dabs-today/DABS_{XXXX_XXXX}.pdf",0), "false");
		dabs.addPageEntry("Dabs Morgen", "https://docs.google.com/gview?embedded=true&url=" + this.replaceDateWithOffset("http://www.skyguide.ch/fileadmin/dabs-tomorrow/DABS_{YYYY_YYYY}.pdf",1), "false");
		dabs.addPageEntry("Notam", "https://docs.google.com/gview?embedded=true&url=" + this.encodeUrl("http://www.vfr-bulletin.de/pdf/getnotampdf.php?query=NOF:LSSN&call_sign=SAMPLE"), "false");

		PageDefinitionGroup hindernisse = new PageDefinitionGroup("Luftfahrthindernisdaten");
		bundle.definitionGroups.add(hindernisse);
		hindernisse.addPageEntry("Luftfahrthindernisdaten", "http://map.bazl.admin.ch/", "false");

		PageDefinitionGroup webcams = new PageDefinitionGroup("Webcams");
		bundle.definitionGroups.add(webcams);
		webcams.addPageEntry("Webcams", "http://www.webcam-4insiders.com/de/", "false");
		webcams.addPageEntry("Westwind", "http://www.westwind.ch/w_0liv.php", "false");

		PageDefinitionGroup meteostationen = new PageDefinitionGroup("Meteostationen");
		bundle.definitionGroups.add(meteostationen);
		meteostationen.addPageEntry("Wetterstationen Meteomedia", "http://wetterstationen.meteomedia.ch/?map=Schweiz", "false");
		meteostationen.addPageEntry("Messwerte SLF", "http://www.slf.ch/schneeinfo/messwerte/wt-daten/index_DE", "false");
		
		return bundle;
	}

	@Override
	public void definitionLoaded() {
		PageBundle bundle = this.pageDefinitionRepository.readAllDefinitions();

        PreferenceManager.setDefaultValues(this, R.xml.advanced_preferences, false);
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (settings.getBoolean("checkbox_deactivate_defaultpages", false)) {
            bundle = this.pageDefinitionRepository.readCustomDefinitions();
        }

		if(bundle != null)
		{
			this.pageGroups = bundle.definitionGroups;
		}
		
		this.initializeSpinner();
		
	}
	
	class MyAdapter extends ArrayAdapter<String>{
		private String[] strings;
       public MyAdapter(Context context, int textViewResourceId,   String[] objects) {
           super(context, textViewResourceId, objects);
           this.strings = objects;
       }

       @Override
       public View getDropDownView(int position, View convertView,ViewGroup parent) {
           return getCustomView(position, convertView, parent);
       }

       @Override
       public View getView(int position, View convertView, ViewGroup parent) {
           return getCustomView(position, convertView, parent);
       }

       public View getCustomView(int position, View convertView, ViewGroup parent) {
           LayoutInflater inflater=getLayoutInflater();
           View row=inflater.inflate(R.layout.spinner_row, parent, false);
           TextView label=(TextView)row.findViewById(R.id.itemname);
           label.setText(strings[position]);
           
           return row;
           }
       }
}

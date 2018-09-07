package ch.icarosdev.basisrauschmeteo;

import java.util.ArrayList;
import java.util.List;

import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.GridView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import ch.icarosdev.basisrauschmeteo.regtherm.RegthermData;
import ch.icarosdev.basisrauschmeteo.regtherm.RegthermMeteotestDataWorkspace;
import ch.icarosdev.basisrauschmeteo.regtherm.RegthermPageData;
import ch.icarosdev.basisrauschmeteo.regtherm.RegthermRow;
import ch.icarosdev.webviewloadlib.xml.IDefinitionDownloadedListener;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
//import com.google.analytics.tracking.android.EasyTracker;

public class RethermMeteoTestViewFragment extends SherlockFragment implements IDefinitionDownloadedListener {

	public static String ARG_URL_KEY = "url_key";
	public static String ARG_INITIALSCALE_KEY = "initialscale_key";
	public static String ARG_POSTURL_KEY = "posturl";
	public static String ARG_POSTARGS_KEY = "postkey";
	public static String ARG_SHOW_SOURCE_URL = "show_sourceurl";
	public static String TAG = "WebViewLoader";
	
	public static int countLoadingProcesses = 0;
	
	private String urlToopen;
	private String postUrlToOpenBefore;
	private String postArguments;
	private int initialScale = 1;
	
	private boolean errorLoading = false;
	private AsyncLoadWebView asyncLoader;
	
	private String headerRow;
	
	private List<RegthermRow> regthermRows;
	private String basis;
	private String top;
	private double max_wind_val;
	private String max_therm_val;
	private TableLayout tableLayout;
	private TextView dayTitle;
	
	private String appName;
	private IDefinitionDownloadedListener listener;
	
	private ScaleGestureDetector detector;
	private View mainView;
	
	float scaleFactor;
	
	// Remember some things for zooming
	 PointF start = new PointF();
	 PointF mid = new PointF();

	 float oldDist = 1f;
	 PointF oldDistPoint = new PointF();

	 static final int NONE = 0;
	 static final int DRAG = 1;
	 static final int ZOOM = 2;
	 int mode = NONE;
	private ScaleGestureDetector scaleGestureDetector;
	private boolean showSourceUrl;
	private TextView sourceLabel;
	private TextView sourceUrl;
	private TextView cloud_basis;
	private TextView cloud_top;
	private TextView max_wind;
	private TextView mid_therm;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
    	View view = inflater.inflate(R.layout.fragment_regthermview, container, false);
    	this.mainView = view;
    	this.sourceLabel = (TextView) view.findViewById(R.id.source_label);
    	this.sourceUrl = (TextView) view.findViewById(R.id.source_link);
    	
    	this.cloud_basis = (TextView) view.findViewById(R.id.cloud_basis);
    	this.cloud_top = (TextView) view.findViewById(R.id.cloud_top);
    	this.max_wind = (TextView) view.findViewById(R.id.max_wind_kmh);
    	this.mid_therm = (TextView) view.findViewById(R.id.mid_thermal_ms);
    	
    	this.appName = getResources().getString(R.string.app_name);
    	this.listener = this;
    	
    	this.tableLayout = (TableLayout)view.findViewById(R.id.tableLayoutregtherm);
    	View pinchZoomer = view.findViewById(R.id.pinchzoomer);
    	
    	scaleGestureDetector = new ScaleGestureDetector(container.getContext(), new ScaleListener());
    	
    	this.initializePinchZoom(pinchZoomer);
    	
    	this.dayTitle = (TextView)view.findViewById(R.id.retherm_dayTitle);
    	
    	this.regthermRows = new ArrayList<RegthermRow>();
    	
    	this.reloadWebView();
    	return view;
    }

	private void reloadWebView()
    {    	
    	if(this.showSourceUrl){
    		sourceUrl.setText("http://www.flugbasis.ch/index.php/wetter/thermik/regtherm");
    		sourceUrl.scrollTo(0, 0);
    		sourceUrl.invalidate();
    	}
    	else{
    		sourceLabel.setVisibility(View.INVISIBLE);
    		sourceUrl.setVisibility(View.INVISIBLE);
    	}
    	
//		zoom(0,0,1);
    	errorLoading = false;
    	this.resetRegthermList();
    	this.asyncLoader = new AsyncLoadWebView(); 
    	this.asyncLoader.execute("");
    }
    
    private void resetRegthermList() {
    	this.regthermRows = new ArrayList<RegthermRow>();
    	this.tableLayout.removeAllViews();
    	zoom(0, 0, 1);
	}

	@Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible && errorLoading) {
            this.reloadWebView();
        }
        if(!visible && this.asyncLoader != null)
        {
        	try {
        		this.asyncLoader.cancel(true);
        	}
        	catch(Exception e)
        	{
        		Log.e(TAG, e.toString(), e);
        	}
        }
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	setHasOptionsMenu(true);
    	
    	if(savedInstanceState == null)
    	{
    		savedInstanceState = getArguments();
    	}
    	
    	if(savedInstanceState != null){
           this.urlToopen = savedInstanceState.getString(ARG_URL_KEY);
           this.initialScale = savedInstanceState.getInt(ARG_INITIALSCALE_KEY);
           this.postUrlToOpenBefore = savedInstanceState.getString(ARG_POSTURL_KEY);
           this.postArguments = savedInstanceState.getString(ARG_POSTARGS_KEY);
           this.showSourceUrl = savedInstanceState.getBoolean(ARG_SHOW_SOURCE_URL);
    	}
    }
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    	MenuItem menuItem = menu.findItem(21);
    	
    	if(menuItem != null){
	    	menuItem.setOnMenuItemClickListener(new OnMenuItemClickListener() {
				
				@Override
				public boolean onMenuItemClick(MenuItem item) {
	
					reloadWebView();
					return false;
				}
			});
    	}
    }
    
    
    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putString(ARG_URL_KEY, this.urlToopen);
        outState.putString(ARG_POSTURL_KEY, this.postUrlToOpenBefore);
        outState.putString(ARG_POSTARGS_KEY, this.postArguments);
        
        outState.putInt(ARG_INITIALSCALE_KEY, this.initialScale);
        outState.putBoolean(ARG_SHOW_SOURCE_URL, this.showSourceUrl);
    }
    
    private void loadRegthermData() {
    	
    	TableLayout view = tableLayout;
    	LayoutInflater inflater = LayoutInflater.from(view.getContext());
    	this.dayTitle.setText(this.headerRow);
    	int height = 4200;
    	
    	View redRow = new View(view.getContext());
    	//redRow.getLayoutParams().height = 2;
    	redRow.setLayoutParams(new LayoutParams(800, 3));
    	redRow.setBackgroundColor(view.getContext().getResources().getColor(R.color.regtherm_color_Horbar));
    	view.addView(redRow);
    	
    	for (int i = 0; i < 19; i++) {
	    	TableRow tr = (TableRow)inflater.inflate(R.layout.regtherm_tablerow, view, false);
	    	
	    	GridView gridView = (GridView) tr.findViewById(R.id.regtherm_gridview);
	    	gridView.setAdapter(new ImageAdapter(view.getContext(), this.regthermRows, 20-i));
	    	
	    	TextView textView = (TextView) tr.findViewById(R.id.textrowhight);
	    	
	    	textView.setText(height + "m ");
	    	
	    	height = height - 200;
	    	
	    	view.addView(tr);
    	}
    	
    	TableRow tr = (TableRow)inflater.inflate(R.layout.regtherm_tablerow_times, view, false);
    	view.addView(tr);
    	
    	cloud_basis.setText("Basis: " + basis +"m");
    	cloud_top.setText("Top: " + top +"m");
    	max_wind.setText("Max wind: " + max_wind_val + "km/h");
    	mid_therm.setText("Max therm: " + max_therm_val + "m/s");
	}
    
    /// ------------------------------ Loadwebview ---------------------------------
    
    private class AsyncLoadWebView extends AsyncTask<String, Void, String> {

    	private boolean errorOnLoading = false;
		private boolean isLoading;
    	
        @Override
        protected String doInBackground(String... params) {
        	try
        	{
	        	this.isLoading = false;
	        	
	        	RegthermMeteotestDataWorkspace workspace = RegthermMeteotestDataWorkspace.getInstance().initialize(appName, urlToopen, listener, postUrlToOpenBefore, postArguments);
	        	RegthermData data = workspace.loadRegthermData();
	        	RegthermPageData pageData = data.findPageData(urlToopen + " " + postArguments);
	        	
	        	if(pageData != null)
	        	{
	        		headerRow = pageData.headerRow;
	        		regthermRows = pageData.regthermRows;
	        		top = pageData.top;
	        		basis = pageData.basis;
	        		max_wind_val = pageData.maxwindkmh;
	        		max_therm_val = pageData.maxtherm;
	        	}
        	}
        	catch(Exception e)
        	{
        		Log.e(TAG, e.toString(), e);
        	}
        	
        	return null;
        }


		@Override
        protected void onPostExecute(String result) {
         	try
        	{
        		countLoadingProcesses --;
        		loadRegthermData();
        		if(countLoadingProcesses <= 0){
        			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
        		}
        	}
        	catch(Exception e)
        	{
        		Log.e(TAG, e.toString(), e);
        	}
        }
		
		@Override
		protected void onCancelled() {
			super.onCancelled();
			try
        	{
        		countLoadingProcesses --;
        		if(countLoadingProcesses <= 0){
        			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
        		}
        	}
        	catch(Exception e)
        	{
        		Log.e(TAG, e.toString(), e);
        	}
		}
		
		@Override
		protected void onCancelled(String result) {
			// TODO Auto-generated method stub
			super.onCancelled(result);
			try
        	{
        		countLoadingProcesses --;
        		if(countLoadingProcesses <= 0){
        			getSherlockActivity().setSupportProgressBarIndeterminateVisibility(false);
        		}
        	}
        	catch(Exception e)
        	{
        		Log.e(TAG, e.toString(), e);
        	}
		}

        @Override
        protected void onPreExecute() {
        	try
        	{
	        	getSherlockActivity().setSupportProgressBarIndeterminateVisibility(true);
	        	countLoadingProcesses ++;
        	}
        	catch(Exception e)
        	{
        		Log.e(TAG, e.toString(), e);
        	}
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
  }
    
// ------------------------------------------- SSL Stuff -------------------------------------------------

	@Override
	public void definitionLoaded() {
		//reloadWebView();
	}
	
	// ----------------------------------------------------------------- Zooming:
	
	private void initializePinchZoom(View view) {
		view.setOnTouchListener(new OnTouchListener() {
			   @Override
			   public boolean onTouch(View v, MotionEvent event) {
				   scaleGestureDetector.onTouchEvent(event);
				   return true;
			   }});
		
//		wiev.setOnTouchListener(new OnTouchListener() {
//			   @Override
//			   public boolean onTouch(View v, MotionEvent event) {
//			    Log.d(TAG, "mode=DRAG");
//			    switch (event.getAction() & MotionEvent.ACTION_MASK) {
//			    case MotionEvent.ACTION_DOWN:
//			     start.set(event.getX(), event.getY());
//			     Log.d(TAG, "mode=DRAG");
//			     mode = DRAG;
//
//			     break;
//			    case MotionEvent.ACTION_POINTER_DOWN:
//			     oldDist = spacing(event);
//			     oldDistPoint = spacingPoint(event);
//			     Log.d(TAG, "oldDist=" + oldDist);
//			     if (oldDist > 10f) {
//			      midPoint(mid, event);
//			      mode = ZOOM;
//			      Log.d(TAG, "mode=ZOOM");
//			     }
//			     break;// return !gestureDetector.onTouchEvent(event);
//			    case MotionEvent.ACTION_UP:
//			    case MotionEvent.ACTION_POINTER_UP:
//			     Log.d(TAG, "mode=NONE");
//			     mode = NONE;
//			     break;
//			    case MotionEvent.ACTION_MOVE:
//			     if (mode == DRAG) {
//			    	 //zoom(0f, 0f, start);
//			     } else if (mode == ZOOM) {
//			      float newD = spacing(event);
//			      float scale = newD / oldDist;
//			      zoom(scale, scale, start);
//			     }
//			     break;
//			    }
//			    return true;
//			   }
//			  });
	}
	
	/** 
	  * zooming is done from here 
	  */
	 public void zoom(float posx, float posy, float scale) {
		 Log.d(TAG, "scale: " + scale);

	  if(scale != 0){
		  mainView.setScaleX(scale);
		  mainView.setScaleY(scale);
	  }
	 }

	 /**
	  * space between the first two fingers
	  */
	 private float spacing(MotionEvent event) {
	  // ...
	  float x = event.getX(0) - event.getX(event.getPointerCount()-1);
	  float y = event.getY(0) - event.getY(event.getPointerCount()-1);
	  return FloatMath.sqrt(x * x + y * y);
	 }

	 private PointF spacingPoint(MotionEvent event) {
	  PointF f = new PointF();
	  f.x = event.getX(0) - event.getX(event.getPointerCount()-1);
	  f.y = event.getY(0) - event.getY(event.getPointerCount()-1);
	  return f;
	 }

	 /**
	  * the mid point of the first two fingers
	  */
	 private void midPoint(PointF point, MotionEvent event) {
	  // ...
	  float x = event.getX(0) + event.getX(event.getPointerCount()-1);
	  float y = event.getY(0) + event.getY(event.getPointerCount()-1);
	  point.set(x / 2, y / 2);
	 }
    
	 private class ScaleListener extends
     ScaleGestureDetector.SimpleOnScaleGestureListener {
   @Override
   public boolean onScale(ScaleGestureDetector detector) {
     scaleFactor *= detector.getScaleFactor();
     float posx = detector.getFocusX();
     float posy = detector.getFocusY();
     // Don't let the object get too small or too large.
     scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));
     
     zoom(posx,  posy, scaleFactor);
	  
     return true;
   }
 }
	 
}

package ch.icarosdev.webviewloadlib;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
//import android.view.ViewGroup.LayoutParams;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class WebViewFragment extends SherlockFragment {

    public static String ARG_URL_KEY = "url_key";
    public static String ARG_INITIALSCALE_KEY = "initialscale_key";
    public static String ARG_SHOW_SOURCE_URL = "show_sourceurl";
    public static String ARG_POSTURL_KEY = "posturl";
    public static String ARG_POSTARGS_KEY = "postkey";
    public static String TAG = "WebViewLoader";
    private String urlToopen;
    private String postUrlToOpenBefore;
    private String postArguments;
    private int initialScale = 1;
    private WebView webview;
    private boolean errorLoading = false;
    private WebViewLoader asyncLoader;
    private boolean showSourceUrl;
    private TextView sourceLabel;
    private TextView sourceUrl;
    private View view;
    private SherlockFragmentActivity sherlockActivity;
    private ConnectivityManager connectivityManager;
    private SharedPreferences settings;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        this.view = inflater.inflate(R.layout.fragment_webview, container, false);
        this.sherlockActivity = getSherlockActivity();
        this.connectivityManager = (ConnectivityManager) sherlockActivity.getSystemService(Activity.CONNECTIVITY_SERVICE);
        this.sourceLabel = (TextView) view.findViewById(R.id.source_label);
        this.sourceUrl = (TextView) view.findViewById(R.id.source_link);

        this.webview = (WebView) this.view.findViewById(R.id.webview);
        this.settings = PreferenceManager.getDefaultSharedPreferences(this.sherlockActivity);

        this.reloadWebView();
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        this.webview.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ONLY);
        this.webview.onPause();
//        this.stopWebViewRunning();
//        this.webview.removeAllViews();
//        this.webview.clearHistory();
//        this.webview.destroy();
    }

    @Override
    public void onStop() {
        super.onStop();
        this.stopWebViewRunning();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.stopWebViewRunning();
        this.webview.removeAllViews();
        this.webview.clearHistory();
        this.webview.destroy();
    }

    private void stopWebViewRunning() {

        this.webview.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ONLY);
        this.webview.stopLoading();
        this.webview.removeAllViews();
        this.webview.clearHistory();
        this.webview.loadData("", "text/html", "utf-8");
        this.webview.reload();

        this.webview.setWebChromeClient(null);
        this.webview.setWebViewClient(null);
    }

    private void reloadWebView() {

        if (this.showSourceUrl) {
            sourceUrl.setText(this.urlToopen);
            sourceUrl.scrollTo(0, 0);
            sourceUrl.invalidate();
        } else {
            sourceLabel.setVisibility(View.INVISIBLE);
            sourceUrl.setVisibility(View.INVISIBLE);
        }

        errorLoading = false;
        this.asyncLoader = new WebViewLoader();
        this.asyncLoader.loadWebView();
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible && errorLoading) {
            this.reloadWebView();
        }
//        if (!visible && this.asyncLoader != null) {
//            try {
//                this.asyncLoader.abortLoad();
//            } catch (Exception e) {
//                Log.e(TAG, e.toString(), e);
//            }
//        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        if (savedInstanceState == null) {
            savedInstanceState = getArguments();
        }

        if (savedInstanceState != null) {
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

        if (menuItem != null) {
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_URL_KEY, this.urlToopen);
        outState.putString(ARG_POSTURL_KEY, this.postUrlToOpenBefore);
        outState.putString(ARG_POSTARGS_KEY, this.postArguments);

        outState.putInt(ARG_INITIALSCALE_KEY, this.initialScale);
        outState.putBoolean(ARG_SHOW_SOURCE_URL, this.showSourceUrl);
    }

    private class WebViewLoader {

        private final String appCachePath;
        private WebView webViewLoading;
        private boolean loginExecuted;
        private boolean toastNoConnectionShown;

        public WebViewLoader() {
            this.webViewLoading = webview;
            this.appCachePath = sherlockActivity.getCacheDir().getAbsolutePath();
        }

        public void loadWebView() {
            try {
                WebSettings webSettings = this.webViewLoading.getSettings();
                webSettings.setJavaScriptEnabled(true);
                webSettings.setAppCacheEnabled(true);
                webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
                webSettings.setAppCachePath(appCachePath);
                webSettings.setBuiltInZoomControls(true);
                webSettings.setSupportZoom(true);
                webSettings.setLoadWithOverviewMode(false);
                webSettings.setUseWideViewPort(true);
                webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);

                this.webViewLoading.setInitialScale(initialScale);
                this.webViewLoading.setWebChromeClient(new WebChromeClient() {
                    @Override
                    public void onShowCustomView(View view, CustomViewCallback callback) {
                        super.onShowCustomView(view, callback);
                        if (view instanceof FrameLayout) {
                            FrameLayout frame = (FrameLayout) view;
                            if (frame.getFocusedChild() instanceof VideoView) {
                                VideoView video = (VideoView) frame.getFocusedChild();
                                frame.removeView(video);
                                video.start();
                            }
                        }
                    }
                });

                this.webViewLoading.setWebChromeClient(new WebChromeClient() {

                    @Override
                    public void onProgressChanged(WebView view, int progress) {
                        super.onProgressChanged(view, progress);
                        // Activities and WebViews measure progress with different scales.
                        // The progress meter will automatically disappear when we reach 100%
                        try {
                            sherlockActivity.setProgress(progress * 100);
                        }catch (Exception e){
                            //Log.w(TAG, e.toString(), e);
                        }
                    }
                });

                this.webViewLoading.setWebViewClient(new WebViewClient() {

                    @Override
                    public void onPageStarted(WebView view, String url, Bitmap favicon) {
                        sherlockActivity.setSupportProgressBarIndeterminateVisibility(true);
                        super.onPageStarted(view, url, favicon);
                    }

                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        if(loginExecuted){
                            try {
                                loginExecuted = false;
                                setConnectivitySettings();
                                webViewLoading.loadUrl(urlToopen);
                            } catch (Exception e) {
                                Log.w(TAG, e.toString(), e);
                            }
                        }

//                        webViewLoading.loadUrl("javascript:(function() { " +
//                                "document.getElementsByTagName('body')[0].style.color = 'red'; " +
//                                "})()");

//                        webViewLoading.loadUrl("javascript:(function() { " +
//                                "document.getElementsByTagName('body')[0].style.color = 'red'; " +
//                                "var m=document.getElementsByTagName('meta');"+
//                                "for(var c=0;c<m.length;c++) {"+
//                                "if(m[c].content == 'no-cache'){"+
//                                "m[c].content='public';"+
//                                "m[c].name='Cache-control';}}"+
//                                "//alert(m[c].content);}"+
//                                "})()");

//                        webview.loadUrl("javascript:(function() { " +
//                                "document.getElementsByTagName('header')[0].style.display=\"none\"; " +
//                                "})()");

                        sherlockActivity.setSupportProgressBarIndeterminateVisibility(false);
                    }

                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        // YouTube video link
                        if(!url.equals(urlToopen)) {
                            if (url.contains("youtube.com")) {
                                int n = url.indexOf("?");
                                if (n > 0) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("http://www.youtube.com/v/%s", url.substring("vnd.youtube:".length(), n)))));
                                }
                                return true;
                            }
                            if (url.endsWith(".pdf")) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                                return true;
                            }
                        }

                        return false;
                    }

                    @Override
                    public void onReceivedSslError(WebView view, android.webkit.SslErrorHandler handler, android.net.http.SslError error) {
                        handler.proceed();
                    }

                    @Override
                    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                        try {
                            errorLoading = true;

                            if(!toastNoConnectionShown) {
                                toastNoConnectionShown = true;
                                Toast.makeText(view.getContext(), getString(R.string.err_no_internetconnection) + description, Toast.LENGTH_SHORT).show();
                                webViewLoading.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ONLY);
                                webViewLoading.loadUrl(urlToopen);
                            }
                            else{
                                sherlockActivity.setSupportProgressBarIndeterminateVisibility(false);
                            }
                        } catch (Exception e) {
                            Log.w(TAG, e.toString(), e);
                        }
                    }
                });

                this.toastNoConnectionShown = false;

                if(this.checkLoadFromCache()) {
                    this.executeLoginProcedure(this.webViewLoading);
                    if (!loginExecuted) {
                        if(this.webViewLoading.getUrl() != null){
                            webViewLoading.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
//                            this.webViewLoading.reload();
                            this.webViewLoading.loadUrl(urlToopen);
                        }
                        else {
                            this.setConnectivitySettings();
                            this.webViewLoading.loadUrl(urlToopen);
                        }
                    }
                }
                else{
                    Toast.makeText(view.getContext(), getString(R.string.warn_no_internetconnection), Toast.LENGTH_SHORT).show();

                    this.webViewLoading.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ONLY);
                    this.webViewLoading.loadUrl(urlToopen);

                    this.toastNoConnectionShown = true;
                }

            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }
        }

        private void setConnectivitySettings() {
            try {
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                int type = networkInfo.getType();

                switch (type){
                    case ConnectivityManager.TYPE_WIFI:{
                        webViewLoading.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                        break;
                    }
                    case ConnectivityManager.TYPE_ETHERNET:{
                        webViewLoading.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                        break;
                    }
                    default:{
                        webViewLoading.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
                    }
                }

                return;
            }
            catch (Exception e){
                Log.e(TAG, e.toString(), e);
            }

            webViewLoading.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);

        }

        private boolean checkLoadFromCache() {
            boolean loadFromCache = settings.getBoolean("checkbox_load_from_cache", false);
            return !loadFromCache && connectivityManager != null && connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
        }

        public void abortLoad() {
            try {
                if(webview != null)
                {
                    webview.stopLoading();
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }
        }

        private void executeLoginProcedure(WebView webViewLoading) {
            if (postUrlToOpenBefore != null && postUrlToOpenBefore.length() > 0) {
                String[] urlsExecuteBefore = postUrlToOpenBefore.split(":#_#:");
                String[] urlPostArguments = new String[urlsExecuteBefore.length];

                if (postArguments != null && postArguments.length() > 0) {
                    urlPostArguments = postArguments.split(":#_#:");
                }

                int i = 0;
                for (String urlToExecute : urlsExecuteBefore) {
                    this.loginExecuted = true;
                    String arguments = "";
                    if (urlPostArguments.length > i) {
                        arguments = urlPostArguments[i];
                    }

                    if (arguments != null && arguments.length() > 0) {
                        webViewLoading.postUrl(urlToExecute, arguments.getBytes());
                    } else {
                        webViewLoading.loadUrl(urlToExecute);
                    }
                }
            }
        }
    }
}

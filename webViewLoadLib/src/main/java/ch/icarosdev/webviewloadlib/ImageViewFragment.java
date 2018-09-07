package ch.icarosdev.webviewloadlib;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import ch.icarosdev.imageviewlib.PanAndZoomListener;
import ch.icarosdev.imageviewlib.PanAndZoomListener.Anchor;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.MenuItem.OnMenuItemClickListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;

public class ImageViewFragment extends SherlockFragment {

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
    private ImageView imageView;
    private Bitmap bmp;
    private View view;

    private boolean errorLoading = false;
    private boolean showSourceUrl;
    private TextView sourceLabel;
    private TextView sourceUrl;
    private AsyncLoadWebView asyncLoader;
    private SherlockFragmentActivity sherlockActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_imageview, container, false);
        this.sherlockActivity = getSherlockActivity();
        this.sourceLabel = (TextView) view.findViewById(R.id.source_label);
        this.sourceUrl = (TextView) view.findViewById(R.id.source_link);

        this.imageView = (ImageView) view.findViewById(R.id.image);
        this.reloadImageView();
        return view;
    }

    private void reloadImageView() {
        this.replaceImage();
        if (this.showSourceUrl) {
            sourceUrl.setText(this.urlToopen);
            sourceUrl.scrollTo(0, 0);
            sourceUrl.invalidate();
        } else {
            sourceLabel.setVisibility(View.INVISIBLE);
            sourceUrl.setVisibility(View.INVISIBLE);
        }
        errorLoading = false;
        this.asyncLoader = new AsyncLoadWebView();
        this.asyncLoader.execute("");
    }

    @Override
    public void setMenuVisibility(final boolean visible) {
        super.setMenuVisibility(visible);
        if (visible && errorLoading) {
            this.reloadImageView();
        }
        if (!visible && this.asyncLoader != null) {
            try {
                this.asyncLoader.abortLoad();
                this.asyncLoader = null;
            } catch (Exception e) {
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(ARG_URL_KEY, this.urlToopen);
        outState.putString(ARG_POSTURL_KEY, this.postUrlToOpenBefore);
        outState.putString(ARG_POSTARGS_KEY, this.postArguments);
        outState.putInt(ARG_INITIALSCALE_KEY, this.initialScale);
        outState.putBoolean(ARG_SHOW_SOURCE_URL, this.showSourceUrl);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem menuItemRefresh = menu.findItem(21);
        MenuItem menuItemShare = menu.findItem(12);
        if (menuItemRefresh != null) {
            menuItemRefresh.setOnMenuItemClickListener(new OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    reloadImageView();
                    return false;
                }
            });
        }

        if (menuItemShare != null) {
            menuItemShare.setOnMenuItemClickListener(new OnMenuItemClickListener() {

                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    shareImage();
                    return false;
                }
            });
        }
    }

    private void shareImage() {
        AsyncSharePicture share = new AsyncSharePicture();
        share.execute("");
    }

    private void replaceImage() {
        try {
            if (bmp != null) {
                if (sherlockActivity != null) {
                    sherlockActivity.setSupportProgressBarIndeterminateVisibility(true);
                }

                imageView.setImageBitmap(bmp);
                view.setOnTouchListener(new PanAndZoomListener(view, imageView, Anchor.TOPLEFT));
                imageView.invalidate();
                imageView.refreshDrawableState();
                imageView.setVisibility(View.VISIBLE);

                if (sherlockActivity != null) {
                    sherlockActivity.setSupportProgressBarIndeterminateVisibility(false);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }

    }

    private class AsyncSharePicture extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                java.util.Date date = new java.util.Date();
                String timeStampNow = (new Timestamp(date.getTime())).toString();
                timeStampNow = timeStampNow.replaceAll(" ", "");
                timeStampNow = timeStampNow.replaceAll(":", "");
                File imageFile = new File(path, timeStampNow + "BeoWebcam.png");
                FileOutputStream fileOutPutStream = new FileOutputStream(imageFile);
                Bitmap bitmap = bmp;
                bitmap.compress(Bitmap.CompressFormat.PNG, 80, fileOutPutStream);

                fileOutPutStream.flush();
                fileOutPutStream.close();

//         		return Uri.parse("file://" + imageFile.getAbsolutePath());
                return imageFile.getAbsolutePath();
            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                if (result != null) {
                    Uri uri = Uri.parse("file://" + result);
                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);

                    shareIntent.setType("image/png");

                    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);

                    startActivity(Intent.createChooser(shareIntent, "send"));
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }
        }

    }


    private class AsyncLoadWebView extends AsyncTask<String, Void, String> {

        private URLConnection currentConnection;

        public void abortLoad() {
            try {
                this.cancel(true);
            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }
        }

        @Override
        protected String doInBackground(String... params) {
            InputStream stream = null;
            try {
                URL url = new URL(urlToopen);
                this.currentConnection = url.openConnection();
                this.publishProgress();
                stream = this.currentConnection.getInputStream();
                this.publishProgress();
                bmp = BitmapFactory.decodeStream(stream);
            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        Log.e(TAG, e.toString(), e);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            try {
                countLoadingProcesses--;
                if (countLoadingProcesses <= 0) {
                    sherlockActivity.setSupportProgressBarIndeterminateVisibility(false);
                }
                replaceImage();
            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }
        }

        @Override
        protected void onPreExecute() {
            try {
                sherlockActivity.setSupportProgressBarIndeterminateVisibility(true);
                countLoadingProcesses++;
                if (bmp == null) {
                    imageView.setVisibility(View.GONE);
                }

            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            replaceImage();
        }
    }
}

package ch.icarosdev.webviewloadlib.xml;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created with IntelliJ IDEA.
 * User: Florian
 * Date: 27.10.13
 * Time: 14:34
 * To change this template use File | Settings | File Templates.
 */
public class AsyncDownloadFile extends AsyncTask<String, Void, IDefinitionDownloadedListener> {

    private static final String TAG = "XML Serializer";
    private IDefinitionDownloadedListener definitionDownloadedListener;

    public AsyncDownloadFile(IDefinitionDownloadedListener definitionDownloadedListener) {
        this.definitionDownloadedListener = definitionDownloadedListener;
    }

    @Override
    protected IDefinitionDownloadedListener doInBackground(String... params) {
        try {
            URL url = new URL(params[0]);
            URLConnection connection = url.openConnection();
            connection.connect();

            // download the file
            InputStream input = new BufferedInputStream(url.openStream());
            OutputStream output = new FileOutputStream(params[1]);

            byte data[] = new byte[1024];
            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }

            output.flush();
            output.close();
            input.close();
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }

        return definitionDownloadedListener;
    }

    @Override
    protected void onPostExecute(IDefinitionDownloadedListener result) {
        try {
            if (result != null) {
                result.definitionLoaded();
            }
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
    }
}

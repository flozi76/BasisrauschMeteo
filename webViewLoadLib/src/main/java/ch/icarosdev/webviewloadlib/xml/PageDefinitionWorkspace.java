package ch.icarosdev.webviewloadlib.xml;

import android.os.Environment;
import android.util.Log;
import ch.icarosdev.webviewloadlib.domain.PageBundle;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;

public class PageDefinitionWorkspace {

    private static final String TAG = "XML Serializer";
    private static PageDefinitionWorkspace pageDefinitionWorkspace;

    private boolean isInitialized = false;
    private String applicationName = "";
    private String fileName;

    public PageDefinitionWorkspace(String applicationName, String fileName) {
        this.applicationName = applicationName;
        this.fileName = fileName;
        this.isInitialized = true;
    }

    public PageBundle readFile() {
        try {
            if (!isInitialized) {
                return new PageBundle();
            }

            File xmlFile = new File(getPath());
            if (!xmlFile.exists()) {
                return new PageBundle();
            }

            Serializer serializer = new Persister();
            PageBundle pageBundle = serializer.read(PageBundle.class, xmlFile);
            return pageBundle;

        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }

        return null;
    }

    public void persistFile(PageBundle bundle) {
        try {
            if (!isInitialized) {
                return;
            }

            File xmlFile = new File(getPath());

            if (xmlFile.exists()) {
                xmlFile.delete();
            }

            this.initializeDir();

            Serializer serializer = new Persister();
            serializer.write(bundle, xmlFile);
        } catch (Exception e) {
            Log.e(TAG, e.toString(), e);
        }
    }

    public void initializeDir() {
        File dir = new File(Environment.getExternalStorageDirectory()
                + File.separator + this.applicationName);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public String getPath() {
        return Environment.getExternalStorageDirectory() + File.separator
                + this.applicationName + File.separator + this.fileName;
    }

    public void deleteFile() {
        File xmlFile = new File(getPath());
        if (xmlFile.exists()) {
            xmlFile.delete();
        }
    }
}

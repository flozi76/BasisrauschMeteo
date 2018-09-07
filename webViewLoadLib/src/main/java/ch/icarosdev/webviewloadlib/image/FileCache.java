package ch.icarosdev.webviewloadlib.image;

import android.content.Context;

import java.io.File;
import java.util.Calendar;

public class FileCache {

    private File cacheDir;

    public FileCache(Context context, boolean useCacheDir) {
        //Find the dir to save cached images
        if (!useCacheDir && android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "CacheBeoWebcams");
        else
            cacheDir = context.getCacheDir();
        if (!cacheDir.exists())
            cacheDir.mkdirs();
    }

    public File getFile(String url, boolean updateDayly) {
        //I identify images by hashcode. Not a perfect solution, good for the demo.
        String filename = String.valueOf(url.hashCode());

        if (updateDayly) {
            int day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            filename = day + "_" + filename;
        }
        //Another possible solution (thanks to grantland)
        //String filename = URLEncoder.encode(url);
        File f = new File(cacheDir, filename);
        return f;

    }

    public void clear() {
        File[] files = cacheDir.listFiles();
        if (files == null)
            return;
        for (File f : files)
            f.delete();
    }

}
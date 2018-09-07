package ch.icarosdev.webviewloadlib.image;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import ch.icarosdev.webviewloadlib.R;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {

    MemoryCache memoryCache = new MemoryCache();
    FileCache fileCache;
    ExecutorService executorService;
    private Map<View, String> imageViews = Collections.synchronizedMap(new WeakHashMap<View, String>());
    private Context context;
    private boolean updateDayly;
    private int stub_id = R.drawable.device_access_video;

    public ImageLoader(Context context, boolean updateDayly, boolean useOsCacheDir) {
        this.context = context;
        fileCache = new FileCache(context, useOsCacheDir);
        this.updateDayly = updateDayly;
        executorService = Executors.newFixedThreadPool(5);
    }

    public void DisplayImage(String url, View imageView, int defaultImage) {
        imageViews.put(imageView, url);
        Bitmap bitmap = memoryCache.get(url);
        stub_id = defaultImage;

        this.loadImageToView(bitmap, url, imageView);

        // this.addReflectionToView(bitmap, (ImageView)imageView);
    }

    private void addReflectionToView(Bitmap originalImage, ImageView imageViewReflection) {
        if (imageViewReflection == null) {
            return;
        }

        //The gap we want between the reflection and the original image
        final int reflectionGap = 4;
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        //This will not scale but will flip on the Y axis

        if (width <= 0 || height <= 0) {
            return;
        }

        Matrix matrix = new Matrix();
        matrix.preScale(1, -1);
        //Create a Bitmap with the flip matix applied to it.

        Bitmap reflectionImage = Bitmap.createBitmap(originalImage, 0, height / 2, width, height / 2, matrix, false);

        //Create a new bitmap with same width but taller to fit reflection

        Bitmap bitmapWithReflection = Bitmap.createBitmap(width, (height + height / 2), Config.ARGB_8888);


        //Create a new Canvas with the bitmap that's big enough for

        //the image plus gap plus reflection
        Canvas canvas = new Canvas(bitmapWithReflection);

        //Draw in the original image

        canvas.drawBitmap(originalImage, 0, 0, null);

        //Draw in the gap

        Paint deafaultPaint = new Paint();

        canvas.drawRect(0, height, width, height + reflectionGap, deafaultPaint);

        //Draw in the reflection

        canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);


        //Create a shader that is a linear gradient that covers the reflection

        Paint paint = new Paint();

        LinearGradient shader = new LinearGradient(0, originalImage.getHeight(), 0,

                bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff, 0x00ffffff, TileMode.CLAMP);

        //Set the paint to use this shader (linear gradient)

        paint.setShader(shader);

        //Set the Transfer mode to be porter duff and destination in

        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));

        //Draw a rectangle using the paint with our linear gradient

        canvas.drawRect(0, height, width,

                bitmapWithReflection.getHeight() + reflectionGap, paint);

        //Create an Image view and add our bitmap with reflection to it

        imageViewReflection.setImageBitmap(bitmapWithReflection);

    }

    private void loadImageToView(Bitmap bitmap, String url, View imageView) {
        if (bitmap != null) {
            if (imageView instanceof TextView) {
                TextView textView = (TextView) imageView;
                Drawable drawable = new BitmapDrawable(this.context.getResources(), bitmap);
                textView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
            } else {
                ImageView imgView = (ImageView) imageView;
                imgView.setImageBitmap(bitmap);
                this.addReflectionToView(bitmap, (ImageView) imageView);
            }


        } else {
            if (url != null) {
                queuePhoto(url, imageView);
            }

            if (imageView instanceof TextView) {
                TextView textView = (TextView) imageView;
                textView.setCompoundDrawablesWithIntrinsicBounds(0, 0, stub_id, 0);
            } else {
                ImageView imgView = (ImageView) imageView;
                imgView.setImageResource(stub_id);

                Bitmap originalImage = BitmapFactory.decodeResource(imageView.getContext().getResources(), stub_id);
                this.addReflectionToView(originalImage, (ImageView) imageView);
            }


        }

    }

    private void queuePhoto(String url, View imageView) {
        PhotoToLoad p = new PhotoToLoad(url, imageView);
        executorService.submit(new PhotosLoader(p));
    }

    private Bitmap getBitmap(String url) {
        File f = fileCache.getFile(url, updateDayly);

        //from SD cache
        Bitmap b = decodeFile(f);
        if (b != null)
            return b;

        //from web
        try {
            Bitmap bitmap = null;
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            Utils.CopyStream(is, os);
            os.close();
            bitmap = decodeFile(f);
            return bitmap;
        } catch (Throwable ex) {
            ex.printStackTrace();
            if (ex instanceof OutOfMemoryError)
                memoryCache.clear();
            return null;
        }
    }

    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f) {
        try {
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1 = new FileInputStream(f);
            BitmapFactory.decodeStream(stream1, null, o);
            stream1.close();

            //Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = 300;
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            FileInputStream stream2 = new FileInputStream(f);
            Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
            stream2.close();
            return bitmap;
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    boolean imageViewReused(PhotoToLoad photoToLoad) {
        String tag = imageViews.get(photoToLoad.imageView);
        if (tag == null || !tag.equals(photoToLoad.url))
            return true;
        return false;
    }

    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }

    //Task for the queue
    private class PhotoToLoad {
        public String url;
        public View imageView;

        public PhotoToLoad(String u, View i) {
            url = u;
            imageView = i;
        }
    }

    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;

        PhotosLoader(PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
        }

        @Override
        public void run() {
            if (imageViewReused(photoToLoad))
                return;
            Bitmap bmp = getBitmap(photoToLoad.url);
            memoryCache.put(photoToLoad.url, bmp);
            if (imageViewReused(photoToLoad))
                return;
            BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad, context);
            Activity a = (Activity) photoToLoad.imageView.getContext();
            a.runOnUiThread(bd);
        }
    }

    //Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;
        Context context;

        public BitmapDisplayer(Bitmap b, PhotoToLoad p, Context context) {
            bitmap = b;
            photoToLoad = p;
            this.context = context;
        }

        public void run() {
            try {
                if (imageViewReused(photoToLoad))
                    return;

                loadImageToView(bitmap, null, photoToLoad.imageView);
//            if(bitmap!=null)
//            {
//            	Drawable drawable = new BitmapDrawable(this.context.getResources(), bitmap);
//            	photoToLoad.imageView.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null);
//            }
//            else
//            {
//            	photoToLoad.imageView.setCompoundDrawablesWithIntrinsicBounds(0, 0, stub_id, 0);
//            }
            } catch (Exception ex) {
                // do nothing
            }
        }
    }

}

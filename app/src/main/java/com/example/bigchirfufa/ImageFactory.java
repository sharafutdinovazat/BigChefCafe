package com.example.bigchirfufa;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayDeque;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

public class ImageFactory {

    View root_view;
    FileCache cache;
    ExecutorService es;
    ArrayDeque<Pair<DownloadTask, String>> tasks;

    private Timer timer;

    boolean is_download;

    public ImageFactory(View root_view, Context context) {
        this.root_view = root_view;
        cache = new FileCache(context);
        tasks = new ArrayDeque<Pair<DownloadTask, String>>();
        is_download = false;
        timer = new Timer();
    }

    public void load_image(String url)
    {
        for (Pair<DownloadTask, String> pair_task: tasks) {
            if (pair_task.second.equals(url))
            {
                return;
            }
        }
        File file = cache.getFile(url);
        if (file.exists()) return;
        DownloadTask task = new DownloadTask(file, url, null);
        tasks.addLast(new Pair<DownloadTask, String>(task, url));
        if (is_download) return;
        tasks.getFirst().first.execute(tasks.getFirst().second);
        is_download = true;
    }

    void set_image(ImageView view, String url)
    {
        File file = cache.getFile(url);
        if (!file.exists())
        {
            file.delete();
            DownloadTask task = new DownloadTask(file, url, view);
            tasks.addLast(new Pair<DownloadTask, String>(task, url));
            if (is_download) return;
            tasks.getFirst().first.execute(tasks.getFirst().second);
            return;
        }
        Bitmap bit = BitmapFactory.decodeFile(file.getPath());
        if (bit == null)
        {
            file.delete();
            DownloadTask task = new DownloadTask(file, url, view);
            tasks.addLast(new Pair<DownloadTask, String>(task, url));
            if (is_download) return;
            tasks.getFirst().first.execute(tasks.getFirst().second);
            return;
        }
        view.setImageBitmap(bit);
    }



    void next_download(Bitmap bitmap)
    {
        if (tasks.isEmpty())
        {
            is_download = false;
            return;
        }

        DownloadTask task = tasks.getFirst().first;
        if (task.view != null)
        {
            if (bitmap != null)
            {
                task.view.setImageBitmap(bitmap);
            }
        }
        tasks.poll();
        if (tasks.isEmpty())
        {
            is_download = false;
            return;
        }
        tasks.getFirst().first.execute(tasks.getFirst().second);
        is_download = true;
    }

    void re_download(String url, ImageView view)
    {
        File file = cache.getFile(url);
        DownloadTask task = new DownloadTask(file, url, view);
        tasks.addLast(new Pair<DownloadTask, String>(task, url));
    }

    class DownloadTask extends AsyncTask<String, Void, Bitmap> {

        String url;
        File file;
        ImageView view;

        public DownloadTask(File file, String url, ImageView view) {
            this.file = file;
            this.url = url;
            this.view = view;
        }

        private Bitmap loadBitmap(String url)
        {
            Bitmap bitmap;
            FileOutputStream fOut;
            FileOutputStream fOut_big;
            File big_file = cache.getFile(url + "big");
            try {
                url = "http://bigchefufa.ru" + url;
                URLConnection conn = new URL(url).openConnection();
                conn.connect();
                fOut = new FileOutputStream(file);
                fOut_big = new FileOutputStream(big_file);
                bitmap = BitmapFactory.decodeStream(conn.getInputStream());

            }
            catch (IOException e)
            {
                e.printStackTrace();
                file.delete();
                return null;
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            if (bitmap == null)
            {
                file.delete();
                return null;
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, 10, fOut_big);
            try {
                fOut_big.flush();
                fOut_big.close();
            }
            catch (IOException e)
            {
                big_file.delete();
                e.printStackTrace();
                return null;
            }
            bitmap.compress(Bitmap.CompressFormat.JPEG, 10, stream);
            bitmap.recycle();
            byte[] byteArray = stream.toByteArray();
            Bitmap compressed_bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeByteArray(byteArray, 0 , byteArray.length), 300, 250, false);
            compressed_bitmap.compress(Bitmap.CompressFormat.JPEG, 10, fOut);
            try {
                fOut.flush();
                fOut.close();
            }
            catch (IOException e)
            {
                file.delete();
                e.printStackTrace();
                return null;
            }
            return compressed_bitmap;
        }

        @Override
        protected Bitmap doInBackground( String... params) {

            return loadBitmap(params[0]);
        }

        @Override
        protected void onPostExecute(Bitmap bitmap)
        {
            Log.w("Suak blyat'", "Картинка скачалась");
            if (bitmap == null)
            {
                re_download(url, view);
            }
            next_download(bitmap);
        }
    }

    class DownloadTimer extends TimerTask{

        private String url;
        private File file;
        private ImageView view;

        public DownloadTimer(File file, String url, ImageView view)
        {
            this.url = url;
            this.file = file;
            this.view = view;
        }

        @Override
        public void run() {
            DownloadTask task = new DownloadTask(file, url, view);
            tasks.addLast(new Pair<DownloadTask, String>(task, url));
            if (is_download) return;
            tasks.getFirst().first.execute(tasks.getFirst().second);
            is_download = true;
        }
    }
}


class FileCache {

    private File cacheDir;

    public FileCache(Context context) {
        //Find the dir to save cached images
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "TTImages_cache");
        else
            cacheDir = context.getCacheDir();
        if (!cacheDir.exists())
            cacheDir.mkdirs();
    }

    public File getFile(String url) {

        String filename = String.valueOf(url.hashCode());

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

package com.example.bigchirfufa;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;

public class ImageFactory {

    Context context;

    public ImageFactory(View root_view, Context context) {
        this.context = context;
    }


    private Bitmap addRedBorder(Bitmap bmp, int borderSize) {
        Bitmap bmpWithBorder = Bitmap.createBitmap(bmp.getWidth() + borderSize * 2, bmp.getHeight() + borderSize * 2, bmp.getConfig());
        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawColor(Color.RED);
        canvas.drawBitmap(bmp, borderSize, borderSize, null);
        bmp.recycle();
        return bmpWithBorder;
    }

    public void load_image(String url)
    {

    }

    void set_image(ImageView view, String url)
    {

    }



    void next_download(Bitmap bitmap)
    {

    }

    void re_download(String url, ImageView view)
    {

    }


}



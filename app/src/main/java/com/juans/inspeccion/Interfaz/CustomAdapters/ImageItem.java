package com.juans.inspeccion.Interfaz.CustomAdapters;

import android.graphics.Bitmap;

/**
 * Created by Juan on 04/05/2015.
 */
public class ImageItem {
    private Bitmap image;
    private String title;

    public String getImgPath() {
        return imgPath;
    }

    public void setImgPath(String imgPath) {
        this.imgPath = imgPath;
    }

    private String imgPath;

    public ImageItem(Bitmap image, String title,String imgPath) {
        super();
        this.image = image;
        this.imgPath=imgPath;
        this.title = title;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

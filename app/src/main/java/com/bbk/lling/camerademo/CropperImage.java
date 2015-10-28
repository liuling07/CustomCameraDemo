package com.bbk.lling.camerademo;

import android.graphics.Bitmap;

import java.io.Serializable;

/**
 * @Class:
 * @Description:
 * @author: lling(www.cnblogs.com/liuling)
 * @Date: 2015/10/23
 */
public class CropperImage implements Serializable{
    private Bitmap bitmap;
    private float x;
    private float y;
    private float width;
    private float height;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }
}

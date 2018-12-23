package com.zhouqianbin.demo.camera;

import android.graphics.ImageFormat;
import android.view.SurfaceView;

public class CameraParamet {

    private SurfaceView mSurfaceView;

    private int mCameraId;

    private int mPreviewWidth;

    private int mPreviewHeight;

    private int mPictureWidth;

    private int mPictureHeight;

    private int mImageFormat = ImageFormat.JPEG;

    public SurfaceView getmSurfaceView() {
        return mSurfaceView;
    }

    public int getmCameraId() {
        return mCameraId;
    }

    public int getmPreviewWidth() {
        return mPreviewWidth;
    }

    public int getmPreviewHeight() {
        return mPreviewHeight;
    }

    public int getmImageFormat() {
        return mImageFormat;
    }

    public int getmPictureWidth() {
        return mPictureWidth;
    }

    public int getmPictureHeight() {
        return mPictureHeight;
    }

    public static class Builder{

        private CameraParamet paramet;

        public Builder(){
            paramet = new CameraParamet();
        }

        public Builder setSurfaceView(SurfaceView surfaceView){
            paramet.mSurfaceView = surfaceView;
            return this;
        }

        public Builder setCameraId(int cameraId){
            paramet.mCameraId = cameraId;
            return this;
        }

        public Builder setPreviewSize(int width,int height){
            paramet.mPreviewWidth = width;
            paramet.mPreviewHeight = height;
            return this;
        }

        public Builder setPictureSize(int width,int height){
            paramet.mPictureWidth = width;
            paramet.mPictureHeight = height;
            return this;
        }

        public Builder setImageFormat(int imageFormat){
            paramet.mImageFormat = imageFormat;
            return this;
        }

        public CameraParamet Build(){
            return paramet;
        }
    }

}

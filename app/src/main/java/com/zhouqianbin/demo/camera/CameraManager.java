package com.zhouqianbin.demo.camera;


import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CameraManager implements SurfaceHolder.Callback,Camera.PreviewCallback {

    private CameraManager() { }
    private static class SingleHolder {
        private static final CameraManager INSTANCE = new CameraManager();
    }
    public static CameraManager getInstance() {
        return SingleHolder.INSTANCE;
    }
    private static final String TAG = CameraManager.class.getSimpleName();

    private Camera mCamera;
    private int mPreviewWidth;
    private int mPreviewHeight;
    private int mPictureWidth;
    private int mPictureHeight;
    private SurfaceHolder mSurfaceHolder;
    private int mCameraId;
    private int mCameraAngle;
    private int mImageFormat;
    private Activity mActivity;
    private OnCameraPreview mCameraPreview;


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated");
        startPreview(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged");
        mCamera.stopPreview();
        startPreview(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surfaceDestroyed");
        releaseCamera();
    }


    /**
     * 打开摄像头
     *
     * @param activity
     * @param paramet
     * @param cameraListen
     */
    public void openCamera(Activity activity, CameraParamet paramet, OnCameraListen cameraListen) {
        Log.d(TAG, "openCamera");
        this.mActivity = activity;
        initParamet(paramet);
        if (null == mCamera) {
            try {
                mCamera = Camera.open(paramet.getmCameraId());
                if (null != cameraListen) {
                    if (null != mCamera) {
                        Log.d(TAG, "onSuccess");
                        cameraListen.oppenSuss();
                    } else {
                        Log.d(TAG, "onError");
                        cameraListen.oppenEror("获取的Camera实例为null");
                    }
                }
            } catch (Exception e) {
                if (null != cameraListen) {
                    Log.d(TAG, "onError");
                    cameraListen.oppenEror("摄像头正在使用或该设备" +
                            "没有摄像头");
                }
            }
        }
        setCameraConfig();
    }

    /**
     * 打开摄像头
     *
     * @param activity
     * @param paramet
     */
    public void openCamera(Activity activity, CameraParamet paramet) {
        Log.d(TAG, "openCamera");
        this.mActivity = activity;
        initParamet(paramet);
        if (null == mCamera) {
            mCamera = Camera.open(paramet.getmCameraId());
        }
        setCameraConfig();
    }


    private void initParamet(CameraParamet paramet) {
        Log.d(TAG, "openCamera initParamet");
        mSurfaceHolder = paramet.getmSurfaceView().getHolder();
        mSurfaceHolder.addCallback(this);
        this.mCameraId = paramet.getmCameraId();
        this.mPreviewWidth = paramet.getmPreviewWidth();
        this.mPreviewHeight = paramet.getmPreviewHeight();
        this.mPictureWidth = paramet.getmPictureWidth();
        this.mPictureHeight = paramet.getmPictureHeight();
        this.mImageFormat = paramet.getmImageFormat();
    }


    /**
     * 设置摄像头参数
     */
    private void setCameraConfig() {
        Log.d(TAG, "openCamera setCameraConfig");
        if (mCamera == null) {
            Log.d(TAG, "mCamera = null ");
            return;
        }
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        Camera.getCameraInfo(mCameraId, cameraInfo);
        Camera.Parameters params = mCamera.getParameters();
        Camera.Size bestPreviewSize = calBestPreviewSize(
                mCamera.getParameters(), mPreviewWidth, mPreviewHeight);
        mPreviewWidth = bestPreviewSize.width;
        mPreviewHeight = bestPreviewSize.height;
        Camera.Size bestPictureSize = calBestPictureSize(mCamera.getParameters(),
                mPictureWidth, mPictureHeight);
        mPictureWidth = bestPictureSize.width;
        mPictureHeight = bestPictureSize.height;
        //预览的大小，也就是分辨率，越小越模糊. 设置的时候需要根据设备支持的分辨率设置
        params.setPreviewSize(mPreviewWidth, mPreviewHeight);
        //params.setPreviewFormat();
        //设置生成图片的大小，这个不一定跟预览相同，所以也需要获取支持的图片大小再设置
        params.setPictureSize(mPreviewWidth, mPreviewHeight);
        //图片的格式，是jpeg还是其他类型的格式
        params.setPictureFormat(mImageFormat);
        //设置对焦的模式,有些手机加上这个会报错所以
        // 需要params.getSupportedFocusModes()获取支持的模式再设置
        //params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        mCamera.setParameters(params);
    }


    /**
     * 通过传入的宽高算出最接近于宽高值的相机大小
     */
    private Camera.Size calBestPreviewSize(Camera.Parameters camPara,
                                           final int width, final int height) {
        Log.d(TAG, "calBestPreviewSize " + width + " " + height);

        List<Camera.Size> allSupportedSize = camPara.getSupportedPreviewSizes();
        ArrayList<Camera.Size> widthLargerSize = new ArrayList<Camera.Size>();
        for (Camera.Size tmpSize : allSupportedSize) {
            Log.d("支持的预览大小 ", "width===" + tmpSize.width
                    + ", tmpSize.height===" + tmpSize.height);
            if (tmpSize.width > tmpSize.height) {
                widthLargerSize.add(tmpSize);
            }
        }

        Collections.sort(widthLargerSize, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                int off_one = Math.abs(lhs.width * lhs.height - width * height);
                int off_two = Math.abs(rhs.width * rhs.height - width * height);
                return off_one - off_two;
            }
        });

        return widthLargerSize.get(0);
    }

    private Camera.Size calBestPictureSize(Camera.Parameters camPara,
                                           final int width, final int height) {
        Log.d(TAG, "calBestPictureSize " + width + " " + height);

        List<Camera.Size> allSupportedSize = camPara.getSupportedPictureSizes();
        ArrayList<Camera.Size> widthLargerSize = new ArrayList<Camera.Size>();
        for (Camera.Size tmpSize : allSupportedSize) {
            Log.d("支持的图片大小 ", "width===" + tmpSize.width
                    + ", tmpSize.height===" + tmpSize.height);
            if (tmpSize.width > tmpSize.height) {
                widthLargerSize.add(tmpSize);
            }
        }

        Collections.sort(widthLargerSize, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                int off_one = Math.abs(lhs.width * lhs.height - width * height);
                int off_two = Math.abs(rhs.width * rhs.height - width * height);
                return off_one - off_two;
            }
        });

        return widthLargerSize.get(0);
    }


    private long startDelayTime;
    private long endDelayTime;

    /**
     * 开始预览
     * @param holder holder实例
     */
    private void startPreview(SurfaceHolder holder) {
        Log.d(TAG, "startPreview");
        try {
            mCamera.setPreviewDisplay(holder);
            setCameraDisplayOrientation(mCameraId, mCamera);
            mCamera.setPreviewCallback(this);
            mCamera.startPreview();
            //startDelayTime = System.currentTimeMillis();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        if (null == mCameraPreview) {
            return;
        }
        mCameraPreview.onPreview(data);
        //是否可以传输
        /*
        if(mIsTransmittalData){
            mCameraPreview.onPreview(data);
        }
        */
        //每隔指定时间发送图像数据
                   /* endDelayTime = System.currentTimeMillis();
                    if (endDelayTime - startDelayTime > 3000) {
                        Log.d("test", "开始检测");
                        mCameraPreview.onPreview(data);
                        startDelayTime = System.currentTimeMillis();
                    } else {
                        endDelayTime = System.currentTimeMillis();
                    }*/
    }


    private boolean mIsTransmittalData = true;
    public void setIsTransmittalData(boolean isTransmittalData){
        this.mIsTransmittalData = isTransmittalData;
    }

    /**
     * 设置照相机旋转角度
     *
     * @param cameraId
     * @param camera
     */
    private void setCameraDisplayOrientation(int cameraId, Camera camera) {
        Log.d(TAG, "startPreview setCameraDisplayOrientation");
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = mActivity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int mDegrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                mDegrees = 0;
                break;
            case Surface.ROTATION_90:
                mDegrees = 90;
                break;
            case Surface.ROTATION_180:
                mDegrees = 180;
                break;
            case Surface.ROTATION_270:
                mDegrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + mDegrees) % 360;
            result = (360 - result) % 360;
        } else {
            result = (info.orientation - mDegrees + 360) % 360;
        }
        mCameraAngle = result;
        Log.d(TAG, "Camera当前角度 " + mCameraAngle);
        camera.setDisplayOrientation(mCameraAngle);
    }


    /**
     * 关闭摄像头
     */
    public void closeCamera() {
        Log.d(TAG, "closeCamera");
        if (null != mCamera) {
            mCamera.stopPreview();
        }
    }


    public void takePicture(final OnCameraPicture cameraPicture){
        if(null != mCamera){
            mCamera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    cameraPicture.onPicture(data);
                    if(null != mCamera){
                        camera.startPreview();
                    }
                }
            });
        }

    }

    /**
     * 拍照
     *
     * @param cameraPicture   数据回调结果
     * @param isRefreshCamera 是否刷新预览
     */
    public void takePicture(final OnCameraPicture cameraPicture, final boolean isRefreshCamera) {
        Log.d(TAG, "takePicture");
        if (null != mCamera) {
            //前置摄像头没有对角功能
            List<String> focusModes = mCamera.getParameters().getSupportedFocusModes();
            if (null != focusModes && focusModes.size() > 0) {
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            startTakePicture(cameraPicture,isRefreshCamera,null);
                        }
                    }
                });
            } else {
                startTakePicture(cameraPicture,isRefreshCamera,null);
            }
        }
    }


    /**
     * 拍照
     * @param cameraPicture   数据回调结果
     * @param isRefreshCamera 是否刷新预览
     * @param path            图片保存的位置
     */
    public void takePicture(final OnCameraPicture cameraPicture, final boolean isRefreshCamera, final String path) {
        if (null != mCamera) {
            //前置摄像头没有对角功能
            List<String> focusModes = mCamera.getParameters().getSupportedFocusModes();
            if (null != focusModes && focusModes.size() > 0) {
                mCamera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            startTakePicture(cameraPicture, isRefreshCamera, path);
                        }
                    }
                });
            } else {
                startTakePicture(cameraPicture, isRefreshCamera, path);
            }
        }
    }

    private void startTakePicture(final OnCameraPicture cameraPicture, final boolean isRefreshCamera, final String path) {
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                cameraPicture.onPicture(data);
                savePicture(data, path,cameraPicture);
                if (isRefreshCamera) {
                    Log.d(TAG,"takePicture");
                    if(null == mCamera){
                       return;
                    }
                    camera.startPreview();
                }
            }
        });
    }

    /**
     * 保存图片
     * @param bytes
     * @param path
     * @param cameraPicture
     */
    private void savePicture(byte[] bytes, String path, OnCameraPicture cameraPicture) {
        if(null == path){
            return;
        }
        File imageFile = new File(path);
        try {
            FileOutputStream fos = new FileOutputStream(imageFile);
            fos.write(bytes);
            fos.close();
            cameraPicture.saveState(true);
        } catch (IOException e) {
            e.printStackTrace();
            cameraPicture.saveState(false);
        }
    }

    /**
     * 设置预览监听
     * @param previewListen
     */
    public void setPreviewListen(OnCameraPreview previewListen) {
        this.mCameraPreview = previewListen;
    }

    /**
     * 释放摄像头资源
     */
    public void releaseCamera() {
        Log.d(TAG, "releaseCamera");
        if(null !=  mSurfaceHolder){
            mSurfaceHolder.removeCallback(this);
        }
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
        if (null != mActivity) {
            mActivity = null;
        }
        setPreviewListen(null);
    }

    /**
     * 判断该设备是否支持摄像头
     *
     * @param context
     * @return
     */
    public boolean isSupportCamera(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 切换前后置摄像头
     */
    public void switchCamera() {
        Log.d(TAG, "switchCamera");
        mCameraId = (mCameraId + 1) % Camera.getNumberOfCameras();
        if (mCamera != null) {
            releaseCamera();
        }
        try {
            mCamera = Camera.open(mCameraId);
            startPreview(mSurfaceHolder);
        } catch (Exception e) {
            Log.d(TAG, "未发现相机");
        }
    }


    public int getmPictureWidth() {
        return mPictureWidth;
    }

    public int getmPictureHeight() {
        return mPictureHeight;
    }

    public int getmPreviewWidth() {
        return mPreviewWidth;
    }

    public int getmPreviewHeight() {
        return mPreviewHeight;
    }

    public int getmCameraId() {
        return mCameraId;
    }

    public int getmCameraAngle() {
        return mCameraAngle;
    }

    public interface OnCameraListen {
        void oppenSuss();
        void oppenEror(String errorMsg);
    }

    public interface OnCameraPreview {
        void onPreview(byte[] bytes);
    }

    public interface OnCameraPicture {
        void onPicture(byte[] bytes);
        void saveState(boolean state);
    }

}

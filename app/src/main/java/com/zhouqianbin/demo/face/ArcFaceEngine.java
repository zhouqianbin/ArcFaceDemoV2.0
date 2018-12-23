package com.zhouqianbin.demo.face;

import android.content.Context;
import android.util.Log;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.Face3DAngle;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.FaceSimilar;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.zhouqianbin.demo.AppSetting;

import java.util.ArrayList;
import java.util.List;

public class ArcFaceEngine {

    private ArcFaceEngine() { }
    private static class SingleHolder {
        private static final ArcFaceEngine INSTANCE = new ArcFaceEngine();
    }
    public static ArcFaceEngine getInstance() {
        return SingleHolder.INSTANCE;
    }

    private static final String TAG = ArcFaceEngine.class.getSimpleName();
    //人脸检测的模式
    public static final long MODEL_IMAGE = FaceEngine.ASF_DETECT_MODE_IMAGE;
    public static final long MODEL_VIDEO = FaceEngine.ASF_DETECT_MODE_VIDEO;
    //人脸检测的角度
    public static final int ANGLE_0 = FaceEngine.ASF_OP_0_ONLY;
    public static final int ANGLE_90 = FaceEngine.ASF_OP_90_ONLY;
    public static final int ANGLE_180 = FaceEngine.ASF_OP_180_ONLY;
    public static final int ANGLE_270 = FaceEngine.ASF_OP_270_ONLY;
    //人脸数据的格式
    public static final int FORMAT_NV21 = FaceEngine.CP_PAF_NV21;
    public static final int FORMAT_BGR24 = FaceEngine.CP_PAF_BGR24;

    /**
     * 人脸检测的引擎
     */
    private FaceEngine mFaceEngine = new FaceEngine();

    /**
     * 存储检测到的脸部信息
     * rect和人脸角度
     */
    private List<FaceInfo> mFaceInfoList = new ArrayList<>();

    /**
     * 存储人脸年龄信息
     */
    private List<AgeInfo> mAgeInfoList = new ArrayList<>();

    /**
     * 存储人脸性别信息
     */
    private List<GenderInfo> mGenderInfoList = new ArrayList<>();

    /**
     * 存储人脸3D角度信息
     */
    private List<Face3DAngle> mFace3DAngleList = new ArrayList<>();

    /**
     * 活体信息,里面定义常量来辨别是否为活体
     */
    private List<LivenessInfo> mLivenessInfoList = new ArrayList<>();

    /**
     * 人脸比对得到的置信度
     */
    private FaceSimilar mFaceSimilar = new FaceSimilar();

    private List<FaceFeature> mFaceFeatureList = new ArrayList<>();

    /**
     * 人脸特征对象
     */
    private FaceFeature mFaceFeature = new FaceFeature();

    /**
     * 人脸检测对象，将检测到的信息全部存储到该对象中
     */
    private List<FaceDetectInfo> mFaceDetectInfoList = new ArrayList<>();

    private Context mContext;

    /**
     * 激活引擎
     *
     * @param context           上下文
     * @param engineStateListen 引擎初执行状态回调
     */
    public void initEngine(Context context,ArcFaceParameter faceParameter,OnEngineStateListen engineStateListen) {
        this.mContext = context;
        //激活虹软人脸识别引擎
        int activeCode = mFaceEngine.active(context, AppSetting.APP_ID, AppSetting.APP_SDK_KEY);
        //当激活的错误码不为成功并且不为SDK已激活
        if (ErrorInfo.MOK != activeCode && ErrorInfo.MERR_ASF_ALREADY_ACTIVATED != activeCode) {
            Log.d(TAG, "人脸引擎激活失败,错误码为 " + activeCode);
            engineStateListen.onError("人脸引擎激活失败,错误码为 " + activeCode);
        }
        Log.d(TAG, "人脸引擎激活成功");

        if (null == faceParameter) {
            throw new RuntimeException("请设置FaceParameter");
        }
        //初始虹软人脸引擎
        int initCode = mFaceEngine.init(mContext,
                faceParameter.getDetectModel(),
                faceParameter.getDetectAngle(),
                faceParameter.getDetectFaceScalval(),
                faceParameter.getDetectFaceMaxnum(),
                FaceEngine.ASF_FACE_RECOGNITION |
                        FaceEngine.ASF_FACE_DETECT |
                        FaceEngine.ASF_AGE |
                        FaceEngine.ASF_GENDER |
                        FaceEngine.ASF_FACE3DANGLE |
                        FaceEngine.ASF_LIVENESS);
        //如果是个人认证删除 ASF_LIVENESS，该值只对企业认证有效，否则报错
        //检验初始化结果
        if (initCode != ErrorInfo.MOK) {
            if (null != engineStateListen) {
                engineStateListen.onError("人脸引擎初始化失败，错误码 " + initCode);
            }
            Log.d(TAG, "人脸引擎初始化失败,错误码 " + initCode);
            return;
        }
        if (null != engineStateListen) {
            engineStateListen.onSuccess();
            Log.d(TAG, "人脸引擎初始化成功");
        }
    }



    /**
     * 检测人脸
     *
     * @param bytes        图像数据
     * @param width        图像的宽度
     * @param height       图像的高度
     * @param format       图像的格式，NV21还是BGR24
     * @param detectResult 人脸检测结果回调
     */
    public void detectFace(byte[] bytes, int width, int height, int format, OnFaceDetectResult detectResult) {
        mFaceInfoList.clear();
        mFaceDetectInfoList.clear();
        mAgeInfoList.clear();
        mGenderInfoList.clear();
        mFace3DAngleList.clear();
        mLivenessInfoList.clear();
        mFaceFeatureList.clear();

        int detectFacesCode = mFaceEngine.detectFaces(
                bytes,
                width,
                height,
                format, mFaceInfoList);
        if (detectFacesCode != ErrorInfo.MOK) {
            Log.d(TAG, "人脸检测失败 " + detectFacesCode);
            detectResult.detectError("人脸检测失败" + detectFacesCode);
            return;
        }
        Log.d(TAG, "人脸检测成功 ");
        if (null == mFaceInfoList || mFaceInfoList.size() <= 0) {
            Log.d(TAG, "检测不到人脸数据");
            if (null != detectResult) {
                detectResult.detectNotFace();
            }
            return;
        }
        Log.d(TAG, "检测到人脸数据 " + mFaceInfoList.size() + mFaceInfoList.toString());
        //循环遍历提取每个人脸特征
        for (FaceInfo faceInfo : mFaceInfoList) {
            extractFaceFeature(bytes, width, height, format, faceInfo);
        }
        //检测人脸年龄，性别等
        detectFaceInfo(bytes, width, height, format, detectResult);
    }

    /**
     * 检测人脸年龄，性别等信息
     *
     * @param bytes        检测的图像数据
     * @param width        图像宽度
     * @param height       图像高度
     * @param format       图像的格式
     * @param detectResult
     */
    private void detectFaceInfo(byte[] bytes, int width, int height, int format,
                                OnFaceDetectResult detectResult) {
        int process = mFaceEngine.process(
                bytes,
                width,
                height,
                format,
                mFaceInfoList,
                FaceEngine.ASF_AGE |
                        FaceEngine.ASF_GENDER |
                        FaceEngine.ASF_FACE3DANGLE |
                        FaceEngine.ASF_LIVENESS);
        //如果是个人认证删除 ASF_LIVENESS，该值只对企业认证有效，否则报错
        if (process != ErrorInfo.MOK) {
            Log.d(TAG, "人脸信息检测失败 " + process);
            return;
        }
        Log.d(TAG, "人脸信息检测成功");
        int ageCode = mFaceEngine.getAge(mAgeInfoList);
        int genderCode = mFaceEngine.getGender(mGenderInfoList);
        int angleCode = mFaceEngine.getFace3DAngle(mFace3DAngleList);
        int livenessCode = mFaceEngine.getLiveness(mLivenessInfoList);
        if ((ageCode | genderCode | angleCode | livenessCode) != ErrorInfo.MOK) {
            Log.d(TAG, "获取信息失败,错误码" + "年龄" + ageCode +
                    " 性别 " + genderCode +
                    " 角度 " + angleCode +
                    " 活体 " + livenessCode);
            return;
        }
        for (int i = 0; i < mFaceInfoList.size(); i++) {
            Log.d(TAG, "人脸信息 " + mFaceInfoList.get(i).toString() + " " +
                    "年龄" + mAgeInfoList.get(i).getAge() +
                    " 性别 " + mGenderInfoList.get(i).getGender() +
                    " 角度 " + mFace3DAngleList.get(i).toString() +
                    " 活体 " + mLivenessInfoList.get(i).getLiveness());
        }
        //将检测到的信息存储到FaceDetectInfo中
        for (FaceInfo faceInfo : mFaceInfoList) {
            int index = mFaceInfoList.indexOf(faceInfo);
            FaceDetectInfo faceDetectInfo = new FaceDetectInfo();
            faceDetectInfo.setFaceInfo(mFaceInfoList.get(index));
            faceDetectInfo.setFace3DAngle(mFace3DAngleList.get(index));
            faceDetectInfo.setFaceAge(mAgeInfoList.get(index).getAge());
            faceDetectInfo.setFaceGender(mGenderInfoList.get(index).getGender());
            faceDetectInfo.setFaceLiveness(mLivenessInfoList.get(index));
            faceDetectInfo.setFaceFeature(mFaceFeatureList.get(index));
            mFaceDetectInfoList.add(faceDetectInfo);
        }
        if (detectResult != null) {
            detectResult.detectResult(mFaceDetectInfoList);
        }
    }


    /**
     * 提取人脸特征
     *
     * @param bytes    人脸图像数据
     * @param width    图像宽度
     * @param height   图像高度
     * @param format   图像的格式，NV21还是BGR24
     * @param faceInfo 检测到的脸部信息
     */
    private void extractFaceFeature(byte[] bytes, int width, int height, int format,
                                    FaceInfo faceInfo) {
        Log.d(TAG, "extractFaceFeature");
        FaceFeature faceFeature = mFaceFeature.clone();
        int extractFaceFeature = mFaceEngine.extractFaceFeature(
                bytes,
                width,
                height,
                format,
                faceInfo,
                faceFeature);
        if (extractFaceFeature != ErrorInfo.MOK) {
            Log.d(TAG, "人脸特征检测失败 " + extractFaceFeature);
            mFaceFeatureList.add(new FaceFeature());
        }
        Log.d(TAG, "人脸特征 " + faceFeature.toString());
        mFaceFeatureList.add(faceFeature);
    }


    /**
     * 人脸比对
     * @param faceFeature1 第一张人脸特征
     * @param faceFeature2 第二张人脸特征
     */
    public float compareFaceFeature(FaceFeature faceFeature1, FaceFeature faceFeature2) {
        int compareFaceFeature = mFaceEngine.compareFaceFeature(faceFeature1, faceFeature2,
                mFaceSimilar);
        if (compareFaceFeature == ErrorInfo.MOK) {
            Log.d(TAG, "人脸比对成功 " + mFaceSimilar.getScore());
            return mFaceSimilar.getScore();
        }
        Log.d(TAG, "人脸比对失败 " + compareFaceFeature);
        return 0;
    }


    /**
     * 释放资源
     */
    public void destory() {
        if (null != mFaceEngine) {
            mFaceEngine.unInit();
        }
        if (null != mContext) {
            mContext = null;
        }
    }


}

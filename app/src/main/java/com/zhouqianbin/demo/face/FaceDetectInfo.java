package com.zhouqianbin.demo.face;

import android.graphics.Rect;

import com.arcsoft.face.Face3DAngle;
import com.arcsoft.face.FaceFeature;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.LivenessInfo;

public class FaceDetectInfo {

    /**
     * 人脸年龄
     */
    private int faceAge;

    /**
     * 人脸性别
     */
    private int faceGender;

    /**
     * 人脸3D的角度值
     */
    private Face3DAngle face3DAngle;

    /**
     * 活体信息
     */
    private LivenessInfo faceLiveness;


    /**
     * 人脸特征
     */
    private FaceFeature faceFeature;

    /**
     * 人脸信息
     */
    private FaceInfo faceInfo;

    public FaceInfo getFaceInfo() {
        return faceInfo;
    }

    public void setFaceInfo(FaceInfo faceInfo) {
        this.faceInfo = faceInfo;
    }

    public FaceFeature getFaceFeature() {
        return faceFeature;
    }

    public void setFaceFeature(FaceFeature faceFeature) {
        this.faceFeature = faceFeature;
    }

    public int getFaceAge() {
        return faceAge;
    }

    public void setFaceAge(int faceAge) {
        this.faceAge = faceAge;
    }

    public int getFaceGender() {
        return faceGender;
    }

    public void setFaceGender(int faceGender) {
        this.faceGender = faceGender;
    }

    public Face3DAngle getFace3DAngle() {
        return face3DAngle;
    }

    public void setFace3DAngle(Face3DAngle face3DAngle) {
        this.face3DAngle = face3DAngle;
    }


    public LivenessInfo getFaceLiveness() {
        return faceLiveness;
    }

    public void setFaceLiveness(LivenessInfo faceLiveness) {
        this.faceLiveness = faceLiveness;
    }


    @Override
    public String toString() {
        return "FaceDetectInfo{" +
                "faceAge=" + faceAge +
                ", faceGender=" + faceGender +
                ", face3DAngle=" + face3DAngle +
                ", faceLiveness=" + faceLiveness +
                ", faceFeature=" + faceFeature +
                ", faceInfo=" + faceInfo +
                '}';
    }
}

package com.zhouqianbin.demo.entity;



import android.graphics.Rect;

import org.litepal.crud.LitePalSupport;

import java.util.Arrays;



public class FaceInfoEntity  extends LitePalSupport{

    private int id;
    private int faceAge;
    private int faceGender;
    private String faceName;
    private byte[] faceImage;
    private byte[] faceFeature;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public String getFaceName() {
        return faceName;
    }

    public void setFaceName(String faceName) {
        this.faceName = faceName;
    }

    public byte[] getFaceImage() {
        return faceImage;
    }

    public void setFaceImage(byte[] faceImage) {
        this.faceImage = faceImage;
    }

    public byte[] getFaceFeature() {
        return faceFeature;
    }

    public void setFaceFeature(byte[] faceFeature) {
        this.faceFeature = faceFeature;
    }

    @Override
    public String toString() {
        return "FaceInfoEntity{" +
                "id=" + id +
                ", faceAge=" + faceAge +
                ", faceGender=" + faceGender +
                ", faceName='" + faceName + '\'' +
                ", faceImage64='" + faceImage + '\'' +
                ", faceFeature=" + Arrays.toString(faceFeature) +
                '}';
    }
}

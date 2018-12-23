package com.zhouqianbin.demo.entity;

import android.graphics.Rect;

public class DrawFaceInfoEntity {

    private String faceName;

    private int faceAge;

    private int faceGender;

    private Rect faceRect;

    private int livenress;

    public DrawFaceInfoEntity() {
    }
    public DrawFaceInfoEntity(String faceName, int faceAge, int gender, Rect faceRect, int livenress) {
        this.faceName = faceName;
        this.faceAge = faceAge;
        this.faceGender = gender;
        this.faceRect = faceRect;
        this.livenress = livenress;
    }
    public String getFaceName() {
        return faceName;
    }

    public void setFaceName(String faceName) {
        this.faceName = faceName;
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

    public void setFaceGender(int gender) {
        this.faceGender = gender;
    }

    public Rect getFaceRect() {
        return faceRect;
    }

    public void setFaceRect(Rect faceRect) {
        this.faceRect = faceRect;
    }

    public int getLivenress() {
        return livenress;
    }

    public void setLivenress(int livenress) {
        this.livenress = livenress;
    }

    @Override
    public String toString() {
        return "DrawFaceInfoEntity{" +
                "faceName='" + faceName + '\'' +
                ", faceAge=" + faceAge +
                ", faceGender=" + faceGender +
                ", faceRect=" + faceRect +
                ", livenress=" + livenress +
                '}';
    }
}

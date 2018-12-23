package com.zhouqianbin.demo.face;

public class ArcFaceParameter {

    private int detectAngle;
    private long detectModel;
    private int detectFaceMaxNum;
    private int detectFaceScalval;

    public int getDetectAngle() {
        return detectAngle;
    }
    public long getDetectModel() {
        return detectModel;
    }
    public int getDetectFaceMaxnum() {
        return detectFaceMaxNum;
    }
    public int getDetectFaceScalval() {
        return detectFaceScalval;
    }
    public static class Builder{

        private ArcFaceParameter parameter;
        public Builder(){
            parameter = new ArcFaceParameter();
        }
        public Builder setDetectAngle(int detectAngle){
            parameter.detectAngle = detectAngle;
            return this;
        }
        public Builder setDetectModel(long detectModel){
            parameter.detectModel = detectModel;
            return this;
        }
        public Builder setDetectFaceMaxNum(int detectFaceMaxNum){
            parameter.detectFaceMaxNum = detectFaceMaxNum;
            return this;
        }
        public Builder setDetectFaceScalval(int detectFaceScalval){
            parameter.detectFaceScalval = detectFaceScalval;
            return this;
        }
        public ArcFaceParameter Build(){
            return parameter;
        }
    }
}

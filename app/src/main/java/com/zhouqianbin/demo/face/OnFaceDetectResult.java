package com.zhouqianbin.demo.face;

import java.util.List;

public interface OnFaceDetectResult {

    /**
     * 检测的结果
     * @param faceDetectInfos
     */
    void detectResult(List<FaceDetectInfo> faceDetectInfos);

    /**
     * 检测不到人脸
     */
    void detectNotFace();

    /**
     * 检测失败
     * @param errorMsg
     */
    void detectError(String errorMsg);

}

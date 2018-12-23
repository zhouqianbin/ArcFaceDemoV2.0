package com.zhouqianbin.demo.entity;

/**
 * @Copyright (C), 2018, 漳州科能电器有限公司
 * @FileName: CompareFaceResult
 * @Author: 周千滨
 * @Date: 2018/11/1 16:34
 * @Description:
 * @Version: 1.0.0
 * @UpdateHistory: 修改历史
 * @修改人: 周千滨
 * @修改描述: 创建文件
 */

public class CompareFaceResult {

    /**
     * 比对的结果值
     */
    private float score;

    /**
     * 当前检测到的索引
     */
    private int detectFaceIndex;

    /**
     * 与数据库中最相似的人脸索引
     */
    private int dbFaceIndex;

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public int getDetectFaceIndex() {
        return detectFaceIndex;
    }

    public void setDetectFaceIndex(int detectFaceIndex) {
        this.detectFaceIndex = detectFaceIndex;
    }

    public int getDbFaceIndex() {
        return dbFaceIndex;
    }

    public void setDbFaceIndex(int dbFaceIndex) {
        this.dbFaceIndex = dbFaceIndex;
    }

    @Override
    public String toString() {
        return "CompareFaceResult{" +
                "score=" + score +
                ", detectFaceIndex=" + detectFaceIndex +
                ", dbFaceIndex=" + dbFaceIndex +
                '}';
    }

    public CompareFaceResult(float score, int dbFaceIndex, int detectFaceIndex) {
        this.score = score;
        this.detectFaceIndex = detectFaceIndex;
        this.dbFaceIndex = dbFaceIndex;
    }
}

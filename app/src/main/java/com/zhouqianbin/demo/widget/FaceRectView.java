package com.zhouqianbin.demo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;

import com.arcsoft.face.LivenessInfo;
import com.zhouqianbin.demo.entity.DrawFaceInfoEntity;
import com.zhouqianbin.demo.entity.FaceInfoEntity;

import java.util.ArrayList;
import java.util.List;


public class FaceRectView extends View {

    public FaceRectView(Context context) {
        this(context, null);
    }
    public FaceRectView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public FaceRectView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initPaint();
    }

    private Paint mTextPaint;
    private Paint faceRectPaint;

    private void initPaint() {
        faceRectPaint = new Paint();
        faceRectPaint.setAntiAlias(true);
        faceRectPaint.setStrokeWidth(3);
        faceRectPaint.setStyle(Paint.Style.STROKE);
        faceRectPaint.setColor(Color.RED);
        mTextPaint = new Paint();
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setStrokeWidth(2);
        mTextPaint.setTextSize(20);
        mTextPaint.setColor(Color.RED);
    }

    List<DrawFaceInfoEntity> mDrawFaceInfoEntityList = new ArrayList<>();

    /**
     * 开始画矩形框
     * @param
     */
    public void drawFaceRect(List<DrawFaceInfoEntity> drawFaceInfoEntities) {
        this.mDrawFaceInfoEntityList = drawFaceInfoEntities;
        //在主线程发起绘制请求
        postInvalidate();
    }

    public void clearFaceRectInfo() {
        mDrawFaceInfoEntityList.clear();
        postInvalidate();
    }

    private Path mFacePath;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //清空画布
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        if(null == mDrawFaceInfoEntityList){
            return;
        }
        mFacePath = new Path();
        //循环绘制检测到的人脸数据
        for (DrawFaceInfoEntity drawFaceInfoEntity : mDrawFaceInfoEntityList){
            Rect faceRect = drawFaceInfoEntity.getFaceRect();
            //左上
            mFacePath.moveTo(drawFaceInfoEntity.getFaceRect().left, faceRect.top + faceRect.height() / 4);
            mFacePath.lineTo(faceRect.left, faceRect.top);
            mFacePath.lineTo(faceRect.left + faceRect.width() / 4, faceRect.top);
            //右上
            mFacePath.moveTo(faceRect.right - faceRect.width() / 4, faceRect.top);
            mFacePath.lineTo(faceRect.right, faceRect.top);
            mFacePath.lineTo(faceRect.right, faceRect.top + faceRect.height() / 4);
            //右下
            mFacePath.moveTo(faceRect.right, faceRect.bottom - faceRect.height() / 4);
            mFacePath.lineTo(faceRect.right, faceRect.bottom);
            mFacePath.lineTo(faceRect.right - faceRect.width() / 4, faceRect.bottom);
            //左下
            mFacePath.moveTo(faceRect.left + faceRect.width() / 4, faceRect.bottom);
            mFacePath.lineTo(faceRect.left, faceRect.bottom);
            mFacePath.lineTo(faceRect.left, faceRect.bottom - faceRect.height() / 4);
            canvas.drawPath(mFacePath, faceRectPaint);
            //canvas.drawRect(faceRect, faceRectPaint);
            StringBuilder stringBuilder = new StringBuilder();
            if(TextUtils.isEmpty(drawFaceInfoEntity.getFaceName())){
                stringBuilder.append("姓名:未知   " );
            }else {
                stringBuilder.append("姓名:" + drawFaceInfoEntity.getFaceName() + "   ");
            }
            stringBuilder.append("年龄:" + drawFaceInfoEntity.getFaceAge() + "   ");
            switch (drawFaceInfoEntity.getFaceGender()) {
                case 0:
                    stringBuilder.append("性别:男   ");
                    break;
                case 1:
                    stringBuilder.append("性别:女   ");
                    break;
                default:
                    stringBuilder.append("性别:未知   ");
                    break;
            }

            switch (drawFaceInfoEntity.getLivenress()){
                case LivenessInfo.UNKNOWN:
                    stringBuilder.append("活体:未知");
                    break;
                case LivenessInfo.NOT_ALIVE:
                    stringBuilder.append("活体:不是");
                    break;
                case LivenessInfo.ALIVE:
                    stringBuilder.append("活体:是");
                    break;
                case LivenessInfo.FACE_NUM_MORE_THAN_ONE:
                    stringBuilder.append("活体:活体只支持单张检测");
                    break;
            }
            canvas.drawText(stringBuilder.toString()
                    , faceRect.left, faceRect.top - 20, mTextPaint);
        }
    }

}

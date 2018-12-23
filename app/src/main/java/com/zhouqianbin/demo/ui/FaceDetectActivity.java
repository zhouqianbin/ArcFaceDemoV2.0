package com.zhouqianbin.demo.ui;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.Toast;

import com.arcsoft.face.FaceFeature;
import com.zhouqianbin.demo.R;
import com.zhouqianbin.demo.camera.CameraManager;
import com.zhouqianbin.demo.camera.CameraParamet;
import com.zhouqianbin.demo.entity.DrawFaceInfoEntity;
import com.zhouqianbin.demo.entity.CompareFaceResult;
import com.zhouqianbin.demo.entity.FaceInfoEntity;
import com.zhouqianbin.demo.face.ArcFaceParameter;
import com.zhouqianbin.demo.face.FaceDetectInfo;
import com.zhouqianbin.demo.face.ArcFaceEngine;
import com.zhouqianbin.demo.face.OnEngineStateListen;
import com.zhouqianbin.demo.face.OnFaceDetectResult;
import com.zhouqianbin.demo.utils.FaceConvertUtils;
import com.zhouqianbin.demo.widget.FaceRectView;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class FaceDetectActivity extends AppCompatActivity {

    private static final String TAG = FaceDetectActivity.class.getSimpleName();
    private SurfaceView mSurfaceView;
    private FaceRectView mFaceRectView;

    private List<FaceInfoEntity> mFaceInfoEntityList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_detect);
        mSurfaceView = findViewById(R.id.main_surf_view);
        mFaceRectView = findViewById(R.id.main_face_rect);
        mFaceInfoEntityList = LitePal.findAll(FaceInfoEntity.class);
        Log.d(TAG,"数据库人脸数量 "+mFaceInfoEntityList.size());
    }

    /**
     * 初始化人脸引擎
     */
    private void initEngine() {
        ArcFaceParameter faceParameter = new ArcFaceParameter.Builder()
                .setDetectAngle(ArcFaceEngine.ANGLE_270)
                .setDetectModel(ArcFaceEngine.MODEL_VIDEO)
                .setDetectFaceMaxNum(10)
                .setDetectFaceScalval(16)
                .Build();
        ArcFaceEngine.getInstance().initEngine(
                this,
                faceParameter,
                new OnEngineStateListen() {
                    @Override
                    public void onSuccess() {
                        Log.d(TAG,"人脸引擎初始化成功");
                    }

                    @Override
                    public void onError(String errorMsg) {
                        Log.d(TAG,"人脸引擎初始化失败 " + errorMsg);
                    }
                });
    }


    /**
     * 检测人脸
     * @param bytes
     */
    private void detectFace(final byte[] bytes) {
        ArcFaceEngine.getInstance().detectFace(
                bytes,
                CameraManager.getInstance().getmPreviewWidth(),
                CameraManager.getInstance().getmPreviewHeight(),
                ArcFaceEngine.FORMAT_NV21,
                new OnFaceDetectResult() {
                    @Override
                    public void detectResult(List<FaceDetectInfo> faceDetectInfos) {
                        Log.d(TAG,"检测结果 " + faceDetectInfos.toString());
                        drawFaceInfo(faceDetectInfos);
                        quertFaceForDb(faceDetectInfos);
                    }

                    @Override
                    public void detectNotFace() {
                        mFaceRectView.clearFaceRectInfo();
                    }

                    @Override
                    public void detectError(String errorMsg) {

                    }
                }
        );
    }


    private List<DrawFaceInfoEntity> mDrawFaceInfoEntityList = new ArrayList<>();
    private FaceFeature mFaceFeature = new FaceFeature();
    /**
     * 当前比对的结果值
     */
    private float mCurrentScore;

    private List<CompareFaceResult> mRegisterCompareFaces = new ArrayList<>();
    private List<CompareFaceResult> mNotRegisterCompareFaces = new ArrayList<>();

    /**
     *查询数据库获取人脸信息
     * @param faceDetectInfos
     */
    public void quertFaceForDb(List<FaceDetectInfo> faceDetectInfos){
        mRegisterCompareFaces.clear();
        mNotRegisterCompareFaces.clear();
        //循环获取检测到的人脸信息
        for (int detectIndex = 0;detectIndex<faceDetectInfos.size();detectIndex++) {
            //检测到的人脸特征
            FaceFeature faceFeature = faceDetectInfos.get(detectIndex).getFaceFeature();
            //循环获取数据库人脸特征
            for (int dbIndex = 0; dbIndex < mFaceInfoEntityList.size(); dbIndex++) {
                byte[] dbFaceFeture = mFaceInfoEntityList.get(detectIndex).getFaceFeature();
                FaceFeature dbFeature = mFaceFeature.clone();
                dbFeature.setFeatureData(dbFaceFeture);
                //比对的结果值
                float score = ArcFaceEngine.getInstance().compareFaceFeature(dbFeature, faceFeature);
                Log.d(TAG, "当前比对的结果值 " + score);
                if (score > mCurrentScore) {
                    mCurrentScore = score;
                }
                //是否循环到最后一个
                if (dbIndex == faceDetectInfos.size() - 1) {
                    Log.d(TAG, "最终比对的结果值 " + mCurrentScore);
                    //是否大于0.8
                    if (mCurrentScore > 0.8) {
                        //匹配数据库的人脸信息
                        mRegisterCompareFaces.add(
                                new CompareFaceResult(mCurrentScore, dbIndex, detectIndex));
                    } else {
                        //不是注册的人脸信息
                        mNotRegisterCompareFaces.add(
                                new CompareFaceResult(mCurrentScore, dbIndex, detectIndex));
                    }
                }
            }
        }
        //比对结果
        Log.d(TAG,"比对输出 " + mNotRegisterCompareFaces.toString() + " "
                + mRegisterCompareFaces.toString());
        //把之前检测的人脸删除掉
        mDrawFaceInfoEntityList.clear();
        if(null != mRegisterCompareFaces && mRegisterCompareFaces.size()>0){
            for (CompareFaceResult item : mRegisterCompareFaces){
                DrawFaceInfoEntity drawFaceInfoEntity = new DrawFaceInfoEntity();
                //匹配到数据库的人脸索引
                int dbFaceIndex = item.getDbFaceIndex();
                //数据库匹配到的人脸实体
                FaceInfoEntity faceInfoEntity = mFaceInfoEntityList.get(dbFaceIndex);
                //匹配检测到的人脸索引
                int detectIndex = item.getDetectFaceIndex();
                FaceDetectInfo faceDetectInfo = faceDetectInfos.get(detectIndex);
                drawFaceInfoEntity.setFaceName(faceInfoEntity.getFaceName());
                drawFaceInfoEntity.setFaceAge(faceInfoEntity.getFaceAge());
                drawFaceInfoEntity.setFaceGender(faceInfoEntity.getFaceGender());
                drawFaceInfoEntity.setLivenress(faceDetectInfo.getFaceLiveness().getLiveness());
                Rect adjustRect = FaceConvertUtils.adjustRect(
                        faceDetectInfo.getFaceInfo().getRect(),
                        CameraManager.getInstance().getmPreviewWidth(),
                        CameraManager.getInstance().getmPreviewHeight(),
                        mFaceRectView.getWidth(),
                        mFaceRectView.getHeight(),
                        CameraManager.getInstance().getmCameraAngle(),
                        CameraManager.getInstance().getmCameraId());
                drawFaceInfoEntity.setFaceRect(adjustRect);
                mDrawFaceInfoEntityList.add(drawFaceInfoEntity);
            }
        }
        if(null != mNotRegisterCompareFaces && mNotRegisterCompareFaces.size()>0){
            for (CompareFaceResult item : mNotRegisterCompareFaces){
                DrawFaceInfoEntity drawFaceInfoEntity = new DrawFaceInfoEntity();
                //匹配到数据库最相似的人脸索引
                int dbFaceIndex = item.getDbFaceIndex();
                //数据库匹配到的人脸实体
                FaceInfoEntity faceInfoEntity = mFaceInfoEntityList.get(dbFaceIndex);
                //匹配检测到的人脸索引
                int detectIndex = item.getDetectFaceIndex();
                FaceDetectInfo faceDetectInfo = faceDetectInfos.get(detectIndex);
                drawFaceInfoEntity.setFaceName(faceInfoEntity.getFaceName());
                drawFaceInfoEntity.setFaceAge(faceInfoEntity.getFaceAge());
                drawFaceInfoEntity.setFaceGender(faceInfoEntity.getFaceGender());
                drawFaceInfoEntity.setLivenress(faceDetectInfo.getFaceLiveness().getLiveness());
                Rect adjustRect = FaceConvertUtils.adjustRect(
                        faceDetectInfo.getFaceInfo().getRect(),
                        CameraManager.getInstance().getmPreviewWidth(),
                        CameraManager.getInstance().getmPreviewHeight(),
                        mFaceRectView.getWidth(),
                        mFaceRectView.getHeight(),
                        CameraManager.getInstance().getmCameraAngle(),
                        CameraManager.getInstance().getmCameraId());
                drawFaceInfoEntity.setFaceRect(adjustRect);
                mDrawFaceInfoEntityList.add(drawFaceInfoEntity);
            }
        }
        //重新绘制人脸
       mFaceRectView.drawFaceRect(mDrawFaceInfoEntityList);
        //最后根据mRegisterCompareFaces和mNotRegisterCompareFaces去做业务
        if(mRegisterCompareFaces.size()>0){
            FaceInfoEntity faceInfoEntity = mFaceInfoEntityList.get(mRegisterCompareFaces.get(0).getDbFaceIndex());
            Toast.makeText(FaceDetectActivity.this,faceInfoEntity.getFaceName() + "您好",Toast.LENGTH_SHORT).show();
        }
        if(mNotRegisterCompareFaces.size() >0){
            Toast.makeText(FaceDetectActivity.this,"贵宾您好",Toast.LENGTH_SHORT).show();
        }

    }


    /**
     * 绘制人脸信息
     * @param faceDetectInfos
     */
    private void drawFaceInfo(List<FaceDetectInfo> faceDetectInfos) {
        mDrawFaceInfoEntityList.clear();
        for (int i = 0 ; i < faceDetectInfos.size(); i++){
            Rect adjustRect = FaceConvertUtils.adjustRect(
                    faceDetectInfos.get(i).getFaceInfo().getRect(),
                    CameraManager.getInstance().getmPreviewWidth(),
                    CameraManager.getInstance().getmPreviewHeight(),
                    mFaceRectView.getWidth(),
                    mFaceRectView.getHeight(),
                    CameraManager.getInstance().getmCameraAngle(),
                    CameraManager.getInstance().getmCameraId());

            mDrawFaceInfoEntityList.add(new DrawFaceInfoEntity(
                    "",
                    faceDetectInfos.get(i).getFaceAge(),
                    faceDetectInfos.get(i).getFaceGender(),
                    adjustRect,
                    faceDetectInfos.get(i).getFaceLiveness().getLiveness()));
        }
        Log.d(TAG,"绘制人脸 " + mDrawFaceInfoEntityList.toString());
        mFaceRectView.drawFaceRect(mDrawFaceInfoEntityList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initEngine();
        CameraParamet cameraParamet = new CameraParamet.Builder()
                .setCameraId(1)
                .setImageFormat(ImageFormat.JPEG)
                .setPictureSize(1920,1080)
                .setPreviewSize(1920,1080)
                .setSurfaceView(mSurfaceView)
                .Build();
        CameraManager.getInstance().openCamera(FaceDetectActivity.this, cameraParamet, new CameraManager.OnCameraListen() {
            @Override
            public void oppenSuss() {
                Log.d(TAG,"摄像头打开成功");
                CameraManager.getInstance().setPreviewListen(new CameraManager.OnCameraPreview() {
                    @Override
                    public void onPreview(byte[] bytes) {
                        detectFace(bytes);
                    }
                });
            }

            @Override
            public void oppenEror(String errorMsg) {
                Log.d(TAG,"摄像头打开失败");
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ArcFaceEngine.getInstance().destory();
    }


}

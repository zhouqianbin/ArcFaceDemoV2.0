package com.zhouqianbin.demo.ui;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.zhouqianbin.demo.R;
import com.zhouqianbin.demo.adapter.FaceRegisterRecyAdapter;
import com.zhouqianbin.demo.entity.FaceInfoEntity;
import com.zhouqianbin.demo.face.ArcFaceEngine;
import com.zhouqianbin.demo.face.ArcFaceParameter;
import com.zhouqianbin.demo.face.FaceDetectInfo;
import com.zhouqianbin.demo.face.OnEngineStateListen;
import com.zhouqianbin.demo.face.OnFaceDetectResult;
import com.zhouqianbin.demo.utils.FaceConvertUtils;

import org.litepal.LitePal;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;


public class FaceRegisterActivity extends AppCompatActivity {

    private static final String TAG = FaceRegisterActivity.class.getSimpleName();
    private List<FaceInfoEntity> mFaceInfoEntityList = new ArrayList<>();
    private FaceRegisterRecyAdapter mFaceRegisterRecyAdapter;
    private RecyclerView mRecyclerView;
    private AppCompatImageView mImgAddFace;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_register);

        mRecyclerView = findViewById(R.id.face_register_recy_list);
        mImgAddFace = findViewById(R.id.face_regis_img_add_face);

        mFaceRegisterRecyAdapter = new FaceRegisterRecyAdapter(R.layout.item_face_register_rect_list, mFaceInfoEntityList);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mFaceRegisterRecyAdapter);

        mImgAddFace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOptionsMenu(v);
            }
        });

    }


    private void initFaceEngine() {
        ArcFaceParameter faceParameter = new ArcFaceParameter.Builder()
                .setDetectAngle(ArcFaceEngine.ANGLE_0)
                .setDetectModel(ArcFaceEngine.MODEL_IMAGE)
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
     * 显示选项菜单
     * @param view
     */
    private void showOptionsMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        //通过MenuInflater进行填充数据
        MenuInflater mInflater = popupMenu.getMenuInflater();
        //把定义好的menuXML资源文件填充到popupMenu当中
        mInflater.inflate(R.menu.menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_album:
                        openSystemAlbum();
                        break;
                    case R.id.action_camera:
                        openSyatemCamera();
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    // 拍照回传码
    public final static int CAMERA_REQUEST_CODE = 0;
    // 相册选择回传吗
    public final static int GALLERY_REQUEST_CODE = 1;
    private Uri mUri;
    private File mFile;

    /**
     * 打开系统摄像头
     */
    private void openSyatemCamera() {
        Log.d(TAG, "openSyatemCamera");
        Intent intent = new Intent();
        //保存图片的文件路径
        mFile = new File(Environment.getExternalStorageDirectory()
                + "/woodman/" + System.currentTimeMillis() + ".jpg");
        if (!mFile.getParentFile().exists()) {
            mFile.getParentFile().mkdirs();
        }
        // 如果版本大于7.0使用FileProvider
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mUri = FileProvider.getUriForFile(this,
                    "com.zhouqianbin.fileprovider", mFile);
        } else {
            mUri = Uri.fromFile(mFile);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }

    /**
     * 打开系统相册
     */
    private void openSystemAlbum() {
        Log.d(TAG, "openSystemAlbum");
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        // 如果限制上传到服务器的图片类型时可以直接写如："image/jpeg 、 image/png等的类型" 所有类型则写 "image/*"
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/jpeg");
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case GALLERY_REQUEST_CODE: {
                    try {
                        Uri imageUri = data.getData();
                        if (imageUri != null) {
                            Bitmap bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                            detectFace(bitmap);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d(TAG, "文件未找到");
                    }
                    break;
                }
                case CAMERA_REQUEST_CODE: {
                    Uri imageUri = FileProvider.getUriForFile(this,
                            "com.zhouqianbin.fileprovider", mFile);
                    if (imageUri != null) {
                        Bitmap bitmap = null;
                        try {
                            bitmap = BitmapFactory.decodeStream(getContentResolver().openInputStream(imageUri));
                            detectFace(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                            Log.d(TAG, "文件未找到");
                        }
                    }
                    break;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    /**
     * 检测人脸
     *
     * @param bitmap
     */
    private void detectFace(Bitmap bitmap) {
        final Bitmap bitmapForNv21 = FaceConvertUtils.alignBitmapForNv21(bitmap);
        byte[] nv21 = FaceConvertUtils.bitmapToNv21(
                bitmapForNv21,
                bitmapForNv21.getWidth(),
                bitmapForNv21.getHeight());

        ArcFaceEngine.getInstance().detectFace(
                nv21,
                bitmapForNv21.getWidth(),
                bitmapForNv21.getHeight(),
                ArcFaceEngine.FORMAT_NV21,
                new OnFaceDetectResult() {
                    @Override
                    public void detectResult(List<FaceDetectInfo> faceDetectInfos) {
                        if (faceDetectInfos.size() > 0) {
                            showFaceInfoDialog(faceDetectInfos.get(0), bitmapForNv21);
                        }
                    }

                    @Override
                    public void detectError(String errorMsg) {
                        Toast.makeText(FaceRegisterActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void detectNotFace() {
                        Toast.makeText(FaceRegisterActivity.this, "检测不到人脸", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    /**
     * 显示人脸信息对话框
     *
     * @param faceDetectInfo
     * @param bitmapForNv21
     */
    private void showFaceInfoDialog(final FaceDetectInfo faceDetectInfo, final Bitmap bitmapForNv21) {
        final EditText editText = new EditText(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("请输入人脸性别")
                .setView(editText)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String faceName = editText.getText().toString();
                        if (TextUtils.isEmpty(faceName)) {
                            Toast.makeText(FaceRegisterActivity.this, "姓名不能为空", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        saveFaceInfo(faceName, faceDetectInfo, bitmapForNv21);
                    }
                });
        builder.show();
    }

    /**
     * 保存人脸信息
     * @param faceName
     * @param detectFaceInfoEntity
     * @param bitmapForNv21
     */
    private void saveFaceInfo(final String faceName,
                              final FaceDetectInfo detectFaceInfoEntity,
                              final Bitmap bitmapForNv21) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //根据检测到的人脸Rect剪裁图片
                Bitmap bitmap = FaceConvertUtils.imageCrop(bitmapForNv21,
                        detectFaceInfoEntity.getFaceInfo().getRect());
                if(null == bitmap){
                    Log.d(TAG,"裁剪人脸区域失败");
                    return;
                }
                Log.d(TAG,"裁剪人脸区域成功");
                //保存信息
                FaceInfoEntity faceInfoEntity = new FaceInfoEntity();
                faceInfoEntity.setFaceImage(FaceConvertUtils.biamap2byte(bitmap));
                faceInfoEntity.setFaceAge(detectFaceInfoEntity.getFaceAge());
                faceInfoEntity.setFaceName(faceName);
                faceInfoEntity.setFaceFeature(detectFaceInfoEntity.getFaceFeature().getFeatureData());
                faceInfoEntity.setFaceGender(detectFaceInfoEntity.getFaceGender());
                final boolean saveState = faceInfoEntity.save();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (saveState) {
                            loadFaceInfo();
                            Toast.makeText(FaceRegisterActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Toast.makeText(FaceRegisterActivity.this, "保存失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }

    /**
     * 加载数据库人脸信息
     */
    private void loadFaceInfo() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("正在拼命加载中...");
        progressDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                List<FaceInfoEntity> faceInfoEntityList = LitePal.findAll(FaceInfoEntity.class);
                Log.d(TAG,"查询数据库人脸 " + faceInfoEntityList.toString());
                mFaceInfoEntityList.clear();
                mFaceInfoEntityList.addAll(faceInfoEntityList);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mFaceRegisterRecyAdapter.notifyDataSetChanged();
                        progressDialog.hide();
                    }
                });
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initFaceEngine();
        loadFaceInfo();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ArcFaceEngine.getInstance().destory();
    }


}

package com.zhouqianbin.demo.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.zhouqianbin.demo.R;
import com.zhouqianbin.demo.entity.FaceInfoEntity;

import java.util.List;

public class FaceRegisterRecyAdapter extends BaseQuickAdapter<FaceInfoEntity,BaseViewHolder> {

    public FaceRegisterRecyAdapter(int layoutResId, @Nullable List<FaceInfoEntity> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, FaceInfoEntity item) {
        ImageView imageView = helper.getView(R.id.item_face_regis_img_user);
        byte[] faceImage = item.getFaceImage();
        try {
            Bitmap bitmap = BitmapFactory.decodeByteArray(faceImage, 0, faceImage.length);
            imageView.setImageBitmap(bitmap);
        }catch (Exception e){

        }

        helper.setText(R.id.item_face_regis_tv_name, item.getFaceName());
        helper.setText(R.id.item_face_regis_tv_age, String.valueOf(item.getFaceAge()));
        switch (item.getFaceGender()) {
            case 0:
                helper.setText(R.id.item_face_regis_tv_gender, "男");
                break;
            case 1:
                helper.setText(R.id.item_face_regis_tv_gender, "女");
                break;
            default:
                helper.setText(R.id.item_face_regis_tv_gender, "未知");
                break;
        }
    }

}

package com.lln.nikki.avatar.util;

import static com.lln.nikki.avatar.util.CommonUtils.toast;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;

import com.lln.nikki.avatar.constant.AvatarServiceInterfaceConstant;
import com.lln.nikki.avatar.model.AvatarType;

import java.io.OutputStream;

public class AvatarUtils {

    /**
     * 获取头像的url
     */
    public static String getAvatarUrl(@NonNull AvatarType type, @NonNull String uid, int num) {
        String base;
        switch (type) {
            case MAINLAND:
                base = AvatarServiceInterfaceConstant.MAINLAND_AVATAR_URL;
                break;
            case TAIWAN:
                base = AvatarServiceInterfaceConstant.TAIWAN_AVATAR_URL;
                break;
            case JAPAN:
                base = AvatarServiceInterfaceConstant.JAPAN_AVATAR_URL;
                break;
            default:
                base = "wtf";
                break;
        }
        return String.format(base, uid, uid, num);
    }


    public static void saveToGallery(Context context, Bitmap bitmap) {
        try {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, System.currentTimeMillis() + ".png");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
            boolean compress = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            outputStream.flush();
            outputStream.close();
            if (!compress) {
                throw new RuntimeException("保存失败");
            }
            toast(context, "保存成功");
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
        toast(context, "导出失败");
    }
}

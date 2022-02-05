package com.lln.nikki.avatar.view;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import com.github.chrisbanes.photoview.PhotoView;
import com.lln.nikki.avatar.R;

public class BigImageShowHelper {

    public static void showImage(Context context, Bitmap bitmap) {
        View view = LayoutInflater
                .from(context)
                .inflate(R.layout.big_image_show, null);
        PhotoView photoView = view.findViewById(R.id.big_image_show);
        photoView.setImageBitmap(bitmap);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = MATCH_PARENT;
        lp.height = MATCH_PARENT;
        lp.setTitle("big_image");
        WindowManager windowManager = context.getSystemService(WindowManager.class);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode != KeyEvent.KEYCODE_BACK) {
                return false;
            }
            if (event.getAction() == KeyEvent.ACTION_UP) {
                windowManager.removeViewImmediate(view);
            }
            return true;
        });
        windowManager.addView(view, lp);
        view.requestFocus();
    }
}

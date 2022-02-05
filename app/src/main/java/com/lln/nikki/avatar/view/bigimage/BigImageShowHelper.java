package com.lln.nikki.avatar.view.bigimage;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.viewpager2.widget.ViewPager2;

import com.lln.nikki.avatar.R;
import com.lln.nikki.avatar.model.Avatar;

import java.util.List;

public class BigImageShowHelper {

    /**
     * 显示大图
     *
     * @param avatarList 头像列表
     * @param index      现在要显示哪个
     */
    public static void showImage(Context context, List<Avatar> avatarList, int index) {
        WindowManager windowManager = context.getSystemService(WindowManager.class);

        View view = LayoutInflater
                .from(context)
                .inflate(R.layout.big_image_viewer, null);
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

        ViewPager2 viewPager = view.findViewById(R.id.big_image_viewer);
        viewPager.setAdapter(new BigImageShowAdapter(context, avatarList, () -> windowManager
                .removeViewImmediate(view)));
        viewPager.setCurrentItem(index, false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = MATCH_PARENT;
        lp.height = MATCH_PARENT;
        lp.setTitle("big_image");

        windowManager.addView(view, lp);
        view.requestFocus();
    }
}

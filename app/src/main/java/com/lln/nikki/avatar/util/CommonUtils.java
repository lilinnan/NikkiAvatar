package com.lln.nikki.avatar.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

public class CommonUtils {
    public static boolean checkUserIdIsValid(String userId) {
        if (userId == null) {
            return false;
        }
        return userId.matches("\\d{10,11}");
    }

    public static void toast(Context context, Object message) {
        if (message == null) {
            return;
        }
        Toast.makeText(context, message.toString(), Toast.LENGTH_SHORT).show();
    }


    public static void joinQQGroup(Context context) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com" +
                "%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26jump_from%3Dwebapi%26k%3DN" +
                "20FVhaS80shZ6v8NzMy-arUjbGuB1z8"));
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            toast(context, "打开QQ失败");
        }
    }

    public static void openLink(Context context, String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(intent);
    }

}

package com.lln.nikki.avatar;

import okhttp3.OkHttpClient;

public class ApplicationManager {
    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient();

    public static OkHttpClient getOkHttpClient() {
        return OK_HTTP_CLIENT;
    }

    public static int getVersionCode() {
        return 20220114;
    }


}

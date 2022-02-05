package com.lln.nikki.avatar.network.impl;

import com.lln.nikki.avatar.ApplicationManager;
import com.lln.nikki.avatar.constant.AppServiceInterfaceConstant;
import com.lln.nikki.avatar.model.UpdateMessage;
import com.lln.nikki.avatar.model.UsableMessage;
import com.lln.nikki.avatar.network.AppService;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.Request;
import okhttp3.Response;

/**
 * app服务
 */
public class AppServiceImpl implements AppService {
    private final Executor mExecutorService;
    private final UpdateMessage mUpdateMessage;
    private final UsableMessage mUsableMessage;

    public AppServiceImpl() {
        mExecutorService = Executors.newSingleThreadExecutor();
        mUpdateMessage = UpdateMessage.builder().build();
        mUsableMessage = UsableMessage.builder().build();
    }

    @Override
    public void checkUpdate(OnUpdateCheckCallback onUpdateCheckCallback) {
        mExecutorService.execute(() -> {
            try {
                String data = getTextFromUrl(AppServiceInterfaceConstant.APP_UPDATE_CHECK_URL);
                String[] split = Objects.requireNonNull(data).split(",");
                mUpdateMessage.setVersionCode(Integer.parseInt(split[0]));
                mUpdateMessage.setVersionName(split[1]);
                mUpdateMessage.setMessage(split[2]);
                mUpdateMessage.setUrl(split[3]);
                onUpdateCheckCallback.onCallback(mUpdateMessage);
            } catch (Exception e) {
                onUpdateCheckCallback.onCallback(null);
                e.printStackTrace();
            }
        });
    }

    @Override
    public void checkUsable(OnUsableCheckCallback onUsableCheckCallback) {
        mExecutorService.execute(() -> {
            try {
                String data = getTextFromUrl(AppServiceInterfaceConstant.APP_USABLE_CHECK_URL);
                if (Objects.requireNonNull(data).startsWith("1")) {
                    mUsableMessage.setUsable(true);
                } else {
                    mUsableMessage.setUsable(false);
                    mUsableMessage.setReason(data);
                }
                onUsableCheckCallback.onCallback(mUsableMessage);
            } catch (Exception e) {
                onUsableCheckCallback.onCallback(null);
                e.printStackTrace();
            }
        });
    }

    private String getTextFromUrl(String url) {
        try {
            Response response = ApplicationManager.getOkHttpClient()
                    .newCall(new Request.Builder().url(url).build())
                    .execute();
            return Objects.requireNonNull(response.body()).string();
        } catch (Exception e) {
            return null;
        }
    }
}

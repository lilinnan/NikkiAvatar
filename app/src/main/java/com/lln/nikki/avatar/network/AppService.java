package com.lln.nikki.avatar.network;

import com.lln.nikki.avatar.model.UpdateMessage;
import com.lln.nikki.avatar.model.UsableMessage;

public interface AppService {
    void checkUpdate(OnUpdateCheckCallback onUpdateCheckCallback);

    void checkUsable(OnUsableCheckCallback onUsableCheckCallback);


    interface OnUpdateCheckCallback {
        void onCallback(UpdateMessage updateMessage);
    }

    interface OnUsableCheckCallback {
        void onCallback(UsableMessage usableMessage);
    }
}

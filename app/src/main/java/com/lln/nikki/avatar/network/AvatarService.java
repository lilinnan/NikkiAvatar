package com.lln.nikki.avatar.network;

import com.lln.nikki.avatar.model.Avatar;
import com.lln.nikki.avatar.model.AvatarType;

public interface AvatarService {
    void getUserAvatar(String userId, AvatarType type, int threadNumber,
                       OnAvatarAddListener onAvatarAddListener, Runnable onSearchFinished);

    interface OnAvatarAddListener {
        void onAvatarAdded(Avatar avatar);
    }
}

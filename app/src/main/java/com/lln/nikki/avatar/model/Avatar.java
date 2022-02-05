package com.lln.nikki.avatar.model;

import android.graphics.Bitmap;

import lombok.Builder;
import lombok.Data;

/**
 * 头像对象
 */
@Builder
@Data
public class Avatar {
    private Bitmap avatar;
    private AvatarType type;
    private int number;
    private String userId;
}

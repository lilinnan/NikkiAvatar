package com.lln.nikki.avatar.model;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateMessage {
    private int versionCode;
    private String versionName;
    private String message;
    private String url;
}

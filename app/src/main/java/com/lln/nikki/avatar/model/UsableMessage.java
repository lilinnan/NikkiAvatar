package com.lln.nikki.avatar.model;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsableMessage {
    private boolean usable;
    private String reason;
}

package com.mirrorfly.gateway.dto;

import lombok.Data;

@Data
public class PrivacySettingsDto {

    private Integer id;

    private String username;

    private Integer seenReceipt;

    private Integer hideProfile;
}

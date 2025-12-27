package com.mirrorfly.gateway.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "privacy_settings")
@Data
public class PrivacySettingsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "username", nullable = false, length = 255)
    private String username;

    @Column(name = "seen_receipt")
    private Integer seenReceipt;

    @Column(name = "hide_profile")
    private Integer hideProfile;
}

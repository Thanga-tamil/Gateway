package com.mirrorfly.gateway.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mirrorfly.gateway.model.ChatSettings;

@Repository
public interface ChatSettingsRepo extends JpaRepository<ChatSettings, Integer> {

	ChatSettings findByLicenseKey(String licenseKey);

}

package com.mirrorfly.gateway.repo;

import com.mirrorfly.gateway.model.PrivacySettingsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrivacySettingsRepo extends JpaRepository<PrivacySettingsEntity, Integer> {

	PrivacySettingsEntity findByUsername(String contactNo);
}

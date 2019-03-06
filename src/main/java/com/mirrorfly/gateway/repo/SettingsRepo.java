package com.mirrorfly.gateway.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mirrorfly.gateway.model.Settings;

@Repository
public interface SettingsRepo extends JpaRepository<Settings, Long> {

}

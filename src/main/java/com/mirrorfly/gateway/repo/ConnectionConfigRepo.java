package com.mirrorfly.gateway.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mirrorfly.gateway.model.ConnectionConfig;

@Repository
public interface ConnectionConfigRepo extends JpaRepository<ConnectionConfig, Integer> {

}

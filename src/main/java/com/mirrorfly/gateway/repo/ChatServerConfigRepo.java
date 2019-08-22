package com.mirrorfly.gateway.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mirrorfly.gateway.model.ChatServerConfig;

@Repository
public interface ChatServerConfigRepo extends JpaRepository<ChatServerConfig, Integer> {

	@Query("Select c from ChatServerConfig c where c.configId=:configId")
	List<ChatServerConfig> findByChatConfigId(@Param(value = "configId") Integer configId);

}

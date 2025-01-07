package com.mirrorfly.gateway.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mirrorfly.gateway.model.ConnectionConfig;
import com.mirrorfly.gateway.model.EmailConfig;

@Repository
public interface ConnectionConfigRepo extends JpaRepository<ConnectionConfig, Integer> {

	@Query("SELECT c.emailConfig FROM ConnectionConfig c WHERE c.apiDomain = :apiDomain")
	EmailConfig getEmailConfigByApiDomain(@Param("apiDomain") String apiDomain);

}

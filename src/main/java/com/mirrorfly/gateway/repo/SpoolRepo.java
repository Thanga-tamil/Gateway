package com.mirrorfly.gateway.repo;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mirrorfly.gateway.model.Spool;

@Repository
public interface SpoolRepo extends JpaRepository<Spool, Long> {

	@Modifying
	@Transactional
	@Query("DELETE FROM Spool s WHERE s.username = :username and s.deviceId = :deviceId")
	public void deleteOfflilneRecordsByUsernameAndDeviceId(@Param("username") String username, @Param("username") String deviceId);

}

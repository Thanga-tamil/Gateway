package com.mirrorfly.gateway.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mirrorfly.gateway.model.PrivacyUsersList;

@Repository
public interface PrivacyUsersListRepo extends JpaRepository<PrivacyUsersList, Long> {

	List<PrivacyUsersList> findByUsername(String username);

	@Modifying
	@Transactional
	@Query("DELETE FROM PrivacyUsersList p WHERE p.username = :username")
	void deleteByUsername(@Param("username") String username);

}

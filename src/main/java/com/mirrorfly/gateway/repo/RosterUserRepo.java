package com.mirrorfly.gateway.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mirrorfly.gateway.model.RosterUser;
import com.mirrorfly.gateway.model.RosterUserKey;

@Repository
public interface RosterUserRepo extends JpaRepository<RosterUser, RosterUserKey> {

	@Transactional
	@Modifying
	@Query("Delete from RosterUser r where r.rosterUserKey.username=:username or r.jidUsername=:username")
	public void deleteRosterList(@Param(value = "username") String username);

}

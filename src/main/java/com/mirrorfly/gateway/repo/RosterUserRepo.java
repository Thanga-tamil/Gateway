package com.mirrorfly.gateway.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mirrorfly.gateway.model.RosterUser;
import com.mirrorfly.gateway.model.RosterUserKey;

import java.util.List;
import java.util.Set;

@Repository
public interface RosterUserRepo extends JpaRepository<RosterUser, RosterUserKey> {

	@Transactional
	@Modifying
	@Query("Delete from RosterUser r where r.rosterUserKey.username=:username or r.jidUsername=:username")
	public void deleteRosterList(@Param(value = "username") String username);

    @Query(value = "select username from rosterusers where username in ( :usernames ) and jid = :jid ", nativeQuery = true)
    List<String> findAllRosters(@Param("usernames") Set<String> usernames, @Param("jid") String jid);
}

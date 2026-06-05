package com.mirrorfly.gateway.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mirrorfly.gateway.model.TokenDetail;

@Repository
public interface TokenRepo extends JpaRepository<TokenDetail, Long> {

	TokenDetail findByToken(String header);

	TokenDetail findTop1ByTokenOrderByIdDesc(String header);

	Optional<TokenDetail> findTop1ByUsernameAndDevicetypeOrderByIdDesc(String username, String devicetype);

	@Modifying
	@Transactional
	@Query(value = "DELETE FROM chat_access_tokens "
			+ "WHERE username = :username "
			+ "AND device_type NOT IN (SELECT device_id FROM chat_users_details WHERE username = :username) ",
			nativeQuery = true)
	int deleteByUnUsedTokenByUsername(String username);

}

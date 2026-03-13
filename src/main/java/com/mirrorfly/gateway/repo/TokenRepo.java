package com.mirrorfly.gateway.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mirrorfly.gateway.model.TokenDetail;

@Repository
public interface TokenRepo extends JpaRepository<TokenDetail, Long> {

	public TokenDetail findByToken(String header);

	public TokenDetail findTop1ByTokenOrderByIdDesc(String header);

	public Optional<TokenDetail> findFirstByUsername(String username);

}

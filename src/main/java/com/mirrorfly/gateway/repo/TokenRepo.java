package com.mirrorfly.gateway.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mirrorfly.gateway.model.TokenDetail;

@Repository
public interface TokenRepo extends JpaRepository<TokenDetail, Long> {

	public TokenDetail findByToken(String header);

}

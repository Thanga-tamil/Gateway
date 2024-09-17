package com.mirrorfly.gateway.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mirrorfly.gateway.model.OtpTokens;

@Repository
public interface OtpTokensRepository extends JpaRepository<OtpTokens, Integer> {

	Optional<OtpTokens> findByEmail(String email);

}

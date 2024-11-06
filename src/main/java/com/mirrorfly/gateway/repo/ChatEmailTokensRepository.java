package com.mirrorfly.gateway.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mirrorfly.gateway.model.ChatEmailTokens;

@Repository
public interface ChatEmailTokensRepository extends JpaRepository<ChatEmailTokens, Integer> {

	Optional<ChatEmailTokens> findByEmail(String email);

}

package com.mirrorfly.gateway.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mirrorfly.gateway.model.EmailToken;

public interface EmailTokenRepo extends JpaRepository<EmailToken, String> {

	EmailToken findByEmailIdAndEmailTemplate(String email, String template);

	EmailToken findByToken(String token);

	EmailToken findByTokenAndStatus(String token, String status);

}

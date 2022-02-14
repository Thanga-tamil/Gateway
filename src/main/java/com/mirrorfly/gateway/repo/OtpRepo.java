package com.mirrorfly.gateway.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mirrorfly.gateway.model.Otp;

@Repository
public interface OtpRepo extends JpaRepository<Otp, Long> {
 
}

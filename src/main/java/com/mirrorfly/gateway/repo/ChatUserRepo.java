package com.mirrorfly.gateway.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mirrorfly.gateway.model.ChatUser;

@Repository
public interface ChatUserRepo extends JpaRepository<ChatUser, String> {

	public ChatUser findByMobileNumber(@Param("mobileNumber") String mobileNumber);

}

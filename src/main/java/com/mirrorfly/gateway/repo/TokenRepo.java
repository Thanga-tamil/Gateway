package com.mirrorfly.gateway.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mirrorfly.gateway.model.TokenDetail;

@Repository
public interface TokenRepo extends JpaRepository<TokenDetail, Long> {

	List<TokenDetail> findByToken(String header);

	List<TokenDetail> findTop1ByTokenOrderByIdDesc(String header);

	List<TokenDetail> findByUsernameAndDevicetype(String username, String deviceId);
}

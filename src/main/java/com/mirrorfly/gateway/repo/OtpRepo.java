package com.mirrorfly.gateway.repo;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mirrorfly.gateway.model.Otp;

@Repository
public interface OtpRepo extends JpaRepository<Otp, Long> {

	@Query("Select o from Otp o where o.mobileNumber=:mobileNumber and o.otp=:otp order by o.id desc")
	public List<Otp> findOtpByMobileNumber(@Param("mobileNumber") String mobileNumber, @Param("otp") String otp,
			Pageable pageable);

}

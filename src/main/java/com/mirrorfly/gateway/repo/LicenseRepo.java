package com.mirrorfly.gateway.repo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mirrorfly.gateway.model.License;

@Repository
public interface LicenseRepo extends JpaRepository<License,Long> {

	License findByActivationKeyAndSdk(@Param("activationKey") String activationKey,@Param("sdk") String sdk);

}

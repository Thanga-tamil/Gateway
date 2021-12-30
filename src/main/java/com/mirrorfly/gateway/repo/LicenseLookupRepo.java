package com.mirrorfly.gateway.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mirrorfly.gateway.model.LicenseLookup;

@Repository
public interface LicenseLookupRepo extends JpaRepository<LicenseLookup,Long> {

}

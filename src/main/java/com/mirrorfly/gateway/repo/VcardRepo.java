package com.mirrorfly.gateway.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mirrorfly.gateway.model.Vcard;

@Repository
public interface VcardRepo extends CrudRepository<Vcard, String> {

	public Vcard findByUsername(@Param("username") String username);

}

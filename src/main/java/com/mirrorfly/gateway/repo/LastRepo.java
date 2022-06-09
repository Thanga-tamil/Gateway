package com.mirrorfly.gateway.repo;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.mirrorfly.gateway.model.Last;

@Repository
public interface LastRepo extends CrudRepository<Last, String> {
 
}

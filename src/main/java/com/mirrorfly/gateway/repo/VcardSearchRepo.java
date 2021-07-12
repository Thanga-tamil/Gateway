package com.mirrorfly.gateway.repo;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VcardSearchRepo extends PagingAndSortingRepository<VcardSearch, String> {

	public VcardSearch findByUsername(String username);

}

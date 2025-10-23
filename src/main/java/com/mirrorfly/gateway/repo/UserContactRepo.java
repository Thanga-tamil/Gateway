package com.mirrorfly.gateway.repo;

import com.mirrorfly.gateway.model.UserContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserContactRepo extends JpaRepository<UserContact, String>, CrudRepository<UserContact, String> {

	@Query("SELECT uc FROM UserContact uc WHERE uc.mobileNumber LIKE CONCAT('%', :mobileNumber) group by uc.username")
    List<UserContact> findByMobileNumber(@Param("mobileNumber") String mobileNumber);

}

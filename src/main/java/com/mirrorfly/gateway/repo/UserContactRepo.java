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

    // Expects reversed mobile number as input for optimal index usage
    // Example: If mobile is "9998887771", pass "1777888999"
    @Query("SELECT uc FROM UserContact uc WHERE uc.mobileNumberReversedTrim LIKE CONCAT(:reversedMobileNumber, '%') GROUP BY uc.username")
    List<UserContact> findByMobileNumber(@Param("reversedMobileNumber") String reversedMobileNumber);

}
package com.mirrorfly.gateway.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mirrorfly.gateway.model.UserContact;


@Repository
public interface UserContactRepo extends JpaRepository<UserContact, String>, CrudRepository<UserContact, String> {

	@Transactional
	@Modifying
	@Query("DELETE FROM UserContact uc WHERE uc.username = :username")
	public void deleteContactListByUsername(@Param("username") String username);

	@Query("Select uc from UserContact uc where uc.mobileNumber=:username")
	public List<UserContact> getUserContactListByMobileNo(@Param(value = "username") String username);

	@Query("Select uc.mobileNumber from UserContact uc where uc.username=:username")
	public List<String> getUserContactListByUsername(@Param(value = "username") String username);

	@Transactional
	@Modifying
	@Query("Delete from UserContact uc where uc.mobileNumber=:username")
	public void deleteUserContactListByMobileNo(@Param(value = "username") String username);

	@Query("SELECT uc.mobileNumber FROM UserContact uc WHERE uc.username = :username AND uc.mobileNumber IN :mobiles")
	public List<String> findExistingMobileNumbers(@Param("username") String username, @Param("mobiles") List<String> mobiles);
	
	@Query("SELECT uc FROM UserContact uc WHERE uc.mobileNumber LIKE CONCAT('%', :mobileNumber)")
	List<UserContact> findByMobileNumber(@Param("mobileNumber") String mobileNumber);

}

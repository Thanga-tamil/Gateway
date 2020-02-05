package com.mirrorfly.gateway.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mirrorfly.gateway.model.AdminUser;


@Repository
public interface AdminUserRepo extends JpaRepository<AdminUser, String> {

	/*
	 * @Query("Select u from AdminUser u where u.deletedAt is NULL") public
	 * List<AdminUser> getAdminUserDetailList();
	 * 
	 * @Query("Select u from AdminUser u where u.userRole.roleId=:roleId and u.deletedAt is NULL"
	 * ) public List<AdminUser> getAdminUserDetailList(@Param(value = "roleId")
	 * Integer roleId);
	 * 
	 * @Query("Select count(u.username) from AdminUser u where u.emailId=:emailId and u.deletedAt is NULL"
	 * ) public Long checkEmailAlreadyExist(@Param(value = "emailId") String
	 * emailId);
	 * 
	 * @Query("Select count(u.username) from AdminUser u where u.username=:username and u.deletedAt is NULL"
	 * ) public Long checkUserNameAlreadyExist(@Param(value = "username") String
	 * username);
	 */

	@Query("Select u from AdminUser u where u.username=:username and u.isDeleted=0 ")
	public AdminUser findByUsername(@Param(value = "username") String username);
	
	@Query("Select u from AdminUser u where u.emailId=:emailId ")
	public AdminUser findByEmailId(@Param(value = "emailId") String emailId);
	

	/*
	 * @Query("Select count(u.username) from AdminUser u where u.emailId=:emailId and u.username!=:username and u.deletedAt is NULL"
	 * ) public Long checkEmailAlreadyExist(@Param(value = "emailId") String
	 * emailId,
	 * 
	 * @Param(value = "username") String username);
	 * 
	 * @Query("Select count(u.username) from AdminUser u where u.userRole.roleId=:roleId and u.deletedAt is NULL"
	 * ) public Long countUsersByRole(@Param(value = "roleId") Integer roleId);
	 * 
	 * @Query("Select count(u.username) from AdminUser u where  u.deletedAt is NULL"
	 * ) public Long countUsers();
	 */
}

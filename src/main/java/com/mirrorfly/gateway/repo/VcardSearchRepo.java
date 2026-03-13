package com.mirrorfly.gateway.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mirrorfly.gateway.model.VcardSearch;

@Repository
public interface VcardSearchRepo extends JpaRepository<VcardSearch, String> {

	public VcardSearch findByUsername(String username);

	@Query(value = "SELECT v.username FROM vcard_search v WHERE URL_DECODE(CONVERT(AES_DECRYPT(from_base64(v.email), LEFT(SHA2(v.username, 256), 32), :iv) USING utf8)) = :email OR v.email = :email LIMIT 1 ", nativeQuery = true)
	String findUsernameByEmail(@Param("email") String email, @Param("iv") String iv);

}

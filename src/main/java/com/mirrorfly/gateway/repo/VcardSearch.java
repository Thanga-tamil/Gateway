package com.mirrorfly.gateway.repo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Entity
@Table(name = "vcard_search")
@Data
public class VcardSearch {

	@Id
	@Column(name = "username")
	private String username;

	@Column(name = "nickname")
	private String nickName;

	@Column(name = "email")
	private String email;

	@Column(name = "image")
	private String image;

	@Column(name = "status")
	private String status;
	
	@Column(name = "is_block")
	private int isBlock;

	@Column(name = "mobilenumber")
	private String mobileNumber;

}

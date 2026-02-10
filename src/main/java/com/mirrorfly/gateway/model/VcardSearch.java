package com.mirrorfly.gateway.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "vcard_search")
@Data
public class VcardSearch implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "username")
	private String username;

	@Column(name = "lusername")
	private String lusername;

	@Column(name = "fn")
	private String fn = "";

	@Column(name = "lfn")
	private String lfn = "";

	@Column(name = "family")
	private String family = "";

	@Column(name = "lfamily")
	private String lfamily = "";

	@Column(name = "given")
	private String given = "";

	@Column(name = "lgiven")
	private String lgiven = "";

	@Column(name = "middle")
	private String middle = "";

	@Column(name = "lmiddle")
	private String lmiddle = "";

	@Column(name = "nickname")
	private String nickname = "";

	@Column(name = "lnickname")
	private String lnickname = "";

	@Column(name = "bday")
	private String bday = "";

	@Column(name = "lbday")
	private String lbday = "";

	@Column(name = "ctry")
	private String ctry = "";

	@Column(name = "lctry")
	private String lctry = "";

	@Column(name = "locality")
	private String locality = "";

	@Column(name = "llocality")
	private String llocality = "";

	@Column(name = "email")
	private String email = "";

	@Column(name = "lemail")
	private String lemail = "";

	@Column(name = "image")
	private String image = "";

	@Column(name = "limage")
	private String limage = "";

	@Column(name = "status")
	private String status = "";

	@Column(name = "lstatus")
	private String lstatus = "";

	@Column(name = "mobilenumber")
	private String mobilenumber = "";

	@Column(name = "lmobilenumber")
	private String lmobilenumber = "";

	@Column(name = "orgname")
	private String orgname = "";

	@Column(name = "lorgname")
	private String lorgname = "";

	@Column(name = "orgunit")
	private String orgunit = "";

	@Column(name = "lorgunit")
	private String lorgunit = "";

	@Column(name = "is_block")
	private int isBlock = 0;

	@Column(name = "msg_disappear")
	private Integer msgDisappear = 0;

	@Column(name = "disp_updated_at")
	private LocalDateTime dispUpdatedAt;

	@Column(name = "updated_by")
	private String updatedBy;

}

package com.mirrorfly.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mirrorfly.gateway.model.Vcard;
import com.mirrorfly.gateway.model.VcardSearch;
import com.mirrorfly.gateway.repo.VcardRepo;
import com.mirrorfly.gateway.repo.VcardSearchRepo;

@Service
public class VcardService {

	@Autowired
	private VcardRepo vcardRepo;

	@Autowired
	private VcardSearchRepo vcardSearchRepo;
	
	public VcardSearch findVcardSearchByUserName(String username) {
		return vcardSearchRepo.findByUsername(username);
	}

	public void updateVcard(VcardSearch vcardSearch, Vcard vcard) {
		StringBuilder sb = new StringBuilder();
		sb.append("<vCard xmlns='vcard-temp'>");
		if (vcardSearch.getImage() != null) {
			sb.append("<image>");
			sb.append(vcardSearch.getImage());
			sb.append("</image>");
		} else {
			sb.append("<image/>");
		}
		if (vcardSearch.getNickname() != null) {
			sb.append("<nickName>");
			sb.append(vcardSearch.getNickname());
			sb.append("</nickName>");
		} else {
			sb.append("<nickName/>");
		}
		if (vcardSearch.getEmail() != null) {
			sb.append("<email>");
			sb.append(vcardSearch.getEmail());
			sb.append("</email>");
		} else {
			sb.append("<email/>");
		}
		if (vcardSearch.getStatus() != null) {
			sb.append("<status>");
			sb.append(vcardSearch.getStatus());
			sb.append("</status>");
		} else {
			sb.append("<status/>");
		}
		if (vcardSearch.getStatus() != null) {
			sb.append("<mobileNumber>");
			sb.append(vcardSearch.getMobilenumber());
			sb.append("</mobileNumber>");
		} else {
			sb.append("<mobileNumber/>");
		}
		sb.append("</vCard>");
		vcard.setUsername(vcardSearch.getUsername());
		vcard.setVcardXml(sb.toString());
		vcardRepo.save(vcard);
	}

	public VcardSearch updateVcardSearchDetails(String username) {
		VcardSearch vcardSearch = new VcardSearch();
		vcardSearch.setEmail("");
		vcardSearch.setLemail("");
		vcardSearch.setMobilenumber("");
		vcardSearch.setLmobilenumber("");
		vcardSearch.setIsBlock(0);
		vcardSearch.setNickname("");
		vcardSearch.setLnickname("");
		vcardSearch.setUsername(username);
		vcardSearch.setLusername(username);
		vcardSearchRepo.save(vcardSearch);
		return null;
	}

}

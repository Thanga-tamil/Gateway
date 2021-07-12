package com.mirrorfly.gateway.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.mirrorfly.gateway.config.ApplicationProperties;
import com.mirrorfly.gateway.dto.SandboxContactDto;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.model.ChatUser;
import com.mirrorfly.gateway.model.RosterUser;
import com.mirrorfly.gateway.model.RosterUserKey;
import com.mirrorfly.gateway.repo.ChatUserRepo;
import com.mirrorfly.gateway.repo.RosterUserRepo;
import com.mirrorfly.gateway.repo.VcardSearch;
import com.mirrorfly.gateway.repo.VcardSearchRepo;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.util.GatewayConstants;

@Service
public class SandboxContactService {

	@Autowired
	ApplicationProperties applicationProperties;

	@Autowired
	private ChatUserRepo chatUserRepo;

	@Autowired
	private VcardSearchRepo vcardSearchRepo;

	@Autowired
	private RosterUserRepo rosterUserRepo;

	public Response<String> syncContactsNew(SandboxContactDto synccontactDto) {
		if (synccontactDto.getLicenseKey() == null || synccontactDto.getLicenseKey().trim().isEmpty()) {
			throw new BadDataException(GatewayConstants.USER_LICENSEKEY_EMPTY);
		}
		if (synccontactDto.getUsername() == null || synccontactDto.getUsername().trim().isEmpty()) {
			throw new BadDataException(GatewayConstants.USER_NAME_EMPTY);
		}
		rosterUserRepo.deleteRosterList(synccontactDto.getUsername());
		addNewContacts(synccontactDto);
		Response<String> response = new Response<>();
		response.setStatus(HttpStatus.OK.value());
		response.setMessage(GatewayConstants.CONTACT_ADD_SUCCESS);
		return response;
	}

	private void addNewContacts(SandboxContactDto synccontactDto) {
		List<ChatUser> vcards = chatUserRepo.findBylicenseKey(synccontactDto.getLicenseKey());
		VcardSearch vcardDetails = vcardSearchRepo.findByUsername(synccontactDto.getUsername());
		String domain = applicationProperties.getXmppDomain();
		if (vcardDetails != null && vcards != null && !vcards.isEmpty()) {
			List<RosterUser> roasterList = new ArrayList<>();
			vcards.stream().forEach(vcard -> {
				if (!synccontactDto.getUsername().equals(vcard.getUsername())) {
					RosterUser rUser = new RosterUser();
					StringBuilder result = new StringBuilder(synccontactDto.getUsername() + "@" + domain);
					RosterUserKey rUserKey = new RosterUserKey(vcard.getUsername(), result.toString());
					rUser.setRosterUserKey(rUserKey);
					rUser.setServer('N');
					rUser.setAsk('N');
					rUser.setJidUsername(synccontactDto.getUsername());
					rUser.setNick(vcardDetails.getNickName() != null ? vcardDetails.getNickName() : "Mirrorfly");
					rUser.setSubscription('B');
					rUser.setType("item");
					roasterList.add(rUser);

					rUser = new RosterUser();
					result = new StringBuilder(vcard.getUsername() + "@" + domain);
					rUserKey = new RosterUserKey(synccontactDto.getUsername(), result.toString());
					rUser.setRosterUserKey(rUserKey);
					rUser.setServer('N');
					rUser.setAsk('N');
					rUser.setJidUsername(vcard.getUsername());
					rUser.setNick(vcard.getName() != null ? vcard.getName() : "Mirrorfly");
					rUser.setSubscription('B');
					rUser.setType("item");
					roasterList.add(rUser);
				}
			});
			rosterUserRepo.saveAll(roasterList);
		}

	}
}

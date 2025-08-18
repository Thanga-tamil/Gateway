package com.mirrorfly.gateway.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.mirrorfly.gateway.datasourcerouter.AbstractRoutingDataSourceImpl;
import com.mirrorfly.gateway.datasourcerouter.SwitchDataSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.exception.UserUnAuthorizedException;
import com.mirrorfly.gateway.model.ConnectionConfig;
import com.mirrorfly.gateway.repo.VcardSearchRepo;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.util.GatewayConstants;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SsoAuthService {

	private final RestTemplate restTemplate;

	private final CommonService commonService;

	private final VcardSearchRepo vcardSearchRepo;

	@SwitchDataSource(value = "[0]")
	public Response<Object> validateAndFetchUsername(String userdomain, Map<String, Object> request) {

		String ssoToken = Optional.ofNullable((String) request.get("ssoToken")).filter(StringUtils::isNotBlank)
				.orElseThrow(() -> new BadDataException(GatewayConstants.SSO_TOKEN_NOT_FOUND));

		String ssoType = Optional.ofNullable((String) request.get("ssoType")).filter(StringUtils::isNotBlank)
				.orElseThrow(() -> new BadDataException(GatewayConstants.SSO_TYPE_NOT_FOUND));

		ConnectionConfig config = commonService.getConnectionConfigByDomain(userdomain);

		String email = validateAndFetchEmail(ssoType, ssoToken);

		if (email == null)
			throw new UserUnAuthorizedException("Invalid SSO token");
		/*
		 * Enable Encryption mode aes-256-cbc in the MySQL Server
		 */
		AbstractRoutingDataSourceImpl.setDatabaseName(userdomain);

		commonService.enableEncryptionMode();

		Map<String, Object> response = new HashMap<>();
		String username = vcardSearchRepo.findUsernameByEmail(email, config.getLicenseKey());

		if (StringUtils.isNotBlank(username))
			response.put("isExistingUser", Boolean.TRUE);
		else
			response.put("isExistingUser", Boolean.FALSE);
		response.put("username", username);

		return new Response<>(HttpStatus.OK.value(), response, GatewayConstants.SSO_VERIFY_SUCCESS);
	}

	public String validateAndFetchEmail(String ssoType, String token) {
		switch (ssoType.toLowerCase()) {
		case "google":
			return validateGoogleToken(token);
		case "outlook":
			return validateOutlookToken(token);
		default:
			throw new BadDataException("Unsupported SSO type: " + ssoType);
		}
	}

	private String validateGoogleToken(String token) {
		try {
			String validationUrl = GatewayConstants.GOOGLE_SSO_AUTH_API + "?id_token=" + token;

			ResponseEntity<Map<String, Object>> response = restTemplate.exchange(validationUrl, HttpMethod.GET, null,
					new ParameterizedTypeReference<Map<String, Object>>() {
					});
			if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
				return (String) response.getBody().get("email");
			}
		} catch (Exception e) {
			log.error("Error validating Google token: " + e.getMessage());
		}
		return null;
	}

	private String validateOutlookToken(String token) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "Bearer " + token);
			headers.set("Accept", "application/json");

			HttpEntity<String> entity = new HttpEntity<>(headers);

			ResponseEntity<Map<String, Object>> response = restTemplate.exchange(GatewayConstants.OUTLOOK_SSO_AUTH_API,
					HttpMethod.GET, entity, new ParameterizedTypeReference<Map<String, Object>>() {
					});

			if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
				return (String) response.getBody().get("userPrincipalName");
			}
		} catch (Exception e) {
			log.error("Error validating Outlook token: " + e.getMessage());
		}
		return null;
	}

}

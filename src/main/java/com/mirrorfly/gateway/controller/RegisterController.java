package com.mirrorfly.gateway.controller;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirrorfly.gateway.config.ApplicationProperties;
import com.mirrorfly.gateway.dto.RegisterDto;
import com.mirrorfly.gateway.dto.RegisterInfoDto;
import com.mirrorfly.gateway.dto.RegisterRequestDto;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.service.RegisterService;
import com.mirrorfly.gateway.util.AESEncryption;
import com.mirrorfly.gateway.util.GatewayConstants;

@RestController
@RequestMapping("/api/v1/register")
public class RegisterController {

	@Autowired
	private RegisterService registerService;

	@Autowired
	private ApplicationProperties applicationProperties;

	/**
	 * Function to validate username and password by passing through loginService.
	 * This function return a response stating that login success or error with
	 * error code and error message.
	 * 
	 * @param login this has username and password.
	 * @return success full response login screen with token for authorization.
	 */

	@GetMapping("/info")
	public @ResponseBody Response<RegisterInfoDto> getRegisterInfo() {
		return registerService.getRegisterInfo();
	}

	/**
	 * Function to validate username and password by passing through loginService.
	 * This function return a response stating that login success or error with
	 * error code and error message.
	 * 
	 * @param login this has username and password.
	 * @return success full response login screen with token for authorization.
	 */

	@PostMapping
	public @ResponseBody Response<String> registerNewUser(
			@Validated @RequestBody RegisterRequestDto registerRequestDto) {
		String decyptedString = AESEncryption.decryptString(registerRequestDto.getRegisterData(),
				applicationProperties.getLicenseKey() + applicationProperties.getLicenseKey()
						+ applicationProperties.getLicenseKey());
		ObjectMapper mapper = new ObjectMapper();
		RegisterDto registerDto;
		try {
			registerDto = mapper.readValue(URLDecoder.decode(decyptedString, StandardCharsets.UTF_8.toString()), RegisterDto.class);
		} catch (JsonProcessingException | UnsupportedEncodingException e) {
			throw new BadDataException(GatewayConstants.LICENSEKEY_INVALID);
		}
		return registerService.registerNewUser(registerDto);
	}

}

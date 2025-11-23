package com.mirrorfly.gateway.controller;

import com.mirrorfly.gateway.dto.RegisterResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mirrorfly.gateway.config.ApplicationProperties;
import com.mirrorfly.gateway.dto.RegisterInfoDto;
import com.mirrorfly.gateway.dto.RegisterRequestDto;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.service.RegisterService;
import com.mirrorfly.gateway.util.GatewayConstants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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
	public Response<RegisterInfoDto> getRegisterInfo(@RequestHeader String userdomain) {
		return registerService.getRegisterInfo(userdomain);
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
	public Response<String> registerNewUser(@Validated @RequestBody RegisterRequestDto registerRequestDto,
			@RequestHeader String userdomain) {
		return registerService.registerNewUser(userdomain, registerRequestDto);
	}

	/*
	* Function to check whether to skip OTP for a mobile number or not.
	* Based on the response the client will either skip OTP or validate OTP.
	* @param mobileNumber
	* @return Response with status SKIP_OTP or VALIDATE_OTP
	*/
	@GetMapping("/skip-otp")
	public Response<String> skipOtp(
		@RequestHeader String userdomain,
		@RequestParam(required = true) String mobileNumber
	) {
		Response<String> response = new Response<>();
		
		log.info("skipOtp - getSkipOTPMobileNumbers: {}", applicationProperties.getSkipOTPMobileNumbers());
		if(mobileNumber == null || mobileNumber.isEmpty()) {
			response.setStatus(400);
			response.setMessage(GatewayConstants.DATA_RETRIEVED);
			return response;
		}

		if(applicationProperties.getSkipOTPMobileNumbers() != null && applicationProperties.getSkipOTPMobileNumbers().contains(mobileNumber)) {
			response.setStatus(200);
			response.setMessage("SKIP_OTP");
			return response;
		}
		response.setStatus(200);
		response.setMessage("VALIDATE_OTP");
		return response;
	}

}

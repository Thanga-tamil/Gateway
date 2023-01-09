package com.mirrorfly.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mirrorfly.gateway.dto.VerifyUserDto;
import com.mirrorfly.gateway.dto.VerifyUserResponse;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.service.OtpService;

import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/api/v1")
public class OtpController {

	@Autowired
	private OtpService otpService;

	/**
	 * Function to validate mobilenumber by passing through otp service. This
	 * function return a response stating that otp is sent success or error with
	 * error code and error message.
	 * 
	 * @param otp this has mobilenumber.
	 * @return success full response verify otp for authorization.
	 */
	@PostMapping("/verifyuser")
	public @ResponseBody Response<VerifyUserResponse> verifyUser(@RequestBody VerifyUserDto userDto,
			@ApiParam(hidden = true) @RequestHeader(value = "userdomain") String userdomain) {
		return otpService.verifyUser(userdomain, userDto);
	}

	@PostMapping("/verifyfcm")
	public @ResponseBody Response<VerifyUserResponse> verifyfcm(@RequestBody VerifyUserDto userDto,
			@ApiParam(hidden = true) @RequestHeader(value = "userdomain") String userdomain) {
		return otpService.verifyUser(userdomain, userDto);
	}

}

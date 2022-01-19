package com.mirrorfly.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mirrorfly.gateway.dto.OtpResponseDto;
import com.mirrorfly.gateway.dto.VerifyUserDto;
import com.mirrorfly.gateway.dto.VerifyUserResponse;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.service.OtpService;

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
//	@GetMapping("/requestotp")
//	public @ResponseBody Response<OtpResponseDto> requestOtp(
//			@RequestParam(required = true, name = "mobileNumber") String mobileNumber) {
//		return otpService.requestOtp(mobileNumber);
//	}

	/**
	 * Function to validate mobilenumber by passing through otp service. This
	 * function return a response stating that otp is sent success or error with
	 * error code and error message.
	 * 
	 * @param otp this has mobilenumber.
	 * @return success full response verify otp for authorization.
	 */
	@PostMapping("/verifyuser")
	public @ResponseBody Response<VerifyUserResponse> verifyUser(@RequestBody VerifyUserDto userDto) {
		return otpService.verifyUser(userDto);
	}
	
	@PostMapping("/verifyfcm")
	public @ResponseBody Response<VerifyUserResponse> verifyfcm(@RequestBody VerifyUserDto userDto) {
		return otpService.verifyUser(userDto);
	}

}

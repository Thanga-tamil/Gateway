package com.mirrorfly.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mirrorfly.gateway.dto.OtpDto;
import com.mirrorfly.gateway.dto.OtpResponseDto;
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
	@PostMapping("/requestotp")
	public @ResponseBody Response<OtpResponseDto> requestOtp(@RequestBody OtpDto otpDto) {
		return otpService.requestOtp(otpDto);
	}

}

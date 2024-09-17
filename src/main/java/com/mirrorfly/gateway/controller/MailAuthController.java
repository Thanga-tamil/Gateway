package com.mirrorfly.gateway.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.service.MailAuthService;

@RestController
@RequestMapping("api/v1/mail")
public class MailAuthController {

	@Autowired
	private MailAuthService mailAuthService;

	@PostMapping("send-otp")
	public Response<Object> sendMailOtp(@RequestHeader(value = "userdomain") String userdomain,
			@RequestBody Map<String, String> request) {
		return mailAuthService.sendOtpViaMail(userdomain, request.get("email"));
	}

	@PostMapping("verify-otp")
	public Response<Object> verifyMailOtp(@RequestHeader(value = "userdomain") String userdomain,
			@RequestBody Map<String, String> request) {
		return mailAuthService.validateEmailOtp(userdomain, request.get("email"), request.get("otp"));
	}

}

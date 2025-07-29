package com.mirrorfly.gateway.controller;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.service.SsoAuthService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/v1/sso")
@RequiredArgsConstructor
public class SsoAuthController {

	private final SsoAuthService ssoAuthService;

	@PostMapping("validate")
	public Response<Object> validateSsoToken(@RequestHeader String userdomain,
			@RequestBody Map<String, Object> request) {
		return ssoAuthService.validateAndFetchUsername(userdomain, request);
	}

}

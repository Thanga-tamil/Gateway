package com.mirrorfly.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mirrorfly.gateway.dto.SanboxUserRegisterDto;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.service.SandboxUserService;

@RestController
//@RequestMapping("/v1/sandbox")
public class SandboxUserController {

	@Autowired
	private SandboxUserService sandboxUserService;

	@PostMapping("api/v1/gateway/register")
	public @ResponseBody ResponseEntity<Response<String>> registerNewUser(
			@Validated @RequestBody SanboxUserRegisterDto registerDto) throws JsonProcessingException {
		Response<String> response = sandboxUserService.registerNewUser(registerDto);
		return ResponseEntity.status(response.getStatus()).body(response);
	}
	
	@PostMapping("api/v2/gateway/register")
	public @ResponseBody ResponseEntity<Response<String>> registerNewUserv1(
			@Validated @RequestBody SanboxUserRegisterDto registerDto) throws JsonProcessingException {
		Response<String> response = sandboxUserService.registerNewUser(registerDto);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

}

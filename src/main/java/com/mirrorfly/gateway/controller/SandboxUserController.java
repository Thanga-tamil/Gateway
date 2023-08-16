package com.mirrorfly.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mirrorfly.gateway.dto.SanboxUserRegisterDto;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.service.SandboxUserService;

import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/api/v1/sandbox")
public class SandboxUserController {

	@Autowired
	private SandboxUserService sandboxUserService;

	@PostMapping("/register")
	public @ResponseBody ResponseEntity<Response<String>> registerNewUser(
			@Validated @RequestBody SanboxUserRegisterDto registerDto,
			@ApiParam(hidden = true) @RequestHeader(value = "userdomain") String userdomain)
			throws JsonProcessingException {
		Response<String> response = sandboxUserService.registerNewUser(userdomain, registerDto);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

}

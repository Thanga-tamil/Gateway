package com.mirrorfly.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mirrorfly.gateway.dto.SanboxUserRegisterDto;
import com.mirrorfly.gateway.dto.SandboxContactDto;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.service.SandboxContactService;
import com.mirrorfly.gateway.service.SandboxUserService;

@RestController
@RequestMapping("/api/v1/sandbox")
public class SandboxUserController {

	@Autowired
	private SandboxUserService sandboxUserService;

	@Autowired
	private SandboxContactService sandboxContactService;

	@PostMapping("/register")
	public @ResponseBody ResponseEntity<Response<String>> registerNewUser(
			@Validated @RequestBody SanboxUserRegisterDto registerDto) {
		Response<String> response = sandboxUserService.registerNewUser(registerDto);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

	@PostMapping("/syncContacts")
	public @ResponseBody ResponseEntity<Response<String>> syncContacts(
			@Validated @RequestBody SandboxContactDto sandboxContactDto) {
		Response<String> response = sandboxContactService.syncContactsNew(sandboxContactDto);
		return ResponseEntity.status(response.getStatus()).body(response);
	}

}

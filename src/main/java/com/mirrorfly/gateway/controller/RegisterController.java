package com.mirrorfly.gateway.controller;

import java.util.Objects;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mirrorfly.gateway.dto.RegisterInfoDto;
import com.mirrorfly.gateway.dto.RegisterRequestDto;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.service.RegisterService;

@RestController
@RequestMapping("/api/v1/register")
public class RegisterController {

	private final RegisterService registerService;

	public RegisterController(RegisterService registerService) {
		this.registerService = registerService;
	}

	@GetMapping("/info")
	public Response<RegisterInfoDto> getRegisterInfo(@RequestHeader String userdomain) {
		return this.registerService.getRegisterInfo(userdomain);
	}

	@PostMapping
	public Response<String> registerNewUser(@RequestBody RegisterRequestDto registerRequestDto, @RequestHeader String userdomain) {
		Objects.requireNonNull(registerRequestDto, "RegisterData must not be null");
		return this.registerService.registerNewUser(userdomain, registerRequestDto);
	}

}

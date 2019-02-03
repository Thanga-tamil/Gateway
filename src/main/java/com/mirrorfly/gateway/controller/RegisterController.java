package com.mirrorfly.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mirrorfly.gateway.dto.RegisterDto;
import com.mirrorfly.gateway.dto.RegisterInfoDto;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.service.RegisterService;

@RestController
@RequestMapping("/register")
public class RegisterController {

	@Autowired
	private RegisterService registerService;

	/**
	 * Function to validate username and password by passing through loginService.
	 * This function return a response stating that login success or error with
	 * error code and error message.
	 * 
	 * @param login this has username and password.
	 * @return success full response login screen with token for authorization.
	 */

	@GetMapping("/info")
	public @ResponseBody Response<RegisterInfoDto> getRegisterInfo() {
		return registerService.getRegisterInfo();
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
	public @ResponseBody Response<RegisterInfoDto> registerNewUser(@RequestBody RegisterDto registerDto) {
		return registerService.registerNewUser(registerDto);
	}

}

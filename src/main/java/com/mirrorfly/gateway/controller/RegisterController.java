package com.mirrorfly.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mirrorfly.gateway.dto.RegisterInfoDto;
import com.mirrorfly.gateway.dto.RegisterRequestDto;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.service.RegisterService;

@RestController
//@RequestMapping("/v1/register")
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

	@GetMapping("api/v1/gateway/info")
	public @ResponseBody Response<RegisterInfoDto> getRegisterInfo() {
		return registerService.getRegisterInfo();
	}

	@GetMapping("api/v2/gateway/info")
	public @ResponseBody Response<RegisterInfoDto> getRegisterInfov1() {
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

	@PostMapping("api/v1/gateway/registerNewUser")
	public @ResponseBody Response<String> registerNewUser(@Validated @RequestBody RegisterRequestDto registerRequestDto) {
		return registerService.registerNewUser(registerRequestDto);
	}
	
	@PostMapping("api/v2/gateway/registerNewUser")
	public @ResponseBody Response<String> registerNewUserv2(@Validated @RequestBody RegisterRequestDto registerRequestDto) {
		return registerService.registerNewUser(registerRequestDto);
	}

}

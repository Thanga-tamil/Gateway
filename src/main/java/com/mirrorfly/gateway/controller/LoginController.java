
package com.mirrorfly.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mirrorfly.gateway.dto.AdminLoginDto;
import com.mirrorfly.gateway.dto.ForgetPasswordDto;
import com.mirrorfly.gateway.dto.LoginDetailsDto;
import com.mirrorfly.gateway.dto.LoginDto;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.service.LoginService;

@RestController
@RequestMapping("/api/v1")
public class LoginController {

	@Autowired
	private LoginService loginService;

	/**
	 * Function to validate username and password by passing through loginService.
	 * This function return a response stating that login success or error with
	 * error code and error message.
	 * 
	 * @param login this has username and password.
	 * @return success full response login screen with token for authorization.
	 */

	@PostMapping("/login")
	public @ResponseBody Response<LoginDetailsDto> login(@Validated @RequestBody LoginDto login) {
		return loginService.login(login);
	}

	/**
	 * Function to validate username and password by passing through loginService.
	 * This function return a response stating that login success or error with
	 * error code and error message.
	 * 
	 * @param login this has username and password.
	 * @return success full response login screen with token for authorization.
	 */

	@PostMapping("/admin/login")
	public @ResponseBody Response<LoginDetailsDto> admin(@Validated @RequestBody AdminLoginDto login) {
		return loginService.adminLogin(login);
	}

	/**
	 * Function to forgot password through loginService. This function return a
	 * response stating that logout success or error with error code and error
	 * message.
	 *
	 * @param emailId.
	 * @return success full response login screen with token for authorization.
	 */

	@PostMapping("/admin/forgotpassword")
	public @ResponseBody Response<String> login(@Validated @RequestBody ForgetPasswordDto forgetPasswordDto) {
		return loginService.createVerification(forgetPasswordDto.getEmailId());
	}

	@GetMapping("/logout")
	public @ResponseBody Response<String> logout(@RequestHeader("Authorization") String authorization) {
		return loginService.logout(authorization);
	}

}
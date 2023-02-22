
package com.mirrorfly.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mirrorfly.gateway.dto.AdminLoginDto;
import com.mirrorfly.gateway.dto.AdminLoginResponse;
import com.mirrorfly.gateway.dto.ForgetPasswordDto;
import com.mirrorfly.gateway.dto.LoginDetailsDto;
import com.mirrorfly.gateway.dto.LoginDto;
import com.mirrorfly.gateway.dto.PasswordDto;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.service.LoginService;

@RestController
//@RequestMapping("/v1")
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
	 * @throws JsonProcessingException
	 */

	@PostMapping("api/v1/gateway/login")
	public @ResponseBody Response<LoginDetailsDto> login(@Validated @RequestBody LoginDto loginDto) {
		return loginService.login(loginDto);
	}
	
	@PostMapping("api/v2/gateway/login")
	public @ResponseBody Response<LoginDetailsDto> loginv1(@Validated @RequestBody LoginDto loginDto) {
		return loginService.login(loginDto);
	}

	/**
	 * Function to validate username and password by passing through loginService.
	 * This function return a response stating that login success or error with
	 * error code and error message.
	 * 
	 * @param login this has username and password.
	 * @return success full response login screen with token for authorization.
	 */

	@PostMapping("api/v1/gateway/admin/login")
	public @ResponseBody Response<AdminLoginResponse> admin(@Validated @RequestBody AdminLoginDto login) {
		return loginService.adminLogin(login);
	}
	
	@PostMapping("api/v2/gateway/admin/login")
	public @ResponseBody Response<AdminLoginResponse> adminv2(@Validated @RequestBody AdminLoginDto login) {
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

	@PostMapping("api/v1/gateway/admin/forgotpassword")
	public @ResponseBody Response<String> login(@Validated @RequestBody ForgetPasswordDto forgetPasswordDto) {
		return loginService.sendForgotPasswordEmail(forgetPasswordDto.getEmailId());
	}
	
	@PostMapping("api/v2/gateway/admin/forgotpassword")
	public @ResponseBody Response<String> loginv2(@Validated @RequestBody ForgetPasswordDto forgetPasswordDto) {
		return loginService.sendForgotPasswordEmail(forgetPasswordDto.getEmailId());
	}

	/**
	 * Function to reset password through loginService. This function return a
	 * response stating that reset password or error with error code and error
	 * message.
	 * 
	 * @param userName
	 * @param passwordDto
	 * @return success full response login screen with token for authorization.
	 */

	@GetMapping("api/v1/gateway/admin/resetpassword")
	public @ResponseBody Response<String> resetpassword(@RequestParam String userName, PasswordDto passwordDto) {
		return loginService.resetPassword(userName, passwordDto);
	}
	
	@GetMapping("api/v2/gateway/admin/resetpassword")
	public @ResponseBody Response<String> resetpasswordv2(@RequestParam String userName, PasswordDto passwordDto) {
		return loginService.resetPassword(userName, passwordDto);
	}

	/**
	 * Function to Activate Account through loginService. This function return a
	 * response stating that Activate account or error with error code and error
	 * message.
	 * 
	 * @param token
	 * @param passwordDto
	 * @return
	 */
	@GetMapping("api/v1/gateway/admin/activateaccount")
	public @ResponseBody Response<String> activateAccount(@RequestParam String token, PasswordDto passwordDto) {
		return loginService.activateAccount(token, passwordDto);
	}
	
	@GetMapping("api/v2/gateway/admin/activateaccount")
	public @ResponseBody Response<String> activateAccountv2(@RequestParam String token, PasswordDto passwordDto) {
		return loginService.activateAccount(token, passwordDto);
	}

	@GetMapping("api/v1/gateway/logout")
	public @ResponseBody Response<String> logout(@RequestHeader("Authorization") String authorization) {
		return loginService.logout(authorization);
	}
	
	@GetMapping("api/v2/gateway/logout")
	public @ResponseBody Response<String> logoutv2(@RequestHeader("Authorization") String authorization) {
		return loginService.logout(authorization);
	}

}
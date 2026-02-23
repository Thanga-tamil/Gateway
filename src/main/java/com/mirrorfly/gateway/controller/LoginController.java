
package com.mirrorfly.gateway.controller;

import java.util.concurrent.CompletableFuture;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mirrorfly.gateway.dto.AdminLoginDto;
import com.mirrorfly.gateway.dto.AdminLoginResponse;
import com.mirrorfly.gateway.dto.ForgetPasswordDto;
import com.mirrorfly.gateway.dto.LoginDetailsDto;
import com.mirrorfly.gateway.dto.LoginDto;
import com.mirrorfly.gateway.dto.PasswordDto;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.service.LoginService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LoginController {

	private final LoginService loginService;

//	@PostMapping("/login")
//	public Response<LoginDetailsDto> login(@Validated @RequestBody LoginDto loginDto,
//			@RequestHeader String userdomain) {
//		return loginService.login(userdomain, loginDto);
//	}

	@PostMapping("/login")
	public CompletableFuture<Response<LoginDetailsDto>> login(@RequestHeader String userdomain, @RequestBody LoginDto loginDto) {
		return loginService.loginAsync(userdomain, loginDto);
	}

	@PostMapping("/admin/login")
	public Response<AdminLoginResponse> admin(@Validated @RequestBody AdminLoginDto login, @RequestHeader String userdomain) {
		return loginService.adminLogin(userdomain, login);
	}

	@PostMapping("/admin/forgotpassword")
	public Response<String> login(@Validated @RequestBody ForgetPasswordDto forgetPasswordDto, @RequestHeader String userdomain) {
		return loginService.sendForgotPasswordEmail(forgetPasswordDto.getEmailId(), userdomain);
	}

	@GetMapping("/admin/resetpassword")
	public Response<String> resetpassword(@RequestParam String userName, PasswordDto passwordDto) {
		return loginService.resetPassword(userName, passwordDto);
	}

	@GetMapping("/admin/activateaccount")
	public Response<String> activateAccount(@RequestParam String token, PasswordDto passwordDto) {
		return loginService.activateAccount(token, passwordDto);
	}

	@GetMapping("/logout")
	public Response<String> logout(@RequestHeader("Authorization") String authorization, @RequestHeader String userdomain,@RequestParam(required = false, defaultValue = "false") Boolean simBindLogout) {
		// The simBindLogout flag is used to force logout from the web when an Android device SIM rejection occurs.
		return loginService.logout(userdomain, authorization, simBindLogout);
	}

	@GetMapping("/admin/inmemory/refresh")
	public Response<String> refreshInMemoryData() {
		return loginService.refreshInMemory();
	}

}
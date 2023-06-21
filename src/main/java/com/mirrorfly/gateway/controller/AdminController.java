package com.mirrorfly.gateway.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mirrorfly.gateway.dto.LoginDetailsDto;
import com.mirrorfly.gateway.dto.LoginDto;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.exception.UserUnAuthorizedException;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.response.StringResponse;
import com.mirrorfly.gateway.service.AdminService;
import com.mirrorfly.gateway.service.LoginService;

import io.micrometer.core.instrument.util.StringUtils;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import springfox.documentation.annotations.ApiIgnore;

@RestController
public class AdminController {
	
	@Autowired
	private AdminService adminService;
	
	@Autowired
	private LoginService loginService;
	
	@ApiIgnore
	@ApiOperation(value = "Refreshes the inmemory datasources (Only for admins)")
	@GetMapping("/reload/connectionconfig")
	public StringResponse reload() {
		return adminService.reloadConnectionConfig();
	}
	
	@ApiOperation(value = "Refreshes the inmemory datasources (Only for contus)")
	@GetMapping("/reload/connectionconfig/all")
	public Mono<List<StringResponse>> reloadAll(
			@ApiParam(hidden = true) @RequestHeader(value = "userdomain") String userdomain, @RequestParam(required = true) String username,
			@RequestParam(required = true) String password) {
		
		if(StringUtils.isBlank(username)) {
			throw new BadDataException("username is required");
		}else if(StringUtils.isBlank(password)) {
			throw new BadDataException("password is required");
		}
		
		username = username.trim();
		password = password.trim();
		
		if (adminService.isAdmin(userdomain, username)) {
			
			LoginDto dto = new LoginDto();
			dto.setUsername(username);
			dto.setPassword(password);
			Response<LoginDetailsDto> login = loginService.login(userdomain, dto);
			if(login !=null && login.getStatus() == 200) {
				String token = login.getData()!=null ? login.getData().getToken() : "";
				Mono<StringResponse> gateway = Mono.justOrEmpty(reload()).onErrorReturn(adminService.getErrorMessage("gateway"));
				Mono<StringResponse> user = adminService.reloadAll("/api/v1/users/reload/connectionconfig", token).onErrorReturn(adminService.getErrorMessage("user"));
				Mono<StringResponse> chat = adminService.reloadAll("/api/v1/chat/reload/connectionconfig", token).onErrorReturn(adminService.getErrorMessage("chat api"));
				Mono<StringResponse> group = adminService.reloadAll("/api/v1/group/reload/connectionconfig", token).onErrorReturn(adminService.getErrorMessage("group"));
				Mono<StringResponse> contact = adminService.reloadAll("/api/v1/contacts/reload/connectionconfig", token).onErrorReturn(adminService.getErrorMessage("contacts"));
				Mono<StringResponse> media = adminService.reloadAll("/api/v1/media/reload/connectionconfig", token).onErrorReturn(adminService.getErrorMessage("media"));
				Mono<StringResponse> moderation = adminService.reloadAll("/api/v1/moderation/reload/connectionconfig", token).onErrorReturn(adminService.getErrorMessage("moderation"));
				
				return Flux.merge(gateway, user, chat, group, contact, media, moderation).collectList();
			}else {
				throw new UserUnAuthorizedException("Unauthorized access");
			}
		} else {
			throw new UserUnAuthorizedException("Unauthorized access");
		}
	}
	
}

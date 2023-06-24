package com.mirrorfly.gateway.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	public List<Map<String, Object>> reloadAll(
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
			List<Map<String, Object>> response = new ArrayList<>();
			
			LoginDto dto = new LoginDto();
			dto.setUsername(username);
			dto.setPassword(password);
			Response<LoginDetailsDto> login = loginService.login(userdomain, dto);
			if(login !=null && login.getStatus() == 200) {
				String token = login.getData()!=null ? login.getData().getToken() : "";
				response.add(adminService.reload("/api/v1/users/reload/connectionconfig?type=resttemplate1", token, "users1"));
				response.add(adminService.reload("/api/v1/users/reload/connectionconfig?type=resttemplate2", token, "users2"));
				return response;
			}else {
				throw new UserUnAuthorizedException("Unauthorized access");
			}
		} else {
			throw new UserUnAuthorizedException("Unauthorized access");
		}
	}
	
}

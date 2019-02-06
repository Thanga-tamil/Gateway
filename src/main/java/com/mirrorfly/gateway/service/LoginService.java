package com.mirrorfly.gateway.service;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mirrorfly.gateway.dto.LoginDetailsDto;
import com.mirrorfly.gateway.dto.LoginDto;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.model.Users;
import com.mirrorfly.gateway.repo.UserRepo;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.util.GatewayConstants;

@Service
public class LoginService {

	@Autowired
	private TokenService tokenService;

	@Autowired
	private UserRepo userRepo;

	public Response<LoginDetailsDto> login(LoginDto login) {
		Users users = userRepo.findByUsername(login.getUsername());
		if (users == null) {
			throw new BadDataException(GatewayConstants.USER_NAME_NOT_AVAIL);
		}
		if (!users.getPassword().equals(login.getPassword())) {
			throw new BadDataException(GatewayConstants.BAD_CREDENTIALS);
		}
		Response<LoginDetailsDto> response = new Response<>();
		LoginDetailsDto loginDetails = new LoginDetailsDto();
		loginDetails.setToken(tokenService.getToken(login.getUsername(), "", ""));
		response.setStatus(HttpStatus.SC_OK);
		response.setData(loginDetails);
		response.setMessage(GatewayConstants.DATA_RETRIEVED);
		return response;
	}

	public Response<String> logout(String authorization) {
		String username = tokenService.getUserNameByToken(authorization);
		tokenService.deleteToken(authorization);
		Response<String> response = new Response<>();
		response.setStatus(HttpStatus.SC_OK);
		response.setMessage(GatewayConstants.LOG_OUT);
		return response;
	}

}

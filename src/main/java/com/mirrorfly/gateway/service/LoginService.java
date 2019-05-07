package com.mirrorfly.gateway.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.mirrorfly.gateway.dto.LoginDetailsDto;
import com.mirrorfly.gateway.dto.LoginDto;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.model.ChatUser;
import com.mirrorfly.gateway.model.Users;
import com.mirrorfly.gateway.repo.ChatUserRepo;
import com.mirrorfly.gateway.repo.UserRepo;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.util.GatewayConstants;

@Service
public class LoginService {

	@Autowired
	private TokenService tokenService;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private ChatUserRepo chatUserRepo;

	public Response<LoginDetailsDto> login(LoginDto login) {
		Users users = userRepo.findByUsername(login.getUsername());
		if (users == null) {
			throw new BadDataException(GatewayConstants.USER_NAME_NOT_AVAIL);
		}
		if (login.getType() == null || !login.getType().equals("WEB")) {
			if (!users.getPassword().equals(login.getPassword())) {
				throw new BadDataException(GatewayConstants.BAD_CREDENTIALS);
			}
		} else {
			if (!users.getWsPassword().equals(login.getPassword())) {
				throw new BadDataException(GatewayConstants.BAD_CREDENTIALS);
			}
		}
		Response<LoginDetailsDto> response = new Response<>();
		LoginDetailsDto loginDetails = new LoginDetailsDto();
		loginDetails.setToken(tokenService.getToken(login.getUsername(), "", ""));
		response.setStatus(HttpStatus.OK.value());
		response.setData(loginDetails);
		response.setMessage(GatewayConstants.DATA_RETRIEVED);
		return response;
	}

	public Response<String> logout(String authorization) {
		String username = tokenService.getUserNameByToken(authorization);
		tokenService.deleteToken(authorization);
		if (username != null) {
			Optional<ChatUser> chatUser = chatUserRepo.findById(username);
			if (chatUser.isPresent()) {
				chatUser.get().setDeviceToken("");
				chatUser.get().setDevicetype("");
				chatUserRepo.save(chatUser.get());
			}
		}
		Response<String> response = new Response<>();
		response.setStatus(HttpStatus.OK.value());
		response.setMessage(GatewayConstants.LOG_OUT);
		return response;
	}

}

package com.mirrorfly.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mirrorfly.gateway.dto.RegisterDto;
import com.mirrorfly.gateway.dto.RegisterInfoDto;
import com.mirrorfly.gateway.dto.RegisterResponseDto;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.model.ChatUser;
import com.mirrorfly.gateway.model.Settings;
import com.mirrorfly.gateway.model.Users;
import com.mirrorfly.gateway.repo.ChatUserRepo;
import com.mirrorfly.gateway.repo.OtpRepo;
import com.mirrorfly.gateway.repo.SettingsRepo;
import com.mirrorfly.gateway.repo.UserRepo;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.util.AESEncryption;
import com.mirrorfly.gateway.util.GatewayConstants;
import com.mirrorfly.gateway.util.GatewayUtil;

@Service
public class RegisterService {

	@Autowired
	private SettingsRepo settingsRepo;

	@Autowired
	private OtpRepo otpRepo;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private PasswordService passwordService;

	@Autowired
	private ChatUserRepo chatUserRepo;

	@Autowired
	private UserRepo userRepo;

	public Response<RegisterInfoDto> getRegisterInfo() {
		Settings settings = settingsRepo.findAll().get(0);
		RegisterInfoDto info = new RegisterInfoDto();
		info.setDomain(settings.getDomain());
		info.setMailLogin(settings.getMailLogin());
		Response<RegisterInfoDto> response = new Response<>();
		response.setStatus(HttpStatus.OK.value());
		response.setData(info);
		response.setMessage(GatewayConstants.DATA_RETRIEVED);
		return response;
	}

	public Response<String> registerNewUser(RegisterDto registerDto) {
		RegisterResponseDto registerResponseDto = registerUser(registerDto);
		ObjectMapper mapper = new ObjectMapper();
		String jsonString;
		try {
			jsonString = mapper.writeValueAsString(registerResponseDto);
		} catch (JsonProcessingException e) {
			throw new BadDataException("Json Parsing Error");
		}
		Response<String> response = new Response<>();
		response.setData(AESEncryption.encryptString(jsonString,
				registerDto.getMobileNumber() + registerDto.getMobileNumber() + registerDto.getMobileNumber()));
		response.setStatus(HttpStatus.OK.value());
		response.setMessage(GatewayConstants.DATA_RETRIEVED);
		return response;
	}

	private RegisterResponseDto registerUser(RegisterDto registerDto) {
		ChatUser chatUser = chatUserRepo.findByMobileNumber(registerDto.getMobileNumber());
		if (chatUser == null) {
			chatUser = new ChatUser();
			chatUser.setUsername(registerDto.getMobileNumber());
			chatUser.setMobileNumber(registerDto.getMobileNumber());
			chatUser.setCreatedAt(GatewayUtil.getCurrentDateTime());
		}
		chatUser.setDeviceToken(registerDto.getDeviceToken());
		chatUser.setDevicetype(registerDto.getDeviceType());
		chatUserRepo.save(chatUser);
		RegisterResponseDto registerResponseDto = new RegisterResponseDto();
		registerResponseDto.setPassword(passwordService.generateRandomPassword());
		registerResponseDto.setToken(tokenService.getToken(registerDto.getMobileNumber(), registerDto.getDeviceType(),
				registerDto.getDeviceType()));
		registerResponseDto.setUsername(registerDto.getMobileNumber());
		Users users = new Users();
		users.setPassword(registerResponseDto.getPassword());
		users.setUsername(registerDto.getMobileNumber());
		userRepo.save(users);
		return registerResponseDto;
	}

}

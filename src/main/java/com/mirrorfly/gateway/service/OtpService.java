package com.mirrorfly.gateway.service;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mirrorfly.gateway.dto.OtpDto;
import com.mirrorfly.gateway.dto.OtpResponseDto;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.model.ChatUser;
import com.mirrorfly.gateway.model.Otp;
import com.mirrorfly.gateway.repo.ChatUserRepo;
import com.mirrorfly.gateway.repo.OtpRepo;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.util.GatewayConstants;
import com.mirrorfly.gateway.util.GatewayUtil;

@Service
public class OtpService {

	@Autowired
	private OtpRepo otpRepo;

	@Autowired
	private PasswordService passwordService;

	@Autowired
	private ChatUserRepo chatUserRepo;

	public Response<OtpResponseDto> requestOtp(OtpDto otpDto) {

		if (otpDto.getMobileNumber() == null) {
			throw new BadDataException(GatewayConstants.MOBILE_NO_AVAIL);
		}
		ChatUser chatUser = chatUserRepo.findByMobileNumber(otpDto.getMobileNumber());
		Otp otp = new Otp();
		otp.setCreatedAt(GatewayUtil.getCurrentDateTime());
		otp.setOtp(passwordService.generateOtp());
		otp.setStatus(0);
		otp.setUsername(otpDto.getMobileNumber());
		otp.setMobileNumber(otpDto.getMobileNumber());
		otpRepo.save(otp);
		OtpResponseDto otpResponseDto = new OtpResponseDto();
		otpResponseDto.setOtp(otp.getOtp());
		if (chatUser != null && chatUser.getDeviceToken() != null) {
			otpResponseDto.setDeviceToken(chatUser.getDeviceToken());
		}
		Response<OtpResponseDto> response = new Response<>();
		response.setMessage(GatewayConstants.OTP_SENT);
		response.setStatus(HttpStatus.SC_OK);
		response.setData(otpResponseDto);
		return response;
	}

}

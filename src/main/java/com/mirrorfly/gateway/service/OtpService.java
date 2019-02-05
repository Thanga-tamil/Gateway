package com.mirrorfly.gateway.service;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mirrorfly.gateway.dto.OtpResponseDto;
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

	public Response<OtpResponseDto> requestOtp(String mobileNumber) {

		ChatUser chatUSer = chatUserRepo.findByMobileNumber(mobileNumber);
		Otp otp = new Otp();
		otp.setCreatedAt(GatewayUtil.getCurrentDateTime());
		otp.setOtp(passwordService.generateOtp());
		otp.setStatus(0);
		otp.setUsername(mobileNumber);
		otp.setMobileNumber(mobileNumber);
		otpRepo.save(otp);
		OtpResponseDto otpResponseDto = new OtpResponseDto();
		otpResponseDto.setOtp(otp.getOtp());
		if (chatUSer != null && chatUSer.getDeviceToken() != null) {
			otpResponseDto.setDeviceToken(chatUSer.getDeviceToken());
		}
		Response<OtpResponseDto> response = new Response<>();
		response.setMessage(GatewayConstants.OTP_SENT);
		response.setStatus(HttpStatus.SC_OK);
		response.setData(otpResponseDto);
		return response;
	}

}

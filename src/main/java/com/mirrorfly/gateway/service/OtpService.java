package com.mirrorfly.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.mirrorfly.gateway.dto.FCMRequestDto;
import com.mirrorfly.gateway.dto.OtpResponseDto;
import com.mirrorfly.gateway.dto.VerifyFCMDto;
import com.mirrorfly.gateway.dto.VerifyFCMResponse;
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

	@Autowired
	private RestService restService;

	@Autowired
	private RegisterService registerService;

	public Response<OtpResponseDto> requestOtp(String mobileNumber) {

		if (mobileNumber == null) {
			throw new BadDataException(GatewayConstants.MOBILE_NO_AVAIL);
		}
		ChatUser chatUser = chatUserRepo.findByMobileNumber(mobileNumber);
		if (chatUser != null && 1 == chatUser.getIsBlock()) {
			throw new BadDataException(GatewayConstants.USER_BLOCK);
		}
		Otp otp = new Otp();
		otp.setCreatedAt(GatewayUtil.getCurrentDateTime());
		otp.setOtp(passwordService.generateOtp());
		otp.setStatus(0);
		otp.setUsername(mobileNumber);
		otp.setMobileNumber(mobileNumber);
		otpRepo.save(otp);
		OtpResponseDto otpResponseDto = new OtpResponseDto();
		otpResponseDto.setOtp(otp.getOtp());
		if (chatUser != null && chatUser.getDeviceToken() != null) {
			otpResponseDto.setDeviceToken(chatUser.getDeviceToken());
			otpResponseDto.setIsUpdated(1);
		}
		Response<OtpResponseDto> response = new Response<>();
		response.setMessage(GatewayConstants.OTP_SENT);
		response.setStatus(HttpStatus.OK.value());
		response.setData(otpResponseDto);
		return response;
	}

	public Response<VerifyFCMResponse> verifyFCM(VerifyFCMDto fcmDto) {
		Response<VerifyFCMResponse> response = new Response<>();
		FCMRequestDto requestDto = new FCMRequestDto();
		/*
		 * FCMResponseDto fcm = restService.verifyFCMToken(requestDto); if
		 * (fcm.getKind().equals(fcmDto.getMobileNumber())) { throw new
		 * BadDataException("Phone Number and UserName Different Existing"); }
		 */
		VerifyFCMResponse dto = new VerifyFCMResponse();
		response.setMessage(GatewayConstants.FCM_SENT);
		response.setStatus(HttpStatus.OK.value());
		response.setData(dto);
		return response;
	}

}

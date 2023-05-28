
package com.mirrorfly.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.mirrorfly.gateway.datasourcerouter.AbstractRoutingDataSourceImpl;
import com.mirrorfly.gateway.datasourcerouter.SwitchDataSource;
import com.mirrorfly.gateway.dto.ChatUserDto;
import com.mirrorfly.gateway.dto.OtpResponseDto;
import com.mirrorfly.gateway.dto.VerifyUserDto;
import com.mirrorfly.gateway.dto.VerifyUserResponse;
import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.model.ChatSettings;
import com.mirrorfly.gateway.model.ChatUser;
import com.mirrorfly.gateway.model.Otp;
import com.mirrorfly.gateway.repo.ChatSettingsRepo;
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
	private ChatSettingsRepo chatSettingsRepo;
	
	@SwitchDataSource(value="[0]")
	public Response<OtpResponseDto> requestOtp(String mobileNumber) {

		if (mobileNumber == null) {
			throw new BadDataException(GatewayConstants.MOBILE_NO_AVAIL);
		}
		ChatUser chatUser = chatUserRepo.findByUsername(mobileNumber);
		if (chatUser != null && 1 == chatUser.getIsBlock()) {
			throw new BadDataException(GatewayConstants.USER_BLOCK);
		}
		Otp otp = new Otp();
		otp.setCreatedAt(GatewayUtil.getCurrentDateTime());
		otp.setOtpNo(passwordService.generateOtp());
		otp.setStatus(0);
		otp.setUsername(mobileNumber);
		otp.setMobileNumber(mobileNumber);
		otpRepo.save(otp);
		OtpResponseDto otpResponseDto = new OtpResponseDto();
		otpResponseDto.setOtp(otp.getOtpNo());
		if (chatUser != null) {
			otpResponseDto.setIsUpdated(1);
		}
		Response<OtpResponseDto> response = new Response<>();
		response.setMessage(GatewayConstants.OTP_SENT);
		response.setStatus(HttpStatus.OK.value());
		response.setData(otpResponseDto);
		return response;
	}

	@SwitchDataSource(value="[0]")
	public Response<VerifyUserResponse> verifyUser(String userdomain, VerifyUserDto userDto) {
		Response<VerifyUserResponse> response = new Response<>();
		AbstractRoutingDataSourceImpl.setDatabaseName(userdomain);
		VerifyUserResponse dto = new VerifyUserResponse();
		ChatSettings settings = chatSettingsRepo.findAll().get(0);
		ChatUserDto chatUser = chatUserRepo.findByMobileNumber(userDto.getMobileNumber(),settings.getIvProfile());
		if (chatUser != null) {
			dto.setDeviceToken(chatUser.getDeviceToken());
		}
		response.setMessage(GatewayConstants.AVAILABLE);
		response.setStatus(HttpStatus.OK.value());
		response.setData(dto);
		return response;
	}

}

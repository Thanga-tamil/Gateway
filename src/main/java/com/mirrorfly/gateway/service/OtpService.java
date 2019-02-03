package com.mirrorfly.gateway.service;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mirrorfly.gateway.model.Otp;
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

	public Response<String> requestOtp(String mobileNumber) {
		Otp otp = new Otp();
		otp.setCreatedAt(GatewayUtil.getCurrentDateTime());
		otp.setOtp(passwordService.generateOtp());
		otp.setStatus(0);
		otp.setUsername(mobileNumber);
		otp.setMobileNumber(mobileNumber);
		otpRepo.save(otp);
		Response<String> response = new Response<>();
		response.setData(otp.getOtp());
		response.setMessage(GatewayConstants.DATA_RETRIEVED);
		response.setStatus(HttpStatus.SC_OK);
		return response;
	}

}

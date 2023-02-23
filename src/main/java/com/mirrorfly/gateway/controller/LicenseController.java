package com.mirrorfly.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mirrorfly.gateway.dto.LicenseReqDto;
import com.mirrorfly.gateway.dto.LicenseResDto;
import com.mirrorfly.gateway.dto.LicenseUpdateRequestDto;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.service.LicenseService;

@RestController
//@RequestMapping("api/v1/gateway/license")
public class LicenseController {

	@Autowired
	private LicenseService licenseService;

	@GetMapping("api/v1/gateway/getLicense")
	public @ResponseBody Response<LicenseResDto> getLicense(LicenseReqDto license) {
		return licenseService.getLicense(license);
	}
	
	@GetMapping("api/v2/gateway/getLicense")
	public @ResponseBody Response<LicenseResDto> getLicensev1(LicenseReqDto license) {
		return licenseService.getLicense(license);
	}
	
	@PostMapping("api/v1/gateway/updateLicense")
	public @ResponseBody Response<String> postLicenseEncyptedData(@Validated @RequestBody LicenseUpdateRequestDto request) {
		return licenseService.updateLicense(request);
	}
	
	@PostMapping("api/v2/gateway/updateLicense")
	public @ResponseBody Response<String> postLicenseEncyptedDatav2(@Validated @RequestBody LicenseUpdateRequestDto request) {
		return licenseService.updateLicense(request);
	}
	
}

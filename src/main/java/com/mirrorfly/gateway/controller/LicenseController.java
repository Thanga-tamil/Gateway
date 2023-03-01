package com.mirrorfly.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mirrorfly.gateway.dto.LicenseReqDto;
import com.mirrorfly.gateway.dto.LicenseResDto;
import com.mirrorfly.gateway.dto.LicenseUpdateRequestDto;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.service.LicenseService;

import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/api/v1/license")
public class LicenseController {

	@Autowired
	private LicenseService licenseService;

	@GetMapping
	public @ResponseBody Response<LicenseResDto> getLicense(LicenseReqDto license) {
		return licenseService.getLicense(license);
	}
	
	@PostMapping
	public @ResponseBody Response<String> postLicenseEncyptedData(
			@ApiParam(hidden = true) @RequestHeader(value = "remoteip") String remoteIPAddress,
			@Validated @RequestBody LicenseUpdateRequestDto request) {
		return licenseService.updateLicense(request, remoteIPAddress);
	}
}

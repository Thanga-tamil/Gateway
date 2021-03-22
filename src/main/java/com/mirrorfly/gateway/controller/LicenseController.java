package com.mirrorfly.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mirrorfly.gateway.dto.LicenseReqDto;
import com.mirrorfly.gateway.dto.LicenseResDto;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.service.LicenseService;

@RestController
@RequestMapping("license")
public class LicenseController {
 
	@Autowired
	private LicenseService licenseService;
	
	@GetMapping
	public @ResponseBody Response<LicenseResDto> getLicense(LicenseReqDto license){
		return licenseService.getLicense(license);
	}
	
	@PostMapping
	public @ResponseBody Response<String> postLicenseEncyptedData(
			@RequestParam String key){
		return licenseService.updateLicense(key);
	}
	
	
	
}

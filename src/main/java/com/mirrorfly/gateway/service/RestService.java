package com.mirrorfly.gateway.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.mirrorfly.gateway.config.ApplicationProperties;
import com.mirrorfly.gateway.dto.FCMRequestDto;
import com.mirrorfly.gateway.dto.FCMResponseDto;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RestService {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ApplicationProperties applicationProperties;

	public FCMResponseDto verifyFCMToken(FCMRequestDto fcmRequestDto) {
		FCMResponseDto request = new FCMResponseDto();
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.add("Accept", MediaType.APPLICATION_JSON_VALUE);
			headers.setContentType(MediaType.APPLICATION_JSON);
			FCMResponseDto response = restTemplate.postForObject(applicationProperties.getFcmURL(), fcmRequestDto,
					FCMResponseDto.class);
		} catch (Exception ex) {
			log.error("Error IN FireBase Connection");
		}
		return request;
	}

}

package com.mirrorfly.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mirrorfly.gateway.dto.ChatSettingDataDto;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.service.ChatSettingService;

import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/chatsetting")
public class ChatSettingController {

	@Autowired
	private ChatSettingService chatSettingService;

	@PostMapping
	public @ResponseBody Response<String> postChatSettingEncyptedData(
			@ApiParam(hidden = true) @RequestHeader(value = "remoteip") String remoteIPAddress,
			@Validated @RequestBody ChatSettingDataDto request,
			@ApiParam(hidden = true, required = false) @RequestParam(required = false) @RequestHeader(value = "X-Forwarded-For") String xForwarded,
			@ApiParam(hidden = true, required = false) @RequestParam(required = false) @RequestHeader(value = "X-Real-IP") String xRealIp) {

		log.info("userIpAccessValidation - Header Key X-Forwarded-For ---> " + xForwarded);
		log.info("userIpAccessValidation - Header Key X-Real-IP ---> " + xRealIp);

		return chatSettingService.updateChatSetting(request, remoteIPAddress);
	}

}

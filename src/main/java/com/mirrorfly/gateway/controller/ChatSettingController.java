package com.mirrorfly.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.mirrorfly.gateway.dto.ChatSettingDataDto;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.service.ChatSettingService;

import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/api/v1/config")
public class ChatSettingController {

	@Autowired
	private ChatSettingService chatSettingService;

	@PostMapping
	public @ResponseBody Response<String> postChatSettingEncyptedData(
			@ApiParam(hidden = true) @RequestHeader(value = "remoteip") String remoteIPAddress,
			@Validated @RequestBody ChatSettingDataDto request) {
		return chatSettingService.updateChatSetting(request, remoteIPAddress);
	}

}

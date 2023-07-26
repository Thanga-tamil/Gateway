package com.mirrorfly.gateway.controller;

import java.util.List;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
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
			@Validated @RequestBody ChatSettingDataDto request, ServerHttpRequest httpRequest) {
		log.info("Protected API requested, will be validating the request before allowing it. URL "+ httpRequest.getURI() + " RemoteIP "+remoteIPAddress);
		HttpHeaders headers = httpRequest.getHeaders();
		for (Entry<String, List<String>> header : headers.entrySet()) {
			log.info("Header Key ---> " + header.getKey() + " Value ---> " + header.getValue());
		}
		
		return chatSettingService.updateChatSetting(request, remoteIPAddress);
	}

}

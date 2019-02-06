package com.mirrorfly.gateway.controller;

import java.io.ByteArrayOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.service.MediaFileService;

@RestController
@RequestMapping("/media")
public class MediaFileController {

	@Autowired
	private MediaFileService mediaFileService;

	@PostMapping
	public Response<String> uploadFile(@RequestHeader("Authorization") String authorization,
			@RequestParam("toUser") String touser,
			@RequestParam(value = "type", required = false) Integer type,
			@RequestParam("file") MultipartFile file) {
		return mediaFileService.uploadFile(authorization, touser, type, file);
	}

	@PostMapping("/forward")
	public Response<String> uploadForwardFile(@RequestHeader("Authorization") String authorization,
			@RequestParam("toUser") String touser, @RequestParam("type") String type,
			@RequestParam("fileToken") String fileToken) {
		return mediaFileService.forwardFile(authorization, touser, type, fileToken);
	}

	/*
	 * Download Files
	 */
	@GetMapping("/{fileToken}")
	public ResponseEntity<byte[]> downloadFile(@RequestHeader("Authorization") String authorization,
			@PathVariable String fileToken) {
		ByteArrayOutputStream downloadInputStream = mediaFileService.downloadFile(authorization, fileToken);

		return ResponseEntity.ok().contentType(contentType(fileToken))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileToken + "\"")
				.body(downloadInputStream.toByteArray());
	}

	private MediaType contentType(String keyname) {
		String[] arr = keyname.split("\\.");
		String type = arr[arr.length - 1];
		switch (type) {
		case "txt":
			return MediaType.TEXT_PLAIN;
		case "png":
			return MediaType.IMAGE_PNG;
		case "jpg":
			return MediaType.IMAGE_JPEG;
		default:
			return MediaType.APPLICATION_OCTET_STREAM;
		}
	}

}

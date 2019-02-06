package com.mirrorfly.gateway.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;

import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.mirrorfly.gateway.config.ApplicationProperties;
import com.mirrorfly.gateway.exception.NoDataFoundException;
import com.mirrorfly.gateway.model.MediaFiles;
import com.mirrorfly.gateway.repo.MediaFilesRepo;
import com.mirrorfly.gateway.response.Response;
import com.mirrorfly.gateway.util.GatewayConstants;
import com.mirrorfly.gateway.util.GatewayUtil;
import com.mirrorfly.gateway.util.MediaConstants;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MediaFileService {

	@Autowired
	private AmazonS3 s3client;

	@Autowired
	private TokenService tokenService;

	@Autowired
	private MediaFilesRepo fileUploadRepo;

	@Autowired
	private ApplicationProperties applicationProperties;

	public ByteArrayOutputStream downloadFile(String authorization, String fileToken) {
		try {
			String username = tokenService.getUserNameByToken(authorization);

			checkFileValidation(fileToken, username);

			S3Object s3object = s3client
					.getObject(new GetObjectRequest(applicationProperties.getAWSS3BucketName(), fileToken));

			InputStream is = s3object.getObjectContent();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int len;
			byte[] buffer = new byte[4096];
			while ((len = is.read(buffer, 0, buffer.length)) != -1) {
				baos.write(buffer, 0, len);
			}
			return baos;
		} catch (IOException ioe) {
			log.error("IOException: " + ioe.getMessage());
		} catch (AmazonServiceException ase) {
			log.info("sCaught an AmazonServiceException from GET requests, rejected reasons:");
			log.info("Error Message:    " + ase.getMessage());
			log.info("HTTP Status Code: " + ase.getStatusCode());
			log.info("AWS Error Code:   " + ase.getErrorCode());
			log.info("Error Type:       " + ase.getErrorType());
			log.info("Request ID:       " + ase.getRequestId());
			throw ase;
		} catch (AmazonClientException ace) {
			log.info("Caught an AmazonClientException: ");
			log.info("Error Message: " + ace.getMessage());
			throw ace;
		}

		return null;
	}

	private MediaFiles checkFileValidation(String fileToken, String username) {
		MediaFiles fileUpload = fileUploadRepo.findByFileToken(fileToken);
		if (fileUpload == null)
			throw new NoDataFoundException(GatewayConstants.FILE_NOT_AVILABLE);
		if (fileUpload.getGroupId() == null) {
			if (username.equals(fileUpload.getFromUser()) || username.equals(fileUpload.getToUser())) {
				return fileUpload;
			}
		}
		return fileUpload;

	}

	public Response<String> uploadFile(String authorization, String toUser, String type, MultipartFile file) {
		Response<String> response = new Response<>();
		String username = tokenService.getUserNameByToken(authorization);
		try {
			String extension = FilenameUtils.getExtension(file.getOriginalFilename());
			String token = username + Instant.now().toEpochMilli() + "." + extension;
			saveFileToDB(toUser, file.getOriginalFilename(), file.getContentType(), type, username, token);
			ObjectMetadata metadata = new ObjectMetadata();
			metadata.setContentLength(file.getSize());
			s3client.putObject(applicationProperties.getAWSS3BucketName(), token, file.getInputStream(), metadata);
			response.setData(token);
		} catch (AmazonServiceException ase) {
			log.info("Caught an AmazonServiceException from PUT requests, rejected reasons:");
			log.info("Error Message:    " + ase.getMessage());
			log.info("HTTP Status Code: " + ase.getStatusCode());
			log.info("AWS Error Code:   " + ase.getErrorCode());
			log.info("Error Type:       " + ase.getErrorType());
			log.info("Request ID:       " + ase.getRequestId());
			throw ase;
		} catch (AmazonClientException ace) {
			log.info("Caught an AmazonClientException: ");
			log.info("Error Message: " + ace.getMessage());
			throw ace;
		} catch (IOException e) {
			e.printStackTrace();
		}
		response.setStatus(HttpStatus.SC_OK);
		response.setMessage(GatewayConstants.DATA_RETRIEVED);
		return response;
	}

	private void saveFileToDB(String toUser, String fileName, String contentType, String type, String username,
			String token) {
		MediaFiles fileUpload = new MediaFiles();
		fileUpload.setCreatedAt(GatewayUtil.getCurrentDateTime());
		fileUpload.setFileName(fileName);
		fileUpload.setFileToken(token);
		// fileUpload.setFilePath(filePath);
		fileUpload.setFromIsDeleted(0);
		fileUpload.setFromUser(username);
		if (MediaConstants.GROUP.equals(type))
			fileUpload.setGroupId(toUser);
		fileUpload.setGroupIsDeleted(0);
		fileUpload.setIsActive(0);
		fileUpload.setIsProfile(0);
		fileUpload.setIsServer(0);
		fileUpload.setMimeType(contentType);
		fileUpload.setToIsDeleted(0);
		fileUpload.setToUser(toUser);
		fileUpload.setUpdatedAt(GatewayUtil.getCurrentDateTime());
		fileUploadRepo.save(fileUpload);
	}

	public Response<String> forwardFile(String authorization, String keyName, String type, String file) {
		Response<String> response = new Response<>();
		String username = tokenService.getUserNameByToken(authorization);
		// checkFileValidation(fileToken, username);
		return response;
	}

}

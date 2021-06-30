package com.mirrorfly.gateway.dto;

import java.sql.Timestamp;
import java.util.List;

import lombok.Data;

@Data
public class LicenseDataDto {

	private Timestamp expiresAt;

	private List<String> modulesPurchased;

	private List<String> domains;

}

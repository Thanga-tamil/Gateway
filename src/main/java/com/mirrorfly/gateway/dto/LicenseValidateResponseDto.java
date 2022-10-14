package com.mirrorfly.gateway.dto;

import lombok.Data;

@Data
public class LicenseValidateResponseDto {

	private Long maxUsersCount;

	private Boolean isFreemiumUser;

}

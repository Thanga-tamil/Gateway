package com.mirrorfly.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueuePayloadDto {

	private String type;

	private String customString;

	private String userDomain;

}

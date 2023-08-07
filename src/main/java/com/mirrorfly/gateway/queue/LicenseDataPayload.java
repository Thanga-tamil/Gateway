package com.mirrorfly.gateway.queue;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LicenseDataPayload {
	
	private String data;
	
	private String userDomain;
	
}

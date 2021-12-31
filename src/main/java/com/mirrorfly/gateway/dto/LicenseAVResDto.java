package com.mirrorfly.gateway.dto;
import java.util.List;

import lombok.Data;

@Data
public class LicenseAVResDto extends LicenseResDto {
	
    private List<String> stuns;

	private List<TurnDto> turns;
	
	private String signalServer;

}

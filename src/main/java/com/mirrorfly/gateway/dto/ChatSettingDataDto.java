package com.mirrorfly.gateway.dto;

import lombok.Data;

@Data
public class ChatSettingDataDto {
	
	private String licenceKey;
	
	private Integer groupChat;

	private Integer attachment;

	private Integer imageAttachment;

	private Integer videoAttachment;

	private Integer audioAttachment;

	private Integer documentAttachment;

	private Integer contactAttachment;

	private Integer locationAttachment;

	private Integer one2oneCall;

	private Integer groupCall;
	
	private Integer recentchatSearch;

}

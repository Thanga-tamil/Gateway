package com.mirrorfly.gateway.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class RedisChatToken implements Serializable {

	private static final long serialVersionUID = -2408355252426575161L;

	private String token;

	private String username;

}

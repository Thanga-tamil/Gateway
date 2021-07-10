package com.mirrorfly.gateway.util;

public enum EnumQueue {

	EMAIL_QUEUE("email_queue"), LICENSE_QUEUE("data");

	private String queue;

	EnumQueue(String queue) {
		this.queue = queue;
	}

	public String getQueue() {
		return queue;
	}

}

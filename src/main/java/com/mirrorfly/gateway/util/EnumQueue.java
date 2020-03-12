package com.mirrorfly.gateway.util;

public enum EnumQueue {

	EMAIL_QUEUE("email_queue");

	private String queue;

	EnumQueue(String queue) {
		this.queue = queue;
	}

	public String getQueue() {
		return queue;
	}

}

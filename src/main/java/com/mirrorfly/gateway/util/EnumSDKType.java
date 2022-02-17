package com.mirrorfly.gateway.util;

public enum EnumSDKType {

	IOSAVCALLS("1"), IOSCHAT("2"), ANDROIDAVCALLS("3"), ANDROIDCHAT("4"), WEBAVCALLS("5"), WEBCHAT("6");

	private String sdkType;

	EnumSDKType(String sdkType) {
		this.sdkType = sdkType;
	}

	public String getSdkType() {
		return sdkType;
	}

}

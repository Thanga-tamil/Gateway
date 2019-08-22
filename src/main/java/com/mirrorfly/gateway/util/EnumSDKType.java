package com.mirrorfly.gateway.util;

public enum EnumSDKType {

	iOSAVCalls("1"), iOSChat("2"), AndroidAVCalls("3"), AndroidChat("4"), WebAVCalls("5"), WebChat("6");

	private String sdkType;

	EnumSDKType(String sdkType) {
		this.sdkType = sdkType;
	}

	public String getSdkType() {
		return sdkType;
	}

}

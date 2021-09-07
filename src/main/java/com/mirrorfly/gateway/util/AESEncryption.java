package com.mirrorfly.gateway.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AESEncryption {

	public static String encryptString(String msg, String msgId) {
		try {
			CryptLib crypt = new CryptLib();
			String key = CryptLib.SHA256(msgId, 32);
			String iv = GatewayConstants.IV;
			return crypt.encrypt(msg, key, iv); // output
 		} catch (Exception ex) {
			log.error(ex.getMessage());
			return msg;
		}
	}

	public static String decryptString(String encryptedText, String msgID) {
		try {
			CryptLib crypt = new CryptLib();
			String key = CryptLib.SHA256(msgID, 32);
			String iv = GatewayConstants.IV;
			return crypt.decrypt(encryptedText, key, iv); //output
		} catch (Exception ex) {
			log.error(ex.getMessage());
			return "";
		}
	}

}

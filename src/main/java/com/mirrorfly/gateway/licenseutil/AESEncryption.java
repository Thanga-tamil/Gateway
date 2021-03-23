package com.mirrorfly.gateway.licenseutil;

import com.mirrorfly.gateway.exception.BadDataException;
import com.mirrorfly.gateway.util.GatewayConstants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AESEncryption {

	private AESEncryption() {

	}

	public static String encryptString(String data, String encryptKey) {
		try {
			CryptLib crypt = new CryptLib();
			String key = CryptLib.sha256(encryptKey, 32);
			String iv = GatewayConstants.LICENSEIV;
			return crypt.encrypt(data, key, iv);
		} catch (Exception ex) {
			log.error(ex.getMessage());
			throw new BadDataException("Encryption failed");
		}
	}

	public static String decryptString(String encryptedText, String encryptKey) {
		try {
			CryptLib crypt = new CryptLib();
			String key = CryptLib.sha256(encryptKey, 32);
			String iv = GatewayConstants.LICENSEIV;
			return crypt.decrypt(encryptedText, key, iv);
		} catch (Exception ex) {
			log.error(ex.getMessage());
			throw new BadDataException("Decyption failed");
		}
	}

}

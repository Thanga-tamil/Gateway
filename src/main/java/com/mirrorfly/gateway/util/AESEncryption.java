package com.mirrorfly.gateway.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.util.Base64Utils;

public class AESEncryption {
	public static String encrypt(String key, String initVector, String value) {
		try {
			IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

			byte[] encrypted = cipher.doFinal(value.getBytes());
			System.out.println("encrypted string: " + Base64Utils.encodeToString(encrypted));

			return Base64Utils.encodeToString(encrypted);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

	public static String decrypt(String key, String initVector, String encrypted) {
		try {
			IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

			byte[] original = cipher.doFinal(Base64Utils.decodeFromString(encrypted));

			return new String(original);
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

	public static void main(String[] args) {
		String key = "948782998694878299869487829986"; // 128 bit key
		String initVector = "ddc0f15cc2c90fca"; // 16 bytes IV
		System.out.println(decrypt(key, initVector,
				"QaJ0g5TeijEvrAKzUfQHl1Hw2u9wglGWMljDczacvvHmKhdIut/2LPWnuFOSGFQMZpPuoKtclrS0UcLVn8UvsmW9rQcv2qaUSdDPpVfdq6XR54JBrV22tXGykgn4yEEfIa3s5Ba5cw+8Wi8OpWfFspGj1mLPJtBByYukNfWJ8np9fnq6MZUR8NZ2x7oCc3bdV6juvvg+IgmeaoC4SwU0ZHLPi8yqFqzcRGtPExBX8OTzDXVuX5eiwCzXcyHe74hrkDWPxIJKPiTC/HG+8F39XLWMN2HqX4nWNbnRMbIJK83F1wpWigBSi1ExToqP+pBMU9wPUgamoeLciCKJsgBG4gXRP3XbKGDHE43vXnDXal4Ady9Iy2gwq1UwCxzd+3VEkyWWg5Kvru9GiIAaMQ+TDSfy/klvO4uf2Sq7WkOIs052qDwtIDIAnwlxudsrHg8LqyE2S8LXy2LUr2zXCpH6JVrINeXFNYL96qiw1eozt7x2zmVDZ+uKhC4VMjXFPut9zPrgeu779RoOwuEuMNdX9/wqBqNnS8BNue6JJAKCfwvwRYhlhu4glaKXK6I8Cv95QRH20gbQETpmWnEo+aIelM7roD/W+0Q05ru0/6gkKqmfk7VjaALPAjmjHFpnmexCRkCDCYbr8hkywZQgXPPFyiWT0WMDmv58LL4prO3EUlw="));
	}
}

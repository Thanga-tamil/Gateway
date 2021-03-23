/*
 * @category ContusFly
 * @copyright Copyright (C) 2017 Contus. All rights reserved.
 * @license http://www.apache.org/licenses/LICENSE-2.0
 */

package com.mirrorfly.gateway.licenseutil;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;

/*****************************************************************
 * CrossPlatform CryptLib
 * <p>
 * This cross platform CryptLib uses AES 256 for encryption. This library can be
 * used for encryption and decryption of string on iOS, Android and Windows
 * platform.
 * <p>
 * Features:
 * <p>
 * 1. 256 bit AES encryption
 * <p>
 * 2. Random IV generation.
 * <p>
 * 3. Provision for SHA256 hashing of key.
 *
 *****************************************************************/

@Slf4j
public class CryptLib {
	/**
	 * Cipher to be used for encryption and decryption
	 */
	private Cipher cx;

	/**
	 * Encryption key and initialization vector
	 */
	private byte[] key;

	private byte[] iv;

	public CryptLib() throws NoSuchAlgorithmException, NoSuchPaddingException {
		/**
		 * Initialize the cipher with transformation AES/CBC/PKCS5Padding
		 */
		cx = Cipher.getInstance("AES/CBC/PKCS5Padding");
		/**
		 * 256 bit key space
		 */
		key = new byte[32];
		/**
		 * 128 bit IV
		 */
		iv = new byte[16];
	}

	/**
	 * Note: This function is no longer used. This function generates md5 hash of
	 * the input string
	 *
	 * @param inputString
	 *
	 * @return md5 hash of the input string
	 */
	public static final String md5(final String inputString) {
		final String MD5 = "MD5";
		try {
			/**
			 * Create MD5 Hash
			 */
			MessageDigest digest = MessageDigest.getInstance(MD5);
			digest.update(inputString.getBytes());
			byte messageDigest[] = digest.digest();

			/**
			 * Create Hex String
			 */
			StringBuilder hexString = new StringBuilder();
			for (byte aMessageDigest : messageDigest) {
				String h = Integer.toHexString(0xFF & aMessageDigest);
				while (h.length() < 2)
					h = "0" + h;
				hexString.append(h);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			log.error(e.getMessage());
		}
		return "";
	}

	/***
	 * This function computes the SHA256 hash of input string
	 *
	 * @param text   Input text whose SHA256 hash has to be computed
	 * @param length Length of the text to be returned
	 *
	 * @return returns SHA256 hash of input text
	 *
	 * @throws NoSuchAlgorithmException
	 * @throws UnsupportedEncodingException
	 */
	public static String sha256(String text, int length) throws NoSuchAlgorithmException {

		String resultStr;
		MessageDigest md = MessageDigest.getInstance("SHA-256");

		md.update(text.getBytes(StandardCharsets.UTF_8));
		byte[] digest = md.digest();

		StringBuilder result = new StringBuilder();
		for (byte b : digest) {
			/**
			 * Convert to hex
			 */
			result.append(String.format("%02x", b));
		}

		if (length > result.toString().length()) {
			resultStr = result.toString();
		} else {
			resultStr = result.toString().substring(0, length);
		}

		return resultStr;

	}

	/**
	 * The function generates random string for given length
	 *
	 * @param length Desired length * @return
	 */
	public static String generateRandomIV(int length) {
		SecureRandom ranGen = new SecureRandom();
		byte[] aesKey = new byte[16];
		ranGen.nextBytes(aesKey);
		StringBuilder result = new StringBuilder();
		for (byte b : aesKey) {
			/**
			 * Convert to hex
			 */
			result.append(String.format("%02x", b));
		}
		if (length > result.toString().length()) {

			return result.toString();
		} else {
			return result.toString().substring(0, length);
		}
	}

	/**
	 * @param inputText     Text to be encrypted or decrypted
	 * @param encryptionKey Encryption key to used for encryption / decryption
	 * @param mode          specify the mode encryption / decryption
	 * @param initVector    Initialization vector
	 *
	 * @return encrypted or decrypted string based on the mode
	 *
	 * @throws UnsupportedEncodingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	private String encryptDecrypt(String inputText, String encryptionKey, EncryptMode mode, String initVector)
			throws InvalidKeyException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, BadPaddingException {
		/**
		 * Output string
		 */
		String out = "";
		/**
		 * encryptionKey = md5(encryptionKey); length of the key provided
		 */
		int len = encryptionKey.getBytes(StandardCharsets.UTF_8).length;

		if (encryptionKey.getBytes(StandardCharsets.UTF_8).length > key.length)
			len = key.length;

		int ivlen = initVector.getBytes(StandardCharsets.UTF_8).length;

		if (initVector.getBytes(StandardCharsets.UTF_8).length > iv.length)
			ivlen = iv.length;

		System.arraycopy(encryptionKey.getBytes(StandardCharsets.UTF_8), 0, key, 0, len);
		System.arraycopy(initVector.getBytes(StandardCharsets.UTF_8), 0, iv, 0, ivlen);
		/**
		 * Code for Dynamic key
		 *
		 * KeyGenerator keyGen = KeyGenerator.getInstance("AES"); keyGen.init(128);
		 */
		/**
		 * Create a new SecretKeySpec
		 */
		SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
		/**
		 * Create a new IvParameterSpec instance with the bytes from the specified
		 * buffer iv used as initialization vector.
		 */
		IvParameterSpec ivSpec = new IvParameterSpec(iv);

		/**
		 * Encryption
		 */

		if (mode.equals(EncryptMode.ENCRYPT)) {
			/**
			 * Initialize this cipher instance.
			 * <p>
			 * Read
			 * https://android-developers.blogspot.com/2013/08/some-securerandom-thoughts.html
			 * for more info.
			 */
			cx.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
			byte[] results = cx.doFinal(inputText.getBytes(StandardCharsets.UTF_8));
			/**
			 * Multi-part, transformation and (encryption)
			 * <p>
			 * Ciphertext
			 */
			out = Base64.getEncoder().encodeToString(results);
		}

		/**
		 * Decryption
		 */

		if (mode.equals(EncryptMode.DECRYPT)) {
			cx.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);// Initialize this ipher instance

			byte[] decodedValue = Base64.getDecoder().decode(inputText.getBytes());
			byte[] decryptedVal = cx.doFinal(decodedValue); // Finish
			/**
			 * Multi-part transformation(decryption)
			 */
			out = new String(decryptedVal);
		}
		/**
		 * Return encrypted/decrypted string
		 */
		return out;
	}

	/***
	 * This function encrypts the plain text to cipher text using the key provided.
	 * You'll have to use the same key for decryption
	 *
	 * @param _plainText Plain text to be encrypted
	 * @param key       Encryption Key. You'll have to use the same key for
	 *                   decryption
	 * @param iv        initialization Vector
	 *
	 * @return returns encrypted (cipher) text
	 *
	 * @throws InvalidKeyException
	 * @throws UnsupportedEncodingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */

	public String encrypt(String plainText, String key, String iv)
			throws InvalidKeyException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, BadPaddingException {
		return encryptDecrypt(plainText, key, EncryptMode.ENCRYPT, iv);
	}

	/***
	 * This function decrypts the encrypted text to plain text using the key
	 * provided. You'll have to use the same key which you used during encryption.
	 *
	 * @param _encryptedText Encrypted/Cipher text to be decrypted
	 * @param key           Encryption key which you used during encryption
	 * @param iv            initialization Vector
	 *
	 * @return encrypted value
	 *
	 * @throws InvalidKeyException
	 * @throws UnsupportedEncodingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public String decrypt(String encryptedText, String key, String iv)
			throws InvalidKeyException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, BadPaddingException {
		return encryptDecrypt(encryptedText, key, EncryptMode.DECRYPT, iv);
	}

	/**
	 * Encryption mode enumeration
	 */
	private enum EncryptMode {
		ENCRYPT, DECRYPT
	}
}

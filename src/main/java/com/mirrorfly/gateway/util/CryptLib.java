/*
 * @category ContusFly
 * @copyright Copyright (C) 2017 Contus. All rights reserved.
 * @license http://www.apache.org/licenses/LICENSE-2.0
 */

package com.mirrorfly.gateway.util;

import java.io.UnsupportedEncodingException;
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
	private Cipher _cx;

	/**
	 * Encryption key and initialization vector
	 */
	private byte[] _key;

	private byte[] _iv;

	public CryptLib() throws NoSuchAlgorithmException, NoSuchPaddingException {
		/**
		 * Initialize the cipher with transformation AES/CBC/PKCS5Padding
		 */
		_cx = Cipher.getInstance("AES/CBC/PKCS5Padding");
		/**
		 * 256 bit key space
		 */
		_key = new byte[32];
		/**
		 * 128 bit IV
		 */
		_iv = new byte[16];
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
	public static String SHA256(String text, int length) throws NoSuchAlgorithmException, UnsupportedEncodingException {

		String resultStr;
		MessageDigest md = MessageDigest.getInstance("SHA-256");

		md.update(text.getBytes("UTF-8"));
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
	 * @param _inputText     Text to be encrypted or decrypted
	 * @param _encryptionKey Encryption key to used for encryption / decryption
	 * @param _mode          specify the mode encryption / decryption
	 * @param _initVector    Initialization vector
	 *
	 * @return encrypted or decrypted string based on the mode
	 *
	 * @throws UnsupportedEncodingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	private String encryptDecrypt(String _inputText, String _encryptionKey, EncryptMode _mode, String _initVector)
			throws UnsupportedEncodingException, InvalidKeyException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, BadPaddingException {
		/**
		 * Output string
		 */
		String _out = "";
		/**
		 * _encryptionKey = md5(_encryptionKey); length of the key provided
		 */
		int len = _encryptionKey.getBytes("UTF-8").length;

		if (_encryptionKey.getBytes("UTF-8").length > _key.length)
			len = _key.length;

		int ivlen = _initVector.getBytes("UTF-8").length;

		if (_initVector.getBytes("UTF-8").length > _iv.length)
			ivlen = _iv.length;

		System.arraycopy(_encryptionKey.getBytes("UTF-8"), 0, _key, 0, len);
		System.arraycopy(_initVector.getBytes("UTF-8"), 0, _iv, 0, ivlen);
		/**
		 * Code for Dynamic key
		 *
		 * KeyGenerator _keyGen = KeyGenerator.getInstance("AES"); _keyGen.init(128);
		 */
		/**
		 * Create a new SecretKeySpec
		 */
		SecretKeySpec keySpec = new SecretKeySpec(_key, "AES");
		/**
		 * Create a new IvParameterSpec instance with the bytes from the specified
		 * buffer iv used as initialization vector.
		 */
		IvParameterSpec ivSpec = new IvParameterSpec(_iv);

		/**
		 * Encryption
		 */

		if (_mode.equals(EncryptMode.ENCRYPT)) {
			/**
			 * Initialize this cipher instance.
			 * <p>
			 * Read
			 * https://android-developers.blogspot.com/2013/08/some-securerandom-thoughts.html
			 * for more info.
			 */
			_cx.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
			byte[] results = _cx.doFinal(_inputText.getBytes("UTF-8"));
			/**
			 * Multi-part, transformation and (encryption)
			 * <p>
			 * Ciphertext
			 */
			_out = Base64.getEncoder().encodeToString(results);
		}

		/**
		 * Decryption
		 */

		if (_mode.equals(EncryptMode.DECRYPT)) {
			_cx.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);// Initialize this ipher instance

			byte[] decodedValue = Base64.getDecoder().decode(_inputText.getBytes());
			byte[] decryptedVal = _cx.doFinal(decodedValue); // Finish
			/**
			 * Multi-part transformation(decryption)
			 */
			_out = new String(decryptedVal);
		}
		/**
		 * Return encrypted/decrypted string
		 */
		return _out;
	}

	/***
	 * This function encrypts the plain text to cipher text using the key provided.
	 * You'll have to use the same key for decryption
	 *
	 * @param _plainText Plain text to be encrypted
	 * @param _key       Encryption Key. You'll have to use the same key for
	 *                   decryption
	 * @param _iv        initialization Vector
	 *
	 * @return returns encrypted (cipher) text
	 *
	 * @throws InvalidKeyException
	 * @throws UnsupportedEncodingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */

	public String encrypt(String _plainText, String _key, String _iv)
			throws InvalidKeyException, UnsupportedEncodingException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, BadPaddingException {
		return encryptDecrypt(_plainText, _key, EncryptMode.ENCRYPT, _iv);
	}

	/***
	 * This function decrypts the encrypted text to plain text using the key
	 * provided. You'll have to use the same key which you used during encryption.
	 *
	 * @param _encryptedText Encrypted/Cipher text to be decrypted
	 * @param _key           Encryption key which you used during encryption
	 * @param _iv            initialization Vector
	 *
	 * @return encrypted value
	 *
	 * @throws InvalidKeyException
	 * @throws UnsupportedEncodingException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public String decrypt(String _encryptedText, String _key, String _iv)
			throws InvalidKeyException, UnsupportedEncodingException, InvalidAlgorithmParameterException,
			IllegalBlockSizeException, BadPaddingException {
		return encryptDecrypt(_encryptedText, _key, EncryptMode.DECRYPT, _iv);
	}

	/**
	 * Encryption mode enumeration
	 */
	private enum EncryptMode {
		ENCRYPT, DECRYPT
	}
}

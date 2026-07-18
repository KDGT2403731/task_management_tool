package com.example.taskmanagementtool.service;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenEncryptionService {
	private static final String ALGORITHM = "AES/GCM/NoPadding";
	private static final int GCM_TAG_LENGTH_BITS = 128;
	private static final int GCM_IV_LENGTH_BYTES = 12;

	private final SecretKeySpec secretKey;

	public TokenEncryptionService(@Value("${app.encryption.secret-key}") String base64SecretKey) {
		byte[] keyBytes = Base64.getDecoder().decode(base64SecretKey);
		this.secretKey = new SecretKeySpec(keyBytes, "AES");
	}

	public String encrypt(String plainText) {
		if (plainText == null) {
			return null;
		}
		try {
			byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
			new SecureRandom().nextBytes(iv);

			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
			byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
			byte[] combined = new byte[iv.length + cipherText.length];
			System.arraycopy(iv, 0, combined, 0, iv.length);
			System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);

			return Base64.getEncoder().encodeToString(combined);
		} catch (Exception e) {
			throw new IllegalStateException("トークンの暗号化に失敗しました。", e);
		}
	}

	public String decrypt(String encryptedText) {
		if (encryptedText == null) {
			return null;
		}
		try {
			byte[] combined = Base64.getDecoder().decode(encryptedText);

			byte[] iv = new byte[GCM_IV_LENGTH_BYTES];
			System.arraycopy(combined, 0, iv, 0, iv.length);

			byte[] cipherText = new byte[combined.length - iv.length];
			System.arraycopy(combined, iv.length, cipherText, 0, cipherText.length);

			Cipher cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
			byte[] plainBytes = cipher.doFinal(cipherText);

			return new String(plainBytes, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new IllegalStateException("トークンの復号に失敗しました。", e);
		}
	}
}

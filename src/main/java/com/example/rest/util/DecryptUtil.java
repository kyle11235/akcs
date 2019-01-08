package com.example.rest.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;

public class DecryptUtil {

	private static String algorithm1 = "DES";
	private static String algorithm2 = "DES/CBC/NoPadding";
	private static IvParameterSpec iv = new IvParameterSpec(new byte[] { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 });
	private static SecretKey key;
	private static Cipher cipherD;
	private static Cipher cipherE;

	public DecryptUtil(String kmKey) {
		key = new SecretKeySpec(kmKey.getBytes(), algorithm1);
		try {
			cipherE = Cipher.getInstance(algorithm2);
			cipherE.init(Cipher.ENCRYPT_MODE, key, iv);
			cipherD = Cipher.getInstance(algorithm2);
			cipherD.init(Cipher.DECRYPT_MODE, key, iv);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public String encrypt(String value) {
		byte[] out = null;
		try {
			cipherE.init(Cipher.ENCRYPT_MODE, key, iv);
			out = cipherE.doFinal(value.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return new String(Hex.encodeHex(out));

	}

	public String decrypt(String value) {
		byte[] decoded = null;
		byte[] out = null;
		try {
			decoded = Hex.decodeHex(value.toCharArray());
			out = cipherD.doFinal(decoded);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return new String(out);
	}

	public static void main(String[] args) {
		DecryptUtil util = new DecryptUtil("starpass");
		// x * 8 bytes
		String e = util.encrypt("kyle.z.zhang@xxx.com|10000000");
		System.out.println(e);
		System.out.println(util.decrypt(e));
	}

}

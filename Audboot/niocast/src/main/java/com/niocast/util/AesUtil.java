package com.niocast.util;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

public class AesUtil {
	/**生成key，作为加密和解密密钥且只有密钥相同解密加密才会成功
	 * @return
	 */
	public static Key createKey(String keystr) {
		try {
			// 生成key
			KeyGenerator keyGenerator;
			//构造密钥生成器，指定为AES算法,不区分大小写
			keyGenerator = KeyGenerator.getInstance("AES");
			//生成一个128位的随机源,根据传入的字节数组
			keyGenerator.init(128);
			// key转换,根据字节数组生成AES密钥
			Key key = new SecretKeySpec(keystr.getBytes(), "AES");
			return key;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	
	}
	/**加密
	 * @param context 需要加密的明文
	 * @param key 加密用密钥
	 * @return
	 */
	public static byte[] jdkAES(String context, Key key) {
		try {

			Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
			
			cipher.init(cipher.ENCRYPT_MODE, key);
			//将加密并编码后的内容解码成字节数组
			byte[] result = cipher.doFinal(context.getBytes());
			return result;
		} catch (InvalidKeyException | IllegalBlockSizeException
				| BadPaddingException e) {
			e.printStackTrace();
			return null;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/** 解密
	 * @param result 加密后的密文byte数组
	 * @param key 解密用密钥
	 */
	public static byte[] decrypt(byte[] info, Key key) {

		Cipher cipher;
		byte[] result = new byte[(int) (Math.ceil(info.length/16.0))*16];
		try {
			System.arraycopy(info, 0, result, 0, info.length);
			cipher = Cipher.getInstance("AES/ECB/NoPadding");
			//初始化密码器，第一个参数为加密(Encrypt_mode)或者解密(Decrypt_mode)操作，第二个参数为使用的KEY
			cipher.init(Cipher.DECRYPT_MODE, key);
			result = cipher.doFinal(result);
 
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return result;
	}
}

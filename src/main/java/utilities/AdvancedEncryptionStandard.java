package utilities;

import java.nio.charset.StandardCharsets;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AdvancedEncryptionStandard
{

	private static final String ALGORITHM = "AES";
	private static final byte[] key = "ThisI$lenskart18".getBytes(StandardCharsets.UTF_8);

	public static byte[] encrypt(byte[] plainText)
	{
		SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
		byte[] cipherText = null;
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			cipherText = cipher.doFinal(plainText);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return cipherText;
	}

	public static byte[] decrypt(byte[] cipherText) 
	{
		SecretKeySpec secretKey = new SecretKeySpec(key, ALGORITHM);
		byte[] plainText = null;
		Cipher cipher;
		try {
			cipher = Cipher.getInstance(ALGORITHM);
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			plainText = cipher.doFinal(cipherText);
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		return plainText; 
	}
}

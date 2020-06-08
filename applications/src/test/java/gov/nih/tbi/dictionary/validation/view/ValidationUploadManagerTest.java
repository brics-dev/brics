package gov.nih.tbi.dictionary.validation.view;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ValidationUploadManagerTest {
	
	@Test
	public void setKeyTest() {
		ValidationUploadManager.setKey("same");
		SecretKeySpec key1 = ValidationUploadManager.getKey();
		ValidationUploadManager.setKey("same");
		SecretKeySpec key2 = ValidationUploadManager.getKey();
		ValidationUploadManager.setKey("different");
		SecretKeySpec key3 = ValidationUploadManager.getKey();
		Assert.assertTrue(key1.equals(key2));
		Assert.assertTrue(!(key2.equals(key3)));
	}
	
	@Test
	public void testDecryptProperty() {
		String keyWord = "Kaizen";
		String encryptedText = "";
		String unencryptedText;
		String plainText = "PlainText";
		try
        {
            ValidationUploadManager.setKey(keyWord);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, ValidationUploadManager.getKey());
            encryptedText = Base64.getEncoder().encodeToString(cipher.doFinal(plainText.getBytes("UTF-8")));
        } 
        catch (Exception e) 
        {
            System.out.println("Error while encrypting property: ".concat(plainText).concat(e.toString()));
        }
		Assert.assertTrue(!plainText.equals(encryptedText) && !encryptedText.equals(""));
		unencryptedText = ValidationUploadManager.decryptSftpProperty(encryptedText, keyWord);
		Assert.assertTrue(plainText.equals(unencryptedText));
	}
}
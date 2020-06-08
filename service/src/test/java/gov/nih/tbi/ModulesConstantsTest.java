package gov.nih.tbi;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.testng.Assert;
import org.testng.annotations.Test;


public class ModulesConstantsTest {
	
	@Test
	public void setKeyTest() {
		ModulesConstants.setKey("same");
		SecretKeySpec key1 = ModulesConstants.getKey();
		ModulesConstants.setKey("same");
		SecretKeySpec key2 = ModulesConstants.getKey();
		ModulesConstants.setKey("different");
		SecretKeySpec key3 = ModulesConstants.getKey();
		Assert.assertTrue(key1.equals(key2));
		Assert.assertTrue(!(key2.equals(key3)));
	}
	
	@Test
	public void testEncryptProperty() {
		String keyWord = "Kaizen";
		String encryptedText;
		String unencryptedText = "";
		String plainText = "PlainText";
		encryptedText = ModulesConstants.encryptSftpProperty(plainText, keyWord);
		Assert.assertTrue(!plainText.equals(encryptedText) && !encryptedText.equals(null));
		try
        {
            ModulesConstants.setKey(keyWord);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, ModulesConstants.getKey());
            unencryptedText = new String(cipher.doFinal(Base64.getDecoder().decode(encryptedText)));
        } 
        catch (Exception e) 
        {
            System.out.println("Error while encrypting property: ".concat(plainText).concat(e.toString()));
        }
		Assert.assertTrue(plainText.equals(unencryptedText));
	}
}
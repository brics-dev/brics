package dataimport;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;


public class test {

	static Logger logger = Logger.getLogger(ImportDelegate.class);

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		ImportDelegate testDelegate = new ImportDelegate(); 
 
		testDelegate.processJob("skyzer@sapient.com", "StageLocationSubmission", "NewStudy", "c:\\work\\");  // The current arguments could be user name and XML package 
		
//		List<String> messages = Arrays.asList("Buenos Aires", "Cordoba", "La Plata");
//		boolean success = true;
//		String emailAddress = "skyzer@sapient.com, shawnkyzer@yahoo.com";
//		testDelegate.sendMessageToHTTP(messages, success, emailAddress);
		
		System.exit(0);
	}
	

}
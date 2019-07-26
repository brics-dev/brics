package gov.nih.nichd.ctdb.selfreporting.util;


/**
 * 
 * @author pandyan
 *
 */
public class SelfReportingAcessControlUtil {
	
	
	
	
	/**
	 * create a random token
	 * @return
	 */
	public static  String createRandomToken() {

    	String token = "";
    	String[] tokenChars = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","0","1","2","3","4","5","6","7","8","9"};
    	int tokenCharsLength = tokenChars.length;
    	

		token = "";
		for(int i=0;i<16;i++) {
			int randomIndex = (int )(Math.random() * tokenCharsLength - 1);
			String tokenChar = tokenChars[randomIndex];
			token = token + tokenChar;
		}
	

    	return token;
    }
	
	
	
	
	

}

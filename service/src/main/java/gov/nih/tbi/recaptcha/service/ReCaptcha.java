/**
 * 
 */
package gov.nih.tbi.recaptcha.service;

import java.io.IOException;

import gov.nih.tbi.recaptcha.model.ReCaptchaResponse;

/**
 * @author Ryan Stewart
 *
 */
public interface ReCaptcha {
	
	/**
	 * Set secret for reCapthca account
	 * 
	 * @param secret key provided by google
	 * @return
	 * @throws IOException 
	 */
	public void setSecret(String secret);
	
	/**
	 * Validates a reCaptcha challenge and response.
	 * 
	 * @param remoteAddr The address of the user, eg. request.getRemoteAddr()
	 * @param challenge The challenge from the reCaptcha form, this is usually request.getParameter("recaptcha_challenge_field") in your code.
	 * @param response The response from the reCaptcha form, this is usually request.getParameter("recaptcha_response_field") in your code.
	 * @return
	 * @throws IOException 
	 */
	public ReCaptchaResponse checkAnswer(String response, String remoteip) throws IOException; 

}

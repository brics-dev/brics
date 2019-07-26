
package gov.nih.tbi.recaptcha.model;

import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.StaticReferenceManager;
import gov.nih.tbi.commons.ws.HashMethods;
import gov.nih.tbi.dictionary.model.StaticField;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This class creates an object for recaptcha.
 * 
 * @author Ryan Stewart
 * 
 */
public class ReCaptchaResponse {

	private boolean valid;
	private String errorMessage;

	public ReCaptchaResponse(boolean valid, String errorMessage) {
		this.valid = valid;
		this.errorMessage = errorMessage;
	}

	/**
	 * The reCaptcha error message. invalid-site-public-key invalid-site-private-key invalid-request-cookie 
	 * incorrect-captcha-sol verify-params-incorrect verify-params-incorrect recaptcha-not-reachable
	 * 
	 * @return
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
	
	/**
	 * True if captcha is "passed".
	 * @return
	 */
	public boolean isValid() {
		return valid;
	}
	
}

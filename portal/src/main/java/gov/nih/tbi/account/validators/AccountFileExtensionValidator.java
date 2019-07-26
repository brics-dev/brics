package gov.nih.tbi.account.validators;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

import gov.nih.tbi.PortalConstants;

public class AccountFileExtensionValidator extends FieldValidatorSupport {
	
	String allowedExtensions;
	
	public String getAllowedExtensions() {
		return allowedExtensions;
	}


	public void setAllowedExtensions(String allowedExtensions) {
		this.allowedExtensions = allowedExtensions;
	}
	
	private static final String REGEX_FORMAT = "(.+(\\.(?i)(%s))$)";

	@Override
	public void validate(Object object) throws ValidationException {
		Boolean isApproved = (Boolean) this.getFieldValue(PortalConstants.IS_APPROVED, object);
		
		if(!isApproved) {
			String fieldName = this.getFieldName();
	        String fileName = (String) this.getFieldValue(this.getFieldName(), object);
	        
	        if(!validateFileExtension(fileName)) {
	        	addFieldError(fieldName, "Invalid Extension");
	        }
		}
	}
	
	private boolean validateFileExtension(String fileName) {
		if(allowedExtensions == null || allowedExtensions.isEmpty()) {
			return false;
		}
		
		String regexPattern = String.format(REGEX_FORMAT, allowedExtensions);
		
		Pattern pattern = Pattern.compile(regexPattern);
		Matcher matcher = pattern.matcher(fileName);
		return matcher.matches();
	}
}

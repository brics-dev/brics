package gov.nih.tbi.account.validators;

import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.service.AccountManager;

public class UniqueUploadValidator extends FieldValidatorSupport {

	@Autowired
	AccountManager accountManager;

	@Override
	public void validate(Object object) throws ValidationException {
		String fileName = (String) this.getFieldValue(this.getFieldName(), object);
		Long userId = (Long) this.getFieldValue(PortalConstants.USER_ID, object);
		
		// If the user name is not unique, return an error
		if (fileName == null || !accountManager.checkUniqueFileName(fileName, userId)) {
			// Not sure why, but this message does not ever appear. Instead the failure message from struts xml file
			// is displayed. However a field error must be added to indicate that something has failed.
			addFieldError(fieldName, "Invalid File Name");
		}
	}
}

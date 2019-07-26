package gov.nih.tbi.account.validators;

import java.util.List;
import java.util.Map;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.repository.model.hibernate.UserFile;

public class SessionUniqueUploadValidator extends FieldValidatorSupport {

	@Override
	public void validate(Object object) throws ValidationException {
		String fileName = (String) this.getFieldValue(this.getFieldName(), object);
		List<String> uploadedFileNames = (List<String>) this.getFieldValue("sessionFileNames", object);
		
		if(uploadedFileNames == null) {
			return;
		}
		
		// If the user name is not unique, return an error
		if (uploadedFileNames.contains(fileName)) {
			// Not sure why, but this message does not ever appear. Instead the failure message from struts xml file
			// is displayed. However a field error must be added to indicate that something has failed.
			addFieldError(fieldName, "Invalid File Name");
		}
	}
}

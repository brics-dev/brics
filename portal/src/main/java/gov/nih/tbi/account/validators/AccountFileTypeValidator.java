package gov.nih.tbi.account.validators;


import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

import gov.nih.tbi.PortalConstants;

public class AccountFileTypeValidator extends FieldValidatorSupport {

	@Override
	public void validate(Object object) throws ValidationException {
		Boolean isApproved = (Boolean) this.getFieldValue(PortalConstants.IS_APPROVED, object);

		if (!isApproved) {
			String fileType = (String) this.getFieldValue(this.getFieldName(), object);
			System.out.println("File Type: " + fileType);

			String existingFileTypes = (String) this.getFieldValue(PortalConstants.EXISTING_FILE_TYPES, object);

			System.out.println("Existing file types: "+ existingFileTypes);
			String[] existingFileTypeList = existingFileTypes.split(";");

			for (String existingFileType : existingFileTypeList) {
				if (fileType.equals(existingFileType)) {
					addFieldError(fieldName, "File type already exists");
				}
			}
		}
	}
}

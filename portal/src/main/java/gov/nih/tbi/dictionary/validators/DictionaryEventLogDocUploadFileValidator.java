package gov.nih.tbi.dictionary.validators;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

import gov.nih.tbi.dictionary.model.hibernate.DictionarySupportingDocumentation;
import gov.nih.tbi.repository.model.SupportingDocumentationInterface;

public class DictionaryEventLogDocUploadFileValidator extends FieldValidatorSupport{
	private String errorMessage;

	/**
	 * Method called by struts2 validation process
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void validate(Object object) throws ValidationException {

		String fieldName = this.getFieldName();
		String fieldValue = (String) this.getFieldValue(this.getFieldName(), object);

		String addDocSelect = (String) this.getFieldValue("addDocSelect", object);

		if ("file".equals(addDocSelect)) {

			if (StringUtils.isBlank(fieldValue)) {
				errorMessage = "Upload File is a required field.";
			} else {
				// Uniqueness validation, for editing doc, only check against other documents.
				Boolean isEditingDoc = (Boolean) this.getFieldValue("isEditingDoc", object);
				String selectedDocumentName = (String) this.getFieldValue("selectedDocumentFromParam", object);

				Set<DictionarySupportingDocumentation> supportDocSet =
						(Set<DictionarySupportingDocumentation>) this.getFieldValue("eventLogSupportDocList",
								object);

				for (SupportingDocumentationInterface sd : supportDocSet) {
					if (fieldValue.equalsIgnoreCase(sd.getName())) {
						if (!isEditingDoc || (isEditingDoc && !fieldValue.equals(selectedDocumentName))) {
							errorMessage =
									"This file already exists, please select a different file.";
						}
					}
				}
			}
		}

		if (errorMessage != null) {
			this.addFieldError(fieldName, errorMessage);
		}
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}


}

package gov.nih.tbi.commons.validators;

import gov.nih.tbi.repository.model.SupportingDocumentationInterface;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class DocumentationUploadUrlValidator extends FieldValidatorSupport {

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

		if ("url".equals(addDocSelect)) {

			if (StringUtils.isBlank(fieldValue)) {
				errorMessage = "URL is a required field.";
			} else if (fieldValue.trim().length() > 255) {
				errorMessage = "URL can not exceed 255 characters.";
			} else {
				// URL pattern match
				// used the @diegoperini method at https://mathiasbynens.be/demo/url-regex
				String[] schemes = {"http", "https"};
				UrlValidator urlValidator = new UrlValidator(schemes);
				if (!urlValidator.isValid(fieldValue)) {
					errorMessage = "Please enter a valid URL.";

				} else {

					// Uniqueness validation, for editing doc, only check against other documents in meta study.
					Boolean isEditingDoc = (Boolean) this.getFieldValue("isEditingDoc", object);
					String selectedDocumentName = (String) this.getFieldValue("selectedDocumentFromParam", object);

					Set<SupportingDocumentationInterface> supportDocSet =
							(Set<SupportingDocumentationInterface>) this.getFieldValue("sessionObject.supportingDocumentationSet", object);

					for (SupportingDocumentationInterface sd : supportDocSet) {
						if (fieldValue.equalsIgnoreCase(sd.getName())) {
							if (!isEditingDoc || (isEditingDoc && !fieldValue.equals(selectedDocumentName))) {
								errorMessage =
										"This URL already exists, please enter a different URL.";
							}
						}
					}
				}
			}

			if (errorMessage != null) {
				this.addFieldError(fieldName, errorMessage);
			}
		}
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

}

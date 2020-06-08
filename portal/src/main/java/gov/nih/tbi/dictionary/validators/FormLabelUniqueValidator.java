package gov.nih.tbi.dictionary.validators;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

import gov.nih.tbi.dictionary.service.DictionaryServiceInterface;

public class FormLabelUniqueValidator extends FieldValidatorSupport {

	@Autowired
	DictionaryServiceInterface dictionaryService;
	
	@Override
	public void validate(Object object) throws ValidationException {

		String fieldName = this.getFieldName();
		String fieldValue = (String) this.getFieldValue(this.getFieldName(), object);
		
		if (StringUtils.isEmpty(fieldValue)) {
			 addFieldError(fieldName, "Invalid Form Label.");
		} else {
			if (!dictionaryService.isFormLabelUnique(fieldValue)) {
				 addFieldError(fieldName, "Form Label already exists.");
			}
		}
	}
	
}

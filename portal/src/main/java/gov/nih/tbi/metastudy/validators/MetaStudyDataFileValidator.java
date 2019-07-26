package gov.nih.tbi.metastudy.validators;

import gov.nih.tbi.metastudy.model.hibernate.MetaStudyData;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class MetaStudyDataFileValidator extends FieldValidatorSupport {

	private String errorMessage;

	/**
	 * Method called by struts2 validation process
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void validate(Object object) throws ValidationException {

		String fieldName = this.getFieldName();
		String fieldValue = (String) this.getFieldValue(this.getFieldName(), object);

		if (StringUtils.isBlank(fieldValue)) {
			errorMessage = "Upload File is a required field.";

		} else {
			
			// Uniqueness validation, for editing data file, only check against other data files in meta study.
			Boolean isEditingData = (Boolean) this.getFieldValue("isEditingData", object);
			String selectedDataName = (String) this.getFieldValue("sessionMetaStudy.selectedDataName", object);
			
			Set<MetaStudyData> dataSet =
					(Set<MetaStudyData>) this.getFieldValue("sessionMetaStudy.metaStudy.metaStudyDataSet", object);

			for (MetaStudyData msd : dataSet) {
				if (fieldValue.equalsIgnoreCase(msd.getName())) {
					if (!isEditingData || (isEditingData && !fieldValue.equals(selectedDataName))) {
						errorMessage = "This file already exists in the Meta Study, please select a different file.";
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

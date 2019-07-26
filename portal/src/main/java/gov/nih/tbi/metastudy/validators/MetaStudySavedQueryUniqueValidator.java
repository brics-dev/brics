package gov.nih.tbi.metastudy.validators;

import gov.nih.tbi.metastudy.model.hibernate.MetaStudyData;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class MetaStudySavedQueryUniqueValidator extends FieldValidatorSupport {

	private String errorMessage;

	/**
	 * Method called by struts2 validation process
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void validate(Object object) throws ValidationException {

		String fieldName = this.getFieldName();
		String fieldValue = (String) this.getFieldValue(this.getFieldName(), object);

		if (fieldValue == null || StringUtils.isBlank(fieldValue)) {
			errorMessage = "";

		} else {
			
			// Uniqueness validation, for editing data file, only check against other data files in meta study.
			Boolean isEditingData = (Boolean) this.getFieldValue("isEditingData", object);
			Long savedQueryId = (Long) this.getFieldValue("editSavedQueryId", object);
			
			Set<MetaStudyData> dataSet =
					(Set<MetaStudyData>) this.getFieldValue("sessionMetaStudy.metaStudy.metaStudyDataSet", object);

			for (MetaStudyData msd : dataSet) {
				if (msd.getSavedQuery() != null && msd.getSavedQuery().getName() != null) {
					if (!isEditingData && msd.getSavedQuery().getName().contains(fieldValue)) {
						errorMessage = "This Saved Query already exists in the Meta Study, please select a different Saved Query.";
					} 
					if(isEditingData){
						  if (!msd.getSavedQuery().getId().equals(savedQueryId) && (fieldValue.equals(msd.getSavedQuery().getName()) || fieldValue.concat(" COPY").equals(msd.getSavedQuery().getName()))){
							errorMessage = "This Saved Query already exists in the Meta Study, please select a different Saved Query.";
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

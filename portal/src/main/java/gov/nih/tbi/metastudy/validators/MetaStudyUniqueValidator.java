package gov.nih.tbi.metastudy.validators;

import gov.nih.tbi.commons.service.MetaStudyManager;

import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class MetaStudyUniqueValidator extends FieldValidatorSupport {

    @Autowired
    MetaStudyManager metaStudyManager;

    /**
     * Method called by struts2 validation process
     */
    @SuppressWarnings("unchecked")
    @Override
    public void validate(Object object) throws ValidationException {
    	
        String fieldName = this.getFieldName();
        String fieldValue = (String) this.getFieldValue(this.getFieldName(), object);

        // Gets current data element from the action
        boolean isNewTitle = false;
        Long id = (Long) this.getFieldValue("metaStudyDetailsForm.id", object);

        if (id == null) {
        	isNewTitle = true;
        } else {
        	String oldTitle = (String) this.getFieldValue("sessionMetaStudy.metaStudy.title", object);
        	if (oldTitle != null && !oldTitle.equals(fieldValue)) {
            	isNewTitle = true;
        	}
        }
        		
        if (isNewTitle && !metaStudyManager.isTitleUnique(fieldValue)) {
            // Not sure why, but this message does not ever appear. Instead the failure message from struts xml file
            // is displayed. However a field error must be added to indicate that something has failed.
            addFieldError(fieldName, "Meta Study title must be unique");
        }
    }
}

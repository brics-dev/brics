
package gov.nih.tbi.dictionary.validators;

import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.service.DictionaryToolManager;

import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

/**
 * Short Name Validator for struts2 validation:
 * 
 * Determines if size should be required.
 * 
 * @author Francis Chen
 * 
 */
public class ElementSizeValidator extends FieldValidatorSupport
{

    @Autowired
    DictionaryToolManager dictionaryToolManager;

    /**
     * Method called by struts2 validation process
     */
    @Override
    public void validate(Object object) throws ValidationException
    {

        String fieldName = this.getFieldName();
        Integer fieldValue = (Integer) this.getFieldValue(this.getFieldName(), object);

        // Gets current data structure from the action
        // AbstractDataElement dataElement = (AbstractDataElement) this.getFieldValue( "currentDataElement", object);
        DataType type = (DataType) this.getFieldValue("valueRangeForm.type", object);
        Boolean defined = (Boolean) this.getFieldValue("valueRangeForm.defined", object);
        // If type is String, check if element size is filled in
        if (DataType.ALPHANUMERIC.equals(type) && !defined && fieldValue == null)
        {
            addFieldError(fieldName, "Maximum Allowable Length/Size is required");
        }

    }

}

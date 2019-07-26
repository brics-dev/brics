
package gov.nih.tbi.dictionary.validators;

import gov.nih.tbi.commons.service.DictionaryToolManager;
import gov.nih.tbi.dictionary.model.hibernate.Alias;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;

import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class AliasValidator extends FieldValidatorSupport
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
        String fieldValue = (String) this.getFieldValue(this.getFieldName(), object);

        // Gets current data element from the action
        DataElement dataElement = (DataElement) this.getFieldValue("currentDataElement", object);

        dataElement.setName((String) this.getFieldValue("dataElementForm.name", object));

        FormStructure dataStructure = (FormStructure) this.getFieldValue("sessionDataStructure.dataStructure", object);

        // If the name is not valid then throw an error
        if (fieldValue == null
                || !dictionaryToolManager.validateAlias(dataStructure, new Alias(fieldValue), dataElement))
        {
            // Not sure why, but this message does not ever appear. Instead the failure message from struts xml file
            // is displayed. However a field error must be added to indicate that something has failed.
            addFieldError(fieldName, "Invalid Element Name");
        }

    }

}

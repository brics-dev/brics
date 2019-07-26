
package gov.nih.tbi.dictionary.validators;

import gov.nih.tbi.commons.service.DictionaryToolManager;
import gov.nih.tbi.dictionary.model.SessionDataElement;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;

import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class ElementNameValidator extends FieldValidatorSupport
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
        SessionDataElement currentSessionDataElement = (SessionDataElement) this.getFieldValue("sessionDataElement",
                object);
        DataElement currentDataElement = currentSessionDataElement.getDataElement();

        // Gets current data element from the action
        Long id = (Long) this.getFieldValue("dataElementForm.id", object);
        DataElement dataElement = new DataElement();
        dataElement.setId(id);
        dataElement.setName(fieldValue);
        dataElement.setAliasList(currentDataElement.getAliasList());

        // If the name is not valid then throw an error
        if (fieldValue == null
                || !dictionaryToolManager.validateDataElementName(dataElement, currentDataElement.getName()))
        {
            // Not sure why, but this message does not ever appear. Instead the failure message from struts xml file
            // is displayed. However a field error must be added to indicate that something has failed.
            addFieldError(fieldName, "Invalid Element Name");
        }

    }
}

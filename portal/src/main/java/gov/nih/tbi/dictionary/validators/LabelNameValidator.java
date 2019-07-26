
package gov.nih.tbi.dictionary.validators;

import gov.nih.tbi.commons.service.DictionaryToolManager;
import gov.nih.tbi.dictionary.model.hibernate.Keyword;
import gov.nih.tbi.dictionary.portal.DataElementAction;
import gov.nih.tbi.dictionary.portal.MapElementAction;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class LabelNameValidator extends FieldValidatorSupport
{

    @Autowired
    DictionaryToolManager dictionaryToolManager;

    /**
     * Method called by struts2 validation process
     */
    @SuppressWarnings("unchecked")
    @Override
    public void validate(Object object) throws ValidationException
    {

        String fieldName = this.getFieldName();
        String fieldValue = (String) this.getFieldValue(this.getFieldName(), object);

        Set<Keyword> sessionLabels = null;

        if (object instanceof MapElementAction)
        {
            MapElementAction action = (MapElementAction) object;
            sessionLabels = action.getSessionDataElement().getNewLabels();
        }
        else
            if (object instanceof DataElementAction)
            {
                DataElementAction action = (DataElementAction) object;
                sessionLabels = action.getSessionDataElement().getNewLabels();
            }

        // If the name is not valid or it already exists then throw an error
        if (fieldValue == null || dictionaryToolManager.validateLabelName(fieldValue))
        {
            // Not sure why, but this message does not ever appear. Instead the failure message from struts xml file
            // is displayed. However a field error must be added to indicate that something has failed.
            addFieldError(fieldName, "Invalid Keyword Name");
        }

        // Check against the fields that have already been added this session
        if (sessionLabels != null)
        {
            for (Keyword key : sessionLabels)
            {
                if (key.getKeyword().equals(fieldValue))
                {
                    addFieldError(fieldName, "Invalid Keyword Name");
                }
            }
        }
    }
}

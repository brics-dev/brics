
package gov.nih.tbi.dictionary.validators;

import gov.nih.tbi.commons.service.DictionaryToolManager;
import gov.nih.tbi.dictionary.model.hibernate.Keyword;
import gov.nih.tbi.dictionary.portal.DataElementAction;
import gov.nih.tbi.dictionary.portal.MapElementAction;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class KeywordNameValidator extends FieldValidatorSupport
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

        Set<Keyword> sessionKeywords = null;

        if (object instanceof MapElementAction)
        {
            MapElementAction action = (MapElementAction) object;
            sessionKeywords = action.getSessionDataElement().getNewKeywords();
        }
        else
            if (object instanceof DataElementAction)
            {
                DataElementAction action = (DataElementAction) object;
                sessionKeywords = action.getSessionDataElement().getNewKeywords();
            }

        // If the name is not valid or it already exists then throw an error
        if (fieldValue == null || dictionaryToolManager.validateKeywordName(fieldValue))
        {
            // Not sure why, but this message does not ever appear. Instead the failure message from struts xml file
            // is displayed. However a field error must be added to indicate that something has failed.
            addFieldError(fieldName, "Invalid Keyword Name");
        }

        // Check against the fields that have already been added this session
        if (sessionKeywords != null)
        {
            for (Keyword key : sessionKeywords)
            {
                if (key.getKeyword().equals(fieldValue))
                {
                    addFieldError(fieldName, "Invalid Keyword Name");
                }
            }
        }
    }
}

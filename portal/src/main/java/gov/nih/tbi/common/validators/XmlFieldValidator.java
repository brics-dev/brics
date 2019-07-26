
package gov.nih.tbi.common.validators;

import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.service.DictionaryToolManager;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

/**
 * Validates that all the characters in a string are valid xml unicode characters
 * 
 * @author Francis Chen
 * 
 */
public class XmlFieldValidator extends FieldValidatorSupport
{
    @Autowired
    DictionaryToolManager dictionaryManager;
    
    private boolean trim = true;

    public void validate(Object object) throws ValidationException
    {

        String fieldName = getFieldName();
        Object value = (String) this.getFieldValue(fieldName, object);

        // XW-375 - must be a string
        if (!(value instanceof String))
        {
            return;
        }

        String in = (String) value;

        if (trim)
        {
            in = in.trim();
        }

        char current; // Used to reference the current character.

        if (value == null || (PortalConstants.EMPTY_STRING.equals(value)))
        {
            return;
        }

        for (int i = 0; i < in.length(); i++)
        {
            current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
            if (!dictionaryManager.isValidForXml(current))
            {
                addFieldError(fieldName, object);
                return;
            }
        }
    }

    /**
     * @return Returns whether the expression should be trimed before matching. Default is <code>true</code>.
     */
    public boolean isTrimed()
    {

        return trim;
    }

    /**
     * Sets whether the expression should be trimed before matching. Default is <code>true</code>.
     */
    public void setTrim(boolean trim)
    {

        this.trim = trim;
    }

}
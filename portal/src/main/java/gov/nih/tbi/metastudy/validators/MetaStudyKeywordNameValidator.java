
package gov.nih.tbi.metastudy.validators;

import gov.nih.tbi.commons.service.MetaStudyManager;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyKeyword;
import gov.nih.tbi.metastudy.portal.MetaStudyAction;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class MetaStudyKeywordNameValidator extends FieldValidatorSupport
{

    @Autowired
    MetaStudyManager metaStudyManager;

    /**
     * Method called by struts2 validation process
     */
    @SuppressWarnings("unchecked")
    @Override
    public void validate(Object object) throws ValidationException
    {

        String fieldName = this.getFieldName();
        String keyword = (String) this.getFieldValue(this.getFieldName(), object);

        Set<MetaStudyKeyword> sessionKeywords = null;

        MetaStudyAction action = (MetaStudyAction) object;
        sessionKeywords = action.getSessionMetaStudy().getNewKeywords();

        // If the name is not valid or it already exists then throw an error
        if (keyword == null || validateKeywordName(keyword))
        {
            // Not sure why, but this message does not ever appear. Instead the failure message from struts xml file
            // is displayed. However a field error must be added to indicate that something has failed.
            addFieldError(fieldName, "Invalid Keyword Name");
            return;
        }

        // Check against the fields that have already been added this session
        if (sessionKeywords != null)
        {
            for (MetaStudyKeyword sessionKeyword : sessionKeywords)
            {
                if (sessionKeyword.getKeyword().equals(keyword))
                {
                    addFieldError(fieldName, "Invalid Keyword Name");
                }
            }
        }
    }
    
	public Boolean validateKeywordName(String keywordName) {

		List<MetaStudyKeyword> keyword = metaStudyManager.retrieveAllKeywords(); 

		for (MetaStudyKeyword key : keyword) {
			if (key.getKeyword().equals(keywordName)) {
				return true;
			}

		}

		return false;
	}
}

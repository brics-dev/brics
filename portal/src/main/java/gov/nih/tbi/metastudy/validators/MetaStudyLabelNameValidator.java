
package gov.nih.tbi.metastudy.validators;

import gov.nih.tbi.commons.service.MetaStudyManager;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyLabel;
import gov.nih.tbi.metastudy.portal.MetaStudyAction;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class MetaStudyLabelNameValidator extends FieldValidatorSupport
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
        String label = (String) this.getFieldValue(this.getFieldName(), object);

        Set<MetaStudyLabel> sessionLabels = null;

        MetaStudyAction action = (MetaStudyAction) object;
        sessionLabels = action.getSessionMetaStudy().getNewLabels();

        // If the name is not valid or it already exists then throw an error
        if (label == null || validateLabelName(label))
        {
            addFieldError(fieldName, "Invalid Label Name");
            return;
        }

        // Check against the fields that have already been added this session
        if (sessionLabels != null)
        {
            for (MetaStudyLabel sessionLabel : sessionLabels)
            {
                if (sessionLabel.getLabel().equals(label))
                {
                    addFieldError(fieldName, "Invalid Label Name");
                }
            }
        }
    }
    
	public Boolean validateLabelName(String labelName) {

		List<MetaStudyLabel> labels = metaStudyManager.retrieveAllLabels(); 

		for (MetaStudyLabel label : labels) {
			if (label.getLabel().equals(labelName)) {
				return true;
			}

		}

		return false;
	}
}

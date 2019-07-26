
package gov.nih.tbi.dictionary.validators;

import java.util.Set;

import gov.nih.tbi.commons.service.DictionaryToolManager;
import gov.nih.tbi.dictionary.model.hibernate.ClassificationElement;

import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

/**
 * Called out on data element details validation. Makes sure that each classification on the page has an item selected
 * in the drop down (non-null).
 * 
 * @author mvalei
 * 
 */
public class ElementClassificationValidator extends FieldValidatorSupport
{

    @Autowired
    DictionaryToolManager dictionaryToolManager;

    /**
     * Method called by struts2 validation process
     * 
     * Validates all the subgroups attached to the data element.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void validate(Object object) throws ValidationException
    {

        String fieldName = this.getFieldName();

        Set<ClassificationElement> classificationElementList = (Set<ClassificationElement>) this.getFieldValue(
                "dataElementForm.classificationElementList", object);

        for (ClassificationElement ce : classificationElementList)
        {
            if (ce.getClassification() == null)
            {
                addFieldError(fieldName, "Invalid Classifications");
                return;
            }
        }
    }

}

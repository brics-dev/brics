
package gov.nih.tbi.dictionary.validators;

import gov.nih.tbi.commons.service.DictionaryToolManager;
import gov.nih.tbi.dictionary.model.hibernate.DiseaseElement;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

/**
 * 
 * This is a validator for the diseaseList which is a Hash Set
 * 
 * @author mgree1
 * 
 */
public class ElementDiseaseListValidator extends FieldValidatorSupport
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

        List<DiseaseElement> diseaseElementList = (List<DiseaseElement>) this.getFieldValue(
                "dataElementForm.diseaseList", object);

        if (diseaseElementList == null || diseaseElementList.isEmpty())
        {
            addFieldError(fieldName, "Required Disease");
            return;
        }

    }
}

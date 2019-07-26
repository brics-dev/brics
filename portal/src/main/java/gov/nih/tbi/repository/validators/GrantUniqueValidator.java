
package gov.nih.tbi.repository.validators;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.service.RepositoryManager;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class GrantUniqueValidator extends FieldValidatorSupport
{

    //
    // private static final String DCB_REST_LOCATION = "http://localhost:8085/rest.service/service";
    // private static final String DCB_REST_GRANT_LOCATION = "/impacii/projectinfo/%s.xml";

    @Autowired
    RepositoryManager repositoryManager;

    /**
     * Method called by struts2 validation process
     */
    @SuppressWarnings("unchecked")
    @Override
    public void validate(Object object) throws ValidationException
    {

        String fieldName = this.getFieldName();
        String fieldValue = (String) this.getFieldValue(this.getFieldName(), object);

        List<String> grantList = (List<String>) this.getFieldValue(PortalConstants.SESSION_GRANT_LIST, object);

        // If the Grant IDis not unique, return an error
        if (grantList != null && grantList.contains(fieldValue))
        {
        	// Not sure why, but this message does not ever appear. Instead the failure message from struts xml file
            // is displayed. However a field error must be added to indicate that something has failed.
            addFieldError(fieldName, "Invalid Grant ID");
        }
    }
}

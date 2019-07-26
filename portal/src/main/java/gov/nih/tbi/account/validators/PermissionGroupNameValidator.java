
package gov.nih.tbi.account.validators;

import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.commons.service.AccountManager;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class PermissionGroupNameValidator extends FieldValidatorSupport
{

    @Autowired
    AccountManager accountManager;

    @Override
    public void validate(Object object) throws ValidationException
    {

        String fieldName = this.getFieldName();
        String fieldValue = (String) this.getFieldValue(this.getFieldName(), object);

        Long permissionGroupId = (Long) this.getFieldValue("permissionGroupId", object);

        if (fieldValue != null && !accountManager.validatePermissionGroupName(permissionGroupId, fieldValue))
        {
            addFieldError(fieldName, "Permission group must be unique");
        }
    }
}


package gov.nih.tbi.account.validators;

import org.apache.log4j.Logger;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class PasswordConfirmValidator extends FieldValidatorSupport
{
	static Logger logger = Logger.getLogger(PasswordConfirmValidator.class);
    /**
     * Method called by struts2 validation process
     */
    @Override
    public void validate(Object object) throws ValidationException
    {

        String fieldName = this.getFieldName();
        String fieldValue = (String) this.getFieldValue(this.getFieldName(), object);

        // Confirm password may be in a the confirmPasswordField instead of the accountDetailsForm.confirmPassword page
        String confirmPassword = null;

        //If we are coming from the change password screen, then we need to go the field value of confirmPassword
        if (this.getFieldName().equals("newPassword") || this.getFieldName().equals("accountDetailsForm.passwordString"))
        {	
        	if( this.getFieldName().equals("newPassword") && this.getFieldValue("confirmPassword", object) != null ){
        		confirmPassword = (String) this.getFieldValue("confirmPassword", object);
        	} else if( this.getFieldName().equals("accountDetailsForm.passwordString") 
        			&& this.getFieldValue("accountDetailsForm.confirmPassword", object) != null ){
        		confirmPassword = (String) this.getFieldValue("accountDetailsForm.confirmPassword", object);
        	}
        }
        else
        {
        	if( this.getFieldValue("accountDetailsForm.confirmPassword", object) != null){
        		fieldValue = (String) this.getFieldValue("accountDetailsForm.passwordString", object);
        		confirmPassword = (String) this.getFieldValue("accountDetailsForm.confirmPassword", object);
        	} else if (this.getFieldValue("confirmPassword", object) != null) {
        		fieldValue = (String) this.getFieldValue("newPassword", object);
        		confirmPassword = (String) this.getFieldValue("confirmPassword", object);
        	}
        }

    	if( fieldValue == null || confirmPassword == null || !fieldValue.equals(confirmPassword) )
    	// Not sure why, but this message does not ever appear. Instead the failure message from struts xml file
        // is displayed. However a field error must be added to indicate that something has failed.
        addFieldError(fieldName, "Password and confirmPassword do not match.");
    }
}

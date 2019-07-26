package gov.nih.tbi.dictionary.validators;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class FutureDateValidator extends FieldValidatorSupport {
	private static SimpleDateFormat isoFormatting = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Taking input date and check if it is today or in the future
     */
    @SuppressWarnings("unchecked")
    @Override
    public void validate(Object object) throws ValidationException {

        String fieldName = this.getFieldName();
        String inputDate = (String) this.getFieldValue(this.getFieldName(), object);

        try {
        	Date input;
        	Date currentDate = isoFormatting.parse(isoFormatting.format(new Date()));  //remove timestamp in order to compare dates only

            if (inputDate != null &&  !inputDate.equals("") ) {
            	input = isoFormatting.parse(inputDate);
                if (input.before(currentDate)){
                    addFieldError(fieldName, "The input date must be today or in the future.");
                }
            }
        }
        catch (ParseException e){
            // TODO Auto-generated catch block
			// e.printStackTrace();
        }
    }
}


package gov.nih.tbi.ordermanager.validators;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class BiospecimenOrderStartEndDateValidator extends FieldValidatorSupport
{

    private static SimpleDateFormat isoFormatting = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Taking the both the start and end dates, the dates are then compared to ensure that the start date is before the
     * end date
     */
    @SuppressWarnings("unchecked")
    @Override
    public void validate(Object object) throws ValidationException
    {

        String fieldName = this.getFieldName();
        String toDate = (String) this.getFieldValue(this.getFieldName(), object);
        String fromDate = (String) this.getFieldValue("submitDateFrom", object);

        Date end;
        Date start;

        try
        {
                end = isoFormatting.parse(toDate);
                start = isoFormatting.parse(fromDate);
                // required field will throw the error
                if (end.before(start))
                {
                    addFieldError(fieldName, "The Start Date must be before the End Date");
                }
        }
        //This error is caught in a different validator
        catch (ParseException e){}
    }

}

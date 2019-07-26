
package gov.nih.tbi.commons.validators;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class IsoDateValidator extends FieldValidatorSupport
{

    private static SimpleDateFormat isoFormatting = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public void validate(Object object) throws ValidationException
    {

        String fieldName = this.getFieldName();
        String date = (String) this.getFieldValue(this.getFieldName(), object);

        if (date != null && !date.equals(""))
        {
            // TODO Auto-generated method stub
            if (!validateIsoDate(date))
            {
                // Not sure why, but this message does not ever appear. Instead the failure message from struts xml file
                // is displayed. However a field error must be added to indicate that something has failed.
                addFieldError(fieldName, "Invalid Date");
            }
        }
        else
        {
            // just return b/c the required field will catch null values for the error message
            return;
        }
    }

    private boolean validateIsoDate(String date)
    {

        try
        {
            isoFormatting.setLenient(false);
            Date d = isoFormatting.parse(date);

            // Test to make sure that the date is within the proper range
            Date minDate = isoFormatting.parse("1900-01-01");
            Date maxDate = isoFormatting.parse("2999-01-01");

            if (d.before(minDate))
            {
                return false;
            }
            else
                if (d.after(maxDate))
                {
                    return false;
                }

        }
        catch (ParseException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        return true;
    }
}

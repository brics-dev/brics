
package gov.nih.tbi.repository.validators;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class AccessRecordStartEndDateValidator extends FieldValidatorSupport {

	private static SimpleDateFormat isoFormatting = new SimpleDateFormat("yyyy-MM-dd");

	/**
	 * Taking the both the start and end dates, the dates are then compared to ensure that the start date is before the
	 * end date
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void validate(Object object) throws ValidationException {

		String fieldName = this.getFieldName();
		String endDate = (String) this.getFieldValue(this.getFieldName(), object);
		String startDate = (String) this.getFieldValue("startAccessReportDate", object);

		Date end;
		Date start;

		try {
			if (endDate != null && startDate != null && !endDate.equals("") && !startDate.equals("")) {

				end = isoFormatting.parse(endDate);
				start = isoFormatting.parse(startDate);
				if (end.before(start)) {
					// Not sure why, but this message does not ever appear. Instead the failure message from struts
					// xml
					// file
					// is displayed. However a field error must be added to indicate that something has failed.
					addFieldError(fieldName, "The Start Date must be before the End Date");
				}
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			// e.printStackTrace();
		}
	}

}

package gov.nih.tbi.dictionary.validators;

import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.dictionary.portal.ValueRangeAction;

import java.util.List;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class PvOutputCodeUniqueValidator extends FieldValidatorSupport {

	/**
	 * Method called by struts2 validation process
	 */
	@Override
	public void validate(Object object) throws ValidationException {
		String fieldName = this.getFieldName();
		Integer fieldValue = (Integer) this.getFieldValue(this.getFieldName(), object);

		List<ValueRange> valueRanges = ((ValueRangeAction) object).getValueRangeList();
		
		if (fieldValue != null && valueRanges != null) {
			for (ValueRange pv : valueRanges) {
				
				if (pv.getOutputCode() != null && fieldValue.intValue() == pv.getOutputCode().intValue()) {
					addFieldError(fieldName, "Permissible Value Output Code must be unique.");
					break;
				}
			}
		}
	}

}

package gov.nih.tbi.ordermanager.validators;

import gov.nih.tbi.account.model.SessionAccount;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.ordermanagement.OrderManager;
import gov.nih.tbi.ordermanager.model.SessionOrder;

import org.springframework.beans.factory.annotation.Autowired;

import com.opensymphony.xwork2.validator.ValidationException;
import com.opensymphony.xwork2.validator.validators.FieldValidatorSupport;

public class OrderTitleUniqueValidator extends FieldValidatorSupport {

	@Autowired
	OrderManager orderManager;

	/**
	 * Method called by struts2 validation process
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void validate(Object object) throws ValidationException {

		String fieldName = this.getFieldName();
		String fieldValue = (String) this.getFieldValue(this.getFieldName(), object);

		// In case of editing an existing order, we need to exclude the title of the old order when checking uniqueness.
		Long orderId = null;
		SessionOrder sessionOrder = (SessionOrder) this.getFieldValue("sessionOrder", object);
		if (sessionOrder != null && sessionOrder.getOrder() != null) {
			orderId = sessionOrder.getOrder().getId();
		}

		if (fieldValue != null && !orderManager.isOrderTitleUnique(fieldValue,orderId)) {
			addFieldError(fieldName, "Invalid Order Title");
		}
	}
}

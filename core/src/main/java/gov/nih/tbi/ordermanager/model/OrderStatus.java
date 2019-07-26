
package gov.nih.tbi.ordermanager.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * These statuses will be used by the OrderService to orchestrate the workflow or ordering process. There is the high
 * level description of the status flow or the way restriction will be set up in the engine that drives the order status
 * 
 * CREATED => PERSISTED | PENDING_APPROVAL; PERSISTED => PENDING_APPROVAL | ERROR; PENDING_APPROVAL => ACCEPTED |
 * REJECTED | REVISION_REQUESTED | ERROR; REVISION_REQUESTED => PERSISTED | PENDING_APPROVAL | ERROR; REJECTED =>
 * CANCELLED | ERROR; ACCEPTED => SUBMITTED | PENDING_SUBMISSION | ERROR; PENDING_SUBMISSION => SUBMITTED | ERROR;
 * 
 * @author vpacha
 * 
 */
public enum OrderStatus {
	CREATED(0L, "Created"), APPROVED(1L, "Approved"), REJECTED(2L, "Rejected"), CANCELLED(3L, "Cancelled"),
	ERROR(4L, "Error"), PERSISTED(5L, "Created"), PENDING_APPROVAL(6L, "Pending Review"),
	PENDING_SUBMISSION(7L, "Pending Order Placement"), SUBMITTED(8L, "Submitted"),
	REVISION_REQUESTED(9L, "Revision Requested"), SHIPPED(10L, "Shipped"), COMPLETED(10L, "Completed");

	private static final Map<String, OrderStatus> lookup = new HashMap<String, OrderStatus>();
	static {
		for (OrderStatus s : EnumSet.allOf(OrderStatus.class)) {
			lookup.put(s.getValue(), s);
		}
	}

	private Long id;

	private String value;

	private OrderStatus(Long id, String value) {

		this.id = id;
		this.value = value;
	}

	public Long getId() {

		return id;
	}

	public String getValue() {

		return value;
	}

	public static OrderStatus getByValue(String value) {

		return lookup.get(value);
	}
}

package gov.nih.tbi.account.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum AccountReportType {
	NEW(0L, "New", "New Account Requests"),
	APPROVED(1L, "Approved", "Approved Account Requests"),
	REJECTED(2L, "Rejected", "Rejected Account Requests"),
	AWAITING(3L, "Awaiting Documentation", "Awaiting Account Requests"),
	PENDING(4L, "Pending Approval", "Pending Account Requests"),
	CHANGE_REQUESTED(5L, "Change Requested", "Change Requested Account Requests"),
	ALL(6L, "All", "All Account Requests");
	
	private static final Map<Long, AccountReportType> lookup = new HashMap<Long, AccountReportType>();
	private static final Map<String, AccountReportType> lookupName = new HashMap<String, AccountReportType>();
	
	static {
		for (AccountReportType s : EnumSet.allOf(AccountReportType.class)) {
			lookup.put(s.getId(), s);
			lookupName.put(s.getName(), s);
		}

	}
	
	private Long id;
	private String name;
	private String description;
	
	AccountReportType(Long id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}

	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public static AccountReportType getById(Long id) {

		return lookup.get(id);
	}

	public static AccountReportType getByName(String name) {

		return lookupName.get(name);
	}
	

}

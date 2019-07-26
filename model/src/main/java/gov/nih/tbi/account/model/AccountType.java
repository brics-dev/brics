package gov.nih.tbi.account.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import gov.nih.tbi.commons.model.RoleType;

public enum AccountType {
	
	DSR(0L,"DSR","Data Submitter Request"),
	DAR(1L,"DAR","Data Accesser Request"),
	DSRDAR(2L,"DSR + DAR","Data Submitter and Acesser Request"),
	OTHER(3L,"OTHER","Other");
	
	private static final Map<Long, AccountType> lookup = new HashMap<Long, AccountType>();
	private static final Map<String, AccountType> lookupName = new HashMap<String, AccountType>();

	static {
		for (AccountType s : EnumSet.allOf(AccountType.class)) {
			lookup.put(s.getId(), s);
			lookupName.put(s.getName(), s);
		}

	}
	
	private Long id;
	private String name;
	private String description;
	
	AccountType(Long id, String name, String description) {
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
	
	public static AccountType getById(Long id) {

		return lookup.get(id);
	}

	public static AccountType getByName(String name) {

		return lookupName.get(name);
	}
	

}

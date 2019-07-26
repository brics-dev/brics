
package gov.nih.tbi.commons.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum RoleStatus {
	ACTIVE(0L, "Active"), PENDING(1L, "Pending"), INACTIVE(2L, "Inactive"), WITHDRAWN(3L, "Withdrawn"),
	NOT_REQUESTED(4L, "Not Requested"), EXPIRING_SOON(5L, "Active - Expiring Soon"), EXPIRED(6L, "Expired");

	private static final Map<Long, RoleStatus> lookup = new HashMap<Long, RoleStatus>();

	static {
		for (RoleStatus s : EnumSet.allOf(RoleStatus.class))
			lookup.put(s.getId(), s);
	}

	private Long id;
	private String name;

	RoleStatus(Long id, String name) {

		this.id = id;
		this.name = name;
	}

	public Long getId() {

		return id;
	}

	public String getName() {

		return name;
	}

	public static RoleStatus getById(Long id) {

		return lookup.get(id);
	}
}

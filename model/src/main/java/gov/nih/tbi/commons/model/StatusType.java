
package gov.nih.tbi.commons.model;

public enum StatusType {

	DRAFT(0L, "Draft"), AWAITING_PUBLICATION(1L, "Awaiting Publication"), PUBLISHED(2L, "Published"),
	ARCHIVED(3L, "Archived"), SHARED_DRAFT(5L, "Shared Draft"),UNKNOWN(999L, "Unknown"),PUBLISH_PENDING(6L,"Publication - Pending");

	private Long id;
	private String type;

	StatusType(Long id, String type) {

		this.id = id;
		this.type = type;
	}

	public Long getId() {

		return id;
	}

	public String getType() {

		return type;
	}

	public static StatusType statusOf(Long id) {

		for (StatusType status : StatusType.values()) {
			if (id.equals(status.getId())) {
				return status;
			}
		}
		return null;
	}

	public static StatusType statusOf(String type) {

		for (StatusType status : StatusType.values()) {
			if (status.getType().equals(type)) {
				return status;
			}
		}
		return null;
	}
}

package gov.nih.tbi.commons.model;

public enum MetaStudyStatus {

	DRAFT(0L, "Draft"), AWAITING_PUBLICATION(1L, "Awaiting Publication"), PUBLISHED(2L, "Published");

	private Long id;
	private String name;

	MetaStudyStatus(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public static MetaStudyStatus statusOf(long id) {
		for (MetaStudyStatus status : MetaStudyStatus.values()) {
			if (status.getId() == id) {
				return status;
			}
		}
		return null;
	}

	public static MetaStudyStatus statusOf(String name) {
		for (MetaStudyStatus status : MetaStudyStatus.values()) {
			if (status.getName().equals(name)) {
				return status;
			}
		}
		return null;
	}
}

package gov.nih.tbi.commons.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum DatasetStatus {
	PRIVATE(0L, "Private", "Private", true),
	SHARED(1L, "Shared", "Share", false),
	ARCHIVED(2L, "Archived", "Archive", false),
	DELETED(3L, "Deleted", "Delete", false),
	UPLOADING(4L, "Uploading", "Upload", true),
	LOADING(5L, "Loading Data", "Load Data", false),
	ERROR(6L, "Error During Load", "Error During Load", true),
	CANCELLED(7L, "User Stopped", "User Stopped", false);

	private static final Map<Long, DatasetStatus> idLookup = new HashMap<Long, DatasetStatus>();
	private static final Map<String, DatasetStatus> nameLookup = new HashMap<String, DatasetStatus>();

	static {
		for (DatasetStatus s : EnumSet.allOf(DatasetStatus.class)) {
			idLookup.put(s.getId(), s);
			nameLookup.put(s.getName().toLowerCase(), s);
		}
	}

	private Long id;
	private String name;
	private String verb;
	private boolean deletable;

	DatasetStatus(Long _id, String _name, String _verb, boolean _deletable ) {

		this.id = _id;
		this.name = _name;
		this.verb = _verb;
		this.deletable = _deletable;
	}

	public Long getId() {

		return id;
	}

	public String getName() {

		return name;
	}

	public String getVerb() {

		return verb;
	}

	public boolean getIsDeletable() {

		return this.deletable;
	}

	public static DatasetStatus getById(Long id) {

		return idLookup.get(id);
	}

	public static DatasetStatus getByName(String name) {
		if (name != null) {
			name = name.toLowerCase();
		}
		return nameLookup.get(name);
	}
}

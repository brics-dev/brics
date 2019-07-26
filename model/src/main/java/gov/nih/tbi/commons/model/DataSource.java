package gov.nih.tbi.commons.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum DataSource {
	REPOSITORY(0L, "Data Repository"), QUERY_TOOL(1L, "Query Tool");

	private static final Map<Long, DataSource> idLookup = new HashMap<Long, DataSource>();
	private static final Map<String, DataSource> nameLookup = new HashMap<String, DataSource>();

	static {
		for (DataSource d : EnumSet.allOf(DataSource.class)) {
			idLookup.put(d.getId(), d);
			nameLookup.put(d.getName().toLowerCase(), d);
		}
	}

	private Long id;
	private String name;

	DataSource(Long id, String name) {

		this.id = id;
		this.name = name;
	}

	public Long getId() {

		return id;
	}

	public String getName() {

		return name;
	}

	public static DataSource getById(Long id) {

		return idLookup.get(id);
	}

	public static DataSource getByName(String name) {
		if (name != null) {
			name = name.toLowerCase();
		}
		return nameLookup.get(name);
	}

	public static DataSource getByNamePartial(String compareTo) {
		compareTo = compareTo.toLowerCase();
		if (compareTo != null) {
			for (DataSource ds : DataSource.values()) {
				if (ds.name.toLowerCase().contains(compareTo)) {
					return ds;
				}
			}
		}
		return null;
	}
}

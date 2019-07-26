
package gov.nih.tbi.commons.model;

public enum EntityType {
	DATA_STRUCTURE(0L, "Data Structure", RoleType.ROLE_DICTIONARY),
	DATA_ELEMENT(1L, "Data Element", RoleType.ROLE_DICTIONARY), STUDY(2L, "Study", RoleType.ROLE_STUDY),
	DATASET(3L, "Dataset", RoleType.ROLE_STUDY), SAVED_QUERY(4L, "Saved Query", RoleType.ROLE_QUERY),
	USER_FILE(5L, "User File", RoleType.ROLE_DICTIONARY), META_STUDY(6L, "Meta Study", RoleType.ROLE_METASTUDY),
	EFORM(7L, "eForm", RoleType.ROLE_DICTIONARY);

	private Long id;
	private String name;
	private RoleType roleName;

	EntityType(Long id, String name, RoleType roleName) {

		this.id = id;
		this.name = name;
		this.roleName = roleName;
	}

	public Long getId() {

		return id;
	}

	public String getName() {

		return name;
	}

	public RoleType getRole() {

		return roleName;
	}

	public RoleType getAdminRole() {

		String test = roleName.getName() + "_ADMIN";
		return RoleType.valueOf(test);
	}
	
	public static EntityType nameOf(String name) {

		for (EntityType type : EntityType.values()) {
			if (type.name().equals(name)) {
				return type;
			}
		}
		return null;
	}
}

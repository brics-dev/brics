
package gov.nih.tbi.commons.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum RoleType {
	ROLE_USER(0L, "ROLE_USER", "Account", "Allows user to log into system, manage profile and password, and upload documentation", false, true),
	ROLE_ADMIN(1L, "ROLE_ADMIN", "Admin", "Administrator of the system. Provides full access to the entire site", true, false),
	ROLE_DICTIONARY(2L, "ROLE_DICTIONARY", "Data Dictionary", "View and submit requests to create or edit data elements and form structures", false, true),
	ROLE_DICTIONARY_ADMIN(3L, "ROLE_DICTIONARY_ADMIN", "Dictionary Admin", "Administrative functionality for the Data Dictionary tool", true, false),
	ROLE_GUID(4L, "ROLE_GUID", "GUID", "Create and view study subject Global Unique Identifiers (GUIDs)", false, true),
	ROLE_GUID_ADMIN(5L, "ROLE_GUID_ADMIN", "GUID Admin", "Administrative functionality for the GUID tool", true, false),
	ROLE_STUDY(6L, "ROLE_STUDY", "Data Repository", "Create and administer studies containing research data; validate, upload and download datasets", false, true),
	ROLE_STUDY_ADMIN(7L, "ROLE_STUDY_ADMIN", "Study Admin", "Administrative functionality for the Study tool", true, false),
	ROLE_QUERY(8L, "ROLE_QUERY", "Query", "View, filter, and download research data by study.", false, true),
	ROLE_QUERY_ADMIN(9L, "ROLE_QUERY_ADMIN", "Query Tool Admin", "Administrative functionlaity for the Query tool", true, false),
	ROLE_REPOSITORY_ADMIN(10L, "ROLE_REPOSITORY_ADMIN", "Repository Admin", "Administrative functionality for the Repository Manager", true, false),
	ROLE_PROFORMS(11L, "ROLE_PROFORMS", "ProFoRMS", "Create, design, and administer forms for prospective data collection", false, true),
	ROLE_PROFORMS_ADMIN(12L, "ROLE_PROFORMS_ADMIN", "ProFoRMS Admin", "Administrative functionality for ProFoRMS", true, false),
	ROLE_ORDER_ADMIN(13L, "ROLE_ORDER_ADMIN", "Biosample Orders Admin", "Administrative functionailty for Biosample Orders. Create biosample orders in repository catalogs.", true, false),
	ROLE_ACCOUNT_ADMIN(14L, "ROLE_ACCOUNT_ADMIN", "Account Admin", "Administrative functionailty for User Accounts", true, false),
	ROLE_METASTUDY(15L, "ROLE_METASTUDY", "Meta Study", "Create and administer Meta Studies containing research data, upload and download study documentation and data artifacts", false, true),
	ROLE_METASTUDY_ADMIN(16L, "ROLE_METASTUDY_ADMIN", "Meta Study Admin", "Administrative functionality for the Meta Study", true, false),
	ROLE_DICTIONARY_EFORM(17L, "ROLE_DICTIONARY_EFORM", "Dictionary eForm", "eForm functionality for the Data Dictionary tool", true, false),
	ROLE_ACCOUNT_REVIEWER(18L, "ROLE_ACCOUNT_REVIEWER", "Account Reviewer", "Allows users charged with reviewing account requests and renewals to view and manage relevant accounts", false, false),
	ROLE_REPORTING_ADMIN(19L, "ROLE_REPORTING_ADMIN", "Reporting Admin", "Administrative functionlaity for the Reports", true, false),
	ROLE_CLINICAL_ADMIN(20L, "ROLE_CLINICAL_ADMIN", "ProFoRMS Clinical Admin", "Administrative functionlaity for the Clinicals in ProFoRMS", true, false);


	private static final Map<Long, RoleType> lookup = new HashMap<Long, RoleType>();
	private static final Map<String, RoleType> lookupName = new HashMap<String, RoleType>();

	static {
		for (RoleType s : EnumSet.allOf(RoleType.class)) {
			lookup.put(s.getId(), s);
			lookupName.put(s.getName(), s);
		}

	}

	private Long id;
	private String name;
	private String title;
	private String description;
	private boolean isAdmin;
	private boolean alwaysShow;

	RoleType(Long id, String name, String title, String description, boolean isAdmin, boolean alwaysShow) {

		this.id = id;
		this.name = name;
		this.title = title;
		this.description = description;
		this.isAdmin = isAdmin;
		this.alwaysShow = alwaysShow;
	}

	public Long getId() {

		return id;
	}

	public String getName() {

		return name;
	}

	public String getTitle() {

		return title;
	}

	public String getDescription() {

		return description;
	}

	public static RoleType getById(Long id) {

		return lookup.get(id);
	}

	public static RoleType getByName(String name) {

		return lookupName.get(name);
	}

	public boolean getIsAdmin() {

		return isAdmin;
	}

	public boolean getAlwaysShow() {
		return alwaysShow;
	}

	public static RoleType[] getRoleTypes() {
		RoleType[] out = {ROLE_USER, ROLE_ACCOUNT_REVIEWER, ROLE_ADMIN, ROLE_DICTIONARY, ROLE_DICTIONARY_EFORM,
				ROLE_DICTIONARY_ADMIN, ROLE_GUID, ROLE_GUID_ADMIN, ROLE_STUDY, ROLE_STUDY_ADMIN, ROLE_QUERY,
				ROLE_QUERY_ADMIN, ROLE_ORDER_ADMIN, ROLE_ACCOUNT_ADMIN, ROLE_METASTUDY, ROLE_METASTUDY_ADMIN,
				ROLE_PROFORMS, ROLE_PROFORMS_ADMIN};
		return out;
	}
	
	//This should be removed when other non-NTRR instances get the reporting module
	public static RoleType[] getNTRRRoleTypes() {
		RoleType[] out = {ROLE_USER, ROLE_ACCOUNT_REVIEWER, ROLE_ADMIN, ROLE_DICTIONARY, ROLE_DICTIONARY_EFORM,
				ROLE_DICTIONARY_ADMIN, ROLE_GUID, ROLE_GUID_ADMIN, ROLE_STUDY, ROLE_STUDY_ADMIN, ROLE_QUERY,
				ROLE_QUERY_ADMIN, ROLE_PROFORMS, ROLE_PROFORMS_ADMIN, ROLE_ORDER_ADMIN,
				ROLE_ACCOUNT_ADMIN, ROLE_METASTUDY, ROLE_METASTUDY_ADMIN,ROLE_REPORTING_ADMIN};
		return out;
	}

	public static RoleType[] getAlwaysShownRoles() {
		RoleType[] out = {ROLE_USER, ROLE_DICTIONARY, ROLE_GUID, ROLE_STUDY, ROLE_QUERY, ROLE_PROFORMS, ROLE_METASTUDY};
		return out;
	}
	
	//This should be removed when other non-NTRR instances get the clinical part in proforms
	public static RoleType[] getCISTARRoleTypes() {
		RoleType[] out = {ROLE_USER, ROLE_ACCOUNT_REVIEWER, ROLE_ADMIN, ROLE_DICTIONARY, ROLE_DICTIONARY_EFORM,
				ROLE_DICTIONARY_ADMIN, ROLE_GUID, ROLE_GUID_ADMIN, ROLE_STUDY, ROLE_STUDY_ADMIN, ROLE_QUERY,
				ROLE_QUERY_ADMIN, ROLE_PROFORMS, ROLE_PROFORMS_ADMIN, ROLE_ORDER_ADMIN,
				ROLE_ACCOUNT_ADMIN, ROLE_METASTUDY, ROLE_METASTUDY_ADMIN,ROLE_REPORTING_ADMIN, ROLE_CLINICAL_ADMIN};
		return out;
	}
}

package gov.nih.tbi.commons.model;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

public enum StudyType {
	@SerializedName("Natural History")
	NATURAL_HISTORY(0L, "Natural History"), 
	@SerializedName("Epidemiology")
	EPIDEMIOLOGY(1L, "Epidemiology"), 
	@SerializedName("Clinical Trial")
	CLINICAL_TRIAL(2L, "Clinical Trial"),
	@SerializedName("Pre-Clinical")
	PRE_CLINICAL(3L, "Pre-Clinical"), 
	@SerializedName("Meta Analysis")
	META_ANALYSIS(4L, "Meta Analysis"),
	@SerializedName("Other")
	OTHER(5L, "Other"),
	@SerializedName("Retrospective Review")
	RETROSPECTIVE_REVIEW(6L, "Retrospective Review"),
	@SerializedName("Prospective Observational")
	PROSPECTIVE_OBSERVATIONAL(7L, "Prospective Observational"),
	@SerializedName(" Case Report")
	CASE_REPORT(8L, "Case Report"),
	@SerializedName(" Case Series")
	CASE_SERIES(9L, "Case Series");
	
	private static final Set<StudyType> studyTypeSet = new HashSet<StudyType>();
	private static final Map<String, StudyType> lookupByName = new HashMap<String, StudyType>();
	private static final Map<Long, StudyType> lookupById = new HashMap<Long, StudyType>();

	static {
		studyTypeSet.addAll(EnumSet.allOf(StudyType.class));

		for (StudyType st : studyTypeSet) {
			lookupByName.put(st.getName(), st);
			lookupById.put(st.getId(), st);
		}
	}

	private Long id;
	private String name;

	StudyType(Long id, String name) {
		this.id = id;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public static StudyType getStudyTypeFromName(String name) {
		return lookupByName.get(name);
	}
	
	public static StudyType getStudyTypeById(Long id) {
		return lookupById.get(id);
	}

	/**
	 * Generates an array of StudyType enums from the cached set of StudyType enums, which contains all study types in
	 * the system. The returned array is not cached itself. So a new array of study types will be created on each
	 * invocation of this method, therefore the returned array may be altered at will.
	 * 
	 * @return An array of all study types in the system.
	 */
	public static synchronized StudyType[] getStudyOnlyTypes() {
		return studyTypeSet.toArray(new StudyType[studyTypeSet.size()]);
	}

	/**
	 * Retrieves the study type name based on the study type ID. If there is no study type associated with the specified
	 * ID, an empty string will be returned instead.
	 * 
	 * @param id - The study type ID to search for.
	 * @return The name of the study type that corresponds to the specified ID, or an empty string if no such relation
	 *         exists.
	 */
	public static synchronized String getNameFromId(Long id) {
		String typeName = "";

		if (lookupById.containsKey(id)) {
			typeName = lookupById.get(id).getName();
		}

		return typeName;
	}

}

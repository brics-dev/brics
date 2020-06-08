package gov.nih.tbi.file.model;

import java.util.HashMap;
import java.util.Map;

import gov.nih.tbi.commons.model.EntityType;

public enum SystemFileCategory {
	DATASET(0L, "dataset", EntityType.DATASET), DOWNLOAD(1L, "download", null),
	ELECTRONIC_SIGNATURE(2L, "electronic_signature", null), META_STUDY(3L, "meta_study", EntityType.META_STUDY),
	ORDER_MANAGER(4L, "order_manager", null), RESEARCH_MANAGEMENT(5L, "research_management", null),
	QUESTION_DOCUMENT(6L, "question_document", EntityType.EFORM), STUDY(7L, "study", EntityType.STUDY),
	META_STUDY_SUPPORTING_DOCUMENTATION(8L, "meta_study_supporting_documentation", EntityType.META_STUDY),
	STUDY_SUPPORTING_DOCUMENTATION(9L, "study_supporting_documentaion", EntityType.STUDY),
	FORM_STRUCTURE_SUPPORTING_DOCUMENTATION(10L, "form_structure_supporting_documentation", EntityType.DATA_STRUCTURE),
	DATA_ELEMENT_SUPPORTING_DOCUMENTATION(11L, "data_element_supporting_documentation", EntityType.DATA_ELEMENT);

	private static final Map<Long, SystemFileCategory> lookup = new HashMap<>();

	static {
		for (SystemFileCategory type : SystemFileCategory.values()) {
			lookup.put(type.getId(), type);
		}
	}

	private Long id;
	private String directoryName;
	private EntityType entityType;

	private SystemFileCategory(Long id, String directoryName, EntityType entityType) {
		this.setId(id);
		this.setDirectoryName(directoryName);
		this.setEntityType(entityType);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDirectoryName() {
		return directoryName;
	}

	public void setDirectoryName(String directoryName) {
		this.directoryName = directoryName;
	}

	public EntityType getEntityType() {
		return entityType;
	}

	public void setEntityType(EntityType entityType) {
		this.entityType = entityType;
	}

	/**
	 * Retrieves the SystemFileCategory enum by its ID.
	 * 
	 * @param targetId - The ID of the SystemFileCategory to retrieve.
	 * @return The SystemFileCategory enum that corresponds with the given ID, or null if it is not found.
	 */
	public static SystemFileCategory getById(Long targetId) {
		return lookup.get(targetId);
	}
}

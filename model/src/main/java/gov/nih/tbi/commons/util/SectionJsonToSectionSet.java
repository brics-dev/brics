package gov.nih.tbi.commons.util;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.dictionary.model.hibernate.eform.Section;

public class SectionJsonToSectionSet {
	static Logger logger = Logger.getLogger(SectionJsonToSectionSet.class);

	private JSONArray sectionsJSONArr;

	private HashMap<Long, Section> sectionMap = new HashMap<Long, Section>();

	public SectionJsonToSectionSet(JSONArray sectionsJSONArr) {
		setSectionsJSONArr(sectionsJSONArr);
	}

	// the return is a map of sections
	// the string key is the section ID
	public HashMap<Long, Section> parseSectionJSONToSectionMap() {

		// create all the sections and add them to the section map
		if (sectionsJSONArr.length() > 0) {
			for (int i = 0; i < sectionsJSONArr.length(); i++) {
				Section section = createSection(sectionsJSONArr.getJSONObject(i));
				addSectionMap(section.getId(), section);
			}
		}
		return this.sectionMap;
	}

	private Section createSection(JSONObject sectionJson) {

		Section section = new Section();
		section.setId(sectionJson.getLong(ModelConstants.ID));
		section.setFormCol(sectionJson.getInt(ModelConstants.EFROM_COLUMN));
		section.setFormRow(sectionJson.getInt(ModelConstants.EFROM_ROW));
		section.setName(sectionJson.getString(ModelConstants.EFROM_NAME));
		section.setDescription(sectionJson.getString(ModelConstants.EFROM_DESCRIPTION).trim());
		section.setCollapsable(sectionJson.getBoolean("isCollapsable"));
		section.setIsRepeatable(sectionJson.getBoolean("isRepeatable"));
		section.setInitialRepeatedSections(sectionJson.getInt("initRepeatedSecs"));
		section.setMaxRepeatedSections(sectionJson.getInt("maxRepeatedSecs"));
		String repeatedSectionParentString=sectionJson.getString("repeatedSectionParent");
		Long repeatedSectionParent = null;
		if (!repeatedSectionParentString.equals("-1")) {
			if (repeatedSectionParentString.startsWith(ModelConstants.EFROM_SECTION_PATTERN)) {
				repeatedSectionParentString = repeatedSectionParentString.substring(2, repeatedSectionParentString.length());
			}
			repeatedSectionParent = Long.valueOf(repeatedSectionParentString);
		}
		section.setRepeatedSectionParent(repeatedSectionParent);
		section.setGroupName(sectionJson.getString("repeatableGroupName"));
		section.setIsManuallyAdded(sectionJson.getBoolean("isManuallyAdded"));
		
		return section;
	}

	private void setSectionsJSONArr(JSONArray sectionsJSONArr) {
		this.sectionsJSONArr = sectionsJSONArr;
	}

	private void addSectionMap(Long key, Section section) {
		this.sectionMap.put(key, section);
	}
	
	public HashMap<Long, Section> getSectionMap(){
		return this.sectionMap;
	}
}
package gov.nih.tbi.dictionary.model.hibernate.eform.adapters;

import gov.nih.tbi.dictionary.model.migration.eform.MigratedSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement
@XmlType(name = "MigratedEform")
@XmlAccessorType(XmlAccessType.FIELD)
public class EformMigrationAdapter {

	@XmlElement(name = "eformShortName")
	private String eformShortName = "";
	
	@XmlElementWrapper(name = "migratedSectionIds")
	@XmlElement(name = "migratedSection")
	private ArrayList<MigratedSection> sectionList = new ArrayList<MigratedSection>();

	public EformMigrationAdapter() {}
								
	public EformMigrationAdapter(String eformShortName, HashMap<Long, MigratedSection> returnSectionMap) {
		this.eformShortName = eformShortName;
		for(Entry<Long, MigratedSection> section : returnSectionMap.entrySet()){
			this.sectionList.add(section.getValue());
		}
	}

	public List<MigratedSection> getSectionList() {
		return this.sectionList;
	}

	public String getEformShortName() {
		return this.eformShortName;
	}
}

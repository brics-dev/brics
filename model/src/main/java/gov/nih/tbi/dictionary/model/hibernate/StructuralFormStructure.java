package gov.nih.tbi.dictionary.model.hibernate;

import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.dictionary.model.restful.StructuralFormStructureListItem;
import gov.nih.tbi.dictionary.model.restful.ListableEntity;
import gov.nih.tbi.repository.model.SubmissionType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlID;

import org.apache.log4j.Logger;

@Entity
@Table(name = "DATA_STRUCTURE")
@XmlRootElement(name = "StructuralFormStructure")
@XmlAccessorType(XmlAccessType.FIELD)
public class StructuralFormStructure implements Serializable, ListableEntity<StructuralFormStructureListItem> {

	private static final long serialVersionUID = 7170213749668994317L;

	static Logger logger = Logger.getLogger(StructuralFormStructure.class);

	private static final String MAIN_REPEATABLE_GROUP_NAME = "main";

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "dataStructure", targetEntity = RepeatableGroup.class, orphanRemoval = true)
	@OrderBy(value = "position")
	private Set<RepeatableGroup> repeatableGroups;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "formStructure", targetEntity = DiseaseStructure.class, orphanRemoval = true)
	private Set<DiseaseStructure> diseaseList;

	/**********************************************************************/

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DATA_STRUCTURE_SEQ")
	@SequenceGenerator(name = "DATA_STRUCTURE_SEQ", sequenceName = "DATA_STRUCTURE_SEQ", allocationSize = 1)
	private Long id;

	@XmlID
	@Column(name = "SHORT_NAME")
	private String shortName;

	@Column(name = "VERSION")
	private String version;
	@Column(name = "TITLE")
	private String title;
	@Column(name = "DESCRIPTION")
	private String description;
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "STATUS_ID")
	private StatusType status;
	@Column(name = "PUBLICATION_DATE")
	private Date publicationDate;
	@Column(name = "VALIDATABLE")
	private Boolean validatable;
	@Column(name = "ORGANIZATION")
	private String organization;
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "FILE_TYPE_ID")
	private SubmissionType fileType;
	@Column(name = "IS_COPYRIGHTED")
	private Boolean isCopyrighted;
	@Column(name = "MODIFIED_DATE")
	private Date modifiedDate;
	@Column(name = "MODIFIED_USER_ID")
	private Long modifiedUserId;
	// added by Ching-Heng
	@Column(name = "IS_CAT")
	private boolean isCAT;
	@Column(name = "CAT_OID")
	private String catOid;
	@Column(name = "MEASUREMENT_TYPE")
	private String measurementType;

	@JoinTable(name = "form_structure_supporting_documentation", joinColumns = {
			@JoinColumn(name = "form_structure_id")}, inverseJoinColumns = {
					@JoinColumn(name = "supporting_documentation_id")})
	@ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity = DictionarySupportingDocumentation.class)
	private Set<DictionarySupportingDocumentation> supportingDocumentationSet =
			new HashSet<DictionarySupportingDocumentation>();

	/**********************************************************************/

	public Long getId() {

		return id;
	}

	public void setId(Long id) {

		this.id = id;
	}

	public String getShortName() {

		return shortName;
	}

	public void setShortName(String shortName) {

		this.shortName = shortName;
	}

	public String getVersion() {

		return version;
	}

	public void setVersion(String version) {

		this.version = version;
	}

	public String getTitle() {

		return title;
	}

	public void setTitle(String title) {

		this.title = title;
	}

	public String getDescription() {

		return description;
	}

	public void setDescription(String description) {

		this.description = description;
	}

	public StatusType getStatus() {

		return status;
	}

	public void setStatus(StatusType status) {

		this.status = status;
	}

	public Date getPublicationDate() {

		return publicationDate;
	}

	public void setPublicationDate(Date publicationDate) {

		this.publicationDate = publicationDate;
	}

	public Boolean getValidatable() {

		return validatable;
	}

	public void setValidatable(Boolean validatable) {

		this.validatable = validatable;
	}

	public String getOrganization() {

		return organization;
	}

	public void setOrganization(String organization) {

		this.organization = organization;
	}

	public SubmissionType getFileType() {

		return fileType;
	}

	public void setFileType(SubmissionType fileType) {

		this.fileType = fileType;
	}

	public Boolean getIsCopyrighted() {

		return isCopyrighted;
	}

	public void setIsCopyrighted(Boolean isCopyrighted) {

		this.isCopyrighted = isCopyrighted;
	}

	public Date getModifiedDate() {
		return modifiedDate;
	}

	public void setModifiedDate(Date modifiedDate) {
		this.modifiedDate = modifiedDate;
	}

	public Long getModifiedUserId() {
		return modifiedUserId;
	}

	public void setModifiedUserId(Long modifiedUserId) {
		this.modifiedUserId = modifiedUserId;
	}
	public boolean isCAT() {
		return isCAT;
	}

	public void setIsCat(boolean isCAT) {
		this.isCAT = isCAT;
	}

	
	public String getCatOid() {
		return catOid;
	}

	public void setCatOid(String catOid) {
		this.catOid = catOid;
	}

	public String getMeasurementType() {
		return measurementType;
	}

	public void setMeasurementType(String measurementType) {
		this.measurementType = measurementType;
	}

	/**********************************************************************/

	public String getReadableName() {

		return shortName + " v" + version;
	}


	public Set<RepeatableGroup> getRepeatableGroups() {

		if (repeatableGroups == null) {
			repeatableGroups = new HashSet<RepeatableGroup>();
		}

		return repeatableGroups;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((version == null) ? 0 : version.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StructuralFormStructure other = (StructuralFormStructure) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (version == null) {
			if (other.version != null)
				return false;
		} else if (!version.equals(other.version))
			return false;
		return true;
	}

	public void setRepeatableGroups(Set<RepeatableGroup> repeatableGroups) {

		if (this.repeatableGroups == null) {
			this.repeatableGroups = new HashSet<RepeatableGroup>();
		}

		this.repeatableGroups.clear();

		if (repeatableGroups != null) {
			this.repeatableGroups.addAll(repeatableGroups);
		}
	}

	public Set<DiseaseStructure> getDiseaseList() {

		return diseaseList;
	}

	public void setDiseaseList(Set<DiseaseStructure> diseaseList) {

		this.diseaseList = diseaseList;
	}

	/*
	 * public Set<FormStructurePublishNote> getPublishNoteSet() {
	 * 
	 * return publishNoteSet; }
	 * 
	 * 
	 * public void setPublishNoteSet(Set<FormStructurePublishNote> publishNoteSet) {
	 * 
	 * this.publishNoteSet = publishNoteSet; }
	 */

	/**
	 * Returns a set of all the MapElements that are attached to this data structure through a repeatable group.
	 * 
	 * @return elements
	 */
	public Set<MapElement> getDataElements() {

		if (repeatableGroups == null) {
			repeatableGroups = new HashSet<RepeatableGroup>();
		}

		Set<MapElement> elements = new HashSet<MapElement>();

		for (RepeatableGroup group : repeatableGroups) {
			for (MapElement mapElement : group.getMapElements()) {
				elements.add(mapElement);
			}
		}

		return elements;
	}

	/**
	 * This will take a list of map elements in the data structure and return a unique list of all the data elements in
	 * the structure
	 * 
	 * @return
	 */
	public List<StructuralDataElement> getUniqueDataElements() {

		Set<MapElement> allData = getDataElements();
		List<StructuralDataElement> uniqueList = new ArrayList<StructuralDataElement>();

		// loop through all the map elements in the list
		for (MapElement element : allData) {
			boolean isUnique = true;
			StructuralDataElement currentElement = element.getStructuralDataElement();
			// loop through the return list of DEs
			for (StructuralDataElement inReturnList : uniqueList) {
				// if a duplicate DE is found in the list set the unique
				// identifier to false
				if (currentElement.equals(inReturnList)) {
					isUnique = false;
				}
			}
			// add the DE to the list if it is found to be unique
			if (isUnique) {
				uniqueList.add(element.getStructuralDataElement());
			}
		}

		return uniqueList;
	}

	public RepeatableGroup getMainRepeatableGroup() {

		if (repeatableGroups == null || repeatableGroups.isEmpty()) {
			return null;
		} else {
			for (RepeatableGroup rg : repeatableGroups) {
				if (rg.getName().equalsIgnoreCase(MAIN_REPEATABLE_GROUP_NAME)) {
					return rg;
				}
			}
		}

		return null;
	}

	/**
	 * Returns the number of repeatable groups associated with this dataStructure
	 * 
	 * @return
	 */
	public Integer getSize() {

		return repeatableGroups.size();
	}

	/**
	 * Returns the repeatable group in this data structure with a certain name
	 * 
	 * @param name
	 * @return
	 */
	public RepeatableGroup getRepeatableGroupByName(String name) {

		if (repeatableGroups != null) {
			for (RepeatableGroup repeatableGroup : repeatableGroups) {
				if (name.equalsIgnoreCase(repeatableGroup.getName())) {
					return repeatableGroup;
				}
			}
		}

		return null;
	}

	public String getDiseaseStructureString() {

		StringBuilder toReturn = new StringBuilder();
		int index = 0;
		int diseaseListLen = getDiseaseList().size();
		for (DiseaseStructure ds : getDiseaseList()) {
			if (index == (diseaseListLen - 1))
				toReturn.append(ds.getDisease().getName());
			else
				toReturn.append(ds.getDisease().getName() + ", ");
			index++;
		}
		// This is clear out the extra white space
		toReturn.trimToSize();
		return toReturn.toString();
	}

	public String getShortNameAndVersion() {

		return shortName + "V" + version;
	}

	public Set<DictionarySupportingDocumentation> getSupportingDocumentationSet() {
		return supportingDocumentationSet;
	}

	public void setSupportingDocumentationSet(Set<DictionarySupportingDocumentation> supportingDocumentationSet) {
		this.supportingDocumentationSet = supportingDocumentationSet;
	}

	public void addSupportingDocumentation(DictionarySupportingDocumentation supportingDocumentation) {
		if (this.supportingDocumentationSet == null) {
			this.supportingDocumentationSet = new HashSet<DictionarySupportingDocumentation>();
		}

		this.supportingDocumentationSet.add(supportingDocumentation);
	}

	public StructuralFormStructureListItem getListItem() {

		StructuralFormStructureListItem result = new StructuralFormStructureListItem(this.id, this.shortName,
				this.version, this.status, this.organization);

		return result;
	}
}

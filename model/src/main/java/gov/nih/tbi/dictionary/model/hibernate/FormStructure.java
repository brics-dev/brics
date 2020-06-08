package gov.nih.tbi.dictionary.model.hibernate;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.RequiredType;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.util.BRICSStringUtils;
import gov.nih.tbi.dictionary.model.FormStructureStandardization;
import gov.nih.tbi.dictionary.model.InstanceRequiredFor;
import gov.nih.tbi.dictionary.model.rdf.SemanticFormStructure;
import gov.nih.tbi.repository.model.SubmissionType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.log4j.Logger;

@XmlRootElement(name = "formStructure")
@XmlAccessorType(XmlAccessType.FIELD)
public class FormStructure implements Serializable {

	private static final long serialVersionUID = 4136318235943074307L;

	static Logger logger = Logger.getLogger(FormStructure.class);

	public static final int DEFAULT_STATUS = 0;

	public static final String ID = "id";
	public static final String SHORT_NAME = "shortName";
	public static final String VERSION = "version";
	public static final String TITLE = "title";
	public static final String DESCRIPTION = "description";
	public static final String STATUS = "status";
	public static final String PUBLICATION_DATE = "publicationDate";
	public static final String VALIDATABLE = "validatable";
	public static final String ORGANIZATION = "organization";

	@XmlElement(name = "StructuralFormStructure")
	private StructuralFormStructure structuralObject;
	@XmlElement(name = "SemanticFormStructure")
	private SemanticFormStructure semanticObject;
	@XmlElement(name = "DataElement")
	private Map<String, DataElement> dataElements;

	/**********************************************************************/
	public FormStructure() {

		this(new StructuralFormStructure(), new SemanticFormStructure());
	}

	public FormStructure(FormStructure formStructure) {

		this(new StructuralFormStructure(), new SemanticFormStructure());

		this.setShortName(formStructure.getShortName());
		this.setTitle(formStructure.getTitle());
		this.setOrganization(formStructure.getOrganization());
		this.setDateCreated(formStructure.getDateCreated());
		this.setCreatedBy(formStructure.getCreatedBy());
		this.setStatus(formStructure.getStatus());
		this.setModifiedDate(formStructure.getModifiedDate());
		this.setVersion(formStructure.getVersion());
		this.setModifiedUserId(formStructure.getModifiedUserId());
		this.setPublicationDate(formStructure.getPublicationDate());

		this.setDescription(formStructure.getDescription());
		this.setFileType(formStructure.getFileType());
		this.setIsCopyrighted(formStructure.getIsCopyrighted());
		this.setStandardization(formStructure.getStandardization());
		this.addAllInstancesRequiredFor(formStructure.getInstancesRequiredFor());
		//added by Ching-Heng
		this.setIsCat(formStructure.isCAT());
		this.setCatOid(formStructure.getCatOid());
		this.setMeasurementType(formStructure.getMeasurementType());
		// copy dataElements (r.s)
		this.dataElements = formStructure.dataElements;
		
		
		//copy SupportingDocumentation
        Set<DictionarySupportingDocumentation> supportingDocumentationSet = new HashSet<DictionarySupportingDocumentation>();
        
        for (DictionarySupportingDocumentation suppDoc : formStructure.getSupportingDocumentationSet())
        {
        	DictionarySupportingDocumentation supportingDocumentation = new DictionarySupportingDocumentation();
        	
        	supportingDocumentation.setDescription(suppDoc.getDescription());
        	supportingDocumentation.setFileType(suppDoc.getFileType());
        	supportingDocumentation.setUrl(suppDoc.getUrl());
        	supportingDocumentation.setUserFile(suppDoc.getUserFile());
        	supportingDocumentation.setDateCreated(suppDoc.getDateCreated());
        	
        	supportingDocumentationSet.add(supportingDocumentation);
        }
        
        this.setSupportingDocumentationSet(supportingDocumentationSet);

		// Copy Diseases
		Set<DiseaseStructure> diseaseStructureSet = new HashSet<DiseaseStructure>();

		for (DiseaseStructure ds : formStructure.getDiseaseList()) {
			Disease d = new Disease(ds.getDisease());
			DiseaseStructure newDs = new DiseaseStructure(d, this.getFormStructureSqlObject());
			diseaseStructureSet.add(newDs);
		}

		this.setDiseaseList(diseaseStructureSet);

		// Copy Repeatable Groups
		Set<RepeatableGroup> oldRepeatableGroups = formStructure.getRepeatableGroups();
		Set<RepeatableGroup> newRepeatableGroups = new HashSet<RepeatableGroup>();
		Long groupElementId = Long.valueOf(-2);
		Long mapElementId = Long.valueOf(-1);
		for (RepeatableGroup oldRepeatableGroup : oldRepeatableGroups) {
			RepeatableGroup newRepeatableGroup = new RepeatableGroup(oldRepeatableGroup);

			// we need to give these groups on groupId, since the main group is
			// always main i will say this group is -1,
			if (newRepeatableGroup.getName().equals("Main")) {
				newRepeatableGroup.setId(Long.valueOf(-1));
			} else {
				// other groups get a -Long
				newRepeatableGroup.setId(groupElementId);
				groupElementId--;
			}
			newRepeatableGroup.setDataStructure(this.getFormStructureSqlObject());

			LinkedHashSet<MapElement> mapElementList = new LinkedHashSet<MapElement>();
			for (MapElement mapElement : oldRepeatableGroup.getMapElements()) {
				// map elements need an id as well.
				MapElement newMapElement = new MapElement(mapElement);
				newMapElement.setId(mapElementId);
				mapElementId--;
				newMapElement.setRepeatableGroup(newRepeatableGroup);
				mapElementList.add(newMapElement);
			}

			newRepeatableGroup.setMapElements(mapElementList);

			newRepeatableGroups.add(newRepeatableGroup);
		}
		this.setRepeatableGroups(newRepeatableGroups);

	}

	public FormStructure(StructuralFormStructure sqlObject) {

		this(sqlObject, new SemanticFormStructure());
	}

	public FormStructure(SemanticFormStructure rdfObject) {

		this(new StructuralFormStructure(), rdfObject);
	}

	public FormStructure(StructuralFormStructure sqlObject, SemanticFormStructure rdfObject) {

		this.structuralObject = sqlObject;
		this.semanticObject = rdfObject;
	}

	public FormStructure(StructuralFormStructure sqlObject, SemanticFormStructure rdfObject,
			Map<String, DataElement> dataElements) {

		this.structuralObject = sqlObject;
		this.semanticObject = rdfObject;
		this.dataElements = dataElements;
	}

	public Long getId() {

		return this.structuralObject.getId();
	}

	public void setId(Long id) {

		this.structuralObject.setId(id);
	}

	public String getShortName() {

		return this.structuralObject.getShortName();
	}

	public void setShortName(String shortName) {

		this.semanticObject.setShortName(shortName);
		this.structuralObject.setShortName(shortName);
	}

	public String getVersion() {

		return this.structuralObject.getVersion();
	}

	public void setVersion(String version) {

		this.semanticObject.setVersion(version);
		this.structuralObject.setVersion(version);
	}

	public String getTitle() {

		return this.structuralObject.getTitle();
	}

	public void setTitle(String title) {

		this.semanticObject.setTitle(title);
		this.structuralObject.setTitle(title);
	}

	public String getDescription() {

		return this.structuralObject.getDescription();
	}

	public void setDescription(String description) {

		this.semanticObject.setDescription(description);
		this.structuralObject.setDescription(description);
	}

	public StatusType getStatus() {

		return this.semanticObject.getStatus();
	}

	public void setStatus(StatusType status) {

		// TODO: MV 6/6/2014 - Status should only be stored in the semantic
		// object.
		this.structuralObject.setStatus(status);
		this.semanticObject.setStatus(status);
	}
	
	public Date getPublicationDate() {

		return this.structuralObject.getPublicationDate();
	}

	public void setPublicationDate(Date publicationDate) {

		this.structuralObject.setPublicationDate(publicationDate);
	}

	public Boolean getValidatable() {

		return this.structuralObject.getValidatable();
	}

	public void setValidatable(Boolean validatable) {

		this.structuralObject.setValidatable(validatable);
	}

	public String getOrganization() {

		if (this.semanticObject.getOrganization() != null)
			return this.semanticObject.getOrganization();
		return this.structuralObject.getOrganization();
	}

	public void setOrganization(String organization) {

		this.semanticObject.setOrganization(organization);
		this.structuralObject.setOrganization(organization);
	}

	public SubmissionType getFileType() {

		return this.structuralObject.getFileType();
	}

	public void setFileType(SubmissionType fileType) {

		this.semanticObject.setSubmissionType(fileType);
		this.structuralObject.setFileType(fileType);
	}

	public Boolean getIsCopyrighted() {

		return this.structuralObject.getIsCopyrighted();
	}

	public void setIsCopyrighted(Boolean isCopyrighted) {

		this.semanticObject.setIsCopyrighted(isCopyrighted);
		this.structuralObject.setIsCopyrighted(isCopyrighted);
	}
	
	// added by Ching-Heng
	public void setIsCat(Boolean isCAT) {
		this.structuralObject.setIsCat(isCAT);
	}

	public boolean isCAT() {
		return this.structuralObject.isCAT();
	}
	
	public void setCatOid(String catOid) {
		this.structuralObject.setCatOid(catOid);
	}
	
	public String getCatOid() {
		return this.structuralObject.getCatOid();
	}
	
	public void setMeasurementType(String measurementType) {
		this.structuralObject.setMeasurementType(measurementType);
	}
	
	public String getMeasurementType() {
		return this.structuralObject.getMeasurementType();
	}
	
	/**********************************************************************/

	public String getReadableName() {

		return this.structuralObject.getReadableName();
	}

	public Set<RepeatableGroup> getRepeatableGroups() {

		return this.structuralObject.getRepeatableGroups();
	}

	public void setRepeatableGroups(Set<RepeatableGroup> repeatableGroups) {

		this.structuralObject.setRepeatableGroups(repeatableGroups);
	}

	public Set<DiseaseStructure> getDiseaseList() {

		return this.structuralObject.getDiseaseList();
	}

	public void setDiseaseList(Set<DiseaseStructure> diseaseList) {

		/**
		 * Drops the existing list of diseases and recreates it with this new list. Since this is a "set" and not an
		 * "add", this is the expected behavior.
		 */
		getDiseases().clear();
		for (DiseaseStructure diseaseStructure : diseaseList) {
			getDiseases().add(diseaseStructure.getDisease());
		}

		this.structuralObject.setDiseaseList(diseaseList);
	}

	public List<FormLabel> getFormLabelList() {
		return this.semanticObject.getFormLabels();
	}
	
	public void setFormLabelList(List<FormLabel> formLabels) {
		this.semanticObject.setFormLabels(formLabels);
	}
	
	/**
	 * Returns a set of all the MapElements that are attached to this data structure through a repeatable group.
	 * 
	 * @return elements
	 */
	public Set<MapElement> getMapElements() {

		return this.structuralObject.getDataElements();
	}

	/**
	 * Returns a set of all the DataElements that are attached to this data structure through a repeatable group.
	 * Returns empty if the list is blank.
	 * 
	 * @return elements
	 */
	public Map<String, DataElement> getDataElements() {

		// if the FS has no associated DEs return blank list
		if (dataElements == null) {
			dataElements = new HashMap<String, DataElement>();
		}
		return dataElements;
	}

	/**
	 * Returns the list of data elements in this form structure sorted by the map element position index (Note: this is
	 * used for data element report page)
	 * 
	 * @return
	 */
	public List<DataElement> getSortedDataElementList(RepeatableGroup repeatableGroup) {
		List<MapElement> mapElements = new ArrayList<MapElement>(repeatableGroup.getMapElements());
		Collections.sort(mapElements); // sort mapElements by position field

		List<DataElement> dataElements = new ArrayList<DataElement>();

		// need to extract the dataElements from the mapElements
		for (MapElement mapElement : mapElements) {
			DataElement currentDataElement =
					getDataElements().get(mapElement.getStructuralDataElement().getNameAndVersion());
			dataElements.add(currentDataElement);
		}

		return dataElements;
	}
	
	/**
	 * Returns the list of required fields of data elements in this form structure sorted by the map element position index (Note: this is
	 * used for data element report page)
	 * 
	 * @return
	 */
	public List<Boolean> getSortedDataElementRequiredList(RepeatableGroup repeatableGroup) {
		List<MapElement> mapElements = new ArrayList<MapElement>(repeatableGroup.getMapElements());
		Collections.sort(mapElements); // sort mapElements by position field

		List<Boolean> requiredElements = new ArrayList<Boolean>();

		// need to extract the dataElements from the mapElements
		for (MapElement mapElement : mapElements) {
		    Boolean requiredElement = (mapElement.getRequiredType() == RequiredType.REQUIRED);
		    requiredElements.add(requiredElement);
		}

		return requiredElements;
	}

	public List<RepeatableGroup> getSortedRepeatableGroups() {
		List<RepeatableGroup> repeatableGroups = new ArrayList<RepeatableGroup>(getRepeatableGroups());
		Collections.sort(repeatableGroups);
		return repeatableGroups;
	}

	public List<DataElement> getSortedDataElementList() {
		Set<String> deNames = new HashSet<String>();
		List<RepeatableGroup> sortedRepeatableGroups = getSortedRepeatableGroups();
		List<DataElement> sortedDataElements = new ArrayList<DataElement>();
		for (RepeatableGroup repeatableGroup : sortedRepeatableGroups) {
			for (DataElement de : getSortedDataElementList(repeatableGroup)) {
				if (!deNames.contains(de.getName())) {
					deNames.add(de.getName());
					sortedDataElements.add(de);
				}
			}
		}
		return sortedDataElements;
	}

	/**
	 * This will take a list of map elements in the data structure and return a unique list of all the data elements in
	 * the structure
	 * 
	 * @return
	 */
	public List<StructuralDataElement> getUniqueDataElements() {

		return this.structuralObject.getUniqueDataElements();
	}

	public RepeatableGroup getMainRepeatableGroup() {

		return this.structuralObject.getMainRepeatableGroup();
	}

	/**
	 * Returns the number of repeatable groups associated with this dataStructure
	 * 
	 * @return
	 */
	public Integer getSize() {

		return this.structuralObject.getSize();
	}

	/**
	 * Returns the repeatable group in this data structure with a certain name
	 * 
	 * @param name
	 * @return
	 */
	public RepeatableGroup getRepeatableGroupByName(String name) {

		return this.structuralObject.getRepeatableGroupByName(name);
	}

	public String getDiseaseStructureString() {

		return this.structuralObject.getDiseaseStructureString();
	}

	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + ((structuralObject == null) ? 0 : structuralObject.hashCode());
		result = prime * result + ((semanticObject == null) ? 0 : semanticObject.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FormStructure other = (FormStructure) obj;
		if (structuralObject == null) {
			if (other.structuralObject != null)
				return false;
		} else if (!structuralObject.equals(other.structuralObject))
			return false;
		if (semanticObject == null) {
			if (other.semanticObject != null)
				return false;
		} else if (!semanticObject.equals(other.semanticObject))
			return false;
		return true;
	}

	/* generate the getter and setter delegates for the rdf object */

	public String getRDFVersion() {

		return this.semanticObject.getVersion();
	}

	public void setRDFVersion(String version) {

		this.semanticObject.setVersion(version);
	}

	public String getUri() {

		return this.semanticObject.getUri();
	}

	public void setUri(String uri) {

		this.semanticObject.setUri(uri);
	}

	public String getRDFTitle() {

		return this.semanticObject.getTitle();
	}

	public void setRDFTitle(String title) {

		this.semanticObject.setTitle(title);
	}

	public String getRDFDescription() {

		return this.semanticObject.getDescription();
	}

	public void setRDFDescription(String description) {

		this.semanticObject.setDescription(description);
	}

	public List<Disease> getDiseases() {

		return this.semanticObject.getDiseases();
	}

	public void setDiseases(List<Disease> diseases) {

		this.semanticObject.setDiseases(diseases);
	}

	/**
	 * @return the modifiedDate
	 */
	public Date getModifiedDate() {

		if (this.semanticObject.getModifiedDate() != null)
			return this.semanticObject.getModifiedDate();
		return this.structuralObject.getModifiedDate();
	}

	/**
	 * @param modifiedDate the modifiedDate to set
	 */
	public void setModifiedDate(Date modifiedDate) {

		this.semanticObject.setModifiedDate(modifiedDate);
		this.structuralObject.setModifiedDate(modifiedDate);
	}

	/**
	 * @return the modifiedAccount
	 */
	public Long getModifiedUserId() {

		return this.semanticObject.getModifiedUserId();
	}

	/**
	 * @param modifiedAccount the modifiedAccount to set
	 */
	public void setModifiedUserId(Long modUserId) {

		this.semanticObject.setModifiedUserId(modUserId);
		this.structuralObject.setModifiedUserId(modUserId);
	}

	public StructuralFormStructure getFormStructureSqlObject() {

		return this.structuralObject;
	}

	public SemanticFormStructure getFormStructureRDFObject() {

		return this.semanticObject;
	}

	public String getShortNameAndVersion() {

		return getShortName() + "V" + getVersion();
	}

	public Date getDateCreated() {

		return semanticObject.getDateCreated();
	}

	public String getDateCreatedString() throws DateParseException {

		Date date = semanticObject.getDateCreated();
		return date != null ? BRICSTimeDateUtil.formatDate(date) : ModelConstants.EMPTY_STRING;
	}

	public void setDateCreated(long dateCreated) {
		semanticObject.setDateCreated(dateCreated);
	}

	public void setDateCreated(Date dateCreated) {

		semanticObject.setDateCreated(dateCreated);
	}

	//why is this a String ??
	public void setDateCreated(String dateCreated) {

		semanticObject.setDateCreated(dateCreated);
	}

	//why is this a String ??
	public String getCreatedBy() {

		return semanticObject.getCreatedBy();
	}

	//why is this a String ??
	public void setCreatedBy(String createdBy) {

		semanticObject.setCreatedBy(createdBy);
	}

	public Set<DictionarySupportingDocumentation> getSupportingDocumentationSet() {
		return structuralObject.getSupportingDocumentationSet();
	}

	public void setSupportingDocumentationSet(Set<DictionarySupportingDocumentation> supportingDocumentationSet) {
		structuralObject.setSupportingDocumentationSet(supportingDocumentationSet);
	}

	public void addSupportingDocumentation(DictionarySupportingDocumentation supportingDocumentation) {
		structuralObject.addSupportingDocumentation(supportingDocumentation);
	}
	
    public FormStructureStandardization getStandardization(){
    	return semanticObject.getStandardization();
    }
    
    public void setStandardization(FormStructureStandardization standardization){
    	semanticObject.setStandardization(standardization);;
    }
    
    //gets the list of instances that marked this form structure as required
    // if empty no instances marked form as required
	public List<InstanceRequiredFor> getInstancesRequiredFor() {
       return semanticObject.getInstancesRequiredFor();
   }
   
	public void addAllInstancesRequiredFor(List<InstanceRequiredFor> instancesRequiredFor) {

		semanticObject.addAllInstancesRequiredFor(instancesRequiredFor);
	}
	

	public void addAllInstancesRequiredForString(List<String> instancesRequiredFor) {

		semanticObject.addAllInstancesRequiredForStrings(instancesRequiredFor);
	}

	public void addInstancesRequiredFor(String instancesRequiredFor) {

		semanticObject.addInstancesRequiredFor(instancesRequiredFor);
	}
}


package gov.nih.tbi.dictionary.model.rdf;

import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.dictionary.model.hibernate.Category;
import gov.nih.tbi.dictionary.model.hibernate.ClassificationElement;
import gov.nih.tbi.dictionary.model.hibernate.ExternalId;
import gov.nih.tbi.dictionary.model.hibernate.Keyword;
import gov.nih.tbi.dictionary.model.hibernate.Population;
import gov.nih.tbi.dictionary.model.hibernate.Schema;
import gov.nih.tbi.dictionary.model.hibernate.SubDomainElement;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.dictionary.model.hibernate.StructuralDataElement;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@XmlRootElement(name = "SemanticDataElement")
@XmlAccessorType(XmlAccessType.FIELD)
public class SemanticDataElement implements Serializable {

	private static final long serialVersionUID = -7013304123061289885L;

	/**** PROPERTIES *********************************************************/

	private String uri;
	private String title;
	private String description;
	private String shortDescription;
	private Set<SubDomainElement> subDomainElementList;
	private String format;
	private String notes;
	private String guidelines;
	private String historicalNotes;
	private String references;
	private Population population;
	private Set<ClassificationElement> classificationElementList;
	private Set<Keyword> keywords;
	private Set<Keyword> labels;
	@XmlTransient
	private Set<ExternalId> externalIdSet;

	// These properties exist in both the semantic and the structural
	private DataType type;
	private Category category;
	private Date dateCreated;
	private String createdBy;
	private DataElementStatus status;
	private Set<ValueRange> valueRangeList;
	private String version;
	private String name;

	private String submittingOrgName;
	private String submittingContactName;
	private String submittingContactInfo;
	private String stewardOrgName;
	private String stewardContactName;
	private String stewardContactInfo;
	private Date effectiveDate;
	private Date untilDate;
	private Date modifiedDate;
	private String seeAlso;

	/**** CONSTRUCTORS *******************************************************/

	public SemanticDataElement() {

	}

	// public SemanticDataElement(SemanticDataElement dataElement)
	// {
	//
	// this.name = dataElement.getName();
	// this.title = dataElement.getTitle();
	// this.description = dataElement.getDescription();
	// this.shortDescription = dataElement.getShortDescription();
	// this.format = dataElement.getFormat();
	// this.notes = dataElement.getNotes();
	// this.guidelines = dataElement.getGuidelines();
	// this.historicalNotes = dataElement.getHistoricalNotes();
	// this.references = dataElement.getReferences();
	// this.population = dataElement.getPopulation();
	// this.setClassificationElementList(dataElement.getClassificationElementList());
	// this.setKeywordList(dataElement.getKeywordList());
	// this.setExternalIdSet(dataElement.getExternalIdSet());
	// this.setValueRangeList(dataElement.getValueRangeList());
	// }

	/**** GETTERS AND SETTERS ************************************************/

	public SemanticDataElement(SemanticDataElement semanticObject) {

		this.uri = semanticObject.getUri();
		this.title = semanticObject.getTitle();
		this.description = semanticObject.getDescription();
		this.shortDescription = semanticObject.getShortDescription();
		this.format = semanticObject.getFormat();
		this.notes = semanticObject.getNotes();
		this.guidelines = semanticObject.getGuidelines();
		this.historicalNotes = semanticObject.getHistoricalNotes();
		this.references = semanticObject.getReferences();
		this.type = semanticObject.getType();
		this.dateCreated = semanticObject.getDateCreated();
		this.createdBy = semanticObject.getCreatedBy();
		this.status = semanticObject.getStatus();
		this.version = semanticObject.getVersion();
		this.name = semanticObject.getName();
		this.submittingOrgName = semanticObject.getSubmittingOrgName();
		this.submittingContactInfo = semanticObject.getSubmittingContactInfo();
		this.submittingContactName = semanticObject.getSubmittingContactName();
		this.stewardOrgName = semanticObject.getStewardOrgName();
		this.stewardContactName = semanticObject.getStewardContactName();
		this.stewardContactInfo = semanticObject.getStewardContactInfo();
		this.effectiveDate = semanticObject.getEffectiveDate();
		this.untilDate = semanticObject.getUntilDate();
		this.modifiedDate = semanticObject.getModifiedDate();
		this.seeAlso = semanticObject.getSeeAlso();

		// Copy value range list
		if (semanticObject.getValueRangeList() != null) {
			Set<ValueRange> vrList = new HashSet<ValueRange>();
			for (ValueRange vr : semanticObject.getValueRangeList()) {
				ValueRange newVr = new ValueRange();
				newVr.setDescription(vr.getDescription());
				newVr.setOutputCode(vr.getOutputCode());
				newVr.setValueRange(vr.getValueRange());
				vrList.add(newVr);
			}
			this.setValueRangeList(vrList);
		}

		if (semanticObject.getCategory() != null) {
			Category category = new Category(semanticObject.getCategory());
			this.category = category;
		}

		// Copy External Ids
		if (semanticObject.getExternalIdSet() != null) {
			Set<ExternalId> idList = new HashSet<ExternalId>();
			for (ExternalId id : semanticObject.getExternalIdSet()) {
				ExternalId newId = new ExternalId();
				newId.setValue(id.getValue());
				newId.setSchema(id.getSchema());
				newId.setSemanticDataElement(semanticObject);
				idList.add(newId);
			}
			this.setExternalIdSet(idList);
		}

		// Copy Labels
		if (semanticObject.getLabels() != null) {
			Set<Keyword> labelList = new HashSet<Keyword>();
			for (Keyword l : semanticObject.getLabels()) {
				Keyword newK = new Keyword();
				newK.setKeyword(l.getKeyword());
				newK.setCount(l.getCount());
				labelList.add(newK);
			}
			this.labels = labelList;
		}

		// Copy Keywords
		if (semanticObject.getKeywords() != null) {
			Set<Keyword> keyList = new HashSet<Keyword>();
			for (Keyword k : semanticObject.getKeywords()) {
				Keyword newK = new Keyword();
				newK.setKeyword(k.getKeyword());
				newK.setCount(k.getCount());
				keyList.add(newK);
			}
			this.keywords = keyList;
		}

		if (semanticObject.getPopulation() != null) {
			Population newPopulation = new Population();
			newPopulation.setId(semanticObject.getPopulation().getId());
			newPopulation.setName(semanticObject.getPopulation().getName());
			this.population = newPopulation;
		}

		if (semanticObject.getClassificationElementList() != null) {
			Set<ClassificationElement> classList = new HashSet<ClassificationElement>();
			for (ClassificationElement c : semanticObject.getClassificationElementList()) {
				ClassificationElement newC = new ClassificationElement();
				newC.setDisease(c.getDisease());
				newC.setSubgroup(c.getSubgroup());
				newC.setClassification(c.getClassification());
				classList.add(newC);
			}
			this.classificationElementList = classList;
		}

		// Copy SubdomainElementList
		if (semanticObject.getSubDomainElementList() != null) {
			Set<SubDomainElement> subList = new HashSet<SubDomainElement>();
			for (SubDomainElement s : semanticObject.getSubDomainElementList()) {
				SubDomainElement newS = new SubDomainElement();
				newS.setDisease(s.getDisease());
				newS.setDomain(s.getDomain());
				newS.setSubDomain(s.getSubDomain());
				subList.add(newS);
			}
			this.subDomainElementList = subList;
		}
	}

	/**
	 * @return the uri
	 */
	public String getUri() {

		return uri;
	}

	public String getName() {

		return name;
	}

	public void setName(String name) {

		this.name = name;
	}

	/**
	 * @param uri the uri to set
	 */
	public void setUri(String uri) {

		this.uri = uri;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {

		return title;
	}

	/**
	 * @param title the title to set
	 */
	public void setTitle(String title) {

		this.title = title;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {

		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {

		this.description = description;
	}

	/**
	 * @return the shortDescription
	 */
	public String getShortDescription() {

		return shortDescription;
	}

	/**
	 * @param shortDescription the shortDescription to set
	 */
	public void setShortDescription(String shortDescription) {

		this.shortDescription = shortDescription;
	}

	public Set<SubDomainElement> getSubDomainElementList() {

		if (subDomainElementList == null) {
			subDomainElementList = new HashSet<SubDomainElement>();
		}
		return subDomainElementList;
	}

	public void setSubDomainElementList(Set<SubDomainElement> subDomainElementList) {

		if (this.subDomainElementList == null) {
			this.subDomainElementList = new HashSet<SubDomainElement>();
		}

		this.subDomainElementList.clear();

		if (subDomainElementList != null) {
			this.subDomainElementList.addAll(subDomainElementList);
		}
	}

	/**
	 * @return the format
	 */
	public String getFormat() {

		return format;
	}

	/**
	 * @param format the format to set
	 */
	public void setFormat(String format) {

		this.format = format;
	}

	/**
	 * @return the notes
	 */
	public String getNotes() {

		return notes;
	}

	/**
	 * @param notes the notes to set
	 */
	public void setNotes(String notes) {

		this.notes = notes;
	}

	/**
	 * @return the guidlines
	 */
	public String getGuidelines() {

		return guidelines;
	}

	/**
	 * @param guidlines the guidlines to set
	 */
	public void setGuidelines(String guidelines) {

		this.guidelines = guidelines;
	}

	/**
	 * @return the historicalNotes
	 */
	public String getHistoricalNotes() {

		return historicalNotes;
	}

	/**
	 * @param historicalNotes the historicalNotes to set
	 */
	public void setHistoricalNotes(String historicalNotes) {

		this.historicalNotes = historicalNotes;
	}

	/**
	 * @return the references
	 */
	public String getReferences() {

		return references;
	}

	/**
	 * @param references the references to set
	 */
	public void setReferences(String references) {

		this.references = references;
	}

	/**
	 * @return the population
	 */
	public Population getPopulation() {

		return population;
	}

	/**
	 * @param population the population to set
	 */
	public void setPopulation(Population population) {

		this.population = population;
	}

	/**
	 * @return the classificationElementList
	 */
	public Set<ClassificationElement> getClassificationElementList() {

		if (classificationElementList == null) {
			classificationElementList = new HashSet<ClassificationElement>();
		}
		return classificationElementList;
	}

	/**
	 * @param classificationElementList the classificationElementList to set
	 */
	public void setClassificationElementList(Set<ClassificationElement> classificationElementList) {

		if (this.classificationElementList == null) {
			this.classificationElementList = new TreeSet<ClassificationElement>();
		}

		this.classificationElementList.clear();

		if (classificationElementList != null) {
			this.classificationElementList.addAll(classificationElementList);
		}
	}

	public Set<Keyword> getLabels() {

		if (labels == null) {
			labels = new HashSet<Keyword>();
		}
		return labels;
	}

	public void setLabels(Set<Keyword> labels) {

		this.labels = labels;
	}

	/**
	 * @return the keywordList
	 */
	public Set<Keyword> getKeywords() {

		if (keywords == null) {
			keywords = new HashSet<Keyword>();
		}
		return keywords;
	}

	/**
	 * @param keywords the keywords to set
	 */
	public void setKeywords(Set<Keyword> keywords) {

		this.keywords = keywords;
	}

	/**
	 * @return the externalIdSet
	 */
	public Set<ExternalId> getExternalIdSet() {

		if (externalIdSet == null) {
			this.externalIdSet = new HashSet<ExternalId>();
		}
		return externalIdSet;
	}

	/**
	 * @param externalIdSet the externalIdSet to set
	 */
	public void setExternalIdSet(Set<ExternalId> externalIdSet) {

		if (this.externalIdSet == null) {
			this.externalIdSet = new HashSet<ExternalId>();
		}

		this.externalIdSet.clear();

		if (externalIdSet != null) {
			this.externalIdSet.addAll(externalIdSet);
		}
	}

	public void addExternalId(ExternalId externalId) {

		if (this.externalIdSet == null) {
			this.externalIdSet = new HashSet<ExternalId>();
		}

		this.externalIdSet.add(externalId);
	}

	/**** FUNCTIONS **********************************************************/

	/**
	 * Updates the specified external id to the given value. If the given value does not exist then it is added.
	 * 
	 * @param type
	 * @param id
	 */
	public void updateExternalId(Schema schema, String value) {

		if (this.externalIdSet == null) {
			this.externalIdSet = new HashSet<ExternalId>();
		}

		for (ExternalId external : externalIdSet) {
			if (schema.equals(external.getSchema())) {
				external.setValue(value);
				return;
			}
		}
		
		externalIdSet.add(new ExternalId(schema, value));
	}

	/**
	 * Gets the external id of the specified type. If one does not exist they null is returned.
	 * 
	 * @param type
	 * @return
	 */
	public ExternalId getExternalId(Schema schema) {

		if (schema == null || externalIdSet == null) {
			return null;
		}

		for(ExternalId externalId:externalIdSet) {
			if(schema.equals(externalId.getSchema())) {
				return externalId;
			}
		}
		
		return null;
	}

	public void setValueRangeList(Set<ValueRange> valueRangeList) {

		if (this.valueRangeList == null) {
			this.valueRangeList = new HashSet<ValueRange>();
		}

		this.valueRangeList.clear();

		if (valueRangeList != null) {
			this.valueRangeList.addAll(valueRangeList);
		}
	}

	public Set<ValueRange> getValueRangeList() {

		return valueRangeList == null ? new TreeSet<ValueRange>() : valueRangeList;
	}

	public void addValueRange(ValueRange valueRange) {

		if (valueRangeList == null) {
			valueRangeList = new TreeSet<ValueRange>();
		}

		// we need to do this for sorting because it needs to know the data type
		// which comes from StructuralDataElement
		StructuralDataElement dataElement = valueRange.getDataElement();

		for (ValueRange item : valueRangeList) {
			item.setDataElement(dataElement);
		}

		valueRangeList.add(valueRange);
	}
	
	public void removeValueRange(ValueRange valueRange){  	
    	this.valueRangeList.remove(valueRange);
    }

	public String getVersion() {

		return version;
	}

	public void setVersion(String version) {

		this.version = version;
	}

	/**
	 * @return the type
	 */
	public DataType getType() {

		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(DataType type) {

		this.type = type;
	}

	/**
	 * @return the category
	 */
	public Category getCategory() {

		return category;
	}

	/**
	 * @param category the category to set
	 */
	public void setCategory(Category category) {

		this.category = category;
	}

	/**
	 * @return the dateCreated
	 */
	public Date getDateCreated() {

		return dateCreated;
	}

	/**
	 * @param dateCreated the dateCreated to set
	 */
	public void setDateCreated(Date dateCreated) {

		this.dateCreated = dateCreated;
	}

	public String getCreatedBy() {

		return createdBy;
	}

	public void setCreatedBy(String createdBy) {

		this.createdBy = createdBy;
	}

	/**
	 * @return the status
	 */
	public DataElementStatus getStatus() {

		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(DataElementStatus status) {

		this.status = status;
	}

	public String getSubmittingOrgName() {

		return submittingOrgName;
	}

	public void setSubmittingOrgName(String submittingOrgName) {

		this.submittingOrgName = submittingOrgName;
	}

	public String getSubmittingContactName() {

		return submittingContactName;
	}

	public void setSubmittingContactName(String submittingContactName) {

		this.submittingContactName = submittingContactName;
	}

	public String getSubmittingContactInfo() {

		return submittingContactInfo;
	}

	public void setSubmittingContactInfo(String submittingContactInfo) {

		this.submittingContactInfo = submittingContactInfo;
	}

	public String getStewardOrgName() {

		return stewardOrgName;
	}

	public void setStewardOrgName(String stewardOrgName) {

		this.stewardOrgName = stewardOrgName;
	}

	public String getStewardContactName() {

		return stewardContactName;
	}

	public void setStewardContactName(String stewardContactName) {

		this.stewardContactName = stewardContactName;
	}

	public String getStewardContactInfo() {

		return stewardContactInfo;
	}

	public void setStewardContactInfo(String stewardContactInfo) {

		this.stewardContactInfo = stewardContactInfo;
	}

	public Date getEffectiveDate() {

		return effectiveDate;
	}

	public void setEffectiveDate(Date effectiveDate) {

		this.effectiveDate = effectiveDate;
	}

	public Date getUntilDate() {

		return untilDate;
	}

	public void setUntilDate(Date untilDate) {

		this.untilDate = untilDate;
	}

	/**
	 * @return the modifiedDate
	 */
	public Date getModifiedDate() {

		return modifiedDate;
	}

	/**
	 * @param modifiedDate the modifiedDate to set
	 */
	public void setModifiedDate(Date modifiedDate) {

		this.modifiedDate = modifiedDate;
	}

	public String getSeeAlso() {

		return seeAlso;
	}

	public void setSeeAlso(String seeAlso) {

		this.seeAlso = seeAlso;
	}

	public void addSubDomainElement(SubDomainElement subDomainElement) {

		if (subDomainElementList == null) {
			subDomainElementList = new HashSet<SubDomainElement>();
		}

		subDomainElementList.add(subDomainElement);
	}

	public void addClassificationElement(ClassificationElement classificationElement) {

		if (classificationElementList == null) {
			classificationElementList = new HashSet<ClassificationElement>();
		}

		classificationElementList.add(classificationElement);
	}

	public String getShortNameAndVersion() {

		return name + "V" + version;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;

		// Getter methods are needed here to ensure that Hibernate/JPA retrieves the proper values.
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		result = prime * result + ((getVersion() == null) ? 0 : getVersion().hashCode());

		return result;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null) {
			return false;
		}
		if (this == obj) {
			return true;
		}
		if (obj instanceof SemanticDataElement) {

			SemanticDataElement semanticDE = (SemanticDataElement) obj;
			return getName().equals(semanticDE.getName()) && getVersion().equals(semanticDE.getVersion());
		}

		return false;
	}

}


package gov.nih.tbi.dictionary.model.hibernate;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.dictionary.model.rdf.SemanticDataElement;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.http.impl.cookie.DateParseException;

@XmlRootElement(name = "dataElement")
@XmlAccessorType(XmlAccessType.FIELD)
public class DataElement implements Serializable {

	private static final long serialVersionUID = 3333413702783642452L;

	public static final String DOCUMENTATION_URL = "documentationUrl";
	public static final String DOCUMENTATION_FILE_ID = "documentationFileId";
	public static final String ID = "id";

	/**** PROPERTIES *********************************************************/
	@XmlElement(name = "StructuralDataElement")
	private StructuralDataElement structuralObject;
	@XmlElement(name = "SemanticDataElement")
	private SemanticDataElement semanticObject;

	/**** CONSTRUCTORS *******************************************************/

	/**
	 * Default constructor initializes the structural and semantic objects. These objects should never be null.
	 */
	public DataElement() {

		structuralObject = new StructuralDataElement();
		semanticObject = new SemanticDataElement();
	}

	public DataElement(DataElement dataElement) {

		structuralObject = new StructuralDataElement(dataElement.getStructuralObject());
		semanticObject = new SemanticDataElement(dataElement.getSemanticObject());
	}

	/**
	 * 
	 * @param structuralObject
	 * @param semanticObject
	 */
	public DataElement(StructuralDataElement structuralObject, SemanticDataElement semanticObject) {

		this.structuralObject = structuralObject;
		this.semanticObject = semanticObject;
	}

	public DataElement(SemanticDataElement semanticObject, StructuralDataElement structuralObject) {

		this.structuralObject = structuralObject;
		this.semanticObject = semanticObject;
	}

	/**** GETTERS AND SETTERS ************************************************/

	public SemanticDataElement getSemanticObject() {

		return semanticObject;
	}

	public StructuralDataElement getStructuralObject() {

		return structuralObject;
	}

	public Category getCategory() {

		return semanticObject.getCategory();
	}

	public void setCategory(Category category) {

		// TODO: MV 6/10/2014 - Category should only be stored in the semantic object.
		structuralObject.setCategory(category);
		semanticObject.setCategory(category);
	}

	public Set<ExternalId> getExternalIdSet() {

		return semanticObject.getExternalIdSet();
	}

	public void setExternalIdSet(Set<ExternalId> externalIdSet) {

		semanticObject.setExternalIdSet(externalIdSet);
	}

	public MeasuringUnit getMeasuringUnit() {

		return structuralObject.getMeasuringUnit();
	}

	public void setMeasuringUnit(MeasuringUnit measuringUnit) {

		structuralObject.setMeasuringUnit(measuringUnit);
	}

	public InputRestrictions getRestrictions() {

		return structuralObject.getRestrictions();
	}

	public void setRestrictions(InputRestrictions restrictions) {

		structuralObject.setRestrictions(restrictions);
	}

	public void setDescription(String description) {

		semanticObject.setDescription(description);
	}

	public String getDescription() {

		return semanticObject.getDescription();
	}

	public void setShortDescription(String shortDescription) {

		semanticObject.setShortDescription(shortDescription);
	}

	public String getShortDescription() {

		return semanticObject.getShortDescription();
	}

	public void setNotes(String notes) {

		semanticObject.setNotes(notes);
	}

	public String getNotes() {

		return semanticObject.getNotes();
	}

	public void setValueRangeList(Set<ValueRange> valueRangeList) {

		semanticObject.setValueRangeList(valueRangeList);
		structuralObject.setValueRangeList(valueRangeList);
	}

	public void addValueRange(ValueRange valueRange) {

		semanticObject.addValueRange(valueRange);
		structuralObject.addValueRange(valueRange);
	}
	
	public void removeValueRange(ValueRange valueRange) {
		semanticObject.removeValueRange(valueRange);
		structuralObject.removeValueRange(valueRange);
	}

	public Set<ValueRange> getValueRangeList() {

		return structuralObject.getValueRangeList();
	}
	
	public Set<ValueRange> getSemanticValueRangeList() {
		
		return semanticObject.getValueRangeList();
	}

	public List<ValueRange> getSortedValueRangeList() {
		Set<ValueRange> valueRangeSet = this.getValueRangeList();
		List<ValueRange> result = new ArrayList(valueRangeSet);
		Collections.sort(result);
		return result;
	}

	public boolean isCommonDataElement() {

		return structuralObject.isCommonDataElement();
	}

	public void setKeywords(Set<Keyword> keywords) {

		semanticObject.setKeywords(keywords);
	}

	public void setLabels(Set<Keyword> labels) {

		semanticObject.setLabels(labels);
	}

	public String getGuidelines() {

		return semanticObject.getGuidelines();
	}

	public void setGuidelines(String guidelines) {

		semanticObject.setGuidelines(guidelines);
	}

	public String getHistoricalNotes() {

		return semanticObject.getHistoricalNotes();
	}

	public void setHistoricalNotes(String historicalNotes) {

		semanticObject.setHistoricalNotes(historicalNotes);
	}

	public String getSuggestedQuestion() {

		return structuralObject.getSuggestedQuestion();
	}

	public void setSuggestedQuestion(String suggestedQuestion) {

		structuralObject.setSuggestedQuestion(suggestedQuestion);
	}

	public String getReferences() {

		return semanticObject.getReferences();
	}

	public void setReferences(String references) {

		semanticObject.setReferences(references);
	}

	public String getTitle() {

		return semanticObject.getTitle();
	}

	public void setTitle(String title) {

		semanticObject.setTitle(title);
	}

	public Long getId() {

		return structuralObject.getId();
	}

	public void setId(Long id) {

		structuralObject.setId(id);
	}

	public void setSize(Integer size) {

		structuralObject.setSize(size);
	}

	public Integer getSize() {

		return structuralObject.getSize();
	}

	public BigDecimal getMaximumValue() {

		return structuralObject.getMaximumValue();
	}

	public void setMaximumValue(BigDecimal maximumValue) {

		structuralObject.setMaximumValue(maximumValue);
	}

	public BigDecimal getMinimumValue() {

		return structuralObject.getMinimumValue();
	}

	public void setMinimumValue(BigDecimal minimumValue) {

		structuralObject.setMinimumValue(minimumValue);
	}

	public DataType getType() {

		return structuralObject.getType();
	}

	public void setType(DataType type) {

		// Type should be stored in both objects (unless removed from search result table). Primary filed (for gets) is
		// structural.
		structuralObject.setType(type);
		semanticObject.setType(type);
	}

	public String getName() {

		return structuralObject.getName();
	}

	public void setName(String name) {

		structuralObject.setName(name);
		semanticObject.setName(name);
	}

	public Set<ClassificationElement> getClassificationElementList() {

		return semanticObject.getClassificationElementList();
	}

	public void addClassificationElement(ClassificationElement classificationElement) {

		semanticObject.addClassificationElement(classificationElement);
	}

	public void setClassificationElementList(Set<ClassificationElement> classificationElementList) {

		semanticObject.setClassificationElementList(classificationElementList);
	}

	public Population getPopulation() {

		return semanticObject.getPopulation();
	}

	public void setPopulation(Population population) {

		semanticObject.setPopulation(population);
	}

	public Set<SubDomainElement> getSubDomainElementList() {

		return semanticObject.getSubDomainElementList();
	}

	public void addSubDomainElement(SubDomainElement subDomainElement) {

		semanticObject.addSubDomainElement(subDomainElement);
	}

	public void setSubDomainElementList(Set<SubDomainElement> subDomainElementList) {

		semanticObject.setSubDomainElementList(subDomainElementList);
	}

	public Set<Keyword> getKeywords() {

		return semanticObject.getKeywords();
	}

	public Set<Keyword> getLabels() {

		return semanticObject.getLabels();
	}

	public Set<Alias> getAliasList() {

		return structuralObject.getAliasList();
	}

	public void setAliasList(Set<Alias> aliasList) {

		structuralObject.setAliasList(aliasList);
	}

	public String getUri() {

		return semanticObject.getUri();
	}

	public void setUri(String uri) {

		semanticObject.setUri(uri);
	}

	public DataElementStatus getStatus() {

		return structuralObject.getStatus();
	}

	public void setStatus(DataElementStatus status) {

		// Status needs to be semantic (for searches) and structural (for validation and ws).
		structuralObject.setStatus(status);
		semanticObject.setStatus(status);
	}

	public Date getDateCreated() {

		// TODO: MV 6/6/2014 - dateCreated should only be stored in the semantic object.
		return structuralObject.getDateCreated();
	}

	public String getDateCreatedString() {

		return structuralObject.getDateCreatedString();
	}

	public void setDateCreated(Date dateCreated) {

		structuralObject.setDateCreated(dateCreated);
		semanticObject.setDateCreated(dateCreated);
	}

	public String getCreatedBy() {

		return semanticObject.getCreatedBy();
	}

	public void setCreatedBy(String createdBy) {

		semanticObject.setCreatedBy(createdBy);
	}

	public String getFormat() {

		return semanticObject.getFormat();
	}

	public void setFormat(String format) {

		semanticObject.setFormat(format);
	}
	
	 public String getCatOid() {
		return structuralObject.getCatOid();
	}

	public void setCatOid(String catOid) {
		structuralObject.setCatOid(catOid);
	}

	public String getFormItemId() {
		return structuralObject.getFormItemId();
	}

	public void setFormItemId(String formItemId) {
		structuralObject.setFormItemId(formItemId);
	}


	/**** FUNCTIONS **********************************************************/

	@Override
	public String toString() {

		return "DataElement [id=" + getId() + ", name=" + getName() + ", version=" + getVersion() + ", size="
				+ getSize() + "]";
	}

	public String displayValueRange() {

		return structuralObject.displayValueRange();
	}

	public void updateExternalId(Schema schema, String value) {

		semanticObject.updateExternalId(schema, value);
	}

	public ExternalId getExternalId(Schema schema) {

		return semanticObject.getExternalId(schema);
	}

	public String getSubmittingOrgName() {

		return semanticObject.getSubmittingOrgName();
	}

	public void setSubmittingOrgName(String submittingOrgName) {

		semanticObject.setSubmittingOrgName(submittingOrgName);
	}

	public String getSubmittingContactName() {

		return semanticObject.getSubmittingContactName();
	}

	public void setSubmittingContactName(String submittingContactName) {

		semanticObject.setSubmittingContactName(submittingContactName);
	}

	public String getSubmittingContactInfo() {

		return semanticObject.getSubmittingContactInfo();
	}

	public void setSubmittingContactInfo(String submittingContactInfo) {

		semanticObject.setSubmittingContactInfo(submittingContactInfo);
	}

	public String getStewardOrgName() {

		return semanticObject.getStewardOrgName();
	}

	public void setStewardOrgName(String stewardOrgName) {

		semanticObject.setStewardOrgName(stewardOrgName);
	}

	public String getStewardContactName() {

		return semanticObject.getStewardContactName();
	}

	public void setStewardContactName(String stewardContactName) {

		semanticObject.setStewardContactName(stewardContactName);
	}

	public String getStewardContactInfo() {

		return semanticObject.getStewardContactInfo();
	}

	public void setStewardContactInfo(String stewardContactInfo) {

		semanticObject.setStewardContactInfo(stewardContactInfo);
	}

	public Date getEffectiveDate() {

		return semanticObject.getEffectiveDate();
	}

	public void setEffectiveDate(Date effectiveDate) {

		semanticObject.setEffectiveDate(effectiveDate);
	}

	public String getEffectiveDateString() {

		String effectiveDate = ModelConstants.EMPTY_STRING;
		if (this.getEffectiveDate() != null) {
			DateFormat newDate = new SimpleDateFormat("yyyy-MM-dd");
			effectiveDate = newDate.format(this.getEffectiveDate());
		}
		return effectiveDate;
	}

	public String getDateCreatedWithoutTime() {

		String dateStr = ModelConstants.EMPTY_STRING;
		if (this.getDateCreated() != null) {
			DateFormat newDate = new SimpleDateFormat("yyyy-MM-dd");
			dateStr = newDate.format(this.getDateCreated());
		}
		return dateStr;
	}

	public void setEffectiveDateString(String effectiveDateString) throws ParseException {

		Date effectiveDate = null;
		if (effectiveDateString != null && !effectiveDateString.equals("")) {
			DateFormat newDate = new SimpleDateFormat("yyyy-MM-dd");
			effectiveDate = newDate.parse(effectiveDateString);
		}
		this.setEffectiveDate(effectiveDate);
	}

	public Date getUntilDate() {

		return semanticObject.getUntilDate();
	}

	public void setUntilDate(Date untilDate) {

		semanticObject.setUntilDate(untilDate);
	}

	public String getUntilDateString() {

		String untilDate = ModelConstants.EMPTY_STRING;
		if (this.getUntilDate() != null) {
			DateFormat newDate = new SimpleDateFormat("yyyy-MM-dd");
			untilDate = newDate.format(this.getUntilDate());
		}
		return untilDate;
	}

	public void setUntilDateString(String untilDateString) throws ParseException {

		Date untilDate = null;
		if (untilDateString != null && !untilDateString.equals("")) {
			DateFormat newDate = new SimpleDateFormat("yyyy-MM-dd");
			untilDate = newDate.parse(untilDateString);
		}

		this.setUntilDate(untilDate);
	}

	public Date getModifiedDate() {

		return semanticObject.getModifiedDate();
	}

	public String getModifiedDateString() throws DateParseException {

		Date date = semanticObject.getModifiedDate();
		return date != null ? BRICSTimeDateUtil.formatDate(date) : ModelConstants.EMPTY_STRING;
	}

	public void setModifiedDate(Date modifiedDate) {

		semanticObject.setModifiedDate(modifiedDate);
	}

	public void setVersion(String version) {

		semanticObject.setVersion(version);
		structuralObject.setVersion(version);
	}

	public String getVersion() {

		return semanticObject.getVersion();
	}

	public String getSeeAlso() {

		return semanticObject.getSeeAlso();
	}

	public void setSeeAlso(String seeAlso) {

		semanticObject.setSeeAlso(seeAlso);
	}

	public String getNameAndVersion() {

		return getName() + "V" + getVersion();
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
}

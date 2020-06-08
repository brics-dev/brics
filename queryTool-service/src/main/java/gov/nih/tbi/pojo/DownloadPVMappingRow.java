package gov.nih.tbi.pojo;

import java.io.Serializable;
import java.util.Set;

import gov.nih.tbi.dictionary.model.hibernate.Keyword;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;

/**
 * Model object that represents a single row in QT downloading mapping file.
 * 
 * @author jim3
 */
public class DownloadPVMappingRow implements Serializable {

	private static final long serialVersionUID = 3698273104391308737L;

	private String deName;
	private String deTitle;
	private String elementType;
	private String version;
	private String definition;
	private String shortDescription;
	private String dataType;
	private String maxCharQuantity;
	private String inputRestriction;
	private String minVal;
	private String maxVal;
	private String pvValue;
	private String pvCode;
	private String pvDesciption;
	private String itemResponseOID;
	private String elementOID;
	private String unitOfMeasurement;
	private String guidelines;
	private String notes;
	private String preferredQuestionText;
	private String keywords;
	private String references;
	private String populationAll;
	private String historicalNotes;
	private String labels;
	private String seeAlso;
	private String submittingOrgName;
	private String submittingContactName;
	private String submittingContactInformation;
	private String effectiveDate;
	private String untilDate;
	private String stewardOrgName;
	private String stewardContactName;
	private String stewardContactInfo;
	
	
	private String deDescription;
	private String schemaDeId;
	private String schemaValue;
	
	public DownloadPVMappingRow(gov.nih.tbi.dictionary.model.hibernate.DataElement de) {
		this.deName = de.getName();
		this.deTitle = de.getTitle();
		this.elementType = de.getType().getValue();
		this.version = de.getVersion();
		this.definition = de.getDescription();
		this.shortDescription = de.getShortDescription();
		this.dataType = de.getType().getValue();
		this.maxCharQuantity = de.getType().getSqlFormatString();
		this.inputRestriction = de.getType().getValue();
		if(de.getMinimumValue() != null) {
			this.minVal = de.getMinimumValue().toString();
		}
		if(de.getMaximumValue() != null) {
			this.maxVal = de.getMaximumValue().toString();
		}
		
		Set<ValueRange> vrList = de.getValueRangeList();
		String pv, pvd, pvc, pvItemResponseOID, pvElementOID;
		pv = pvd = pvc =  pvItemResponseOID = pvElementOID = "";
		
		
		for(ValueRange vr: vrList) {
			pv = pv.concat(vr.getValueRange() + ";");
			pvd = pvd.concat(vr.getDescription() + ";");
			pvc = pvc.concat(vr.getOutputCode() + ";");
			if(vr.getItemResponseOid() != null) {
				pvItemResponseOID = pvItemResponseOID.concat(vr.getItemResponseOid() + ";");
			}
			if(vr.getElementOid() != null) {
				pvElementOID = pvElementOID.concat(vr.getElementOid()+ ";");
			}
			
		}
		
		this.pvValue = pv;
		this.pvDesciption = pvd;
		this.pvCode = pvc;
		this.itemResponseOID = pvItemResponseOID;
		this.elementOID = pvElementOID;
		
		if(de.getMeasuringUnit() != null) {
			this.unitOfMeasurement = de.getMeasuringUnit().getDisplayName();
		}
		this.guidelines = de.getGuidelines();
		this.notes = de.getNotes();
		this.preferredQuestionText = de.getSuggestedQuestion();
		
		Set<Keyword> keywordSet = de.getKeywords();
		
		String keywordString = "";
		for(Keyword key: keywordSet) {
			keywordString = keywordString.concat(key.getKeyword() + ";");
		}
		
		this.keywords = keywordString;
		this.references = de.getReferences();
		this.populationAll = de.getPopulation().getName();
		this.submittingOrgName = de.getSubmittingOrgName();
		this.submittingContactName = de.getSubmittingContactName();
		this.submittingContactInformation = de.getSubmittingContactInfo();
		this.effectiveDate = de.getEffectiveDateString();
		this.untilDate = de.getUntilDateString();
		this.stewardOrgName = de.getStewardOrgName();
		this.stewardContactName = de.getStewardContactName();
		this.stewardContactInfo = de.getStewardContactInfo();
		
		this.historicalNotes = de.getHistoricalNotes();
		this.seeAlso = de.getSeeAlso();

		Set<Keyword> labelsSet = de.getLabels();
		String labelString = "";
	
		for(Keyword label: labelsSet) {
			labelString = labelString.concat(label.getKeyword() + ";");
		}
		
		this.labels = labelString;
		
		
	}
	
	public String getDeName() {
		return deName;
	}
	public void setDeName(String deName) {
		this.deName = deName;
	}
	public String getDeTitle() {
		return deTitle;
	}
	public void setDeTitle(String deTitle) {
		this.deTitle = deTitle;
	}
	public String getElementType() {
		return elementType;
	}
	public void setElementType(String elementType) {
		this.elementType = elementType;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	public String getDefinition() {
		return definition;
	}
	public void setDefinition(String definition) {
		this.definition = definition;
	}
	public String getShortDescription() {
		return shortDescription;
	}
	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public String getMaxCharQuantity() {
		return maxCharQuantity;
	}
	public void setMaxCharQuantity(String maxCharQuantity) {
		this.maxCharQuantity = maxCharQuantity;
	}
	public String getInputRestriction() {
		return inputRestriction;
	}
	public void setInputRestriction(String inputRestriction) {
		this.inputRestriction = inputRestriction;
	}
	public String getMinVal() {
		return minVal;
	}
	public void setMinVal(String minVal) {
		this.minVal = minVal;
	}
	public String getMaxVal() {
		return maxVal;
	}
	public void setMaxVal(String maxVal) {
		this.maxVal = maxVal;
	}
	public String getPvValue() {
		return pvValue;
	}
	public void setPvValue(String pvValue) {
		this.pvValue = pvValue;
	}
	public String getPvCode() {
		return pvCode;
	}
	public void setPvCode(String pvCode) {
		this.pvCode = pvCode;
	}
	public String getPvDesciption() {
		return pvDesciption;
	}
	public void setPvDesciption(String pvDesciption) {
		this.pvDesciption = pvDesciption;
	}
	public String getItemResponseOID() {
		return itemResponseOID;
	}
	public void setItemResponseOID(String itemResponseOID) {
		this.itemResponseOID = itemResponseOID;
	}
	public String getElementOID() {
		return elementOID;
	}
	public void setElementOID(String elementOID) {
		this.elementOID = elementOID;
	}
	public String getUnitOfMeasurement() {
		return unitOfMeasurement;
	}
	public void setUnitOfMeasurement(String unitOfMeasurement) {
		this.unitOfMeasurement = unitOfMeasurement;
	}
	public String getGuidelines() {
		return guidelines;
	}
	public void setGuidelines(String guidelines) {
		this.guidelines = guidelines;
	}
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}
	public String getPreferredQuestionText() {
		return preferredQuestionText;
	}
	public void setPreferredQuestionText(String preferredQuestionText) {
		this.preferredQuestionText = preferredQuestionText;
	}
	public String getKeywords() {
		return keywords;
	}
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	public String getReferences() {
		return references;
	}
	public void setReferences(String references) {
		this.references = references;
	}
	public String getPopulationAll() {
		return populationAll;
	}
	public void setPopulationAll(String populationAll) {
		this.populationAll = populationAll;
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
	public String getSubmittingContactInformation() {
		return submittingContactInformation;
	}
	public void setSubmittingContactInformation(String submittingContactInformation) {
		this.submittingContactInformation = submittingContactInformation;
	}
	public String getEffectiveDate() {
		return effectiveDate;
	}
	public void setEffectiveDate(String effectiveDate) {
		this.effectiveDate = effectiveDate;
	}
	public String getUntilDate() {
		return untilDate;
	}
	public void setUntilDate(String untilDate) {
		this.untilDate = untilDate;
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
	public String getDeDescription() {
		return deDescription;
	}
	public void setDeDescription(String deDescription) {
		this.deDescription = deDescription;
	}
	public String getSchemaDeId() {
		return schemaDeId;
	}
	public void setSchemaDeId(String schemaDeId) {
		this.schemaDeId = schemaDeId;
	}
	public String getSchemaValue() {
		return schemaValue;
	}
	public void setSchemaValue(String schemaValue) {
		this.schemaValue = schemaValue;
	}
	public String getHistoricalNotes() {
		return historicalNotes;
	}

	public void setHistoricalNotes(String historicalNotes) {
		this.historicalNotes = historicalNotes;
	}

	public String getLabels() {
		return labels;
	}

	public void setLabels(String labels) {
		this.labels = labels;
	}

	public String getSeeAlso() {
		return seeAlso;
	}

	public void setSeeAlso(String seeAlso) {
		this.seeAlso = seeAlso;
	}
}

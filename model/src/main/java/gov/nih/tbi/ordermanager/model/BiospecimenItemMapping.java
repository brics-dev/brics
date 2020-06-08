package gov.nih.tbi.ordermanager.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;


@Entity
@Table(name = "Biospecimen_Item_Mapping")
@XmlRootElement(name = "BiospecimenItemMapping")
public class BiospecimenItemMapping implements Serializable {

	private static final long serialVersionUID = -635971766401967479L;

	@Id
	private Long id;

	@Column(name = "FORM_NAME")
	private String formName;

	@Column(name = "BIOSAMPLE_ID")
	private String biosampleId;

	@Column(name = "REPOSITORY_NAME")
	private String repositoryName;

	@Column(name = "SAMPLE_REF_ID")
	private String sampleRefId;
	@Column(name = "Original_Container_Type_Received")
	private String originalContainerTypeReceived;
	@Column(name = "Visit_Description")
	private String visitDescription;
	@Column(name = "GUID")
	private String guid;
	@Column(name = "VISIT_TYPE_PDBP")
	private String visitTypePDBP;
	@Column(name = "AGE_YRS")
	private String ageYrs;
	@Column(name = "AGE_VAL")
	private String ageVal;
	@Column(name = "BIOSAMPLE_DATA_ORIGINATOR")
	private String biosampleDataOriginator;
	@Column(name = "BIO_REPOS_TUBE_ID")
	private String bioreposTubeID;
	@Column(name = "SAMP_COLL_TYPE")
	private String sampCollType;
	@Column(name = "SAMPLE_ALIQUOT_MASS")
	private String sampleAliquotMass;
	@Column(name = "SAMPLE_ALIQUOT_MASS_UNITS")
	private String sampleAliquotMassUnits;
	@Column(name = "SAMPLE_ALIQUOT_VOL")
	private String sampleAliquotVol;
	@Column(name = "SAMPLE_ALIQUOT_VOL_UNITS")
	private String sampleAliquotVolUnits;
	@Column(name = "SAMPLE_AVG_HEMOGLOBIN_VAL")
	private String sampleAvgHemoglobinVal;
	@Column(name = "CONCENTRATION_UOM")
	private String concentrationUoM;
	@Column(name = "REPOSITORY_BIOSAMPLE")
	private String repositoryBiosample;
	@Column(name = "INVENTORY")
	private String inventory;
	@Column(name = "INVENTORY_DATE")
	private String inventoryDate;
	@Column(name = "UNIT_NUMBER")
	private String unitNumber;
	@Column(name = "UNIT_MEASUREMENT")
	private String unitMeasurement;
	@Column(name = "NEURO_DIAGNOSIS")
	private String neuroDiagnosis;
	@Column(name = "CASE_CONTROL")
	private String caseControl;
	@Column(name = "PDBP_STUDY_ID")
	private String pdbpStudyId;

	public Long getId() {

		return id;
	}

	public void setId(Long id) {

		this.id = id;
	}

	public String getFormName() {

		return formName;
	}

	public void setFormName(String formName) {

		this.formName = formName;
	}

	public String getBiosampleId() {

		return biosampleId;
	}

	public void setBiosampleId(String biosampleId) {

		this.biosampleId = biosampleId;
	}

	public String getRepositoryName() {

		return repositoryName;
	}

	public void setRepositoryName(String repositoryName) {

		this.repositoryName = repositoryName;
	}

	public String getSampleRefId() {

		return sampleRefId;
	}

	public void setSampleRefId(String sampleRefId) {

		this.sampleRefId = sampleRefId;
	}

	public String getOriginalContainerTypeReceived() {

		return originalContainerTypeReceived;
	}

	public void setOriginalContainerTypeReceived(String originalContainerTypeReceived) {

		this.originalContainerTypeReceived = originalContainerTypeReceived;
	}

	public String getVisitDescription() {

		return visitDescription;
	}

	public void setVisitDescription(String visitDescription) {

		this.visitDescription = visitDescription;
	}

	public String getGuid() {

		return guid;
	}

	public void setGuid(String guid) {

		this.guid = guid;
	}

	public String getVisitTypePDBP() {

		return visitTypePDBP;
	}

	public void setVisitTypePDBP(String visitTypePDBP) {

		this.visitTypePDBP = visitTypePDBP;
	}

	public String getAgeYrs() {

		return ageYrs;
	}

	public void setAgeYrs(String ageYrs) {

		this.ageYrs = ageYrs;
	}

	public String getAgeVal() {

		return ageVal;
	}

	public void setAgeVal(String ageVal) {

		this.ageVal = ageVal;
	}

	public String getBiosampleDataOriginator() {

		return biosampleDataOriginator;
	}

	public void setBiosampleDataOriginator(String biosampleDataOriginator) {

		this.biosampleDataOriginator = biosampleDataOriginator;
	}

	public String getBioreposTubeID() {

		return bioreposTubeID;
	}

	public void setBioreposTubeID(String bioreposTubeID) {

		this.bioreposTubeID = bioreposTubeID;
	}

	public String getSampCollType() {

		return sampCollType;
	}

	public void setSampCollType(String sampCollType) {

		this.sampCollType = sampCollType;
	}

	public String getSampleAliquotMass() {

		return sampleAliquotMass;
	}

	public void setSampleAliquotMass(String sampleAliquotMass) {

		this.sampleAliquotMass = sampleAliquotMass;
	}

	public String getSampleAliquotMassUnits() {

		return sampleAliquotMassUnits;
	}

	public void setSampleAliquotMassUnits(String sampleAliquotMassUnits) {

		this.sampleAliquotMassUnits = sampleAliquotMassUnits;
	}

	public String getSampleAliquotVol() {

		return sampleAliquotVol;
	}

	public void setSampleAliquotVol(String sampleAliquotVol) {

		this.sampleAliquotVol = sampleAliquotVol;
	}

	public String getSampleAliquotVolUnits() {

		return sampleAliquotVolUnits;
	}

	public void setSampleAliquotVolUnits(String sampleAliquotVolUnits) {

		this.sampleAliquotVolUnits = sampleAliquotVolUnits;
	}

	public String getSampleAvgHemoglobinVal() {

		return sampleAvgHemoglobinVal;
	}

	public void setSampleAvgHemoglobinVal(String sampleAvgHemoglobinVal) {

		this.sampleAvgHemoglobinVal = sampleAvgHemoglobinVal;
	}

	public String getConcentrationUoM() {

		return concentrationUoM;
	}

	public void setConcentrationUoM(String concentrationUoM) {

		this.concentrationUoM = concentrationUoM;
	}

	public String getRepositoryBiosample() {

		return repositoryBiosample;
	}

	public void setRepositoryBiosample(String repositoryBiosample) {

		this.repositoryBiosample = repositoryBiosample;
	}

	public String getInventory() {

		return inventory;
	}

	public void setInventory(String inventory) {

		this.inventory = inventory;
	}

	public String getInventoryDate() {

		return inventoryDate;
	}

	public void setInventoryDate(String inventoryDate) {

		this.inventoryDate = inventoryDate;
	}

	public String getUnitNumber() {

		return unitNumber;
	}

	public void setUnitNumber(String unitNumber) {

		this.unitNumber = unitNumber;
	}

	public String getUnitMeasurement() {

		return unitMeasurement;
	}

	public void setUnitMeasurement(String unitMeasurement) {

		this.unitMeasurement = unitMeasurement;
	}

	public String getNeuroDiagnosis() {
		return neuroDiagnosis;
	}

	public void setNeuroDiagnosis(String neuroDiagnosis) {
		this.neuroDiagnosis = neuroDiagnosis;
	}

	public String getCaseControl() {
		return caseControl;
	}

	public void setCaseControl(String caseControl) {
		this.caseControl = caseControl;
	}

	public String getPdbpStudyId() {
		return pdbpStudyId;
	}

	public void setPdbpStudyId(String pdbpStudyId) {
		this.pdbpStudyId = pdbpStudyId;
	}
}

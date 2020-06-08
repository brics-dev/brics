package gov.nih.tbi.repository.dataimport;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import gov.nih.tbi.ModelConstants;

@JsonPropertyOrder({"sampleId","repositoryName","sampleCollectionType","visitType","PDBPStudyId","GUID","sampleUnitNum","sampleUnitMeasurement","biorepositoryCount","additionalStock",
	"concentrationDnaRna","concentrationDnaRnaUnits","sample260_280_Ratio","sample260_230_Ratio","rnaIntegrityNum","rRatio","rnaQualityScale","sampleClottingIndicator",
	"sampleTurbidityScale","sampleAvgHemoglobinVal","sampleAvgHemoglobinUnits","sampleAvgHemoglobinResult","sampleAvgHemoglobinResultDesc","sampleHemolysisSacle",
	"inventoryBiorepositoryDate"})
public class BiosampleCSVData {
	
	@JsonProperty("SampleId") private String sampleId;
	
	@JsonProperty("RepositoryName") private String repositoryName;
	
	@JsonProperty("SampCollTyp") private String sampleCollectionType;
	
	@JsonProperty("VisitTypPDBP") private String visitType;
	
	@JsonIgnore private int daysSinceBaseline;
	
	@JsonProperty("PDBPStudyId") private String pdbpStudyId;
	
	@JsonProperty("GUID") private String GUID;
	
	@JsonProperty("SampleUnitNum") private int sampleUnitNum;
	
	@JsonProperty("SampleUnitMeasurement") private String sampleUnitMeasurement;
	
	@JsonProperty("Inventory_BiorepositoryCount") private int biorepositoryCount;
	
	@JsonProperty("ADDTL_STOCK") private String additionalStock;
	
	@JsonProperty("ConcentrationDNA_RNA") private float concentrationDnaRna;
	
	@JsonProperty("ConcentrationDNA_RNAUnits") private String concentrationDnaRnaUnits;

	@JsonProperty("Sample260_280Ratio") private float sample260_280_Ratio;
	
	@JsonProperty("Sample260_230Ratio") private float sample260_230_Ratio;
	
	@JsonProperty("RNAIntegrityNum") private float rnaIntegrityNum;
	
	@JsonProperty("rRatio") private float rRatio;
	
	@JsonProperty("RNAQualityScale") private String rnaQualityScale;
	
	@JsonProperty("SampleClottingInd") private String sampleClottingIndicator;
	
	@JsonProperty("SampleTurbidityScale") private String sampleTurbidityScale;
	
	@JsonProperty("SampleAvgHemoglobinVal") private float sampleAvgHemoglobinVal;
	
	@JsonProperty("SampleAvgHemoglobinUnits") private String sampleAvgHemoglobinUnits;
	
	@JsonProperty("SampleAvgHemoglobinResult") private String sampleAvgHemoglobinResult;
	
	@JsonProperty("SampleAvgHemoglobinResultDesc") private String sampleAvgHemoglobinResultDesc;
	
	@JsonProperty("SampleHemolysisScale") private int sampleHemolysisSacle;
	
	@JsonProperty("Inventory_BiorepositoryDate") private String inventoryBiorepositoryDate;
	
	
	private static final String DELIMITER = ",";
	private static final String STEADY_PD_III = "STEADYPDIII";
     
	/**
	 * Constructor for a Biosample CSV row. The constructor will take in a row from the CSV file
	 * and parse the data in to a BiosampleCSVData object.
	 * @param row - Row from Biosample CSV
	 * @param delimiter - Delimiter of the CSV file
	 */
	@JsonCreator
	public BiosampleCSVData (@JsonProperty("SampleId") String sampleId, @JsonProperty("RepositoryName") String repositoryName, @JsonProperty("SampCollTyp")  String sampleCollectionType, 
			@JsonProperty("VisitTypPDBP")  String visitType, @JsonProperty("PDBPStudyId") String pdbpStudyId, @JsonProperty("GUID")  String GUID, @JsonProperty("SampleUnitNum")  int sampleUnitNum, 
			@JsonProperty("SampleUnitMeasurement")  String sampleUnitMeasurement, @JsonProperty("Inventory_BiorepositoryCount")  int biorepositoryCount, 
			@JsonProperty("ADDTL_STOCK")  String additionalStock, @JsonProperty("ConcentrationDNA_RNA")  float conenctrationDnaRna, 
			@JsonProperty("ConcentrationDNA_RNAUnits")  String concentrationDnaRnaUnits, @JsonProperty("Sample260_280Ratio")  float sample260_280_Ratio, 
			@JsonProperty("Sample260_230Ratio")  float sample260_230_Ratio, @JsonProperty("RNAIntegrityNum")  float rnaIntegrityNum, 
			@JsonProperty("rRatio")  float rRatio, @JsonProperty("RNAQualityScale")  String rnaQualityScale, @JsonProperty("SampleClottingInd")  String sampleClottingIndicator, 
			@JsonProperty("SampleTurbidityScale")  String sampleTurbidityScale, @JsonProperty("SampleAvgHemoglobinVal")  float sampleAvgHemoglobinVal, 
			@JsonProperty("SampleAvgHemoglobinUnits")  String sampleAvgHemoglobinUnits, @JsonProperty("SampleAvgHemoglobinResult")  String sampleAvgHemoglobinResult, 
			@JsonProperty("SampleAvgHemoglobinResultDesc")  String sampleAvgHemoglobinResultDesc, @JsonProperty("SampleHemolysisScale")  int sampleHemolysisSacle, 
			@JsonProperty("Inventory_BiorepositoryDate")  String inventoryBiorepositoryDate) {
		this.sampleId = sampleId;
		this.repositoryName = repositoryName;
		this.sampleCollectionType = sampleCollectionType;
		if(repositoryName.equals(STEADY_PD_III)) {
			this.visitType = updateSteadyPDIIIVisitType(visitType);
		} else {
			this.visitType = visitType;
		}
		this.daysSinceBaseline = calculateDaysSinceBaseline(this.visitType);
		this.pdbpStudyId = pdbpStudyId;
		this.GUID = updateGuid(GUID);
		this.sampleUnitNum = sampleUnitNum;
		this.sampleUnitMeasurement = sampleUnitMeasurement;
		this.biorepositoryCount = biorepositoryCount;
		if(additionalStock.equals("Y")) {
			this.additionalStock = "Yes";
		} else if(additionalStock.equals("N")) {
			this.additionalStock = "No";
		}
		this.concentrationDnaRna = conenctrationDnaRna;
		this.concentrationDnaRnaUnits = concentrationDnaRnaUnits;
		this.sample260_280_Ratio = sample260_280_Ratio;
		this.sample260_230_Ratio = sample260_230_Ratio;
		this.rnaIntegrityNum = rnaIntegrityNum;
		this.rRatio = rRatio;
		this.rnaQualityScale = rnaQualityScale;
		this.sampleClottingIndicator = sampleClottingIndicator;
		this.sampleTurbidityScale = sampleTurbidityScale;
		this.sampleAvgHemoglobinVal = sampleAvgHemoglobinVal;
		this.sampleAvgHemoglobinUnits = sampleAvgHemoglobinUnits;
		this.sampleAvgHemoglobinResult = sampleAvgHemoglobinResult;
		this.sampleAvgHemoglobinResultDesc = sampleAvgHemoglobinResultDesc;
		this.sampleHemolysisSacle = sampleHemolysisSacle;
		this.inventoryBiorepositoryDate = inventoryBiorepositoryDate;
		
	}
	
	public BiosampleCSVData() {
		
	}

	public String getSampleId() {
		return sampleId;
	}

	public void setSampleId(String sampleId) {
		this.sampleId = sampleId;
	}

	public String getRepositoryName() {
		return repositoryName;
	}

	public void setRepositoryName(String repositoryName) {
		this.repositoryName = repositoryName;
	}

	public String getSampleCollectionType() {
		return sampleCollectionType;
	}

	public void setSampleCollectionType(String sampleCollectionType) {
		this.sampleCollectionType = sampleCollectionType;
	}

	public String getVisitType() {
		return visitType;
	}

	public void setVisitType(String visitType) {
		this.visitType = visitType;
	}

	public String getGUID() {
		return GUID;
	}

	public void setGUID(String gUID) {
		GUID = gUID;
	}

	public int getSampleUnitNum() {
		return sampleUnitNum;
	}

	public void setSampleUnitNum(int sampleUnitNum) {
		this.sampleUnitNum = sampleUnitNum;
	}

	public String getSampleUnitMeasurement() {
		return sampleUnitMeasurement;
	}

	public void setSampleUnitMeasurement(String sampleUnitMeasurement) {
		this.sampleUnitMeasurement = sampleUnitMeasurement;
	}

	public int getBiorepositoryCount() {
		return biorepositoryCount;
	}

	public void setBiorepositoryCount(int biorepositoryCount) {
		this.biorepositoryCount = biorepositoryCount;
	}

	public String getAdditionalStock() {
		return additionalStock;
	}

	public void setAdditionalStock(String additionalStock) {
		this.additionalStock = additionalStock;
	}

	public float getConenctrationDnaRna() {
		return concentrationDnaRna;
	}

	public void setConenctrationDnaRna(float conenctrationDnaRna) {
		this.concentrationDnaRna = conenctrationDnaRna;
	}

	public String getConcentrationDnaRnaUnits() {
		return concentrationDnaRnaUnits;
	}

	public void setConcentrationDnaRnaUnits(String concentrationDnaRnaUnits) {
		this.concentrationDnaRnaUnits = concentrationDnaRnaUnits;
	}

	public float getSample260_280_Ratio() {
		return sample260_280_Ratio;
	}

	public void setSample260_280_Ratio(float sample260_280_Ratio) {
		this.sample260_280_Ratio = sample260_280_Ratio;
	}

	public float getRnaIntegrityNum() {
		return rnaIntegrityNum;
	}

	public void setRnaIntegrityNum(float rnaIntegrityNum) {
		this.rnaIntegrityNum = rnaIntegrityNum;
	}

	public float getrRatio() {
		return rRatio;
	}

	public void setrRatio(float rRatio) {
		this.rRatio = rRatio;
	}

	public String getRnaQualityScale() {
		return rnaQualityScale;
	}

	public void setRnaQualityScale(String rnaQualityScale) {
		this.rnaQualityScale = rnaQualityScale;
	}

	public String getSampleClottingIndicator() {
		return sampleClottingIndicator;
	}

	public void setSampleClottingIndicator(String sampleClottingIndicator) {
		this.sampleClottingIndicator = sampleClottingIndicator;
	}

	public String getSampleTurbidityScale() {
		return sampleTurbidityScale;
	}

	public void setSampleTurbidityScale(String sampleTurbidityScale) {
		this.sampleTurbidityScale = sampleTurbidityScale;
	}

	public String getSampleAvgHemoglobinUnits() {
		return sampleAvgHemoglobinUnits;
	}

	public void setSampleAvgHemoglobinUnits(String sampleAvgHemoglobinUnits) {
		this.sampleAvgHemoglobinUnits = sampleAvgHemoglobinUnits;
	}

	public String getSampleAvgHemoglobinResult() {
		return sampleAvgHemoglobinResult;
	}

	public void setSampleAvgHemoglobinResult(String sampleAvgHemoglobinResult) {
		this.sampleAvgHemoglobinResult = sampleAvgHemoglobinResult;
	}

	public String getSampleAvgHemoglobinResultDesc() {
		return sampleAvgHemoglobinResultDesc;
	}

	public void setSampleAvgHemoglobinResultDesc(String sampleAvgHemoglobinResultDesc) {
		this.sampleAvgHemoglobinResultDesc = sampleAvgHemoglobinResultDesc;
	}

	public int getSampleHemolysisSacle() {
		return sampleHemolysisSacle;
	}

	public void setSampleHemolysisSacle(int sampleHemolysisSacle) {
		this.sampleHemolysisSacle = sampleHemolysisSacle;
	}

	public String getInventoryBiorepositoryDate() {
		return inventoryBiorepositoryDate;
	}

	public void setInventoryBiorepositoryDate(String inventoryBiorepositoryDate) {
		this.inventoryBiorepositoryDate = inventoryBiorepositoryDate;
	}
	
	public int getDaysSinceBaseline() {
		return daysSinceBaseline;
	}

	public void setDaysSinceBaseline(int daysSinceBaseline) {
		this.daysSinceBaseline = daysSinceBaseline;
	}

	public String getPdbpStudyId() {
		return pdbpStudyId;
	}

	public void setPdbpStudyId(String pdbpStudyId) {
		this.pdbpStudyId = pdbpStudyId;
	}

	public float getConcentrationDnaRna() {
		return concentrationDnaRna;
	}

	public void setConcentrationDnaRna(float concentrationDnaRna) {
		this.concentrationDnaRna = concentrationDnaRna;
	}

	public float getSample260_230_Ratio() {
		return sample260_230_Ratio;
	}

	public void setSample260_230_Ratio(float sample260_230_Ratio) {
		this.sample260_230_Ratio = sample260_230_Ratio;
	}

	public float getSampleAvgHemoglobinVal() {
		return sampleAvgHemoglobinVal;
	}

	public void setSampleAvgHemoglobinVal(float sampleAvgHemoglobinVal) {
		this.sampleAvgHemoglobinVal = sampleAvgHemoglobinVal;
	}

	protected int calculateDaysSinceBaseline(String visitType) {
		
		switch(visitType) {
		case ModelConstants.VISIT_TYPE_BASELINE:
			return 0;
		case ModelConstants.VISIT_TYPE_SIX_MONTHS:
			return 180;
		case ModelConstants.VISIT_TYPE_TWELVE_MONTHS:
			return 365;
		case ModelConstants.VISIT_TYPE_EIGHTEEN_MONTHS:
			return 545;
		case ModelConstants.VISIT_TYPE_TWENTY_FOUR_MONTHS:
			return 730;
		case ModelConstants.VISIT_TYPE_THIRTY_MONTHS:
			return 910;
		case ModelConstants.VISIT_TYPE_THIRTY_SIX_MONTHS:
			return 1095;
		case ModelConstants.VISIT_TYPE_FOURTY_TWO_MONTHS:
			return 1275;
		case ModelConstants.VISIT_TYPE_FOURTY_EIGHT_MONTHS:
			return 1460;
		case ModelConstants.VISIT_TYPE_FIFTY_FOUR_MONTHS:
			return 1640;
		case ModelConstants.VISIT_TYPE_SIXTY_MONTHS:
			return 1820;
		case ModelConstants.VISIT_TYPE_SEVENTY_TWO_MONTHS:
			return 2190;
		
		}
		return -1;
	}
	
	protected String updateGuid(String guid) {
		if(guid.contains("CSF-POOL-") || guid.contains("PLASMA-POOL-") || guid.contains("SERUM-POOL-")) {
			return "";
		}
		return guid;
	}
	
	protected String updateSteadyPDIIIVisitType(String visitType) {
		switch(visitType) {
		case ModelConstants.VISIT_TYPE_SCREENING:
			return ModelConstants.VISIT_TYPE_BASELINE;
		case ModelConstants.VISIT_TYPE_RSONE:
			return ModelConstants.VISIT_TYPE_BASELINE;
		case ModelConstants.VISIT_TYPE_RSTWO:
			return ModelConstants.VISIT_TYPE_BASELINE;
		case ModelConstants.VISIT_TYPE_VTEN:
			return ModelConstants.VISIT_TYPE_THIRTY_SIX_MONTHS;
		}
		
		return "";
	}
	
	 public String printCSVRecord() {
         return "x" + DELIMITER + this.repositoryName + DELIMITER + this.pdbpStudyId + DELIMITER + this.GUID + DELIMITER + this.visitType + DELIMITER + this.daysSinceBaseline + DELIMITER + this.sampleId 
        		 + DELIMITER + this.sampleCollectionType + DELIMITER + this.sampleUnitNum + DELIMITER + this.sampleUnitMeasurement + DELIMITER + this.sampleAvgHemoglobinVal 
        		 + DELIMITER + this.sampleAvgHemoglobinUnits + DELIMITER + this.sampleAvgHemoglobinResult + DELIMITER + this.sampleAvgHemoglobinResultDesc + DELIMITER + 
        		 this.sampleClottingIndicator + DELIMITER +  this.sampleHemolysisSacle + DELIMITER + this.sampleTurbidityScale + DELIMITER + this.concentrationDnaRna
        		 + DELIMITER + this.concentrationDnaRnaUnits + DELIMITER + this.sample260_280_Ratio + DELIMITER + this.sample260_230_Ratio + DELIMITER + this.rnaQualityScale
        		 + DELIMITER + this.rnaIntegrityNum + DELIMITER + this.rRatio + DELIMITER + this.biorepositoryCount + DELIMITER + this.additionalStock + DELIMITER + this.inventoryBiorepositoryDate +"\n";
        		 }

	@Override
	public String toString() {
		return "BiosampleCSVData [sampleId=" + sampleId + ", repositoryName=" + repositoryName
				+ ", sampleCollectionType=" + sampleCollectionType + ", visitType=" + visitType + ", GUID=" + GUID
				+ ", sampleUnitNum=" + sampleUnitNum + ", sampleUnitMeasurement=" + sampleUnitMeasurement
				+ ", biorepositoryCount=" + biorepositoryCount + ", additionalStock=" + additionalStock
				+ ", conenctrationDnaRna=" + concentrationDnaRna + ", concentrationDnaRnaUnits="
				+ concentrationDnaRnaUnits + ", sample260_280_Ratio=" + sample260_280_Ratio + ", rnaIntegrityNum="
				+ rnaIntegrityNum + ", rRatio=" + rRatio + ", rnaQualityScale=" + rnaQualityScale
				+ ", sampleClottingIndicator=" + sampleClottingIndicator + ", sampleTurbidityScale="
				+ sampleTurbidityScale + ", sampleAvgHemoglobinUnits=" + sampleAvgHemoglobinUnits
				+ ", sampleAvgHemoglobinResult=" + sampleAvgHemoglobinResult + ", sampleAvgHemoglobinResultDesc="
				+ sampleAvgHemoglobinResultDesc + ", sampleHemolysisSacle=" + sampleHemolysisSacle
				+ ", inventoryBiorepositoryDate=" + inventoryBiorepositoryDate + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((GUID == null) ? 0 : GUID.hashCode());
		result = prime * result + ((additionalStock == null) ? 0 : additionalStock.hashCode());
		result = prime * result + biorepositoryCount;
		result = prime * result + Float.floatToIntBits(concentrationDnaRna);
		result = prime * result + ((concentrationDnaRnaUnits == null) ? 0 : concentrationDnaRnaUnits.hashCode());
		result = prime * result + daysSinceBaseline;
		result = prime * result + ((inventoryBiorepositoryDate == null) ? 0 : inventoryBiorepositoryDate.hashCode());
		result = prime * result + Float.floatToIntBits(rRatio);
		result = prime * result + ((repositoryName == null) ? 0 : repositoryName.hashCode());
		result = prime * result + Float.floatToIntBits(rnaIntegrityNum);
		result = prime * result + ((rnaQualityScale == null) ? 0 : rnaQualityScale.hashCode());
		result = prime * result + Float.floatToIntBits(sample260_230_Ratio);
		result = prime * result + Float.floatToIntBits(sample260_280_Ratio);
		result = prime * result + ((sampleAvgHemoglobinResult == null) ? 0 : sampleAvgHemoglobinResult.hashCode());
		result = prime * result
				+ ((sampleAvgHemoglobinResultDesc == null) ? 0 : sampleAvgHemoglobinResultDesc.hashCode());
		result = prime * result + ((sampleAvgHemoglobinUnits == null) ? 0 : sampleAvgHemoglobinUnits.hashCode());
		result = prime * result + Float.floatToIntBits(sampleAvgHemoglobinVal);
		result = prime * result + ((sampleClottingIndicator == null) ? 0 : sampleClottingIndicator.hashCode());
		result = prime * result + ((sampleCollectionType == null) ? 0 : sampleCollectionType.hashCode());
		result = prime * result + sampleHemolysisSacle;
		result = prime * result + ((sampleId == null) ? 0 : sampleId.hashCode());
		result = prime * result + ((sampleTurbidityScale == null) ? 0 : sampleTurbidityScale.hashCode());
		result = prime * result + ((sampleUnitMeasurement == null) ? 0 : sampleUnitMeasurement.hashCode());
		result = prime * result + sampleUnitNum;
		result = prime * result + ((visitType == null) ? 0 : visitType.hashCode());
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
		BiosampleCSVData other = (BiosampleCSVData) obj;
		if (GUID == null) {
			if (other.GUID != null)
				return false;
		} else if (!GUID.equals(other.GUID))
			return false;
		if (additionalStock == null) {
			if (other.additionalStock != null)
				return false;
		} else if (!additionalStock.equals(other.additionalStock))
			return false;
		if (biorepositoryCount != other.biorepositoryCount)
			return false;
		if (Float.floatToIntBits(concentrationDnaRna) != Float.floatToIntBits(other.concentrationDnaRna))
			return false;
		if (concentrationDnaRnaUnits == null) {
			if (other.concentrationDnaRnaUnits != null)
				return false;
		} else if (!concentrationDnaRnaUnits.equals(other.concentrationDnaRnaUnits))
			return false;
		if (daysSinceBaseline != other.daysSinceBaseline)
			return false;
		if (inventoryBiorepositoryDate == null) {
			if (other.inventoryBiorepositoryDate != null)
				return false;
		} else if (!inventoryBiorepositoryDate.equals(other.inventoryBiorepositoryDate))
			return false;
		if (Float.floatToIntBits(rRatio) != Float.floatToIntBits(other.rRatio))
			return false;
		if (repositoryName == null) {
			if (other.repositoryName != null)
				return false;
		} else if (!repositoryName.equals(other.repositoryName))
			return false;
		if (Float.floatToIntBits(rnaIntegrityNum) != Float.floatToIntBits(other.rnaIntegrityNum))
			return false;
		if (rnaQualityScale == null) {
			if (other.rnaQualityScale != null)
				return false;
		} else if (!rnaQualityScale.equals(other.rnaQualityScale))
			return false;
		if (Float.floatToIntBits(sample260_230_Ratio) != Float.floatToIntBits(other.sample260_230_Ratio))
			return false;
		if (Float.floatToIntBits(sample260_280_Ratio) != Float.floatToIntBits(other.sample260_280_Ratio))
			return false;
		if (sampleAvgHemoglobinResult == null) {
			if (other.sampleAvgHemoglobinResult != null)
				return false;
		} else if (!sampleAvgHemoglobinResult.equals(other.sampleAvgHemoglobinResult))
			return false;
		if (sampleAvgHemoglobinResultDesc == null) {
			if (other.sampleAvgHemoglobinResultDesc != null)
				return false;
		} else if (!sampleAvgHemoglobinResultDesc.equals(other.sampleAvgHemoglobinResultDesc))
			return false;
		if (sampleAvgHemoglobinUnits == null) {
			if (other.sampleAvgHemoglobinUnits != null)
				return false;
		} else if (!sampleAvgHemoglobinUnits.equals(other.sampleAvgHemoglobinUnits))
			return false;
		if (Float.floatToIntBits(sampleAvgHemoglobinVal) != Float.floatToIntBits(other.sampleAvgHemoglobinVal))
			return false;
		if (sampleClottingIndicator == null) {
			if (other.sampleClottingIndicator != null)
				return false;
		} else if (!sampleClottingIndicator.equals(other.sampleClottingIndicator))
			return false;
		if (sampleCollectionType == null) {
			if (other.sampleCollectionType != null)
				return false;
		} else if (!sampleCollectionType.equals(other.sampleCollectionType))
			return false;
		if (sampleHemolysisSacle != other.sampleHemolysisSacle)
			return false;
		if (sampleId == null) {
			if (other.sampleId != null)
				return false;
		} else if (!sampleId.equals(other.sampleId))
			return false;
		if (sampleTurbidityScale == null) {
			if (other.sampleTurbidityScale != null)
				return false;
		} else if (!sampleTurbidityScale.equals(other.sampleTurbidityScale))
			return false;
		if (sampleUnitMeasurement == null) {
			if (other.sampleUnitMeasurement != null)
				return false;
		} else if (!sampleUnitMeasurement.equals(other.sampleUnitMeasurement))
			return false;
		if (sampleUnitNum != other.sampleUnitNum)
			return false;
		if (visitType == null) {
			if (other.visitType != null)
				return false;
		} else if (!visitType.equals(other.visitType))
			return false;
		return true;
	}
	
	

}

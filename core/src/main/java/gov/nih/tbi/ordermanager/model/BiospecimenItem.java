
package gov.nih.tbi.ordermanager.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * 
 * @author vpacha
 * 
 */
@Entity
@Table(name = "Biospecimen_Item")
public class BiospecimenItem implements Serializable
{

    private static final long serialVersionUID = 7197485714508241027L;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BIOSPECIMEN_ITEM_SEQ")
    @SequenceGenerator(name = "BIOSPECIMEN_ITEM_SEQ", sequenceName = "BIOSPECIMEN_ITEM_SEQ", allocationSize = 1)
    private Long id;
    @Column(name = "Coriell_ID")
    private String coriellId;
    @Column(name = "Sample_Ref_ID")
    private Long sampleRefId;
    @Column(name = "Specimen_Type")
    private String specimenType;
    @Column(name = "Original_Container_Type_Received")
    private String originalContainerTypeReceived;
    /* TODO currently ignoring subcollection field, need to figure out more about that field before adding it here */
    @Column(name = "Visit_Description")
    private String visitDescription;
    @Column(name = "Number_Of_Aliquots")
    private Integer numberOfAliquots;
    @ManyToOne(optional = false, fetch = FetchType.EAGER, targetEntity = BioRepository.class)
    @JoinColumn(name = "Repository_ID")
    private BioRepository bioRepository;
    @ManyToOne(optional = true, fetch = FetchType.LAZY, targetEntity = ItemQueue.class)
    @JoinColumn(name = "Item_Queue_ID")
    private ItemQueue itemQueue;
    @ManyToOne(optional = true, fetch = FetchType.LAZY, targetEntity = BiospecimenOrder.class)
    @JoinColumn(name = "Biospecimen_Order_ID")
    private BiospecimenOrder biospecimenOrder;

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
    private Date inventoryDate;
    @Column(name = "UNIT_NUMBER")
    private String unitNumber;
    @Column(name = "UNIT_MEASUREMENT")
    private String unitMeasurement;  
    @Column(name = "ST_NUMBER")
    private String stNumber;
    @Column(name = "NEURO_DIAGNOSIS")
    private String neuroDiagnosis;
    @Column(name = "CASE_CONTROL")
    private String caseControl;
    

    public Long getId()
    {

        return id;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    public String getCoriellId()
    {

        return coriellId;
    }

    public void setCoriellId(String coriellId)
    {

        this.coriellId = coriellId;
    }

    public Long getSampleRefId()
    {

        return sampleRefId;
    }

    public void setSampleRefId(Long sampleRefId)
    {

        this.sampleRefId = sampleRefId;
    }

    public String getSpecimenType()
    {

        return specimenType;
    }

    public void setSpecimenType(String specimenType)
    {

        this.specimenType = specimenType;
    }

    public String getOriginalContainerTypeReceived()
    {

        return originalContainerTypeReceived;
    }

    public void setOriginalContainerTypeReceived(String originalContainerTypeReceived)
    {

        this.originalContainerTypeReceived = originalContainerTypeReceived;
    }

    public String getVisitDescription()
    {

        return visitDescription;
    }

    public void setVisitDescription(String visitDescription)
    {

        this.visitDescription = visitDescription;
    }

    public Integer getNumberOfAliquots()
    {

        return numberOfAliquots;
    }

    public void setNumberOfAliquots(Integer numberOfAliquots)
    {

        this.numberOfAliquots = numberOfAliquots;
    }

    public ItemQueue getItemQueue()
    {

        return itemQueue;
    }

    public void setItemQueue(ItemQueue itemQueue)
    {

        this.itemQueue = itemQueue;
    }

    public BiospecimenOrder getBiospecimenOrder()
    {

        return biospecimenOrder;
    }

    public void setBiospecimenOrder(BiospecimenOrder biospecimenOrder)
    {

        this.biospecimenOrder = biospecimenOrder;
    }

    public BioRepository getBioRepository()
    {

        return bioRepository;
    }

    public void setBioRepository(BioRepository bioRepository)
    {

        this.bioRepository = bioRepository;
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

	@Override
    public int hashCode()
    {

        final int prime = 31;
        int result = 1;
        result = prime * result + ((coriellId == null) ? 0 : coriellId.hashCode());
        result = prime * result + ((bioRepository.getId() == null) ? 0 : bioRepository.getId().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BiospecimenItem other = (BiospecimenItem) obj;
        if (coriellId == null)
        {
            if (other.coriellId != null)
                return false;
        }
        else
            if (!coriellId.equals(other.coriellId))
                return false;
        
        if (bioRepository == null)
        {
            if (other.bioRepository != null)
                return false;
        }
        else
            if (!bioRepository.equals(other.bioRepository))
                return false;
        return true;
    }

    public String getGuid()
    {

        return guid;
    }

    public void setGuid(String guid)
    {

        this.guid = guid;
    }

    public String getVisitTypePDBP()
    {

        return visitTypePDBP;
    }

    public void setVisitTypePDBP(String visitTypePDBP)
    {

        this.visitTypePDBP = visitTypePDBP;
    }

    public String getAgeYrs()
    {

        return ageYrs;
    }

    public void setAgeYrs(String ageYrs)
    {

        this.ageYrs = ageYrs;
    }

    public String getAgeVal()
    {

        return ageVal;
    }

    public void setAgeVal(String ageVal)
    {

        this.ageVal = ageVal;
    }

    public String getBiosampleDataOriginator()
    {

        return biosampleDataOriginator;
    }

    public void setBiosampleDataOriginator(String biosampleDataOriginator)
    {

        this.biosampleDataOriginator = biosampleDataOriginator;
    }

    public String getBioreposTubeID()
    {

        return bioreposTubeID;
    }

    public void setBioreposTubeID(String bioreposTubeID)
    {

        this.bioreposTubeID = bioreposTubeID;
    }

    public String getSampCollType()
    {

        return sampCollType;
    }

    public void setSampCollType(String sampCollType)
    {

        this.sampCollType = sampCollType;
    }

    public String getSampleAliquotMass()
    {

        return sampleAliquotMass;
    }

    public void setSampleAliquotMass(String sampleAliquotMass)
    {

        this.sampleAliquotMass = sampleAliquotMass;
    }

    public String getSampleAliquotMassUnits()
    {

        return sampleAliquotMassUnits;
    }

    public void setSampleAliquotMassUnits(String sampleAliquotMassUnits)
    {

        this.sampleAliquotMassUnits = sampleAliquotMassUnits;
    }

    public String getSampleAliquotVol()
    {

        return sampleAliquotVol;
    }

    public void setSampleAliquotVol(String sampleAliquotVol)
    {

        this.sampleAliquotVol = sampleAliquotVol;
    }

    public String getSampleAliquotVolUnits()
    {

        return sampleAliquotVolUnits;
    }

    public void setSampleAliquotVolUnits(String sampleAliquotVolUnits)
    {

        this.sampleAliquotVolUnits = sampleAliquotVolUnits;
    }

    public String getSampleAvgHemoglobinVal()
    {

        return sampleAvgHemoglobinVal;
    }

    public void setSampleAvgHemoglobinVal(String sampleAvgHemoglobinVal)
    {

        this.sampleAvgHemoglobinVal = sampleAvgHemoglobinVal;
    }

    public String getConcentrationUoM()
    {

        return concentrationUoM;
    }

    public void setConcentrationUoM(String concentrationUoM)
    {

        this.concentrationUoM = concentrationUoM;
    }

    public String getRepositoryBiosample()
    {

        return repositoryBiosample;
    }

    public void setRepositoryBiosample(String repositoryBiosample)
    {

        this.repositoryBiosample = repositoryBiosample;
    }

    public String getInventory()
    {
    
        return inventory;
    }

    public void setInventory(String inventory)
    {
    
        this.inventory = inventory;
    }
    
    public Date getInventoryDate()
    {
    
        return inventoryDate;
    }
    
    public void setInventoryDate(Date inventoryDate)
    {
    
        this.inventoryDate = inventoryDate;
    }
    
    public String getUnitNumber()
    {
    
        return unitNumber;
    }
    
    public void setUnitNumber(String unitNumber)
    {
    
        this.unitNumber = unitNumber;
    }
    
    public String getUnitMeasurement()
    {
    
        return unitMeasurement;
    }
   
    public void setUnitMeasurement(String unitMeasurement)
    {
    
        this.unitMeasurement = unitMeasurement;
    }

	public String getStNumber() {
		return stNumber;
	}

	public void setStNumber(String stNumber) {
		this.stNumber = stNumber;
	}

	

}

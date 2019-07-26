package gov.nih.tbi.dictionary.model.hibernate.eform;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.commons.model.StatusType;

@Entity
@Table(name = "EFORM")
@XmlRootElement(name = "BasicEform")
@XmlAccessorType(XmlAccessType.FIELD)
public class BasicEform implements Serializable{

	private static final long serialVersionUID = -3701624117976727162L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EFORM_SEQ")
	@SequenceGenerator(name = "EFORM_SEQ", sequenceName = "EFORM_SEQ", allocationSize = 1)
	private Long id;
	
	@Column(name = "TITLE")
	private String title;
	
	@Column(name = "SHORT_NAME")
	private String shortName;
	
	@Column(name = "FORM_STRUCTURE_NAME")
	private String formStructureShortName;
	
	@Column(name = "DESCRIPTION")
	private String description;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "STATUS_ID")
	public StatusType status;

	@Column(name = "CREATED_BY")
	private String createBy;

	@Column(name = "CREATE_DATE")
	private Date createDate;
	
	@Column(name = "PUBLISHED_DATE")
	private Date publicationDate;
	
	@Column(name = "UPDATED_DATE")
	private Date updatedDate;
	
	@Column(name = "IS_SHARED")
	private Boolean isShared;
	
	@Column(name = "IS_LEGACY")
	private Boolean isLegacy;
	
	@Column(name="allow_multiple_collection_instances")
	private boolean allowMultipleCollectionInstances;
	
	//added by Ching-Heng
	@Column(name = "IS_CAT")
	private Boolean isCAT;
	
	@Column(name = "CAT_OID")
	private String catOid;
	
	@Column(name = "MEASUREMENT_TYPE")
	private String measurementType;
	
	@Transient
	private String formStructureTitle;
	@Transient
	private Boolean isMandatory;
	@Transient
	private Boolean isSelfReport;
	@Transient
	private Integer orderValue;
	@Transient
	private Boolean isConfigured;

	public BasicEform() {
		this.formStructureTitle = "";
		this.isMandatory = Boolean.FALSE;
		this.isSelfReport = Boolean.FALSE;
		this.orderValue = Integer.valueOf(0);
		this.isConfigured = Boolean.FALSE;;
	}
	
	/**
	 * Copy constructor, which produces a deep copy of the target object.
	 * 
	 * @param bf - The BasicEform object whose data will be copied to this new instance.
	 */
	public BasicEform(BasicEform bf) {
		this.id = bf.getId();
		this.title = bf.getTitle();
		this.shortName = bf.getShortName();
		this.formStructureShortName = bf.getFormStructureShortName();
		this.description = bf.getDescription();
		this.status = bf.getStatus();
		this.createBy = bf.getCreateBy();
		this.createDate = bf.getCreateDate();
		this.publicationDate = bf.getPublicationDate();
		this.updatedDate = bf.getUpdatedDate();
		this.isShared = bf.getIsShared();
		this.isLegacy = bf.getIsLegacy();
		this.allowMultipleCollectionInstances = bf.isAllowMultipleCollectionInstances();
		this.formStructureTitle = bf.getFormStructureTitle();
		this.isMandatory = bf.getIsMandatory();
		this.isSelfReport = bf.getIsSelfReport();
		this.orderValue = bf.getOrderValue();
		this.isCAT=bf.getIsCAT();
		this.catOid=bf.getCatOid();
		this.measurementType=bf.getMeasurementType();
		this.isConfigured=bf.getIsConfigured();
	}

	public Boolean getIsCAT() {
		return isCAT;
	}

	public void setIsCAT(Boolean isCAT) {
		this.isCAT = isCAT;
	}

	public String getMeasurementType() {
		return measurementType;
	}

	public void setMeasurementType(String measurementType) {
		this.measurementType = measurementType;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getCatOid() {
		return catOid;
	}

	public void setCatOid(String catOid) {
		this.catOid = catOid;
	}

	public void setShortName(String shortName){
		this.shortName = shortName;
	}
	
	public String getShortName(){
		return this.shortName;
	}

	public String getFormStructureShortName() {
		return formStructureShortName;
	}

	public void setFormStructureShortName(String formStructureShortName) {
		this.formStructureShortName = formStructureShortName;
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
	
	public String getCreateBy() {
		return createBy;
	}

	public void setCreateBy(String createBy) {
		this.createBy = createBy;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
	public String getDateCreatedString() {

        if (createDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(ModelConstants.ISO_DATE_FORMAT);
            StringBuffer date = new StringBuffer();
            sdf.format(createDate, date, new FieldPosition(DateFormat.MONTH_FIELD));
            return date.toString();
        } else {
            return ModelConstants.EMPTY_STRING;
        }
    }

	public Date getPublicationDate() {
		return publicationDate;
	}

	public void setPublicationDate(Date publicationDate) {
		this.publicationDate = publicationDate;
	}
	
	public String getDatePublishedString() {

        if (publicationDate != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(ModelConstants.ISO_DATE_FORMAT);
            StringBuffer date = new StringBuffer();
            sdf.format(publicationDate, date, new FieldPosition(DateFormat.MONTH_FIELD));
            return date.toString();
        } else {
            return ModelConstants.EMPTY_STRING;
        }
    }
	
	public Date getUpdatedDate() {
		return updatedDate;
	}
	
	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}
	
	public void setupdateddate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}
	
	public Boolean getIsShared() {
		return isShared;
	}

	public void setIsShared(Boolean isShared) {
		this.isShared = isShared;
	}

	public String getFormStructureTitle() {
		return formStructureTitle;
	}

	public void setFormStructureTitle(String formStructureTitle) {
		this.formStructureTitle = formStructureTitle;
	}

	public Boolean getIsMandatory() {
		return isMandatory;
	}

	public void setIsMandatory(Boolean isMandatory) {
		this.isMandatory = isMandatory;
	}

	public Boolean getIsSelfReport() {
		return isSelfReport;
	}

	public void setIsSelfReport(Boolean isSelfReport) {
		this.isSelfReport = isSelfReport;
	}

	public boolean isAllowMultipleCollectionInstances() {
		return allowMultipleCollectionInstances;
	}

	public void setAllowMultipleCollectionInstances(boolean allowMultipleCollectionInstances) {
		this.allowMultipleCollectionInstances = allowMultipleCollectionInstances;
	}

	public Integer getOrderValue() {
		return orderValue;
	}

	public void setOrderValue(Integer orderValue) {
		this.orderValue = orderValue;
	}
	
	public Boolean getIsLegacy() {
		return isLegacy;
	}

	public void setIsLegacy(Boolean isLegacy) {
		this.isLegacy = isLegacy;
	}
	
	public List<Date> getAllDates(){
		List<Date> dates  = new ArrayList<>();
		dates.add(getCreateDate());
		dates.add(getUpdatedDate());
		dates.add(getPublicationDate());
		
		return dates;
	}

	@Override
	public String toString() {
		return "BasiceForm [eForm Id=" + id + ", title=" + title + ", Status=" + status.name() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (allowMultipleCollectionInstances ? 1231 : 1237);
		result = prime * result + ((catOid == null) ? 0 : catOid.hashCode());
		result = prime * result + ((createBy == null) ? 0 : createBy.hashCode());
		result = prime * result + ((createDate == null) ? 0 : createDate.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((formStructureShortName == null) ? 0 : formStructureShortName.hashCode());
		result = prime * result + ((formStructureTitle == null) ? 0 : formStructureTitle.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((isCAT == null) ? 0 : isCAT.hashCode());
		result = prime * result + ((isConfigured == null) ? 0 : isConfigured.hashCode());
		result = prime * result + ((isLegacy == null) ? 0 : isLegacy.hashCode());
		result = prime * result + ((isMandatory == null) ? 0 : isMandatory.hashCode());
		result = prime * result + ((isSelfReport == null) ? 0 : isSelfReport.hashCode());
		result = prime * result + ((isShared == null) ? 0 : isShared.hashCode());
		result = prime * result + ((measurementType == null) ? 0 : measurementType.hashCode());
		result = prime * result + ((orderValue == null) ? 0 : orderValue.hashCode());
		result = prime * result + ((publicationDate == null) ? 0 : publicationDate.hashCode());
		result = prime * result + ((shortName == null) ? 0 : shortName.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((updatedDate == null) ? 0 : updatedDate.hashCode());
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
		BasicEform other = (BasicEform) obj;
		if (allowMultipleCollectionInstances != other.allowMultipleCollectionInstances)
			return false;
		if (catOid == null) {
			if (other.catOid != null)
				return false;
		} else if (!catOid.equals(other.catOid))
			return false;
		if (createBy == null) {
			if (other.createBy != null)
				return false;
		} else if (!createBy.equals(other.createBy))
			return false;
		if (createDate == null) {
			if (other.createDate != null)
				return false;
		} else if (!createDate.equals(other.createDate))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (formStructureShortName == null) {
			if (other.formStructureShortName != null)
				return false;
		} else if (!formStructureShortName.equals(other.formStructureShortName))
			return false;
		if (formStructureTitle == null) {
			if (other.formStructureTitle != null)
				return false;
		} else if (!formStructureTitle.equals(other.formStructureTitle))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (isCAT == null) {
			if (other.isCAT != null)
				return false;
		} else if (!isCAT.equals(other.isCAT))
			return false;
		if (isConfigured == null) {
			if (other.isConfigured != null)
				return false;
		} else if (!isConfigured.equals(other.isConfigured))
			return false;
		if (isLegacy == null) {
			if (other.isLegacy != null)
				return false;
		} else if (!isLegacy.equals(other.isLegacy))
			return false;
		if (isMandatory == null) {
			if (other.isMandatory != null)
				return false;
		} else if (!isMandatory.equals(other.isMandatory))
			return false;
		if (isSelfReport == null) {
			if (other.isSelfReport != null)
				return false;
		} else if (!isSelfReport.equals(other.isSelfReport))
			return false;
		if (isShared == null) {
			if (other.isShared != null)
				return false;
		} else if (!isShared.equals(other.isShared))
			return false;
		if (measurementType == null) {
			if (other.measurementType != null)
				return false;
		} else if (!measurementType.equals(other.measurementType))
			return false;
		if (orderValue == null) {
			if (other.orderValue != null)
				return false;
		} else if (!orderValue.equals(other.orderValue))
			return false;
		if (publicationDate == null) {
			if (other.publicationDate != null)
				return false;
		} else if (!publicationDate.equals(other.publicationDate))
			return false;
		if (shortName == null) {
			if (other.shortName != null)
				return false;
		} else if (!shortName.equals(other.shortName))
			return false;
		if (status != other.status)
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (updatedDate == null) {
			if (other.updatedDate != null)
				return false;
		} else if (!updatedDate.equals(other.updatedDate))
			return false;
		//not including isSelfReport in this method to prevent duplicates
		//in the psr eform configuring table
		return true;
	}

	public Boolean getIsConfigured() {
		return isConfigured;
	}

	public void setIsConfigured(Boolean isConfigured) {
		this.isConfigured = isConfigured;
	}
	
}
package gov.nih.tbi.dictionary.model.hibernate.eform;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashSet;
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
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.dictionary.model.EformPfCategory;

@Entity
@Table(name = "EFORM")
@XmlRootElement(name = "Eform")
@XmlAccessorType(XmlAccessType.FIELD)
public class Eform implements Serializable{

	private static final long serialVersionUID = -3701434117976727162L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "EFORM_SEQ")
	@SequenceGenerator(name = "EFORM_SEQ", sequenceName = "EFORM_SEQ", allocationSize = 1)
	@XmlTransient
	private Long id;
	
	@Column(name = "TITLE")
	private String title;
	
	@Column (name ="SHORT_NAME")
	private String shortName;
	
	@Column(name = "DESCRIPTION")
	private String description;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "STATUS_ID")
	private StatusType status;

	@Column(name = "CREATED_BY")
	private String createBy;

	@Column(name = "CREATE_DATE")
	private Date createDate;
	
	@Column(name = "UPDATED_DATE")
	@XmlTransient
	private Date updatedDate;
	
	@Column(name = "UPDATED_BY")
	@XmlTransient
	private String updatedBy;

	@Column(name = "FORM_BORDER")
	private Boolean formBorder;

	@Column(name = "SECTION_BORDER")
	private Boolean sectionBorder;

	@Column(name = "FORM_NAME_FONT")
	private String formNameFont;

	@Column(name = "FORM_NAME_COLOR")
	private String formNameColor;

	@Column(name = "SECTION_NAME_FONT")
	private String sectionNameFont;

	@Column(name = "SECTION_NAME_COLOR")
	private String sectionNameColor;

	@Column(name = "ORDER_VAL")
	private Integer orderVal;

	@Column(name = "HEADER")
	private String header;

	@Column(name = "FOOTER")
	private String footer;

	@Column(name = "FONT_SIZE")
	private Integer fontSize;

	@Column(name = "DATA_ENTRY_WORK_FLOW_TYPE")
	@XmlTransient
	private Integer dataEntryWorkFlowType;

	@Column(name = "CELL_PADDING")
	private Integer cellPadding;

	@Column(name = "ATTACH_FILES")
	@XmlTransient
	private boolean attachFiles;

	@Column(name = "ENABLE_DATA_SPRING")
	@XmlTransient
	private String enableDataSpring;

	@Column(name = "TAB_DISPLAY")
	@XmlTransient
	private boolean tabDisplay;

	@Column(name = "COPY_RIGHT")
	private Boolean copyRight;

	@Column(name = "FORM_STRUCTURE_NAME")
	private String formStructureShortName;
	
	@Column(name="allow_multiple_collection_instances")
	private boolean allowMultipleCollectionInstances;
	
	@Column(name = "IS_LEGACY")
	private Boolean isLegacy;
	
	@Column(name = "IS_SHARED")
	private Boolean isShared;
	
	// added by Cging-Heng
	@Column(name = "IS_CAT")
	private Boolean isCAT;
	
	@Column(name = "CAT_OID")
	private String catOid;
	
	@Column(name = "MEASUREMENT_TYPE")
	private String measurementType;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "PF_CATEGORY")
	private EformPfCategory pfCategory;
	
	@XmlElementWrapper(name = "SectionSet")
	@XmlElement(name ="Section")
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@JoinColumn(name = "eform_id", nullable = true)
	private Set<Section> sectionList = new LinkedHashSet<Section>();
	
	public Eform(){}
	
	public Eform(Eform eform){
		this.setId(null);
		this.setTitle(null);
		this.setShortName(null);
		this.setDescription(eform.getDescription());
		this.setStatus(StatusType.DRAFT);
		this.setCreateDate(new Date());
		this.setUpdatedDate(null);
		this.setUpdatedBy(null);
		this.setFormBorder(eform.getFormBorder());
		this.setSectionBorder(eform.getSectionBorder());
		this.setFormNameFont(eform.getFormNameFont());
		this.setFormNameColor(eform.getFormNameColor());
		this.setSectionNameFont(eform.getSectionNameFont());
		this.setSectionNameColor(eform.getSectionNameColor());
		this.setOrderVal(eform.getOrderVal());
		this.setHeader(eform.getHeader());
		this.setFooter(eform.getFooter());
		this.setFontSize(eform.getFontSize());
		this.setDataEntryWorkFlowType(eform.getDataEntryWorkFlowType());
		this.setCellPadding(eform.getCellPadding());
		this.setAttachFiles(eform.getAttachFiles());
		this.setEnableDataSpring(eform.getEnableDataSpring());
		this.setTabDisplay(eform.getTabDisplay());
		this.setCopyRight(eform.getCopyRight());
		this.setFormStructureShortName(eform.getFormStructureShortName());
		this.setAllowMultipleCollectionInstances(eform.isAllowMultipleCollectionInstances());
		this.setIsShared(Boolean.FALSE);	
		this.setIsCAT(eform.getIsCAT());
		this.setCatOid(eform.getCatOid());
		this.setMeasurementType(eform.getMeasurementType());
		this.setPfCategory(eform.getPfCategory());
	}
	
	public Boolean getIsCAT() {
		return isCAT;
	}

	public void setIsCAT(Boolean isCAT) {
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

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
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

	public Date getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(Date updatedDate) {
		this.updatedDate = updatedDate;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public Boolean getFormBorder() {
		return formBorder;
	}

	public void setFormBorder(Boolean formBorder) {
		this.formBorder = formBorder;
	}

	public Boolean getSectionBorder() {
		return sectionBorder;
	}

	public void setSectionBorder(Boolean sectionBorder) {
		this.sectionBorder = sectionBorder;
	}

	public String getFormNameFont() {
		return formNameFont;
	}

	public void setFormNameFont(String formNameFont) {
		this.formNameFont = formNameFont;
	}

	public String getFormNameColor() {
		return formNameColor;
	}

	public boolean isAllowMultipleCollectionInstances() {
		return allowMultipleCollectionInstances;
	}

	public void setAllowMultipleCollectionInstances(boolean allowMultipleCollectionInstances) {
		this.allowMultipleCollectionInstances = allowMultipleCollectionInstances;
	}

	public void setFormNameColor(String formNameColor) {
		this.formNameColor = formNameColor;
	}

	public String getSectionNameFont() {
		return sectionNameFont;
	}

	public void setSectionNameFont(String sectionNameFont) {
		this.sectionNameFont = sectionNameFont;
	}

	public String getSectionNameColor() {
		return sectionNameColor;
	}

	public void setSectionNameColor(String sectionNameColor) {
		this.sectionNameColor = sectionNameColor;
	}

	public Integer getOrderVal() {
		return orderVal;
	}

	public void setOrderVal(Integer orderVal) {
		this.orderVal = orderVal;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getFooter() {
		return footer;
	}

	public void setFooter(String footer) {
		this.footer = footer;
	}

	public Integer getFontSize() {
		return fontSize;
	}

	public void setFontSize(Integer fontSize) {
		this.fontSize = fontSize;
	}

	public Integer getDataEntryWorkFlowType() {
		return dataEntryWorkFlowType;
	}

	public void setDataEntryWorkFlowType(Integer dataEntryWorkFlowType) {
		this.dataEntryWorkFlowType = dataEntryWorkFlowType;
	}

	public Integer getCellPadding() {
		return cellPadding;
	}

	public void setCellPadding(Integer cellPadding) {
		this.cellPadding = cellPadding;
	}

	public boolean getAttachFiles() {
		return attachFiles;
	}

	public void setAttachFiles(boolean attachFiles) {
		this.attachFiles = attachFiles;
	}

	public String getEnableDataSpring() {
		return enableDataSpring;
	}

	public void setEnableDataSpring(String enableDataSpring) {
		this.enableDataSpring = enableDataSpring;
	}

	public boolean getTabDisplay() {
		return tabDisplay;
	}

	public void setTabDisplay(boolean tabDisplay) {
		this.tabDisplay = tabDisplay;
	}

	public Boolean getCopyRight() {
		return copyRight;
	}

	public void setCopyRight(Boolean copyRight) {
		this.copyRight = copyRight;
	}

	public String getFormStructureShortName() {
		return formStructureShortName;
	}

	public void setFormStructureShortName(String formStructureShortName) {
		this.formStructureShortName = formStructureShortName;
	}

	public Boolean getIsLegacy() {
		return isLegacy;
	}

	public void setIsLegacy(Boolean isLegacy) {
		this.isLegacy = isLegacy;
	}


	public Boolean getIsShared() {
		return isShared;
	}

	public void setIsShared(Boolean isShared) {
		this.isShared = isShared;
	}

	public Set<Section> getSectionList() {
		return sectionList;
	}

	public void setSectionList(Set<Section> sectionList) {
		this.sectionList = sectionList;
	}
	
	public void addToSectionList(Section section){
		this.sectionList.add(section);
	}
	
	public EformPfCategory getPfCategory() {
		return pfCategory;
	}
	
	public void setPfCategory(EformPfCategory pfCategory) {
		this.pfCategory = pfCategory;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((shortName == null) ? 0 : shortName.hashCode());
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
		Eform other = (Eform) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (shortName == null) {
			if (other.shortName != null)
				return false;
		} else if (!shortName.equals(other.shortName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "eForm [eForm Id=" + id + ", title=" + title + ", Status=" + status.name() + ", short name=" + shortName + "]";
	}
	
}
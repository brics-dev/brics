package gov.nih.tbi.dictionary.model.hibernate.eform;


import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import gov.nih.tbi.commons.model.WebServiceStringToLongAdapter;

@Entity
@Table(name = "SECTION")
@XmlRootElement(name = "Section")
@XmlType(namespace = "http://tbi.nih.gov/DictionarySchema")
@XmlAccessorType(XmlAccessType.FIELD)
public class Section implements Comparable<Section>, Serializable{

	private static final long serialVersionUID = 7780358232728882425L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SECTION_SEQ")
	@SequenceGenerator(name = "SECTION_SEQ", sequenceName = "SECTION_SEQ", allocationSize = 1)
	@XmlID
	@XmlJavaTypeAdapter(WebServiceStringToLongAdapter.class)
	private Long id;

	@Column(name = "NAME")
	private String name;

	@Column(name = "DESCRIPTION")
	private String  description;

	@Column(name = "ORDER_VAL")
	@XmlTransient
	private Integer orderVal;

	@Column(name = "FORM_ROW")
	private Integer formRow;

	@Column(name = "FORM_COL")
	private Integer formCol;

	@Column(name = "SUPPRESS_FLAG")
	private Boolean suppressFlag;

	@Column(name = "LABEL")
	private String label;

	@Column(name = "ALT_LABEL")
	private String altLabel;

	@Column(name = "INTO_BOOLEAN")
	private Boolean intoBoolean;

	@Column(name = "COLLAPSABLE")
	private Boolean collapsable;

	@Column(name = "IS_RESPONSE_IMAGE")
	private Boolean isResponseImage;
	
	@Column(name = "ISREPEATABLE")
	private Boolean isRepeatable;

	@Column(name = "INITIAL_REPEATED_SECTIONS")
	@XmlElement(name="minimumValue")
	private Integer initialRepeatedSections;

	@Column(name = "MAX_REPEATED_SECTIONS")
	@XmlElement(name="maximumValue")
	private Integer maxRepeatedSections;

	@Column(name = "REPEATED_SECTION_PARENT_ID")
	private Long repeatedSectionParent;

	@Column(name = "GROUP_NAME")
	private String groupName;

	@Column(name = "IS_MANUALLY_ADDED")
	private Boolean isManuallyAdded;
	
	@XmlElementWrapper(name = "SectionQuestionSet")
	@XmlElement(name ="SectionQuestion")
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "section", targetEntity = SectionQuestion.class, orphanRemoval = true)
	private Set<SectionQuestion> sectionQuestion = new HashSet<SectionQuestion>();
	
	public Section(){}

	public Section(Section section){
		this.setId(null);
		this.setName(section.getName());
		this.setDescription(section.getDescription());
		this.setOrderVal(section.getOrderVal());
		this.setFormRow(section.getFormRow());
		this.setFormCol(section.getFormCol());
		this.setSuppressFlag(section.getSuppressFlag());
		this.setLabel(section.getLabel());
		this.setAltLabel(section.getAltLabel());
		this.setIntoBoolean(section.getIntoBoolean());
		this.setCollapsable(section.getCollapsable());
		this.setIsResponseImage(section.getIsResponseImage());
		this.setIsRepeatable(section.getIsRepeatable());
		this.setInitialRepeatedSections(section.getInitialRepeatedSections());
		this.setMaxRepeatedSections(section.getMaxRepeatedSections());
		this.setRepeatedSectionParent(section.getRepeatedSectionParent());
		this.setGroupName(section.getGroupName());
		this.setIsManuallyAdded(section.getIsManuallyAdded());
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getOrderVal() {
		return orderVal;
	}

	public void setOrderVal(Integer orderVal) {
		this.orderVal = orderVal;
	}

	public Integer getFormRow() {
		return formRow;
	}

	public void setFormRow(Integer formRow) {
		this.formRow = formRow;
	}

	public Integer getFormCol() {
		return formCol;
	}

	public void setFormCol(Integer formCol) {
		this.formCol = formCol;
	}

	public Boolean getSuppressFlag() {
		return suppressFlag;
	}

	public void setSuppressFlag(Boolean suppressFlag) {
		this.suppressFlag = suppressFlag;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getAltLabel() {
		return altLabel;
	}

	public void setAltLabel(String altLabel) {
		this.altLabel = altLabel;
	}

	public Boolean getIntoBoolean() {
		return intoBoolean;
	}

	public void setIntoBoolean(Boolean intoBoolean) {
		this.intoBoolean = intoBoolean;
	}

	public Boolean getCollapsable() {
		return collapsable;
	}

	public void setCollapsable(Boolean collapsable) {
		this.collapsable = collapsable;
	}

	public Boolean getIsResponseImage() {
		return isResponseImage;
	}

	public void setIsResponseImage(Boolean isResponseImage) {
		this.isResponseImage = isResponseImage;
	}

	public Boolean getIsRepeatable() {
		return isRepeatable;
	}

	public void setIsRepeatable(Boolean isRepeatable) {
		this.isRepeatable = isRepeatable;
	}

	public Integer getInitialRepeatedSections() {
		return initialRepeatedSections;
	}

	public void setInitialRepeatedSections(Integer initialRepeatedSections) {
		this.initialRepeatedSections = initialRepeatedSections;
	}

	public Integer getMaxRepeatedSections() {
		return maxRepeatedSections;
	}

	public void setMaxRepeatedSections(Integer maxRepeatedSections) {
		this.maxRepeatedSections = maxRepeatedSections;
	}

	public Long getRepeatedSectionParent() {
		return repeatedSectionParent;
	}

	public void setRepeatedSectionParent(Long repeatedSectionParent) {
		this.repeatedSectionParent = repeatedSectionParent;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Boolean getIsManuallyAdded() {
		return isManuallyAdded;
	}

	public void setIsManuallyAdded(Boolean isManuallyAdded) {
		this.isManuallyAdded = isManuallyAdded;
	}
	
	 public Set<SectionQuestion> getSectionQuestion() {
	 return sectionQuestion;
	 }
	
	 public void setSectionQuestion(Set<SectionQuestion> sectionQuestion) {
	 this.sectionQuestion = sectionQuestion;
	 }
	
	 public void addToSectionQuestion(SectionQuestion sectionQuestion) {
		 if(this.sectionQuestion == null){
			 Set<SectionQuestion> newSectionQuestionSet = new HashSet<SectionQuestion>();
			 setSectionQuestion(newSectionQuestionSet);
		 }
	 this.sectionQuestion.add(sectionQuestion);
	 }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Section other = (Section) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Section [Section Id=" + id + ", name=" + name + "]";
	}
	
	@Override
	public int compareTo(Section o) {
		
		if(this.formRow.equals(o.formRow)) {
			return this.formCol.compareTo(o.formCol);
		}else {
			return this.formRow.compareTo(o.formRow);
		}
		
		
	}
}
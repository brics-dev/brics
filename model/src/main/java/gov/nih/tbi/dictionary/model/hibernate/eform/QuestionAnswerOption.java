package gov.nih.tbi.dictionary.model.hibernate.eform;

import java.io.Serializable;

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
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "QUESTION_ANSWER_OPTION")
@XmlRootElement(name = "QuestionAnswerOption")
@XmlAccessorType(XmlAccessType.FIELD)
public class QuestionAnswerOption implements Serializable, Comparable<QuestionAnswerOption> {

	private static final long serialVersionUID = -8639554981618782771L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "QUESTION_ANSWER_OPTION_SEQ")
	@SequenceGenerator(name = "QUESTION_ANSWER_OPTION_SEQ", sequenceName = "QUESTION_ANSWER_OPTION_SEQ", allocationSize = 1)
	@XmlTransient
	private Long id;
	
	@Column(name = "DISPLAY")
	private String display = "";
	
	@Column(name = "CODE_VALUE")
	private String codeValue;

	@Column(name = "submitted_value")
	private String submittedValue;
	
	// added by Ching-Heng
	@Column(name = "ITEM_RESPONSE_OID")
	private String itemResponseOid;
	
	@Column(name = "ELEMENT_OID")
	private String elementOid;
	
	@Column(name = "SCORE")
	private double score = Integer.MIN_VALUE;
	
	@Enumerated(EnumType.ORDINAL)
	@Column(name = "QUESTION_ANSWER_TYPE_ID")
	private QuestionAnswerDataType questionAnswerDataType;
	
	@Transient
	@XmlTransient
	private int minCharacters = Integer.MIN_VALUE;
	
	@Transient
	@XmlTransient
	private int maxCharacters = Integer.MIN_VALUE;
	
	@Transient
	@XmlTransient
	private boolean selected = false;
	
	@Transient
	@XmlTransient
	private boolean includeOther = false;

	@Transient
	@XmlTransient
	private boolean displayPV = false;

	@Column(name = "ORDERVAL")
	private Integer orderVal;
	
	public QuestionAnswerOption(){}
	
	public QuestionAnswerOption(QuestionAnswerOption questionanswerOption){
		this.setId(null);
		this.setDisplay(questionanswerOption.getDisplay());
		this.setCodeValue(questionanswerOption.getCodeValue());
		this.setSubmittedValue(questionanswerOption.getSubmittedValue());
		this.setScore(questionanswerOption.getScore());
		this.setElementOid(questionanswerOption.getElementOid());
		this.setItemResponseOid(questionanswerOption.getItemResponseOid());
		this.setQuestionAnswerDataType(questionanswerOption.getQuestionAnswerDataType());
		this.setOrderVal(questionanswerOption.getOrderVal());
	}
	
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getDisplay() {
		return display;
	}
	
	public void setDisplay(String display) {
		this.display = display;
	}
	
	public String getCodeValue() {
		return codeValue;
	}
	
	public void setCodeValue(String codeValue) {
		this.codeValue = codeValue;
	}
	
	public String getSubmittedValue() {
		return submittedValue;
	}

	public void setSubmittedValue(String submittedValue) {
		this.submittedValue = submittedValue;
	}

	public String getItemResponseOid() {
		return itemResponseOid;
	}

	public void setItemResponseOid(String itemResponseOid) {
		this.itemResponseOid = itemResponseOid;
	}

	public String getElementOid() {
		return elementOid;
	}

	public void setElementOid(String elementOid) {
		this.elementOid = elementOid;
	}

	public double getScore() {
		return score;
	}
	
	public void setScore(double score) {
		this.score = score;
	}

	public QuestionAnswerDataType getQuestionAnswerDataType() {
		return questionAnswerDataType;
	}
	
	public void setQuestionAnswerDataType(QuestionAnswerDataType questionAnswerDataType) {
		this.questionAnswerDataType = questionAnswerDataType;
	}
	
	public int getMinCharacters() {
		return minCharacters;
	}
	
	public void setMinCharacters(int minCharacters) {
		this.minCharacters = minCharacters;
	}
	
	public int getMaxCharacters() {
		return maxCharacters;
	}
	public void setMaxCharacters(int maxCharacters) {
		this.maxCharacters = maxCharacters;
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public boolean isIncludeOther() {
		return includeOther;
	}
	
	public void setIncludeOther(boolean includeOther) {
		this.includeOther = includeOther;
	}
	
	public boolean isDisplayPV() {
		return displayPV;
	}

	public void setDisplayPV(boolean displayPV) {
		this.displayPV = displayPV;
	}

	public Integer getOrderVal(){
		return this.orderVal;
	}
	
	public void setOrderVal(Integer orderVal){
		this.orderVal = orderVal;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codeValue == null) ? 0 : codeValue.hashCode());
		result = prime * result + ((display == null) ? 0 : display.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (includeOther ? 1231 : 1237);
		result = prime * result + maxCharacters;
		result = prime * result + minCharacters;
		result = prime * result + ((questionAnswerDataType == null) ? 0 : questionAnswerDataType.hashCode());
		long temp;
		temp = Double.doubleToLongBits(score);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (selected ? 1231 : 1237);
		result = prime * result + ((codeValue == null) ? 0 : orderVal.hashCode());
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
		QuestionAnswerOption other = (QuestionAnswerOption) obj;
		if (codeValue == null) {
			if (other.codeValue != null)
				return false;
		} else if (!codeValue.equals(other.codeValue))
			return false;
		if (display == null) {
			if (other.display != null)
				return false;
		} else if (!display.equals(other.display))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (includeOther != other.includeOther)
			return false;
		if (maxCharacters != other.maxCharacters)
			return false;
		if (minCharacters != other.minCharacters)
			return false;
		if (questionAnswerDataType != other.questionAnswerDataType)
			return false;
		if (Double.doubleToLongBits(score) != Double.doubleToLongBits(other.score))
			return false;
		if (selected != other.selected)
			return false;
		if (orderVal == null) {
			if (other.orderVal != null)
				return false;
		} else if (!orderVal.equals(other.orderVal))
			return false;
		return true;
	}
	
    @Override
    public int compareTo(QuestionAnswerOption o){
    	if(this.getOrderVal() != null && o.getOrderVal() != null){
    		return this.getOrderVal().compareTo(o.getOrderVal());
    	} else {
    		return 0;
    	}
    }
}

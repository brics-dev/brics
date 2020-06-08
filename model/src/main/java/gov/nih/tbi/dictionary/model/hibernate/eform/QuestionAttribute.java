package gov.nih.tbi.dictionary.model.hibernate.eform;

import gov.nih.tbi.commons.model.AnswerType;
import gov.nih.tbi.commons.model.SkipRuleOperatorType;
import gov.nih.tbi.commons.model.SkipRuleType;

import java.io.Serializable;

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
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

@Entity
@Table(name = "QUESTION_ATTRIBUTES")
@XmlRootElement(name = "QuestionAttribute")
@XmlAccessorType(XmlAccessType.FIELD)
public class QuestionAttribute implements Serializable{

	private static final long serialVersionUID = -8182968152891914016L;

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "QUESTION_ATTRIBUTES_SEQ")
	@SequenceGenerator(name = "QUESTION_ATTRIBUTES_SEQ", sequenceName = "QUESTION_ATTRIBUTES_SEQ", allocationSize = 1)
	@XmlTransient
	private Long id;

	@Column(name = "REQUIRED_FLAG")
	private Boolean requiredFlag; //needs validation

	@Column(name = "CALCULATED_FLAG")
	private Boolean calculatedFlag;

	@Column(name = "SKIP_RULE_FLAG")
	private Boolean skipRuleFlag;
	
	@Column(name = "SKIP_RULE_EQUALS")
	private String skipRuleEquals;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "SKIP_RULE_TYPE")
	private SkipRuleType skipRuleType;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "SKIP_RULE_OPERATOR_TYPE")
	private SkipRuleOperatorType skipRuleOperatorType;

	@Column(name = "HEIGHT_ALIGN")
	private String hAlign;

	@Column(name = "VERTICLE_ALIGN")
	private String vAlign;

	@Column(name = "TEXT_COLOR")
	private String textColor;

	@Column(name = "FONT_FACE")
	private String fontFace;

	@Column(name = "FONT_SIZE")
	private String fontSize;

	@Column(name = "INDENT")
	private Integer indent;

	@Column(name = "RANGE_OPERATOR")
	private String rangeOperator;

	@Column(name = "RANGE_VALUE_1")
	private String rangeValue1;

	@Column(name = "RANGE_VALUE_2")
	private String rangeValue2;

	@Column(name = "DT_CONVERSION_FACTOR")
	private Integer dtConversionFactor;

	@Enumerated(EnumType.ORDINAL)
	@Column(name = "ANSWER_TYPE")
	private AnswerType answerType; //needs validation

	@Column(name = "MIN_CHARACTERS")
	private Integer minCharacters;

	@Column(name = "MAX_CHARACTERS")
	private Integer maxCharacters;

	@Column(name = "HORIZONTAL_DISPLAY")
	private Boolean horizontalDisplay;

	@Column(name = "TEXTBOX_HEIGHT")
	private Integer textBoxHeight;

	@Column(name = "TEXTBOX_WIDTH")
	private Integer textBoxWidth;

	@Column(name = "TEXTBOX_LENGTH")
	private Integer textBoxLength;

	@Column(name = "DATA_SPRING")
	private Boolean dataSpring;

	@Column(name = "XHTML_TEXT")
	private String xhtmlText;

	@Column(name = "HORIZONTAL_DISPLAY_BREAK")
	private Boolean horizontalDisplayBreak;

	@Column(name = "DATA_ELEMENT_NAME")
	private String dataElementName;

	@Column(name = "PREPOPULATION")
	private Boolean prepopulation;

	@Column(name = "PREPOPULATION_VALUE")
	private String prepopulationValue;

	@Column(name = "DECIMAL_PRECISION")
	private Integer decimalPrecision;

	@Column(name = "HAS_CONVERSION_FACTOR")
	private Boolean hasConversionFactor;
	
	@Column(name = "CONVERSION_FACTOR")
	private String conversionFactor;

	@Column(name = "GROUP_NAME")
	private String groupName;

	@Column(name = "SHOW_TEXT")
	private Boolean showText;
	
	@Column(name = "TBL_HEADER_TYPE")
	private Integer tableHeaderType;
	
	@XmlElement(name ="EmailTrigger")
	@OneToOne(orphanRemoval=true, cascade = CascadeType.ALL, fetch=FetchType.EAGER)
	@JoinColumn(name = "EMAIL_TRIGGER_ID", nullable = true)
	private EmailTrigger emailTrigger; 
	
	@Column(name = "CONDITIONAL_FOR_CALC")
	private Boolean conditionalForCalc;
	
	@Column(name = "COUNT_FLAG")
	private Boolean countFlag;
	
	

	public QuestionAttribute(){}
	
	public QuestionAttribute(QuestionAttribute questionAttribute){
		this.setId(null);
		this.setRequiredFlag(questionAttribute.getRequiredFlag());
		this.setCalculatedFlag(questionAttribute.getCalculatedFlag());
		this.setSkipRuleFlag(questionAttribute.getSkipRuleFlag());
		this.setSkipRuleEquals(questionAttribute.getSkipRuleEquals());
		this.setSkipRuleType(questionAttribute.getSkipRuleType());
		this.setSkipRuleOperatorType(questionAttribute.getSkipRuleOperatorType());
		this.sethAlign(questionAttribute.gethAlign());
		this.setvAlign(questionAttribute.getvAlign());
		this.setTextColor(questionAttribute.getTextColor());
		this.setFontFace(questionAttribute.getFontFace());
		this.setFontSize(questionAttribute.getFontSize());
		this.setIndent(questionAttribute.getIndent());
		this.setRangeOperator(questionAttribute.getRangeOperator());
		this.setRangeValue1(questionAttribute.getRangeValue1());
		this.setRangeValue2(questionAttribute.getRangeValue2());
		this.setDtConversionFactor(questionAttribute.getDtConversionFactor());
		this.setAnswerType(questionAttribute.getAnswerType());
		this.setMinCharacters(questionAttribute.getMinCharacters());
		this.setMaxCharacters(questionAttribute.getMaxCharacters());
		this.setHorizontalDisplay(questionAttribute.getHorizontalDisplay());
		this.setTextBoxHeight(questionAttribute.getTextBoxHeight());
		this.setTextBoxWidth(questionAttribute.getTextBoxWidth());
		this.setTextBoxLength(questionAttribute.getTextBoxLength());
		this.setXhtmlText(questionAttribute.getXhtmlText());
		this.setHorizontalDisplayBreak(questionAttribute.getHorizontalDisplayBreak());
		this.setDataElementName(questionAttribute.getDataElementName());
		this.setPrepopulation(questionAttribute.getPrepopulation());
		this.setPrepopulationValue(questionAttribute.getPrepopulationValue());
		this.setDecimalPrecision(questionAttribute.getDecimalPrecision());
		this.setHasConversionFactor(questionAttribute.getHasConversionFactor());
		this.setConversionFactor(questionAttribute.getConversionFactor());
		this.setGroupName(questionAttribute.getGroupName());
		this.setShowText(questionAttribute.getShowText());
		this.setTableHeaderType(questionAttribute.getTableHeaderType());
		if(questionAttribute.getEmailTrigger() != null){
			this.setEmailTrigger(new EmailTrigger(questionAttribute.getEmailTrigger()));
		}
		this.setConditionalForCalc(questionAttribute.getConditionalForCalc());
		this.setCountFlag(questionAttribute.getCountFlag());
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getRequiredFlag() {
		return requiredFlag;
	}

	public void setRequiredFlag(Boolean requiredFlag) {
		this.requiredFlag = requiredFlag;
	}

	public String getSkipRuleEquals() {
		return skipRuleEquals;
	}

	public void setSkipRuleEquals(String skipRuleEquals) {
		this.skipRuleEquals = skipRuleEquals;
	}

	public Boolean getCalculatedFlag() {
		return calculatedFlag;
	}

	public void setCalculatedFlag(Boolean calculatedFlag) {
		this.calculatedFlag = calculatedFlag;
	}
	
	public Boolean getConditionalForCalc() {
		return conditionalForCalc;
	}

	public void setConditionalForCalc(Boolean conditionalForCalc) {
		this.conditionalForCalc = conditionalForCalc;
	}

	public Boolean getSkipRuleFlag() {
		return skipRuleFlag;
	}

	public void setSkipRuleFlag(Boolean skipRuleFlag) {
		this.skipRuleFlag = skipRuleFlag;
	}

	public SkipRuleType getSkipRuleType() {
		return skipRuleType;
	}

	public void setSkipRuleType(SkipRuleType skipRuleType) {
		this.skipRuleType = skipRuleType;
	}

	public SkipRuleOperatorType getSkipRuleOperatorType() {
		return skipRuleOperatorType;
	}

	public void setSkipRuleOperatorType(SkipRuleOperatorType skipRuleOperatorType) {
		this.skipRuleOperatorType = skipRuleOperatorType;
	}

	public String gethAlign() {
		return hAlign;
	}

	public void sethAlign(String hAlign) {
		this.hAlign = hAlign;
	}

	public String getvAlign() {
		return vAlign;
	}

	public void setvAlign(String vAlign) {
		this.vAlign = vAlign;
	}

	public String getTextColor() {
		return textColor;
	}

	public void setTextColor(String textColor) {
		this.textColor = textColor;
	}

	public String getFontFace() {
		return fontFace;
	}

	public void setFontFace(String fontFace) {
		this.fontFace = fontFace;
	}

	public String getFontSize() {
		return fontSize;
	}

	public void setFontSize(String fontSize) {
		this.fontSize = fontSize;
	}

	public Integer getIndent() {
		return indent;
	}

	public void setIndent(Integer indent) {
		this.indent = indent;
	}

	public String getRangeOperator() {
		return rangeOperator;
	}

	public void setRangeOperator(String rangeOperator) {
		this.rangeOperator = rangeOperator;
	}

	public String getRangeValue1() {
		return rangeValue1;
	}

	public void setRangeValue1(String rangeValue1) {
		this.rangeValue1 = rangeValue1;
	}

	public String getRangeValue2() {
		return rangeValue2;
	}

	public void setRangeValue2(String rangeValue2) {
		this.rangeValue2 = rangeValue2;
	}

	public Integer getDtConversionFactor() {
		return dtConversionFactor;
	}

	public void setDtConversionFactor(Integer dtConversionFactor) {
		this.dtConversionFactor = dtConversionFactor;
	}

	public AnswerType getAnswerType() {
		return answerType;
	}

	public void setAnswerType(AnswerType answerType) {
		this.answerType = answerType;
	}

	public Integer getMinCharacters() {
		return minCharacters;
	}

	public void setMinCharacters(Integer minCharacters) {
		this.minCharacters = minCharacters;
	}

	public Integer getMaxCharacters() {
		return maxCharacters;
	}

	public void setMaxCharacters(Integer maxCharacters) {
		this.maxCharacters = maxCharacters;
	}

	public Boolean getHorizontalDisplay() {
		return horizontalDisplay;
	}

	public void setHorizontalDisplay(Boolean horizontalDisplay) {
		this.horizontalDisplay = horizontalDisplay;
	}

	public Integer getTextBoxHeight() {
		return textBoxHeight;
	}

	public void setTextBoxHeight(Integer textBoxHeight) {
		this.textBoxHeight = textBoxHeight;
	}

	public Integer getTextBoxWidth() {
		return textBoxWidth;
	}

	public void setTextBoxWidth(Integer textBoxWidth) {
		this.textBoxWidth = textBoxWidth;
	}

	public Integer getTextBoxLength() {
		return textBoxLength;
	}

	public void setTextBoxLength(Integer textBoxLength) {
		this.textBoxLength = textBoxLength;
	}

	public Boolean getDataSpring() {
		return dataSpring;
	}

	public void setDataSpring(Boolean dataSpring) {
		this.dataSpring = dataSpring;
	}

	public String getXhtmlText() {
		return xhtmlText;
	}

	public void setXhtmlText(String xhtmlText) {
		this.xhtmlText = xhtmlText;
	}

	public Boolean getHorizontalDisplayBreak() {
		return horizontalDisplayBreak;
	}

	public void setHorizontalDisplayBreak(Boolean horizontalDisplayBreak) {
		this.horizontalDisplayBreak = horizontalDisplayBreak;
	}

	public String getDataElementName() {
		return dataElementName;
	}

	public void setDataElementName(String dataElementName) {
		this.dataElementName = dataElementName;
	}

	public Boolean getPrepopulation() {
		return prepopulation;
	}

	public void setPrepopulation(Boolean prepopulation) {
		this.prepopulation = prepopulation;
	}

	public String getPrepopulationValue() {
		return prepopulationValue;
	}

	public void setPrepopulationValue(String prepopulationValue) {
		this.prepopulationValue = prepopulationValue;
	}

	public Integer getDecimalPrecision() {
		return decimalPrecision;
	}

	public void setDecimalPrecision(Integer decimalPrecision) {
		this.decimalPrecision = decimalPrecision;
	}

	public Boolean getHasConversionFactor() {
		return hasConversionFactor;
	}

	public void setHasConversionFactor(Boolean hasConversionFactor) {
		this.hasConversionFactor = hasConversionFactor;
	}

	public String getConversionFactor() {
		return conversionFactor;
	}

	public void setConversionFactor(String conversionFactor) {
		this.conversionFactor = conversionFactor;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Boolean getShowText() {
		return showText;
	}

	public void setShowText(Boolean showText) {
		this.showText = showText;
	}

	public Integer getTableHeaderType() {
		return tableHeaderType;
	}

	public void setTableHeaderType(Integer tableHeaderType) {
		this.tableHeaderType = tableHeaderType;
	}

	public EmailTrigger getEmailTrigger() {
		return emailTrigger;
	}

	public void setEmailTrigger(EmailTrigger emailTrigger) {
		this.emailTrigger = emailTrigger;
	}
	
	
	

	public Boolean getCountFlag() {
		return countFlag;
	}

	public void setCountFlag(Boolean countFlag) {
		this.countFlag = countFlag;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((answerType == null) ? 0 : answerType.hashCode());
		result = prime * result + ((calculatedFlag == null) ? 0 : calculatedFlag.hashCode());
		result = prime * result + ((conversionFactor == null) ? 0 : conversionFactor.hashCode());
		result = prime * result + ((dataElementName == null) ? 0 : dataElementName.hashCode());
		result = prime * result + ((dataSpring == null) ? 0 : dataSpring.hashCode());
		result = prime * result + ((decimalPrecision == null) ? 0 : decimalPrecision.hashCode());
		result = prime * result + ((dtConversionFactor == null) ? 0 : dtConversionFactor.hashCode());
		result = prime * result + ((emailTrigger == null) ? 0 : emailTrigger.hashCode());
		result = prime * result + ((fontFace == null) ? 0 : fontFace.hashCode());
		result = prime * result + ((fontSize == null) ? 0 : fontSize.hashCode());
		result = prime * result + ((groupName == null) ? 0 : groupName.hashCode());
		result = prime * result + ((hAlign == null) ? 0 : hAlign.hashCode());
		result = prime * result + ((hasConversionFactor == null) ? 0 : hasConversionFactor.hashCode());
		result = prime * result + ((horizontalDisplay == null) ? 0 : horizontalDisplay.hashCode());
		result = prime * result + ((horizontalDisplayBreak == null) ? 0 : horizontalDisplayBreak.hashCode());
		result = prime * result + ((indent == null) ? 0 : indent.hashCode());
		result = prime * result + ((maxCharacters == null) ? 0 : maxCharacters.hashCode());
		result = prime * result + ((minCharacters == null) ? 0 : minCharacters.hashCode());
		result = prime * result + ((prepopulation == null) ? 0 : prepopulation.hashCode());
		result = prime * result + ((prepopulationValue == null) ? 0 : prepopulationValue.hashCode());
		result = prime * result + ((rangeOperator == null) ? 0 : rangeOperator.hashCode());
		result = prime * result + ((rangeValue1 == null) ? 0 : rangeValue1.hashCode());
		result = prime * result + ((rangeValue2 == null) ? 0 : rangeValue2.hashCode());
		result = prime * result + ((requiredFlag == null) ? 0 : requiredFlag.hashCode());
		result = prime * result + ((showText == null) ? 0 : showText.hashCode());
		result = prime * result + ((skipRuleEquals == null) ? 0 : skipRuleEquals.hashCode());
		result = prime * result + ((skipRuleFlag == null) ? 0 : skipRuleFlag.hashCode());
		result = prime * result + ((skipRuleOperatorType == null) ? 0 : skipRuleOperatorType.hashCode());
		result = prime * result + ((skipRuleType == null) ? 0 : skipRuleType.hashCode());
		result = prime * result + ((tableHeaderType == null) ? 0 : tableHeaderType.hashCode());
		result = prime * result + ((textBoxHeight == null) ? 0 : textBoxHeight.hashCode());
		result = prime * result + ((textBoxLength == null) ? 0 : textBoxLength.hashCode());
		result = prime * result + ((textBoxWidth == null) ? 0 : textBoxWidth.hashCode());
		result = prime * result + ((textColor == null) ? 0 : textColor.hashCode());
		result = prime * result + ((vAlign == null) ? 0 : vAlign.hashCode());
		result = prime * result + ((xhtmlText == null) ? 0 : xhtmlText.hashCode());
		result = prime * result + ((conditionalForCalc == null) ? 0 : conditionalForCalc.hashCode());
		result = prime * result + ((countFlag == null) ? 0 : countFlag.hashCode());
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
		QuestionAttribute other = (QuestionAttribute) obj;
		if (answerType != other.answerType)
			return false;
		if (calculatedFlag == null) {
			if (other.calculatedFlag != null)
				return false;
		} else if (!calculatedFlag.equals(other.calculatedFlag))
			return false;
		if (conditionalForCalc == null) {
			if (other.conditionalForCalc != null)
				return false;
		} else if (!conditionalForCalc.equals(other.conditionalForCalc))
			return false;
		if (conversionFactor == null) {
			if (other.conversionFactor != null)
				return false;
		} else if (!conversionFactor.equals(other.conversionFactor))
			return false;
		if (dataElementName == null) {
			if (other.dataElementName != null)
				return false;
		} else if (!dataElementName.equals(other.dataElementName))
			return false;
		if (dataSpring == null) {
			if (other.dataSpring != null)
				return false;
		} else if (!dataSpring.equals(other.dataSpring))
			return false;
		if (decimalPrecision == null) {
			if (other.decimalPrecision != null)
				return false;
		} else if (!decimalPrecision.equals(other.decimalPrecision))
			return false;
		if (dtConversionFactor == null) {
			if (other.dtConversionFactor != null)
				return false;
		} else if (!dtConversionFactor.equals(other.dtConversionFactor))
			return false;
		if (emailTrigger == null) {
			if (other.emailTrigger != null)
				return false;
		} else if (!emailTrigger.equals(other.emailTrigger))
			return false;
		if (fontFace == null) {
			if (other.fontFace != null)
				return false;
		} else if (!fontFace.equals(other.fontFace))
			return false;
		if (fontSize == null) {
			if (other.fontSize != null)
				return false;
		} else if (!fontSize.equals(other.fontSize))
			return false;
		if (groupName == null) {
			if (other.groupName != null)
				return false;
		} else if (!groupName.equals(other.groupName))
			return false;
		if (hAlign == null) {
			if (other.hAlign != null)
				return false;
		} else if (!hAlign.equals(other.hAlign))
			return false;
		if (hasConversionFactor == null) {
			if (other.hasConversionFactor != null)
				return false;
		} else if (!hasConversionFactor.equals(other.hasConversionFactor))
			return false;
		if (horizontalDisplay == null) {
			if (other.horizontalDisplay != null)
				return false;
		} else if (!horizontalDisplay.equals(other.horizontalDisplay))
			return false;
		if (horizontalDisplayBreak == null) {
			if (other.horizontalDisplayBreak != null)
				return false;
		} else if (!horizontalDisplayBreak.equals(other.horizontalDisplayBreak))
			return false;
		if (indent == null) {
			if (other.indent != null)
				return false;
		} else if (!indent.equals(other.indent))
			return false;
		if (maxCharacters == null) {
			if (other.maxCharacters != null)
				return false;
		} else if (!maxCharacters.equals(other.maxCharacters))
			return false;
		if (minCharacters == null) {
			if (other.minCharacters != null)
				return false;
		} else if (!minCharacters.equals(other.minCharacters))
			return false;
		if (prepopulation == null) {
			if (other.prepopulation != null)
				return false;
		} else if (!prepopulation.equals(other.prepopulation))
			return false;
		if (prepopulationValue == null) {
			if (other.prepopulationValue != null)
				return false;
		} else if (!prepopulationValue.equals(other.prepopulationValue))
			return false;
		if (rangeOperator == null) {
			if (other.rangeOperator != null)
				return false;
		} else if (!rangeOperator.equals(other.rangeOperator))
			return false;
		if (rangeValue1 == null) {
			if (other.rangeValue1 != null)
				return false;
		} else if (!rangeValue1.equals(other.rangeValue1))
			return false;
		if (rangeValue2 == null) {
			if (other.rangeValue2 != null)
				return false;
		} else if (!rangeValue2.equals(other.rangeValue2))
			return false;
		if (requiredFlag == null) {
			if (other.requiredFlag != null)
				return false;
		} else if (!requiredFlag.equals(other.requiredFlag))
			return false;
		if (showText == null) {
			if (other.showText != null)
				return false;
		} else if (!showText.equals(other.showText))
			return false;
		if (skipRuleEquals == null) {
			if (other.skipRuleEquals != null)
				return false;
		} else if (!skipRuleEquals.equals(other.skipRuleEquals))
			return false;
		if (skipRuleFlag == null) {
			if (other.skipRuleFlag != null)
				return false;
		} else if (!skipRuleFlag.equals(other.skipRuleFlag))
			return false;
		if (skipRuleOperatorType != other.skipRuleOperatorType)
			return false;
		if (skipRuleType != other.skipRuleType)
			return false;
		if (tableHeaderType == null) {
			if (other.tableHeaderType != null)
				return false;
		} else if (!tableHeaderType.equals(other.tableHeaderType))
			return false;
		if (textBoxHeight == null) {
			if (other.textBoxHeight != null)
				return false;
		} else if (!textBoxHeight.equals(other.textBoxHeight))
			return false;
		if (textBoxLength == null) {
			if (other.textBoxLength != null)
				return false;
		} else if (!textBoxLength.equals(other.textBoxLength))
			return false;
		if (textBoxWidth == null) {
			if (other.textBoxWidth != null)
				return false;
		} else if (!textBoxWidth.equals(other.textBoxWidth))
			return false;
		if (textColor == null) {
			if (other.textColor != null)
				return false;
		} else if (!textColor.equals(other.textColor))
			return false;
		if (vAlign == null) {
			if (other.vAlign != null)
				return false;
		} else if (!vAlign.equals(other.vAlign))
			return false;
		if (xhtmlText == null) {
			if (other.xhtmlText != null)
				return false;
		} else if (!xhtmlText.equals(other.xhtmlText))
			return false;
		if (countFlag == null) {
			if (other.countFlag != null)
				return false;
		} else if (!countFlag.equals(other.countFlag))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "QuestionAttribute [QuestionAttribute Id=" + id + ", groupName=" + groupName + "]";
	}
}
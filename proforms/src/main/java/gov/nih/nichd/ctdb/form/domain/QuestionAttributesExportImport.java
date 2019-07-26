package gov.nih.nichd.ctdb.form.domain;

import gov.nih.nichd.ctdb.emailtrigger.domain.EmailTrigger;

public class QuestionAttributesExportImport {

	private int qaId;
	private int questionVersion;
	private int answerType;
	private int minCharacters;
	private int maxCharacters;
	
	private int skipRuleType = Integer.MIN_VALUE;
	private int skipRuleOperatorType = Integer.MIN_VALUE;
	
	private int textareaHeight;
	private int textareaWidth;
	private int textboxLength;
	private int decimalPrecision;
	
	//HTML attributes
	private int indent;
	
	
	
	
	private boolean required = false;
	private boolean calculatedQuestion = false;
	private String calculation;
	private int calculationType;
	private String dataElementName;
	//private int instanceType=Integer.MIN_VALUE;
	
	private boolean skipRule = false;
	private boolean horizontalDisplay = false;
	private boolean horizDisplayBreak = true;
	private boolean dataSpring;
	private boolean prepopulation;
	private boolean hasUnitConversionFactor;
	private String unitConversionFactor;
	private int dateConversionFactor;

	//HTML attributes
	private String halign;
	private String valign;
	private String textcolor;
	private String fontface;
	private String fontsize;
	

	private String rangeOperator;
	private String rangeValue1;
	private String rangeValue2;
	private String label;
	private String skipRuleEquals;
	private String htmlText;
	private String prepopulationValue;
    private String toEmailAddress;
    private String ccEmailAddress;
	private String emailSubject ;
    private String emailBody;
    private int eMailTriggerId;
    private boolean deleteTrigger=false;
    private String emailTriggerAnswers;
    private EmailTrigger emailTrigger;
    
    private boolean showText = true;
    private int tableHeaderType = 0;
    

	
	public boolean isShowText() {
		return showText;
	}

	public void setShowText(boolean showText) {
		this.showText = showText;
	}

	public int getTableHeaderType() {
		return tableHeaderType;
	}

	public void setTableHeaderType(int tableHeaderType) {
		this.tableHeaderType = tableHeaderType;
	}

	public EmailTrigger getEmailTrigger() {
		return emailTrigger;
	}

	public void setEmailTrigger(EmailTrigger emailTrigger) {
		this.emailTrigger = emailTrigger;
	}

	public int getCalculationType() {
		return calculationType;
	}

	public void setCalculationType(int calculationType) {
		this.calculationType = calculationType;
	}

	public int getDateConversionFactor() {
		return dateConversionFactor;
	}

	public void setDateConversionFactor(int dateConversionFactor) {
		this.dateConversionFactor = dateConversionFactor;
	}

	public String getCalculation() {
		return calculation;
	}

	public void setCalculation(String calculation) {
		this.calculation = calculation;
	}

	public String getDataElementName() {
		return dataElementName;
	}

	public void setDataElementName(String dataElementName) {
		this.dataElementName = dataElementName;
	}

	private int sectionId= Integer.MIN_VALUE;
	
	public int getSectionId() {
		return sectionId;
	}

	public void setSectionId(int sectionId) {
		this.sectionId = sectionId;
	}

/*	public int getInstanceType() {
		return instanceType;
	}

	public void setInstanceType(int instanceType) {
		this.instanceType = instanceType;
	}
*/
	public int getQaId() {
		return qaId;
	}

	public void setQaId(int qaId) {
		this.qaId = qaId;
	}

	public int getQuestionVersion() {
		return questionVersion;
	}

	public void setQuestionVersion(int questionVersion) {
		this.questionVersion = questionVersion;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public boolean isCalculatedQuestion() {
		return calculatedQuestion;
	}

	public void setCalculatedQuestion(boolean calculatedQuestion) {
		this.calculatedQuestion = calculatedQuestion;
	}

	public boolean isSkipRule() {
		return skipRule;
	}

	public int getDecimalPrecision() {
		return decimalPrecision;
	}

	public void setDecimalPrecision(int decimalPrecision) {
		this.decimalPrecision = decimalPrecision;
	}

	public void setSkipRule(boolean skipRule) {
		this.skipRule = skipRule;
	}

	public int getTextareaHeight() {
		return textareaHeight;
	}

	public void setTextareaHeight(int textareaHeight) {
		this.textareaHeight = textareaHeight;
	}

	public int getTextareaWidth() {
		return textareaWidth;
	}

	public void setTextareaWidth(int textareaWidth) {
		this.textareaWidth = textareaWidth;
	}

	public int getTextboxLength() {
		return textboxLength;
	}

	public boolean isHasUnitConversionFactor() {
		return hasUnitConversionFactor;
	}

	public void setHasUnitConversionFactor(boolean hasUnitConversionFactor) {
		this.hasUnitConversionFactor = hasUnitConversionFactor;
	}

	public void setTextboxLength(int textboxLength) {
		this.textboxLength = textboxLength;
	}

	public String getFontsize() {
		return fontsize;
	}

	public void setFontsize(String fontsize) {
		this.fontsize = fontsize;
	}

	public int getIndent() {
		return indent;
	}

	public boolean isDataSpring() {
		return dataSpring;
	}

	public void setDataSpring(boolean dataSpring) {
		this.dataSpring = dataSpring;
	}

	public void setIndent(int indent) {
		this.indent = indent;
	}



	public boolean isPrepopulation() {
		return prepopulation;
	}

	public void setPrepopulation(boolean prepopulation) {
		this.prepopulation = prepopulation;
	}

	public String getPrepopulationValue() {
		return prepopulationValue;
	}

	public String getUnitConversionFactor() {
		return unitConversionFactor;
	}

	public void setUnitConversionFactor(String unitConversionFactor) {
		this.unitConversionFactor = unitConversionFactor;
	}

	public void setPrepopulationValue(String prepopulationValue) {
		this.prepopulationValue = prepopulationValue;
	}

	public String getHalign() {
		return halign;
	}

	public void setHalign(String halign) {
		this.halign = halign;
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

	public boolean isHorizontalDisplay() {
		return horizontalDisplay;
	}

	public String getHtmlText() {
		return htmlText;
	}

	public void setHtmlText(String htmlText) {
		this.htmlText = htmlText;
	}

	public void setHorizontalDisplay(boolean horizontalDisplay) {
		this.horizontalDisplay = horizontalDisplay;
	}

	public boolean isHorizDisplayBreak() {
		return horizDisplayBreak;
	}

	public void setHorizDisplayBreak(boolean horizDisplayBreak) {
		this.horizDisplayBreak = horizDisplayBreak;
	}

	public int getAnswerType() {
		return answerType;
	}

	public void setAnswerType(int answerType) {
		this.answerType = answerType;
	}

	public String getValign() {
		return valign;
	}

	public void setValign(String valign) {
		this.valign = valign;
	}

	public String getTextcolor() {
		return textcolor;
	}

	public int getSkipRuleType() {
		return skipRuleType;
	}

	public void setSkipRuleType(int skipRuleType) {
		this.skipRuleType = skipRuleType;
	}

	public int getSkipRuleOperatorType() {
		return skipRuleOperatorType;
	}

	public void setSkipRuleOperatorType(int skipRuleOperatorType) {
		this.skipRuleOperatorType = skipRuleOperatorType;
	}

	public String getSkipRuleEquals() {
		return skipRuleEquals;
	}

	public void setSkipRuleEquals(String skipRuleEquals) {
		this.skipRuleEquals = skipRuleEquals;
	}

	public void setTextcolor(String textcolor) {
		this.textcolor = textcolor;
	}

	public String getFontface() {
		return fontface;
	}

	public void setFontface(String fontface) {
		this.fontface = fontface;
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

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

    public String getToEmailAddress() {
		return toEmailAddress;
	}

	public void setToEmailAddress(String toEmailAddress) {
		this.toEmailAddress = toEmailAddress;
	}

	public String getCcEmailAddress() {
		return ccEmailAddress;
	}

	public void setCcEmailAddress(String ccEmailAddress) {
		this.ccEmailAddress = ccEmailAddress;
	}

	public String getEmailSubject() {
		return emailSubject;
	}

	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}

	public String getEmailBody() {
		return emailBody;
	}

	public void setEmailBody(String emailBody) {
		this.emailBody = emailBody;
	}

	public int geteMailTriggerId() {
		return eMailTriggerId;
	}

	public void seteMailTriggerId(int eMailTriggerId) {
		this.eMailTriggerId = eMailTriggerId;
	}

	public boolean isDeleteTrigger() {
		return deleteTrigger;
	}

	public void setDeleteTrigger(boolean deleteTrigger) {
		this.deleteTrigger = deleteTrigger;
	}

	public String getEmailTriggerAnswers() {
		return emailTriggerAnswers;
	}

	public void setEmailTriggerAnswers(String emailTriggerAnswers) {
		this.emailTriggerAnswers = emailTriggerAnswers;
	}


}

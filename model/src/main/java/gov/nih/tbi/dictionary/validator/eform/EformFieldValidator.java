package gov.nih.tbi.dictionary.validator.eform;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import gov.nih.tbi.commons.model.AnswerType;
import gov.nih.tbi.commons.model.QuestionType;
import gov.nih.tbi.commons.model.SkipRuleOperatorType;
import gov.nih.tbi.commons.model.SkipRuleType;
import gov.nih.tbi.dictionary.constants.EformValidationConstants;
import gov.nih.tbi.dictionary.model.hibernate.eform.CalculationQuestion;
import gov.nih.tbi.dictionary.model.hibernate.eform.CalculationQuestionPk;
import gov.nih.tbi.dictionary.model.hibernate.eform.Eform;
import gov.nih.tbi.dictionary.model.hibernate.eform.EmailTrigger;
import gov.nih.tbi.dictionary.model.hibernate.eform.EmailTriggerValue;
import gov.nih.tbi.dictionary.model.hibernate.eform.Question;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionAnswerOption;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionAttribute;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionDocument;
import gov.nih.tbi.dictionary.model.hibernate.eform.Section;
import gov.nih.tbi.dictionary.model.hibernate.eform.SectionQuestion;
import gov.nih.tbi.dictionary.model.hibernate.eform.VisualScale;

public class EformFieldValidator {

	private Eform eform; 

	public EformFieldValidator(){
		throw new NullPointerException();
	}

	public EformFieldValidator(Eform eform) {
		if(eform == null){
			throw new NullPointerException();
		}
		this.eform = eform;
	}

	public List<String> validateEform(){
		
		List<String> errors = new ArrayList<String>();
		//form structure
		validateFormStructureName(eform.getFormStructureShortName(),errors);
		//short name
		validateShortName(eform.getShortName(),errors);
		//title
		validateTitle(eform.getTitle(),errors);
		//description
		validateDescription(eform.getDescription(),errors);
		//is legacy
		if(eform.getIsLegacy() != null){
			boolean isLegacy = eform.getIsLegacy().booleanValue();
			validateIsLegacy(isLegacy,errors);
		}
		validateStatus(eform.getStatus().name(),errors);
		//is shared
		/* Commented out CRIT-6574
		if(eform.getIsShared() != null){
			boolean isShared = eform.getIsShared().booleanValue();
			validateIsShared(isShared,errors);
		}
		*/
		//form name font 
		validateFormNameFont(eform.getFormNameFont(),errors);
		//form name color
		validateFormNameColor(eform.getFormNameColor(),errors);
		//section name font
		validateSectionNameFont(eform.getSectionNameFont(),errors);
		//form font size
		int formFontSize = eform.getFontSize().intValue();
		validateFormFontSize(formFontSize,errors);
		//cell padding
		int cellPadding = eform.getCellPadding().intValue();
		validateCellPadding(cellPadding,errors);
		//section name color
		validateSectionNameColor(eform.getSectionNameColor(),errors); 
		//header
		validateHeader(eform.getHeader(),errors);
		//footer
		validateFooter(eform.getFooter(),errors);
		
		for(Section eformSection: eform.getSectionList()) {
			//section name
			validateSectionName(eformSection.getName(),errors);
			//section description
			validateSectionDescription(eformSection.getDescription(),errors);
			//form row
			validateSectionFormRow(eformSection.getFormRow(),errors);
			//form col
			validateSectionFormCol(eformSection.getFormCol(),errors);
			//repeatable
			boolean isRepeatable = eformSection.getIsRepeatable().booleanValue();
			if(isRepeatable) {
				Integer initialRepeatedSections = eformSection.getInitialRepeatedSections();
				Integer maxRepeatedSections = eformSection.getMaxRepeatedSections();
				validateRepeatableMinMax(initialRepeatedSections,maxRepeatedSections,errors);
			}
			//group name
			validateSectionGroupName(eformSection.getGroupName(),errors);
			
			for(SectionQuestion eformSQ: eformSection.getSectionQuestion()){
				Question eformQuestion = eformSQ.getQuestion();
				//visual scale
				if(eformQuestion.getType() == QuestionType.VISUAL_SCALE) {
					validateVisualScale(eformQuestion.getVisualScale(),errors);
				}
				//question name
				validateQuestionName(eformQuestion.getName(),errors);
				//question text
				validateQuestionText(eformQuestion.getText(),errors);
				//default value
				validateDefaultValue(eformQuestion.getDefaultValue(),errors);
				//unanswered value
				validateUnansweredValue(eformQuestion.getUnansweredValue(),errors);
				//description up
				validateDescriptionUp(eformQuestion.getDescriptionUp(),errors);
				//description down
				validateDescriptionDown(eformQuestion.getDescriptionDown(),errors);
				//question answer options
				for(QuestionAnswerOption eformOption: eformQuestion.getQuestionAnswerOption()){
					//display
					validateQuestionAnswerOptionDisplay(eformOption.getDisplay(),errors);
					//submitted value
					validateQuestionAnswerOptionSubmittedValue(eformOption.getSubmittedValue(),errors);
				}
				//question images
				if (eformQuestion.getQuestionDocument() != null) {
			       for (QuestionDocument eformQD : eformQuestion.getQuestionDocument()) {
			        	String questionImageName = eformQD.getUserFile().getName();
			        	validateQuestionImageFileName(questionImageName,errors);
			        }
			     }
				//question attributes
				 QuestionAttribute eformQuestionAttribute = eformQuestion.getQuestionAttribute();
				 boolean calculatedFlag = false;
				 if(eformQuestionAttribute.getCalculatedFlag() != null) {
			        calculatedFlag = eformQuestionAttribute.getCalculatedFlag().booleanValue();
			     }
				 //calculated question
				 if (calculatedFlag == true) {
					 String calculation = eformSQ.getCalculation();
					 //calculation
					 validateCalculation(calculation,errors);

					 for (CalculationQuestion eformCalcQuestion : eformSQ.getCalculatedQuestion()) {
						 CalculationQuestionPk eformCalculationQuestionPk = eformCalcQuestion.getCalculationQuestionCompositePk();
						 Question eformQuestionUsedInCalc = eformCalculationQuestionPk.getCalculationQuestion();
						 if(eformQuestionUsedInCalc.getQuestionAnswerOption() != null && !eformQuestionUsedInCalc.getQuestionAnswerOption().isEmpty()){
							 for(QuestionAnswerOption eformCalcOption: eformQuestionUsedInCalc.getQuestionAnswerOption()){
								 if(eformQuestionUsedInCalc.getType() == QuestionType.SELECT || eformQuestionUsedInCalc.getType() == QuestionType.RADIO) {
									 //score
									 double score = eformCalcOption.getScore();
									 validateQuestionAnswerOptionDisplay(score,errors);
								 }
								 
							 }
						 }
					 }
				 }
				 //data element name
				 validateDataElementName(eformQuestionAttribute.getDataElementName(),errors);
				 //group name
				validateQuestionAttrubutesGroupName(eformQuestionAttribute.getGroupName(),errors);
				//prepopulation
				boolean prepopulation = false;
			    if(eformQuestionAttribute.getPrepopulation() != null) {
			        prepopulation = eformQuestionAttribute.getPrepopulation().booleanValue();
			    }
			    if(prepopulation) {
			    	//prepopulationValue
			    	validatePrepopulationValue(eformQuestionAttribute.getPrepopulationValue(),errors);	
			    }
			    //conversion factor
				boolean hasConversionFactor = false;
				if(eformQuestionAttribute.getHasConversionFactor() != null) {
					hasConversionFactor = eformQuestionAttribute.getHasConversionFactor().booleanValue();
				}
				if(hasConversionFactor) {
					String conversionFactor = eformQuestionAttribute.getConversionFactor();
					validateConversionFactor(conversionFactor,errors);
				}
				//skip rule
				boolean skipRule = false;
				if(eformQuestionAttribute.getSkipRuleFlag() != null) {
					skipRule = eformQuestionAttribute.getSkipRuleFlag().booleanValue();
				}
				if (skipRule) {
					//skip rule type
					validateSkipRuleType(eformQuestionAttribute.getSkipRuleType(),errors);
					//skip rule operator type
					validateSkipRuleOperatorType(eformQuestionAttribute.getSkipRuleOperatorType(),errors);
					//skip rule equals
					validateSkipRuleEquals(eformQuestionAttribute.getSkipRuleEquals(),errors);
				}
				//height align
				validateHeightAlign(eformQuestionAttribute.gethAlign(),errors);
				//vertical align
				validateVerticalAlign(eformQuestionAttribute.getvAlign(),errors);
				//text color
				validateQuestionTextColor(eformQuestionAttribute.getTextColor(),errors);
				//font face
				validateQuestionFontFace(eformQuestionAttribute.getFontFace(),errors);
				//font size
				validateQuestionFontSize(eformQuestionAttribute.getFontSize(),errors);
				//range operator
				validateRangeOperator(eformQuestionAttribute.getRangeOperator(),errors);
				//range value 1
				validateRangeValue1(eformQuestionAttribute.getRangeValue1(),errors);
				//range value 2
				validateRangeValue2(eformQuestionAttribute.getRangeValue2(),errors);
				//answer type
				validateAnswerType(eformQuestionAttribute.getAnswerType(),errors);
				//text area width
				int textAreaWidth = eformQuestionAttribute.getTextBoxWidth().intValue();
				validateTextAreaWidth(textAreaWidth,errors);
				//text area height
				int textAreaHeight = eformQuestionAttribute.getTextBoxHeight().intValue();
				validateTextAreaHeight(textAreaHeight,errors);
				//text box length
				int textBoxLength = eformQuestionAttribute.getTextBoxLength().intValue();
				validateTextBoxLength(textBoxLength,errors);
				//email trigger
				EmailTrigger eformEmailTrigger = eformQuestionAttribute.getEmailTrigger();
				if (eformEmailTrigger != null) { 
					//to email address
					validateToEmailAddress(eformEmailTrigger.getToEmailAddress(),errors);
					//cc email address
					validateCcEmailAddress(eformEmailTrigger.getCcEmailAddress(),errors);
					//subject
					validateSubject(eformEmailTrigger.getSubject(),errors);
					//body
					validateBody(eformEmailTrigger.getBody(),errors);	
					for (EmailTriggerValue emailTriggerValue : eformEmailTrigger.getTriggerValues()) {
						//email trigger answer
						validateEmailTriggerAnswer(emailTriggerValue.getAnswer(),errors);
					}
				}
				//xhtml
				validateXhtml(eformQuestionAttribute.getXhtmlText(),errors);
				//table header type
				int tableHeaderType = eformQuestionAttribute.getTableHeaderType().intValue();
				validateTableHeaderType(tableHeaderType,errors);
			}
		}
		return errors;
	}
	
	private void validateStatus(String Status, List<String> errors) {
		if(!isStatusValid(Status)){
			errors.add(EformValidationConstants.EFORM_STATUS_DRAFT);
		}
		
	}

	private void validateQuestionImageFileName(String fileName, List<String> errors) {
		if(!isQuestionImageFileNameMaxCharValid(fileName)) {
			errors.add(EformValidationConstants.EFORM_SIZE_QUESTION_IMAGE_FILE_NAME);
		}
	}
		
	private void validateEmailTriggerAnswer(String answer, List<String> errors) {
		if(!isEmailTriggerAnswerMaxCharValid(answer)) {
			errors.add(EformValidationConstants.EFORM_SIZE_EMAIL_TRIGGER_ANSWER);
		}
	}
		
	private void validateTableHeaderType(int tableHeaderType, List<String> errors) {
		if(!isTableHeaderTypeValid(tableHeaderType)) {
			errors.add(EformValidationConstants.EFORM_TABLE_HEADER_TYPE);
		}
	}
	
	private void validateXhtml(String xhtml, List<String> errors) {
		if(xhtml != null) {
			if(!isXhtmlMaxCharValid(xhtml)) {
				errors.add(EformValidationConstants.EFORM_SIZE_XHTML_TEXT);
			}
		}
	}
	
	private void validateBody(String body, List<String> errors) {
		if(body != null) {
			if(!isBodyMaxCharValid(body)) {
				errors.add(EformValidationConstants.EFORM_SIZE_EMAIL_BODY);
			}
		}
	}
	
	private void validateSubject(String subject, List<String> errors) {
		if(subject != null) {
			if(!isSubjectMaxCharValid(subject)) {
				errors.add(EformValidationConstants.EFORM_SIZE_EMAIL_SUBJECT);
			}
		}
	}
	
	private void validateCcEmailAddress(String ccEmailAddress, List<String> errors) {
		if(ccEmailAddress != null) {
			if(!isCcEmailAddressMaxCharValid(ccEmailAddress)) {
				errors.add(EformValidationConstants.EFORM_SIZE_CC_EMAIL_ADDRESS);
			}
		}
	}
	
	private void validateToEmailAddress(String toEmailAddress, List<String> errors) {
		if(toEmailAddress != null) {
			if(!isToEmailAddressMaxCharValid(toEmailAddress)) {
				errors.add(EformValidationConstants.EFORM_SIZE_TO_EMAIL_ADDRESS);
			}
		}
	}
		
	private void validateTextBoxLength(int textBoxLength, List<String> errors) {
		if(!isTextBoxLengthValid(textBoxLength)) {
			errors.add(EformValidationConstants.EFORM_SIZE_TEXTBOX_LENGTH);
		}
	}
	
	private void validateTextAreaHeight(int textAreaHeight, List<String> errors) {
		if(!isTextAreaHeightValid(textAreaHeight)) {
			errors.add(EformValidationConstants.EFORM_SIZE_TEXTAREA_HEIGHT);
		}
	}
	
	private void validateTextAreaWidth(int textAreaWidth, List<String> errors) {
		if(!isTextAreaWidthValid(textAreaWidth)) {
			errors.add(EformValidationConstants.EFORM_SIZE_TEXTAREA_WIDTH);
		}
	}
	
	private void validateAnswerType(AnswerType answerType, List<String> errors) {
		if(!isAnswerTypeValid(answerType)) {
			errors.add(EformValidationConstants.EFORM_ANSWER_TYPE);
		}
	}
	
	private void validateRangeValue2(String rangeValue2, List<String> errors) {
		if(rangeValue2 != null) {
			if(!isRangeValue2MaxCharValid(rangeValue2)) {
				errors.add(EformValidationConstants.EFORM_SIZE_RANGE_VALUE2);
			}
		}
	}
	
	private void validateRangeValue1(String rangeValue1, List<String> errors) {
		if(rangeValue1 != null) {
			if(!isRangeValue1MaxCharValid(rangeValue1)) {
				errors.add(EformValidationConstants.EFORM_SIZE_RANGE_VALUE1);
			}
		}
	}
	
	private void validateRangeOperator(String rangeOperator, List<String> errors) {
		if(rangeOperator != null) {
			if(!isRangeOperatorMaxCharValid(rangeOperator)) {
				errors.add(EformValidationConstants.EFORM_SIZE_RANGE_OPERATOR);
			}
		}
	}
	
	private void validateQuestionFontSize(String fontSize, List<String> errors) {
		if(!isQuestionFontSizeMaxCharValid(fontSize)) {
			errors.add(EformValidationConstants.EFORM_SIZE_FONT_SIZE);
		}
	}
		
	private void validateQuestionFontFace(String fontFace, List<String> errors) {
		boolean isValid = true;
		isValid = isNotNull(fontFace);
		if(isValid) {
			isValid = isQuestionFontFaceMaxCharValid(fontFace);
			if(!isValid) {
				errors.add(EformValidationConstants.EFORM_SIZE_FONT_FACE);
			}
		}else {
			errors.add(EformValidationConstants.EFORM_NULL_FONT_FACE);
		}	
	}
	
	private void validateQuestionTextColor(String textColor, List<String> errors) {
		boolean isValid = true;
		isValid = isNotNull(textColor);
		if(isValid) {
			isValid = isQuestionTextColorMaxCharValid(textColor);
			if(!isValid) {
				errors.add(EformValidationConstants.EFORM_SIZE_TEXT_COLOR);
			}
		}else {
			errors.add(EformValidationConstants.EFORM_NULL_TEXT_COLOR);
		}	
	}
		
	private void validateVerticalAlign(String vAlign, List<String> errors) {
		boolean isValid = true;
		isValid = isNotNull(vAlign);
		if(isValid) {
			isValid = isVerticalAlignMaxCharValid(vAlign);
			if(!isValid) {
				errors.add(EformValidationConstants.EFORM_SIZE_VERTICAL_ALIGN);
			}
		}else {
			errors.add(EformValidationConstants.EFORM_NULL_VERTICAL_ALIGN);
		}	
	}
	
	private void validateHeightAlign(String hAlign, List<String> errors) {
		boolean isValid = true;
		isValid = isNotNull(hAlign);
		if(isValid) {
			isValid = isHeightAlignMaxCharValid(hAlign);
			if(!isValid) {
				errors.add(EformValidationConstants.EFORM_SIZE_HEIGHT_ALIGN);
			}
		}else {
			errors.add(EformValidationConstants.EFORM_NULL_HEIGHT_ALIGN);
		}	
	}
		
	public void validateSkipRuleOperatorType(SkipRuleOperatorType skipRuleOperatorType, List<String> errors) {
		if(!isSkipRuleOperatorTypeValid(skipRuleOperatorType)) {
			errors.add(EformValidationConstants.EFORM_SKIP_RULE_OPERATOR_TYPE);
		}
	}
	
	public void validateSkipRuleType(SkipRuleType skipRuleType, List<String> errors) {
		if(!isSkipRuleTypeValid(skipRuleType)) {
			errors.add(EformValidationConstants.EFORM_SKIP_RULE_TYPE);
		}
	}
	
	public void validateSkipRuleEquals(String skipRuleEquals, List<String> errors) {
		if(!isSkipRuleEqualsMaxCharValid(skipRuleEquals)) {
			errors.add(EformValidationConstants.EFORM_SIZE_SKIP_RULE_EQUALS);
		}
	}
	
	private void validateIsLegacy(boolean isLegacy, List<String> errors) {
		if(isLegacy) {
			errors.add(EformValidationConstants.EFORM_IS_LEGACY);
		}
	}
	
	private void validateIsShared(boolean isShared, List<String> errors) {
		if(isShared) {
			errors.add(EformValidationConstants.EFORM_IS_SHARED);
		}
	}
	
	private void validateConversionFactor(String conversionFactor, List<String> errors) {
		if(conversionFactor != null) {
			if(!isConversionFactorMaxCharValid(conversionFactor)) {
				errors.add(EformValidationConstants.EFORM_SIZE_CONVERSION_FACTOR);
			}
		}
	}
	
	private void validatePrepopulationValue(String prepopulationValue, List<String> errors) {
		if(prepopulationValue != null) {
			if(!isPrepopulationValueNameMaxCharValid(prepopulationValue)) {
				errors.add(EformValidationConstants.EFORM_SIZE_PREPOPULATION_VALUE);
			}
		}
	}
	
	private void validateQuestionAttrubutesGroupName(String groupName, List<String> errors) {
		if(groupName != null) {
			if(!isQuestionAttrubutesGroupNameMaxCharValid(groupName)) {
				errors.add(EformValidationConstants.EFORM_SIZE_GROUP_NAME);
			}
		}
	}
	
	private void validateDataElementName(String dataElementName, List<String> errors) {
		if(dataElementName != null) {
			if(!isDataElementNameMaxCharValid(dataElementName)) {
				errors.add(EformValidationConstants.EFORM_SIZE_DATA_ELEMENT_NAME);
			}
		}
	}
		
	private void validateCalculation(String calculation, List<String> errors) {
		if(calculation != null) {
			if(!isCalculationMaxCharValid(calculation)) {
				errors.add(EformValidationConstants.EFORM_SIZE_CALCULATION);
			}
		}
	}
	
	private void validateQuestionAnswerOptionSubmittedValue(String submittedValue, List<String> errors) {
		if(submittedValue != null) {
			if(!isQuestionAnswerOptionSubmittedValueMaxCharValid(submittedValue)) {
				errors.add(EformValidationConstants.EFORM_SIZE_QUESTION_ANSWER_OPTION_SUBMITTED_VALUE);
			}
		}
	}
	
	private void validateQuestionAnswerOptionDisplay(String display, List<String> errors) {
		if(display != null) {
			if(!isQuestionAnswerOptionDisplayMaxCharValid(display)) {
				errors.add(EformValidationConstants.EFORM_SIZE_QUESTION_ANSWER_OPTION_DISPLAY);
			}
		}
	}
		
    private void validateQuestionAnswerOptionDisplay(double score, List<String> errors) {
		if(!isQuestionAnswerOptionScoreNotNull(score)) {
			errors.add(EformValidationConstants.EFORM_REQUIRED_QUESTION_ANSWER_OPTION_SCORE);
		}
	}
	
	private void validateDescriptionDown(String descriptionDown, List<String> errors) {
		if(descriptionDown != null) {
			if(!isDescriptionDownMaxCharValid(descriptionDown)) {
				errors.add(EformValidationConstants.EFORM_SIZE_QUESTION_DESCRIPTION_DOWN);
			}
		}
	}
		
	private void validateDescriptionUp(String descriptionUp, List<String> errors) {
		if(descriptionUp != null) {
			if(!isDescriptionUpMaxCharValid(descriptionUp)) {
				errors.add(EformValidationConstants.EFORM_SIZE_QUESTION_DESCRIPTION_UP);
			}
		}
	}
	
	private void validateUnansweredValue(String unansweredValue, List<String> errors) {
		if(unansweredValue != null) {
			if(!isUnansweredValueMaxCharValid(unansweredValue)) {
				errors.add(EformValidationConstants.EFORM_SIZE_UNANSWERED_VALUE);
			}
		}
	}
		
	private void validateDefaultValue(String defaultValue, List<String> errors) {
		if(defaultValue != null) {
			if(!isDefaultValuetMaxCharValid(defaultValue)) {
				errors.add(EformValidationConstants.EFORM_SIZE_DEFAULT_VALUE);
			}
		}
	}
	
	private void validateQuestionText(String questionText, List<String> errors) {
		boolean isValid = true;
		isValid = isNotNull(questionText);
		if(isValid) {
			isValid = isQuestionTextMaxCharValid(questionText);
			if(!isValid) {
				errors.add(EformValidationConstants.EFORM_SIZE_QUESTION_TEXT);
			}
		}else {
			errors.add(EformValidationConstants.EFORM_NULL_QUESTION_TEXT);
		}	
	}
	
	private void validateQuestionName(String questionName, List<String> errors) {
		boolean isValid = true;
		isValid = isNotNull(questionName);
		if(isValid) {
			isValid = isQuestionNameMaxCharValid(questionName);
			if(!isValid) {
				errors.add(EformValidationConstants.EFORM_SIZE_QUESTION_NAME);
			}
		}else {
			errors.add(EformValidationConstants.EFORM_NULL_QUESTION_NAME);
		}	
	}
	
	private void validateVisualScale(VisualScale eformVisualScale, List<String> errors) {
		if(!isNotNull(eformVisualScale.getWidthMM())) {
			errors.add(EformValidationConstants.EFORM_NULL_VISUAL_SCALE_WIDTH);
		}
		boolean isMinValid = isNotNull(eformVisualScale.getStartRange());
		if(!isMinValid) {
			errors.add(EformValidationConstants.EFORM_NULL_VISUAL_SCALE_MIN);
		}
		boolean isMaxValid = isNotNull(eformVisualScale.getEndRange());
		if(!isMaxValid) {
			errors.add(EformValidationConstants.EFORM_NULL_VISUAL_SCALE_MAX);
		}
		boolean isValid = true;
		if(isMinValid && isMaxValid) {
			isValid = isVisualScaleMinMaxValid(eformVisualScale.getStartRange(),eformVisualScale.getEndRange());
			if(!isValid) {
				errors.add(EformValidationConstants.EFORM_SIZE_VISUAL_SCALE_MINMAX_RANGE);
			}
		}
		if(eformVisualScale.getLeftText() != null) {
			isValid = isVisualScaleLeftTextMaxCharValid(eformVisualScale.getLeftText());
			if(!isValid) {
				errors.add(EformValidationConstants.EFORM_SIZE_VISUAL_SCALE_LEFT_TEXT);
			}
		}
		if(eformVisualScale.getRightText() != null) {
			isValid = isVisualScaleRightTextMaxCharValid(eformVisualScale.getRightText());
			if(!isValid) {
				errors.add(EformValidationConstants.EFORM_SIZE_VISUAL_SCALE_RIGHT_TEXT);
			}
		}
		if(eformVisualScale.getCenterText() != null) {
			isValid = isVisualScaleCenterTextMaxCharValid(eformVisualScale.getCenterText());
			if(!isValid) {
				errors.add(EformValidationConstants.EFORM_SIZE_VISUAL_SCALE_CENTER_TEXT);
			}
		}
	}
		
	private void validateSectionGroupName(String groupName, List<String> errors) {
		if(groupName != null) {
			if(!isSectionGroupNameMaxCharValid(groupName)) {
				errors.add(EformValidationConstants.EFORM_SIZE_SECTION_GROUP_NAME);
			}	
		}
	}
		
	private void validateRepeatableMinMax(Integer initialRepeatedSections, Integer maxRepeatedSections, List<String> errors) {
		boolean isInitValid = isNotNull(initialRepeatedSections);
		if(!isInitValid) {
			errors.add(EformValidationConstants.EFORM_NULL_INITIAL_REPEATED_SECTIONS);
		}
		boolean isMaxValid = isNotNull(initialRepeatedSections);
		if(!isMaxValid) {
			errors.add(EformValidationConstants.EFORM_NULL_MAX_REPEATED_SECTIONS);
		}
		if(isInitValid && isMaxValid) {
			boolean isValid = true;
			isValid = isRepeatableMinMaxValid(initialRepeatedSections,maxRepeatedSections);
			if(!isValid) {
				errors.add(EformValidationConstants.EFORM_SIZE_INIT_MAX_REPEATED_SECTIONS_RANGE);
			}
			isValid = isRepeatableMaxValid(maxRepeatedSections);
			if(!isValid) {
				errors.add(EformValidationConstants.EFORM_SIZE_MAX_REPEATED_SECTIONS);
			}
		}
	}
	
	private void validateSectionFormCol(Integer formCol, List<String> errors) {
		if(!isNotNull(formCol)) {
			errors.add(EformValidationConstants.EFORM_NULL_SECTION_FORM_COL);
		}
	}
		
	private void validateSectionFormRow(Integer formRow, List<String> errors) {
		if(!isNotNull(formRow)) {
			errors.add(EformValidationConstants.EFORM_NULL_SECTION_FORM_ROW);
		}
	}
		
	private void validateSectionDescription(String sectionDescription, List<String> errors) {
		if(!isSectionDescriptionMaxCharValid(sectionDescription)) {
			errors.add(EformValidationConstants.EFORM_SIZE_SECTION_DESCRIPTION);
		}	
	}
		
	private void validateSectionName(String sectionName, List<String> errors) {
		if(isNotNull(sectionName)) {
			if(!isSectionNameMaxCharValid(sectionName)) {
				errors.add(EformValidationConstants.EFORM_SIZE_SECTION_NAME);
			}	
		}else {
			errors.add(EformValidationConstants.EFORM_NULL_SECTION_NAME);
		}	
	}
		
	private void validateFormStructureName(String formStructureName, List<String> errors) {
		if(isNotNull(formStructureName)) {
			if(!isFormStructureNameMaxCharValid(formStructureName)) {
				errors.add(EformValidationConstants.EFORM_SIZE_FORM_STRUCTURE);
			}	
		}else {
			errors.add(EformValidationConstants.EFORM_NULL_FORM_STRUCTURE);
		}	
	}
		
	private void validateCellPadding(int cellPadding, List<String> errors) {
		if(!isCellPaddingSizeMinMaxValid(cellPadding)) {
			errors.add(EformValidationConstants.EFORM_SIZE_CELL_PADDING);
		}
	}
		
	private void validateFormFontSize(int formFontSize, List<String> errors) {
		if(!isFormFontSizeMinMaxValid(formFontSize)) {
			errors.add(EformValidationConstants.EFORM_SIZE_FONT);
		}
		
	}
		
	private void validateFooter(String footer, List<String> errors) {
		if(!isFooterMaxCharValid(footer)) {
			errors.add(EformValidationConstants.EFORM_SIZE_FORM_FOOTER);
		}
	}
		
	private void validateHeader(String header, List<String> errors) {
		if(!isHeaderMaxCharValid(header)) {
			errors.add(EformValidationConstants.EFORM_SIZE_FORM_HEADER);
		}
	}
		
	private void validateSectionNameColor(String sectionNameColor, List<String> errors) {
		if(!isNotNull(sectionNameColor)) {
			errors.add(EformValidationConstants.EFORM_NULL_SECTION_NAME_COLOR);
		}
	}	
	
	private void validateSectionNameFont(String sectionNameFont, List<String> errors) {
		if(!isNotNull(sectionNameFont)) {
			errors.add(EformValidationConstants.EFORM_NULL_SECTION_NAME_FONT);
		}
	}
	
	private void validateFormNameColor(String formNameColor, List<String> errors) {
		if(!isNotNull(formNameColor)) {
			errors.add(EformValidationConstants.EFORM_NULL_FORM_NAME_COLOR);
		}
	}
	
	private void validateFormNameFont(String formNameFont, List<String> errors) {
		if(!isNotNull(formNameFont)) {
			errors.add(EformValidationConstants.EFORM_NULL_FORM_NAME_FONT);
		}
	}
	
	private void validateDescription(String description, List<String> errors) {
		if(!isDescriptionMaxCharValid(description)) {
			errors.add(EformValidationConstants.EFORM_SIZE_DESCRIPTION);
		}
	}
	
	private void validateTitle(String title, List<String> errors) {
		if(isNotNull(title)) {
			if(!isTitleMaxCharValid(title)) {
				errors.add(EformValidationConstants.EFORM_SIZE_TITLE);
			}
		} else {
			errors.add(EformValidationConstants.EFORM_NULL_TITLE);
		}
	}
	
	private void validateShortName(String shortName, List<String> errors) {
		if(isNotNull(shortName)) {
			//uniquess check will happen in businsess rules validation
			if(!doesEformShortNameMatchPattern(shortName)) {
				errors.add(EformValidationConstants.EFORM_PATTERN_SHORT_NAME);
			}
			if(!isShortNameMaxCharValid(shortName)) {
				errors.add(EformValidationConstants.EFORM_SIZE_SHORT_NAME);
			}
		} else {
			errors.add(EformValidationConstants.EFORM_NULL_SHORT_NAME);
		}
	}
	
	private boolean isNotNull(Object field) {
		if(field instanceof String) {
			if(field == null  || field.equals("")) {
				return false;
			}else {
				return true;
			}
		}else {
			return field != null;
		}
		
	}
	
	private boolean doesEformShortNameMatchPattern(String shortName) {
		Pattern pattern = Pattern.compile("^[A-Za-z][A-Za-z0-9_]*$");
		if (pattern.matcher(shortName).matches()) {
			return true;
		} else {
			return false;
		}	
	}
	
	private boolean isShortNameMaxCharValid(String shortName) {
		int maxLength = 50;
		if(shortName.length() <= maxLength) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isTitleMaxCharValid(String title) {
		int maxLength = 100;
		if(title.length() <= maxLength) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isDescriptionMaxCharValid(String description) {
		int maxLength = 1000;
		if(description.length() <= maxLength) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isHeaderMaxCharValid(String header) {
		if(header != null) {
			int maxLength = 500;
			if(header.length() <= maxLength) {
				return true;
			} else {
				return false;
			}
		}else {
			return true;
		}
	}
	
	private boolean isFooterMaxCharValid(String footer) {
		if(footer != null) {
			int maxLength = 500;
			if(footer.length() <= maxLength) {
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}
	
	private boolean isFormFontSizeMinMaxValid(int fontSize) {
		int formFontSizeMin = 8;
		int formFontSizeMax = 18;
		if(fontSize >= formFontSizeMin && fontSize <= formFontSizeMax) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isCellPaddingSizeMinMaxValid(int cellPadding) {
		int cellPaddingSizeMin = 1;
		int cellPaddingSizeMax = 20;
		if(cellPadding >= cellPaddingSizeMin && cellPadding <= cellPaddingSizeMax) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isFormStructureNameMaxCharValid(String formStructureName) {
		int maxLength = 50;
		if(formStructureName.length() <= maxLength) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isSectionNameMaxCharValid(String sectionName) {
		int maxLength = 128;
		if(sectionName.length() <= maxLength) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isSectionDescriptionMaxCharValid(String sectionDescription) {
		int maxLength = 4000;
		if(sectionDescription.length() <= maxLength) {
			return true;
		} else {
			return false;
		}
	}

	private boolean isRepeatableMinMaxValid(int initRepeatableNum, int maxRepeatableNum) {
		return initRepeatableNum <= maxRepeatableNum;
	}
	
	private boolean isRepeatableMaxValid(int maxRepeatableNum) {
		int maxRepeatables = 45;
		if(maxRepeatableNum <= maxRepeatables) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isSectionGroupNameMaxCharValid(String groupName) {
		int maxLength = 75;
		if(groupName.length() <= maxLength) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isVisualScaleMinMaxValid(int visualScaleMin, int visualScaleMax) {
		return visualScaleMin < visualScaleMax;
	}	
	
	private boolean isVisualScaleLeftTextMaxCharValid(String leftText) {
		int maxLength = 255;
		if(leftText.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isVisualScaleRightTextMaxCharValid(String rightText) {
		int maxLength = 255;
		if(rightText.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isVisualScaleCenterTextMaxCharValid(String centerText) {
		int maxLength = 2000;
		if(centerText.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isQuestionNameMaxCharValid(String questionName) {
		int maxLength = 40;
		if(questionName.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isQuestionTextMaxCharValid(String questionText) {
		int maxLength = 4000;
		if(questionText.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isDefaultValuetMaxCharValid(String defaultValue) {
		int maxLength = 4000;
		if(defaultValue.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isUnansweredValueMaxCharValid(String unansweredValue) {
		int maxLength = 4000;
		if(unansweredValue.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isDescriptionUpMaxCharValid(String descriptionUp) {
		int maxLength = 4000;
		if(descriptionUp.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isDescriptionDownMaxCharValid(String descriptionDown) {
		int maxLength = 4000;
		if(descriptionDown.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isQuestionAnswerOptionDisplayMaxCharValid(String display) {
		int maxLength = 400;
		if(display.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isQuestionAnswerOptionSubmittedValueMaxCharValid(String submittedValue) {
		int maxLength = 400;
		if(submittedValue.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isCalculationMaxCharValid(String calculation) {
		int maxLength = 4000;
		if(calculation.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
   private boolean isQuestionAnswerOptionScoreNotNull(double score) {
		return score != Integer.MIN_VALUE;
	}
	
	private boolean isDataElementNameMaxCharValid(String dataElementName) {
		int maxLength = 50;
		if(dataElementName.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isQuestionAttrubutesGroupNameMaxCharValid(String dataElementName) {
		int maxLength = 75;
		if(dataElementName.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isPrepopulationValueNameMaxCharValid(String prepopulationValue) {
		int maxLength = 50;
		if(prepopulationValue.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isConversionFactorMaxCharValid(String conversionFactor) {
		int maxLength = 4000;
		if(conversionFactor.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isSkipRuleEqualsMaxCharValid(String skipRuleEquals) {
		int maxLength = 50;
		if(skipRuleEquals.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isSkipRuleTypeValid(SkipRuleType skipRuleType) {
		return skipRuleType != null;
	}
	
	private boolean isSkipRuleOperatorTypeValid(SkipRuleOperatorType skipRuleOperatorType) {
		return skipRuleOperatorType != null;
	}
	
	private boolean isHeightAlignMaxCharValid(String heightAlign) {
		int maxLength = 20;
		if(heightAlign.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isVerticalAlignMaxCharValid(String verticalAlign) {
		int maxLength = 20;
		if(verticalAlign.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isQuestionTextColorMaxCharValid(String textColor) {
		int maxLength = 20;
		if(textColor.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isQuestionFontFaceMaxCharValid(String fontFace) {
		int maxLength = 20;
		if(fontFace.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isQuestionFontSizeMaxCharValid(String fontSize) {
		int maxLength = 3;
		if(fontSize.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isRangeOperatorMaxCharValid(String rangeOperator) {
		int maxLength = 4000;
		if(rangeOperator.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isRangeValue1MaxCharValid(String rangeValue1) {
		int maxLength = 30;
		if(rangeValue1.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isRangeValue2MaxCharValid(String rangeValue2) {
		int maxLength = 30;
		if(rangeValue2.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isStatusValid(String status) {
		if(!status.trim().equalsIgnoreCase("DRAFT")){
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isAnswerTypeValid(AnswerType answerType) {
		return answerType != null;
	}
	
	private boolean isTextAreaWidthValid(int textAreaWidth) {
		return textAreaWidth <= 100;
	}
	
	private boolean isTextAreaHeightValid(int textAreaHeight) {
		return textAreaHeight <= 60;
	}
	
	private boolean isTextBoxLengthValid(int textBoxLength) {
		return textBoxLength <= 20;
	}
	
	private boolean isToEmailAddressMaxCharValid(String toEmailAddress) {
		int maxLength = 255;
		if(toEmailAddress.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isCcEmailAddressMaxCharValid(String ccEmailAddress) {
		int maxLength = 255;
		if(ccEmailAddress.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isSubjectMaxCharValid(String subject) {
		int maxLength = 255;
		if(subject.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isBodyMaxCharValid(String body) {
		int maxLength = 4000;
		if(body.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isXhtmlMaxCharValid(String xhtml) {
		int maxLength = 4000;
		if(xhtml.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isTableHeaderTypeValid(int tableHeader) {
		return tableHeader == 0 || tableHeader == 1;
	}
	
	private boolean isEmailTriggerAnswerMaxCharValid(String answer) {
		int maxLength = 255;
		if(answer.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}
	
	private boolean isQuestionImageFileNameMaxCharValid(String fileName) {
		int maxLength = 300;
		if(fileName.length() <= maxLength) {
			return true;
		}else {
			return false;
		}
	}	
}

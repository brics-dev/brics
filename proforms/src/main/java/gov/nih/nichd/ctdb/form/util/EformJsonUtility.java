package gov.nih.nichd.ctdb.form.util;

import java.util.List;
import java.util.ListIterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.HtmlAttributes;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.emailtrigger.domain.EmailTrigger;
import gov.nih.nichd.ctdb.emailtrigger.domain.EmailTriggerValue;
import gov.nih.nichd.ctdb.form.common.FormConstants;
import gov.nih.nichd.ctdb.form.common.FormHtmlAttributes;
import gov.nih.nichd.ctdb.form.domain.CalculatedFormQuestionAttributes;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.domain.FormQuestionAttributes;
import gov.nih.nichd.ctdb.form.domain.Section;
import gov.nih.nichd.ctdb.question.domain.Answer;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.domain.VisualScale;
import gov.nih.nichd.ctdb.question.manager.QuestionManager;
import gov.nih.nichd.ctdb.security.common.UserNotFoundException;
import gov.nih.nichd.ctdb.security.manager.SecurityManager;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.StatusType;

public class EformJsonUtility {
	private int nextQuestId;
	
	/**
	 * Default EformJsonUtility constructor.
	 * 
	 * @throws CtdbException If the question ID generation value cannot be generated.
	 */
	public EformJsonUtility() throws CtdbException {
		QuestionManager qm = new QuestionManager();
		nextQuestId = qm.getNextQuestionId();
	}
	
	/**
	 * Converts the given ProFoRMS Form object to a JSON object that can be used with
	 * the eForm Dictionary web services.
	 * 
	 * @param form - The ProFoRMS Form object to convert.
	 * @return A Dictionary eForm compatible JSON.
	 * @throws UserNotFoundException When the user/owner could not be found in the database.
	 * @throws CtdbException When there is a database error.
	 * @throws JSONException When there is an error while constructing the JSON object.
	 */
	public JSONObject formToEformJson(Form form) throws UserNotFoundException, CtdbException, JSONException {
		JSONObject eFormJson = new JSONObject();
		JSONArray sectionsJSONArr = new JSONArray();
		JSONArray questionsJSONArr = new JSONArray();
		
		// Convert form details to JSON.
		formDetailsToJson(eFormJson, form);
		
		// Convert the form sections and questions to JSON.
		for ( List<Section> row : form.getRowList() ) {
			for ( Section section : row ) {
				if ( section != null ) {
					sectionsJSONArr.put(sectionToJSON(section));
					
					for ( Question q : section.getQuestionList() ) {
						int oldQId = -1;
						
						// Check if the question IDs need to be reset.
						if ( !section.isRepeatable() ) {
							oldQId = q.getId();
							q.setId(nextQuestId);
							nextQuestId++;
						}
						
						questionsJSONArr.put(questionToJSON(q, section.getId(), oldQId));
					}
				}
			}
		}
		
		// Apply the question and section arrays to the eForm JSON.
		eFormJson.put("sectionsJSON", sectionsJSONArr);
		eFormJson.put("questionsJSON", questionsJSONArr);
		
		return eFormJson;
	}
	
	/**
	 * Converts the data from the given Form object to JSON. The names of the JSON
	 * attributes are based off of the ones used for eForms in Dictionary.
	 * 
	 * @param eFormJson - The JSON object which will receive the form data.
	 * @param form - The Form object which data will be read from.
	 * @throws JSONException When there is an error while setting data into the JSON object.
	 * @throws UserNotFoundException When the user who created the form could not be found in
	 * the database.
	 * @throws CtdbException When there is any other database errors while getting the user.
	 */
	private void formDetailsToJson(JSONObject eFormJson, Form form) throws JSONException, UserNotFoundException, CtdbException {
		eFormJson.put("id", -1);
		eFormJson.put("title", form.getName());
		
		// Set the form structure name.
		if ( Utils.isBlank(form.getDataStructureName()) ) {
			String shortName = form.getName().replace(" ", "_");
			
			// Ensure that the new short name is 48 characters or below.
			if ( shortName.length() > 48 ) {
				shortName = shortName.substring(0, 47);
			}
			
			eFormJson.put("shortName", shortName);
		}
		else {
			eFormJson.put("shortName", form.getDataStructureName());
		}
		
		eFormJson.put("description", form.getDescription());
		eFormJson.put("isLegacy", form.isLegacy());
		//added by Ching-Heng
		eFormJson.put("isCAT", form.isCAT());
		eFormJson.put("measurementType", form.getMeasurementType());
		eFormJson.put("status", StatusType.PUBLISHED.getType());
		eFormJson.put("allowMultipleCollectionInstances", form.isAllowMultipleCollectionInstances());
		eFormJson.put("enableDataSpring", form.isDataSpring());
		eFormJson.put("footer", form.getFormFooter());
		eFormJson.put("header", form.getFormHeader());
		eFormJson.put("orderVal", form.getOrderValue());
		
		// Set created by (owner).
		SecurityManager sm = new SecurityManager();
		
		eFormJson.put("createBy", sm.getUser(form.getCreatedBy()).getUsername());
		eFormJson.put("createDate", BRICSTimeDateUtil.formatTimeStamp(form.getCreatedDate()));
		
		// Set form HTML attributes.
		FormHtmlAttributes formHtmlAttrs = form.getFormHtmlAttributes();
		
		eFormJson.put("sectionNameColor", formHtmlAttrs.getSectionColor());
		eFormJson.put("sectionNameFont", formHtmlAttrs.getSectionFont());
		eFormJson.put("sectionBorder", formHtmlAttrs.getSectionBorder());
		eFormJson.put("cellPadding", formHtmlAttrs.getCellpadding());
		eFormJson.put("formNameColor", formHtmlAttrs.getFormColor());
		eFormJson.put("formNameFont", formHtmlAttrs.getFormFont());
		eFormJson.put("fontSize", formHtmlAttrs.getFormFontSize());
		eFormJson.put("formBorder", formHtmlAttrs.getFormBorder());
	}
	
	/**
	 * Converts the given Section object to its JSON counterpart. The names of the JSON
	 * attributes are based off of the ones used for eForms in Dictionary.
	 * 
	 * @param sec - The Section object to convert.
	 * @return The JSON version of the passed in Section object.
	 * @throws JSONException When there is an error during the JSON conversion.
	 */
	private JSONObject sectionToJSON(Section sec) throws JSONException {
		JSONObject secJSON = new JSONObject();
		
		secJSON.put("id", sec.getId() * -1);
		secJSON.put("name", sec.getName());
		secJSON.put("description", sec.getDescription());
		secJSON.put("isRepeatable", sec.isRepeatable());
		secJSON.put("isCollapsable", sec.isCollapsable());
		secJSON.put("initRepeatedSecs", sec.getInitRepeatedSections());
		secJSON.put("maxRepeatedSecs", sec.getMaxRepeatedSections());
		
		// Handle repeated section parent conversion
		int repeatedSectionParent = sec.getRepeatedSectionParent();
		
		if ( repeatedSectionParent != -1 ) {
			secJSON.put("repeatedSectionParent", String.valueOf(repeatedSectionParent * -1));
		}
		else {
			secJSON.put("repeatedSectionParent", "-1");
		}
		
		secJSON.put("repeatableGroupName", sec.getRepeatableGroupName());
		secJSON.put("row", sec.getRow());
		secJSON.put("col", sec.getCol());
		
		return secJSON;
	}
	
	/**
	 * Converts the given Question object to JSON. The names of the JSON attributes are based
	 * off of the ones used for eForms in Dictionary.
	 * 
	 * @param ques - The Question object to convert from.
	 * @param sectionId - The section ID where the question is associated with.
	 * @param oldQId - The old question ID if it was replaced.
	 * @return The JSON version of the passed in Question object.
	 * @throws JSONException When there is an error while constructing the JSON object.
	 * @throws CtdbException When there is a database error while looking up skip rule information.
	 */
	private JSONObject questionToJSON(Question ques, int sectionId, int oldQId) throws JSONException, CtdbException {
		JSONObject quesJSON = new JSONObject();
		
		quesJSON.put("questionId", ques.getId() * -1);
		
		if ( oldQId > 0 ) {
			quesJSON.put("oldQuestionId", oldQId);
		}
		
		quesJSON.put("questionName", ques.getName());
		quesJSON.put("questionText", ques.getText());
		quesJSON.put("sectionId", String.valueOf(sectionId * -1));
		quesJSON.put("newQuestionDivId", (sectionId * -1) + "_" + (ques.getId() * -1));
		quesJSON.put("questionType", ques.getType().getValue());
		quesJSON.put("defaultValue", ques.getDefaultValue());
		quesJSON.put("unansweredValue", ques.getUnansweredValue());
		quesJSON.put("descriptionUp", ques.getDescriptionUp());
		quesJSON.put("descriptionDown", ques.getDescriptionDown());
		quesJSON.put("includeOther", ques.isIncludeOtherOption());
		
		quesJSON.put("questionOrder", ques.getQuestionOrder());
		quesJSON.put("questionOrder_col", ques.getQuestionOrderCol());
		
		// Convert answer data.
		JSONArray questionOptionArray = new JSONArray();

		for ( ListIterator<Answer> li =  ques.getAnswers().listIterator(); li.hasNext(); ) {
			Answer a = li.next();
			JSONObject questionOption = new JSONObject();
			double score = a.getScore();

			questionOption.put("option", a.getDisplay());
			questionOption.put("score", score != Integer.MIN_VALUE ? String.valueOf(score) : "");
			questionOption.put("submittedValue", a.getSubmittedValue());
			questionOption.put("orderVal", li.previousIndex() + 1);

			questionOptionArray.put(questionOption);
		}

		quesJSON.put("questionOptionsObjectArray", questionOptionArray);
		
		// Convert question attributes to JSON.
		JSONObject qAttrJSON = questionAttributesToJSON(ques, sectionId);
		quesJSON.put("attributeObject", qAttrJSON);
		
		return quesJSON;
	}
	
	/**
	 * Converts the question attributes from the given Question object to JSON. The names of the JSON
	 * attributes are based off of the ones used for eForms in Dictionary.
	 * 
	 * @param ques - The Question object where the attributes data will be read from.
	 * @param sectionId - The section ID that is associated with the given Question object.
	 * @return The JSON version of the given question's attributes.
	 * @throws JSONException When there is an error while constructing the JSON object.
	 * @throws CtdbException When there is a database error while getting skip rule data.
	 */
	private JSONObject questionAttributesToJSON(Question ques, int sectionId) throws JSONException, CtdbException {
		JSONObject attrJson = new JSONObject();
		FormQuestionAttributes qAttrs = ques.getFormQuestionAttributes();
		
		// Add SectionQuestion and QuestionAttribrute ID place holders.
		attrJson.put("qaId", -1);
		attrJson.put("sqId", -1);
		
		// Handle visual scale attributes.
		if ( ques instanceof VisualScale ) {
			visualScaleQuesToJSON(attrJson, (VisualScale) ques);
		}
		
		// Convert question attribute data.
		attrJson.put("answerType", qAttrs.getAnswerType().getValue());
		attrJson.put("qType", ques.getType().getValue());
		
		String deName = qAttrs.getDataElementName();
		attrJson.put("dataElementName", !deName.equals("none") ? deName : "");
		attrJson.put("prepopulationValue", qAttrs.getPrepopulationValue());
		attrJson.put("decimalPrecision", String.valueOf(qAttrs.getDecimalPrecision()));
		attrJson.put("tableHeaderType", qAttrs.getTableHeaderType());
		attrJson.put("required", qAttrs.isRequired());
		attrJson.put("htmlText", qAttrs.getHtmlText());
		
		//// Handle conversion factor.
		attrJson.put("hasUnitConversionFactor", qAttrs.isHasUnitConversionFactor());
		attrJson.put("unitConversionFactor", qAttrs.getUnitConversionFactor());
		
		//// Handle calculation attributes.
		questionCalculationToJSON(attrJson, ques, sectionId, qAttrs);
		
		//// Handle pre-population.
		attrJson.put("prepopulation", qAttrs.isPrepopulation());
		attrJson.put("prepopulationValue", qAttrs.getPrepopulationValue());
		
		//// Handle HTML attributes.
		htmlAttributesToJSON(attrJson, qAttrs);
		
		//// Handle range data.
		attrJson.put("rangeOperator", qAttrs.getRangeOperator());
		attrJson.put("rangeValue1", qAttrs.getRangeValue1());
		attrJson.put("rangeValue2", qAttrs.getRangeValue2());
		
		//// Handle skip rule data conversion.
		skipRulesToJSON(attrJson, qAttrs, sectionId, ques.getId());
		
		//// Handle email trigger data conversion.
		emailTriggerToJSON(attrJson, qAttrs);
		
		return attrJson;
	}
	
	/**
	 * Adds visual scale attributes to the given JSON object. The names of the JSONattributes are based off 
	 * of the ones used for eForms in Dictionary.
	 * 
	 * @param attrJson - The question attributes JSON object to store the data.
	 * @param vs - The VisualScale object to read data from.
	 * @throws JSONException When an error occurs while adding data to the JSON object.
	 */
	private void visualScaleQuesToJSON(JSONObject attrJson, VisualScale vs) throws JSONException {
		attrJson.put("vsId", -1);
		attrJson.put("vscaleRangeStart", vs.getRangeStart());
		attrJson.put("vscaleRangeEnd", vs.getRangeEnd());
		attrJson.put("vscaleCenterText", vs.getCenterText());
		attrJson.put("vscaleRightText", vs.getRightText());
		attrJson.put("vscaleShowHandle", vs.isShowHandle());
		attrJson.put("vscaleWidth", vs.getWidth());
		attrJson.put("vscaleLeftText", vs.getLeftText());
	}
	
	/**
	 * Converts the question calculation data in the given Question object to JSON. The names of the JSON
	 * attributes are based off of the ones used for eForms in Dictionary.
	 * 
	 * @param attrJson - The JSON object where the calculation data will be saved to.
	 * @param ques - The Question object where the calculation data will be read from.
	 * @param sectionId - The section ID associated with the given question.
	 * @param qAttrs - The question attributes object where some of the calculation data will be read from.
	 * @throws JSONException When there is an error while adding calculation data to the given JSON object.
	 */
	private void questionCalculationToJSON(JSONObject attrJson, Question ques, int sectionId, FormQuestionAttributes qAttrs) throws JSONException {
		CalculatedFormQuestionAttributes calAttrs = ques.getCalculatedFormQuestionAttributes();
		
		// Set calculation type, if able.
		if ( calAttrs.getCalculationType() != null ) {
			attrJson.put("calculationType", calAttrs.getCalculationType().getValue());
		}
		
		// Set conversion factor, if able.
		if ( calAttrs.getConversionFactor() != null ) {
			attrJson.put("conversionFactor", calAttrs.getConversionFactor().getValue());
		}
		
		// Handle Calculation
		JSONArray questionToCalculateJson = new JSONArray();
		
		for ( Question calQues : calAttrs.getQuestionsToCalculate() ) {
			questionToCalculateJson.put(FormConstants.FORM_SECTION_PATTERN + (calQues.getSectionId() * -1) + 
					FormConstants.FORM_QUESTION_PATTERN + (calQues.getId() * -1));
		}
		
		attrJson.put("questionsToCalculate", questionToCalculateJson);
		attrJson.put("calculation", calAttrs.getCalculation());
		attrJson.put("calculatedQuestion", qAttrs.isCalculatedQuestion());
		attrJson.put("calDependent", ques.getHasCalDependent());
	}
	
	/**
	 * Converts the HTML attributes data from the given Question object to JSON. The names of the JSON
	 * attributes are based off of the ones used for eForms in Dictionary.
	 * 
	 * @param attrJson - The JSON object where the HTML attribute data will be saved to.
	 * @param qAttrs - The question attributes object where some of the HTML attributes data will
	 * be read from.
	 * @throws JSONException When there is an error while adding HTML attribute data to the given JSON object.
	 */
	private void htmlAttributesToJSON(JSONObject attrJson, FormQuestionAttributes qAttrs) throws JSONException {
		HtmlAttributes htmlAttrs = qAttrs.getHtmlAttributes();
		
		attrJson.put("vAlign", htmlAttrs.getvAlign());
		attrJson.put("align", htmlAttrs.getAlign());
		attrJson.put("color", htmlAttrs.getColor());
		attrJson.put("fontSize", htmlAttrs.getFontSize());
		attrJson.put("fontFace", htmlAttrs.getFontFace());
		attrJson.put("indent", htmlAttrs.getIndent());
		
		attrJson.put("minCharacters", qAttrs.getMinCharacters());
		attrJson.put("maxCharacters", qAttrs.getMaxCharacters());
		attrJson.put("horizontalDisplay", qAttrs.isHorizontalDisplay());
		attrJson.put("horizDisplayBreak", qAttrs.isHorizDisplayBreak());
		attrJson.put("textareaWidth", qAttrs.getTextareaWidth());
		attrJson.put("textareaHeight", qAttrs.getTextareaHeight());
		attrJson.put("textboxLength", qAttrs.getTextboxLength());
		attrJson.put("dataSpring", qAttrs.isDataSpring());
		attrJson.put("showText", qAttrs.isShowText());
	}
	
	/**
	 * Converts the skip rules data from the given question attributes object to JSON. The names of the JSON
	 * attributes are based off of the ones used for eForms in Dictionary.
	 * 
	 * @param attrJson - The JSON object where the skip rule data will be saved to.
	 * @param qAttrs - The question attributes object where the skip rule data will be read from.
	 * @param sectionId - The section ID that is associated with the question attributes.
	 * @param questionId - The question ID that is associated with the question attributes.
	 * @throws JSONException When there is an error while adding the skip rule data to the given JSON object.
	 * @throws CtdbException When there is a database error while query if the question is skip rule dependent.
	 */
	private void skipRulesToJSON(JSONObject attrJson, FormQuestionAttributes qAttrs, int sectionId, int questionId) throws JSONException, CtdbException {
		JSONArray questionsToSkipJsonArr = new JSONArray();
		
		// Set questions to skip data.
		for ( Question skipQues : qAttrs.getQuestionsToSkip() ) {
			questionsToSkipJsonArr.put(FormConstants.FORM_SECTION_PATTERN + (skipQues.getSkipSectionId() * -1) +
					FormConstants.FORM_QUESTION_PATTERN + (skipQues.getId() * -1));
		}
		
		attrJson.put("questionsToSkip", questionsToSkipJsonArr);
		
		// Set the rest of the skip rule data.
		if ( qAttrs.getSkipRuleType() != null ) {
			attrJson.put("skipRuleType", qAttrs.getSkipRuleType().getValue());
		}
		else {
			attrJson.put("skipRuleType", Integer.MIN_VALUE);
		}
		
		if ( qAttrs.getSkipRuleOperatorType() != null ) {
			attrJson.put("skipRuleOperatorType", qAttrs.getSkipRuleOperatorType().getValue());
		}
		else {
			attrJson.put("skipRuleOperatorType", Integer.MIN_VALUE);
		}
		
		attrJson.put("skipRuleEquals", qAttrs.getSkipRuleEquals());
		attrJson.put("skiprule", qAttrs.hasSkipRule());
	}
	
	/**
	 * Converts the email trigger data from the given question attributes object to JSON. The names of the
	 * JSON attributes are based off of the ones used for eForms in Dictionary.
	 * 
	 * @param attrJson - The JSON object where the email trigger data will be saved to.
	 * @param qAttrs - The question attributes object where the email data will be read from.
	 * @throws JSONException When there is an error while adding data to the given JSON object.
	 */
	private void emailTriggerToJSON(JSONObject attrJson, FormQuestionAttributes qAttrs) throws JSONException {
		EmailTrigger et = qAttrs.getEmailTrigger();
		
		attrJson.put("etId", -1);
		attrJson.put("toEmailAddress", et.getToEmailAddress());
		attrJson.put("ccEmailAddress", et.getCcEmailAddress());
		attrJson.put("body", et.getBody());
		attrJson.put("subject", et.getSubject());
		
		JSONArray triggerValuesJsonArr = new JSONArray();
		// List<EmailTriggerValue> emailTriggerValueList = new ArrayList<EmailTriggerValue>(et.getTriggerValues());
		for (EmailTriggerValue etv : et.getTriggerValues()) {
			JSONObject etValue = new JSONObject();
			etValue.put("etValId", etv.getId());
			etValue.put("etAnswer", etv.getAnswer());
			etValue.put("etCondition", etv.getTriggerCondition());
//			etValue.put("etCondOrdinalNum", (etv.getConditionOrdinal() == null ? -1 : etv.getConditionOrdinal()));
//			etValue.put("etConditionOperator", (etv.getOperator() == null ? "" : etv.getOperator().getName()));
//			etValue.put("etQuestion", (etv.getTriggerQuestion() == null ? "" : etv.getTriggerQuestion()));
//			etValue.put("etInputVal", (etv.getTriggerInputValue() == null ? "" : etv.getTriggerInputValue()));
//			etValue.put("etCondLogicalOperator", (etv.getLogicalOperator() == null ? "" : etv.getLogicalOperator()));
			triggerValuesJsonArr.put(etValue);
		}
		attrJson.put("triggerValues", triggerValuesJsonArr);
		
		// // Convert trigger values.
		// JSONArray triggerAnswersJsonArr = new JSONArray();
		//
		// for ( String ta : et.getTriggerAnswers() ) {
		// if ( ta != null ) {
		// JSONObject etAnswers = new JSONObject();
		//
		// etAnswers.put("etValId", -1);
		// etAnswers.put("etAnswer", ta);
		//
		// triggerAnswersJsonArr.put(etAnswers);
		// }
		// }
		//
		// attrJson.put("triggerAnswers", triggerAnswersJsonArr);
	}
}

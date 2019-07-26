package gov.nih.tbi.commons.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.commons.model.AnswerType;
import gov.nih.tbi.commons.model.QuestionType;
import gov.nih.tbi.commons.model.SkipRuleOperatorType;
import gov.nih.tbi.commons.model.SkipRuleType;
import gov.nih.tbi.dictionary.model.hibernate.eform.CalculationQuestion;
import gov.nih.tbi.dictionary.model.hibernate.eform.CalculationQuestionPk;
import gov.nih.tbi.dictionary.model.hibernate.eform.Eform;
import gov.nih.tbi.dictionary.model.hibernate.eform.EmailTrigger;
import gov.nih.tbi.dictionary.model.hibernate.eform.EmailTriggerValue;
import gov.nih.tbi.dictionary.model.hibernate.eform.Question;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionAnswerDataType;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionAnswerOption;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionAttribute;
import gov.nih.tbi.dictionary.model.hibernate.eform.Section;
import gov.nih.tbi.dictionary.model.hibernate.eform.SectionQuestion;
import gov.nih.tbi.dictionary.model.hibernate.eform.SkipRuleQuestion;
import gov.nih.tbi.dictionary.model.hibernate.eform.SkipRuleQuestionPk;
import gov.nih.tbi.dictionary.model.hibernate.eform.VisualScale;

public class FormBuilderQuestionJSONToEform {
	static Logger logger = Logger.getLogger(FormBuilderQuestionJSONToEform.class);

	private JSONArray questionsJSONArr;
	private Eform eform;
	private String formMode;


	private HashMap<Long, Section> sectionMap = new HashMap<Long, Section>();
	private HashMap<Long, Question> questionMap = new HashMap<Long, Question>();

	private static String SECTION_QUESTION_SEPERATOR = "_";

	public FormBuilderQuestionJSONToEform(Eform eform, HashMap<Long, Section> sectionMap, JSONArray questionsJSONArr, String formMode) {
		setSectionMap(sectionMap);
		setQuestionsJSONArr(questionsJSONArr);
		setEform(eform);
		this.setFormMode(formMode);
	}

	// the return is a map of sections
	// the string key is the section ID
	public Eform parseQuestionJSONToEform() {

		this.eform.getSectionList().clear();
		// create questions and associate them to the section they belong to
		if (questionsJSONArr.length() > 0) {
			for (int i = 0; i < questionsJSONArr.length(); i++) {
				createQuestion(questionsJSONArr.getJSONObject(i));
			}
		}
		addSectionMapToEform();
		// remove all psuedo IDs to persist to DB
		removeAllPsuedoIdsFromEform();

		return this.eform;
	}

	private void createQuestion(JSONObject questionJson) {

		Question question = new Question();
		JSONObject attributeObjJSON = questionJson.getJSONObject("attributeObject");
		question.setId(questionJson.getLong("questionId"));
		question.setName(questionJson.getString("questionName"));
		question.setText(questionJson.getString("questionText"));
		question.setType(QuestionType.getByValue(questionJson.getInt("questionType")));
		question.setDefaultValue(questionJson.getString("defaultValue"));
		question.setUnansweredValue(questionJson.getString("unansweredValue"));
		question.setDescriptionUp(questionJson.getString("descriptionUp"));
		question.setDescriptionDown(questionJson.getString("descriptionDown"));
		question.setIncludeOther(questionJson.getBoolean("includeOther"));
		question.setDisplayPV(questionJson.getBoolean("displayPV"));
		question.setVisualScale(createVisualScale(questionJson));
		question.setHtmltext(attributeObjJSON.optString("htmlText"));
		// added by Ching-Heng
		question.setCatOid(questionJson.getString("catOid"));
		question.setFormItemOid(questionJson.getString("formItemOid"));

		// associate the question to the question attribute
		QuestionAttribute qa = createQuestionAttribute(attributeObjJSON);
		question.setQuestionAttribute(qa);

		SectionQuestion sq = new SectionQuestion();
		sq.setId(attributeObjJSON.getLong("sqId") > 0 ? attributeObjJSON.getLong("sqId") : null);

		if(attributeObjJSON.optString("calculation") != null){
			sq.setCalculation(removePsuedoSectionIdsFromCalculation(attributeObjJSON.optString("calculation")));
		}
		
		Integer questionOrder = questionJson.getInt("questionOrder");
		Integer questionOrderColumn = questionJson.getInt("questionOrder_col");

		sq.setQuestionOrder(questionOrder);
		sq.setQuestionOrderColumn(questionOrderColumn);
		sq.setQuestion(question);

		// get the section from the section map and add it to the section question
											       
		String sectionID = questionJson.getString("sectionId").replace("S_", "");
		Section section = this.sectionMap.get(Long.parseLong(sectionID));
		sq.setSection(section);

		sq.setSkipRuleQuestion(createSkipRuleQuestionSet(sq, attributeObjJSON.getJSONArray("questionsToSkip")));
		sq.setCalculatedQuestion(createCalculationQuestionSet(sq, attributeObjJSON.getJSONArray("questionsToCalculate")));

		section.addToSectionQuestion(sq);
		question.setQuestionAnswerOption(createQuestionAnswerOption(questionJson));

		addQuestionToQuestionMap(question.getId(), question);

	}

	private Set<QuestionAnswerOption> createQuestionAnswerOption(JSONObject questionJson) {
		JSONArray questionOptionsObjectArray = questionJson.getJSONArray("questionOptionsObjectArray");
		JSONObject questionAttributesObject = questionJson.getJSONObject("attributeObject");

		if (questionOptionsObjectArray != null) {
			Set<QuestionAnswerOption> qaOptionsList = new HashSet<QuestionAnswerOption>();

			for (int i = 0; i < questionOptionsObjectArray.length(); i++) {
				JSONObject questionOption = questionOptionsObjectArray.getJSONObject(i);

				QuestionAnswerOption qaOption = new QuestionAnswerOption();
				String option = questionOption.optString("option");
				String score = questionOption.getString("score");
				String submittedValue = questionOption.optString("submittedValue");
				Long anwerTypeId = questionAttributesObject.getLong("answerType");
				qaOption.setQuestionAnswerDataType(QuestionAnswerDataType.questionAnswerDataTypeById(anwerTypeId));
				qaOption.setDisplay(option.trim());
				qaOption.setOrderVal(questionOption.getInt("orderVal"));

				if (score != null && !score.trim().equals("")) {
					qaOption.setScore(Double.parseDouble(score));
				}
				qaOption.setSubmittedValue(submittedValue);
				// added by Ching-Heng
				qaOption.setElementOid(questionOption.getString("elementOid"));
				qaOption.setItemResponseOid(questionOption.getString("itemResponseOid"));
				qaOptionsList.add(qaOption);
			}
			return qaOptionsList;
		} else {
			return null;
		}

	}

	private VisualScale createVisualScale(JSONObject questionJson) {

		JSONObject attributeObjJSON = questionJson.getJSONObject("attributeObject");

		VisualScale visualScale = new VisualScale();
		visualScale.setId(attributeObjJSON.optLong("vsId") > 0 ? attributeObjJSON.optLong("vsId") : null);
		visualScale.setStartRange(attributeObjJSON.optInt("vscaleRangeStart"));
		visualScale.setEndRange(attributeObjJSON.optInt("vscaleRangeEnd"));
		visualScale.setCenterText(attributeObjJSON.optString("vscaleCenterText"));
		visualScale.setRightText(attributeObjJSON.optString("vscaleRightText"));
		visualScale.setShowHandle(attributeObjJSON.optBoolean("vscaleShowHandle"));
		visualScale.setWidthMM(attributeObjJSON.optInt("vscaleWidth"));
		visualScale.setLeftText(attributeObjJSON.optString("vscaleLeftText"));

		return visualScale;
	}

	private List<SkipRuleQuestion> createSkipRuleQuestionSet(SectionQuestion sectionQuestion,
			JSONArray skipQuestionJSONArray) {

		if (skipQuestionJSONArray != null && skipQuestionJSONArray.length() > 0) {
			List<SkipRuleQuestion> skipRuleQuestionList = new ArrayList<SkipRuleQuestion>();
			for (int i = 0; i < skipQuestionJSONArray.length(); i++) {

				SkipRuleQuestionPk skipRuleQuestionPk = new SkipRuleQuestionPk();
				skipRuleQuestionPk.setQuestion(sectionQuestion.getQuestion());
				skipRuleQuestionPk.setSection(sectionQuestion.getSection());

				String skipRuleQuestionText = (String) skipQuestionJSONArray.get(i);
				SkipRuleQuestion skipRuleQuestion =
						addSkipRuleQuestionAndSectionToSkipRuleQuestion(skipRuleQuestionText, skipRuleQuestionPk);
				skipRuleQuestionList.add(skipRuleQuestion);
			}
			return skipRuleQuestionList;
		} else {
			return null;
		}
	}

	private SkipRuleQuestion addSkipRuleQuestionAndSectionToSkipRuleQuestion(String jsonSkipSectionAndQuestion,
			SkipRuleQuestionPk skipRuleQuestionPk) {
		SkipRuleQuestion skipRuleQuestion = new SkipRuleQuestion();

		// Ex: S_-3_Q_19563
		String[] skipRuleQuestionAndSectionId = sectionQuestionStringToStringArray(jsonSkipSectionAndQuestion);
		String skipRuleSectionId = skipRuleQuestionAndSectionId[1];
		String skipRuleQuestionId = skipRuleQuestionAndSectionId[3].replace("]", "");

		skipRuleQuestionPk.setSkipRuleSection(sectionMap.get(Long.parseLong(skipRuleSectionId)));
		skipRuleQuestionPk.setSkipRuleQuestion(getQuestionFormQuestionMap(Long.parseLong(skipRuleQuestionId)));

		skipRuleQuestion.setSkipRuleQuestionCompositePk(skipRuleQuestionPk);

		return skipRuleQuestion;
	}

	private List<CalculationQuestion> createCalculationQuestionSet(SectionQuestion sectionQuestion,
			JSONArray calculationQuestionJSONArray) {

		if (calculationQuestionJSONArray != null && calculationQuestionJSONArray.length() > 0) {
			List<CalculationQuestion> calculationQuestionList = new ArrayList<CalculationQuestion>();
			for (int i = 0; i < calculationQuestionJSONArray.length(); i++) {

				CalculationQuestionPk calculationQuestionPk = new CalculationQuestionPk();
				calculationQuestionPk.setQuestion(sectionQuestion.getQuestion());
				calculationQuestionPk.setSection(sectionQuestion.getSection());

				String calculationQuestionText = (String) calculationQuestionJSONArray.get(i);
				CalculationQuestion calculationQuestion =
						addCalculateQuestionAndSectionToCalculateQuestion(calculationQuestionText,
								calculationQuestionPk);
				// calculationQuestion.setSectionQuestion(sectionQuestion);
				calculationQuestionList.add(calculationQuestion);
			}
			return calculationQuestionList;
		} else {
			return null;
		}
	}

	private CalculationQuestion addCalculateQuestionAndSectionToCalculateQuestion(String jsonCalculationSectionAndQuestion, CalculationQuestionPk calculationQuestionPk) {

		CalculationQuestion calculationQuestion = new CalculationQuestion();

		// Ex: [S_-3_Q_19563]
		String[] calculationRuleQuestionAndSectionId = sectionQuestionStringToStringArray(jsonCalculationSectionAndQuestion);
		String calculationRuleSectionId = calculationRuleQuestionAndSectionId[1];
		String calculationRuleQuestionId = (calculationRuleQuestionAndSectionId[3]).replace("]", "");

		calculationQuestionPk.setCalculationSection(sectionMap.get(Long.parseLong(calculationRuleSectionId)));
		calculationQuestionPk.setCalculationQuestion(getQuestionFormQuestionMap(Long.parseLong(calculationRuleQuestionId)));

		calculationQuestion.setCalculationQuestionCompositePk(calculationQuestionPk);

		return calculationQuestion;
	}

	private Question getQuestionFormQuestionMap(Long questionId) {
		Question mapFromQuestion = questionMap.get(questionId);

		if (mapFromQuestion != null) {
			return mapFromQuestion;
		} else {
			Question emptyQuestionWithId = new Question(questionId);
			return emptyQuestionWithId;
		}
	}

	private String[] sectionQuestionStringToStringArray(String sectionQuestionText) {
		return sectionQuestionText.split(SECTION_QUESTION_SEPERATOR);
	}

	private EmailTrigger createEmailTrigger(JSONObject attributeObjJSON) {

		String emailAddress = attributeObjJSON.getString("toEmailAddress");

		if (emailAddress != null && !emailAddress.equals("")) {
			EmailTrigger et = new EmailTrigger();
			et.setId(attributeObjJSON.getLong("etId") > 0 ? attributeObjJSON.getLong("etId") : null);
			et.setBody(attributeObjJSON.getString("body"));
			et.setSubject(attributeObjJSON.getString("subject"));
			et.setCcEmailAddress(attributeObjJSON.optString("ccEmailAddress"));
			et.setToEmailAddress(attributeObjJSON.getString("toEmailAddress"));
			et.setTriggerValues(createEmailTriggerValue(attributeObjJSON.getJSONArray("triggerAnswers")));

			return et;
		}
		return null;
	}

	private Set<EmailTriggerValue> createEmailTriggerValue(JSONArray emailTriggerAnswersJSON) {

		Set<EmailTriggerValue> triggerValues = new LinkedHashSet<EmailTriggerValue>();
		if (emailTriggerAnswersJSON != null && emailTriggerAnswersJSON.length() > 0) {
			for (int i = 0; i < emailTriggerAnswersJSON.length(); i++) {
				JSONObject emailTriggerValueJson = emailTriggerAnswersJSON.getJSONObject(i);
				EmailTriggerValue etv = new EmailTriggerValue();
				etv.setAnswer(emailTriggerValueJson.getString("etAnswer"));
				etv.setId(emailTriggerValueJson.getLong("etValId") > 0 ? emailTriggerValueJson.getLong("etValId") : null);
				triggerValues.add(etv);
			}
		}

		return triggerValues;
	}

	/**
	 * Creates a QuestionAttribute object from the given JSON object.
	 * 
	 * @param attributeObjJSON - The attribute JSON array object that contains all the visual scale JSON.
	 * @return A QuestionAttribute constructed from the data of the given JSON object.
	 */
	private QuestionAttribute createQuestionAttribute(JSONObject attributeObjJSON) {
		QuestionAttribute qa = new QuestionAttribute();

		String geDotGrpName = attributeObjJSON.getString("dataElementName");
		String dataElementName = "none";
		String repeatableGroupName = "none";
		if(geDotGrpName.contains(".")){
			dataElementName = geDotGrpName.substring(geDotGrpName.indexOf(".") + 1, geDotGrpName.length());
			repeatableGroupName = geDotGrpName.substring(0, geDotGrpName.indexOf("."));
		} 
		if(!(geDotGrpName.contains("."))&&geDotGrpName !=null){
			dataElementName=geDotGrpName;
		}
		// if the qa id is negative set it to null
		qa.setId(attributeObjJSON.getLong("qaId") > 0 ? attributeObjJSON.getLong("qaId") : null);
		qa.setRequiredFlag(attributeObjJSON.getBoolean("required"));
		qa.setDataElementName(dataElementName);
		qa.setGroupName(repeatableGroupName);
		qa.setAnswerType(AnswerType.getByValue(attributeObjJSON.getInt("answerType")));
		qa.setPrepopulation(attributeObjJSON.getBoolean("prepopulation"));
		qa.setPrepopulationValue(attributeObjJSON.getString("prepopulationValue"));
		qa.setvAlign(attributeObjJSON.getString("vAlign"));
		qa.sethAlign(attributeObjJSON.getString("align"));
		qa.setTextColor(attributeObjJSON.getString("color"));
		qa.setFontFace(attributeObjJSON.getString("fontFace"));
		qa.setFontSize(attributeObjJSON.optString("fontSize"));
		qa.setIndent(attributeObjJSON.getInt("indent"));
		qa.setRangeOperator(attributeObjJSON.optString("rangeOperator")); // is not an int.
		qa.setRangeValue1(attributeObjJSON.optString("rangeValue1"));
		qa.setRangeValue2(attributeObjJSON.optString("rangeValue2"));
		qa.setDtConversionFactor(attributeObjJSON.optInt("conversionFactor"));
		qa.setMinCharacters(attributeObjJSON.getInt("minCharacters"));
		qa.setMaxCharacters(attributeObjJSON.getInt("maxCharacters"));
		qa.setHorizontalDisplay(attributeObjJSON.getBoolean("horizontalDisplay"));
		qa.setTextBoxHeight(attributeObjJSON.getInt("textareaHeight"));
		qa.setTextBoxWidth(attributeObjJSON.getInt("textareaWidth"));
		qa.setTextBoxLength(attributeObjJSON.getInt("textboxLength"));
		qa.setDataSpring(attributeObjJSON.optBoolean("dataSpring"));
		qa.setXhtmlText(attributeObjJSON.optString("htmlText"));
		qa.setHorizontalDisplayBreak(attributeObjJSON.getBoolean("horizDisplayBreak"));
		qa.setPrepopulation(attributeObjJSON.getBoolean("prepopulation"));
		qa.setPrepopulationValue(attributeObjJSON.getString("prepopulationValue"));
		qa.setDecimalPrecision(attributeObjJSON.getInt("decimalPrecision"));
		qa.setHasConversionFactor(attributeObjJSON.optBoolean("hasUnitConversionFactor"));
		qa.setConversionFactor(attributeObjJSON.optString("unitConversionFactor"));
		qa.setShowText(attributeObjJSON.getBoolean("showText"));
		qa.setTableHeaderType(attributeObjJSON.getInt("tableHeaderType"));

		// calculation properties
		qa.setCalculatedFlag(attributeObjJSON.getBoolean("calculatedQuestion"));
		qa.setConditionalForCalc(attributeObjJSON.getBoolean("conditionalForCalc"));
		
		// skip rule properties
		// Will have to revisit skip rules after core Eform saves correctly since we need refactor that logic
		qa.setSkipRuleOperatorType(SkipRuleOperatorType.getByDisplay(attributeObjJSON.getInt("skipRuleOperatorType")));
		qa.setSkipRuleType(SkipRuleType.getByDisplay(attributeObjJSON.getInt("skipRuleType")));
		qa.setSkipRuleFlag(attributeObjJSON.getBoolean("skiprule"));
		qa.setSkipRuleEquals(attributeObjJSON.optString("skipRuleEquals"));
		// look through the skip rule for skipEquals - Are we still using it?

		// set the email trigger if there is one
		qa.setEmailTrigger(createEmailTrigger(attributeObjJSON));

		// still need to set the email trigger
		return qa;
	}

	private void removeAllPsuedoIdsFromEform() {
		for (Section section : this.eform.getSectionList()) {
			replaceSectionPsuedoId(section);

			for (SectionQuestion sq : section.getSectionQuestion()) {
				sq.setQuestion(replacePsuedoQuestion(sq.getQuestion()));
				replaceSkipRuleQuestion(sq.getSkipRuleQuestion());
				replaceCalculationQuestion(sq.getCalculatedQuestion());
				replacePsuedoQuestion(sq.getQuestion());
			}
		}
	}

	private void replaceSectionPsuedoId(Section section) {
		if (section.getId() != null && section.getId() < 0) {
			section.setId(null);
		}
	}

	private void replaceSkipRuleQuestion(List<SkipRuleQuestion> skipRuleQuestionSet) {
		if (skipRuleQuestionSet != null && !skipRuleQuestionSet.isEmpty()) {
			for (SkipRuleQuestion skipQuestion : skipRuleQuestionSet) {
				SkipRuleQuestionPk skipRuleKey = skipQuestion.getSkipRuleQuestionCompositePk();
				skipRuleKey.setQuestion(replacePsuedoQuestion(skipRuleKey.getQuestion()));
				skipRuleKey.setSkipRuleQuestion(replacePsuedoQuestion(skipRuleKey.getSkipRuleQuestion()));
				skipQuestion.setSkipRuleQuestionCompositePk(skipRuleKey);
			}
		}
	}

	private void replaceCalculationQuestion(List<CalculationQuestion> calculationQuestionSet) {
		if (calculationQuestionSet != null && !calculationQuestionSet.isEmpty()) {
			for (CalculationQuestion calcQuestion : calculationQuestionSet) {
				CalculationQuestionPk calcQuesKey = calcQuestion.getCalculationQuestionCompositePk();
				calcQuesKey.setQuestion(replacePsuedoQuestion(calcQuesKey.getQuestion()));
				calcQuesKey.setCalculationQuestion(replacePsuedoQuestion(calcQuesKey.getCalculationQuestion()));
				calcQuestion.setCalculationQuestionCompositePk(calcQuesKey);
			}
		}
	}

	private Question replacePsuedoQuestion(Question psuedoQuestion) {
		Question completeQuestion = questionMap.get(psuedoQuestion.getId());
		return completeQuestion;
	}

	private String removePsuedoSectionIdsFromCalculation(String calculationRule) {
		if (calculationRule != null & !calculationRule.isEmpty()) {

			List<Integer> s_indicesList = new ArrayList<Integer>();
			List<Integer> q_indicesList = new ArrayList<Integer>();
			List<String> stringRealSectionIds = new ArrayList<String>();

			int index = calculationRule.indexOf("S_");
			while (index != -1) {
				s_indicesList.add(index + 2);
				index = calculationRule.indexOf("S_", index + 1);
			}

			if (s_indicesList.size() > 0) {
				for (int s_ind : s_indicesList) {
					int q_ind = calculationRule.indexOf("_Q", s_ind);
					q_indicesList.add(q_ind);
					String bSecId = calculationRule.substring(s_ind, q_ind);
					Section sectionFromMap = sectionMap.get(Long.parseLong(bSecId));

					if (sectionFromMap != null) {
						String rSecId = String.valueOf(sectionFromMap.getId());
						stringRealSectionIds.add(rSecId);
					} else {
						stringRealSectionIds.add("none");
					}
				}

				for (int m = s_indicesList.size() - 1; m >= 0; m--) {
					int s_ind = s_indicesList.get(m);
					int q_ind = q_indicesList.get(m);
					String sId = stringRealSectionIds.get(m);
					if (!sId.equals("none")) {
						calculationRule =
								calculationRule.substring(0, s_ind) + sId
										+ calculationRule.substring(q_ind, calculationRule.length());
					}
				}
			}
		}
		return calculationRule;
	}

	private void addSectionMapToEform() {
		for (Entry<Long, Section> section : sectionMap.entrySet()) {
			this.eform.addToSectionList(section.getValue());
		}
	}

	private void setQuestionsJSONArr(JSONArray questionsJSONArr) {
		this.questionsJSONArr = questionsJSONArr;
	}

	private void addQuestionToQuestionMap(Long key, Question question) {
		this.questionMap.put(key, question);
	}

	private void setEform(Eform eform) {
		this.eform = eform;
	}

	public void setSectionMap(HashMap<Long, Section> sectionMap) {
		this.sectionMap = sectionMap;;
	}

	public String getFormMode() {
		return formMode;
	}

	public void setFormMode(String formMode) {
		this.formMode = formMode;
	}
}

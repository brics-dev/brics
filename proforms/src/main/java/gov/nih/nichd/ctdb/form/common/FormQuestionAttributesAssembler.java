package gov.nih.nichd.ctdb.form.common;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import gov.nih.nichd.ctdb.common.AssemblerException;
import gov.nih.nichd.ctdb.common.CtdbAssembler;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbForm;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.HtmlAttributes;
import gov.nih.nichd.ctdb.common.Version;
import gov.nih.nichd.ctdb.emailtrigger.domain.EmailTriggerValue;
import gov.nih.nichd.ctdb.form.domain.CalculatedFormQuestionAttributes;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.domain.FormExportImport;
import gov.nih.nichd.ctdb.form.domain.FormInfoExportImport;
import gov.nih.nichd.ctdb.form.domain.FormQuestionAttributes;
import gov.nih.nichd.ctdb.form.domain.QuestionAttributesExportImport;
import gov.nih.nichd.ctdb.form.domain.QuestionSectionExportImport;
import gov.nih.nichd.ctdb.form.domain.Section;
import gov.nih.nichd.ctdb.form.domain.SectionExportImport;
import gov.nih.nichd.ctdb.form.form.QuestionAttributesForm;
import gov.nih.nichd.ctdb.form.manager.FormManager;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.question.domain.Answer;
import gov.nih.nichd.ctdb.question.domain.AnswerType;
import gov.nih.nichd.ctdb.question.domain.CalculationType;
import gov.nih.nichd.ctdb.question.domain.ConversionFactor;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.domain.QuestionType;
import gov.nih.nichd.ctdb.question.domain.SkipRuleOperatorType;
import gov.nih.nichd.ctdb.question.domain.SkipRuleType;
import gov.nih.nichd.ctdb.question.manager.QuestionManager;
import gov.nih.nichd.ctdb.response.domain.DataEntryWorkflowType;
import gov.nih.nichd.ctdb.security.domain.User;


/**
 * Enables assembly of a Question domain object into a QuestionForm object and
 * vice-versa.
 * 
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class FormQuestionAttributesAssembler extends CtdbAssembler {

	/**
	 * Transforms a QuestionForm Object into a Question domain object with
	 * validation of the input data
	 * 
	 * @param form
	 *            The QuestionForm Object to transform into a Question object
	 * @return The Question domain object
	 * @throws AssemblerException
	 *             Thrown if any error occurs while transforming the
	 *             QuestionForm object to the Question Domain Object
	 */
	public static CtdbDomainObject jsonToDomain(JSONObject attributeObjJSON,
			Question oQuestion, int sectionId, int questionAttributesId,
			Map bogusNewSectionIDMap) throws AssemblerException {
		try {
			String dataElementName = attributeObjJSON.getString("dataElementName");
			int type = attributeObjJSON.getInt("qType");
			int answerType = attributeObjJSON.getInt("answerType");
			int skipRuleType = attributeObjJSON.getInt("skipRuleType");
			int skipRuleOperatorType = attributeObjJSON.getInt("skipRuleOperatorType");
			String skipRuleEquals = attributeObjJSON.getString("skipRuleEquals");
			JSONArray questionsToSkipJSON = attributeObjJSON.getJSONArray("questionsToSkip");
			String[] selectedSkipQuestions = null;
			
			if (questionsToSkipJSON != null && questionsToSkipJSON.length() > 0) {
				selectedSkipQuestions = new String[questionsToSkipJSON.length()];
				for (int i = 0; i < questionsToSkipJSON.length(); i++) {
					String questionToSkipString = (String) questionsToSkipJSON.get(i);
					String SkipString = replaceSectionBogusId(
							bogusNewSectionIDMap, questionToSkipString);
					selectedSkipQuestions[i] = SkipString;
				}

			}

			Question question = new Question();
			question.setType(QuestionType.getByValue(type));

			question.getFormQuestionAttributes().setDataElementName(
					dataElementName);

			// prepopulation
			boolean prepopulation = attributeObjJSON.getBoolean("prepopulation");
			String prepopulationValue = attributeObjJSON.getString("prepopulationValue");
			question.getFormQuestionAttributes().setPrepopulation(prepopulation);
			if (prepopulation) {
				question.getFormQuestionAttributes().setPrepopulationValue(
						prepopulationValue);
			} else {
				question.getFormQuestionAttributes().setPrepopulationValue("");
			}

			// decimal precision
			int decimalPrecision = Integer.parseInt(attributeObjJSON
					.getString("decimalPrecision"));
			question.getFormQuestionAttributes().setDecimalPrecision(
					decimalPrecision);
			
			
			//table header type
			int tableHeaderType = attributeObjJSON.getInt("tableHeaderType");
		    question.getFormQuestionAttributes().setTableHeaderType(tableHeaderType);
		    
		    //show hide text
			boolean showText = attributeObjJSON.getBoolean("showText");
		    question.getFormQuestionAttributes().setShowText(showText);
			
			

			// unit conversion factor
			boolean hasUnitConversionFactor = attributeObjJSON
					.getBoolean("hasUnitConversionFactor");
			question.getFormQuestionAttributes().setHasUnitConversionFactor(
					hasUnitConversionFactor);
			if (hasUnitConversionFactor) {
				String unitConversionFactor = attributeObjJSON
						.getString("unitConversionFactor");
				question.getFormQuestionAttributes().setUnitConversionFactor(
						unitConversionFactor);
			}

			// SKIP RULE QUESTION SETUP
			if (skipRuleType != Integer.MIN_VALUE && skipRuleOperatorType != Integer.MIN_VALUE) {
				question.getFormQuestionAttributes().setHasSkipRule(true);
				question.getFormQuestionAttributes().setSkipRuleType(SkipRuleType.getByValue(skipRuleType));
				question.getFormQuestionAttributes().setSkipRuleOperatorType(SkipRuleOperatorType.getByValue(skipRuleOperatorType));

				// skip rule types set check for other needed information
				if (question.getFormQuestionAttributes().getSkipRuleOperatorType().equals(SkipRuleOperatorType.EQUALS) || question.getFormQuestionAttributes().getSkipRuleOperatorType().equals(SkipRuleOperatorType.CONTAINS)) {
					// skip rule is an equals, check for equals value
					if (skipRuleEquals != null) {
						question.getFormQuestionAttributes().setSkipRuleEquals(skipRuleEquals);
					}

				}

				if (selectedSkipQuestions != null) {
					question.getFormQuestionAttributes().setSkipRuleType(SkipRuleType.getByValue(skipRuleType));
					question.getFormQuestionAttributes().setSkipRuleOperatorType(SkipRuleOperatorType.getByValue(skipRuleOperatorType));
					//question.getFormQuestionAttributes().setSkipRuleEquals(skipRuleEquals);

					int numOfQuestions = selectedSkipQuestions.length;
					List questionsToSkip = new ArrayList();

					Question tempSkipQuestion = null;

					for (int idx = 0; idx < numOfQuestions; idx++) {
						// modify by sunny for parse the new question id format
						String qString = selectedSkipQuestions[idx];
						String strQ[] = qString.split("_Q_");
						int skipQId = Integer.parseInt(strQ[1]);
						String strS[] = strQ[0].split("S_");
						int skipSId = Integer.parseInt(strS[1]);

						tempSkipQuestion = new Question();
						tempSkipQuestion.setId(skipQId);
						tempSkipQuestion.setSectionId(skipSId);
						questionsToSkip.add(tempSkipQuestion);
					}

					question.getFormQuestionAttributes().setQuestionsToSkip(
							questionsToSkip);
				}

			} 

			// SET RESPONSE METADATA

			boolean required = attributeObjJSON.getBoolean("required");
			question.getFormQuestionAttributes().setRequired(required);

			int minCharacters = attributeObjJSON.getInt("minCharacters");
			int maxCharacters = attributeObjJSON.getInt("maxCharacters");

			boolean validMin = false;
			boolean validMax = false;
			try {
				if (minCharacters != Integer.MIN_VALUE) {

					validMin = true;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			try {
				if (maxCharacters != Integer.MIN_VALUE) {

					validMax = true;
				}
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
			// check max characters is less than 4000
			if (validMax) {
				int maxChars = maxCharacters;

			}
			if (question.getType().equals(QuestionType.TEXTBOX)
					|| question.getType().equals(QuestionType.TEXTAREA)
					|| question.getType().equals(QuestionType.PATIENT_CALENDAR)) {
				// SINGLE RESPONSE
				if (validMin) {
					question.getFormQuestionAttributes().setMinCharacters(
							minCharacters);
					if (question.getFormQuestionAttributes().getMinCharacters() < 0) {

					}
				} else {
					question.getFormQuestionAttributes().setMinCharacters(
							Integer.MIN_VALUE);
				}
				if (validMax) {
					question.getFormQuestionAttributes().setMaxCharacters(
							maxCharacters);
					if (question.getFormQuestionAttributes().getMaxCharacters() < question
							.getFormQuestionAttributes().getMinCharacters()) {

					}

					if (question.getDefaultValue() != null
							&& question.getDefaultValue().length() > question
									.getFormQuestionAttributes()
									.getMaxCharacters()) {

					}
					if (question.getDefaultValue() != null
							&& question.getDefaultValue().length() < question
									.getFormQuestionAttributes()
									.getMinCharacters()) {

					}
				} else {
					question.getFormQuestionAttributes().setMaxCharacters(
							Integer.MIN_VALUE);
				}
			} 
			
			// SET HTML ATTRIBUTES
			String align = attributeObjJSON.getString("align");
			String vAlign = attributeObjJSON.getString("vAlign");
			String color = attributeObjJSON.getString("color");
			String fontFace = attributeObjJSON.getString("fontFace");
			String fontSize = attributeObjJSON.getString("fontSize");
			int indent = attributeObjJSON.getInt("indent");

			HtmlAttributes attributes = new HtmlAttributes();
			attributes.setAlign(align);
			attributes.setvAlign(vAlign);
			attributes.setColor(color);
			attributes.setFontFace(fontFace);
			attributes.setFontSize(fontSize);
			attributes.setIndent(indent);
			question.getFormQuestionAttributes().setHtmlAttributes(attributes);

			String rangeOperator = attributeObjJSON.getString("rangeOperator");
			String rangeValue1 = attributeObjJSON.getString("rangeValue1");
			String rangeValue2 = attributeObjJSON.getString("rangeValue2");

			if (rangeOperator != null && rangeOperator.equals("")) {
				rangeOperator = null;
			}
			question.getFormQuestionAttributes().setRangeOperator(rangeOperator);

			if (rangeValue1 != null && rangeValue1.equals("")) {
				rangeValue1 = null;
			}
			question.getFormQuestionAttributes().setRangeValue1(rangeValue1);

			if (rangeValue2 != null && rangeValue2.equals("")) {
				rangeValue2 = null;
			}
			question.getFormQuestionAttributes().setRangeValue2(rangeValue2);

			boolean horizontalDisplay = attributeObjJSON
					.getBoolean("horizontalDisplay");
			boolean horizDisplayBreak = attributeObjJSON
					.getBoolean("horizDisplayBreak");

			question.getFormQuestionAttributes().setAnswerType(
					AnswerType.getByValue(answerType));
			question.getFormQuestionAttributes().setId(questionAttributesId);
			question.getFormQuestionAttributes().setSectionId(sectionId);

			question.getFormQuestionAttributes().setHorizontalDisplay(
					horizontalDisplay);
			question.getFormQuestionAttributes().setHorizDisplayBreak(
					horizDisplayBreak);

			String toEmailAddress = attributeObjJSON.getString("toEmailAddress");
			String ccEmailAddress = attributeObjJSON.getString("ccEmailAddress");//
			String emailSubject = attributeObjJSON.getString("subject");
			String emailBody = attributeObjJSON.getString("body");
			int emailId = attributeObjJSON.getInt("eMailTriggerId");
			question.getFormQuestionAttributes().getEmailTrigger()
					.setId(emailId);

			int emailUpdatedBy = attributeObjJSON.getInt("eMailTriggerUpdatedBy");
			if (emailId == Integer.MIN_VALUE) {
				// creating now
				question.getFormQuestionAttributes().getEmailTrigger()
						.setCreatedBy(emailUpdatedBy);
			}
			question.getFormQuestionAttributes().getEmailTrigger()
					.setUpdatedBy(emailUpdatedBy);

			boolean deleteTrigger = attributeObjJSON.getBoolean("deleteTrigger");
			question.getFormQuestionAttributes()
					.setDeleteTrigger(deleteTrigger);

			if (!toEmailAddress.trim().equals("")) {
				question.getFormQuestionAttributes().getEmailTrigger()
						.setToEmailAddress(toEmailAddress);
				if (!(ccEmailAddress == null || ccEmailAddress.trim()
						.equals(""))) {
					question.getFormQuestionAttributes().getEmailTrigger()
							.setCcEmailAddress(ccEmailAddress);
				}

				question.getFormQuestionAttributes().getEmailTrigger()
						.setSubject(emailSubject);
				question.getFormQuestionAttributes().getEmailTrigger()
						.setBody(emailBody);

				JSONArray emailTriggerValuesJSON = attributeObjJSON.getJSONArray("triggerValues");

				List<EmailTriggerValue> etvList = new ArrayList<EmailTriggerValue>();
				if (emailTriggerValuesJSON != null && emailTriggerValuesJSON.length() > 0) {

					for (int i = 0; i < emailTriggerValuesJSON.length(); i++) {
						EmailTriggerValue etv = new EmailTriggerValue();

						JSONObject etvJsonObj = emailTriggerValuesJSON.getJSONObject(i);
						etv.setAnswer(etvJsonObj.getString("etAnswer"));
						etv.setId(etvJsonObj.getLong("etValId") > 0 ? etvJsonObj.getInt("etValId") : null);
						etv.setTriggerCondition(etvJsonObj.getString("etCondition").isEmpty() ? null : etvJsonObj
								.getString("etCondition"));
						etvList.add(etv);
					}

				}

				if (etvList != null || etvList.size() > 0) {
						question.getFormQuestionAttributes().getEmailTrigger()
								.getTriggerValues()
							.addAll(etvList);
				}


				boolean dataSpring = attributeObjJSON.getBoolean("dataSpring");
				String htmlText = attributeObjJSON.getString("htmlText");

				question.getFormQuestionAttributes().setDataSpring(dataSpring);
				if (!oQuestion.getText().equals(htmlText)) {
					question.getFormQuestionAttributes().setHtmlText(htmlText);
				} else {
					question.getFormQuestionAttributes().setHtmlText(null);
				}

			}

			boolean calculatedQuestion = attributeObjJSON
					.getBoolean("calculatedQuestion");
			// System.out.println("calculated question is " +
			// calculatedQuestion);

			if (calculatedQuestion) {
				question.getFormQuestionAttributes().setIsCalculatedQuestion(
						true);
				CalculatedFormQuestionAttributes calculatedFormQuestionAttributes = new CalculatedFormQuestionAttributes();
				int conversionFactor = attributeObjJSON
						.getInt("conversionFactor");
				int calculationType = attributeObjJSON
						.getInt("calculationType");
				String calculation = attributeObjJSON.getString("calculation");
				// System.out.println("calculation is "+ calculation);

				JSONArray questionsToCalculateJSON = attributeObjJSON
						.getJSONArray("questionsToCalculate");

				List questionsToCalculate = new ArrayList();
				/*
				 * if(questionsToCalculateJSON == null) {
				 * System.out.println("NULL!"); }else {
				 * System.out.println(questionsToCalculateJSON.length()); }
				 */
				if (questionsToCalculateJSON != null
						&& questionsToCalculateJSON.length() > 0) {
					/* questionsToCalculate = new ArrayList(); */
					for (int i = 0; i < questionsToCalculateJSON.length(); i++) {
						String calcQuestString = (String) questionsToCalculateJSON
								.get(i);
						calcQuestString = replaceSectionBogusId(
								bogusNewSectionIDMap, calcQuestString);

						questionsToCalculate.add(calcQuestString);
					}
				}

				calculatedFormQuestionAttributes
						.setQuestionsToCalculate(questionsToCalculate);
				calculatedFormQuestionAttributes.setCalculation(calculation);

				CalculationType calcType = null;
				if (calculationType == 1) {
					calcType = CalculationType.SUM;
				} else if (calculationType == 2) {
					calcType = CalculationType.DIFFERENCE;
				} else if (calculationType == 3) {
					calcType = CalculationType.AVERAGE;
				} else if (calculationType == 4) {
					calcType = CalculationType.MULTIPLICATI0N;
				} else if (calculationType == 5) {
					calcType = CalculationType.DIVISION;
				}

				calculatedFormQuestionAttributes.setCalculationType(calcType);

				ConversionFactor factor = null;
				if (conversionFactor == 1) {
					factor = ConversionFactor.YEARS;
				} else if (conversionFactor == 2) {
					factor = ConversionFactor.MONTHS;
				} else if (conversionFactor == 3) {
					factor = ConversionFactor.WEEKS;
				} else if (conversionFactor == 4) {
					factor = ConversionFactor.DAYS;
				} else if (conversionFactor == 5) {
					factor = ConversionFactor.HOURS;
				} else if (conversionFactor == 6) {
					factor = ConversionFactor.MINUTES;
				} else if (conversionFactor == 7) {
					factor = ConversionFactor.SECONDS;
				}
				calculatedFormQuestionAttributes.setConversionFactor(factor);

				question.setCalculatedFormQuestionAttributes(calculatedFormQuestionAttributes);

			} else {
				question.getFormQuestionAttributes().setIsCalculatedQuestion(
						true);
			}
			return question;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Question xmlToQuestionDomain( QuestionSectionExportImport qsei) throws AssemblerException
    {
    	 QuestionAttributesExportImport qaei = qsei.getQaei();
    	 try {
        	String dataElementName = qaei.getDataElementName();
            int type = qsei.getQuestionType();
          
            int answerType = qaei.getAnswerType();
            int skipRuleType = qaei.getSkipRuleType();
            int skipRuleOperatorType = qaei.getSkipRuleOperatorType();
            String skipRuleEquals = qaei.getSkipRuleEquals();
            //TODO: get list of for questions to skip
            String questionsToSkipJSON = qaei.getSkipRuleEquals();
            
            Question question = new Question();
            question.setType(QuestionType.getByValue(type));
            question.setId(qsei.getQuestionId());
            question.setSectionId(qsei.getSectionId());
            question.setQuestionOrder(qsei.getQuestionOrder());
            question.setQuestionOrderCol(qsei.getQuestionOrderCol());
            question.setVersion(new Version(1));
            
         
            question.getFormQuestionAttributes().setDataElementName(dataElementName);
            question.getFormQuestionAttributes().setSectionId(qsei.getSectionId());
            question.getFormQuestionAttributes().setQuestionId(qsei.getQuestionId());
            question.getFormQuestionAttributes().setId(qsei.getQuestionAttributeId());
            
            //prepopulation
            boolean prepopulation = Boolean.valueOf(qaei.getPrepopulationValue());
            String prepopulationValue = qaei.getPrepopulationValue();
            question.getFormQuestionAttributes().setPrepopulation(prepopulation);
            if(prepopulation) {
            	question.getFormQuestionAttributes().setPrepopulationValue(prepopulationValue);
            }else {
            	question.getFormQuestionAttributes().setPrepopulationValue("");
            }
            
            //decimal precision
            int decimalPrecision = qaei.getDecimalPrecision();
            question.getFormQuestionAttributes().setDecimalPrecision(decimalPrecision);
            
            //table header type
			int tableHeaderType = qaei.getTableHeaderType();
		    question.getFormQuestionAttributes().setTableHeaderType(tableHeaderType);
		    
		    //show hide text
			boolean showText = qaei.isShowText();
		    question.getFormQuestionAttributes().setShowText(showText);
            
            
            //unit conversion factor
            boolean hasUnitConversionFactor = qaei.isHasUnitConversionFactor();
            question.getFormQuestionAttributes().setHasUnitConversionFactor(hasUnitConversionFactor);
            if(hasUnitConversionFactor) {
            	String unitConversionFactor = qaei.getUnitConversionFactor();
                question.getFormQuestionAttributes().setUnitConversionFactor(unitConversionFactor);
            }
            
            
            
            // SKIP RULE QUESTION SETUP
            if(skipRuleType != Integer.MIN_VALUE && skipRuleOperatorType != Integer.MIN_VALUE)
            {
                question.getFormQuestionAttributes().setHasSkipRule(true);
                question.getFormQuestionAttributes().setSkipRuleType(SkipRuleType.getByValue(skipRuleType));
                question.getFormQuestionAttributes().setSkipRuleOperatorType(SkipRuleOperatorType.getByValue(skipRuleOperatorType));

                // skip rule types set check for other needed information
                if (question.getFormQuestionAttributes().getSkipRuleOperatorType().equals(SkipRuleOperatorType.EQUALS) || question.getFormQuestionAttributes().getSkipRuleOperatorType().equals(SkipRuleOperatorType.CONTAINS))
                {
                    // skip rule is an equals, check for equals value
                    if(skipRuleEquals != null)
                    {
                        question.getFormQuestionAttributes().setSkipRuleEquals(skipRuleEquals);
                    }
                   
                }
            }

            //SET RESPONSE METADATA
            
            boolean required = qaei.isRequired();
            question.getFormQuestionAttributes().setRequired(required);
            
            int minCharacters = qaei.getMinCharacters();
            int maxCharacters = qaei.getMaxCharacters() ;

            boolean validMin = false;
            boolean validMax = false;
            try
            {
                if(minCharacters != Integer.MIN_VALUE)
                {
                 
                    validMin = true;
                }
            }
            catch(NumberFormatException e)
            {
                e.printStackTrace();
            }
            try
            {
                if(maxCharacters != Integer.MIN_VALUE)
                {
            
                    validMax = true;
                }
            }
            catch(NumberFormatException e)
            {
               e.printStackTrace();
            }
            // check max characters is less than 4000
            if(validMax)
            {
                int maxChars = maxCharacters;
                
            }
            if(question.getType().equals(QuestionType.TEXTBOX) ||
                    question.getType().equals(QuestionType.TEXTAREA) ||
                    question.getType().equals(QuestionType.PATIENT_CALENDAR))
            {
                // SINGLE RESPONSE
                if(validMin)
                {
                    question.getFormQuestionAttributes().setMinCharacters(minCharacters);
                    if(question.getFormQuestionAttributes().getMinCharacters() < 0)
                    {
                        
                    }
                }
                else
                {
                    question.getFormQuestionAttributes().setMinCharacters(Integer.MIN_VALUE);
                }
                if(validMax)
                {
                    question.getFormQuestionAttributes().setMaxCharacters(maxCharacters);
                    if(question.getFormQuestionAttributes().getMaxCharacters() < question.getFormQuestionAttributes().getMinCharacters())
                    {
                        
                    }

                    if(question.getDefaultValue() != null &&
                            question.getDefaultValue().length() > question.getFormQuestionAttributes().getMaxCharacters())
                    {
                        
                    }
                    if(question.getDefaultValue() != null &&
                            question.getDefaultValue().length() < question.getFormQuestionAttributes().getMinCharacters())
                    {
                        
                    }
                }
                else
                {
                    question.getFormQuestionAttributes().setMaxCharacters(Integer.MIN_VALUE);
                }
            }

            // SET HTML ATTRIBUTES
            String align = qaei.getHalign();
            String vAlign = qaei.getValign();
            String color = qaei.getTextcolor();
            String fontFace = qaei.getFontface();
            String fontSize = qaei.getFontsize();
            int indent = qaei.getIndent();
            
            
            HtmlAttributes attributes = new HtmlAttributes();
            attributes.setAlign(align);
            attributes.setvAlign(vAlign);
            attributes.setColor(color);
            attributes.setFontFace(fontFace);
            attributes.setFontSize(fontSize);
            attributes.setIndent(indent);
            question.getFormQuestionAttributes().setHtmlAttributes(attributes);
            
            String rangeOperator = qaei.getRangeOperator();
            String rangeValue1 = qaei.getRangeValue1();
            String rangeValue2 = qaei.getRangeValue2();

            if (rangeOperator != null && rangeOperator.equals(""))
            {
            	rangeOperator = null;
            }
            question.getFormQuestionAttributes().setRangeOperator(rangeOperator);

            if (rangeValue1 != null && rangeValue1.equals(""))
            {
                rangeValue1 = null;
            }
            question.getFormQuestionAttributes().setRangeValue1 (rangeValue1);

            if (rangeValue2 != null && rangeValue2.equals(""))
            {
                rangeValue2 = null;
            }
            question.getFormQuestionAttributes().setRangeValue2(rangeValue2);
            
            
            boolean horizontalDisplay = qaei.isHorizontalDisplay();
            boolean horizDisplayBreak = qaei.isHorizDisplayBreak();
            //System.out.println("saving horzDisplay to  " + horizontalDisplay);
            //System.out.println("saving horzDisplayBreak to  " + horizDisplayBreak);

            question.getFormQuestionAttributes().setAnswerType(AnswerType.getByValue(answerType));
            question.getFormQuestionAttributes().setPrepopulation(qaei.isPrepopulation());
            question.getFormQuestionAttributes().setPrepopulationValue(qaei.getPrepopulationValue());

            question.getFormQuestionAttributes().setHorizontalDisplay(horizontalDisplay);
            question.getFormQuestionAttributes().setHorizDisplayBreak(horizDisplayBreak);
            
            question.getFormQuestionAttributes().setDeleteTrigger(qaei.isDeleteTrigger() );
            if(qaei.getEmailTrigger() != null && qaei.getEmailTrigger().getId() != Integer.MIN_VALUE){
            	question.getFormQuestionAttributes().setEmailTrigger(qaei.getEmailTrigger());
            }
/*                if (emailTriggerAnswers != null || emailTriggerAnswers.length > 0) {       
                    for (int i = 0; i < emailTriggerAnswers.length; i++) {
                        question.getFormQuestionAttributes().getEmailTrigger().getTriggerAnswers().add(emailTriggerAnswers[i]);
                    }
                }
*/
            
            boolean dataSpring = qaei.isDataSpring();
            String htmlText = qaei.getHtmlText();
            
            question.getFormQuestionAttributes().setDataSpring(dataSpring);
            question.getFormQuestionAttributes().setHtmlText(htmlText);
            
            boolean calculatedQuestion = qaei.isCalculatedQuestion();
            //System.out.println("calculated question is " + calculatedQuestion);
             
            if(calculatedQuestion) {
            	question.getFormQuestionAttributes().setIsCalculatedQuestion(true);
            	CalculatedFormQuestionAttributes calculatedFormQuestionAttributes = new CalculatedFormQuestionAttributes();
            	int conversionFactor = qaei.getDateConversionFactor();
            	int calculationType = qaei.getCalculationType();
            	String calculation = qaei.getCalculation();
            	//System.out.println("calculation is "+ calculation); 
            	
            	List questionsToCalculate = new ArrayList();
                 calculatedFormQuestionAttributes.setQuestionsToCalculate(questionsToCalculate);
                 calculatedFormQuestionAttributes.setCalculation(calculation);
                 
                 CalculationType calcType = null;
                 if(calculationType == 1) {
                	 calcType = CalculationType.SUM;
                 }else if(calculationType == 2) {
                	 calcType = CalculationType.DIFFERENCE;
                 }else if(calculationType == 3) {
                	 calcType = CalculationType.AVERAGE;
                 }else if(calculationType == 4) {
                	 calcType = CalculationType.MULTIPLICATI0N;
                 }else if(calculationType == 5) {
                	 calcType = CalculationType.DIVISION;
                 }
                 
                 calculatedFormQuestionAttributes.setCalculationType(calcType);
                 
                 ConversionFactor factor = null;
                 if(conversionFactor == 1) {
                	 factor = ConversionFactor.YEARS;
                 }else if(conversionFactor == 2) {
                	 factor = ConversionFactor.MONTHS;
                 }else if(conversionFactor == 3) {
                	 factor = ConversionFactor.WEEKS;
                 }else if(conversionFactor == 4) {
                	 factor = ConversionFactor.DAYS;
                 }else if(conversionFactor == 5) {
                	 factor = ConversionFactor.HOURS;
                 }else if(conversionFactor == 6) {
                	 factor = ConversionFactor.MINUTES;
                 }else if(conversionFactor == 7) {
                	 factor = ConversionFactor.SECONDS;
                 }
                 calculatedFormQuestionAttributes.setConversionFactor(factor);
               
                 question.setCalculatedFormQuestionAttributes(calculatedFormQuestionAttributes);
            	
            }else {
            	question.getFormQuestionAttributes().setIsCalculatedQuestion(false);            	
            }
            return question;
            
        }catch(Exception e) {
        	e.printStackTrace();
        	return null;
        }
    }

	/**
	 * Transforms a Question Domain Object into a QuestionForm object
	 * 
	 * @param domain
	 *            The Question Domain Object to transform into a QuestionForm
	 *            object
	 * @throws AssemblerException
	 *             Thrown if any error occurs while transforming the Patient
	 *             domain object to the PatientForm object
	 */
	public static void domainToJson(Question question,
			JSONObject attributeObjJson, int sectionId)
			throws AssemblerException {
		try {
			//System.out.println("FormQuestionAttributeAssembler:domainToJSON");
			// Question question = (Question) domain;

			FormManager fm = new FormManager();

			// FormQuestionAttributes qAttrs =
			// question.getFormQuestionAttributes();

			FormQuestionAttributes qAttrs = fm.getFormQuestionAttributes(
					sectionId, question.getId());

			// SET STANDARD METADATA
			// questionAttributesForm.setId(questionAttributesForm.getId());
			// ?????

			// questionAttributesForm.setType(question.getType().getValue());
			attributeObjJson.put("qType", question.getType().getValue());

			attributeObjJson.put("answerType", qAttrs.getAnswerType()
					.getValue());

			attributeObjJson
					.put("dataElementName", qAttrs.getDataElementName());

			// prepopulation
			attributeObjJson.put("prepopulation", qAttrs.isPrepopulation());
			attributeObjJson.put("prepopulationValue",
					qAttrs.getPrepopulationValue());

			// decimal precision
			attributeObjJson.put("decimalPrecision",
					String.valueOf(qAttrs.getDecimalPrecision()));

		    //table header type
			attributeObjJson.put("tableHeaderType", qAttrs.getTableHeaderType());
			
			
			//show hide text
			attributeObjJson.put("showText", qAttrs.isShowText());
			
			
			
			// conversion factor
			if (qAttrs.isHasUnitConversionFactor()) {
				attributeObjJson.put("hasUnitConversionFactor",
						qAttrs.isHasUnitConversionFactor());
				attributeObjJson.put("unitConversionFactor",
						qAttrs.getUnitConversionFactor());
			} else {
				attributeObjJson.put("hasUnitConversionFactor",
						qAttrs.isHasUnitConversionFactor());
				attributeObjJson.put("unitConversionFactor", "");
			}

			// questionAttributesForm.setRequired(qAttrs.isRequired());
			attributeObjJson.put("required", qAttrs.isRequired());

			// questionAttributesForm.setRangeOperator
			// (qAttrs.getRangeOperator());
			String rangeOperator = "";
			if (qAttrs.getRangeOperator() != null) {
				rangeOperator = qAttrs.getRangeOperator();
			}
			attributeObjJson.put("rangeOperator", rangeOperator);

			String rangeValue1 = "";
			if (qAttrs.getRangeValue1() != null) {
				rangeValue1 = qAttrs.getRangeValue1();
			}
			// questionAttributesForm.setRangeValue1 (qAttrs.getRangeValue1());
			attributeObjJson.put("rangeValue1", rangeValue1);

			String rangeValue2 = "";
			if (qAttrs.getRangeValue2() != null) {
				rangeValue2 = qAttrs.getRangeValue2();
			}
			// questionA
			// questionAttributesForm.setRangeValue2 (qAttrs.getRangeValue2());
			attributeObjJson.put("rangeValue2", rangeValue2);

			// questionAttributesForm.setHorizontalDisplay(qAttrs.isHorizontalDisplay());
			attributeObjJson.put("horizontalDisplay",
					qAttrs.isHorizontalDisplay());

			// questionAttributesForm.setHorizDisplayBreak(qAttrs.isHorizDisplayBreak());
			attributeObjJson.put("horizDisplayBreak",
					qAttrs.isHorizDisplayBreak());

			// questionAttributesForm.setTextareaWidth(qAttrs.getTextareaWidth());
			attributeObjJson.put("textareaWidth", qAttrs.getTextareaWidth());

			// questionAttributesForm.setTextareaHeight(qAttrs.getTextareaHeight());
			attributeObjJson.put("textareaHeight", qAttrs.getTextareaHeight());

			// questionAttributesForm.setTextboxLength(qAttrs.getTextboxLength());
			attributeObjJson.put("textboxLength", qAttrs.getTextboxLength());

			JSONArray questionsToSkipJsonArr = new JSONArray();
			// JSONArray qSkipSectionIdArr = new JSONArray();
			// SET SKIP RULE
			if (qAttrs.hasSkipRule()) {
				if (qAttrs.getSkipRuleEquals() == null) {
					attributeObjJson.put("skipRuleEquals", "");
				} else {
					attributeObjJson.put("skipRuleEquals", qAttrs.getSkipRuleEquals());
				}

				// questionAttributesForm.setSkipRuleOperatorType(qAttrs.getSkipRuleOperatorType().getValue());
				if (qAttrs.getSkipRuleOperatorType() != null) {
					attributeObjJson.put("skipRuleOperatorType", qAttrs
							.getSkipRuleOperatorType().getValue());
				} else {
					attributeObjJson.put("skipRuleOperatorType",
							Integer.MIN_VALUE);
				}

				if (qAttrs.getSkipRuleType() != null) {
					attributeObjJson.put("skipRuleType", qAttrs
							.getSkipRuleType().getValue());
				} else {
					attributeObjJson.put("skipRuleType", Integer.MIN_VALUE);
				}

				List questionsToSkipList = qAttrs.getQuestionsToSkip();
				if (questionsToSkipList != null
						&& questionsToSkipList.size() > 0) {
					Iterator iter = questionsToSkipList.iterator();
					while (iter.hasNext()) {
						Question qS = (Question) iter.next();
						// String questionToSkipString =
						// String.valueOf(qS.getId());
						String questionToSkipString = "S_"
								+ qS.getSkipSectionId() + "_Q_" + qS.getId();
						// System.out.println("question to skip : " +
						// questionToSkipString);
						questionsToSkipJsonArr.put(questionToSkipString);
						// qSkipSectionIdArr.put(qS.getSkipSectionId());

					}

				}

			} else {

				attributeObjJson.put("skipRuleType", Integer.MIN_VALUE);
				attributeObjJson.put("skipRuleOperatorType", Integer.MIN_VALUE);
				attributeObjJson.put("skipRuleEquals", "");

			}
			attributeObjJson.put("questionsToSkip", questionsToSkipJsonArr);
			// attributeObjJson.put("qSkipSectionId",qSkipSectionIdArr);

			QuestionManager qm = new QuestionManager();
			boolean isSkDependent = qm.isSkipRuleDependent(
					question.getSectionId(), question.getId());
			if (isSkDependent == true) {
				attributeObjJson.put("skipRuleDependent", true);
			}

			// relational skip rule
			if (questionsToSkipJsonArr.length() > 0) {
				attributeObjJson.put("skipRuleDependent", true);
			}

			// questionAttributesForm.setAnswerType(qAttrs.getAnswerType().getValue());
			if (qAttrs.getAnswerType() != null) {
				attributeObjJson.put("answerTypeDisplay", qAttrs
						.getAnswerType().getValue());
			} else {
				attributeObjJson.put("answerTypeDisplay", "");
			}
			// if(qAttrs.getMinCharacters() != Integer.MIN_VALUE)
			if (qAttrs.getMinCharacters() != 0) // changes by Ching Heng
			{
				// questionAttributesForm.setMinCharacters(Integer.toString(qAttrs.getMinCharacters()));
				attributeObjJson
						.put("minCharacters", qAttrs.getMinCharacters());
			} else {
				// questionAttributesForm.setMinCharacters(null);
				attributeObjJson
						.put("minCharacters", qAttrs.getMinCharacters());
			}
			// if(qAttrs.getMaxCharacters() != Integer.MIN_VALUE)
			if (qAttrs.getMaxCharacters() != 4000) // changed by Ching Heng
			{
				// questionAttributesForm.setMaxCharacters(Integer.toString(qAttrs.getMaxCharacters()));
				attributeObjJson
						.put("maxCharacters", qAttrs.getMaxCharacters());
			} else {
				// questionAttributesForm.setMaxCharacters(null);
				attributeObjJson
						.put("maxCharacters", qAttrs.getMaxCharacters());
			}

			// SET HTML ATTRIBUTES
			// questionAttributesForm.setAlign(qAttrs.getHtmlAttributes().getAlign());
			attributeObjJson
					.put("align", qAttrs.getHtmlAttributes().getAlign());

			// questionAttributesForm.setvAlign(qAttrs.getHtmlAttributes().getvAlign());
			attributeObjJson.put("vAlign", qAttrs.getHtmlAttributes()
					.getvAlign());

			// questionAttributesForm.setColor(qAttrs.getHtmlAttributes().getColor());
			attributeObjJson
					.put("color", qAttrs.getHtmlAttributes().getColor());

			// questionAttributesForm.setFontFace(qAttrs.getHtmlAttributes().getFontFace());
			attributeObjJson.put("fontFace", qAttrs.getHtmlAttributes()
					.getFontFace());

			// questionAttributesForm.setFontSize(qAttrs.getHtmlAttributes().getFontSize());
			if (qAttrs.getHtmlAttributes().getFontSize() != null) {
				attributeObjJson.put("fontSize", qAttrs.getHtmlAttributes()
						.getFontSize());
			} else {
				attributeObjJson.put("fontSize", "");
			}

			// questionAttributesForm.setIndent(qAttrs.getHtmlAttributes().getIndent());
			attributeObjJson.put("indent", qAttrs.getHtmlAttributes()
					.getIndent());

			// email trigger
			attributeObjJson.put("toEmailAddress", "");
			attributeObjJson.put("ccEmailAddress", "");
			attributeObjJson.put("body", "");
			attributeObjJson.put("subject", "");


			// attributeObjJson.put("questionsToSkip",questionsToSkipJsonArr);

			attributeObjJson.put("eMailTriggerId", qAttrs.getEmailTrigger()
					.getId());
			attributeObjJson.put("eMailTriggerUpdatedBy", Integer.MIN_VALUE);

			attributeObjJson.put("deleteTrigger", false);

			if (qAttrs.getEmailTrigger().getToEmailAddress() != null) {
				String toEmailAddress = "";
				if(qAttrs.getEmailTrigger().getToEmailAddress() != null) {
					toEmailAddress = qAttrs.getEmailTrigger().getToEmailAddress();
				}
				attributeObjJson.put("toEmailAddress", toEmailAddress);
				
				String ccEmailAddress = "";
				if(qAttrs.getEmailTrigger().getCcEmailAddress() != null) {
					ccEmailAddress = qAttrs.getEmailTrigger().getCcEmailAddress();
				}
				attributeObjJson.put("ccEmailAddress", ccEmailAddress);
				
				
				String body = "";
				if(qAttrs.getEmailTrigger().getBody() != null) {
					body = qAttrs.getEmailTrigger().getBody();
				}
				attributeObjJson.put("body", body);
				
				
				String subject = "";
				if(qAttrs.getEmailTrigger().getSubject() != null) {
					subject = qAttrs.getEmailTrigger().getSubject();
				}
				attributeObjJson.put("subject", subject);

				JSONArray triggerValuesJsonArr = new JSONArray();
				Set<EmailTriggerValue> triggerValuesList = qAttrs.getEmailTrigger().getTriggerValues();

				if (triggerValuesList != null && triggerValuesList.size() > 0) {
					Iterator iter = triggerValuesList.iterator();
					while (iter.hasNext()) {
						EmailTriggerValue etv = (EmailTriggerValue) iter.next();
						JSONObject etvJsonObj = new JSONObject();
						etvJsonObj.put("etValId", etv.getId());
						etvJsonObj.put("etAnswer", etv.getAnswer());
						etvJsonObj.put("etCondition",
								(etv.getTriggerCondition() == null ? "" : etv.getTriggerCondition()));
						triggerValuesJsonArr.put(etvJsonObj);
					}

				}
				attributeObjJson.put("triggerValues", triggerValuesJsonArr);

				attributeObjJson.put("eMailTriggerId", qAttrs.getEmailTrigger()
						.getId());
			}

			attributeObjJson.put("dataSpring", qAttrs.isDataSpring());

			// set html text
			if (StringUtils.isNotEmpty(qAttrs.getHtmlText())) {

				attributeObjJson.put("htmlText", qAttrs.getHtmlText());
			} else {

				attributeObjJson.put("htmlText", question.getText());
			}

			// calculation stuff
			CalculatedFormQuestionAttributes calculatedFormQuestionAttributes = question
					.getCalculatedFormQuestionAttributes();
			attributeObjJson.put("calculationType", Integer.MIN_VALUE);
			attributeObjJson.put("conversionFactor", Integer.MIN_VALUE);
			JSONArray questionToCalculateJson = new JSONArray();
			attributeObjJson.put("questionsToCalculate",
					questionToCalculateJson);
			attributeObjJson.put("calculation", "");
			attributeObjJson.put("calculatedQuestion", false);
			attributeObjJson.put("calDependent", false);

			/*
			 * if(qAttrs.isCalculatedQuestion()) {
			 * CalculatedFormQuestionAttributes cQattrs =
			 * (CalculatedFormQuestionAttributes)qAttrs;
			 * System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%% " +
			 * cQattrs.getCalculation()); }
			 * 
			 * 
			 * 
			 * if(calculatedFormQuestionAttributes.getCalculation() == null) {
			 * System.out.println(
			 * "calculatedFormQuestionAttributes.getCalculation() isd null"); }
			 */
			if (qAttrs.isCalculatedQuestion()) {
				CalculatedFormQuestionAttributes cQattrs = (CalculatedFormQuestionAttributes) qAttrs;

				attributeObjJson.put("calculatedQuestion", true);

				CalculationType calcType = cQattrs.getCalculationType();
				if (calcType != null) {
					if (calcType.getDispValue().equals("Sum")) {
						attributeObjJson.put("calculationType", 1);
					} else if (calcType.getDispValue().equals("Difference")) {
						attributeObjJson.put("calculationType", 2);
					} else if (calcType.getDispValue().equals("Average")) {
						attributeObjJson.put("calculationType", 3);
					} else if (calcType.getDispValue().equals("Multiplication")) {
						attributeObjJson.put("calculationType", 4);
					} else if (calcType.getDispValue().equals("Division")) {
						attributeObjJson.put("calculationType", 5);
					}
				}
				ConversionFactor convFactor = cQattrs.getConversionFactor();
				if (convFactor != null) {
					if (convFactor.getDispValue().equals("Years")) {
						attributeObjJson.put("conversionFactor", 1);
					} else if (convFactor.getDispValue().equals("Months")) {
						attributeObjJson.put("conversionFactor", 2);
					} else if (convFactor.getDispValue().equals("Weeks")) {
						attributeObjJson.put("conversionFactor", 3);
					} else if (convFactor.getDispValue().equals("Days")) {
						attributeObjJson.put("conversionFactor", 4);
					} else if (convFactor.getDispValue().equals("Hours")) {
						attributeObjJson.put("conversionFactor", 5);
					} else if (convFactor.getDispValue().equals("Minutes")) {
						attributeObjJson.put("conversionFactor", 6);
					} else if (convFactor.getDispValue().equals("Seconds")) {
						attributeObjJson.put("conversionFactor", 7);
					}
				}

				String calcu = cQattrs.getCalculation();

				attributeObjJson.put("calculation", calcu);

				List questToCalcList = cQattrs.getQuestionsToCalculate();

				if (questToCalcList != null && questToCalcList.size() > 0) {
					Iterator iter = questToCalcList.iterator();
					while (iter.hasNext()) {
						Question qC = (Question) iter.next();
						String qTC = String.valueOf(qC.getId());
						questionToCalculateJson.put("S_" + qC.getSectionId()
								+ "_Q_" + qTC);
					}

					attributeObjJson.put("questionsToCalculate",
							questionToCalculateJson);
				}

			}

		} catch (Exception e) {
			throw new AssemblerException(
					"Unable to tranform domain object to question attributes form object: "
							+ e.getMessage(), e);
		}
	}

	/**
	 * Transforms a QuestionForm Object into a Question domain object with
	 * validation of the input data
	 * 
	 * @param form
	 *            The QuestionForm Object to transform into a Question object
	 * @return The Question domain object
	 * @throws AssemblerException
	 *             Thrown if any error occurs while transforming the
	 *             QuestionForm object to the Question Domain Object
	 */
	public static CtdbDomainObject validateFormToDomain(List errors,
			CtdbForm form, Question oQuestion) throws AssemblerException {
		try {
			QuestionAttributesForm questionAttributesForm = (QuestionAttributesForm) form;

			Question question = new Question();
			question.setType(QuestionType.getByValue(questionAttributesForm
					.getType()));

			// SKIP RULE QUESTION SETUP
			if (questionAttributesForm.getSkipRuleType() != Integer.MIN_VALUE
					&& questionAttributesForm.getSkipRuleOperatorType() != Integer.MIN_VALUE) {
				question.getFormQuestionAttributes().setHasSkipRule(true);
				question.getFormQuestionAttributes().setSkipRuleType(
						SkipRuleType.getByValue(questionAttributesForm
								.getSkipRuleType()));
				question.getFormQuestionAttributes().setSkipRuleOperatorType(
						SkipRuleOperatorType.getByValue(questionAttributesForm
								.getSkipRuleOperatorType()));

				// skip rule types set check for other needed information
				if (question.getFormQuestionAttributes()
						.getSkipRuleOperatorType()
						.equals(SkipRuleOperatorType.EQUALS)) {
					// skip rule is an equals, check for equals value
					if (questionAttributesForm.getSkipRuleEquals() != null) {
						question.getFormQuestionAttributes().setSkipRuleEquals(
								questionAttributesForm.getSkipRuleEquals());
					} else {
						errors.add("Please specify an equals value for the skip rule in the skip rules tab.");
					}
				}

				if (questionAttributesForm.getQuestionsToSkip() != null) {
					question.getFormQuestionAttributes().setSkipRuleType(
							SkipRuleType.getByValue(questionAttributesForm
									.getSkipRuleType()));
					question.getFormQuestionAttributes()
							.setSkipRuleOperatorType(
									SkipRuleOperatorType
											.getByValue(questionAttributesForm
													.getSkipRuleOperatorType()));
					question.getFormQuestionAttributes().setSkipRuleEquals(
							questionAttributesForm.getSkipRuleEquals());

					int[] selectedSkipQuestions = questionAttributesForm
							.getQuestionsToSkip();
					int numOfQuestions = selectedSkipQuestions.length;
					List questionsToSkip = new ArrayList();

					Question tempSkipQuestion = null;

					for (int idx = 0; idx < numOfQuestions; idx++) {
						tempSkipQuestion = new Question();
						tempSkipQuestion.setId(selectedSkipQuestions[idx]);
						questionsToSkip.add(tempSkipQuestion);
					}

					question.getFormQuestionAttributes().setQuestionsToSkip(
							questionsToSkip);
				} else {
					errors.add("Please assign questions to skip in the skip rules tab.");
				}

			} else if (questionAttributesForm.getSkipRuleType() == Integer.MIN_VALUE
					&& questionAttributesForm.getSkipRuleOperatorType() != Integer.MIN_VALUE) {
				errors.add("Please specify a skip rule to associate with the skip rule operator in the skip rules tab.");
				if (questionAttributesForm.getSkipRuleOperatorType() == SkipRuleOperatorType.EQUALS
						.getValue()) {
					if (questionAttributesForm.getSkipRuleEquals() == null) {
						errors.add("Please specify an equals value for the skip rule in the skip rules tab.");
					}
				}
				if (questionAttributesForm.getQuestionsToSkip() != null) {
					question.getFormQuestionAttributes().setSkipRuleType(
							SkipRuleType.getByValue(questionAttributesForm
									.getSkipRuleType()));
					question.getFormQuestionAttributes()
							.setSkipRuleOperatorType(
									SkipRuleOperatorType
											.getByValue(questionAttributesForm
													.getSkipRuleOperatorType()));
					question.getFormQuestionAttributes().setSkipRuleEquals(
							questionAttributesForm.getSkipRuleEquals());

					int[] selectedSkipQuestions = questionAttributesForm
							.getQuestionsToSkip();
					int numOfQuestions = selectedSkipQuestions.length;
					List questionsToSkip = new ArrayList();

					Question tempSkipQuestion = null;

					for (int idx = 0; idx < numOfQuestions; idx++) {
						tempSkipQuestion = new Question();
						tempSkipQuestion.setId(selectedSkipQuestions[idx]);
						questionsToSkip.add(tempSkipQuestion);
					}

					question.getFormQuestionAttributes().setQuestionsToSkip(
							questionsToSkip);
				} else {
					errors.add("Please assign questions to skip in the skip rules tab.");
				}
			} else if (questionAttributesForm.getSkipRuleType() != Integer.MIN_VALUE
					&& questionAttributesForm.getSkipRuleOperatorType() == Integer.MIN_VALUE) {
				errors.add("Please specify a skip rule operator to associate with the skip rule in the skip rules tab.");
				if (questionAttributesForm.getQuestionsToSkip() != null) {
					question.getFormQuestionAttributes().setSkipRuleType(
							SkipRuleType.getByValue(questionAttributesForm
									.getSkipRuleType()));
					question.getFormQuestionAttributes()
							.setSkipRuleOperatorType(
									SkipRuleOperatorType
											.getByValue(questionAttributesForm
													.getSkipRuleOperatorType()));
					question.getFormQuestionAttributes().setSkipRuleEquals(
							questionAttributesForm.getSkipRuleEquals());

					int[] selectedSkipQuestions = questionAttributesForm
							.getQuestionsToSkip();
					int numOfQuestions = selectedSkipQuestions.length;
					List questionsToSkip = new ArrayList();

					Question tempSkipQuestion = null;

					for (int idx = 0; idx < numOfQuestions; idx++) {
						tempSkipQuestion = new Question();
						tempSkipQuestion.setId(selectedSkipQuestions[idx]);
						questionsToSkip.add(tempSkipQuestion);
					}

					question.getFormQuestionAttributes().setQuestionsToSkip(
							questionsToSkip);
				} else {
					errors.add("Please assign questions to skip in the skip rules tab.");
				}
			}

			if (questionAttributesForm.getSkipRuleType() == Integer.MIN_VALUE
					&& questionAttributesForm.getSkipRuleOperatorType() == Integer.MIN_VALUE
					&& questionAttributesForm.getQuestionsToSkip() != null) {
				errors.add("Please specify a skip rule operator and a skip rule to associate with the questions in the skip rules tab.");

				int[] selectedSkipQuestions = questionAttributesForm
						.getQuestionsToSkip();
				int numOfQuestions = selectedSkipQuestions.length;
				List questionsToSkip = new ArrayList();

				Question tempSkipQuestion = null;

				for (int idx = 0; idx < numOfQuestions; idx++) {
					tempSkipQuestion = new Question();
					tempSkipQuestion.setId(selectedSkipQuestions[idx]);
					questionsToSkip.add(tempSkipQuestion);
				}

				question.getFormQuestionAttributes().setQuestionsToSkip(
						questionsToSkip);
			}

			// SET RESPONSE METADATA
			question.getFormQuestionAttributes().setRequired(
					questionAttributesForm.isRequired());

			boolean validMin = false;
			boolean validMax = false;
			try {
				if (questionAttributesForm.getMinCharacters() != null
						&& !questionAttributesForm.getMinCharacters().trim()
								.equalsIgnoreCase("")) {
					Integer.parseInt(questionAttributesForm.getMinCharacters());
					validMin = true;
				}
			} catch (NumberFormatException e) {
				errors.add("Minimum Characters in the Validation tab must be an integer.");
			}
			try {
				if (questionAttributesForm.getMaxCharacters() != null
						&& !questionAttributesForm.getMaxCharacters().trim()
								.equalsIgnoreCase("")) {
					Integer.parseInt(questionAttributesForm.getMaxCharacters());
					validMax = true;
				}
			} catch (NumberFormatException e) {
				errors.add("Maximum Characters in the Validation tab must be an integer.");
			}
			// check max characters is less than 4000
			if (validMax) {
				int maxChars = Integer.parseInt(questionAttributesForm
						.getMaxCharacters());
				if (maxChars > 4000) {
					errors.add("Maximum Characters can not be greater than 4000.");
				}
			}
			if (question.getType().equals(QuestionType.TEXTBOX)
					|| question.getType().equals(QuestionType.TEXTAREA)
					|| question.getType().equals(QuestionType.PATIENT_CALENDAR)) {
				// SINGLE RESPONSE
				if (validMin) {
					question.getFormQuestionAttributes().setMinCharacters(
							Integer.parseInt(questionAttributesForm
									.getMinCharacters()));
					if (question.getFormQuestionAttributes().getMinCharacters() < 0) {
						errors.add("Please enter a value for the Minimum Characters greater or equal to zero.");
					}
				} else {
					question.getFormQuestionAttributes().setMinCharacters(
							Integer.MIN_VALUE);
				}
				if (validMax) {
					question.getFormQuestionAttributes().setMaxCharacters(
							Integer.parseInt(questionAttributesForm
									.getMaxCharacters()));
					if (question.getFormQuestionAttributes().getMaxCharacters() < question
							.getFormQuestionAttributes().getMinCharacters()) {
						errors.add("Please enter a value for the Maximum Characters greater or equal to the value of Minimum Characters.");
					}

					if (question.getDefaultValue() != null
							&& question.getDefaultValue().length() > question
									.getFormQuestionAttributes()
									.getMaxCharacters()) {
						errors.add("Please enter a default value that is not greater than the Maximum Characters specified.");
					}
					if (question.getDefaultValue() != null
							&& question.getDefaultValue().length() < question
									.getFormQuestionAttributes()
									.getMinCharacters()) {
						errors.add("Please enter a default value that is not less than the Minimum Characters specified.");
					}
				} else {
					question.getFormQuestionAttributes().setMaxCharacters(
							Integer.MIN_VALUE);
				}
			} else {
				// MULTIPLE RESPONSE
				List answers = oQuestion.getAnswers();
				boolean skipEqualsFound = false;
				Answer answer = null;
				for (Iterator iter = answers.iterator(); iter.hasNext();) {
					answer = (Answer) iter.next();
					if (question.getFormQuestionAttributes().hasSkipRule()) {
						// skip rule is an equals, check for equals value
						if (question.getFormQuestionAttributes()
								.getSkipRuleEquals() != null) {
							if (answer.getDisplay().trim().equals(
									question.getFormQuestionAttributes().getSkipRuleEquals().trim())) {
								skipEqualsFound = true;
								break;
							}
						}
					}
				}
				if (!skipEqualsFound
						&& question.getFormQuestionAttributes()
								.getSkipRuleEquals() != null) {
					errors.add("Please enter an equals value for the skip rule that is in the list of multiple choice options.");
				}
			}

			// SET HTML ATTRIBUTES
			HtmlAttributes attributes = new HtmlAttributes();
			attributes.setAlign(questionAttributesForm.getAlign());
			attributes.setvAlign(questionAttributesForm.getvAlign());
			attributes.setColor(questionAttributesForm.getColor());
			attributes.setFontFace(questionAttributesForm.getFontFace());
			attributes.setFontSize(questionAttributesForm.getFontSize());
			attributes.setIndent(questionAttributesForm.getIndent());
			question.getFormQuestionAttributes().setHtmlAttributes(attributes);

			question.getFormQuestionAttributes().setRangeOperator(
					questionAttributesForm.getRangeOperator());
			String rangeValue1 = questionAttributesForm.getRangeValue1();
			if (rangeValue1 != null && rangeValue1.equals("")) {
				rangeValue1 = null;
			}
			question.getFormQuestionAttributes().setRangeValue1(rangeValue1);
			String rangeValue2 = questionAttributesForm.getRangeValue2();
			if (rangeValue2 != null && rangeValue2.equals("")) {
				rangeValue2 = null;
			}
			question.getFormQuestionAttributes().setRangeValue2(rangeValue2);

			question.getFormQuestionAttributes().setAnswerType(
					AnswerType.getByValue(questionAttributesForm
							.getAnswerType()));

			question.getFormQuestionAttributes().setId(
					questionAttributesForm.getId());
			question.getFormQuestionAttributes().setSectionId(
					questionAttributesForm.getSectionId());

			question.getFormQuestionAttributes().setHorizontalDisplay(
					questionAttributesForm.isHorizontalDisplay());
			question.getFormQuestionAttributes().setHorizDisplayBreak(
					questionAttributesForm.isHorizDisplayBreak());

			if (questionAttributesForm.getTextareaWidth() < 20
					|| questionAttributesForm.getTextareaWidth() > 900) {
				errors.add("Please ensure the textarea width is a number between 20 and 900");
			}
			question.getFormQuestionAttributes().setTextareaWidth(
					questionAttributesForm.getTextareaWidth());
			if (questionAttributesForm.getTextareaHeight() < 20
					|| questionAttributesForm.getTextareaHeight() > 700) {
				errors.add("Please ensure the textarea height is a number between 20 and 700");
			}
			question.getFormQuestionAttributes().setTextareaHeight(
					questionAttributesForm.getTextareaHeight());
			question.getFormQuestionAttributes().setTextboxLength(
					questionAttributesForm.getTextboxLength());
			if (questionAttributesForm.getTextboxLength() < 2
					|| questionAttributesForm.getTextboxLength() > 100) {
				errors.add("Please ensure the textbox length is between 2 and 100");
			}

			validateEmailTrigger(errors, questionAttributesForm, question);
			question.getFormQuestionAttributes().setDeleteTrigger(
					questionAttributesForm.isDeleteTrigger());

			question.getFormQuestionAttributes().setDataSpring(
					questionAttributesForm.isDataSpring());
			if (!oQuestion.getText().equals(
					questionAttributesForm.getHtmlText())) {
				question.getFormQuestionAttributes().setHtmlText(
						questionAttributesForm.getHtmlText());
			} else {
				question.getFormQuestionAttributes().setHtmlText(null);
			}
			return question;
		} catch (ClassCastException e) {
			throw new AssemblerException(
					"Unable to transform form object to domain object: "
							+ e.getMessage(), e);
		}
	}

	private static void validateEmailTrigger(List errors,
			QuestionAttributesForm questionAttributesForm, Question question) {
		// validates input from form object , if valid, adds email trigger to
		// domain object, question
		if (questionAttributesForm.getEmailTrigger().getToEmailAddress() == null
				|| questionAttributesForm.getEmailTrigger().getToEmailAddress()
						.trim().equals("")) {
			// no toEmail address == no email trigger
			return;
		} else {
			try {
				new InternetAddress(questionAttributesForm.getEmailTrigger()
						.getToEmailAddress(), true);

				question.getFormQuestionAttributes()
						.getEmailTrigger()
						.setToEmailAddress(
								questionAttributesForm.getEmailTrigger()
										.getToEmailAddress());
				question.getFormQuestionAttributes()
						.getEmailTrigger()
						.setId(questionAttributesForm.getEmailTrigger().getId());
				if (questionAttributesForm.getEmailTrigger().getId() == Integer.MIN_VALUE) {
					// creating now
					question.getFormQuestionAttributes()
							.getEmailTrigger()
							.setCreatedBy(
									questionAttributesForm.getEmailTrigger()
											.getUpdatedBy());
				}
				question.getFormQuestionAttributes()
						.getEmailTrigger()
						.setUpdatedBy(
								questionAttributesForm.getEmailTrigger()
										.getUpdatedBy());

			} catch (AddressException ae) {
				errors.add("To Email Address is not a valid email address, please specify only vaild email addresses");
			}
			if (!(questionAttributesForm.getEmailTrigger().getCcEmailAddress() == null || questionAttributesForm
					.getEmailTrigger().getCcEmailAddress().trim().equals(""))) {
				try {

					new InternetAddress(questionAttributesForm
							.getEmailTrigger().getCcEmailAddress(), true);
					question.getFormQuestionAttributes()
							.getEmailTrigger()
							.setCcEmailAddress(
									questionAttributesForm.getEmailTrigger()
											.getCcEmailAddress());
				} catch (AddressException ae) {
					errors.add("CC Email Address is not a valid email address, please specify only vaild email addresses");
				}
			}

			question.getFormQuestionAttributes()
					.getEmailTrigger()
					.setSubject(
							questionAttributesForm.getEmailTrigger()
									.getSubject());
			question.getFormQuestionAttributes()
					.getEmailTrigger()
					.setBody(questionAttributesForm.getEmailTrigger().getBody());
			if (questionAttributesForm.getEmailTrigger().getSelectedAnswers() == null
					|| questionAttributesForm.getEmailTrigger()
							.getSelectedAnswers().length < 1) {
				errors.add("When creating an email trigger, answers to activate the trigger are required");
			} else {
				for (int i = 0; i < questionAttributesForm.getEmailTrigger()
						.getSelectedAnswers().length; i++) {
					EmailTriggerValue etv =
							new EmailTriggerValue(questionAttributesForm.getEmailTrigger().getSelectedAnswers()[i], "");

					question.getFormQuestionAttributes()
							.getEmailTrigger()
							.getTriggerValues().add(etv);
				}
			}
		}

	}

	/**
	 * Transforms a Question Domain Object into a QuestionForm object
	 * 
	 * @param domain
	 *            The Question Domain Object to transform into a QuestionForm
	 *            object
	 * @throws AssemblerException
	 *             Thrown if any error occurs while transforming the Patient
	 *             domain object to the PatientForm object
	 */
	public static void domainToForm(CtdbDomainObject domain, CtdbForm form)
			throws AssemblerException {
		try {
			QuestionAttributesForm questionAttributesForm = (QuestionAttributesForm) form;
			Question question = (Question) domain;
			FormQuestionAttributes qAttrs = question
					.getFormQuestionAttributes();

			// SET STANDARD METADATA
			questionAttributesForm.setId(questionAttributesForm.getId());
			questionAttributesForm.setType(question.getType().getValue());
			questionAttributesForm.setRequired(qAttrs.isRequired());
			questionAttributesForm.setRangeOperator(qAttrs.getRangeOperator());
			questionAttributesForm.setRangeValue1(qAttrs.getRangeValue1());
			questionAttributesForm.setRangeValue2(qAttrs.getRangeValue2());
			questionAttributesForm.setHorizontalDisplay(qAttrs
					.isHorizontalDisplay());
			questionAttributesForm.setHorizDisplayBreak(qAttrs
					.isHorizDisplayBreak());
			questionAttributesForm.setTextareaWidth(qAttrs.getTextareaWidth());
			questionAttributesForm
					.setTextareaHeight(qAttrs.getTextareaHeight());
			questionAttributesForm.setTextboxLength(qAttrs.getTextboxLength());

			// SET SKIP RULE
			if (qAttrs.hasSkipRule()) {
				questionAttributesForm.setSkipRuleEquals(qAttrs
						.getSkipRuleEquals());
				questionAttributesForm.setSkipRuleOperatorType(qAttrs
						.getSkipRuleOperatorType().getValue());
				questionAttributesForm.setSkipRuleType(qAttrs.getSkipRuleType()
						.getValue());
			}

			questionAttributesForm.setAnswerType(qAttrs.getAnswerType()
					.getValue());

			if (qAttrs.getMinCharacters() != Integer.MIN_VALUE) {
				questionAttributesForm.setMinCharacters(Integer.toString(qAttrs
						.getMinCharacters()));
			} else {
				questionAttributesForm.setMinCharacters(null);
			}
			if (qAttrs.getMaxCharacters() != Integer.MIN_VALUE) {
				questionAttributesForm.setMaxCharacters(Integer.toString(qAttrs
						.getMaxCharacters()));
			} else {
				questionAttributesForm.setMaxCharacters(null);
			}

			// SET HTML ATTRIBUTES
			questionAttributesForm.setAlign(qAttrs.getHtmlAttributes()
					.getAlign());
			questionAttributesForm.setvAlign(qAttrs.getHtmlAttributes()
					.getvAlign());
			questionAttributesForm.setColor(qAttrs.getHtmlAttributes()
					.getColor());
			questionAttributesForm.setFontFace(qAttrs.getHtmlAttributes()
					.getFontFace());
			questionAttributesForm.setFontSize(qAttrs.getHtmlAttributes()
					.getFontSize());
			questionAttributesForm.setIndent(qAttrs.getHtmlAttributes()
					.getIndent());

			// email trigger
			if (qAttrs.getEmailTrigger().getToEmailAddress() != null) {
				// to Email address is required, if not there dont bother
				questionAttributesForm.getEmailTrigger().setToEmailAddress(
						qAttrs.getEmailTrigger().getToEmailAddress());
				questionAttributesForm.getEmailTrigger().setCcEmailAddress(
						qAttrs.getEmailTrigger().getCcEmailAddress());
				questionAttributesForm.getEmailTrigger().setBody(
						qAttrs.getEmailTrigger().getBody());
				questionAttributesForm.getEmailTrigger().setSubject(
						qAttrs.getEmailTrigger().getSubject());

				List<String> strList = new ArrayList<String>();
				Set<EmailTriggerValue> etvList = qAttrs.getEmailTrigger().getTriggerValues();
				String[] strAry = new String[etvList.size()];
				for (EmailTriggerValue etv : etvList) {
					strList.add(etv.getAnswer());
				}
				strAry = strList.toArray(strAry);
				questionAttributesForm.getEmailTrigger().setSelectedAnswers(
						strAry);

				// questionAttributesForm.getEmailTrigger().setSelectedAnswers(qAttrs.getEmailTrigger().getTriggerAnswers().toArray(questionAttributesForm.getEmailTrigger().getSelectedAnswers()));
				questionAttributesForm.getEmailTrigger().setId(
						qAttrs.getEmailTrigger().getId());
			}

			questionAttributesForm.setDataSpring(qAttrs.isDataSpring());

			// set html text
			if (StringUtils.isNotEmpty(qAttrs.getHtmlText())) {
				questionAttributesForm.setHtmlText(qAttrs.getHtmlText());
			} else {
				questionAttributesForm.setHtmlText(question.getText());
			}

		} catch (Exception e) {
			throw new AssemblerException(
					"Unable to tranform domain object to question attributes form object: "
							+ e.getMessage(), e);
		}
	}

	/**
	 * replace the bogus section id to real section id
	 * 
	 */
	private static String replaceSectionBogusId(Map bogusNewSectionIDMap, String targetString) {
		if (targetString != null && targetString.length() > 0) {
			ArrayList<Integer> s_indicesList = new ArrayList<Integer>();
			ArrayList<Integer> q_indicesList = new ArrayList<Integer>();
			ArrayList<String> stringRealSectionIds = new ArrayList<String>();
			int index = targetString.indexOf("S_");
			while (index != -1) {
				s_indicesList.add(Integer.valueOf(index + 2));
				index = targetString.indexOf("S_", index + 1);
			}
			if (s_indicesList.size() > 0) {
				for (int m = 0; m < s_indicesList.size(); m++) {
					int s_ind = s_indicesList.get(m).intValue();
					int q_ind = targetString.indexOf("_Q", s_ind);
					q_indicesList.add(Integer.valueOf(q_ind));
					String bSecId = targetString.substring(s_ind, q_ind);
					// get real sectionid here!
					if (bogusNewSectionIDMap.get("S_" + bSecId) != null) {
						String rSecId = String
								.valueOf(((Integer) bogusNewSectionIDMap
										.get("S_" + bSecId)).intValue());
						stringRealSectionIds.add(rSecId);
					} else {
						stringRealSectionIds.add("none");
					}
				}

				for (int m = s_indicesList.size() - 1; m >= 0; m--) {
					int s_ind = s_indicesList.get(m).intValue();
					int q_ind = q_indicesList.get(m).intValue();
					String sId = stringRealSectionIds.get(m);
					if (!sId.equals("none")) {
						targetString = targetString.substring(0, s_ind)
								+ sId
								+ targetString.substring(q_ind,
										targetString.length());
					} else {

					}
				}
			}
		}
		return targetString;
	}
	
	public static Form createFormObjectFromXML(HttpServletRequest request,FormExportImport fe) 
			throws DuplicateObjectException, CtdbException {
		User user = (User) request.getSession().getAttribute(CtdbConstants.USER_SESSION_KEY);
		Protocol study=(Protocol)request.getSession().getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		//Import Form from XML
		Form f = new Form();
		FormInfoExportImport fexportImport = fe.getFormInfo();
		//String timeStamp = new SimpleDateFormat("yyyyMMddhhmm").format(new Date());
		//String fromName = fexportImport.getName()+timeStamp;
		String fromName = fexportImport.getName();
		f.setName(fromName.substring(0,Math.min(fromName.length(), 50)) ); // 50 Max. for form name
		f.setDescription(fexportImport.getDescription());
		//status will be inactive when it's first imported
		f.setStatus(new CtdbLookup(1));
		f.setCreatedBy(user.getId());
		f.setUpdatedBy(user.getId());
		//Set version Object for form
		f.setVersion(new Version(1)); // default
		f.setCopyRight(fexportImport.isCopyRight());
		f.setAllowMultipleCollectionInstances(fexportImport.isAllowMultipleCollectionInstances());
		f.setLockFlag(fexportImport.isLockFlag());
		
		//As part of new form builder new data structure name and version also will be need to be imported
		f.setDataStructureName(fexportImport.getDataStructureName());
		f.setDataStructureVersion(fexportImport.getDataStructureVersion());
		
		FormHtmlAttributes fHTMLatt = new FormHtmlAttributes();
		fHTMLatt.setFormBorder(fexportImport.isFormBorder());
		fHTMLatt.setSectionBorder(fexportImport.isSectionBorder());
		fHTMLatt.setFormColor(fexportImport.getFormColor());
		fHTMLatt.setSectionColor(fexportImport.getSectionColor());
		fHTMLatt.setFormFont(fexportImport.getFormFont());
		fHTMLatt.setSectionFont(fexportImport.getSectionFont());
		fHTMLatt.setFormFontSize(fexportImport.getFormFontSize());
		fHTMLatt.setCellpadding(fexportImport.getCellpadding());
		f.setFormHtmlAttributes(fHTMLatt);
		f.setProtocolId(study.getId());
		f.setOrderValue(fexportImport.getOrderValue());
		f.setSingleDoubleKeyFlag(fexportImport.getSingleDoubleKeyFlag());
		f.setAccessFlag(fexportImport.getAccessFlag());
		f.setFormHeader(fexportImport.getFormHeader());
		f.setFormFooter(fexportImport.getFormFooter());
		f.setDataEntryWorkflow(DataEntryWorkflowType.getByValue(fexportImport.getFormDataEntryWorkFlowTypeIntVal()));
		f.setAttachFiles(fexportImport.isAttachFiles());
		f.setDataSpring(fexportImport.isDataSpring());
		f.setTabDisplay(fexportImport.isTabDisplay());
		f.setFormType(fexportImport.getFormType());
		
		ArrayList<SectionExportImport> sectionExportList = fexportImport.getSections();
		List <Section> sectionList = new ArrayList <Section>();
		
		for ( SectionExportImport sectionExpImport : sectionExportList ) {
			Section formSection = new Section();
			List <Question>questionList  = new ArrayList<Question>();
			formSection.setId(sectionExpImport.getSectionId());
			formSection.setFormId(fexportImport.getFromId());
			formSection.setName(sectionExpImport.getName());
			formSection.setDescription(sectionExpImport.getDescription());
			formSection.setCreatedBy(user.getId());
			formSection.setUpdatedBy(user.getId());
			formSection.setOrderValue(sectionExpImport.getOrderValue());
			formSection.setRow(sectionExpImport.getRow());
			formSection.setCol(sectionExpImport.getCol());
			formSection.setTextDisplayed(sectionExpImport.isTextDisplayed());
			formSection.setInstructionalText(sectionExpImport.getInstructionalText());
			formSection.setAltLabel(sectionExpImport.getAltLabel());
			formSection.setIntob(sectionExpImport.isIntob());
			formSection.setCollapsable(sectionExpImport.isCollapsable());
			formSection.setRepeatable(sectionExpImport.isRepeatable());
			formSection.setInitRepeatedSections(sectionExpImport.getInitRepeatedSections());
			formSection.setMaxRepeatedSections(sectionExpImport.getMaxRepeatedSections());
			formSection.setRepeatedSectionParent(sectionExpImport.getRepeatedSectionParent());
			formSection.setGridtype(sectionExpImport.isGridtype());
			formSection.setTableGroupId(sectionExpImport.getTableGroupId());
			
			// Create Section Question data with default form_question_attributes
			ArrayList<QuestionSectionExportImport> sectionQuestionList = sectionExpImport.getSectionQuestion();
			for(QuestionSectionExportImport qsei:sectionQuestionList){
				Question q = (Question)FormQuestionAttributesAssembler.xmlToQuestionDomain(qsei);
				q.getFormQuestionAttributes().setCreatedBy(user.getId());
				q.getFormQuestionAttributes().setUpdatedBy(user.getId());
				if(q.getFormQuestionAttributes().getEmailTrigger().getId() != Integer.MIN_VALUE){
					q.getFormQuestionAttributes().getEmailTrigger().setCreatedBy(user.getId());
					q.getFormQuestionAttributes().getEmailTrigger().setUpdatedBy(user.getId());
				}
				if(qsei.getQuestionsToSkip() != null  && qsei.getQuestionsToSkip().size() >0){
					q.getFormQuestionAttributes().setQuestionsToSkip(convertToSkippedQuestionList(qsei.getQuestionsToSkip()) ) ;
				}
				questionList.add(q);
			}
			formSection.setQuestionList(questionList);
			sectionList.add(formSection);
		}
		f.getRowList().add(sectionList);
		String formValidationStr = f.validateFormInformation();
		if (!formValidationStr.isEmpty()) {
			throw new CtdbException("Form failed the validation:" + formValidationStr);
		}
		return f;
	}
	
	public static List <Question> convertToSkippedQuestionList(List<String> qs){
		List <Question> qList = new ArrayList <Question>();
		Pattern p = Pattern.compile("S_(\\d+)_Q_(\\d+)");
		for (String sqStr: qs){
			Matcher m = p.matcher(sqStr);
			if(m.find()){
				Question skippedQ  = new Question();
				skippedQ.setSectionId(Integer.valueOf( m.group(1)) );
				skippedQ.setId(Integer.valueOf( m.group(2)) );
				qList.add(skippedQ);
			}
		}
		return qList;
	}
}

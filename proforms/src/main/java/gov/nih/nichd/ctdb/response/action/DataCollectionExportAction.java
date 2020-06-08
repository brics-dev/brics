package gov.nih.nichd.ctdb.response.action;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.struts2.ServletActionContext;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.form.domain.DataCollectionExport;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.domain.FormQuestionAttributes;
import gov.nih.nichd.ctdb.form.domain.Section;
import gov.nih.nichd.ctdb.form.manager.FormManager;
import gov.nih.nichd.ctdb.form.util.FormDataStructureUtility;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.domain.SkipRuleOperatorType;
import gov.nih.nichd.ctdb.question.domain.SkipRuleType;
import gov.nih.nichd.ctdb.response.manager.ResponseManager;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.nichd.ctdb.common.CtdbConstants;


public class DataCollectionExportAction extends BaseAction {
	private static final long serialVersionUID = -3114723249529471699L;
	
	

	public String validateExportDataCollection() throws IOException, CtdbException {
		String output;
		
		FormManager fm = new FormManager();
		
		// get list of selected aformIds from request
		String aformIdsString = request.getParameter("aformIds");
		// convert it to int array
		String[] aformIdsStringArray = aformIdsString.split(",");
		int[] aformIds = new int[aformIdsStringArray.length];
		for (int i = 0; i < aformIds.length; i++) {
			aformIds[i] = Integer.valueOf(aformIdsStringArray[i]);
		}

		// verify the selected ids refer to the same form and all are locked
		if (!fm.areAllAFormsTheSameEForm(aformIds)) {
			output = "{\"status\":\"error\", \"message\":\"You cannot export collections for different forms\"}";
		}
		else if (!SysPropUtil.getProperty("guid_with_non_pii").equals("0") && !fm.areAllAFormsLocked(aformIds)) {
			output = "{\"status\":\"error\", \"message\":\"All forms in an export must be locked\"}";
		}
		else {
			output = "{\"status\":\"ok\"}";
		}
		
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("application/json");
	    response.getWriter().write(output);
	
		return null;
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception
	 */
	public String exportDataCollection() throws Exception {
		// initialize managers
		ResponseManager rm = new ResponseManager();
		FormManager fm = new FormManager();
		FormDataStructureUtility fsUtil = new FormDataStructureUtility();
		HttpServletResponse response = ServletActionContext.getResponse();
		response.resetBuffer();

		// get list of selected aformIds from request
		String aformIdsString = request.getParameter("aformIds");

		// convert it to int array
		String[] aformIdsStringArray = aformIdsString.split(",");
		int[] aformIds = new int[aformIdsStringArray.length];
		for (int i = 0; i < aformIds.length; i++) {
			aformIds[i] = Integer.valueOf(aformIdsStringArray[i]);
		}

		// sort the array in ascending order...this should, as a result, sort them by date
		Arrays.sort(aformIds);

		// just use the first aformid to get the form
		int eformId = rm.getFormId(aformIds[0]);
		String shortName = fm.getEFormShortNameByAFormId(aformIds[0]);
		Form form = fsUtil.getEformFromBrics(request, shortName);
		form.setId(eformId);

		//Added for Populating default value for disabled questions due to skip rule.
		Map<Question, List<String>> skipQuestionsMap = getAllQuestionWithSkipRule(form);
		List<String> skipQuestions = new ArrayList<String>();
		List<Integer> skipRuleQuestions = new ArrayList<Integer>();
		Map<Integer, Question> skipRuleQuestionsMap = new HashMap<Integer, Question>();
		Map<Question, DataCollectionExport> questionToDCEMap = new HashMap<Question, DataCollectionExport>();
		for (Question primaryQuestion : skipQuestionsMap.keySet()) {
			skipQuestions.addAll(skipQuestionsMap.get(primaryQuestion));
			skipRuleQuestions.add(primaryQuestion.getId());
			skipRuleQuestionsMap.put(primaryQuestion.getId(), primaryQuestion);
		}
		
		// get list of all repeatable section parents in the form...this represents how many different repeatable groups
		// there are
		List<Integer> repeatableSectionParentIds = fsUtil.getRepeableSectionParentSectionIds(form);

		// for each repeatable section parent, find out max repeated section showing from visibleadminsteredsection
		// based on all the aformids for each repeatable section parent
		HashMap<Integer, Integer> maxRepeatedSectionMap = new HashMap<>();
		for (Integer repeatedSectionParentId : repeatableSectionParentIds) {
			int max = rm.getMaxRepeatedSectionsForAdminForms(form, aformIds, repeatedSectionParentId);
			maxRepeatedSectionMap.put(repeatedSectionParentId, Integer.valueOf(max));
		}

		// set up response out
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss");
		String formName = form.getName();
		formName = formName.replaceAll("\\s", "");
		String fileName = "exportCollections_" + formName + "_" + df.format(new Date()) + ".csv";
		
		response.setHeader("Content-Disposition", "attachment;filename=" + fileName);
		StringBuilder outputString = new StringBuilder("");

		// gets all the data for each aformId
		HashMap<Integer, HashMap<String, DataCollectionExport>> aformIdsDCEMap =
				rm.getDataCollectionExport(aformIds, form);

		// iterate over each aform in this hashmap
		int aformCounter = 0;
		for(Integer aformIdInteger : aformIdsDCEMap.keySet()) {
			
			// get the locked data which is in hashmap with section_question as key for the aformId
			HashMap<String, DataCollectionExport> dceMap = aformIdsDCEMap.get(aformIdInteger);

			// if there are no answers, dont need to try and write anything
			if (dceMap.keySet().isEmpty()) {
				continue;
			}

			// get the values out of the map and sort by the order of each question in the form
			List<DataCollectionExport> dceList = new ArrayList<>(dceMap.values());
			
			Collections.sort(dceList, (DataCollectionExport o1, DataCollectionExport o2) -> {
				return Integer.valueOf(o1.getOrder()).compareTo(Integer.valueOf(o2.getOrder()));
			});

			// write out column headers
			if (aformCounter == 0) {
				StringBuffer columnHeadersSB = new StringBuffer();
				int repeatedSectionCounter = 1;
				Integer repeatedSectionParentKey = null;
				for (int k = 0; k < dceList.size(); k++) {
					DataCollectionExport dce = dceList.get(k);
					boolean isRepeatable = dce.isRepeatable();
					int sectionId = dce.getSectionId();
					int repeatedSectionParentId = dce.getRepeatedSectionParent();
					int questionOrder = dce.getQuestionOrder();

					if (isRepeatable && repeatedSectionParentId == -1) {
						repeatedSectionCounter = 1;
						repeatedSectionParentKey = sectionId;
					} else if (isRepeatable && repeatedSectionParentId != -1 && questionOrder == 1) {
						repeatedSectionCounter++;
						repeatedSectionParentKey = repeatedSectionParentId;
					}

					String columnLabel = dce.getColumnLabel();
					
					if (skipRuleQuestions.contains(dce.getQuestionId())) {
						questionToDCEMap.put(skipRuleQuestionsMap.get(dce.getQuestionId()), dce);
					}
						

					if (!isRepeatable) {
						columnLabel = StringEscapeUtils.escapeCsv(columnLabel);
						columnHeadersSB.append(columnLabel);
						columnHeadersSB.append(",");
					} else {
						int max = maxRepeatedSectionMap.get(repeatedSectionParentKey);
						if (repeatedSectionCounter <= max) {
							columnLabel = StringEscapeUtils.escapeCsv(columnLabel);
							columnHeadersSB.append(columnLabel);
							columnHeadersSB.append(",");
						}
					}
				}

				// remove last ,
				columnHeadersSB.deleteCharAt(columnHeadersSB.length() - 1);
				outputString.append(columnHeadersSB.toString()).append(System.lineSeparator());
			}

			// write out the data
			StringBuffer dataSB = new StringBuffer();
			int repeatedSectionCounter = 1;
			Integer repeatedSectionParentKey = null;
			for (int k = 0; k < dceList.size(); k++) {
				DataCollectionExport dce = dceList.get(k);
				String submitAnswer = dce.getSubmitAnswer();
				boolean isRepeatable = dce.isRepeatable();
				int sectionId = dce.getSectionId();
				String strSecQuesId= "S_" + dce.getSectionId() + "_Q_" + dce.getQuestionId();
				
				
				
				int repeatedSectionParentId = dce.getRepeatedSectionParent();
				int questionOrder = dce.getQuestionOrder();
				if (isRepeatable && repeatedSectionParentId == -1) {
					repeatedSectionCounter = 1;
					repeatedSectionParentKey = sectionId;
				} else if (isRepeatable && repeatedSectionParentId != -1 && questionOrder == 1) {
					repeatedSectionCounter++;
					repeatedSectionParentKey = repeatedSectionParentId;
				}
				Map<Integer,String>  qWithDefaultValue = getQuestionsWithdefaultValues(form);

				if (!isRepeatable) {
					if (submitAnswer != null) {
						submitAnswer = StringEscapeUtils.escapeCsv(submitAnswer);
						dataSB.append(submitAnswer);
						dataSB.append(",");
					} else {
						if (skipQuestions.contains(strSecQuesId) && (!qWithDefaultValue.keySet().contains(dce.getQuestionId()))) {
							String str = fillDefaultSkipValue(skipQuestionsMap, dce, questionToDCEMap);
							String appendStr=StringEscapeUtils.escapeCsv(str);
							dataSB.append(appendStr);
						} else if (qWithDefaultValue.keySet().contains(dce.getQuestionId())){
							String defValue = qWithDefaultValue.get(dce.getQuestionId());
							defValue = StringEscapeUtils.escapeCsv(defValue);
							dataSB.append(defValue);
							dataSB.append(",");		

						}else
						 {
							dataSB.append("");
						}
						dataSB.append(",");
					}

				} else {
					int max = maxRepeatedSectionMap.get(repeatedSectionParentKey);
					if (repeatedSectionCounter <= max) {
						if (submitAnswer != null) {
							submitAnswer = StringEscapeUtils.escapeCsv(submitAnswer);
							dataSB.append(submitAnswer);
							dataSB.append(",");
						} else if (qWithDefaultValue.keySet().contains(dce.getQuestionId())){
							String defValue = qWithDefaultValue.get(dce.getQuestionId());
							defValue = StringEscapeUtils.escapeCsv(defValue);
							dataSB.append(defValue);
							dataSB.append(",");
						}else{
							if(skipQuestions.contains(strSecQuesId) && (!qWithDefaultValue.keySet().contains(dce.getQuestionId()))) {
								String str = fillDefaultSkipValue(skipQuestionsMap, dce, questionToDCEMap);
								String appendStr=StringEscapeUtils.escapeCsv(str);
								dataSB.append(appendStr);
							} else {
								dataSB.append("");
							}
							dataSB.append(",");
						}
					}
				}
			}

			// remove last ,
			dataSB.deleteCharAt(dataSB.length() - 1);

			outputString.append(dataSB.toString()).append(System.lineSeparator());

			aformCounter++;
		}

		response.getWriter().write(outputString.toString());
		return null;

	}

	//Added for Populating default value for disabled questions due to skip rule.
	public Map<Question, List<String>> getAllQuestionWithSkipRule( Form form) throws CtdbException {
	

        Map<Question, List<String>> skipRuleQuestionsMap = new HashMap<Question, List<String>>();
        
        // get all allQuestionsInForm in the form
        for ( List<Section> row : form.getRowList() ) {
        	for ( Section section : row ) {
        		if (section != null) {
                	List<Question> secQuestionList = section.getQuestionList();
                	if ( secQuestionList != null ) {
                		for ( Question q : secQuestionList ) {

                			FormQuestionAttributes fqa = q.getFormQuestionAttributes();
                			if (fqa.hasSkipRule()) {

                		        List<String> allQuestionIdsInForm = new ArrayList<String>();
                				skipRuleQuestionsMap.put(q, allQuestionIdsInForm);
                				List<Question> skipQList = fqa.getQuestionsToSkip();
                				for ( Question skipQs : skipQList ) {
                					
                					String strSQID = "S_" + skipQs.getSkipSectionId() + "_Q_" + skipQs.getId();
                					
                					allQuestionIdsInForm.add(strSQID);
 
                				}
                			}
                			
                		}
                	}
                	
                }
            }
        }
        return skipRuleQuestionsMap;
	}

	///Added for Populating default value for disabled questions due to skip rule.
	private String fillDefaultSkipValue(Map<Question, List<String>> skipQuestionsMap, DataCollectionExport dce, Map<Question, DataCollectionExport> questionToDCEMap) {
		String returnValue = "";
		for (Question primaryQuestion : skipQuestionsMap.keySet()) {
			
			String strDceSQ = "S_" + dce.getSectionId() + "_Q_" + dce.getQuestionId();
			
			if (skipQuestionsMap.get(primaryQuestion).contains(strDceSQ)) {
				
				FormQuestionAttributes questionAttributes = primaryQuestion.getFormQuestionAttributes();
				SkipRuleType skipType = questionAttributes.getSkipRuleType();
				SkipRuleOperatorType skipOperatorType = questionAttributes.getSkipRuleOperatorType();
				String skipEquals = questionAttributes.getSkipRuleEquals();

				// Validate and return the value.
				if (skipType == SkipRuleType.DISABLE) {
					DataCollectionExport dce1 = questionToDCEMap.get(primaryQuestion);
					String[] svalues = null;
					if (skipEquals != null && skipEquals.indexOf("|") > -1) {
						svalues = skipEquals.split("\\|");
					}
					if (svalues == null && skipEquals != null) {
						svalues = new String[] {skipEquals};
					}
					if (skipOperatorType == SkipRuleOperatorType.CONTAINS) {
						if (svalues != null) {
							for(String svalue: svalues) {
								if (dce1.getSubmitAnswer().trim().toLowerCase().contains(svalue.trim().toLowerCase())) {
									// Condition Satisfied
									if (dce.getSubmitAnswer() == null) {
										// All good Set default value.
										returnValue = CtdbConstants.EXPORT_DEFAULT_SKIP_DISABLE_VALUE;
										break;
									}
								}
							}
						}
					} else if (skipOperatorType == SkipRuleOperatorType.EQUALS) {
						if (svalues != null) {
							for(String svalue: svalues) {
								if (dce1.getSubmitAnswer().trim().toLowerCase().equals(svalue.trim().toLowerCase())) {
									if (dce.getSubmitAnswer() == null) {
										returnValue = CtdbConstants.EXPORT_DEFAULT_SKIP_DISABLE_VALUE;
										break;
									}
								}
							}
						}
					} else if (skipOperatorType == SkipRuleOperatorType.GREATER_THAN) {
						if (!Utils.isBlank(dce1.getSubmitAnswer()) && !Utils.isBlank(skipEquals) && Utils.isNumeric(dce1.getSubmitAnswer())) {
							float floatValue = Float.parseFloat(dce1.getSubmitAnswer().trim());
							float floatSvalue = Float.parseFloat(skipEquals.trim());
							if (floatValue > floatSvalue) {
								if (dce.getSubmitAnswer() == null) {
									returnValue = CtdbConstants.EXPORT_DEFAULT_SKIP_DISABLE_VALUE;
									break;
								}
							}
						}
					} else if (skipOperatorType == SkipRuleOperatorType.GREATER_THAN_EQUAL_TO) {
						if (!Utils.isBlank(dce1.getSubmitAnswer()) && !Utils.isBlank(skipEquals) && Utils.isNumeric(dce1.getSubmitAnswer())) {
							float floatValue = Float.parseFloat(dce1.getSubmitAnswer().trim());
							float floatSvalue = Float.parseFloat(skipEquals.trim());
							if (floatValue >= floatSvalue) {
								if (dce.getSubmitAnswer() == null) {
									returnValue = CtdbConstants.EXPORT_DEFAULT_SKIP_DISABLE_VALUE;
									break;
								}
							}
						}
					} else if (skipOperatorType == SkipRuleOperatorType.HAS_ANY_VALUE) {
						if (dce.getSubmitAnswer() == null) {
							returnValue = CtdbConstants.EXPORT_DEFAULT_SKIP_DISABLE_VALUE;
							break;
						}
					} else if (skipOperatorType == SkipRuleOperatorType.IS_BLANK) {
						if (dce1.getSubmitAnswer() == null || dce1.getSubmitAnswer().trim().equals("")) {
							if (dce.getSubmitAnswer() == null) {
								returnValue = CtdbConstants.EXPORT_DEFAULT_SKIP_DISABLE_VALUE;
								break;
							}
						}
					} else if (skipOperatorType == SkipRuleOperatorType.LESS_THAN) {
						if (!Utils.isBlank(dce1.getSubmitAnswer()) && !Utils.isBlank(skipEquals) && Utils.isNumeric(dce1.getSubmitAnswer())) {
							float floatValue = Float.parseFloat(dce1.getSubmitAnswer().trim());
							float floatSvalue = Float.parseFloat(skipEquals.trim());
							if (floatValue < floatSvalue) {
								if (dce.getSubmitAnswer() == null) {
									returnValue = CtdbConstants.EXPORT_DEFAULT_SKIP_DISABLE_VALUE;
									break;
								}
							}
						}
					} else if (skipOperatorType == SkipRuleOperatorType.LESS_THAN_EQUAL_TO) {
						if (!Utils.isBlank(dce1.getSubmitAnswer()) && !Utils.isBlank(skipEquals)
								&& Utils.isNumeric(dce1.getSubmitAnswer())) {
							float floatValue = Float.parseFloat(dce1.getSubmitAnswer().trim());
							float floatSvalue = Float.parseFloat(skipEquals.trim());
							if (floatValue <= floatSvalue) {
								if (dce.getSubmitAnswer() == null) {
									returnValue = CtdbConstants.EXPORT_DEFAULT_SKIP_DISABLE_VALUE;
									break;
								}
							}
						}
					}
				}
			}
		}

		return returnValue;
	}
	
	
	
	private Map<Integer,String> getQuestionsWithdefaultValues(Form form) throws CtdbException {
		
        Map<Integer,String> DefaultValueQuestionsList = new HashMap<Integer,String>();
        
        // get all allQuestionsInForm in the form
        for ( List<Section> row : form.getRowList() ) {
        	for ( Section section : row ) {
        		if (section != null) {
                	List<Question> secQuestionList = section.getQuestionList();
                	if ( secQuestionList != null ) {
                		for ( Question q : secQuestionList ) {
               			String QuestionDefaultValue= "";
                			QuestionDefaultValue = q.getDefaultValue();
                			if(!QuestionDefaultValue.equals("")) {
                  				DefaultValueQuestionsList.put(q.getId(),q.getDefaultValue());
                 				}
                 		}
		
                	}
        		}
        	}
        }
		return DefaultValueQuestionsList;
	}
}

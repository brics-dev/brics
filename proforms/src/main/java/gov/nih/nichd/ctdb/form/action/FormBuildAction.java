package gov.nih.nichd.ctdb.form.action;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import gov.nih.nichd.ctdb.common.AssemblerException;
import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.Version;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.form.common.FormQuestionAttributesAssembler;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.domain.FormQuestionAttributes;
import gov.nih.nichd.ctdb.form.domain.Section;
import gov.nih.nichd.ctdb.form.manager.FormManager;
import gov.nih.nichd.ctdb.form.util.FormDataStructureUtility;
import gov.nih.nichd.ctdb.question.domain.Answer;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.domain.QuestionType;
import gov.nih.nichd.ctdb.question.domain.VisualScale;
import gov.nih.nichd.ctdb.question.manager.QuestionManager;
import gov.nih.nichd.ctdb.response.manager.ResponseManager;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.util.common.Message;
import gov.nih.nichd.ctdb.util.common.MessageHandler;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;

public class FormBuildAction extends BaseAction {
	
	private static final long serialVersionUID = 3271708126475385290L;
	private static Logger logger = Logger.getLogger(FormBuildAction.class);
	
	/**
     * Struts Constant used to set/get ActionMessages for this action from session.
     * Used for the redirect to the main listing page for this functionality.
     */
    public static final String ACTION_MESSAGES_KEY = "FormBuildAction_ActionMessages";
    
	private String formMode;
	private String formBuildFormId;
	private String sectionsJSON;
	private String questionsJSON;
	private String existingSectionIdsToDeleteJSON;
	private String existingQuestionIdsToDeleteJSON;
	
	/**
	 * 
	 */
	public String execute() throws Exception {
		JSONArray questionsJSONArr = new JSONArray(questionsJSON);
		
		// need to build the version table which is essentially a list of 
		// the questionId and the version chosen
		Map<String, String> versionTable = new HashMap<String, String>();
		
		if (questionsJSONArr.length() > 0) {
			for (int k = 0; k < questionsJSONArr.length(); k++) {
				JSONObject addQuestionJSON = questionsJSONArr.getJSONObject(k);
				int questionId = addQuestionJSON.getInt("questionId");
				String questionVersionLetter = addQuestionJSON.getString("questionVersionLetter");
				versionTable.put(String.valueOf(questionId), questionVersionLetter);
			} 
		 }
		
		FormManager fm = new FormManager();
		FormDataStructureUtility fsUtil = new FormDataStructureUtility();
		
		int formId = Integer.parseInt(formBuildFormId);
		Form form = fm.getFormAndSetofQuestions(formId);
		
		logger.info("the mode in FormBuildAction is " + formMode);
		MessageHandler messageHandler = new MessageHandler(request);
		messageHandler.clearAll();
		
		try {
 			if (formMode.equals("create")) {
 				this.createForm(versionTable, form);
				messageHandler.addMessage( new Message (getText(StrutsConstants.SUCCESS_ADD_KEY, new String[]{" form, " + form.getName() + ","}),
											MessageHandler.MESSAGE_TYPE_MESSAGE) );
				
				String dsName = form.getDataStructureName();
				FormStructure fs = fsUtil.getDataStructureFromWebService(dsName, request);
				boolean doesFSContainDeprecatedOrRetiredDEs = fsUtil.doesFSContainDeprecatedOrRetiredDEs(fs);
				if(doesFSContainDeprecatedOrRetiredDEs) {
					messageHandler.addMessage(new Message(getText(StrutsConstants.CONTAINS_DEPRECATED_OR_RETIRED_DES, Arrays.asList("")),  MessageHandler.MESSAGE_TYPE_WARNING));
				}
			}
 			else if (formMode.equals("edit")) {
				this.editForm(versionTable, form);
				messageHandler.addMessage( new Message (getText(StrutsConstants.SUCCESS_EDIT_KEY, new String[]{" form, " + form.getName() + ","}),
						MessageHandler.MESSAGE_TYPE_MESSAGE) );
			}
 			
			session.remove("qustionMap");

			//need to do this b/c we need all the created sections from save and edit
			Form savedForm = fm.getFormAndSections(formId); 
			FormStructure ds = fsUtil.getDataStructureFromWebService(savedForm.getDataStructureName(), request);
			boolean isAllReqDEPresent = fsUtil.validateRequiredDataElements(savedForm, ds);

			if (!isAllReqDEPresent) {
				messageHandler.addMessage(new Message(getText(StrutsConstants.VALIDATE_REQUIRED_DE_WARNING, new String[]{""}),
						MessageHandler.MESSAGE_TYPE_WARNING));
			}
			
			messageHandler.save(request);
		}
		catch (Exception e) {
			logger.error("An error occurred while saveing form " + formBuildFormId + ".", e);
			
			// Add error message to the message handler.
			String message = getText(StrutsConstants.ERROR_DATA_SAVE, 
				new String[]{getText("form.forms.formInfo.FormDisplay").toLowerCase(request.getLocale())});
			addActionError(message);
			session.put(FormAction.ACTION_ERRORS_KEY, getActionErrors());
			
			return BaseAction.ERROR;
		}
			 
		if (form.getFormType() == 10) {
			session.put("formTypeToDisplay", "Subject");
		}
		else {
			session.put("formTypeToDisplay", "nonSubject");
		}
		
		session.remove("QustionList");
		return SUCCESS;
	}
	
	/**
	 * 
	 * 
	 * @param versionTable - A list of the questionId and the version chosen.
	 * @param form
	 * @throws Exception
	 */
	private void createForm(Map<String, String> versionTable, Form form) throws Exception {
	
		JSONArray sectionsJSONArr = new JSONArray(URLDecoder.decode(sectionsJSON, "UTF-8"));
		//JSONArray questionsJSONArr = new JSONArray(URLDecoder.decode(questionsJSON, "UTF-8"));
		JSONArray questionsJSONArr = new JSONArray(questionsJSON);
		
		int formId = Integer.parseInt(formBuildFormId);
		User user = getUser();
		FormManager fm = new FormManager();
		QuestionManager qm = new QuestionManager();

		// first determine number of rows in the form
		int numRows = 0;
		for (int i=0; i<sectionsJSONArr.length(); i++) {
			JSONObject addSectionJSON = sectionsJSONArr.getJSONObject(i);
			int row = addSectionJSON.getInt("row");
			if (row > numRows) {
				numRows = row;
			}
		}
		
		// now determine number of cols in each row and then insert that row with num cols
		for (int i=0; i<numRows; i++) {
			int numCols = 0;
			for (int k=0; k<sectionsJSONArr.length(); k++) {
				JSONObject addSectionJSON = sectionsJSONArr.getJSONObject(k);
				int row = addSectionJSON.getInt("row");
				if (row==i+1) {
					int col = addSectionJSON.getInt("col");
					if (col > numCols) {
						numCols = col;
					}
				}
			}
			fm.addRow(formId, numCols);
		}
		
		//first lets update core question 
		JSONArray uniqueQuestionsJSONArr = new JSONArray();
		ArrayList<Question> uniqueQuestionsArr = new ArrayList<Question>();
		for (int i=0; i<questionsJSONArr.length(); i++) {
			JSONObject questionJSON = questionsJSONArr.getJSONObject(i);
			String questionName =  questionJSON.getString("questionName");
			boolean found = false;
			for(int k=0;k<uniqueQuestionsJSONArr.length();k++) {
				JSONObject uniqueQuestionJSON = uniqueQuestionsJSONArr.getJSONObject(k);
				String uniqueQuestionName =  uniqueQuestionJSON.getString("questionName");
				if(questionName.equals(uniqueQuestionName)) {
					found = true;
				}
			}
			if(!found) {
				uniqueQuestionsJSONArr.put(questionJSON);
				Question q = jsonCoreToDomainCore(questionJSON,user);
				qm.updateQuestion(q);
			}
		}
		
		
		
		
		
		
		//create all the sections!!
		Map<String, Integer> bogusNewSectionIDMap = new HashMap<String, Integer>();
		if (sectionsJSONArr.length() > 0) {
			for (int i=0; i<sectionsJSONArr.length(); i++) {
				JSONObject addSectionJSON = sectionsJSONArr.getJSONObject(i);
				String bogusSecId = addSectionJSON.getString("id");  

				Section section = this.createNewSection(addSectionJSON);
		        int newSectionId = fm.createSection(section, true);
		        bogusNewSectionIDMap.put(bogusSecId, newSectionId);
			}
		}
		
		
		if (sectionsJSONArr.length() > 0) {
			
			// for all repeatable children, we have to update section to the proper parent section id
			for (int i=0;i<sectionsJSONArr.length();i++) {
				JSONObject addSectionJSON = sectionsJSONArr.getJSONObject(i);
				String repeatedSectionParent = addSectionJSON.getString("repeatedSectionParent"); 
				
				if (!repeatedSectionParent.equals("-1")) {
					String bogusSecId = addSectionJSON.getString("id"); 
					int id = bogusNewSectionIDMap.get(bogusSecId);
					int repeatedSectionParentId = bogusNewSectionIDMap.get(repeatedSectionParent);
					
					//update the section
					Section section = new Section();
					section.setId(id);
	                section.setRepeatedSectionParent(repeatedSectionParentId);
	                fm.updateRepeatedSectionParent(section);
				}
			}

			for (int i=0; i<sectionsJSONArr.length(); i++) {
				JSONObject addSectionJSON = sectionsJSONArr.getJSONObject(i);

				//this is the section id that is temporarily given while building the form
				String bogusSecId = addSectionJSON.getString("id");  
				String secDesc = addSectionJSON.getString("description");
				if (secDesc.equals("null")) {
					secDesc = "";
				}
				
				int newSectionId = bogusNewSectionIDMap.get(bogusSecId);
				
                //find all the questions that need to be attached to this section
                if (questionsJSONArr.length() > 0) {
                	
                	List<Integer> questionRows = new ArrayList<Integer>();
                	List<Integer> questionCols = new ArrayList<Integer>();
                	List<String> questionsToAttach = new ArrayList<String>();
                	for (int k=0; k<questionsJSONArr.length(); k++) {
                		JSONObject addQuestionJSON = questionsJSONArr.getJSONObject(k);
    					String secId =  addQuestionJSON.getString("sectionId");
    					
    					if (secId.equals(bogusSecId)) {
        					int questionId = addQuestionJSON.getInt("questionId");
    						questionsToAttach.add(String.valueOf(questionId));
    						
        					questionRows.add(addQuestionJSON.getInt("questionOrder"));
        					if (addQuestionJSON.has("questionOrder_col")) {
        						questionCols.add(addQuestionJSON.getInt("questionOrder_col"));
        					}
        					else {
        						questionCols.add(1);
        					}
    					}
                	}
                
                	//attach all the questions to the section
                	if (!questionsToAttach.isEmpty()) {
                		// I hate that these are split but it would take a LOT more work to make it correct
                		String[] questionsToAttachArr = questionsToAttach.toArray(new String[questionsToAttach.size()]);
                		Integer[] questionRowsToAttach = questionRows.toArray(new Integer[questionRows.size()]);
                		Integer[] questionColsToAttach = questionCols.toArray(new Integer[questionCols.size()]);
                        FormQuestionAttributes qAttrs = new FormQuestionAttributes();
                        qAttrs.setCreatedBy(user.getId());
                        qAttrs.setUpdatedBy(user.getId());
                		
                        if (form.getImportedDate() != null) {
                            String formFileUploadPath = SysPropUtil.getProperty("app.formfilepath");
                            form.setFormFileUploadPath(request.getServletContext().getRealPath(formFileUploadPath));
                            fm.addQuestionsToSection(form, newSectionId, questionsToAttachArr, versionTable, qAttrs, questionRowsToAttach, questionColsToAttach);
                        } else {
                            fm.addQuestionsToSection(newSectionId, questionsToAttachArr, versionTable, qAttrs, questionRowsToAttach, questionColsToAttach);
                        }
                	}
                }
			}
			
			// now set the attributes for questions in the form
			List<Question> newQuestionList = new ArrayList<Question>();
        	for (int k=0;k<questionsJSONArr.length();k++) {
        		JSONObject addQuestionJSON = questionsJSONArr.getJSONObject(k);
        		
				int questionId = addQuestionJSON.getInt("questionId");
				String bogusSecId = addQuestionJSON.getString("sectionId");
            	Question oQuestion = qm.getQuestion(questionId); 
            	int newSectionId = bogusNewSectionIDMap.get(bogusSecId);
            	int qaId = fm.getFormQuestionAttributes(newSectionId, oQuestion.getId()).getId();
            		
            	//////////////////////////////////////////////////
            	//HERE WE NEED TO REPLACE CALCULATION STRING AND SKIP RULE STUFF WITH CORRECT SECTION IDS AND NOT THE BOGUS ONES!!
				JSONObject attributeObjJSON = addQuestionJSON.getJSONObject("attributeObject");
            	boolean calculatedQuestion = attributeObjJSON.getBoolean("calculatedQuestion");

            	if (calculatedQuestion) {
            		String calculation = attributeObjJSON.getString("calculation");
            		if (calculation != null && calculation.length() > 0) {
            			calculation = replaceSectionBogusId(bogusNewSectionIDMap, calculation);
                    	attributeObjJSON.put("calculation", calculation);		                         		
                    }
            	}

            	Question newQuestion = (Question) FormQuestionAttributesAssembler.jsonToDomain(
            				attributeObjJSON, oQuestion, newSectionId, qaId, bogusNewSectionIDMap);
            	newQuestion.setId(oQuestion.getId());
            	newQuestion.setVersion(oQuestion.getVersion());
            	FormQuestionAttributes newFqa = newQuestion.getFormQuestionAttributes();
            	newFqa.setSectionId(newSectionId);
            	newFqa.setUpdatedBy(user.getId());
            	newFqa.getEmailTrigger().setVersion(oQuestion.getFormQuestionAttributes().getEmailTrigger().getVersion());
            	newQuestionList.add(newQuestion);

            	
        	}
        	
        	fm.updateQuestionAttributes(newQuestionList, formId);
		}
	}
	
	
	
	private  static Question jsonCoreToDomainCore(JSONObject questionJSON, User user) throws AssemblerException {
		Question q = null;
		QuestionManager qm = new QuestionManager();
		try {
			
			JSONObject attributeObjJSON = questionJSON.getJSONObject("attributeObject");
			
			
			
			if(questionJSON.getInt("questionType") == QuestionType.VISUAL_SCALE.getValue()) {
				q = new VisualScale();
				((VisualScale) q).setRightText(attributeObjJSON.getString("vscaleRightText"));
       		 	((VisualScale) q).setLeftText(attributeObjJSON.getString("vscaleLeftText"));
       		 	((VisualScale) q).setRangeEnd(Integer.valueOf(attributeObjJSON.getString("vscaleRangeEnd")));
       		 	((VisualScale) q).setRangeStart(Integer.valueOf(attributeObjJSON.getString("vscaleRangeStart")));
       		 	((VisualScale) q).setWidth(Integer.valueOf(attributeObjJSON.getString("vscaleWidth")));
       		 	((VisualScale) q).setCenterText (attributeObjJSON.getString("vscaleCenterText"));
			}else {
				q = new Question();
			}
			
			int questionId = questionJSON.getInt("questionId");
			int questionVersion = 1;


			
			q.setId(questionId);

			q.setVersion(new Version(questionJSON.getString("questionVersionLetter"))); //questionVersionNumber
			
			q.setName(questionJSON.getString("questionName"));
	        q.setText(questionJSON.getString("questionText"));
	        q.setType(QuestionType.getByValue(questionJSON.getInt("questionType")));
	        q.setHtmltext(questionJSON.getString("questionName"));
	        q.setHtmltext(attributeObjJSON.getString("htmlText"));
	        q.setUpdatedBy(user.getId());
	        
	        // add by sunny 
	        q.setDescriptionUp(questionJSON.getString("descriptionUp"));
	        q.setDescriptionDown(questionJSON.getString("descriptionDown"));
	        
	        // added by Ching Heng for Other option
	        q.setIncludeOtherOption(questionJSON.getBoolean("includeOther"));	        

	        q.setDefaultValue(questionJSON.getString("defaultValue"));


	        q.setUnansweredValue(questionJSON.getString("unansweredValue"));
	        
	        
	        //need to set the options
	        
	        if(q.getType() == QuestionType.RADIO || q.getType() == QuestionType.SELECT || q.getType() == QuestionType.CHECKBOX || q.getType() == QuestionType.MULTI_SELECT) {
	        
		        JSONArray questionOptionsObjectArray = questionJSON.getJSONArray("questionOptionsObjectArray");

		        if(questionOptionsObjectArray != null && questionOptionsObjectArray.length() > 0) {	

					List<Answer> answers = new ArrayList<Answer>();

		            Answer a;
		            
		            for (int i=0;i<questionOptionsObjectArray.length();i++) {
			        	JSONObject questionOption = questionOptionsObjectArray.getJSONObject(i);
			        	String option = questionOption.getString("option");
			        	String score = questionOption.getString("score");
			        	String submittedValue = questionOption.getString("submittedValue");
			        	 a = new Answer();
			        	 a.setDisplay(option.trim());
			        	 if(score != null && !score.trim().equals("")) {
			        		 a.setScore(Double.parseDouble(score));
			        	 }
			        	 a.setSubmittedValue(submittedValue);
			        	 answers.add(a);
			        }
		            
		           
		            q.setAnswers(answers);
		        }
		        
	        }  
			
			
			
			
			
		}catch(Exception e) {
			throw new AssemblerException(
					"Unable to tranform json object to question object: "
							+ e.getMessage(), e);
		}
		
		
		
		
		return q;
	}
	
	/**
	 * Saves data from the edited form.<br/><br/>
	 * 
	 * The way this is going to happen is the following:  we will delete all entries from the form layout table, 
	 * and then add new rows. This is done because when in edit mode, the user can alter the layout quite a bit from what is was.
	 * So its much easier to re do it.
	 * 
	 * @param versionTable - A list of the questionId and the version chosen.
	 * @param form - The edited form whose changes are to be saved.
	 * @throws Exception	When any errors happen when the changes to the form is been saved.
	 */
    private void editForm(Map<String, String> versionTable, Form form) throws Exception {
    	
		JSONArray sectionsJSONArr = new JSONArray(URLDecoder.decode(sectionsJSON, "UTF-8"));
		JSONArray questionsJSONArr = new JSONArray(questionsJSON);
		JSONArray existingSectionIdsToDeleteJSONArr = new JSONArray(existingSectionIdsToDeleteJSON);
		JSONArray existingQuestionIdsToDeleteJSONArr = new JSONArray(existingQuestionIdsToDeleteJSON);
		
		int formId = Integer.parseInt(formBuildFormId);
		User user = getUser();
		FormManager fm = new FormManager();
		QuestionManager qm = new QuestionManager();
		ResponseManager rm = new ResponseManager();
		
		fm.deleteAllRows(formId);			

		//first determine number of rows in the form
		int numRows = 0;
		
		for ( int i = 0; i < sectionsJSONArr.length(); i++ ) {
			JSONObject addSectionJSON = sectionsJSONArr.getJSONObject(i);
			int row = addSectionJSON.getInt("row");
			
			if ( row > numRows ) {
				numRows = row;
			}
		}
		
		//now determine number of cols in each row and then insert that row with num cols
		for ( int i = 0; i < numRows; i++ ) {
			int numCols = 0;
			
			for ( int k = 0; k < sectionsJSONArr.length(); k++ ) {
				JSONObject addSectionJSON = sectionsJSONArr.getJSONObject(k);
				int row = addSectionJSON.getInt("row");
				
				if ( row == i + 1 ) {
					int col = addSectionJSON.getInt("col");
					
					if ( col > numCols ) {
						numCols = col;
					}
				}
			}
			
			fm.addRow(formId, numCols);
		}
		
		// first do the existingQuestionIdsToDeleteJSONArr
		if ( (existingQuestionIdsToDeleteJSONArr != null) && (existingQuestionIdsToDeleteJSONArr.length() > 0) ) {
			for ( int i = 0; i < existingQuestionIdsToDeleteJSONArr.length(); i++ ) {
				String eq_string = existingQuestionIdsToDeleteJSONArr.getString(i); 
				String[] splits = eq_string.split("#");
				int questionId = Integer.parseInt(splits[0]);
				int sectionId = Integer.parseInt(splits[1]);
				
				fm.deleteSectionQuestion(sectionId, questionId, user);
			}
		}
		
		// now do the existingSectionIdsToDeleteJSONArr
		if ( (existingSectionIdsToDeleteJSONArr != null) && (existingSectionIdsToDeleteJSONArr.length() > 0) ) {
			for ( int i = 0; i < existingSectionIdsToDeleteJSONArr.length(); i++ ) {
				String sIDString = existingSectionIdsToDeleteJSONArr.getString(i);
				int sectionId = Integer.parseInt(sIDString);
				
				rm.deleteVisibleAdministeredSection(sectionId);
				fm.deleteSection(sectionId, user);
			}
		}
		
		
		
				//first lets update core question 
				JSONArray uniqueQuestionsJSONArr = new JSONArray();
				ArrayList<Question> uniqueQuestionsArr = new ArrayList<Question>();
				for (int i=0; i<questionsJSONArr.length(); i++) {
					JSONObject questionJSON = questionsJSONArr.getJSONObject(i);
					String questionName =  questionJSON.getString("questionName");
					boolean found = false;
					for(int k=0;k<uniqueQuestionsJSONArr.length();k++) {
						JSONObject uniqueQuestionJSON = uniqueQuestionsJSONArr.getJSONObject(k);
						String uniqueQuestionName =  uniqueQuestionJSON.getString("questionName");
						if(questionName.equals(uniqueQuestionName)) {
							found = true;
						}
					}
					if(!found) {
						uniqueQuestionsJSONArr.put(questionJSON);
						Question q = jsonCoreToDomainCore(questionJSON,user);
						qm.updateQuestion(q);
					}
				}
		
		
		
		// now do the add sections and update sections part, lets add all the sections first!!
		if (sectionsJSONArr.length() > 0) {
			Map<String, Integer> bogusNewSectionIDMap = new HashMap<String, Integer>();
			
			for ( int i = 0; i < sectionsJSONArr.length(); i++ ) {
				JSONObject addSectionJSON = sectionsJSONArr.getJSONObject(i);
				boolean existingSection = addSectionJSON.getBoolean("existingSection");
				
				if ( !existingSection ) {
					String bogusSecId = addSectionJSON.getString("id");  

					Section section = this.createNewSection(addSectionJSON);
			        int newSectionId = fm.createSection(section, true);
			        bogusNewSectionIDMap.put(bogusSecId, newSectionId);
				}
			}

			// for all repeatable children, we have to update section to the proper parent section id
			for ( int i = 0; i < sectionsJSONArr.length(); i++ ) {
				JSONObject addSectionJSON = sectionsJSONArr.getJSONObject(i);
				boolean existingSection = addSectionJSON.getBoolean("existingSection");
				
				if ( !existingSection ) {
					String repeatedSectionParent = addSectionJSON.getString("repeatedSectionParent"); 
					
					if (repeatedSectionParent.startsWith("S_") && repeatedSectionParent.contains("-")) {
						String bogusSecId = addSectionJSON.getString("id"); 
						int id = bogusNewSectionIDMap.get(bogusSecId);
						int repeatedSectionParentId = bogusNewSectionIDMap.get(repeatedSectionParent);
						
						//update the section
						Section section = new Section();
						section.setId(id);
		                section.setRepeatedSectionParent(repeatedSectionParentId);
		                fm.updateRepeatedSectionParent(section);
					}
				}
			}
			
			// iterate over the sections that user would like to attach
			for ( int i = 0; i < sectionsJSONArr.length(); i++ ) {
				JSONObject addSectionJSON = sectionsJSONArr.getJSONObject(i);
				boolean existingSection = addSectionJSON.getBoolean("existingSection");
				
				if ( existingSection ) {
					Section section = this.createNewSection(addSectionJSON);
					String sectionIdStr = addSectionJSON.getString("id");
					
					// Update the section
					section.setId(Integer.parseInt(sectionIdStr));
	                fm.updateSection(section);
				}
			}

			// iterate over the sections that user would like to attach
			for ( int i = 0; i < sectionsJSONArr.length(); i++ ) {
				JSONObject addSectionJSON = sectionsJSONArr.getJSONObject(i);
				int sectionId = -1;
				String bogusSecId = "";
				String secIdString = "";
				boolean existingSection = addSectionJSON.getBoolean("existingSection");
				
				if ( existingSection ) {
					//need to just update the section
					secIdString = addSectionJSON.getString("id");
					sectionId = Integer.parseInt(secIdString);
					String secDesc =  addSectionJSON.getString("description");
					if (secDesc.equals("null")) {
						secDesc = "";
					}
					
	                //this is where we need to delete questions from this section if they have been removed or moved to other sections
	                //first get the list of 'old' set of questions for this section
	                List<Question> oldQuestions = fm.getSectionQuestions(sectionId);
	                
	                for ( Question oldQuestion : oldQuestions ) {
	                	int oldQuestionId = oldQuestion.getId();
		                boolean match = false;
	                	
	                	for ( int k = 0; k < questionsJSONArr.length(); k++ ) {
		                	JSONObject addQuestionJSON = questionsJSONArr.getJSONObject(k);
		                	int questionId = addQuestionJSON.getInt("questionId");
		                	String secId = addQuestionJSON.getString("sectionId");
		                	
    						if (secId.equals(secIdString)) {
    							if (oldQuestionId == questionId) {
    								match = true;
    								break;
    							}
    						}
		                }
	                	
	                	if ( !match ) {
	                		fm.deleteSectionQuestion(sectionId, oldQuestionId, user);
	                		@SuppressWarnings("unchecked")
							Map<Integer, Question> qustionMap = (Map<Integer, Question>) session.get("qustionMap");	
	                		if (qustionMap != null && !qustionMap.isEmpty()) {
		                		qustionMap.remove(new Integer(oldQuestionId));
		                		session.put("qustionMap", qustionMap);
	                		}
	                	}
	                }
				}
				else {
					//need to add the section
					bogusSecId = addSectionJSON.getString("id");  //this is the section id that is temporarily given while building the form
					sectionId = bogusNewSectionIDMap.get(bogusSecId);
				}
				
				if ( questionsJSONArr.length() > 0 ) { 
					//need to attach new questions to section
					//also need to update the order of existing questions
					int sectionQuestionCounter = 0;
					
					for ( int k = 0; k < questionsJSONArr.length(); k++ ) {
                		JSONObject addQuestionJSON = questionsJSONArr.getJSONObject(k);
    					String secId = addQuestionJSON.getString("sectionId");
    					
    					if ( existingSection ) {
    						if (secId.equals(String.valueOf(sectionId))) {
    							sectionQuestionCounter++;
    						}
    					}
    					else if ( secId.equals(bogusSecId) ) {
    						sectionQuestionCounter++;
    					}
					}
					
					int questionArrayIndex = 0;
					
					if ( sectionQuestionCounter > 0 ) {
                        List<String> questionsToAttachAL = new ArrayList<String>();
                        //String[][] questionsOrdered = new String[counter][3];
                        ArrayList<String[]> existingQuestionsOrdered = new ArrayList<String[]>();
                        List<Integer> rows = new ArrayList<Integer>();
                        List<Integer> cols = new ArrayList<Integer>();
                        
                        for ( int k = 0; k < questionsJSONArr.length(); k++ ) {
                              JSONObject addQuestionJSON = questionsJSONArr.getJSONObject(k);
                                     int questionId = addQuestionJSON.getInt("questionId");
                                     String secId = addQuestionJSON.getString("sectionId");
                                     String questionIdStr = String.valueOf(questionId);
                                     int questionOrder = addQuestionJSON.getInt("questionOrder");
                                     int questionOrderCol = 1;
                                     
                                     try {
                                            // if not found (IE: old form builder), just assign it to 1
                                            questionOrderCol = addQuestionJSON.getInt("questionOrder_col");
                                     }
                                     catch(Exception e) {
                                            questionOrderCol = 1;
                                     }
                                     
                                     if ( existingSection ) {
                                    	 	//existing section
                                            if ( secId.equals(String.valueOf(sectionId)) ) {
                                                   if ( !addQuestionJSON.getBoolean("existingQuestion") ) {
                                                	  //this means it is a brand new question to the section
                                                      questionsToAttachAL.add(questionIdStr);
                                                      rows.add(questionOrder);
                                                      cols.add(questionOrderCol);
                                                   }
                                                   else {
                                                	   //this means it is an already exisitng question in the section but we need to determine if it
                                                	   //is an existing question from another section being moved to this section
                                                	   boolean match = false;
                                                      List<Question> oldQuestions = fm.getSectionQuestions(sectionId);
                                                      
		                                              for ( Question oldQuestion : oldQuestions ) {
		                                                   if (questionId == oldQuestion.getId()) {
		                                                          match = true;
		                                                         break;
		                                                   }
		                                              }
		
		                                              if ( !match ) {
		                                                   //this means it is an existing question from another section
		                                            	  questionsToAttachAL.add(questionIdStr);
		                                                   rows.add(questionOrder);
		                                                          cols.add(questionOrderCol);
		                                              }else {
		                                                   //this means it is an existing question originially in the section
		                                                   String[] arr = new String[3];
		                                                   arr[0] = questionIdStr;
		                                                   arr[1] = String.valueOf(questionOrder);
		                                                   arr[2] = String.valueOf(questionOrderCol);
		                                                   existingQuestionsOrdered.add(arr);
		                                              }
                                                   }

                                            }     
                                     } else if (secId.equals(bogusSecId)) {
                                    	 	//new section
                                            questionsToAttachAL.add(questionIdStr); 
                                            rows.add(questionOrder);
                                            cols.add(questionOrderCol);
                                     }
                        }
                        
                        //Lets update the ordering of the questions in the db before we insert any new questions being added to the section
                        // bc when we add new questions to the section,below,the correct order is set for them.
                        if(existingQuestionsOrdered.size() > 0) {
                             int qLength = existingQuestionsOrdered.size();
                             String[][] questionsOrdered = new String[qLength][3];
                              for(int k=0;k<existingQuestionsOrdered.size();k++) {
                                     String[] arr = existingQuestionsOrdered.get(k);
                                     questionsOrdered[k] = arr;
                              }
                               fm.updateQuestionOrdering(sectionId, questionsOrdered);
                        }
                        
                        
                        
                        // attach all the questions to the section
                        if ( !questionsToAttachAL.isEmpty() ) {
                              String[] questionsToAttach = new String[questionsToAttachAL.size()];
                              
                              for ( int m = 0; m < questionsToAttachAL.size(); m++ ) {
                                     questionsToAttach[m] = (String)questionsToAttachAL.get(m);
                              }

                           FormQuestionAttributes qAttrs = new FormQuestionAttributes();
                           qAttrs.setCreatedBy(user.getId());
                           qAttrs.setUpdatedBy(user.getId());
                              
                           Integer[] aRows = new Integer[rows.size()];
                           aRows = rows.toArray(aRows);
                           Integer[] aCols = new Integer[cols.size()];
                           aCols = cols.toArray(aCols);
                           
                           if ( form.getImportedDate() != null ) {
                               String formFileUploadPath = SysPropUtil.getProperty("app.formfilepath");
                               form.setFormFileUploadPath(request.getServletContext().getRealPath(formFileUploadPath));
                               
                               fm.addQuestionsToSection(form, sectionId, questionsToAttach, versionTable, qAttrs, aRows, aCols);
                           }
                           else {
                               fm.addQuestionsToSection(sectionId, questionsToAttach, versionTable, qAttrs, aRows, aCols);
                           }
                        } 
                        
                        
                 }

				}				
			}	 
			
			//now set the attributes for questions in the section
			List<Integer> orgQuestionList= new ArrayList<Integer>();
			List<Integer> qAttributeIdList= new ArrayList<Integer>();
			List<Question> newQuestionList = new ArrayList<Question>();
			
        	for ( int k = 0; k < questionsJSONArr.length(); k++ ) {
        		JSONObject addQuestionJSON = questionsJSONArr.getJSONObject(k);
				int questionId = addQuestionJSON.getInt("questionId");
				String secId = addQuestionJSON.getString("sectionId");
				JSONObject attributeObjJSON = addQuestionJSON.getJSONObject("attributeObject");
            	Question oQuestion = qm.getQuestion(questionId);
            	int sectionId = 0;
            	
            	if ( secId.contains("-") ) {
            		sectionId = bogusNewSectionIDMap.get(secId);
            	}
            	else {
            		sectionId = Integer.valueOf(secId);
            	}
            		
            	int qaId = fm.getFormQuestionAttributes(sectionId, oQuestion.getId()).getId();
            	boolean calculatedQuestion = attributeObjJSON.getBoolean("calculatedQuestion");

            	if ( calculatedQuestion ) {
                    String calculation = attributeObjJSON.getString("calculation");
                    
                    if ( (calculation != null) && (!calculation.isEmpty()) ) {
                    	calculation = replaceSectionBogusId(bogusNewSectionIDMap, calculation);
                    	attributeObjJSON.put("calculation", calculation);		                         		
                    }
                }
            		
            	Question newQuestion = (Question) FormQuestionAttributesAssembler.jsonToDomain(
            			attributeObjJSON, oQuestion, sectionId, qaId, bogusNewSectionIDMap);
            	newQuestion.setId(oQuestion.getId());
            	newQuestion.setVersion(oQuestion.getVersion());
            	
            	FormQuestionAttributes newFqa = newQuestion.getFormQuestionAttributes();
            	newFqa.setSectionId(sectionId);
            	newFqa.setUpdatedBy(user.getId());
            	newFqa.getEmailTrigger().setUpdatedBy(user.getId());
            	
            	Version emailTriggerVersion = new Version(fm.getEmailTriggerVersion(qaId));
            	newFqa.getEmailTrigger().setVersion(emailTriggerVersion);
					
                orgQuestionList.add(questionId);
				qAttributeIdList.add(newFqa.getId());
 				newQuestionList.add(newQuestion);
        	}
        	
        	fm.updateQuestionAttributes(newQuestionList,formId);
        	
            /*boolean nowcopyright = (Boolean) session.get("nowCopyright");
            boolean orgCopyright = (Boolean) session.get("orgCopyright");
            
            if ( !orgCopyright && nowcopyright ) {	                        
            	@SuppressWarnings("unchecked")
				Map<Integer, Integer> qustionMap = (Map<Integer, Integer>) session.get("qustionMap");
            	
        		if ( qustionMap == null ) {
        			qustionMap = new HashMap<Integer, Integer>();
        		}
            	
        		for ( int a = 0; a < orgQuestionList.size(); a++ ) {
        			int qid = orgQuestionList.get(a);
        			int aid = qAttributeIdList.get(a);
        			boolean sameQuestionExist = false;
        			boolean needCopy = true;
        			int newQexistId = 0;          			
        			
            		if ( qustionMap.containsValue(qid) ) {
						sameQuestionExist = true;
						
						for ( int key : qustionMap.keySet() ) {
							if ( qid == qustionMap.get(key) ) {
								newQexistId = key;
		                		needCopy = false;
								break;
							}
						}
					} 
                	
                	if ( needCopy ) { 
                		int newQuestionId = 0;
                		
                		if ( !sameQuestionExist ) {
	                		newQuestionId = qm.copyQuestion(qid);		
	                		qustionMap.put(newQuestionId, qid);
                		}
                		else {
                			Question questionCopy = qm.getQuestion(newQexistId); 
                			newQuestionId = questionCopy.getId();
                		}
                		
                		qm.updateCopiedQuestion(formId, qid, newQuestionId, aid);	
                	}
        		}
        	}*/
		}	
		
		//user might have deleted all questions and it might have been active so we need to make it inactive now
		Form f = fm.getFormAndSetofQuestions(formId);
		int status = f.getStatus().getId();
		
		if (f.getNumQuestions() == 0 && status != 1) {
			 f.setStatus(new CtdbLookup(1, "inactive"));
          	 fm.updateFormInfo(f);
		}
	}
    
    /**
     * Helper function that well convert the given JSON version of a section to a new Section object.
     * 
     * @param addSectionJSON - The JSON data that will be transfered to the new Section object
     * @return	A new Section object with the data from the given JSON object.
     * @throws Exception	When an error occurs during the JSON to Section object transformation
     */
	private Section createNewSection(JSONObject addSectionJSON) throws Exception {

		int formId = Integer.parseInt(formBuildFormId);
		User user = getUser();
		
		//this is the section id that is temporarily given while building the form
		String secName = addSectionJSON.getString("name");
		String secDesc = addSectionJSON.getString("description");
		if (secDesc.equals("null")) {
			secDesc = "";
		}
		
		boolean secIsCollaps =  addSectionJSON.getBoolean("isCollapsable");
		int row = addSectionJSON.getInt("row");
		int column = addSectionJSON.getInt("col");
		
		boolean isRepeatable = addSectionJSON.getBoolean("isRepeatable");
		int initRepeatedSecs = addSectionJSON.getInt("initRepeatedSecs");
		int maxRepeatedSecs = addSectionJSON.getInt("maxRepeatedSecs");
		String repeatedSectionParentString = addSectionJSON.getString("repeatedSectionParent");
		String repeatableGroupName = addSectionJSON.getString("repeatableGroupName");
		boolean isGridtype = addSectionJSON.getBoolean("gridtype");
		int tableGroupId = addSectionJSON.getInt("tableGroupId");
		int tableHeaderType = addSectionJSON.getInt("tableHeaderType");
		
		int	repeatedSectionParent;
		if (repeatedSectionParentString.contains("-")) {
			repeatedSectionParent = -1;
		} else {
			if (repeatedSectionParentString.startsWith("S_")) {
				repeatedSectionParentString = repeatedSectionParentString.substring(
						2, repeatedSectionParentString.length());
			}
			repeatedSectionParent = Integer.parseInt(repeatedSectionParentString);
		}
		
		// create the section
		Section section = new Section();
		section.setCol(column);
        section.setRow(row);
        section.setName(secName);	                
        section.setDescription(secDesc.trim());
        section.setCollapsable(secIsCollaps);
        section.setFormId(formId);
        section.setCreatedBy(user.getId());
        section.setUpdatedBy(user.getId());
        section.setCreatedDate(new Date());
        section.setUpdatedDate(new Date());
        section.setRepeatable(isRepeatable);
        section.setInitRepeatedSections(initRepeatedSecs);
        section.setMaxRepeatedSections(maxRepeatedSecs);
        section.setRepeatedSectionParent(repeatedSectionParent);
        section.setRepeatableGroupName(repeatableGroupName);
        section.setGridtype(isGridtype); 
        section.setTableGroupId(tableGroupId); 
        section.setTableHeaderType(tableHeaderType);
        
        return section;
	}
	
	/**
	 * 
	 * @param bogusNewSectionIDMap
	 * @param targetString
	 * @return
	 */
	private String replaceSectionBogusId(Map<String, Integer> bogusNewSectionIDMap, String targetString) {
    	if (!Utils.isBlank(targetString)) {

    		List<Integer> s_indicesList = new ArrayList<Integer>();
    		List<Integer> q_indicesList = new ArrayList<Integer>();
    		List<String> stringRealSectionIds = new ArrayList<String>();
    		
    		int index = targetString.indexOf("S_");
    		while (index != -1) {
    			s_indicesList.add(index+2);
    			index = targetString.indexOf("S_", index+1);
    		}
    		
    		if (s_indicesList.size() > 0) {
    			for (int s_ind : s_indicesList) {
    				int q_ind = targetString.indexOf("_Q", s_ind);
    				q_indicesList.add(q_ind);
    				String bSecId = targetString.substring(s_ind, q_ind);

    				if (bogusNewSectionIDMap.get("S_"+bSecId) != null) {
	    				String rSecId = String.valueOf(bogusNewSectionIDMap.get("S_"+bSecId));
	    				stringRealSectionIds.add(rSecId);
    				} else {
    					stringRealSectionIds.add("none");
    				}
    			}
     			
    			for (int m=s_indicesList.size()-1;m>=0;m--) {
    				int s_ind = s_indicesList.get(m);
    				int q_ind = q_indicesList.get(m);
    				String sId =  stringRealSectionIds.get(m);
    				if (!sId.equals("none")) {
    					targetString = targetString.substring(0, s_ind) + sId + 
    							targetString.substring(q_ind, targetString.length());
    				}
    			}
    		}
    	}
    	return targetString;
    }  
	
	public String getFormMode() {
		return formMode;
	}
	
	public void setFormMode(String formMode) {
		this.formMode = formMode;
	}

	public String getFormBuildFormId() {
		return formBuildFormId;
	}

	public void setFormBuildFormId(String formBuildFormId) {
		this.formBuildFormId = formBuildFormId;
	}

	public String getSectionsJSON() {
		return sectionsJSON;
	}

	public void setSectionsJSON(String sectionsJSON) {
		this.sectionsJSON = sectionsJSON;
	}

	public String getQuestionsJSON() {
		return questionsJSON;
	}

	public void setQuestionsJSON(String questionsJSON) {
		this.questionsJSON = questionsJSON;
	}

	public String getExistingSectionIdsToDeleteJSON() {
		return existingSectionIdsToDeleteJSON;
	}

	public void setExistingSectionIdsToDeleteJSON(String existingSectionIdsToDeleteJSON) {
		this.existingSectionIdsToDeleteJSON = existingSectionIdsToDeleteJSON;
	}

	public String getExistingQuestionIdsToDeleteJSON() {
		return existingQuestionIdsToDeleteJSON;
	}

	public void setExistingQuestionIdsToDeleteJSON(String existingQuestionIdsToDeleteJSON) {
		this.existingQuestionIdsToDeleteJSON = existingQuestionIdsToDeleteJSON;
	}
}

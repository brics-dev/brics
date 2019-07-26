package gov.nih.nichd.ctdb.question.action;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.form.common.FormConstants;
import gov.nih.nichd.ctdb.question.common.QuestionAssembler;
import gov.nih.nichd.ctdb.question.common.QuestionConstants;
import gov.nih.nichd.ctdb.question.domain.Answer;
import gov.nih.nichd.ctdb.question.domain.ImageMapOption;
import gov.nih.nichd.ctdb.question.domain.ImageMapQuestion;
import gov.nih.nichd.ctdb.question.domain.InstanceType;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.domain.QuestionType;
import gov.nih.nichd.ctdb.question.domain.VisualScale;
import gov.nih.nichd.ctdb.question.form.AddEditQuestionForm;
import gov.nih.nichd.ctdb.question.form.QuestionOption;
import gov.nih.nichd.ctdb.question.manager.QuestionManager;
import gov.nih.nichd.ctdb.security.domain.User;


public class AddEditQuestionAction extends BaseAction {
	private static final long serialVersionUID = 2431281187438176100L;
	private static Logger logger = Logger.getLogger(AddEditQuestionAction.class);
	
	AddEditQuestionForm questionForm = new AddEditQuestionForm();
	private String id = null;
	private String action = null;

    @SuppressWarnings("unchecked")
	public String execute() throws Exception {
        
    	HttpServletResponse response = ServletActionContext.getResponse();
    	response.setContentType("text/text");
		PrintWriter out = response.getWriter();
		
        boolean directSave = false;

        try {
            QuestionManager qm = new QuestionManager();
            
            request.setAttribute("availableQuestionGroups", qm.getGroups());
        	 
            questionForm.setQuestionOptionsObjectList(new JSONArray(questionForm.getQuestionOptionsJSON()));
            
            
            if (action == null) {
                 // coming from redirect
            	if (session.get(QuestionConstants.FINISH) != null) {
            		session.remove(QuestionConstants.FINISH);
            		questionForm.setAction(StrutsConstants.ACTION_PROCESS_EDIT);
            		directSave = true;
            	} else if (session.get(QuestionConstants.ORIGINAL_QUESTION_OBJ) == null) {
            		questionForm.setAction(StrutsConstants.ACTION_ADD_FORM);
            	} else {
            		questionForm.setAction(StrutsConstants.ACTION_EDIT_FORM);
            	}
            }

            if (action.equalsIgnoreCase("addQuestionAjax") || action.equalsIgnoreCase("editQuestionAjax")) {    		 
            	Question question = null;
            	if (questionForm.getType() == QuestionType.IMAGE_MAP.getValue()) {  
            		question = (Question) session.get(QuestionConstants.QUESTION_IN_PROGRESS);     
            		if (question == null) {
            			question = qm.getQuestion(questionForm.getId());                                                 
            		}
            		
                	// if user hasn't finished the ImageMap definition
            		ImageMapQuestion checkedQuestion = (ImageMapQuestion) question;
            		try { // test the imageMap is done or not
            			checkedQuestion.getGridFileName();
            		} catch (NullPointerException e) {
            			out.print("ImageMap_NOTDONE");
            			out.flush();
            			return null;
            		}
            		
            	} else if (questionForm.getType() == QuestionType.VISUAL_SCALE.getValue()) { // Visual scale Type
            		question = new VisualScale();
            		// For the Visual Scale Action
            		if (questionForm.getRangeEnd() < questionForm.getRangeStart()) { // Visual scale logically check
            			out.print("ERROR_SCALE_MAXMIN");
            			out.flush();
            			return null;
            		} else {
            			// process form to DB, get user creating question
            			QuestionAssembler.OptionsQuestionsInForm(questionForm, question);
            		}
            		
            	} else {       // basic Type
            		question = new Question();
            	}
        		 
        		// Check this question has same one
            	if (!action.equalsIgnoreCase("addQuestionAjax")) {
            		// question map doesn't need clone, we already got it before
            		if (questionForm.getType() != QuestionType.IMAGE_MAP.getValue() || 
            			!action.equalsIgnoreCase("editQuestionAjax")) {   
            			int questionId = Integer.parseInt(request.getParameter("qId"));
            			int questionVersion = Integer.parseInt(request.getParameter("qVersion"));
            			question.clone(qm.getQuestion(questionId, questionVersion));
            		}
            		
            		if (questionForm.getType() == QuestionType.VISUAL_SCALE.getValue()) {
            			question.setType(QuestionType.VISUAL_SCALE);
            			question.getFormQuestionAttributes().setInstanceType(InstanceType.VISUAL_SCALE_QUESTION);
            		}
            		question.setId(Integer.parseInt(request.getParameter("qId")));
                	 
            	} else {
            		// adding a question, check the name
            		if (qm.questionNameExists(questionForm.getQuestionName().trim())) {
            			out.print("ERROR_DUPLICATE_QUESTION");
            			out.flush();
            			return null;
            		}
            	}
                 
            	// process form to DB, get user creating question
            	QuestionAssembler.QuestionWizardStartInForm(questionForm, question); 
            	
            	// Options Question add
            	if (request.getParameter("forceNewVersion") != null && request.getParameter("forceNewVersion").equals("true")) {
            		if (session.get(FormConstants.SECTIONID) != null) {
            			// edit is from section home,
            			if ((Boolean)session.get("hasSkipRuleOnCurrentForm")) {
            				// if its on the current form, must remove skip rule
            				int sectionId = Integer.parseInt((String)session.get(FormConstants.SECTIONID));
            	            qm.removeSkipRule(question.getId(), question.getVersion().getVersionNumber(), sectionId);
            				
            				if ((Boolean)session.get("hasSkipRuleOnOtherForms")) {
            					// skip rule is on current form and others, remove and new version
            					session.put("ForceNewVersion", true);
            				}
            			} else {
            				// no skip rule on current form, must be on other form, force new version
            				session.put("ForceNewVersion", true);
            			}
            		} else {
            			session.put("ForceNewVersion", true);
            		}
            	}
            	
            	if (addAnswers(question, out)) {
            		if ("true".equals(request.getParameter("finish"))) {
            			session.put(QuestionConstants.FINISH, true);
            		} 
            	} else {
            		request.setAttribute("answers", Arrays.asList(questionForm.getOptions()));
                    questionForm.setOptions(null);
                    request.setAttribute ("questionWizardOptionsForm", questionForm);
                    return null;
                }                  
                 
            	if (request.getParameterValues("questionGroupIds") != null) {
                 	String[] QGstring = request.getParameterValues("questionGroupIds"); 
                 	int[] QGint = new int[QGstring.length];  
                 	for (int i=0; i<QGstring.length; i++) {
                 		int intTemp = Integer.parseInt(QGstring[i]);
                 		QGint[i] = intTemp;
                 	}
                 	questionForm.setQuestionGroupIds(QGint); // set the questionGroupIds to the question form
                 }
                 
                 // Force New version??
                 if (request.getParameter("versionUpdate") != null) {
                     session.put("ForceNewVersion", true);
                 } else {
                     session.remove("ForceNewVersion");
                 }

                 if (directSave) {
                     updateExtraInfo(question, (Question)session.get(QuestionConstants.ORIGINAL_QUESTION_OBJ));
                 }
                 if (!validateDefaultValue(question)) {
                     out.print("ERROR_DefaultValue_NEED");
                     out.flush();
                     return null;
                     
                 } else {
                	 try {
                         // Insert question to database
						User user = getUser();
                         question.setUpdatedBy(user.getId());
                         
                         boolean nowcopyright = (Boolean)session.get("nowCopyright");
                         if (action.equalsIgnoreCase("addQuestionAjax")) {
                        	 // create question
                        	 question.setCreatedBy(user.getId());
                             qm.createQuestion(question, nowcopyright);
                             
                         } else {/*  
                        	 // edit question
                        	 boolean needCopy = false;
                        	 if (nowcopyright) {	 
                        		 if ("1".equals(request.getParameter("import"))) {
                    				 needCopy = true;
                    			 }
                        	 }                        	 

                        	 if (needCopy) { // edit the question in CopyRight Form
                        		 Map<Integer, Integer> qustionMap = new HashMap<Integer, Integer>();
                        		 boolean sameQuestionExist = false;
                    			 int orgQuestionId = question.getId();
                        		 int newQexistId = 0;
                        		 if (session.get("qustionMap") != null) {
                        			 qustionMap = (Map<Integer, Integer>) session.get("qustionMap");
                        			 
                        			 if (qustionMap.containsValue(orgQuestionId)) {
                        				 sameQuestionExist = true;
                        				 Iterator<Entry<Integer, Integer>> it = qustionMap.entrySet().iterator();  
                        				 while (it.hasNext()) {  
                        					 Entry<Integer, Integer> entry = it.next();  
                        					 if (orgQuestionId == entry.getValue()) {
                        						 newQexistId = entry.getKey();
                        					 }
                        				 }										
                        			 }
                        		 }	
								
                        		 int newQustionId=0;
                        		 if (!sameQuestionExist) {
                        			 newQustionId = qm.copyQuestion(orgQuestionId);
                        		 } else {
                        			 newQustionId = newQexistId;
                        		 }
                        		 
                        		 Question questionCopy = qm.getQuestion(newQustionId);                                    
                        		 question.setId(questionCopy.getId()); //update the changed value to new question
                        		 question.setVersion(questionCopy.getVersion());
                        		 question.setName(questionCopy.getName());
                        		 question.setCreatedBy(user.getId());
                        		 question.setUpdatedBy(user.getId());
								 qm.updateQuestion(question);
								 
								 qustionMap.put(newQustionId, orgQuestionId );
								 session.put("qustionMap", qustionMap);    
                                 	
                        	 } else { // edit the question in non CopyRight Form
                        		 question.setUpdatedBy(user.getId());
                        		 
                        		 if ("local".equalsIgnoreCase(request.getParameter("editMode"))) { // local change
                        			 int newQustionId = qm.copyNonCopyRightQuestion(question.getId());
                                     Question questionCopy = qm.getQuestion(newQustionId);                                    
                                     question.setId(questionCopy.getId()); //update the changed value to new question
                                     question.setVersion(questionCopy.getVersion());
                                     question.setName(questionCopy.getName());
                                     question.setUpdatedBy(user.getId());
                                     qm.updateQuestion(question);
                        		 } else { // global change
                        			 qm.updateQuestion(question);
                        		 }
                        	 }
                         */
                        	 
                        	 ///////////////TO EDIT AFTER WE GET MORE REQS
                        	 //if ("local".equalsIgnoreCase(request.getParameter("editMode"))) { // local change
                    			
                                 //qm.updateQuestion(question);
                    		// } else { // global change
                    			// qm.updateQuestion(question);
                    		 //}
                        	 
                        	 
                        	 qm.updateQuestion(question);

                         }
                	 } catch (DuplicateObjectException e) {
                		 logger.error("Duplicate question found.", e);
                         out.print("ERROR_DUPLICATE_QUESTION");
                         out.flush();
                         return null;
                     }
                 }
                 
                 // let's try Json
                 JSONObject jsonObj = new JSONObject();
                 jsonObj.put("questionId", question.getId());
                 jsonObj.put("questionVersionString", question.getVersion().getToString());
                 jsonObj.put("questionVersionNumber", question.getVersion().getVersionNumber());
                 jsonObj.put("questionName", question.getName());
                 jsonObj.put("questionText", question.getText());
                 jsonObj.put("descriptionUp", question.getDescriptionUp());//add by sunny
                 jsonObj.put("descriptionDown", question.getDescriptionDown());//add by sunny
                 jsonObj.put("questionType", question.getType().getValue());
                 jsonObj.put("defaultValue", question.getDefaultValue());
                 jsonObj.put("unansweredValue", question.getUnansweredValue());
                 jsonObj.put("includeOther", question.isIncludeOtherOption());
                 jsonObj.put("hasDecimalPrecision", question.getHasDecimalPrecision());
                 jsonObj.put("hasCalDependent", question.getHasCalDependent());
                 jsonObj.put("prepopulation", question.getPrepopulation());
                 jsonObj.put("associatedGroupIds",qm.getAssociatedGroupIds(question.getId()));
                 jsonObj.put("text", question.getHtmltext()); // added by JP

                 // get the select, radio, checkbox, multi-select options
                 /*StringBuffer options = new StringBuffer();
                 
                 if (questionForm.getOptions() != null) {
	                 for ( int i = 0; i < questionForm.getOptions().length; i++ ) {
	                	 options.append(questionForm.getOptions()[i] + StrutsConstants.alienSymbol);
	                 }
	                 
	                 if (question.isIncludeOtherOption()) {
	                	 options.append(CtdbConstants.OTHER_OPTION + StrutsConstants.alienSymbol);
	                 }
                 }
                 
                 jsonObj.put("options", options.toString());*/
                 
                 // get imageMap options question type is image map
                 if ( questionForm.getType() == 9 ) {
                	 StringBuffer imageOption = new StringBuffer();
	                 List<ImageMapOption> mapOptions = (List<ImageMapOption>) session.get("mapOptions");
	                 
	                 if ( mapOptions != null ) {
	                	 for ( ImageMapOption imo : mapOptions ) {
	                		 imageOption.append(imo.getOption() + StrutsConstants.alienSymbol);
	                	 } 
	                 }
	                 else {
	                	 List<String> optionList = qm.getImageMapOptions(question.getId(), question.getVersion().getVersionNumber());
	                	 
	                	 for ( String opt : optionList ) {
	                		 imageOption.append(opt);
	                	 }
	                 }
	                 
                	 jsonObj.put("imageOption", imageOption.toString());
		             String imageFileName = ((ImageMapQuestion) question).getImageFileName();
		        	 jsonObj.put("imageFileName", imageFileName);
                 }
        		 
        		 Set<Integer> attachedFormIds = qm.getAttachedFormIds(question.getId(), question.getVersion().getVersionNumber());
        		 Integer formId = (Integer) session.get(FormConstants.FORMID);
        		 
        		 if ( formId != null ) {
        			 attachedFormIds.remove(formId);// don't include the current form Id.
        		 }
        		 
        		 jsonObj.put("attachedFormIds", attachedFormIds);
                 jsonObj.put("attachedFormNames", qm.getCalDependentFormNames(question.getId()));
        		 
        		 out.print(jsonObj);
             	 out.flush();
             	 
                 return null;
        	 }
        }
        catch ( CtdbException ce ) {
        	logger.error("Database error occurred.", ce);
        	return StrutsConstants.FAILURE;
        }
        catch ( JSONException je ) {
        	logger.error("JSON error occurred.", je);
        	return StrutsConstants.FAILURE;
        }
        
        return null;
    }
  
        
    private boolean validateDefaultValue(Question q) {
        if (q.getType().equals(QuestionType.TEXTAREA) || q.getType().equals(QuestionType.TEXTBOX) || 
        	q.getType().equals(QuestionType.VISUAL_SCALE) || q.getType().equals(QuestionType.PATIENT_CALENDAR) || 
        	q.getDefaultValue() == null || q.getDefaultValue().trim().equals("")) {
            return true;
            
        } else if (q.getType().equals(QuestionType.IMAGE_MAP)) {
            boolean valueFound = false;
            List<ImageMapOption> imOptions = ((ImageMapQuestion)q).getOptions();
            for (ImageMapOption imOption : imOptions)  {
            	if (q.getDefaultValue().trim().equals(imOption.getOption().trim())) {
            		valueFound = true;;
            	}
            }
            return valueFound;
            
        } else {
            boolean valueFound = false;
            List<Answer> answers = q.getAnswers();
            for (Answer answer : answers) {
            	if (q.getDefaultValue().trim().equals(answer.getDisplay().trim())) {
            		valueFound = true;;
            	}
            }
            return valueFound;
        }
    }
    
    
    private void updateExtraInfo(Question editQuestion, Question origQuestion) throws CtdbException {
        editQuestion.setDefaultValue(origQuestion.getDefaultValue());
        editQuestion.setGroupsAssociatedWith(origQuestion.getGroupsAssociatedWith());
    }
    
    private boolean addAnswers(Question q, PrintWriter out) {

    	if (questionForm.getType() == 1  || questionForm.getType() == 2 ||
    		questionForm.getType() == 9  || questionForm.getType() == 10 || 
    		questionForm.getType() == 11 || questionForm.getType() == 12 ) {
    		return true;
    		
    	} else {
            List<Answer> answers = new ArrayList<Answer>();
            List<QuestionOption>  questionOptionsObjectList = questionForm.getQuestionOptionsObjectList();
            if(questionOptionsObjectList.size() > 0) {
            	Iterator iter = questionOptionsObjectList.iterator();
            	Answer a;
            	while(iter.hasNext()) {
            		QuestionOption obj = (QuestionOption)iter.next();
            		 a = new Answer();
            		 String option = obj.getOption();
            		 String score = obj.getScore();
            		 String submittedValue = obj.getSubmittedValue();
                     a.setDisplay(option.trim());
                     if(score != null && !score.trim().equals("")) {
                    	 a.setScore(Double.parseDouble(score.trim()));
                     }
                     if(submittedValue != null && !submittedValue.trim().equals("")) {
                    	 a.setSubmittedValue(submittedValue.trim());
                     }
                     answers.add(a);
            	}
            	 
            	 if (q.isIncludeOtherOption()) {
                 	a = new Answer();
                 	a.setDisplay(CtdbConstants.OTHER_OPTION_DISPLAY);
                 	a.setCodeValue("null");
                 	answers.add(a);
                 }
            	 
            	 q.setAnswers(answers);
            }

            return true;
        }
    }
    
    private boolean checkAnswers(String[] options, QuestionType type, PrintWriter out) {
        if (options == null || options.length == 0) {
        	out.print("OneOptionError");
        	out.flush();
        	return false;
        	
        } else if ((type.equals(QuestionType.SELECT ) && options.length < 1) ||
        		   (type.equals(QuestionType.RADIO ) && options.length < 1)) {
            out.print("CheckBoxError");
       	 	out.flush();
           return false;
           
        }
        
        return true;
    }
    
    
    private boolean scoreValidation(String[] options, QuestionType type, PrintWriter out) {
    	if (type.equals(QuestionType.RADIO) || type.equals(QuestionType.SELECT)) {
	    	int a = 0, b=0;
	        for (int i=0; i<options.length; i++) {
	        	String option = options[i];
	        	if(option.endsWith("|")) {
	        		option = option + " ";
	        	}
	        	String[] codeValue = option.split("\\|");
	            if (codeValue.length > 2 && !("".equals(codeValue[1].trim()))) {
	            	b = 1;
	            } else {
	            	a = 1;
	            }
	        }
	        
	        if (a == b) {
	        	out.print("scoreMissing");
	        	out.flush();
	        	return false;
	        } else { 
	        	return true;
	        }
    	} else { // multi-select and checkbox is not need to calidate the score
    		return true;
    	}
    }

	public AddEditQuestionForm getQuestionForm() {
		return questionForm;
	}

	public void setQuestionForm(AddEditQuestionForm questionForm) {
		this.questionForm = questionForm;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}
}
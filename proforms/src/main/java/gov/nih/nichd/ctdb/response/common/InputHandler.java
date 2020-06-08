package gov.nih.nichd.ctdb.response.common;

import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.GenericValidator;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.joda.time.Period;
import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.attachments.domain.Attachment;
import gov.nih.nichd.ctdb.attachments.manager.AttachmentManager;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.common.ServerFileSystemException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.emailtrigger.domain.EmailTrigger;
import gov.nih.nichd.ctdb.emailtrigger.domain.EmailTriggerValue;
import gov.nih.nichd.ctdb.emailtrigger.thread.EmailTriggerThread;
import gov.nih.nichd.ctdb.form.domain.CalculatedFormQuestionAttributes;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.domain.FormQuestionAttributes;
import gov.nih.nichd.ctdb.form.domain.Section;
import gov.nih.nichd.ctdb.patient.domain.PatientVisit;
import gov.nih.nichd.ctdb.patient.domain.PatientVisitPrepopValue;
import gov.nih.nichd.ctdb.patient.manager.PatientManager;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.question.domain.Answer;
import gov.nih.nichd.ctdb.question.domain.AnswerType;
import gov.nih.nichd.ctdb.question.domain.CalculationType;
import gov.nih.nichd.ctdb.question.domain.ConversionFactor;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.domain.QuestionType;
import gov.nih.nichd.ctdb.question.domain.SkipRuleOperatorType;
import gov.nih.nichd.ctdb.question.domain.SkipRuleType;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;
import gov.nih.nichd.ctdb.response.domain.PatientCalendarCellResponse;
import gov.nih.nichd.ctdb.response.domain.Response;
import gov.nih.nichd.ctdb.response.form.DataEntrySetupForm;
import gov.nih.nichd.ctdb.response.manager.ResponseManager;
import gov.nih.nichd.ctdb.response.util.DataCollectionUtils;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.security.manager.SecurityManager;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

/**
 * The InputHandler represents the Java class that parses the response from the form page
 * and does validation
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class InputHandler {
	private static Logger logger = Logger.getLogger(InputHandler.class);
	
    /**
     * Get responses from the request and validate.
     *
     * @param aform    The AdministratedFormObject containing form data
     * @param request  The HTTP request we are processing
     * @param validate The boolean value indicating if validation is needed.
     * @throws CtdbException Thrown if any error occurs
     * @throws MessagingException 
     * @throws UnknownHostException 
     * @throws ParseException 
     */
    public static List<String> getResponses(AdministeredForm aform, HttpServletRequest request, boolean validate,boolean isSelfReporting,DataEntrySetupForm dataEntryForm) throws CtdbException, 
    		UnknownHostException, MessagingException, ParseException  {
        
    	String responseVisitDate = null;
    	
    	//get list of prepop vals from db
    	List<PatientVisitPrepopValue> pvPrepopValueList = new ArrayList<PatientVisitPrepopValue>();
		PatientManager patientMan = new PatientManager();
		ProtocolManager protocolMan = new ProtocolManager();
		
		boolean isVisitDate_DateTime = false;
		try {
			PatientVisit pv = null;
			int intervalId = aform.getInterval().getId();
			pv = patientMan.getPatientVisit(aform.getPatient().getId(), aform.getVisitDate(), intervalId);
			int visitdateId = pv.getId();
			pvPrepopValueList = protocolMan.getPvPrepopValuesForInterval(intervalId, visitdateId);
		}catch(ObjectNotFoundException e) {
			logger.info("No Scheduled visit for Subject.");
		}
    	
    	
    	List<String> errors = new ArrayList<String>();
        //added by Ching Heng
        User u;
        if(isSelfReporting) {
        	SecurityManager sm = new SecurityManager();
    		u = sm.getPatientUser();
        }else {
        	u = (User) request.getSession().getAttribute(CtdbConstants.USER_SESSION_KEY);
        }
        
        AttachmentManager am = new AttachmentManager();
        ProtocolManager pm = new ProtocolManager();
        Form form = aform.getForm();
        List<Question> allQuestionsInForm = new ArrayList<Question>();
        
        // get all allQuestionsInForm in the form
        for ( List<Section> row : form.getRowList() ) {
            for ( Section section : row ) {
                if (section != null) {
                	int sectionId = section.getId();
                	List<Question> secQuestionList = section.getQuestionList();
                	
                	if ( secQuestionList != null ) {
                		for ( Question q : secQuestionList ) {
                			q.setSectionId(sectionId);
                		}
                	}
                	
                    allQuestionsInForm.addAll(secQuestionList);
                }
            }
        }
        
        // validate required allQuestionsInForm

        // A Hash map from question ID to answer for calculated allQuestionsInForm
        Map<String, String> answersCalculated = new HashMap<String, String>();

        if (validate) {
            calculate(aform,allQuestionsInForm, request, errors, answersCalculated);
            if ( aform.isMarkAsCompleted() ) {
            	validateRequired(allQuestionsInForm, request, answersCalculated, errors, false,aform,dataEntryForm);
            	validateSkipRequired(allQuestionsInForm, request, answersCalculated, errors,aform,dataEntryForm);
            }
          
        } else {
            calculate(aform,allQuestionsInForm, request, null, answersCalculated);
        }

        //get a map of current responses
        Map<String, Integer> questionResponse = new HashMap<String, Integer>();
        Response r = new Response();
        
        for ( Response response : aform.getResponses() ) {
            if ( !(response instanceof PatientCalendarCellResponse) ) {
            	if( questionResponse.containsKey("S_" + response.getQuestion().getSectionId() + "_Q_" + response.getQuestion().getId()) ){
            		String  browserDetails  =   request.getHeader("User-Agent");
            		//send email in case of duplicate records
    				String emailSubject = "Duplicate data entries in Data Collection in Input Handler";
    				String emailBodyText = "There are duplicate entries in data collection Input Handler.\n Please check your postgres Database in following Environment:\t"+InetAddress.getLocalHost().getHostName()+
    						"\n InputHandler->getResponses->AdminFormId:\t"+aform.getId()+
    						"\n InputHandler->getResponses->comingFromAutoSaver:\t"+aform.isComingFromAutoSaver()+
    						"\n InputHandler->getResponses->currently logged in user->getUsername():\t"+((User)request.getSession().getAttribute(CtdbConstants.USER_SESSION_KEY)).getUsername()+
    						"\n InputHandler->getResponses->aform.getCreatedBy()"+aform.getCreatedBy()+
    						"\n InputHandler->getResponses->response.getQuestion().getSectionId()"+response.getQuestion().getSectionId()+
    						"\n InputHandler->getResponses->response.getQuestion().getId()"+response.getQuestion().getId()+
    						"\n InputHandler->aform->formName"+aform.getForm().getName()+
    						"\n InputHandler->aform->studyID"+aform.getForm().getId()+
    						"\n InputHandler->aform->intervalId"+aform.getInterval().getId()+
    						"\n InputHandler->aform->patientId"+aform.getPatient().getId()+
    						"\n InputHandler->Browser deatails"+browserDetails+
    						"\n InputHandler->Timestamp"+(new Date()).toString();
    				DataCollectionUtils.sendEmailOnDuplicateDataEntry(emailSubject,emailBodyText);
    				throw new DuplicateObjectException("------------->Duplicate data sneaked in at this point.<--------");
            	}
            	
                questionResponse.put("S_" + response.getQuestion().getSectionId() + "_Q_" + response.getQuestion().getId(), new Integer(response.getId()));
            } 
        }
 
        // update/add the responses
        List<Response> responses = new ArrayList<Response>();
        for ( Question q : allQuestionsInForm ) {
             List<String> answers = new ArrayList<String>();
             List<String> submitAnswers = new ArrayList<String>();
             String otherValue[] = null;

	         if ( !q.getFormQuestionAttributes().isCalculatedQuestion() && !q.getFormQuestionAttributes().isCountQuestion()) { // it is NOT calculate question or count question
	        	if ( q.getType().getValue()!=QuestionType.File.getValue() ) { // it is NOT a File type question
					String S_Q = "S_" + q.getSectionId() + "_Q_" + q.getId();
	                String values[] = request.getParameterValues(S_Q);

	                if (values != null) {
	                    for ( int i = 0; i < values.length; i++ ) {
	                        String a = values[i].trim();
	                        boolean overMax4000 = false;
	                        
	                        if (q.isIncludeOtherOption()) {
	                            // for the "Other textbox"
	                            if( a.equals(CtdbConstants.OTHER_OPTION_DISPLAY) ){
	                            	otherValue = request.getParameterValues(S_Q+"_otherBox");
	                            	a = otherValue[0];
	                            	
	                            	if ( a.isEmpty() ) {
	                            		errors.add("Answer for \"Other, please specify\" for question \"" + q.getText() + "\" is required.  <a href=\"Javascript:goToValidationErrorFlag();document.getElementById ('S_" + q.getSectionId() + "_Q_" + q.getId() + "_otherBox').focus();\">Go To</a>");
	                            		a = CtdbConstants.OTHER_OPTION_DISPLAY;
	                            		answers.add(a);
	                            		submitAnswers.add(a);
	                            	}
	                            }
	                        }
	                        
	                        if (q.getFormQuestionAttributes().getAnswerType() == AnswerType.STRING && a.length() > 4000) {
	                        	errors.add("Maximum 4000 characters required for question \"" + q.getText() + 
	                            		"\".  <a href=\"Javascript:goToValidationErrorFlag(); document.getElementById('S_" + 
	                            		q.getSectionId() + "_Q_" + q.getId() + "').focus();\">Go To</a>");
	                        	overMax4000 = true;
	                        }
	                        if (a.length() != 0) {
	                            if (validate) {
	                                validateAnswer(q, a, errors,overMax4000);
	                            } 
	
	                            answers.add(a);
	                            
	                            //determine submit answer...could be different in cases for select/multi-select/radio/and CB....also for textbox if it is
	                            //type numeric and has set up a unit conversion factor and then do decimal precision if necessary
	                            String sa = a;
	                            
	                            if ( q.getType() == QuestionType.SELECT || q.getType() == QuestionType.MULTI_SELECT || 
	                            		q.getType() == QuestionType.RADIO || q.getType() == QuestionType.CHECKBOX ) {
	                            	sa = determineSubmitAnswerForSelectTypeQuestions(q,sa);
	                            }
	                            else if (q.getType() == QuestionType.TEXTBOX && 
	                            		q.getFormQuestionAttributes().getAnswerType().equals(AnswerType.NUMERIC) && 
	                            		q.getFormQuestionAttributes().isHasUnitConversionFactor()) {
	                            	sa = calculateUnitConversionFactor(q,sa,errors);
	                            	
	                            	if ( q.getFormQuestionAttributes().getDecimalPrecision() != -1 ) {
	        	                     	double d = Double.valueOf(sa);
	        	                 		int precision = q.getFormQuestionAttributes().getDecimalPrecision();
	        	                 		String formatString = "#0";
	        	                 		DecimalFormat df = null;
	        	
	        	             			if ( precision != 0 ) {
	        	             				formatString = formatString + ".";
	        	             				
	        	             				for ( int k = 1; k <= precision; k++ ) {
	        	             					formatString = formatString + "0";
	        	             				}
	        	             			}
	        	             			
	        	             			df = new DecimalFormat(formatString);
	        	             			sa = df.format(d);
	        	                     }
	                            }
	                            
	                            submitAnswers.add(sa);
	                        }
	                    }
	                }
	        	}
	        	else { // It is a File type question added by Ching Heng
	                String key = "S_" + q.getSectionId() + "_Q_"+String.valueOf(q.getId());
	                
	                if ( aform.getFiles() != null && aform.getFiles().get(key) != null ) {
	                    Attachment at = (Attachment) aform.getFiles().get(key);
	                    
	                    if ( at != null && !Utils.isBlank(at.getFileName()) ) { // the file exist
		       	            at.updateCreatedByInfo(u);
		       	            at.updateUpdatedByInfo(u);
		       	            at.setAssociatedId(form.getId());
		       	            at.setType(new CtdbLookup(AttachmentManager.FILE_COLLECTION));
		       	            
		       	            try {
		       	            	// save real file to computer and database
								am.createAttachment(at, form.getProtocol().getId());
							}
		       	            catch (ServerFileSystemException e) {
		       	            	logger.warn("Could not create the attachment.", e);
							}
		       	            
		       	            // the answer of File type question is "fileName:attachementId"
		       	            String fileTypeAnswer=at.getFileName()+":"+String.valueOf(at.getId());
		       	            answers.add(fileTypeAnswer);
		       	            submitAnswers.add(fileTypeAnswer);
	                    }
	               }
	        	}
	        }
	         else { // calculated question, file type question can't be calculated question
	            String a = (String) answersCalculated.get("S_" + q.getSectionId() + "_Q_" + q.getId());
	            //do decimal precision!!!!!!  NISH
	            
	            if ( a != null ) {
	                if( q.getFormQuestionAttributes().getDecimalPrecision() != -1 ) {
	                	double d = Double.valueOf(a);
	            		int precision = q.getFormQuestionAttributes().getDecimalPrecision();
	            		String formatString = "#0";
	            		DecimalFormat df = null;
	
	        			if ( precision != 0 ) {
	        				formatString = formatString + ".";
	        				
	        				for ( int i = 1; i <= precision; i++ ) {
	        					formatString = formatString + "0";
	        				}
	        			}
	        			
	        			df = new DecimalFormat(formatString);
	        			a = df.format(d);
	                }
	
	                if (validate) {
	                    validateAnswer(q, a, errors, false);
	                } 
	                
	                answers.add(a);
	                
	              //determine submit answer...could be  for textbox if it is type numeric and has set up a unit conversion factor
	              //then do decmial precision again if necessary  
	              String sa = a;
	              
	              if ( q.getType() == QuestionType.TEXTBOX && q.getFormQuestionAttributes().getAnswerType().equals(AnswerType.NUMERIC) && 
	            		  q.getFormQuestionAttributes().isHasUnitConversionFactor() ) {
	            	 sa = calculateUnitConversionFactor(q,sa,errors);
	              
	              	 if ( q.getFormQuestionAttributes().getDecimalPrecision() != -1 ) {
	                 	double d = Double.valueOf(sa);
	             		int precision = q.getFormQuestionAttributes().getDecimalPrecision();
	             		String formatString = "#0";
	             		DecimalFormat df = null;
	
	         			if ( precision != 0 ) {
	         				formatString = formatString + ".";
	         				
	         				for ( int i = 1; i <= precision; i++ ) {
	         					formatString = formatString + "0";
	         				}
	         			}
	         			
	         			df = new DecimalFormat(formatString);
	         			sa = df.format(d);
	                 }
	              } 
	              
	              submitAnswers.add(sa);
	            }
	        }
	
	        if ( answers.size() > 0 ) {
	            r = new Response();
	            Integer responseId = (Integer) questionResponse.get("S_" + q.getSectionId() + "_Q_" + q.getId());
	            
	            if ( responseId != null ) {
	                r.setId(responseId.intValue());
	            }
	            
	            if ( otherValue != null && !otherValue[0].isEmpty() ) {
	            	r.setAnswerIncludesOtherPleaseSpecify(true);
	            }
	            
	            r.setQuestion(q);
	            r.setAdministeredForm(aform);
	            answers = removeDuplicates(answers);
	            if(q.getType() == QuestionType.SELECT || q.getType() == QuestionType.MULTI_SELECT || 
                		q.getType() == QuestionType.RADIO || q.getType() == QuestionType.CHECKBOX) {
	
					//we want the answers to be stored in the bd as PVD always! (the submit answers will be PV)
					List<Answer> questionOptions = q.getAnswers();
					if(q.isDisplayPV()) {
						Iterator ansIter = answers.iterator();
						int index = 0;
						while(ansIter.hasNext()) {
							String answer = (String)ansIter.next();
							//need to find matching pvd for this 
							Iterator qOptionsIter = questionOptions.iterator();
							while(qOptionsIter.hasNext()) {
								Answer a = (Answer)qOptionsIter.next();
								String pvd = a.getPvd();
								String submittedValue = a.getSubmittedValue();
								if(answer.equals(submittedValue)) {
									answer = pvd;
									answers.set(index, answer);
									break;	
								}	
								
							}
							index++;
						}	
					}
					r.setAnswers(answers);
				} else {
					r.setAnswers(answers);
				}
	            
	            r.setSubmitAnswers(submitAnswers);
	            responses.add(r);
	            
	             //check on visitdate...whatever user answers for visist date de, we need to update aform visitdate with it
	        	 if(responseVisitDate == null) {
		        	  String dataElementName = q.getFormQuestionAttributes().getDataElementName();
		        	  if(dataElementName.equals(CtdbConstants.VISIT_DATE_DATA_ELEMENT)) {
		        		  if(q.getFormQuestionAttributes().getAnswerType() == AnswerType.DATETIME) {
		        			  isVisitDate_DateTime = true; 
		        		  }
		        		  responseVisitDate = answers.get(0);
		        	  }
	        	  }
	        	 
	        	//check and make sure that guid entered as answer is same as aform guid...also check that 
	        	 //visit type entered as answer is same as aform visit type
	        	 String dataElementName = q.getFormQuestionAttributes().getDataElementName();
	        	 if(dataElementName.equals(CtdbConstants.GUID_DATA_ELEMENT)) {
	        		String aformGUID =  aform.getPatient().getGuid();
	        		if(!answers.get(0).equalsIgnoreCase(aformGUID)) {
	        			errors.add(CtdbConstants.GUID_ERR_TEXT +  "\"" + q.getText() + "\".  <a href=\"Javascript:goToValidationErrorFlag(); document.getElementById('S_" + q.getSectionId() + "_Q_" + q.getId() + "').focus();\">Go To</a>");
	        		}
	        		 
	        	 }
	        	 if(dataElementName.equals(CtdbConstants.VISIT_TYPE_DATA_ELEMENT) || dataElementName.equals(CtdbConstants.VISIT_TYP_PDBP_DATA_ELEMENT)) {
	        		 String aformVisitType = aform.getInterval().getName();
	        		 if(!answers.get(0).equalsIgnoreCase(aformVisitType)) {
	        			 errors.add(CtdbConstants.VTYPE_ERR_TEXT +  "\"" + q.getText() + "\".  <a href=\"Javascript:goToValidationErrorFlag(); document.getElementById('S_" + q.getSectionId() + "_Q_" + q.getId() + "').focus();\">Go To</a>");
	        		 }
	        		 
	        	 }
	        	 
	
	        }
	        else if ( questionResponse.get("S_" + q.getSectionId() + "_Q_" + q.getId()) != null ) {
	          // there exists a respsonse, but no answers, someone is deleting their answers
	          if( aform.isNonPatient() && q.getType().getValue()==QuestionType.File.getValue() ) {
	        	  // but for file type, it is not satisfied this condition
	        	  // we do some thing for patient form, but "non patient form" need other way to deal with it.
	        	  r = new Response();
	        	  r.setId(questionResponse.get("S_" + q.getSectionId() + "_Q_" + q.getId()).intValue());
	        	  r.setQuestion(q);
	        	  r.setAdministeredForm(aform);
	        	  r.setAnswers(answers);
	        	  r.setSubmitAnswers(submitAnswers);
	        	  responses.add(r);
	          }
	          else {
	        	  r = new Response();
	        	  r.setId(questionResponse.get("S_" + q.getSectionId() + "_Q_" + q.getId()).intValue());
	        	  r.setQuestion(q);
	        	  r.setAdministeredForm(aform);
	        	  answers=removeDuplicates(answers);
	        	  r.setAnswers(answers);
	        	  r.setSubmitAnswers(submitAnswers);
	        	  responses.add(r);
	          }
			}

			aform.setResponses(responses);
        }
        
        boolean noAnswers = true;
        
        for ( Response res : aform.getResponses() ) {
        	Question q = res.getQuestion();
        	String deName = q.getFormQuestionAttributes().getDataElementName();
        	boolean isCalculated = q.getFormQuestionAttributes().isCalculatedQuestion();
        	boolean isCount = q.getFormQuestionAttributes().isCountQuestion();
        	
			// we will ignore all prepop questions and any calculated and count questions bc calculated and count questions can include the
			// prepop questions in the calculation
        	if(!isPrepop(deName,pvPrepopValueList) && !isCalculated && !isCount) {
        		List<String> a = res.getAnswers();
	        	
	        	if( a!= null && a.size() > 0 ) {
	        		noAnswers = false;
	        		break;
	        	}
        	}
        }

        if(errors.size() == 0 && responseVisitDate != null) {
			aform.setVisitDate(DataCollectionUtils.convertStringToDate(responseVisitDate,isVisitDate_DateTime));
		}
		aform.setAreThereAnswers(!noAnswers);
		
		// Email Trigger
		for (Response res : aform.getResponses()) {
			Question q = res.getQuestion();

			EmailTrigger emailTrigger = q.getFormQuestionAttributes().getEmailTrigger();
			if (!emailTrigger.getToEmailAddress().equals("")) {
				Boolean isTrigged = checkEmailIsTriggerred(emailTrigger, res, aform.getResponses(), q.getType(),
						q.getFormQuestionAttributes().getAnswerType());
				if (isTrigged) {
					String condStrWithVal = getConditionStrWithText(emailTrigger, res, aform.getResponses(),
							q.getType(), q.getFormQuestionAttributes().getAnswerType(), false);
					// this question has an email trigger and is answered
					EmailTriggerThread ett = new EmailTriggerThread(emailTrigger, res, q.getType(),
							q.getFormQuestionAttributes().getAnswerType(), condStrWithVal);
					ett.start();
				}
			}
		}
        
        return errors;
    }
    
    
    
    public static boolean isPrepop(String dataElementName,List<PatientVisitPrepopValue> pvPrepopValueList) {
    	if(dataElementName.equals(CtdbConstants.VISIT_DATE_DATA_ELEMENT)) {
    		return true;
    	}else if(dataElementName.equals(CtdbConstants.VISIT_TYPE_DATA_ELEMENT)) {
    		return true;
    	}else if(dataElementName.equals(CtdbConstants.VISIT_TYP_PDBP_DATA_ELEMENT)) {
    		return true;
    	}else if(dataElementName.equals(CtdbConstants.GUID_DATA_ELEMENT)) {
    		return true;
    	}else if(dataElementName.equals(CtdbConstants.SITE_NAME_DATA_ELEMENT)) {
    		return true;
    	}
    	for(PatientVisitPrepopValue pvPrepopValue : pvPrepopValueList){
			if (dataElementName.equals(pvPrepopValue.getPrepopDataElement().getShortName())){
				return true;
			}
		}
    	return false;
    }
    
    
    public static String determineSubmitAnswerForSelectTypeQuestions(Question q, String answer) {
		String submitAnswer = null;
		List<Answer> questionOptions = q.getAnswers();
		
		if (questionOptions != null && questionOptions.size() > 0 && questionOptions.get(0).getDisplay() != null) {
			Iterator<Answer> iter = questionOptions.iterator();
			while(iter.hasNext()) {
				Answer a = iter.next();
				String dispValue = a.getDisplay();
				if (answer.equals(dispValue)) {
					//now check to see if there is a submitValue
					String submitValue = a.getSubmittedValue();
					if(submitValue != null && !submitValue.equals("")) {
						submitAnswer = submitValue;
					} else {
						submitAnswer = answer;
					}
					
					break;
				}
			}
		}
		
		//no questionoptions so set submitanswer same as answer
		if(submitAnswer == null) {
			submitAnswer = answer;
		}
				
		return submitAnswer;
		
	}

    /**
     * Calculate answers for calculated questions.
     *
     * @param questions         The list of questions
     * @param request           The HTTP request we are processing
     * @param errors            The error message list generated by this function. If null, no error is saved.
     * @param answersCalculated The map from Integer question id to calculated answer String generated by this function
     * @throws CtdbException Thrown if any error occurs
     */
    public static void calculate(AdministeredForm aform, List<Question> questions, HttpServletRequest request, List<String> errors, Map<String, String> answersCalculated) throws CtdbException {
    	
    	/*
    	 * Here's the deal:
    	 * * It is possible to have multiple "layers" of calculated questions.  That means that a calc question can rely
    	 *   on OTHER calculated questions, which themselves can rely on others, on down to regular questions.  We know
    	 *   that questions have to either rely on regular questions or be calculable on their own.
    	 * * We don't know the order of these questions.  The most reliant question could be the first in the form.
    	 * * Therefore, we can't say with certainty that going through the questions top to bottom and calculating each
    	 *   will yield correct answers.
    	 * * We ALSO cannot say that we got ANY answer from the front end for calculated questions.
    	 * * Therefore, we need to calculated all calculable questions here.
    	 * * We do not have a good way to reference other questions here because they are all in a list with no reference
    	 *   so we cannot easily set up a tree of reference and recurse through them.
    	 * * To calculate them, then, we will use a method from computational math: evolving the SET of equations until 
    	 *   an equilibrium is reached.
    	 * * That involves:
    	 *   1. Calculate answers to ALL calculable questions and store the list of answers in an ordered structure
    	 *   2. Calculate answers to ALL calculable questions AGAIN - using the answers from the first run as the
    	 *      "stored" values instead of what came from the front end.  Store those in an ordered structure.
    	 *   3. Compare the two structures.  If they are the same, we are finished and the system has found the correct
    	 *      answers.  Otherwise, copy structure from 2 into the structure from 1 then run steps 2 and 3 again.
    	 *   4. Repeat until the two structures are equivalent.  Once they are, the answers are correct.
    	 * 
    	 * @author Joshua Park
    	 */
    	
		Map<String, String> evolutionOneAnswers = new HashMap<String, String>();
    	// first iteration: compares empty map to answersCalculated - fails if answersCalculated is empty - as intended
    	// second iteration and after: compares previous evolution to new evolution
    	// our eventual aim is to have the correct answers in answersCalculated
    	do {
    		// empty and replace contents of evolutionOneAnswers to ensure it is functionally equivalent to answersCalculated
    		evolutionOneAnswers.clear();
    		evolutionOneAnswers.putAll(answersCalculated);
    		for (Question question : questions) {
    			if (question.getFormQuestionAttributes().isCalculatedQuestion() || question.getFormQuestionAttributes().isCountQuestion()) {
    				calculateSingleLayer(aform, question, request, errors, answersCalculated);
    			}
    		}
    		// answersCalculated has new values
    	}
    	while (!hashMapsEquivalent(answersCalculated, evolutionOneAnswers));
    }
    
    public static String calculateSingleLayer(AdministeredForm aform, Question question, HttpServletRequest request, List<String> errors, Map<String, String> answersCalculated) throws CtdbException {
    	String answer = "";
    	String questionKey = "S_" + question.getSectionId() + "_Q_" + question.getId();
    	String deName = question.getName().substring(question.getName().indexOf("_")+1, question.getName().length());
    	Map<String, String> dependentQuestionAnswers = new HashMap<String, String>();
    	
    	boolean conditional = question.getCalculatedFormQuestionAttributes().getConditionalForCalc();
    	float conditionalCode = StrutsConstants.CONDITIONAL_CODE_FOR_DO_NOT_CALCULATE;
        boolean userEntry = false;
        for ( Question q : question.getCalculatedFormQuestionAttributes().getQuestionsToCalculate() ) {
        	String postedValue = "";
        	String postedValueDip = "";
        	String qToCalculateKey = "S_" + q.getSectionId() + "_Q_" + q.getId();
        	boolean isSelect = false;
        	
        	if(question.getFormQuestionAttributes().isCountQuestion()) {
        		if ( q.getType().getValue()==QuestionType.File.getValue() ) {
        			//need to handle file type questions since they are bit different
        			 if ( (aform.getFiles() != null && aform.getFiles().get(qToCalculateKey) != null) || (aform.getAssocFileQuestionIds() != null && aform.getAssocFileQuestionIds().contains(qToCalculateKey)) ) {
        				postedValue = "1";
             			dependentQuestionAnswers.put(qToCalculateKey, postedValue);
        			 }else {
        				 postedValue = "0";
        			 }
        		}else {
        			//all other questions
        			if (answersCalculated.containsKey(qToCalculateKey)) {
                		postedValue = answersCalculated.get(qToCalculateKey);
                	}else {
                		postedValue = request.getParameter(qToCalculateKey);
                		if(postedValue != null && !postedValue.equals("")) {
                			postedValue = "1";
                			dependentQuestionAnswers.put(qToCalculateKey, postedValue);
                		}else {
                			postedValue = "0";
                		}
                	}
        		}
        		
        		
        		
        		
        	}else {
        		if( QuestionType.SELECT.equals(q.getType()) || QuestionType.RADIO.equals(q.getType()) ) {
                	isSelect = true;
                	if (answersCalculated.containsKey(qToCalculateKey)) {
                		postedValueDip = answersCalculated.get(qToCalculateKey);
                	}
                	else {
                		postedValueDip = request.getParameter(qToCalculateKey);
                	}
                	
                	for (Answer ans : q.getAnswers() ) {
                		if ( ans.getDisplay().equalsIgnoreCase(postedValueDip) ) {
                			postedValue = String.valueOf(ans.getScore());
                			
                			// other,please specify has a score of Intger.min. if user selects other please specify and enters something. Don't do calculation
                			if ( ans.getScore() == Integer.MIN_VALUE ) {
                				answersCalculated.put(questionKey, null);
                				return null;
                			}
                		}
                	}
                }
                else {
                	if (answersCalculated.containsKey(qToCalculateKey)) {
                		postedValue = answersCalculated.get(qToCalculateKey);
                	}
                	else {
                		postedValue = request.getParameter(qToCalculateKey);
                	}
                }
                
                if (postedValue != null) {
                    postedValue = postedValue.replaceAll("\\.\\.", "\\.");
                    dependentQuestionAnswers.put(qToCalculateKey, postedValue);
                }
                
              if(conditional) {
                	if(postedValue == null || postedValue.equals("") || (isSelect && (Float.parseFloat(postedValue)==conditionalCode))) {
                		if(!deName.equalsIgnoreCase("PROMISRawScore")) {
                		return null;
                		}else {
                			userEntry = true;
                	}
                }
                }
        		logger.error("postedValue------------------------------------------"+postedValue);
        		logger.error("postedValueDip------------------------------------------"+postedValueDip);
        	}
        	
        	
        	
            
        }
            
        try {		
        	String S_Q = "S_" + question.getSectionId() + "_Q_" + question.getId();
			String values[] = request.getParameterValues(S_Q);
        	if(userEntry) {
        		if(values[0].length() == 0) {
        			return null;
        		}else {
        			answersCalculated.put(questionKey, values[0]);
        			return values[0];
        		}
        	}

            double d = calculate(question, dependentQuestionAnswers, errors);
            answer = formatNumber(d, 8);
            answersCalculated.put(questionKey, answer);
        }
        catch (BlankCalculationException bce) {
            //bce.printStackTrace();
        	logger.debug("Blank calculation exception caught but ignored");
        }
    	catch (ScriptException se) {
    		logger.debug("Script Exception exception caught but ignored"); 
    	}
    	return answer;
    }
    
    public static boolean hashMapsEquivalent(Map<String, String> mapOne, Map<String, String> mapTwo) {
    	for (final String key : mapOne.keySet()) {
    		if (mapTwo.containsKey(key)) {
    			if (!mapTwo.get(key).equals(mapOne.get(key))) {
    				// values aren't equal, fail fast
    				return false;
    			}
    		}
    		else {
    			// if the key doesn't exist (really shouldn't happen), they're not equivalent
    			return false;
    		}
    	}
    	return true;
    }
    
   public static String calculateUnitConversionFactor(Question q, String answer,List<String> errors) {
	   String calc = q.getFormQuestionAttributes().getUnitConversionFactor();
	   
	   calc = calc.replaceAll("\\[this\\]", answer);
	   calc = calc.replaceAll("%", "*(1/100)*");
	   
       org.nfunk.jep.JEP myParser = new org.nfunk.jep.JEP();
       myParser.addStandardFunctions();
       myParser.parseExpression(calc);
       
       double answerValue = myParser.getValue();
       String answerValueString = "0";

       if (Double.isNaN(answerValue) || Double.isInfinite(answerValue)) {
           if (errors != null) {
               errors.add("Submit answer for  question \"" + q.getText() + "\" can not be calculated because of division by zero. <a href=\"Javascript:goToValidationErrorFlag();document.getElementById ('S_" + q.getSectionId() + "_Q_" + q.getId() + "').focus();\">Go To</a>");
           }  
       } else {
    	   answerValue = trim(answerValue, 8);
    	   
    		if((int)answerValue == answerValue) {
    			answerValue = (int)answerValue;
    		}
    		
    	   answerValueString = String.valueOf(answerValue);
       }

       return answerValueString;
   }

   public static double calculate(Question question, Map<String, String> questionMap, List<String> errors) throws CtdbException, BlankCalculationException, ScriptException  {
    	if ( question.getFormQuestionAttributes().isCalculatedQuestion() || question.getFormQuestionAttributes().isCountQuestion()) {
            boolean atLeastOneAnswered = false;
            String calc = question.getCalculatedFormQuestionAttributes().getCalculation();
            List<String> errorTemp = new ArrayList<String>();

            if (question.getFormQuestionAttributes().getAnswerType().equals(AnswerType.NUMERIC)) {
                Pattern p = Pattern.compile("\\[(S_([0-9]+)_Q_([0-9]+))\\]");
                Matcher m = p.matcher(calc);
                String found;
                while (m.find()) {
                    found = m.group(1);
                    String replaceString = null;
                    
                    if ( questionMap.containsKey(found) ) {
                        replaceString = questionMap.get(found).trim();
                    }

                    if (replaceString == null || replaceString.length() < 1) {
                        replaceString = "0";
                    }
                    else {
                        atLeastOneAnswered = true;
                    }

                    double d = 0.0;

                    try {
                        d = Double.parseDouble(replaceString);
                    }
                    catch (NumberFormatException nfe) {
                        int qNumber = 0;
                        
                        try {
                           String qN = found.substring(found.indexOf("Q_")+2, found.length());
                        	qNumber = Integer.valueOf(qN);
                        }
                        catch (NumberFormatException nfe2) {
                        	qNumber = 0;
                        }

                        List<Question> questionList = question.getCalculatedFormQuestionAttributes().getQuestionsToCalculate();
                        String qText = "";
                        int sId = 0;
                        
                        for ( Question q : questionList ) {
                            if ( q.getId() == qNumber ) {
                                qText = q.getText();
                                sId = q.getSectionId();
                            }
                        }

                        if (errors != null) {
                        	String err = "Answer needs to be numeric for question \"" + qText + "\" which is used in the calculation of the question \"" + 
                                	question.getText() + "\".  <a href=\"Javascript:goToValidationErrorFlag();document.getElementById ('S_" + 
                                	sId + "_Q_" + qNumber + "').focus();\">Go To</a>";
                        	
                        	if(!errors.contains(err) && !errorTemp.contains(err)) {
                        		errorTemp.add(err);
                        	}
                        }
                    }
                    calc = calc.replaceAll("\\[" + found + "\\]", Double.toString(d));
                    m = p.matcher(calc);
                }
            }
            else if (question.getFormQuestionAttributes().getAnswerType().equals(AnswerType.DATE) ||
                    question.getFormQuestionAttributes().getAnswerType().equals(AnswerType.DATETIME)) {
                Pattern p = Pattern.compile("\\[S_([0-9]+)_Q_([0-9]+)\\]\\s*-\\s*\\[S_([0-9]+)_Q_([0-9]+)\\]");
                Matcher m = p.matcher(calc);

                while (m.find()) {
                	String found1 = "S_"+m.group(1)+"_Q_"+m.group(2);
                    String replaceString1 = "";
                    
                    if (questionMap.containsKey(found1)) {
                        replaceString1 = ((String) questionMap.get(found1)).trim();
                        if (replaceString1 != null && replaceString1.length() > 0) {
                            atLeastOneAnswered = true;
                        }
                    }

                    String found2 = "S_"+m.group(3)+"_Q_"+m.group(4);
                    String replaceString2 = "";
                    
                    if (questionMap.containsKey(found2)) {
                        replaceString2 = ((String) questionMap.get(found2)).trim();
                        
                        if (replaceString2 != null && replaceString2.length() > 0) {
                            atLeastOneAnswered = true;
                        }
                    }

                    double value = 0.0;

                    try {
                        value = Math.abs(getDateDiff(replaceString1, replaceString2,
                                question.getCalculatedFormQuestionAttributes().getConversionFactor()));
                    }
                    catch (NumberFormatException nfe) {
                        if (errors != null) {
                        	String err = "There was a problem in the date difference between QID#"
                                    + m.group(2) + " and QID#" + m.group(4)
                                    + " which is used in the calculation of the question \"" + question.getText()
                                    + "\".  <a href=\"Javascript:goToValidationErrorFlag();document.getElementById ('Q_" + m.group(2) + "').focus();\">Go To Q</a>";
                        	
                        	if(!errors.contains(err) && !errorTemp.contains(err)) {
                        		errorTemp.add(err);
                        	}
                        }
                    }
                    
                    calc = calc.replaceAll("\\[(" + found1 + ")\\]\\s*-\\s*\\[(" + found2 + ")\\]", Double.toString(value));
                    m = p.matcher(calc);
                }
            }
            else {
                throw new CtdbException("AnswerType " +
                        question.getFormQuestionAttributes().getAnswerType().getDispValue() +
                        " should not be used in calculated questions");
            }

            if (!atLeastOneAnswered) {
               throw new BlankCalculationException();
            }
            else {
                if (errors != null) {
                    errors.addAll(errorTemp);
                }
            }

            calc = calc.replaceAll("%", "*(1/100)*");
            org.nfunk.jep.JEP myParser = new org.nfunk.jep.JEP();
            myParser.addStandardFunctions();
            myParser.parseExpression(calc);
			double answerValue = myParser.getValue();
		   if (calc.contains("if (") || calc.contains("else {") || calc.contains("else if (")  || calc.contains("==") || calc.contains("!=") || calc.contains(">")  || calc.contains(">=") || calc.contains("<")  || calc.contains("<=") || calc.contains("&&")  || calc.contains("||")  || calc.contains("{")   || calc.contains("}")) {
				ScriptEngineManager mgr = new ScriptEngineManager();
				ScriptEngine engine = mgr.getEngineByName("JavaScript");
				logger.error("engine.eval(calc)------------------------------------------" + engine.eval(calc));
				Object answerObj = engine.eval(calc);
				if (Integer.class.isInstance(answerObj)) {
					answerValue = (double) ((Integer) engine.eval(calc)).intValue();
				}
				if (Double.class.isInstance(answerObj)) {
					answerValue = ((Double) engine.eval(calc)).doubleValue();
				}
		   }

            if (Double.isNaN(answerValue) || Double.isInfinite(answerValue)) {
                if (errors != null) {
                   
                	String err = "Answer for calculated question \"" + question.getText() + "\" can not be found because of division by zero. " +
                        	"<a href=\"Javascript:goToValidationErrorFlag();document.getElementById ('S_" + question.getSectionId() + "_Q_" + 
                        	question.getId() + "').focus();\">Go To</a>";
                	if(!errors.contains(err)) {
                		errors.add(err);
                	}
                }
                
                throw new BlankCalculationException();
            }

            answerValue = trim(answerValue, 10);
            
            return answerValue;
        }
    	else {
            return 0.0;
        }
    }

    public static double getDateDiff(String dateString1, String dateString2, ConversionFactor conversionFactor) throws CtdbException {
        Date date1 = new Date();
        boolean d1Blank = (dateString1 == "") || dateString1 == null;
        try {
            date1 = makeDate(dateString1);
        } catch (Exception e) {
            throw new NumberFormatException("Poor Date Format " + date1);
        }

        Date date2 = new Date();
        boolean d2Blank = (dateString2 == "") || dateString2 == null;
        try {
            date2 = makeDate(dateString2);
        } catch (Exception e) {
            throw new NumberFormatException("Poor Date Format " + date2);
        }

        if (d1Blank && d2Blank) {
            return 0;
        }

        if ((d1Blank && !d2Blank) ||
                (!d1Blank && d2Blank)) {
            if (d1Blank) {
                return timeSinceYear(date2, conversionFactor);
            } else if (d2Blank) {
                return timeSinceYear(date1, conversionFactor);
            }
        }

        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        if (date1.before(date2)) {
            cal1.setTime(date2);
            cal2.setTime(date1);
        } else {
            cal1.setTime(date1);
            cal2.setTime(date2);
        }

        long d1 = cal1.getTime().getTime() + cal1.get(Calendar.ZONE_OFFSET) + cal1.get(Calendar.DST_OFFSET);
        long d2 = cal2.getTime().getTime() + cal2.get(Calendar.ZONE_OFFSET) + cal2.get(Calendar.DST_OFFSET);
        double diff = d1 - d2;
        diff = Math.abs(diff);

        if (conversionFactor.getValue() == ConversionFactor.SECONDS.getValue()) {
            return diff / (1000.0);
        } else if (conversionFactor.getValue() == ConversionFactor.MINUTES.getValue()) {
            return diff / (1000.0 * 60);
        } else if (conversionFactor.getValue() == ConversionFactor.HOURS.getValue()) {
            return diff / (1000.0 * 60 * 60);
        } else if (conversionFactor.getValue() == ConversionFactor.DAYS.getValue()) {
            return diff / (1000.0 * 60 * 60 * 24);
        } else if (conversionFactor.getValue() == ConversionFactor.WEEKS.getValue()) {
            return diff / (1000.0 * 60 * 60 * 24 * 7);
        } else {
            DateTime dt1 = new DateTime(cal1.getTimeInMillis());
            DateTime dt2 = new DateTime(cal2.getTimeInMillis());

            Period p = new Period(dt2, dt1);
            if (conversionFactor.getValue() == ConversionFactor.MONTHS.getValue()) {

                double d =
                        (p.getYears() * 12.0) + p.getMonths() +
                        ((double)
                        (p.getWeeks() * 7.0 * 24.0 * 60.0 * 60.0 * 1000.0) +
                        (p.getDays() * 24.0 * 60.0 * 60.0 * 1000.0) +
                        (p.getHours() * 60.0 * 60.0 * 1000.0) +
                        (p.getMinutes() * 60.0 * 1000.0) +
                        (p.getSeconds() * 1000.0) +
                        p.getMillis()
                        ) / ((365.0 / 12.0) * 24.0 * 60.0 * 60.0 * 1000.0);
                return d;

            } else if (conversionFactor.getValue() == ConversionFactor.YEARS.getValue()) {
                double d = (double) p.getYears() +
                        ((double) p.getMonths() / 12.0) +
                        ((double)
                        (p.getWeeks() * 7.0 * 24.0 * 60.0 * 60.0 * 1000.0) +
                        (p.getDays() * 24.0 * 60.0 * 60.0 * 1000.0) +
                        (p.getHours() * 60.0 * 60.0 * 1000.0) +
                        (p.getMinutes() * 60.0 * 1000.0) +
                        (p.getSeconds() * 1000.0) +
                        p.getMillis()
                        ) / (365.0 * 24.0 * 60.0 * 60.0 * 1000.0);
                return d;
            } else
                return 0;
        }
    }

    private static Date makeDate(String value) throws NumberFormatException {
    	int year1 = 0;
    	int month = 0;
    	int date = 0;
    	int hour = 0;
    	int minute = 0;
    	int second = 0;

    	GregorianCalendar calendar;
    	List<String> values = new ArrayList<String>();

    	//Date: YYYY-MM-DD
    	if (value.indexOf(":") == -1) {
    		values = Utils.tokenizeToList(value, "-");

    		if (values.size() == 3) {
    			year1 = Integer.parseInt((String) values.get(0)) - 1;
    			month = Integer.parseInt((String) values.get(1));
    			date= Integer.parseInt((String) values.get(2));
    		}

    		calendar = new GregorianCalendar(year1, month, date);
    	}
    	//Date-Time: YYYY-MM-DD HH:MM
    	else {
    		value=value.replaceAll(" ","-").replaceAll(":","-"); // YYYY-MM-DD-HH-MM
    		values = Utils.tokenizeToList(value, "-");

    		if (values.size() >= 3) {
    			year1 = Integer.parseInt((String) values.get(0)) - 1;
    			month = Integer.parseInt((String) values.get(1));
    			date = Integer.parseInt((String) values.get(2));
    		}

    		if (values.size() == 5) {
    			hour = Integer.parseInt((String) values.get(3));
    			minute = Integer.parseInt((String) values.get(4));
    			second = Integer.parseInt("0");
    		}

    		calendar = new GregorianCalendar(year1, month, date, hour, minute, second);
    	}

    	return calendar.getTime();
    }

    private static double timeSinceYear(Date date, ConversionFactor conversionFactor) throws CtdbException {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(date);
        int year = cal.get(Calendar.YEAR);

        date = new Date();
        cal.setTime(date);
        cal.set(year, 1, 1, 0, 0, 0);

        return Math.abs(getDateDiff(date.toString(), cal.getTime().toString(), conversionFactor));
    }

    public static double doDTCalculation(CalculatedFormQuestionAttributes formQuestionAttrs, List<String> answers) throws CtdbException {
        if (answers.isEmpty()) {
            throw new CtdbException("Empty answer list passed to doDTCalculation");
        }
        
        Iterator<String> iter = answers.iterator();
        String value = "";
        int year1 = 0;
        int year2 = 0;
        int month = 0;
        int date = 0;
        int hour = 0;
        int minute = 0;
        int second = 0;
        double result;
        GregorianCalendar calendar;
        List<String> values = new ArrayList<String>();
        int day1 = 0;
        int dayDiff = 0;
        
        if (iter.hasNext()) {
            value = iter.next();
            if (value.indexOf("/") != -1) //Date: MM/DD/YYYY
            {
                values = Utils.tokenizeToList(value, "/");
                if (values.size() == 3) {
                    try {
                        month = Integer.parseInt(values.get(0)) - 1;
                        date = Integer.parseInt(values.get(1));
                        dayDiff = date;
                        year1 = Integer.parseInt(values.get(2));
                    } catch (NumberFormatException nfe) {
                        month = 0;
                        date = 0;
                        year1 = 0;
                    }
                }
                calendar = new GregorianCalendar(year1, month, date);
                day1 = calendar.get(Calendar.DAY_OF_YEAR);
            } else  //Date-Time: MM-DD-YYYY-HH-MM-SS
            {
                values = Utils.tokenizeToList(value, "-");
                try {
                    if (values.size() >= 3) {
                        month = Integer.parseInt(values.get(0)) - 1;
                        date = Integer.parseInt(values.get(1));
                        dayDiff = date;
                        year1 = Integer.parseInt(values.get(2));
                    }
                    if (values.size() == 6) {
                        hour = Integer.parseInt(values.get(3));
                        minute = Integer.parseInt(values.get(4));
                        second = Integer.parseInt(values.get(5));
                    }
                } catch (NumberFormatException nfe) {
                    month = 0;
                    date = 0;
                    year1 = 0;
                    hour = 0;
                    minute = 0;
                    second = 0;
                }
                calendar = new GregorianCalendar(year1, month, date, hour, minute, second);
                day1 = calendar.get(Calendar.DAY_OF_YEAR);
            }
        }
        if (answers.size() == 1) {
            result = doConversion(formQuestionAttrs, day1, 0);
            return result;
        }
        int day2 = 0;
        while (iter.hasNext()) {
            value = iter.next();
            values = new ArrayList<String>();
            if (value.indexOf("/") != -1) //Date: MM/DD/YYYY
            {
                values = Utils.tokenizeToList(value, "/");
                if (values.size() >= 3) {
                    try {
                        month = Integer.parseInt(values.get(0)) - 1;
                        date = Integer.parseInt(values.get(1));
                        dayDiff = Math.abs(dayDiff - date);
                        year2 = Integer.parseInt(values.get(2));
                    } catch (NumberFormatException nfe) {
                        month = 0;
                        date = 0;
                        year2 = 0;
                    }
                }
                calendar = new GregorianCalendar(year2, month, date);
                day2 = calendar.get(Calendar.DAY_OF_YEAR);
            }
            //Date-Time: MM-DD-YYYY-HH-MM-SS
            else {
                values = Utils.tokenizeToList(value, "-");
                try {
                    if (values.size() >= 3) {
                        month = Integer.parseInt(values.get(0)) - 1;
                        date = Integer.parseInt(values.get(1));
                        dayDiff = Math.abs(dayDiff - date);
                        year2 = Integer.parseInt(values.get(2));
                    }
                    if (values.size() == 6) {
                        hour = Integer.parseInt(values.get(3));
                        minute = Integer.parseInt(values.get(4));
                        second = Integer.parseInt(values.get(5));
                    }
                } catch (NumberFormatException nfe) {
                    month = 0;
                    date = 0;
                    year2 = 0;
                    hour = 0;
                    minute = 0;
                    second = 0;
                }
                calendar = new GregorianCalendar(year2, month, date, hour, minute, second);
                day2 = calendar.get(Calendar.DAY_OF_YEAR);
            }
            
            int days = 0;
            
            if ((year2 - year1) > 0) {
                if (day1 > day2) {
                    days = (day1 - day2) * (year2 - year1) + 365;
                } else if (day1 < day2) {
                    days = (day2 - day1) * (year2 - year1) + 365;
                } else {
                    days = (year2 - year1) * 365;
                }
            } else if ((year2 - year1) < 0) {
                if (day1 > day2) {
                    days = (day1 - day2) * (year1 - year2) + 365;
                } else if (day1 < day2) {
                    days = (day2 - day1) * (year1 - year2) + 365;
                } else {
                    days = (year1 - year2) * 365;
                }
            } else {
                if (day1 > day2) {
                    days = day1 - day2;
                } else {
                    days = day2 - day1;
                }
            }
            result = doConversion(formQuestionAttrs, days, dayDiff);
            return result;
        }
        return 0;
    }

    private static double doConversion(CalculatedFormQuestionAttributes formQAttrs, double days, int dayDiff) {
        double ret;
        ConversionFactor cf = formQAttrs.getConversionFactor();
        
        if (cf == ConversionFactor.YEARS) {
            ret = days / 365;
        } else if (cf == ConversionFactor.MONTHS) {
            ret = (new Double(days / 30)).intValue();
            if (dayDiff >= 15 && dayDiff < 25) {
                ret = ret + 1;
            }
        } else if (cf == ConversionFactor.DAYS) {
            ret = days;
        } else if (cf == ConversionFactor.HOURS) {
            ret = days / 24;
        } else if (cf == ConversionFactor.MINUTES) {
            ret = days / (24 * 60);
        } else {
            ret = days / (24 * 60 * 60);
        }
        
        return ret;
    }

    /**
     * Does the actual calculation.
     * Requirements:
     * 1. answers List passed is always not empty.
     *
     * @param question The Calculated Question object which contains calculation rule
     * @param answers  The answers which is a List containing Double objects.
     * @return result The calculated result which is a number of type double.
     * @throws CtdbException if any error occurs.
     */
    public static double doCalculation(Question question, List<Double> answers) throws CtdbException {
        if (answers.isEmpty()) {
            throw new CtdbException("Empty answer list passed to doCalculation");
        }
        
        if (answers.size() == 1) {
            return answers.get(0).doubleValue();
        }

        double result = Double.NaN; // will always be overridden
        Iterator<Double> iter = answers.iterator();
        
        if (iter.hasNext()) {
            result = iter.next().doubleValue();
        } else {
            throw new CtdbException("Empty answer list passed to doCalculation");
        }
        
        CalculationType cType = question.getCalculatedFormQuestionAttributes().getCalculationType();
        
        while (iter.hasNext()) {
            if ((cType == CalculationType.SUM) ||
                    (cType == CalculationType.AVERAGE)) {
                result += iter.next().doubleValue();
            } else if (cType == CalculationType.DIFFERENCE) {
                result -= iter.next().doubleValue();
            } else if (cType == CalculationType.MULTIPLICATI0N) {
                result *= iter.next().doubleValue();
            } else if (cType == CalculationType.DIVISION) {
                result /= iter.next().doubleValue();
            } else {
                throw new CtdbException("Invalid question calculation type.");
            }
        }
        if (cType == CalculationType.AVERAGE) {
            result /= answers.size();
        }

        return result;
    }


    /**
     * Validate the required questions all have answers.
     *
     * @param questions     The list of questions
     * @param request       The HTTP request we are processing
     * @param errors        The error message list
     * @param isAllRequired The boolean value indicating if all the questions in the list are required.
     */
    private static boolean validateRequired(List<Question> questions, HttpServletRequest request, Map<String, String> answersCalculated, 
    		List<String> errors, boolean isAllRequired, AdministeredForm aform, DataEntrySetupForm dataEntryForm) throws CtdbException{
    	boolean valid = true;
    	Iterator<Question> iter = questions.iterator();
    	Form form = aform.getForm();
    	HashMap<Integer,Section> sectionMap = form.getSectionMap();
    	ResponseManager rm = new ResponseManager();
    	
    	while (iter.hasNext()) {
    		boolean found = false;
    		Question question = iter.next();
    		int sectionid = question.getSectionId();
			Section section = sectionMap.get(Integer.valueOf(sectionid));
			int questionid = question.getId();
			
			/*skip to check the disabled locked questions*/
			List auditpair = rm.getLastAuditComment(aform.getId(), sectionid, questionid); //loggedInUser
			if(auditpair!=null && auditpair.size()!=0) {	
				String auditStatus = (String) auditpair.get(1);
				if (auditStatus != null && CtdbConstants.AUDITCOMMENT_STATUS_LOCKED.equalsIgnoreCase(auditStatus) ) {
					continue;
				}
			}
			
    		if (!question.getFormQuestionAttributes().isCalculatedQuestion() && !question.getFormQuestionAttributes().isCountQuestion()) {
    			if (isAllRequired || question.getFormQuestionAttributes().isRequired()) {
    				// Modified for different require condition.
    				String values[] = null;

    				if (question.getFormQuestionAttributes().isRequired()) {
    					if (question.getType().getValue()!=QuestionType.File.getValue()) { 
    						values = request.getParameterValues("S_" + question.getSectionId() + "_Q_" + question.getId());// for question
    					} else {
    						String key = "S_" + question.getSectionId() + "_Q_"+String.valueOf(question.getId());
    						if (aform.getFiles().get(key)!=null) {
    							values = new String[1];
    							values[0] = "JUNK";
    						} else {
    							List<String> assocFileQuestionIds = aform.getAssocFileQuestionIds();
    							if(assocFileQuestionIds == null) {
    								values = new String[1];
    								values[0] = "";
    							} else {
    								if(assocFileQuestionIds.contains(key)) {
    									values = new String[1];
    									values[0] = "JUNK";
    								} else {
    									values = new String[1];
    									values[0] = "";
    								}
    							}
    						}
    					}
    				} else {
    					if (question.getType().getValue() != QuestionType.File.getValue()) {
    						//for skip rule
    						values = request.getParameterValues("S_" + question.getSkipSectionId() + "_Q_" + question.getId());
    					} else {
    						String key = "S_" + question.getSkipSectionId() + "_Q_"+String.valueOf(question.getId());
    						if (aform.getFiles() != null && aform.getFiles().get(key) != null) {
    							Attachment file = (Attachment) aform.getFiles().get(key);
    							if (Utils.isBlank(file.getFileName())) {
    								List<String> assocFileQuestionIds = aform.getAssocFileQuestionIds();
    								if (assocFileQuestionIds == null) {
    									values = new String[1];
    									values[0] = "";
    								} else {
    									if(assocFileQuestionIds.contains(key)) {
    										values = new String[1];
    										values[0] = "JUNK";
    									} else {
    										values = new String[1];
    										values[0] = "";
    									}
    								}
    							} else {
    								values = new String[1];
    								values[0] = file.getFileName();
    							}
    						} else {
    							List<String> assocFileQuestionIds = aform.getAssocFileQuestionIds();
    							if (assocFileQuestionIds == null) {
    								values = new String[1];
    								values[0] = "";
    							} else {
    								if (assocFileQuestionIds.contains(key)) {
    									values = new String[1];
    									values[0] = "JUNK";
    								} else {
    									values = new String[1];
    									values[0] = "";
    								}
    							}
    						}
    					}
    				}

    				if (values != null) {
    					for (int i = 0; i < values.length; i++) {
    						String a = values[i].trim();
    						if (a.length() != 0) {
    							found = true;
    							break;
    						}
    					}
    				}
    				if (!found) {
    					boolean isRequiredChildOfDisable = isRequiredChildOfSkipRuleDisable(questions, question, request);
    					if (!question.getFormQuestionAttributes().hasSkipRule() && isRequiredChildOfDisable) {
    						//no errors
    					} else {
    						String msg = "";
    						
    						
    						String id = "S_";
    						if (question.getFormQuestionAttributes().isRequired()) {
    							id+= question.getSectionId();
    						} else {
    							id+= question.getSkipSectionId();
    						}
    						id+=  "_Q_" + question.getId();
    						
    						
    						
    						//throw error if q is visible...will happen in non-repeatable sections, parent repeatable sections, and and opened child repeatable section
    						if(!section.isRepeatable() || (section.isRepeatable() && section.getRepeatedSectionParent() == -1) || (section.isRepeatable() && dataEntryForm.getClickedSectionFields().contains(String.valueOf(question.getSectionId())))) {
    						
    						
	    						if(question.getType() == QuestionType.File) {
	    							
	    							msg="Answer is required for question \"" + question.getText() + 
	        								"\". <a href=\"Javascript:goToFileQuestion('11','" + id + "')\">Go To</a>";
	        						errors.add(msg);
	        						valid = false;
	    							
	    							
	    							
	    							
	    						}else {
	    							
	    							msg="Answer is required for question \"" + question.getText() + 
	        								"\". <a href=\"Javascript:goToValidationErrorFlag();document.getElementById ('S_";
	        						
	        						if (question.getFormQuestionAttributes().isRequired()) {
	        							msg+= question.getSectionId();
	        						} else {
	        							msg+= question.getSkipSectionId();
	        						}
	        						
	        						msg+=  "_Q_" + question.getId() + "').focus();\">Go To</a>";
	        						errors.add(msg);
	        						valid = false;
	
	    						}
    						
    						}
    						
    						
    					}
    				}
    			}
    		} else {
    			if (isAllRequired || question.getFormQuestionAttributes().isRequired()) {
    				found = (answersCalculated.get("S_" + question.getSectionId() + "_Q_" + question.getId()) != null);

    				if (!found) {
    					//throw error if q is visible...will happen in non-repeatable sections, parent repeatable sections, and and opened child repeatable section
    					if(!section.isRepeatable() || (section.isRepeatable() && section.getRepeatedSectionParent() == -1) || (section.isRepeatable() && dataEntryForm.getClickedSectionFields().contains(String.valueOf(question.getSectionId())))) {
    						
    						
    					
	    					errors.add("Answer is required for the calculated question \"" + question.getText() + 
	    							"\".  Please make sure that all questions used for the calculation are answered as numeric " +
	    							"type and division by zero does not occur. Answers in \'Other, Please Specify\' are not " +
	    							"valid answers used in calculated rules.  <a href=\"Javascript:goToValidationErrorFlag();" +
	    							"document.getElementById ('S_" + question.getSectionId() + "_Q_" + question.getId() + "').focus();\">Go To</a>");
	    					valid = false;
	    					
    					}
    				}
    			}
    		}
    	}

    	return valid;
    }

    /**
     * Validate the required questions that are affected by skip rules.
     *
     * @param questions         The list of questions
     * @param request           The HTTP request we are processing
     * @param answersCalculated The map from question id to calculated answer string
     * @param errors            The error message list
     */
    private static boolean validateSkipRequired(List<Question> questions, HttpServletRequest request, 
    		Map<String, String> answersCalculated, List<String> errors, AdministeredForm af, DataEntrySetupForm dataEntryForm) 
    				throws CtdbException {
        // Get a set of question IDs in the form
        Set<String> questionIds = new HashSet<String>();
        Iterator<Question> iter1 = questions.iterator();
        String[] svalues = null;
        
        while (iter1.hasNext()) {
            Question question = iter1.next();
            questionIds.add("S_" +question.getSectionId() + "_Q_" + question.getId());
        }

        boolean valid = true;
        Iterator<Question> iter = questions.iterator();
        while (iter.hasNext()) {
            Question question = iter.next();
            FormQuestionAttributes qAttrs = question.getFormQuestionAttributes();
            String skipRuleEquals = qAttrs.getSkipRuleEquals();
            
           
            if (qAttrs.hasSkipRule() && qAttrs.getSkipRuleType() == SkipRuleType.REQUIRE) {
                // get answer
                boolean hasValue = false;
                String values[] = request.getParameterValues("S_" + question.getSectionId() + "_Q_" + question.getId());
                if (values != null) {
                    for (int i = 0; i < values.length; i++) {
                        String a = values[i].trim();
                        if (a.length() != 0) {
                            hasValue = true;
                            break;
                        }
                    }
                }

                // check skip condition
                boolean conditionSatisfied = false;
                if (qAttrs.getSkipRuleOperatorType().equals(SkipRuleOperatorType.HAS_ANY_VALUE) &&
                        hasValue) {
                    conditionSatisfied = true;
                } else if (qAttrs.getSkipRuleOperatorType().equals(SkipRuleOperatorType.IS_BLANK) &&
                        !hasValue) {
                    conditionSatisfied = true;
                } else if ( qAttrs.getSkipRuleOperatorType().equals(SkipRuleOperatorType.EQUALS) &&
                        hasValue) { 
                	
                	if(skipRuleEquals != null && skipRuleEquals.indexOf("|") > -1) {
                		svalues = skipRuleEquals.split("\\|");
                		for(String value: values) {
                			for(String svalue: svalues) {
                				if(value.trim().toLowerCase().equals(svalue.trim().toLowerCase())) {
                					conditionSatisfied = true;
                					break;
                				}
                			}
                			if(conditionSatisfied)  
                				break;
                		}
                	}else {
                		for(String value: values) {
                			if(value.trim().toLowerCase().equals(skipRuleEquals.trim().toLowerCase())){
                				conditionSatisfied = true;
                				break;
                			}
                		}
                	}
                	
                } else if ( qAttrs.getSkipRuleOperatorType().equals(SkipRuleOperatorType.CONTAINS) &&
                	 hasValue) { 
                	
                	if(skipRuleEquals != null && skipRuleEquals.indexOf("|") > -1) {
                		svalues = skipRuleEquals.split("\\|");
                		for(String value: values) {
                			for(String svalue: svalues) {
                				if(value.trim().toLowerCase().contains(svalue.trim().toLowerCase())) {
                					conditionSatisfied = true;
                					break;
                				}
                			}
                			if(conditionSatisfied)  
                				break;
                		}
                	}else {
                		for(String value: values) {
                			if(value.trim().toLowerCase().contains(skipRuleEquals.trim().toLowerCase())){
                				conditionSatisfied = true;
                				break;
                			}
                		}
                	}
                	
				} else if (qAttrs.getSkipRuleOperatorType().equals(SkipRuleOperatorType.LESS_THAN) && hasValue) {

						for (String value : values) {
						if (!Utils.isBlank(value) && !Utils.isBlank(skipRuleEquals)
								&& Utils.isNumeric(value)) {
							float floatValue = Float.parseFloat(value.trim());
							float floatSvalue = Float.parseFloat(skipRuleEquals.trim());
							if (floatValue < floatSvalue) {
									conditionSatisfied = true;
									break;
								}
                }
						}


				} else if (qAttrs.getSkipRuleOperatorType().equals(SkipRuleOperatorType.LESS_THAN_EQUAL_TO)
						&& hasValue) {

						for (String value : values) {
						if (!Utils.isBlank(value) && !Utils.isBlank(skipRuleEquals)
								&& Utils.isNumeric(value)) {
							float floatValue = Float.parseFloat(value.trim());
							float floatSvalue = Float.parseFloat(skipRuleEquals.trim());
							if (floatValue <= floatSvalue) {
									conditionSatisfied = true;
									break;
								}
							}
						}

				} else if (qAttrs.getSkipRuleOperatorType().equals(SkipRuleOperatorType.GREATER_THAN) && hasValue) {

						for (String value : values) {
						if (!Utils.isBlank(value) && !Utils.isBlank(skipRuleEquals)
								&& Utils.isNumeric(value)) {
							float floatValue = Float.parseFloat(value.trim());
							float floatSvalue = Float.parseFloat(skipRuleEquals.trim());
							if (floatValue > floatSvalue) {
									conditionSatisfied = true;
									break;
								}
							}
						}

				} else if (qAttrs.getSkipRuleOperatorType().equals(SkipRuleOperatorType.GREATER_THAN_EQUAL_TO)
						&& hasValue) {

						for (String value : values) {
						if (!Utils.isBlank(value) && !Utils.isBlank(skipRuleEquals)
								&& Utils.isNumeric(value)) {
							float floatValue = Float.parseFloat(value.trim());
							float floatSvalue = Float.parseFloat(skipRuleEquals.trim());
							if (floatValue >= floatSvalue) {
								conditionSatisfied = true;
								break;
								}
							}
						}
				}

                
                // validate required
                if (conditionSatisfied) {
                    List<Question> questionsToSkip = qAttrs.getQuestionsToSkip();
                    List<Question> questionsToCheck = new ArrayList<Question>();
                    Iterator<Question> iter2 = questionsToSkip.iterator();
                    
                    while (iter2.hasNext()) {
                        Question tmpQuestion = iter2.next();
                        Integer tmpQuestionId = new Integer(tmpQuestion.getId());
                        
                        // Remove those questions that are not in the form questions
                        if (questionIds.contains("S_" + tmpQuestion.getSkipSectionId() + "_Q_" + tmpQuestionId)) {
                            questionsToCheck.add(tmpQuestion);
                        }
                    }
                    
                    if (!validateRequired(questionsToCheck, request, answersCalculated, errors, true,af,dataEntryForm)) {
                        errors.add("(The above questions are required because of the skip rule in question \"" + question.getText() + 
                        		"\".)  <a href=\"Javascript:goToValidationErrorFlag();document.getElementById ('S_" + 
                        		question.getSectionId() + "_Q_" + question.getId() + "').focus();\">Go To</a>");
                        valid = false;
                    }
                }
            }
        }
        return valid;
    }

    /**
     * Validate the answers based on the question validation data
     *
     * @param q      The question
     * @param a      The answer string
     * @param errors The error message list
     */
    private static boolean validateAnswer(Question q, String a, List<String> errors,boolean overMax4000) {
        return validateAnswer(q, a, errors, false,overMax4000);
    }

    private static boolean validateAnswer(Question q, String a, List<String> errors, boolean isEditFinalAnswer, boolean overMax4000) {
        if (q.getType() == QuestionType.IMAGE_MAP) {
            return true;
        }
        
        boolean valid = true;
        FormQuestionAttributes qAttrs = q.getFormQuestionAttributes();

        if (q.getType().getValue() == QuestionType.TEXTBOX.getValue() || q.getType().getValue() == QuestionType.TEXTAREA.getValue()) {
            if (qAttrs.getAnswerType().getValue() == AnswerType.NUMERIC.getValue()) {
                if (!GenericValidator.matchRegexp(a, qAttrs.getAnswerType().getRegexp())) {
                    valid = false;
                    String e = "Numeric answer type required for question \"" + q.getText() + 
                    		"\".  <a href=\"Javascript:goToValidationErrorFlag(); document.getElementById('S_" + 
                    		q.getSectionId() + "_Q_" + q.getId() + "').focus();\">Go To</a>";
                    errors.add(e);
                } else {
                	// it is numeric...so now check for decimal precision for non-calculated questions
                	 if ((q.getType() == QuestionType.TEXTBOX) && (!qAttrs.isCalculatedQuestion())  && (!qAttrs.isCountQuestion())) {
                     	int decimalPrecision = qAttrs.getDecimalPrecision();
                     	if (decimalPrecision != -1) {
                     		if (decimalPrecision == 0) {
                     			if (a.contains(".")) {
                     				String e = "Numeric answer must not contain any decimals for question \"" + q.getText() + 
                     						"\".  <a href=\"Javascript:goToValidationErrorFlag(); document.getElementById('S_" + 
                     						q.getSectionId() + "_Q_" + q.getId() + "').focus();\">Go To</a>";
                     				errors.add(e);
                     			}
                     		} else {
                     			if (a.contains(".")) {
                     				int decimalIndex = a.indexOf(".") + 1;
                     				int aLength = a.length();
                     				
                     				if (aLength - decimalIndex > decimalPrecision) {
                     					String e = "Numeric answer must have a decimal precision of " + decimalPrecision + " for question \"" + 
                     							q.getText() + "\".  <a href=\"Javascript:goToValidationErrorFlag(); document.getElementById('S_" + 
                     							q.getSectionId() + "_Q_" + q.getId() + "').focus();\">Go To</a>";
 										errors.add(e);
                     				}
                     			}
                     		}
                     	}
                     }
                	 
                	// range validation migration from rangevalidation.js->confirmLockValidateRange JS to server side
                	if (Integer.valueOf(qAttrs.getRangeOperator()) == CtdbConstants.FORM_ISEQUAL_RANGEOPERATOR) {
                		if (Double.valueOf(a).doubleValue()!= Double.valueOf(qAttrs.getRangeValue1()).doubleValue()) {
                			String e = "Question " + q.getText() + " is not equal to " +qAttrs.getRangeValue1() + 
                					"\".  <a href=\"Javascript:goToValidationErrorFlag(); document.getElementById('S_" + 
                					q.getSectionId() + "_Q_" + q.getId() + "').focus();\">Go To</a>";
	                		 errors.add(e);
                		}
                	}
                	
                	if (Integer.valueOf(qAttrs.getRangeOperator()) ==CtdbConstants.FORM_ISLESSTHAN_RANGEOPERATOR) {
                		if(Double.valueOf(a).doubleValue() > Double.valueOf(qAttrs.getRangeValue1()).doubleValue()){
                			String e = "Question " + q.getText() + " is not less than or equal to " + qAttrs.getRangeValue1() + 
                					"\".  <a href=\"Javascript:goToValidationErrorFlag();document.getElementById ('S_" + 
                					q.getSectionId() + "_Q_" + q.getId() + "').focus();\">Go To</a>";
	                		 errors.add(e);
                		}
                		
                	}
                	
                	if (Integer.valueOf(qAttrs.getRangeOperator()) ==CtdbConstants.FORM_ISGREATERTHAN_RANGEOPERATOR) {
                		if (Double.valueOf(a).doubleValue() < Double.valueOf(qAttrs.getRangeValue1()).doubleValue()) {
                			String e = "Question " + q.getText() + " is not greater than or equal to " +qAttrs.getRangeValue1() + 
                					"\".  <a href=\"Javascript:goToValidationErrorFlag(); document.getElementById('S_" + 
                					q.getSectionId() + "_Q_" + q.getId() + "').focus();\">Go To</a>";
	                		 errors.add(e);
                		}
                	}
                	
                	if (Integer.valueOf(qAttrs.getRangeOperator()) == CtdbConstants.FORM_INBETWEEN_RANGEOPERATOR) {
	                	 if (Double.valueOf(a).doubleValue() > Double.valueOf(qAttrs.getRangeValue2()).doubleValue() || 
	                			 Double.valueOf(a).doubleValue() < Double.valueOf(qAttrs.getRangeValue1()).doubleValue()){
	                		 String e = "Question " + q.getText() + " is not between " + qAttrs.getRangeValue1() + " and " + qAttrs.getRangeValue2() + 
	                				 "\".  <a href=\"Javascript:goToValidationErrorFlag(); document.getElementById('S_" + q.getSectionId() + 
	                				 "_Q_" + q.getId() + "').focus();\">Go To</a>";
	                		 errors.add(e);
	                	 }
                	}
                	
                	// End of range migration from JS to server side
                	 
                }
            } else if (qAttrs.getAnswerType().getValue() == AnswerType.DATE.getValue()) {
            	// MM/DD/YYYY
                if (!GenericValidator.isDate(a, AnswerType.DATE.getRegexp(), true)) {
                    valid = false;
                    String e = "Date answer type (" + qAttrs.getAnswerType().getRegexp() + ") required for question \"" + q.getText() + 
                    		"\".  <a href=\"Javascript:goToValidationErrorFlag(); document.getElementById('S_" + q.getSectionId() + 
                    		"_Q_" + q.getId() + "').focus();\">Go To</a>";

                	errors.add(e);
                }
            } else if (qAttrs.getAnswerType().getValue() == AnswerType.DATETIME.getValue()) {
            	// MM-dd-yyyy-HH-mm-ss
                if (!GenericValidator.isDate(a, AnswerType.DATETIME.getRegexp(), true)) {
                    String aTemp = a.replaceAll("-", "/");
                    if (!GenericValidator.isDate(aTemp, SysPropUtil.getProperty("default.system.dateformat"), true)) {
                        valid = false;
                        String e = "Date-Time answer type (" + qAttrs.getAnswerType().getRegexp() + ") required for question \"" + q.getText() + 
                        		"\".  <a href=\"Javascript:goToValidationErrorFlag(); document.getElementById('S_" + q.getSectionId() + 
                        		"_Q_" + q.getId() + "').focus();\">Go To</a>";
                        
                        errors.add(e);
                    } else {
                        a += "-00-00-00";
                        if (!GenericValidator.isDate(a, AnswerType.DATETIME.getRegexp(), true)) {
                            valid = false;
                            String e = "Date-Time answer type (" + qAttrs.getAnswerType().getRegexp() + ") required for question \"" + 
                            		q.getText() + "\".  <a href=\"Javascript:goToValidationErrorFlag(); document.getElementById('S_" + 
                            		q.getSectionId() + "_Q_" + q.getId() + "').focus();\">Go To</a>";

                        	errors.add(e);
                        }
                    }
                }
            }
        }

        if ((q.getType() == QuestionType.TEXTBOX || q.getType() == QuestionType.TEXTAREA) &&
                (qAttrs.getAnswerType() == AnswerType.STRING )) {
            // check max min length
            int minCharacters = qAttrs.getMinCharacters();
            if (minCharacters != Integer.MIN_VALUE) {
                if (!GenericValidator.minLength(a, minCharacters)) {
                    valid = false;
                    errors.add("Minimum " + minCharacters + " characters required for question \"" + q.getText() + 
                    		"\".  <a href=\"Javascript:goToValidationErrorFlag(); document.getElementById('S_" + 
                    		q.getSectionId() + "_Q_" + q.getId() + "').focus();\">Go To</a>");
                }
            }
            int maxCharacters = qAttrs.getMaxCharacters();
            if (maxCharacters != Integer.MIN_VALUE) {
                if (!GenericValidator.maxLength(a, maxCharacters)) {
                    valid = false;
                    if(!overMax4000) {
                    	 errors.add("Maximum " + maxCharacters + " characters required for question \"" + q.getText() + 
                         		"\".  <a href=\"Javascript:goToValidationErrorFlag(); document.getElementById('S_" + 
                         		q.getSectionId() + "_Q_" + q.getId() + "').focus();\">Go To</a>");
                    }
                   
                }
            }
        }
        
        return valid;
    }


    /**
     * Retrieve user's answers for discrepancy resolution.  This method will check whether two answers
     * entered by double key entry are the same,
     *
     * @param aform   The AdministeredForm object that carries discrepant answers after double key
     *                data entry.
     * @param request The HTTP request object that contains final answers for discrepancy resolution.
     * @return The error list gerenated during answer validation.
     * @throws CtdbException if any error occurs.
     */
    public static List<String> getFinalAnswers(AdministeredForm aform, HttpServletRequest request, List<String> qidList) throws CtdbException {
        List<Response> responses = aform.getResponses();
        List<String> errors = new ArrayList<String>();
        Iterator<Response> it = responses.iterator();
        
        while (it.hasNext()) {
            List<String> list = new ArrayList<String>();
            List<String> submitAnswers = new ArrayList<String>();
            Response resp = it.next();
            String qid = Integer.toString(resp.getQuestion().getId());
            String blankId = "bk_" + qid;
            String[] blanks = request.getParameterValues(blankId);
            String qType = resp.getQuestion().getType().toString();

            /*if (resp.getQuestion().getType() == QuestionType.PATIENT_CALENDAR) {
                try {
                    if (request.getParameter(Integer.toString(resp.getQuestion().getId())) != null &&
                            request.getParameter(Integer.toString(resp.getQuestion().getId())).length() > 0) {

                        ((CalendarResponse) resp).setRow(Integer.parseInt(request.getParameter("row_" + resp.getQuestion().getId())));
                        ((CalendarResponse) resp).setCol(Integer.parseInt(request.getParameter("col_" + resp.getQuestion().getId())));

                        try {
                            ((CalendarResponse) resp).getResponse1().setId(Integer.parseInt(request.getParameter("responseid1_" + resp.getQuestion().getId())));
                        } catch (Exception e) {
                            System.out.println("there was no responseid1");
                        }

                        try {
                            ((CalendarResponse) resp).getResponse2().setId(Integer.parseInt(request.getParameter("responseid2_" + resp.getQuestion().getId())));
                        } catch (Exception e) {
                            System.out.println("there was no responseid2");
                        }

                        String finals = request.getParameter(qid);
                        if (finals != null) {
                            finals = finals.trim();
                            if (finals.length() > 0) {
                                list.add(finals);
                                submitAnswers.add(finals);
                            }
                        }

                    }
                } catch (NumberFormatException nfe) {
                    throw new CtdbException("Number format exception on parsing patient calendar data");
                }

            } else if (qType.equals(QuestionType.IMAGE_MAP.toString())) {
                String[] finals = request.getParameterValues(qid);
                if (finals != null && finals.length > 0) {
                    for (int i = 0; i < finals.length; i++) {
                        list.add(finals[i]);
                    	submitAnswers.add(finals[i]);
                    }
                }
            } else*/ 
            if (qType.equalsIgnoreCase("multi-select") || qType.equalsIgnoreCase("checkbox")
                    || qType.equalsIgnoreCase(QuestionType.VISUAL_SCALE.toString())) {
                String[] finals = request.getParameterValues(qid);
                
                if (finals != null && finals.length > 0) {
                    for (int i = 0; i < finals.length; i++) {
                        list.add(finals[i]);

                    	String sa = finals[i];
                    	sa = determineSubmitAnswerForSelectTypeQuestions(resp.getQuestion(),sa);
                    	submitAnswers.add(sa);
                    }
                }
            } else if (qType.equalsIgnoreCase("Textbox") || qType.equalsIgnoreCase("Textarea") ||qType.equalsIgnoreCase("File")) {
                String finals = request.getParameter(qid);
                if (finals != null) {
                    finals = finals.trim();
                    if (finals.length() > 0){
                        list.add(finals);
                        String sa = finals;
                        
                        if (resp.getQuestion().getType() == QuestionType.TEXTBOX && 
                        		resp.getQuestion().getFormQuestionAttributes().getAnswerType().equals(AnswerType.NUMERIC) && 
                        		resp.getQuestion().getFormQuestionAttributes().isHasUnitConversionFactor()) {
                         	sa = calculateUnitConversionFactor(resp.getQuestion(), sa, errors);
                         	
                         	if (resp.getQuestion().getFormQuestionAttributes().getDecimalPrecision() != -1) {
     	                     	double d = Double.valueOf(sa);
     	                 		int precision = resp.getQuestion().getFormQuestionAttributes().getDecimalPrecision();
     	                 		String formatString = "#0";
     	                 		DecimalFormat df = null;
     	
     	             			if(precision != 0) {
     	             				formatString = formatString + ".";
     	             				for(int k=1;k<=precision;k++) {
     	             					formatString = formatString + "0";
     	             				}
     	             			}
     	             			
     	             			df = new DecimalFormat(formatString);
     	             			sa = df.format(d);
     	                     }
                        }
                        
                    	submitAnswers.add(finals);
                    }
                }
            } else if (qType.equalsIgnoreCase("Radio")) {
                String finals = request.getParameter(qid);
                if (finals != null){
                    list.add(finals);
                    String sa = finals;
                	sa = determineSubmitAnswerForSelectTypeQuestions(resp.getQuestion(),sa);
                	submitAnswers.add(finals);
                }
            } else if (qType.equalsIgnoreCase("select")) {
                String finals = request.getParameter(qid);
                
                if (finals != null && finals.length() > 0) {
                	list.add(finals);
                    String sa = finals;
                	sa = determineSubmitAnswerForSelectTypeQuestions(resp.getQuestion(),sa);
                	submitAnswers.add(finals);
                }
            }

            // when both answer and checkbox are given, we just use checkbox
            // to clear the answer.
            if (list.size() > 0 && blanks != null) {
                String empty = "";
                list.clear();
                list.add(empty);
                submitAnswers.add(empty);
            }
            // have a final answer, and the checkbox is not checked...normal situation.
            else if (list.size() > 0 && blanks == null) {
                for (int j = 0; j < list.size(); j++) {
                    String answer = (String) list.get(j);
                    boolean overMax4000 = false;
                    if (answer.length() > 4000) {
                    	errors.add("Maximum 4000 characters required for question \"" + resp.getQuestion().getText() + 
                        		"\".  <a href=\"Javascript:goToValidationErrorFlag(); document.getElementById('S_" + 
                        		resp.getQuestion().getSectionId() + "_Q_" + resp.getQuestion().getId() + "').focus();\">Go To</a>");
                    	overMax4000 = true;
                    }
                    InputHandler.validateAnswer(resp.getQuestion(), answer, errors,overMax4000);

                }
            }
            // If no final answer is given, but will be set to blank, then
            // add an empty string as the answer.
            else if (list.size() == 0 && blanks != null) {
                String empty = "";
                list.add(empty);
                submitAnswers.add(empty);
            }
            
            
            
            
            if(resp.getQuestion().getType() == QuestionType.SELECT || resp.getQuestion().getType() == QuestionType.MULTI_SELECT || 
            		resp.getQuestion().getType() == QuestionType.RADIO || resp.getQuestion().getType() == QuestionType.CHECKBOX) {
	
				//we want the answers to be stored in the bd as PVD always! (the submit answers will be PV)
				List<Answer> questionOptions = resp.getQuestion().getAnswers();
				if(resp.getQuestion().isDisplayPV()) {
					Iterator ansIter = list.iterator();
					int index = 0;
					while(ansIter.hasNext()) {
						String answer = (String)ansIter.next();
						//need to find matching pvd for this 
						Iterator qOptionsIter = questionOptions.iterator();
						while(qOptionsIter.hasNext()) {
							Answer a = (Answer)qOptionsIter.next();
							String pvd = a.getPvd();
							String submittedValue = a.getSubmittedValue();
							if(answer.equals(submittedValue)) {
								answer = pvd;
								list.set(index, answer);
								break;	
							}	
							
						}
						index++;
					}	
				}
				resp.setAnswers(list);
			}else {
				resp.setAnswers(list);
			}
            
            String comment=request.getParameter("comment_" + resp.getQuestion().getId())==null?"":request.getParameter("comment_" + resp.getQuestion().getId());
            resp.setComment(comment);
            resp.setSubmitAnswers(submitAnswers);
        }
        
        // we have done all of our validation, now do BRICS FS validation webservice calls
		//validateAgainstFS(aform, request);
        
        return errors;
    }

    /**
     * Retrieve admin's edited answers.
     *
     * @param aform   The AdministeredForm object.
     * @param request The HTTP request object that contains final edited answers.
     * @return The error list gerenated during answer validation.
     * @throws CtdbException if any error occurs.
     */
    public static List<String> getEditedAnswers(AdministeredForm aform, HttpServletRequest request,DataEntrySetupForm dataEntryForm) throws CtdbException, ParseException  {
    	String responseVisitDate = null;
    	
    	//get list of prepop vals from db
    	List<PatientVisitPrepopValue> pvPrepopValueList = new ArrayList<PatientVisitPrepopValue>();
		PatientManager patientMan = new PatientManager();
		ProtocolManager protocolMan = new ProtocolManager();
		ResponseManager rm = new ResponseManager();
		boolean isVisitDate_DateTime = false;
		
		try {
			PatientVisit pv = null;
			int intervalId = aform.getInterval().getId();
			pv = patientMan.getPatientVisit(aform.getPatient().getId(), aform.getVisitDate(), intervalId);
			int visitdateId = pv.getId();
			pvPrepopValueList = protocolMan.getPvPrepopValuesForInterval(intervalId, visitdateId);
		} catch(ObjectNotFoundException e) {
			logger.info("No Scheduled visit for Subject.");
		}
    	
    	
    	
    	boolean deletingAnswers = false;
        List<String> errors = new ArrayList<String>();
        User u = (User) request.getSession().getAttribute(CtdbConstants.USER_SESSION_KEY);
        AttachmentManager am = new AttachmentManager();
        Form form = aform.getForm();
        List<String> edits = new ArrayList<String>();
        List<String> submitAnswers = new ArrayList<String>();
        Response resp = new Response();
        Question q = new Question();
        String reason = "";
        
        form = aform.getForm();
        List<Question> allQuestionsInForm = new ArrayList<Question>();

        // get all allQuestionsInForm in the form
        for ( List<Section> row : form.getRowList() ) {
            for ( Section section : row ) {
                if (section != null) {
                    allQuestionsInForm.addAll(section.getQuestionList());
                }
            }
        }
        
		Map<String, Integer> questionResponse = new HashMap<String, Integer>();
		for (Response response : aform.getResponses()) {
			if (!(response instanceof PatientCalendarCellResponse)) {
				questionResponse.put(
						"S_" + response.getQuestion().getSectionId() + "_Q_" + response.getQuestion().getId(),
						new Integer(response.getId()));
			}
		}

        Map<String, String> answersCalculated = new HashMap<String, String>();
        calculate(aform, allQuestionsInForm, request, errors, answersCalculated);
        validateRequired(allQuestionsInForm, request, answersCalculated, errors, false, aform,dataEntryForm);
        validateSkipRequired(allQuestionsInForm, request, answersCalculated, errors, aform,dataEntryForm);
        		
        for (Iterator<Response> it = aform.getResponses().iterator(); it.hasNext();) {
            edits = new ArrayList<String>();
            submitAnswers = new ArrayList<String>();
            resp = it.next();
            String qid = Integer.toString(resp.getQuestion().getId());
            q = resp.getQuestion();
            String reasonId = "reason_" + qid;
            reason = request.getParameter(reasonId);
            
			/*skip to check the disabled locked questions*/
            int sidInt = q.getSectionId(); 
            int qidInt = q.getId();
			List auditpair = rm.getLastAuditComment(aform.getId(), sidInt, qidInt); //loggedInUser
			if(auditpair!=null && auditpair.size()!=0) {	
				String auditStatus = (String) auditpair.get(1);
				if (auditStatus != null && CtdbConstants.AUDITCOMMENT_STATUS_LOCKED.equalsIgnoreCase(auditStatus) ) {
					continue;
				}
			}
            
            if (q.getType().equals(QuestionType.File)) {
       			 String key = "S_" + q.getSectionId() + "_Q_"+String.valueOf(q.getId());
       			 
       			 if (aform.getFiles() != null && aform.getFiles().get(key) != null) {
       				 Attachment at = (Attachment) aform.getFiles().get(key);
       				 
       				 // the file exist
       				 if (at != null && !Utils.isBlank(at.getFileName())) {
       					
       					 //store the file into the data base
       					 at.updateCreatedByInfo(u);
       					 at.updateUpdatedByInfo(u);
       					 at.setAssociatedId(form.getId());
       					 at.setType(new CtdbLookup(AttachmentManager.FILE_COLLECTION));
       					 
       					 try {
       						 // save real file to computer and database
							am.createAttachment(at, form.getProtocol().getId());
       					 } catch (ServerFileSystemException e) {
       						 e.printStackTrace();
       					 } 
       					 
       					 // the answer of File type question is "fileName:attachementId"
       					 String fileTypeAnswer = at.getFileName() + ":" + String.valueOf(at.getId());
       					 edits.add(fileTypeAnswer);
       					 submitAnswers.add(fileTypeAnswer);
       				 }
       			 }
            } else if(q.getFormQuestionAttributes().isCalculatedQuestion() || q.getFormQuestionAttributes().isCountQuestion()) {
            	// calculated question, file type question can't be calculated question
                String a = (String) answersCalculated.get("S_" + q.getSectionId() + "_Q_" + q.getId());
                
                // do decimal precision!!!!!!
                if (a != null) {
                	if (q.getFormQuestionAttributes().getDecimalPrecision() != -1) {
                		double d = Double.valueOf(a);
                		int precision = q.getFormQuestionAttributes().getDecimalPrecision();
                		StringBuffer formatBuffer = new StringBuffer();
                		DecimalFormat df = null;
                		
                		formatBuffer.append("#0");

                		if (precision != 0) {
                			formatBuffer.append(".");
                			
                			for(int i = 1; i <= precision; i++) {
                				formatBuffer.append("0");
                			}
                		}

                		df = new DecimalFormat(formatBuffer.toString());
                		a = df.format(d);
                	}

                	edits.add(a);

                	// determine submit answer...could be  for textbox if it is type numeric and has set up a unit conversion factor
                	// then do decimal precision again if necessary  
                	String sa = a;
                	if (q.getType() == QuestionType.TEXTBOX && 
                			q.getFormQuestionAttributes().getAnswerType().equals(AnswerType.NUMERIC) && 
                			q.getFormQuestionAttributes().isHasUnitConversionFactor()) {

                		sa = calculateUnitConversionFactor(q,sa,errors);

                		if (q.getFormQuestionAttributes().getDecimalPrecision() != -1) {
                			double d = Double.valueOf(sa);
                			int precision = q.getFormQuestionAttributes().getDecimalPrecision();
                			StringBuffer formatBuffer = new StringBuffer();
                			DecimalFormat df = null;
                			
                			formatBuffer.append("#0");

                			if (precision != 0) {
                				formatBuffer.append(".");
                				
                				for (int i = 1; i <= precision; i++) {
                					formatBuffer.append("0");
                				}
                			}
                			
                			df = new DecimalFormat(formatBuffer.toString());
                			sa = df.format(d);
                		}
                	} 

                	submitAnswers.add(sa);
                }
            } else {
            	String[] answers = request.getParameterValues("S_" + Integer.toString(resp.getQuestion().getSectionId()) + "_Q_"+Integer.toString(resp.getQuestion().getId()));
                for (int i = 0; answers != null &&  i < answers.length; i++) {
                    if (answers[i] != null && ! answers[i].trim().equals("")) {
                    	if (q.isIncludeOtherOption() && answers[i].equalsIgnoreCase(CtdbConstants.OTHER_OPTION_DISPLAY)) {
                    		String otherValue[] = request.getParameterValues("S_" + Integer.toString(resp.getQuestion().getSectionId())  + "_Q_" + Integer.toString(resp.getQuestion().getId())+"_otherBox");
                    		if (otherValue[0].isEmpty()) {
                    			errors.add("Answer for \"Other, please specify\" for question \"" + resp.getQuestion().getText() + "\" is required.  <a href=\"Javascript:goToValidationErrorFlag();document.getElementById ('S_" + resp.getQuestion().getSectionId() + "_Q_" + resp.getQuestion().getId() + "_otherBox').focus();\">Go To</a>");
                    			otherValue[0] = CtdbConstants.OTHER_OPTION_DISPLAY;
                    		}
                    		
                    		edits.add(otherValue[0].trim());
                    		submitAnswers.add(otherValue[0].trim());
                    	} else {
                    		edits.add(answers[i].trim());

                    		String sa = answers[i].trim();
                    		if (q.getType() == QuestionType.SELECT || q.getType() == QuestionType.MULTI_SELECT ||
                    				q.getType() == QuestionType.RADIO || q.getType() == QuestionType.CHECKBOX ) {
                    			sa = determineSubmitAnswerForSelectTypeQuestions(q,sa);
                    		} else if (q.getType() == QuestionType.TEXTBOX && 
                    				q.getFormQuestionAttributes().getAnswerType().equals(AnswerType.NUMERIC) && 
                    				q.getFormQuestionAttributes().isHasUnitConversionFactor()) {
                    			sa = calculateUnitConversionFactor(q,sa,errors);

                    			if (q.getFormQuestionAttributes().getDecimalPrecision() != -1) {
                    				double d = Double.valueOf(sa);
                    				int precision = q.getFormQuestionAttributes().getDecimalPrecision();
                    				StringBuffer formatBuffer = new StringBuffer();
                    				DecimalFormat df = null;
                    				
                    				formatBuffer.append("#0");

                    				if (precision != 0) {
                    					formatBuffer.append(".");
                    					
                    					for (int k = 1; k <= precision; k++) {
                    						formatBuffer.append("0");
                    					}
                    				}
                    				
                    				df = new DecimalFormat(formatBuffer.toString());
                    				sa = df.format(d);
                    			}
                    		}

                    		submitAnswers.add(sa);
                    	} 
                    }
                }
            }

            
            for (int j = 0; j < edits.size(); j++) {
                String answer = (String) edits.get(j);
                
                boolean overMax4000 = false;
                if (answer.length() > 4000) {
                	errors.add("Maximum 4000 characters required for question \"" + q.getText() + 
                    		"\".  <a href=\"Javascript:goToValidationErrorFlag(); document.getElementById('S_" + 
                    		q.getSectionId() + "_Q_" + q.getId() + "').focus();\">Go To</a>");
                	overMax4000 = true;
                }
                

                InputHandler.validateAnswer(q, answer, errors, true, overMax4000);
           }

         // Has answer changed ?
            if(resp.getQuestion().isDisplayPV()) {
            	if (edits.containsAll(resp.getSubmitAnswers()) && resp.getSubmitAnswers().containsAll(edits)) {
                    // answers are the same, do nothing?
                    continue;
                }
            }else {
            	if (edits.containsAll(resp.getAnswers()) && resp.getAnswers().containsAll(edits)) {
                    // answers are the same, do nothing?
                    continue;
                }
            }
            
			// deleting answer?
			if (edits.isEmpty() && !resp.getAnswers().isEmpty()) {
				// For file type question, there is no way to delete the file,
				// skip check the File type question
				if (q.getType().getValue() != QuestionType.File.getValue()) {
					edits.add("^^DELETED^^");
					deletingAnswers = true;
				}
			}
            
			if(q.getType() == QuestionType.SELECT || q.getType() == QuestionType.MULTI_SELECT || 
             		q.getType() == QuestionType.RADIO || q.getType() == QuestionType.CHECKBOX) {
	
				//we want the answers to be stored in the bd as PVD always! (the submit answers will be PV)
				List<Answer> questionOptions = q.getAnswers();
				if(q.isDisplayPV()) {
					Iterator ansIter = edits.iterator();
					int index = 0;
					while(ansIter.hasNext()) {
						String answer = (String)ansIter.next();
						//need to find matching pvd for this 
						Iterator qOptionsIter = questionOptions.iterator();
						while(qOptionsIter.hasNext()) {
							Answer a = (Answer)qOptionsIter.next();
							String pvd = a.getPvd();
							String submittedValue = a.getSubmittedValue();
							if(answer.equals(submittedValue)) {
								answer = pvd;
								edits.set(index, answer);
								break;	
							}	
							
						}
						index++;
					}	
				}
				resp.setEditAnswers(edits);
			}else {
				resp.setEditAnswers(edits);
			}
            
            if (q.getFormQuestionAttributes().isCalculatedQuestion()) {
            	reason = "Automatically calculated value.";
            }
            
            if (q.getFormQuestionAttributes().isCountQuestion()) {
            	reason = "Automatically counted value.";
            }
            
             //check on visitdate...whatever user answers for visist date de, we need to update aform visitdate with it
          	 if(responseVisitDate == null) {
   	        	  String dataElementName = q.getFormQuestionAttributes().getDataElementName();
   	        	  if(dataElementName.equals(CtdbConstants.VISIT_DATE_DATA_ELEMENT)) {
   	        		if(q.getFormQuestionAttributes().getAnswerType() == AnswerType.DATETIME) {
	        			  isVisitDate_DateTime = true; 
	        		  }
   	        		  responseVisitDate = edits.get(0);
   	        	  }
          	  }
          	 
          	 
          	//check and make sure that guid entered as answer is same as aform guid...also check that 
        	 //visit type entered as answer is same as aform visit type
        	 String dataElementName = q.getFormQuestionAttributes().getDataElementName();
        	 if(dataElementName.equals(CtdbConstants.GUID_DATA_ELEMENT)) {
        		String aformGUID =  aform.getPatient().getGuid();
        		if(!edits.get(0).equalsIgnoreCase(aformGUID)) {
        			errors.add(CtdbConstants.GUID_ERR_TEXT +  "\"" + q.getText() + "\".  <a href=\"Javascript:goToValidationErrorFlag(); document.getElementById('S_" + q.getSectionId() + "_Q_" + q.getId() + "').focus();\">Go To</a>");
        		}
        		 
        	 }
        	 if(dataElementName.equals(CtdbConstants.VISIT_TYPE_DATA_ELEMENT) || dataElementName.equals(CtdbConstants.VISIT_TYP_PDBP_DATA_ELEMENT)) {
        		 String aformVisitType = aform.getInterval().getName();
        		 if(!edits.get(0).equalsIgnoreCase(aformVisitType)) {
        			 errors.add(CtdbConstants.VTYPE_ERR_TEXT +  "\"" + q.getText() + "\".  <a href=\"Javascript:goToValidationErrorFlag(); document.getElementById('S_" + q.getSectionId() + "_Q_" + q.getId() + "').focus();\">Go To</a>");
        		 }
        		 
        	 }
            
            resp.setEditReason(reason);
            resp.setSubmitAnswers(submitAnswers);
            resp.setAdministeredForm(aform);
            
            if (reason != null && reason.length() > 255)
                errors.add("Reason can't be longer that 255 characters: question \"" + q.getText() + "\".  <a href=\"Javascript:;\" onclick=\"goToValidationErrorFlag();document.getElementById ('S_" + q.getSectionId() + "_Q_" + q.getId() + "').focus();$('#S_" + q.getSectionId() + "_Q_" + q.getId() + "').trigger('change');\">Go To</a>");

			if (!edits.isEmpty() && (reason == null || reason.length() == 0)) {
				if (!q.getFormQuestionAttributes().isCalculatedQuestion() && !q.getFormQuestionAttributes().isCountQuestion()) {
					errors.add("Reason is required for editing the answer: question \"" + q.getText()
							+ "\".  <a href=\"Javascript:;\" onclick=\"goToValidationErrorFlag(); document.getElementById('S_"
							+ q.getSectionId() + "_Q_" + q.getId() + "').focus(); $('#S_" + q.getSectionId() + "_Q_"
							+ q.getId() + "').trigger('change');\">Go To</a>");
				}
			}
        }

        if (errors.size() > 0 && deletingAnswers) {
            removeDeleted(aform.getResponses());
        }
        
        logger.info(DataCollectionUtils.getUserIdSessionIdString(request)+"#InputHandler->getEditedAnswers2->errors size#:"+errors.size());
        
        boolean noAnswers = true;
        
        for ( Response res : aform.getResponses() ) {
        	Question qt = res.getQuestion();
        	String deName = qt.getFormQuestionAttributes().getDataElementName();
        	boolean isCalculated = qt.getFormQuestionAttributes().isCalculatedQuestion();
        	boolean isCount = qt.getFormQuestionAttributes().isCountQuestion();
        	/*if ( !qt.getFormQuestionAttributes().isPrepopulation() ) {
	        	List<String> a = res.getAnswers();
	        	
	        	if( a!= null && a.size() > 0 ) {
	        		noAnswers = false;
	        		break;
	        	}
        	}	*/
        	
        	if(!isPrepop(deName,pvPrepopValueList) && !isCalculated && !isCount) {
        		List<String> a = res.getAnswers();
	        	
	        	if( a!= null && a.size() > 0 ) {
	        		noAnswers = false;
	        		break;
	        	}
        	}
        }

		aform.setAreThereAnswers(!noAnswers);
		
		// Email Trigger
		for (Response res : aform.getResponses()) {
			Question qt = res.getQuestion();

			// Email Trigger
			EmailTrigger emailTrigger = res.getQuestion().getFormQuestionAttributes().getEmailTrigger();
			if (!emailTrigger.getToEmailAddress().equals("") && res.getAdministeredForm() != null) {
				Boolean isTrigged = checkEmailIsTriggerred(emailTrigger, res, aform.getResponses(), qt.getType(),
						qt.getFormQuestionAttributes().getAnswerType());
				if (isTrigged) {
					String condStrWithVal = getConditionStrWithText(emailTrigger, res, aform.getResponses(),
							qt.getType(), qt.getFormQuestionAttributes().getAnswerType(), true);
					// this question has an email trigger and is answered
					EmailTriggerThread ett = new EmailTriggerThread(emailTrigger, res, qt.getType(),
							qt.getFormQuestionAttributes().getAnswerType(), condStrWithVal);
					ett.start();
				}
			}
		}
		// we have done all of our validation, now do BRICS FS validation webservice calls
		//validateAgainstFS(aform, request);
		
		if(errors.size() == 0 && responseVisitDate != null) {
			aform.setVisitDate(DataCollectionUtils.convertStringToDate(responseVisitDate,isVisitDate_DateTime));
		}
        return errors;
    }

    /**
     * if answers are deleted, ^^DELETED^^ added as placehoder, remove it if errors occour
     * so users dont see it.
     * @param responses
     */
    private static void removeDeleted(List<Response> responses) {
        for (Iterator<Response> i = responses.iterator(); i.hasNext(); ) {
            Response r = i.next();
            List<String> answers = new ArrayList<String>();
            
            for (Iterator<String> ii = r.getEditAnswers().iterator(); ii.hasNext();) {
                String ans = (String) ii.next();
                
                if (ans != "^^DELETED^^"){
                    answers.add(ans);
                }
            }
            
            r.setEditAnswers(answers);
        }
    }

    /* Audit Comment: getEditedAnswers */
    /** get from UI, set to response.java to db
     * auditComment = request.getParameter(commentId); from UI
     * Response resp.setEditAuditComment(auditComment);
     */
    public static List<String> getAuditComments(AdministeredForm aform, HttpServletRequest request,DataEntrySetupForm dataEntryForm) throws CtdbException {

        List<String> errors = new ArrayList<String>();
        User u = (User) request.getSession().getAttribute(CtdbConstants.USER_SESSION_KEY);

        Form form = aform.getForm();
        Response resp = new Response();
        Question q = new Question();
        String auditComment = "";
        String auditStatus = "";
        
        form = aform.getForm();
        List<Question> allQuestionsInForm = new ArrayList<Question>();

        // get all allQuestionsInForm in the form
        for ( List<Section> row : form.getRowList() ) {
            for ( Section section : row ) {
                if (section != null) {
                    allQuestionsInForm.addAll(section.getQuestionList());
                }
            }
        }                		
        for (Iterator<Response> it = aform.getResponses().iterator(); it.hasNext();) {
            resp = it.next();
            String qid = Integer.toString(resp.getQuestion().getId());
            String sid = Integer.toString(resp.getQuestion().getSectionId());
       		String otherValue[] = request.getParameterValues("S_" + Integer.toString(resp.getQuestion().getSectionId())  + "_Q_" + Integer.toString(resp.getQuestion().getId())+"_otherBox");
            q = resp.getQuestion();
            String commentId = "auditComment_S_" +sid +"_Q_"+ qid; //TODO: "auditComment_"+qid; //666
            auditComment = request.getParameter(commentId);

//            // Has answer changed ?
//            if (edits.containsAll(resp.getAnswers()) && resp.getAnswers().containsAll(edits)) {
//                // answers are the same, do nothing?
//                continue;
//            }
            
//            if (q.getFormQuestionAttributes().isCalculatedQuestion()) {
//            	auditComment = "Automatically calculated value.";
//            }

            resp.setEditAuditComment(auditComment);
            /* for auditStatus*/
            String auditstatusId = "auditStatus_S_" +sid +"_Q_"+ qid; //TODO: "auditComment_"+qid; //666
            auditStatus = request.getParameter(auditstatusId);
            resp.setAuditStatus(auditStatus);

            resp.setAdministeredForm(aform);
     
            if (auditComment != null && auditComment.length() > 255)
                errors.add("Audit comment can't be longer that 255 characters: question \"" + q.getText() + "\".  <a href=\"Javascript:;\" onclick=\"goToValidationErrorFlag();document.getElementById ('S_" + q.getSectionId() + "_Q_" + q.getId() + "').focus();$('#S_" + q.getSectionId() + "_Q_" + q.getId() + "').trigger('change');\">Go To</a>");
        }

//        if (errors.size() > 0 && deletingAnswers) {
//            removeDeleted(aform.getResponses());
//        }
        
        logger.info(DataCollectionUtils.getUserIdSessionIdString(request)+"#InputHandler->getAuditComments->errors size#:"+errors.size());
        		
        return errors;
    }

    /**
     * It checks if the passed in dependent question of skip rule for the type of disable requires answer.
     *
     * @param questions All the questions on the data entry form.
     * @param q         The dependent question of skip rule to check for
     * @param request   The HTTP request object that contains parent skip rule question's answers.
     * @return boolean  true: if the question requires answer of the skip rule for the type of disable.
     */
    private static boolean isRequiredChildOfSkipRuleDisable(List<Question> questions, Question q, HttpServletRequest request) {
        boolean isRequiredChildOfDisable = false;
        for (Iterator<Question> it = questions.iterator(); it.hasNext();) {
            Question qq = it.next();
            FormQuestionAttributes qAttrs = qq.getFormQuestionAttributes();
            
            if (qAttrs.hasSkipRule() && qAttrs.getSkipRuleType() == SkipRuleType.DISABLE) {
                boolean hasValue = false;
                String values[] = request.getParameterValues("S_" + qq.getSectionId() + "_Q_" + qq.getId());

                if (values != null) {
                    for (int i = 0; i < values.length; i++) {
                        String a = values[i].trim();
                        if (a.length() != 0) {
                            hasValue = true;
                            break;
                        }
                    }
                }
                
                for (Question qqq : qAttrs.getQuestionsToSkip()) {
                    if (qqq.getId() == q.getId()) {
                        if ((qAttrs.getSkipRuleOperatorType().equals(SkipRuleOperatorType.EQUALS)) &&
                                hasValue && (values.length == 1) && (values[0].equals(qAttrs.getSkipRuleEquals()))) {
                            isRequiredChildOfDisable = true;
                        } else if ((qAttrs.getSkipRuleOperatorType().equals(SkipRuleOperatorType.IS_BLANK)) && !hasValue) {
                            isRequiredChildOfDisable = true;
                        } else if ((qAttrs.getSkipRuleOperatorType().equals(SkipRuleOperatorType.HAS_ANY_VALUE)) && hasValue) {
                            isRequiredChildOfDisable = true;
                        }
                    }
                }
            }
        }
        
        return isRequiredChildOfDisable;
    }

    public static double trim(double n, int digits) {
        double d = 0.0;

        try {
            d = Double.parseDouble(formatNumber(n, digits));
        } catch (Exception e) {
            d = 0.0;
        }

        return d;
    }

    public static String formatNumber(double d, int digits) {
        String clipped;
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(digits);
        df.setGroupingUsed(false);
        clipped = df.format(d);

        return clipped;
    }
    
    public static List<String> removeDuplicates(List<String> l) {
        Set<String> s = new TreeSet<String>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                // ... compare the two object according to your requirements
            	int flag = 1;
            	if ( o1.equalsIgnoreCase(o2) ) {
            		flag= 0;
            	}
            	
            	return flag;
            }
        });
        
        s.addAll(l);
        l.clear();
        l.addAll(s);
        
        return l;
    }
    //!!!!!!!!!!!!!!!!!!!!ALERT FROM YOGI & TSEGA!!!!!!!!!!!!!!!!!!!!!!!!!!//
    //****************************//
    //****************************//
   //*PLEASE DON'T DELETE THESE AND REFERENCED METHOD WE NEED TO REVISIT THESE IMPLEMENTATION**/ 
    //****************************//
    //****************************//
    //****************************//
    //****************************//
    
    
//	public static List<String> validateAgainstFS(AdministeredForm aForm, HttpServletRequest request) {
//		class ValidateThread extends Thread {
//			private AdministeredForm aForm;
//
//			public ValidateThread(AdministeredForm aForm) {
//				super();
//				this.aForm = aForm;
//			}
//
//			@Override
//			public void run() {
//				try {
//					String xmlAform = InputHandler.toBricsXML(aForm);
//					String restfulDomain = SysPropUtil.getProperty("webservice.restful.ddt.domain");
//					String restfulUrl = restfulDomain + SysPropUtil.getProperty("webservice.restful.ddt.fs.validation");
//					Client client = ClientBuilder.newClient();
//
//					restfulUrl = restfulUrl + "?aformid=" + aForm.getId();
//					logger.info("calling validateAgainstFS...url: " + restfulUrl);
//
//					WebTarget target = client.target(restfulUrl);
//					Entity<String> entity = Entity.entity(xmlAform, MediaType.TEXT_XML);
//					String response = target.request(MediaType.TEXT_XML).post(entity, String.class);
//
//					logger.info("Response from server: " + response);
//				}
//				catch (TransformationException | ParserConfigurationException | TransformerFactoryConfigurationError | TransformerException e) {
//					logger.error("Could not convert the administered form to XML.", e);
//				}
//				catch (WebApplicationException | ProcessingException e) {
//					logger.error("Could not validate form structure.", e);
//				}
//			}
//		}
//
//		ValidateThread vt = new ValidateThread(aForm);
//		vt.start();
//
//		return new ArrayList<String>();
//	}
//    
//	/**
//	 * Creates an XML String representation of an AdministeredForm instance data.
//	 * The XML produced will validate against the BRICS dictionary schema.
//	 * 
//	 * @param aForm
//	 * @return String
//	 * @throws TransformationException
//	 * @throws ParserConfigurationException 
//	 * @throws TransformerException 
//	 * @throws TransformerFactoryConfigurationError 
//	 */
//	public static String toBricsXML(AdministeredForm aForm) throws TransformationException,
//			ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException {
//		boolean containsInvalidText = false;
//		String result = "";
//
//		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
//		Element root = document.createElement("Dataset");
//
//		root.setAttribute("URI", "");
//        document.appendChild(root);
//		
//		Element formNode = document.createElement(aForm.getForm().getDataStructureName());
//		// going back in at some point - just needs to not be in for now
//		// formNode.setAttribute("URI", Integer.toString(aForm.getId()));
//
//		Collections.sort(aForm.getForm().getRowList(), new Comparator<List<Section>>() {
//			@Override
//			public int compare(List<Section> o1, List<Section> o2) {
//				Collections.sort(o1);
//				Collections.sort(o2);
//				return o1.get(0).compareTo(o2.get(0));
//			}
//		});
//
//		for (List<Section> sectionList : aForm.getForm().getRowList()) {
//			for (Section section : sectionList) {
//				Element repeatableGroupNode;
//				
//				try {
//					repeatableGroupNode = document
//							.createElement(cleanRgName(cleanXmlText(section.getRepeatableGroupName())));
//				} catch (Exception e) {
//					repeatableGroupNode = document.createElement("Invalid");
//					containsInvalidText = true;
//				}
//
//				if (section.getQuestionList().size() > 0) {
//					formNode.appendChild(repeatableGroupNode);
//				}
//
//				List<Question> questions = section.getQuestionList();
//				Collections.sort(questions);
//
//				for (Question question : section.getQuestionList()) {
//					String dataElementName = question.getFormQuestionAttributes().getDataElementName();
//					Element dataElementNode;
//					try {
//						dataElementNode = document.createElement(
//								cleanXmlText(dataElementName.substring(dataElementName.lastIndexOf('.') + 1)));
//
//						for (Response response : aForm.getResponses()) {
//							if (response.getQuestion().getId() == question.getId()) {
//
//								String answer = response.getSubmitAnswers().get(0);
//
//								if (isValidDate(answer)) {
//									answer = parseBricsDate(answer);
//								}
//
//								dataElementNode.setTextContent(answer);
//								repeatableGroupNode.appendChild(dataElementNode);
//							}
//						}
//					} catch (Exception e) {
//						repeatableGroupNode = document.createElement("Invalid");
//						containsInvalidText = true;
//					}
//
//				}
//			}
//		}
//
//		// right now, the webservice doesn't expect the dataset node. I am
//		// replacing this with the
//		// form node for now as the root element (insert happens above)
//		root.appendChild(formNode);
//
//		result = formatXML(document);
//
//		if (containsInvalidText == false) {
//			logger.info("XML file generated successfully with valid data.");
//		} else {
//			logger.warn("XML file generated with invalid data.");
//		}
//
//		return result;
//	}
	
	private static boolean isValidDate(String input) {
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		
	     try {
	          format.parse(input);
	          return true;
	     }
	     catch(Exception e){
	          return false;
	     }
	}
	
	private static String parseBricsDate(String input) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-ddTHH:mm");
		
	     try {
	          Date output = format.parse(input);
	          return format.format(output);
	     }
	     catch(Exception e){
	          return null;
	     }
	}
	
	/**
	 * Utility method to clean text used for XML tags.
	 * 
	 * @param text
	 * @return String
	 */
	private static String cleanXmlText(String text){
		String cleanString;
		
		cleanString = StringUtils.deleteWhitespace(text);
		cleanString = cleanString.replaceAll("/", "");
		cleanString = cleanString.replaceAll("&", "");
		cleanString = StringEscapeUtils.escapeXml(cleanString);
		
		return cleanString;
		
	}
	
	/**
	 * Because regular expression names (for xml) should contain no characters other than alphanumeric
	 * upper or lower case, this method cleans out everything else.
	 * 
	 * @param text the input string to clean
	 * @return a string with all non-alphanumeric characters removed
	 */
	private static String cleanRgName(String text) {
		return text.replaceAll("[^A-Za-z0-9]", "");
		
	}
	
	/**
	 * Utility method to format a given xml document into a human-readable format.
	 * 
	 * @param doc
	 * @return String
	 * @throws TransformerFactoryConfigurationError 
	 * @throws TransformerException 
	 */
	private static String formatXML (Document doc) throws TransformerFactoryConfigurationError, TransformerException{
		String formattedXML = "";
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(doc);
		
		transformer.transform(source, result);
		formattedXML = result.getWriter().toString();
		
		return formattedXML;
	}

	public static Boolean checkEmailIsTriggerred(EmailTrigger et, Response currentRes, List<Response> responses,
			QuestionType questionType, AnswerType answerType) throws CtdbException {
		Boolean isTriggerred = false;
		List<EmailTriggerValue> etvList = new ArrayList<EmailTriggerValue>(et.getTriggerValues());
		if (questionType == QuestionType.RADIO || questionType == QuestionType.CHECKBOX
				|| questionType == QuestionType.SELECT || questionType == QuestionType.MULTI_SELECT) {
			List<String> resAnswers = currentRes.getAnswers();
			for (EmailTriggerValue etv : etvList) {
				for (String ans : resAnswers) {
					if (ans.equalsIgnoreCase(etv.getAnswer())) {
						isTriggerred = true;
					}
				}
			}

		} else if (questionType == QuestionType.TEXTBOX && answerType == AnswerType.NUMERIC && etvList.size() > 0) {
			String conditionStr = etvList.get(0).getTriggerCondition();

			isTriggerred = checkCondtions(conditionStr, responses);
		}
		return isTriggerred;
	}


	public static Boolean checkCondtions(String conditionStr, List<Response> responses)
			throws CtdbException {
		List<String> emptyErrs = new ArrayList<String>();
		conditionStr = conditionStr.replaceAll("\\s+", ""); // remove all spaces from condition string
		conditionStr = replaceAllQuestionsWithValues(conditionStr, responses, emptyErrs);
		Boolean condResult = null;
		// find if cond str contains &&( or ||( or )&& or )||, which means condition string has groups of logical
		// operators
		Pattern pattern1 = Pattern.compile("&&\\(|\\|\\|\\(|\\)&&|\\)\\|\\|");
		Matcher matcher1 = pattern1.matcher(conditionStr);
		// if there is no any group of logical operations
		if (!matcher1.find()) {
			condResult = checkSimpleCondition(conditionStr);
		} else {
			condResult = checkNestedCondition(conditionStr, responses);
		}
		if (emptyErrs.size() > 0) {
			return false;
		} else {
			return condResult;
		}
	}

	public static Boolean checkNestedCondition(String condition, List<Response> responses)
			throws CtdbException {
		Boolean condResult = null;

		Pattern patternOrOp = Pattern.compile("\\|\\|\\(|\\)\\|\\|");
		Matcher orOpMatcher = patternOrOp.matcher(condition);
		if(orOpMatcher.find()) {
			condResult = checkOrCondition(condition, responses, orOpMatcher);
		} else {
			Pattern patternAndOp = Pattern.compile("&&\\(|\\)&&");
			Matcher andOpMatcher = patternAndOp.matcher(condition);
			condResult = checkAndCondition(condition, responses, andOpMatcher);
		}

		return condResult;
	}

	public static Boolean checkOrCondition(String condition, List<Response> responses, Matcher orOpMatcher)
			throws CtdbException {
		Boolean condResult = null;
		List<String> orCondList = new ArrayList<String>();
		if (orOpMatcher.find()) {
			int firstOrIdx = 0;
			if (condition.indexOf(")||") > 0) {
				firstOrIdx = condition.indexOf(")||");
			} else if (condition.indexOf("||(") > 0) {
				firstOrIdx = condition.indexOf("||(");
			}
			orCondList.add(condition.substring(0, firstOrIdx));
			condition = condition.substring(firstOrIdx + 2);
			orCondList.add(condition);
		}

		for (String orCond : orCondList) {
			if (condResult == null) {
				condResult = checkCondtions(orCond, responses);
			} else {
				condResult = condResult || checkCondtions(orCond, responses);
			}
		}
		return condResult;
	}

	public static Boolean checkAndCondition(String condition, List<Response> responses, Matcher andOpMatcher)
			throws CtdbException {
		Boolean condResult = null;
		List<String> andCondList = new ArrayList<String>();
		if (andOpMatcher.find()) {
			int firstAndIdx = 0;
			if (condition.indexOf(")&&") > 0) {
				firstAndIdx = condition.indexOf(")&&");
			} else if (condition.indexOf("&&(") > 0) {
				firstAndIdx = condition.indexOf("&&(");
			}
			andCondList.add(condition.substring(0, firstAndIdx));
			condition = condition.substring(firstAndIdx + 2);
			andCondList.add(condition);
		}

		for (String andCond : andCondList) {
			if (condResult == null) {
				condResult = checkCondtions(andCond, responses);
			} else {
				condResult = condResult && checkCondtions(andCond, responses);
			}
		}
		return condResult;
	}

	public static Boolean checkSimpleCondition(String condition)
			throws CtdbException {
		Boolean condResult = null;
		if (condition.startsWith("(") && condition.endsWith(")")) { // remove leading and trailing parenthesis
			condition = condition.replaceAll("^\\(|\\)$", "");
		}
		String[] orCondArr = condition.split("\\|\\|"); // split cond string by or operator first

		for (String orCond : orCondArr) {

			Boolean andCondsResult = null;
			String[] andCondArr = orCond.split("&&"); // then split sub cond string by or operator first
			for (String andCond : andCondArr) {
				if (andCondsResult == null) {
					// subCond only contains comparator
					andCondsResult = getResultFromComparison(andCond);
				} else {
					andCondsResult = andCondsResult
							&& getResultFromComparison(andCond);
				}
			}
			if (condResult == null) {
				condResult = andCondsResult;
			} else {
				condResult =
						condResult || getResultFromComparison(orCond);
			}
		}

		return condResult;
	}


	/* the condition contains only comparator */
	public static Boolean getResultFromComparison(String condition)
			throws CtdbException {
		Boolean condResult = false;

		String comparator = getComparator(condition);
		List<String> comparareValList = Arrays.asList(condition.split(comparator));

		switch (comparator) {
			case "==":
				try {
					double valLeft = getValueFromCondition(comparareValList.get(0));
					double valRight = getValueFromCondition(comparareValList.get(1));
					if (valLeft == valRight) {
						condResult = true;
					}
				} catch (Exception e) {
					logger.info("Errors occur in equal comparative.");
				}
				break;
			case "!=":
				try {
					double valLeft = getValueFromCondition(comparareValList.get(0));
					double valRight = getValueFromCondition(comparareValList.get(1));
					if (valLeft != valRight) {
						condResult = true;
					}
				} catch (Exception e) {
					logger.info("Errors occur in not equal compartive.");
				}
				break;
			case ">":
				try {
					double valLeft = getValueFromCondition(comparareValList.get(0));
					double valRight = getValueFromCondition(comparareValList.get(1));
					if (valLeft > valRight) {
						condResult = true;
					}
				} catch (Exception e) {
					logger.info("Errors occur in greater than comparative.");
				}
				break;
			case "<":
				try {
					double valLeft = getValueFromCondition(comparareValList.get(0));
					double valRight = getValueFromCondition(comparareValList.get(1));
					if (valLeft < valRight) {
						condResult = true;
					}
				} catch (Exception e) {
					logger.info("Errors occur in less than comparative.");
				}
				break;
			case ">=":
				try {
					double valLeft = getValueFromCondition(comparareValList.get(0));
					double valRight = getValueFromCondition(comparareValList.get(1));
					if (valLeft >= valRight) {
						condResult = true;
					}
				} catch (Exception e) {
					logger.info("Errors occur in greater equal comparative.");
				}
				break;
			case "<=":
				try {
					double valLeft = getValueFromCondition(comparareValList.get(0));
					double valRight = getValueFromCondition(comparareValList.get(1));
					if (valLeft <= valRight) {
						condResult = true;
					}
				} catch (Exception e) {
					logger.info("Errors occur in less equal comparative.");
				}
				break;
			default:
				logger.info("Email trigger condition is missing a comparator.");
				throw new CtdbException("Email trigger condition is missing a comparator");
		}

		return condResult;
	}


	public static String getComparator(String subCond) {
		String comparator = "";
		if (subCond.contains("==")) {
			comparator = "==";
		} else if (subCond.contains("!=")) {
			comparator = "!=";
		} else if (subCond.contains(">")) {
			comparator = ">";
		} else if (subCond.contains("<")) {
			comparator = "<";
		} else if (subCond.contains(">=")) {
			comparator = ">=";
		} else if (subCond.contains("<=")) {
			comparator = "<=";
		}
		return comparator;
	}

	public static double getValueFromCondition(String condition)
			throws CtdbException {
		condition = condition.replaceAll("%", "*(1/100)*");
		org.nfunk.jep.JEP myParser = new org.nfunk.jep.JEP();
		myParser.addStandardFunctions();
		myParser.parseExpression(condition);

		double conditionVal = myParser.getValue();
		conditionVal = trim(conditionVal, 2);
		return conditionVal;
	}

	public static String replaceAllQuestionsWithValues(String condition, List<Response> responses, List<String> errors)
			throws CtdbException {
		String rtnCond = condition.replaceAll("\\[|\\]", "");

		List<String> sectionQuestionIdList = new ArrayList<String>();
		List<String> tempList = getSectionQuestionIdListFromCondition(rtnCond);
		if (tempList.size() > 0) {
			sectionQuestionIdList.addAll(tempList);
		}
		for (String sQId : sectionQuestionIdList) {
			sQId = sQId.replaceAll("\\[|\\]", "");
			List<String> postedAnswers = new ArrayList<String>();
			String questionText = "";
			for (Response res : responses) {

				if (sQId.equals("S_" + res.getQuestion().getSectionId() + "_Q_" + res.getQuestion().getId())) {
					postedAnswers.addAll(res.getAnswers());
					questionText = res.getQuestion().getText();
				}
			}
			String postedVal = postedAnswers.size() > 0 ? postedAnswers.get(0) : "";
			if (rtnCond.contains("thisQuestion_")) {
				rtnCond = rtnCond.replaceAll("\\[|\\]", "").replaceAll("thisQuestion_", "").replace(sQId, postedVal);
			} else {
				rtnCond = rtnCond.replaceAll("\\[|\\]", "").replace(sQId, postedVal);
			}
			if (postedVal.isEmpty()) {
				if (errors != null) {
					String err = "Answer for question \"" + questionText
							+ "\" does not have value. Email trigger will overlook this question -'S_" + sQId + "'";
					if (!errors.contains(err)) {
						errors.add(err);
					}
				}
			}
		}
		return rtnCond;
	}

	public static List<String> getSectionQuestionIdListFromCondition(String condition) {
		if(condition.contains("thisQuestion_")) {
			condition = condition.replaceAll("thisQuestion_", "");
		}
		List<String> sectionQuestionIdList = new ArrayList<String>();
		Pattern pattern = Pattern.compile("S_[-]?\\d+_Q_([0-9]+)");
		Matcher match = pattern.matcher(condition);
		while (match.find()) {
			sectionQuestionIdList.add(match.group());
		}
		return sectionQuestionIdList;
	}

	private static String getConditionStrWithText(EmailTrigger et, Response currentRes, List<Response> responses,
			QuestionType questionType, AnswerType answerType, Boolean isEditAnswer) throws CtdbException {
		String rtnStr = "";
		List<EmailTriggerValue> etvList = new ArrayList<EmailTriggerValue>(et.getTriggerValues());

		if (questionType == QuestionType.TEXTBOX && answerType == AnswerType.NUMERIC && etvList.size() > 0) {
			String conditionStr = etvList.get(0).getTriggerCondition();
			rtnStr = replaceAllQuestionsWithText(conditionStr, responses, isEditAnswer);
		}

		return rtnStr;
	}

	public static String replaceAllQuestionsWithText(String condition, List<Response> responses, Boolean isEditAnswer)
			throws CtdbException {
		String rtnCond = condition.replaceAll("\\[|\\]", "");

		List<String> sectionQuestionIdList = new ArrayList<String>();
		List<String> tempList = getSectionQuestionIdListFromCondition(rtnCond);
		if (tempList.size() > 0) {
			sectionQuestionIdList.addAll(tempList);
		}

		for (String sQId : sectionQuestionIdList) {
			sQId = sQId.replaceAll("\\[|\\]", "");
			String questionText = "";
			List<String> postedAnswers = new ArrayList<String>();
			for (Response res : responses) {
				if (sQId.equals("S_" + res.getQuestion().getSectionId() + "_Q_" + res.getQuestion().getId())) {
					questionText = res.getQuestion().getText();
					if (isEditAnswer) {
						postedAnswers.addAll(res.getEditAnswers());
					} else {
						postedAnswers.addAll(res.getAnswers());
					}
				}
			}
			String postedVal = postedAnswers.size() > 0 ? postedAnswers.get(0) : "";
			if (rtnCond.contains("thisQuestion_")) {
				rtnCond = rtnCond.replaceAll("\\[|\\]", "").replaceAll("thisQuestion_", "").replace(sQId,
						questionText + "(" + postedVal + ")");
			} else {
				rtnCond = rtnCond.replaceAll("\\[|\\]", "").replace(sQId, questionText + "(" + postedVal + ")");
			}
		}
		return rtnCond;
	}

}
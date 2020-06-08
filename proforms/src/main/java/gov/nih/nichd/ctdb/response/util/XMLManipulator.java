package gov.nih.nichd.ctdb.response.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.domain.Section;
import gov.nih.nichd.ctdb.patient.domain.PatientVisit;
import gov.nih.nichd.ctdb.patient.domain.PatientVisitPrepopValue;
import gov.nih.nichd.ctdb.patient.manager.PatientManager;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.question.domain.Answer;
import gov.nih.nichd.ctdb.question.domain.AnswerType;
import gov.nih.nichd.ctdb.question.domain.ImageMapOption;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.domain.QuestionType;
import gov.nih.nichd.ctdb.question.manager.QuestionManager;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;
import gov.nih.nichd.ctdb.response.domain.PatientCalendarCellResponse;
import gov.nih.nichd.ctdb.response.domain.Response;
import gov.nih.nichd.ctdb.response.manager.ResponseManager;
import gov.nih.nichd.ctdb.site.domain.Site;
import gov.nih.nichd.ctdb.site.manager.SiteManager;

/**
 * XMLManipulator manipulates the XML representation of a form in order to set
 * user responses. This is necessary in order to manipulate the responses into
 * the right tree path so that the XSL template used for display can retrieve
 * data.
 * 
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class XMLManipulator {
	private static final Logger logger = Logger.getLogger(XMLManipulator.class);
	/**
	 * Poplutes the form XML represenation with user responses.
	 * 
	 * @param administeredForm
	 *            The administered form object set answers for
	 * @return The form Document object with answers
	 * @throws CtdbException
	 *             Thrown if any errors occur while processing
	 * @throws UnknownBusinessManagerException
	 * @throws BusinessManagerAccessException
	 */
	public static Document populateFormWithAnswers(
			AdministeredForm administeredForm) throws CtdbException {
		return populateFormWithAnswers(administeredForm, false, null);
	}

	public static Document populateFormWithAnswers(AdministeredForm administeredForm, boolean dataEntry, PatientVisit pvPSR) throws CtdbException {
		ProtocolManager protocolMan = new ProtocolManager();
		SiteManager siteMan = new SiteManager();
		
		final String questionDefaultValueXPath1 = "form/row/formcell/section[@id=?]/sectionRows/";
		final String questionDefaultValueXPath2 = "questions/question[@id=?]/defaultValue";
		final String questionTypeXPath1 = "form/row/formcell/section[@id=?]/sectionRows/";
		final String questionTypeXPath2 = "questions/question[@id=?]/@type";
		final String questionAnswerXPath1 = "form/row/formcell/section[@id=?]/sectionRows/";
		final String questionAnswerXPath2 = "questions/question[@id=?]/answers/answer[translate(display,'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')='&']/selected";
		//final String imageMapQuestionAnswerXPath1 = "form/row/formcell/section[@id=?]/sectionRows/";
		//final String imageMapQuestionAnswerXPath2 = "questions/question[@id=?]/answers/option[translate(display,'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')='&']/@selected";
		//final String TOCDisplayPath = "form/TOC/@display";

		// added by Ching Heng
		final String questionUploadFileIdXPath1 = "form/row/formcell/section[@id=?]/sectionRows/";
		final String questionUploadFileIdXPath2 = "questions/question[@id=?]/attachmentId";

		// for other option textbox
		final String questionOtherTextBoxXPath1 = "form/row/formcell/section[@id=?]/sectionRows/";
		final String questionOtherTextBoxXPath2 = "questions/question[@id=?]/otherBox";

		// added by yogi for repeatable section
		final String repeatableVisibleSectionXPath7 = "form/row/formcell[@secId=?]/@visible";
		// added by yogi for de comments
		//final String deCommentXPath2 = "questions/question[@id=?]/deComment";

		String newrepeatableVisibleSectionXPath7 = "";

		try {
			Form form = administeredForm.getForm();
			Set<String> sectionQuestionIds = form.getSectionQuestionIds();
			Map<Integer, Question> mapQuestionIdQuestion = form.getMapQuestionIdQuestion();

			Document d = form.getFormXml();
			Document dom = (Document) d.cloneNode(true);
			JXPathContext context = JXPathContext.newContext(dom);

			Response response;
			Question question;
			List<String> answers = null;
			//List<String> deComments = null;

			// repeatable section
			ResponseManager rm = new ResponseManager();
			
			List<Integer> sectionGroupList = rm.getVisibleRepeableSectionsList(administeredForm.getId(), administeredForm.getLoggedInUserId());
			// Get all section for aform from request
			List<List<Section>> rowList = administeredForm.getForm().getRowList();
			 
			int intervalId = administeredForm.getInterval().getId();								

			if (!sectionGroupList.isEmpty()) {
				for (List<Section> colunm : rowList) { // each row
					for ( Section sec : colunm ) { // each column
						if (sec != null) {
							if (sectionGroupList.contains(sec.getId())) {
								newrepeatableVisibleSectionXPath7 = StringUtils.replace(
										repeatableVisibleSectionXPath7, 
										"secId=?", "secId=" + String.valueOf(sec.getId()));
								context.setValue(newrepeatableVisibleSectionXPath7, "true");
							} else {
								newrepeatableVisibleSectionXPath7 = StringUtils.replace(
										repeatableVisibleSectionXPath7,
										"secId=?", "secId=" + String.valueOf(sec.getId()));
								context.setValue(newrepeatableVisibleSectionXPath7, "false");
							}
						}
					}
				}
			}
			
//			if (dataEntry) {
//				try {
//					context.setValue(TOCDisplayPath, "true");
//				} catch (Exception e) { }
//			}
			
			
			List<PatientVisitPrepopValue> pvPrepopValueList = new ArrayList<PatientVisitPrepopValue>();
			PatientManager patientMan = new PatientManager();
			try {
				PatientVisit pv = null;
				if(pvPSR == null){
					pv = patientMan.getPatientVisit(administeredForm.getPatient().getId(), administeredForm.getVisitDate(), intervalId);
				} else {
					pv = pvPSR;
				}
				int visitdateId = pv.getId();
				pvPrepopValueList = protocolMan.getPvPrepopValuesForInterval(intervalId, visitdateId);
			}catch(ObjectNotFoundException e) {
				logger.info("No Scheduled visit for Subject.");
				//We don't need any stack trace since QA thinks it error otherwiese.
				//DAO already says that there is no schedule visit that is good enough
				//e.printStackTrace();
			}
			
//			logger.info("administeredForm.getInterval().id: " + intervalId + " pvPrepopValueList.size(): " +pvPrepopValueList.size());	
			
			int current = 0;
			QuestionManager qm = new QuestionManager();
			if ( administeredForm.isStartMode() ) {
				
				for ( List<Section> row : rowList ) {
					for ( Section section : row ) {
						if ( section != null ) {
							List<Question> qList = section.getQuestionList();
							int sectionId = section.getId();
							
							for (Question q : qList) {
								int questionId = q.getId();//logger.info("q.getName(): " + q.getName()+ " questionId: "+questionId);
								String dataElementName = q.getFormQuestionAttributes().getDataElementName();
//								logger.info("q.getName(): " + q.getName()+ " dataElementName: "+dataElementName);
							
									String questionDefaultValueXPath1New = StringUtils.replace(questionDefaultValueXPath1, "id=?", "id=" + sectionId);
									String questionDefaultValueXPath2New = StringUtils.replace(questionDefaultValueXPath2, "id=?", "id=" + questionId);
									String questionDefaultValueXPathNew = questionDefaultValueXPath1New + questionDefaultValueXPath2New;

									String prepopulationAnswer = "";
									SimpleDateFormat df = null;

									switch (dataElementName) {
									case CtdbConstants.VISIT_DATE_DATA_ELEMENT:
										if ( q.getFormQuestionAttributes().getAnswerType().equals(AnswerType.DATE) ) {
											df = new SimpleDateFormat("yyyy-MM-dd");
										} else if ( q.getFormQuestionAttributes().getAnswerType().equals(AnswerType.DATETIME) ) {
											df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
										}

										//regardless if its single entry or double entry, when in start, the getVisitDate has whichever date you need
										if ( administeredForm.getVisitDate() != null ) {
											prepopulationAnswer = df.format(administeredForm.getVisitDate());
										}
										break;

									case CtdbConstants.VISIT_TYPE_DATA_ELEMENT:
									case CtdbConstants.VISIT_TYP_PDBP_DATA_ELEMENT:
										if ( administeredForm.getInterval() == null ) {
											prepopulationAnswer = "Other";
										} else {
											prepopulationAnswer = protocolMan.getInterval(intervalId).getName();
										}
										
										if(q.getType() == QuestionType.SELECT || q.getType() == QuestionType.RADIO) {
											List<Answer> questionAnswers = q.getAnswers();
											
											boolean valid = false;
											if (questionAnswers != null && !questionAnswers.isEmpty()) {

												for (Iterator iter = questionAnswers.iterator(); iter.hasNext();) {
													String questionAnswer = ((Answer) iter.next()).getDisplay();
													if (questionAnswer == null) {
														continue;
													}

													if (prepopulationAnswer.trim().equalsIgnoreCase(questionAnswer.trim())) {
														prepopulationAnswer = questionAnswer.trim();
														valid = true;
														break;
													}
												}
											}
											
											if (!valid) {
												prepopulationAnswer = "";	
											}
										}

										break;

									case CtdbConstants.GUID_DATA_ELEMENT:
										if( administeredForm.getPatient() != null && administeredForm.getPatient().getGuid() != null ) {
											prepopulationAnswer = administeredForm.getPatient().getGuid();
										}
										break;

									case CtdbConstants.SITE_NAME_DATA_ELEMENT:
										if(administeredForm.getSiteName() == null) {
											int protocolId = administeredForm.getForm().getProtocolId();
                                            Protocol protocol = protocolMan.getProtocol(protocolId);
                                            List<Site> sites = protocol.getStudySites();

                                            if ( sites.size() == 1 ) {
                                                prepopulationAnswer = sites.get(0).getName();
                                            } else {
                                                boolean primarySiteFound = false;                                            
                                                for ( Site site : sites ) {
                                                    if( site.isPrimarySite() ) {
                                                        primarySiteFound = true;
                                                        prepopulationAnswer = site.getName();
                                                        break;
                                                    }
                                                }
                                                
                                                if ( !primarySiteFound ) {
                                                    prepopulationAnswer = "";
                                                }
                                            }
										}else {
											prepopulationAnswer= administeredForm.getSiteName();
										}
										
										if(q.getType() == QuestionType.SELECT || q.getType() == QuestionType.RADIO) {
											List<Answer> questionAnswers = q.getAnswers();
											
											boolean valid = false;
											if (questionAnswers != null && !questionAnswers.isEmpty()) {

												for (Iterator iter = questionAnswers.iterator(); iter.hasNext();) {
													String questionAnswer = ((Answer) iter.next()).getDisplay();
													if (questionAnswer == null) {
														continue;
													}

													if (prepopulationAnswer.trim().equalsIgnoreCase(questionAnswer.trim())) {
														prepopulationAnswer = questionAnswer.trim();
														valid = true;
														break;
													}
												}
											}
											
											if (!valid) {
												prepopulationAnswer = "";	
											}
										}
										
										break;
									default:
										for(PatientVisitPrepopValue pvPrepopValue : pvPrepopValueList){
											if (dataElementName.equals(pvPrepopValue.getPrepopDataElement().getShortName())){
												String prepopvalue = pvPrepopValue.getPrepopvalue();
												prepopulationAnswer = prepopvalue == null ? "" : prepopvalue;
//												logger.info("dataElementName: " + dataElementName+" prepopulationAnswer: " + prepopulationAnswer);
											}
										} //end for loop
										break;									
									}//end switch
									
									if (!prepopulationAnswer.equals("")) {
										context.setValue(questionDefaultValueXPathNew, prepopulationAnswer);
									}									
									
								
							}
						}
					}
				}
			}
			
			for (Iterator<Response> iterator = administeredForm.getResponses().iterator(); iterator.hasNext();) {
				response = iterator.next();
				question = response.getQuestion();
				String secQues = "S_" + question.getSectionId() + "_Q_"+ question.getId();
				
				if (!(response instanceof PatientCalendarCellResponse)) {
					if (question != null) {
						int questionId = question.getId();

						if (!sectionQuestionIds.contains(secQues)) {
							continue;
						}

						List<Answer> questionAnswers = ((Question) mapQuestionIdQuestion.get(questionId)).getAnswers();

						String questionTypeXPath1New = StringUtils.replace(
								questionTypeXPath1, "id=?", "id=" + question.getSectionId());
						String questionTypeXPath2New = StringUtils.replace(
								questionTypeXPath2, "id=?", "id=" + question.getId());
//						String deCommentXPath2New = StringUtils.replace(
//								deCommentXPath2, "id=?", "id=" + question.getId());
						
						//String deCommentXPath = questionTypeXPath1New+deCommentXPath2New;
						String questionTypeXPathNew = questionTypeXPath1New + questionTypeXPath2New;
						String type = (String) context.getValue(questionTypeXPathNew);

						// for edit answer, if they failed to complete edit
						// answer, try to populate the form w/ their edits.
						if (!response.getEditAnswers().isEmpty() || 
							(response.getEditReason() != null && response.getEditAnswers().isEmpty())) {
							answers = response.getEditAnswers();
//							deComments = response.getDeComments();
//							if(deComments.size()>0) {
//									context.setValue(deCommentXPath, deComments.get(0));	
//							}
						}
						else {
							answers = response.getAnswers();
//							deComments = response.getDeComments();
//							if(deComments.size()>0) {
//								context.setValue(deCommentXPath, deComments.get(0));	
//							}
							Iterator<String> iter = answers.iterator();

							while (iter.hasNext()) {
								int temp = response.getQuestion().getSectionId();
								iter.next();
								
								if (temp > current) {
									current = temp;
								}
							}
						}

						if (answers != null) {
							if (type.equalsIgnoreCase(QuestionType.MULTI_SELECT.getDispValue()) ||
								type.equalsIgnoreCase(QuestionType.CHECKBOX.getDispValue())) {

								// multiple answers
								String answer, answer1;

								String questionAnswerXPath1New = StringUtils.replace(
										questionAnswerXPath1, "id=?", "id=" + question.getSectionId());
								String questionAnswerXPath2New = StringUtils.replace(
										questionAnswerXPath2, "id=?", "id=" + question.getId());
								String xpath = questionAnswerXPath1New + questionAnswerXPath2New;
								
								// for other option textbox
								String questionOtherTextBoxXPath1New = StringUtils.replace(
										questionOtherTextBoxXPath1, "id=?", "id=" + question.getSectionId());
								String questionOtherTextBoxXPath2New = StringUtils.replace(
										questionOtherTextBoxXPath2, "id=?", "id=" + question.getId());
								String questionOtherTextBoxXPathNew = questionOtherTextBoxXPath1New
										+ questionOtherTextBoxXPath2New;

								if (question.isIncludeOtherOption()) {
									for (Iterator<String> answerItr = answers.iterator(); answerItr.hasNext();) {
										answer1 = answerItr.next().trim();
										answer = answer1.toUpperCase();
										
										// make sure the answer is part of the options list
										if (isItOther(answer.trim(), questionAnswers)) {
											// set other text field
											if (answer1.equals(CtdbConstants.OTHER_OPTION_DISPLAY)) {
												answer1 = "";
											}
											
											context.setValue(questionOtherTextBoxXPathNew, answer1);
											// set display value
											answer = CtdbConstants.OTHER_OPTION_DISPLAY.toUpperCase();
											textToUnicode(answer);
											String newAns = StringUtils.replace(answer, "'", "&apos;");
											context.setValue(StringUtils.replace(xpath, "&", newAns), "true");
										}
										else {
											textToUnicode(answer);
											String newAns = StringUtils.replace(answer, "'", "&apos;");
											context.setValue(StringUtils.replace(xpath, "&", newAns), "true");
										}
									}
								}
								else {
									for (Iterator<String> answerItr = answers.iterator(); answerItr.hasNext();) {
										answer = answerItr.next().trim();
										if(question.isDisplayPV()) {
											//need to convert the long pvd answer to pv bc the display is in pv
											List<Answer> questionOptions = question.getAnswers();
											Iterator qOptionsIter = questionOptions.iterator();
											while(qOptionsIter.hasNext()) {
					            				Answer a = (Answer)qOptionsIter.next();
					            				String pvd = a.getPvd();
					            				String submittedValue = a.getSubmittedValue();
					            				if(answer.equals(pvd)) {
					            					answer = submittedValue;
					            					break;	
					            				}	
					            				
					            			}
											
											
										}
										// make sure the answer is part of the
										// options list
										if (isAnswerValid(answer, questionAnswers)) {
											answer = textToUnicode(answer).toUpperCase();
											String newAns = StringUtils.replace(answer, "'", "&apos;");
											context.setValue(StringUtils.replace(xpath, "&", newAns), "true");
										}
									}
								}

							}
							else {
								// if document is being populated with answers,
								// default value shoudl not be used,
								// if default value was saved, it is the answer,
								// if the answer is blank, the default
								// value was deleted. the default value is
								// inserted into the xml if it exists
								// remove it or replace w/ answer
								// single answer
								String answer;
								
								if ( answers.isEmpty() ) {
									answer = "";
								}
								else {
									answer = answers.get(0);
								}
								
								if (type.equalsIgnoreCase(QuestionType.SELECT.getDispValue()) ||
									type.equalsIgnoreCase(QuestionType.RADIO.getDispValue())) {
									
									String questionDefaultValueXPath1New = StringUtils.replace(
													questionDefaultValueXPath1,
													"id=?", "id=" + question.getSectionId());
									String questionDefaultValueXPath2New = StringUtils.replace(
													questionDefaultValueXPath2,
													"id=?", "id=" + question.getId());
									String questionDefaultValueXPathNew = questionDefaultValueXPath1New
											+ questionDefaultValueXPath2New;
									// for other option textbox
									String questionOtherTextBoxXPath1New = StringUtils.replace(
													questionOtherTextBoxXPath1,
													"id=?", "id=" + question.getSectionId());
									String questionOtherTextBoxXPath2New = StringUtils.replace(
													questionOtherTextBoxXPath2,
													"id=?", "id=" + question.getId());
									String questionOtherTextBoxXPathNew = questionOtherTextBoxXPath1New
											+ questionOtherTextBoxXPath2New;
									
									if (question.isIncludeOtherOption()) { 
										// no need to make sure the answer is part of the options list
										if (isItOther(answer.trim(), questionAnswers)) {
											// set other text field
											if (answer.equals(CtdbConstants.OTHER_OPTION_DISPLAY)) {
												answer = "";
											}
											
											context.setValue(questionOtherTextBoxXPathNew, answer);
											// set display value
											answer = CtdbConstants.OTHER_OPTION_DISPLAY;
											// convert to unicode (for Chinese) add by sunny
											textToUnicode(answer);
											context.setValue(StringUtils.replace(questionDefaultValueXPathNew, "?", Integer.toString(questionId)),
													answer.trim());
										}
										else {
											// set display value
											String SameAsAnswer = whichOther(answer.trim(), questionAnswers);
											textToUnicode(SameAsAnswer);
											context.setValue(StringUtils.replace(questionDefaultValueXPathNew, "?", Integer.toString(questionId)),
													SameAsAnswer.trim());
										}
									}
									else {
										if(question.isDisplayPV()) {
											//need to convert the long pvd answer to pv bc the display is in pv
											List<Answer> questionOptions = question.getAnswers();
											Iterator qOptionsIter = questionOptions.iterator();
											while(qOptionsIter.hasNext()) {
					            				Answer a = (Answer)qOptionsIter.next();
					            				String pvd = a.getPvd();
					            				String submittedValue = a.getSubmittedValue();
					            				if(answer.equals(pvd)) {
					            					answer = submittedValue;
					            					break;	
					            				}	
					            				
					            			}
											
											
										}
										// make sure the answer is part of the
										// options list
										if (isAnswerValid(answer.trim(), questionAnswers)) {
											// set display value
											context.setValue(StringUtils.replace(
													questionDefaultValueXPathNew, "?", Integer.toString(questionId)),
													answer.trim());
										}
									}
								}
								else {
									// added by Ching Heng
									if (type.equals(QuestionType.File.getDispValue())) { 
										// if it is a file question, replace the default value, attachmentId
										// with file name and real attachment id
										// replace the attachment id
										int attachmentId = rm.getAttachmentIdFromResponse(response, administeredForm);

										String questionUploadFileIdXPath1New = StringUtils.replace(
														questionUploadFileIdXPath1,
														"id=?", "id=" + question.getSectionId());
										String questionUploadFileIdXPath2New = StringUtils.replace(
														questionUploadFileIdXPath2,
														"id=?", "id=" + question.getId());
										String questionUploadFileIdXPathNew = questionUploadFileIdXPath1New
												+ questionUploadFileIdXPath2New;
										context.setValue(questionUploadFileIdXPathNew, Integer.toString(attachmentId));

										String fileName = rm.getAttachmentNameFromResponse(response, administeredForm);
										String questionDefaultValueXPath1New = StringUtils.replace(
														questionDefaultValueXPath1,
														"id=?", "id=" + question.getSectionId());
										String questionDefaultValueXPath2New = StringUtils.replace(
														questionDefaultValueXPath2,
														"id=?", "id=" + question.getId());
										String questionDefaultValueXPathNew = questionDefaultValueXPath1New
												+ questionDefaultValueXPath2New;
										context.setValue(StringUtils.replace(
												questionDefaultValueXPathNew, "?", Integer.toString(questionId)),
												fileName);
									}
									else {
										String questionDefaultValueXPath1New = StringUtils.replace(questionDefaultValueXPath1,"id=?","id="+ question.getSectionId());
										String questionDefaultValueXPath2New = StringUtils.replace(questionDefaultValueXPath2,"id=?","id="+ question.getId());
										String questionDefaultValueXPathNew = questionDefaultValueXPath1New + questionDefaultValueXPath2New;

										context.setValue(questionDefaultValueXPathNew, answer);
									}
								}
							}
						}
					}
				}
			}
			
			return dom;
		}
		catch (Exception te) {
			throw new CtdbException("Unable to populate Document with answers: " + te.getMessage(), te);
		}
	}

	/**
	 * isAnswerValid checks to see if the answer is one of the options for
	 * multiselect, select, checkbox, and radio type questions.
	 * 
	 * @param answer
	 *            The question answer
	 * @param questionAnswers
	 *            List of valid answers. Could be empty list.
	 * @return The form Document object with answers
	 */
	public static boolean isAnswerValid(String answer, List questionAnswers) {
		if (answer == null) {
			return false;
		}

		boolean valid = false;
		if (questionAnswers != null && !questionAnswers.isEmpty()) {

			for (Iterator iter = questionAnswers.iterator(); iter.hasNext();) {
				String questionAnswer = ((Answer) iter.next()).getDisplay();
				if (questionAnswer == null) {
					continue;
				}

				if (answer.trim().equalsIgnoreCase(questionAnswer.trim())) {
					valid = true;
					break;
				}
			}
		} else {

			valid = true;
		}

		return valid;
	}

	public static boolean isOptionValid(String answer, List<ImageMapOption> options) {
		if (answer == null) {
			return false;
		}

		boolean valid = false;
		
		if (options != null && !options.isEmpty()) {
			for (Iterator<ImageMapOption> iter = options.iterator(); iter.hasNext();) {
				String questionAnswer = iter.next().getOption();
				
				if (questionAnswer == null) {
					break;
				}

				if (answer.trim().equalsIgnoreCase(questionAnswer.trim())) {
					valid = true;
					break;
				}
			}
		}
		else {
			valid = true;
		}

		return valid;
	}

	public static boolean isItOther(String answer, List<Answer> questionAnswers) {
		if (answer.equals(CtdbConstants.OTHER_OPTION_DISPLAY)) {
			return true;
		}
		
		if(answer.trim().equals("")) {
			return false;
		}
		
		boolean valid = true;
		
		if (questionAnswers != null && !questionAnswers.isEmpty()) {
			for (Iterator<Answer> iter = questionAnswers.iterator(); iter.hasNext();) {
				String questionAnswer = iter.next().getDisplay();
				
				if (questionAnswer == null) {
					continue;
				}

				if (answer.trim().equalsIgnoreCase(questionAnswer.trim())) {
					valid = false;
					break;
				}
			}
		}
		else {
			valid = true;
		}

		return valid;
	}

	public static String whichOther(String answer, List questionAnswers) {
		if (answer == null) {
			return "";
		}

		String ans = "";
		if (questionAnswers != null && !questionAnswers.isEmpty()) {

			for (Iterator iter = questionAnswers.iterator(); iter.hasNext();) {
				String questionAnswer = ((Answer) iter.next()).getDisplay();
				if (questionAnswer == null) {
					continue;
				}

				if (answer.trim().equalsIgnoreCase(questionAnswer.trim())) {
					ans = questionAnswer.trim();
					break;
				}
			}
		} else {

			ans = "";
		}

		return ans;
	}

	// add by sunny
	public static String textToUnicode(String s) {
		String as[] = new String[s.length()];
		String String = "";
		for (int i = 0; i < s.length(); i++) {

			char c = s.charAt(i);
			if ((int) c < 256) {
				String += c;
			} else {
				as[i] = Integer.toHexString(c & 0xffff);
				String unicode = "\\u" + as[i];
				String += unicode;
			}
		}
		// System.out.println(String);
		return String;

	}
}

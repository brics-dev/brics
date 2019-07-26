package gov.nih.nichd.ctdb.form.action;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.Version;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.form.common.FormQuestionAttributesAssembler;
import gov.nih.nichd.ctdb.form.domain.CalculatedFormQuestionAttributes;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.domain.FormExportImport;
import gov.nih.nichd.ctdb.form.domain.FormQuestionAttributes;
import gov.nih.nichd.ctdb.form.domain.Section;
import gov.nih.nichd.ctdb.form.form.XmlImportForm;
import gov.nih.nichd.ctdb.form.manager.FormManager;
import gov.nih.nichd.ctdb.form.util.FormDataStructureUtility;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.question.common.QuestionAssembler;
import gov.nih.nichd.ctdb.question.domain.Answer;
import gov.nih.nichd.ctdb.question.domain.ImageMapExportImport;
import gov.nih.nichd.ctdb.question.domain.ImageMapOption;
import gov.nih.nichd.ctdb.question.domain.ImageMapQuestion;
import gov.nih.nichd.ctdb.question.domain.ImageMapValuesExportImport;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.domain.QuestionExportImport;
import gov.nih.nichd.ctdb.question.domain.QuestionGraphic;
import gov.nih.nichd.ctdb.question.domain.QuestionImage;
import gov.nih.nichd.ctdb.question.domain.QuestionOptionsExportImport;
import gov.nih.nichd.ctdb.question.domain.QuestionType;
import gov.nih.nichd.ctdb.question.domain.VisualScale;
import gov.nih.nichd.ctdb.question.domain.VisualScaleExportImport;
import gov.nih.nichd.ctdb.question.manager.QuestionManager;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.util.common.Message;
import gov.nih.nichd.ctdb.util.common.MessageHandler;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;

public class ImportXmlFormAction extends BaseAction {
	private static final long serialVersionUID = 437838769250405484L;
	private static final Logger logger = Logger.getLogger(ImportXmlFormAction.class);
	
	public static final String ACTION_MESSAGES_KEY = "ImportFormAction_ActionMessages";
    private XmlImportForm xmlImportForm = new XmlImportForm();

    @SuppressWarnings("unchecked")
	public String importXmlForm () throws Exception
    {
		buildLeftNav(LeftNavController.LEFTNAV_FORM_IMPORT);
		User user = getUser();
        try {
            Form xmlForm = null;
    		List<Question> qList = null;
    		HashSet<String> requiredQHashSet = new HashSet<String>();
            	// 1. objects already created and saved in session
            if(session.get(CtdbConstants.FORM_XML_IMPORT) != null){
            	xmlForm = (Form )session.get(CtdbConstants.FORM_XML_IMPORT);
            	if(session.get(CtdbConstants.QUESTIONS_XML_IMPORT) != null){
            		qList= (List <Question>) session.get(CtdbConstants.QUESTIONS_XML_IMPORT);
            	}
            }
            else{ // 1 parsing from file
            	File importedFile = xmlImportForm.getDocument();
            	if(importedFile == null || importedFile.length() == 0 || xmlImportForm.getDocumentFileName().isEmpty() ){
        			addActionError(getText(StrutsConstants.ERROR_FILEUPLOAD_NOTFOUND, new String[]{""}) ); // the file name may be null
        			return StrutsConstants.FAILURE;
            	}
            	try {
            		JAXBContext jaxbContext = JAXBContext.newInstance(FormExportImport.class);
            		Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            		// Get form FormExportImport Object
            		// FormExportImport fe = (FormExportImport) jaxbUnmarshaller.unmarshal(importedFile.getInputStream());
            		FormExportImport fe = (FormExportImport) jaxbUnmarshaller.unmarshal(importedFile);
            		
            		boolean isLegacy = fe.getFormInfo().isLegacy();
            		if(isLegacy) {
            			addActionError(getText("errors.form.importXML.legacy", new String[]{ xmlImportForm.getDocumentFileName() }));
                		return StrutsConstants.EXCEPTION;
            		}
            		
            		
            		//call method to import form from XML	
            		xmlForm = FormQuestionAttributesAssembler.createFormObjectFromXML(request, fe);
            		qList = getQuestionListFromXML(request, fe);
            	} catch (JAXBException e) {
            		e.printStackTrace();
            		addActionError(getText("errors.form.importXML.parsing", new String[]{ xmlImportForm.getDocumentFileName() }));
            		return StrutsConstants.EXCEPTION;
            	}
            	catch (NullPointerException e) {
            		e.printStackTrace();
            		addActionError(getText("errors.form.importXML.nullPointer", new String[]{ xmlImportForm.getDocumentFileName() }));
            		return StrutsConstants.EXCEPTION;
            	}
            	catch (CtdbException e) {
            		e.printStackTrace();
            		addActionError(getText("errors.form.importXML.exceptions", new String[]{ xmlImportForm.getDocumentFileName(), e.getMessage() }));
            		return StrutsConstants.EXCEPTION;
            	}
                catch(Exception e)
                {
            		e.printStackTrace();
                	addActionError(getText("errors.form.importXML.exceptions", new String[]{ xmlImportForm.getDocumentFileName(), e.getMessage() }));
            		return StrutsConstants.EXCEPTION;
                }
            }

            // update form & question names if needed
            updateFormToDomain(xmlImportForm, xmlForm, qList);

            
            List<List<Section>> sectionTable = xmlForm.getRowList();
    		for ( List<Section> row : xmlForm.getRowList() ) {
    			for ( Section sec : row ) {
    				for ( Question q : sec.getQuestionList() ) {
    					boolean isRequired = q.getFormQuestionAttributes().isRequired();
    					if(isRequired) {
    						String deName = q.getFormQuestionAttributes().getDataElementName();
    						System.out.println(deName); 
    						requiredQHashSet.add(deName);
    					}
    				}
    			}
    		}
            
            int requiredQHashSetSize = requiredQHashSet.size();

            // validate  (or re-evaluate ) question and form names and other fields
            Boolean isValid  = validateForm(xmlForm, requiredQHashSetSize, xmlImportForm, request);

            if(isValid){
            	session.remove(CtdbConstants.FORM_XML_IMPORT);
            	session.remove(CtdbConstants.QUESTIONS_XML_IMPORT);
            	saveForm(xmlForm, qList, user);

            	// TODO: Below is the STRUTS2 message redirected to another page through session. Need to update later
            	addActionMessage(getText(StrutsConstants.SUCCESS_ADD_KEY, new String[]{"form " + xmlForm.getName()}));
            	
            	
            	String dsName = xmlForm.getDataStructureName();
            	FormDataStructureUtility fsUtil = new FormDataStructureUtility();
            	FormStructure fs = fsUtil.getDataStructureFromWebService(dsName, request);
        		boolean doesFSContainDeprecatedOrRetiredDEs = fsUtil.doesFSContainDeprecatedOrRetiredDEs(fs);

        		if(doesFSContainDeprecatedOrRetiredDEs) {
        			MessageHandler messageHandler = new MessageHandler(request);
        			messageHandler.addMessage(new Message(getText(StrutsConstants.CONTAINS_DEPRECATED_OR_RETIRED_DES, Arrays.asList("")),  MessageHandler.MESSAGE_TYPE_WARNING));
        			messageHandler.save(request);
        		}
            	
            	session.put(ACTION_MESSAGES_KEY, getActionMessages());
            	return SUCCESS;
            }
            else{
            	request.getSession().setAttribute(CtdbConstants.FORM_XML_IMPORT, xmlForm);
            	request.getSession().setAttribute(CtdbConstants.QUESTIONS_XML_IMPORT, qList);
            	if(xmlImportForm.isFormNameError()){
            		//msgCenter.addMessage("The name for the imported form already exists in the system", MessageHandler.MESSAGE_TYPE_ERROR);
            		addActionError(getText("errors.form.importXML.duplicate.formname"));
            	}
            	if(xmlImportForm.isFormStructureDataElementsError()){
            		addActionError(getText("errors.form.importXML.formstructure.null.or.dataelementss.mismatch"));
            	}
            	
            	return StrutsConstants.FAILURE;
            }	    		
        }
        catch(CtdbException e)
        {
    		e.printStackTrace();
        	addActionError(getText("errors.form.importXML.exceptions", new String[]{ xmlImportForm.getDocumentFileName(), e.getMessage() }));
    		return StrutsConstants.EXCEPTION;
        }
        catch(Exception e)
        {
    		e.printStackTrace();
        	addActionError(getText("errors.form.importXML.exceptions", new String[]{ xmlImportForm.getDocumentFileName(), e.getMessage() }));
    		return StrutsConstants.EXCEPTION;
        }

    }
    
	public Boolean validateForm(Form xmlForm, int requiredQHashSetSize, XmlImportForm importForm, HttpServletRequest request)
			throws DuplicateObjectException, CtdbException, NoRouteToHostException, UnknownHostException, RuntimeException {
		FormDataStructureUtility fsUtil = new FormDataStructureUtility();
		FormManager fm = new FormManager();
        Protocol currentProtocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		if(fm.isNameExist(xmlForm.getName(), currentProtocol.getId()) ){
			importForm.setExistingFormName(xmlForm.getName());
			importForm.setNewFormName(xmlForm.getName());
			importForm.setFormNameError(true);
		}
		else{
			importForm.setFormNameError(false);
		}
		
		boolean fsValidaton   = fsUtil.doesThisDataStructureExistInBricsAndHaveSameNumOfReqDEs(request, xmlForm.getDataStructureName(), requiredQHashSetSize);
		if(fsValidaton){
			importForm.setFormStructureDataElementsError(false);
		}else{
			importForm.setFormStructureDataElementsError(true);
		}
		
		return  !importForm.isFormNameError() && !importForm.isFormStructureDataElementsError();
	}
	
	public List <Question> getQuestionListFromXML(HttpServletRequest request, FormExportImport fe) 
			throws DuplicateObjectException, CtdbException, IOException {
		String imageFolderPath = request.getServletContext().getRealPath(SysPropUtil.getProperty("filesystem.directory.questionimagepath"));
		List<QuestionExportImport> qeList = fe.getQuestion();

		User user = getUser();
		List<Question> questionList = new ArrayList<Question>();
		
		// Iterate over questions and get values set in question objects
		List<QuestionOptionsExportImport> qoAnswersList = null;
		List<QuestionGraphic> qgList = null;
		Question q = null;
		
		for ( QuestionExportImport qe : qeList ) {
			List<Answer> ansList = new ArrayList<Answer>();
			QuestionType qType = QuestionType.getByValue(qe.getType());
			q = QuestionAssembler.createQuestion(qType);
			String questionName = qe.getName();
			q.setId(qe.getId());
			q.setName(questionName);
			q.setText(qe.getText());
			q.setDescriptionUp(qe.getDescriptionUp());
			q.setDescriptionDown(qe.getDescriptionDown());
			q.setDefaultValue(qe.getDefaultValue());
			q.setUnansweredValue(qe.getUnansweredValue());
			q.setHtmltext(qe.getTextBlockHtmlText());
			q.setType(QuestionType.getByValue(qe.getType()));
			q.setCopyRight(qe.getCopyRight());
			q.setCreatedBy(user.getId());
			q.setUpdatedBy(user.getId());
			q.setVersion(new Version(1));
			q.setQuestionOrder(qe.getQuestionOrder());
			q.setQuestionOrderCol(qe.getQuestionOrderCol());
			
			//Generate Question Answers Options list only for SELECT, MULTI-SELECT, RADIO, CHECKBOX
			if (q.getType().equals(QuestionType.CHECKBOX) || q.getType().equals(QuestionType.MULTI_SELECT)
					|| q.getType().equals(QuestionType.SELECT) || q.getType().equals(QuestionType.RADIO)) {
				qoAnswersList = qe.getQuestionOptions();
				
				for ( QuestionOptionsExportImport qo : qoAnswersList ) {
					Answer a = new Answer();
					a.setScore(qo.getScore());
					a.setSubmittedValue(qo.getSubmittedValue());
					a.setDisplay(qo.getDisplay());
					ansList.add(a);
				}
			}
			
			if( ansList.size() > 0 ) {
				q.setAnswers(ansList);
			}
			
			// DECODING Question Graphics back to physical disk
			qgList = qe.getQuestionsGraphics();
			QuestionImage qImg = new QuestionImage();
			ArrayList<String> questionImagesNames = new ArrayList<String>();
			ArrayList<BufferedImage> buffImgList = new ArrayList<BufferedImage>();
			
			for ( QuestionGraphic qg : qgList ) {
				byte[] bytes = Base64.decodeBase64(qg.getQuestionGraphic().getBytes());
				BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
				questionImagesNames.add(qg.getFileName());
				buffImgList.add(image);
			}
			
			// Set a Question Id,buffImgList list and question names list to QuestionImage Object to call updateQuestionImagesForExportImport method
			qImg.setNames(questionImagesNames);
			qImg.setBufferedImage(buffImgList);
			qImg.setPath(imageFolderPath);
			q.setImages(questionImagesNames);
			q.setImageHolder(qImg);
			
			// To link the graphicsQuesiton to the form
			if (q.getType().equals(QuestionType.VISUAL_SCALE)) {
				// Recreate Visual Scale Question in imported form
				VisualScale vs = (VisualScale) q;
				VisualScaleExportImport vse = qe.getVisualScaleParameters();
				vs.setCenterText(vse.getCenterText());
				vs.setRightText(vse.getRightText());
				vs.setLeftText(vse.getLeftText());
				vs.setRangeStart(vse.getRangeStart());
				vs.setRangeEnd(vse.getRangeEnd());
				vs.setWidth(vse.getWidth());
				vs.setShowHandle(vse.isShowHandle());
			}

			if (q.getType().equals(QuestionType.IMAGE_MAP)) {
				// Recreate Image Map Question in imported Form
				ImageMapQuestion imageMapQuest = (ImageMapQuestion) q;
				ImageMapExportImport imExport = qe.getImageMap();
				imageMapQuest.setGridResolution(imExport.getGridResolution());
				imageMapQuest.setHeight( String.valueOf(imExport.getHeight()) );
				imageMapQuest.setWidth( String.valueOf(imExport.getWidth()) );
				imageMapQuest.setShowGrid(imExport.isShowGrid());
				// TODO The line below is inserting the wrong type in the list. Please fix.
				//imageMapQuest.setOptions(imExport.getiMapValues());

				// recreate image map question passing the ImageMapExportImport
				qImg = new QuestionImage();
				qImg.setPath(imageFolderPath);
				qImg.setNames( new ArrayList<String>() );
				qImg.setBufferedImage( new ArrayList<BufferedImage>() );
				qImg.getNames().add(imExport.getImageFileName());
				imageMapQuest.setImageFileName(imExport.getImageFileName());

				byte[] bytes = Base64.decodeBase64(imExport.getMapGraphic().getQuestionGraphic().getBytes());
				BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
				qImg.getBufferedImage().add(image);
				imageMapQuest.setMapHolder(qImg);
				
				//Set imageMap option list
				List<ImageMapValuesExportImport> imageMapValList = imExport.getiMapValues();
				
				if ( imageMapValList != null && !imageMapValList.isEmpty() ) {
					List <ImageMapOption> options  = new ArrayList<ImageMapOption>();
					for ( ImageMapValuesExportImport vei : imageMapValList ) {
						ImageMapOption imo = new ImageMapOption();
						imo.setOption(vei.getImageOption());

						HashMap <String, List<String>> coordinates = new HashMap<String, List<String>>();
						List<String> cols = new ArrayList<String>();
						cols.add(String.valueOf(vei.getImageMapColumn() ));
						coordinates.put(String.valueOf(vei.getImageMapRow()),cols );
						imo.setCoordinates(coordinates);
						options.add(imo);
					}
					
					imageMapQuest.setOptions(options);
				}
			}
			
			questionList.add(q);
		}
		return questionList;
	}
	
	public void saveForm(Form f, List <Question> qList, User user) throws DuplicateObjectException, CtdbException {
		HashMap<Integer, Integer> oldNewSectionIdsMap = new HashMap<Integer, Integer>();
		HashMap<Integer, Integer> oldNewQuestionIdsMap = new HashMap<Integer, Integer>();
		FormManager fm = new FormManager();
		QuestionManager qm = new QuestionManager();
		fm.createForm(f);
		int tmpId;
		int newFormId  = f.getId();
		List<List<Section>> sectionTable = f.getRowList();
		
		// Create all sections
		// Create layout for each section
		for ( List<Section> row : sectionTable ) {
			for ( Section sec : row ) {
				if ( sec != null ) {
					sec.setFormId(newFormId);
					
					// For repeated sections, the parent Section is already created first, so its new ID should be in the section name hashmap
					if ( sec.getRepeatedSectionParent() != -1 ) {
						if ( oldNewSectionIdsMap.containsKey(new Integer((sec.getRepeatedSectionParent()))) ) {
							sec.setRepeatedSectionParent( oldNewSectionIdsMap.get( new Integer(sec.getRepeatedSectionParent())) );
						}
						else {
							addActionError(getText("errors.form.importXML.repdsectparent", 
									new String[]{ Integer.toString(sec.getRepeatedSectionParent()) }));
						}
					}
					
					tmpId = sec.getId();
					fm.addRowInFormLayoutTableForGivenFormId(newFormId, sec.getRow(), sec.getCol());
					fm.createSection(sec);
					oldNewSectionIdsMap.put(tmpId, sec.getId());
				}
			}
		}

		// Create the unique question set
		for ( Question q : qList ) {
			tmpId = q.getId();
			
			// Must overwrite the lagacy qid before saving because DAO is not already increasing SEQ_number to get a new GID
			q.setId(Integer.MIN_VALUE);
			//Strip the first two letter from question name and append with new form id
			String newQuestionNameFormat = String.valueOf(newFormId)+q.getName().substring(q.getName().indexOf("_"));
			q.setName(newQuestionNameFormat);
			qm.createQuestion(q);
			
            // save images held in the image holder: move it here because the current question creation set autocommit to false,
			// and the new question ID is needed for inserting data to questionimage table
            if(q.getImageHolder() !=null && q.getImageHolder().getNames().size() > 0){
            	qm.updateQuestionImagesForExportImport(q.getImageHolder(), "Imported_Question_image_");
            }

			oldNewQuestionIdsMap.put(tmpId, q.getId());
		}
		
		// update all legacy section & Question IDs to the newly created ID
		updateBogusSectionQuestionIds(f, oldNewSectionIdsMap, oldNewQuestionIdsMap);
		
		//Save Section Question Section 
		FormQuestionAttributes formQuestionAttr = new  FormQuestionAttributes();
		formQuestionAttr.setCreatedBy(user.getId());
		formQuestionAttr.setUpdatedBy(user.getId());
		
		for ( List<Section> row : sectionTable ) {
			for ( Section sec : row ) {
				fm.addQuestionsToSection(sec.getId(), sec.getQustionIdStr(), formQuestionAttr, sec.getQustionRows(), sec.getQustionCols());
				fm.updateQuestionAttributes(sec.getQuestionList(), newFormId);
			}
		}
	}
	
	public void updateBogusSectionQuestionIds(Form f, HashMap<Integer, Integer> oldNewSectionIdsMap, HashMap<Integer, Integer> oldNewQuestionIdsMap) {
		if ( (f == null) || f.getRowList().isEmpty() ) {
			return;
		}
		
		for ( List<Section> row : f.getRowList() ) {
			for ( Section sec : row ) {
				for ( Question q : sec.getQuestionList() ) {
					int newQId = oldNewQuestionIdsMap.get( q.getId()).intValue();
					q.setId( newQId );
					q.setSectionId(sec.getId());
					q.getFormQuestionAttributes().setQuestionId(newQId);
					q.getFormQuestionAttributes().setSectionId(sec.getId());
					
					// Update CalculationRule and Skipped Questions 
					if ( q.getFormQuestionAttributes().isCalculatedQuestion() ) {
						this.updateImporteFomula(q.getCalculatedFormQuestionAttributes(), oldNewSectionIdsMap, oldNewQuestionIdsMap);
					}
	
					if( q.getFormQuestionAttributes().getQuestionsToSkip().size() > 0 ) {
						for(Question tmpQ : q.getFormQuestionAttributes().getQuestionsToSkip() ) {
							int newId = oldNewQuestionIdsMap.get(tmpQ.getId()).intValue();
							
							tmpQ.setId(newId);
							newId = oldNewSectionIdsMap.get(tmpQ.getSectionId()).intValue();
							tmpQ.setSectionId(newId);
						}
					}
					
					//Required to wipe out legacy email ID before creating a new one
					q.getFormQuestionAttributes().getEmailTrigger().setId(Integer.MIN_VALUE);
				}
			}
		}
	}

	public void updateImporteFomula(CalculatedFormQuestionAttributes attr, HashMap<Integer, Integer> sectionMap, HashMap<Integer, Integer> questionMap){
		String formula  = attr.getCalculation();
		if(formula == null || formula.isEmpty()){
			return;
		}
		ArrayList <String> secQeustionTokens = new ArrayList<String>();
		Pattern pattern = Pattern.compile("(\\[S_(\\d+)_Q_(\\d+)\\])");
		String newFormula = new String(formula);
		Matcher m = pattern.matcher(formula);
		
		while ( m.find() ) {
			String oldToken = m.group(1);
			String secId = m.group(2);
			String qId = m.group(3);
			String newToken = "[S_" + sectionMap.get( Integer.valueOf(secId) ) + "_Q_" + questionMap.get( Integer.valueOf(qId) ) + "]";;
			newFormula = newFormula.replace(oldToken, newToken);
			secQeustionTokens.add(newToken);
		}
		
		attr.setCalculation(newFormula);
	}

	public void updateFormToDomain(XmlImportForm importForm, Form xmlForm, List <Question> qList) {
		// update Form name if needed
		if (!Utils.isBlank(importForm.getNewFormName())){
			xmlForm.setName(importForm.getNewFormName().trim());
		}
		
		// Question List
		HashMap <String, String> newQuestionNames = new HashMap<String, String>();
		
		try {
			if (!Utils.isBlank(importForm.getNewNames())){
				JSONArray qArr = new JSONArray(importForm.getNewNames());
				
				// convert string to HashMap; loop all questions to update
				for (int i = 0; i < qArr.length(); i++) {
					JSONObject namePair = qArr.getJSONObject(i);
					if (!namePair.getString("value").isEmpty() ){
						newQuestionNames.put(namePair.getString("name"), namePair.getString("value").trim());
					}
				}
			}
		}
		catch ( JSONException e ) {
			logger.error("JSON parsing error.", e);
		}
		
		// update Question names
		if ( qList != null ) {
			for( Question q : qList ) {
				if(newQuestionNames.containsKey(q.getName()) ){
					q.setName(newQuestionNames.get(q.getName()) );
				}
			}
		}
	}

	public XmlImportForm getXmlImportForm() {
		return xmlImportForm;
	}

	public void setXmlImportForm(XmlImportForm xmlImportForm) {
		this.xmlImportForm = xmlImportForm;
	}
	
    public String displayImportForm() throws Exception {
		buildLeftNav(LeftNavController.LEFTNAV_FORM_IMPORT);
		
		if (Utils.isBlank(xmlImportForm.getAction()) || xmlImportForm.getAction().equalsIgnoreCase(StrutsConstants.FORM)) {
			// clean up the session
            session.remove(CtdbConstants.FORM_XML_IMPORT);
    		session.remove(CtdbConstants.QUESTIONS_XML_IMPORT);
            xmlImportForm.setAction(StrutsConstants.ACTION_PROCESS_IMPORT);
        }
		
		return SUCCESS;
    }
}

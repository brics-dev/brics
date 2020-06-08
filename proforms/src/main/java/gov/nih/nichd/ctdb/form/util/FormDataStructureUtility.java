package gov.nih.nichd.ctdb.form.util;

import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.json.JSONArray;

import gov.nih.nichd.ctdb.btris.domain.BtrisObject;
import gov.nih.nichd.ctdb.common.CasProxyTicketException;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.HtmlAttributes;
import gov.nih.nichd.ctdb.common.Version;
import gov.nih.nichd.ctdb.emailtrigger.domain.EmailTrigger;
import gov.nih.nichd.ctdb.form.common.FormHtmlAttributes;
import gov.nih.nichd.ctdb.form.domain.CalculatedFormQuestionAttributes;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.domain.FormQuestionAttributes;
import gov.nih.nichd.ctdb.form.domain.Section;
import gov.nih.nichd.ctdb.form.manager.FormManager;
import gov.nih.nichd.ctdb.question.domain.Answer;
import gov.nih.nichd.ctdb.question.domain.AnswerType;
import gov.nih.nichd.ctdb.question.domain.ConversionFactor;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.domain.QuestionType;
import gov.nih.nichd.ctdb.question.domain.SkipRuleOperatorType;
import gov.nih.nichd.ctdb.question.domain.SkipRuleType;
import gov.nih.nichd.ctdb.question.domain.VisualScale;
import gov.nih.nichd.ctdb.response.domain.DataEntryWorkflowType;
import gov.nih.nichd.ctdb.security.util.SecuritySessionUtil;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.DataStructureList;
import gov.nih.tbi.dictionary.model.EformPfCategory;
import gov.nih.tbi.dictionary.model.EformRestServiceModel.BasicEformList;
import gov.nih.tbi.dictionary.model.EformRestServiceModel.EformList;
import gov.nih.tbi.dictionary.model.hibernate.BtrisMapping;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.dictionary.model.hibernate.StructuralDataElement;
import gov.nih.tbi.dictionary.model.hibernate.eform.BasicEform;
import gov.nih.tbi.dictionary.model.hibernate.eform.Eform;
import gov.nih.tbi.dictionary.model.hibernate.eform.EmailTriggerValue;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionAnswerOption;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionDocument;
import gov.nih.tbi.dictionary.model.hibernate.eform.SectionQuestion;

public class FormDataStructureUtility {
	
	private static final Logger logger = Logger.getLogger(FormDataStructureUtility.class);
	
	public FormDataStructureUtility() {}
	
	/**
	 * Gets Brics Eform from Brics using shortname and converts to Proforms Form
	 * @param request
	 * @param shortName
	 * @return
	 * @throws CasProxyTicketException
	 * @throws WebApplicationException
	 */
	public Form getEformFromBrics(HttpServletRequest request, String shortName) throws CasProxyTicketException, WebApplicationException {
		Form form = null;
		String restfulDomain = SysPropUtil.getProperty("webservice.restful.ddt.domain");
		String restfulUrl = restfulDomain + SysPropUtil.getProperty("webservice.restful.eform.ddt.url")+ "/"  + shortName;
		Client client = ClientBuilder.newClient();
		WebTarget wt = client.target(restfulUrl);
		
		try {
			SecuritySessionUtil ssu = new SecuritySessionUtil(request);
			String proxyTicket = ssu.getProxyTicket(restfulDomain);
			
			if ( proxyTicket != null ) {
				wt = wt.queryParam("ticket", proxyTicket);
			}
			else {
				logger.error("Proxy ticket from CAS is null!");
			}
		}
		catch (IOException | RuntimeException e) {
			String message = "Couldn't create a proxy ticket for " + wt.getUri().toString() + ".";
			logger.error(message);
			throw new CasProxyTicketException(message, e);
		}
        
        EformList eformList = wt.request(MediaType.APPLICATION_XML, MediaType.TEXT_XML).get(EformList.class);
        List<Eform> eforms = eformList.getList();
        Eform eform = eforms.get(0);
	    form = transformBricsEform(eform);

		return form;
	}
	
	
	
	
	/**
	 * Gets the BasicEform list given list of short names
	 * @param request
	 * @param shortNames
	 * @return
	 * @throws CasProxyTicketException
	 * @throws WebApplicationException
	 */
	public List<BasicEform> getBasicEforms(HttpServletRequest request, List <String> shortNames) throws CasProxyTicketException, WebApplicationException {
		String restfulDomain = SysPropUtil.getProperty("webservice.restful.ddt.domain");
		String restfulUrl = restfulDomain + SysPropUtil.getProperty("webservice.restful.eform.ddt.url")+ "/getBasicEForms";
		
		Client client = ClientBuilder.newClient();
		WebTarget wt = client.target(restfulUrl);

		JSONArray arr = new JSONArray(shortNames);
		Entity<String> entity = Entity.entity(arr.toString(), MediaType.APPLICATION_JSON);
		BasicEformList basicEformList = wt.request(MediaType.APPLICATION_XML, MediaType.TEXT_XML).post(entity, BasicEformList.class);
		List<BasicEform> basicEforms = basicEformList.getList();
		
		return basicEforms;
	}
	
	public List<Form> getEformsForShortnamesFromBrics(HttpServletRequest request, String shortNames) throws CasProxyTicketException, WebApplicationException {
		
		System.out.println("shortnames: "+shortNames);
		List<Form> formList = new ArrayList<Form>();
		String restfulDomain = SysPropUtil.getProperty("webservice.restful.ddt.domain");
		String restfulUrl = restfulDomain + SysPropUtil.getProperty("webservice.restful.eform.ddt.url")+ "/geteForms";
		Client client = ClientBuilder.newClient();
		WebTarget wt = client.target(restfulUrl);
		
		try {
			SecuritySessionUtil ssu = new SecuritySessionUtil(request);
			String proxyTicket = ssu.getProxyTicket(restfulDomain);
			
			if ( proxyTicket != null ) {
				wt = wt.queryParam("ticket", proxyTicket);
			}
			else {
				logger.error("Proxy ticket from CAS is null!");
			}
		}
		catch (IOException | RuntimeException e) {
			String message = "Couldn't create a proxy ticket for " + wt.getUri().toString() + ".";
			logger.error(message);
			throw new CasProxyTicketException(message, e);
		}
		Entity<String> entity = Entity.entity(shortNames, MediaType.APPLICATION_JSON);
        EformList eformList = wt.request(MediaType.APPLICATION_XML, MediaType.TEXT_XML).post(entity, EformList.class);
		List<Eform> eforms = eformList.getList();
        for(Eform eform: eforms) {
			formList.add(transformBricsEform(eform));
		}
		return formList;
	}

	/**
	 * Gets Brics Eform from Brics using shortname and converts to Proforms Form
	 * @param request
	 * @param shortName
	 * @return
	 * @throws CasProxyTicketException
	 * @throws WebApplicationException
	 */
	public String getEformXmlFromBrics(HttpServletRequest request, String shortName) throws CasProxyTicketException, WebApplicationException {
		String restfulDomain = SysPropUtil.getProperty("webservice.restful.ddt.domain");
		String restfulUrl = restfulDomain + SysPropUtil.getProperty("webservice.restful.eform.ddt.url")+ "/"  + shortName;
		Client client = ClientBuilder.newClient();
		WebTarget wt = client.target(restfulUrl);
		
		try {
			SecuritySessionUtil ssu = new SecuritySessionUtil(request);
			String proxyTicket = ssu.getProxyTicket(restfulDomain);
			
			if ( proxyTicket != null ) {
				wt = wt.queryParam("ticket", proxyTicket);
			}
			else {
				logger.error("Proxy ticket from CAS is null!");
			}
		}
		catch (IOException | RuntimeException e) {
			String message = "Couldn't create a proxy ticket for " + wt.getUri().toString() + ".";
			logger.error(message);
			throw new CasProxyTicketException(message, e);
		}
        
        String eformList = wt.request(MediaType.APPLICATION_XML, MediaType.TEXT_XML).get(String.class);
		return eformList;
	}
	
	/**
	 * Converter of Brics Eform to Proforms Form...this is the REAL one
	 * @param eform
	 * @return
	 */
	public Form transformBricsEform(Eform eform) {
		Form form = new Form();
		HashMap<String,Question> questionMap = new HashMap<String,Question>();
		HashMap<Integer,Section> sectionMap = new HashMap<Integer,Section>();
		Set<String> calcRuleQuestions = new HashSet<String>();
		Set<String> calcRuleDependentQuestions = new HashSet<String>();
		Set<String> skipRuleQuestions = new HashSet<String>();
		Set<String> skipRuleDependentQuestions = new HashSet<String>();
		ArrayList<Section> orderedSectionList = new ArrayList<Section>();
		//this will get done after form is returned
		////////////////////////////////form.setId(rs.getInt("formid"));
		HashMap<String, Question> btrisQuestionMap = new HashMap<String, Question>();

		String name = eform.getTitle();
		form.setName(name);
		
		String shortName = eform.getShortName();
		form.setShortName(shortName); 
		
		String description = eform.getDescription();
		form.setDescription(description);
		
		CtdbLookup status = new CtdbLookup(3,shortName, null);
		form.setStatus(status);
		
		// added by Ching-Heng
		boolean isCAT = eform.getIsCAT().booleanValue();
		form.setCAT(isCAT);
		
		String measurementType = eform.getMeasurementType();
		form.setMeasurementType(measurementType);
		
		String catOid = eform.getCatOid();
		form.setCatOid(catOid);
		//this will get done after form is returned
		/////////////////////////////////////////form.setProtocolId(rs.getInt("protocolid"));

		form.setSingleDoubleKeyFlag(CtdbConstants.SINGLE_ENTRY_FORM);

		boolean allowMultipleInstances = eform.isAllowMultipleCollectionInstances();
		form.setAllowMultipleCollectionInstances(allowMultipleInstances);

		String dataStructureName = eform.getFormStructureShortName();
		form.setDataStructureName(dataStructureName);
		
		EformPfCategory pfCategory = eform.getPfCategory();
		form.setPfCategory(pfCategory);

		FormHtmlAttributes formHtmlAttributes = new FormHtmlAttributes();

		boolean formBorder = eform.getFormBorder().booleanValue();
		formHtmlAttributes.setFormBorder(formBorder);

		boolean sectionBorder = eform.getSectionBorder().booleanValue();
		formHtmlAttributes.setSectionBorder(sectionBorder);
		
		String formNameFont = eform.getFormNameFont();
		formHtmlAttributes.setFormFont(formNameFont);
		
		String formNameColor = eform.getFormNameColor();
		formHtmlAttributes.setFormColor(formNameColor);

		String sectionNameFont = eform.getSectionNameFont();
		formHtmlAttributes.setSectionFont(sectionNameFont);
		
        String sectionNameColor = eform.getSectionNameColor();
		formHtmlAttributes.setSectionColor(sectionNameColor);
		
		int formFontSize = eform.getFontSize().intValue();
		formHtmlAttributes.setFormFontSize(formFontSize);
		
		int cellPadding = eform.getCellPadding().intValue();
		//int cellPadding = 1;
		formHtmlAttributes.setCellpadding(cellPadding);

		form.setFormHtmlAttributes(formHtmlAttributes);

		String header = eform.getHeader();
		if(header == null) {
			header = "";
		}
		form.setFormHeader(header);
		
		String footer = eform.getFooter();
		if(footer == null) {
			footer = "";
		}
		form.setFormFooter(footer);
		boolean legacy = false;
		if(eform.getIsLegacy() == null) {
			legacy = false;
		}else {
			legacy = eform.getIsLegacy();
		}
		form.setLegacy(legacy);
		//int dataentryworkflowtype = eform.getDataEntryWorkFlowType().intValue();
		//int dataentryworkflowtype = 1;
		form.setDataEntryWorkflow(DataEntryWorkflowType.EXPRESS);

		//form.setAttachFiles(Boolean.parseBoolean(rs.getString("attachfiles")));
		
		//form.setTabDisplay(Boolean.parseBoolean(rs.getString("tabdisplay")));

		Set<gov.nih.tbi.dictionary.model.hibernate.eform.Section> eformSectionSet = eform.getSectionList();
		  List<gov.nih.tbi.dictionary.model.hibernate.eform.Section> eformSectionList = 
				  new ArrayList<gov.nih.tbi.dictionary.model.hibernate.eform.Section>(eformSectionSet);
			Collections.sort(eformSectionList, new Comparator<gov.nih.tbi.dictionary.model.hibernate.eform.Section>() {

				@Override
				public int compare(gov.nih.tbi.dictionary.model.hibernate.eform.Section s1, gov.nih.tbi.dictionary.model.hibernate.eform.Section s2) {
					//return s1.getFormRow().compareTo(s2.getFormRow());
					if(s1.getFormRow() == s2.getFormRow()) {
						return s1.getFormCol().compareTo(s2.getFormCol());
					}else {
						return s1.getFormRow().compareTo(s2.getFormRow());
					}
				}
			});

		List<List<Section>> sections = new ArrayList<List<Section>>();
		int currentRowIndex = 1;
		int orderVal = 1;
		List<Section> row = new ArrayList<Section>();
		int numQuestions = 0;
		for(gov.nih.tbi.dictionary.model.hibernate.eform.Section eformSection:eformSectionList ) {

			Section proformsSection = new Section();
			
			int sectionId = eformSection.getId().intValue();
			proformsSection.setId(sectionId);
			proformsSection.setIdText("S_" + sectionId);

			String sectionName = eformSection.getName();
			proformsSection.setName(sectionName);
			
			String sectionDescription = eformSection.getDescription();
			proformsSection.setDescription(sectionDescription);
			
			//int orderVal = eformSection.getOrderVal().intValue();
			proformsSection.setOrderValue(orderVal);
			
			int formRow = eformSection.getFormRow().intValue();
			proformsSection.setRow(formRow);
			
			int formCol = eformSection.getFormCol().intValue();
			proformsSection.setCol(formCol);
			
			boolean suppressFlag = true;
			proformsSection.setTextDisplayed(suppressFlag);
			
			String label = eformSection.getLabel();
			if(label == null) {
				label = "";
			}
			proformsSection.setInstructionalText(label);

			boolean intob = false;
			proformsSection.setIntob(intob);

			boolean isCollapsable = eformSection.getCollapsable().booleanValue();
			proformsSection.setCollapsable(isCollapsable);
			
			boolean isResponseImage= false;
			proformsSection.setResponseImage(isResponseImage);
			
			boolean isRepeatable = eformSection.getIsRepeatable().booleanValue();
			proformsSection.setRepeatable(isRepeatable);
			
			int initialRepeatedSections = eformSection.getInitialRepeatedSections().intValue();
			proformsSection.setInitRepeatedSections(initialRepeatedSections);
			
			int maxRepeatedSections = eformSection.getMaxRepeatedSections().intValue();
			proformsSection.setMaxRepeatedSections(maxRepeatedSections); 
			
			int repeatedSectionParent = -1;
			if(eformSection.getRepeatedSectionParent() != null) {
				repeatedSectionParent = eformSection.getRepeatedSectionParent().intValue();
			}
			proformsSection.setRepeatedSectionParent(repeatedSectionParent);

			String sectionGroupName = eformSection.getGroupName();
			if (sectionGroupName == null || sectionGroupName.equals("")) { 
				proformsSection.setRepeatableGroupName("None");
			}
			else {
				proformsSection.setRepeatableGroupName(sectionGroupName);
			}

			String altLabel = eformSection.getAltLabel();
			if(altLabel == null) {
				altLabel = "";
			}
			proformsSection.setAltLabel(altLabel);

			Set<SectionQuestion> eformSectionQuestionSet = eformSection.getSectionQuestion();
			
			  List<gov.nih.tbi.dictionary.model.hibernate.eform.SectionQuestion> eformSectionQuestionList = 
					  new ArrayList<gov.nih.tbi.dictionary.model.hibernate.eform.SectionQuestion>(eformSectionQuestionSet);
				Collections.sort(eformSectionQuestionList, new Comparator<gov.nih.tbi.dictionary.model.hibernate.eform.SectionQuestion>() {

					@Override
					public int compare(gov.nih.tbi.dictionary.model.hibernate.eform.SectionQuestion sq1, gov.nih.tbi.dictionary.model.hibernate.eform.SectionQuestion sq2) {

						if(sq1.getQuestionOrder() != sq2.getQuestionOrder()) {
							return sq1.getQuestionOrder().compareTo(sq2.getQuestionOrder());
						} else {
							return sq1.getQuestionOrderColumn().compareTo(sq2.getQuestionOrderColumn());
						}
						
					}

				});
			
			List<Question> qList = new ArrayList<Question>();
			//while(sqIter.hasNext()) {
			for(SectionQuestion eformSQ:eformSectionQuestionList){
				//SectionQuestion eformSQ = (SectionQuestion)sqIter.next();
				gov.nih.tbi.dictionary.model.hibernate.eform.Question eformQuestion = eformSQ.getQuestion();
				Question proformsQuestion = null;

				if(eformQuestion.getType().getValue() == QuestionType.VISUAL_SCALE.getValue()) {
					 proformsQuestion = new VisualScale();
					 gov.nih.tbi.dictionary.model.hibernate.eform.VisualScale eformVisualScale = eformQuestion.getVisualScale();
					 int width = eformVisualScale.getWidthMM().intValue();
					((VisualScale)proformsQuestion).setWidth(width);
					int rangeEnd = eformVisualScale.getEndRange().intValue();
					((VisualScale)proformsQuestion).setRangeEnd(rangeEnd);
					int rangeStart = eformVisualScale.getStartRange().intValue();
					((VisualScale)proformsQuestion).setRangeStart(rangeStart);
					String leftText = eformVisualScale.getLeftText();
					((VisualScale)proformsQuestion).setLeftText(leftText);
					String rightText = eformVisualScale.getRightText();
					((VisualScale)proformsQuestion).setRightText(rightText);
					String centerText = eformVisualScale.getCenterText();
					((VisualScale)proformsQuestion).setCenterText(centerText);
					boolean showHandle = eformVisualScale.getShowHandle().booleanValue();
					((VisualScale)proformsQuestion).setShowHandle(showHandle); 
				}else {
					proformsQuestion = new Question();
				}
				
				int questionId = eformQuestion.getId().intValue();
				proformsQuestion.setId(questionId);
				proformsQuestion.setIdText("S_" + sectionId + "_Q_" + questionId);
				
				proformsQuestion.setVersion(new Version(1));
				
				String questionName = eformQuestion.getName();
				proformsQuestion.setName(questionName);
				
				String text = eformQuestion.getText();
				proformsQuestion.setText(text);
				
				proformsQuestion.setTextDisplayed(true);

				int type = eformQuestion.getType().getValue();
				proformsQuestion.setType(QuestionType.getByValue(type));
				
				String defaultValue = eformQuestion.getDefaultValue();
				proformsQuestion.setDefaultValue(defaultValue);
				
				String unansweredValue = eformQuestion.getUnansweredValue();
				proformsQuestion.setUnansweredValue(unansweredValue);

				String descUp = eformQuestion.getDescriptionUp();
				proformsQuestion.setDescriptionUp(descUp);
				
				String descDown = eformQuestion.getDescriptionDown();
				proformsQuestion.setDescriptionDown(descDown);
		        
		        //added by Ching Heng
				boolean isIncludeOther = eformQuestion.getIncludeOther().booleanValue();
				proformsQuestion.setIncludeOtherOption(isIncludeOther);
				proformsQuestion.setParentSectionName(sectionName);
				proformsQuestion.setCatOid(eformQuestion.getCatOid());
				
				Boolean isDisplayPV = eformQuestion.getDisplayPV();
				proformsQuestion.setDisplayPV(isDisplayPV);
				
				//int copyright = eformQuestion.getCopyRight().intValue();
				int copyright = 0;
				proformsQuestion.setCopyRight(copyright);
				
				SortedSet<QuestionAnswerOption> orderedSectionQuestions = new TreeSet<QuestionAnswerOption>();
				orderedSectionQuestions.addAll(eformQuestion.getQuestionAnswerOption()); 
		     				
				List<Answer> answers = new ArrayList<Answer>();
				for(QuestionAnswerOption eformOption: orderedSectionQuestions){
					int answerId = eformOption.getId().intValue();
					Answer answer = new Answer();
			        answer.setId(answerId);
			        String display = eformOption.getDisplay();
			        answer.setPvd(display);
			        
			        if(eformQuestion.getDisplayPV()) {
			        	display = eformOption.getSubmittedValue();
			        }
			        
			        answer.setDisplay(display);
			        double score = eformOption.getScore();
			        answer.setScore(score);
			        String submittedValue = eformOption.getSubmittedValue();
			        if (submittedValue != null && !submittedValue.isEmpty()) {
			            answer.setSubmittedValue(submittedValue);
			            //added by Ching-Heng
			            answer.setCodeValue(submittedValue);	
			            answer.setItemResponseOid(eformOption.getItemResponseOid());
			        }
			        
			        answer.setIdText("S_" + sectionId + "_Q_" + questionId + "_" + answerId);
			        answers.add(answer);	
				}
				proformsQuestion.setAnswers(answers);

				//question images
				
		        List<String> imageFileNames = new ArrayList<String>();
		        Set<QuestionDocument> questionDocuments = eformQuestion.getQuestionDocument();
		        
		        if (questionDocuments != null) {
			        for (QuestionDocument eformQD : questionDocuments) {
			        	String questionImageName = eformQD.getUserFile().getName();
			        	imageFileNames.add(questionImageName);	
			        }
		        }
		        
		        proformsQuestion.setImages(imageFileNames); 
		        proformsQuestion.setSectionId(sectionId);
		        proformsQuestion.setParentSectionName(sectionName);
		        
		        int questionOrder = eformSQ.getQuestionOrder().intValue();
		        proformsQuestion.setQuestionOrder(questionOrder);
		        int questionOrderCol = eformSQ.getQuestionOrderColumn().intValue();
				proformsQuestion.setQuestionOrderCol(questionOrderCol);

				// BtrisMapping
				BtrisMapping eformBtrisMapping = eformQuestion.getBtrisMapping();
				if (eformBtrisMapping != null) {
					BtrisObject proformsBO = new BtrisObject();

					proformsBO.setBtrisObservationName(eformBtrisMapping.getBtrisObservationName());
					proformsBO.setBtrisSpecimenType(eformBtrisMapping.getBtrisSpecimenType());
					proformsBO.setBtrisRedCode(eformBtrisMapping.getBtrisRedCode());
					proformsBO.setBtrisTable(eformBtrisMapping.getBtrisTable());
					proformsQuestion.setBtrisObject(proformsBO);
					form.setHasBtrisMappingQuestion(true);
					String key = "S_" + sectionId + "_Q_" + questionId;
					btrisQuestionMap.put(key, proformsQuestion);
				}
		     
		        //question attributes
		        FormQuestionAttributes questionAttributes = new FormQuestionAttributes();
		        
		        gov.nih.tbi.dictionary.model.hibernate.eform.QuestionAttribute eformQuestionAttribute = eformQuestion.getQuestionAttribute();
		        boolean calculatedFlag = false;
		        boolean countFlag = false;
		        
		        if(eformQuestionAttribute.getCountFlag()!= null) {
		        	countFlag = eformQuestionAttribute.getCountFlag().booleanValue();
		        }
		        
		        if (countFlag == true) {

					calcRuleQuestions.add(proformsQuestion.getIdText());
					CalculatedFormQuestionAttributes calculatedFormQuestionAttributes = new CalculatedFormQuestionAttributes();
					//String calculation = eformQuestionAttribute.getCalculation();
					String countFormula = eformSQ.getCountFormula();
					calculatedFormQuestionAttributes.setCalculation(countFormula);
					calculatedFormQuestionAttributes.setIsCount(true);
					
					List<gov.nih.tbi.dictionary.model.hibernate.eform.CountQuestion> eformsCountQuestions = eformSQ.getCountQuestion();
					List<Question> questionsToCalculate = new ArrayList<Question>();
					
					for (gov.nih.tbi.dictionary.model.hibernate.eform.CountQuestion eformCountQuestion : eformsCountQuestions) {
						gov.nih.tbi.dictionary.model.hibernate.eform.CountQuestionPk eformCountQuestionPk = eformCountQuestion.getCountQuestionCompositePk();
						int eformCountSectionId = eformCountQuestionPk.getCountSection().getId().intValue();
						gov.nih.tbi.dictionary.model.hibernate.eform.Question eformQuestionUsedInCount = eformCountQuestionPk.getCountQuestion();
						int eformQuestionId = eformQuestionUsedInCount.getId().intValue();
						Question countQuestion = new Question();
						countQuestion.setSectionId(eformCountSectionId);
						countQuestion.setParentSectionName(sectionName);
						countQuestion.setId(eformQuestionId);
						countQuestion.setIdText("S_" + eformCountSectionId + "_Q_" + eformQuestionId); 
						countQuestion.setVersion(new Version(1));
						countQuestion.setName(eformQuestionUsedInCount.getName());
						countQuestion.setText(eformQuestionUsedInCount.getText());
						countQuestion.setType(QuestionType.getByValue(eformQuestionUsedInCount.getType().getValue()));

						countQuestion.getFormQuestionAttributes().setAnswerType(AnswerType.getByValue(eformQuestionUsedInCount.getQuestionAttribute().getAnswerType().getValue()));
						calcRuleDependentQuestions.add(countQuestion.getIdText());
						questionsToCalculate.add(countQuestion);						
						
					}
					calculatedFormQuestionAttributes.setAnswerType(AnswerType.getByValue(eformQuestionAttribute.getAnswerType().getValue()));
					calculatedFormQuestionAttributes.setQuestionsToCalculate(questionsToCalculate);
					int dtConversionFactor = eformQuestionAttribute.getDtConversionFactor().intValue();
					calculatedFormQuestionAttributes.setConversionFactor(ConversionFactor.getByValue(dtConversionFactor));
					proformsQuestion.setCalculatedFormQuestionAttributes(calculatedFormQuestionAttributes);
					calculatedFormQuestionAttributes.setConditionalForCalc(eformQuestionAttribute.getConditionalForCalc());
				}
		        
		        
		        
		        
		        if(eformQuestionAttribute.getCalculatedFlag() != null) {
		        	calculatedFlag = eformQuestionAttribute.getCalculatedFlag().booleanValue();
		        }
		        if (calculatedFlag == true) {

					calcRuleQuestions.add(proformsQuestion.getIdText());
					CalculatedFormQuestionAttributes calculatedFormQuestionAttributes = new CalculatedFormQuestionAttributes();
					//String calculation = eformQuestionAttribute.getCalculation();
					String calculation = eformSQ.getCalculation();
					calculatedFormQuestionAttributes.setCalculation(calculation);
					
					List<gov.nih.tbi.dictionary.model.hibernate.eform.CalculationQuestion> eformsCalculatedQuestions = eformSQ.getCalculatedQuestion();
					List<Question> questionsToCalculate = new ArrayList<Question>();
					
					for (gov.nih.tbi.dictionary.model.hibernate.eform.CalculationQuestion eformCalcQuestion : eformsCalculatedQuestions) {
						gov.nih.tbi.dictionary.model.hibernate.eform.CalculationQuestionPk eformCalculationQuestionPk = 
								eformCalcQuestion.getCalculationQuestionCompositePk();
						int eformCalcSectionId = eformCalculationQuestionPk.getCalculationSection().getId().intValue();
						gov.nih.tbi.dictionary.model.hibernate.eform.Question eformQuestionUsedInCalc = eformCalculationQuestionPk.getCalculationQuestion();
						int eformQuestionId = eformQuestionUsedInCalc.getId().intValue();
						Question calcQuestion = new Question();
						calcQuestion.setSectionId(eformCalcSectionId);
						calcQuestion.setParentSectionName(sectionName);
						calcQuestion.setId(eformQuestionId);
						calcQuestion.setIdText("S_" + eformCalcSectionId + "_Q_" + eformQuestionId); 
						calcQuestion.setVersion(new Version(1));
						calcQuestion.setName(eformQuestionUsedInCalc.getName());
						calcQuestion.setText(eformQuestionUsedInCalc.getText());
						calcQuestion.setType(QuestionType.getByValue(eformQuestionUsedInCalc.getType().getValue()));

						List<Answer> calcAnswers = new ArrayList<Answer>();
						if(eformQuestionUsedInCalc.getQuestionAnswerOption() != null && !eformQuestionUsedInCalc.getQuestionAnswerOption().isEmpty()){
							
							SortedSet<QuestionAnswerOption> calcAnswer = new TreeSet<QuestionAnswerOption>();
							calcAnswer.addAll(eformQuestionUsedInCalc.getQuestionAnswerOption()); 
							
							for(QuestionAnswerOption eformCalcOption: calcAnswer){
	                            
                                //int answerId = eformCalcOption.getId().intValue();
                                Answer answer = new Answer();
                                //answer.setId(answerId);
                            
                            
                                String display = eformCalcOption.getDisplay();
                                answer.setPvd(display);
                            
                                if(eformQuestionUsedInCalc.getDisplayPV()) {
                                    display = eformCalcOption.getSubmittedValue();
                                }
                            
                                answer.setDisplay(display);
                                
                                double score = eformCalcOption.getScore();
                                answer.setScore(score);
                                String submittedValue = eformCalcOption.getSubmittedValue();
                                if (submittedValue != null && !submittedValue.isEmpty()) {
                                    answer.setSubmittedValue(submittedValue);
                                }
                                calcAnswers.add(answer);    
                            }
						}
						calcQuestion.setAnswers(calcAnswers); 
						calcQuestion.getFormQuestionAttributes().setAnswerType(AnswerType.getByValue(eformQuestionUsedInCalc.getQuestionAttribute().getAnswerType().getValue()));
						calcRuleDependentQuestions.add(calcQuestion.getIdText());
						questionsToCalculate.add(calcQuestion);						
						
					}
					calculatedFormQuestionAttributes.setAnswerType(AnswerType.getByValue(eformQuestionAttribute.getAnswerType().getValue()));
					calculatedFormQuestionAttributes.setQuestionsToCalculate(questionsToCalculate);
					int dtConversionFactor = eformQuestionAttribute.getDtConversionFactor().intValue();
					calculatedFormQuestionAttributes.setConversionFactor(ConversionFactor.getByValue(dtConversionFactor));
					proformsQuestion.setCalculatedFormQuestionAttributes(calculatedFormQuestionAttributes);
					calculatedFormQuestionAttributes.setConditionalForCalc(eformQuestionAttribute.getConditionalForCalc());
				}
		        
		        String dataElementName = eformQuestionAttribute.getDataElementName();
		        if (dataElementName == null || dataElementName.equals("")) {
 					questionAttributes.setDataElementName("none");
				} else {
					 questionAttributes.setDataElementName(dataElementName);
				}
		        
		        //setting the form structure repeatable group name 
		        if (eformQuestionAttribute.getGroupName() == null || eformQuestionAttribute.getGroupName().equals("")) {
 					questionAttributes.setRepeatableGroupName("none");
				} else {
					 questionAttributes.setRepeatableGroupName(eformQuestionAttribute.getGroupName());
				}
		        
				//prepopulation
		        boolean prepopulation = false;
		        if(eformQuestionAttribute.getPrepopulation() != null) {
		        	prepopulation = eformQuestionAttribute.getPrepopulation().booleanValue();
		        }
				questionAttributes.setPrepopulation(prepopulation);
				
				if(prepopulation) {
					String prepopulationValue = eformQuestionAttribute.getPrepopulationValue();
					questionAttributes.setPrepopulationValue(prepopulationValue);
				}
				else {
					questionAttributes.setPrepopulationValue("");
				}
				
				//decimalprecision
				int decimalPrecision = -1;
				if(eformQuestionAttribute.getDecimalPrecision() != null) {
					decimalPrecision = eformQuestionAttribute.getDecimalPrecision().intValue();
				}
				questionAttributes.setDecimalPrecision(decimalPrecision);
				
				//unit conversion factor
				boolean hasConversionFactor = false;
				if(eformQuestionAttribute.getHasConversionFactor() != null) {
					hasConversionFactor = eformQuestionAttribute.getHasConversionFactor().booleanValue();
				}
				questionAttributes.setHasUnitConversionFactor(hasConversionFactor);
				
				String conversionFactor = eformQuestionAttribute.getConversionFactor();
				questionAttributes.setUnitConversionFactor(conversionFactor);
				
				questionAttributes.setIsCalculatedQuestion(calculatedFlag);
				questionAttributes.setIsCountQuestion(countFlag);
				//int qaID = eformQuestionAttribute.getId().intValue();
				//questionAttributes.setId(qaID);
						
				questionAttributes.setQuestionId(questionId);
				questionAttributes.setVersion(new Version(1));
				
				boolean requiredFlag = false;
				if(eformQuestionAttribute.getRequiredFlag() != null) {
					requiredFlag = eformQuestionAttribute.getRequiredFlag().booleanValue();
				}
				questionAttributes.setRequired(requiredFlag);
				
				// Skip rule
				boolean skipRule = false;
				if(eformQuestionAttribute.getSkipRuleFlag() != null) {
					skipRule = eformQuestionAttribute.getSkipRuleFlag().booleanValue();
				}

				
				if (skipRule) {
					skipRuleQuestions.add(proformsQuestion.getIdText());
					int skipRuleType = eformQuestionAttribute.getSkipRuleType().getValue();
					questionAttributes.setSkipRuleType(SkipRuleType.getByValue(skipRuleType));
					int skipRuleOperatorType = eformQuestionAttribute.getSkipRuleOperatorType().getValue();
					questionAttributes.setSkipRuleOperatorType(SkipRuleOperatorType.getByValue(skipRuleOperatorType));
					String skipRuleEquals = eformQuestionAttribute.getSkipRuleEquals();
					questionAttributes.setSkipRuleEquals(skipRuleEquals);
					List<gov.nih.tbi.dictionary.model.hibernate.eform.SkipRuleQuestion> eformsSkipRuleQuestionQuestions = eformSQ.getSkipRuleQuestion();
					List<Question> questionsToSkip = new ArrayList<Question>();
					
					for (gov.nih.tbi.dictionary.model.hibernate.eform.SkipRuleQuestion eformSkipRuleQuestion : eformsSkipRuleQuestionQuestions) {
						gov.nih.tbi.dictionary.model.hibernate.eform.SkipRuleQuestionPk eformSkipRuleQuestionPK = 
								eformSkipRuleQuestion.getSkipRuleQuestionCompositePk();
						int eformSkipSectionId = eformSkipRuleQuestionPK.getSkipRuleSection().getId().intValue();
						gov.nih.tbi.dictionary.model.hibernate.eform.Question eformQuestionUsedInSkip = eformSkipRuleQuestionPK.getSkipRuleQuestion();

						int eformQuestionId = eformQuestionUsedInSkip.getId().intValue();
						Question skipQuestion = new Question();
						skipQuestion.setId(eformQuestionId);
						skipQuestion.setVersion(new Version(1));
						skipQuestion.setName(eformQuestionUsedInSkip.getName());
						skipQuestion.setText(eformQuestionUsedInSkip.getText());
						skipQuestion.setType(QuestionType.getByValue(eformQuestionUsedInSkip.getType().getValue()));
						skipQuestion.setSectionId(sectionId);
						
						skipQuestion.setParentSectionName(sectionName);
						skipQuestion.setSkipSectionId(eformSkipSectionId);
						skipQuestion.setIdText("S_" + eformSkipSectionId + "_Q_" + eformQuestionId);
						skipRuleDependentQuestions.add(skipQuestion.getIdText());
						questionsToSkip.add(skipQuestion);
					}

					questionAttributes.setQuestionsToSkip(questionsToSkip);
				}
				
				questionAttributes.setHasSkipRule(skipRule);
				
				// HTML Attributes
				HtmlAttributes attributes = new HtmlAttributes();
				
				String halign = eformQuestionAttribute.gethAlign();
				attributes.setAlign(halign);
				
				String valign = eformQuestionAttribute.getvAlign();
				attributes.setvAlign(valign);
				
				String textColor = eformQuestionAttribute.getTextColor();
				attributes.setColor(textColor);
				
				String fontFace = eformQuestionAttribute.getFontFace();
				attributes.setFontFace(fontFace);
				
				String fontSize = eformQuestionAttribute.getFontSize();
				attributes.setFontSize(fontSize);
				
				int indent = eformQuestionAttribute.getIndent().intValue();
				attributes.setIndent(indent);
				questionAttributes.setHtmlAttributes(attributes);
				
				String rangeOperator = eformQuestionAttribute.getRangeOperator();
				questionAttributes.setRangeOperator(rangeOperator);
				
				String rangeValue1 = eformQuestionAttribute.getRangeValue1();
				questionAttributes.setRangeValue1(rangeValue1);
				
				String rangeValue2 = eformQuestionAttribute.getRangeValue2();
				questionAttributes.setRangeValue2(rangeValue2);
				
				int answerType = eformQuestionAttribute.getAnswerType().getValue();
				questionAttributes.setAnswerType(AnswerType.getByValue(answerType));
				
				int minChars = eformQuestionAttribute.getMinCharacters().intValue();
				questionAttributes.setMinCharacters(minChars);
				
				int maxChars = eformQuestionAttribute.getMaxCharacters().intValue();
				questionAttributes.setMaxCharacters(maxChars);
				
				boolean horizDisplay = eformQuestionAttribute.getHorizontalDisplay().booleanValue();
				questionAttributes.setHorizontalDisplay(horizDisplay);
				
				boolean horizDisplayBreak = eformQuestionAttribute.getHorizontalDisplayBreak().booleanValue();
				questionAttributes.setHorizDisplayBreak(horizDisplayBreak);
				
				int textAreaWidth = eformQuestionAttribute.getTextBoxWidth().intValue();
				questionAttributes.setTextareaWidth(textAreaWidth);
				
				int textAreaHeight = eformQuestionAttribute.getTextBoxHeight().intValue();
				questionAttributes.setTextareaHeight(textAreaHeight);
				
				int textAreaLength = eformQuestionAttribute.getTextBoxLength().intValue();
				questionAttributes.setTextboxLength(textAreaLength);
				
				
				gov.nih.tbi.dictionary.model.hibernate.eform.EmailTrigger eformEmailTrigger = eformQuestionAttribute.getEmailTrigger();
				
				if (eformEmailTrigger != null) { 
					EmailTrigger et = new EmailTrigger();
					if (eformEmailTrigger.getId() != null) {
						et.setId(eformEmailTrigger.getId().intValue());
					}
					String toEmailAddress = eformEmailTrigger.getToEmailAddress();
					et.setToEmailAddress(toEmailAddress);
					String ccEmailAddress = eformEmailTrigger.getCcEmailAddress();
					et.setCcEmailAddress(ccEmailAddress);
					String emailSubject = eformEmailTrigger.getSubject();
					et.setSubject(emailSubject);
					String emailBody = eformEmailTrigger.getBody();
					et.setBody(emailBody);
					Set<EmailTriggerValue> emailTriggerValueSet = eformEmailTrigger.getTriggerValues();
					// List<String> triggerAnswers = new ArrayList<String>();
					Set<gov.nih.nichd.ctdb.emailtrigger.domain.EmailTriggerValue> emailTriggerValueSetPF =
							new HashSet<gov.nih.nichd.ctdb.emailtrigger.domain.EmailTriggerValue>();
					for (gov.nih.tbi.dictionary.model.hibernate.eform.EmailTriggerValue emailTriggerValueDD : emailTriggerValueSet) {
						gov.nih.nichd.ctdb.emailtrigger.domain.EmailTriggerValue emailTriggerValuePf =
								new gov.nih.nichd.ctdb.emailtrigger.domain.EmailTriggerValue(
										emailTriggerValueDD.getAnswer(), emailTriggerValueDD.getTriggerCondition());
						emailTriggerValueSetPF.add(emailTriggerValuePf);
					}
							
					// et.setTriggerAnswers(triggerAnswers);
					et.setTriggerValues(emailTriggerValueSetPF);
					questionAttributes.setEmailTrigger(et);
				}

				questionAttributes.setDataSpring(false);
				String xthmlText = eformQuestionAttribute.getXhtmlText();
				questionAttributes.setHtmlText(xthmlText);
				//proformsQuestion.setHtmltext(xthmlText);
				int tableHeaderType = eformQuestionAttribute.getTableHeaderType().intValue();
				questionAttributes.setTableHeaderType(tableHeaderType);
				
				boolean showText = eformQuestionAttribute.getShowText().booleanValue();
				
				questionAttributes.setShowText(showText);
				questionAttributes.setSectionId(sectionId); 
				questionAttributes.setInstanceType(proformsQuestion.getInstanceType());
				proformsQuestion.setFormQuestionAttributes(questionAttributes); 
				String key = "S_" + sectionId + "_Q_" + questionId;
				questionMap.put(key, proformsQuestion);
		        qList.add(proformsQuestion);
			}//end for each sectionquestion list
			
			

			Collections.sort(qList, new QuestionDisplayOrderComparator());
			

			
			proformsSection.setQuestionList(qList);
			
			//need to set flag indicating if this section has only required questions
			boolean hasAnyRequiredQuestions = false;
			Iterator<Question> qListIter = qList.iterator();
			while(qListIter.hasNext()) {
				Question qListQ = qListIter.next();
				boolean isReq = qListQ.getFormQuestionAttributes().isRequired();
				boolean hasSkip =  qListQ.getFormQuestionAttributes().hasSkipRule();
				if(isReq) {
					hasAnyRequiredQuestions = true;
					break;
				}
			}
			proformsSection.setHasAnyRequiredQuestions(hasAnyRequiredQuestions); 

			numQuestions = numQuestions + qList.size();
			
			if(formRow == currentRowIndex) {
				row.add(proformsSection);
				
			}else {
				sections.add(row); //handles previous row
				currentRowIndex = formRow;
				row = new ArrayList<Section>();
				row.add(proformsSection);
			}
			sectionMap.put(Integer.valueOf(sectionId), proformsSection); 
			orderedSectionList.add(proformsSection);
			orderVal++;
		}//end for each section

		//need following line to handle last row
		sections.add(row);
		form.setNumQuestions(numQuestions); 
		form.setRowList(sections);
	    form.setQuestionMap(questionMap); 
	    form.setSectionMap(sectionMap);
	    form.setOrderedSectionList(orderedSectionList);
	    form.setBtrisQuestionMap(btrisQuestionMap);
	    form.setCalcRuleQuestions(calcRuleQuestions);
	    form.setCalcRuleDependentQuestions(calcRuleDependentQuestions);
	    form.setSkipRuleQuestions(skipRuleQuestions);
	    form.setSkipRuleDependentQuestions(skipRuleDependentQuestions);
	    
		return form;
	}
	
	class QuestionDisplayOrderComparator implements Comparator<Question> {
		@Override
		/**
		 * Compares question 1 and question 2 to order them properly in
		 * page display order.
		 * 
		 * when comparing two questions in different sections, section
		 * order is the ONLY factor.  If the questions are in the same
		 * section, questionOrder is primary and questionOrderCol is
		 * secondary.
		 * 
		 * @param qOne the first question
		 * @param qTwo the second question
		 * @return a positive integer if the first question comes before the second, 
		 * a negative integer if the first question comes after the second, 
		 * or zero if they are the same (different sections)
		 */
		public int compare(Question qOne, Question qTwo) {
			if (qOne.getSectionId() != qTwo.getSectionId()) {
				// can't compare these two really, so give a zero
				return 0;
			}
			
			// rows different?
			if (qOne.getQuestionOrder() > qTwo.getQuestionOrder()) {
				return 1;
			}
			else if (qOne.getQuestionOrder() < qTwo.getQuestionOrder()) {
				return -1;
			}
			
			// otherwise, same row...cols different?
			if (qOne.getQuestionOrderCol() > qTwo.getQuestionOrderCol()) {
				return 1;
			}
			else if (qOne.getQuestionOrderCol() < qTwo.getQuestionOrderCol()) {
				return -1;
			}
			
			// fall back just in case
			return 0;
		}
		
	};
	
	public List<Integer> getRepeableSectionParentSectionIds(Form form) {
		
		HashMap<Integer,Section> sectionMap = form.getSectionMap();
		Set<Integer> sectionIdKeySet = sectionMap.keySet();
		Iterator<Integer> iter = sectionIdKeySet.iterator();
		List<Integer> results = new ArrayList<Integer>();
		while(iter.hasNext()) {
			Integer key = iter.next();
			Section section = sectionMap.get(key);
			boolean isRepeatable = section.isRepeatable();
			if (isRepeatable) {
				int repeatedSectionParent = section.getRepeatedSectionParent();
				if(repeatedSectionParent == -1) {
					int sectionId = section.getId();
					results.add(new Integer(sectionId));
				}	
			}
		}
		return results;
	}
	
	
	public List<Integer> getChildSectionIdsForParentSectionId(Form form, int repeatedSectionParentId) {
		HashMap<Integer,Section> sectionMap = form.getSectionMap();
		Set<Integer> sectionIdKeySet = sectionMap.keySet();
		Iterator<Integer> iter = sectionIdKeySet.iterator();
		List<Integer> results = new ArrayList<Integer>();
		while(iter.hasNext()) {
			Integer key = iter.next();
			Section section = sectionMap.get(key);
			boolean isRepeatable = section.isRepeatable();
			if (isRepeatable) {
				int repeatedSectionParent = section.getRepeatedSectionParent();
				if(repeatedSectionParent == repeatedSectionParentId) {
					int sectionId = section.getId();
					results.add(new Integer(sectionId));
				}	
			}
		}
		return results;
	}
	
	/**
	 * Gets a list of form structures from the data dictionary web service. Any archived or shared draft
	 * form structures that are returned from the web service will be removed from the final list.
	 * 
	 * @param request - The servlet request object, which is used to retrieve the proxy ticket from the CAS server.
	 * @return	A list of FormStructure objects or an empty list if there are no form structures given.
	 * @throws NoRouteToHostException
	 * @throws UnknownHostException
	 * @throws UniformInterfaceException
	 * @throws ClientHandlerException
	 */
	public List<FormStructure> getDataStructureListFromWebService(HttpServletRequest request) 
			throws NoRouteToHostException, UnknownHostException, RuntimeException {
		String restfulDomain = SysPropUtil.getProperty("webservice.restful.ddt.domain");
		String restfulUrl = restfulDomain + SysPropUtil.getProperty("webservice.restful.ddt.url");
		Client client = ClientBuilder.newClient();
		
		SecuritySessionUtil ssu = new SecuritySessionUtil(request);
		String proxyTicket = ssu.getProxyTicket(restfulDomain);
		
		if(proxyTicket != null) {
			//restfulUrl =  restfulUrl + SysPropUtil.getProperty("webservice.restful.alldatastructures.ddt.url");
			restfulUrl =  restfulUrl + SysPropUtil.getProperty("webservice.restful.publisheddatastructures.ddt.url");
			restfulUrl = ssu.compileProxiedWebserviceUrl(restfulUrl, proxyTicket);
		}
		else {
			restfulUrl =  restfulUrl + SysPropUtil.getProperty("webservice.restful.publisheddatastructures.ddt.url");
		}
		
		WebTarget target = client.target(restfulUrl);
		DataStructureList dataStructureList = target.request(MediaType.TEXT_XML).get(DataStructureList.class);
		List<FormStructure> dsList = new LinkedList<FormStructure>(dataStructureList.getList());
		
		// Remove any archived or shared draft form structures.
		for ( Iterator<FormStructure> iter = dsList.iterator(); iter.hasNext(); ) {
			FormStructure bds = iter.next();
			StatusType st = bds.getStatus();
			
			if ( st.name().equals(StatusType.ARCHIVED.name()) || st.name().equals(StatusType.SHARED_DRAFT.name()) ) {
				iter.remove();
			}
		}
		return dsList;
	}
	
	public FormStructure getDataStructureFromWebService(String dataStructureName, HttpServletRequest request) throws CtdbException, 
			UnknownHostException, NoRouteToHostException, WebApplicationException {
		String restfulDomain = SysPropUtil.getProperty("webservice.restful.ddt.domain");
		String restfulUrl = restfulDomain + SysPropUtil.getProperty("webservice.restful.ddt.url")+ "/"  + dataStructureName;
		SecuritySessionUtil ssu = new SecuritySessionUtil(request);
		String proxyTicket = ssu.getProxyTicket(restfulDomain);
		
		if ( proxyTicket != null ) {
			restfulUrl = ssu.compileProxiedWebserviceUrl(restfulUrl, proxyTicket);
		}

		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(restfulUrl);
		FormStructure ds = target.request(MediaType.TEXT_XML).get(FormStructure.class);
		
		return ds;
		
	}
	
	
	public String getQuestionGraphic(String questionid, String filename, HttpServletRequest request) throws RuntimeException, IOException, JAXBException  {
		String restfulDomain = SysPropUtil.getProperty("webservice.restful.ddt.domain");
		String restfulUrl = restfulDomain + SysPropUtil.getProperty("webservice.restful.eform.graphic.url1") + questionid + SysPropUtil.getProperty("webservice.restful.eform.graphic.url2") + filename;
		
		SecuritySessionUtil ssu = new SecuritySessionUtil(request);
		String proxyTicket = ssu.getProxyTicket(restfulDomain);
		
		if ( proxyTicket != null ) {
			restfulUrl = ssu.compileProxiedWebserviceUrl(restfulUrl, proxyTicket);
		}
		
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(restfulUrl);
		String image = target.request(MediaType.APPLICATION_JSON).get(String.class);
		return image;
	}
	
	/**
	 * Method to valiate whether form(data) structure exist in BRICS and how many data elements it contains otherwise we show validation error to user saying that it cannot be imported in new system
	 * @param request
	 * @param dataStructureName
	 * @param dataElmentSizeInXML
	 * @return valid or invalied form import based on data(form ) structre and data elements size associated to that form structure
	 * @throws NoRouteToHostException
	 * @throws UnknownHostException
	 * @throws RuntimeException
	 */
	public boolean doesThisDataStructureExistInBricsAndHaveSameNumOfReqDEs(HttpServletRequest request,String dataStructureName,int requiredDataElmentSizeInXML) throws NoRouteToHostException, UnknownHostException, RuntimeException{
		boolean existAndHasSameDataElementCount = true;
		String restfulDomain = SysPropUtil.getProperty("webservice.restful.ddt.domain");
		String restfulUrl = restfulDomain + SysPropUtil.getProperty("webservice.restful.ddt.url")+ "/"  + dataStructureName;
		SecuritySessionUtil ssu = new SecuritySessionUtil(request);
		String proxyTicket = ssu.getProxyTicket(restfulDomain);
		
		if ( proxyTicket != null ) {
			restfulUrl = ssu.compileProxiedWebserviceUrl(restfulUrl, proxyTicket);
		}

		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(restfulUrl);
		FormStructure ds =  null;
		try{
			ds = target.request(MediaType.TEXT_XML).get(FormStructure.class);
		}catch(Exception e){
			existAndHasSameDataElementCount = false;
			return existAndHasSameDataElementCount;
		}
		if(ds==null|| ds.equals("")){
			existAndHasSameDataElementCount = false;
			return existAndHasSameDataElementCount;
		}else if(!dataStructureName.equalsIgnoreCase(ds.getShortName())){
			existAndHasSameDataElementCount = false;
			return existAndHasSameDataElementCount;
		}

		HashSet<String>  fsRequiredDE = new HashSet<String>();
		for ( RepeatableGroup rg : ds.getRepeatableGroups() ) {
			String rgName = rg.getName();
			
			for ( MapElement me : rg.getDataElements() ) {
				StructuralDataElement de = me.getStructuralDataElement();
				String req = me.getRequiredType().name();
				
				if (req.equalsIgnoreCase("required")) {
					String deN = de.getName();
					fsRequiredDE.add(rgName + "." + deN);
				}
			}
		}
		
		int fsRequiredDESize = fsRequiredDE.size();
		//int dataElementSizeInFS = ds.getDataElements().size();
		if(fsRequiredDESize != requiredDataElmentSizeInXML){
			existAndHasSameDataElementCount = false;
			System.out.println("Mismatch in required DEs for import.");
			System.out.println("requiredDataElmentSizeInXML is " +  requiredDataElmentSizeInXML );
			System.out.println("fsRequiredDESize is " + fsRequiredDESize);
		}
		return existAndHasSameDataElementCount;
	}
	
	public boolean doesFSContainDeprecatedOrRetiredDEs(FormStructure ds) throws Exception {
		boolean doesContain = false;
		for ( RepeatableGroup rg : ds.getRepeatableGroups() ) {
			for ( MapElement me : rg.getDataElements() ) {
				StructuralDataElement de = me.getStructuralDataElement();
				DataElementStatus des = de.getStatus();
				if(des.equals(DataElementStatus.DEPRECATED) || des.equals(DataElementStatus.RETIRED)) {
					return true;
				}
			}
		}
		return doesContain;
	}
	
	public boolean validateRequiredDataElements (Form form, FormStructure ds) throws Exception {
		FormManager fm = new FormManager();
		ArrayList<String> requiredDataElementNames = new ArrayList<String>();
		ArrayList<String> formDataElementNames = new ArrayList<String>();

		for ( List<Section> row : form.getRowList() ) {
			for ( Section section : row ) {
				if (section != null) {
					int sectionId = section.getId();
					List<Question> questions = fm.getSectionQuestions(sectionId);
					
					for ( Question q : questions ) {
						String dataElementName = fm.getFormQuestionAttributes(sectionId, q.getId()).getDataElementName();
						formDataElementNames.add(dataElementName);
					}
				}
			}
		}
		
		for ( RepeatableGroup rg : ds.getRepeatableGroups() ) {
			String rgName = rg.getName();
			
			for ( MapElement me : rg.getDataElements() ) {
				StructuralDataElement de = me.getStructuralDataElement();
				String req = me.getRequiredType().name();
				
				if (req.equalsIgnoreCase("required")) {
					String deN = de.getName();
					requiredDataElementNames.add(rgName + "." + deN);
				}
			}
		}
		
		boolean success2 = true;
		
		for ( String rName : requiredDataElementNames ) {
			boolean match = false;
			
			for ( String fName : formDataElementNames ) {
				if ( fName.equals(rName) ) { 
					match = true;
					break;
				}
			}

			if (match == false) {
				success2 = false;
				break;
			}
		}
		
		return success2;
	}
}

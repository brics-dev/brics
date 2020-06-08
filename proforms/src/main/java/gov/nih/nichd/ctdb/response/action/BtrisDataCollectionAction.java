package gov.nih.nichd.ctdb.response.action;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gov.nih.nichd.ctdb.btris.domain.BtrisObject;
import gov.nih.nichd.ctdb.btris.manager.BtrisManager;
import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.domain.Section;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.response.common.ResponseConstants;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;
import gov.nih.nichd.ctdb.response.domain.MappedBtrisQuestion;
import gov.nih.nichd.ctdb.response.tag.MappedBtrisQuestionIdtDecorator;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;

public class BtrisDataCollectionAction extends BaseAction {

	private static final long serialVersionUID = 5390466769911136426L;
	
	private static final Logger logger = Logger.getLogger(BtrisDataCollectionAction.class);

	private String jsonString = CtdbConstants.EMPTY_JSON_ARRAY_STR;
	private String errRespMsg;

	public String getMappedBtrisQuestionDTList() throws CtdbException, JSONException {
		List<MappedBtrisQuestion> mappedQList = new ArrayList<MappedBtrisQuestion>();
		AdministeredForm aform = (AdministeredForm) session.get(ResponseConstants.AFORM_SESSION_KEY);
		if (aform != null) {
			Form form = aform.getForm();
			HashMap<String, Question> btrisQuestionMap = form.getBtrisQuestionMap();
			HashMap<Integer, Section> sectionMap = form.getSectionMap();

			for (HashMap.Entry<String, Question> qEntry : btrisQuestionMap.entrySet()) {
				String sectionQuestionId = qEntry.getKey();
				Question q = qEntry.getValue();

				int sectionId = Integer.parseInt(sectionQuestionId.split("_")[1]);
				Section sec = sectionMap.get(sectionId);
				MappedBtrisQuestion mappedQ = new MappedBtrisQuestion();
				mappedQ.setSectionQuestionId(sectionQuestionId);
				mappedQ.setSectionName(sec.getName());
				mappedQ.setQuestionName(q.getText());
				mappedQList.add(mappedQ);
			}
		}
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<MappedBtrisQuestion> outputList = new ArrayList<MappedBtrisQuestion>(mappedQList);
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new MappedBtrisQuestionIdtDecorator());
			idt.output();

		} catch (InvalidColumnException e) {
			e.printStackTrace();
		}

		return null;
	}

	public String getBtrisDataForSelectedQ() throws IOException, CtdbException, JSONException {
		String sectionQuestionIds = request.getParameter("sectionQuestionIds");
		List<String> sectionQuestionIdList = Arrays.asList(sectionQuestionIds.split(","));

		AdministeredForm aform = (AdministeredForm) session.get(ResponseConstants.AFORM_SESSION_KEY);
		Form form = aform.getForm();
		HashMap<String, Question> questionMap = form.getQuestionMap();
		HashMap<Integer, Section> sectionMap = form.getSectionMap();
		String mrn = aform.getPatient().getMrn();

		HashMap<String, Question> selectedQMap = new HashMap<String, Question>();
		for (String sectionQuestionId : sectionQuestionIdList) {
			Question q = questionMap.get(sectionQuestionId);
			selectedQMap.put(sectionQuestionId, q);
		
		}

		BtrisManager bm = new BtrisManager();
		HashMap<String, BtrisObject> rtnBOMap = bm.getBtrisDataByQuestions(mrn, selectedQMap);

		if (rtnBOMap == null) {
			String errMsg = "Fail to get BTRIS data for patient "+ mrn;
			logger.error(errMsg);
			errRespMsg = getText(StrutsConstants.ERROR_DATABASE_GET, new String[]{errMsg});
			addActionError(errRespMsg);
			return ERROR;
		}
		
		HashMap<String, BtrisObject> relatedRtnBOMap = new HashMap<String, BtrisObject>();
		HashMap<String, Question> allRelatedQMap = new HashMap<String, Question>();		
		
		for (String mainSectionQuestionId : sectionQuestionIdList) {
			Question mainQ = questionMap.get(mainSectionQuestionId);
			String mainQDataElementName = mainQ.getFormQuestionAttributes().getDataElementName().toLowerCase();
			allRelatedQMap = selectBtrisRelatedQs(allRelatedQMap, mainQDataElementName,questionMap);
			for (HashMap.Entry<String, Question> selectedRelQues : allRelatedQMap.entrySet()) {
				Question selectedQues = selectedRelQues.getValue();
				relatedRtnBOMap.put(selectedRelQues.getKey(), selectedQues.getBtrisObject());
			}
		}
		
		fillBtrisDbData(rtnBOMap,relatedRtnBOMap);
		
		rtnBOMap.putAll(relatedRtnBOMap);
		JSONArray qBOJsonArr = new JSONArray();
		for (Entry<String, BtrisObject> boEntry : rtnBOMap.entrySet()) {
			
			String sectionQuestionId = boEntry.getKey();
			
			BtrisObject bo = boEntry.getValue();
			Question q = new Question();
			
			if (selectedQMap.containsKey(sectionQuestionId)) {
				q=selectedQMap.get(sectionQuestionId);
			}else {
				q=allRelatedQMap.get(sectionQuestionId);
			}
			
			int sectionId = Integer.parseInt(sectionQuestionId.split("_")[1]);
			Section sec = sectionMap.get(sectionId);

			JSONObject qBOJsonObj = new JSONObject();

			qBOJsonObj.put("sectionQuestionId", sectionQuestionId);
			qBOJsonObj.put("questionType", q.getType().getDispValue());

			String secName = sec.getName();
			qBOJsonObj.put("sectionName", secName);

			String qText = q.getText().replaceAll("\\<[^>]*>", ""); // remove html tags
			qBOJsonObj.put("questionText", qText);
				
			String qName = q.getName().toLowerCase();
			String dataEleName = q.getFormQuestionAttributes().getDataElementName().toLowerCase();
			
			if (selectedQMap.containsKey(sectionQuestionId)) {
				
					if (!bo.getBtrisValueText().isEmpty()) {
						qBOJsonObj.put("btrisValue", bo.getBtrisValueText());
					} else if (!bo.getBtrisValueNumeric().isEmpty()) {
						qBOJsonObj.put("btrisValue", bo.getBtrisValueNumeric());
					} else {
						qBOJsonObj.put("btrisValue", "");
					}
					
			}else if (allRelatedQMap.containsKey(sectionQuestionId)){
					
				if((qName.contains(CtdbConstants.BTRIS_DATA_ELEMENT_RANGE)) && (qName.contains(dataEleName)) ) {
					if (!bo.getBtrisRange().isEmpty()) {
						qBOJsonObj.put("btrisValue", bo.getBtrisRange());
					}else {
						qBOJsonObj.put("btrisValue", "");
					}
				}else if((qName.contains(CtdbConstants.BTRIS_DATA_ELEMENT_UNIT)) && (qName.contains(dataEleName)) ) {
					
					if (!bo.getBtrisUnitOfMeasure().isEmpty()) {
						qBOJsonObj.put("btrisValue", bo.getBtrisUnitOfMeasure());
					}else {
						qBOJsonObj.put("btrisValue", "");
					}
				}else if((qName.contains(CtdbConstants.BTRIS_DATA_ELEMENT_COMMENT)) && (qName.contains(dataEleName))) {
					if (!bo.getBtrisValueNameComment().isEmpty()) {
						qBOJsonObj.put("btrisValue", bo.getBtrisValueNameComment());
					}else {
						qBOJsonObj.put("btrisValue", "");
					}
				}else if((qName.contains(CtdbConstants.BTRIS_DATA_ELEMENT_DATE)) && (qName.contains(dataEleName))) {
					if (bo.getBtrisPrimaryDateTime() != null) {
						qBOJsonObj.put("btrisValue", bo.getBtrisPrimaryDateTime());
					}else {
						qBOJsonObj.put("btrisValue", "");
					}
				}
				
			}

			qBOJsonArr.put(qBOJsonObj);
		}
		
		jsonString = qBOJsonArr.toString();
		
		System.out.println("jsonString    :   "+jsonString);

		return BaseAction.SUCCESS;
	}
	
	private void fillBtrisDbData(HashMap<String, BtrisObject> rtnBOMap, HashMap<String, BtrisObject> relatedRtnBOMap) {
		AdministeredForm aform = (AdministeredForm) session.get(ResponseConstants.AFORM_SESSION_KEY);
		Form form = aform.getForm();
		HashMap<String, Question> questionMap = form.getQuestionMap();
		for (Entry<String, BtrisObject> rtnBOEntry : rtnBOMap.entrySet()) {
		for (Entry<String, BtrisObject> relatedRtnBOEntry : relatedRtnBOMap.entrySet()) {
				if((questionMap.get(relatedRtnBOEntry.getKey()).getName().contains(questionMap.get(rtnBOEntry.getKey()).getName()))) {
					relatedRtnBOEntry.setValue(rtnBOEntry.getValue());
				}
			}
		}
	}

	private HashMap<String, Question> selectBtrisRelatedQs(HashMap<String, Question> selectedRelQMap, String mainQDataElementName, HashMap<String, Question> questionMap) {
		for (Entry<String, Question> q : questionMap.entrySet()) {
			String qName = q.getValue().getName().toLowerCase();
			String secQuesId = "";
			if(qName.contains(mainQDataElementName)) {
				if(qName.contains(CtdbConstants.BTRIS_DATA_ELEMENT_UNIT) || 
						qName.contains(CtdbConstants.BTRIS_DATA_ELEMENT_COMMENT) ||
						qName.contains(CtdbConstants.BTRIS_DATA_ELEMENT_DATE) || 
						qName.contains(CtdbConstants.BTRIS_DATA_ELEMENT_RANGE)) {
					secQuesId = ("S_" + q.getValue().getSectionId() + "_Q_" + q.getValue().getId());
					selectedRelQMap.put(secQuesId, q.getValue());
				}
			}
		}
		
		return selectedRelQMap;
	}
	
	public String getJsonString() {
		return jsonString;
	}

	public void setJsonString(String jsonString) {
		this.jsonString = jsonString;
	}
	
	public String getErrRespMsg() {
		return errRespMsg;
	}
	
	public void setErrRespMsg(String errRespMsg) {
		this.errRespMsg = errRespMsg;
	}
	
}

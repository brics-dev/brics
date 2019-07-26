package gov.nih.nichd.ctdb.importdata.thread;


import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.domain.Section;
import gov.nih.nichd.ctdb.form.manager.FormManager;
import gov.nih.nichd.ctdb.patient.common.PatientResultControl;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.patient.manager.PatientManager;
import gov.nih.nichd.ctdb.question.domain.ImportQuestion;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.response.form.ImportHL7DataForm;
import gov.nih.nichd.ctdb.response.manager.ResponseManager;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

/**
 * Created by Ching Heng Lin
 * Date: 2012/12/26
 */
public class JsonDataSendingThread extends Thread {
	private static Logger logger = Logger.getLogger(JsonDataSendingThread.class);
	
	ImportHL7DataForm importForm;
	
    public JsonDataSendingThread (ImportHL7DataForm inForm) {
    	importForm = inForm;

    }

    public void run () {
    	try {
			FormManager fm = new FormManager();
			int formID = importForm.getTheFormId();
			Form theForm = fm.getFormAndSetofQuestions(formID);
			JSONObject formJsonObj = formToJson(theForm,importForm);
			sendJson(formJsonObj);  // web service client side
			logger.info("**" + formJsonObj.toString());
		}
    	catch (ObjectNotFoundException e) {
			logger.error("Could not found form.", e);
		}
    	catch (CtdbException e) {
			logger.error("Database error occurred.", e);
		}
    	catch (JSONException | RuntimeException e) {
			logger.error("Error occurred when constructing JSON object", e);
		}
    }
    
    public JSONObject formToJson(Form theForm,ImportHL7DataForm importForm) throws JSONException, CtdbException {
    	JSONObject jsonObj = new JSONObject();
        jsonObj.put("protocaoId", importForm.getProtocol().getId());
        jsonObj.put("userId", importForm.getUser().getId());
        jsonObj.put("theFormId", theForm.getId());
        jsonObj.put("intervalId", importForm.getIntervalId());
        jsonObj.put("importDate", SysPropUtil.getProperty("default.system.datetimeformat"));
        jsonObj.put("dataStructureName", theForm.getDataStructureName());
        jsonObj.put("dataStructureVersion", theForm.getDataStructureVersion());
        
        // get all import questions
        List<ImportQuestion> importQuestionList = new ArrayList<ImportQuestion>();
        
        for ( List<Section> row : theForm.getRowList() ) {
        	for ( Section sec : row ) { // each column (section)
        		if ( sec != null ){
        			for( Question question : sec.getQuestionList() ) {
        				ImportQuestion imQ = new ImportQuestion();
        				imQ.questionToImportQuestion(question);
        				importQuestionList.add(imQ);
        			}
        		}
        	}
        }
        
        // subjects and questions
        Map<String, List<ImportQuestion>> patientQuestionsMap = new HashMap<String, List<ImportQuestion>>();
        List<Integer> patientIdList = new ArrayList<Integer>();
        List<String> patientMRNList = new ArrayList<String>();
        PatientManager pm = new PatientManager();
        ResponseManager rm = new ResponseManager();
        if(importForm.isBySubject()){ // import by subject, so we have subject
        	Patient p=rm.getPatientForCollection(importForm.getProtocol().getId(), importForm.getSubjectId());
        	patientQuestionsMap.put(p.getSubjectId(), importQuestionList); // <--------------The best way is using the GUID, but now it's SubjectId
			patientIdList.add(p.getId());
			patientMRNList.add(p.getSubjectId());
        	jsonObj.put("patientQuestions", patientQuestionsMap);
            jsonObj.put("patientsId", patientIdList);
            jsonObj.put("patientsMRN", patientMRNList);
        }
        else { // import by form, get list of patients
            PatientResultControl prc = new PatientResultControl();
    		prc.setProtocolId(theForm.getProtocol().getId());
    		prc.setInProtocol(true);
    		prc.setActiveInProtocol("");
    		prc.setSortBy(PatientResultControl.SORT_BY_ORDERVAL);
    		prc.setSortOrder(PatientResultControl.SORT_ASC);
    		prc.setFormId(theForm.getId());
    		
    		for ( Patient p : pm.getMinimalPatients(prc) ) {
    			patientQuestionsMap.put(p.getSubjectId(),importQuestionList); // <--------------The best way is using the GUID, but now it's SubjectId
    			patientIdList.add(p.getId());
    			patientMRNList.add(p.getSubjectId());
    		}
    		
    		jsonObj.put("patientQuestions", patientQuestionsMap);
            jsonObj.put("patientsId", patientIdList);
            jsonObj.put("patientsMRN", patientMRNList);
        }
        
        // set data source
        jsonObj.put("dataSourceURL", importForm.getDataSourceURL());
        // set local IP address
        String url = "";
        
        try {
			InetAddress myIPaddress = InetAddress.getLocalHost();
			url = myIPaddress.getHostAddress() + ":8080/ibis/resource/HL7";
			jsonObj.put("responseURL", url);
		}
        catch (UnknownHostException e) {
			logger.warn("Could not connect to " + url + ".", e);
		}
        
        return jsonObj;
    }

    public void sendJson(JSONObject formJsonObj) throws JSONException, RuntimeException {
    	String url = formJsonObj.getString("dataSourceURL");
    	Client client = ClientBuilder.newClient();
    	WebTarget target = client.target(url);
    	javax.ws.rs.core.Form webForm = new javax.ws.rs.core.Form();
    	
    	webForm.param("importInfo", formJsonObj.toString());
    	Response response = target.request(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
    			.post(Entity.form(webForm));
    	
    	// Process response    	
    	if ( response.getStatus() != 200 ) {
    		throw new WebApplicationException("Could not send JSON.", response.getStatus());
    	}
    	
    	response.close();
    }
    
}

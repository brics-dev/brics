package gov.nih.nichd.ctdb.importdata.thread;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.form.manager.FormManager;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.patient.manager.PatientManager;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.manager.QuestionManager;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;
import gov.nih.nichd.ctdb.response.domain.Response;
import gov.nih.nichd.ctdb.response.manager.ResponseManager;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.security.manager.SecurityManager;

/**
 * Created by Ching Heng Lin
 * Date: 2012/12/26
 */
public class JsonDataImportingThread extends Thread {
	private static final Logger log = Logger.getLogger(JsonDataImportingThread.class);
	
	private JSONObject FormJsonObj;

	public JsonDataImportingThread (JSONObject formJsonObj) {
		FormJsonObj = formJsonObj;
	}

	@Override
	public void run () {
		try {
			jsonToDataCollection(FormJsonObj);
		}
		catch (Exception e) {
			log.error("Could not import form.", e);
		}
	}

	public void jsonToDataCollection(JSONObject formJsonObj) throws JSONException, ObjectNotFoundException, CtdbException, ParseException {	    	
		// manager objects
		ResponseManager rm = new ResponseManager();
		PatientManager pm = new PatientManager();
		FormManager fm = new FormManager();
		ProtocolManager protoMan = new ProtocolManager();
		SecurityManager sm = new SecurityManager();
		QuestionManager qm = new QuestionManager();

		//get protocol
		Protocol protocol = protoMan.getProtocol(formJsonObj.getInt("protocaoId"));
		//get user
		User user = sm.getUser(formJsonObj.getInt("userId"));
		// set administered form
		AdministeredForm aform = new AdministeredForm();
		aform.setForm(fm.getFormAndSetofQuestions(formJsonObj.getInt("theFormId")));
		aform.setInterval(protoMan.getInterval(formJsonObj.getInt("intervalId")));    	
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Date visitDate = df.parse(formJsonObj.getString("importDate"));
		aform.setVisitDate(visitDate);

		JSONArray patientsId = formJsonObj.getJSONArray("patientsId");
		JSONObject patientQuestions = formJsonObj.getJSONObject("patientQuestions");

		for (int i = 0; i < patientsId.length(); i++) { // each patient
			Patient p = rm.getPatientForCollection(protocol.getId(), patientsId.getInt(i));
			//Patient p = pm.getPatient(patientsId.getInt(i));
			aform.setPatient(p); // set different patient
			rm.create(aform, user); // create administered form
			// set response
			List<Response> responses = new ArrayList<Response>();
			JSONArray patientQuestion = patientQuestions.getJSONArray(p.getSubjectId());

			for (int j = 0; j < patientQuestion.length(); j++) { //get each patient's data by subject id
				JSONObject importDataJson = patientQuestion.getJSONObject(j);
				Response response = new Response();
				List<String> answers = new ArrayList<String>();
				String ans = importDataJson.getString("theAnswer");
				
				answers.add(ans);
				Question q = qm.getQuestion(importDataJson.getInt("importQuestionId"));
				q.setSectionId(importDataJson.getInt("sectionId"));
				response.setQuestion(q);
				response.setAdministeredForm(aform);
				response.setAnswers(answers);
				response.getAdministeredForm().getForm().setProtocol(protocol);
				responses.add(response);
				aform.setResponses(responses);
				rm.saveProgress(aform, user); // <------------------------------------create the answer
			}
		}	    	
	}
}

package gov.nih.nichd.ctdb.response.action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.form.common.FormConstants;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.importdata.thread.JsonDataSendingThread;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.patient.domain.PatientVisit;
import gov.nih.nichd.ctdb.patient.manager.PatientManager;
import gov.nih.nichd.ctdb.protocol.domain.Interval;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.response.form.ImportHL7DataForm;
import gov.nih.nichd.ctdb.response.manager.ResponseManager;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

/**
 * Created by Ching Heng Lin.
 * Date: Jan 28, 2013
 */
public class SendHL7DataBySubjectAction extends BaseAction {
	
	private static final long serialVersionUID = 8897969468335064839L;
	private ImportHL7DataForm importForm = new ImportHL7DataForm();
	
	public String execute()throws Exception {
        buildLeftNav(LeftNavController.LEFTNAV_COLLECT_COLLECT);
        
        Protocol currProtocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		User user = getUser();
        ProtocolManager protoMan = new ProtocolManager();
        PatientManager pm = new PatientManager();
        ResponseManager rm = new ResponseManager();
        if (importForm.getAction() == null){
        	String patientIdString = request.getParameter("patientVisitId");
        	int patientId = Integer.parseInt(patientIdString);
        	request.setAttribute("patientId", patientIdString);
			Patient p = rm.getPatientForCollection(currProtocol.getId(), patientId);
			
			request.setAttribute("patientDisplayLabel", p.getDisplayLabel(
					currProtocol.getPatientDisplayType(), currProtocol.getId()));
			PatientVisit pv = pm.getPatientVisit(patientId);
			//String patientRecordId = pv.getSubjectId();
			
			request.setAttribute("patientRecordId", p.getSubjectId());
			
			// get list of forms
			int intervalId = pv.getIntervalId();
			List<Form> activeForms = protoMan.getActiveFormsForInterval(intervalId);
			Map<String, String> activeFormsLabels = new HashMap<String, String>();
			
			for (Form f : activeForms) {
				activeFormsLabels.put(Integer.toString(f.getId()), f.getName());
			}
			if (activeFormsLabels.isEmpty()) {
				activeFormsLabels.put("noForms", "There are no active forms for this study yet");
			}
			session.put(FormConstants.ACTIVEFORMS, activeFormsLabels);
        	
			// get all intervals for protocol
			List<Interval> intervals = protoMan.getIntervals(currProtocol.getId());
			Map<String, String> intervalOptions = new HashMap<String, String>();

			for (Interval interv : intervals) {
				intervalOptions.put(Integer.toString(interv.getId()), interv.getName());
			}
			request.setAttribute("intervalOptions", intervalOptions);

			// set data source
			Map<String, String> dataSourceList = new HashMap<String, String>();
			for (int i=1; i<3; i++) { // just for demo, those two data source are the same
				dataSourceList.put("BRITS HL7 Web Service-"+i, SysPropUtil.getProperty("brits.hl7.web.service"));
			}
			request.setAttribute("dataSourceURLOptions", dataSourceList);

			// set action
			importForm.setAction(StrutsConstants.CONTINUE);
			return StrutsConstants.FORM;
			
        } else if (importForm.getAction().equalsIgnoreCase(StrutsConstants.CONTINUE)){
        	importForm.setProtocol(currProtocol);
        	importForm.setUser(user);
        	importForm.setBySubject(true);
        	JsonDataSendingThread jsonDataSendingThread = new JsonDataSendingThread(importForm);
        	jsonDataSendingThread.start();
        	
        	return SUCCESS;
        	
        } else {
        	// unknown action, send to error page
            return StrutsConstants.FAILURE;
        }
    }
}

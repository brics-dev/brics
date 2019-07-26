package gov.nih.nichd.ctdb.patient.action;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.ServletActionContext;
import org.json.JSONArray;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.patient.common.PatientVisitResultControl;
import gov.nih.nichd.ctdb.patient.domain.PatientVisit;
import gov.nih.nichd.ctdb.patient.manager.PatientManager;
import gov.nih.nichd.ctdb.patient.util.VisitJson;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;

public class PatientVisitsJSONAction extends BaseAction {
	private static final long serialVersionUID = 102074209684122074L;

	public String execute() throws Exception {
		PatientManager pm = new PatientManager();
		PatientVisitResultControl pvrc = new PatientVisitResultControl();
		
		String year = request.getParameter("year");
		String month = request.getParameter("month");
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.YEAR, Integer.decode(year));
		cal.set(Calendar.MONTH, Integer.decode(month));
		cal.set(Calendar.DAY_OF_MONTH, 1);
		pvrc.setStartDate(cal.getTime());
		cal.add(Calendar.MONTH, 1);
		pvrc.setEndDate(cal.getTime());
		
		Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		List<PatientVisit> visits = new ArrayList<PatientVisit>();
		if (protocol != null) {
			pvrc.setProtocolId(protocol.getId());
			visits = (List<PatientVisit>)pm.getMonthPatientVisits(pvrc);
			
		} else {
			// if the protocol is not explicitly set, we are in "All Studies" and we need to get the visits
			// only from the studies that this user has access to view
			List<Protocol> protocolsList = (List<Protocol>) session.get(CtdbConstants.USER_PROTOCOL_LIST);
			for (Protocol prot : protocolsList) {
				pvrc.setProtocolId(prot.getId());
				visits.addAll(pm.getMonthPatientVisits(pvrc));
			}
		}
		
		//List<JSONObject> jsonVisits = new ArrayList<JSONObject>();
		JSONArray jsonVisits = new JSONArray();
		for (PatientVisit visit : visits) {
			jsonVisits.put(VisitJson.fromPatientVisit(visit));
		}
		
		HttpServletResponse response = ServletActionContext.getResponse();
		response.setContentType("text/text");
    	PrintWriter out = response.getWriter();
    	out.print(jsonVisits.toString());
    	out.flush();
		
		return null;
	}
}

package gov.nih.nichd.ctdb.response.action;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.struts2.result.StreamResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.patient.manager.PatientManager;
import gov.nih.nichd.ctdb.protocol.domain.ClinicalLocation;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.response.domain.ScheduleReport;
import gov.nih.nichd.ctdb.response.domain.ScheduleReportFilter;
import gov.nih.nichd.ctdb.response.manager.ReportingManager;
import gov.nih.nichd.ctdb.response.tag.ScheduleReportIdtDecorator;
import gov.nih.nichd.ctdb.security.common.SecurityConstants;
import gov.nih.nichd.ctdb.security.domain.Privilege;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.security.util.SecuritySessionUtil;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;


public class ScheduleAction extends BaseAction{

	private static final long serialVersionUID = 6360470100879672871L;
	private static final Logger logger = Logger.getLogger(ReportingAction.class);
	
	private String currProtoId;
	private List<Protocol> protocolList = new ArrayList<Protocol>();
	private List<ClinicalLocation> clinicalLocList = new ArrayList<ClinicalLocation>();
	private List<Patient> patientList = new ArrayList<Patient>();
	private String jsonString = CtdbConstants.EMPTY_JSON_ARRAY_STR;
	private String selectedProtocolId;
	private String selectedClinicalLocId;
	private String selectedSubjectId;
	private String scheduleStartDateStr;
	private String scheduleEndDateStr;

	public String showGenerateSchedule() throws CtdbException {
		buildLeftNav(LeftNavController.LEFTNAV_SCHEDULE);
		clearScheduleReportSession();
		
		Protocol curProtocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		this.setCurrProtoId(String.valueOf(curProtocol.getId()));

		User user = getUser();
		// only see the protocol(s) this user can access
		Privilege piPrivlege = new Privilege(SecurityConstants.PI_SCHEDULER_PRIV);
		// able to see all protocols
		Privilege schedulerPriv = new Privilege(SecurityConstants.SCHEDULER_PRIV);

		ProtocolManager protMan = new ProtocolManager();
		PatientManager patMan = new PatientManager();
		if (!user.isSysAdmin() && user.hasPrivilege(piPrivlege)) {
			this.setProtocolList((List<Protocol>) session.get(CtdbConstants.USER_PROTOCOL_LIST));
			if (this.getProtocolList().isEmpty()) {
				this.setProtocolList(SecuritySessionUtil.refreshUserProtocols(user, request));
			}
			// this.getProtocolList().add(curProtocol);

			List<ClinicalLocation> clinicalLocList = new ArrayList<ClinicalLocation>();
			for (Protocol prot : this.getProtocolList()) {
				clinicalLocList.addAll(protMan.getProtocolClinicalLocs(prot.getId()));
			}
			this.setClinicalLocList(clinicalLocList);
			// this.setClinicalLocList(protMan.getProtocolClinicalLocs(Integer.parseInt(this.getCurrProtoId())));

			List<Patient> patientList = new ArrayList<Patient>();
			for (Protocol prot : this.getProtocolList()) {
				patientList.addAll(patMan.getPatientListByProtocol(prot.getId()));
			}
			this.setPatientList(patientList);
			// this.setPatientList(patMan.getPatientListByProtocol(Integer.parseInt(this.getCurrProtoId())));

		} else if (user.hasPrivilege(schedulerPriv)) {

			this.setProtocolList(protMan.getProtocols());

			this.setClinicalLocList(protMan.getAllClinicalLocs());

			this.setPatientList(patMan.getPatientListForAllProtocol());
		}
		List<ScheduleReport> repoList = new ArrayList<ScheduleReport>();
		session.put("scheduleReportList", repoList);
		
		return SUCCESS;
	}
	public String getClinicLocListByProto() throws CtdbException, JSONException {
		this.selectedProtocolId = this.getSelectedProtocolId();
		Integer protoId = StringUtils.isBlank(selectedProtocolId) ? null : Integer.valueOf(selectedProtocolId);
		List<ClinicalLocation> clinicLocList = new ArrayList<ClinicalLocation>();
		ProtocolManager protoMan = new ProtocolManager();
		if(protoId != null){
			clinicLocList = protoMan.getProtocolClinicalLocs(protoId);
		} else {
			clinicLocList = protoMan.getAllClinicalLocs();
		}
		this.setClinicalLocList(clinicLocList);
		
		JSONArray clinialLocJsonArr = new JSONArray();
		
		for (ClinicalLocation loc : clinicLocList) {
			JSONObject clinicalJsonObj = new JSONObject();
			clinicalJsonObj.put("id", loc.getId());
			clinicalJsonObj.put("name", loc.getName());			
			clinialLocJsonArr.put(clinicalJsonObj);
		}
	
		jsonString = clinialLocJsonArr.toString();
		
		return BaseAction.SUCCESS;
	}
	public String getPatientListByProto() throws CtdbException, JSONException {
		this.selectedProtocolId = this.getSelectedProtocolId();
		PatientManager patMan = new PatientManager();
		List<Patient> patList = new ArrayList<Patient>();
		Integer protoId = StringUtils.isBlank(selectedProtocolId) ? null : Integer.valueOf(selectedProtocolId);
		if(protoId != null){
			patList = patMan.getPatientListByProtocol(protoId);
		} else {
			patList = patMan.getPatientListForAllProtocol();
		}
		this.setPatientList(patList);
		JSONArray patJsonArr = new JSONArray();
		
		for (Patient pat : patList) {
			JSONObject patJsonObj = new JSONObject();
			patJsonObj.put("id", pat.getId());
			patJsonObj.put("name", pat.getLastNameFirstName());			
			patJsonArr.put(patJsonObj);
		}
	
		jsonString = patJsonArr.toString();
		
		return BaseAction.SUCCESS;
	}
	public StreamResult generateScheduleReport() throws CtdbException {
		this.selectedProtocolId = this.getSelectedProtocolId();
		this.selectedClinicalLocId = this.getSelectedClinicalLocId();
		this.selectedSubjectId = this.getSelectedSubjectId();
		this.scheduleStartDateStr = this.getScheduleStartDateStr();
		this.scheduleEndDateStr = this.getScheduleEndDateStr();
		
		SimpleDateFormat df = new SimpleDateFormat(SysPropUtil.getProperty("default.system.dateformat"));
		
		Date scheduleStartDate = null, scheduleEndDate = null;
		try {
			scheduleStartDate = df.parse(scheduleStartDateStr);
			scheduleEndDate = df.parse(scheduleEndDateStr);
			// include end date
			Calendar c = Calendar.getInstance();
			c.setTime(scheduleEndDate);
			c.add(Calendar.DATE, 1);
			scheduleEndDate = c.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		Integer protocolId = StringUtils.isBlank(selectedProtocolId) ? null : Integer.valueOf(selectedProtocolId);
		Integer clinicalLocId = StringUtils.isBlank(selectedClinicalLocId) ? null : Integer.valueOf(selectedClinicalLocId);
		Integer subjectId = StringUtils.isBlank(selectedSubjectId) ? null :Integer.valueOf(selectedSubjectId);
		ScheduleReportFilter schReportFilter = new ScheduleReportFilter(protocolId, subjectId, clinicalLocId, 
				scheduleStartDate, scheduleEndDate);
		
		ReportingManager repoMan = new ReportingManager();
		List<ScheduleReport> repoList = new ArrayList<ScheduleReport>();
		repoList = repoMan.getScheduleReportByFilters(schReportFilter);
		session.put("scheduleReportList", repoList);
		
		return new StreamResult (new ByteArrayInputStream(BaseAction.SUCCESS.getBytes()));	
	}
	
	public String getScheduleReportDTList() throws CtdbException {
		@SuppressWarnings("unchecked")
		List<ScheduleReport> repoList =  (List<ScheduleReport>) session.get("scheduleReportList");	
		
		try {			
			IdtInterface idt = new Struts2IdtInterface();			
			ArrayList<ScheduleReport> outputList = new ArrayList<ScheduleReport>(repoList);
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new ScheduleReportIdtDecorator());
			idt.output();

		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		
		return null;
	}
	private void clearScheduleReportSession() {
		session.remove("scheduleReportList");
	}
	public String getCurrProtoId(){
		return this.currProtoId;
	}
	public void setCurrProtoId(String protoId){
		this.currProtoId = protoId;
	}

	public List<Protocol> getProtocolList() {
		return this.protocolList;
	}

	public void setProtocolList(List<Protocol> protocolList) {
		this.protocolList.clear();
		if (protocolList != null) {
			this.protocolList.addAll(protocolList);
		}
	}
	public List<ClinicalLocation> getClinicalLocList() {
		return this.clinicalLocList;
	}	
	public void setClinicalLocList(List<ClinicalLocation> clinicalLocList) {
		this.clinicalLocList.clear();
		if(clinicalLocList != null){
			this.clinicalLocList.addAll(clinicalLocList);
		}
	}	
	public List<Patient> getPatientList() {
		return this.patientList;
	}	
	public void setPatientList(List<Patient> patientList) {
		this.patientList.clear();
		if(patientList != null){
			this.patientList.addAll(patientList);
		}
	}
	public String getJsonString() {
		return jsonString;
	}

	public void setJsonString(String jsonString) {
		this.jsonString = jsonString;
	}
	public String getSelectedProtocolId() {
		return this.selectedProtocolId;
	}
	public void setSelectedProtocolId(String protoId){
		this.selectedProtocolId = protoId;
	}

	public String getSelectedClinicalLocId() {
		return this.selectedClinicalLocId;
	}
	public void setSelectedClinicalLocId(String selectedClinicalLocId){
		this.selectedClinicalLocId = selectedClinicalLocId;
	}
	public String getSelectedSubjectId() {
		return this.selectedSubjectId;
	}
	public void setSelectedSubjectId(String selectedSubjectId){
		this.selectedSubjectId = selectedSubjectId;
	}
	public String getScheduleStartDateStr() {
		return this.scheduleStartDateStr;
	}
	public void setScheduleStartDateStr(String scheduleStartDateStr){
		this.scheduleStartDateStr = scheduleStartDateStr;
	}
	public String getScheduleEndDateStr() {
		return this.scheduleEndDateStr;
	}
	public void setScheduleEndDateStr(String scheduleEndDateStr){
		this.scheduleEndDateStr = scheduleEndDateStr;
	}
	

}

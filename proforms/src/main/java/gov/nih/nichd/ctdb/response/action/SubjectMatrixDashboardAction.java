package gov.nih.nichd.ctdb.response.action;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.patient.domain.PatientProtocol;
import gov.nih.nichd.ctdb.patient.manager.PatientManager;
import gov.nih.nichd.ctdb.protocol.domain.Interval;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.response.common.ReportingConstants;
import gov.nih.nichd.ctdb.response.form.FormVisitTypeStatusSubjectMatrix;
import gov.nih.nichd.ctdb.response.manager.ReportingManager;

public class SubjectMatrixDashboardAction extends ReportingAction {
	private static final long serialVersionUID = -8405410943869247236L;
	private static final Logger logger = Logger.getLogger(SubjectMatrixDashboardAction.class);

	/**
	 * This variable gets the selected GUID from the JSP to action.
	 */
	private String selectedGuidInAction;

	public SubjectMatrixDashboardAction() {
		super();
		this.selectedGuidInAction = "";
	}

	public String subjectMatrixDashbaord() throws Exception {
		String forward = BaseAction.SUCCESS;
		buildLeftNav(LeftNavController.LEFTNAV_SUBJECT_MATRIX_DASHBORAD);
		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		
		if (p == null) {
			return StrutsConstants.SELECTPROTOCOL;
		}
		
		cleanupReports();
		
		try {
			// List of visity type for a selected study
			ReportingManager repMgr = new ReportingManager();
			List<Interval> visitTypeList = repMgr.getIntervalListForSelectedStudy(Long.valueOf(p.getId()));
			JSONArray jsonArrayOfVisitType = new JSONArray();
			JSONObject jsonObjOfVisitType = new JSONObject();
			// Insert the first element as text of form and visit type name
			jsonObjOfVisitType.put("title", "FormName/VisitType");
			jsonObjOfVisitType.put("name", "FormName/VisitType");
			jsonArrayOfVisitType.put(jsonObjOfVisitType);
			
			// Build the column of the data table dynamically by returning jsonArray of
			// jsonObject
			for (Interval i : visitTypeList) {
				// create new jsonObj each time and put into to array to send to JSP
				jsonObjOfVisitType = new JSONObject();
				jsonObjOfVisitType.put("title", i.getName());
				jsonObjOfVisitType.put("name", i.getName());
				jsonArrayOfVisitType.put(jsonObjOfVisitType);
			}

			// List of GUID for a selected study

			List<Patient> guidList = repMgr.getGUIDListForSubject(Long.valueOf(p.getId()));
			Long patientId = (long) -1;
			//List<String> guidOrMrnListToJsp = new ArrayList<String>();
			JSONArray jsonArrayOfGuid = new JSONArray();
			for (Patient pat : guidList) {

				patientId = (long) pat.getId();
				
				if(p.getPatientDisplayType()==CtdbConstants.PATIENT_DISPLAY_GUID) {
					if(pat.getGuid()!=null) {
						jsonArrayOfGuid.put(pat.getGuid());
					}
					patientId = (long) pat.getId();
				}
				
				if(p.getPatientDisplayType()==CtdbConstants.PATIENT_DISPLAY_MRN){
					if(pat.getMrn()!=null) {
						jsonArrayOfGuid.put(pat.getMrn());
						
					}
					patientId = (long) pat.getId();
				}
				
				if(p.getPatientDisplayType()==CtdbConstants.PATIENT_DISPLAY_ID){
					patientId = (long) pat.getId();
					
					PatientManager patientMan = new PatientManager();
					PatientProtocol patientProtocol = patientMan.getPatientProtocalByPatientAndProtocol(patientId, (long) p.getId());
					if(patientProtocol.getSubjectId()!=null) {
						jsonArrayOfGuid.put(patientProtocol.getSubjectId());
					}
					
				}

			}
			
			List<FormVisitTypeStatusSubjectMatrix> subjectMatrix = repMgr
					.getSubjectMatrixForFormVsVisitTypeWithCollectionStatusFilteredableByGuid(Long.valueOf(p.getId()),
							selectedGuidInAction);

			// Build multi dimensional jsonArray-->jsonArrayOfSubjectMatrix
			JSONArray jsonArrayOfSubjectMatrix = new JSONArray();
			for (FormVisitTypeStatusSubjectMatrix sM : subjectMatrix) {
				JSONArray singleD = new JSONArray();
				singleD.put(sM.getFormName().replaceAll("[(]", "&#40;").replaceAll("[)]", "&#41;"));
				
				if (patientId == -1) {
					for (int i = 0; i < visitTypeList.size(); i++) {
						singleD.put(" ");
					}
				}

				if (sM.getVisitType() != null) {
					for (Interval vt : visitTypeList) {
						if (sM.getVisitTypeStatusMap().containsKey(vt.getName())) {
							if (sM.getVisitTypeStatusMap().get(vt.getName())
									.equalsIgnoreCase(CtdbConstants.DATACOLLECTION_STATUS_LOCKED)) {
								if (sM.isRequiredByVisitType(vt.getName())) {
									singleD.put(
											"<div class=\"greenCircle floatLeft statusCir noExport\">R</div><div class=\"greenCircle floatLeft statusCir displayNone\">"
													+ CtdbConstants.DATACOLLECTION_STATUS_LOCKED_R + "</div>");
								}
								else {
									singleD.put(
											"<div class=\"greenCircle floatLeft statusCir noExport\"></div><div class=\"greenCircle floatLeft statusCir displayNone\">"
													+ CtdbConstants.DATACOLLECTION_STATUS_LOCKED_O + "</div>");
								}
							}
							else if (sM.getVisitTypeStatusMap().get(vt.getName())
									.equalsIgnoreCase(CtdbConstants.DATACOLLECTION_STATUS_COMPLETED)) {
								if (sM.isRequiredByVisitType(vt.getName())) {
									singleD.put(
											"<div class=\"yellowCircle floatLeft statusCir noExport\">R</div><div class=\"yellowCircle floatLeft statusCir displayNone\">"
													+ CtdbConstants.DATACOLLECTION_STATUS_COMPLETED_R + "</div>");
								}
								else {
									singleD.put(
											"<div class=\"yellowCircle floatLeft statusCir noExport\"></div><div class=\"yellowCircle floatLeft statusCir displayNone\">"
													+ CtdbConstants.DATACOLLECTION_STATUS_COMPLETED_O + "</div>");
								}
							}
							else if (sM.getVisitTypeStatusMap().get(vt.getName())
									.equalsIgnoreCase(CtdbConstants.DATACOLLECTION_STATUS_INPROGRESS)) {
								if (sM.isRequiredByVisitType(vt.getName())) {
									singleD.put(
											"<div class=\"redCircle floatLeft statusCir noExport\">R</div><div class=\"redCircle floatLeft statusCir displayNone\">"
													+ CtdbConstants.DATACOLLECTION_STATUS_INPROGRESS_R + "</div>");
								}
								else {
									singleD.put(
											"<div class=\"redCircle floatLeft statusCir noExport\"></div><div class=\"redCircle floatLeft statusCir displayNone\">"
													+ CtdbConstants.DATACOLLECTION_STATUS_INPROGRESS_O + "</div>");
								}
							}
							else if (sM.getVisitTypeStatusMap().get(vt.getName())
									.equalsIgnoreCase(CtdbConstants.DATACOLLECTION_STATUS_NOTSTARTED)) {
								if (sM.isRequiredByVisitType(vt.getName())) {
									singleD.put(
											"<div class=\"whiteCircle floatLeft statusCir noExport\">R</div><div class=\"whiteCircle floatLeft statusCir displayNone\">"
													+ CtdbConstants.DATACOLLECTION_STATUS_NOTSTARTED_R + "</div>");
								}
								else {
									singleD.put(
											"<div class=\"whiteCircle floatLeft statusCir noExport\"></div><div class=\"whiteCircle floatLeft statusCir displayNone\">"
													+ CtdbConstants.DATACOLLECTION_STATUS_NOTSTARTED_O + "</div>");
								}
							}
							else if (sM.getVisitTypeStatusMap().get(vt.getName()) == null) {
								if (sM.isRequiredByVisitType(vt.getName())) {
									singleD.put("<div class=\"whiteCircle floatLeft statusCir noExport\">R</div>");
								}
								else {
									singleD.put("<div class=\"whiteCircle floatLeft statusCir noExport\"></div>");
								}
							}
							else if (sM.getVisitTypeStatusMap().get(vt.getName()) == "-") {
								singleD.put("<div>-</div>");
							}
							else {
								// Do nothing
							}
						}
						// If there are no forms associated in visit type then we put this
						else {
							singleD.put("<div>-</div>");
						}
					}
				}
				// if the map is null then there is no visit type and form association (key i.e.
				// visit type is null)
				else {
					for (int i = 0; i < visitTypeList.size(); i++) {
						singleD.put("<div>-</div>");
					}
				}

				jsonArrayOfSubjectMatrix.put(singleD);
			}

			session.put(ReportingConstants.JSON_ARRAY_SUBJECT_MATRIX_KEY, jsonArrayOfSubjectMatrix);
			session.put(ReportingConstants.JSON_ARRAY_GUID_KEY, jsonArrayOfGuid);
			session.put(ReportingConstants.SELECTED_GUID_KEY, selectedGuidInAction);
			session.put(ReportingConstants.JSON_ARRAY_VISIT_TYPE_KEY, jsonArrayOfVisitType);
		}
		catch (CtdbException ce) {
			logger.error("A database error occurred while getting data for the Form Status by GUID report.", ce);
			forward = StrutsConstants.FAILURE;
		}
		catch (JSONException je) {
			logger.error("A JSON error occurred while generating the Form Status by GUID report.", je);
			forward = StrutsConstants.FAILURE;
		}
		catch (Exception e) {
			logger.error("An error occurred while generating the Form Status by GUID report.", e);
			forward = StrutsConstants.FAILURE;
		}

		return forward;
	}

	public String getSelectedGuidInAction() {
		return selectedGuidInAction;
	}

	public void setSelectedGuidInAction(String selectedGuidInAction) {
		this.selectedGuidInAction = selectedGuidInAction;
	}
	


}

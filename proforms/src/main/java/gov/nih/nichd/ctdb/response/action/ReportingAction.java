package gov.nih.nichd.ctdb.response.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.question.tag.SubmissionReportDecorator;
import gov.nih.nichd.ctdb.response.common.ReportingConstants;
import gov.nih.nichd.ctdb.response.domain.SubmissionSummaryReport;
import gov.nih.nichd.ctdb.response.form.ReportingForm;
import gov.nih.nichd.ctdb.response.manager.ReportingManager;
import gov.nih.nichd.ctdb.response.tag.CompletedVisitsReportIdtDecorator;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;

public class ReportingAction extends BaseAction {
	private static final long serialVersionUID = -2827964076654047077L;
	private static final Logger logger = Logger.getLogger(ReportingAction.class);
	
	private List<ReportingForm> guidsWithoutCollectionsReport = null;	
	private List<ReportingForm> completedVisitsReport = null;
	private List<ReportingForm> performanceOverviewReport = null;
	private List<ReportingForm> formsRequiringLockList = null;
	private List<ReportingForm> completedFormsReport = null;

	protected int[] ifDisabledLinks = new int[] { LeftNavController.LEFTNAV_SUBJECTS_MANAGE,
			LeftNavController.LEFTNAV_COLLECT, LeftNavController.LEFTNAV_FORM_HOME,
			LeftNavController.LEFTNAV_STUDY_HOME, LeftNavController.LEFTNAV_QUERY_DISCREPANCY,
			LeftNavController.LEFTNAV_QUERY_FORMSTATUS, LeftNavController.LEFTNAV_QUERY_COMPLETED,
			LeftNavController.LEFTNAV_QUERY_FORMS_REQ_LOCK, LeftNavController.LEFTNAV_QUERY_PERFORMANCE_OVERVIEW,
			LeftNavController.LEFTNAV_QUERY_SUBMISSION_SUMMARY, LeftNavController.LEFTNAV_SUBJECT_MATRIX_DASHBORAD,
			LeftNavController.LEFTNAV_QUERY_GUIDS_WITHOUT_COLLECTIONS };

	// Completed Visits
	public String showCompletedVisits() {
		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		
		if (p != null) {
			buildLeftNav(LeftNavController.LEFTNAV_QUERY_COMPLETED_VISITS);
		}
		else {
			buildLeftNav(LeftNavController.LEFTNAV_QUERY_COMPLETED_VISITS, ifDisabledLinks);
		}

		this.cleanupReports();
		
		try {
			ReportingManager repMgr = new ReportingManager();
			
			if (p != null) {
				setCompletedVisitsReport(repMgr.getCompletedVisitsReport(p.getId()));
			}
			else {
				setCompletedVisitsReport(repMgr.getCompletedVisitsReport(-1));
			}

			session.put(ReportingConstants.COMPLETED_VISITS_KEY, getCompletedVisitsReport());
		}
		catch (CtdbException ce) {
			logger.error("Database error occurred when getting data for the Completed Visits report.", ce);
			this.addActionError(getText(StrutsConstants.ERROR_DATABASE_GET, new String[] { "data" }));
		}
		catch (Exception e) {
			logger.error("An error occurred when generating the Completed Visits report.", e);
			return StrutsConstants.FAILURE;
		}

		return SUCCESS;
	}
	
	public String getCompletedVisitsReportList() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			showCompletedVisits();
			ArrayList<ReportingForm> outputList = new ArrayList<ReportingForm>(getCompletedVisitsReport());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new CompletedVisitsReportIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	// Performance Overview
	public String showPerformanceOverview() {
		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);

		if (p != null) {
			buildLeftNav(LeftNavController.LEFTNAV_QUERY_PERFORMANCE_OVERVIEW);
		}
		else {
			return StrutsConstants.SELECTPROTOCOL;
		}

		this.cleanupReports();
		
		try {
			ReportingManager repMgr = new ReportingManager();
			setPerformanceOverviewReport(repMgr.getPerformanceOverviewReport(p.getId()));
			session.put(ReportingConstants.PERFORMANCE_OVERVIEW_KEY, getPerformanceOverviewReport());
		}
		catch (CtdbException ce) {
			logger.error("Database error occurred when getting data for the Performance Overview report.", ce);
			this.addActionError(getText(StrutsConstants.ERROR_DATABASE_GET, new String[] { "data" }));
		}
		catch (Exception e) {
			logger.error("An error occurred when generating the Performance Overview report.", e);
			return StrutsConstants.FAILURE;
		}

		return SUCCESS;
	}
	
	public String getPerformanceOverviewReportList() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			showPerformanceOverview();
			ArrayList<ReportingForm> outputList = new ArrayList<ReportingForm>(getPerformanceOverviewReport());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}	

	// GUIDs Without Collections
	public String showGuidsWithoutCollections() {
		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		
		if (p != null) {
			buildLeftNav(LeftNavController.LEFTNAV_QUERY_GUIDS_WITHOUT_COLLECTIONS);
		}
		else {
			return StrutsConstants.SELECTPROTOCOL;
		}

		this.cleanupReports();
		try {
			ReportingManager repMgr = new ReportingManager();
			setGuidsWithoutCollectionsReport(repMgr.getGuidsWithoutCollectionsReport(p.getId()));
			session.put(ReportingConstants.GUIDS_WO_COLLECTIONS_KEY, getGuidsWithoutCollectionsReport());
		}
		catch (CtdbException ce) {
			logger.error("Database error occurred when getting data for the GUIDs Without Collections report.", ce);
			this.addActionError(getText(StrutsConstants.ERROR_DATABASE_GET, new String[] { "data" }));
		}
		catch (Exception e) {
			logger.error("An error occurred when generating the GUIDs Without Collections report.", e);
			return StrutsConstants.FAILURE;
		}

		return SUCCESS;
	}
	
	public String getGuidsWithoutCollectionsReportsList() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			showGuidsWithoutCollections();
			ArrayList<ReportingForm> outputList = new ArrayList<ReportingForm>(getGuidsWithoutCollectionsReport());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}	

	// Forms Requiring Lock
	public String showFormsReqLock() {
		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		
		if (p != null) {
			buildLeftNav(LeftNavController.LEFTNAV_QUERY_FORMS_REQ_LOCK);
		}
		else {
			return StrutsConstants.SELECTPROTOCOL;
		}

		this.cleanupReports();

		try {
			ReportingManager repMgr = new ReportingManager();
			setFormsRequiringLockList(repMgr.getFormsRequiringLockReport(p.getId()));
			session.put(ReportingConstants.FORMS_REQ_LOCK_KEY, getFormsRequiringLockList());
		}
		catch (CtdbException ce) {
			logger.error("Database error occurred when getting data for the Forms Requiring Lock report.", ce);
			this.addActionError(getText(StrutsConstants.ERROR_DATABASE_GET, new String[] { "data" }));
		}
		catch (Exception e) {
			logger.error("An error occurred when generating the Forms Requiring Lock report.", e);
			return StrutsConstants.FAILURE;
		}

		return SUCCESS;
	}
	
	public String getFormsRequiringLockListList() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			showFormsReqLock();
			ArrayList<ReportingForm> outputList = new ArrayList<ReportingForm>(getFormsRequiringLockList());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}	

	// Locked Forms
	public String showCompletedForms() {
		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		
		if (p != null) {
			buildLeftNav(LeftNavController.LEFTNAV_QUERY_COMPLETED);
		}
		else {
			return StrutsConstants.SELECTPROTOCOL;
		}

		this.cleanupReports();
		
		try {
			ReportingManager repMgr = new ReportingManager();
			setCompletedFormsReport(repMgr.getCompletedFormsReport(p.getId()));
			session.put(ReportingConstants.LOCKED_FORMS_KEY, getCompletedFormsReport());
		}
		catch (CtdbException ce) {
			logger.error("Database error occurred when getting data for the Locked Forms report.", ce);
			this.addActionError(getText(StrutsConstants.ERROR_DATABASE_GET, new String[] { "data" }));
		}
		catch (Exception e) {
			logger.error("An error occurred when generating the Locked Forms report.", e);
			return StrutsConstants.FAILURE;
		}

		return SUCCESS;
	}

	public String getCompletedFormsList() {
		List<ReportingForm> completedForms = new ArrayList<ReportingForm>();
		completedForms= (ArrayList<ReportingForm>) session.get(ReportingConstants.LOCKED_FORMS_KEY);
		
		try {
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<ReportingForm> outputList = new ArrayList<ReportingForm>(completedForms);
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}	


	// Submission Summary
	public String showSubmissionSummary() {
		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		
		if (p != null) {
			buildLeftNav(LeftNavController.LEFTNAV_QUERY_SUBMISSION_SUMMARY);
		}
		else {
			return StrutsConstants.SELECTPROTOCOL;
		}

		this.cleanupReports();
		
		try {
			ReportingManager repMgr = new ReportingManager();
			List<SubmissionSummaryReport> submissionReportList = repMgr.getSubmissionSummaryReport(p.getId());
			session.put(ReportingConstants.SUBMISSION_SUMMARY_KEY, submissionReportList);
		}
		catch (CtdbException ce) {
			logger.error("Database error occurred when getting data for the Submission Summary report.", ce);
			this.addActionError(getText(StrutsConstants.ERROR_DATABASE_GET, new String[] { "data" }));
		}
		catch (Exception e) {
			logger.error("An error occurred when generating the Submission Summary report.", e);
			return StrutsConstants.FAILURE;
		}

		return SUCCESS;
	}
	
	public String getSubmissionSummaryList() {

		List<SubmissionSummaryReport> submissionReportList = new ArrayList<SubmissionSummaryReport>();
		submissionReportList= (ArrayList<SubmissionSummaryReport>) session.get(ReportingConstants.SUBMISSION_SUMMARY_KEY);
		
		try{
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<SubmissionSummaryReport> outputList = new ArrayList<SubmissionSummaryReport>(submissionReportList);
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new SubmissionReportDecorator());
			idt.output();			
		} catch (InvalidColumnException e){
			logger.error("Invalid Column: " + e);
			e.printStackTrace();
		}
		
		return null;
	}

	/**
	 * Clears the session of all reporting data.
	 */
	protected void cleanupReports() {
		session.remove(ReportingConstants.COMPLETED_VISITS_KEY);
		session.remove(ReportingConstants.PERFORMANCE_OVERVIEW_KEY);
		session.remove(ReportingConstants.GUIDS_WO_COLLECTIONS_KEY);
		session.remove(ReportingConstants.FORMS_REQ_LOCK_KEY);
		session.remove(ReportingConstants.LOCKED_FORMS_KEY);
		session.remove(ReportingConstants.SUBMISSION_SUMMARY_KEY);
		
		// Remove data from the Form Status by GUID report.
		session.remove(ReportingConstants.JSON_ARRAY_SUBJECT_MATRIX_KEY);
		session.remove(ReportingConstants.SELECTED_GUID_KEY);
		session.remove(ReportingConstants.JSON_ARRAY_GUID_KEY);
		session.remove(ReportingConstants.JSON_ARRAY_VISIT_TYPE_KEY);
	}
	
	public void setGuidsWithoutCollectionsReport(List<ReportingForm> guidsWithoutCollectionsReport) {
		this.guidsWithoutCollectionsReport = guidsWithoutCollectionsReport;
	}
	public List<ReportingForm> getGuidsWithoutCollectionsReport() {
		return guidsWithoutCollectionsReport;
	}
	
	public void setCompletedVisitsReport(List<ReportingForm> completedVisitsReport) {
		this.completedVisitsReport = completedVisitsReport;
	}
	public List<ReportingForm> getCompletedVisitsReport() {
		return completedVisitsReport;
	}
	
	public void setPerformanceOverviewReport(List<ReportingForm> performanceOverviewReport) {
		this.performanceOverviewReport = performanceOverviewReport;
	}
	public List<ReportingForm> getPerformanceOverviewReport() {
		return performanceOverviewReport;
	}
	
	public void setFormsRequiringLockList(List<ReportingForm> formsRequiringLockList) {
		this.formsRequiringLockList = formsRequiringLockList;
	}
	public List<ReportingForm> getFormsRequiringLockList() {
		return formsRequiringLockList;
	}
	
	public List<ReportingForm> getCompletedFormsReport() {
		return completedFormsReport;
	}
	public void setCompletedFormsReport(List<ReportingForm> completedFormsReport) {
		this.completedFormsReport = completedFormsReport;
	}
}

package gov.nih.nichd.ctdb.response.action;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.InternalServerErrorException;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.apache.struts2.ServletActionContext;
import org.xml.sax.SAXException;

import au.com.bytecode.opencsv.CSVWriter;
import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.protocol.domain.BricsStudy;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.response.form.ReportingForm;
import gov.nih.nichd.ctdb.response.manager.ReportingManager;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.util.common.BricsRepoWsClient;
import gov.nih.tbi.guid.model.GuidSearchResult;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;

public class StaticQueryAction extends BaseAction {
	
	private static final long serialVersionUID = 8610223051451978983L;

	public String execute() {
		
		Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		
		int[] disabledLinks = null;
		if (protocol == null) {
			disabledLinks = new int[]{
					LeftNavController.LEFTNAV_SUBJECTS_MANAGE,
					LeftNavController.LEFTNAV_COLLECT, 
					LeftNavController.LEFTNAV_FORM_HOME, 
					LeftNavController.LEFTNAV_STUDY_HOME, 
					LeftNavController.LEFTNAV_QUERY_DISCREPANCY, 
					LeftNavController.LEFTNAV_QUERY_FORMSTATUS, 
					LeftNavController.LEFTNAV_QUERY_COMPLETED, 
					LeftNavController.LEFTNAV_QUERY_FORMS_REQ_LOCK, 
					LeftNavController.LEFTNAV_QUERY_PERFORMANCE_OVERVIEW, 
					LeftNavController.LEFTNAV_QUERY_SUBMISSION_SUMMARY,
					LeftNavController.LEFTNAV_SUBJECT_MATRIX_DASHBORAD,
					LeftNavController.LEFTNAV_QUERY_GUIDS_WITHOUT_COLLECTIONS,
					LeftNavController.LEFTNAV_SCHEDULE,
					LeftNavController.LEFTNAV_ADVERSE_EVENT,
					LeftNavController.LEFTNAV_VIEW_AUDITOR_COMMENTS
			};
		}
		
		
		String reportType = request.getParameter(CtdbConstants.REPORT_PARAM);
		if(reportType != null && reportType.equals(CtdbConstants.DETAILED_PROTOCOL_REPORT)) {
			this.buildLeftNav(LeftNavController.LEFTNAV_QUERY_DETAILED_STUDY, disabledLinks);
		}else {
			this.buildLeftNav(LeftNavController.LEFTNAV_QUERY_STUDY, disabledLinks);
		}
		
		 

		return SUCCESS;
	}
	
	public String getStudyReportList() throws CtdbException, ParserConfigurationException, SAXException, IOException {
		Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		try {				
			IdtInterface idt = new Struts2IdtInterface();
			ReportingManager repMgr = new ReportingManager();
			List<ReportingForm> studyReport = null;
		
			if (protocol != null) {
				studyReport = repMgr.getStudyReportForAStudy(protocol.getId());
			} else {
				studyReport = repMgr.getStudyReport();
			}
			
			BricsRepoWsClient client = new BricsRepoWsClient();
			User user = getUser();
			Hashtable<String, BricsStudy> bricsStudyTable = client.getBricsStudiesForUser(user, request, protocol);
		
			for (ReportingForm rf : studyReport) {
				String bricsStudyId = rf.getBricsStudyId();
				
				if (!StringUtils.isEmpty(bricsStudyId)) {
					BricsStudy bs = bricsStudyTable.get(bricsStudyId);
					
					if (bs != null) {
						rf.setStudyNumberSubjects(bs.getStudyNumberSubjects());
						rf.setStudyStartDate(bs.getStudyStartDate());
						rf.setStudyEndDate(bs.getStudyEndDate());
					}
				}	
			}	
					
			ArrayList<ReportingForm> outputList = new ArrayList<ReportingForm>(studyReport);
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.output();
		} catch (InvalidColumnException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	
	public String getDetailedStudyReportList() throws CtdbException, ParserConfigurationException, SAXException, IOException {
		Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		try {				
			IdtInterface idt = new Struts2IdtInterface();
			ReportingManager repMgr = new ReportingManager();
			List<ReportingForm> studyReport = null;
		
			if (protocol != null) {
				studyReport = repMgr.getDetailedStudyReportForAStudy(protocol.getId());
			} else {
				studyReport = repMgr.getDetailedStudyReport();
			}
			
			BricsRepoWsClient client = new BricsRepoWsClient();
			User user = getUser();
			Hashtable<String, BricsStudy> bricsStudyTable = client.getBricsStudiesForUser(user, request, protocol);
		
			for (ReportingForm rf : studyReport) {
				String bricsStudyId = rf.getBricsStudyId();
				
				if (!StringUtils.isEmpty(bricsStudyId)) {
					BricsStudy bs = bricsStudyTable.get(bricsStudyId);
					
					if (bs != null) {
						rf.setStudyNumberSubjects(bs.getStudyNumberSubjects());
						rf.setStudyStartDate(bs.getStudyStartDate());
						rf.setStudyEndDate(bs.getStudyEndDate());
					}
				}	
			}	
					
			ArrayList<ReportingForm> outputList = new ArrayList<ReportingForm>(studyReport);
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.output();
		} catch (InvalidColumnException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	public String exportDetailedProtocolCSV() throws CtdbException, ParserConfigurationException, SAXException {
		Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		ReportingManager repMgr = new ReportingManager();
		List<ReportingForm> studyReport = null;
		
		if (protocol != null) {
			studyReport = repMgr.getDetailedStudyReportForAStudyExport(protocol.getId());
		} else {
			studyReport = repMgr.getDetailedStudyReportExport();
		}
		
		
		
		
		
		
		//File csvFile = null;
		CSVWriter writer = null;
		ByteArrayOutputStream baos = null;

		try {
			///String prefix = "detailedProtocolReport-" + (new Date()).getTime();
			String filename = "detailedProtocolReport-" + (new Date()).getTime() + ".csv";
			//csvFile = File.createTempFile(prefix, ".csv");
			//csvFile.deleteOnExit();
			//writer = new CSVWriter(new FileWriter(csvFile));
			baos = new ByteArrayOutputStream();
			writer = new CSVWriter(new OutputStreamWriter(baos));

			String[] reportHeaderArr = { "PROTOCOL NAME", "PRINCIPAL INVESTIGATOR", "START DATE", "END DATE", "# OF SUBJECTS ENROLLED",
					"# OF EFORMS PER PROTOCOL", "EFORM ASSOCIATED WITH PROTOCOL", "NUMBER OF ADMINISTERED FORMS" };
			writer.writeNext(reportHeaderArr);
			
			BricsRepoWsClient client = new BricsRepoWsClient();
			User user = getUser();
			Hashtable<String, BricsStudy> bricsStudyTable = client.getBricsStudiesForUser(user, request, protocol);

			for (ReportingForm reportingForm : studyReport) {
				String bricsStudyId = reportingForm.getBricsStudyId();
				
				if (!StringUtils.isEmpty(bricsStudyId)) {
					BricsStudy bs = bricsStudyTable.get(bricsStudyId);
					
					if (bs != null) {
						reportingForm.setStudyStartDate(bs.getStudyStartDate());
						reportingForm.setStudyEndDate(bs.getStudyEndDate());
					}
				}
				String[] reportResultArr = new String[reportHeaderArr.length];
				reportResultArr[0] = reportingForm.getStudyName();
				reportResultArr[1] = reportingForm.getStudyPI();
				reportResultArr[2] = reportingForm.getStudyStartDate();
				reportResultArr[3] = reportingForm.getStudyEndDate();
				reportResultArr[4] = reportingForm.getStudySubjectCount();
				reportResultArr[5] = reportingForm.getStudyEformCount();
				reportResultArr[6] = reportingForm.getEformName();
				reportResultArr[7] = String.valueOf(reportingForm.getNumAforms());
				writer.writeNext(reportResultArr);
			}
			writer.flush();
			HttpServletResponse response = ServletActionContext.getResponse();
			response.resetBuffer();
			response.setHeader("Content-Disposition", "attachment;filename=" + filename);
			
			
			response.getWriter().write(baos.toString());
			//response.getWriter().write(writer.);
			//response.getWriter().write(baos.toByteArray());

		} catch (IOException e) {
			e.printStackTrace();
			throw new InternalServerErrorException("Error occured while generating the CSV file.");
		} finally {
			try {
				writer.close();
			} catch (IOException e) { }
		}
		
		return null;
		

	}
}

package gov.nih.nichd.ctdb.response.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;

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
					LeftNavController.LEFTNAV_QUERY_GUIDS_WITHOUT_COLLECTIONS
			};
		}
		
		this.buildLeftNav(LeftNavController.LEFTNAV_QUERY_STUDY, disabledLinks);
		
		try {
			ReportingManager repMgr = new ReportingManager();
			List<ReportingForm> studyReport = null;
		
			if (protocol != null) {
				studyReport = repMgr.getStudyReportForAStudy(protocol.getId());
			} else {
				studyReport = repMgr.getStudyReport();
			}
		
			session.put("studyReport", studyReport);
			
		} catch(CtdbException ubme) {
            return StrutsConstants.FAILURE;
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
}

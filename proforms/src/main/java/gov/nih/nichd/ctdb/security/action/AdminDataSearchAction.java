package gov.nih.nichd.ctdb.security.action;


import java.util.ArrayList;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.response.form.AdminSearchForm;
import gov.nih.nichd.ctdb.response.manager.AdminDataSearchManager;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;

public class AdminDataSearchAction extends BaseAction {
	private static final long serialVersionUID = -871573054763492789L;

	private static final Logger logger = Logger.getLogger(AdminDataSearchAction.class);
	
	public String guidList;
	public String shortNameList;
	public String visitList;
	
	public String execute() throws Exception {
		logger.info("AdminDataSearchAction-----------------> begin");

		User user = getUser();

		// Ensure that the current user is system admin.
		if ((user == null) || !user.isSysAdmin()) {
			addActionError("The user (" + user.getUsername() + ") is not a system admin.");
			return StrutsConstants.FORBIDDEN;
		}
		this.buildLeftNav(LeftNavController.LEFTNAV_ADMIN_FORM_SEARCH, new int[]{3, 8, 11, 15});
		return SUCCESS;
	}
	
	public void searchFormParams() {
		logger.info("AdminDataSearchAction-----------------> searchAdminForms");
	
		ArrayList<AdminSearchForm> outputList = new ArrayList<>();
		AdminDataSearchManager adsm = new AdminDataSearchManager();
		//checks for initial page load to prevent an error with JSON 
		if (guidList != null && visitList != null && shortNameList != null) {
			String[] guidSplit = guidList.replace("\n", ",").split(",");
			String[] shortNameSplit = shortNameList.replace("\n", ",").split(",");
			String[] visitSplit = visitList.replace("\n", ",").split(",");
			try {
				outputList = new ArrayList<AdminSearchForm>(adsm.searchForms(guidSplit, shortNameSplit, visitSplit));
			} catch (CtdbException e) {
				// Not handling error here because outputList is initialized to be empty, thus
				// we want to return an empty table
				logger.error("Query failed form search");
				e.printStackTrace();
			}		
		}
		try {
			IdtInterface idt = new Struts2IdtInterface();
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("Failure populating columns for Idt",e);
		}
	}

	public String getGuidList() {
		return guidList;
	}

	public void setGuidList(String guidList) {
		this.guidList = guidList;
	}

	public String getShortNameList() {
		return shortNameList;
	}

	public void setShortNameList(String shortnameList) {
		this.shortNameList = shortnameList;
	}

	public String getVisitList() {
		return visitList;
	}

	public void setVisitList(String visitList) {
		this.visitList = visitList;
	}


}
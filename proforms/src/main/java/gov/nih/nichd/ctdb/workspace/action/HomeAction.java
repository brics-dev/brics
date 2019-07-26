package gov.nih.nichd.ctdb.workspace.action;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.protocol.common.ProtocolConstants;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.tag.ProtocolInboxIdtDecorator;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.security.util.SecuritySessionUtil;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;


public class HomeAction extends BaseAction {
	private static final long serialVersionUID = 6394017082500287181L;
	private static final Logger logger = Logger.getLogger(HomeAction.class);

	public String execute() throws Exception {
		HttpServletResponse response = ServletActionContext.getResponse();
		response.addHeader("X-UA-Compatible", "IE=edge");
        
    	SecuritySessionUtil securitySession = new SecuritySessionUtil(request);
    	securitySession.switchProtocol();
    	Protocol protocol = securitySession.getSelectedProtocol();
    	
    	// we no longer want to remove the selected protocol here.  Process it like MyWorkspaceAction
    	if (protocol == null) {
			buildLeftNav(LeftNavController.LEFTNAV_PICKSTUDY,
					new int[] {LeftNavController.LEFTNAV_SUBJECTS_MANAGE, LeftNavController.LEFTNAV_COLLECT,
							LeftNavController.LEFTNAV_FORM_HOME, LeftNavController.LEFTNAV_STUDY_HOME});
    	} else {
    		buildLeftNav(LeftNavController.LEFTNAV_HOME);
    	}
    	
		User user = getUser();
    	List<Protocol> newProtocolList = SecuritySessionUtil.refreshUserProtocols(user, request);
		request.setAttribute("protocolList", newProtocolList);
    	
    	// Clear out some study session variables
    	session.remove(CtdbConstants.CURRENT_PROTOCOL_STUDY_SITES_SESSION_KEY);
		session.remove("xinstitutes");
		session.remove("xProtocolStatus");
		session.remove("xProtocolTypes");
		session.remove("xuserlist");
		session.remove("__SiteHome_states");
		session.remove("__SiteHome_countries");
		session.remove("_availiableDefaults");
		session.remove("_btrisAccess");
		session.remove(CtdbConstants.SITE_HASH_MAP);
		session.remove(CtdbConstants.DRUG_DEVICE_HASH_MAP);
		session.remove(ProtocolConstants.BRICS_STUDY_HASHTABLE);
    	
        return SUCCESS;
	}
	
	// url: http://fitbir-portal-local.cit.nih.gov:8082/ibis/getProtocolList.action
	public String getProtocolList() {
		// get the list of user's protocols from the session
        SecuritySessionUtil securitySession = new SecuritySessionUtil(request);  // Constructor auto-refreshes the session's protocol list.
		try {
			IdtInterface idt = new Struts2IdtInterface();			
			ArrayList<Protocol> outputList = new ArrayList<Protocol>(securitySession.getProtocols());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new ProtocolInboxIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}
}

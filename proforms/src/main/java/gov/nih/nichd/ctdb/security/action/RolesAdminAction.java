package gov.nih.nichd.ctdb.security.action;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.security.common.SecurityConstants;
import gov.nih.nichd.ctdb.security.domain.Role;
import gov.nih.nichd.ctdb.security.manager.SecurityManager;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;

public class RolesAdminAction extends BaseAction {

	private static final long serialVersionUID = 6036726473247179526L;
	private static final Logger logger = Logger.getLogger(RolesAdminAction.class);
	
	public String execute() throws Exception {
   		buildLeftNav(LeftNavController.LEFTNAV_ADMIN_ROLES, new int[]{3, 8, 11, 15});

        this.retrieveActionMessages(RoleAction.ROLEACTION_MESSAGES_KEY);

        try {
            SecurityManager sm = new SecurityManager();
            request.setAttribute(SecurityConstants.ROLE_REQUEST_KEY, sm.getSystemRoles());
        }
        catch ( CtdbException ce ) {
        	logger.error("Database error occurred while retrieving a list of system roles.", ce);
        	
        	return StrutsConstants.FAILURE;
        }

        return SUCCESS;
	}
	
	// url: http://fitbir-portal-local.cit.nih.gov:8082/proforms/admin/getSystemRolesList.action
	public String getSystemRolesList() throws CtdbException {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			SecurityManager sm = new SecurityManager();
			ArrayList<Role> outputList = new ArrayList<Role>(sm.getSystemRoles());
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

}

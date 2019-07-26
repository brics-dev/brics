package gov.nih.nichd.ctdb.security.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.security.domain.SiteLink;
import gov.nih.nichd.ctdb.security.manager.SecurityManager;
import gov.nih.nichd.ctdb.security.tag.SiteLinkIdtDecorator;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;

/**
 * SiteLinkAdminAction will display a list of all Site URLs in the CTDB System
 * for display to the System Administrator.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class SiteLinkAdminAction extends BaseAction {
	private static final long serialVersionUID = 6651573094596956952L;
	private static final Logger log = Logger.getLogger(SiteLinkAdminAction.class);
	
	private String deleteId;

	public String execute() throws Exception {

		buildLeftNav(LeftNavController.LEFTNAV_ADMIN_URLS, new int[]{3, 8, 11, 15});

        try {
            SecurityManager sm = new SecurityManager();
            
            request.setAttribute("siteLinks", sm.getSiteLinks());
            this.retrieveActionMessages(SiteLinkAction.ACTION_MESSAGES_KEY);
            
        } catch (Exception e) {
        	log.error("Error occurred while getting the site links.", e);
            return StrutsConstants.FAILURE;
        }

        return SUCCESS;
    }
	
	// url: http://fitbir-portal-local.cit.nih.gov:8082/proforms/admin/getSiteLinkAdmin.action
	public String getSiteLinkAdmin() throws Exception {
		try {
			IdtInterface idt = new Struts2IdtInterface();
            SecurityManager sm = new SecurityManager();
			ArrayList<SiteLink> outputList = new ArrayList<SiteLink>(sm.getSiteLinks());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new SiteLinkIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			log.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}	
	
    public String deleteSiteLink() {
    	
        buildLeftNav(LeftNavController.LEFTNAV_ADMIN_URLS, new int[]{3, 8, 11, 15});
        
        try {
            SecurityManager sm = new SecurityManager();
        	
            if (deleteId == null || deleteId.isEmpty()) {
                return StrutsConstants.FAILURE;
            }
            
        	List<Integer> idList = Utils.convertStrToIntArray(deleteId);
        	
        	int numDeleted = sm.deleteSiteLinks(idList);
        	int numNotDeleted = idList.size() - numDeleted;
        	
        	if (numNotDeleted != 0) {
        		addActionError("Some of the requested Site URL(s) could not be deleted.");
        	} else {
        		addActionMessage(getText(StrutsConstants.SUCCESS_DELETE_KEY, new String[]{"requested Site URL(s)"}));
        	}
        	
            request.setAttribute("siteLinks", sm.getSiteLinks());

        } catch (CtdbException ce) {
        	log.error("Could not delete the site link.", ce);
            return StrutsConstants.FAILURE;
        }
    	return SUCCESS;
    }

	public String getDeleteId() {
		return deleteId;
	}

	public void setDeleteId(String deleteId) {
		this.deleteId = deleteId;
	}
    

}
package gov.nih.nichd.ctdb.security.action;

import java.util.List;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.security.domain.SiteLink;
import gov.nih.nichd.ctdb.security.manager.SecurityManager;

/**
 * SiteLinkOrderAction will handle the manipulating of SiteLink orders and how they are displayed.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class SiteLinkOrderAction extends BaseAction {

	private static final long serialVersionUID = -1977003029771036839L;
	
	private String[] orderedSiteLinks;
    private List<SiteLink> siteLinks;

    public String execute() throws Exception {
   		buildLeftNav(LeftNavController.LEFTNAV_ADMIN_URLS, new int[]{3, 8, 11, 15});

        try {
            SecurityManager sm = new SecurityManager();
            this.setSiteLinks(sm.getSiteLinks());
        } catch (CtdbException ce) {
        	return StrutsConstants.FAILURE;
        }
        
        return SUCCESS;
    }
    
    
    public String updateSiteLinkOrder() {
   		buildLeftNav(LeftNavController.LEFTNAV_ADMIN_URLS, new int[]{3, 8, 11, 15});

        try {
            SecurityManager sm = new SecurityManager();
            
            sm.updateSiteLinkOrdering(this.getOrderedSiteLinks());
            this.setSiteLinks(sm.getSiteLinks());
            
        } catch (CtdbException ce) {
        	return StrutsConstants.FAILURE;
        }
        
        return SUCCESS;
    }

	public String[] getOrderedSiteLinks() {
		return orderedSiteLinks;
	}

	public void setOrderedSiteLinks(String[] orderedSiteLinks) {
		this.orderedSiteLinks = orderedSiteLinks;
	}

	public List<SiteLink> getSiteLinks() {
		return siteLinks;
	}

	public void setSiteLinks(List<SiteLink> siteLinks) {
		this.siteLinks = siteLinks;
	}

}
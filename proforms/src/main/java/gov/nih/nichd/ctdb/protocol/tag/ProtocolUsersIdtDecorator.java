package gov.nih.nichd.ctdb.protocol.tag;

import java.util.ArrayList;
import java.util.List;

import org.apache.struts2.ServletActionContext;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.tag.ActionIdtDecorator;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.domain.ProtocolRoleUser;
import gov.nih.nichd.ctdb.security.domain.Role;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.site.domain.Site;

public class ProtocolUsersIdtDecorator extends ActionIdtDecorator {
	public ProtocolUsersIdtDecorator()
	{
		super();
	}
	
	public String getUserName() {
		ProtocolRoleUser usr = (ProtocolRoleUser) getObject();
		
		String userName = usr.getUsername();

		return userName;
	}
	
	public String getFullName() {
		ProtocolRoleUser usr = (ProtocolRoleUser) getObject();
		
		String fullName = "";
		
		fullName = "<input type=\"hidden\" name=\"username_" + usr.getId()+ "\" id=\"username_" + usr.getId() + "\" value=\"" + usr.getUsername() + "\" />" + usr.getFullName();

		return fullName;
	}
	
	public String getRoleSelected() {
		User currentUser = (User) ServletActionContext.getRequest().getSession().getAttribute(CtdbConstants.USER_SESSION_KEY);
		ProtocolRoleUser usr = (ProtocolRoleUser) getObject();		
		List<Role> roles = (List<Role>) ServletActionContext.getRequest().getAttribute("roles");
		String disabled = "";
		String roleDisabled = "";
		String roleSelect = "";
		int roleId;
        String selected;
		
        if (usr.getRole() == null || (usr.getRole().getId() != 2 && usr.getRole().getId() != 3)) {
        	disabled = " disabled=\"disabled\"";
        }
		if ( currentUser.getUsername().equalsIgnoreCase(usr.getUsername()) ) {
			roleDisabled = " disabled=\"disabled\"";
			disabled = " disabled=\"disabled\"";
		}        
		
		roleSelect = "<select class=\"loneText roleselect\" id=\"user_" + usr.getId() + "_role\" name=\"user_" + usr.getId() + "_role\"" + roleDisabled + " onchange=\"roleChanged(this)\">";
		roleSelect += "<option value=\"0\">--</option>";
		String selectedRoleName = "";
        for (Role role : roles) {
            roleId = role.getId();
            selected = "";
            if (usr.getRole() != null && roleId == usr.getRole().getId()) {
            	selected = " selected=\"selected\"";
            	selectedRoleName = role.getName();
            }
        	roleSelect += "<option value=\"" + roleId + "\"" + selected +">" + role.getName() + "</option>";
        }
        
        roleSelect += "</select>";
        roleSelect += "<p hidden id=\"hidden_user_"+ usr.getId() + "_role\">" + selectedRoleName + "</p>";
        
		return roleSelect;
	}
	
	public String getSiteSelect() {
		User currentUser = (User) ServletActionContext.getRequest().getSession().getAttribute(CtdbConstants.USER_SESSION_KEY);
		ProtocolRoleUser usr = (ProtocolRoleUser) getObject();		
		Protocol study  = (Protocol) ServletActionContext.getRequest().getAttribute("study");
		String disabled = "";
		String siteSelect = "";
        String selected;

        if (study.getStudySites() != null && study.getStudySites().size() > 0) {

        	List<String> seletedSiteName = new ArrayList<String>();
        	for (Site studySite : study.getStudySites()) {
        		if (usr.getSites() != null &&  usr.getSites().contains(studySite)) {
                	seletedSiteName.add(studySite.getName());
                }
        	}
        	String defaultOption = "<option value=\"0\">-- Select Site --</option>";
        	if (usr.getRole() == null) {
            	disabled = " disabled=\"disabled\"";
            } else {
            	if (seletedSiteName.size() == 0) {
            		defaultOption = "<option value=\"0\"" + " selected=\"selected\">-- Select Site --</option>";
            	}
            }
    		if ( currentUser.getUsername().equalsIgnoreCase(usr.getUsername()) ) {
    			disabled = " disabled=\"disabled\"";
    		}
    		siteSelect = "<select multiple=\"multiple\" class=\"multiSiteSelect\" id=\"user_" + usr.getId() + "_site\" name=\"user_" + usr.getId() + "_site\"" + disabled +"onChange=\" siteChangedMultiple(this)\">";
    		siteSelect += defaultOption;
            for (Site studySite : study.getStudySites()) {
            	int studySiteId = studySite.getId();
            	
                selected = "";
                disabled = "";
				if (usr.getSites() != null &&  usr.getSites().contains(studySite)) {
                	selected = " selected=\"selected\"";
                }
            	siteSelect += "<option value=\"" + studySiteId + "\" " + selected +">" + studySite.getName() + "</option>";
            }
            	siteSelect += "</select>";
            
            for (int k=0;k<seletedSiteName.size();k++) {
            	siteSelect += "<p hidden id=\"hidden_user_"+ usr.getId() + "_site\">" + seletedSiteName.get(k) + "</p>";
            }
            
        }
        
        siteSelect +="<script> $(\".multiSiteSelect\").select2() </script>";
        
		return siteSelect;
	}
}

package gov.nih.nichd.ctdb.protocol.domain;

import gov.nih.nichd.ctdb.security.domain.Role;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.site.domain.Site;

public class ProtocolRoleUser extends User {
	private static final long serialVersionUID = 4774687085227920569L;
	private Role role;
	private Site site;
	private Protocol study;
	
	public ProtocolRoleUser() {
		
	}
	
	public ProtocolRoleUser(User user) {
		setUser(user);
	}
	
	public ProtocolRoleUser(User user, Role role, Site site, Protocol study) {
		setUser(user);
		setRole(role);
		setSite(site);
		setStudy(study);
	}
	
	public Role getRole() {
		return role;
	}
	public void setRole(Role role) {
		this.role = role;
	}
	public Site getSite() {
		return site;
	}
	public void setSite(Site site) {
		this.site = site;
	}
	public Protocol getStudy() {
		return study;
	}
	public void setStudy(Protocol study) {
		this.study = study;
	}
	
    public void setUser(User user)
    {
        this.setId(user.getId());
        this.setUsername(user.getUsername());
        this.setPassword(user.getPassword());
        this.setFirstName(user.getFirstName());
        this.setMiddleName(user.getMiddleName());
        this.setLastName(user.getLastName());
        this.setOffice(user.getOffice());
        this.setPhoneNumber(user.getPhoneNumber());
        this.setEmail(user.getEmail());
        this.setSysAdmin(user.isSysAdmin());
        this.setPasswordExpired(user.isPasswordExpired());
        this.setRoleList(user.getRoleList());
        this.setRoleMap(user.getRoleMap());
        this.setUseTreeView(user.isUseTreeView());
        this.setDisplayName(user.getDisplayName());
        this.setStaff(user.isStaff());
        this.setProtocolSiteMap(user.getProtocolSiteMap());
        this.setInstituteId(user.getInstituteId());
        this.setEditPasswords(user.isEditPasswords());
        this.setRefreshCtss(user.isRefreshCtss());
        this.setVbrAdmin(user.isVbrAdmin());
        this.setCtdbLookupStringForDisplay(user.getCtdbLookupStringForDisplay());
        this.setCreatedBy(user.getCreatedBy());
        this.setCreatedDate(user.getCreatedDate());
        this.setUpdatedBy(user.getUpdatedBy());
        this.setUpdatedDate(user.getUpdatedDate());
    }
    
    public int compareTo(ProtocolRoleUser compareO) throws ClassCastException {
    	if (role == null) {
    		if (compareO.getRole() == null) {
    			// all else being equal, sort by person's name
    			return getFullName().toLowerCase().compareTo(compareO.getFullName().toLowerCase());
    		}
    		return 1;
    	}
    	else {
    		if (compareO.getRole() == null) {
    			return -1;
    		}
        	if (role.getId() > compareO.getRole().getId()) {
        		return -1;
        	}
    	}
    	return 1;
    }
}

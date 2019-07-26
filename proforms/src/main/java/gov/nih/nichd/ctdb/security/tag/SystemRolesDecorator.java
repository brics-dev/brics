package gov.nih.nichd.ctdb.security.tag;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.security.domain.Role;

public class SystemRolesDecorator extends ActionDecorator {
	public SystemRolesDecorator() {
		super();
	}
	
	public String getRoleCheckbox() {
		Role domainObject = (Role) this.getObject();
		return "<input type='checkbox' name='selectRoleId' id=\"" + domainObject.getId() + "\" onclick=\"\" />";
	}
}

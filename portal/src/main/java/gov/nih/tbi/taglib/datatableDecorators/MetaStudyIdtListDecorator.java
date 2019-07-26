package gov.nih.tbi.taglib.datatableDecorators;

import java.util.HashMap;

import org.apache.struts2.ServletActionContext;
import org.apache.taglibs.display.Decorator;

import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.MetaStudyStatus;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.idt.ws.IdtDecorator;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;

import static org.apache.commons.lang.StringEscapeUtils.escapeHtml;

public class MetaStudyIdtListDecorator extends IdtDecorator {
	EntityMap permission;
	
	public String initRow(Object obj, int rowIndex) {
		String feedback = super.initRow(obj, rowIndex);
		
		MetaStudy metaStudy = (MetaStudy) this.getObject();
		
		@SuppressWarnings("unchecked")
		HashMap<Long, EntityMap> permissionList =
				(HashMap<Long, EntityMap>) ServletActionContext.getRequest().getAttribute("permissionList");
		if (permissionList != null && permissionList.containsKey(metaStudy.getId())) {
			permission = permissionList.get(metaStudy.getId());
		}
		else {
			permission = new EntityMap();
			permission.setPermission(PermissionType.ADMIN);
		}
		return feedback;
	}

	public String getTitleLink() {
		MetaStudy metaStudy = (MetaStudy) this.getObject();
		String title = metaStudy.getTitle();
		String output = "<a href=\"/portal/metastudy/metaStudyAction!view.action?metaStudyId=" + metaStudy.getId() + "\">";
		output += stringToEscape(title);
		output += "</a>";
		return output;
	}
	
	public String getPermissionName() {
		if(permission.getPermission().equals(PermissionType.READ) && permission.getType().equals(EntityType.META_STUDY)){
			return PermissionType.OWNER.name();
		}
		return permission.getPermission().getName();
	}
	
	public String getStatusText() {
		MetaStudy metaStudy = (MetaStudy) this.getObject();
		MetaStudyStatus status = metaStudy.getStatus();
		return status.getName();
	}
	
	/*
	 * escape HTML for display
	 */
	private String stringToEscape(String title){
		title = escapeHtml(title);
		return title;
	}
}

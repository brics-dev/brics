package gov.nih.tbi.taglib.datatableDecorators;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.taglibs.display.Decorator;

import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.repository.model.hibernate.Study;

public class StudyReportingListDecorator extends Decorator {
	EntityMap permission;
	
	static Logger logger = Logger.getLogger(StudyReportingListDecorator.class);
	
	public String initRow(Object obj, int viewIndex, int listIndex) {
		String feedback = super.initRow(obj, viewIndex, listIndex);
		
		Study study = (Study) this.getObject();
		HashMap<Long, EntityMap> permissionList =
				(HashMap<Long, EntityMap>) this.getPageContext().findAttribute("permissionList");
		if (permissionList != null && permissionList.containsKey(study.getId())) {
			permission = permissionList.get(study.getId());
		}
		else {
			permission = new EntityMap();
			permission.setPermission(PermissionType.ADMIN);
		}
		return feedback;
	}
	
	public String getStudyAdminLink() {
		Study study = (Study) this.getObject();
		String output = "";
		output = "<a href=\"/portal/studyAdmin/viewStudyAction!view.action?studyId=" + study.getPrefixedId() + "\">";
		output += study.getTitle();
		output += "</a>";
		return output;
	}
	
	public String getStudyNoAdminLink() {
		Study study = (Study) this.getObject();
		String output = "";
		output = "<a href=\"/portal/study/viewStudyAction!view.action?studyId=" + study.getPrefixedId() + "\">";
		output += study.getTitle();
		output += "</a>";
		return output;
	}
	
	public String getPermission() {
		return permission.getPermission().getName();
	}
	
	public String getDataTypes() {
		Study study = (Study) this.getObject();
		String output = "<span class=\"nobreak\">";
		String description = "";
		
		if (study.getIsGenomic()) {
			output += "<img src=\"/portal/images/brics/study/icon_genomics.png\" title=\"Omics Data\" />";
			description += "genomics ";
		}
		else {
			output += "<img src=\"/portal/images/brics/study/icon_genomics_disabled.png\" title=\"Omics Data\" />";
		}
		
		if (study.getIsClinical()) {
			output += "<img src=\"/portal/images/brics/study/icon_clinical_assesment.png\" title=\"Clinical Assessments\" />";
			description += "clinical assessment ";
		}
		else {
			output += "<img src=\"/portal/images/brics/study/icon_clinical_assesment_disabled.png\" title=\"Clinical Assessments\" />";
		}
		
		if (study.getIsImaging()) {
			output += "<img src=\"/portal/images/brics/study/icon_imaging.png\" title=\"Imaging Data\" />";
			description += "imaging ";
		}
		else {
			output += "<img src=\"/portal/images/brics/study/icon_imaging_disabled.png\" title=\"Imaging Data\" />";
		}

		output += "</span><span class=\"hidden\">" + description + "</span>";
		
		if (description == "") {
			output += "<span class=\"hidden\">no data</span>";
		}
		
		return output;
	}
	
	public String getStatus() {
		Study study = (Study) this.getObject();
		String output = "";
		if (study.getStudyStatus().getId() == 2) {
			output += "<span class=\"red-text\">";
			output += study.getStudyStatus().getName();
			output += "</span>";
		}
		else {
			output += study.getStudyStatus().getName();
		}
		return output;
	}
	
	public String getOwner() {
		Study study = (Study) this.getObject();
		String ownerName = "";
		if (permission != null) {
			try {
				ownerName = permission.getAccount().getUser().getFullName();
			}
			catch(Exception e) {
				ownerName = "";
				logger.error("The study with ID " + String.valueOf(study.getId()) + " has no owner!");
			}
			
			if (ownerName == null) {
				ownerName = "";
			}
		}
		return ownerName;
	}
}

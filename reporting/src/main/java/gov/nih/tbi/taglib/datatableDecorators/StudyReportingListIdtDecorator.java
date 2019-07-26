package gov.nih.tbi.taglib.datatableDecorators;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;

import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.idt.ws.IdtDecorator;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.StudyKeyword;

public class StudyReportingListIdtDecorator extends IdtDecorator {
	EntityMap permission;

	static Logger logger = Logger.getLogger(StudyReportingListIdtDecorator.class);

	public String initRow(Object obj, int rowIndex) {
		String feedback = super.initRow(obj, rowIndex);

		Study study = (Study) this.getObject();

		@SuppressWarnings("unchecked")
		HashMap<Long, EntityMap> permissionList =
				(HashMap<Long, EntityMap>) ServletActionContext.getRequest().getAttribute("permissionList");
		if (permissionList != null && permissionList.containsKey(study.getId())) {
			permission = permissionList.get(study.getId());
		} else {
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





	public String getStatus() {
		Study study = (Study) this.getObject();
		String output = "";
		if (study.getStudyStatus() != null && study.getStudyStatus().getId() == 2) {
			output += "<span class=\"red-text\">";
			output += study.getStudyStatus().getName();
			output += "</span>";
		} else {
			output += study.getStudyStatus().getName();
		}
		return output;
	}
	
	
	public String getStudyTyp() {
		Study study = (Study) this.getObject();
		String output = "";
		if(study.getStudyType() != null) {
			output += study.getStudyType().getName();
		}
		return output;
	}
	
	public String getStudykeyword() {
		Study study = (Study) this.getObject();
		String output = "";
		StringBuffer keyWord = new StringBuffer();
		for(StudyKeyword word : study.getKeywordSet()) {
			keyWord.append(word.getKeyword()).append(",");
		}
		
		output += keyWord.toString();
		return output;
	}

	
	public String getFundSrc() {
		Study study = (Study) this.getObject();
		String output = "";
		if(study.getFundingSource() != null) {
			output += study.getFundingSource().getName();
		}
		return output;
	}
	
	public String getRecruitmentStatus() {
		Study study = (Study) this.getObject();
		String output = "";
		if(study.getRecruitmentStatus() != null) {
			output += study.getRecruitmentStatus().getName();
		}
		return output;
	}
}

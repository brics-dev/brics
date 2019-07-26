package gov.nih.tbi.taglib.datatableDecorators;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.struts2.ServletActionContext;

import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.idt.ws.IdtDecorator;
import gov.nih.tbi.repository.model.hibernate.BasicStudySearch;
import gov.nih.tbi.repository.model.hibernate.ResearchManagement;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.model.hibernate.StudyKeyword;

public class StudyListIdtDecorator extends IdtDecorator {
	EntityMap permission;

	List<BasicStudySearch> basicStudySearches;

	static Logger logger = Logger.getLogger(StudyListIdtDecorator.class);

	private static BigInteger ZERO  = BigInteger.ZERO;

	public StudyListIdtDecorator(List<BasicStudySearch> basicStudySearches){
		super();
		this.basicStudySearches = basicStudySearches;
	}

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
		} else {
			output += "<img src=\"/portal/images/brics/study/icon_genomics_disabled.png\" title=\"Omics Data\" />";
		}

		if (study.getIsClinical()) {
			output +=
					"<img src=\"/portal/images/brics/study/icon_clinical_assesment.png\" title=\"Clinical Assessments\" />";
			description += "clinical assessment ";
		} else {
			output +=
					"<img src=\"/portal/images/brics/study/icon_clinical_assesment_disabled.png\" title=\"Clinical Assessments\" />";
		}

		if (study.getIsImaging()) {
			output += "<img src=\"/portal/images/brics/study/icon_imaging.png\" title=\"Imaging Data\" />";
			description += "imaging ";
		} else {
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
		} else {
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
			} catch (Exception e) {
				ownerName = "";
				logger.error("The study with ID " + String.valueOf(study.getId()) + " has no owner!");
			}

			if (ownerName == null) {
				ownerName = "";
			}
		}
		return ownerName;
	}

	public String getFundingSource(){

		Study study = (Study) this.getObject();
		if(study.getFundingSource() == null){
			return "";
		}
		return study.getFundingSource().getName();
	}

	public String getOrganization() {
		Study study = (Study) this.getObject();
		for (ResearchManagement researcher : study.getResearchMgmtSet()) {
			String pi = study.getPrincipalInvestigator();
			if (pi != null && pi.equals(researcher.getFullName())) {
				if(researcher.getOrgName() != null){
					return researcher.getOrgName();
				}
			}
		}
		return "";
	}

	public String getSharedData() {
		Study study = (Study) this.getObject();
		for(BasicStudySearch basicStudySearch: basicStudySearches){
			if(study.getTitle().equals(basicStudySearch.getTitle())){
					if(basicStudySearch.getSharedDatasetCount().compareTo(ZERO) > 0){
						return "Y";
					}
			}
		}
		return "N";
	}
	
	public String getStudyType() {
		Study study = (Study) this.getObject();
		if(study != null && study.getStudyType() != null){
		return study.getStudyType().getName();
		}
		return "";
	}
	

	public String getStudyKeywords() {
	    
		Study study = (Study) this.getObject();
	      
		Set<StudyKeyword> availableKeywords = study.getKeywordSet();
		List<String> searchedKeywords = new ArrayList<String>();  
		 if (availableKeywords == null || availableKeywords.isEmpty()){
			 return "";
		 }	
				for (StudyKeyword keyword : availableKeywords) {
					  searchedKeywords.add(keyword.getKeyword()); 
				 }	 
		return  searchedKeywords.toString();
	}
}

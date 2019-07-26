package gov.nih.tbi.taglib.datatableDecorators;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.nih.tbi.idt.ws.IdtDecorator;
import gov.nih.tbi.metastudy.model.hibernate.ResearchManagementMeta;
import gov.nih.tbi.repository.model.hibernate.ResearchManagement;

public class ResearchMgmtMetaListDecorator extends IdtDecorator {

	public String getRemoveLink() {

		ResearchManagement resMgmt = (ResearchManagement) this.getObject();
		Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
		String output = "";
		
		try {
			String jsonStr = gson.toJson(resMgmt);
			output = "<a href='javascript:;' onclick='removeResearchMgmt(" + jsonStr + ")'>Remove</a>";
		} catch (Exception e) {
			e.printStackTrace();
		}

		return output;
	}
	
	public String getRoleTitle() {
		ResearchManagementMeta resMgmt = (ResearchManagementMeta) obj;
		
		String output ="<span style=\"display:none\">"+resMgmt.getRole().getId()+"</span>"+resMgmt.getRole().getName();
		return output;
	}

}
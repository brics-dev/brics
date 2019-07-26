package gov.nih.tbi.taglib.datatableDecorators;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.nih.tbi.idt.ws.IdtDecorator;
import gov.nih.tbi.repository.model.hibernate.ResearchManagement;

public class ResearchMgmtIdtListDecorator extends IdtDecorator {
	public String getRemoveLink() {
		String output = "";
		if (obj instanceof ResearchManagement) {
			ResearchManagement resMgmt = (ResearchManagement) obj;
			Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

			try {
				String jsonStr = gson.toJson(resMgmt);
				output = "<a href='javascript:;' onclick='removeResearchMgmt(" + jsonStr + ")'>Remove</a>";
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return output;
	}
	
	public String getRoleTitle() {
		ResearchManagement resMgmt = (ResearchManagement) obj;
		
		String output ="<span style=\"display:none\">"+resMgmt.getRole().getId()+"</span>"+resMgmt.getRole().getName();
		return output;
	}
}

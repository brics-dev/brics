package gov.nih.tbi.taglib.datatableDecorators;

import org.apache.taglibs.display.Decorator;

import com.google.gson.Gson;

import gov.nih.tbi.metastudy.model.hibernate.MetaStudyGrant;

public class MetaStudyGrantListDecorator extends Decorator {

	public String getGrantId() {
		
		MetaStudyGrant grant = (MetaStudyGrant) this.getObject();
		String output = "<a href=\"javascript:viewGrantInfo('" + grant.getGrantId() + "');\">" + grant.getGrantId() + "</a>";
		return output;
	}
	
	public String getRemoveLink() {
		
		MetaStudyGrant grant = (MetaStudyGrant) this.getObject();
		Gson gson = new Gson();
		String jsonStr = gson.toJson(grant);
		
		String output = "<a href='javascript:;' onclick='removeGrant(" + jsonStr + ")'>Remove</a>";
		return output;
	}


}

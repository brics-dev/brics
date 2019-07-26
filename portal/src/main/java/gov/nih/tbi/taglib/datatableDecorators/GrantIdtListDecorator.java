package gov.nih.tbi.taglib.datatableDecorators;

import gov.nih.tbi.idt.ws.IdtDecorator;
import gov.nih.tbi.repository.model.hibernate.Grant;

import com.google.gson.Gson;

public class GrantIdtListDecorator extends IdtDecorator {

	public String getGrantId() {

		Grant grant = (Grant) this.getObject();
		String output =
				"<a href=\"javascript:viewGrantInfo('" + grant.getGrantId() + "');\">" + grant.getGrantId() + "</a>";
		return output;
	}

	public String getRemoveLink() {

		Grant grant = (Grant) this.getObject();
		Gson gson = new Gson();
		String jsonStr = gson.toJson(grant);

		String output = "<a href='javascript:;' onclick='removeGrant(" + jsonStr + ")'>Remove</a>";
		return output;
	}

}

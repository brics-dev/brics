package gov.nih.tbi.taglib.datatableDecorators;

import gov.nih.tbi.idt.ws.IdtDecorator;
import gov.nih.tbi.repository.model.hibernate.StudySponsorInfo;

import com.google.gson.Gson;

public class SponsorInfoIdtListDecorator extends IdtDecorator {

	public String getRemoveLink() {

		StudySponsorInfo sponsorInfo = (StudySponsorInfo) getObject();

		Gson gson = new Gson();
		String jsonStr = gson.toJson(sponsorInfo);

		String output = "<a href='javascript:;' onclick='removeSponsorInfo(" + jsonStr + ")'>Remove</a>";
		return output;
	}

}

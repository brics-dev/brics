package gov.nih.tbi.taglib.datatableDecorators;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.taglibs.display.Decorator;

import gov.nih.tbi.commons.model.GuidSubject;

public class GuidListDecorator extends Decorator {

	public String getGuidLink() {
		GuidSubject guid = (GuidSubject) this.getObject();
		String guidString = guid.getGuid();

		boolean inAdmin = (boolean) this.getPageContext().findAttribute("inAdmin");

		if (!inAdmin) {
			return "<a href=\"javascript:redirectWithReferrer(\'/portal/guid/guidAction!view.action?guid=" + guidString
					+ "\');\">" + guidString + "</a>";
		} else {
			return "<a href=\"javascript:redirectWithReferrer(\'/portal/guidAdmin/guidAction!view.action?guid="
					+ guidString + "\');\">" + guidString + "</a>";
		}
	}

	public String getFormattedDateRegistered() {
		String output = "";
		GuidSubject guid = (GuidSubject) this.getObject();
		Date dateCreated = guid.getDateCreated();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		if (dateCreated != null) {
			output = dateFormat.format(dateCreated);
		}
		return output;
	}
	
	public String getDisplayName() {
		GuidSubject guid = (GuidSubject) this.getObject();		
		return guid.getCreatedBy();
	}
}

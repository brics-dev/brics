package gov.nih.tbi.taglib.datatableDecorators;

import org.apache.taglibs.display.Decorator;

import gov.nih.tbi.metastudy.model.hibernate.MetaStudyClinicalTrial;

public class MetaStudyClinicalTrialListDecorator extends Decorator {

	public String getClinicalTrialId() {

		MetaStudyClinicalTrial ct = (MetaStudyClinicalTrial) this.getObject();
		String output =
				"<a href='javascript:;' onclick='viewClinicalTrial(\"" + ct.getClinicalTrialId() + "\")'>"
						+ ct.getClinicalTrialId() + "</a>";
		return output;
	}

	public String getRemoveLink() {

		MetaStudyClinicalTrial ct = (MetaStudyClinicalTrial) this.getObject();
		String output =
				"<a href='javascript:;' onclick='removeClinicalTrial(\"" + ct.getClinicalTrialId() + "\")'>Remove</a>";
		return output;
	}


}

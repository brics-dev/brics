package gov.nih.tbi.taglib.datatableDecorators;

import gov.nih.tbi.repository.model.hibernate.ClinicalTrial;

import org.apache.taglibs.display.Decorator;

public class ClinicalTrialListDecorator extends Decorator {

	public String getClinicalTrialId() {

		ClinicalTrial ct = (ClinicalTrial) this.getObject();
		String output =
				"<a href='javascript:;' onclick='viewClinicalTrial(\"" + ct.getClinicalTrialId() + "\")'>"
						+ ct.getClinicalTrialId() + "</a>";
		return output;
	}

	public String getRemoveLink() {

		ClinicalTrial ct = (ClinicalTrial) this.getObject();
		String output =
				"<a href='javascript:;' onclick='removeClinicalTrial(\"" + ct.getClinicalTrialId() + "\")'>Remove</a>";
		return output;
	}

}

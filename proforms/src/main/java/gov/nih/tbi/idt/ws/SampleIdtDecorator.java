package gov.nih.tbi.idt.ws;

import gov.nih.nichd.ctdb.form.domain.Form;

public class SampleIdtDecorator extends IdtDecorator {
	public String getName() {
		if (obj instanceof Form) {
			Form instance = (Form)obj;
			return instance.getName() + "_dec";
		}
		return "__none__";
	}
}

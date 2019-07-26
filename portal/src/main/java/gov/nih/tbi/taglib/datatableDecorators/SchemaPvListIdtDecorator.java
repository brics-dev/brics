package gov.nih.tbi.taglib.datatableDecorators;

import gov.nih.tbi.dictionary.model.hibernate.SchemaPv;
import gov.nih.tbi.idt.ws.IdtDecorator;

public class SchemaPvListIdtDecorator extends IdtDecorator {
	public String getValueRange() {
		SchemaPv schemaPv = (SchemaPv) this.getObject();
		return schemaPv.getValueRange() != null?schemaPv.getValueRange().getValueRange():"";
	}
	
	public String getValueRangeDescription() {
		SchemaPv schemaPv = (SchemaPv) this.getObject();
		return schemaPv.getValueRange() != null?schemaPv.getValueRange().getDescription():"";
	}
}

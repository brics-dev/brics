package gov.nih.tbi.taglib.datatableDecorators;

import org.apache.taglibs.display.Decorator;

import gov.nih.tbi.dictionary.model.hibernate.SchemaPv;

public class SchemaPvListDecorator extends Decorator {
	public String getValueRange() {
		SchemaPv schemaPv = (SchemaPv) this.getObject();
		return schemaPv.getValueRange() != null?schemaPv.getValueRange().getValueRange():"";
	}
	
	public String getValueRangeDescription() {
		SchemaPv schemaPv = (SchemaPv) this.getObject();
		return schemaPv.getValueRange() != null?schemaPv.getValueRange().getDescription():"";
	}
}

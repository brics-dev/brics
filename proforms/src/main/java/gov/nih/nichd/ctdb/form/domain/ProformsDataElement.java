package gov.nih.nichd.ctdb.form.domain;

import gov.nih.tbi.dictionary.model.hibernate.DataElement;

public class ProformsDataElement extends DataElement {
	
	
	//flag to let us know if this data element came from a group that is truly repeatbale (not repeats only 1)
	public Boolean groupRepeatable;
	
	public int order;

	public Boolean getGroupRepeatable() {
		return groupRepeatable;
	}

	public void setGroupRepeatable(Boolean groupRepeatable) {
		this.groupRepeatable = groupRepeatable;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}
}

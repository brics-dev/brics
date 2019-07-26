package gov.nih.nichd.ctdb.attachments.tag;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.tag.ActionDecorator;

public class AttachmentCategoryHomeDecorator extends ActionDecorator {
	public String getCheckbox() {
		int id = ((CtdbDomainObject) this.getObject()).getId();
		
		return "<input type=\"checkbox\" name=\"selectedIds\" value=\"" + id + "\" />";
	}
}

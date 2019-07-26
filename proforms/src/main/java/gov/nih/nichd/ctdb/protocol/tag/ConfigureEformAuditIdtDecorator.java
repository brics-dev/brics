package gov.nih.nichd.ctdb.protocol.tag;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.common.tag.ActionIdtDecorator;
import gov.nih.nichd.ctdb.protocol.domain.ConfigureEformAuditDetail;

public class ConfigureEformAuditIdtDecorator extends ActionIdtDecorator {

	public ConfigureEformAuditIdtDecorator() {
		super();
	}
	
	
	
	public String getQuestionText() {
		ConfigureEformAuditDetail auditDetail = (ConfigureEformAuditDetail)this.getObject();
		String questionText = auditDetail.getQuestionText();
		if(questionText == null) {
			return "";
		}else {
			return questionText;
		}
	}
	
}

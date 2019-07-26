package gov.nih.nichd.ctdb.workspace.tag;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;

public class ActiveFormsDecorator extends ActionDecorator {
	public ActiveFormsDecorator() {
		super();
	}
	
	public String getActiveFormIdCheckbox() {
		Form domainObject = (Form) this.getObject();
		return "<input type='checkbox' name='selectFormId' id=\"checkForm_" + domainObject.getId() + "\" onclick=\"\" />";
	}
	
	public String getFormLink() {
		Form domainObject = (Form) this.getObject();
		return "/response/dataEntryInProgress.do?formId=" + domainObject.getId();
	}
	
	public String getPatientId() {
		AdministeredForm domainObject = (AdministeredForm) this.getObject();
		Protocol proto = (Protocol) this.getPageContext().getSession().getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		if (proto.isUsePatientName()) {
			return domainObject.getPatient().getDisplayLabel();
		}
		else {
			return Integer.toString(domainObject.getPatient().getId());
		}
	}
}

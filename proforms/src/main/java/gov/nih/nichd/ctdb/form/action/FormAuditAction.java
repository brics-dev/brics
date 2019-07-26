package gov.nih.nichd.ctdb.form.action;

import java.util.List;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.form.common.FormConstants;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.manager.FormManager;

/**
 * The Struts Action class responsable for displaying the form
 * audit trail for the NICHD CTDB
 *
 * @author  Booz Allen Hamilton
 * @version 1.0
 */
public class FormAuditAction extends BaseAction {

	private static final long serialVersionUID = -9026822100090377076L;

	public String execute() throws Exception {
		try {
			String formId = request.getParameter(CtdbConstants.ID_REQUEST_ATTR);

			if (Utils.isBlank(formId)) {
				throw new Exception("Invalid Id passed to Form Audit Action");
			} else {
				FormManager fm = new FormManager();
				List<Form> versions = fm.getFormVersions(Integer.parseInt(formId));
				request.setAttribute("formversions", versions);
				Form form = versions.get(0);
				session.remove(FormConstants.FORMNAME);
				request.setAttribute(FormConstants.FORMNAME, form.getName());
			}
		} catch (CtdbException ce) {
			return StrutsConstants.FAILURE;
		}

		return SUCCESS;
	}
}

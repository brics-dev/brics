package gov.nih.nichd.ctdb.protocol.tag;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;

import javax.servlet.jsp.JspException;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Oct 16, 2007
 * Time: 7:23:19 AM
 * To change this template use File | Settings | File Templates.
 */
public class InprogressDecorator extends ActionDecorator {

    public String getInProgressAction () throws JspException {
        AdministeredForm af = (AdministeredForm) this.getObject();
        String str = "<a href='"+this.getWebRoot() + "/protocol/inProgressSelect.do?action="+ StrutsConstants.ACTION_EDIT_FORM;
        str += "&id="+af.getId()+"&protocolId="+af.getForm().getProtocolId()+"&formid="+af.getForm().getId()+"&patientId="+af.getPatient().getId()+"'>continue</a>&nbsp;&nbsp;";

        return str;

    }

    public String getPatientDec     () throws JspException {
        AdministeredForm af = (AdministeredForm) this.getObject();
        if (af.getForm().getProtocol().getPatientDisplayType() == CtdbConstants.PATIENT_DISPLAY_ID) {
            return af.getPatient().getSubjectId();
        } else {
            return af.getPatient().getLastName() + ", "+af.getPatient().getFirstName();
        }
    }
}

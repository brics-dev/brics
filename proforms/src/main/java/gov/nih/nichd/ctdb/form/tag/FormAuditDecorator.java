package gov.nih.nichd.ctdb.form.tag;

import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.common.tag.ActionDecorator;

import javax.servlet.jsp.JspException;

/**
 * FromAuditDecorator enables a table to have a column with Action links (Edit/View/..). This
 * class works with the <code>display</code> tag library.
 *
 * @author  Booz Allen Hamilton
 * @version 1.0
 */
public class FormAuditDecorator  extends ActionDecorator
{

    public String getVersionDec () throws JspException {
        Form form = (Form) this.getObject();

        int id = form.getId();
        String root = this.getWebRoot();
/*        String vString = "<a href=\"Javascript: popupWindow ('" + root + "/form/form.do?action=view_form&displayQids=true&source=formAudit&id=";
        vString += form.getId() + "&formVersion="+form.getVersion().getVersionNumber();
        vString += "');\">"+form.getVersion().toString()+"</a>";*/
        String vString = form.getVersion().toString();
        return vString;

    }

}

package gov.nih.nichd.ctdb.patient.tag;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.patient.domain.PatientRole;
import gov.nih.nichd.ctdb.patient.domain.PatientCategory;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;

import javax.servlet.jsp.JspException;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Nov 6, 2006
 * Time: 10:58:13 AM
 * To change this template use File | Settings | File Templates.
 */
public class PatientRoleDecorator extends ActionDecorator {

    public String getRoleDec () throws JspException {

        PatientCategory domainObject = (PatientCategory) this.getObject();
        int id = domainObject.getId();
        String root = this.getWebRoot();

        String role = "<div id='role_"+id+"_desc' >"+domainObject.getName()+"</div>";
        role += "<div id='role_"+id+"_action' style='display:none;'><input type='text' size=35 id='"+id+"' name='"+id+"' value="+domainObject.getName()+" style='font-size:11; border:1px ridge black;'";
        role+= " onkeyup=\"changeRole('"+id+"', '"+domainObject.getType() +"');\"  >";
        if (!domainObject.getType().equals("Role")) {
            role +="<input type='text' size=35 id='"+id+"_descr' name='"+id+"_desc' value="+domainObject.getDescription()+" style='font-size:11; border:1px ridge black;'";
        role+= " onkeyup=\"changeRole('"+id+"', '"+domainObject.getType() +"');\"  >";
        }
        role+="</div>";
        return role;
    }


    public String getActionDec () throws JspException {
        PatientCategory domainObject = (PatientCategory) this.getObject();
        int id = domainObject.getId();
        String root = this.getWebRoot();

        StringBuffer sb = new StringBuffer("<a href=\"Javascript:doNothing();\" onClick=\"editRole('"+id+"');\">edit</a>&nbsp;&nbsp;&nbsp;");
        sb.append("<a href='"+root+"/patient/patientRoles.do?PatientRole_type="+domainObject.getType()+"&action=process_delete&PatientRole_Id=").append(id).append("'>remove</a>&nbsp;&nbsp;&nbsp;");

        if (domainObject instanceof PatientRole  ){
            sb.append("<a href='"+root+"/patient/patientRoleFormExclusion.do?action=edit_form&roleId=").append(id).append("'>exclude&nbsp;forms</a>");
        }

       return sb.toString();
    }


}

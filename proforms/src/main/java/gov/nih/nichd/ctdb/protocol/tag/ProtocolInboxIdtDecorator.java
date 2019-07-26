package gov.nih.nichd.ctdb.protocol.tag;

import javax.servlet.jsp.JspException;

import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.tbi.commons.model.StudyType;
import gov.nih.tbi.idt.ws.IdtDecorator;


/**
 * ProtocolInboxDecorator enables a table to have a column with Action links
 * with information specific to protocols. This class works with the <code>display</code> tag library.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class ProtocolInboxIdtDecorator extends IdtDecorator {

	/** Creates a new instance of ProtocolListDecorator */
	public ProtocolInboxIdtDecorator() {
		super();
	}

	public String getSwitchStudyLink() throws JspException {
		String linkText = "";
		String root = SysPropUtil.getProperty("app.webroot");
		Protocol domainObject = (Protocol) this.getObject();
		String protocolId = Integer.toString(domainObject.getId());
		linkText = "<a href=\"" + root + "/dashboard.action?id="+protocolId+"\">"+domainObject.getProtocolNumber()+"</a>";
		return linkText;
	}
	
	public String getStudyTypeName() {
		StudyType st = ((Protocol) this.getObject()).getStudyType();
		
		return st != null ? st.getName() : "";
	}
	

}
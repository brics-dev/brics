package gov.nih.nichd.ctdb.protocol.tag;

import javax.servlet.jsp.JspException;

import gov.nih.nichd.ctdb.common.tag.ActionIdtDecorator;
import gov.nih.tbi.dictionary.model.hibernate.eform.BasicEform;

public class ConfigureEformHomeIdtDecorator extends ActionIdtDecorator {

	
		public ConfigureEformHomeIdtDecorator() {
			super();
		}
		
		public String getTitle() throws JspException {
			
			BasicEform basicEform = (BasicEform)this.getObject();
	        String url = this.getWebRoot() + "/form/viewFormDetail.action?source=popup&id=" + basicEform.getId();
	        String titleLink = "<a href=\"Javascript:popupWindowWithMenu('" + url + " ');\">" + basicEform.getShortName()+ "</a>";
	        return titleLink;
		}
}

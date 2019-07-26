package gov.nih.nichd.ctdb.protocol.tag;

import javax.servlet.jsp.JspException;

import gov.nih.nichd.ctdb.attachments.domain.Attachment;
import gov.nih.nichd.ctdb.common.tag.ActionDecorator;

public class StudyDocumentDecorator extends ActionDecorator
{
	public String getDocCheckbox()
	{
		Attachment a = (Attachment) getObject();
		String id = Long.toString(a.getId());
		
		return "<input type=\"checkbox\" name=\"docChkBoxes\" value=\"" + id + "\"";
	}
	
	public String getDownloadTitle() throws JspException
	{
		Attachment a = (Attachment) getObject();
		String html = "";
		
		if ( (a.getId() > 0) && (a.getFileName().length() > 0) )
		{
			html = "<a href=\"javascript:;\" onclick=\"downloadFile(" + Long.toString(a.getId()) + ", " + Long.toString(a.getAssociatedId()) +
					", " + Long.toString(a.getType().getId()) + ")\" >" + a.getName() + "</a>";
		}
		else
		{
			html = a.getName();
		}
		
		return html;
	}
	
	public String getDocType()
	{
		Attachment a = (Attachment) getObject();
		String typeName = "";
		
		switch ( a.getPublicationType().getId() )
		{
			case 10:
				typeName = "Abstract";
				break;
			case 12:
    			typeName = "Poster";
    			break;
    		case 14: 
    			typeName = "Manuscript";
    			break;
			default: 
    			typeName = "Other";
    			break;
		}
		
		return typeName;
	}
}

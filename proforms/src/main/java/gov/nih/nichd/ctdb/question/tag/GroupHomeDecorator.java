package gov.nih.nichd.ctdb.question.tag;

import javax.servlet.jsp.JspException;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.question.domain.Group;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

/**
 * GroupHomeDecorator enables a table to display question group
 * version information without client side java coding.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 * 
 * add by Ching-Heng
 */
public class GroupHomeDecorator extends ActionDecorator
{

    /**
     * Default Constructor
     */
    public GroupHomeDecorator()
    {
        // default contructor
        super();
    }
    
    /**
     * add by Chin-Heng
     * HTML string displaying the group name on a Row
     */
    public String getGroupName(){
    	Group group = (Group) this.getObject();
        String name = group.getName();
        return name;
    }
    public String getGroupIdCheckbox()
	{
    	Group group = (Group) this.getObject();
		int groupId = group.getId();
		//return "<input type='checkbox' name='selectProtocolId' id=\"" + protocolId + "\" onclick=\"validateSelectedProtocols(" +protocolId+ ")\"/>";		
		return "<input type='checkbox' name='questionGroupIds' id=\"" + groupId + "\" value=\""+groupId+"\">";
	}

    /**
     * Retrieves the description of a question group in a way that the description length
     * is limited to about 90 characters on display.
     *
     * @return  HTML string displaying the description on a Row
     */
    public String getDescr()
    {
        Group group = (Group) this.getObject();

        String description = group.getDescription();

        if(description == null)
            return description;

        int index = description.indexOf(" ", 90);

        if(index != -1)
            return description.substring(0, index) + "...";

        else
            return description;

    }

    /**
     * Returns DHTML for the Action column of the Question Groups
     *
     * @return  The actions allowed
     */
    public String getActionDec() throws JspException
    {
        Group domainObject = (Group) this.getObject();
        int id = domainObject.getId();
        String root = this.getWebRoot();
        StringBuffer text = new StringBuffer();
        text.append("<a href=\"" + root + "/question/group.do?action=edit_form&id=" + id + "\">edit</a>");
        text.append("&nbsp;&nbsp;<a href=\"" + root + "/question/groupQuestionSearch.do?action=add_form&id=" + id + "\">attach questions to group</a>");
        return text.toString();
    }
    
    // moved by Ching Heng from QuestionImageDecorator
    /**
     * Retrieves the checkbox value associated with the image.
     *
     * @return  HTML string displaying the image's checkbox on a Row
     */
    public String getCheckboxDec()
    {
        String currentName = (String) this.getObject();

        return "<input type=\"checkbox\" name=\"namesToDelete\" value=\"" + currentName + "\">";
    }

    /**
     * Retrieves the number associated with the image.
     *
     * @return  HTML string displaying the image's number on a Row
     */
    public String getNumberDec() throws JspException
    {
        int idx = this.getListIndex();
        String currentName = (String) this.getObject();
        String root = this.getWebRoot();
        String imageroot = SysPropUtil.getProperty("filesystem.directory.questionimagepath");

        StringBuffer html = new StringBuffer(800);

        html.append("<a href=\"javascript:doNothing();\"");
        html.append("target=\"_blank\" onclick=\"goImgWin('" + root + "/" + imageroot + "/" + currentName);
        html.append("',240,302,100,50);return false;\">");
        html.append(idx + 1);
        html.append("</a>");

        return html.toString();
    }

    /**
     * Retrieves the thumbnail associated with the image.
     *
     * @return  HTML string displaying the image's thumbnail on a Row
     */
    public String getThumbnailDec() throws JspException
    {
        String currentName = (String) this.getObject();
        String root = this.getWebRoot();
        String imageroot = SysPropUtil.getProperty("filesystem.directory.questionimagepath");

        StringBuffer html = new StringBuffer(800);
        html.append("<a href=\"javascript:doNothing();\"");
        html.append("target=\"_blank\" onclick=\"goImgWin('" + root + "/" + imageroot + "/" + currentName);
        html.append("',240,302,100,50);return false;\">");
        html.append("<img height=\"60\" width=\"60\" border=\"0\" src=\"" + root + "/" + imageroot + "/" + currentName);
        html.append("\"/>");
        html.append("</a>");

        return html.toString();
    }
    
    
}

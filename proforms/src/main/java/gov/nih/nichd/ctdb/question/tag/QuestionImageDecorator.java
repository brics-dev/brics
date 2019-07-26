package gov.nih.nichd.ctdb.question.tag;

import javax.servlet.jsp.JspException;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;



/**
 * QuestionImageDecorator prepares table column in question image page. This
 * class works with the <code>display</code> tag library.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class QuestionImageDecorator extends ActionDecorator
{
	
	/**
     * Default Constructor
     */
    public QuestionImageDecorator()
    {
        super();
    }

    /**
     * Retrieves the checkbox value associated with the image.
     *
     * @return  HTML string displaying the image's checkbox on a Row
     */
    public String getCheckboxDec()
    {
        String currentName = (String) this.getObject();

        return "<input type=\"checkbox\" name=\"namesToDelete\" value=\"" + currentName + "\"  id=\""+currentName+"\">";
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

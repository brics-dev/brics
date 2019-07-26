package gov.nih.nichd.ctdb.response.tag;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.response.domain.DataEntryAssArch;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

/**
 * ViewEditAnswerDecorator enables a table to have a column with Action links
 * This class works with the <code>display</code> tag library.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class ViewAssignmentDecorator extends ActionDecorator
{

    /**
     * Default Constructor
     */
    public ViewAssignmentDecorator()
    {
        super();
    }
    
    /**
     * Retrieves DataEntrySession
     *
     * @return  HTML string displaying the DataEntrySession.
     */
    public String getDataEntryFlag()
	{
		DataEntryAssArch dataEntryAssArch = (DataEntryAssArch) this.getObject();
        return Integer.toString(dataEntryAssArch.getDataEntryFlag());
    }
    
    /**
     * Retrieves Previous Data Entry user By Name
     *
     * @return  HTML string displaying the Previous Data Entry user By Name.
     */
    public String getPreviousByName()
	{
		DataEntryAssArch dataEntryAssArch = (DataEntryAssArch) this.getObject();
        return dataEntryAssArch.getPreviousByName();
    }

    /**
     * Retrieves Current Data Entry user By Name
     *
     * @return  HTML string displaying the Current Data Entry user By Name.
     */
    public String getCurrentByName()
	{
		DataEntryAssArch dataEntryAssArch = (DataEntryAssArch) this.getObject();
        return dataEntryAssArch.getCurrentByName();
    }

    /**
     * Retrieves The user By Name, who did this re-assignment Data Entry
     *
     * @return  HTML string displaying The user By Name, who did this re-assignment Data Entry.
     */
    public String getAssignedByName()
	{
		DataEntryAssArch dataEntryAssArch = (DataEntryAssArch) this.getObject();
        return dataEntryAssArch.getAssignedByName();
    }

    /**
     * Retrieves re-Assigned Date
     *
     * @return  HTML string displaying the re-Assigned Date.  If date is null,
     *			return empty string.
     */   
    public String getAssignedDate()
    {
		DataEntryAssArch dataEntryAssArch = (DataEntryAssArch) this.getObject();
        Timestamp date = dataEntryAssArch.getAssignedDate();
        if(date != null)
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat(SysPropUtil.getProperty("default.system.dateformat"));
            SimpleDateFormat timeFormat = new SimpleDateFormat(SysPropUtil.getProperty("default.system.timeformat"));
            return dateFormat.format(date) + "<br>" + timeFormat.format(date);
        }
        else
            return "";
    }
}

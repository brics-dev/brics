package gov.nih.nichd.ctdb.response.tag;

import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspException;

import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.response.domain.PatientCalendarCellResponse;
import gov.nih.nichd.ctdb.response.domain.Response;

/**
 * DoubleKeyDataEntryDecorator is used to control data display that
 *		are obtained from double key data entry.
 * This class works with the <code>display</code> tag library.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class CalendarCellEditDecorator extends DoubleKeyDataEntryDecorator
{
    private final String tableString = "<table width=\"100%\" cellspacing=0 cellpadding=0>";
    private final String startString = "<tr><td class=\"tableCell\" width=\"130\" valign=\"top\" align=\"right\">";
    private final String middleString = "</td><td class=\"tableCell\" valign=\"top\" align=\"left\">";
    private final String endString = "</td></tr>";
    private final String closeString = "</table>";

    /**
     * Default Constructor
     */
    public CalendarCellEditDecorator()
    {
        super();
    }

    /**
     * Retrieves the string that will be displayed as a checkbox for 
     * setting answer to null.
     *
     * @return  HTML string displaying the data as a unit on a row.
     */
    public String getSetBlank() throws JspException
    {   
        PatientCalendarCellResponse response = (PatientCalendarCellResponse) this.getObject();
        Question q = response.getQuestion();
        String qId = "bk_" + Integer.toString(q.getId());
        List<String> answers = response.getAnswers();
        List<String> editedAnswers = response.getEditAnswers();
        boolean checked = false;
        
        if (!editedAnswers.isEmpty())
        {
        	for (String dummy : editedAnswers)
        	{
        		if (dummy.length() == 0)
        		{
        			checked = true;
        			break;
        		}
        	}
        }

		if ( q.getFormQuestionAttributes().isRequired() || answers.isEmpty())
			return "";
		else
		{
        	StringBuffer text = new StringBuffer();
        	
				text.append("<input type=\"checkbox\" id=\"" + qId + "\" name=\"" + qId + "\" value=\"" + qId + "\"");
				
        		if (checked)
        		{
        			text.append("checked");
        		}
        			
				text.append(">");

			return text.toString();
		}
    }

    /**
     * Retrieves the all data from double key data entry for final edit.
     *
     * @return  HTML string displaying the data as a unit on a row.
     */
    public String getResponseEdit() throws JspException
    {   
        PatientCalendarCellResponse response = (PatientCalendarCellResponse) this.getObject();
        Question q = response.getQuestion();
        String qId = Integer.toString(q.getId());
        Response r1 = response.getResponse1();
        List<String> answers1 = r1.getAnswers();
        List<String> editedAnswers = response.getEditAnswers();
        Response r2 = response.getResponse2();
        List<String> answers2 = r2.getAnswers();
        String reason = response.getEditReason();
        List<String> answers = response.getAnswers();
        StringBuffer text = new StringBuffer(200);
    
        text.append(tableString);
        text.append(startString);
        text.append("Row:&nbsp;&nbsp;&nbsp;&nbsp;");
        text.append(middleString);
        // add one to shift center from 0,0 to 1,1
        text.append("<div id=\"this_row\">");
        text.append(response.getRow() + 1);
        text.append("</div>"); 
        text.append(endString);
        text.append(startString);
        text.append("Period:&nbsp;&nbsp;&nbsp;&nbsp;");
        text.append(middleString);
        text.append("<div id=\"this_col\">");
        text.append(response.getCol() + 1);
        text.append("</div>");
        text.append(endString);        
        text.append(startString);

        text.append("Data Entry 1 Answer:&nbsp;&nbsp;&nbsp;&nbsp;");

        text.append(middleString);
        
        if (answers1.size() > 1)
        {
        	for (Iterator<String> it = answers1.iterator(); it.hasNext();)
            {
                text.append(it.next());
                
                if (it.hasNext())
                {
                    text.append("; ");
                }
            }
        }
        else if (!answers1.isEmpty())
        {
        	text.append(answers1.get(0));
        }
        
        text.append(endString);
        text.append(startString);
        text.append("Data Entry 2 Answer:&nbsp;&nbsp;&nbsp;&nbsp;");
        text.append(middleString);
        
        if (answers2.size() > 1)
        {
        	for (Iterator<String> it = answers2.iterator(); it.hasNext();)
            {
                text.append(it.next());
                
                if (it.hasNext())
                {
                    text.append("; ");
                }
            }
        }
        else if (!answers2.isEmpty())
        {
        	text.append(answers2.get(0));
        }
        
        text.append(endString);

        text.append(startString);
        text.append("Final Answer:&nbsp;&nbsp;&nbsp;&nbsp;");
        text.append(middleString);
        
        if (answers.size() > 1)
        {
        	for (Iterator<String> it = answers.iterator(); it.hasNext();)
            {
                text.append(it.next());
                
                if (it.hasNext())
                {
                    text.append("; ");
                }
            }
        }
        else if (!answers.isEmpty())
        {
        	text.append(answers.get(0));
        }
        
        text.append(endString);
        text.append(startString);
        text.append("Edit:&nbsp;&nbsp;&nbsp;&nbsp;");
        text.append(middleString);

        text.append("<input ");

        text.append("type=\"text\" name=\"");
        text.append(qId);
        text.append("\" id=\"");
        text.append(qId);
        text.append("\" size=\"25\" value=\"");
        
        if (!editedAnswers.isEmpty())
        {
            text.append(editedAnswers.get(0));
        }

        text.append("\">");
            
        text.append(endString);
        text.append(startString);
        
        text.append("Reason for Change:&nbsp;&nbsp;&nbsp;&nbsp;");
        String reasonId = "reason_" + qId;
        text.append(middleString);
        text.append("<textarea name=\"");
        text.append(reasonId);
        text.append("\" id=\"");
        text.append(reasonId);
        text.append("\" cols=\"50\" rows=\"5\">");
        
        if (reason != null && reason.length() > 0)
        {
        	text.append(reason);
        }
        	
        text.append("</textarea>");
        text.append(endString);
        text.append(closeString);
   
        return text.toString();
    }
    
    /**
     * Retrieves the all data from double key data entry for final edit.
     *
     * @return  HTML string displaying the data as a unit on a row.
     */
    public String getTwoAnswers() throws JspException
    {   
        PatientCalendarCellResponse response = (PatientCalendarCellResponse) this.getObject();
        Question q = response.getQuestion();
        String qId = Integer.toString(q.getId());
        Response r1 = response.getResponse1();
        List<String> answers1 = r1.getAnswers();
        List<String> editedAnswers = response.getEditAnswers();
        Response r2 = response.getResponse2();
        List<String> answers2 = r2.getAnswers();
        StringBuffer text = new StringBuffer(200);
    
        text.append(tableString);
        text.append(startString);
        text.append("Row:&nbsp;&nbsp;&nbsp;&nbsp;");
        text.append(middleString);
        // add one to shift center from 0,0 to 1,1
        text.append("<div id=\"this_row\">");
        text.append(response.getRow() + 1);
        text.append("</div>"); 
        text.append(endString);
        text.append(startString);
        text.append("Period:&nbsp;&nbsp;&nbsp;&nbsp;");
        text.append(middleString);
        text.append("<div id=\"this_col\">");
        text.append(response.getCol() + 1);
        text.append("</div>");
        text.append(endString);        
        text.append(startString);

        text.append("Data Entry 1 Answer:&nbsp;&nbsp;&nbsp;&nbsp;");

        text.append(middleString);
        
        if (answers1.size() > 2)
        {
        	for (Iterator<String> it = answers1.iterator(); it.hasNext();)
            {
                text.append(it.next());
                
                if (it.hasNext())
                {
                    text.append("; ");
                }
            }
        }
        else if (!answers1.isEmpty())
        {
        	text.append(answers1.get(0));
        }
        
        text.append(endString);
        text.append(startString);
        text.append("Data Entry 2 Answer:&nbsp;&nbsp;&nbsp;&nbsp;");
        text.append(middleString);
        
        if (answers2.size() > 1)
        {
        	for (Iterator<String> it = answers2.iterator(); it.hasNext();)
            {
                text.append(it.next());
                
                if (it.hasNext())
                {
                    text.append("; ");
                }
            }
        }
        else if (!answers2.isEmpty())
        {
        	text.append(answers2.get(0));
        }
        
        text.append(endString);

        text.append(startString);
        text.append("Final Answer:&nbsp;&nbsp;&nbsp;&nbsp;");
        text.append(middleString);
        
        text.append("<input ");

        text.append("type=\"text\" name=\"");
        text.append(qId);
        text.append("\" id=\"");
        text.append(qId);
        text.append("\" size=\"25\" value=\"");
        
        if (!editedAnswers.isEmpty())
        {
            text.append(editedAnswers.get(0));
        }

        text.append("\">");
            
        text.append(endString);
        text.append(startString);
        
        text.append(endString);
        text.append(closeString);
   
        return text.toString();
    }
}

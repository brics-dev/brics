package gov.nih.nichd.ctdb.response.tag;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspException;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.tag.ActionIdtDecorator;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.response.domain.DataEntryDraft;
import gov.nih.nichd.ctdb.response.domain.EditAnswerDisplay;
import gov.nih.nichd.ctdb.response.domain.Response;
import gov.nih.nichd.ctdb.response.util.MetaDataHistory;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

/**
 * ViewEditAnswerDecorator enables a table to have a column with Action links
 * This class works with the <code>display</code> tag library.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class ViewEditedAnswerIdtDecorator extends ActionIdtDecorator
{

    /**
     * Default Constructor
     */
    public ViewEditedAnswerIdtDecorator()
    {
        super();
    }

    /**
     * Gets the question ID.
     *
     * @return String of Question ID
     */
    public String getQuestionIdDec()
    {
        Response response = (Response)this.getObject();
        Question q = response.getQuestion();
        if (q.getId() < 0)
        {
            return "";
        }
        return q.getId() + "";
    }

    /**
     * Gets the answer for resolve discrepancies.
     *
     * @return  String of list of answers
     */
    public String getResolvedAnswer()
    {
        Response response = (Response)this.getObject();
        List answers = response.getAnswers();
        String answerString = "";
        if (answers.size() > 0)
        {
            int i = 0;
            for (Iterator it=answers.iterator(); it.hasNext();)
            {
                i++;
                if (i == 1)
                {
                    answerString += (String)it.next();
                }
                else
                {
                    answerString += "<br>" + (String)it.next();
                }
            }
        }
        return answerString;
    }

    /**
     * Gets answers for the data entry one.
     *
     * @return  String of list of answers
     */
    public String getDataEntry1Answer()
    {
        Response response = (Response)this.getObject();
        Response response1 = response.getResponse1();
        List answers = response1.getAnswers();

        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < answers.size(); i++)
        {
        	String answer = (String)answers.get(i);
        	if (i > 0)
        		sb.append("<br>");
        	sb.append(answer);
        }
        return sb.toString();
    }

    /**
     * Gets answers for the data entry two.
     *
     * @return  String of list of answers
     */
    public String getDataEntry2Answer()
    {
        Response response = (Response)this.getObject();
        Response response2 = response.getResponse2();
        List answers = response2.getAnswers();

        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < answers.size(); i++)
        {
        	String answer = (String)answers.get(i);
        	if (i > 0)
        		sb.append("<br>");
        	sb.append(answer);
        }
        return sb.toString();

      /*  String answerString = "";
        if (answers.size() > 0)
        {
            int i = 0;
            for (Iterator it=answers.iterator(); it.hasNext();)
            {
                i++;
                if (i == 1)
                {
                    answerString += (String)it.next();
                }
                else
                {
                    answerString += "<br>" + (String)it.next();
                }
            }
        }
        return answerString; */
    }

    /**
     * Retrieves data entry's number of questions answered.
     *
     * @return  String of number of questions answered.
     */
    public String getNumQuestionsAnswered()
    {
        DataEntryDraft dataEntry = (DataEntryDraft)this.getObject();
        if (dataEntry.getNumQuestionsAnswered() == Integer.MIN_VALUE)
        {
            return "";
        }
        else
        {
            return dataEntry.getNumQuestionsAnswered() + "";
        }
    }

    /**
     * Retrieves response's answers after editing occured
     *
     * @return  HTML string displaying the answers before editing.
     */
    public String getPostAnswers()
	{
        EditAnswerDisplay  ead = (EditAnswerDisplay) this.getObject();
		StringBuffer sb = new StringBuffer();

        List answers = ead.getEditedAnswer();
        for (int i = 0; i < answers.size(); i++)
        {
        	String answer = (String)answers.get(i);
        	if (i > 0)
        		sb.append("<br>");
        	sb.append(answer);
        }
        return sb.toString();
    }

    /**
     * Retrieves response's answers before editing occured
     *
     * @return  HTML string displaying the answers after editing.
     */
    public String getPreAnswers()
	{
        EditAnswerDisplay  ead = (EditAnswerDisplay) this.getObject();
		StringBuffer sb = new StringBuffer();

        List answers = ead.getPreviousAnswer();
        for (int i = 0; i < answers.size(); i++)
        {
        	String answer = (String)answers.get(i);
        	if (i > 0)
        		sb.append("<br>");
        	sb.append(answer);
        }
        return sb.toString();
    }
    
	public String getAudStatus() throws JspException {
		EditAnswerDisplay ead = (EditAnswerDisplay) this.getObject();
		String audstatus = ead.getAuditStatus();
		int sId = ead.getSectionId();
		int qId = ead.getQuestionId();
		int selected_Form_Ids = ead.getAdministeredformId();

		if (audstatus != null && !audstatus.trim().isEmpty()) {
			String audStatusTm = audstatus.trim();
			if (CtdbConstants.AUDITCOMMENT_STATUS_COMPLETED.equalsIgnoreCase(audStatusTm)) {
				audstatus = "<a href=\"" + this.getWebRoot()
						+ "/response/dataCollection.action?action=auditComments&mode=formPatient&aformId="
						+ selected_Form_Ids + "&audit=true&sqshow=true&sId=" + sId + "&qId=" + qId + "\">" + "Completed"
						+ "</a>";
			} else if (CtdbConstants.AUDITCOMMENT_STATUS_LOCKED.equalsIgnoreCase(audStatusTm)) {
				audstatus = "<a href=\"" + this.getWebRoot()
						+ "/response/dataCollection.action?action=auditComments&mode=formPatient&aformId="
						+ selected_Form_Ids + "&audit=true&sqshow=true&sId=" + sId + "&qId=" + qId + "\">" + "Locked"
						+ "</a>";
			} else if (CtdbConstants.AUDITCOMMENT_STATUS_INPROGRESS.equalsIgnoreCase(audStatusTm)) {
				audstatus = "<a href=\"" + this.getWebRoot()
						+ "/response/dataCollection.action?action=auditComments&mode=formPatient&aformId="
						+ selected_Form_Ids + "&audit=true&sqshow=true&sId=" + sId + "&qId=" + qId + "\">"
						+ "In Progress" + "</a>";
			}
		}

		return audstatus;
	}

	public String getQuesText() throws JspException {
		EditAnswerDisplay ead = (EditAnswerDisplay) this.getObject();
		String quesText = ead.getQuestionText();
		int sId = ead.getSectionId();
		int qId = ead.getQuestionId();
		int selected_Form_Ids = ead.getAdministeredformId();

		if (quesText != null && !quesText.trim().isEmpty()) {
			String quesTextTm = quesText.trim();
			quesText = "<a href=\"" + this.getWebRoot()
					+ "/response/dataCollection.action?action=auditComments&mode=formPatient&aformId="
					+ selected_Form_Ids + "&audit=true&sqshow=true&sId=" + sId + "&qId=" + qId + "\">" + quesTextTm
					+ "</a>";
		}
		return quesText;
	}

	/**
	 * Retrieves response's answer edit date
	 *
	 * @return HTML string displaying the answer edit date. If date is null, return empty string.
	 */
	public String getEditDate() {
		EditAnswerDisplay ead = (EditAnswerDisplay) this.getObject();
		SimpleDateFormat df = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

		if (ead.getEditDate() != null) {
			return df.format(ead.getEditDate());
		} else {
			return "N/A";
		}
	}

    /**
     * Retrieves meta data edit date string
     *
     * @return  HTML string displaying the meta data edit date.  If date is null,
     *			return empty string.
     */
    public String getWhenEdited()
    {
    	MetaDataHistory metadata = (MetaDataHistory) this.getObject();
        Date date = metadata.getEditdate();
        if(date != null)
        {
            SimpleDateFormat dateFormat = new SimpleDateFormat(SysPropUtil.getProperty("default.system.dateformat"));
            SimpleDateFormat timeFormat = new SimpleDateFormat(SysPropUtil.getProperty("default.system.timeformat"));
            return dateFormat.format(date) + "<br>" + timeFormat.format(date);
        }
        else
            return "";
    }
    
    public String getOrigEntryESignature() {
    	DataEntryDraft dataEntryDraft = (DataEntryDraft) this.getObject();
    	String rtnStr = "";
    	if (dataEntryDraft.getStatus().equals(CtdbConstants.DATACOLLECTION_STATUS_COMPLETED)
    			|| dataEntryDraft.getStatus().equals(CtdbConstants.DATACOLLECTION_STATUS_LOCKED)) {
    		rtnStr = "Signed";
    	}
    	
    	return rtnStr;
    }

    public String getLockESignature() {    	
    	return "Signed";
    }
    
    public String getEditESignature() {    	
    	return "Signed";
    }
}

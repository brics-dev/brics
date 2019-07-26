package gov.nih.nichd.ctdb.response.domain;

import java.util.Date;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

/**
 * DataEntryDraft DomainObject for the NICHD CTDB Application
 *
 * @author  Booz Allen Hamilton
 * @version 1.0
 */

public class DataEntryDraft extends CtdbDomainObject
{
	private static final long serialVersionUID = 7939514741186729387L;
	
	private int formId = Integer.MIN_VALUE;
    private int administeredFormId = Integer.MIN_VALUE;
    private int dataEntryDraftId = Integer.MIN_VALUE;
    private int dataEnteredBy = Integer.MIN_VALUE;
    private int dataEnteredFlag= Integer.MIN_VALUE;
    private String dataEnteredByName = "";
    private int numQuestionsAnswered = Integer.MIN_VALUE;
    private Date lockDate;
    private String status = null;
    private int totalQuestions = Integer.MIN_VALUE;
    private String questionCompleted = null;

    /**
     * Default Constructor for the DataEntryDraft Domain Object
     */
    public DataEntryDraft()
    {
        // default constructor
    }

    /**
     * Gets the Form id
     *
     * @return int formId
     */
    public int getFormId()
    {
        return formId;
    }

    /**
     * Sets the form Id
     *
     * @param formId
     */
    public void setFormId(int formId)
    {
        this.formId = formId;
    }

    /**
     * Gets the dataEntryDraft id
     *
     * @return int dataEntryDraftId
     */
    public int getDataEntryDraftId()
    {
        return dataEntryDraftId;
    }

    /**
     * Sets the dataEntryDraft Id
     *
     * @param dataEntryDraftId
     */
    public void setDataEntryDraftId(int dataEntryDraftId)
    {
        this.dataEntryDraftId = dataEntryDraftId;
    }

    /**
     * Gets the administeredFormId
     *
     * @return int administeredFormId
     */
    public int getAdministeredFormId()
    {
        return administeredFormId;
    }

    /**
     * Sets the administeredFormId
     *
     * @param administeredFormId
     */
    public void setAdministeredFormId(int administeredFormId)
    {
        this.administeredFormId = administeredFormId;
    }

    /**
     * Gets dataEntered By
     *
     * @return  int The dataEnteredBy
     */
    public int getDataEnteredBy()
    {
        return dataEnteredBy;
    }

    /**
     * Sets dataEntered By
     *
     * @param dataEnteredBy
     */
    public void setDataEnteredBy(int dataEnteredBy)
    {
        this.dataEnteredBy = dataEnteredBy;
    }

    /**
     * Gets data Entered Flag
     *
     * @return  int dataEnteredFlag
     */
    public int getDataEnteredFlag()
    {
        return dataEnteredFlag;
    }

    /**
     * Sets data Entered Flag
     *
     * @param dataEnteredFlag
     */
    public void setDataEnteredFlag(int dataEnteredFlag)
    {
        this.dataEnteredFlag = dataEnteredFlag;
    }

    /**
     * Gets data Entered By Name
     *
     * @return String dataEnteredByName
     */
    public String getDataEnteredByName()
    {
        return dataEnteredByName;
    }

    /**
     * Sets the data Entered By Name
     *
     * @param dataEnteredByName
     */
    public void setDataEnteredByName(String dataEnteredByName)
    {
        this.dataEnteredByName = dataEnteredByName;
    }

    /**
     * Get the number questions answered value.
     *
     * @return The NumberQuestions value.
     */
    public int getNumQuestionsAnswered()
    {
        return numQuestionsAnswered;
    }

    /**
     * Set the NumberQuestions value.
     *
     * @param newNumberQuestions The new NumberQuestions value.
     */
    public void setNumQuestionsAnswered(int newNumberQuestions)
    {
        this.numQuestionsAnswered = newNumberQuestions;
    }

    /**
     * Gets lock date of data entry
     *
     * @return Lock date
     */
    public Date getLockDate()
    {
        return lockDate;
    }

    /**
     * Sets the data entry's Lock Date
     *
     * @param lockDate The data entry's lock date
     */
    public void setLockDate(Date lockDate)
    {
        this.lockDate = lockDate;
    }

    /**
     * Gets the data entry status.
     *
     * @return status string
     */
    public String getStatus()
    {
        return status;
    }

    /**
     * Sets the data entry status.
     *
     * @param status the status of the data entry.
     */
    public void setStatus(String status)
    {
        this.status = status;
    }

    /**
     * Get the total number of questions value.
     *
     * @return The total number of questions value.
     */
    public int getTotalQuestions()
    {
        return totalQuestions;
    }

    /**
     * Set the total number of questions value.
     *
     * @param totalQuestions The total number of questions value.
     */
    public void setTotalQuestions(int totalQuestions)
    {
        this.totalQuestions = totalQuestions;
    }

    public Document toXML() throws TransformationException
	{
    	throw new UnsupportedOperationException("toXML() no supported in DataEntryDraft.");
	}

	/**
	 * @return the questionCompleted
	 */
	public String getQuestionCompleted() {
		return questionCompleted;
	}

	/**
	 * @param questionCompleted the questionCompleted to set
	 */
	public void setQuestionCompleted(String questionCompleted) {
		this.questionCompleted = questionCompleted;
	}


}

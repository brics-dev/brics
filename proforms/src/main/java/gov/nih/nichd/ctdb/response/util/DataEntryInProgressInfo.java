package gov.nih.nichd.ctdb.response.util;

/**
 * The DataEntryInProgressInfo represents the Java bean class containing table data
 * on the DataEntryInProgress page.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class DataEntryInProgressInfo
{
    private String nihRecordNumber;
    private String intervalName;
    private int numQuestionsAnswered;

    /**
     * Default Constructor
     */
    public DataEntryInProgressInfo()
    {

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
     * Get the IntervalName value.
     *
     * @return The IntervalName value.
     */
    public String getIntervalName()
    {
        return intervalName;
    }

    /**
     * Set the IntervalName value.
     *
     * @param newIntervalName The new IntervalName value.
     */
    public void setIntervalName(String newIntervalName)
    {
        this.intervalName = newIntervalName;
    }

    /**
     * Get the RecordNumber value.
     *
     * @return the RecordNumber value.
     */
    public String getNihRecordNumber()
    {
        return nihRecordNumber;
    }

    /**
     * Set the RecordNumber value.
     *
     * @param newRecordNumber The new RecordNumber value.
     */
    public void setNihRecordNumber(String newRecordNumber)
    {
        this.nihRecordNumber = newRecordNumber;
    }
}

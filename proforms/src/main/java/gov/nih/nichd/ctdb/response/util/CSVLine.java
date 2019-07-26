package gov.nih.nichd.ctdb.response.util;

import java.util.List;
/**
 * CSVLine represents a line inside of the upload data transfer template.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class CSVLine
{
    private String nihRecordNumber;
    private int questionId;
    private List responses;
    private int lineNumber;
    private StringBuffer error = new StringBuffer(20);

    /**
     * Overloaded constructor to set line number
     *
     * @param lineNumber The current line number
     */
    public CSVLine(int lineNumber)
    {
        this.lineNumber = lineNumber;
    }

    /**
     * The populate method populates all attributes for this line
     *
     * @param nihRecordNumber The current NIH Record Number
     * @param questionId The current question ID
     * @param response The current response
     */
    public void populate(String nihRecordNumber, int questionId, List responses)
    {
        this.nihRecordNumber = nihRecordNumber;
        this.questionId = questionId;
        this.responses = responses;
    }

    /**
     * Gets the current NIH Record Number
     *
     * @return The current NIH Record Number
     */
    public String getNihRecordNumber()
    {
        return nihRecordNumber;
    }

    /**
     * Sets the current NIH Record Number
     *
     * @param nihRecordNumber The current NIH Record Number
     */
    public void setNihRecordNumber(String nihRecordNumber)
    {
        this.nihRecordNumber = nihRecordNumber;
    }

    /**
     * Gets the current question ID
     *
     * @return The current question ID
     */
    public int getQuestionId()
    {
        return questionId;
    }

    /**
     * Sets the current question ID
     *
     * @param questionId The current question ID
     */
    public void setQuestionId(int questionId)
    {
        this.questionId = questionId;
    }

    /**
     * Gets the current Response
     *
     * @return The current response
     */
    public List getResponses()
    {
        return responses;
    }

    /**
     * Sets the current response
     *
     * @param response The current response
     */
    public void setResponses(List responses)
    {
        this.responses = responses;
    }

    /**
     * Gets all errors related to this line
     *
     * @return The errors for this line
     */
    public String getError()
    {
        return error.toString();
    }

    /**
     * Add's an error to the current string of errors for this line
     *
     * @param error The line error
     */
    public void addError(String error)
    {
        this.error.append(error);
    }

    /**
     * Gets the current line number
     *
     * @return The current line number
     */
    public int getLineNumber()
    {
        return lineNumber;
    }

    /**
     * Sets the current line number
     *
     * @param lineNumber The current line number
     */
    public void setLineNumber(int lineNumber)
    {
        this.lineNumber = lineNumber;
    }
}
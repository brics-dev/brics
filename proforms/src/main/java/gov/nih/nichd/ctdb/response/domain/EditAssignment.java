package gov.nih.nichd.ctdb.response.domain;

import java.sql.Timestamp;

/**
 * EditAssignment DomainObject for the NICHD CTDB Application
 *
 * @author  Booz Allen Hamilton
 * @version 1.0
 */

public class EditAssignment extends DataEntryAssArch
{
	private static final long serialVersionUID = 1034814633151416747L;
	
	private int formId = Integer.MIN_VALUE;
    private int administeredFormId = Integer.MIN_VALUE;
    private int intervalNumber = Integer.MIN_VALUE;
    private int protocolId = Integer.MIN_VALUE;
    private String subjectId = "";
    private String formName = "";
    private String intervalName = "";
    private Timestamp startDate;
    private String patientNameLabel;
    private String visitDate;


    /**
     * Default Constructor for the EditAssignment Domain Object
     */
    public EditAssignment()
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
     * @param int formId
     */
    public void setFormId(int formId)
    {
        this.formId = formId;
    }

    /**
     * Gets the protocol id
     *
     * @return int protocolId
     */
    public int getProtocolId()
    {
        return protocolId;
    }

    /**
     * Sets the protocol Id
     *
     * @param int protocolId
     */
    public void setProtocolId(int protocolId)
    {
        this.protocolId = protocolId;
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
     * @param int administeredFormId
     */
    public void setAdministeredFormId(int administeredFormId)
    {
        this.administeredFormId = administeredFormId;
    }

    /**
     * Gets intervalNumber
     *
     * @return  int The intervalNumber
     */
    public int getIntervalNumber()
    {
        return intervalNumber;
    }

    /**
     * Sets intervalNumber
     *
     * @param int intervalNumber
     */
    public void setIntervalNumber(int intervalNumber)
    {
        this.intervalNumber = intervalNumber;
    }


    /**
     * Gets the Form Name
     *
     * @return String The Form Name
     */
    public String getFormName()
    {
        return formName;
    }

    /**
     * Sets the Form Name
     *
     * @param String FormName
     */
    public void setFormName(String formName)
    {
        this.formName = formName;
    }

    /**
     * Gets the interval Name
     *
     * @return String The interval Name
     */
    public String getIntervalName()
    {
        return intervalName;
    }

    /**
     * Sets the interval Name
     *
     * @param String intervalName
     */
    public void setIntervalName(String intervalName)
    {
        this.intervalName = intervalName;
    }

    /**
     * Gets Started date of the Data Entry
     *
     * @return startDate
     */
    public Timestamp getStartDate()
    {
        return startDate;
    }

    /**
     * Sets the Data Entry started Date
     *
     * @param startDate the Data Entry started Date
     */
    public void setStartDate(Timestamp startDate)
    {
        this.startDate = startDate;
    }

    public String getPatientNameLabel() {
        return patientNameLabel;
    }

    public void setPatientNameLabel(String patientNameLabel) {
        this.patientNameLabel = patientNameLabel;
    }

    public String getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(String visitDate) {
        this.visitDate = visitDate;
    }

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}
}

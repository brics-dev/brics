package gov.nih.nichd.ctdb.patient.form;

import gov.nih.nichd.ctdb.common.CtdbForm;
import gov.nih.nichd.ctdb.patient.common.PatientResultControl;

/**
 * The PatientSearchForm represents the Java class behind the HTML
 * to search for a patient
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class PatientSearchForm extends CtdbForm
{
    private String lastNameSearch = null;
    private String firstNameSearch = null;
    private String recordNumberSearch = null;
    private String mrnSearch = null;
    private int protocolIdSearch = Integer.MIN_VALUE;
    private String clicked = "submit";
    private String sortBy = PatientResultControl.SORT_BY_SUBJECTID;
    private String sortedBy = null;
    private String sortOrder = PatientResultControl.SORT_ASC;
    private String enrollmentStatus = "this";
	private String inProtocol = "yes";
    private String activeInProtocol = null;
	private String patientGroupNameSearch  = null;
    private String numResults = null;
    private String numResultsPerPage = null;
    private String subjectNumberSearch = null;
    private String guidSearch = null;

    public String getMrnSearch() {
		return mrnSearch;
	}

	public void setMrnSearch(String mrnSearch) {
		this.mrnSearch = mrnSearch;
	}

	public String getGuidSearch() {
		return guidSearch;
	}

	public void setGuidSearch(String guidSearch) {
		this.guidSearch = guidSearch;
	}

	/*
 *     private String[] selectedIds =null;
    public String[] getSelectedPatientIds() {
		return selectedIds;
	}
	public void setSelectedPatientIds(String[] selectedPatientIds) {
		this.selectedIds = selectedPatientIds;
	}
*/
	public String getEnrollmentStatus() {
		return enrollmentStatus;
	}

	public void setEnrollmentStatus(String enrollmentStatus) {
		this.enrollmentStatus = enrollmentStatus;
	}

	public String getActiveInProtocol() {
		return activeInProtocol;
	}

	public void setActiveInProtocol(String activeInProtocol) {
		this.activeInProtocol = activeInProtocol;
	}

    public String getPatientGroupNameSearch() {
		return patientGroupNameSearch;
	}

	public void setPatientGroupNameSearch(String patientGroupSearch) {
		this.patientGroupNameSearch = patientGroupSearch;
	}
	
	public String getFirstNameSearch()
    {
        return firstNameSearch;
    }

    /**
     * Set the first name
     *
     * @param firstNameSearch The first name entered
     */
    public void setFirstNameSearch(String firstNameSearch)
    {
        this.firstNameSearch = firstNameSearch;
    }

    /**
     * Return the last name
     *
     * @return Last name
     */
    public String getLastNameSearch()
    {
        return lastNameSearch;
    }

    /**
     * Set the last name
     *
     * @param lastNameSearch The last name entered
     */
    public void setLastNameSearch(String lastNameSearch)
    {
        this.lastNameSearch = lastNameSearch;
    }

    /**
     * Return the NIH Record Number
     *
     * @return NIH Record Number
     */
    public String getRecordNumberSearch()
    {
        return recordNumberSearch;
    }

    /**
     * Set the nih record number
     *
     * @param recordNumberSearch The nih record number entered.
     */
    public void setRecordNumberSearch(String recordNumberSearch)
    {
        this.recordNumberSearch = recordNumberSearch;
    }

    /**
     * Return the protocolName associated with a patient
     *
     * @return Protocol ID associated with a patient
     */
    public int getProtocolIdSearch()
    {
        return protocolIdSearch;
    }

    /**
     * Set the protocolName associated with a patient
     *
     * @param protocolIdSearch The protocolName associated with a patient entered.
     */
    public void setProtocolIdSearch(int protocolIdSearch)
    {
        this.protocolIdSearch = protocolIdSearch;
    }

    /**
     * Gets the sort by for the search/listing
     *
     * @return The sort by for the search/listing
     */
    public String getSortBy()
    {
        return sortBy;
    }

    /**
     * Set the sort by for the search/listing
     *
     * @param sortBy The sort by for the search/listing
     */
    public void setSortBy(String sortBy)
    {
        this.sortBy = sortBy;
    }

    /**
     * Gets the sorted by for the search/listing
     *
     * @return The sorted by for the search/listing
     */
    public String getSortedBy()
    {
        return sortedBy;
    }

    /**
     * Set the sorted by for the search/listing
     *
     * @param sortedBy The sorted by for the search/listing
     */
    public void setSortedBy(String sortedBy)
    {
        this.sortedBy = sortedBy;
    }

    /**
     * Gets the sort order for this search/listing
     *
     * @return The sort order for this search/listing
     */
    public String getSortOrder()
    {
        return sortOrder;
    }

    /**
     * Sets the sort order for this search/listing
     *
     * @param sortOrder The sort order for this search/listing
     */
    public void setSortOrder(String sortOrder)
    {
        this.sortOrder = sortOrder;
    }

    /**
     * Gets the value of what was clicked on the screen. By
     * default it is submit, possible values are submit or sort.
     *
     * @return The value of what was clicked on the screen
     */
    public String getClicked()
    {
        return clicked;
    }

    /**
     * Sets the value of what was clicked on the screen. By
     * default it is submit, possible values are submit or sort.
     *
     * @param clicked The value of what was clicked on the screen
     */
    public void setClicked(String clicked)
    {
        this.clicked = clicked;
    }

    /**
     * Gets the value the variable inProtocol
     *
     * @return The value of inProtocol
     */
    public String getInProtocol()
    {
        return inProtocol;
    }

    /**
     * Sets the value of inProtocol for searching
     *
     * @param value The value of what was clicked on the screen
     */
    public void setInProtocol(String value)
    {
        this.inProtocol = value;
    }
    public String getNumResults() {
        return numResults;
    }

    public void setNumResults(String numResults) {
        this.numResults = numResults;
    }

    public String getNumResultsPerPage() {
        return numResultsPerPage;
    }

    public void setNumResultsPerPage(String numResultsPerPage) {
        this.numResultsPerPage = numResultsPerPage;
    }


    public String getSubjectNumberSearch() {
        return subjectNumberSearch;
    }

    public void setSubjectNumberSearch(String subjectNumberSearch) {
        this.subjectNumberSearch = subjectNumberSearch;
    }

    public boolean isValidNotInProtocolSearchOld () {
        if ((hasData(lastNameSearch) && hasData(firstNameSearch )
                && hasData(subjectNumberSearch) && hasData(recordNumberSearch))
                && inProtocol.equals("no")) {
            return false;
        }else {
            return true;
        }
    }

    public boolean isValidNotInProtocolSearch () {
        if (enrollmentStatus != null && !enrollmentStatus.equalsIgnoreCase("this")) { // outside of current study
        	if(hasData(lastNameSearch) && hasData(firstNameSearch) && hasData(mrnSearch) ){
        		return true;
        	}
        	else{
        		return false;
        	}
        }else {// within protocol
            return true;
        }
    }

    /**
     *
     *
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void resetSearchableItems()
    {
        lastNameSearch = null;
        firstNameSearch = null;
        recordNumberSearch = null;
        patientGroupNameSearch = null;
        activeInProtocol  = null;
        inProtocol = null;
        action = null;
        activeInProtocol  = null;
    }
    

    public void clone (PatientSearchForm form) {
        super.clone(form);
        this.lastNameSearch = form.getLastNameSearch();
        this.firstNameSearch = form.getFirstNameSearch();
        this.recordNumberSearch = form.getRecordNumberSearch();
        this.protocolIdSearch = form.getProtocolIdSearch();
        this.sortBy = form.getSortBy();
        this.sortOrder = form.getSortOrder();
        this.clicked = form.getClicked();
        this.inProtocol = form.getInProtocol();
        this.numResults = form.getNumResults();
        this.numResultsPerPage = form.getNumResultsPerPage();
        this.subjectNumberSearch = form.getSubjectNumberSearch();
        this.patientGroupNameSearch  = form.getPatientGroupNameSearch();
        this.activeInProtocol  = form.getActiveInProtocol();

    }
    private boolean hasData (String s) {
        if (s != null && !(s.trim().equals(""))) {
            return true;
        } else {
            return false;
        }
    }


}

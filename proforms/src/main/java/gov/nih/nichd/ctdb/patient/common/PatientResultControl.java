package gov.nih.nichd.ctdb.patient.common;

import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.ResultControl;
/**
 * PatientResultControl handles searching and sorting of patient records in the system.
 *
 * @deprecated This is a rats nest of SQL injection attacks that are waiting to happen. Please do not use any of the methods in this class, 
 * and please take time to removing the usage of this class from your code.
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class PatientResultControl
 extends ResultControl
{
    /**
     *  PatientResultControl Constant used to sort by last name
     */
	public static final String SORT_BY_LASTNAME = "lastname";

    /**
     *  PatientResultControl Constant used to sort by first name
     */
    public static final String SORT_BY_FIRSTNAME = "firstname";

    /**
     *  PatientResultControl Constant used to sort by NIH Record Number
     */
    public static final String SORT_BY_SUBJECTID = "subjectid";
    public static final String SORT_BY_ORDERVAL = "orderval";
    private String guid = null;
    private String mrn = null;
	private String lastName = null;
    private String firstName = null;
    private String subjectId = null;
    private int protocolId = Integer.MIN_VALUE;
    private int patientId = Integer.MIN_VALUE;
    private boolean inProtocol = true;
    private String enrollmentStatus = "";

	private String activeInProtocol = null;

	private String maidenName = null;
    private String otherName = null;
    private boolean hasSamples = false;
    private int siteId = Integer.MIN_VALUE;
    private int formId = Integer.MIN_VALUE;
    private String subjectNumber = null;
	private String  patientGroupName = "";

    public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
        if(guid == null)        {
            this.guid = null;
        }
        else
        {
            this.guid = guid.trim().toUpperCase();
        }
	}

    public String getMrn() {
		return mrn;
	}

	public String getEnrollmentStatus() {
		return enrollmentStatus;
	}

	public void setEnrollmentStatus(String enrollmentStatus) {
		this.enrollmentStatus = enrollmentStatus;
	}


	public String getPatientGroupName() {
		return patientGroupName;
	}

	public void setPatientGroupName(String patientGroupName) {
		this.patientGroupName = patientGroupName;
	}
    
	/**
     * Default constructor for the PatientResultControl
     */
    public PatientResultControl()
    {
        // default constructor
        this.setSortBy(PatientResultControl.SORT_BY_SUBJECTID);
        this.setSortOrder(ResultControl.SORT_ASC);
    }

    /**
     * Gets the patient's last name to search for
     *
     * @return The patient last name to search for
     */
    public String getLastName()
    {
        return this.lastName;
    }

    /**
     * Sets the patient's last name to search for
     *
     * @param lastName The patient's last name to search for
     */
    public void setLastName(String lastName)
    {
        if(lastName == null)
        {
            this.lastName = null;
        }
        else
        {
            this.lastName = lastName.trim().toUpperCase();
        }
    }

    /**
     * Gets the patient's first name to search for
     *
     * @return The patient first name to search for
     */
    public String getFirstName()
    {
        return firstName;
    }

    /**
     * Sets the patient's first name to search for
     *
     * @param firstName The patient's first name to search for
     */
    public void setFirstName(String firstName)
    {
        if(firstName == null)
        {
            this.firstName = null;
        }
        else
        {
            this.firstName = firstName.trim().toUpperCase();
        }
    }

    /**
     * Gets the patient's NIH Record Number to search for
     *
     * @return The patient NIH Record Number to search for
     */
    public String getSubjectId()
    {
        return subjectId;
    }

    /**
     * Used to determine if the user wants to search for
     * patients within the current protocol
     *
     * @return  boolean, in protocol
     */
    public boolean isInProtocol()
    {
        return inProtocol;
    }

    /**
     * Sets the value indicating if the user wants to search
     * for patients within the current protocol.
     *
     * @param inProtocol
     */
    public void setInProtocol(boolean inProtocol)
    {
        this.inProtocol = inProtocol;
    }


    /**
     * Sets the patient's NIH Record Number to search for
     *
     * @param subjectId The patient's NIH Record Number to search for
     */
    public void setSubjectId(String nihRecordNumber)
    {
        if(nihRecordNumber == null)
        {
            this.subjectId = null;
        }
        else
        {
            this.subjectId = nihRecordNumber.trim().toUpperCase();
        }
    }

    public void setMrn(String mrn)
    {
        if(mrn == null)
        {
            this.mrn = null;
        }
        else
        {
            this.mrn = mrn.trim().toUpperCase();
        }
    }
    /**
     * Gets the protocol to search for
     *
     * @return The protocol to search for
     */
    public int getProtocolId()
    {
        return protocolId;
    }


    public boolean isHasSamples() {
        return hasSamples;
    }

    public void setHasSamples(boolean hasSamples) {
        this.hasSamples = hasSamples;
    }


    public String getSubjectNumber() {
        return subjectNumber;
    }

    public void setSubjectNumber(String subjectNumber) {
        this.subjectNumber = subjectNumber;
    }

    /**
     * Gets the protocol ID search string to be used by the DB
     *
     * @return The search string to be used by the DB
     */
    private String getProtocolIdSearchString()
    {
        if(this.protocolId != Integer.MIN_VALUE )
        {
            String clause;
                if (isInProtocol()) {
                    clause = " and patientprotocol.protocolid = " + this.protocolId + " ";
                } else {
                     clause = " and ( patient.patientid not in (select patientid from patientprotocol where protocolid = " + this.protocolId + ")  ";
                     clause += " or patient.patientid in (select patient.patientid from patient minus select patientprotocol.patientid from patientprotocol)  ";
                    clause += " ) ";
                }
            return clause;
        }
        else
        {
            return " ";
        }
    }

    private String getEnrollmentStatuSearchString()    {
//        if(this.protocolId != Integer.MIN_VALUE )
          String clause = " ";
            if (this.enrollmentStatus.equals("this") ) {
                    clause = " and patientprotocol.protocolid = " + this.protocolId + " ";
            } 
            else if (this.enrollmentStatus.equals("all") ){
            	clause = " ";
            }
            else if(this.enrollmentStatus.equals("none") ){
                     clause = " and patient.patientid not in (select patientid from patientprotocol)  ";
            }
            return clause;
    }

    private String getSiteIdSearchString () {
        if (this.siteId != Integer.MIN_VALUE) {
            return " and patientprotocol.siteid = " +this.siteId + " ";
        } else {
            return " ";
        }
    }
    
    /** Getter for property activeInProtocol.
     * @return Value of property activeInProtocol.
     */
    public Boolean isActiveInProtocol()
    {
        return activeInProtocol != null && activeInProtocol.equals("yes");
    }
    public String getActiveInProtocol() {
		return activeInProtocol;
	}
    public void setActiveInProtocol(String activeInProtocol)
    {
        this.activeInProtocol = activeInProtocol;
    }
    
    /**
     * Gets the active protocol ID search string to be used by the DB
     *
     * @return The search string to be used by the DB
     */
    private String getActiveInProtocolSearchString()
    {
    	String clause =" ";
    	if(this.activeInProtocol != null){
    		if(this.protocolId != Integer.MIN_VALUE && this.activeInProtocol.equals("yes"))     	{
    			clause = " and patientprotocol.active = true ";
    		}
    		else if(this.activeInProtocol.equals("no"))   {
    			clause =" and patientprotocol.active = false ";
    		}
    	}
       	return clause;
    }

    private String getPatientGroupNameSearchString(){
        if (this.patientGroupName != null && !this.patientGroupName.trim().equals("")) {
            patientGroupName  = patientGroupName.replaceAll("'", "''");
            return " and upper(patientgroup.name) like upper('%"+patientGroupName+"%') ";
        } else {
            return " ";
        }
    }
    private String getGuidSearchString(){
        if (this.guid != null && !this.guid.trim().equals("")) {
            guid  = guid.replaceAll("'", "''");
            return " and upper(patient.guid) like upper('%"+guid+"%') ";
        } else {
            return " ";
        }
    }

    private String getMaidenNameSearchString () {
        if (this.maidenName != null && !this.maidenName.trim().equals("")) {
            maidenName = maidenName.replaceAll("'", "''");
            return " and upper(" + CtdbDao.getDecryptionFunc("maidenname") + ")  like upper('%"+maidenName+"%') ";
        } else {
            return " ";
        }
    }
    private String getOtherNameSearchString () {
        if (this.otherName!= null && !this.otherName.trim().equals("")) {
            otherName = otherName.replaceAll("'", "''");
            return " and upper ("+ CtdbDao.getDecryptionFunc("othername") + ")  like upper('%"+otherName+"%') ";
        } else {
            return " ";
        }
    }

    private String getFirstNameSearchString () {
        if (this.firstName!= null && !this.firstName.trim().equals("")) {
            firstName = firstName.replaceAll("'", "''");
            return " and upper(" + CtdbDao.getDecryptionFunc("firstname") + ")  like upper('%"+firstName+"%') ";
        } else {
            return " ";
        }
    }

    private String getLastNameSearchString () {
        if (this.lastName!= null && !this.lastName.trim().equals("")) {
            lastName = lastName.replaceAll("'", "''");
            return " and upper(" + CtdbDao.getDecryptionFunc("lastname")  + ")  like upper('%"+lastName+"%') ";
        } else {
            return " ";
        }
    }

    private String getNihNumSearchString () {
        if (this.subjectId!= null && !this.subjectId.trim().equals("")) {
            subjectId = subjectId.replaceAll("'", "''");
            return " and upper(subjectid)  like upper('%"+subjectId+"%') ";
        } else {
            return " ";
        }
    }

    private String getMrnSearchString () {
        if (this.mrn!= null && !this.mrn.trim().equals("")) {
            mrn = mrn.replaceAll("'", "''");
            return " and upper(" + CtdbDao.getDecryptionFunc("mrn") + ")  like upper('%"+mrn+"%') ";
        } else {
            return " ";
        }
    }

    private String getRoleFormExclusionString () {
        if (this.formId != Integer.MIN_VALUE) {
            return " and xpatientroleid not in (select xpatientroleid from patientroleformexclusions where formid = " + this.formId + ") ";
        } else {
            return " ";
        }
    }

    private String getSubjectNumberSearchString () {
        if (this.subjectNumber!= null && !this.subjectNumber.trim().equals("")) {
            subjectNumber = subjectNumber.replaceAll("'", "''");
            return " and upper (patientprotocol.subjectNumber)  like upper('%"+subjectNumber+"%') ";
        } else {
            return " ";
        }
    }

    private String getPatientIdSearchString () {
        if (this.patientId != Integer.MIN_VALUE) {
            return " and patient.patientId = " + this.patientId + " ";
        } else {
            return " ";
        }
    }

    /**
     * Sets the protocol to search for
     *
     * @param protocolId The protocol ID to search for
     */
    public void setProtocolId(int protocolId)
    {
        this.protocolId = protocolId;
    }

    public String getMaidenName() {
        return maidenName;
    }

    public void setMaidenName(String maidenName) {
        if (maidenName == null) {
            this.maidenName = null;
        } else {
            this.maidenName = maidenName.trim().toUpperCase();
        }
    }

    public String getOtherName() {
        return otherName;
    }

    public void setOtherName(String otherName) {
        if (otherName == null) {
            this.otherName = null;
        } else {
            this.otherName = otherName.trim().toUpperCase();
        }
    }


    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    public int getFormId()
    {
        return formId;
    }

    public void setFormId(int formId)
    {
        this.formId = formId;
    }

    private String getOrderByClause () {


        if (this.getSortBy().equals(PatientResultControl.SORT_BY_ORDERVAL)) {
            return " order by patientprotocol.orderval " + this.getSortOrder();
        } else  if (this.getSortBy() != null &&  !this.getSortBy().trim().equals("")) {
            //return " order by patient." + this.getSortBy() + " " + this.getSortOrder();
             return " order by patientprotocol.orderval " + this.getSortOrder();
        }
        else {
            return "";
        }
    }


    /**
     * Gets the Search Clause for this SQL operation to determine the results to be returned.
     *
     * @return The string representation of the search clause for this SQL operation
     */
    public String getSearchClause()
    {
        StringBuffer clause = new StringBuffer(180);
        clause.append(this.getPatientIdSearchString() + " ");
        clause.append(this.getFirstNameSearchString() + " ");
        clause.append(this.getLastNameSearchString() + " ");
        if(this.enrollmentStatus !=null && !this.enrollmentStatus.isEmpty()){
        	clause.append(this.getEnrollmentStatuSearchString() + " ");
        }
        else{
            clause.append(this.getProtocolIdSearchString() + " ");
        }
        // Active/Inactive in Protocol is conflicted with  "not enrolled"
        // if(this.enrollmentStatus !=null && this.enrollmentStatus.equalsIgnoreCase("none") ){
        clause.append(this.getActiveInProtocolSearchString() + " ");
        //}
        clause.append(this.getMaidenNameSearchString() + " ");
        clause.append(this.getOtherNameSearchString() + " ");
        clause.append(this.getNihNumSearchString() + " ");
        clause.append(this.getMrnSearchString() + " ");
        clause.append( this.getSiteIdSearchString() + " ");
        clause.append( this.getRoleFormExclusionString() + " ");
        //clause.append(this.getOrderByClause() + " ");
        clause.append(this.getSubjectNumberSearchString() + " " ); 
        clause.append(this.getPatientGroupNameSearchString() + " " ); 
        clause.append(this.getGuidSearchString() + " " ); 
        // System.out.println("Patient search: \n" + clause.toString());
        return clause.toString();
    }

	public int getPatientId() {
		return patientId;
	}

	public void setPatientId(int patientId) {
		this.patientId = patientId;
	}

}

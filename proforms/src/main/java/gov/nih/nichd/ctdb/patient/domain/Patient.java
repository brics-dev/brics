package gov.nih.nichd.ctdb.patient.domain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.attachments.domain.Attachment;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbPerson;
import gov.nih.nichd.ctdb.common.TransformationException;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.security.domain.User;

/**
 * Patient DomainObject for the NICHD CTDB Application
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class Patient extends CtdbPerson
{
	private static final long serialVersionUID = 9076013671941502070L;
	//PatientProtocol
	private String subjectId;
	
	private int patientId;


	//Patient
	private String mrn;
	private String guid;
    private String email;
    private List<PatientProtocol> protocols = null;
    private PatientProtocol patientprotocol = null;
    private String dateOfBirth;
    private PatientExtraInfo extraInfo;

    private Attachment attachment;
    private List<Attachment> attachments = null;
	private boolean deleteFlag = false;
  
    private String usingNonPII = "0";
    
	private Map<String, Attachment> attachmentHashMap;
    
	public Map<String, Attachment> getAttachmentHashMap() {
		return attachmentHashMap;
	}

	public void setAttachmentHashMap(Map<String, Attachment> attachmentHashMap) {
		this.attachmentHashMap = attachmentHashMap;
	}
	/**
     * Default Constructor for the Patient Domain Object
     */
    public Patient()
    {
        //default constructor
    }


	public String getMrn() {
		return mrn;
	}

	public void setMrn(String mrn) {
		this.mrn = mrn;
	}

    public boolean getDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(boolean deleteFlag) {
		this.deleteFlag = deleteFlag;
	}

	public Attachment  getAttachment() {
		return attachment;
	}

	public void setAttachment(Attachment a) {
		this.attachment = a;
	}

	public List<Attachment>  getAttachments() {
		return attachments != null ? attachments : new ArrayList<Attachment>();
	}

	public void setAttachments(List<Attachment> attFileList) {
		this.attachments = attFileList;
	}



    /**
     * Gets the patient's email address
     *
     * @return The patient's email address
     */
    public String getEmail()
    {
        return email;
    }

    /**
     * Sets the patient's email address
     *
     * @param email Patient's email address
     */
    public void setEmail(String email)
    {
        this.email = email;
    }

        /**
     * Gets the patient's dob
     *
     * @return The patient's dob
     */
    public String getDateOfBirth()
    {
        return dateOfBirth;
    }

    /**
     * Sets the patient's birthday
     *
     * @param dob Patient's dob
     */
    public void setDateOfBirth(String dob)
    {
        this.dateOfBirth = dob;
    }
    
    /**
     * Gets the patients protocols
     *
     * @return A list of the patients protocols
     */
    public List<PatientProtocol> getProtocols()
    {
        return protocols != null ? protocols : new ArrayList<PatientProtocol>();
    }

    /**
     * Sets the patients protocols
     *
     * @param protocols The list of patient's protocols
     */
    public void setProtocols(List<PatientProtocol> protocols)
    {
        this.protocols = protocols;
    }

    public String getSubjectNumber() {
        return ((PatientProtocol)protocols.get(0)).getSubjectNumber();
    }

    /**
     * Determines if an object is equal to the current Patient Object.
     * Equal is based on if the first name and last name are equal.
     *
     * @param   o The object to determine if it is equal to the current Patient
     * @return  True if the object is equal to the Patient.
     *          False if the object is not equal to the Patient.
     */
    public boolean equals(Object o)
    {
        if( (o == null) || !super.equals(o) )
        {
            return false;
        }

        if( !(o instanceof Patient) )
        {
            return false;
        }
        
        Patient oPatient = (Patient) o;
        
        return getId() == oPatient.getId();
    }

    /** Getter for property extraInfo.
     * @return Value of property extraInfo.
     */
    public PatientExtraInfo getExtraInfo()
    {
        return extraInfo;
    }    

    /** Setter for property extraInfo.
     * @param extraInfo New value of property extraInfo.
     */
    public void setExtraInfo(PatientExtraInfo extraInfo)
    {
        this.extraInfo = extraInfo;
    }

 

    public void updateCreatedByInfo (User u)
    {
        super.updateCreatedByInfo (u);
        this.extraInfo.updateCreatedByInfo(u);
        this.getHomeAddress().updateCreatedByInfo(u);
    }
    
    
    public void updateUpdatedByInfo (User u)
    {
        super.updateUpdatedByInfo (u);
        this.extraInfo.updateUpdatedByInfo(u);
        this.getHomeAddress().updateUpdatedByInfo(u);
    }
    
    public String getDisplayLabel (int displayType, int protocolId) {
        switch (displayType) {

            case CtdbConstants.PATIENT_DISPLAY_NAME:
                return getLastName() + ", " + getFirstName();
            case CtdbConstants.PATIENT_DISPLAY_SUBJECT:
                return findSubjectNumber(protocolId);
            case CtdbConstants.PATIENT_DISPLAY_GUID:
                return getGuid();
            case CtdbConstants.PATIENT_DISPLAY_ID:
                return getSubjectId();
            case CtdbConstants.PATIENT_DISPLAY_MRN:
                return getMrn();
             default:
                return getSubjectId();
        }
    }

    private String findSubjectNumber (int protocolId) {
        String result = "";
        
        if (this.protocols != null) {
            for (Iterator i = this.protocols.iterator(); i.hasNext(); ) {
                PatientProtocol pp = (PatientProtocol) i.next();
                if (pp.getId() == protocolId && pp.getSubjectNumber() != null && !pp.getSubjectNumber().trim().equals("")) {
                    return  pp.getSubjectNumber();
                }
            }
        }
        // subject number not found
    	if(getFirstName() != null && getFirstName().length() >0 &&
      	    	getLastName() != null && getLastName().length() >0){
                result = getLastName() + ", " + getFirstName();
        	}
        	else if(this.getGuid() != null && this.getGuid().length() >0){
                result = getGuid();
        	}
        	
        return result;

    }

    public Protocol getCurrentProtocol (int protocolId) {
        for (Iterator<PatientProtocol> i = this.protocols.iterator(); i.hasNext();){
            Protocol p = i.next();
            if (p.getId() == protocolId) {
                return p;
            }
        }
        
        return null;
    }
    /**
     * This method allows the transformation of a Patient into an XML Document.
     * If no implementation is available at this time,
     * an UnsupportedOperationException will be thrown.
     *
     * @return XML Document
     * @throws TransformationException is thrown if there is an error during the XML tranformation
     * @throws UnsupportedOperationException is thrown if this method is currently unsupported and not implemented.
     */
    public Document toXML() throws TransformationException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException("No Implementation at this time for the method toXML() in Patient.");
    }
	
	public String getUsingNonPII() {
		return usingNonPII;
	}

	public void setUsingNonPII(String usingNonPII) {
		this.usingNonPII = usingNonPII;
	}

	public void updateValidatedByInfo(User user, int currentProtocolId, Boolean validated) {
		if(!validated){
			return;
		}
		
        List <PatientProtocol> allProtocols = this.getProtocols();
        
        for (Iterator<PatientProtocol> iter = allProtocols.iterator(); iter.hasNext();) {
            PatientProtocol pp = iter.next();
            if (pp.getId() == currentProtocolId) {
             	pp.setValidatedBy(user.getId());
            	pp.setValidatedDate(new java.util.Date());
            }
        }
		return;
	}

	public PatientProtocol getPatientprotocol() {
		return patientprotocol;
	}

	public void setPatientprotocol(PatientProtocol patientprotocol) {
		this.patientprotocol = patientprotocol;
	}
	
	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String subjectId) {
		this.subjectId = subjectId;
	}
	public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

	public int getPatientId() {
		return patientId;
	}

	public void setPatientId(int patientId) {
		this.patientId = patientId;
	}
}

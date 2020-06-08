package gov.nih.nichd.ctdb.patient.domain;

import java.util.Date;

import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.domain.ProtocolRandomization;

/**
 * PatientProtocol DomainObject for the NICHD CTDB Application
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class PatientProtocol extends Protocol
{
    
	private String subjectId;
	private boolean associated;
    private CtdbLookup patientRole;
    private String subjectNumber;
    private String enrollmentDate;
    private String completionDate;
    private int siteId = Integer.MIN_VALUE;
    private int groupId = Integer.MIN_VALUE;
    private int cohortId = Integer.MIN_VALUE;
    private String groupName;
	private boolean subject = true;
    private boolean futureStudy = false;
    private boolean validated = false;
    private boolean recruited = false;
    private int validatedBy = Integer.MIN_VALUE;
    private Date validatedDate = null;
    private ProtocolRandomization randomization = new ProtocolRandomization();

    public boolean isSubject() {
		return subject;
	}

	public void setSubject(boolean subject) {
		this.subject = subject;
	}

	public boolean isFutureStudy() {
		return futureStudy;
	}

	public void setFutureStudy(boolean futureStudy) {
		this.futureStudy = futureStudy;
	}

	public boolean isValidated() {
		return validated;
	}

	public void setValidated(boolean validated) {
		this.validated = validated;
	}

	public boolean isRecruited() {
		return recruited;
	}

	public void setRecruited(boolean recruited) {
		this.recruited = recruited;
	}


	public int getValidatedBy() {
		return validatedBy;
	}

	public void setValidatedBy(int validatedBy) {
		this.validatedBy = validatedBy;
	}

	public Date getValidatedDate() {
		return validatedDate;
	}

	public void setValidatedDate(Date validatedDate) {
		this.validatedDate = validatedDate;
	}

    public boolean isAssociated() {
		return associated;
	}

	public void setAssociated(boolean associated) {
		this.associated = associated;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	/** Creates a new instance of PatientProtocol */
    public PatientProtocol()
    {

    }
    
    public PatientProtocol (Protocol protocol)
    {
        this.setActive (true);
        this.setId (protocol.getId());
    }

    /**
     * Gets the patient role
     *
     * @return  The patient role
     */
    public CtdbLookup getPatientRole()
    {
        return patientRole;
    }

    /**
     * Sets the patient role
     *
     * @param   patientRole  The patient role
     */
    public void setPatientRole(CtdbLookup patientRole)
    {
        this.patientRole = patientRole;
    }

    public String getSubjectNumber() {
        return subjectNumber;
    }

    public void setSubjectNumber(String subjectNumber) {
        this.subjectNumber = subjectNumber;
    }

    public String getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(String enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }


    public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getCohortId() {
        return cohortId;
    }

    public void setCohortId(int cohortId) {
        this.cohortId = cohortId;
    }

    public String getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(String completionDate) {
        this.completionDate = completionDate;
    }

	public String getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(String nihRecordNumber) {
		this.subjectId = nihRecordNumber;
	}
	
	public ProtocolRandomization getProtocolRandomization() {
		return this.randomization;
	}
	
	public void setProtocolRandomization(ProtocolRandomization randomization) {
		this.randomization = randomization;
	}
}

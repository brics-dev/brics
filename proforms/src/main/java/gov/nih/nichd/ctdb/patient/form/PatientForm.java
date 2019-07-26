package gov.nih.nichd.ctdb.patient.form;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.attachments.domain.Attachment;
import gov.nih.nichd.ctdb.common.CtdbForm;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

/**
 * The PatientForm represents the Java class for nichd ctdb patients
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class PatientForm extends CtdbForm {
    
    private static final long serialVersionUID = -3066393985690931766L;
	private static final Logger logger = Logger.getLogger(PatientForm.class);
    
	private String active = null;
    private boolean recruited = false;
	private String patientAttachmentActionFlag = null;    
    private String subjectId = null;
    private String mrn = null;
	private String lastName = null;
    private String firstName = null;
    private String middleName = null;

    private String birthCity;
    private int birthCountryId;
    private String sex = null;
    private String homePhone = null;
    private String workPhone = null;
    private String mobilePhone = null;
    private String email = null;
    private String address1 = null;
    private String address2 = null;
    private String city = null;
    private String state = null;
    private String zip = null;
    private String country = null;
    private String dateOfBirth = null;
	
    private int addressId;
    
    /* the protocol values should be the protocol number, while the name is displayed*/
    /* properties used for the checkbox that associate and un-associate a patient to current protocol */
    private String currentProtocolId = null;

    private boolean futureStudy = false;

    private String subjectNumber = null;
    private String biorepositoryId = null;
    private String enrollmentDate = null;
    private String completionDate = null;
    private String sectionDisplay = "default";

	private boolean validated = false;
    private int validatedBy;
    private Date validatedDate;

    private int siteId = Integer.MIN_VALUE;
    private String guid;
	private int attAssociatedId = Integer.MIN_VALUE;
    private String associated2Protocol = null;

	// new elements for patient view display
	private String visitDate = null;
	private Attachment attachment = null;

	// Displayed strings for viewing patients
	private String displayHomeState= null;
	private String displayHomeCountry = null;
	private String displayBirthCountry = null;
	private String displaySiteName = null;
	
    public String getMrn() {
		return mrn;
	}

	public void setMrn(String mrn) {
		this.mrn = mrn;
	}

    public boolean isRecruited() {
		return recruited;
	}

	public void setRecruited(boolean isRecruited) {
		this.recruited = isRecruited;
	}

    public String getDisplaySiteName() {
		return displaySiteName;
	}

	public void setDisplaySiteName(String displaySiteName) {
		this.displaySiteName = displaySiteName;
	}

	public String getSectionDisplay() {
		return sectionDisplay;
	}

	public void setSectionDisplay(String sectionDisplay) {
		this.sectionDisplay = sectionDisplay;
	}

    public Attachment getAttachment() {
		if (attachment == null) {             	//We are not getting the site from database
			attachment = new Attachment();
		}

		return attachment;
	}

	public void setAttachment(Attachment attachment) {
		this.attachment = attachment;
	}

    public String getVisitDate() {
		return visitDate;
	}

	public void setVisitDate(String visitDate) {
		this.visitDate = visitDate;
	}
    
    public int getAttAssociatedId() {
		return attAssociatedId;
	}

	public void setAttAssociatedId(int attAssociatedId) {
		this.attAssociatedId = attAssociatedId;
	}

    /**
     * Return the first name
     *
     * @return First name
     */
    public String getFirstName()
    {
        return firstName;
    }

    /**
     * Set the first name
     *
     * @param firstName The first name entered.
     */
    public void setFirstName(String firstName)
    {
        if(firstName == null)
        {
            this.firstName = null;
        }
        else
        {
            this.firstName = firstName.trim();
        }
    }

    /**
     * Return the last name
     *
     * @return Last name
     */
    public String getLastName()
    {
        return lastName;
    }

    /**
     * Set the last name
     *
     * @param lastName The last name entered.
     */
    public void setLastName(String lastName)
    {
        if(lastName == null)
        {
            this.lastName = null;
        }
        else
        {
            this.lastName = lastName.trim();
        }
    }

    /**
     * Return the middle name
     *
     * @return Middle name
     */
    public String getMiddleName()
    {
        return middleName;
    }

    /**
     * Set the middle name.
     *
     * @param middleName The middle name entered.
     */
    public void setMiddleName(String middleName)
    {
        if(middleName == null)
        {
            this.middleName = null;
        }
        else
        {
            this.middleName = middleName.trim();
        }
    }

    /**
     * Return the NIH Record Number
     *
     * @return NIH Record Number
     */
    public String getSubjectId()
    {
        return subjectId;
    }

    /**
     * Set the NIH Record Number
     *
     * @param recordNumber The NIH Record Number entered.
     */
    public void setSubjectId(String recordNumber)
    {
        if(recordNumber == null)
        {
            this.subjectId = null;
        }
        else
        {
            this.subjectId = recordNumber.trim();
        }
    }

    /**
     * Return the Home Phone
     *
     * @return Home Phone
     */
    public String getHomePhone()
    {
        return homePhone;
    }

    /**
     * Set the Home Phone
     *
     * @param homePhone The Home Phone entered.
     */
    public void setHomePhone(String homePhone)
    {
        this.homePhone = homePhone;
    }

    /**
     * Return the Work Phone
     *
     * @return Work Phone
     */
    public String getWorkPhone()
    {
        return workPhone;
    }

    /**
     * Set the Work Phone
     *
     * @param workPhone The Work Phone entered.
     */
    public void setWorkPhone(String workPhone)
    {
        this.workPhone = workPhone;
    }

    /**
     * Return the Mobile Phone
     *
     * @return Mobile Phone
     */
    public String getMobilePhone()
    {
        return mobilePhone;
    }

    /**
     * Set the Mobile Phone
     *
     * @param mobilePhone The Mobile Phone entered.
     */
    public void setMobilePhone(String mobilePhone)
    {
        this.mobilePhone = mobilePhone;
    }

    /**
     * Return the Email Address
     *
     * @return Email Address
     */
    public String getEmail()
    {
        return email;
    }

    /**
     * Set the Email Address
     *
     * @param email The Email Address entered.
     */
    public void setEmail(String email)
    {
        if(email == null)
        {
            this.email = null;
        }
        else
        {
            this.email = email.trim();
        }
    }

    public String getSex() {
        return sex;
    }
    
    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAddress1()
    {
        return address1;
    }

    public void setAddress1(String address1)
    {
        if(address1 == null)
        {
            this.address1 = null;
        }
        else
        {
            this.address1 = address1.trim();
        }
    }

    public String getAddress2()
    {
        return address2;
    }

    public void setAddress2(String address2)
    {
        if(address2 == null)
        {
            this.address2 = null;
        }
        else
        {
            this.address2 = address2.trim();
        }
    }

    public String getCity()
    {
        return city;
    }

    public void setCity(String city)
    {
        if(city == null)
        {
            this.city = null;
        }
        else
        {
            this.city = city.trim();
        }
    }

    /**
     * Return the patients home State
     *
     * @return Patients home state
     */
    public String getState()
    {
        return state;
    }

    /**
     * Set the patients home state
     *
     * @param state The patients home state entered.
     */
    public void setState(String state)
    {
        this.state = state;
    }

    /**
     * Return the patients home zip code
     *
     * @return Patients home zip code
     */
    public String getZip()
    {
        return zip;
    }

    /**
     * Set the patients home Zip
     *
     * @param zip The patients home Zip entered.
     */
    public void setZip(String zip)
    {
        if(zip == null)
        {
            this.zip = null;
        }
        else
        {
            this.zip = zip.trim();
        }
    }

    /**
     * Return the patients dateOfBirth
     * @return Patients dateOfBirth
     */
    public String getDateOfBirth()
    {
        return dateOfBirth;
    }

    /**
     * Set the patients dateOfBirth
     *
     * @param dateOfBirth The patients dateOfBirth.
     */
    public void setDateOfBirth(String dateOfBirth)
    {
        if(dateOfBirth == null)
        {
            this.dateOfBirth = null;
        }
        else
        {
            this.dateOfBirth = dateOfBirth.trim();
        }
    }


    /**
     * Return the addressId associated with a patient
     *
     * @return Address ID associated with a patient
     */
    public int getAddressId()
    {
        return addressId;
    }

    /**
     * Set the addressId associated with a patient
     *
     * @param addressId The addressId associated with a patient entered.
     */
    public void setAddressId(int addressId)
    {
        this.addressId = addressId;
    }

    public String getCurrentProtocolId() {
        return currentProtocolId;
    }

    public void setCurrentProtocolId(String currentProtocolId) {
        this.currentProtocolId = currentProtocolId;
    }

    /** Getter for property country.
     * @return Value of property country.
     */
    public String getCountry() {
        return country;
    }
    
    /** Setter for property country.
     * @param country New value of property country.
     */
    public void setCountry(String country) {
        this.country = country;
    }
    
    /** Getter for property active.
     * @return Value of property active.
     */
    public String getActive()
    {
        return active;
    }
    
    /** Setter for property active.
     * @param active New value of property active.
     */
    public void setActive(String active)
    {
        this.active = active;
    }

    /** Getter for property patientRole.
     * @return Value of property role.
     */

    public boolean getFutureStudy() {
        return futureStudy;
    }

    public void setFutureStudy(boolean futureStudy) {
        this.futureStudy = futureStudy;
    }

	public int getSiteId() {
        return siteId;
    }

    public void setSiteId(int siteId) {
        this.siteId = siteId;
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

    public boolean isValidated() {
        return validated;
    }

    public void setValidated(boolean validated) {
        this.validated = validated;
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

    public String getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(String completionDate) {
        this.completionDate = completionDate;
    }

    public String getBirthCity() {
        return birthCity;
    }

    public void setBirthCity(String birthCity) {
        this.birthCity = birthCity;
    }

    public int getBirthCountryId() {
        return birthCountryId;
    }

    public void setBirthCountryId(int birthCountryId) {
        this.birthCountryId = birthCountryId;
    }

    public String getGuid() {
        return guid;
    }

    public void setGuid(String guid) {
        if (guid == null) {
            this.guid = null;
        } else {
            this.guid = guid.trim();
        }
    }

	public String getAssociated2Protocol() {
		return associated2Protocol;
	}

	public void setAssociated2Protocol(String associated2Protocol) {
		this.associated2Protocol = associated2Protocol;
	}

	public void setPatientAttachmentActionFlag(String actionProcessAttachment) {
		this.patientAttachmentActionFlag = actionProcessAttachment;
	}
	public String getPatientAttachmentActionFlag() {
		return this.patientAttachmentActionFlag;
	}
	
	public String getDisplayHomeState() {
		return displayHomeState;
	}

	public void setDisplayHomeState(String displayHomeState) {
		this.displayHomeState = displayHomeState;
	}

	public String getDisplayHomeCountry() {
		return displayHomeCountry;
	}

	public void setDisplayHomeCountry(String displayHomeCountry) {
		this.displayHomeCountry = displayHomeCountry;
	}

	public String getDisplayBirthCountry() {
		return displayBirthCountry;
	}

	public void setDisplayBirthCountry(String displayBirthCountry) {
		this.displayBirthCountry = displayBirthCountry;
	}

	public String getBiorepositoryId() {
		return biorepositoryId;
	}

	public void setBiorepositoryId(String biorepositoryId) {
        if(biorepositoryId == null) {
            this.biorepositoryId = null;
        } else {
            this.biorepositoryId = biorepositoryId.trim();
        }
	}

	public boolean hasInvalidDateOfBirth() {
		if ((this.dateOfBirth != null) && (this.dateOfBirth.length() > 0)) {
			SimpleDateFormat df = new SimpleDateFormat(SysPropUtil.getProperty("default.system.dateformat"));
        	Calendar currentDate = Calendar.getInstance();
        	Calendar bDate = Calendar.getInstance();

       		try {
       			bDate.setTime(df.parse(this.dateOfBirth));
       			Date checkDate = df.parse(this.dateOfBirth);
       			if (currentDate.before(bDate) || !(this.dateOfBirth.equals(df.format(checkDate)))) {
       				return true;
       			}
       		} catch (ParseException e) {
				logger.warn("Couldn't parse date of birth.", e);
       			return true;
       		}
		}
		return false;
	}
}

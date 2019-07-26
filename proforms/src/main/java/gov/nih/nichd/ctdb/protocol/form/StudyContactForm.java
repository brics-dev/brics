package gov.nih.nichd.ctdb.protocol.form;

import gov.nih.nichd.ctdb.common.CtdbForm;
import gov.nih.nichd.ctdb.common.StrutsConstants;

public class StudyContactForm extends CtdbForm
{
	private static final long serialVersionUID = 6275201300057063438L;
	
	private int studyId = Integer.MIN_VALUE;
	private String name = "";
	private String address1 = "";
	private String address2 = "";
	private String city = "";
	private int state = 0;
	private String zipCode = "";
	private int country = 0;
	private int studySite = 0;
	private String organization = "";
	private int instituteId = 11; // default to NICHD
	private String phone1 = "";
	private String phone2 = "";
	private int contactType = 0;
	private String emailAddress = "";
	private int addressId = Integer.MIN_VALUE;
	private String changeMode = StrutsConstants.ACTION_ADD_FORM;
	
	/**
	 * @return the studyId
	 */
	public int getStudyId() {
		return studyId;
	}
	
	/**
	 * @param studyId the studyId to set
	 */
	public void setStudyId(int studyId) {
		this.studyId = studyId;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the address1
	 */
	public String getAddress1() {
		return address1;
	}

	/**
	 * @param address1 the address1 to set
	 */
	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	/**
	 * @return the address2
	 */
	public String getAddress2() {
		return address2;
	}

	/**
	 * @param address2 the address2 to set
	 */
	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	/**
	 * @return the state
	 */
	public int getState() {
		return state;
	}

	/**
	 * @param state the state to set
	 */
	public void setState(int state) {
		this.state = state;
	}

	/**
	 * @return the zipCode
	 */
	public String getZipCode() {
		return zipCode;
	}

	/**
	 * @param zipCode the zipCode to set
	 */
	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	/**
	 * @return the country
	 */
	public int getCountry() {
		return country;
	}

	/**
	 * @param country the country to set
	 */
	public void setCountry(int country) {
		this.country = country;
	}

	/**
	 * @return the studySite
	 */
	public int getStudySite() {
		return studySite;
	}

	/**
	 * @param studySite the studySite to set
	 */
	public void setStudySite(int studySite) {
		this.studySite = studySite;
	}

	/**
	 * @return the organization
	 */
	public String getOrganization() {
		return organization;
	}

	/**
	 * @param organization the organization to set
	 */
	public void setOrganization(String organization) {
		this.organization = organization;
	}

	/**
	 * @return the instituteId
	 */
	public int getInstituteId() {
		return instituteId;
	}

	/**
	 * @param instituteId the instituteId to set
	 */
	public void setInstituteId(int instituteId) {
		this.instituteId = instituteId;
	}

	/**
	 * @return the phone1
	 */
	public String getPhone1() {
		return phone1;
	}

	/**
	 * @param phone1 the phone1 to set
	 */
	public void setPhone1(String phone1) {
		this.phone1 = phone1;
	}

	/**
	 * @return the phone2
	 */
	public String getPhone2() {
		return phone2;
	}

	/**
	 * @param phone2 the phone2 to set
	 */
	public void setPhone2(String phone2) {
		this.phone2 = phone2;
	}

	/**
	 * @return the contactType
	 */
	public int getContactType() {
		return contactType;
	}

	/**
	 * @param contactType the contactType to set
	 */
	public void setContactType(int contactType) {
		this.contactType = contactType;
	}

	/**
	 * @return the emailAddress
	 */
	public String getEmailAddress() {
		return emailAddress;
	}

	/**
	 * @param emailAddress the emailAddress to set
	 */
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	/**
	 * @return the city
	 */
	public String getCity() {
		return city;
	}

	/**
	 * @param city the city to set
	 */
	public void setCity(String city) {
		this.city = city;
	}

	/**
	 * @return the addressId
	 */
	public int getAddressId() {
		return addressId;
	}

	/**
	 * @param addressId the addressId to set
	 */
	public void setAddressId(int addressId) {
		this.addressId = addressId;
	}

	/**
	 * @return the changeMode
	 */
	public String getChangeMode() {
		return changeMode;
	}

	/**
	 * @param changeMode the changeMode to set
	 */
	public void setChangeMode(String changeMode) {
		this.changeMode = changeMode;
	}
}

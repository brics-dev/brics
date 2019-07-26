package gov.nih.nichd.ctdb.common;

import java.util.Map;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.patient.domain.Phone;
import gov.nih.nichd.ctdb.patient.domain.PhoneType;
import gov.nih.nichd.ctdb.util.domain.Address;

/**
 * CtdbPerson DomainObject for the NICHD CTDB Application
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class CtdbPerson extends CtdbDomainObject
{
	private static final long serialVersionUID = 4052204667584916941L;
	
	private String firstName = "";
    private String middleName = "";
    private boolean hasMiddleName = true;
    private String lastName = "";
    private String guid = "";

	private Map<PhoneType, Phone> phoneNumbers;
    private Address homeAddress;
    private String displayLabel;

    /**
     * Creates a new instance of CtdbPerson
     */
    public CtdbPerson() {
    }

    /**
     * Getter for property firstName.
     *
     * @return Value of property firstName.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Setter for property firstName.
     *
     * @param firstName New value of property firstName.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Getter for property middleName.
     *
     * @return Value of property middleName.
     */
    public String getMiddleName() {
        return middleName;
    }

    /**
     * Setter for property middleName.
     *
     * @param middleName New value of property middleName.
     */
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    /**
     * Getter for property lastName.
     *
     * @return Value of property lastName.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Setter for property lastName.
     *
     * @param lastName New value of property lastName.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Getter for property phoneNumbers.
     *
     * @return Value of property phoneNumbers.
     */
    public Map<PhoneType, Phone> getPhoneNumbers() {
        return phoneNumbers;
    }

    /**
     * Setter for property phoneNumbers.
     *
     * @param phoneNumbers New value of property phoneNumbers.
     */
    public void setPhoneNumbers(Map<PhoneType, Phone> phoneNumbers) {
        this.phoneNumbers = phoneNumbers;
    }

    /**
     * Getter for property homeAddress.
     *
     * @return Value of property homeAddress.
     */
    public Address getHomeAddress() {
        return homeAddress;
    }

    /**
     * Setter for property homeAddress.
     *
     * @param homeAddress New value of property homeAddress.
     */
    public void setHomeAddress(Address homeAddress) {
        this.homeAddress = homeAddress;
    }

    public String getLastNameFirstName() {
        return this.lastName + ", " + this.firstName;
    }

    public String getDisplayLabel() {
        return displayLabel;
    }

    public void setDisplayLabel(String displayLabel) {
        this.displayLabel = displayLabel;
    }



    /**
     * Determines if an object is equal to the current Patient Object.
     * Equal is based on if the first name and last name are equal.
     *
     * @param o The object to determine if it is equal to the current Patient
     * @return True if the object is equal to the Patient.
     *         False if the object is not equal to the Patient.
     */
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof CtdbPerson)) {
            return false;
        }

        final CtdbPerson person = (CtdbPerson) o;

        if (this.firstName != null && person.firstName != null && !this.firstName.equals(person.firstName)) {
            return false;
        }
        if (this.lastName != null && person.lastName != null && !this.lastName.equals(person.lastName)) {
            return false;
        }

        if (this.guid != null && person.guid != null && !this.guid.equals(person.guid)) {
            return false;
        }

        return true;
    }
    public String getGuid() {
		return guid;
	}

	public void setGuid(String guid) {
		this.guid = guid;
	}

    /**
     * This method allows the transformation of a Patient into an XML Document.
     * If no implementation is available at this time,
     * an UnsupportedOperationException will be thrown.
     *
     * @return XML Document
     * @throws TransformationException       is thrown if there is an
     *                                       error during the XML tranformation
     * @throws UnsupportedOperationException is thrown if this method
     *                                       is currently unsupported and not implemented.
     */
    public Document toXML() throws TransformationException {
        throw new UnsupportedOperationException("No Implementation at this time for the method toXML() in Patient.");
    }

	/**
	 * @return the hasMiddleName
	 */
	public boolean isHasMiddleName() {
		return hasMiddleName;
	}

	/**
	 * @param hasMiddleName the hasMiddleName to set
	 */
	public void setHasMiddleName(boolean hasMiddleName) {
		this.hasMiddleName = hasMiddleName;
	}

}

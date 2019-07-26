package gov.nih.nichd.ctdb.patient.domain;

import java.io.Serializable;

/**
 * Phone Object for the NICHD CTDB Application
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class Phone implements Serializable
{
    private String number;
    private PhoneType type;

    /**
     * Default Constructor for the Phone Object
     */
    public Phone()
    {
        //default constructor
    }

    /**
     * Overloaded Constructor for the Phone Object to set number and type
     */
    public Phone(String number, PhoneType type)
    {
        this.number = number;
        this.type = type;
    }

    /**
     * Gets the phone number
     *
     * @return The phone number
     */
    public String getNumber()
    {
        return number;
    }

    /**
     * Sets the phone number
     *
     * @param number The phone number
     */
    public void setNumber(String number)
    {
        this.number = number;
    }

    /**
     * Gets the phone type (Work, Mobile, ...)
     *
     * @return The phone type (Work, Mobile, ...)
     */
    public PhoneType getType()
    {
        return type;
    }

    /**
     * Sets the phone type (Work, Mobile, ...)
     *
     * @param type The phone type (Work, Mobile, ...)
     */
    public void setType(PhoneType type)
    {
        this.type = type;
    }
}

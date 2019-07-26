package gov.nih.nichd.ctdb.patient.domain;

import java.util.Iterator;

import gov.nih.nichd.ctdb.common.util.EnumeratedType;

/**
 * PhoneType EnumeratedType object for the NICHD CTDB Application. PhoneType
 * represents the different types of phone numbers that a Patient may have
 * (work, home, mobile).
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class PhoneType extends EnumeratedType
{
	private static final long serialVersionUID = -3928797938164062226L;
	
	/**
     * Final qualifiers for different Phone Types.
     */
    public static final PhoneType HOME = new PhoneType(1, "Home");
    public static final PhoneType WORK = new PhoneType(2, "Work");
    public static final PhoneType MOBILE = new PhoneType(3, "Mobile");

    /**
     * Protected Constructor to populate default
     * enumerated types as set as final variables
     * in the PhoneType class.
     *
     * @param value The int value for the type
     * @param name The display name for the type
     */
    protected PhoneType(int value, String name)
    {
        super(value, name);
    }

    /**
     * Gets a PhoneType by an <code>int</code> value
     *
     * @param value The int value to lookup
     * @return The PhoneType corresponding to the value
     */
    public static PhoneType getByValue(int value)
    {
        return (PhoneType) getByValue(HOME.getClass(), value);
    }

    /**
     * Gets an Iterator containing all PhoneType objects in the system
     *
     * @return An Iterator containing all PhoneType objects in the system
     */
    public static Iterator elements()
    {
        return elements(HOME.getClass());
    }

    /**
     * Returns the display value for the phone type
     *
     * @return The display value for the PhoneType
     */
    public String toString()
    {
        return this.getDispValue();
    }
}

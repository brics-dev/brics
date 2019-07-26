package gov.nih.nichd.ctdb.question.domain;

import java.util.Iterator;

import gov.nih.nichd.ctdb.common.util.EnumeratedType;

/**
 * ConversionFactor EnumeratedType object for the NICHD CTDB Application.
 * ConversionFactor represents the different output of calculations
 * that can be performed to get a value for a question.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class ConversionFactor extends EnumeratedType
{
	private static final long serialVersionUID = 1243765998944063445L;
	
	/** Final Date/Time conversion factor for NICHD CTDB questions
     */
    public static final ConversionFactor YEARS = new ConversionFactor(1, "Years");
    public static final ConversionFactor MONTHS = new ConversionFactor(2, "Months");
    public static final ConversionFactor WEEKS = new ConversionFactor(3, "Weeks");
    public static final ConversionFactor DAYS = new ConversionFactor(4, "Days");
    public static final ConversionFactor HOURS = new ConversionFactor(5, "Hours");
    public static final ConversionFactor MINUTES = new ConversionFactor(6, "Minutes");
    public static final ConversionFactor SECONDS = new ConversionFactor(7, "Seconds");

    /**
     * Protected Constructor to populate default
     * enumerated types as set as final variables
     * in the ConversionFactor class.
     *
     * @param value The int value for the conversion factor
     * @param name The display name for the conversion factor
     */
    protected ConversionFactor(int value, String name)
    {
        super(value, name);
    }

    /**
     * Gets a ConversionFactor by an <code>int</code> value
     *
     * @param value The int value to lookup
     * @return The ConversionFactor corresponding to the value
     */
    public static ConversionFactor getByValue(int value)
    {
        return (ConversionFactor) getByValue(YEARS.getClass(), value);
    }

    /**
     * Gets an Iterator containing all ConversionFactor objects in the system
     *
     * @return An Iterator containing all ConversionFactor objects in the system
     */
    public static Iterator elements()
    {
        return elements(YEARS.getClass());
    }

    /**
     * Gets an Array containing all ConversionFactor objects in the system
     *
     * @return An Array containing all ConversionFactor objects in the system
     */
    public static Object[] toArray()
    {
        return toArray(YEARS.getClass());
    }

    /**
     * Returns the display value for the Date/Time question conversion factor
     *
     * @return The display value for the ConversionFactor
     */
    public String toString()
    {
        return this.getDispValue();
    }
}

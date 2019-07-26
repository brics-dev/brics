package gov.nih.nichd.ctdb.question.domain;

import java.util.Iterator;

import gov.nih.nichd.ctdb.common.util.EnumeratedType;

/**
 * CalculationType EnumeratedType object for the NICHD CTDB Application.
 * CalculationType represents the different type of calculations
 * that can be performed to get a value for a question.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class CalculationType extends EnumeratedType
{
	private static final long serialVersionUID = 1750088609663254858L;
	
	/** Final calculation types for NICHD CTDB questions
     */
    public static final CalculationType SUM = new CalculationType(1, "Sum");
    public static final CalculationType DIFFERENCE = new CalculationType(2, "Difference");
    public static final CalculationType AVERAGE = new CalculationType(3, "Average");
    public static final CalculationType MULTIPLICATI0N = new CalculationType(4, "Multiplication");
    public static final CalculationType DIVISION = new CalculationType(5, "Division");

    /**
     * Protected Constructor to populate default
     * enumerated types as set as final variables
     * in the CalculationType class.
     *
     * @param value The int value for the type
     * @param name The display name for the type
     */
    protected CalculationType(int value, String name)
    {
        super(value, name);
    }

    /**
     * Gets a CalculationType by an <code>int</code> value
     *
     * @param value The int value to lookup
     * @return The CalculationType corresponding to the value
     */
    public static CalculationType getByValue(int value)
    {
        return (CalculationType) getByValue(SUM.getClass(), value);
    }

    /**
     * Gets an Iterator containing all CalculationType objects in the system
     *
     * @return An Iterator containing all CalculationType objects in the system
     */
    public static Iterator elements()
    {
        return elements(SUM.getClass());
    }

    /**
     * Gets an Array containing all CalculationType objects in the system
     *
     * @return An Array containing all CalculationType objects in the system
     */
    public static Object[] toArray()
    {
        return toArray(SUM.getClass());
    }

    /**
     * Returns the display value for the question calculation type
     *
     * @return The display value for the CalculationType
     */
    public String toString()
    {
        return this.getDispValue();
    }
}

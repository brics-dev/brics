package gov.nih.nichd.ctdb.question.domain;

import java.util.Iterator;

import gov.nih.nichd.ctdb.common.util.EnumeratedType;

/**
 * InstanceType EnumeratedType object for the NICHD CTDB Application.
 * InstanceType represents the different types of
 * instances of questions in the system.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class InstanceType extends EnumeratedType
{
	private static final long serialVersionUID = 6223964500874407367L;
	
	/**
     * Final InstanceType types
     */
    public static final InstanceType QUESTION = new InstanceType(1, "Question");
    public static final InstanceType CALCULATED_QUESTION = new InstanceType(2, "Calculated Question");
    public static final InstanceType IMAGE_MAP_QUESTION = new InstanceType(3, "Image Map Question");
    public static final InstanceType VISUAL_SCALE_QUESTION = new InstanceType(4, "Visual Scale Question");


    /**
     * Protected Constructor to populate default
     * enumerated types as set as final variables
     * in the InstanceType class.
     *
     * @param value The int value for the type
     * @param name The display name for the type
     */
    protected InstanceType(int value, String name)
    {
        super(value, name);
    }

    /**
     * Gets a InstanceType by an <code>int</code> value
     *
     * @param value The int value to lookup
     * @return The InstanceType corresponding to the value
     */
    public static InstanceType getByValue(int value)
    {
        return (InstanceType) getByValue(QUESTION.getClass(), value);
    }

    /**
     * Gets an Iterator containing all InstanceType objects in the system
     *
     * @return An Iterator containing all InstanceType objects in the system
     */
    public static Iterator elements()
    {
        return elements(QUESTION.getClass());
    }

    /**
     * Returns the display value for the InstanceType
     *
     * @return The display value for the InstanceType
     */
    public String toString()
    {
        return this.getDispValue();
    }
}

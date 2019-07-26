package gov.nih.nichd.ctdb.question.domain;

import java.util.Iterator;

import gov.nih.nichd.ctdb.common.util.EnumeratedType;

/**
 * SkipRuleOperatorType
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class SkipRuleOperatorType extends EnumeratedType
{
	private static final long serialVersionUID = 5336214073041419241L;
	
	/**
     * Final SkipRuleOperatorType types
     */
    public static final SkipRuleOperatorType EQUALS = new SkipRuleOperatorType(1, "Equals");
    public static final SkipRuleOperatorType IS_BLANK = new SkipRuleOperatorType(2, "Is Blank");
    public static final SkipRuleOperatorType HAS_ANY_VALUE = new SkipRuleOperatorType(3, "Has Any Value");
    public static final SkipRuleOperatorType CONTAINS = new SkipRuleOperatorType(4, "Contains");// add by sunny

    /**
     * Protected Constructor to populate default
     * enumerated types as set as final variables
     * in the SkipRuleOperatorType class.
     *
     * @param value The int value for the type
     * @param name The display name for the type
     */
    protected SkipRuleOperatorType(int value, String name)
    {
        super(value, name);
    }

    /**
     * Gets a SkipRuleOperatorType by an <code>int</code> value
     *
     * @param value The int value to lookup
     * @return The SkipRuleOperatorType corresponding to the value
     */
    public static SkipRuleOperatorType getByValue(int value)
    {
        return (SkipRuleOperatorType) getByValue(EQUALS.getClass(), value);
    }

    /**
     * Gets an Iterator containing all SkipRuleOperatorType objects in the system
     *
     * @return An Iterator containing all SkipRuleOperatorType objects in the system
     */
    public static Iterator elements()
    {
        return elements(EQUALS.getClass());
    }

    /**
     * Gets an Array containing all SkipRuleOperatorType objects in the system
     *
     * @return An Array containing all SkipRuleOperatorType objects in the system
     */
    public static Object[] toArray()
    {
        return toArray(EQUALS.getClass());
    }

    /**
     * Returns the display value for the SkipRuleOperatorType
     *
     * @return The display value for the SkipRuleOperatorType
     */
    public String toString()
    {
        return this.getDispValue();
    }
}

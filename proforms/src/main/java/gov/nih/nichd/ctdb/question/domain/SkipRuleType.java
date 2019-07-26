package gov.nih.nichd.ctdb.question.domain;

import java.util.Iterator;

import gov.nih.nichd.ctdb.common.util.EnumeratedType;

/**
 * SkipRuleType EnumeratedType object for the NICHD CTDB Application.
 * SkipRuleType represents the different rules for skipping questions (Required/Disallow)
 * and the necessary operators (Equals, Is Blank, Has any Value).
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class SkipRuleType extends EnumeratedType
{
	private static final long serialVersionUID = -8801425625374141670L;
	
	/** Final calculation types for NICHD CTDB questions
     */
    public static final SkipRuleType REQUIRE = new SkipRuleType(1, "Require");
    public static final SkipRuleType DISABLE = new SkipRuleType(2, "Disable");

    /**
     * Protected Constructor to populate default
     * enumerated types as set as final variables
     * in the SkipRuleType class.
     *
     * @param value The int value for the type
     * @param name The display name for the type
     */
    protected SkipRuleType(int value, String name)
    {
        super(value, name);
    }

    /**
     * Gets a SkipRuleType by an <code>int</code> value
     *
     * @param value The int value to lookup
     * @return The SkipRuleType corresponding to the value
     */
    public static SkipRuleType getByValue(int value)
    {
        return (SkipRuleType) getByValue(REQUIRE.getClass(), value);
    }

    /**
     * Gets an Iterator containing all SkipRuleType objects in the system
     *
     * @return An Iterator containing all SkipRuleType objects in the system
     */
    public static Iterator elements()
    {
        return elements(REQUIRE.getClass());
    }

    /**
     * Gets an Array containing all SkipRuleType objects in the system
     *
     * @return An Array containing all SkipRuleType objects in the system
     */
    public static Object[] toArray()
    {
        return toArray(REQUIRE.getClass());
    }

    /**
     * Returns the display value for the skip rule type
     *
     * @return The display value for the SkipRuleType
     */
    public String toString()
    {
        return this.getDispValue();
    }
}

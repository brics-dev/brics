package gov.nih.nichd.ctdb.question.domain;

import java.util.Iterator;

import gov.nih.nichd.ctdb.common.util.EnumeratedType;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

/**
 * AnswerType EnumeratedType object for the NICHD CTDB Application.
 * AnswerType represents the different types of
 * question responses that researchers may use
 * (string, numeric, date).
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class AnswerType extends EnumeratedType
{
	private static final long serialVersionUID = -7760338401597426746L;

	private String regexp;

    /** 
     * Final Question Answer types for NICHD CTDB questions
     */
    public static final AnswerType STRING = new AnswerType(1, "String", "^\\w+$");
    public static final AnswerType NUMERIC = new AnswerType(2, "Numeric", "^-?[0-9]*\\.?[0-9]*$");
    public static final AnswerType DATE = new AnswerType(3, "Date", SysPropUtil.getProperty("default.system.dateformat"));
    public static final AnswerType DATETIME = new AnswerType(4, "Date-Time", SysPropUtil.getProperty("default.system.datetimeformat"));

    /**
     * Protected Constructor to populate default
     * enumerated types as set as final variables
     * in the AnswerType class.
     *
     * @param value The int value for the type
     * @param name The display name for the type
     */
    protected AnswerType(int value, String name, String regexp)
    {
        super(value, name);
        this.regexp = regexp;
    }

    /**
     * Gets a AnswerType by an <code>int</code> value
     *
     * @param value The int value to lookup
     * @return The AnswerType corresponding to the value
     */
    public static AnswerType getByValue(int value)
    {
        return (AnswerType) getByValue(STRING.getClass(), value);
    }

    /**
     * Gets an Iterator containing all AnswerType objects in the system
     *
     * @return An Iterator containing all AnswerType objects in the system
     */
    public static Iterator<EnumeratedType> elements()
    {
        return elements(STRING.getClass());
    }

    /**
     * Gets an Array containing all AnswerType objects in the system
     *
     * @return An Array containing all AnswerType objects in the system
     */
    public static EnumeratedType[] toArray()
    {
        return toArray(STRING.getClass());
    }

    /**
     * Returns the display value for the question type
     *
     * @return The display value for the AnswerType
     */
    public String toString()
    {
        return this.getDispValue();
    }

    /**
     * Returns the regular expression used for validation for the question response type
     *
     * @return The regular expression for validation for the AnswerType
     */
    public String getRegexp()
    {
        return regexp;
    }
}

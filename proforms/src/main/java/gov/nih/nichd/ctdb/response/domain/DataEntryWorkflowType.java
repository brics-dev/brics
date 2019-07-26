package gov.nih.nichd.ctdb.response.domain;

import java.util.Iterator;

import gov.nih.nichd.ctdb.common.util.EnumeratedType;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Nov 9, 2005
 * Time: 10:02:52 AM
 * To change this template use File | Settings | File Templates.
 */
public class DataEntryWorkflowType  extends EnumeratedType {
	private static final long serialVersionUID = 1042145372709653927L;
	
	public static final DataEntryWorkflowType EXPRESS = new DataEntryWorkflowType(1, "Express");
    public static final DataEntryWorkflowType STANDARD = new DataEntryWorkflowType(2, "Standard");
    /**
        * Protected Constructor to populate default
        * enumerated types as set as final variables
        * in the QuestionType class.
        *
        * @param value The int value for the type
        * @param name The display name for the type
        */
       protected DataEntryWorkflowType (int value, String name)
       {
           super(value, name);
       }


    public static Iterator elements()
    {
        return elements(EXPRESS.getClass());
    }

    public static DataEntryWorkflowType getByValue(int value)
    {
        return (DataEntryWorkflowType) getByValue(EXPRESS.getClass(), value);
    }

    public static Object[] toArray()
    {
        return toArray(EXPRESS.getClass());
    }
     /**
     * Returns the display value for the DataEntryWorkflowType type
     *
     * @return The display value for the QuestionType
     */
    public String toString()
    {
        return this.getDispValue();
    }

}

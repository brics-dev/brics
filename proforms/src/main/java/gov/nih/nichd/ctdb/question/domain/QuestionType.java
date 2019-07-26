package gov.nih.nichd.ctdb.question.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONObject;

import gov.nih.nichd.ctdb.common.util.EnumeratedType;

/**
 * QuestionType EnumeratedType object for the NICHD CTDB Application.
 * QuestionType represents the different types of
 * questions the researchers may use
 * (text, select, radio, etc).
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class QuestionType extends EnumeratedType
{
	private static final long serialVersionUID = 6033585878431170923L;
	
	/**
     * Final Question types for NICHD CTDB questions
     */
    public static final QuestionType TEXTBOX = new QuestionType(1, "Textbox");
    public static final QuestionType TEXTAREA = new QuestionType(2, "Textarea");
    public static final QuestionType SELECT = new QuestionType(3, "Select");
    public static final QuestionType RADIO = new QuestionType(4, "Radio");
    public static final QuestionType MULTI_SELECT = new QuestionType(5, "Multi-Select");
    public static final QuestionType CHECKBOX = new QuestionType(6, "Checkbox");
    public static final QuestionType CALCULATED = new QuestionType(7, "Calculated");
    public static final QuestionType PATIENT_CALENDAR = new QuestionType(8, "Patient Calendar");
    public static final QuestionType IMAGE_MAP = new QuestionType(9, "Image Map");
    public static final QuestionType VISUAL_SCALE = new QuestionType(10, "Visual Scale");
    public static final QuestionType File = new QuestionType(11,"File");
    public static final QuestionType TEXT_BLOCK = new QuestionType(12, "Textblock");    


    /**
     * Protected Constructor to populate default
     * enumerated types as set as final variables
     * in the QuestionType class.
     *
     * @param value The int value for the type
     * @param name The display name for the type
     */
    protected QuestionType(int value, String name)
    {
        super(value, name);
    }

    /**
     * Gets a QuestionType by an <code>int</code> value
     *
     * @param value The int value to lookup
     * @return The QuestionType corresponding to the value
     */
    public static QuestionType getByValue(int value)
    {
        return (QuestionType) getByValue(TEXTBOX.getClass(), value);
    }
    
    /**
     * Gets a QuestionType by an <code>int</code> value
     *
     * @param value The int value to lookup
     * @return The QuestionType corresponding to the value
     */
    public static int getByQT(QuestionType qt)
    {
        return qt.getValue();
    }

    /**
     * Gets an Iterator containing all QuestionType objects in the system
     *
     * @return An Iterator containing all QuestionType objects in the system
     */
    public static Iterator elements()
    {
        return elements(TEXTBOX.getClass());
    }

    /**
     * Gets an Array containing all QuestionType objects in the system
     *
     * @return An Array containing all QuestionType objects in the system
     */
    public static Object[] toArray()
    {
        return toArray(TEXTBOX.getClass());
    }

    public static List<QuestionType> getDisplayTypes() {
        Object[] objs = toArray(TEXTBOX.getClass());
        List<QuestionType> types = new ArrayList<QuestionType>();
        for (int i=0; i < objs.length; i++) {
            QuestionType qt = (QuestionType) objs[i];
            if (! qt.equals(QuestionType.CALCULATED)
                    && ! qt.equals(QuestionType.PATIENT_CALENDAR)
                    && ! qt.equals(QuestionType.TEXT_BLOCK)) {
                types.add(qt);
            }
        }
        return types;
    }


    /**
     * Returns the display value for the question type
     *
     * @return The display value for the QuestionType
     */
    public String toString()
    {
        return this.getDispValue();
    }
    
    /*
     * Conver the content to JSON string 
     */

    public static String toJson(){
    	JSONObject jqt= new JSONObject(new HashMap());
        Object[] objs =  toArray(TEXTBOX.getClass());
        List types = new ArrayList();
        try {
	        for (int i=0; i < objs.length; i++) {
	            QuestionType qt = (QuestionType) objs[i];
	            jqt.put(qt.getDispValue().toUpperCase().replaceAll("\\s+", "_").replaceAll("-", "_"), qt.getValue());
	        }
	    	return jqt.toString();
        }
        catch(Exception e){
        	e.printStackTrace();
        	return "";
        }
    }
}
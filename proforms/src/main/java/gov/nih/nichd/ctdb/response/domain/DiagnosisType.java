package gov.nih.nichd.ctdb.response.domain;

import java.util.Iterator;

import gov.nih.nichd.ctdb.common.util.EnumeratedType;

/**
 * DiagnosisType EnumeratedType object for the NICHD CTDB Application.
 * DiagnosisType represents the different type of diagnosis in the system.
 *
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class DiagnosisType extends EnumeratedType
{
	private static final long serialVersionUID = -997786404083679401L;
	
	/** Final diagnosis types for NICHD CTDB questions
     */
    public static final DiagnosisType PTL = new DiagnosisType(1, "PTL");
    public static final DiagnosisType PPROM = new DiagnosisType(2, "PPROM");
    public static final DiagnosisType PIH = new DiagnosisType(3, "PIH");
    public static final DiagnosisType SGA = new DiagnosisType(4, "SGA");
    public static final DiagnosisType SI = new DiagnosisType(5, "Systemic Infection");
    public static final DiagnosisType IUFD = new DiagnosisType(6, "IUFD");
    public static final DiagnosisType FA = new DiagnosisType(7, "Fetal Anomaly");
    public static final DiagnosisType MG = new DiagnosisType(8, "Multiple Gestation");
    public static final DiagnosisType SUSP = new DiagnosisType(9, "Susp. Cx incomp");

    /**
     * Protected Constructor to populate default
     * enumerated types as set as final variables
     * in the DiagnosisType class.
     *
     * @param value The int value for the type
     * @param name The display name for the type
     */
    protected DiagnosisType(int value, String name)
    {
        super(value, name);
    }

    /**
     * Gets a DiagnosisType by an <code>int</code> value
     *
     * @param value The int value to lookup
     * @return The DiagnosisType corresponding to the value
     */
    public static DiagnosisType getByValue(int value)
    {
        return (DiagnosisType) getByValue(PTL.getClass(), value);
    }

    /**
     * Gets an Iterator containing all DiagnosisType objects in the system
     *
     * @return An Iterator containing all DiagnosisType objects in the system
     */
    public static Iterator elements()
    {
        return elements(PTL.getClass());
    }

    /**
     * Gets an Array containing all DiagnosisType objects in the system
     *
     * @return An Array containing all DiagnosisType objects in the system
     */
    public static Object[] toArray()
    {
        return toArray(PTL.getClass());
    }

    /**
     * Returns the display value for the Diagnosis type
     *
     * @return The display value for the DiagnosisType
     */
    public String toString()
    {
        return this.getDispValue();
    }
}

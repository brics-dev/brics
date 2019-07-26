package gov.nih.nichd.ctdb.patient.domain;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.TransformationException;

import org.w3c.dom.Document;

/**
 * PatientExtraInfo DomainObject for the NICHD CTDB Application
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class PatientExtraInfo extends CtdbDomainObject
{
	private static final long serialVersionUID = -1417726963153768521L;
	
	// Tab 2, demographics
    private String sex = null;
	private String birthCountryName = null;
    
    private String birthCity;
    private CtdbLookup birthCountry = new CtdbLookup(0); // default as UNKNOWN
    
    /** Creates a new instance of PatientExtraInfo */
    public PatientExtraInfo() 
    {
    }
    
	public String getBirthCountryName() {
		return birthCountryName;
	}

	public void setBirthCountryName(String birthCountryName) {
		this.birthCountryName = birthCountryName;
	}

    /** Getter for property sex.
     * @return Value of property sex.
     */
    public String getSex() 
    {
        return sex;
    }
    
    /** Setter for property sex.
     * @param sex New value of property sex.
     */
    public void setSex(String sex) 
    {
        this.sex = sex;
    }
    
    /**
     * This method allows the transformation of a Patient into an XML Document.
     * If no implementation is available at this time,
     * an UnsupportedOperationException will be thrown.
     *
     * @return XML Document
     * @throws TransformationException is thrown if there is an error during the XML tranformation
     * @throws UnsupportedOperationException is thrown if this method is currently unsupported and not implemented.
     */
    public Document toXML() throws TransformationException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException("No Implementation at this time for the method toXML() in Patient.");
    }

    public String getBirthCity() {
        return birthCity;
    }

    public void setBirthCity(String birthCity) {
        this.birthCity = birthCity;
    }

    public CtdbLookup getBirthCountry() {
        return birthCountry;
    }

    public void setBirthCountry(CtdbLookup birthCountry) {
        this.birthCountry = birthCountry;
    }

}

package gov.nih.nichd.ctdb.common;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * CtdbLookup used for lookup table objects. This object will hold the lookup ID, short name
 * and long name if applicable. The only attributes that are required are ID and short name. The
 * calling class should verify that <code>longName</code> is not null before processing.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class CtdbLookup extends CtdbDomainObject {
	private static final long serialVersionUID = 4538536894928134280L;
	
	protected String shortName;
    protected String longName = null;
    protected String fullName;  

    /**
     * Default Constructor for the Patient Domain Object
     */
    public CtdbLookup() {
        // default constructor
    }

    /**
     * Overloaded constructor to set all lookup values
     *
     * @param id        The lookup ID
     * @param shortName The lookup short name
     */
    public CtdbLookup(int id, String shortName) {
        this.setId(id);
        this.shortName = shortName;
    }

    /**
     * Overloaded constructor to set all lookup values
     *
     * @param id The lookup ID
     */
    public CtdbLookup(int id) {
        this.setId(id);
    }

    /**
     * Overloaded constructor to set all lookup values
     *
     * @param id        The lookup ID
     * @param shortName The lookup short name
     * @param longName  The lookup long name
     */
    public CtdbLookup(int id, String shortName, String longName) {
        this.setId(id);
        this.shortName = shortName;
        this.longName = longName;
        this.fullName = shortName +" : "+ longName;  //IBIS-641
    }

    /**
     * Gets the lookup's short name
     *
     * @return The lookup's short name
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * Sets the lookup's short name
     *
     * @param shortName The lookup's short name
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    /**
     * Gets the lookup's long name.
     *
     * @return The lookup's long name.
     */
    public String getLongName() {
        return longName;
    }

    /**
     * Sets the lookup's long name
     *
     * @param longName The lookup's long name
     */
    public void setLongName(String longName) {
        this.longName = longName;
    }
    
    public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((fullName == null) ? 0 : fullName.hashCode());
		result = prime * result + ((longName == null) ? 0 : longName.hashCode());
		result = prime * result + ((shortName == null) ? 0 : shortName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof CtdbLookup))
			return false;
		CtdbLookup other = (CtdbLookup) obj;
		if (fullName == null) {
			if (other.fullName != null)
				return false;
		} else if (!fullName.equals(other.fullName))
			return false;
		if (longName == null) {
			if (other.longName != null)
				return false;
		} else if (!longName.equals(other.longName))
			return false;
		if (shortName == null) {
			if (other.shortName != null)
				return false;
		} else if (!shortName.equals(other.shortName))
			return false;
		return true;
	}

	public void clone(CtdbLookup lu) {
        this.setId(lu.getId());
        if (lu.getShortName() != null) {
            this.shortName = new String(lu.getShortName());
        }
        if (lu.longName != null) {
            this.longName = new String(lu.getLongName());
        }

    }
    /**
     * This method allows the transformation of a CtdbLookup into an XML Document.
     * If no implementation is available at this time,
     * an UnsupportedOperationException will be thrown.
     *
     * @return XML Document
     * @throws TransformationException is thrown if there is an error during the XML tranformation
     */
    public Document toXML() throws TransformationException {
        return this.toXML("lookup");
    }

    /**
     * Overloaded method that allows the transformation of a CtdbLookup into an XML Document.
     * This method takes a Lookup Name in order to set a customizable root node.
     * If no implementation is available at this time,
     * an UnsupportedOperationException will be thrown.
     *
     * @param lookupName The lookup name for the root node
     * @return XML Document
     * @throws TransformationException is thrown if there is an error during the XML tranformation.
     */
    public Document toXML(String lookupName) throws TransformationException {
        try {
            Document document = super.newDocument();
            Element root = super.initXML(document, lookupName);

            Element shortNameNode = document.createElement("shortname");
            shortNameNode.appendChild(document.createTextNode(this.shortName));
            root.appendChild(shortNameNode);

            if (this.longName != null) {
                Element longNameNode = document.createElement("longname");
                longNameNode.appendChild(document.createTextNode(this.longName));
                root.appendChild(longNameNode);
            }

            return document;
        } catch (Exception ex) {
            throw new TransformationException("Unable to transform object " + this.getClass().getName() + " with id = " + this.getId());
        }
    }
}

package gov.nih.nichd.ctdb.patient.domain;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.TransformationException;

/**
 * Created by IntelliJ IDEA. User: matt Date: Nov 5, 2009 Time: 2:39:50 PM To
 * change this template use File | Settings | File Templates.
 */
public class PatientGroup extends PatientCategory {
	private static final long serialVersionUID = -1991099540140247719L;

	private static final String type = "Group";

	public String getType() {
		return type;
	}

	/**
	 * This method allows the transformation of a Protocol into an XML Document.
	 * If no implementation is available at this time, an
	 * UnsupportedOperationException will be thrown.
	 *
	 * @return XML Document
	 * @throws TransformationException is thrown if there is an error during the XML tranformation
	 * @throws UnsupportedOperationException is thrown if this method is currently unsupported and not implemented.
	 */
	public Document toXML() throws TransformationException, UnsupportedOperationException {
		throw new UnsupportedOperationException("No Implementation at this time for the method toXML() in Protocol.");
	}
}

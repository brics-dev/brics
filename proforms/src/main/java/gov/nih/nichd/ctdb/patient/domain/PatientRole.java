package gov.nih.nichd.ctdb.patient.domain;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.TransformationException;

/**
 * Created by IntelliJ IDEA. User: breymaim Date: Nov 3, 2006 Time: 10:04:07 AM
 * To change this template use File | Settings | File Templates.
 */
public class PatientRole extends PatientCategory {
	private static final long serialVersionUID = 8470433368468584551L;

	private static final String type = "Role";

	public String getType() {
		return type;
	}

	public PatientRole() {
	}

	public PatientRole(int protocolId) {
		this.setProtocolId(protocolId);
	}

	public PatientRole(int protocolId, String name) {
		this.setProtocolId(protocolId);
		this.setName(name);
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

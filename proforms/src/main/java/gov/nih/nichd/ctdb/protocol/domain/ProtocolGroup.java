package gov.nih.nichd.ctdb.protocol.domain;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

/**
 * Created by IntelliJ IDEA. User: breymaim Date: Sep 6, 2007 Time: 10:01:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProtocolGroup extends CtdbDomainObject {
	private static final long serialVersionUID = -3040377297707422660L;

	private String name;
	private String description;
	private int[] protocols;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int[] getProtocols() {
		return protocols;
	}

	public void setProtocols(int[] protocols) {
		this.protocols = protocols;
	}

	public Document toXML() throws TransformationException, UnsupportedOperationException {
		throw new UnsupportedOperationException("Not Implemented in ProtocolGroup.");
	}
}

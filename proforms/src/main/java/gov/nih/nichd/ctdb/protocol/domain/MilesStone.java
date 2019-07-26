
package gov.nih.nichd.ctdb.protocol.domain;

import java.util.Date;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

/**
 * MilesStone DomainObject for the NICHD CTDB Application
 * 
 * @author kollas2
 *
 */
public class MilesStone  extends CtdbDomainObject {
	private static final long serialVersionUID = -5219912918863278446L;
	
	private String name;
	private Date milesStoneDate = null;
	
	public MilesStone() {
		super();
	}
	
	public MilesStone(String name){
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the milesStoneDate
	 */
	public Date getMilesStoneDate() {
		return milesStoneDate;
	}

	/**
	 * @param milesStoneDate the milesStoneDate to set
	 */
	public void setMilesStoneDate(Date milesStoneDate) {
		this.milesStoneDate = milesStoneDate;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (this == obj) {
			return true;
		}

		if (obj instanceof MilesStone) {
			MilesStone other = (MilesStone) obj;

			return name.equals(other.name);
		}

		return false;
	}
	@Override
	public Document toXML() throws TransformationException {
		try	{
			Document document = super.newDocument();
			Element root = super.initXML(document, "MilesStone");

			Element typeNode = document.createElement("name");
			typeNode.appendChild(document.createTextNode(this.getName()));
			root.appendChild(typeNode);
			
			return document;

		} catch (Exception ex) {
			throw new TransformationException("Unable to transform object " + this.getClass().getName() + " with id = " + this.getId());
		}
	}
}

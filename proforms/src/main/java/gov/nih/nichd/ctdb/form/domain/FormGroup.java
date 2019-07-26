package gov.nih.nichd.ctdb.form.domain;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

/**
 * Created by IntelliJ IDEA.
 * User: matt
 * Date: May 20, 2005
 * Time: 11:42:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class FormGroup extends CtdbDomainObject {
	private static final long serialVersionUID = 7958782423952630357L;
	
	private String name = "";
    private String description = "";
    private int orderValue = 0;
    private String associatedForms = "";
    private int protocolId = Integer.MIN_VALUE;


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

    public int getOrderValue() {
        return orderValue;
    }

    public void setOrderValue(int orderValue) {
        this.orderValue = orderValue;
    }

    public int getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(int protocolId) {
        this.protocolId = protocolId;
    }

    public String getAssociatedForms() {
		return associatedForms;
	}

	public void setAssociatedForms(String associatedForms) {
		this.associatedForms = associatedForms;
	}

	public boolean equals (FormGroup otherFg) {
		return otherFg.getId() == this.getId();
    }

    public Document toXML() throws TransformationException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException("toXML() is not supported in FormGroup.");
    }
}

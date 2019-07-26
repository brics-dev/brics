package gov.nih.nichd.ctdb.contacts.domain;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.TransformationException;
import gov.nih.nichd.ctdb.util.domain.Address;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Jun 25, 2007
 * Time: 2:18:33 PM
 * To change this template use File | Settings | File Templates.
 */
public class ExternalContact extends CtdbDomainObject {
	private static final long serialVersionUID = -8217989892995598475L;
	
	private int protocolId;
    private String name;
    private String emailAddress;
    private CtdbLookup contactType;
    private String organization;
    private CtdbLookup institute = new CtdbLookup(11); // default to nichd
    private String phone1;
    private String phone2;
    private Address address;
    private int studySiteId;


    public int getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(int protocolId) {
        this.protocolId = protocolId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }


    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }


    public CtdbLookup getContactType() {
        return contactType;
    }

    public void setContactType(CtdbLookup contactType) {
        this.contactType = contactType;
    }

    public CtdbLookup getInstitute() {
        return institute;
    }

    public void setInstitute(CtdbLookup institute) {
        this.institute = institute;
    }

    public String getPhone1() {
        return phone1;
    }

    public void setPhone1(String phone1) {
        this.phone1 = phone1;
    }

    public String getPhone2() {
        return phone2;
    }

    public void setPhone2(String phone2) {
        this.phone2 = phone2;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Document toXML() throws TransformationException {
        throw new UnsupportedOperationException("Not Implemented 4 externa contact");
    }

	public int getStudySiteId() {
		return studySiteId;
	}

	public void setStudySiteId(int studySiteId) {
		this.studySiteId = studySiteId;
	}

}

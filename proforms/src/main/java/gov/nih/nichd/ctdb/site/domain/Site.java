package gov.nih.nichd.ctdb.site.domain;

import java.io.Serializable;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.util.domain.Address;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Nov 19, 2007
 * Time: 2:34:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class Site  extends CtdbDomainObject implements Serializable {
	private static final long serialVersionUID = -7567153979453280633L;
	
	private String name = "";
    private int protocolId = -1;
    private String description = "";
    private String phoneNumber = "";
    private Address address = new Address();
    private String siteActionFlag = ""; //To store user action of adding/editing/deleting site. 
    private String sitePrincipleInvestigator = "";
    private User sitePI = new User();
    private String siteURL = "";
    private boolean primarySite = false;
    private String studySiteId = "";
    private String bricsStudySiteId;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = StringUtils.trim(name);
    }

    public int getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(int protocolId) {
        this.protocolId = protocolId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = StringUtils.trim(description);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = StringUtils.trim(phoneNumber);
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Document toXML() throws TransformationException, UnsupportedOperationException {
    	throw new UnsupportedOperationException("toXML() not supported in Site.");
    }

	public String getSiteActionFlag() {
		return StringUtils.trim(siteActionFlag);
	}

	public void setSiteActionFlag(String siteActionFlag) {
		this.siteActionFlag = StringUtils.trim(siteActionFlag);
	}

	public String getSitePrincipleInvestigator() {
		return sitePrincipleInvestigator;
	}

	public void setSitePrincipleInvestigator(String sitePrincipleInvestigator) {
		this.sitePrincipleInvestigator = StringUtils.trim(sitePrincipleInvestigator);
	}

	public String getSiteURL() {
		return siteURL;
	}

	public void setSiteURL(String siteURL) {
		this.siteURL = StringUtils.trim(siteURL);
	}

	public String getStudySiteId() {
		return studySiteId;
	}

	public void setStudySiteId(String studySiteId) {
		this.studySiteId = StringUtils.trim(studySiteId);
	}

	public boolean isPrimarySite() {
		return primarySite;
	}

	public void setPrimarySite(boolean primarySite) {
		this.primarySite = primarySite;
	}

	public User getSitePI() {
		return sitePI;
	}

	public String getBricsStudySiteId() {
		return bricsStudySiteId;
	}

	public void setBricsStudySiteId(String bricsStudySiteId) {
		this.bricsStudySiteId = bricsStudySiteId;
	}

	public void setSitePI(User sitePI) {
		this.sitePI = sitePI;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(address, bricsStudySiteId, description, name, phoneNumber, protocolId,
				sitePI, sitePrincipleInvestigator, siteURL, studySiteId);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Site other = (Site) obj;
		return Objects.equals(address, other.address) && Objects.equals(bricsStudySiteId, other.bricsStudySiteId)
				&& Objects.equals(description, other.description) && Objects.equals(name, other.name)
				&& Objects.equals(phoneNumber, other.phoneNumber) && protocolId == other.protocolId
				&& Objects.equals(sitePI, other.sitePI)
				&& Objects.equals(sitePrincipleInvestigator, other.sitePrincipleInvestigator)
				&& Objects.equals(siteURL, other.siteURL) && Objects.equals(studySiteId, other.studySiteId);
	}
}

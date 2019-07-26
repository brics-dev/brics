package gov.nih.nichd.ctdb.audit.domain;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.TransformationException;

/**
 * Created by wangla.
 * Date: June 19, 2014.
 * To change this template use File | Settings | File Templates.
 */
public class Audit  extends CtdbDomainObject {
	private static final long serialVersionUID = -6675896387471813674L;
	
	private int protocolId;
    private String reason;

    public int getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(int protocolId) {
        this.protocolId = protocolId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = StringUtils.trim(reason);
    }

    public Document toXML() throws TransformationException, UnsupportedOperationException
    {
        throw new UnsupportedOperationException("No Implementation at this time for the method toXML() in Audit.");
    }
    
}

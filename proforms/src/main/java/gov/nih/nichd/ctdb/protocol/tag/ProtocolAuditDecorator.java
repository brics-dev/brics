package gov.nih.nichd.ctdb.protocol.tag;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.Version;
import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;

/**
 * ProtocolAuditDecorator enables a table to display protocol
 * version information without client side java coding.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class ProtocolAuditDecorator extends ActionDecorator
{

    /** Creates a new instance of ProtocolAuditDecorator */
    public ProtocolAuditDecorator()
    {
        super();
    }

    /** returns the alphabetic version
     * @return  the version to display
     */
    public String getVersionDec()
    {
        Version ver = ((CtdbDomainObject) this.getObject()).getVersion();
        return ver.toString();
    }

    /**  getStatusDec returns the long name of the protocols status
     * @return  String : the status
     */
    public String getStatusDec()
    {
        CtdbLookup lu = ((Protocol) this.getObject()).getStatus();
        return lu.getShortName();
    }
}

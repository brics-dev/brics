package gov.nih.nichd.ctdb.util.common;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Oct 30, 2006
 * Time: 3:36:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class PatientRoleLookupResultControl extends  LookupResultControl {


    private int protocolId = Integer.MIN_VALUE;

    public int getProtocolId() {
        return protocolId;
    }

    public void setProtocolId(int protocolId) {
        this.protocolId = protocolId;
    }

    public String getSearchClause () {
        if (this.protocolId != Integer.MIN_VALUE) {
            return " and protocolpatientrole.protocolid = " + protocolId + " ";
        } else {
            return " ";
        }
    }
}

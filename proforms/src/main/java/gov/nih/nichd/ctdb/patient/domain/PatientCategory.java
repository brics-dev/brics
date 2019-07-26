package gov.nih.nichd.ctdb.patient.domain;

import gov.nih.nichd.ctdb.common.CtdbDomainObject;

/**
 * Created by IntelliJ IDEA.
 * User: matt
 * Date: Nov 6, 2009
 * Time: 8:45:04 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class PatientCategory extends CtdbDomainObject {

     private int protocolId;
    private String name;
    private String description;

    public abstract String getType();

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

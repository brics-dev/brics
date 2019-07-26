package gov.nih.nichd.ctdb.patient.domain;

import gov.nih.nichd.ctdb.common.CtdbPerson;
import gov.nih.nichd.ctdb.security.domain.User;
/**
 * Patient DomainObject for the NICHD CTDB Application
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class NextOfKin extends CtdbPerson 
{
    private String relationship;
    
    /** Creates a new instance of NextOfKin */
    public NextOfKin() 
    {
    }
    
    /** Getter for property relationship.
     * @return Value of property relationship.
     */
    public String getRelationship() {
        return relationship;
    }
    
    /** Setter for property relationship.
     * @param relationship New value of property relationship.
     */
    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
    
        public void updateCreatedByInfo (User u)
    {
        super.updateCreatedByInfo (u);
        this.getHomeAddress().updateCreatedByInfo(u);
    }
    
    
    public void updateUpdatedByInfo (User u)
    {
        super.updateUpdatedByInfo (u);
        this.getHomeAddress().updateUpdatedByInfo(u);
    }
}


package gov.nih.tbi.commons.model;

import java.util.Date;

/**
 * This interface is used to hide any undesirable information from the user
 * 
 * @author Andrew Johnson
 * 
 */
public interface GuidSubject
{

    /**
     * Gets the ID of the Subject row
     * 
     * @return ID of subject
     */
    public Long getId();

    /**
     * Gets the GUID of the Subject
     * 
     * @return guid value
     */
    public String getGuid();

    /**
     * Gets the date the GUID was created
     * 
     * @return date created
     */
    public Date getDateCreated();

    public String getCreatedBy();

    public String getCreatedOrg();
}

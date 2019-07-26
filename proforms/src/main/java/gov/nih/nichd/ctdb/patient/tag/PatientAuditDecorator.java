package gov.nih.nichd.ctdb.patient.tag;

import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.patient.domain.Patient;

/**
 * PatientHomeDecorator enables a table to have a column with Action links (Edit/View/..). This
 *  class works with the <code>display</code> tag library.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class PatientAuditDecorator extends ActionDecorator
{

    /**
     * Default Constructor
     */
    public PatientAuditDecorator()
    {
        super();
    }

}

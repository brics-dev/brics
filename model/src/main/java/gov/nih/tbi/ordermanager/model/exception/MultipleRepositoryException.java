
package gov.nih.tbi.ordermanager.model.exception;

import gov.nih.tbi.ordermanager.model.BiospecimenItem;
import gov.nih.tbi.ordermanager.model.BiospecimenOrder;

/**
 * Exception class to indicate that a {@link BiospecimenOrder} is being created with {@link BiospecimenItem} that belong
 * to two different repositories
 * 
 * @author vpacha
 * 
 */
public class MultipleRepositoryException extends Exception
{

    private static final long serialVersionUID = -7813041832313218747L;

    public MultipleRepositoryException(String message)
    {

        super(message);
    }

    public MultipleRepositoryException(String message, Throwable cause)
    {

        super(message, cause);
    }
}

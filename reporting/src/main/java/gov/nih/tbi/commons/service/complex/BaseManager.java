
package gov.nih.tbi.commons.service.complex;

/**
 * Base Manager
 * 
 * @author Nimesh Patel
 * 
 */
public interface BaseManager
{

 
    /**
     * Escape the characters '_' '%' and '\' with '\' character for use in a hibernate Restrictions.iLike() statement.
     * 
     * @param input
     *            : The string to be escaped, can be null
     * @return
     */
    public String escapeForILike(String input);

}

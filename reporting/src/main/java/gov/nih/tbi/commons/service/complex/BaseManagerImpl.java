
package gov.nih.tbi.commons.service.complex;

import java.io.Serializable;

import org.apache.log4j.Logger;

/**
 * This is the base manager implemenation that uses hibernate
 * 
 * @author Nimesh Patel
 * 
 */
public class BaseManagerImpl implements BaseManager, Serializable
{

    private static final long serialVersionUID = -3856535486237089289L;

    static Logger logger = Logger.getLogger(BaseManagerImpl.class);

    


    /**
     * Escape the characters '_' '%' and '\' with '\' character for use in a hibernate Restrictions.iLike() statement.
     * 
     * @param input
     *            : The string to be escaped, can be null
     * @return
     */
    public String escapeForILike(String input)
    {

        // null check for this function.
        if (input == null)
            return null;

        for (int i = 0; i < input.length(); i++)
        {
            char c = input.charAt(i);
            if (c == '\\' || c == '%' || c == '_')
            {
                input = input.substring(0, i) + '\\' + input.substring(i, input.length());
                i++;
            }
        }
        return input;
    }

}

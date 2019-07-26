
package gov.nih.tbi.commons.util;

import gov.nih.tbi.PostgreConstants;

/**
 * Class containing utility functions related to PostgreSql.
 * 
 * @author dhollo
 * 
 */
public class PostgreUtils
{

    private PostgreUtils()
    {

        // private constructor to prevent instantiation
        throw new AssertionError();
    }

    /**
     * Checks if a provided column name conflicts with reserved columns in PostgreSQL.
     * 
     * @see gov.nih.tbi.commons.util.DaoUtils#getNameSubstitution(String)
     * 
     * @param name
     *            - the column name
     * @return true if the provided column conflicts
     */
    public static boolean isReservedColumnName(String name)
    {

        for (String systemColumn : PostgreConstants.SYSTEM_COLUMNS)
        {
            if (systemColumn.equals(name))
            {
                return true;
            }
        }
        return false;
    }
}
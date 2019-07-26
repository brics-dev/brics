package gov.nih.nichd.ctdb.util.common;

import gov.nih.nichd.ctdb.common.ResultControl;

/**
 * LookupResultControl handles sorting and searching for system lookups
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class LookupResultControl extends ResultControl
{
    /**
     *  LookupResultControl Constant used to sort by longname
     */
    public static final String SORT_BY_LONGNAME = "longname";

    /**
     *  LookupResultControl Constant used to sort by shortname
     */
    public static final String SORT_BY_SHORTNAME = "shortname";


    

    /**
     * Gets the Search Clause for this SQL operation to determine the results to be returned.
     *
     * @return The string representation of the search clause for this SQL operation
     */
    public String getSearchClause()
    {
        return null;
    }
}

package gov.nih.nichd.ctdb.question.common;

import gov.nih.nichd.ctdb.common.ResultControl;

/**
 * GroupResultControl handles searching and sorting of question groups in the system.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class GroupResultControl extends ResultControl
{
    /**
     * Default constructor for the GroupResultControl
     */
    public GroupResultControl()
    {
        // default constructor
    }

    /**
     *  GroupResultControl Constant used to sort by name
     */
    public static final String SORT_BY_NAME = "UPPER(name)";

    /**
     *  GroupResultControl Constant used to sort by description
     */
    public static final String SORT_BY_DESCRIPTION = "UPPER(description)";

    /**
     *  GroupResultControl Constant used to sort by created date
     */
    public static final String SORT_BY_CREATEDDATE = "createddate";

    /**
     *  GroupResultControl Constant used to sort by lastupdated
     */
    public static final String SORT_BY_LASTUPDATED = "updateddate";

    /**
     * Gets the Search Clause for this SQL operation to determine the results to be returned.
     *
     * @return The string representation of the search clause for this SQL operation
     */
    public String getSearchClause()
    {
        return null;  // no searches implemented at this time
    }

}

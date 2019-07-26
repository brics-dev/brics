package gov.nih.nichd.ctdb.common;

import java.sql.Date;


/**
 * ResultControl will handle all contorl of the ResultSet for ordering and sorting.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public abstract class ResultControl {
    public static final String SORT_ASC = "asc";
    public static final String SORT_DESC = "desc";

    private String sortBy = null;
    private String sortOrder = null;
    private int rowNumMax = Integer.MIN_VALUE; // this has to be set for limiting rows

    /**
     * Get the RowNumMax value.
     *
     * @return the RowNumMax value.
     */
    public int getRowNumMax() {
        return rowNumMax;
    }

    /**
     * Set the RowNumMax value.
     *
     * @param newRowNumMax The new RowNumMax value.
     */
    public void setRowNumMax(int newRowNumMax) {
        this.rowNumMax = newRowNumMax;
    }


    /**
     * Default Constructor for the ResultControl Object
     */
    public ResultControl() {
        //default constructor
        sortOrder = ResultControl.SORT_ASC;
    }

    /**
     * Gets the sort by column for the results to be returned
     *
     * @return The sort by column
     */
    public String getSortBy() {
        return sortBy;
    }

    /**
     * Sets the sort by column for the results to be returned
     *
     * @param sortBy The sort by column
     */
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    /**
     * Gets the sort order for the results to be returned
     *
     * @return The sort order (ASC/DESC)
     */
    public String getSortOrder() {
        return sortOrder;
    }

    /**
     * Sets the sort order for the results to be returned
     *
     * @param sortOrder The sort order: ASC/DESC
     */
    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    /**
     * Gets the Order By SQL operation for the results to be returned
     *
     * @return The string representation of the order by SQL operation
     */
    public String getSortString() {
        if (this.sortBy != null && !this.sortBy.equalsIgnoreCase("")) {
            return " order by " + this.sortBy + ' ' + this.sortOrder;
        } else {
            return " ";
        }
    }

    /**
     * Gets the first part of the SQL string when limiting rows
     *
     * @return The first part of the SQL string when limiting rows
     */
    public String getRowLimitStringPrefix() {
        if (this.isLimitingRows()) {
            return " select * from ( ";
        } else {
            return " ";
        }
    }

    /**
     * Gets the last part of the SQL string when limiting rows
     *
     * @return The last part of the SQL string when limiting rows
     */
    public String getRowLimitStringSurfix() {
        if (this.isLimitingRows()) {
            String strRowNumMax = (new Integer(rowNumMax)).toString();
            return " ) as alias where rownum <= " + strRowNumMax + ' ';
        } else {
            return " ";
        }
    }

    /**
     * Checks to see if rowNumMin or rowNumMax is set.
     *
     * @return is limiting rows.
     */
    private boolean isLimitingRows() {
        return this.rowNumMax != Integer.MIN_VALUE && this.rowNumMax > 0;
    }

    /**
     * Replaces apostrophe ' with two apostrophes ''
     *
     * @param toReplace The string to replace.
     * @return The string with single quotes inserted
     */
    public String replaceQuotes(String toReplace) {
        StringBuffer buff = new StringBuffer(toReplace);
        for (int i = 0; i < buff.length(); i++) {
            if (buff.charAt(i) == '\'') {
                buff.insert(i, '\'');
                i++;
            }
        }
        return buff.toString();
    }

    /**
     * Replaces escape percentage sign % with a "^"
     *
     * @param toReplace The string to replace.
     * @return The string with carat inserted
     */
    public String escapePercentageSign(String toReplace) {
        StringBuffer buff = new StringBuffer(toReplace);
        for (int i = 0; i < buff.length(); i++) {
            if (buff.charAt(i) == '%') {
                buff.insert(i, '^');
                i++;
            }
        }
        return buff.toString();
    }

    protected boolean hasData (String s) {
        if (s != null && !s.trim().equals("")){
            return true;
        } else {
            return false;
        }
    }
    protected boolean hasData (int i) {
        if ( i != Integer.MIN_VALUE && i != 0){
            return true;
        } else {
            return false;
        }
    }



        protected boolean hasData (Date s) {
        if (s != null ){
            return true;
        } else {
            return false;
        }
    }

    /**
     * Gets the Search Clause for this SQL operation to determine the results to be returned.
     * This method must be implemented by all sub-classes.
     *
     * @return The string representation of the search clause for this SQL operation
     */
    public abstract String getSearchClause();
}

package gov.nih.nichd.ctdb.common;

import java.io.Serializable;
import java.util.Date;

/**
 * Base Struts form for CTDB
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class CtdbForm implements Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 2527226152048256161L;
	protected String action = null;
    private int id = Integer.MIN_VALUE;
    private int objectClass;
    // assists in determining if search has been submitted
    private String searchSubmitted;

    /**
     * Return the action.
     *
     * @return String action
     */
    public String getAction() {
        return action;
    }

    /**
     * Set the action.
     *
     * @param action The action entered.
     */
    public void setAction(String action) {
        this.action = action;
    }

    /**
     * Return the id.
     *
     * @return String id
     */
    public int getId() {
        return id;
    }

    /**
     * Set the id.
     *
     * @param id The id used for editing.
     */
    public void setId(int id) {
        this.id = id;
    }

    public int getObjectClass() {
        return objectClass;
    }

    public void setObjectClass(int objectClass) {
        this.objectClass = objectClass;
    }

    public void setSearchSubmitted(String searchSubmitted) {
		this.searchSubmitted = searchSubmitted;
	}

	public String getSearchSubmitted() {
		return searchSubmitted;
	}

	/**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    public void reset() {
        this.id = Integer.MIN_VALUE;
        this.action = null;
    }

    /**
     * Sets the properties based upon an existing CtdbForm
     *
     * @param ctdbForm The ctdbForm to be cloned from
     */
    public void clone(CtdbForm ctdbForm) {
        this.id = ctdbForm.id;
        this.action = ctdbForm.action;
    }

    public String notNull (String s) {
        if (s == null || s.trim().equals("") || s.equalsIgnoreCase("null")) {
            return "";
        } else {
            return s;
        }

    }
    public boolean isDateInRange(Date curDate, Date minDate, Date maxDate){
    	return curDate.after(minDate) && curDate.before(maxDate);
    }
}

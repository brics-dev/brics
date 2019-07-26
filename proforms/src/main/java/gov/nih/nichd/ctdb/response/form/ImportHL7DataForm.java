package gov.nih.nichd.ctdb.response.form;

import gov.nih.nichd.ctdb.common.CtdbForm;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.security.domain.User;


/**
 * The ResolveForm represents the Java class behind the HTML
 * for nichd ctdb data entry resolve page
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class ImportHL7DataForm extends CtdbForm
{
	private int intervalId = Integer.MIN_VALUE;
	private int importType = Integer.MIN_VALUE;
	private int theFormId=Integer.MIN_VALUE;
	private Protocol protocol = new Protocol();
	private User user = new User();
	private String dataSourceURL="";
	// for import data by subject
	private int subjectId=Integer.MIN_VALUE;
	private boolean isBySubject=false;
	
	public boolean isBySubject() {
		return isBySubject;
	}

	public void setBySubject(boolean isBySubject) {
		this.isBySubject = isBySubject;
	}

	public int getSubjectId() {
		return subjectId;
	}

	public void setSubjectId(int subjectId) {
		this.subjectId = subjectId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public Protocol getProtocol() {
		return protocol;
	}

	public void setProtocol(Protocol protocol) {
		this.protocol = protocol;
	}

	public int getTheFormId() {
		return theFormId;
	}

	public void setTheFormId(int theFormId) {
		this.theFormId = theFormId;
	}

	public int getImportType() {
		return importType;
	}

	public void setImportType(int importType) {
		this.importType = importType;
	}

	public int getIntervalId() {
		return intervalId;
	}

	public void setIntervalId(int intervalId) {
		this.intervalId = intervalId;
	}
	public String getDataSourceURL() {
		return dataSourceURL;
	}

	public void setDataSourceURL(String dataSourceURL) {
		this.dataSourceURL = dataSourceURL;
	}

	/**
     * Reset all properties to their default values.
     */
    public void reset()
    {
        this.intervalId = Integer.MIN_VALUE;
        this.importType = Integer.MIN_VALUE;
        this.theFormId = Integer.MIN_VALUE;
        this.protocol = new Protocol();
    	this.user = new User();
    	this.dataSourceURL = "";
    	this.subjectId = Integer.MIN_VALUE;
    	this.isBySubject=false;
    }

	
}

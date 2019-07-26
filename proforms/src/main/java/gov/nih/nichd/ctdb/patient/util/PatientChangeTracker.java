package gov.nih.nichd.ctdb.patient.util;

import gov.nih.nichd.ctdb.security.domain.User;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: matt
 * Date: Oct 6, 2010
 * Time: 2:49:41 PM
 * To change this template use File | Settings | File Templates.
 */
public class PatientChangeTracker {

    private Map<String, String[]> changedFields = new HashMap<String, String[]>();
    private Map<String, String> reasons = new HashMap<String, String>();
    private List<String> errors = new ArrayList<String>();
    private User editingUser;
    private int versionNumber = 0;


	public int getVersionNumber() {
		return versionNumber;
	}

	public void setVersionNumber(int versionNumber) {
		this.versionNumber = versionNumber;
	}

	public Map<String, String[]> getChangedFields() {
        return changedFields;
    }

    public void setChangedFields(Map<String, String[]> changedFields) {
        this.changedFields = changedFields;
    }

    public Map<String, String> getReasons() {
        return reasons;
    }

    public void setReasons(Map<String, String> reasons) {
        this.reasons = reasons;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }

    public User getEditingUser() {
        return editingUser;
    }

    public void setEditingUser(User editingUser) {
        this.editingUser = editingUser;
    }
}

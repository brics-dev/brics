package gov.nih.tbi.service.model;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.semantic.model.QueryPermissions.FormResultPermission;
import gov.nih.tbi.semantic.model.QueryPermissions.StudyResultPermission;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS, value = "session")
public class PermissionModel implements Serializable {

	private static final long serialVersionUID = 6506144336548794525L;

	private Account account;

	// key: studyUri
	private Map<String, StudyResultPermission> studyResultPermissions;

	// key: formId TODO: consider changing to formUri
	private Map<Long, FormResultPermission> formResultPermissions;

	private boolean isQueryAdmin;
	private boolean isSysAdmin;

	public PermissionModel() {
		this.studyResultPermissions = new HashMap<String, StudyResultPermission>();
		this.formResultPermissions = new HashMap<Long, FormResultPermission>();
	}

	public PermissionModel(PermissionModel clone) {
		this.account = clone.account;
		this.studyResultPermissions = new HashMap<String, StudyResultPermission>(clone.studyResultPermissions);
		this.formResultPermissions = new HashMap<Long, FormResultPermission>(clone.formResultPermissions);
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Map<String, StudyResultPermission> getStudyResultPermissions() {
		return studyResultPermissions;
	}

	public void setStudyResultPermissions(Map<String, StudyResultPermission> studyResultPermissions) {
		this.studyResultPermissions = studyResultPermissions;
	}

	public void addStudyResultPermission(StudyResultPermission srp) {
		studyResultPermissions.put(srp.getStudyURI(), srp);
	}

	public Map<Long, FormResultPermission> getFormResultPermissions() {
		return formResultPermissions;
	}

	public void setFormResultPermissions(Map<Long, FormResultPermission> formResultPermissions) {
		this.formResultPermissions = formResultPermissions;
	}

	public void addFormResultPermission(FormResultPermission frp) {
		formResultPermissions.put(frp.getFormId(), frp);
	}

	public boolean isQueryAdmin() {
		return isQueryAdmin;
	}

	public void setQueryAdmin(boolean isQueryAdmin) {
		this.isQueryAdmin = isQueryAdmin;
	}

	public boolean isSysAdmin() {
		return isSysAdmin;
	}

	public void setSysAdmin(boolean isSysAdmin) {
		this.isSysAdmin = isSysAdmin;
	}

	public String getUserName() {
		if (account != null) {
			return account.getUserName();
		}

		return null;
	}

}

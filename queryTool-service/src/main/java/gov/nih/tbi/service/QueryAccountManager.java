package gov.nih.tbi.service;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.pojo.StudyResult;
import gov.nih.tbi.service.model.PermissionModel;

import java.util.List;

public interface QueryAccountManager extends BaseManager {

	public void updateGraphAccount(PermissionModel permissionModel);
	
	public List<StudyResult> hidePrivateStudyToNonAdmin(List<StudyResult> studyResultList,
			PermissionModel permissionModel);

	public boolean canEditSavedQuery(Long entityId, Account account);
	
	public boolean canDeleteSavedQuery(Long entityId, Account account);
	
}

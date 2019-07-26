package gov.nih.tbi.metastudy.portal;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import org.apache.log4j.Logger;
import org.apache.struts2.result.StreamResult;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.account.ws.exception.UserAccessDeniedException;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.MetaStudyStatus;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.portal.BaseAction;
import gov.nih.tbi.commons.service.MetaStudyManager;
import gov.nih.tbi.commons.service.StaticReferenceManager;
import gov.nih.tbi.metastudy.model.SessionMetaStudy;
import gov.nih.tbi.portal.PortalUtils;
import gov.nih.tbi.repository.model.SessionUploadFile;

public class BaseMetaStudyAction extends BaseAction {

	private static final long serialVersionUID = -3847123251438277367L;

	@Autowired
	protected SessionMetaStudy sessionMetaStudy;

	@Autowired
	protected StaticReferenceManager staticManager;

	@Autowired
	protected MetaStudyManager metaStudyManager;
	
	@Autowired
	protected SessionUploadFile sessionUploadFile;


	public SessionMetaStudy getSessionMetaStudy() {

		if (sessionMetaStudy == null) {
			sessionMetaStudy = new SessionMetaStudy();
		}

		return sessionMetaStudy;
	}

	private PermissionType getSessionMetaStudyPermission() throws UnsupportedEncodingException,UserAccessDeniedException {
		
		if (sessionMetaStudy.getSessionMetaStudyUserPermissionType() != null) {
			return sessionMetaStudy.getSessionMetaStudyUserPermissionType();
		} else {
			String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
			RestAccountProvider restProvider = new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
			
			EntityMap entityMap = restProvider.getAccess(getAccount().getId(), EntityType.META_STUDY,
					sessionMetaStudy.getMetaStudy().getId());
			
			sessionMetaStudy.setSessionMetaStudyUserEntityMap(entityMap);
			sessionMetaStudy.setSessionMetaStudyUserPermissionType(entityMap.getPermission());
	
			return entityMap.getPermission();
		}
	}

	public boolean getHasAdminPermission() throws UserAccessDeniedException{
		try {
			return PermissionType.compare(getSessionMetaStudyPermission(), PermissionType.ADMIN) >= 0;
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
			return false;
		}
	}

	public boolean getHasWritePermission() throws UserAccessDeniedException{
		try {
			return PermissionType.compare(getSessionMetaStudyPermission(), PermissionType.WRITE) >= 0;
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
			return false;
		}
	}

	public boolean getHasReadPermission() throws UserAccessDeniedException{
		try {
			return PermissionType.compare(getSessionMetaStudyPermission(), PermissionType.READ) >= 0;
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
			return false;
		}
	}

	public boolean getIsOwner() throws UserAccessDeniedException{
		try {
			return PermissionType.compare(getSessionMetaStudyPermission(), PermissionType.OWNER) == 0;
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
			return false;
		}
	}

	public boolean getIsAdmin() {

		// If we are not in session then this functions will throw null pointer exceptions and we simply want to return
		// if the user has the role.
		try {
			if (PermissionType.compare(getSessionMetaStudyPermission(), PermissionType.ADMIN) >= 0) {
				return true;
			}
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
		} catch (UserAccessDeniedException e) {
			e.printStackTrace();
		}

		return getIsMetaStudyAdmin();
	}

	public StreamResult validationSuccess() {
		return new StreamResult(new ByteArrayInputStream((SUCCESS).getBytes()));
	}

	public boolean getIsPublished() {
		return sessionMetaStudy.getMetaStudy().getStatus().equals(MetaStudyStatus.PUBLISHED);
	}

	public boolean getIsAwaitingPublication() {
		return sessionMetaStudy.getMetaStudy().getStatus().equals(MetaStudyStatus.AWAITING_PUBLICATION);
	}

	public boolean getIsDraft() {
		return sessionMetaStudy.getMetaStudy().getStatus().equals(MetaStudyStatus.DRAFT);
	}

	public boolean getIsMetaStudyAdmin() {
		return accountManager.hasRole(getAccount(), RoleType.ROLE_METASTUDY_ADMIN);
	}

	public boolean canAssignDoiForMetaStudy() throws UserAccessDeniedException{
		String doi = sessionMetaStudy.getMetaStudy().getDoi();

		return (doi == null || doi.isEmpty()) && (getIsAdmin() || getIsOwner()) && getIsDoiEnabled()
				&& getIsPublished();
	}
	
	public SessionUploadFile getSessionUploadFile() {

		if (sessionUploadFile == null) {
			sessionUploadFile = new SessionUploadFile();
		}
		return sessionUploadFile;
	}

}

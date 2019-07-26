package gov.nih.tbi.dictionary.portal;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;

import org.apache.struts2.result.StreamResult;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.account.ws.exception.UserAccessDeniedException;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.dictionary.model.formbuilder.SessionEform;
import gov.nih.tbi.dictionary.service.DictionaryService;
import gov.nih.tbi.portal.PortalUtils;
import gov.nih.tbi.commons.service.EformManager;

public class BaseEformAction extends BaseDictionaryAction {
	/**
	 * Genereated serialVersionUID
	 */
	private static final long serialVersionUID = 6852407954810792283L;
	
	@Autowired
	protected SessionEform sessionEform;
	
	@Autowired
	DictionaryService dictionaryService;
	
	@Autowired
	protected EformManager eformManager;

	public SessionEform getSessionEform(){
		return this.sessionEform;
	}
	
	private PermissionType getSessionEformPermission() throws UnsupportedEncodingException, UserAccessDeniedException {
		
		if(sessionEform.getSessionEformUserPermissionType() != null){
			return sessionEform.getSessionEformUserPermissionType();
		} else {
			String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
			RestAccountProvider restProvider = new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
			PermissionType userEntityAccess = null;
			
			if(sessionEform.getBasicEform() == null){
				userEntityAccess = restProvider.getAccess(getAccount().getId(), EntityType.EFORM, sessionEform.getEform().getId()).getPermission();
			} else {
				userEntityAccess = restProvider.getAccess(getAccount().getId(), EntityType.EFORM, sessionEform.getBasicEform().getId()).getPermission();
			}
			
			sessionEform.setSessionEformUserPermissionType(userEntityAccess);
			return sessionEform.getSessionEformUserPermissionType();
		}
	}

	public boolean getHasAdminPermission() throws UserAccessDeniedException{
		try {
			return (getSessionEformPermission() != null && PermissionType.compare(getSessionEformPermission(), PermissionType.ADMIN) >= 0);
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
			return false;
		}
	}

	public boolean getHasWritePermission() throws UserAccessDeniedException{
		try {
			return (getSessionEformPermission() != null && PermissionType.compare(getSessionEformPermission(), PermissionType.WRITE) >= 0);
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
			return false;
		}
	}

	public boolean getHasReadPermission() throws UserAccessDeniedException{
		try {
			return (getSessionEformPermission() != null && PermissionType.compare(getSessionEformPermission(), PermissionType.READ) >= 0);
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
			return false;
		}
	}

	public boolean getIsOwner() throws UserAccessDeniedException{
		try {
			return (getSessionEformPermission() != null && PermissionType.compare(getSessionEformPermission(), PermissionType.OWNER) == 1);
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
			return false;
		}
	}

	public boolean getIsAdmin() {
		try {
			if ((getSessionEformPermission() != null && PermissionType.compare(getSessionEformPermission(), PermissionType.ADMIN) >= 0)) {
				return true;
			}
		} catch (UnsupportedEncodingException uee) {
			uee.printStackTrace();
		} catch(UserAccessDeniedException ex) {
			ex.printStackTrace();
		}

		return getIsDictionaryAdmin();
	}

	public StreamResult validationSuccess() {
		return new StreamResult(new ByteArrayInputStream((SUCCESS).getBytes()));
	}

	public boolean getIsPublished() {
		return sessionEform.getBasicEform().getStatus().equals(StatusType.PUBLISHED);
	}
	
	public boolean getIsArchived() {
		return sessionEform.getBasicEform().getStatus().equals(StatusType.ARCHIVED);
	}

	public boolean getIsAwaitingPublication() {
		return sessionEform.getBasicEform().getStatus().equals(StatusType.AWAITING_PUBLICATION);
	}

	public boolean getIsDraft() {
		return sessionEform.getBasicEform().getStatus().equals(StatusType.DRAFT);
	}
	
	public boolean getHasEformPermission(){
		return accountManager.hasRole(getAccount(), RoleType.ROLE_DICTIONARY_EFORM);
	}
	
	protected RestAccountProvider getNewAccountRestProvider(){
		String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
		return new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
	}
	
	public String getOrgName() {

		return modulesConstants.getModulesOrgName(getDiseaseId()).toLowerCase();
	}
	
}

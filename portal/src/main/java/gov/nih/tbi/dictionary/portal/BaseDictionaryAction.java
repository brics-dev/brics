package gov.nih.tbi.dictionary.portal;

import java.io.UnsupportedEncodingException;

import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.ws.RestAccountProvider;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.portal.BaseAction;
import gov.nih.tbi.commons.service.DictionaryToolManager;
import gov.nih.tbi.commons.service.EformManager;
import gov.nih.tbi.commons.service.StaticReferenceManager;
import gov.nih.tbi.dictionary.model.SessionCondition;
import gov.nih.tbi.dictionary.model.SessionDataElement;
import gov.nih.tbi.dictionary.model.SessionDataElementList;
import gov.nih.tbi.dictionary.model.SessionDataStructure;
import gov.nih.tbi.dictionary.service.DictionaryServiceInterface;
import gov.nih.tbi.portal.PortalUtils;
import gov.nih.tbi.repository.model.SessionUploadFile;

public class BaseDictionaryAction extends BaseAction {

	// This should be removed and replaced with the better, DictionaryService
	@Autowired
	protected DictionaryToolManager dictionaryManager;
	
	@Autowired
	EformManager eformManager;

	@Autowired
	protected DictionaryServiceInterface dictionaryService;

	@Autowired
	protected StaticReferenceManager staticManager;

	private static final long serialVersionUID = -5107367812112408192L;

	/******************************************************************************************************/
	@Autowired
	protected SessionCondition sessionCondition;

	@Autowired
	protected SessionDataStructure sessionDataStructure;

	@Autowired
	protected SessionDataElement sessionDataElement;

	@Autowired
	protected SessionDataElementList sessionDataElementList;

	@Autowired
	protected SessionUploadFile sessionUploadFile;
	
	protected Boolean publicArea;

	/******************************************************************************************************/

	/**
	 * Verifies id of the data element in session, clears session if ID is found to be different
	 * 
	 * @param id
	 */
	public void validateSessionDataElement(Long id) {

		if (sessionDataElement != null && sessionDataElement.getDataElement() != null
				&& !id.equals(sessionDataElement.getDataElement().getId())) {
			sessionDataElement.setDataElement(null);
		}
	}

	/**
	 * @return the publicArea
	 */
	public Boolean getPublicArea() {

		return publicArea;
	}

	/**
	 * @param publicArea the publicArea to set
	 */
	public void setPublicArea(Boolean publicArea) {

		this.publicArea = publicArea;
	}

	public Boolean getInAdmin() {

		return PortalConstants.DICTIONARY_ADMIN.equals(getNameSpace());
	}

	/**
	 * Returns true if the request is coming from the publicData namespace
	 * 
	 * @return
	 */
	public Boolean getInPublicNamespace() {

		return PortalConstants.NAMESPACE_PUBLICDICTIONARY.equals(getNameSpace());
	}

	public SessionDataStructure getSessionDataStructure() {

		if (sessionDataStructure == null) {
			sessionDataStructure = new SessionDataStructure();
		}

		return sessionDataStructure;
	}

	public SessionDataElement getSessionDataElement() {

		if (sessionDataElement == null) {
			sessionDataElement = new SessionDataElement();
		}

		return sessionDataElement;
	}

	public SessionDataElementList getSessionDataElementList() {

		if (sessionDataElementList == null) {
			sessionDataElementList = new SessionDataElementList();
		}

		return sessionDataElementList;
	}

	public SessionCondition getSessionCondition() {

		if (sessionCondition == null) {
			sessionCondition = new SessionCondition();
		}

		return sessionCondition;
	}

	public Boolean getIsDictionaryAdmin() {

		if (dictionaryManager.hasRole(getAccount(), RoleType.ROLE_DICTIONARY_ADMIN)) {
			return true;
		}

		return false;
	}

	public String dictionaryToolButton() {

		return "button";
	}


	public Account getAnonymousAccount() {
		Account account = new Account();
		account.setUserName(CoreConstants.UNAUTHENTICATED_USER);
		account.setId(1L);
		account.setDiseaseKey("-1");

		return account;
	}

	/*convert username in data element's createdBy to be account's display name */
	public String getDisplayNameByUsername(String username) throws UnsupportedEncodingException{
		String displayName = "";
		
		String accountUrl = modulesConstants.getModulesAccountURL(getDiseaseId());
		RestAccountProvider restProvider = new RestAccountProvider(accountUrl, PortalUtils.getProxyTicket(accountUrl));
		
		if(username != null && !username.equals("")){
			if (username.indexOf(",") <= 0){
				Account account = restProvider.getByUserName(username);
				if (account != null){
					displayName = account.getDisplayName();
				}
			} else {
				displayName = username;
			}
		} 
		return displayName;
	}

	public SessionUploadFile getSessionUploadFile() {

		if (sessionUploadFile == null) {
			sessionUploadFile = new SessionUploadFile();
		}
		return sessionUploadFile;
	}

	public String getModulesWithPII() {
		return modulesConstants.getModulesWithPII();
	}
}

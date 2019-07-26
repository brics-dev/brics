package gov.nih.tbi.dictionary.portal.eform;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.portal.AbstractEntityPermissionManagementAction;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.WebServiceManager;
import gov.nih.tbi.dictionary.model.formbuilder.SessionEform;

public class EformPermissionAction extends AbstractEntityPermissionManagementAction {

	private static final long serialVersionUID = -510709279463851481L;

	private static Logger logger = Logger.getLogger(EformPermissionAction.class);

	@Autowired
	SessionEform sessionEform;

	@Autowired
	AccountManager accountManager;

	@Autowired
	WebServiceManager webServiceManager;

	@Override
	public Long getEntityId() {

		if(sessionEform.getEform() != null && sessionEform.getEform().getId() != null){
			return sessionEform.getEform().getId();
		} else {
			return -1l;
		}
	}

	@Override
	public EntityType getEntityType() {

		return EntityType.EFORM;
	}

	@Override
	public List<EntityMap> getEntityMapList() {

		return sessionEform.getEntityMapList();
	}

	@Override
	public List<EntityMap> getRemovedMapList() {

		return sessionEform.getRemovedMapList();
	}

	@Override
	public String getActionName() {
		return "eformPermissionAction";
	}

	@Override
	public List<String> getEntityMapAuthNameList() {
		return sessionEform.getEntityMapAuthNameList();
	}

	// Init for editing permissions. Assumes eform is already in session. Loads the current
	// permissions list.
	@Override
	public String load() throws MalformedURLException, UnsupportedEncodingException {

		if (sessionEform.getEntityMapList() == null) {
			List<EntityMap> entityMapList = new ArrayList<EntityMap>();
			List<String> entityMapAuthNameList = new ArrayList<String>();

			if (getEntityId() != -1) {
				entityMapList = webServiceManager.listEntityAccessRestful(getAccount(), sessionEform.getEform().getId(), EntityType.EFORM);

				for (EntityMap em : entityMapList) {
					entityMapAuthNameList.add(em.getAuthority().getDisplayName());
				}
			}
			sessionEform.setEntityMapList(entityMapList);
			sessionEform.setEntityMapAuthNameList(entityMapAuthNameList);
		}
		
		if(sessionEform.getRemovedMapList() == null) {
			sessionEform.setRemovedMapList(new ArrayList<EntityMap>());
		}

		return list();
	}

}

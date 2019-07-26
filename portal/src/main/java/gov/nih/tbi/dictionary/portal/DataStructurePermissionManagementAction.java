package gov.nih.tbi.dictionary.portal;

import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.portal.AbstractEntityPermissionManagementAction;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.service.WebServiceManager;
import gov.nih.tbi.dictionary.model.SessionDataStructure;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Data Structure implementation of the Permission Management Action
 */
public class DataStructurePermissionManagementAction extends AbstractEntityPermissionManagementAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5633778053998403828L;

	/******************************************************************************/

	@Autowired
	SessionDataStructure sessionDataStructure;

	@Autowired
	WebServiceManager webServiceManager;

	/******************************************************************************/

	@Override
	public Long getEntityId() {

		return sessionDataStructure.getDataStructure().getId();
	}

	@Override
	public EntityType getEntityType() {

		return EntityType.DATA_STRUCTURE;
	}

	@Override
	public List<EntityMap> getEntityMapList() {

		return sessionDataStructure.getEntityMapList();
	}

	@Override
	public List<EntityMap> getRemovedMapList() {

		return sessionDataStructure.getRemovedMapList();
	}

	@Override
	public String getActionName() {

		return "dataStructurePermissionAction";
	}

	@Override
	public List<String> getEntityMapAuthNameList() {
		return sessionDataStructure.getEntityMapAuthNameList();
	}

	/**
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 ****************************************************************************/

	@Override
	public String load() throws MalformedURLException, UnsupportedEncodingException {

		// If there is not entityMapList then try and retreive one with the web service call
		if (sessionDataStructure.getEntityMapList() == null) {
			List<EntityMap> entityMapList = null;
			List<String> entityMapAuthNameList = new ArrayList<String>();

			entityMapList =
					webServiceManager.listEntityAccessRestful(getAccount(), getEntityId(), EntityType.DATA_STRUCTURE);

			if (entityMapList != null) {
				for (EntityMap em : entityMapList) {
					entityMapAuthNameList.add(em.getAuthority().getDisplayName());
				}
			} else {  // If the entityList is still null after retrieval then make an empty one
				entityMapList = new ArrayList<EntityMap>();
			}

			sessionDataStructure.setEntityMapList(entityMapList);
			sessionDataStructure.setEntityMapAuthNameList(entityMapAuthNameList);
		}

		return list();
	}

}

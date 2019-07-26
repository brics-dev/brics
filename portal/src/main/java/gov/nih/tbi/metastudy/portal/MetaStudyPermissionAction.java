
package gov.nih.tbi.metastudy.portal;

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
import gov.nih.tbi.metastudy.model.SessionMetaStudy;

public class MetaStudyPermissionAction extends AbstractEntityPermissionManagementAction {

	private static final long serialVersionUID = -510709279463851481L;

	private static Logger logger = Logger.getLogger(MetaStudyPermissionAction.class);

	@Autowired
	SessionMetaStudy sessionMetaStudy;

	@Autowired
	AccountManager accountManager;

	@Override
	public Long getEntityId() {

		return sessionMetaStudy.getMetaStudy().getId();
	}

	@Override
	public EntityType getEntityType() {

		return EntityType.META_STUDY;
	}

	@Override
	public List<EntityMap> getEntityMapList() {

		return sessionMetaStudy.getEntityMapList();
	}

	@Override
	public List<EntityMap> getRemovedMapList() {

		return sessionMetaStudy.getRemovedMapList();
	}

	@Override
	public String getActionName() {

		return "metaStudyPermissionAction";
	}

	@Override
	public List<String> getEntityMapAuthNameList() {
		return sessionMetaStudy.getEntityMapAuthNameList();
	}


	// Init for editing permissions. Assumes meta study is already in session. Loads the current
	// permissions list.
	@Override
	public String load() throws MalformedURLException, UnsupportedEncodingException {

		if (sessionMetaStudy.getEntityMapList() == null) {
			List<EntityMap> entityMapList = null;
			List<String> entityMapAuthNameList = new ArrayList<String>();

			if (getEntityId() != null) {
				entityMapList = accountManager.listEntityAccess(getEntityId(), EntityType.META_STUDY);
				for (EntityMap em : entityMapList) {
					entityMapAuthNameList.add(em.getAuthority().getDisplayName());
				}
			} else {
				entityMapList = new ArrayList<EntityMap>();
			}

			sessionMetaStudy.setEntityMapList(entityMapList);
			sessionMetaStudy.setRemovedMapList(new ArrayList<EntityMap>());
			sessionMetaStudy.setEntityMapAuthNameList(entityMapAuthNameList);
		}

		return list();
	}

}

package gov.nih.tbi.account.service.impl;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.WebApplicationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.stereotype.Service;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.constants.ApplicationConstants;
import gov.nih.tbi.dao.QueryAccountDao;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.StudyResult;
import gov.nih.tbi.semantic.model.QueryPermissions.FormResultPermission;
import gov.nih.tbi.semantic.model.QueryPermissions.StudyDataset;
import gov.nih.tbi.semantic.model.QueryPermissions.StudyResultPermission;
import gov.nih.tbi.service.QueryAccountManager;
import gov.nih.tbi.service.impl.BaseManagerImpl;
import gov.nih.tbi.service.model.PermissionModel;
import gov.nih.tbi.util.QueryRestProviderUtils;
import gov.nih.tbi.ws.provider.RestQueryAccountProvider;

@Service
@Scope("singleton")
public class QueryAccountManagerImpl extends BaseManagerImpl implements QueryAccountManager, Serializable {

	private static final long serialVersionUID = 8433582169401773898L;

	@Autowired
	QueryAccountDao queryAccountDao;

	@Autowired
	RoleHierarchy roleHierarchy;

	@Autowired
	ApplicationConstants constants;

	/**
	 * {@inheritDoc}
	 */
	public Map<Long, String> getFormIdNameMap() {
		return queryAccountDao.getFormIdNameMap();
	}

	public void updateGraphAccount(PermissionModel permissionModel) {

		queryAccountDao.removeGraphAccount(permissionModel.getUserName());

		List<Long> datasetIds = new ArrayList<Long>();

		Map<String, FormResultPermission> formPermMap = permissionModel.getFormResultPermissions();

		if (formPermMap.size() > 0) {
			for (FormResultPermission formPermission : formPermMap.values()) {
				for (StudyDataset studyDataset : formPermission.getStudyDatasets()) {
					if (!datasetIds.contains(studyDataset.getDatasetId())) {
						datasetIds.add(studyDataset.getDatasetId());
					}
				}
			}
		}

		queryAccountDao.addGraphAccount(permissionModel.getUserName(), datasetIds);
	}

	/* PS-1699: Remove the private studies that non-admin users do not have access from study result list */
	public List<StudyResult> hidePrivateStudyToNonAdmin(List<StudyResult> studyResultList,
			PermissionModel permissionModel) {

		if (!permissionModel.isQueryAdmin() && !permissionModel.isSysAdmin()) {

			for (Iterator<StudyResult> it = studyResultList.iterator(); it.hasNext();) {
				StudyResult studyResult = it.next();

				StudyResultPermission srp = permissionModel.getStudyResultPermissions().get(studyResult.getUri());

				if (srp == null) {
					it.remove();
				} else {
					boolean isAvailable = false;

					for (FormResult form : studyResult.getForms()) {
						if (srp.getFormIds() != null && srp.getFormIds().contains(form.getId())) {
							isAvailable = true;
							break;
						}
					}

					if (!isAvailable) {
						it.remove();
					}
				}
			}
		}

		return studyResultList;
	}


	public boolean canEditSavedQuery(Long entityId, Account account) {

		// ROLE_ADMIN user has full access to all saved queries
		Set<AccountRole> roles = account.getAccountRoleList();
		if (roles != null && !roles.isEmpty()) {
			for (AccountRole role : roles) {
				if (role.getRoleType() == RoleType.ROLE_ADMIN && role.getIsActive()) {
					return true;
				}
			}
		}

		RestQueryAccountProvider accountProvider = new RestQueryAccountProvider(constants.getModulesAccountURL(),
				QueryRestProviderUtils.getProxyTicket(constants.getModulesAccountURL()));

		try {
			EntityMap emw = accountProvider.getSavedQueryEntityPermission(account, entityId,
					constants.getSavedQueryAccessWebServiceURL());

			if (emw != null && emw.getPermission().contains(PermissionType.WRITE)) {
				return true;
			}
		} catch (UnsupportedEncodingException | WebApplicationException e) {
			e.printStackTrace();
		}

		return false;
	}


	public boolean canDeleteSavedQuery(Long entityId, Account account) {

		// ROLE_ADMIN user has full access to all saved queries
		Set<AccountRole> roles = account.getAccountRoleList();
		if (roles != null && !roles.isEmpty()) {
			for (AccountRole role : roles) {
				if (role.getRoleType() == RoleType.ROLE_ADMIN && role.getIsActive()) {
					return true;
				}
			}
		}

		RestQueryAccountProvider accountProvider = new RestQueryAccountProvider(constants.getModulesAccountURL(),
				QueryRestProviderUtils.getProxyTicket(constants.getModulesAccountURL()));

		List<EntityMap> entityMapList = null;
		entityMapList = accountProvider.listEntityAccess(entityId, EntityType.SAVED_QUERY, account,
				constants.getListSavedQueryPermissionsWebServiceURL());

		if (entityMapList != null) {
			for (EntityMap em : entityMapList) {
				if (em.getAccount().equals(account)) {

					PermissionType pt = em.getPermission();
					if (pt != null && (pt == PermissionType.ADMIN || pt == PermissionType.OWNER)) {
						return true;
					}
				}
			}
		}

		return false;
	}
}

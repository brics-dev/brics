package gov.nih.tbi.api.query.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;
import gov.nih.tbi.api.query.data.entity.UserToken;
import gov.nih.tbi.api.query.data.repository.AccountRepository;
import gov.nih.tbi.api.query.data.repository.DatasetRepository;
import gov.nih.tbi.api.query.data.repository.EntityMapRepository;
import gov.nih.tbi.api.query.data.repository.PermissionGroupRepository;
import gov.nih.tbi.api.query.service.model.UserPermissionCache;
import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.pojo.authentication.AccountDetailService;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetDataStructure;
import gov.nih.tbi.semantic.model.DatasetRDF;
import gov.nih.tbi.semantic.model.QueryPermissions;
import gov.nih.tbi.semantic.model.QueryPermissions.FormResultPermission;
import gov.nih.tbi.semantic.model.QueryPermissions.StudyResultPermission;
import gov.nih.tbi.semantic.model.StudyRDF;
import gov.nih.tbi.service.QueryAccountManager;
import gov.nih.tbi.service.model.PermissionModel;

@Service
public class AccountServiceImpl implements AccountService {

	private static Logger logger = Logger.getLogger(AccountServiceImpl.class);

	@Autowired
	RoleHierarchy roleHierarchy;

	@Autowired
	UserPermissionCache userPermissionCache;

	@Autowired
	AccountRepository accountRepository;

	@Autowired
	EntityMapRepository entityMapRepository;

	@Autowired
	PermissionGroupRepository permissionGroupRepository;

	@Autowired
	DatasetRepository dDatasetRepository;

	@Autowired
	QueryAccountManager queryAccountManager;


	@Override
	@Transactional(readOnly = true)
	public Account findByUserName(String username) {
		return accountRepository.findByUserName(username).orElse(null);
	}

	@Override
	@Transactional(readOnly = true)
	public PermissionModel getPermissionModel(UserToken userToken) {
		if (UserPermissionCache.userPermissionExists(userToken)) {
			return UserPermissionCache.getUserPermissionModel(userToken);
		} else {
			PermissionModel permissionModel = this.buildPermissionModel(userToken);
			UserPermissionCache.addUserPermission(userToken, permissionModel);
			return permissionModel;
		}
	}


	private PermissionModel buildPermissionModel(UserToken userToken) {
		PermissionModel permissionModel = new PermissionModel();

		Account account = findByUserName(userToken.getUsername());
		permissionModel.setAccount(account);

		if (this.hasRole(account, RoleType.ROLE_ADMIN)) {
			permissionModel.setSysAdmin(true);
		}
		if (this.hasRole(account, RoleType.ROLE_QUERY_ADMIN)) {
			permissionModel.setQueryAdmin(true);
		}

		QueryPermissions queryPermissions = this.getQTPermissionsMap(account);

		for (StudyResultPermission srp : queryPermissions.getStudyResultPermissions()) {
			permissionModel.addStudyResultPermission(srp);
		}

		Map<Long, String> formIdNameMap = queryAccountManager.getFormIdNameMap();

		for (FormResultPermission frp : queryPermissions.getFormResultPermissions()) {
			Long formId = frp.getFormId();
			String shortName = formIdNameMap.get(formId);
			permissionModel.addFormResultPermission(shortName, frp);
		}

		queryAccountManager.updateGraphAccount(permissionModel);

		return permissionModel;
	}


	public Boolean hasRole(Account account, RoleType role) {

		Collection<? extends GrantedAuthority> authorities =
				roleHierarchy.getReachableGrantedAuthorities(AccountDetailService.getAuthorities(account));

		if (authorities.contains(new SimpleGrantedAuthority(role.getName()))) {
			return true;
		}

		return false;
	}


	private QueryPermissions getQTPermissionsMap(Account account) {
		QueryPermissions queryPermissions = new QueryPermissions();

		List<Dataset> datasets = null;
		if (hasRole(account, RoleType.ROLE_ADMIN) || hasRole(account, RoleType.ROLE_QUERY_ADMIN)
				|| hasRole(account, RoleType.ROLE_STUDY_ADMIN)) {
			datasets = getAdminDatasetAccess(account);
		} else {
			datasets = getNonAdminDatasetAccess(account);
		}

		Set<Long> studyIds = new HashSet<Long>();
		for (Dataset ds : datasets) {
			Long studyId = ds.getStudy().getId();
			studyIds.add(studyId);
		}

		for (Dataset ds : datasets) {
			// If study is not null, has forms and is private or shared
			if (ds != null && ds.getStudy() != null && ds.getStudy().getTitle() != null
					&& !ds.getDatasetDataStructure().isEmpty()) {
				String studyURI = StudyRDF.createStudyResource(ds.getStudy()).getURI();
				String datasetURI = DatasetRDF.createDatasetResource(ds).getURI();

				// Get all forms for this dataset
				Set<Long> formIdList = new HashSet<Long>();
				for (DatasetDataStructure dsds : ds.getDatasetDataStructure()) {
					formIdList.add(dsds.getDataStructureId());

					// Add FormResultPermission to queryPermission
					QueryPermissions.FormResultPermission existingFormResultPermission =
							queryPermissions.getFormResultPermissionByFormId(dsds.getDataStructureId());

					// if list has form id
					if (existingFormResultPermission != null) {
						QueryPermissions.StudyDataset studyDataset = new QueryPermissions.StudyDataset();
						studyDataset.setDatasetId(ds.getId());
						studyDataset.setDatasetURI(datasetURI);
						studyDataset.setStudyURI(studyURI);
						existingFormResultPermission.getStudyDatasets().add(studyDataset);

					} else {
						QueryPermissions.StudyDataset studyDataset = new QueryPermissions.StudyDataset();
						studyDataset.setDatasetId(ds.getId());
						studyDataset.setDatasetURI(datasetURI);
						studyDataset.setStudyURI(studyURI);

						QueryPermissions.FormResultPermission formResultPermission =
								new QueryPermissions.FormResultPermission();
						formResultPermission.setFormId(dsds.getDataStructureId());
						formResultPermission.getStudyDatasets().add(studyDataset);
						queryPermissions.getFormResultPermissions().add(formResultPermission);
					}
				}

				// Add StudyResult to queryPermissions
				QueryPermissions.StudyResultPermission existingStudyResultPermission =
						queryPermissions.getStudyResultPermissionByStudyURI(studyURI);

				// If list already has the study add the form uri's
				if (studyIds.contains(ds.getStudy().getId())) {
					if (existingStudyResultPermission != null) {
						existingStudyResultPermission.getFormIds().addAll(formIdList);
					} else {
						QueryPermissions.StudyResultPermission studyResultPermission =
								new QueryPermissions.StudyResultPermission();
						studyResultPermission.setStudyURI(studyURI);
						studyResultPermission.setFormIds(formIdList);
						queryPermissions.getStudyResultPermissions().add(studyResultPermission);
					}
				}
			}
		}

		// Adds the Role string to the object
		populateAccountRoles(queryPermissions, account);

		return queryPermissions;
	}

	private void populateAccountRoles(QueryPermissions qtPermissions, Account account) {

		qtPermissions.setHasAccessToAccount(false);
		qtPermissions.setHasAccessToGUID(false);
		qtPermissions.setHasAccessToProforms(false);
		qtPermissions.setHasAccessToProforms(false);
		qtPermissions.setHasAccessToRepository(false);
		qtPermissions.setHasAccessToWorkspace(false);
		qtPermissions.setHasAccessToDictionary(false);
		qtPermissions.setHasAccessToQuery(false);
		qtPermissions.setHasAccessToMetaStudy(false);

		// if Admin give them all permissions
		if (hasRole(account, RoleType.ROLE_ADMIN)) {
			qtPermissions.setHasAccessToAccount(true);
			qtPermissions.setHasAccessToGUID(true);
			qtPermissions.setHasAccessToProforms(true);
			qtPermissions.setHasAccessToProforms(true);
			qtPermissions.setHasAccessToRepository(true);
			qtPermissions.setHasAccessToWorkspace(true);
			qtPermissions.setHasAccessToDictionary(true);
			qtPermissions.setHasAccessToQuery(true);
			qtPermissions.setHasAccessToMetaStudy(true);

		} else {
			if (hasRole(account, RoleType.ROLE_USER)) {
				qtPermissions.setHasAccessToWorkspace(true);
				qtPermissions.setHasAccessToAccount(true);
			}

			if (hasRole(account, RoleType.ROLE_PROFORMS) || hasRole(account, RoleType.ROLE_PROFORMS_ADMIN)) {
				qtPermissions.setHasAccessToProforms(true);
			}

			if (hasRole(account, RoleType.ROLE_GUID) || hasRole(account, RoleType.ROLE_GUID_ADMIN)) {
				qtPermissions.setHasAccessToGUID(true);
			}

			if (hasRole(account, RoleType.ROLE_DICTIONARY) || hasRole(account, RoleType.ROLE_DICTIONARY_ADMIN)) {
				qtPermissions.setHasAccessToDictionary(true);
			}

			if (hasRole(account, RoleType.ROLE_STUDY) || hasRole(account, RoleType.ROLE_STUDY_ADMIN)) {
				qtPermissions.setHasAccessToRepository(true);
			}

			if (hasRole(account, RoleType.ROLE_QUERY) || hasRole(account, RoleType.ROLE_QUERY_ADMIN)) {
				qtPermissions.setHasAccessToQuery(true);
			}

			if (hasRole(account, RoleType.ROLE_METASTUDY) || hasRole(account, RoleType.ROLE_METASTUDY_ADMIN)) {
				qtPermissions.setHasAccessToMetaStudy(true);
			}
		}
	}

	/**
	 * Given an admin account, return all the datasets that the admin user has access to
	 * 
	 * @param account
	 * @return
	 */
	private List<Dataset> getAdminDatasetAccess(Account account) {

		// admin user has access to all private and shared datasets in the query tool
		Set<DatasetStatus> privateAndSharedStatuses = new HashSet<DatasetStatus>();
		privateAndSharedStatuses.add(DatasetStatus.PRIVATE);
		privateAndSharedStatuses.add(DatasetStatus.SHARED);

		List<Dataset> datasets = dDatasetRepository.getByStatuses(privateAndSharedStatuses);

		return datasets;
	}


	/**
	 * Given a non-admin account, return all the datasets that the non admin user has access to
	 * 
	 * @param account
	 * @return
	 */
	private List<Dataset> getNonAdminDatasetAccess(Account account) {

		Set<Long> datasetIds = new HashSet<Long>(); // this will be the set of dataset Ids the user has access to

		// ---------------------------- GET LIST OF EXPLICITLY GRANTED STUDY ACCESS (Not through permission group)
		// -----------------------------
		List<EntityMap> grantedStudyEntityMaps = listUserAccessEntities(account, EntityType.STUDY, PermissionType.READ);
		Set<Long> studyIds = new HashSet<Long>();
		if (grantedStudyEntityMaps != null && !grantedStudyEntityMaps.isEmpty()) {
			for (EntityMap study : grantedStudyEntityMaps) {
				studyIds.add(study.getEntityId());
			}
		}
		// user with explicit study access should be able to access private and shared datasets
		Set<DatasetStatus> privateAndSharedStatuses = new HashSet<DatasetStatus>();
		privateAndSharedStatuses.add(DatasetStatus.PRIVATE);
		privateAndSharedStatuses.add(DatasetStatus.SHARED);

		// add all the dataset IDs from the studies the current user has access to
		datasetIds.addAll(dDatasetRepository.getDatasetIdsByStudyIds(studyIds, privateAndSharedStatuses));

		// ------------------------------ GET LIST OF ALL STUDIES THE USER HAS ACCESS TO (Includes public studies)
		// ---------------

		// get all the studies the user has access to, including ones in the public group
		List<EntityMap> allStudyEntityMaps = listUserAccess(account, EntityType.STUDY);
		if (allStudyEntityMaps != null && !allStudyEntityMaps.isEmpty()) {
			for (EntityMap study : allStudyEntityMaps) {
				studyIds.add(study.getEntityId());
			}
		}

		Set<DatasetStatus> sharedStatus = new HashSet<DatasetStatus>();

		// all users should be able to see shared datasets in public studies
		sharedStatus.add(DatasetStatus.SHARED);

		// add all the dataset IDs from the studies the current user has access to
		datasetIds.addAll(dDatasetRepository.getDatasetIdsByStudyIds(studyIds, sharedStatus));

		List<Dataset> datasets = dDatasetRepository.getByIds(datasetIds);
		return datasets;
	}

	private List<EntityMap> listUserAccessEntities(Account account, EntityType type, PermissionType permission) {

		List<EntityMap> entityMapList = null;

		Set<PermissionGroup> grantedPermissionGroups =
				new HashSet<PermissionGroup>(permissionGroupRepository.getGrantedGroups(account.getId()));
		entityMapList = entityMapRepository.listUserAccess(account, grantedPermissionGroups, type, false);

		logger.debug("number of returned results " + entityMapList.size());
		return entityMapList;

	}

	private List<EntityMap> listUserAccess(Account account, EntityType type) {

		Set<PermissionGroup> currentGroups = new HashSet<PermissionGroup>();
		currentGroups.addAll(permissionGroupRepository.getPublicGroups());
		currentGroups.addAll(permissionGroupRepository.getGrantedGroups(account.getId()));

		List<EntityMap> emList =
				entityMapRepository.listUserAccess(account, currentGroups, type, hasRole(account, type.getAdminRole()));
		return emList;
	}
}

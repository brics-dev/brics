package gov.nih.cit.brics.file.service;

import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import gov.nih.cit.brics.file.data.entity.UserToken;
import gov.nih.cit.brics.file.data.entity.dictionary.BasicDataElement;
import gov.nih.cit.brics.file.data.entity.dictionary.BasicFormStructure;
import gov.nih.cit.brics.file.data.entity.dictionary.MiniEform;
import gov.nih.cit.brics.file.data.repository.dictionary.BasicDataElementRepository;
import gov.nih.cit.brics.file.data.repository.dictionary.BasicFormStructureRepository;
import gov.nih.cit.brics.file.data.repository.dictionary.MiniEformRepository;
import gov.nih.cit.brics.file.data.repository.meta.BasicDataSetRepository;
import gov.nih.cit.brics.file.data.repository.meta.BiospecimenOrderRepository;
import gov.nih.cit.brics.file.data.repository.meta.DownloadPackageRepository;
import gov.nih.cit.brics.file.data.repository.meta.ElectronicSignatureRepository;
import gov.nih.cit.brics.file.data.repository.meta.EntityMapRepository;
import gov.nih.cit.brics.file.data.repository.meta.MetaStudyRepository;
import gov.nih.cit.brics.file.data.repository.meta.PermissionGroupRepository;
import gov.nih.cit.brics.file.data.repository.meta.StudyRepository;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.ElectronicSignature;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.file.model.hibernate.BricsFile;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;
import gov.nih.tbi.ordermanager.model.BiospecimenOrder;
import gov.nih.tbi.repository.model.hibernate.BasicDataset;
import gov.nih.tbi.repository.model.hibernate.DownloadPackage;
import gov.nih.tbi.repository.model.hibernate.Study;

@Service
@SessionScope
public class FilePermissionsService {
	private static final Logger logger = LoggerFactory.getLogger(FilePermissionsService.class);

	@Autowired
	private BasicDataSetRepository basicDataSetRepository;

	@Autowired
	private StudyRepository studyRepository;

	@Autowired
	private MetaStudyRepository metaStudyRepository;

	@Autowired
	private EntityMapRepository entityMapRepository;

	@Autowired
	private PermissionGroupRepository permissionGroupRepository;

	@Autowired
	private DownloadPackageRepository downloadPackageRepository;

	@Autowired
	private ElectronicSignatureRepository electronicSignatureRepository;

	@Autowired
	private BiospecimenOrderRepository biospecimenOrderRepository;

	@Autowired
	private MiniEformRepository basicEformRepository;

	@Autowired
	private BasicFormStructureRepository basicFormStructureRepository;

	@Autowired
	private BasicDataElementRepository basicDataElementRepository;

	/**
	 * Validates that the user, represented by the given Account and UserToken objects, can access the given data set
	 * file.
	 * 
	 * @param bricsFile - The BRICS file in question that will need to be tested for access.
	 * @param account - The account of the user who's access is being tested.
	 * @param userToken - A representation of the user's JWT. Provides easy access to the user's account roles.
	 * @return True if the user can access the data set file, or false if any validation tests fail.
	 */
	public boolean validateDataSetAccess(BricsFile bricsFile, Account account, UserToken userToken) {
		boolean hasRole = false;
		boolean hasPermission = false;

		logger.info("Validating data set permission for BricsFile : {} and user : {}...", bricsFile.getId(),
				account.getUserName());

		try {
			BasicDataset dataSet = basicDataSetRepository.findById(bricsFile.getLinkedObjectID()).get();
			Study study = studyRepository.findById(dataSet.getStudy().getId()).get();
			Set<RoleType> roleSet = getUserRoleSet(userToken);
			
			// Check if the user an admin role.
			if (roleSet.contains(RoleType.ROLE_ADMIN) || roleSet.contains(RoleType.ROLE_STUDY_ADMIN)
					|| roleSet.contains(RoleType.ROLE_REPOSITORY_ADMIN)
					|| roleSet.contains(RoleType.ROLE_QUERY_ADMIN)) {
				return true;
			}

			// Check if the user has the correct role to the data set.
			hasRole = roleSet.contains(RoleType.ROLE_STUDY) || roleSet.contains(RoleType.ROLE_QUERY);

			// Check if the user as the correct permission to the data set.
			if (hasRole) {
				if (study.getIsPublic() || account.getUser().equals(dataSet.getSubmitter())) {
					hasPermission = true;
				} else {
					hasPermission = canAccountAccessEntity(study.getId(), bricsFile.getFileCategory().getEntityType(),
							account, PermissionType.READ);
				}
			}
		} catch (NoSuchElementException e) {
			logger.error("Couldn't find data set or study associated with BricsFile : {} and user : {}.",
					bricsFile.getId(), account.getUserName());
			return false;
		}

		return hasRole && hasPermission;
	}

	/**
	 * Validates that the user, represented by the given Account and UserToken objects, can access the given download
	 * package file.
	 * 
	 * @param bricsFile - The BRICS file in question that will need to be tested for access.
	 * @param account - The account of the user who's access is being tested.
	 * @param userToken - A representation of the user's JWT. Provides easy access to the user's account roles.
	 * @return True if the user can access the download package file, or false if any validation tests fail.
	 */
	public boolean validateDownloadAccess(BricsFile bricsFile, Account account, UserToken userToken) {
		boolean hasRole = false;
		boolean hasPermission = false;

		logger.info("Validating download file permission for BricsFile : {} and user : {}...", bricsFile.getId(),
				account.getUserName());

		try {
			DownloadPackage downloadPackage = downloadPackageRepository.findById(bricsFile.getLinkedObjectID()).get();
			Set<RoleType> roleSet = getUserRoleSet(userToken);

			// Validate roles by download package origin.
			switch (downloadPackage.getOrigin()) {
				case ACCOUNT:
					// Check if the user has the system admin role.
					if (roleSet.contains(RoleType.ROLE_ADMIN)) {
						return true;
					}

					// Verify that the user has the regular user role.
					hasRole = roleSet.contains(RoleType.ROLE_USER);

					// Check if the download package is registered to the user.
					hasPermission = account.getUser().equals(downloadPackage.getUser());
					break;
				case DATASET:
					// Check if the user has an admin role for a data set.
					if (roleSet.contains(RoleType.ROLE_ADMIN) || roleSet.contains(RoleType.ROLE_STUDY_ADMIN)
							|| roleSet.contains(RoleType.ROLE_REPOSITORY_ADMIN)
							|| roleSet.contains(RoleType.ROLE_QUERY_ADMIN)) {
						return true;
					}

					// Check if the user has the correct roles for a data set.
					hasRole = roleSet.contains(RoleType.ROLE_STUDY) || roleSet.contains(RoleType.ROLE_QUERY);
					break;
				case QUERY_TOOL:
					// Check if the user has an admin role for the Query Tool.
					if (roleSet.contains(RoleType.ROLE_ADMIN) || roleSet.contains(RoleType.ROLE_QUERY_ADMIN)) {
						return true;
					}

					// Check if the user has the query role.
					hasRole = roleSet.contains(RoleType.ROLE_QUERY);
					break;
				default:
					logger.error("Unsupported origin ({}) encountered for download package : {}",
							downloadPackage.getOrigin(), downloadPackage.getId());
					return false;
			}

			// Check if the user has the right permissions to the download package.
			hasPermission = account.getUser().equals(downloadPackage.getUser());
		} catch (NoSuchElementException e) {
			logger.error("Couldn't find a download package associated with BricsFile : {} and user : {}",
					bricsFile.getId(), account.getUserName());
			return false;
		}

		return hasRole && hasPermission;
	}

	/**
	 * Validates that the user, represented by the given Account and UserToken objects, can access the given electronic
	 * signature file.
	 * 
	 * @param bricsFile - The BRICS file in question that will need to be tested for access.
	 * @param account - The account of the user who's access is being tested.
	 * @param userToken - A representation of the user's JWT. Provides easy access to the user's account roles.
	 * @return True if the user can access the electronic signature file, or false if any validation tests fail.
	 */
	public boolean validateElectronicSignatureAccess(BricsFile bricsFile, Account account, UserToken userToken) {
		boolean hasRole = false;
		boolean hasPermission = false;

		logger.info("Validating electronic signature file permission for BricsFile : {} and user : {}...",
				bricsFile.getId(), account.getUserName());

		try {
			ElectronicSignature eSign = electronicSignatureRepository.findById(bricsFile.getLinkedObjectID()).get();
			Set<RoleType> roleSet = getUserRoleSet(userToken);

			// Check if the user has the system admin role.
			if (roleSet.contains(RoleType.ROLE_ADMIN) || roleSet.contains(RoleType.ROLE_ACCOUNT_ADMIN)) {
				return true;
			}

			// Verify that the user has the regular user role.
			hasRole = roleSet.contains(RoleType.ROLE_USER);

			// Check if the user owns the electronic signature.
			hasPermission = account.equals(eSign.getAccount());
		} catch (NoSuchElementException e) {
			logger.error("Couldn't find a electronic signature associated with BricsFile : {} and user : {}",
					bricsFile.getId(), account.getUserName());
			return false;
		}

		return hasRole && hasPermission;
	}

	/**
	 * Validates that the user, represented by the given Account and UserToken objects, can access the given meta study
	 * file.
	 * 
	 * @param bricsFile - The BRICS file in question that will need to be tested for access.
	 * @param account - The account of the user who's access is being tested.
	 * @param userToken - A representation of the user's JWT. Provides easy access to the user's account roles.
	 * @return True if the user can access the meta study file, or false if any validation tests fail.
	 */
	public boolean validateMetaStudyAccess(BricsFile bricsFile, Account account, UserToken userToken) {
		boolean hasRole = false;
		boolean hasPermission = false;

		logger.info("Validating meta study file permission for BricsFile : {} and user : {}...", bricsFile.getId(),
				account.getUserName());

		try {
			MetaStudy metaStudy = metaStudyRepository.findById(bricsFile.getLinkedObjectID()).get();
			Set<RoleType> roleSet = getUserRoleSet(userToken);

			// Check if the user has an admin role.
			if (roleSet.contains(RoleType.ROLE_ADMIN) || roleSet.contains(RoleType.ROLE_METASTUDY_ADMIN)) {
				return true;
			}

			// Verify that the non-admin user has the meta study role.
			hasRole = roleSet.contains(RoleType.ROLE_METASTUDY);

			// Check if the user can access the meta study.
			if (hasRole) {
				// Check if the meta study is published.
				if (metaStudy.isPublished()) {
					hasPermission = true;
				} else {
					hasPermission = canAccountAccessEntity(metaStudy.getId(),
							bricsFile.getFileCategory().getEntityType(), account, PermissionType.READ);
				}
			}
		} catch (NoSuchElementException e) {
			logger.error("Couldn't find a meta study associated with BricsFile : {} and user : {}", bricsFile.getId(),
					account.getUserName());
			return false;
		}

		return hasRole && hasPermission;
	}

	/**
	 * Validates that the user, represented by the given Account and UserToken objects, can access the given order
	 * manager file.
	 * 
	 * @param bricsFile - The BRICS file in question that will need to be tested for access.
	 * @param account - The account of the user who's access is being tested.
	 * @param userToken - A representation of the user's JWT. Provides easy access to the user's account roles.
	 * @return True if the user can access the order manager file, or false if any validation tests fail.
	 */
	public boolean validateOrderManagerAccess(BricsFile bricsFile, Account account, UserToken userToken) {
		boolean hasRole = false;
		boolean hasPermission = false;

		logger.info("Validating order manager file permission for BricsFile : {} and user : {}...", bricsFile.getId(),
				account.getUserName());

		try {
			BiospecimenOrder order = biospecimenOrderRepository.findById(bricsFile.getLinkedObjectID()).get();
			Set<RoleType> roleSet = getUserRoleSet(userToken);

			// Check if the user has an admin role.
			if (roleSet.contains(RoleType.ROLE_ADMIN) || roleSet.contains(RoleType.ROLE_ORDER_ADMIN)) {
				return true;
			}

			// Verify if the user has the standard user role.
			hasRole = roleSet.contains(RoleType.ROLE_USER);

			// Check if the user can access the biospecimen order.
			User currentUser = account.getUser();
			hasPermission = currentUser.equals(order.getUser()) || currentUser.equals(order.getBracUser());
		} catch (NoSuchElementException e) {
			logger.error("Couldn't find a order manager associated with BricsFile : {} and user : {}",
					bricsFile.getId(), account.getUserName());
			return false;
		}

		return hasRole && hasPermission;
	}

	/**
	 * Validates that the user, represented by the given Account and UserToken objects, can access the given study.
	 * 
	 * @param bricsFile - The BRICS file in question that will need to be tested for access.
	 * @param account - The account of the user who's access is being tested.
	 * @param userToken - A representation of the user's JWT. Provides easy access to the user's account roles.
	 * @return True if the user can access the study, or false if any validation tests fail.
	 */
	public boolean validateStudyAccess(BricsFile bricsFile, Account account, UserToken userToken) {
		boolean hasRole = false;
		boolean hasPermission = false;

		logger.info("Validating study permission for BricsFile : {} and user : {}...", bricsFile.getId(),
				account.getUserName());

		try {
			Study study = studyRepository.findById(bricsFile.getLinkedObjectID()).get();
			Set<RoleType> roleSet = getUserRoleSet(userToken);

			// Check if the user has an admin role.
			if (roleSet.contains(RoleType.ROLE_ADMIN) || roleSet.contains(RoleType.ROLE_STUDY_ADMIN)
					|| roleSet.contains(RoleType.ROLE_REPOSITORY_ADMIN)) {
				return true;
			}

			// Verify if the user has the study role.
			hasRole = roleSet.contains(RoleType.ROLE_STUDY);

			// Check if the user can access the study.
			if (hasRole) {
				// Check if the study is public.
				if (study.getIsPublic()) {
					hasPermission = true;
				} else {
					hasPermission = canAccountAccessEntity(study.getId(), bricsFile.getFileCategory().getEntityType(),
							account, PermissionType.READ);
				}
			}
		} catch (NoSuchElementException e) {
			logger.error("Couldn't find a study associated with BricsFile : {} and user : {}", bricsFile.getId(),
					account.getUserName());
			return false;
		}

		return hasRole && hasPermission;
	}

	/**
	 * Validates that the user, represented by the given Account and UserToken objects, can access the given eForm.
	 * 
	 * @param bricsFile - The BRICS file in question that will need to be tested for access.
	 * @param account - The account of the user who's access is being tested.
	 * @param userToken - A representation of the user's JWT. Provides easy access to the user's account roles.
	 * @return True if the user can access the eForm, or false if any validation tests fail.
	 */
	public boolean validateEformAccess(BricsFile bricsFile, Account account, UserToken userToken) {
		boolean hasRole = false;
		boolean hasPermission = false;

		logger.info("Validating eForm permission for BricsFile : {} and user : {}...", bricsFile.getId(),
				account.getUserName());

		try {
			MiniEform eForm = basicEformRepository.findById(bricsFile.getLinkedObjectID()).get();
			Set<RoleType> roleSet = getUserRoleSet(userToken);
			
			// Check if the user has an admin role.
			if (roleSet.contains(RoleType.ROLE_ADMIN) || roleSet.contains(RoleType.ROLE_DICTIONARY_ADMIN)
					|| roleSet.contains(RoleType.ROLE_PROFORMS_ADMIN) || roleSet.contains(RoleType.ROLE_CLINICAL_ADMIN)
					|| roleSet.contains(RoleType.ROLE_QUERY_ADMIN)) {
				return true;
			}

			// Verify if the non-admin user has the correct roles.
			hasRole = roleSet.contains(RoleType.ROLE_DICTIONARY) || roleSet.contains(RoleType.ROLE_PROFORMS)
					|| roleSet.contains(RoleType.ROLE_QUERY) || roleSet.contains(RoleType.ROLE_DICTIONARY_EFORM);

			// Check if the user can access the eForm.
			if (hasRole) {
				if (eForm.getStatus() == StatusType.PUBLISHED) {
					hasPermission = true;
				} else {
					hasPermission = canAccountAccessEntity(eForm.getId(), bricsFile.getFileCategory().getEntityType(),
							account, PermissionType.READ);
				}
			}
		} catch (NoSuchElementException e) {
			logger.error("Couldn't find a eForm associated with BricsFile : {} and user : {}", bricsFile.getId(),
					account.getUserName());
			return false;
		}

		return hasRole && hasPermission;
	}

	/**
	 * Validates that the user, represented by the given Account and UserToken objects, can access the given form
	 * structure.
	 * 
	 * @param bricsFile - The BRICS file in question that will need to be tested for access.
	 * @param account - The account of the user who's access is being tested.
	 * @param userToken - A representation of the user's JWT. Provides easy access to the user's account roles.
	 * @return True if the user can access the form structure, or false if any validation tests fail.
	 */
	public boolean validateFormStructureAccess(BricsFile bricsFile, Account account, UserToken userToken) {
		boolean hasRole = false;
		boolean hasPermission = false;

		logger.info("Validating form structure permission for BricsFile : {} and user : {}...", bricsFile.getId(),
				account.getUserName());

		try {
			BasicFormStructure formStucture =
					basicFormStructureRepository.findById(bricsFile.getLinkedObjectID()).get();
			Set<RoleType> roleSet = getUserRoleSet(userToken);

			// Check if the user has an admin role.
			if (roleSet.contains(RoleType.ROLE_ADMIN) || roleSet.contains(RoleType.ROLE_DICTIONARY_ADMIN)
					|| roleSet.contains(RoleType.ROLE_PROFORMS_ADMIN) || roleSet.contains(RoleType.ROLE_CLINICAL_ADMIN)
					|| roleSet.contains(RoleType.ROLE_QUERY_ADMIN)) {
				return true;
			}

			// Verify if the non-admin user has the correct roles.
			hasRole = roleSet.contains(RoleType.ROLE_DICTIONARY) || roleSet.contains(RoleType.ROLE_PROFORMS)
					|| roleSet.contains(RoleType.ROLE_QUERY) || roleSet.contains(RoleType.ROLE_DICTIONARY_EFORM);

			// Check if the user can access the form structure.
			if (hasRole) {
				if (formStucture.getStatus() == StatusType.PUBLISHED) {
					hasPermission = true;
				} else {
					hasPermission = canAccountAccessEntity(formStucture.getId(),
							bricsFile.getFileCategory().getEntityType(), account, PermissionType.READ);
				}
			}
		} catch (NoSuchElementException e) {
			logger.error("Couldn't find a form structure associated with BricsFile : {} and user : {}",
					bricsFile.getId(), account.getUserName());
			return false;
		}

		return hasRole && hasPermission;
	}

	/**
	 * Validates that the user, represented by the given Account and UserToken objects, can access the given data
	 * element.
	 * 
	 * @param bricsFile - The BRICS file in question that will need to be tested for access.
	 * @param account - The account of the user who's access is being tested.
	 * @param userToken - A representation of the user's JWT. Provides easy access to the user's account roles.
	 * @return True if the user can access the data element, or false if any validation tests fail.
	 */
	public boolean validateDataElementAccess(BricsFile bricsFile, Account account, UserToken userToken) {
		boolean hasRole = false;
		boolean hasPermission = false;

		logger.info("Validating data element permission for BricsFile : {} and user : {}...", bricsFile.getId(),
				account.getUserName());

		try {
			BasicDataElement dataElement = basicDataElementRepository.findById(bricsFile.getLinkedObjectID()).get();
			Set<RoleType> roleSet = getUserRoleSet(userToken);

			// Check if the user has an admin role.
			if (roleSet.contains(RoleType.ROLE_ADMIN) || roleSet.contains(RoleType.ROLE_DICTIONARY_ADMIN)
					|| roleSet.contains(RoleType.ROLE_PROFORMS_ADMIN) || roleSet.contains(RoleType.ROLE_CLINICAL_ADMIN)
					|| roleSet.contains(RoleType.ROLE_QUERY_ADMIN)) {
				return true;
			}

			// Verify if the non-admin user has the correct roles.
			hasRole = roleSet.contains(RoleType.ROLE_DICTIONARY) || roleSet.contains(RoleType.ROLE_PROFORMS)
					|| roleSet.contains(RoleType.ROLE_QUERY) || roleSet.contains(RoleType.ROLE_DICTIONARY_EFORM);

			// Check if the user can access the data element.
			if (hasRole) {
				if (dataElement.getStatus() == DataElementStatus.PUBLISHED) {
					hasPermission = true;
				} else {
					hasPermission = canAccountAccessEntity(dataElement.getId(),
							bricsFile.getFileCategory().getEntityType(), account, PermissionType.READ);
				}
			}
		} catch (NoSuchElementException e) {
			logger.error("Couldn't find a data element associated with BricsFile : {} and user : {}",
					bricsFile.getId(), account.getUserName());
			return false;
		}

		return hasRole && hasPermission;
	}

	/**
	 * Verifies that the given Account object can access a specific entity (represented by the passed in entity ID), and
	 * can be accessible by the given permission. Basically answers the question, can the given account access a given
	 * entity by the specified access permission.
	 * 
	 * @param entityId - The ID of the entity (study ID, data set ID, etc.) of the entity whos access is being requested
	 *        for.
	 * @param entityType - The entity type of the associated entity ID.
	 * @param account - The account of the user requesting access.
	 * @param requestedPermission - The requested permission from the user.
	 * @return True if the given Account as the given permission over the referenced entity, otherwise it will be false.
	 */
	public boolean canAccountAccessEntity(Long entityId, EntityType entityType, Account account,
			PermissionType requestedPermission) {
		Set<PermissionGroup> permissionGroups =
				new HashSet<>(permissionGroupRepository.findAllByMemberSet_Account(account));

		for (EntityMap em : entityMapRepository.findAllByEntityIdAndType(entityId, entityType)) {
			if ((account.equals(em.getAccount()) || permissionGroups.contains(em.getPermissionGroup()))
					&& hasPermissionTo(requestedPermission, em.getPermission())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * A helper method that will decide if the requested permission type is valid against permission of the entity.
	 * 
	 * @param requestedPermission - The requested permission for the given entityMap.
	 * @param entityPermission - The entity's permission type to be compared with the passed in requested permission
	 *        type.
	 * @return True if it is determined that the requested permission is valid or accessible with the given entity
	 *         permission, or false otherwise.
	 */
	private boolean hasPermissionTo(PermissionType requestedPermission, PermissionType entityPermission) {
		boolean hasPermission = false;

		switch (requestedPermission) {
			case READ:
				hasPermission = entityPermission != null;
				break;
			case ADMIN:
				hasPermission =
						(entityPermission == PermissionType.ADMIN) || (entityPermission == PermissionType.OWNER);
				break;
			case OWNER:
				hasPermission = entityPermission == PermissionType.OWNER;
				break;
			case WRITE:
				hasPermission = (entityPermission == PermissionType.WRITE) || (entityPermission == PermissionType.ADMIN)
						|| (entityPermission == PermissionType.OWNER);
				break;
			default:
				break;
		}

		return hasPermission;
	}

	/**
	 * Converts the listing of Authorities objects from the passed in UserToken object to a set of RoleType objects.
	 * 
	 * @param userToken - Contains the authorities listing that will be used in the RoleType conversion.
	 * @return A set of RoleType objects that correspond to the authorities in the given UserToken object.
	 */
	public Set<RoleType> getUserRoleSet(UserToken userToken) {
		Set<RoleType> roleSet = new HashSet<>();

		for (GrantedAuthority ga : userToken.getAuthorities()) {
			roleSet.add(RoleType.getByName(ga.getAuthority()));
		}

		return roleSet;
	}
}

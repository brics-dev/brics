package gov.nih.brics.file;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import gov.nih.cit.brics.file.data.entity.UserToken;
import gov.nih.cit.brics.file.data.entity.dictionary.BasicDataElement;
import gov.nih.cit.brics.file.data.entity.dictionary.BasicFormStructure;
import gov.nih.cit.brics.file.data.entity.dictionary.MiniEform;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.account.model.hibernate.ElectronicSignature;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;
import gov.nih.tbi.account.model.hibernate.PermissionGroupMember;
import gov.nih.tbi.commons.model.AccountStatus;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.MetaStudyStatus;
import gov.nih.tbi.commons.model.PermissionGroupStatus;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.model.StudyStatus;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;
import gov.nih.tbi.ordermanager.model.BiospecimenOrder;
import gov.nih.tbi.repository.model.DownloadPackageOrigin;
import gov.nih.tbi.repository.model.hibernate.BasicDataset;
import gov.nih.tbi.repository.model.hibernate.DownloadPackage;
import gov.nih.tbi.repository.model.hibernate.Study;

public class BaseFileTesting {

	/*
	 * @formatter:off
	 * ########################################################################
	 * # Test Object Generation Methods
	 * ########################################################################
	 * @formatter:on
	 */

	protected UserToken genUserToken(Long id, Account account, RoleType[] roleTypes) {
		UserToken token = new UserToken();

		token.setId(id);
		token.setUsername(account.getUserName());
		token.setFullName(account.getUser().getFullName());
		token.setOrgId(0L);
		token.setTokenExpiration(LocalDateTime.now().plus(30L, ChronoUnit.MINUTES));
		token.setAuthorities(Arrays.asList(roleTypes).stream().map((r) -> new SimpleGrantedAuthority(r.getName()))
				.collect(Collectors.toList()));

		return token;
	}

	protected User genUser(Long id, String firstName, String lastName, String email) {
		User user = new User();

		user.setId(id);
		user.setFirstName(firstName);
		user.setLastName(lastName);
		user.setEmail(email);

		return user;
	}

	protected Account genAccount(Long id, String userName, User user, RoleType[] roleTypes,
			PermissionGroup permissionGroup) {
		Account account = new Account();

		account.setId(id);
		account.setUserName(userName);
		account.setUser(user);
		account.setPassword("TEST-user-PASSW0RD".getBytes());
		account.setPasswordExpirationDate(Date.from(Instant.now().plus(365L, ChronoUnit.DAYS)));
		account.setAccountStatus(AccountStatus.ACTIVE);
		account.setIsActive(Boolean.TRUE);
		account.setIsLocked(Boolean.FALSE);

		// Set the account roles.
		Set<AccountRole> roleSet = new HashSet<>();

		for (int i = 0; i < roleTypes.length; i++) {
			AccountRole accountRole = new AccountRole();

			accountRole.setId(Long.valueOf(i + 1));
			accountRole.setAccount(account);
			accountRole.setRoleType(roleTypes[i]);
			accountRole.setRoleStatus(RoleStatus.ACTIVE);
			accountRole.setExpirationDate(Date.from(Instant.now().plus(365L, ChronoUnit.DAYS)));

			roleSet.add(accountRole);
		}

		account.setAccountRoleList(roleSet);

		// Set the permission group.
		if (permissionGroup != null) {
			PermissionGroupMember pgm = new PermissionGroupMember();

			pgm.setId(1L);
			pgm.setAccount(account);
			pgm.setPermissionGroup(permissionGroup);
			pgm.setPermissionGroupStatus(PermissionGroupStatus.ACTIVE);
			pgm.setExpirationDate(Date.from(Instant.now().plus(365L, ChronoUnit.DAYS)));

			account.setPermissionGroupMemberList(new HashSet<PermissionGroupMember>(Arrays.asList(pgm)));
		} else {
			account.setPermissionGroupMemberList(new HashSet<PermissionGroupMember>());
		}

		return account;
	}

	protected PermissionGroup genPermissionGroup(Long id, String groupName) {
		PermissionGroup permissionGroup = new PermissionGroup();

		permissionGroup.setId(id);
		permissionGroup.setGroupName(groupName);

		return permissionGroup;
	}

	protected EntityMap genEntityMap(Long id, Long entityId, EntityType entityType, Account account,
			PermissionGroup permissionGroup, PermissionType permissionType) {
		EntityMap entityMap = new EntityMap();

		entityMap.setId(id);
		entityMap.setEntityId(entityId);
		entityMap.setType(entityType);
		entityMap.setAccount(account);
		entityMap.setPermissionGroup(permissionGroup);
		entityMap.setPermission(permissionType);

		return entityMap;
	}

	protected BasicDataset genBasicDataSet(Long id, String name, User submitter, Study study) {
		BasicDataset dataset = new BasicDataset();

		dataset.setId(id);
		dataset.setPrefixedId("TEST-DATASET000" + id);
		dataset.setName(name);
		dataset.setSubmitter(submitter);
		dataset.setStudy(study);

		return dataset;
	}

	protected Study genStudy(Long id, String title, StudyStatus studyStatus) {
		Study study = new Study();

		study.setId(id);
		study.setPrefixedId("TEST-STUDY000" + id);
		study.setTitle(title);
		study.setAbstractText("This is just a test.");
		study.setStudyStatus(studyStatus);

		return study;
	}

	protected DownloadPackage genDownloadPackage(Long id, DownloadPackageOrigin origin, User owner) {
		DownloadPackage download = new DownloadPackage();

		download.setId(id);
		download.setName("Test Download Package " + id);
		download.setDateAdded(new Date());
		download.setDownloadables(Collections.emptySet());
		download.setUser(owner);
		download.setOrigin(origin);

		return download;
	}

	protected ElectronicSignature genElectronicSignature(Long id, Account account) {
		ElectronicSignature esig = new ElectronicSignature();
		User user = account.getUser();

		esig.setId(id);
		esig.setAccount(account);
		esig.setFirstName(user.getFirstName());
		esig.setMiddleName(user.getMiddleName());
		esig.setFirstName(user.getLastName());
		esig.setSignatureDate(Date.from(Instant.now().minus(30L, ChronoUnit.DAYS)));

		return esig;
	}

	protected MetaStudy genMetaStudy(Long id, String title, MetaStudyStatus status) {
		MetaStudy metaStudy = new MetaStudy();

		metaStudy.setId(id);
		metaStudy.setTitle(title);
		metaStudy.setStatus(status);

		return metaStudy;
	}

	protected BiospecimenOrder genBiospecimenOrder(Long id, String title, User owner, User brac) {
		BiospecimenOrder order = new BiospecimenOrder();

		order.setId(id);
		order.setOrderTitle(title);
		order.setUser(owner);
		order.setBracUser(brac);

		return order;
	}

	protected MiniEform genMiniEform(Long id, String title, StatusType status) {
		MiniEform eform = new MiniEform();

		eform.setId(id);
		eform.setShortName("test_eform_" + id);
		eform.setTitle(title);
		eform.setStatus(status);

		return eform;
	}

	protected BasicFormStructure genBasicFormStructure(Long id, String title, StatusType status) {
		BasicFormStructure formStructure = new BasicFormStructure();

		formStructure.setId(id);
		formStructure.setTitle(title);
		formStructure.setShortName("test_formstuct_" + id);
		formStructure.setStatus(status);

		return formStructure;
	}

	protected BasicDataElement genBasicDataElement(Long id, String name, DataElementStatus status) {
		BasicDataElement dataElement = new BasicDataElement();

		dataElement.setId(id);
		dataElement.setVersion("1");
		dataElement.setName(name);
		dataElement.setStatus(status);
		dataElement.setDateCreated(LocalDateTime.now());

		return dataElement;
	}
}

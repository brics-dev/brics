package gov.nih.brics.file.service;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import gov.nih.brics.file.BaseFileTesting;
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
import gov.nih.cit.brics.file.service.FilePermissionsService;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.ElectronicSignature;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.account.model.hibernate.PermissionGroup;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.MetaStudyStatus;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.model.StudyStatus;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.file.model.SystemFileCategory;
import gov.nih.tbi.file.model.hibernate.BricsFile;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudy;
import gov.nih.tbi.ordermanager.model.BiospecimenOrder;
import gov.nih.tbi.repository.model.DownloadPackageOrigin;
import gov.nih.tbi.repository.model.hibernate.BasicDataset;
import gov.nih.tbi.repository.model.hibernate.DownloadPackage;
import gov.nih.tbi.repository.model.hibernate.Study;

public class FilePermissionsServiceTest extends BaseFileTesting {

	@Mock
	private BasicDataSetRepository basicDataSetRepository = mock(BasicDataSetRepository.class);

	@Mock
	private StudyRepository studyRepository = mock(StudyRepository.class);

	@Mock
	private MetaStudyRepository metaStudyRepository = mock(MetaStudyRepository.class);

	@Mock
	private EntityMapRepository entityMapRepository = mock(EntityMapRepository.class);

	@Mock
	private PermissionGroupRepository permissionGroupRepository = mock(PermissionGroupRepository.class);

	@Mock
	private DownloadPackageRepository downloadPackageRepository = mock(DownloadPackageRepository.class);

	@Mock
	private ElectronicSignatureRepository electronicSignatureRepository = mock(ElectronicSignatureRepository.class);

	@Mock
	private BiospecimenOrderRepository biospecimenOrderRepository = mock(BiospecimenOrderRepository.class);

	@Mock
	private MiniEformRepository basicEformRepository = mock(MiniEformRepository.class);

	@Mock
	private BasicFormStructureRepository basicFormStructureRepository = mock(BasicFormStructureRepository.class);

	@Mock
	private BasicDataElementRepository basicDataElementRepository = mock(BasicDataElementRepository.class);

	@InjectMocks
	private FilePermissionsService filePermissionsService;

	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	/*
	 * @formatter:off
	 * ########################################################################
	 * # Data Set Access Tests
	 * ########################################################################
	 * @formatter:on
	 */

	@Test
	public void testDataSetAccessAsSysAdmin() {
		// Create test objects.
		RoleType[] adminRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_ADMIN};
		RoleType[] submitterRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_STUDY};
		User user = genUser(1L, "Supreme", "Overload", "god@brics.nih.gov");
		User submitter = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Study study = genStudy(1L, "Test Study 1", StudyStatus.PRIVATE);
		BasicDataset dataset = genBasicDataSet(1L, "Test Dataset 1", submitter, study);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.csv", SystemFileCategory.DATASET, dataset.getId());
		PermissionGroup permissionGroup = genPermissionGroup(1L, "Test Permission Group 1");
		Account account = genAccount(1L, "admin", user, adminRoles, null);
		Account submitterAccount = genAccount(2L, "rOne", submitter, submitterRoles, permissionGroup);
		UserToken userToken = genUserToken(1L, account, adminRoles);
		EntityMap entityMap =
				genEntityMap(1L, dataset.getId(), EntityType.DATASET, submitterAccount, null, PermissionType.OWNER);

		// Stub the repository method calls.
		when(basicDataSetRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(dataset));
		when(studyRepository.findById(study.getId())).thenReturn(Optional.of(study));
		when(permissionGroupRepository.findAllByMemberSet_Account(account)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(dataset.getId(), EntityType.DATASET))
				.thenReturn(Arrays.asList(entityMap));

		// Test data set access for the system admin user.
		Assert.assertTrue(filePermissionsService.validateDataSetAccess(bricsFile, account, userToken),
				"The admin user should access any data set.");
	}

	@Test
	public void testDataSetAccessAsStudyAdmin() {
		// Create test objects.
		RoleType[] adminRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_STUDY, RoleType.ROLE_STUDY_ADMIN};
		RoleType[] submitterRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_STUDY};
		User user = genUser(1L, "Testing", "User", "test.user@nih.gov");
		User submitter = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Study study = genStudy(1L, "Test Study 1", StudyStatus.PRIVATE);
		BasicDataset dataset = genBasicDataSet(1L, "Test Dataset 1", submitter, study);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.csv", SystemFileCategory.DATASET, dataset.getId());
		PermissionGroup permissionGroup = genPermissionGroup(1L, "Test Permission Group 1");
		Account account = genAccount(1L, "testUser", user, adminRoles, null);
		Account submitterAccount = genAccount(2L, "rOne", submitter, submitterRoles, permissionGroup);
		UserToken userToken = genUserToken(1L, account, adminRoles);
		EntityMap entityMap =
				genEntityMap(1L, dataset.getId(), EntityType.DATASET, submitterAccount, null, PermissionType.OWNER);

		// Stub the repository method calls.
		when(basicDataSetRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(dataset));
		when(studyRepository.findById(study.getId())).thenReturn(Optional.of(study));
		when(permissionGroupRepository.findAllByMemberSet_Account(account)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(dataset.getId(), EntityType.DATASET))
				.thenReturn(Arrays.asList(entityMap));

		// Test data set access for a user who is a study admin.
		Assert.assertTrue(filePermissionsService.validateDataSetAccess(bricsFile, account, userToken),
				"The study admin user should have access to any data set.");
	}

	@Test
	public void testDataSetAccessAsQueryAdmin() {
		// Create test objects.
		RoleType[] adminRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_QUERY, RoleType.ROLE_QUERY_ADMIN};
		RoleType[] submitterRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_STUDY};
		User user = genUser(1L, "Testing", "User", "test.user@nih.gov");
		User submitter = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Study study = genStudy(1L, "Test Study 1", StudyStatus.PRIVATE);
		BasicDataset dataset = genBasicDataSet(1L, "Test Dataset 1", submitter, study);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.csv", SystemFileCategory.DATASET, dataset.getId());
		PermissionGroup permissionGroup = genPermissionGroup(1L, "Test Permission Group 1");
		Account account = genAccount(1L, "testUser", user, adminRoles, null);
		Account submitterAccount = genAccount(2L, "rOne", submitter, submitterRoles, permissionGroup);
		UserToken userToken = genUserToken(1L, account, adminRoles);
		EntityMap entityMap =
				genEntityMap(1L, dataset.getId(), EntityType.DATASET, submitterAccount, null, PermissionType.OWNER);

		// Stub the repository method calls.
		when(basicDataSetRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(dataset));
		when(studyRepository.findById(study.getId())).thenReturn(Optional.of(study));
		when(permissionGroupRepository.findAllByMemberSet_Account(account)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(dataset.getId(), EntityType.DATASET))
				.thenReturn(Arrays.asList(entityMap));

		// Test data set access for a user who is a study admin.
		Assert.assertTrue(filePermissionsService.validateDataSetAccess(bricsFile, account, userToken),
				"The query admin user should have access to any data set.");
	}

	@Test
	public void testDataSetAccessAsRegularStudyUser() {
		// Create test objects.
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_STUDY};
		User user = genUser(1L, "Testing", "User", "test.user@nih.gov");
		User submitter = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Study study = genStudy(1L, "Test Study 1", StudyStatus.PRIVATE);
		BasicDataset dataset = genBasicDataSet(1L, "Test Dataset 1", submitter, study);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.csv", SystemFileCategory.DATASET, dataset.getId());
		PermissionGroup permissionGroup = genPermissionGroup(1L, "Test Permission Group 1");
		Account account = genAccount(1L, "testUser", user, roleTypes, null);
		Account submitterAccount = genAccount(2L, "rOne", submitter, roleTypes, permissionGroup);
		UserToken userToken = genUserToken(1L, account, roleTypes);
		EntityMap entityMap =
				genEntityMap(1L, dataset.getId(), EntityType.DATASET, submitterAccount, null, PermissionType.OWNER);
		EntityMap entityMap2 =
				genEntityMap(2L, dataset.getId(), EntityType.DATASET, account, null, PermissionType.READ);

		// Stub the repository method calls.
		when(basicDataSetRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(dataset));
		when(studyRepository.findById(study.getId())).thenReturn(Optional.of(study));
		when(permissionGroupRepository.findAllByMemberSet_Account(account)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(dataset.getId(), EntityType.DATASET))
				.thenReturn(Arrays.asList(entityMap, entityMap2));

		// Test data set access for a regular user with the study role, and read permission to the target data set.
		Assert.assertTrue(filePermissionsService.validateDataSetAccess(bricsFile, account, userToken),
				"The study user should have access to the data set.");
	}

	@Test
	public void testDataSetAccessAsRegularQueryUser() {
		// Create test objects.
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_QUERY};
		User user = genUser(1L, "Testing", "User", "test.user@nih.gov");
		User submitter = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Study study = genStudy(1L, "Test Study 1", StudyStatus.PRIVATE);
		BasicDataset dataset = genBasicDataSet(1L, "Test Dataset 1", submitter, study);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.csv", SystemFileCategory.DATASET, dataset.getId());
		PermissionGroup permissionGroup = genPermissionGroup(1L, "Test Permission Group 1");
		Account account = genAccount(1L, "testUser", user, roleTypes, null);
		Account submitterAccount = genAccount(2L, "rOne", submitter, roleTypes, permissionGroup);
		UserToken userToken = genUserToken(1L, account, roleTypes);
		EntityMap entityMap =
				genEntityMap(1L, dataset.getId(), EntityType.DATASET, submitterAccount, null, PermissionType.OWNER);
		EntityMap entityMap2 =
				genEntityMap(2L, dataset.getId(), EntityType.DATASET, account, null, PermissionType.READ);

		// Stub the repository method calls.
		when(basicDataSetRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(dataset));
		when(studyRepository.findById(study.getId())).thenReturn(Optional.of(study));
		when(permissionGroupRepository.findAllByMemberSet_Account(account)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(dataset.getId(), EntityType.DATASET))
				.thenReturn(Arrays.asList(entityMap, entityMap2));

		// Test data set access for a regular user with the query role, and read permission to the target data set.
		Assert.assertTrue(filePermissionsService.validateDataSetAccess(bricsFile, account, userToken),
				"The query user should have access to the data set.");
	}

	@Test
	public void testDataSetAccessAsRegularStudyUserWithPermissionGroupOnly() {
		// Create test objects.
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_STUDY};
		User user = genUser(1L, "Testing", "User", "test.user@nih.gov");
		User submitter = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Study study = genStudy(1L, "Test Study 1", StudyStatus.PRIVATE);
		BasicDataset dataset = genBasicDataSet(1L, "Test Dataset 1", submitter, study);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.csv", SystemFileCategory.DATASET, dataset.getId());
		PermissionGroup permissionGroup = genPermissionGroup(1L, "Test Permission Group 1");
		Account account = genAccount(1L, "testUser", user, roleTypes, permissionGroup);
		Account submitterAccount = genAccount(2L, "rOne", submitter, roleTypes, permissionGroup);
		UserToken userToken = genUserToken(1L, account, roleTypes);
		EntityMap entityMap =
				genEntityMap(1L, dataset.getId(), EntityType.DATASET, submitterAccount, null, PermissionType.OWNER);
		EntityMap entityMap2 =
				genEntityMap(2L, dataset.getId(), EntityType.DATASET, null, permissionGroup, PermissionType.READ);

		// Stub the repository method calls.
		when(basicDataSetRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(dataset));
		when(studyRepository.findById(study.getId())).thenReturn(Optional.of(study));
		when(permissionGroupRepository.findAllByMemberSet_Account(account)).thenReturn(Arrays.asList(permissionGroup));
		when(entityMapRepository.findAllByEntityIdAndType(dataset.getId(), EntityType.DATASET))
				.thenReturn(Arrays.asList(entityMap, entityMap2));

		// Test data set access for a regular study user, and is part of a permission group that has read access to the
		// study.
		Assert.assertTrue(filePermissionsService.validateDataSetAccess(bricsFile, account, userToken),
				"The study user is suppose to be a member of the permissions group that can read the study.");
	}

	@Test
	public void testDataSetQueryAccessToPublicStudy() {
		// Create test objects.
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_QUERY};
		RoleType[] submitterRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_STUDY};
		User user = genUser(1L, "Testing", "User", "test.user@nih.gov");
		User submitter = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Study study = genStudy(1L, "Test Study 1", StudyStatus.PUBLIC);
		BasicDataset dataset = genBasicDataSet(1L, "Test Dataset 1", submitter, study);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.csv", SystemFileCategory.DATASET, dataset.getId());
		PermissionGroup permissionGroup = genPermissionGroup(1L, "Test Permission Group 1");
		Account account = genAccount(1L, "testUser", user, roleTypes, null);
		Account submitterAccount = genAccount(2L, "rOne", submitter, submitterRoles, permissionGroup);
		UserToken userToken = genUserToken(1L, account, roleTypes);
		EntityMap entityMap =
				genEntityMap(1L, dataset.getId(), EntityType.DATASET, submitterAccount, null, PermissionType.OWNER);

		// Stub the repository method calls.
		when(basicDataSetRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(dataset));
		when(studyRepository.findById(study.getId())).thenReturn(Optional.of(study));
		when(permissionGroupRepository.findAllByMemberSet_Account(account)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(dataset.getId(), EntityType.DATASET))
				.thenReturn(Arrays.asList(entityMap));

		// Test data set access for a user with only the query role, who is accessing a public study.
		Assert.assertTrue(filePermissionsService.validateDataSetAccess(bricsFile, account, userToken),
				"A user with only the query role should still be able to access a public study.");
	}

	@Test
	public void testDataSetQueryAccessToPrivateStudyWithPermission() {
		// Create test objects.
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_QUERY};
		RoleType[] submitterRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_STUDY};
		User user = genUser(1L, "Testing", "User", "test.user@nih.gov");
		User submitter = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Study study = genStudy(1L, "Test Study 1", StudyStatus.PRIVATE);
		BasicDataset dataset = genBasicDataSet(1L, "Test Dataset 1", submitter, study);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.csv", SystemFileCategory.DATASET, dataset.getId());
		PermissionGroup permissionGroup = genPermissionGroup(1L, "Test Permission Group 1");
		Account account = genAccount(1L, "testUser", user, roleTypes, null);
		Account submitterAccount = genAccount(2L, "rOne", submitter, submitterRoles, permissionGroup);
		UserToken userToken = genUserToken(1L, account, roleTypes);
		EntityMap entityMap =
				genEntityMap(1L, dataset.getId(), EntityType.DATASET, submitterAccount, null, PermissionType.OWNER);
		EntityMap entityMap2 =
				genEntityMap(2L, dataset.getId(), EntityType.DATASET, account, null, PermissionType.READ);

		// Stub the repository method calls.
		when(basicDataSetRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(dataset));
		when(studyRepository.findById(study.getId())).thenReturn(Optional.of(study));
		when(permissionGroupRepository.findAllByMemberSet_Account(account)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(dataset.getId(), EntityType.DATASET))
				.thenReturn(Arrays.asList(entityMap, entityMap2));

		// Test data set access for a user with only the query role, with explicit access to a private study.
		Assert.assertTrue(filePermissionsService.validateDataSetAccess(bricsFile, account, userToken),
				"The user with only the query role should have access to a private study who has the correct explicit permission to do so.");
	}

	@Test
	public void testDataSetAccessWithWrongRole() {
		// Create test objects.
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_GUID};
		RoleType[] submitterRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_STUDY};
		User user = genUser(1L, "Testing", "User", "test.user@nih.gov");
		User submitter = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Study study = genStudy(1L, "Test Study 1", StudyStatus.PRIVATE);
		BasicDataset dataset = genBasicDataSet(1L, "Test Dataset 1", submitter, study);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.csv", SystemFileCategory.DATASET, dataset.getId());
		PermissionGroup permissionGroup = genPermissionGroup(1L, "Test Permission Group 1");
		Account account = genAccount(1L, "testUser", user, roleTypes, null);
		Account submitterAccount = genAccount(2L, "rOne", submitter, submitterRoles, permissionGroup);
		UserToken userToken = genUserToken(1L, account, roleTypes);
		EntityMap entityMap =
				genEntityMap(1L, dataset.getId(), EntityType.DATASET, submitterAccount, null, PermissionType.OWNER);

		// Stub the repository method calls.
		when(basicDataSetRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(dataset));
		when(studyRepository.findById(study.getId())).thenReturn(Optional.of(study));
		when(permissionGroupRepository.findAllByMemberSet_Account(account)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(dataset.getId(), EntityType.DATASET))
				.thenReturn(Arrays.asList(entityMap));

		// Test data set access for a regular user without study role.
		Assert.assertFalse(filePermissionsService.validateDataSetAccess(bricsFile, account, userToken),
				"This user only has GUID access should not have access to files of a data set.");
	}

	@Test
	public void testDataSetAccessWithRightRoleWrongPermission() {
		// Create test objects.
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_STUDY};
		User user = genUser(1L, "Testing", "User", "test.user@nih.gov");
		User submitter = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Study study = genStudy(1L, "Test Study 1", StudyStatus.PRIVATE);
		BasicDataset dataset = genBasicDataSet(1L, "Test Dataset 1", submitter, study);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.csv", SystemFileCategory.DATASET, dataset.getId());
		PermissionGroup permissionGroup = genPermissionGroup(1L, "Test Permission Group 1");
		Account account = genAccount(1L, "testUser", user, roleTypes, null);
		Account submitterAccount = genAccount(2L, "rOne", submitter, roleTypes, permissionGroup);
		UserToken userToken = genUserToken(1L, account, roleTypes);
		EntityMap entityMap =
				genEntityMap(1L, dataset.getId(), EntityType.DATASET, submitterAccount, null, PermissionType.OWNER);

		// Stub the repository method calls.
		when(basicDataSetRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(dataset));
		when(studyRepository.findById(study.getId())).thenReturn(Optional.of(study));
		when(permissionGroupRepository.findAllByMemberSet_Account(account)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(dataset.getId(), EntityType.DATASET))
				.thenReturn(Arrays.asList(entityMap));

		// Test data set access for a regular user with the study role, but without permissions to the study.
		Assert.assertFalse(filePermissionsService.validateDataSetAccess(bricsFile, account, userToken),
				"While this user does have the study rule, this user doesn't have access to the study.");
	}

	/*
	 * @formatter:off
	 * ########################################################################
	 * # Download Package Access Tests
	 * ########################################################################
	 * @formatter:on
	 */

	@Test
	public void testDownloadDatasetAccessAsAdmin() {
		// Create test objects.
		RoleType[] adminRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_ADMIN};
		User user = genUser(1L, "Admin", "User", "admin@brics.nih.gov");
		User owner = genUser(2L, "Some", "User", "some.user@nih.gov");
		Account account = genAccount(1L, "admin", user, adminRoles, null);
		DownloadPackage download = genDownloadPackage(1L, DownloadPackageOrigin.DATASET, owner);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.csv", SystemFileCategory.DOWNLOAD, download.getId());
		UserToken userToken = genUserToken(1L, account, adminRoles);

		// Stub the repository method calls.
		when(downloadPackageRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(download));

		// Test data set download access for the system admin user.
		Assert.assertTrue(filePermissionsService.validateDownloadAccess(bricsFile, account, userToken),
				"The system admin user should have access to all download packages.");
	}

	@Test
	public void testDownloadQueryToolAsSysAdmin() {
		// Create test objects.
		RoleType[] adminRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_ADMIN};
		User user = genUser(1L, "Admin", "User", "admin@brics.nih.gov");
		User owner = genUser(2L, "Some", "User", "some.user@nih.gov");
		Account account = genAccount(1L, "admin", user, adminRoles, null);
		DownloadPackage download = genDownloadPackage(1L, DownloadPackageOrigin.QUERY_TOOL, owner);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.csv", SystemFileCategory.DOWNLOAD, download.getId());
		UserToken userToken = genUserToken(1L, account, adminRoles);

		// Stub the repository method calls.
		when(downloadPackageRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(download));

		// Test query tool download access for the system admin user.
		Assert.assertTrue(filePermissionsService.validateDownloadAccess(bricsFile, account, userToken),
				"The system admin user should have access to all download packages.");
	}

	@Test
	public void testDownloadDataSetAsStudyAdmin() {
		// Create test objects.
		RoleType[] adminRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_STUDY, RoleType.ROLE_STUDY_ADMIN};
		User user = genUser(1L, "Admin", "User", "admin@brics.nih.gov");
		User owner = genUser(2L, "Some", "User", "some.user@nih.gov");
		Account account = genAccount(1L, "admin", user, adminRoles, null);
		DownloadPackage download = genDownloadPackage(1L, DownloadPackageOrigin.DATASET, owner);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.csv", SystemFileCategory.DOWNLOAD, download.getId());
		UserToken userToken = genUserToken(1L, account, adminRoles);

		// Stub the repository method calls.
		when(downloadPackageRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(download));

		// Test data set download access for the study admin user.
		Assert.assertTrue(filePermissionsService.validateDownloadAccess(bricsFile, account, userToken),
				"The study admin user should have access to all data set download packages.");
	}

	@Test
	public void testDownloadDataSetAsQueryAdmin() {
		// Create test objects.
		RoleType[] adminRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_QUERY, RoleType.ROLE_QUERY_ADMIN};
		User user = genUser(1L, "Admin", "User", "admin@brics.nih.gov");
		User owner = genUser(2L, "Some", "User", "some.user@nih.gov");
		Account account = genAccount(1L, "admin", user, adminRoles, null);
		DownloadPackage download = genDownloadPackage(1L, DownloadPackageOrigin.DATASET, owner);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.csv", SystemFileCategory.DOWNLOAD, download.getId());
		UserToken userToken = genUserToken(1L, account, adminRoles);

		// Stub the repository method calls.
		when(downloadPackageRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(download));

		// Test data set download access for the query admin user.
		Assert.assertTrue(filePermissionsService.validateDownloadAccess(bricsFile, account, userToken),
				"The query admin user should have access to all download packages.");
	}

	@Test
	public void testDownloadQueryToolAsQueryAdmin() {
		// Create test objects.
		RoleType[] adminRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_QUERY, RoleType.ROLE_QUERY_ADMIN};
		User user = genUser(1L, "Admin", "User", "admin@brics.nih.gov");
		User owner = genUser(2L, "Some", "User", "some.user@nih.gov");
		Account account = genAccount(1L, "admin", user, adminRoles, null);
		DownloadPackage download = genDownloadPackage(1L, DownloadPackageOrigin.QUERY_TOOL, owner);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.csv", SystemFileCategory.DOWNLOAD, download.getId());
		UserToken userToken = genUserToken(1L, account, adminRoles);

		// Stub the repository method calls.
		when(downloadPackageRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(download));

		// Test query tool download access for the query admin user.
		Assert.assertTrue(filePermissionsService.validateDownloadAccess(bricsFile, account, userToken),
				"The query admin user should have access to all download packages.");
	}

	@Test
	public void testDownloadDataSetAsStandardStudyUser() {
		// Create test objects.
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_STUDY};
		User user = genUser(1L, "Testing", "User", "test.user@nih.gov");
		Account account = genAccount(1L, "testUser", user, roleTypes, null);
		DownloadPackage download = genDownloadPackage(1L, DownloadPackageOrigin.DATASET, user);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.csv", SystemFileCategory.DOWNLOAD, download.getId());
		UserToken userToken = genUserToken(1L, account, roleTypes);

		// Stub the repository method calls.
		when(downloadPackageRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(download));

		// Test data set download access for a standard study user.
		Assert.assertTrue(filePermissionsService.validateDownloadAccess(bricsFile, account, userToken),
				"The study user should have access to its own download package.");
	}

	@Test
	public void testDownloadDataSetAsStandardQueryUser() {
		// Create test objects.
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_QUERY};
		User user = genUser(1L, "Testing", "User", "test.user@nih.gov");
		Account account = genAccount(1L, "testUser", user, roleTypes, null);
		DownloadPackage download = genDownloadPackage(1L, DownloadPackageOrigin.DATASET, user);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.csv", SystemFileCategory.DOWNLOAD, download.getId());
		UserToken userToken = genUserToken(1L, account, roleTypes);

		// Stub the repository method calls.
		when(downloadPackageRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(download));

		// Test data set download access for a standard query tool user.
		Assert.assertTrue(filePermissionsService.validateDownloadAccess(bricsFile, account, userToken),
				"The query user should have access to its own download package.");
	}

	@Test
	public void testDownloadQueryToolAsStandardQueryUser() {
		// Create test objects.
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_QUERY};
		User user = genUser(1L, "Testing", "User", "test.user@nih.gov");
		Account account = genAccount(1L, "testUser", user, roleTypes, null);
		DownloadPackage download = genDownloadPackage(1L, DownloadPackageOrigin.QUERY_TOOL, user);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.csv", SystemFileCategory.DOWNLOAD, download.getId());
		UserToken userToken = genUserToken(1L, account, roleTypes);

		// Stub the repository method calls.
		when(downloadPackageRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(download));

		// Test query tool download access for a standard query tool user.
		Assert.assertTrue(filePermissionsService.validateDownloadAccess(bricsFile, account, userToken),
				"The query user should have access to its own download package.");
	}

	@Test
	public void testDownloadDataSetWithStudyUserNotOwner() {
		// Create test objects.
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_STUDY};
		User user = genUser(1L, "Testing", "User", "test.user@nih.gov");
		User owner = genUser(2L, "Some", "User", "some.user@nih.gov");
		Account account = genAccount(1L, "testUser", user, roleTypes, null);
		DownloadPackage download = genDownloadPackage(1L, DownloadPackageOrigin.DATASET, owner);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.csv", SystemFileCategory.DOWNLOAD, download.getId());
		UserToken userToken = genUserToken(1L, account, roleTypes);

		// Stub the repository method calls.
		when(downloadPackageRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(download));

		// Test data set download access for a standard study user who also doesn't own the package.
		Assert.assertFalse(filePermissionsService.validateDownloadAccess(bricsFile, account, userToken),
				"The study user shouldn't have access to another's download package.");
	}

	@Test
	public void testDownloadDataSetWithUserIncorrectRole() {
		// Create test objects.
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_GUID};
		User user = genUser(1L, "Testing", "User", "test.user@nih.gov");
		Account account = genAccount(1L, "testUser", user, roleTypes, null);
		DownloadPackage download = genDownloadPackage(1L, DownloadPackageOrigin.DATASET, user);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.csv", SystemFileCategory.DOWNLOAD, download.getId());
		UserToken userToken = genUserToken(1L, account, roleTypes);

		// Stub the repository method calls.
		when(downloadPackageRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(download));

		// Test data set download access for a standard user with an incorrect role.
		Assert.assertFalse(filePermissionsService.validateDownloadAccess(bricsFile, account, userToken),
				"The study user shouldn't have access to another's download package.");
	}

	@Test
	public void testDownloadQueryToolWithUserIncorrectRole() {
		// Create test objects.
		RoleType[] adminRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_STUDY, RoleType.ROLE_STUDY_ADMIN};
		User user = genUser(1L, "Admin", "User", "admin@brics.nih.gov");
		User owner = genUser(2L, "Some", "User", "some.user@nih.gov");
		Account account = genAccount(1L, "admin", user, adminRoles, null);
		DownloadPackage download = genDownloadPackage(1L, DownloadPackageOrigin.QUERY_TOOL, owner);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.csv", SystemFileCategory.DOWNLOAD, download.getId());
		UserToken userToken = genUserToken(1L, account, adminRoles);

		// Stub the repository method calls.
		when(downloadPackageRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(download));

		// Test query tool download access for the study admin user.
		Assert.assertFalse(filePermissionsService.validateDownloadAccess(bricsFile, account, userToken),
				"The study admin user shouldn't have access to a query tool download package.");
	}

	/*
	 * @formatter:off
	 * ########################################################################
	 * # eForm File Access Tests
	 * ########################################################################
	 * @formatter:on
	 */

	@Test
	public void testEformAccessAsSysAdmin() {
		// Create test objects.
		RoleType[] adminRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_ADMIN};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY_EFORM};
		User admin = genUser(1L, "Supreme", "Overload", "god@brics.nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account adminAccount = genAccount(1L, "admin", admin, adminRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		MiniEform eform = genMiniEform(1L, "Test eForm", StatusType.DRAFT);
		EntityMap entityMap =
				genEntityMap(1L, eform.getId(), EntityType.EFORM, ownerAccount, null, PermissionType.OWNER);
		BricsFile bricsFile =
				new BricsFile("UUID001", "image.png", SystemFileCategory.QUESTION_DOCUMENT, eform.getId());
		UserToken userToken = genUserToken(1L, adminAccount, adminRoles);

		// Stub the repository method calls.
		when(basicEformRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(eform));
		when(permissionGroupRepository.findAllByMemberSet_Account(adminAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(eform.getId(), EntityType.EFORM))
				.thenReturn(Arrays.asList(entityMap));

		// Test eForm file access for the system admin user.
		Assert.assertTrue(filePermissionsService.validateEformAccess(bricsFile, adminAccount, userToken),
				"The system admin user should have access to all eForm documents.");
	}

	@Test
	public void testEformAccessAsDictionaryAdmin() {
		// Create test objects.
		RoleType[] adminRoles =
				new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY, RoleType.ROLE_DICTIONARY_ADMIN};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY_EFORM};
		User admin = genUser(1L, "Dictionary", "Admin", "dictionary.admin@brics.nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account adminAccount = genAccount(1L, "admin", admin, adminRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		MiniEform eform = genMiniEform(1L, "Test eForm", StatusType.DRAFT);
		EntityMap entityMap =
				genEntityMap(1L, eform.getId(), EntityType.EFORM, ownerAccount, null, PermissionType.OWNER);
		BricsFile bricsFile =
				new BricsFile("UUID001", "image.png", SystemFileCategory.QUESTION_DOCUMENT, eform.getId());
		UserToken userToken = genUserToken(1L, adminAccount, adminRoles);

		// Stub the repository method calls.
		when(basicEformRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(eform));
		when(permissionGroupRepository.findAllByMemberSet_Account(adminAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(eform.getId(), EntityType.EFORM))
				.thenReturn(Arrays.asList(entityMap));

		// Test eForm file access for the dictionary admin user.
		Assert.assertTrue(filePermissionsService.validateEformAccess(bricsFile, adminAccount, userToken),
				"The dictionary admin user should have access to all eForm documents.");
	}

	@Test
	public void testEformAccessAsProformsAdmin() {
		// Create test objects.
		RoleType[] adminRoles =
				new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_PROFORMS, RoleType.ROLE_PROFORMS_ADMIN};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY_EFORM};
		User admin = genUser(1L, "Proforms", "Admin", "proforms.admin@brics.nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account adminAccount = genAccount(1L, "admin", admin, adminRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		MiniEform eform = genMiniEform(1L, "Test eForm", StatusType.DRAFT);
		EntityMap entityMap =
				genEntityMap(1L, eform.getId(), EntityType.EFORM, ownerAccount, null, PermissionType.OWNER);
		BricsFile bricsFile =
				new BricsFile("UUID001", "image.png", SystemFileCategory.QUESTION_DOCUMENT, eform.getId());
		UserToken userToken = genUserToken(1L, adminAccount, adminRoles);

		// Stub the repository method calls.
		when(basicEformRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(eform));
		when(permissionGroupRepository.findAllByMemberSet_Account(adminAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(eform.getId(), EntityType.EFORM))
				.thenReturn(Arrays.asList(entityMap));

		// Test eForm file access for the proforms admin user.
		Assert.assertTrue(filePermissionsService.validateEformAccess(bricsFile, adminAccount, userToken),
				"The proforms admin user should have access to all eForm documents.");
	}

	@Test
	public void testEformAccessAsClinicalAdmin() {
		// Create test objects.
		RoleType[] adminRoles =
				new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_PROFORMS, RoleType.ROLE_CLINICAL_ADMIN};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY_EFORM};
		User admin = genUser(1L, "Clinical", "Admin", "clinical.admin@brics.nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account adminAccount = genAccount(1L, "admin", admin, adminRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		MiniEform eform = genMiniEform(1L, "Test eForm", StatusType.DRAFT);
		EntityMap entityMap =
				genEntityMap(1L, eform.getId(), EntityType.EFORM, ownerAccount, null, PermissionType.OWNER);
		BricsFile bricsFile =
				new BricsFile("UUID001", "image.png", SystemFileCategory.QUESTION_DOCUMENT, eform.getId());
		UserToken userToken = genUserToken(1L, adminAccount, adminRoles);

		// Stub the repository method calls.
		when(basicEformRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(eform));
		when(permissionGroupRepository.findAllByMemberSet_Account(adminAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(eform.getId(), EntityType.EFORM))
				.thenReturn(Arrays.asList(entityMap));

		// Test eForm file access for the clinical admin user.
		Assert.assertTrue(filePermissionsService.validateEformAccess(bricsFile, adminAccount, userToken),
				"The clinical admin user should have access to all eForm documents.");
	}

	@Test
	public void testEformAccessAsQueryAdmin() {
		// Create test objects.
		RoleType[] adminRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_QUERY, RoleType.ROLE_QUERY_ADMIN};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY_EFORM};
		User admin = genUser(1L, "Query", "Admin", "query.admin@brics.nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account adminAccount = genAccount(1L, "admin", admin, adminRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		MiniEform eform = genMiniEform(1L, "Test eForm", StatusType.DRAFT);
		EntityMap entityMap =
				genEntityMap(1L, eform.getId(), EntityType.EFORM, ownerAccount, null, PermissionType.OWNER);
		BricsFile bricsFile =
				new BricsFile("UUID001", "image.png", SystemFileCategory.QUESTION_DOCUMENT, eform.getId());
		UserToken userToken = genUserToken(1L, adminAccount, adminRoles);

		// Stub the repository method calls.
		when(basicEformRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(eform));
		when(permissionGroupRepository.findAllByMemberSet_Account(adminAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(eform.getId(), EntityType.EFORM))
				.thenReturn(Arrays.asList(entityMap));

		// Test eForm file access for the query admin user.
		Assert.assertTrue(filePermissionsService.validateEformAccess(bricsFile, adminAccount, userToken),
				"The query admin user should have access to all eForm documents.");
	}

	@Test
	public void testEformAccessAsStandardDictionaryUser() {
		// Create test objects.
		RoleType[] userRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY_EFORM};
		User user = genUser(1L, "Some", "User", "some.user@nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account account = genAccount(1L, "admin", user, userRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		MiniEform eform = genMiniEform(1L, "Test eForm", StatusType.DRAFT);
		EntityMap entityMap =
				genEntityMap(1L, eform.getId(), EntityType.EFORM, ownerAccount, null, PermissionType.OWNER);
		EntityMap entityMap2 = genEntityMap(2L, eform.getId(), EntityType.EFORM, account, null, PermissionType.WRITE);
		BricsFile bricsFile =
				new BricsFile("UUID001", "image.png", SystemFileCategory.QUESTION_DOCUMENT, eform.getId());
		UserToken userToken = genUserToken(1L, account, userRoles);

		// Stub the repository method calls.
		when(basicEformRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(eform));
		when(permissionGroupRepository.findAllByMemberSet_Account(account)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(eform.getId(), EntityType.EFORM))
				.thenReturn(Arrays.asList(entityMap, entityMap2));

		// Test eForm file access for a regular dictionary user.
		Assert.assertTrue(filePermissionsService.validateEformAccess(bricsFile, account, userToken),
				"The dictionary user should have access to the eForm documents.");
	}

	@Test
	public void testEformAccessAsStandardEformUser() {
		// Create test objects.
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY_EFORM};
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		MiniEform eform = genMiniEform(1L, "Test eForm", StatusType.DRAFT);
		EntityMap entityMap =
				genEntityMap(1L, eform.getId(), EntityType.EFORM, ownerAccount, null, PermissionType.OWNER);
		BricsFile bricsFile =
				new BricsFile("UUID001", "image.png", SystemFileCategory.QUESTION_DOCUMENT, eform.getId());
		UserToken userToken = genUserToken(1L, ownerAccount, ownerRoles);

		// Stub the repository method calls.
		when(basicEformRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(eform));
		when(permissionGroupRepository.findAllByMemberSet_Account(ownerAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(eform.getId(), EntityType.EFORM))
				.thenReturn(Arrays.asList(entityMap));

		// Test eForm file access for a regular eForm user.
		Assert.assertTrue(filePermissionsService.validateEformAccess(bricsFile, ownerAccount, userToken),
				"The eForm user should have access to the eForm documents.");
	}

	@Test
	public void testEformAccessAsStandardProformsUser() {
		// Create test objects.
		RoleType[] userRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_PROFORMS};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY_EFORM};
		PermissionGroup permissionGroup = genPermissionGroup(1L, "Test Permission Group 1");
		User user = genUser(1L, "Some", "User", "some.user@nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account account = genAccount(1L, "admin", user, userRoles, permissionGroup);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		MiniEform eform = genMiniEform(1L, "Test eForm", StatusType.DRAFT);
		EntityMap entityMap =
				genEntityMap(1L, eform.getId(), EntityType.EFORM, ownerAccount, null, PermissionType.OWNER);
		EntityMap entityMap2 =
				genEntityMap(2L, eform.getId(), EntityType.EFORM, null, permissionGroup, PermissionType.READ);
		BricsFile bricsFile =
				new BricsFile("UUID001", "image.png", SystemFileCategory.QUESTION_DOCUMENT, eform.getId());
		UserToken userToken = genUserToken(1L, account, userRoles);

		// Stub the repository method calls.
		when(basicEformRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(eform));
		when(permissionGroupRepository.findAllByMemberSet_Account(account)).thenReturn(Arrays.asList(permissionGroup));
		when(entityMapRepository.findAllByEntityIdAndType(eform.getId(), EntityType.EFORM))
				.thenReturn(Arrays.asList(entityMap, entityMap2));

		// Test eForm file access for a regular proforms user, that has access via a permission group.
		Assert.assertTrue(filePermissionsService.validateEformAccess(bricsFile, account, userToken),
				"The proforms user should have access to the eForm documents.");
	}

	@Test
	public void testEformAccessAsStandardQueryUser() {
		// Create test objects.
		RoleType[] userRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_QUERY};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY_EFORM};
		User user = genUser(1L, "Some", "User", "some.user@nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account account = genAccount(1L, "admin", user, userRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		MiniEform eform = genMiniEform(1L, "Test eForm", StatusType.DRAFT);
		EntityMap entityMap =
				genEntityMap(1L, eform.getId(), EntityType.EFORM, ownerAccount, null, PermissionType.OWNER);
		EntityMap entityMap2 = genEntityMap(2L, eform.getId(), EntityType.EFORM, account, null, PermissionType.READ);
		BricsFile bricsFile =
				new BricsFile("UUID001", "image.png", SystemFileCategory.QUESTION_DOCUMENT, eform.getId());
		UserToken userToken = genUserToken(1L, account, userRoles);

		// Stub the repository method calls.
		when(basicEformRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(eform));
		when(permissionGroupRepository.findAllByMemberSet_Account(account)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(eform.getId(), EntityType.EFORM))
				.thenReturn(Arrays.asList(entityMap, entityMap2));

		// Test eForm file access for a regular query user.
		Assert.assertTrue(filePermissionsService.validateEformAccess(bricsFile, account, userToken),
				"The query user should have access to the eForm documents.");
	}

	@Test
	public void testEformAccessWithPublishedEform() {
		// Create test objects.
		RoleType[] userRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_PROFORMS};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY_EFORM};
		PermissionGroup permissionGroup = genPermissionGroup(1L, "Test Permission Group 1");
		User user = genUser(1L, "Some", "User", "some.user@nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account account = genAccount(1L, "admin", user, userRoles, permissionGroup);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		MiniEform eform = genMiniEform(1L, "Test eForm", StatusType.PUBLISHED);
		EntityMap entityMap =
				genEntityMap(1L, eform.getId(), EntityType.EFORM, ownerAccount, null, PermissionType.OWNER);
		BricsFile bricsFile =
				new BricsFile("UUID001", "image.png", SystemFileCategory.QUESTION_DOCUMENT, eform.getId());
		UserToken userToken = genUserToken(1L, account, userRoles);

		// Stub the repository method calls.
		when(basicEformRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(eform));
		when(permissionGroupRepository.findAllByMemberSet_Account(account)).thenReturn(Arrays.asList(permissionGroup));
		when(entityMapRepository.findAllByEntityIdAndType(eform.getId(), EntityType.EFORM))
				.thenReturn(Arrays.asList(entityMap));

		// Test eForm file access to a published eForm.
		Assert.assertTrue(filePermissionsService.validateEformAccess(bricsFile, account, userToken),
				"The user should have access to the published eForm documents.");
	}

	@Test
	public void testEformAccessWithWrongPermission() {
		// Create test objects.
		RoleType[] userRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY_EFORM};
		PermissionGroup permissionGroup = genPermissionGroup(1L, "Test Permission Group 1");
		User user = genUser(1L, "Some", "User", "some.user@nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account account = genAccount(1L, "admin", user, userRoles, permissionGroup);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		MiniEform eform = genMiniEform(1L, "Test eForm", StatusType.DRAFT);
		EntityMap entityMap =
				genEntityMap(1L, eform.getId(), EntityType.EFORM, ownerAccount, null, PermissionType.OWNER);
		BricsFile bricsFile =
				new BricsFile("UUID001", "image.png", SystemFileCategory.QUESTION_DOCUMENT, eform.getId());
		UserToken userToken = genUserToken(1L, account, userRoles);

		// Stub the repository method calls.
		when(basicEformRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(eform));
		when(permissionGroupRepository.findAllByMemberSet_Account(account)).thenReturn(Arrays.asList(permissionGroup));
		when(entityMapRepository.findAllByEntityIdAndType(eform.getId(), EntityType.EFORM))
				.thenReturn(Arrays.asList(entityMap));

		// Test eForm file access for a user with the wrong permissions.
		Assert.assertFalse(filePermissionsService.validateEformAccess(bricsFile, account, userToken),
				"The user shouldn't have access to the eForm documents.");
	}

	@Test
	public void testEformAccessWithWrongRole() {
		// Create test objects.
		RoleType[] userRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_ACCOUNT_REVIEWER};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY_EFORM};
		User user = genUser(1L, "Some", "User", "some.user@nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account account = genAccount(1L, "admin", user, userRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		MiniEform eform = genMiniEform(1L, "Test eForm", StatusType.DRAFT);
		EntityMap entityMap =
				genEntityMap(1L, eform.getId(), EntityType.EFORM, ownerAccount, null, PermissionType.OWNER);
		BricsFile bricsFile =
				new BricsFile("UUID001", "image.png", SystemFileCategory.QUESTION_DOCUMENT, eform.getId());
		UserToken userToken = genUserToken(1L, account, userRoles);

		// Stub the repository method calls.
		when(basicEformRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(eform));
		when(permissionGroupRepository.findAllByMemberSet_Account(account)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(eform.getId(), EntityType.EFORM))
				.thenReturn(Arrays.asList(entityMap));

		// Test eForm file access from a user with an invalid role.
		Assert.assertFalse(filePermissionsService.validateEformAccess(bricsFile, account, userToken),
				"The user shouldn't have access to the eForm documents.");
	}

	/*
	 * @formatter:off
	 * ########################################################################
	 * # Electronic Signature Access Tests
	 * ########################################################################
	 * @formatter:on
	 */

	@Test
	public void testESignatureAccessAsSysAdmin() {
		// Create test objects.
		RoleType[] adminRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_ADMIN};
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_STUDY};
		User user = genUser(1L, "Admin", "User", "admin@brics.nih.gov");
		User owner = genUser(2L, "Some", "User", "some.user@nih.gov");
		Account account = genAccount(1L, "admin", user, adminRoles, null);
		Account ownerAccount = genAccount(2L, "testUser", owner, roleTypes, null);
		ElectronicSignature esig = genElectronicSignature(1L, ownerAccount);
		BricsFile bricsFile =
				new BricsFile("UUID001", "signature.pdf", SystemFileCategory.ELECTRONIC_SIGNATURE, esig.getId());
		UserToken userToken = genUserToken(1L, account, adminRoles);

		// Stub the repository method calls.
		when(electronicSignatureRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(esig));

		// Test electronic signature access for the system admin user.
		Assert.assertTrue(filePermissionsService.validateElectronicSignatureAccess(bricsFile, account, userToken),
				"The system admin user should have access to all electronic signature documents.");
	}

	@Test
	public void testESignatureAccessAsAccountAdmin() {
		// Create test objects.
		RoleType[] adminRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_ACCOUNT_ADMIN};
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_STUDY};
		User user = genUser(1L, "Admin", "User", "admin@brics.nih.gov");
		User owner = genUser(2L, "Some", "User", "some.user@nih.gov");
		Account account = genAccount(1L, "admin", user, adminRoles, null);
		Account ownerAccount = genAccount(2L, "testUser", owner, roleTypes, null);
		ElectronicSignature esig = genElectronicSignature(1L, ownerAccount);
		BricsFile bricsFile =
				new BricsFile("UUID001", "signature.pdf", SystemFileCategory.ELECTRONIC_SIGNATURE, esig.getId());
		UserToken userToken = genUserToken(1L, account, adminRoles);

		// Stub the repository method calls.
		when(electronicSignatureRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(esig));

		// Test electronic signature access for the account admin user.
		Assert.assertTrue(filePermissionsService.validateElectronicSignatureAccess(bricsFile, account, userToken),
				"The account admin user should have access to all electronic signature documents.");
	}

	@Test
	public void testESignatureAccessAsStandardUser() {
		// Create test objects.
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_STUDY};
		User user = genUser(1L, "Some", "User", "some.user@nih.gov");
		Account account = genAccount(1L, "testUser", user, roleTypes, null);
		ElectronicSignature esig = genElectronicSignature(1L, account);
		BricsFile bricsFile =
				new BricsFile("UUID001", "signature.pdf", SystemFileCategory.ELECTRONIC_SIGNATURE, esig.getId());
		UserToken userToken = genUserToken(1L, account, roleTypes);

		// Stub the repository method calls.
		when(electronicSignatureRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(esig));

		// Test electronic signature access for regular user's own signature document.
		Assert.assertTrue(filePermissionsService.validateElectronicSignatureAccess(bricsFile, account, userToken),
				"The standard user should have access to its own electronic signature document.");
	}

	@Test
	public void testESignatureAccessWithOtherUser() {
		// Create test objects.
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_STUDY};
		User user = genUser(1L, "Some", "User", "some.user@nih.gov");
		User owner = genUser(2L, "Other", "User", "other.user@nih.gov");
		Account account = genAccount(1L, "testUser", user, roleTypes, null);
		Account ownerAccount = genAccount(2L, "otherUser", owner, roleTypes, null);
		ElectronicSignature esig = genElectronicSignature(1L, ownerAccount);
		BricsFile bricsFile =
				new BricsFile("UUID001", "signature.pdf", SystemFileCategory.ELECTRONIC_SIGNATURE, esig.getId());
		UserToken userToken = genUserToken(1L, account, roleTypes);

		// Stub the repository method calls.
		when(electronicSignatureRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(esig));

		// Test electronic signature access for a study user that is trying to access another's signature file.
		Assert.assertFalse(filePermissionsService.validateElectronicSignatureAccess(bricsFile, account, userToken),
				"The standard user should have access to its own electronic signature document.");
	}

	/*
	 * @formatter:off
	 * ########################################################################
	 * # Meta Study File Access Tests
	 * ########################################################################
	 * @formatter:on
	 */

	@Test
	public void testMetaStudyAccessAsSysAdmin() {
		// Create test objects.
		RoleType[] adminRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_ADMIN};
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_METASTUDY};
		User admin = genUser(1L, "Super", "User", "god@brics.nih.gov");
		User owner = genUser(2L, "Some", "User", "some.user@nih.gov");
		Account adminAccount = genAccount(1L, "admin", admin, adminRoles, null);
		Account ownerAccount = genAccount(2L, "someUser", owner, roleTypes, null);
		MetaStudy metaStudy = genMetaStudy(1L, "Test Meta Study", MetaStudyStatus.DRAFT);
		BricsFile bricsFile =
				new BricsFile("UUID001", "some-file.pdf", SystemFileCategory.META_STUDY, metaStudy.getId());
		UserToken userToken = genUserToken(1L, adminAccount, adminRoles);
		EntityMap entityMap =
				genEntityMap(1L, metaStudy.getId(), EntityType.META_STUDY, ownerAccount, null, PermissionType.OWNER);

		// Stub the repository method calls.
		when(metaStudyRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(metaStudy));
		when(permissionGroupRepository.findAllByMemberSet_Account(adminAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(metaStudy.getId(), EntityType.META_STUDY))
				.thenReturn(Arrays.asList(entityMap));

		// Test meta study access for the system admin user.
		Assert.assertTrue(filePermissionsService.validateMetaStudyAccess(bricsFile, adminAccount, userToken),
				"The system admin should be able to access all meta study files.");
	}

	@Test
	public void testMetaStudyAccessAsMetaStudyAdmin() {
		// Create test objects.
		RoleType[] adminRoles =
				new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_METASTUDY, RoleType.ROLE_METASTUDY_ADMIN};
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_METASTUDY};
		User admin = genUser(1L, "Admin", "User", "meta.study.admin@brics.nih.gov");
		User owner = genUser(2L, "Some", "User", "some.user@nih.gov");
		Account adminAccount = genAccount(1L, "metaStudyAdmin", admin, adminRoles, null);
		Account ownerAccount = genAccount(2L, "someUser", owner, roleTypes, null);
		MetaStudy metaStudy = genMetaStudy(1L, "Test Meta Study", MetaStudyStatus.DRAFT);
		BricsFile bricsFile =
				new BricsFile("UUID001", "some-file.pdf", SystemFileCategory.META_STUDY, metaStudy.getId());
		UserToken userToken = genUserToken(1L, adminAccount, adminRoles);
		EntityMap entityMap =
				genEntityMap(1L, metaStudy.getId(), EntityType.META_STUDY, ownerAccount, null, PermissionType.OWNER);

		// Stub the repository method calls.
		when(metaStudyRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(metaStudy));
		when(permissionGroupRepository.findAllByMemberSet_Account(adminAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(metaStudy.getId(), EntityType.META_STUDY))
				.thenReturn(Arrays.asList(entityMap));

		// Test meta study access for the meta study admin user.
		Assert.assertTrue(filePermissionsService.validateMetaStudyAccess(bricsFile, adminAccount, userToken),
				"The meta study admin should be able to access all meta study files.");
	}

	@Test
	public void testMetaStudyAccessAsStandardMetaStudyUser() {
		// Create test objects.
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_METASTUDY};
		User user = genUser(1L, "Some", "User", "some.user@nih.gov");
		Account account = genAccount(1L, "someUser", user, roleTypes, null);
		MetaStudy metaStudy = genMetaStudy(1L, "Test Meta Study", MetaStudyStatus.DRAFT);
		BricsFile bricsFile = new BricsFile("UUID001", "some-file.pdf",
				SystemFileCategory.META_STUDY_SUPPORTING_DOCUMENTATION, metaStudy.getId());
		UserToken userToken = genUserToken(1L, account, roleTypes);
		EntityMap entityMap =
				genEntityMap(1L, metaStudy.getId(), EntityType.META_STUDY, account, null, PermissionType.OWNER);

		// Stub the repository method calls.
		when(metaStudyRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(metaStudy));
		when(permissionGroupRepository.findAllByMemberSet_Account(account)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(metaStudy.getId(), EntityType.META_STUDY))
				.thenReturn(Arrays.asList(entityMap));

		// Test meta study access for a meta study user who has explicit access to the meta study.
		Assert.assertTrue(filePermissionsService.validateMetaStudyAccess(bricsFile, account, userToken),
				"The meta study user should be able to access its own meta study files.");
	}

	@Test
	public void testMetaStudyAccessToPublishedMetaStudy() {
		// Create test objects.
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_METASTUDY};
		User user = genUser(1L, "Some", "User", "some.user@nih.gov");
		User owner = genUser(2L, "Other", "User", "other.user@nih.gov");
		Account account = genAccount(1L, "someUser", user, roleTypes, null);
		Account ownerAccount = genAccount(2L, "otherUser", owner, roleTypes, null);
		MetaStudy metaStudy = genMetaStudy(1L, "Test Meta Study", MetaStudyStatus.PUBLISHED);
		BricsFile bricsFile =
				new BricsFile("UUID001", "some-file.pdf", SystemFileCategory.META_STUDY, metaStudy.getId());
		UserToken userToken = genUserToken(1L, account, roleTypes);
		EntityMap entityMap =
				genEntityMap(1L, metaStudy.getId(), EntityType.META_STUDY, ownerAccount, null, PermissionType.OWNER);

		// Stub the repository method calls.
		when(metaStudyRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(metaStudy));
		when(permissionGroupRepository.findAllByMemberSet_Account(account)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(metaStudy.getId(), EntityType.META_STUDY))
				.thenReturn(Arrays.asList(entityMap));

		// Test meta study access for a meta study user to a publish meta study.
		Assert.assertTrue(filePermissionsService.validateMetaStudyAccess(bricsFile, account, userToken),
				"The meta study user should be able to access files from a published meta study.");
	}

	@Test
	public void testMetaStudyAccessWithWrongPermission() {
		// Create test objects.
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_METASTUDY};
		User user = genUser(1L, "Some", "User", "some.user@nih.gov");
		User owner = genUser(2L, "Other", "User", "other.user@nih.gov");
		Account account = genAccount(1L, "someUser", user, roleTypes, null);
		Account ownerAccount = genAccount(2L, "otherUser", owner, roleTypes, null);
		MetaStudy metaStudy = genMetaStudy(1L, "Test Meta Study", MetaStudyStatus.DRAFT);
		BricsFile bricsFile =
				new BricsFile("UUID001", "some-file.pdf", SystemFileCategory.META_STUDY, metaStudy.getId());
		UserToken userToken = genUserToken(1L, account, roleTypes);
		EntityMap entityMap =
				genEntityMap(1L, metaStudy.getId(), EntityType.META_STUDY, ownerAccount, null, PermissionType.OWNER);

		// Stub the repository method calls.
		when(metaStudyRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(metaStudy));
		when(permissionGroupRepository.findAllByMemberSet_Account(account)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(metaStudy.getId(), EntityType.META_STUDY))
				.thenReturn(Arrays.asList(entityMap));

		// Test meta study access for a meta study that has no permissions to a draft meta study.
		Assert.assertFalse(filePermissionsService.validateMetaStudyAccess(bricsFile, account, userToken),
				"The meta study user shouldn't be able to access files from a meta study they don't have access to.");
	}

	@Test
	public void testMetaStudyAccessWithWrongRole() {
		// Create test objects.
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_PROFORMS};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_METASTUDY};
		User user = genUser(1L, "Some", "User", "some.user@nih.gov");
		User owner = genUser(2L, "Other", "User", "other.user@nih.gov");
		Account account = genAccount(1L, "someUser", user, roleTypes, null);
		Account ownerAccount = genAccount(2L, "otherUser", owner, ownerRoles, null);
		MetaStudy metaStudy = genMetaStudy(1L, "Test Meta Study", MetaStudyStatus.PUBLISHED);
		BricsFile bricsFile =
				new BricsFile("UUID001", "some-file.pdf", SystemFileCategory.META_STUDY, metaStudy.getId());
		UserToken userToken = genUserToken(1L, account, roleTypes);
		EntityMap entityMap =
				genEntityMap(1L, metaStudy.getId(), EntityType.META_STUDY, ownerAccount, null, PermissionType.OWNER);

		// Stub the repository method calls.
		when(metaStudyRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(metaStudy));
		when(permissionGroupRepository.findAllByMemberSet_Account(account)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(metaStudy.getId(), EntityType.META_STUDY))
				.thenReturn(Arrays.asList(entityMap));

		// Test meta study access for a meta study user to a publish meta study.
		Assert.assertFalse(filePermissionsService.validateMetaStudyAccess(bricsFile, account, userToken),
				"The user with an invalid role shouldn't be able to access files from a meta study.");
	}

	/*
	 * @formatter:off
	 * ########################################################################
	 * # Order Manager File Access Tests
	 * ########################################################################
	 * @formatter:on
	 */

	@Test
	public void testOrderManagerAccessAsSysAdmin() {
		// Create test objects.
		RoleType[] adminRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_ADMIN};
		User admin = genUser(1L, "Super", "User", "god@brics.nih.gov");
		User owner = genUser(2L, "Some", "User", "some.user@nih.gov");
		Account adminAccount = genAccount(1L, "admin", admin, adminRoles, null);
		BiospecimenOrder order = genBiospecimenOrder(1L, "Test Order", owner, owner);
		BricsFile bricsFile = new BricsFile("UUID001", "order.xml", SystemFileCategory.ORDER_MANAGER, order.getId());
		UserToken userToken = genUserToken(1L, adminAccount, adminRoles);

		// Stub the repository method calls.
		when(biospecimenOrderRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(order));

		// Test order manager access for the system admin user.
		Assert.assertTrue(filePermissionsService.validateOrderManagerAccess(bricsFile, adminAccount, userToken),
				"The system admin user should have access to all biosample orders.");
	}

	@Test
	public void testOrderManagerAccessAsOrderAdmin() {
		// Create test objects.
		RoleType[] adminRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_ORDER_ADMIN};
		User admin = genUser(1L, "Order", "Admin", "order.admin@brics.nih.gov");
		User owner = genUser(2L, "Some", "User", "some.user@nih.gov");
		Account adminAccount = genAccount(1L, "admin", admin, adminRoles, null);
		BiospecimenOrder order = genBiospecimenOrder(1L, "Test Order", owner, owner);
		BricsFile bricsFile = new BricsFile("UUID001", "order.xml", SystemFileCategory.ORDER_MANAGER, order.getId());
		UserToken userToken = genUserToken(1L, adminAccount, adminRoles);

		// Stub the repository method calls.
		when(biospecimenOrderRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(order));

		// Test order manager access for the order admin user.
		Assert.assertTrue(filePermissionsService.validateOrderManagerAccess(bricsFile, adminAccount, userToken),
				"The order admin user should have access to all biosample orders.");
	}

	@Test
	public void testOrderManagerAccessWithStandardUser() {
		// Create test objects.
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_QUERY};
		User brac = genUser(1L, "Brac", "User", "brac.user@nih.gov");
		User owner = genUser(2L, "Owner", "User", "owner.user@nih.gov");
		Account ownerAccount = genAccount(1L, "someUser", owner, roleTypes, null);
		BiospecimenOrder order = genBiospecimenOrder(1L, "Test Order", owner, brac);
		BricsFile bricsFile = new BricsFile("UUID001", "order.xml", SystemFileCategory.ORDER_MANAGER, order.getId());
		UserToken userToken = genUserToken(1L, ownerAccount, roleTypes);

		// Stub the repository method calls.
		when(biospecimenOrderRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(order));

		// Test order manager access for a regular user.
		Assert.assertTrue(filePermissionsService.validateOrderManagerAccess(bricsFile, ownerAccount, userToken),
				"The standard user should have access to its own biosample order.");
	}

	@Test
	public void testOrderManagerAccessWithBracUser() {
		// Create test objects.
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_QUERY};
		User brac = genUser(1L, "Brac", "User", "brac.user@nih.gov");
		User owner = genUser(2L, "Owner", "User", "owner.user@nih.gov");
		Account bracAccount = genAccount(1L, "bracUser", brac, roleTypes, null);
		BiospecimenOrder order = genBiospecimenOrder(1L, "Test Order", owner, brac);
		BricsFile bricsFile = new BricsFile("UUID001", "order.xml", SystemFileCategory.ORDER_MANAGER, order.getId());
		UserToken userToken = genUserToken(1L, bracAccount, roleTypes);

		// Stub the repository method calls.
		when(biospecimenOrderRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(order));

		// Test order manager access for a brac user.
		Assert.assertTrue(filePermissionsService.validateOrderManagerAccess(bricsFile, bracAccount, userToken),
				"The brac user should have access to all biosample orders.");
	}

	@Test
	public void testOrderManagerAccessWithWrongUser() {
		// Create test objects.
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_QUERY};
		User brac = genUser(1L, "Brac", "User", "brac.user@nih.gov");
		User owner = genUser(2L, "Owner", "User", "owner.user@nih.gov");
		User other = genUser(3L, "Other", "User", "other.user@nih.gov");
		Account account = genAccount(1L, "otherUser", other, roleTypes, null);
		BiospecimenOrder order = genBiospecimenOrder(1L, "Test Order", owner, brac);
		BricsFile bricsFile = new BricsFile("UUID001", "order.xml", SystemFileCategory.ORDER_MANAGER, order.getId());
		UserToken userToken = genUserToken(1L, account, roleTypes);

		// Stub the repository method calls.
		when(biospecimenOrderRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(order));

		// Test order manager access for an invalid user.
		Assert.assertFalse(filePermissionsService.validateOrderManagerAccess(bricsFile, account, userToken),
				"The user shouldn't have access to the biosample order.");
	}

	/*
	 * @formatter:off
	 * ########################################################################
	 * # Study File Access Tests
	 * ########################################################################
	 * @formatter:on
	 */

	@Test
	public void testStudyAccessAsSysAdmin() {
		// Create test objects.
		RoleType[] adminRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_ADMIN};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_STUDY};
		User admin = genUser(1L, "Supreme", "Overload", "god@brics.nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account adminAccount = genAccount(1L, "admin", admin, adminRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		Study study = genStudy(1L, "Test Study 1", StudyStatus.PRIVATE);
		EntityMap entityMap =
				genEntityMap(1L, study.getId(), EntityType.STUDY, ownerAccount, null, PermissionType.OWNER);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.csv", SystemFileCategory.STUDY, study.getId());
		UserToken userToken = genUserToken(1L, adminAccount, adminRoles);

		// Stub the repository method calls.
		when(studyRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(study));
		when(permissionGroupRepository.findAllByMemberSet_Account(adminAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(study.getId(), EntityType.STUDY))
				.thenReturn(Arrays.asList(entityMap));

		// Test study access as the system admin user.
		Assert.assertTrue(filePermissionsService.validateStudyAccess(bricsFile, adminAccount, userToken),
				"The system admin user should have access to all study files.");
	}

	@Test
	public void testStudyAccessAsStudyAdmin() {
		// Create test objects.
		RoleType[] adminRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_STUDY, RoleType.ROLE_STUDY_ADMIN};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_STUDY};
		User admin = genUser(1L, "Study", "Admin", "study.admin@brics.nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account adminAccount = genAccount(1L, "studyAdmin", admin, adminRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		Study study = genStudy(1L, "Test Study 1", StudyStatus.PRIVATE);
		EntityMap entityMap =
				genEntityMap(1L, study.getId(), EntityType.STUDY, ownerAccount, null, PermissionType.OWNER);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.csv", SystemFileCategory.STUDY, study.getId());
		UserToken userToken = genUserToken(1L, adminAccount, adminRoles);

		// Stub the repository method calls.
		when(studyRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(study));
		when(permissionGroupRepository.findAllByMemberSet_Account(adminAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(study.getId(), EntityType.STUDY))
				.thenReturn(Arrays.asList(entityMap));

		// Test study access as the study admin user.
		Assert.assertTrue(filePermissionsService.validateStudyAccess(bricsFile, adminAccount, userToken),
				"The study admin user should have access to all study files.");
	}

	@Test
	public void testStudyAccessAsRepositoryAdmin() {
		// Create test objects.
		RoleType[] adminRoles =
				new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_STUDY, RoleType.ROLE_REPOSITORY_ADMIN};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_STUDY};
		User admin = genUser(1L, "Repository", "Admin", "study.admin@brics.nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account adminAccount = genAccount(1L, "repoAdmin", admin, adminRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		Study study = genStudy(1L, "Test Study 1", StudyStatus.PRIVATE);
		EntityMap entityMap =
				genEntityMap(1L, study.getId(), EntityType.STUDY, ownerAccount, null, PermissionType.OWNER);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.csv", SystemFileCategory.STUDY, study.getId());
		UserToken userToken = genUserToken(1L, adminAccount, adminRoles);

		// Stub the repository method calls.
		when(studyRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(study));
		when(permissionGroupRepository.findAllByMemberSet_Account(adminAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(study.getId(), EntityType.STUDY))
				.thenReturn(Arrays.asList(entityMap));

		// Test study access as the repository admin user.
		Assert.assertTrue(filePermissionsService.validateStudyAccess(bricsFile, adminAccount, userToken),
				"The repository admin user should have access to all study files.");
	}

	@Test
	public void testStudyAccessWithStandardStudyUser() {
		// Create test objects.
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_STUDY};
		PermissionGroup permissionGroup = genPermissionGroup(1L, "Test Permission Group 1");
		User owner = genUser(1L, "Researcher", "One", "rOne@institute.edu");
		User user = genUser(2L, "Some", "User", "some.user@nih.gov");
		Account ownerAccount = genAccount(1L, "rone", owner, roleTypes, null);
		Account account = genAccount(2L, "someUser", user, roleTypes, permissionGroup);
		Study study = genStudy(1L, "Test Study 1", StudyStatus.PRIVATE);
		EntityMap entityMap =
				genEntityMap(1L, study.getId(), EntityType.STUDY, ownerAccount, null, PermissionType.OWNER);
		EntityMap entityMap2 =
				genEntityMap(2L, study.getId(), EntityType.STUDY, null, permissionGroup, PermissionType.READ);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.csv",
				SystemFileCategory.STUDY_SUPPORTING_DOCUMENTATION, study.getId());
		UserToken userToken = genUserToken(1L, account, roleTypes);

		// Stub the repository method calls.
		when(studyRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(study));
		when(permissionGroupRepository.findAllByMemberSet_Account(account)).thenReturn(Arrays.asList(permissionGroup));
		when(entityMapRepository.findAllByEntityIdAndType(study.getId(), EntityType.STUDY))
				.thenReturn(Arrays.asList(entityMap, entityMap2));

		// Test study access as a study user.
		Assert.assertTrue(filePermissionsService.validateStudyAccess(bricsFile, account, userToken),
				"The study user should have access to the study files via a permission group.");
	}

	@Test
	public void testStudyAccessToPublicStudy() {
		// Create test objects.
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_STUDY};
		User owner = genUser(1L, "Researcher", "One", "rOne@institute.edu");
		User user = genUser(2L, "Some", "User", "some.user@nih.gov");
		Account ownerAccount = genAccount(1L, "rone", owner, roleTypes, null);
		Account account = genAccount(2L, "someUser", user, roleTypes, null);
		Study study = genStudy(1L, "Test Study 1", StudyStatus.PUBLIC);
		EntityMap entityMap =
				genEntityMap(1L, study.getId(), EntityType.STUDY, ownerAccount, null, PermissionType.OWNER);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.csv", SystemFileCategory.STUDY, study.getId());
		UserToken userToken = genUserToken(1L, account, roleTypes);

		// Stub the repository method calls.
		when(studyRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(study));
		when(permissionGroupRepository.findAllByMemberSet_Account(account)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(study.getId(), EntityType.STUDY))
				.thenReturn(Arrays.asList(entityMap));

		// Test study access as a study user to a public study it doesn't have explicit access to.
		Assert.assertTrue(filePermissionsService.validateStudyAccess(bricsFile, account, userToken),
				"The study user should have access to the public study files.");
	}

	@Test
	public void testStudyAccessWithWrongUser() {
		// Create test objects.
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_STUDY};
		User owner = genUser(1L, "Researcher", "One", "rOne@institute.edu");
		User user = genUser(2L, "Some", "User", "some.user@nih.gov");
		Account ownerAccount = genAccount(1L, "rone", owner, roleTypes, null);
		Account account = genAccount(2L, "someUser", user, roleTypes, null);
		Study study = genStudy(1L, "Test Study 1", StudyStatus.PRIVATE);
		EntityMap entityMap =
				genEntityMap(1L, study.getId(), EntityType.STUDY, ownerAccount, null, PermissionType.OWNER);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.csv", SystemFileCategory.STUDY, study.getId());
		UserToken userToken = genUserToken(1L, account, roleTypes);

		// Stub the repository method calls.
		when(studyRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(study));
		when(permissionGroupRepository.findAllByMemberSet_Account(account)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(study.getId(), EntityType.STUDY))
				.thenReturn(Arrays.asList(entityMap));

		// Test study access as a study user to a public study it doesn't have explicit access to.
		Assert.assertFalse(filePermissionsService.validateStudyAccess(bricsFile, account, userToken),
				"The study user shouldn't have access to these private study files.");
	}

	@Test
	public void testStudyAccessWithWrongRole() {
		// Create test objects.
		RoleType[] roleTypes = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_STUDY};
		RoleType[] otherRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_REPORTING_ADMIN};
		User owner = genUser(1L, "Researcher", "One", "rOne@institute.edu");
		User user = genUser(2L, "Some", "User", "some.user@nih.gov");
		Account ownerAccount = genAccount(1L, "rone", owner, roleTypes, null);
		Account account = genAccount(2L, "someUser", user, otherRoles, null);
		Study study = genStudy(1L, "Test Study 1", StudyStatus.PUBLIC);
		EntityMap entityMap =
				genEntityMap(1L, study.getId(), EntityType.STUDY, ownerAccount, null, PermissionType.OWNER);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.csv", SystemFileCategory.STUDY, study.getId());
		UserToken userToken = genUserToken(1L, account, otherRoles);

		// Stub the repository method calls.
		when(studyRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(study));
		when(permissionGroupRepository.findAllByMemberSet_Account(account)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(study.getId(), EntityType.STUDY))
				.thenReturn(Arrays.asList(entityMap));

		// Test study access as a user that has an invalid role.
		Assert.assertFalse(filePermissionsService.validateStudyAccess(bricsFile, account, userToken),
				"The study user shouldn't have access to the study files, since the role is incorrect.");
	}

	/*
	 * @formatter:off
	 * ########################################################################
	 * # Form Structure Access Tests
	 * ########################################################################
	 * @formatter:on
	 */

	@Test
	public void testFormStructureAccessAsSysAdmin() {
		// Create test objects.
		RoleType[] adminRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_ADMIN};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY};
		User admin = genUser(1L, "Supreme", "Overload", "god@brics.nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account adminAccount = genAccount(1L, "admin", admin, adminRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		BasicFormStructure formStructure = genBasicFormStructure(1L, "Test Form Structure", StatusType.DRAFT);
		EntityMap entityMap = genEntityMap(1L, formStructure.getId(), EntityType.DATA_STRUCTURE, ownerAccount, null,
				PermissionType.OWNER);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.pdf",
				SystemFileCategory.FORM_STRUCTURE_SUPPORTING_DOCUMENTATION, formStructure.getId());
		UserToken userToken = genUserToken(1L, adminAccount, adminRoles);

		// Stub the repository method calls.
		when(basicFormStructureRepository.findById(bricsFile.getLinkedObjectID()))
				.thenReturn(Optional.of(formStructure));
		when(permissionGroupRepository.findAllByMemberSet_Account(adminAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(formStructure.getId(), EntityType.DATA_STRUCTURE))
				.thenReturn(Arrays.asList(entityMap));

		// Test form structure access as the system admin user.
		Assert.assertTrue(filePermissionsService.validateFormStructureAccess(bricsFile, adminAccount, userToken),
				"The system admin user should have access to all form structure files.");
	}

	@Test
	public void testFormStructureAccessAsDictionaryAdmin() {
		// Create test objects.
		RoleType[] adminRoles =
				new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY, RoleType.ROLE_DICTIONARY_ADMIN};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY};
		User admin = genUser(1L, "Dictionary", "Admin", "dictionary.admin@nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account adminAccount = genAccount(1L, "dictAdmin", admin, adminRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		BasicFormStructure formStructure = genBasicFormStructure(1L, "Test Form Structure", StatusType.DRAFT);
		EntityMap entityMap = genEntityMap(1L, formStructure.getId(), EntityType.DATA_STRUCTURE, ownerAccount, null,
				PermissionType.OWNER);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.pdf",
				SystemFileCategory.FORM_STRUCTURE_SUPPORTING_DOCUMENTATION, formStructure.getId());
		UserToken userToken = genUserToken(1L, adminAccount, adminRoles);

		// Stub the repository method calls.
		when(basicFormStructureRepository.findById(bricsFile.getLinkedObjectID()))
				.thenReturn(Optional.of(formStructure));
		when(permissionGroupRepository.findAllByMemberSet_Account(adminAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(formStructure.getId(), EntityType.DATA_STRUCTURE))
				.thenReturn(Arrays.asList(entityMap));

		// Test form structure access as the dictionary admin user.
		Assert.assertTrue(filePermissionsService.validateFormStructureAccess(bricsFile, adminAccount, userToken),
				"The dictionary admin user should have access to all form structure files.");
	}

	@Test
	public void testFormStructureAccessAsProformsAdmin() {
		// Create test objects.
		RoleType[] adminRoles =
				new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_PROFORMS, RoleType.ROLE_PROFORMS_ADMIN};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY};
		User admin = genUser(1L, "Proforms", "Admin", "proforms.admin@nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account adminAccount = genAccount(1L, "proformsAdmin", admin, adminRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		BasicFormStructure formStructure = genBasicFormStructure(1L, "Test Form Structure", StatusType.DRAFT);
		EntityMap entityMap = genEntityMap(1L, formStructure.getId(), EntityType.DATA_STRUCTURE, ownerAccount, null,
				PermissionType.OWNER);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.pdf",
				SystemFileCategory.FORM_STRUCTURE_SUPPORTING_DOCUMENTATION, formStructure.getId());
		UserToken userToken = genUserToken(1L, adminAccount, adminRoles);

		// Stub the repository method calls.
		when(basicFormStructureRepository.findById(bricsFile.getLinkedObjectID()))
				.thenReturn(Optional.of(formStructure));
		when(permissionGroupRepository.findAllByMemberSet_Account(adminAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(formStructure.getId(), EntityType.DATA_STRUCTURE))
				.thenReturn(Arrays.asList(entityMap));

		// Test form structure access as the proforms admin user.
		Assert.assertTrue(filePermissionsService.validateFormStructureAccess(bricsFile, adminAccount, userToken),
				"The proforms admin user should have access to all form structure files.");
	}

	@Test
	public void testFormStructureAccessAsClinicalAdmin() {
		// Create test objects.
		RoleType[] adminRoles =
				new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_PROFORMS, RoleType.ROLE_CLINICAL_ADMIN};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY};
		User admin = genUser(1L, "Clinical", "Admin", "clinical.admin@nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account adminAccount = genAccount(1L, "clinicalAdmin", admin, adminRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		BasicFormStructure formStructure = genBasicFormStructure(1L, "Test Form Structure", StatusType.DRAFT);
		EntityMap entityMap = genEntityMap(1L, formStructure.getId(), EntityType.DATA_STRUCTURE, ownerAccount, null,
				PermissionType.OWNER);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.pdf",
				SystemFileCategory.FORM_STRUCTURE_SUPPORTING_DOCUMENTATION, formStructure.getId());
		UserToken userToken = genUserToken(1L, adminAccount, adminRoles);

		// Stub the repository method calls.
		when(basicFormStructureRepository.findById(bricsFile.getLinkedObjectID()))
				.thenReturn(Optional.of(formStructure));
		when(permissionGroupRepository.findAllByMemberSet_Account(adminAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(formStructure.getId(), EntityType.DATA_STRUCTURE))
				.thenReturn(Arrays.asList(entityMap));

		// Test form structure access as the clinical admin user.
		Assert.assertTrue(filePermissionsService.validateFormStructureAccess(bricsFile, adminAccount, userToken),
				"The clinical admin user should have access to all form structure files.");
	}

	@Test
	public void testFormStructureAccessAsQueryAdmin() {
		// Create test objects.
		RoleType[] adminRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_QUERY, RoleType.ROLE_QUERY_ADMIN};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY};
		User admin = genUser(1L, "Query", "Admin", "query.admin@nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account adminAccount = genAccount(1L, "queryAdmin", admin, adminRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		BasicFormStructure formStructure = genBasicFormStructure(1L, "Test Form Structure", StatusType.DRAFT);
		EntityMap entityMap = genEntityMap(1L, formStructure.getId(), EntityType.DATA_STRUCTURE, ownerAccount, null,
				PermissionType.OWNER);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.pdf",
				SystemFileCategory.FORM_STRUCTURE_SUPPORTING_DOCUMENTATION, formStructure.getId());
		UserToken userToken = genUserToken(1L, adminAccount, adminRoles);

		// Stub the repository method calls.
		when(basicFormStructureRepository.findById(bricsFile.getLinkedObjectID()))
				.thenReturn(Optional.of(formStructure));
		when(permissionGroupRepository.findAllByMemberSet_Account(adminAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(formStructure.getId(), EntityType.DATA_STRUCTURE))
				.thenReturn(Arrays.asList(entityMap));

		// Test form structure access as the query admin user.
		Assert.assertTrue(filePermissionsService.validateFormStructureAccess(bricsFile, adminAccount, userToken),
				"The query admin user should have access to all form structure files.");
	}

	@Test
	public void testFormStructureAccessAsStandardDictionaryUser() {
		// Create test objects.
		RoleType[] userRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY};
		User user = genUser(1L, "Dictionary", "User", "dictionary.user@nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account userAccount = genAccount(1L, "dictUser", user, userRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		BasicFormStructure formStructure = genBasicFormStructure(1L, "Test Form Structure", StatusType.DRAFT);
		EntityMap entityMap = genEntityMap(1L, formStructure.getId(), EntityType.DATA_STRUCTURE, ownerAccount, null,
				PermissionType.OWNER);
		EntityMap entityMap2 = genEntityMap(2L, formStructure.getId(), EntityType.DATA_STRUCTURE, userAccount, null,
				PermissionType.ADMIN);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.pdf",
				SystemFileCategory.FORM_STRUCTURE_SUPPORTING_DOCUMENTATION, formStructure.getId());
		UserToken userToken = genUserToken(1L, userAccount, userRoles);

		// Stub the repository method calls.
		when(basicFormStructureRepository.findById(bricsFile.getLinkedObjectID()))
				.thenReturn(Optional.of(formStructure));
		when(permissionGroupRepository.findAllByMemberSet_Account(userAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(formStructure.getId(), EntityType.DATA_STRUCTURE))
				.thenReturn(Arrays.asList(entityMap, entityMap2));

		// Test form structure access as a dictionary user.
		Assert.assertTrue(filePermissionsService.validateFormStructureAccess(bricsFile, userAccount, userToken),
				"The dictionary user should have access to the form structure files.");
	}

	@Test
	public void testFormStructureAccessAsStandardProformsUser() {
		// Create test objects.
		RoleType[] userRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_PROFORMS};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY};
		User user = genUser(1L, "Proforms", "User", "proforms.user@nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		PermissionGroup permissionGroup = genPermissionGroup(1L, "Test Permission Group 1");
		Account userAccount = genAccount(1L, "proformsUser", user, userRoles, permissionGroup);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		BasicFormStructure formStructure = genBasicFormStructure(1L, "Test Form Structure", StatusType.DRAFT);
		EntityMap entityMap = genEntityMap(1L, formStructure.getId(), EntityType.DATA_STRUCTURE, ownerAccount, null,
				PermissionType.OWNER);
		EntityMap entityMap2 = genEntityMap(2L, formStructure.getId(), EntityType.DATA_STRUCTURE, null, permissionGroup,
				PermissionType.WRITE);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.pdf",
				SystemFileCategory.FORM_STRUCTURE_SUPPORTING_DOCUMENTATION, formStructure.getId());
		UserToken userToken = genUserToken(1L, userAccount, userRoles);

		// Stub the repository method calls.
		when(basicFormStructureRepository.findById(bricsFile.getLinkedObjectID()))
				.thenReturn(Optional.of(formStructure));
		when(permissionGroupRepository.findAllByMemberSet_Account(userAccount))
				.thenReturn(Arrays.asList(permissionGroup));
		when(entityMapRepository.findAllByEntityIdAndType(formStructure.getId(), EntityType.DATA_STRUCTURE))
				.thenReturn(Arrays.asList(entityMap, entityMap2));

		// Test form structure access as a proforms user.
		Assert.assertTrue(filePermissionsService.validateFormStructureAccess(bricsFile, userAccount, userToken),
				"The proforms user should have access to the form structure files.");
	}

	@Test
	public void testFormStructureAccessAsStandardQueryUser() {
		// Create test objects.
		RoleType[] userRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_QUERY};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY};
		User user = genUser(1L, "Query", "User", "query.user@nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account userAccount = genAccount(1L, "queryUser", user, userRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		BasicFormStructure formStructure = genBasicFormStructure(1L, "Test Form Structure", StatusType.DRAFT);
		EntityMap entityMap = genEntityMap(1L, formStructure.getId(), EntityType.DATA_STRUCTURE, ownerAccount, null,
				PermissionType.OWNER);
		EntityMap entityMap2 = genEntityMap(2L, formStructure.getId(), EntityType.DATA_STRUCTURE, userAccount, null,
				PermissionType.READ);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.pdf",
				SystemFileCategory.FORM_STRUCTURE_SUPPORTING_DOCUMENTATION, formStructure.getId());
		UserToken userToken = genUserToken(1L, userAccount, userRoles);

		// Stub the repository method calls.
		when(basicFormStructureRepository.findById(bricsFile.getLinkedObjectID()))
				.thenReturn(Optional.of(formStructure));
		when(permissionGroupRepository.findAllByMemberSet_Account(userAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(formStructure.getId(), EntityType.DATA_STRUCTURE))
				.thenReturn(Arrays.asList(entityMap, entityMap2));

		// Test form structure access as a query user.
		Assert.assertTrue(filePermissionsService.validateFormStructureAccess(bricsFile, userAccount, userToken),
				"The query user should have access to the form structure files.");
	}

	@Test
	public void testFormStructureAccessAsEformUser() {
		// Create test objects.
		RoleType[] userRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY_EFORM};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY};
		User user = genUser(1L, "Eform", "User", "eform.user@nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account userAccount = genAccount(1L, "eformUser", user, userRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		BasicFormStructure formStructure = genBasicFormStructure(1L, "Test Form Structure", StatusType.DRAFT);
		EntityMap entityMap = genEntityMap(1L, formStructure.getId(), EntityType.DATA_STRUCTURE, ownerAccount, null,
				PermissionType.OWNER);
		EntityMap entityMap2 = genEntityMap(2L, formStructure.getId(), EntityType.DATA_STRUCTURE, userAccount, null,
				PermissionType.READ);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.pdf",
				SystemFileCategory.FORM_STRUCTURE_SUPPORTING_DOCUMENTATION, formStructure.getId());
		UserToken userToken = genUserToken(1L, userAccount, userRoles);

		// Stub the repository method calls.
		when(basicFormStructureRepository.findById(bricsFile.getLinkedObjectID()))
				.thenReturn(Optional.of(formStructure));
		when(permissionGroupRepository.findAllByMemberSet_Account(userAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(formStructure.getId(), EntityType.DATA_STRUCTURE))
				.thenReturn(Arrays.asList(entityMap, entityMap2));

		// Test form structure access as a eform user.
		Assert.assertTrue(filePermissionsService.validateFormStructureAccess(bricsFile, userAccount, userToken),
				"The eform user should have access to the form structure files.");
	}

	@Test
	public void testFormStructureAccessWithPublishedFormStructure() {
		// Create test objects.
		RoleType[] userRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_PROFORMS};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY};
		User user = genUser(1L, "Proforms", "User", "proforms.user@nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account userAccount = genAccount(1L, "proformsUser", user, userRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		BasicFormStructure formStructure = genBasicFormStructure(1L, "Test Form Structure", StatusType.PUBLISHED);
		EntityMap entityMap = genEntityMap(1L, formStructure.getId(), EntityType.DATA_STRUCTURE, ownerAccount, null,
				PermissionType.OWNER);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.pdf",
				SystemFileCategory.FORM_STRUCTURE_SUPPORTING_DOCUMENTATION, formStructure.getId());
		UserToken userToken = genUserToken(1L, userAccount, userRoles);

		// Stub the repository method calls.
		when(basicFormStructureRepository.findById(bricsFile.getLinkedObjectID()))
				.thenReturn(Optional.of(formStructure));
		when(permissionGroupRepository.findAllByMemberSet_Account(userAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(formStructure.getId(), EntityType.DATA_STRUCTURE))
				.thenReturn(Arrays.asList(entityMap));

		// Test form structure access as a proforms user.
		Assert.assertTrue(filePermissionsService.validateFormStructureAccess(bricsFile, userAccount, userToken),
				"The proforms user should have access to the form structure files.");
	}

	@Test
	public void testFormStructureAccessWithWrongPermission() {
		// Create test objects.
		RoleType[] userRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_PROFORMS};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY};
		User user = genUser(1L, "Proforms", "User", "proforms.user@nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account userAccount = genAccount(1L, "proformsUser", user, userRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		BasicFormStructure formStructure = genBasicFormStructure(1L, "Test Form Structure", StatusType.DRAFT);
		EntityMap entityMap = genEntityMap(1L, formStructure.getId(), EntityType.DATA_STRUCTURE, ownerAccount, null,
				PermissionType.OWNER);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.pdf",
				SystemFileCategory.FORM_STRUCTURE_SUPPORTING_DOCUMENTATION, formStructure.getId());
		UserToken userToken = genUserToken(1L, userAccount, userRoles);

		// Stub the repository method calls.
		when(basicFormStructureRepository.findById(bricsFile.getLinkedObjectID()))
				.thenReturn(Optional.of(formStructure));
		when(permissionGroupRepository.findAllByMemberSet_Account(userAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(formStructure.getId(), EntityType.DATA_STRUCTURE))
				.thenReturn(Arrays.asList(entityMap));

		// Test form structure access as a proforms user with no explicit access.
		Assert.assertFalse(filePermissionsService.validateFormStructureAccess(bricsFile, userAccount, userToken),
				"The proforms user shouldn't have access to the form structure files.");
	}

	@Test
	public void testFormStructureAccessWithWrongRole() {
		// Create test objects.
		RoleType[] userRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_GUID};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY};
		User user = genUser(1L, "Guid", "User", "guid.user@nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account userAccount = genAccount(1L, "guidUser", user, userRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		BasicFormStructure formStructure = genBasicFormStructure(1L, "Test Form Structure", StatusType.PUBLISHED);
		EntityMap entityMap = genEntityMap(1L, formStructure.getId(), EntityType.DATA_STRUCTURE, ownerAccount, null,
				PermissionType.OWNER);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.pdf",
				SystemFileCategory.FORM_STRUCTURE_SUPPORTING_DOCUMENTATION, formStructure.getId());
		UserToken userToken = genUserToken(1L, userAccount, userRoles);

		// Stub the repository method calls.
		when(basicFormStructureRepository.findById(bricsFile.getLinkedObjectID()))
				.thenReturn(Optional.of(formStructure));
		when(permissionGroupRepository.findAllByMemberSet_Account(userAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(formStructure.getId(), EntityType.DATA_STRUCTURE))
				.thenReturn(Arrays.asList(entityMap));

		// Test form structure access as a guid user.
		Assert.assertFalse(filePermissionsService.validateFormStructureAccess(bricsFile, userAccount, userToken),
				"The guid user shouldn't have access to the form structure files.");
	}

	/*
	 * @formatter:off
	 * ########################################################################
	 * # Form Structure Access Tests
	 * ########################################################################
	 * @formatter:on
	 */

	@Test
	public void testDataElementAccessAsSysAdmin() {
		// Create test objects.
		RoleType[] adminRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_ADMIN};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY};
		User admin = genUser(1L, "Supreme", "Overload", "god@brics.nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account adminAccount = genAccount(1L, "admin", admin, adminRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		BasicDataElement dataElement = genBasicDataElement(1L, "Test Data Element", DataElementStatus.DRAFT);
		EntityMap entityMap = genEntityMap(1L, dataElement.getId(), EntityType.DATA_ELEMENT, ownerAccount, null,
				PermissionType.OWNER);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.pdf",
				SystemFileCategory.DATA_ELEMENT_SUPPORTING_DOCUMENTATION, dataElement.getId());
		UserToken userToken = genUserToken(1L, adminAccount, adminRoles);

		// Stub the repository method calls.
		when(basicDataElementRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(dataElement));
		when(permissionGroupRepository.findAllByMemberSet_Account(adminAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(dataElement.getId(), EntityType.DATA_ELEMENT))
				.thenReturn(Arrays.asList(entityMap));

		// Test data element access as the system admin user.
		Assert.assertTrue(filePermissionsService.validateDataElementAccess(bricsFile, adminAccount, userToken),
				"The system admin user should have access to all data element files.");
	}

	@Test
	public void testDataElementAccessAsDictionaryAdmin() {
		// Create test objects.
		RoleType[] adminRoles =
				new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY, RoleType.ROLE_DICTIONARY_ADMIN};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY};
		User admin = genUser(1L, "Dictionary", "Admin", "dictionary.admin@nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account adminAccount = genAccount(1L, "dictAdmin", admin, adminRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		BasicDataElement dataElement = genBasicDataElement(1L, "Test Data Element", DataElementStatus.DRAFT);
		EntityMap entityMap = genEntityMap(1L, dataElement.getId(), EntityType.DATA_ELEMENT, ownerAccount, null,
				PermissionType.OWNER);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.pdf",
				SystemFileCategory.DATA_ELEMENT_SUPPORTING_DOCUMENTATION, dataElement.getId());
		UserToken userToken = genUserToken(1L, adminAccount, adminRoles);

		// Stub the repository method calls.
		when(basicDataElementRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(dataElement));
		when(permissionGroupRepository.findAllByMemberSet_Account(adminAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(dataElement.getId(), EntityType.DATA_ELEMENT))
				.thenReturn(Arrays.asList(entityMap));

		// Test data element access as the dictionary admin user.
		Assert.assertTrue(filePermissionsService.validateDataElementAccess(bricsFile, adminAccount, userToken),
				"The dicitionary admin user should have access to all data element files.");
	}

	@Test
	public void testDataElementAccessAsProformsAdmin() {
		// Create test objects.
		RoleType[] adminRoles =
				new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_PROFORMS, RoleType.ROLE_PROFORMS_ADMIN};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY};
		User admin = genUser(1L, "Proforms", "Admin", "proforms.admin@nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account adminAccount = genAccount(1L, "proformsAdmin", admin, adminRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		BasicDataElement dataElement = genBasicDataElement(1L, "Test Data Element", DataElementStatus.DRAFT);
		EntityMap entityMap = genEntityMap(1L, dataElement.getId(), EntityType.DATA_ELEMENT, ownerAccount, null,
				PermissionType.OWNER);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.pdf",
				SystemFileCategory.DATA_ELEMENT_SUPPORTING_DOCUMENTATION, dataElement.getId());
		UserToken userToken = genUserToken(1L, adminAccount, adminRoles);

		// Stub the repository method calls.
		when(basicDataElementRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(dataElement));
		when(permissionGroupRepository.findAllByMemberSet_Account(adminAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(dataElement.getId(), EntityType.DATA_ELEMENT))
				.thenReturn(Arrays.asList(entityMap));

		// Test data element access as the proforms admin user.
		Assert.assertTrue(filePermissionsService.validateDataElementAccess(bricsFile, adminAccount, userToken),
				"The proforms admin user should have access to all data element files.");
	}

	@Test
	public void testDataElementAccessAsClinicalAdmin() {
		// Create test objects.
		RoleType[] adminRoles =
				new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_PROFORMS, RoleType.ROLE_CLINICAL_ADMIN};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY};
		User admin = genUser(1L, "Clinical", "Admin", "clinical.admin@nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account adminAccount = genAccount(1L, "proformsAdmin", admin, adminRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		BasicDataElement dataElement = genBasicDataElement(1L, "Test Data Element", DataElementStatus.DRAFT);
		EntityMap entityMap = genEntityMap(1L, dataElement.getId(), EntityType.DATA_ELEMENT, ownerAccount, null,
				PermissionType.OWNER);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.pdf",
				SystemFileCategory.DATA_ELEMENT_SUPPORTING_DOCUMENTATION, dataElement.getId());
		UserToken userToken = genUserToken(1L, adminAccount, adminRoles);

		// Stub the repository method calls.
		when(basicDataElementRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(dataElement));
		when(permissionGroupRepository.findAllByMemberSet_Account(adminAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(dataElement.getId(), EntityType.DATA_ELEMENT))
				.thenReturn(Arrays.asList(entityMap));

		// Test data element access as the clinical admin user.
		Assert.assertTrue(filePermissionsService.validateDataElementAccess(bricsFile, adminAccount, userToken),
				"The clinical admin user should have access to all data element files.");
	}

	@Test
	public void testDataElementAccessAsQueryAdmin() {
		// Create test objects.
		RoleType[] adminRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_QUERY, RoleType.ROLE_QUERY_ADMIN};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY};
		User admin = genUser(1L, "Query", "Admin", "query.admin@nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account adminAccount = genAccount(1L, "proformsAdmin", admin, adminRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		BasicDataElement dataElement = genBasicDataElement(1L, "Test Data Element", DataElementStatus.DRAFT);
		EntityMap entityMap = genEntityMap(1L, dataElement.getId(), EntityType.DATA_ELEMENT, ownerAccount, null,
				PermissionType.OWNER);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.pdf",
				SystemFileCategory.DATA_ELEMENT_SUPPORTING_DOCUMENTATION, dataElement.getId());
		UserToken userToken = genUserToken(1L, adminAccount, adminRoles);

		// Stub the repository method calls.
		when(basicDataElementRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(dataElement));
		when(permissionGroupRepository.findAllByMemberSet_Account(adminAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(dataElement.getId(), EntityType.DATA_ELEMENT))
				.thenReturn(Arrays.asList(entityMap));

		// Test data element access as the query admin user.
		Assert.assertTrue(filePermissionsService.validateDataElementAccess(bricsFile, adminAccount, userToken),
				"The query admin user should have access to all data element files.");
	}

	@Test
	public void testDataElementAccessAsStandardDictionaryUser() {
		// Create test objects.
		RoleType[] userRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY};
		User user = genUser(1L, "Dictionary", "User", "dictionary.user@nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account userAccount = genAccount(1L, "dictUser", user, userRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		BasicDataElement dataElement = genBasicDataElement(1L, "Test Data Element", DataElementStatus.DRAFT);
		EntityMap entityMap = genEntityMap(1L, dataElement.getId(), EntityType.DATA_ELEMENT, ownerAccount, null,
				PermissionType.ADMIN);
		EntityMap entityMap2 =
				genEntityMap(2L, dataElement.getId(), EntityType.DATA_ELEMENT, userAccount, null, PermissionType.OWNER);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.pdf",
				SystemFileCategory.DATA_ELEMENT_SUPPORTING_DOCUMENTATION, dataElement.getId());
		UserToken userToken = genUserToken(1L, userAccount, userRoles);

		// Stub the repository method calls.
		when(basicDataElementRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(dataElement));
		when(permissionGroupRepository.findAllByMemberSet_Account(userAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(dataElement.getId(), EntityType.DATA_ELEMENT))
				.thenReturn(Arrays.asList(entityMap, entityMap2));

		// Test data element access as a dictionary user.
		Assert.assertTrue(filePermissionsService.validateDataElementAccess(bricsFile, userAccount, userToken),
				"The dictionary user should have access to the data element files.");
	}

	@Test
	public void testDataElementAccessAsStandardProformsUser() {
		// Create test objects.
		RoleType[] userRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_PROFORMS};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY};
		User user = genUser(1L, "Proforms", "User", "proforms.user@nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account userAccount = genAccount(1L, "proformsUser", user, userRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		BasicDataElement dataElement = genBasicDataElement(1L, "Test Data Element", DataElementStatus.DRAFT);
		EntityMap entityMap = genEntityMap(1L, dataElement.getId(), EntityType.DATA_ELEMENT, ownerAccount, null,
				PermissionType.OWNER);
		EntityMap entityMap2 =
				genEntityMap(2L, dataElement.getId(), EntityType.DATA_ELEMENT, userAccount, null, PermissionType.READ);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.pdf",
				SystemFileCategory.DATA_ELEMENT_SUPPORTING_DOCUMENTATION, dataElement.getId());
		UserToken userToken = genUserToken(1L, userAccount, userRoles);

		// Stub the repository method calls.
		when(basicDataElementRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(dataElement));
		when(permissionGroupRepository.findAllByMemberSet_Account(userAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(dataElement.getId(), EntityType.DATA_ELEMENT))
				.thenReturn(Arrays.asList(entityMap, entityMap2));

		// Test data element access as a proforms user.
		Assert.assertTrue(filePermissionsService.validateDataElementAccess(bricsFile, userAccount, userToken),
				"The proforms user should have access to the data element files.");
	}

	@Test
	public void testDataElementAccessAsStandardQueryUser() {
		// Create test objects.
		RoleType[] userRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_QUERY};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY};
		PermissionGroup permissionGroup = genPermissionGroup(1L, "Test Permission Group 1");
		User user = genUser(1L, "Query", "User", "query.user@nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account userAccount = genAccount(1L, "queryUser", user, userRoles, permissionGroup);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		BasicDataElement dataElement = genBasicDataElement(1L, "Test Data Element", DataElementStatus.DRAFT);
		EntityMap entityMap = genEntityMap(1L, dataElement.getId(), EntityType.DATA_ELEMENT, ownerAccount, null,
				PermissionType.OWNER);
		EntityMap entityMap2 = genEntityMap(2L, dataElement.getId(), EntityType.DATA_ELEMENT, null, permissionGroup,
				PermissionType.READ);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.pdf",
				SystemFileCategory.DATA_ELEMENT_SUPPORTING_DOCUMENTATION, dataElement.getId());
		UserToken userToken = genUserToken(1L, userAccount, userRoles);

		// Stub the repository method calls.
		when(basicDataElementRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(dataElement));
		when(permissionGroupRepository.findAllByMemberSet_Account(userAccount))
				.thenReturn(Arrays.asList(permissionGroup));
		when(entityMapRepository.findAllByEntityIdAndType(dataElement.getId(), EntityType.DATA_ELEMENT))
				.thenReturn(Arrays.asList(entityMap, entityMap2));

		// Test data element access as a query user.
		Assert.assertTrue(filePermissionsService.validateDataElementAccess(bricsFile, userAccount, userToken),
				"The query user should have access to the data element files.");
	}

	@Test
	public void testDataElementAccessAsStandardEformUser() {
		// Create test objects.
		RoleType[] userRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY_EFORM};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY};
		User user = genUser(1L, "Eform", "User", "eform.user@nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account userAccount = genAccount(1L, "eformUser", user, userRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		BasicDataElement dataElement = genBasicDataElement(1L, "Test Data Element", DataElementStatus.DRAFT);
		EntityMap entityMap = genEntityMap(1L, dataElement.getId(), EntityType.DATA_ELEMENT, ownerAccount, null,
				PermissionType.OWNER);
		EntityMap entityMap2 =
				genEntityMap(2L, dataElement.getId(), EntityType.DATA_ELEMENT, userAccount, null, PermissionType.READ);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.pdf",
				SystemFileCategory.DATA_ELEMENT_SUPPORTING_DOCUMENTATION, dataElement.getId());
		UserToken userToken = genUserToken(1L, userAccount, userRoles);

		// Stub the repository method calls.
		when(basicDataElementRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(dataElement));
		when(permissionGroupRepository.findAllByMemberSet_Account(userAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(dataElement.getId(), EntityType.DATA_ELEMENT))
				.thenReturn(Arrays.asList(entityMap, entityMap2));

		// Test data element access as a eForm user.
		Assert.assertTrue(filePermissionsService.validateDataElementAccess(bricsFile, userAccount, userToken),
				"The eForm user should have access to the data element files.");
	}

	@Test
	public void testDataElementAccessWithPublishedDataElement() {
		// Create test objects.
		RoleType[] userRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY_EFORM};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY};
		User user = genUser(1L, "Eform", "User", "eform.user@nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account userAccount = genAccount(1L, "eformUser", user, userRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		BasicDataElement dataElement = genBasicDataElement(1L, "Test Data Element", DataElementStatus.PUBLISHED);
		EntityMap entityMap = genEntityMap(1L, dataElement.getId(), EntityType.DATA_ELEMENT, ownerAccount, null,
				PermissionType.OWNER);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.pdf",
				SystemFileCategory.DATA_ELEMENT_SUPPORTING_DOCUMENTATION, dataElement.getId());
		UserToken userToken = genUserToken(1L, userAccount, userRoles);

		// Stub the repository method calls.
		when(basicDataElementRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(dataElement));
		when(permissionGroupRepository.findAllByMemberSet_Account(userAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(dataElement.getId(), EntityType.DATA_ELEMENT))
				.thenReturn(Arrays.asList(entityMap));

		// Test published data element access as a eForm user.
		Assert.assertTrue(filePermissionsService.validateDataElementAccess(bricsFile, userAccount, userToken),
				"The eForm user should have access to files of a published data element.");
	}

	@Test
	public void testDataElementAccessWithWrongPermission() {
		// Create test objects.
		RoleType[] userRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY_EFORM};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY};
		User user = genUser(1L, "Eform", "User", "eform.user@nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account userAccount = genAccount(1L, "eformUser", user, userRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		BasicDataElement dataElement = genBasicDataElement(1L, "Test Data Element", DataElementStatus.DRAFT);
		EntityMap entityMap = genEntityMap(1L, dataElement.getId(), EntityType.DATA_ELEMENT, ownerAccount, null,
				PermissionType.OWNER);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.pdf",
				SystemFileCategory.DATA_ELEMENT_SUPPORTING_DOCUMENTATION, dataElement.getId());
		UserToken userToken = genUserToken(1L, userAccount, userRoles);

		// Stub the repository method calls.
		when(basicDataElementRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(dataElement));
		when(permissionGroupRepository.findAllByMemberSet_Account(userAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(dataElement.getId(), EntityType.DATA_ELEMENT))
				.thenReturn(Arrays.asList(entityMap));

		// Test data element access as a eForm user with no permission to that object.
		Assert.assertFalse(filePermissionsService.validateDataElementAccess(bricsFile, userAccount, userToken),
				"The eForm user shouldn't have access to a data element it doesn't have permissions to.");
	}

	@Test
	public void testDataElementAccessWithWrongRole() {
		// Create test objects.
		RoleType[] userRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_GUID};
		RoleType[] ownerRoles = new RoleType[] {RoleType.ROLE_USER, RoleType.ROLE_DICTIONARY};
		User user = genUser(1L, "Guid", "User", "guid.user@nih.gov");
		User owner = genUser(2L, "Researcher", "One", "rOne@institute.edu");
		Account userAccount = genAccount(1L, "guidUser", user, userRoles, null);
		Account ownerAccount = genAccount(2L, "rone", owner, ownerRoles, null);
		BasicDataElement dataElement = genBasicDataElement(1L, "Test Data Element", DataElementStatus.PUBLISHED);
		EntityMap entityMap = genEntityMap(1L, dataElement.getId(), EntityType.DATA_ELEMENT, ownerAccount, null,
				PermissionType.OWNER);
		BricsFile bricsFile = new BricsFile("UUID001", "testFile.pdf",
				SystemFileCategory.DATA_ELEMENT_SUPPORTING_DOCUMENTATION, dataElement.getId());
		UserToken userToken = genUserToken(1L, userAccount, userRoles);

		// Stub the repository method calls.
		when(basicDataElementRepository.findById(bricsFile.getLinkedObjectID())).thenReturn(Optional.of(dataElement));
		when(permissionGroupRepository.findAllByMemberSet_Account(userAccount)).thenReturn(Collections.emptyList());
		when(entityMapRepository.findAllByEntityIdAndType(dataElement.getId(), EntityType.DATA_ELEMENT))
				.thenReturn(Arrays.asList(entityMap));

		// Test published data element access as a guid user.
		Assert.assertFalse(filePermissionsService.validateDataElementAccess(bricsFile, userAccount, userToken),
				"The guid user shouldn't have access to an object that it doesn't have right role for.");
	}

}

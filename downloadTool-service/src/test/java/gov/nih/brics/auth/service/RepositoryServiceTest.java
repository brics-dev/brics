package gov.nih.brics.auth.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import gov.nih.brics.downloadtool.data.repository.DownloadPackageRepository;
import gov.nih.brics.downloadtool.data.repository.DownloadableRepository;
import gov.nih.brics.downloadtool.exception.UnauthorizedActionException;
import gov.nih.brics.downloadtool.security.jwt.ClaimsOnlyTokenProvider;
import gov.nih.brics.downloadtool.service.RepositoryServiceImpl;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountRole;
import gov.nih.tbi.commons.model.AccountStatus;
import gov.nih.tbi.commons.model.FileClassification;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.hibernate.FileType;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.repository.model.DownloadPackageOrigin;
import gov.nih.tbi.repository.model.SubmissionType;
import gov.nih.tbi.repository.model.hibernate.DownloadPackage;
import gov.nih.tbi.repository.model.hibernate.Downloadable;
import gov.nih.tbi.repository.model.hibernate.QueryToolDownloadFile;
import gov.nih.tbi.repository.model.hibernate.UserFile;

public class RepositoryServiceTest {

	@Mock
	ClaimsOnlyTokenProvider tokenProvider;
	
	@Mock
	DownloadPackageRepository downloadPackageRepository = Mockito.mock(DownloadPackageRepository.class);
	
	@Mock
	DownloadableRepository downloadableRepository = Mockito.mock(DownloadableRepository.class);
	
	@InjectMocks
	RepositoryServiceImpl repositoryService;
	
	@BeforeMethod
	public void setup() {
		Mockito.reset(downloadPackageRepository);
		MockitoAnnotations.initMocks(this);
	}
	
	@Test
	public void testRemoveFromQueue() throws UnauthorizedActionException {
		List<Long> idsToDelete = new ArrayList<>();
		idsToDelete.add(2L);
		
		List<DownloadPackage> packagesToDelete = new ArrayList<>();
		DownloadPackage packageToDelete = createDbDownloadPackage();
		packagesToDelete.add(packageToDelete);
		
		List<Long> packageIdsToDelete = new ArrayList<>();
		packageIdsToDelete.add(packageToDelete.getId());
		Account account = validAccount();
		
		when(downloadPackageRepository.findDistinctByUser(account.getUser())).thenReturn(packagesToDelete);
		when(downloadPackageRepository.findByDownloadablesIsEmpty()).thenReturn(packagesToDelete);
		
		repositoryService.removeFromQueue(account, idsToDelete);
		
		verify(downloadableRepository).deleteAllByIds(idsToDelete);
		verify(downloadPackageRepository).deleteByIds(packageIdsToDelete);
	}
	
	@Test(expectedExceptions = UnauthorizedActionException.class)
	public void testRemoveWhereNotAuthorized() throws UnauthorizedActionException {
		List<Long> idsToDelete = new ArrayList<>();
		idsToDelete.add(1L);
		List<DownloadPackage> packagesToDelete = new ArrayList<>();
		packagesToDelete.add(createDbDownloadPackage());
		Account account = validAccount();
		
		when(downloadPackageRepository.findDistinctByUser(account.getUser())).thenReturn(packagesToDelete);
		when(downloadPackageRepository.findByDownloadablesIsEmpty()).thenReturn(new ArrayList<DownloadPackage>());
		
		repositoryService.removeFromQueue(account, idsToDelete);
		
		verify(downloadableRepository, never()).deleteAllByIds(idsToDelete);
		verify(downloadPackageRepository, never()).deleteByIds(anyList());
	}
	
	@Test
	public void testUserOwnsThesePositive() {
		List<DownloadPackage> packages = new ArrayList<>();
		packages.add(createDbDownloadPackage());
		when(downloadPackageRepository.findDistinctByUser(any(User.class))).thenReturn(packages);
		
		List<Long> inputIds = new ArrayList<>();
		inputIds.add(2L);
		Account account = validAccount();
		
		boolean testThis = repositoryService.doesUserOwnThese(account, inputIds);
		assertThat(testThis).isEqualTo(true);
	}
	
	@Test
	public void testUserOwnsTheseNegative() {
		List<DownloadPackage> packages = new ArrayList<>();
		packages.add(createDbDownloadPackage());
		when(downloadPackageRepository.findDistinctByUser(any(User.class))).thenReturn(packages);
		
		List<Long> inputIds = new ArrayList<>();
		inputIds.add(3L); // an invalid ID
		Account account = validAccount();
		
		boolean testThis = repositoryService.doesUserOwnThese(account, inputIds);
		assertThat(testThis).isEqualTo(false);
	}
	
	private DownloadPackage createDbDownloadPackage() {
		DownloadPackage pkg = new DownloadPackage();
		pkg.setDateAdded(new Date());
		pkg.setId(1L);
		pkg.setName("package name");
		pkg.setOrigin(DownloadPackageOrigin.QUERY_TOOL);
		pkg.setUser(createUser());
		Set<Downloadable> downloadables = new HashSet<>();
		downloadables.add(createDbDownloadable());
		pkg.setDownloadables(downloadables);
		return pkg;
	}
	
	private Downloadable createDbDownloadable() {
		Downloadable downloadable = new QueryToolDownloadFile();
		downloadable.setId(2L);
		downloadable.setType(SubmissionType.DATA_FILE);
		
		downloadable.setUserFile(createDbUserFile());
		return downloadable;
	}
	
	private UserFile createDbUserFile() {
		UserFile uf = new UserFile();
		uf.setUserId(3L);
		uf.setUploadedDate(new Date());
		uf.setSize(1L);
		uf.setPath("/file/path");
		uf.setName("file name");
		uf.setId(4L);
		
		FileType fileType = new FileType();
		fileType.setFileClassification(FileClassification.DATASET_DATA);
		fileType.setId(5L);
		fileType.setIsActive(true);
		fileType.setName("file type name");
		uf.setFileType(fileType);
		uf.setDescription("user file description");
		
		return uf;
	}
	
	public User createUser() {
		User user = new User();
		user.setFirstName("first");
		user.setLastName("last");
		user.setMiddleName("middle");
		user.setId(2L);
		return user;
	}
	
	public Account validAccount() {
		Account account = new Account();
		
		User user = new User();
		user.setFirstName("first");
		user.setLastName("last");
		user.setMiddleName("middle");
		user.setId(2L);
		
		Set<AccountRole> roles = new HashSet<AccountRole>();
		roles.add(new AccountRole(account, RoleType.ROLE_DICTIONARY, RoleStatus.ACTIVE, new Date()));
		roles.add(new AccountRole(account, RoleType.ROLE_ADMIN, RoleStatus.ACTIVE, new Date()));
		
		account.setUserName("user");
		account.setAffiliatedInstitution("PDBP");
		account.setId(1L);
		account.setUser(user);
		account.setAccountRoleList(roles);
		account.setAccountStatus(AccountStatus.ACTIVE);
		account.setIsActive(true);
		account.setIsLocked(false);
		account.setSalt("abc");
		
		return account;
	}
}

package gov.nih.brics.file.service;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import gov.nih.cit.brics.file.data.repository.meta.BricsFileRepository;
import gov.nih.cit.brics.file.exception.LegacyFileAccessException;
import gov.nih.cit.brics.file.service.FileRepositoryService;
import gov.nih.cit.brics.file.util.FileRepositoryConstants;
import gov.nih.tbi.file.model.SystemFileCategory;
import gov.nih.tbi.file.model.hibernate.BricsFile;

public class FileRepositoryServiceTest {

	private static final int FILE_ID_LENGTH = 32;
	private static final String FILE_ROOT_DIR = "/file-repo";

	@Mock
	private FileRepositoryConstants fileRepositoryConstants = mock(FileRepositoryConstants.class);

	@Mock
	private BricsFileRepository bricsFileRepository = mock(BricsFileRepository.class);

	@InjectMocks
	private FileRepositoryService fileRepositoryService;

	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void generateFileIdTest() {
		String fileId = null;

		// Stub method calls.
		when(fileRepositoryConstants.getFileIdLength()).thenReturn(FILE_ID_LENGTH);
		when(bricsFileRepository.existsById(anyString())).thenReturn(Boolean.FALSE);

		// Test if a 32 character string is returned.
		fileId = fileRepositoryService.generateFileId();
		Assert.assertTrue(fileId != null && fileId.length() == FILE_ID_LENGTH,
				String.format("The file ID is not %1$d characters in length.", FILE_ID_LENGTH));
	}

	@Test
	public void getPathToFileTest() {
		// Create test objects.
		BricsFile bricsFile = new BricsFile("UUID001", "test.txt", SystemFileCategory.DATASET, 234L);
		String expectedPath = FILE_ROOT_DIR + File.separator + bricsFile.getFileCategory().getDirectoryName()
				+ File.separator + bricsFile.getLinkedObjectID() + File.separator + bricsFile.getId()
				+ FileRepositoryService.BRICS_DEFAULT_FILE_EXTENSION;

		// Stub method calls.
		when(fileRepositoryConstants.getFileShareRoot()).thenReturn(FILE_ROOT_DIR);

		// Test the path generation method.
		String genPath = fileRepositoryService.getPathToFile(bricsFile);
		Assert.assertEquals(genPath, expectedPath, "Didn't generate the file path correctly.");
	}

	@Test
	public void getSystemFileTest() throws LegacyFileAccessException {
		// Create test objects.
		BricsFile bricsFile = new BricsFile("UUID002", "test.txt", SystemFileCategory.DATASET, 234L);

		// Stub method calls.
		when(fileRepositoryConstants.getFileShareRoot()).thenReturn(FILE_ROOT_DIR);

		// Test if a File object is returned.
		File file = fileRepositoryService.getSystemFile(bricsFile);
		Assert.assertNotNull(file, "No File object was returned.");
	}

	@Test
	public void getRelativeFilePathTest() {
		// Create test objects.
		BricsFile bricsFile = new BricsFile("UUID003", "test.txt", SystemFileCategory.DATASET, 234L);
		String testPath = FILE_ROOT_DIR + File.separator + bricsFile.getFileCategory().getDirectoryName()
				+ File.separator + bricsFile.getLinkedObjectID() + File.separator + bricsFile.getId()
				+ FileRepositoryService.BRICS_DEFAULT_FILE_EXTENSION;
		String expectedPath = File.separator + bricsFile.getFileCategory().getDirectoryName() + File.separator
				+ bricsFile.getLinkedObjectID() + File.separator + bricsFile.getId()
				+ FileRepositoryService.BRICS_DEFAULT_FILE_EXTENSION;
		File testFile = new File(testPath);

		// Stub method calls.
		when(fileRepositoryConstants.getFileShareRoot()).thenReturn(FILE_ROOT_DIR);

		// Test if the the correct relative path is returned.
		String genPath = fileRepositoryService.getRelativeFilePath(testFile);
		Assert.assertEquals(genPath, expectedPath, "The relative path was not generated correctly.");
	}

}

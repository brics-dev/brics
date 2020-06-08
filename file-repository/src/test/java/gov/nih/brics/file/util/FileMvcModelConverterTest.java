package gov.nih.brics.file.util;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import gov.nih.cit.brics.file.mvc.model.swagger.FileDetails;
import gov.nih.cit.brics.file.service.FileRepositoryService;
import gov.nih.cit.brics.file.util.FileMvcModelConverter;
import gov.nih.cit.brics.file.util.FileRepositoryConstants;
import gov.nih.tbi.file.model.SystemFileCategory;
import gov.nih.tbi.file.model.hibernate.BricsFile;

public class FileMvcModelConverterTest {
	private static final String FILE_ROOT_DIR = "/file-repo";

	@Mock
	private FileRepositoryService fileRepositoryService = mock(FileRepositoryService.class);

	@InjectMocks
	private FileMvcModelConverter fileMvcModelConverter;

	@BeforeMethod
	public void init() {
		MockitoAnnotations.initMocks(this);
	}

	@Test
	public void bricsFileToFileDetailsTest() {
		// Create test objects.
		BricsFile bricsFile = new BricsFile("UUID001", "test.txt", SystemFileCategory.DATASET, 234L);
		String testPath = FILE_ROOT_DIR + File.separator + bricsFile.getFileCategory().getDirectoryName()
				+ File.separator + bricsFile.getLinkedObjectID() + File.separator + bricsFile.getId()
				+ FileRepositoryService.BRICS_DEFAULT_FILE_EXTENSION;
		String relPath = File.separator + bricsFile.getFileCategory().getDirectoryName() + File.separator
				+ bricsFile.getLinkedObjectID() + File.separator + bricsFile.getId()
				+ FileRepositoryService.BRICS_DEFAULT_FILE_EXTENSION;
		File testFile = new File(testPath);

		// Stub method calls.
		when(fileRepositoryService.getRelativeFilePath(testFile)).thenReturn(relPath);

		// Test if the correct file details object is returned.
		FileDetails fileDetails = fileMvcModelConverter.bricsFileToFileDetails(bricsFile, testFile);

		Assert.assertEquals(fileDetails.getFileId(), bricsFile.getId(), "The file IDs don't match.");
		Assert.assertEquals(fileDetails.getFileCategoryId(), bricsFile.getFileCategory().getId(),
				"The file category IDs don't match.");
		Assert.assertEquals(fileDetails.getLinkedObjectId(), bricsFile.getLinkedObjectID(),
				"The linked object IDs don't match.");
		Assert.assertEquals(fileDetails.getFileName(), bricsFile.getFileName(), "The file names don't match.");
		Assert.assertEquals(fileDetails.getFileUrl(), FileRepositoryConstants.FILE_API_ROOT + bricsFile.getId(),
				"The file URLs don't match.");
		Assert.assertEquals(fileDetails.getFilePath(), relPath, "The file paths don't match.");
		Assert.assertEquals(fileDetails.getFileSize(), Long.valueOf(0L), "The file size is invalid.");
	}

}

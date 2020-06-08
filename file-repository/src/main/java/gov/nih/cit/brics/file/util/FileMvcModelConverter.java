package gov.nih.cit.brics.file.util;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import gov.nih.cit.brics.file.mvc.model.swagger.FileDetails;
import gov.nih.cit.brics.file.service.FileRepositoryService;
import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.file.model.hibernate.BricsFile;

@Component
@SessionScope
public class FileMvcModelConverter {

	@Autowired
	private FileRepositoryService fileRepositoryService;

	public FileMvcModelConverter() {}

	/**
	 * Creates a new FileDetails object from the info of the passed in BricsFile and File objects.
	 * 
	 * @param bricsFile - Contains file details that is stored in the database.
	 * @param systemFile - Handle to the actual file on the server's file system.
	 * @return A new FileDetails object constructed from the data of the given BricsFile and File objects.
	 */
	public FileDetails bricsFileToFileDetails(BricsFile bricsFile, File systemFile) {
		FileDetails fileDetails = new FileDetails();
		String relativePath = ModelConstants.EMPTY_STRING;
		long fileSize = 0L;

		// Check if the system file was passed in.
		if (systemFile != null) {
			relativePath = fileRepositoryService.getRelativeFilePath(systemFile);
			fileSize = systemFile.length();
		}

		// Populate the new FileDetails object.
		fileDetails.setFileId(bricsFile.getId());
		fileDetails.setFileCategoryId(bricsFile.getFileCategory().getId());
		fileDetails.setLinkedObjectId(bricsFile.getLinkedObjectID());
		fileDetails.setFileName(bricsFile.getFileName());
		fileDetails.setFileUrl(FileRepositoryConstants.FILE_API_ROOT + bricsFile.getId());
		fileDetails.setFilePath(relativePath);
		fileDetails.setFileSize(fileSize);

		return fileDetails;
	}

}

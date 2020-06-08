package gov.nih.cit.brics.file.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import gov.nih.cit.brics.file.data.repository.meta.BricsFileRepository;
import gov.nih.cit.brics.file.data.repository.meta.UserFileRepository;
import gov.nih.cit.brics.file.exception.LegacyFileAccessException;
import gov.nih.cit.brics.file.util.FileRepositoryConstants;
import gov.nih.tbi.file.model.hibernate.BricsFile;
import gov.nih.tbi.repository.model.hibernate.UserFile;

@Service
@SessionScope
public class FileRepositoryService {
	private static final Logger logger = LoggerFactory.getLogger(FileRepositoryService.class);

	public static final String BRICS_DEFAULT_FILE_EXTENSION = ".blob";

	@Autowired
	private FileRepositoryConstants fileRepositoryConstants;

	@Autowired
	private BricsFileRepository bricsFileRepository;

	@Autowired
	private UserFileRepository userFileRepository;

	public FileRepositoryService() {}

	/**
	 * Generates a random file ID, then compares it to the existing file IDs in the database. If the generated file ID
	 * already exists in the database, a new one will be generated until a unique file ID is created.
	 * 
	 * @return A randomly generated string that is not used as a file ID in the database.
	 */
	public String generateFileId() {
		String newFileId = null;

		do {
			newFileId = RandomStringUtils.randomAlphanumeric(fileRepositoryConstants.getFileIdLength());
		} while (bricsFileRepository.existsById(newFileId));

		return newFileId;
	}

	/**
	 * Construct a Java File object pointing to the system file referenced by the passed in BricsFile object. The
	 * testing of existence is left up to the caller, like in cases where a new file will need to be created.
	 * 
	 * @param bricsFile - Used to construct the path to the system file.
	 * @return A Java File object pointing to the system file referenced by the given BricsFile object.
	 * @throws LegacyFileAccessException When there is an error while migrating the legacy user file to the new
	 *         storage format.
	 */
	public File getSystemFile(BricsFile bricsFile) throws LegacyFileAccessException {
		String filePath = getPathToFile(bricsFile);

		logger.info("Getting a handle to a file at: {}", filePath);

		File file = new File(filePath);

		// Check if the parent directories will need to be created.
		File parentDir = file.getParentFile();

		if (parentDir != null && !parentDir.exists()) {
			parentDir.mkdirs();
		}

		// Check if a legacy file reference will need to be returned.
		if (!file.exists() && bricsFile.getLegacyUserFileId() != null) {
			try {
				UserFile userFile = userFileRepository.findById(bricsFile.getLegacyUserFileId()).get();
				String legacyFilePath = fileRepositoryConstants.getLegacyFileShareRoot() + File.separator
						+ userFile.getPath() + userFile.getName();
				
				logger.info("Getting a handle to the legacy file at: {}.", legacyFilePath);
				
				file = new File(legacyFilePath);
				
				// Check if the file exists and is a file and it is readable.
				if (!file.isFile()) {
					String msg = String.format(
							"The legacy file (UserFile ID: %1$s) at \"%2$s\" either doesn't exist or is not a file.",
							userFile.getId(), legacyFilePath);
					throw new LegacyFileAccessException(msg);
				} else if (!file.canRead()) {
					String msg = String.format("The legacy file (UserFile ID: %1$s) at \"%2$s\" is not readable.",
							userFile.getId(), legacyFilePath);
					throw new LegacyFileAccessException(msg);
				}
			} catch (NoSuchElementException e) {
				String msg = String.format(
						"Couldn't find a user file object for the user file ID %1$d for the BRICS file ID %2$s.",
						bricsFile.getLegacyUserFileId(), bricsFile.getId());
				throw new LegacyFileAccessException(msg);
			}
		}

		return file;
	}

	/**
	 * Constructs the path to the file that is stored on the server's file system from the passed in BricsFile object.
	 * The primary used as part of the {@link #getSystemFile(BricsFile)} method.
	 * 
	 * @param bricsFile - Used to construct the path to the file.
	 * @return The path to the file referenced by the given BricsFile object.
	 */
	public String getPathToFile(BricsFile bricsFile) {
		return fileRepositoryConstants.getFileShareRoot() + File.separator
				+ bricsFile.getFileCategory().getDirectoryName() + File.separator + bricsFile.getLinkedObjectID()
				+ File.separator + bricsFile.getId() + BRICS_DEFAULT_FILE_EXTENSION;
	}

	/**
	 * Generate the relative file system path to the given file, which just strips out the path to the file share root
	 * directory.
	 * 
	 * @param file - The file object containing the path to the system file that needs to be shortened.
	 * @return The relative path of the given file.
	 */
	public String getRelativeFilePath(File file) {
		return file.getAbsolutePath().substring(fileRepositoryConstants.getFileShareRoot().length());
	}

	/**
	 * Handles the migration of the user file from the SFTP location to the new storage location and format used by
	 * Brics file. The old user file will be replaced by a symbolic link to the new location used by the this file
	 * repository web service application.
	 * 
	 * @param newFile - The location of the new file used by this file repository application.
	 * @param bricsFile - The brics file that stores meta data for the files managed by this application.
	 * @throws LegacyFileAccessException When there is an error transferring the file data from the old SFTP location
	 *         to the new one used by this application.
	 */
	public void migrateLegacyUserFile(File newFile, BricsFile bricsFile) throws LegacyFileAccessException {
		logger.info("Migrating user file to brics file: {} ...", bricsFile.getId());

		try {
			UserFile userFile = userFileRepository.findById(bricsFile.getLegacyUserFileId()).get();

			// Check to see if there is already a linkage to a brics file object.
			if (userFile.getBricsFile() != null) {
				// Verify that the referenced BricsFile objects are the same.
				if (!userFile.getBricsFile().equals(bricsFile)) {
					logger.info(
							"The user file ({}) already has a reference to a BricsFile object, but it is not up to date. Updating the reference and skip the migration.",
							userFile.getId());

					// Update the brics file reference in user file object and persist changes to the database.
					userFile.setBricsFile(bricsFile);
					userFileRepository.save(userFile);
				} else {
					logger.info(
							"The user file ({}) already has a reference to a BricsFile object. Skipping the migration.",
							userFile.getId());
				}

				return;
			}

			// Get a handle to the legacy file on the server's file system.
			String legacyFilePath =
					fileRepositoryConstants.getLegacyFileShareRoot() + userFile.getPath() + userFile.getName();
			File legacyFile = new File(legacyFilePath);

			// Check if the legacy file is not accessible.
			if (!legacyFile.canWrite()) {
				String errorMsg = String.format("The legacy user file (%1$d) at \"%2$s\" is not accessable.",
						userFile.getId(), legacyFilePath);
				throw new LegacyFileAccessException(errorMsg);
			}

			// Move the legacy file to the new storage location.
			Files.move(legacyFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

			// Create a symbolic link to the relocated file at the old file location.
			Files.createSymbolicLink(legacyFile.toPath(), newFile.toPath());

			logger.info("Linking Brics file ({}) with user file ({}).", bricsFile.getId(), userFile.getId());

			// Add reference to the brics file object in the user file object, and persist to the database.
			userFile.setBricsFile(bricsFile);
			userFileRepository.save(userFile);

			logger.info("Syncing Brics File data ({}) with user file data ({}).", bricsFile.getId(), userFile.getId());

			// Sync brics file data with the user file data, and persist the changes to the database.
			bricsFile.setFileName(userFile.getName());
			bricsFile.setUploadedBy(userFile.getUserId());

			Date uploadDate = userFile.getUploadedDate();

			if (uploadDate != null) {
				LocalDateTime date = LocalDateTime.ofInstant(uploadDate.toInstant(), ZoneId.systemDefault());
				bricsFile.setUploadedDate(date);
			}

			bricsFileRepository.save(bricsFile);
		} catch (NoSuchElementException e) {
			String msg = String.format(
					"Couldn't find a user file object for the user file ID %1$d for the BRICS file ID %2$s.",
					bricsFile.getLegacyUserFileId(), bricsFile.getId());
			throw new LegacyFileAccessException(msg);
		} catch (IOException | UnsupportedOperationException | SecurityException e) {
			String msg = String.format(
					"A file system error occurred while trying to migrate user file (%1$d) to BRICS file (%2$s)",
					bricsFile.getLegacyUserFileId(), bricsFile.getId());
			throw new LegacyFileAccessException(msg, e);
		}
	}

}

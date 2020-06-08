package gov.nih.cit.brics.file.mvc.controller;

import java.io.File;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;

import gov.nih.cit.brics.file.data.repository.meta.BricsFileRepository;
import gov.nih.cit.brics.file.exception.LegacyFileAccessException;
import gov.nih.cit.brics.file.mvc.controller.swagger.QueryApi;
import gov.nih.cit.brics.file.mvc.model.swagger.FileDetails;
import gov.nih.cit.brics.file.service.FileRepositoryService;
import gov.nih.cit.brics.file.util.FileMvcModelConverter;
import gov.nih.tbi.file.model.hibernate.BricsFile;

@RestController
@RequestScope
public class QueryApiController extends BaseFileRestController implements QueryApi {
	private static final Logger logger = LoggerFactory.getLogger(QueryApiController.class);

	@Autowired
	private BricsFileRepository bricsFileRepository;

	@Autowired
	private FileRepositoryService fileRepositoryService;

	@Autowired
	private FileMvcModelConverter fileMvcModelConverter;

	@Override
	public ResponseEntity<FileDetails> getFileDetails(String fileId) {
		FileDetails response = null;

		try {
			BricsFile bricsFile = bricsFileRepository.findById(fileId).get();

			// Check file permissions before continuing.
			if (checkUserPermissions(bricsFile)) {
				File sysFile = fileRepositoryService.getSystemFile(bricsFile);

				// Check if the file exists on the server's file system.
				if (!sysFile.isFile()) {
					logger.error("The file with ID of \"{}\" could not be found on the server's file system.", fileId);
					return ResponseEntity.notFound().build();
				}

				// Create the response object.
				response = fileMvcModelConverter.bricsFileToFileDetails(bricsFile, sysFile);
			} else {
				logger.error("The {} user cannot access BricsFile {}.", getAccount().getUserName(), bricsFile.getId());
				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
			}
		} catch (NoSuchElementException e) {
			logger.error("The given file ID of \"{}\" cannot be found in the database.", fileId);
			return ResponseEntity.notFound().build();
		} catch (SecurityException e) {
			logger.error("Access violation occurred when accessing the file on the server's file system.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (LegacyFileAccessException e) {
			logger.error("Error occurred when accessing the legacy file from the data drop share.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

		return ResponseEntity.ok(response);
	}

	@Override
	public ResponseEntity<String> getFileSize(String fileId) {
		String fileSize = null;

		try {
			BricsFile bricsFile = bricsFileRepository.findById(fileId).get();

			// Check file permissions before continuing.
			if (checkUserPermissions(bricsFile)) {
				File sysFile = fileRepositoryService.getSystemFile(bricsFile);

				// Check if the file exists on the server's file system.
				if (!sysFile.isFile()) {
					logger.error("The file with ID of \"{}\" could not be found on the server's file system.", fileId);
					return ResponseEntity.notFound().build();
				}

				// Create the response object.
				fileSize = Long.toString(sysFile.length());
			} else {
				logger.error("The {} user cannot access BricsFile {}.", getAccount().getUserName(), bricsFile.getId());
				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
			}
		} catch (NoSuchElementException e) {
			logger.error("The given file ID of \"{}\" cannot be found in the database.", fileId);
			return ResponseEntity.notFound().build();
		} catch (SecurityException e) {
			logger.error("Access violation occurred when accessing the file on the server's file system.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (LegacyFileAccessException e) {
			logger.error("Error occurred when accessing the legacy file from the data drop share.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

		return ResponseEntity.ok(fileSize);
	}

}

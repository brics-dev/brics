package gov.nih.cit.brics.file.mvc.controller;

import java.io.File;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.annotation.RequestScope;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import gov.nih.cit.brics.file.data.repository.meta.BricsFileRepository;
import gov.nih.cit.brics.file.data.repository.meta.UserFileRepository;
import gov.nih.cit.brics.file.exception.HttpRangeOutOfBoundsException;
import gov.nih.cit.brics.file.exception.LegacyFileAccessException;
import gov.nih.cit.brics.file.mvc.controller.swagger.FilesApi;
import gov.nih.cit.brics.file.mvc.model.FileStreamingResponseBody;
import gov.nih.cit.brics.file.mvc.model.PartialFileStreamingResponseBody;
import gov.nih.cit.brics.file.mvc.model.swagger.FileDetails;
import gov.nih.cit.brics.file.mvc.model.swagger.FileUploadDetails;
import gov.nih.cit.brics.file.service.FileRepositoryService;
import gov.nih.cit.brics.file.util.FileMvcModelConverter;
import gov.nih.cit.brics.file.util.FileRepositoryConstants;
import gov.nih.tbi.file.model.SystemFileCategory;
import gov.nih.tbi.file.model.hibernate.BricsFile;
import gov.nih.tbi.repository.model.hibernate.UserFile;

@RestController
@RequestScope
public class FilesApiController extends BaseFileRestController implements FilesApi {
	private static final Logger logger = LoggerFactory.getLogger(FilesApiController.class);
	private static final HttpHeaders DEFAULT_HEADERS = new HttpHeaders();
	private static final HttpHeaders OPTION_HEADERS = new HttpHeaders();

	static {
		// Add the default header values.
		DEFAULT_HEADERS.add(HttpHeaders.PRAGMA, "no-cache");
		DEFAULT_HEADERS.add(HttpHeaders.CACHE_CONTROL, "no-store, no-cache, must-revalidate");
		DEFAULT_HEADERS.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"files.json\"");
		DEFAULT_HEADERS.add("X-Content-Type-Options", "nosniff");
		DEFAULT_HEADERS.add(HttpHeaders.ACCEPT_RANGES, "bytes");
		DEFAULT_HEADERS.add(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "*");
		DEFAULT_HEADERS.add(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "OPTIONS, HEAD, GET, POST, PUT, PATCH, DELETE");
		DEFAULT_HEADERS.add(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
				"Content-Type, Content-Range, Content-Disposition, Range");
		DEFAULT_HEADERS.add(HttpHeaders.VARY, "Accept");

		// Add values for the Option response headers.
		OPTION_HEADERS.addAll(DEFAULT_HEADERS);
		OPTION_HEADERS.add(HttpHeaders.CONTENT_LENGTH, "0");
		OPTION_HEADERS.add(HttpHeaders.ALLOW, "OPTIONS, HEAD, GET, POST, DELETE");
	}

	@Autowired
	private FileRepositoryService fileRepositoryService;

	@Autowired
	private BricsFileRepository bricsFileRepository;

	@Autowired
	private UserFileRepository userFileRepository;

	@Autowired
	private FileRepositoryConstants fileRepositoryConstants;

	@Autowired
	private FileMvcModelConverter fileMvcModelConverter;

	private MimetypesFileTypeMap mimeTypeResolver;

	/**
	 * Default FilesApiController constructor.
	 */
	public FilesApiController() {
		super();
		this.mimeTypeResolver = new MimetypesFileTypeMap();
	}

	@Override
	public ResponseEntity<FileDetails> deleteFile(String fileId) {
		FileDetails response = null;

		try {
			BricsFile fileRecord = bricsFileRepository.findById(fileId).get();

			// Check permissions to the file before continuing.
			if (checkUserPermissions(fileRecord)) {
				File sysFile = fileRepositoryService.getSystemFile(fileRecord);

				// Delete the file data that is stored on the server's file system, if able.
				if (sysFile.exists()) {
					Path sysFilePath = sysFile.toPath();
					Files.delete(sysFilePath);
				}

				// Delete the file record from the database.
				bricsFileRepository.deleteById(fileId);
				logger.info("Deleted both file data and BricsFile record denoted by fileId: {}", fileId);

				// Create the response.
				response = fileMvcModelConverter.bricsFileToFileDetails(fileRecord, null);
			} else {
				logger.error("The {} user cannot access BricsFile {}.", getAccount().getUserName(), fileRecord.getId());
				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
			}
		} catch (NoSuchElementException e) {
			logger.error("No BricsFile record found for fileId: " + fileId, e);
			return ResponseEntity.notFound().build();
		} catch (DataAccessException e) {
			logger.error("A database error occurred while deleting a record referenced by fileId: " + fileId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (IOException | SecurityException e) {
			logger.error("File system error occured while deleting the file refernced by fileId: " + fileId, e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (LegacyFileAccessException e) {
			logger.error("Error occurred when accessing the legacy file from the data drop share.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

		return ResponseEntity.ok(response);
	}

	@Override
	public ResponseEntity<StreamingResponseBody> downloadLegacyFile(Long userFileId, String range) {
		StreamingResponseBody response = null;
		HttpHeaders headers = new HttpHeaders();
		HttpStatus httpStatus = HttpStatus.OK;
		File sysFile = null;

		try {
			UserFile userFile = userFileRepository.findById(userFileId).get();
			String userFilePath = fileRepositoryConstants.getLegacyFileShareRoot() + File.separator + userFile.getPath()
					+ userFile.getName();

			sysFile = new File(userFilePath);

			// Check if the file exists in the server's file system, and is a regular file.
			if (!sysFile.isFile()) {
				logger.error("The file (\"{}\") could not be found on in the file system. Path is \"{}\".", userFileId,
						sysFile.getAbsolutePath());
				return ResponseEntity.notFound().build();
			}

			logger.info("A download has been requested for userFileId: {}.", userFileId);

			// Check if a "Range" HTTP header was given. If it does, process the request as a partial download.
			if (StringUtils.isNotBlank(range)) {
				// ++++++ Begin Range header parsing... ++++++
				long startRange = 0L;
				long endRange = sysFile.length() - 1L;
				String rangeDef = range.split("=")[1].trim();

				// Check if there are multiple ranges.
				if (rangeDef.contains(",")) {
					logger.error("The given range ({}) contains mulitple ranges, which is not supported.", range);
					return ResponseEntity.badRequest().build();
				}

				String[] rangeStrArray = rangeDef.split("-", -1);

				// Set the start and end range points.
				if (StringUtils.isNotBlank(rangeStrArray[0]) && StringUtils.isNotBlank(rangeStrArray[1])) {
					// A standard range was specified.
					startRange = Long.valueOf(rangeStrArray[0]);
					endRange = Long.valueOf(rangeStrArray[1]);
				} else if (StringUtils.isBlank(rangeStrArray[0])) {
					// Suffix length is being requested.
					startRange = sysFile.length() - Long.valueOf(rangeStrArray[1]);
				} else {
					// Just the starting point is specified.
					startRange = Long.valueOf(rangeStrArray[0]);
				}

				logger.info("Range header ({}) parsed. Calculated ranges are: ({}, {}).",
						new Object[] {range, startRange, endRange});
				// ++++++ End Range header parsing. ++++++

				// Add additional headers for the partial download response.
				headers.add(HttpHeaders.CONTENT_LENGTH, Long.toString(endRange - startRange + 1L));
				headers.add(HttpHeaders.CONTENT_RANGE, "bytes " + Long.toString(startRange) + "-"
						+ Long.toString(endRange) + "/" + Long.toString(sysFile.length()));

				// Set the new HTTP status.
				httpStatus = HttpStatus.PARTIAL_CONTENT;

				// Create the partial download response object.
				response = new PartialFileStreamingResponseBody(sysFile, startRange, endRange);
			} else {
				// Add headers for a full download request.
				headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + userFile.getName() + "\"");
				headers.add(HttpHeaders.CONTENT_LENGTH, Long.toString(sysFile.length()));

				// Create the full download response object.
				response = new FileStreamingResponseBody(sysFile);
			}

			// Determine the mime type of the file.
			String mimeType = this.mimeTypeResolver.getContentType(userFile.getName());

			// Add HTTP headers for the request that will be needed for both full and partial downloads.
			headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");
			headers.add(HttpHeaders.CONTENT_TYPE, mimeType);

		} catch (NoSuchElementException e) {
			logger.error("No UserFile record found for userFileId: " + userFileId, e);
			return ResponseEntity.notFound().build();
		} catch (NumberFormatException e) {
			logger.error("The given range couldn't be converted to numbers: " + range, e);
			return ResponseEntity.badRequest().build();
		} catch (HttpRangeOutOfBoundsException e) {
			logger.error("The given range is invalid.", e);
			return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
					.header(HttpHeaders.CONTENT_RANGE, "bytes */" + Long.toString(sysFile.length())).build();
		} catch (DataAccessException e) {
			logger.error("A database error occurred while saving changes to the BricsFile object.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

		return ResponseEntity.status(httpStatus).headers(headers).body(response);
	}

	@Override
	public ResponseEntity<StreamingResponseBody> downloadFile(String fileId, String range) {
		StreamingResponseBody response = null;
		HttpHeaders headers = new HttpHeaders();
		HttpStatus httpStatus = HttpStatus.OK;
		File sysFile = null;

		try {
			BricsFile bricsFile = bricsFileRepository.findById(fileId).get();

			// Check file permission.
			if (!checkUserPermissions(bricsFile)) {
				logger.error("The \"{}\" user cannot access fileId: {}.", getAccount().getUserName(), fileId);
				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
			}

			sysFile = fileRepositoryService.getSystemFile(bricsFile);

			// Check if the file exists in the server's file system, and is a regular file.
			if (!sysFile.isFile()) {
				logger.error("The file (\"{}\") could not be found on in the file system. Path is \"{}\".", fileId,
						sysFile.getAbsolutePath());
				return ResponseEntity.notFound().build();
			}

			logger.info("A download has been requested for fileId: {}.", fileId);

			// Check if a "Range" HTTP header was given. If it does, process the request as a partial download.
			if (StringUtils.isNotBlank(range)) {
				// ++++++ Begin Range header parsing... ++++++
				long startRange = 0L;
				long endRange = sysFile.length() - 1L;
				String rangeDef = range.split("=")[1].trim();

				// Check if there are multiple ranges.
				if (rangeDef.contains(",")) {
					logger.error("The given range ({}) contains mulitple ranges, which is not supported.", range);
					return ResponseEntity.badRequest().build();
				}

				String[] rangeStrArray = rangeDef.split("-", -1);
				
				// Set the start and end range points.
				if (StringUtils.isNotBlank(rangeStrArray[0]) && StringUtils.isNotBlank(rangeStrArray[1])) {
					// A standard range was specified.
					startRange = Long.valueOf(rangeStrArray[0]);
					endRange = Long.valueOf(rangeStrArray[1]);
				} else if (StringUtils.isBlank(rangeStrArray[0])) {
					// Suffix length is being requested.
					startRange = sysFile.length() - Long.valueOf(rangeStrArray[1]);
				} else {
					// Just the starting point is specified.
					startRange = Long.valueOf(rangeStrArray[0]);
				}

				logger.info("Range header ({}) parsed. Calculated ranges are: ({}, {}).",
						new Object[] {range, startRange, endRange});
				// ++++++ End Range header parsing. ++++++

				// Add additional headers for the partial download response.
				headers.add(HttpHeaders.CONTENT_LENGTH, Long.toString(endRange - startRange + 1L));
				headers.add(HttpHeaders.CONTENT_RANGE, "bytes " + Long.toString(startRange) + "-"
						+ Long.toString(endRange) + "/" + Long.toString(sysFile.length()));

				// Set the new HTTP status.
				httpStatus = HttpStatus.PARTIAL_CONTENT;

				// Create the partial download response object.
				response = new PartialFileStreamingResponseBody(sysFile, startRange, endRange);
			} else {
				// Add headers for a full download request.
				headers.add(HttpHeaders.CONTENT_DISPOSITION,
						"attachment; filename=\"" + bricsFile.getFileName() + "\"");
				headers.add(HttpHeaders.CONTENT_LENGTH, Long.toString(sysFile.length()));

				// Create the full download response object.
				response = new FileStreamingResponseBody(sysFile);
			}

			// Determine the mime type of the file
			String mimeType = this.mimeTypeResolver.getContentType(bricsFile.getFileName());

			// Add HTTP headers for the request that will be needed for both full and partial downloads.
			headers.add(HttpHeaders.ACCEPT_RANGES, "bytes");
			headers.add(HttpHeaders.CONTENT_TYPE, mimeType);

		} catch (NoSuchElementException e) {
			logger.error("No BricsFile record found for fileId: " + fileId, e);
			return ResponseEntity.notFound().build();
		} catch (NumberFormatException e) {
			logger.error("The given range couldn't be converted to numbers: " + range, e);
			return ResponseEntity.badRequest().build();
		} catch (HttpRangeOutOfBoundsException e) {
			logger.error("The given range is invalid.", e);
			return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
					.header(HttpHeaders.CONTENT_RANGE, "bytes */" + Long.toString(sysFile.length())).build();
		} catch (DataAccessException e) {
			logger.error("A database error occurred while saving changes to the BricsFile object.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (LegacyFileAccessException e) {
			logger.error("Error occurred when accessing the legacy file from the data drop share.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

		return ResponseEntity.status(httpStatus).headers(headers).body(response);
	}

	@Override
	public ResponseEntity<Void> handleHeadRequest() {
		return ResponseEntity.noContent().headers(DEFAULT_HEADERS).build();
	}

	@Override
	public ResponseEntity<Void> handleOptionsRequest() {
		return ResponseEntity.noContent().headers(OPTION_HEADERS).build();
	}

	@Override
	public ResponseEntity<FileUploadDetails> saveFileData(String fileId, Long fileCategoryId, Long linkedObjectId,
			MultipartFile file, Long contentLength, String contentRange) {
		// Check if the file, category, and linked object IDs were given.
		if (StringUtils.isBlank(fileId) || fileCategoryId == null || linkedObjectId == null) {
			logger.error("No file ID, file category ID, or linked object ID was given.");
			return ResponseEntity.badRequest().build();
		}

		FileUploadDetails response = null;
		HttpHeaders headers = new HttpHeaders();

		try {
			BricsFile bricsFile = bricsFileRepository.findById(fileId).get();

			// Check file permission.
			if (!checkUserPermissions(bricsFile)) {
				logger.error("The \"{}\" user cannot access fileId: {}.", getAccount().getUserName(), fileId);
				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
			}

			File sysFile = fileRepositoryService.getSystemFile(bricsFile);

			// Set the standard open options for writing to a file.
			Set<OpenOption> openOpts = new HashSet<>();

			openOpts.add(StandardOpenOption.CREATE);
			openOpts.add(StandardOpenOption.WRITE);

			// Parse "Content-Range" request header field, if provided.
			long[] ranges = null;

			if (StringUtils.isNotBlank(contentRange)) {
				String[] res1 = contentRange.split("-");
				String[] res2 = res1[1].split("/");
				res1[0] = res1[0].replaceAll("[a-zA-Z]", "").trim();

				// Convert strings to numbers
				// ranges[0] => starting byte; ranges[1] => ending byte; ranges[2] => total file size
				ranges = new long[] {Long.parseLong(res1[0]), Long.parseLong(res2[0].trim()),
						Long.parseLong(res2[1].trim())};

				// Validate the starting range.
				long fileEnd = sysFile.length() - 1L;

				if (ranges[0] <= fileEnd) {
					logger.error("Adding data within the current file is not supported.");
					return ResponseEntity.badRequest().build();
				}

				// Set the append open option, if needed.
				if (ranges[0] > 0) {
					openOpts.add(StandardOpenOption.APPEND);
				}
			}
			else {
				// Set to overwrite the file, if it already exists.
				openOpts.add(StandardOpenOption.TRUNCATE_EXISTING);
			}

			logger.info("Saving {} to {} in the local file system...", sysFile.getName(), sysFile.getParent());

			// Save the uploaded file to the file system.
			long crcValue = 0L;
			CheckedInputStream checkedIn = new CheckedInputStream(file.getInputStream(), new CRC32());
			ReadableByteChannel inChannel = Channels.newChannel(checkedIn);
			FileChannel outChannel = FileChannel.open(sysFile.toPath(), openOpts);

			try {
				outChannel.transferFrom(inChannel, outChannel.position(), contentLength);
				crcValue = checkedIn.getChecksum().getValue();
			} finally {
				inChannel.close();
				outChannel.close();
			}

			// Update the upload user and date for the BricsFile object and save it to the database.
			bricsFile.setUploadedBy(getAccount().getUserId());
			bricsFile.setUploadedDate(LocalDateTime.now());
			bricsFileRepository.save(bricsFile);

			logger.info("Completed saving uploaded data to the file system for {}.", sysFile.getName());

			// Build the response object.
			response = new FileUploadDetails();

			response.setFileId(fileId);
			response.setFileCategoryId(fileCategoryId);
			response.setLinkedObjectId(linkedObjectId);
			response.setFileName(bricsFile.getFileName());
			response.setFileUrl(FileRepositoryConstants.FILE_API_ROOT + fileId);
			response.setFilePath(fileRepositoryService.getRelativeFilePath(sysFile));
			response.setFileSize(sysFile.length());
			response.setFileType(file.getContentType());
			response.setCrc(crcValue);

			// If a range was given, set related response object fields and set a header.
			if (ranges != null) {
				response.setUploadFileSize(contentLength);
				response.setMinRange(ranges[0]);
				response.setMaxRange(ranges[1]);

				// Set a "Content-Range" header for the response.
				headers.add("Content-Range",
						String.format("bytes 0-%1$d/%2$d", Long.valueOf(sysFile.length() - 1L), ranges[2]));
			}

		} catch (NoSuchElementException e) {
			logger.error("No BricsFile record found for fileId: " + fileId, e);
			return ResponseEntity.notFound().build();
		}
		catch (IOException e) {
			logger.error("Error occurred while writing the uploaded file to the file system.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (DataAccessException e) {
			logger.error("A database error occurred while saving changes to the BricsFile object.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		} catch (LegacyFileAccessException e) {
			logger.error("Error occurred when accessing the legacy file from the data drop share.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

		ResponseEntity<FileUploadDetails> responseEntity = null;

		// Check if headers will need to be added to the response.
		if (!headers.isEmpty()) {
			responseEntity = ResponseEntity.ok().headers(headers).body(response);
		} else {
			responseEntity = ResponseEntity.ok(response);
		}

		return responseEntity;
	}

	@Override
	public ResponseEntity<FileDetails> saveFileDbRecord(String fileId, Long fileCategoryId, Long linkedObjectId,
			String fileName, Long legacyUserFileId) {
		FileDetails response = null;

		// Verify that the file category ID, linked object ID, and file name are given.
		if (fileCategoryId == null || fileCategoryId < 0L) {
			logger.error("The 'fileCategoryId' form parameter is invalid: {}.", fileCategoryId);
			return ResponseEntity.badRequest().build();
		} else if (linkedObjectId == null || linkedObjectId <= 0L) {
			logger.error("The 'linkedObjectId' form parameter is invalid: {}.", linkedObjectId);
			return ResponseEntity.badRequest().build();
		} else if (StringUtils.isBlank(fileName)) {
			logger.error("The 'fileName' form parameter is invalid.");
			return ResponseEntity.badRequest().build();
		}

		try {
			BricsFile bricsFile = null;

			// Check whether or not a new BricsFile object will need to be created.
			if (StringUtils.isBlank(fileId)) {
				// Create a new BricsFile object and persist it to the database.
				bricsFile = new BricsFile();
				bricsFile.setId(fileRepositoryService.generateFileId());
				logger.info("Creating a new BRICS File object: {}...", bricsFile.getId());
			} else {
				// Get the existing BricsFile record and a handle to the file data.
				bricsFile = bricsFileRepository.findById(fileId).get();
				logger.info("Updating the BRICS File object: {}...", bricsFile.getId());
			}

			// Add in data from the other Form parameters.
			bricsFile.setFileName(fileName);
			bricsFile.setFileCategory(SystemFileCategory.getById(fileCategoryId));
			bricsFile.setLinkedObjectID(linkedObjectId);
			bricsFile.setLegacyUserFileId(legacyUserFileId);

			// Check the permissions to the referenced file and linked object.
			if (!checkUserPermissions(bricsFile)) {
				logger.error("The {} user cannot access BricsFile {}.", getAccount().getUserName(), bricsFile.getId());
				return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
			}

			// Persist changes to the database.
			bricsFile = bricsFileRepository.save(bricsFile);
			logger.info("Saved BricsFile record data denoted by fileId: {}.", bricsFile.getId());

			// Create the response body.
			response = fileMvcModelConverter.bricsFileToFileDetails(bricsFile, null);

		} catch (NoSuchElementException e) {
			logger.error("No BricsFile record found for fileId: " + fileId, e);
			return ResponseEntity.notFound().build();
		} catch (DataAccessException e) {
			logger.error("A database error occurred while saving changes to the BricsFile object.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}

		return ResponseEntity.ok(response);
	}

}

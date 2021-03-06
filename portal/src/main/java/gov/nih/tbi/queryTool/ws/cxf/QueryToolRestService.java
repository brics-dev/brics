package gov.nih.tbi.queryTool.ws.cxf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.jcraft.jsch.JSchException;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.dao.AccountDao;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.ws.AbstractRestService;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.util.MailEngine;
import gov.nih.tbi.query.model.QTDownloadPackage;
import gov.nih.tbi.repository.model.hibernate.UserFile;

public class QueryToolRestService extends AbstractRestService {

	private static Logger log = Logger.getLogger(QueryToolRestService.class);

	@Autowired
	RepositoryManager repositoryManager;

	@Autowired
	AccountManager accountManager;

	@Autowired
	MailEngine mailEngine;

	@Autowired
	ModulesConstants moduleConstants;

	@Autowired
	private AccountDao accountDao;

	private final Response ERROR_RESPONSE =
			Response.status(Status.INTERNAL_SERVER_ERROR).type(MediaType.TEXT_HTML_TYPE).build();

	private final Response OK_RESPONSE = Response.status(Status.OK).type(MediaType.TEXT_HTML_TYPE).build();

	/**
	 * This class takes the download package, unmarshalls it, and adds it to the download queue for the user.
	 * 
	 * @param downloadPackageString
	 * @return
	 */
	// Consolidated data files from all forms into a single download package
	@POST
	@Path("/uploadDownloadPackage")
	@Produces(MediaType.TEXT_HTML)
	public Response uploadQTDownloadPackage(@QueryParam("filePath") String xmlFilePath) {
		JAXBContext jc;

		File xmlFile = new File(xmlFilePath);

		QTDownloadPackage downloadPackage = null;

		FileReader xmlFileReader = null;

		List<File> tempDataFiles = new ArrayList<>();

		try {
			try {
				xmlFileReader = new FileReader(xmlFile);
			} catch (FileNotFoundException e) {
				String msg = "The download package XML saved by the query tool is not found: " + xmlFilePath;
				log.error(msg, e);
				e.printStackTrace();
				throw new InternalServerErrorException(msg, ERROR_RESPONSE);
			}

			try {
				jc = JAXBContext.newInstance(QTDownloadPackage.class);
				downloadPackage = (QTDownloadPackage) jc.createUnmarshaller().unmarshal(xmlFileReader);
			} catch (JAXBException e) {
				String msg = "Error occured during Query Tool Download Package Upload";
				e.printStackTrace();
				log.error(msg, e);
				throw new InternalServerErrorException(msg, ERROR_RESPONSE);
			} finally {
				try {
					xmlFileReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			log.debug("Adding download package for user: " + downloadPackage.getUsername());

			Account account = accountDao.getByUserName(downloadPackage.getUsername());

			if (account == null) {
				String msg = "The username given in the download package does not match any accounts in the system.";
				log.error(msg);
				// return ERROR_RESPONSE;
				throw new InternalServerErrorException(msg, ERROR_RESPONSE);
			}

			String fileDescription = "csv file generated by query tool for user " + account.getUserName();



			// UserFile uploadedFile, uploadedMappingFile = null;
			List<UserFile> userFiles = new ArrayList<UserFile>();

			for (String dataFilePath : downloadPackage.getDataFiles()) {
				File currentFile = new File(dataFilePath);
				tempDataFiles.add(currentFile);
				log.info("Uploading " + dataFilePath);

				try {
					userFiles.add(repositoryManager.uploadFile(account.getUserId(), currentFile, currentFile.getName(),
							fileDescription, "text/csv", new Date()));
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSchException e) {
					e.printStackTrace();
				}
			}

			for (Map.Entry<String, byte[]> entry : downloadPackage.getByteFileMap().entrySet()) {
				String fileName = entry.getKey();
				String fileExtension = getFileExtension(fileName);


				if (fileExtension == null) {
					String msg = "The file name for the package is malformed or missing an extension: " + fileName;
					log.error(msg);
					throw new InternalServerErrorException(msg, ERROR_RESPONSE);
				}
				// TODO: see about writing directly to the sftp instead of creating a temp file
				// write data to file
				File tempFile;
				File properNameFile;
				FileOutputStream fos = null;

				try {
					tempFile = File.createTempFile("tmp", fileExtension);
					properNameFile = new File(tempFile.getParent(), fileName);
					fos = new FileOutputStream(properNameFile);
					log.debug("Writing to file: " + fileName);
					fos.write(entry.getValue());
					log.debug("Finished Writing to file");

				} catch (IOException e) {
					e.printStackTrace();
					String msg = "Error occured during writing data to the temporary file";
					log.error(msg, e);
					// return ERROR_RESPONSE;
					throw new InternalServerErrorException(msg, ERROR_RESPONSE);
				} finally {
					try {
						if (fos != null) {
							fos.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				// upload the file to the SFTP
				try {
					if (log.isDebugEnabled() == true) {
						log.debug("Uploading file to SFTP...");
					}

					userFiles.add(
							repositoryManager.uploadFile(account.getUserId(), properNameFile, properNameFile.getName(),
									(fileName.startsWith(ServiceConstants.QT_PV_MAPPING_FILE_PREFIX) ? ("mapping "
											+ fileDescription) : fileDescription),
									"text/csv", new Date()));
				} catch (SocketException e) {
					e.printStackTrace();
					String msg = "Error occured uploading of file to SFTP";
					log.error(msg, e);
					throw new InternalServerErrorException(msg, ERROR_RESPONSE);
				} catch (IOException e) {
					e.printStackTrace();
					String msg = "Error occured uploading of file to SFTP";
					log.error(msg, e);
					throw new InternalServerErrorException(msg, ERROR_RESPONSE);
				} catch (JSchException e) {
					e.printStackTrace();
					String msg = "Error occured uploading of file to SFTP";
					log.error(msg, e);
					throw new InternalServerErrorException(msg, ERROR_RESPONSE);
				}

				// delete the temporary files we no longer need
				deleteFile(tempFile);
				deleteFile(properNameFile);
			}
			if (userFiles.size() > 0) {
				if (log.isDebugEnabled() == true) {
					log.debug("upload successful");
					log.debug("creating entry in alternate user download queue");
				}

				repositoryManager.addQueryToolDownloadPackage(account, downloadPackage, userFiles);

			} else {
				log.error("Unable to upload the resulting csv file for user - " + account.getUserName()
						+ ". No exceptions occurred");
			}
		} finally {

			// delete the temporary file
			deleteFile(xmlFile);

			for (File tempDataFile : tempDataFiles) {
				deleteFile(tempDataFile);
			}
		}

		return OK_RESPONSE;
	}

	/**
	 * Convenience method to log when a file delete is successful or not
	 * 
	 * @param file
	 */
	private void deleteFile(File file) {
		boolean success = file.delete();

		if (log.isDebugEnabled()) {
			if (success) {
				log.debug("Successfully deleted " + file.getAbsolutePath());
			}
		}

		if (!success) {
			log.error("Error occurred when deleting " + file.getAbsolutePath());
		}
	}

	/**
	 * Given a composite file name (e.g. "test.csv"), returns only the extension of the file. Returns null if the file
	 * extension is missing.
	 * 
	 * @param compositeFileName
	 * @return
	 */
	protected String getFileExtension(String compositeFileName) {
		// this is the extension without the . in the beginning
		String baseExtension = FilenameUtils.getExtension(compositeFileName);

		// we want to have it return the extension with the . in the beginning or null if the extension is missing.
		return baseExtension.isEmpty() ? null : "." + baseExtension;
	}
}

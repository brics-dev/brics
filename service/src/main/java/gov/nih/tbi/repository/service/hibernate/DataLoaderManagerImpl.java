package gov.nih.tbi.repository.service.hibernate;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.DataLoader;
import gov.nih.tbi.commons.service.DataLoaderManager;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.commons.service.util.MailEngine;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.ws.RestDictionaryProvider;
import gov.nih.tbi.repository.dao.DataStoreInfoDao;
import gov.nih.tbi.repository.model.DataFile;
import gov.nih.tbi.repository.model.SubmissionType;
import gov.nih.tbi.repository.model.hibernate.DataStoreInfo;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetDataStructure;
import gov.nih.tbi.repository.model.hibernate.DatasetFile;
import gov.nih.tbi.repository.service.exception.DataLoaderException;

@Service
@Scope("singleton")
public class DataLoaderManagerImpl implements Serializable, DataLoaderManager {
	Logger logger = Logger.getLogger(DataLoaderManagerImpl.class);

	private static final long serialVersionUID = 6156274083309322922L;

	@Autowired
	private MailEngine mailEngine;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private AccountManager accountManager;

	@Autowired
	private RepositoryManager repositoryManager;

	@Autowired
	private ModulesConstants modulesConstants;

	@Autowired
	private DataLoader dataLoader;

	@Autowired
	private DataStoreInfoDao dataStoreInfoDao;

	public synchronized void initializeDatasetFile(Account account, Long datasetId, String proxyTicket)
			throws DataLoaderException {

		if (repositoryManager.getDatasetPendingFileCount(datasetId).longValue() == 0) {

			Dataset dataset = repositoryManager.getDatasetExcludingDatasetFiles(datasetId);
			List<DatasetFile> dataSetFls = repositoryManager.getDatasetFiles(datasetId);

			logger.info(" inside initializeDatasetFile ");

			dataset.setSubmitDate(new Date());
			dataset.setDatasetStatus(DatasetStatus.LOADING);
			// repositoryManager.saveDataset(account, dataset); // BL: comment for testing

			repositoryManager.updateDatasetStatus(DatasetStatus.LOADING, dataset);

			logger.info("changed status to LOADING");

			dataset = repositoryManager.getDatasetExcludingDatasetFiles(datasetId); // BL: comment for testing
			logger.info("new status: " + dataset.getDatasetStatus());

			InputStream in = null;
			List<String> datasetFiles = new ArrayList<String>();
			try {
				List<DataFile> completeDataFileList = new ArrayList<DataFile>();
				ClassLoader cl = gov.nih.tbi.repository.model.ObjectFactory.class.getClassLoader();
				JAXBContext jc = JAXBContext.newInstance("gov.nih.tbi.repository.model", cl);
				Unmarshaller um = jc.createUnmarshaller();

				RestDictionaryProvider restDictionaryProvider =
						new RestDictionaryProvider(modulesConstants.getModulesDDTURL(), proxyTicket);
				/*
				 * RestDictionaryProvider dictionaryProvider = new
				 * RestDictionaryProvider(modulesConstants.getModulesDDTURL(), modulesConstants.getModulesAccountURL(),
				 * account.getUserName(), getHash2(account));
				 */

				// find the datafile to be unmarshalled into the DataFile object
				for (DatasetFile file : dataSetFls) {

					logger.info("file type: " + file.getFileType());
					if (SubmissionType.DATA_FILE.equals(file.getFileType())) {

						logger.info("Getting file from sftp, with userfile id : " + file.getUserFile().getId() + ", "
								+ " fileName : " + file.getUserFile().getName() + ", filePath : "
								+ file.getUserFile().getPath());

						in = repositoryManager.getSftpInputStream(file.getUserFile().getPath(),
								file.getUserFile().getName());

						DataFile jaxbDataFile = (DataFile) um.unmarshal(in);

						// Retains a copy of the file after it is stored. This allows us to get some
						// of the internal information of the newly saved document (that it didn't
						// have
						// before the save).
						jaxbDataFile = dataLoader.storeDataFile(account, dataset, jaxbDataFile, restDictionaryProvider);

						// Places the datafile in a list for later processing
						completeDataFileList.add(jaxbDataFile);
						datasetFiles.add(file.getUserFile().getName());
					}
				}

				// change dataset status from loading to private
				dataset.setDatasetStatus(DatasetStatus.PRIVATE);
				logger.info("changed status to PRIVATE");

				// retain a copy of the dataset for later processing
				Dataset savedDataset = repositoryManager.saveDataset(account, dataset);

				FormStructure ds = null;
				DataStoreInfo dsi = null;

				/**
				 * The original logic involves a special SQL query to pull a cartesian set of all data along with their
				 * join submission ids. This section users a method within repoDao to generate that query. It then uses
				 * the dataLoader to execute the query. The result is a list of maps (each row of the result) - each map
				 * being a key/value pair of the column name/column value:
				 * 
				 * table1.column1,table1.column2,table2.column1,table2.column2,submissionJoinId, datasetId
				 */

				// Map<String, GenericTable> resultsMap = null;
				for (DatasetDataStructure ddse : dataset.getDatasetDataStructure()) {
					ds = restDictionaryProvider.getDataStructureDetailsById(ddse.getDataStructureId());
					logger.info("DatasetDataStructure is: " + ddse.getDataStructureId());
					dsi = dataStoreInfoDao.getByDataStructureId(ddse.getDataStructureId());

					if (dsi == null) {
						repositoryManager.createTableFromDataStructure(ds.getFormStructureSqlObject(), account);
						dsi = dataStoreInfoDao.getByDataStructureId(ddse.getDataStructureId());
					}

					// resultsMap = repoDao.queryByDataStoreInfo(dsi);
					// we only need to do this ONCE since each dataset will contain one structure.
					// So we break.
					break;
				}

				// Create and generate the model based on the dataset, datafiles and
				// joinedTableResults
				// Model model = ModelFactory.createDefaultModel();
				/*
				 * for (DataFile dataFile : completeDataFileList) { model.add(rdfGenerator
				 * .generateRDFWithoutDaoInteraction(dataFile, ds, savedDataset, resultsMap, dsi)); } // use the rdf
				 * uploader and save it to virtuoso rdfUploader.addRDFToRepositoryFromModel(model, false);
				 */


				// Ensure that permission groups are sychronized with study
				sychronizeStudyForSingleDatasetPermissions(dataset.getStudy().getId(), dataset);
			} catch (JAXBException e) {
				throw new DataLoaderException("Error occured while unmarshalling the DataFile object.", e);
			} catch (JSchException | SftpException e) {
				throw new DataLoaderException("Error occured while trying to get the uploaded files from SFTP.", e);
			} catch (MalformedURLException | SQLException | UnsupportedEncodingException e) {
				throw new DataLoaderException(
						"Error occured while trying to get form structure information from dictionary.", e);
			} catch (UserPermissionException e) {
				throw new DataLoaderException("Error occured while trying to insert data into the repository.", e);
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						logger.error(
								"There was an error closing the input stream in the RepoManager initializeDatasetFile()"
										+ e.getMessage());
					}
				}
			}

			if (!datasetFiles.isEmpty()) {
				sendDataUploadCompletionNotification(account, dataset, datasetFiles);
			} else {
				logger.warn(
						"The dataset file list is empty, and a dataset should contain at least one data file for successful data upload");
			}
		}
	}

	private void sychronizeStudyForSingleDatasetPermissions(Long studyId, Dataset currDataSet) {

		// The part will unregister the data set entities and then register the study permission so
		// the two
		// mirror each other.

		List<EntityMap> currentStudyPermissionsEM = accountManager.listEntityAccess(studyId, EntityType.STUDY);

		// iterate through each of the datasets in order to synchronize them with the study
		// permissions

		// Clean out dataset permissions
		accountManager.unregisterEntity(EntityType.DATASET, currDataSet.getId());
		Long dataSetId = currDataSet.getId();

		// register those are in the current study group of permissions
		for (EntityMap currentStudyem : currentStudyPermissionsEM) {

			// Handles adding accounts
			if (currentStudyem.getAccount() != null) {
				accountManager.registerEntity(currentStudyem.getAccount(), EntityType.DATASET, dataSetId,
						currentStudyem.getPermission());
			}
			// Handles adding permission groups
			// We need and exclusion for the public/private group since this is a different set of
			// permissions
			if ((currentStudyem.getPermissionGroup() != null)
					&& (!currentStudyem.getPermissionGroup().getPublicStatus())) {
				accountManager.registerEntity(currentStudyem.getPermissionGroup(), EntityType.DATASET, dataSetId,
						currentStudyem.getPermission());
			}
		}
	}

	private void sendDataUploadCompletionNotification(Account account, Dataset dataset, List<String> datasetFiles) {

		String emailAddress = account.getUser().getEmail();
		String userFullName = account.getUser().getFullName();
		String datasetName = dataset.getName();
		String datasetId = dataset.getId().toString();
		String studyTitle = dataset.getStudy().getTitle();
		String mailSubject = messageSource
				.getMessage(ServiceConstants.MAIL_RESOURCE_COMPLETE_DATASET_SUBMISSION_SUBJECT, null, null);

		StringBuffer dataFileListBuffer = new StringBuffer();
		dataFileListBuffer.append("<ul>");
		for (String fileName : datasetFiles) {
			dataFileListBuffer.append("<li>");
			dataFileListBuffer.append(fileName);
			dataFileListBuffer.append("</li>");
		}
		dataFileListBuffer.append("</ul>");

		Object[] bodyArgs = new Object[] {userFullName, datasetName, datasetId, studyTitle,
				dataFileListBuffer.toString(), modulesConstants.getModulesOrgName(),
				modulesConstants.getModulesOrgPhone(), modulesConstants.getModulesOrgEmail()};
		String mailBody = messageSource.getMessage(ServiceConstants.MAIL_RESOURCE_COMPLETE_DATASET_SUBMISSION_BODY,
				bodyArgs, null);
		String mailFooter = messageSource.getMessage(ServiceConstants.MAIL_RESOURCE_COMMON_FOOTER,
				new Object[] {modulesConstants.getModulesOrgName(), modulesConstants.getModulesDTURL()}, null);

		if (emailAddress != null && !emailAddress.isEmpty()) {
			try {
				mailEngine.sendMail(mailSubject, mailBody + mailFooter, null, emailAddress);
			} catch (MessagingException e) {
				logger.error("Error occured while trying to email to the user for the successful data upload.");
				e.printStackTrace();
			}
		}
	}
}

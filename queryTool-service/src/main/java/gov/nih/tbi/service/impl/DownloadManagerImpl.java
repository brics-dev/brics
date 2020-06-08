package gov.nih.tbi.service.impl;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.jasig.cas.client.validation.Assertion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.jcraft.jsch.JSchException;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.BRICSZipUtil;
import gov.nih.tbi.commons.util.BRICSStringUtils;
import gov.nih.tbi.constants.ApplicationConstants;
import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.dictionary.model.DictionaryRestServiceModel.DataElementList;
import gov.nih.tbi.exceptions.CSVGenerationException;
import gov.nih.tbi.exceptions.FilterEvaluatorException;
import gov.nih.tbi.export.csv.CSVGenerator;
import gov.nih.tbi.pojo.CodeMapping;
import gov.nih.tbi.pojo.DataElement;
import gov.nih.tbi.pojo.DownloadPVMappingRow;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.InstancedDataTable;
import gov.nih.tbi.pojo.RepeatableGroup;
import gov.nih.tbi.query.model.QTDownloadPackage;
import gov.nih.tbi.repository.model.hibernate.DatafileEndpointInfo;
import gov.nih.tbi.repository.model.hibernate.UserFile;
import gov.nih.tbi.service.DataCartManager;
import gov.nih.tbi.service.DownloadManager;
import gov.nih.tbi.service.InstancedDataManager;
import gov.nih.tbi.service.MetaStudyManager;
import gov.nih.tbi.service.cache.InstancedDataCache;
import gov.nih.tbi.service.io.SftpClient;
import gov.nih.tbi.service.io.SftpClientManager;
import gov.nih.tbi.util.QueryRestProviderUtils;
import gov.nih.tbi.ws.provider.DictionaryWebserviceProvider;
import gov.nih.tbi.ws.provider.QueryToolWebserviceProvider;

@Component
@Scope("application")
public class DownloadManagerImpl implements DownloadManager {

	private static final Logger log = Logger.getLogger(DownloadManagerImpl.class);

	@Autowired
	ApplicationConstants constants;

	@Autowired
	DataCartManager dataCartManager;

	@Autowired
	InstancedDataManager instancedDataManager;

	@Autowired
	MetaStudyManager metaStudyManager;


	/*
	 * this method will take instance data and sent it to the user's download queue
	 */
	public String downloadDataTable(String packageName, InstancedDataTable instancedDataTable,
			List<FormResult> selectedForms, CodeMapping codeMapping, String userName, boolean isNormalCSV,
			InstancedDataCache cache, String booleanExpression, boolean showAgeRange)
			throws CSVGenerationException, FilterEvaluatorException {
		// Matt wanted us to add this...
		System.gc();

		// At this stage, query should've been run and the instancedDataTable was populated in dataCart.
		if (instancedDataTable == null) {
			log.error("Instanced DataTable is null when downloading the data table.");
			return QueryToolConstants.ERROR;
		}

		// Mapping file, only for downloading data table to queue
		String displayOption = instancedDataTable.getDisplayOption();
		List<DownloadPVMappingRow> mappings = new ArrayList<>();

		Set<String> deList = new HashSet<>();

		for (FormResult form : selectedForms) {
			for (RepeatableGroup group : form.getRepeatableGroups()) {
				for (DataElement element : group.getDataElements()) {
					deList.add(element.getName());
				}
			}
		}

		DictionaryWebserviceProvider dictionaryWebserviceProvider = new DictionaryWebserviceProvider(
				constants.getModulesDDTURL(), QueryRestProviderUtils.getProxyTicket(constants.getModulesDDTURL()));

		DataElementList dataElementList =
				dictionaryWebserviceProvider.getDataElementList(deList, constants.getDeInfoListURL());

		for (gov.nih.tbi.dictionary.model.hibernate.DataElement element : dataElementList.getList()) {
			mappings.add(new DownloadPVMappingRow(element));

		}

		byte[] mappingFileBytes = null;
		if (mappings != null && !mappings.isEmpty()) {
			try {
				mappingFileBytes = CSVGenerator.generateMappingCSV(mappings, displayOption);
			} catch (CSVGenerationException e) {
				throw e;
			}
		}

		// Need to rebuild the result table since the instancedDataTable passed in doesn't have all rows loaded.
		InstancedDataTable resultTable = instancedDataManager.buildInstancedDataTableForDownload(selectedForms,
				instancedDataTable.getSortColumn(), instancedDataTable.getSortOrder(), cache, codeMapping, userName,
				false, isNormalCSV, false, booleanExpression);
		resultTable.setDisplayOption(displayOption);
		resultTable.setDownloadPVMappings(mappings);

		QueryToolWebserviceProvider provider = new QueryToolWebserviceProvider(constants.getModulesAccountURL(),
				QueryRestProviderUtils.getProxyTicket(constants.getModulesAccountURL()));

		QTDownloadPackage qtDownloadPackage =
				downloadForm(packageName, provider, resultTable, mappingFileBytes, userName, isNormalCSV, showAgeRange);

		if (qtDownloadPackage == null) {
			return QueryToolConstants.ERROR;
		}

		provider.sendDownloadPackage(qtDownloadPackage, constants.getUploadDownloadPackageWebServiceURL());

		return QueryToolConstants.SUCCESS;
	}


	/**
	 * Download all forms in the data cart. No mapping file is generated.
	 * 
	 * @param packageName
	 * @param forms
	 * @param isNormalCSV
	 * @param permissionModel
	 * @return
	 * @throws FilterEvaluatorException
	 */
	public synchronized String downloadDataCart(String packageName, Collection<FormResult> forms, String userName,
			boolean isNormalCSV, boolean showAgeRange) throws FilterEvaluatorException {
		// Matt wanted us to add this...
		System.gc();

		Map<String, byte[]> byteFileMap = new HashMap<>();

		log.info("Initiating data cart download for " + packageName + " user: " + userName);

		Date dateAdded = null;
		Set<Long> datasetIds = new HashSet<Long>();
		ListMultimap<Long, String> attachedFilesMap = ArrayListMultimap.create();

		QueryToolWebserviceProvider provider = new QueryToolWebserviceProvider(constants.getModulesAccountURL(),
				QueryRestProviderUtils.getProxyTicket(constants.getModulesAccountURL()));

		List<String> dataFiles = new ArrayList<String>();
		for (FormResult form : forms) {
			long startTime = System.nanoTime();

			if (form.hasFilter()) {
				form.getFilters().clear();
			}

			List<FormResult> formList = new ArrayList<FormResult>(1);
			formList.add(form);
			
			InstancedDataCache cache = new InstancedDataCache();
			InstancedDataTable dataTable = instancedDataManager.buildInstancedDataTableForDownload(formList, null,
					QueryToolConstants.ASCENDING, cache, null, userName, true, isNormalCSV, false, null);

			dateAdded = new Date();
			String timestamp = BRICSTimeDateUtil.formatTimeStamp(dateAdded);
			String fileName = generateCsvFileName(dataTable.getFormNames(), timestamp);
			log.info("Generating download package: " + fileName);

			try {
				// Access Records
				Map<Long, Long> arMap = dataTable.getDatasetIdsWithRecordCounts();
				provider.sendAccessRecordPackages(arMap, userName, constants.getAccessRecordPackageWebServiceURL());

				File dataFile;
				if (isNormalCSV) {
					dataFile = CSVGenerator.generateCSV(fileName, dataTable, showAgeRange);
				} else {
					dataFile = CSVGenerator.generateFlattenedCSV(fileName, dataTable, showAgeRange);
				}

				dataFiles.add(dataFile.getAbsolutePath());
				datasetIds.addAll(arMap.keySet());
				attachedFilesMap.putAll(dataTable.getAttachedFilesMap());

				log.info("Preparation of " + fileName + " to the download queue has been completed, elapsed time: "
						+ ((System.nanoTime() - startTime) / 1000000) + "ms");
			} catch (CSVGenerationException e) {
				log.error(QueryToolConstants.INITIATE_DOWNLOAD_ERROR, e);
				return QueryToolConstants.ERROR;
			}
		}

		String name = packageName + "_" + BRICSTimeDateUtil.formatTimeStamp(new Date());

		QTDownloadPackage qtDownloadPackage = new QTDownloadPackage(datasetIds, userName, name, true, attachedFilesMap,
				dateAdded, byteFileMap, dataFiles);

		provider.sendDownloadPackage(qtDownloadPackage, constants.getUploadDownloadPackageWebServiceURL());
		return QueryToolConstants.SUCCESS;
	}

	/**
	 * Adds the forms in the given instanced data table into the download queue for the given user.
	 * 
	 * @param instancedDataTable
	 * @param username
	 * @return
	 */
	private synchronized QTDownloadPackage downloadForm(String packageName, QueryToolWebserviceProvider provider,
			InstancedDataTable instancedDataTable, byte[] mappingFileBytes, String username, boolean isNormalCSV,
			boolean showAgeRange) {
		// Matt wanted us to add this...
		System.gc();

		Date dateAdded = new Date();
		String timestamp = BRICSTimeDateUtil.formatTimeStamp(dateAdded);

		String csvFileName = generateCsvFileName(instancedDataTable.getFormNames(), timestamp);

		Map<String, byte[]> byteFileMap = new HashMap<>();
		try {

			// Access Records
			log.info("Building data access record...");
			Map<Long, Long> arMap = instancedDataTable.getDatasetIdsWithRecordCounts();
			log.info("Sending data access record...");
			provider.sendAccessRecordPackages(arMap, username, constants.getAccessRecordPackageWebServiceURL());

			log.info("Generating download package: " + csvFileName + " for user: " + username);

			File dataFile = null;
			if (isNormalCSV) {
				dataFile = CSVGenerator.generateCSV(csvFileName, instancedDataTable, showAgeRange);
			} else {
				dataFile = CSVGenerator.generateFlattenedCSV(csvFileName, instancedDataTable, showAgeRange);
			}

			if (mappingFileBytes != null) {
				byteFileMap.put(packageName + "_" + QueryToolConstants.QT_PV_MAPPING_FILE + ".csv", mappingFileBytes);
			}

			log.debug("Grabbing list of dataset IDs for download package...");

			Set<Long> datasetIds = arMap.keySet();
			ListMultimap<Long, String> attachedFilesMap = instancedDataTable.getAttachedFilesMap();
			log.debug("...Done, " + datasetIds.size() + " dataset IDs added");

			String dirName = packageName + "_" + timestamp;

			List<String> dataFiles = new ArrayList<>();
			dataFiles.add(dataFile.getAbsolutePath());

			QTDownloadPackage qtDownloadPackage = new QTDownloadPackage(datasetIds, username, dirName, false,
					attachedFilesMap, dateAdded, byteFileMap, dataFiles);

			return qtDownloadPackage;
		} catch (CSVGenerationException e) {
			log.error("There was an error generating a CSV for a user packageName: " + packageName + " username: "
					+ username + " isNormalCSV: " + isNormalCSV + " " + QueryToolConstants.INITIATE_DOWNLOAD_ERROR, e);
			return null;
		}
	}


	/*
	 * this method will take instance data and sent it to the specified meta study as a zip file
	 */
	public synchronized String downloadToMetaStudy(String fileName, InstancedDataTable instancedDataTable,
			List<FormResult> selectedForms, CodeMapping codeMapping, Account userAccount, long metaStudyId,
			String description, Assertion casAssertion, InstancedDataCache cache, String booleanExpression,
			boolean showAgeRange) throws FilterEvaluatorException {
		// Matt wanted us to add this...
		System.gc();

		QueryToolWebserviceProvider provider = null;
		if (ApplicationConstants.isWebservicesSecured()) {
			provider = new QueryToolWebserviceProvider(constants.getModulesAccountURL(), QueryRestProviderUtils
					.getOnlyProxyTicketByAssertion(constants.getModulesAccountURL(), casAssertion));
		} else {
			provider = new QueryToolWebserviceProvider(constants.getModulesAccountURL(),
					QueryRestProviderUtils.getProxyTicket(constants.getModulesAccountURL()));
		}

		// call synchronized account method to generate data files and access records
		InstancedDataTable resultTable = instancedDataManager.buildInstancedDataTableForDownload(selectedForms,
				instancedDataTable.getSortColumn(), instancedDataTable.getSortOrder(), cache, codeMapping,
				userAccount.getUserName(), false, true, false, booleanExpression);
		resultTable.setDisplayOption(instancedDataTable.getDisplayOption());
		
		QTDownloadPackage qtDownloadPackage =
				downloadForm(fileName, provider, resultTable, null, userAccount.getUserName(), true, showAgeRange);

		if (qtDownloadPackage == null) {
			return QueryToolConstants.ERROR;
		}

		UserFile instanceDataZip = null;
		File zipTempFile = null;

		try {
			// create a temp zip file to be uploaded to sftp.
			Date dateAdded = new Date();
			String timestamp = BRICSTimeDateUtil.formatTimeStamp(dateAdded);
			zipTempFile = File.createTempFile(fileName.split("\\.")[0] + "_" + timestamp, ".zip");

			List<File> dataFiles = new ArrayList<File>();

			for (String dataFilePath : qtDownloadPackage.getDataFiles()) {
				File dataFile = new File(dataFilePath);
				dataFiles.add(dataFile);
			}

			// writes the zip file using the same buffer size as the one defined for CSVGenerator.
			BRICSZipUtil.writeZipFile(dataFiles, zipTempFile, CSVGenerator.WRITER_BUFFER_SIZE);

			String uploadPath = QueryToolConstants.META_STUDY_FILE_PATH + userAccount.getUserId()
					+ QueryToolConstants.FILE_SEPARATER;

			// send zip file to sftp
			instanceDataZip = storeInstanceDataInSFTP(userAccount.getUserId(), zipTempFile, fileName, description,
					"fileType", new Date(), uploadPath);

			metaStudyManager.sendDownloadPackageToMetaStudy(instanceDataZip, metaStudyId, casAssertion);
		} catch (IOException e) {
			log.error("DownloadManager.downloadToMetaStudy(): There was an error zipping instance data files for user "
					+ userAccount.getUserName(), e);
			return QueryToolConstants.ERROR;
		} catch (JSchException e) {
			log.error("DownloadManager.downloadToMetaStudy(): There was an error uploading the zip file for user "
					+ userAccount.getUserName(), e);
			e.printStackTrace();
		} finally {
			// delete the temporary file
			if (zipTempFile != null) {
				zipTempFile.delete();
			}
		}

		return QueryToolConstants.SUCCESS;
	}


	private static String generateCsvFileName(List<String> formNames, String timestamp) {

		// In the case of three way join, we shorten the name of the forms to avoid the 218 character limit in the path
		// of the file to be opened in MS office products.
		if (formNames.size() > 2) {
			for (int i = 0; i < formNames.size(); i++) {
				String formName = formNames.get(i);

				if (formName.length() > QueryToolConstants.DOWNLOAD_DIRECTORY_FORM_LIMIT) {
					formNames.set(i, formName.substring(0, QueryToolConstants.DOWNLOAD_DIRECTORY_FORM_LIMIT - 1));
				}
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append("query_result_")
				.append(BRICSStringUtils.concatWithDelimiter(formNames, QueryToolConstants.JOIN_SEPARATOR)).append("_")
				.append(timestamp);

		return sb.toString();
	}

	/**
	 * This method will persist a file to the sftp. returns null if the file did not save
	 * 
	 * @param userId
	 * @param uploadFile
	 * @param fileName
	 * @param fileDescription
	 * @param fileType
	 * @param uploadDate
	 * @param filePath
	 * @return
	 */
	private UserFile storeInstanceDataInSFTP(Long userId, File uploadFile, String fileName, String fileDescription,
			String fileType, Date uploadDate, String filePath) throws SocketException, IOException, JSchException {

		// sftp connection
		DatafileEndpointInfo info = constants.getDataDropEndpointInfo();
		SftpClient client = SftpClientManager.getClient(info);

		// user file creation for return
		UserFile userFile = new UserFile();
		userFile.setDatafileEndpointInfo(info);
		userFile.setDescription(fileDescription);
		userFile.setName(fileName);
		userFile.setUploadedDate(uploadDate);
		userFile.setPath(filePath);
		userFile.setUserId(userId);
		userFile.setSize(new Long(uploadFile.length()));

		// Upload method closes itself after running. No need to close explicitly.
		client.upload(uploadFile, filePath, fileName);

		return userFile;
	}


	@Override
	public InstancedDataTable generateInstancedDataTable(List<FormResult> forms, String username,
			String booleanExpression) throws FilterEvaluatorException {
		InstancedDataTable dataTable = instancedDataManager.buildInstancedDataTableForDownload(forms, null,
				QueryToolConstants.ASCENDING, null, null, username, true, false, false, booleanExpression);

		return dataTable;
	}
}

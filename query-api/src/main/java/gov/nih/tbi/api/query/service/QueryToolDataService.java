package gov.nih.tbi.api.query.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Multimap;

import gov.nih.tbi.api.query.exception.ApiEntityNotFoundException;
import gov.nih.tbi.api.query.model.BasicFormStudy;
import gov.nih.tbi.api.query.model.BasicStudyForm;
import gov.nih.tbi.api.query.model.Filter;
import gov.nih.tbi.api.query.utils.FilterAdapter;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.util.BRICSStringUtils;
import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.exceptions.CSVGenerationException;
import gov.nih.tbi.exceptions.FilterEvaluatorException;
import gov.nih.tbi.export.csv.CSVGenerator;
import gov.nih.tbi.export.json.JsonGenerator;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.InstancedDataTable;
import gov.nih.tbi.pojo.StudyResult;
import gov.nih.tbi.service.InstancedDataManager;
import gov.nih.tbi.service.QueryAccountManager;
import gov.nih.tbi.service.ResultManager;
import gov.nih.tbi.service.cache.InstancedDataCache;
import gov.nih.tbi.service.model.PermissionModel;

@Service
public class QueryToolDataService implements DataService {
	@Autowired
	InstancedDataManager instancedDataManager;

	@Autowired
	ResultManager resultManager;

	@Autowired
	QueryAccountManager queryAccountManager;

	private static final Logger log = LogManager.getLogger(QueryToolDataService.class);

	private List<File> generateBulkDataFiles(List<FormResult> formResults, boolean isJsonFormat, boolean isFlattened,
			PermissionModel pm) {
		List<File> dataFiles = new ArrayList<>();

		for (FormResult currentForm : formResults) {
			List<FormResult> singleFormList = new ArrayList<FormResult>();
			singleFormList.add(currentForm);

			File dataFile = null;

			try {
				if (isJsonFormat) {
					dataFile = getJsonData(singleFormList, null, pm);
				} else {
					dataFile = getCsvData(singleFormList, null, isFlattened, pm);
				}

				log.info("Generated " + dataFile.getName());
				dataFiles.add(dataFile);
			} catch (CSVGenerationException e) {
				log.error("Error when generating csv file for form " + currentForm.getShortName(), e);
			} catch (FilterEvaluatorException e) {
				e.printStackTrace();
			}
		}

		return dataFiles;
	}

	/**
	 * {@inheritDoc}
	 */
	public File generateBulkZip(List<FormResult> formResults, boolean isJsonFormat, boolean isFlattened,
			PermissionModel pm) {
		log.info("Zipping up data files for bulk download...");
		
		//generate the list of files to be zipped up
		List<File> dataFiles = generateBulkDataFiles(formResults, isJsonFormat, isFlattened, pm);

		//zip up all the data files
		if (dataFiles != null && !dataFiles.isEmpty()) {
			String fileName = generateZipFileName();
			File zipFile = null;
			FileOutputStream fos = null;
			ZipOutputStream zipOut = null;

			try {
				zipFile = File.createTempFile(fileName, ".zip");
				log.info("Zipping up data files into: " + zipFile.getName());
				fos = new FileOutputStream(zipFile);
				zipOut = new ZipOutputStream(fos);
				for (File currentFile : dataFiles) {
					FileInputStream fis = null;
					try {
						fis = new FileInputStream(currentFile);
						ZipEntry zipEntry = new ZipEntry(currentFile.getName());
						zipOut.putNextEntry(zipEntry);

						byte[] bytes = new byte[524288];
						int length;
						while ((length = fis.read(bytes)) >= 0) {
							zipOut.write(bytes, 0, length);
						}
					} catch (IOException e) {
						log.error("Exception occurred when writing data file to ZIP", e);
					} finally {
						if (fis != null) {
							try {
								fis.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			} catch (FileNotFoundException e) {
				log.error("Could not find the zip file: " + fileName, e);
			} catch (IOException e1) {
				log.error("Exception occurred when writing ZIP", e1);
			} finally {
				if (zipOut != null) {
					try {
						zipOut.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				if (fos != null) {
					try {
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			return zipFile;
		}

		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getCsvData(List<FormResult> formResults, List<Filter> filters, boolean isFlattened, PermissionModel pm)
			throws FilterEvaluatorException, CSVGenerationException {

		InstancedDataTable resultTable = getDataTable(formResults, filters, pm.getUserName());

		// generate proper file name with timestamp
		String fileName = generateFileName(resultTable.getFormNames());
		File dataFile = null;

		// generate the csv file in temp storage
		if (isFlattened) {
			dataFile = CSVGenerator.generateFlattenedCSV(fileName, resultTable, false);
		} else {
			dataFile = CSVGenerator.generateCSV(fileName, resultTable, false);
		}

		return dataFile;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getJsonData(List<FormResult> formResults, List<Filter> filters, PermissionModel pm)
			throws FilterEvaluatorException {

		InstancedDataTable resultTable = getDataTable(formResults, filters, pm.getUserName());

		// generate proper file name with timestamp
		String fileName = generateFileName(resultTable.getFormNames());

		// generate the json file in temp storage
		File dataFile = JsonGenerator.generateJson(fileName, resultTable);

		return dataFile;
	}

	protected InstancedDataTable getDataTable(List<FormResult> formResults, List<Filter> filters, String username)
			throws FilterEvaluatorException {

		// get the form results from Virtuoso
		String filterExpression = null;

		// convert filters described in dataParam to filter objects and set them in the
		// appropriate form result
		if (filters != null && !filters.isEmpty()) {
			FilterAdapter filterAdapter = new FilterAdapter(formResults, filters);
			filterAdapter.adaptFilters();
			filterExpression = filterAdapter.buildExpression();
		}

		InstancedDataCache cache = new InstancedDataCache();
		InstancedDataTable resultTable = instancedDataManager.buildInstancedDataTableForDownload(formResults, null,
				null, cache, null, username, false, true, true, filterExpression);
		return resultTable;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<FormResult> basicFormStudyToFormResults(List<BasicFormStudy> formStudies) {
		List<FormResult> formResults = new ArrayList<>();

		for (BasicFormStudy formStudy : formStudies) {
			FormResult currentFormResult = resultManager.getFormByShortName(formStudy.getForm());

			if (currentFormResult == null) {
				throw new ApiEntityNotFoundException("Form structure not found: " + formStudy.getForm());
			}

			instancedDataManager.seedFormDataElements(currentFormResult);
			retainStudies(currentFormResult, formStudy.getStudies());

			if (log.isDebugEnabled()) {
				List<String> studyIds = currentFormResult.getStudies().stream().map(s -> s.getPrefixedId())
						.collect(Collectors.toList());
				String studyList = "Studies from " + currentFormResult.getShortName() + " - " + studyIds;
				log.debug(studyList);
			}

			formResults.add(currentFormResult);
		}

		return formResults;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<FormResult> basicStudyFormToFormResults(List<BasicStudyForm> studyForms) {

		Map<String, FormResult> resultMap = new HashMap<String, FormResult>();

		List<String> prefixIds = new ArrayList<String>();
		for (BasicStudyForm studyForm : studyForms) {
			prefixIds.add(studyForm.getStudy());
		}

		Multimap<String, FormResult> studyFormResultMap = resultManager.searchFormsByStudyPrefixedIds(prefixIds);
		if (studyFormResultMap == null || studyFormResultMap.isEmpty()) {
			return null;
		}

		// Key: form shortName, value: final set of study prefixedIds associated with
		// the form.
		Map<String, Set<String>> formStudyMap = new HashMap<String, Set<String>>();

		for (BasicStudyForm studyForm : studyForms) {
			String prefixId = studyForm.getStudy();
			List<String> inputFormNames = studyForm.getForms();

			Iterator<FormResult> formResultIt = studyFormResultMap.get(prefixId).iterator();

			while (formResultIt.hasNext()) {
				FormResult currentForm = formResultIt.next();
				String currFormName = currentForm.getShortName();

				// Remove study formResults that are not in the given list.
				if (inputFormNames != null && !inputFormNames.isEmpty() && !inputFormNames.contains(currFormName)) {
					formResultIt.remove();

				} else {
					// otherwise add to the form -> set of studyIds association map for future use
					Set<String> prefixIdSet = null;
					if (!formStudyMap.containsKey(currFormName)) {
						prefixIdSet = new HashSet<>();
					} else {
						prefixIdSet = formStudyMap.get(currFormName);
					}

					prefixIdSet.add(prefixId);
					formStudyMap.put(currFormName, prefixIdSet);
				}
			}
		}

		for (FormResult currForm : studyFormResultMap.values()) {
			String formName = currForm.getShortName();

			if (!resultMap.containsKey(formName)) {
				instancedDataManager.seedFormDataElements(currForm);

				// Here formResult contains all studies it is associated with, we need to filter
				// out studies that are not in input parameters.
				retainStudies(currForm, formStudyMap.get(formName));
				resultMap.put(formName, currForm);
			}


			if (log.isDebugEnabled()) {
				List<String> studyIds =
						currForm.getStudies().stream().map(s -> s.getPrefixedId()).collect(Collectors.toList());
				String studyList = "Studies from " + currForm.getShortName() + " - " + studyIds;
				log.debug(studyList);
			}
		}

		return resultMap.values().stream().collect(Collectors.toList());
	}

	/**
	 * Retain only the studies matching the given prefixed IDs
	 * 
	 * @param formResult
	 * @param studyPrefixedIds
	 */
	protected void retainStudies(FormResult formResult, Collection<String> studyPrefixedIds) {
		// if no study specified, then we want to keep all of the studies
		if (studyPrefixedIds == null || studyPrefixedIds.isEmpty()) {
			return;
		}

		Iterator<StudyResult> studyResultIt = formResult.getStudies().iterator();

		while (studyResultIt.hasNext()) {
			StudyResult currentStudy = studyResultIt.next();
			if (!studyPrefixedIds.contains(currentStudy.getPrefixedId())) {
				studyResultIt.remove();
			}
		}
	}

	protected static String generateZipFileName() {
		Date dateAdded = new Date();
		String timestamp = BRICSTimeDateUtil.formatTimeStamp(dateAdded);
		return "bulkForm_" + timestamp;
	}

	protected static String generateFileName(List<String> formNames) {
		Date dateAdded = new Date();
		String timestamp = BRICSTimeDateUtil.formatTimeStamp(dateAdded);
		// In the case of three way join, we shorten the name of the forms to avoid the
		// 218 character limit in the path
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
}

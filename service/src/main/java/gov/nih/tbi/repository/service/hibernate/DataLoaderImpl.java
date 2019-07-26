package gov.nih.tbi.repository.service.hibernate;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.http.impl.cookie.DateParseException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.service.DataLoader;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.dictionary.ws.RestDictionaryProvider;
import gov.nih.tbi.dictionary.ws.validation.ValidationUtil;
import gov.nih.tbi.repository.dao.DataStoreDao;
import gov.nih.tbi.repository.dao.DataStoreInfoDao;
import gov.nih.tbi.repository.dao.DataStoreTabularInfoDao;
import gov.nih.tbi.repository.dao.DatasetDao;
import gov.nih.tbi.repository.dao.SubmissionRecordJoinDao;
import gov.nih.tbi.repository.model.DataElementData;
import gov.nih.tbi.repository.model.DataFile;
import gov.nih.tbi.repository.model.DataStructureData;
import gov.nih.tbi.repository.model.Record;
import gov.nih.tbi.repository.model.RepeatableGroupData;
import gov.nih.tbi.repository.model.RepeatableGroupRow;
import gov.nih.tbi.repository.model.hibernate.BasicDataset;
import gov.nih.tbi.repository.model.hibernate.DataStoreInfo;
import gov.nih.tbi.repository.model.hibernate.DataStoreTabularColumnInfo;
import gov.nih.tbi.repository.model.hibernate.DataStoreTabularInfo;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetDataStructure;
import gov.nih.tbi.repository.model.hibernate.DatasetSubject;
import gov.nih.tbi.repository.model.hibernate.SubmissionRecordJoin;

@Service
@Scope("singleton")
public class DataLoaderImpl implements DataLoader {
	private static final Logger logger = Logger.getLogger(DataLoaderImpl.class);

	@Autowired
	SubmissionRecordJoinDao submissionRecordJoinDao;

	@Autowired
	DataStoreDao dataStoreDao;

	@Autowired
	DataStoreInfoDao dataStoreInfoDao;

	@Autowired
	DataStoreTabularInfoDao dataStoreTabularInfoDao;

	@Autowired
	DatasetDao datasetDao;

	private static final int QUERY_BATCH_SIZE = 10000;

	public DataFile storeDataFile(Account account, Dataset dataset, DataFile xmlDataFile,
			RestDictionaryProvider restDictionaryProvider) throws SQLException, MalformedURLException {

		long processStartTime = 0;
		if (logger.isDebugEnabled()) {
			processStartTime = System.currentTimeMillis();
		}

		List<String> batchQueries = new ArrayList<String>();
		Set<DatasetSubject> datasetSubjectSet = new HashSet<DatasetSubject>();

		// PS-2881:- Record and display number of records for new datasets
		// initialize recordCount to count all records present per dataset
		int recordCount = 0;

		try {
			// loop through every data structure
			for (DataStructureData dataStructureData : xmlDataFile.getDataStructure()) {

				// Only deal with data rows that actually have data records
				// Creates WS Client

				// Call webservice
				FormStructure currentDataStructure =
						restDictionaryProvider.getLatestDataStructureByName(dataStructureData.getShortName());

				// dictionaryProvider.getFormStructureFirstVersion(
				// dataStructureData.getShortName());
				logger.info("Inserting Data for " + currentDataStructure.getShortName() + " v"
						+ currentDataStructure.getVersion());
				// get the datastructure obj from database
				// Now get this from webservice since dictionary is split out
				// AbstractDataStructure currentDataStructure = dictionaryManager.getDataStructure(account,
				// dataStructureData.getShortName(), Integer.valueOf(dataStructureData.getVersion()));

				Map<String, DataElement> elementMap = ((FormStructure) currentDataStructure).getDataElements();
				logger.debug("Element Map: " + elementMap.toString());

				DataStoreInfo currentDataStore =
						this.getDataStoreFromDataStructure((FormStructure) currentDataStructure);
				if (currentDataStore == null) {
					currentDataStore = new DataStoreInfo(currentDataStructure.getId(), true, false);
				}
				logger.info("Created data store: " + currentDataStore.toString());

				// create a new entry in the table to join datasets and data structures
				logger.info("Creating a new data set data structure object...");
				DatasetDataStructure datasetDataStructure = new DatasetDataStructure();
				logger.info("Setting data set: " + dataset.getName());
				datasetDataStructure.setDataset(dataset);
				logger.info("Setting data set ID: " + Long.toString(currentDataStructure.getId()));
				datasetDataStructure.setDataStructureId(currentDataStructure.getId());
				logger.info(
						"Adding the new data set data structure to the data set: " + datasetDataStructure.toString());
				dataset.getDatasetDataStructure().add(datasetDataStructure);

				logger.info("Making webservice call to get data structure.");
				// not sure why we are making 2 web service calls here
				FormStructure ds = restDictionaryProvider.getDataStructureDetailsById(currentDataStructure.getId());

				// BL: moved from deep nested getColumn method
				if (ds == null) {
					throw new NullPointerException("ERROR: Form Structure: " + dataStructureData.getShortName() + " v"
							+ dataStructureData.getVersion() + " not found, has it been deleted?");
				}

				// BREAK

				// capture metrics for this method
				long dsStartTime = System.currentTimeMillis();
				int metricsNumRows = dataStructureData.getRecord().size();
				int metricsModSize = metricsNumRows / 20; // 5%
				float metricsRowsRemain = metricsNumRows;


				// loop through every record
				for (Record record : dataStructureData.getRecord()) {

					// output percent complete
					if (metricsRowsRemain-- % metricsModSize == 0) {
						logger.info("\nProcessing " + dataStructureData.getShortName() + ": "
								+ (100 - (int) (metricsRowsRemain / metricsNumRows * 100)) + "% "
								+ (System.currentTimeMillis() - dsStartTime) / 1000 + "s");
					}

					// create a new submission record join for every record
					SubmissionRecordJoin submissionRecord = new SubmissionRecordJoin();
					submissionRecord.setDatasetId(dataset.getId());
					submissionRecord = submissionRecordJoinDao.save(submissionRecord);

					// PS-2881:- Record and display number of records for new datasets
					recordCount++;


					// loop through every repeatable group in the record
					for (RepeatableGroupData repeatableGroupData : record.getRepeatableGroup()) {

						RepeatableGroup currentRepeatableGroup = null;
						DataStoreTabularInfo currentTabularInfo = null;

						// get the repeatable group object
						for (RepeatableGroup repeatableGroup : ((FormStructure) currentDataStructure)
								.getRepeatableGroups()) {

							if (repeatableGroupData.getName().equalsIgnoreCase(repeatableGroup.getName())) {
								currentRepeatableGroup = repeatableGroup;
							}
						}

						// get the datastore tabular info of the current repeatable group
						for (DataStoreTabularInfo tabularInfo : currentDataStore.getDataStoreTabularInfos()) {
							// TODO:Webservice This might break
							if (tabularInfo.getRepeatableGroupId().equals(currentRepeatableGroup.getId())) {
								currentTabularInfo = tabularInfo;
								if (logger.isDebugEnabled()) {
									logger.debug("Repository Table: " + currentTabularInfo.getTableName());
								}
							}
						}

						if (currentTabularInfo != null) {

							// this is a mapping of column name, to data
							Map<String, String> insertionMap = new HashMap<String, String>();

							// first insert the submission record join id into the column 'submission_record_join_id'
							String submissionRecordId = submissionRecord.getId().toString();
							insertionMap.put(ServiceConstants.COLUMN_SUBMISSION_JOIN, submissionRecordId);

							RepeatableGroup repeatableGroup =
									ds.getRepeatableGroupByName(repeatableGroupData.getName());
							if (repeatableGroup == null) {
								throw new NullPointerException("ERROR: Repeatable group: "
										+ repeatableGroupData.getName() + " not found, ensure it has not been deleted");
							}

							List<RepeatableGroupRow> repeatableGroupRowData = repeatableGroupData.getGroup();

							// Check for null if a group is empty
							if (repeatableGroupRowData != null) {
								// parse this repeateable group in this row
								for (RepeatableGroupRow row : repeatableGroupData.getGroup()) {

									// parse group columns, process cell values, insert values into map
									for (DataElementData dataColumn : row.getData()) {

										String columnName = getColumnName(currentTabularInfo, dataColumn.getName(),
												repeatableGroup);
										String keyString = null;
										// loop through keys to find the data element
										for (Map.Entry<String, DataElement> entry : elementMap.entrySet()) {
											if (entry.getValue().getName().equalsIgnoreCase(dataColumn.getName())) {
												keyString = entry.getKey();
												break;
											}
										}
										DataElement dataElement = elementMap.get(keyString);
										String value = dataColumn.getValue();
										// if the key string is null we did not find the data element in the map
										if (keyString != null) {
											String cellValue = getCellValueFromElement(dataElement, dataset, value,
													datasetSubjectSet);
											insertionMap.put(columnName, cellValue);
										} else {
											return null;
										}
									}

									// add the query to the list of queries to be batch executed
									String queryString =
											buildInsertQuery(currentTabularInfo.getTableName(), insertionMap);
									if (logger.isDebugEnabled()) {
										logger.debug("Queuing the Following Query: " + queryString);
									}

									batchQueries.add(queryString);

									if (batchQueries.size() >= QUERY_BATCH_SIZE) {
										commitToDatabase(batchQueries);
									}

									// Clear insertion hashMap to avoid inserting data into additional groups whose
									// values
									// are not overwritten
									insertionMap.clear();
									// Add the submissionRecordId back into map
									insertionMap.put(ServiceConstants.COLUMN_SUBMISSION_JOIN, submissionRecordId);
								}
							}
						}
					}

				}


				if (logger.isDebugEnabled()) {

					long dsEndTime = System.currentTimeMillis();

					long estimatedTime = dsEndTime - dsStartTime;
					String time = String.format("%d hrs, %d min, %d sec", TimeUnit.MILLISECONDS.toHours(estimatedTime),
							TimeUnit.MILLISECONDS.toMinutes(estimatedTime),
							TimeUnit.MILLISECONDS.toSeconds(estimatedTime)
									- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(estimatedTime)));
					logger.info("storeDataFile took " + time + " to complete");
				}

				// End BREAK

			}

			dataset.setDatasetSubject(datasetSubjectSet);

			// TODO: Check the speed of this batch insert against large amount of data
			commitToDatabase(batchQueries);

			if (logger.isDebugEnabled()) {
				// Time logging for performance purposes
				long proccessEndTime = System.currentTimeMillis();
				long estimatedTime = proccessEndTime - processStartTime;
				String time = String.format("%d hrs, %d min, %d sec", TimeUnit.MILLISECONDS.toHours(estimatedTime),
						TimeUnit.MILLISECONDS.toMinutes(estimatedTime), TimeUnit.MILLISECONDS.toSeconds(estimatedTime)
								- TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(estimatedTime)));
				logger.info("storeDataFile took " + time + " to complete");
			}
		} catch (UnsupportedEncodingException e) {
			logger.info("This is the error message " + e.getMessage());
			e.printStackTrace();
		} catch (DateParseException e) {
			e.printStackTrace();
		}

		// PS-2881:- Record and display number of records for new datasets
		// this sets the recound count per dataset and saves it to database
		dataset.setRecordCount(Long.valueOf(recordCount));
		// datasetDao.save(dataset);

		return xmlDataFile;

	}

	private void commitToDatabase(List<String> queries) {
		if (queries == null || queries.isEmpty()) {
			return;
		}

		int queriesCount = queries.size();

		logger.info("Executing " + queriesCount + " batched queries...");
		long startTime = System.currentTimeMillis();
		try {
			dataStoreDao.executeBatchQueries(queries);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		long endTime = System.currentTimeMillis();
		logger.info("Batch queries done! " +  (endTime - startTime) + "ms");
		queries.clear();
	}

	/**
	 * Takes a set of mapelements and returns a hashmap of map element names to MapElement
	 * 
	 * @param list
	 * @return
	 */
	private Map<String, MapElement> createElementMap(List<MapElement> list) {

		Map<String, MapElement> newMap = new HashMap<String, MapElement>();
		for (MapElement element : list) {
			newMap.put(element.getStructuralDataElement().getName().toLowerCase(), element);
		}

		return newMap;
	}

	public DataStoreInfo getDataStoreFromDataStructure(FormStructure dataStructure) {

		return dataStoreInfoDao.getByDataStructureId(dataStructure.getId());
	}

	private String getColumnName(DataStoreTabularInfo tabularInfo, String mapElementName,
			RepeatableGroup repeatableGroup) {

		DataStoreTabularColumnInfo currentColumnInfo = null;

		for (DataStoreTabularColumnInfo columnInfo : tabularInfo.getColumnInfos()) {

			for (MapElement mapElement : repeatableGroup.getDataElements()) {
				if (mapElementName.equalsIgnoreCase(mapElement.getStructuralDataElement().getName()) && columnInfo
						.getColumnName().equalsIgnoreCase(mapElement.getStructuralDataElement().getName())) {
					currentColumnInfo = columnInfo;
				}
			}
		}

		if (currentColumnInfo != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Column name: " + currentColumnInfo.getColumnName());
			}
			return currentColumnInfo.getColumnName().toLowerCase();
		} else {
			return null;
		}
	}

	private String getCellValueFromElement(DataElement dataElement, Dataset dataset, String value,
			Set<DatasetSubject> datasetSubjectSet) throws DateParseException {

		if (dataElement != null) {
			// populates the Dataset Subject join table with the GUID value
			if (DataType.GUID.equals(dataElement.getType())) {
				for (DatasetSubject datasetSubject : datasetSubjectSet) {

					if (value.trim().equalsIgnoreCase(datasetSubject.getGuid().trim())) {
						return value.trim();
					}

				}

				DatasetSubject newSubject = new DatasetSubject();
				newSubject.setDataset(new BasicDataset(dataset));
				newSubject.setSubjectGuid(value.trim());
				datasetSubjectSet.add(newSubject);
			}

			// if data element is date/time, we will need to ensure the timezone is -04
			if (DataType.DATE.equals(dataElement.getType())) {
				value = value.trim();

				Date date = ValidationUtil.parseDate(value);
				String finalDateString = BRICSTimeDateUtil.formatDateTime(date);
				value = finalDateString;
				logger.debug("Date Value: " + value);
			}
		}

		return escapeValue(value).trim();
	}

	private static final String quoteChar = "\'";
	private static final String quoteCharReplacement = "\'\'";

	private String escapeValue(String value) {

		if (value.contains(quoteChar)) {
			value = value.replace(quoteChar, quoteCharReplacement);
		}

		return value;
	}

	/**
	 * Builds an insertion query
	 * 
	 * @param tableName - name of the table to insert into
	 * @param insertionMap - This map has the relationship of column name to value
	 * @return - The insertion query
	 */
	private String buildInsertQuery(String tableName, Map<String, String> insertionMap) {

		String query = CoreConstants.INSERT1 + tableName + CoreConstants.INSERT2;
		String columnList = "\"id\"";
		// use sequence as ID
		String insertionList =
				CoreConstants.SEQUENCE1 + tableName + CoreConstants.SEQUENCE_SUFFIX + CoreConstants.SEQUENCE2;

		for (String columnName : insertionMap.keySet()) {
			columnList += CoreConstants.COMMA;
			insertionList += CoreConstants.COMMA;

			columnList += CoreConstants.QUOTE + columnName + CoreConstants.QUOTE;
			insertionList += CoreConstants.SINGLE_QUOTE + insertionMap.get(columnName) + CoreConstants.SINGLE_QUOTE;
		}

		query += columnList + CoreConstants.INSERT3 + insertionList + ");";

		return query;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Map<String, Object>> retrieveObjects(String queryString) {

		return dataStoreDao.getJdbcTemplate().queryForList(queryString);
	}
}

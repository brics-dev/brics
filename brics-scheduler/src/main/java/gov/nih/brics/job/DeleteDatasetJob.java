package gov.nih.brics.job;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import gov.nih.brics.JobScheduler;
import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.repository.dao.AccessRecordDao;
import gov.nih.tbi.repository.dao.DataStoreDao;
import gov.nih.tbi.repository.dao.DataStoreInfoDao;
import gov.nih.tbi.repository.dao.DatasetDao;
import gov.nih.tbi.repository.dao.SubmissionRecordJoinDao;
import gov.nih.tbi.repository.model.hibernate.AccessRecord;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetFile;
import gov.nih.tbi.repository.model.hibernate.UserFile;

/**
 * This class contains the implementation of the various jobs that are to be run
 * at a pre-defined schedule. The jobs are actually scheduled in the
 * {@link JobScheduler}
 * 
 * @author Abhishek Srivastava
 * 
 */
@Component
@Scope("singleton")
public class DeleteDatasetJob {
	private static Logger log = Logger.getLogger(DeleteDatasetJob.class);

	@Autowired
	DataStoreInfoDao dataStoreInfoDao;

	@Autowired
	SubmissionRecordJoinDao submissionRecordJoinDao;

	@Autowired
	DataStoreDao dataStoreDao;

	@Autowired
	DatasetDao datasetDao;

	@Autowired
	AccessRecordDao accessRecordDao;

	@Autowired
	RepositoryManager repositoryManager;

	public void deleteDatasetJob(String days) {
		long startTime = System.currentTimeMillis();

		// any dataset in UPLOADING, ERROR, LOADING, CANCELLED will be deleted
		String statusToDelete = "UPLOADING, ERROR, LOADING, CANCELLED";

		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -(Integer.parseInt(days)));
		Date deleteDatasetCutOffDate = cal.getTime();

		log.info("***********************STARTING delete dataset job	");
		log.info("dataset deletion cut off date :: " + deleteDatasetCutOffDate);
		log.info("dataset Delete status :: " + statusToDelete);

		List<String> status = Arrays.asList(statusToDelete.split("\\s*,\\s*"));
		Set<DatasetStatus> statuses = new HashSet<DatasetStatus>();
		for (String s : status) {
			for (DatasetStatus datasetStatus : DatasetStatus.values()) {
				if (s.equalsIgnoreCase(datasetStatus.name().toString())) {
					statuses.add(datasetStatus);
				}
			}
		}

		List<Dataset> dataset = datasetDao.getDatasetByStatuses(statuses);
		log.info("Total DatasetIds size " + dataset.size());

		List<Dataset> dataSetsToDelete = new ArrayList<Dataset>();
		for (Dataset ds : dataset) {
			if (ds.getSubmitDate() != null) {

				if (ds.getSubmitDate().before(deleteDatasetCutOffDate)) {
					log.info(
							"Datasetid to delete :: " + ds.getId() + " status :: " + ds.getDatasetStatus().getName());
					dataSetsToDelete.add(datasetDao.getDatasetWithChildren(ds.getId()));
				}

			}
		}

		log.info("Final Dataset to delete size :: " + dataSetsToDelete.size());

		List<UserFile> listOfFilesToDelete = new ArrayList<UserFile>();

		for (Dataset ds : dataSetsToDelete) {

			System.out
					.println("deleteDatasetJob :: deleting repo and meta records and also sftp files for datasetid :: "
							+ ds.getId() + " status :: " + ds.getDatasetStatus().getName());

			// check if the file from the delete dataset is linked to dataset in another
			// status
			for (DatasetFile datasetFile : ds.getDatasetFileSet()) {
				UserFile userFile = datasetFile.getUserFile();
				// check if user file is linked to dataset with status SHARED, PRIVATE, ARHIVED,
				// DELETED.
				if (datasetDao.getDatasetUserFiles(userFile.getId()).longValue() == 0) {
					listOfFilesToDelete.add(userFile);
				}
			}

			
			for (UserFile userFile : listOfFilesToDelete) {
				repositoryManager.deleteDatasetFileByName(userFile);

				log.info("deleteDatasetJob :: deleting userfile path :: " + userFile.getPath()
						+ " fileName :: " + userFile.getName());

			}

			// delete access records from meta d/b, access record table and dataset table
			deleteDatasetRecordsFromAccessRecordTable(ds);
			datasetDao.remove(ds.getId());
			
		}

		long endTime = System.currentTimeMillis();
		log.info("***********************ENDING delete dataset job Total time taken "
				+ ((endTime - startTime) / 1000) + " seconds");
	}

	/**
	 * Delete records from Access record table
	 * 
	 * @param d Dataset record
	 */
	private void deleteDatasetRecordsFromAccessRecordTable(Dataset d) {
		List<AccessRecord> accessRecord = accessRecordDao.getAccessRecordsByDataset(d.getId());

		log.info("Deleting repo Access records :: " + accessRecord.size());

		for (AccessRecord ar : accessRecord) {
			accessRecordDao.remove(ar.getId());
		}

		log.info("Deleting repo Access records :: " + accessRecord);
	}

}

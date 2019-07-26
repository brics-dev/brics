package gov.nih.brics.job;


import gov.nih.tbi.repository.model.hibernate.ScheduledJob;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetDataStructure;
import gov.nih.tbi.repository.model.hibernate.DataStoreInfo;
import gov.nih.tbi.repository.model.hibernate.SubmissionRecordJoin;
import gov.nih.tbi.repository.model.hibernate.DataStoreTabularInfo;

import gov.nih.tbi.repository.dao.DataStoreInfoDao;
import gov.nih.tbi.repository.dao.SubmissionRecordJoinDao;
import gov.nih.tbi.repository.dao.DataStoreDao;
import gov.nih.tbi.repository.dao.DatasetDao;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.Date;
import java.sql.SQLException;

import gov.nih.tbi.repository.dao.ScheduledJobDao;
import gov.nih.tbi.commons.model.JobStatus;
import gov.nih.tbi.commons.model.JobType;
import gov.nih.tbi.commons.model.JobParameterKey;


/**
 * Created by amakar on 9/8/2016.
 */

@Component
@Scope("singleton")
public class DatasetDataDeletionJob {

    @Autowired
    ScheduledJobDao jobDao;

    @Autowired
    DataStoreInfoDao dataStoreInfoDao;

    @Autowired
    SubmissionRecordJoinDao submissionRecordJoinDao;

    @Autowired
    DataStoreDao dataStoreDao;

    @Autowired
    DatasetDao datasetDao;

    private static Logger logger = Logger.getLogger(DatasetDataDeletionJob.class);

    private static final int SUBMISSION_DELETION_BATCH_SIZE = 125;

    /**
     *
     */

    @Async
    public void doJob()
    {
        logger.info("Job started");

        List<ScheduledJob> newJobs = this.jobDao.searchByTypeAndStatus(JobType.DATASET_DATA_DELETION, JobStatus.NEW);

        int newJobSize = newJobs.size();

        logger.info("Processing " + newJobSize + " new jobs");

        for(ScheduledJob job : newJobs) {

            job.setJobStatus(JobStatus.PREPARING);
            this.jobDao.save(job);

            this.prepareJob(job);

            job.setJobStatus(JobStatus.READY);
            job.setUpdateTime(new Date());
            this.jobDao.save(job);
        }

        List<ScheduledJob> abortedJobs = this.jobDao.searchByTypeAndStatus(JobType.DATASET_DATA_DELETION, JobStatus.ABORTED);

        int abortedJobSize = abortedJobs.size();

        logger.info("Processing " + abortedJobSize + " previously aborted jobs");

        for(ScheduledJob job : abortedJobs) {

            job.setAbortRequested(false);
            this.jobDao.save(job);

            this.processJob(job);
        }

        List<ScheduledJob> readyJobs = this.jobDao.searchByTypeAndStatus(JobType.DATASET_DATA_DELETION, JobStatus.READY);

        for(ScheduledJob job : readyJobs) {

            this.processJob(job);
        }

        logger.info("Job ended");
    }

    /**
     *
     * @param job
     */

    private void processJob(ScheduledJob job) {

        long jobID = job.getID();
        JobType type = job.getJobType();

        logger.info("Processing " + type + " job ID " + jobID);

        JobStatus initStatus = job.getStatus();

        if(initStatus.equals(JobStatus.ABORTED)) {

            job.setJobStatus(JobStatus.RESUMED);
            job.setUpdateTime(new Date());
        } else {

            job.setJobStatus(JobStatus.STARTED);
            job.setStartTime(new Date());
        }

        this.jobDao.save(job);

        boolean hasErrors = false;

        boolean abortRqstd = job.getAbortRequested();

        List<ScheduledJob> subtasks = job.getSubtasks();

        for(int i = 0; i < subtasks.size() && !abortRqstd; i++) {

            ScheduledJob subtask = subtasks.get(i);

            JobStatus subtaskStatus = subtask.getStatus();

            if(subtaskStatus.equals(JobStatus.READY)) {

                boolean subtaskResult = this.processSubtask(subtask);

                if(!subtaskResult) {
                    hasErrors = true;
                }

                job = this.jobDao.get(jobID);
                abortRqstd = job.getAbortRequested();
            }
        }

        JobStatus jobStatus = null;

        if(abortRqstd) {

            jobStatus = JobStatus.ABORTED;
            job.setUpdateTime(new Date());
            logger.info(type + " job " + jobID + " aborted");
        } else if(hasErrors) {

            jobStatus = JobStatus.ERROR;
            job.setUpdateTime(new Date());
            logger.error("Error processing " + type + " job " + jobID);

        } else {

            jobStatus = JobStatus.COMPLETED;
            job.setEndTime(new Date());
            logger.info(type + " job " + jobID + " completed");
        }


        job.setJobStatus(jobStatus);
        this.jobDao.save(job);
    }

    /**
     *
     * @param subtask
     * @return
     */

    private boolean processSubtask(ScheduledJob subtask) {

        boolean result = false;

        ScheduledJob parent = subtask.getParentJob();

        long datasetID = parent.getLongParameterValue(JobParameterKey.DATASET_ID);

        JobType type = subtask.getJobType();
        long jobID = subtask.getID();
        Date startTime = new Date();

        logger.info("Processing subtask " + type + " ID " + jobID + " started");

        subtask.setJobStatus(JobStatus.STARTED);
        subtask.setStartTime(startTime);

        this.jobDao.save(subtask);

        String deleteSql = null;

        if(type.equals(JobType.DATASTORE_DELETION)) {

            String tableName = subtask.getParameterValue(JobParameterKey.TABLE);

            deleteSql = this.buildDataStoreDeletionQuery(tableName, datasetID);

        } else if(type.equals(JobType.SUBMISSION_JOIN_DELETION)) {

            Long limitID = subtask.getLongParameterValue(JobParameterKey.LIMIT_ID);

            deleteSql = this.buildSubmissionJoinDeletionQuery(datasetID, limitID);
        }

        JobStatus execStatus = null;
        int rowCount = 0;
        Date endTime = null;

        try {

            rowCount = this.dataStoreDao.executeUpdate(deleteSql);
            execStatus = JobStatus.COMPLETED;
            endTime = new Date();
            subtask.setEndTime(endTime);

            result = true;
        } catch(SQLException sqlEx) {

            rowCount = -1;
            //keep hasErrors as true;
            execStatus = JobStatus.ERROR;
            endTime = new Date();
            subtask.setUpdateTime(endTime);
        }

        long timeMillis = endTime.getTime() - startTime.getTime();
        int timeSec = (int)((double)timeMillis / 1000d);

        subtask.setJobStatus(execStatus);
        subtask.addParameter(JobParameterKey.ROWS, new Long(rowCount));
        subtask.addParameter(JobParameterKey.EXEC_TIME, new Long(timeSec));
        this.jobDao.save(subtask);

        logger.info(rowCount + " rows processed in " + timeSec + " sec");

        return result;
    }

    /**
     *
     * @param job
     */

    private void prepareJob(ScheduledJob job)  {

        long jobID = job.getID();

        Long datasetID = job.getLongParameterValue(JobParameterKey.DATASET_ID);

        logger.info("Preparing job ID " + jobID + " for dataset ID " + datasetID);

        Dataset dataset = this.datasetDao.getDatasetWithChildren(datasetID);

        Set<DatasetDataStructure> datasetDataStructures = dataset.getDatasetDataStructure();

        for (DatasetDataStructure datasetDataStructure : datasetDataStructures) {

            long dataStructureID = datasetDataStructure.getDataStructureId();

            // get the datastore info for each of the data structures
            DataStoreInfo currentDataInfo = this.dataStoreInfoDao.getByDataStructureId(dataStructureID);

            Set<DataStoreTabularInfo> dataStoreTabularInfos = currentDataInfo.getDataStoreTabularInfos();

            for (DataStoreTabularInfo tabularInfo : dataStoreTabularInfos) {

                String tableName = tabularInfo.getTableName();

                ScheduledJob dataStoreDelJob = new ScheduledJob(JobType.DATASTORE_DELETION, null);
                dataStoreDelJob.setParentJob(job);
                dataStoreDelJob.setJobStatus(JobStatus.READY);

                dataStoreDelJob = this.jobDao.save(dataStoreDelJob);

                dataStoreDelJob.addParameter(JobParameterKey.TABLE, tableName);
                this.jobDao.save(dataStoreDelJob);

                logger.info(JobType.DATASTORE_DELETION + " subtask created for table " + tableName);
            }
        }

        List<SubmissionRecordJoin> submissionRecords = submissionRecordJoinDao.getByDatasetId(dataset.getId());

        int submissionRecordCount = submissionRecords.size();

        int batchMaxIndx = SUBMISSION_DELETION_BATCH_SIZE - 1;

        while(batchMaxIndx < (submissionRecordCount - 1)) {

            ScheduledJob submissionDelJob = new ScheduledJob(JobType.SUBMISSION_JOIN_DELETION, null);
            submissionDelJob.setParentJob(job);
            submissionDelJob.setJobStatus(JobStatus.READY);
            submissionDelJob = this.jobDao.save(submissionDelJob);

            SubmissionRecordJoin limitRecord = submissionRecords.get(batchMaxIndx);
            long limitID = limitRecord.getId();

            submissionDelJob.addParameter(JobParameterKey.LIMIT_ID, limitID);
            this.jobDao.save(submissionDelJob);

            batchMaxIndx += SUBMISSION_DELETION_BATCH_SIZE;

            logger.info(JobType.SUBMISSION_JOIN_DELETION + " subtask created with LIMIT_ID " + limitID);
        }

        ScheduledJob submissionDelJob = new ScheduledJob(JobType.SUBMISSION_JOIN_DELETION, null);
        submissionDelJob.setParentJob(job);
        submissionDelJob.setJobStatus(JobStatus.READY);

        this.jobDao.save(submissionDelJob);

        logger.info("Final " + JobType.SUBMISSION_JOIN_DELETION + " subtask created");
    }

    /**
     *
     * @param tableName
     * @param datasetID
     * @return
     */

    public String buildDataStoreDeletionQuery(String tableName, long datasetID) {

        StringBuilder builder = new StringBuilder();

        builder.append("DELETE FROM ");
        builder.append(tableName + " WHERE submission_record_join_id IN ");
        builder.append("(SELECT id FROM submission_record_join ");
        builder.append("WHERE dataset_id = " + datasetID + ");");

        String result = builder.toString();

        return result;
    }

    /**
     *
     * @param datasetID
     * @param limitID
     * @return
     */

    public String buildSubmissionJoinDeletionQuery(Long datasetID, Long limitID) {

        StringBuilder builder = new StringBuilder();

        builder.append("DELETE FROM submission_record_join ");
        builder.append("WHERE dataset_id = " + datasetID);

        if(limitID != null) {
            builder.append(" AND id <= " + limitID);
        }

        builder.append(";");

        String result = builder.toString();

        return result;
    }
}

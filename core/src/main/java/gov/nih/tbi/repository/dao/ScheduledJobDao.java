package gov.nih.tbi.repository.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.commons.model.JobStatus;
import gov.nih.tbi.commons.model.JobType;
import gov.nih.tbi.repository.model.hibernate.ScheduledJob;

import java.util.List;

/**
 * Created by amakar on 9/7/2016.
 */
public interface ScheduledJobDao extends GenericDao<ScheduledJob, Long> {

    List<ScheduledJob> searchByTypeAndStatus(JobType type, JobStatus status);
}

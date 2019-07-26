package gov.nih.brics.job;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import gov.nih.tbi.repository.dao.DownloadPackageDao;

@Component
@Scope("singleton")
public class DownloadDatasetDeleteJob {
	
	private static Logger log = Logger.getLogger(DownloadDatasetDeleteJob.class);
	
	@Autowired
	private DownloadPackageDao downloadPackageDao;
	
	@Async
	public void doJob() {
		log.log(Level.INFO, "---------Beginning downloadDatasetDelete job");	
		int totalRows = downloadPackageDao.deleteDatasetsOlderThan(30);
		log.log(Level.INFO, "Total download packages deleted: "+totalRows);
		log.log(Level.INFO, "---------End downloadDatasetDelete job");
	}

}

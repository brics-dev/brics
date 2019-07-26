package gov.nih.brics;

import gov.nih.brics.job.AccountEmailReportJob;
import gov.nih.brics.job.AccountExpirationEmailJob;
import gov.nih.brics.job.AccountReportingEmailJob;
import gov.nih.brics.job.AdvancedViewCreateFileJob;
import gov.nih.brics.job.CheckIUStatusAndRetrievManifestJob;
import gov.nih.brics.job.DataElementStatusUpdateJob;
import gov.nih.brics.job.DeleteLoginPendingApprovedAccountJob;
import gov.nih.brics.job.DownloadDatasetDeleteJob;
import gov.nih.brics.job.PasswordExpirationEmailJob;
import gov.nih.brics.job.PublicSiteStudiesCreateFileJob;
import gov.nih.brics.job.RDFJobs;
import gov.nih.brics.job.RetrieveIUBiosampleCatalogJob;
import gov.nih.brics.job.StuckDatasetStatusChangeJob;
import gov.nih.brics.job.UpdateAccountPrivilegesExpirationJob;
import gov.nih.brics.job.DatasetDataDeletionJob;
import gov.nih.brics.job.DatasetStatusEmailJob;
import gov.nih.brics.job.DeleteDatasetJob;
import gov.nih.brics.pojo.TimestampType;
import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.RdfGenConstants;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hp.hpl.jena.datatypes.RDFDatatype;
import com.hp.hpl.jena.datatypes.TypeMapper;

/**
 * This class will have all the methods that are to be run at periodic schedule. The pattern is to implement the job
 * code in the Jobs class and in every scheduled method in this class, call the particular method in the Jobs class
 * which implements the code to be run.
 * 
 * @author vivek pachauri
 * 
 */

@Component
@Scope("singleton")
public class JobScheduler {

	private static Logger log = Logger.getLogger(JobScheduler.class);
	public static boolean RUN_SCHEDULED_RDF_UPLOAD = false;

	public JobScheduler() {
		if (log.isInfoEnabled() == true) {
			log.info("Registering parser for Timestamp");
		}
		RDFDatatype type = TimestampType.timestampType;
		TypeMapper.getInstance().registerDatatype(type);
	}

	@Autowired
	PasswordExpirationEmailJob passwordExpirationEmailJob;

	@Autowired
	AccountExpirationEmailJob accountExpirationEmailJob;

	@Autowired
	RDFJobs rdfJobs;

	@Autowired
	DeleteDatasetJob deleteDataset;
	
	@Autowired
	DataElementStatusUpdateJob dataElementStatusUpdateJob;

	@Autowired
	DeleteLoginPendingApprovedAccountJob deleteLoginPendingApprovedAccountJob;

	@Autowired
	CheckIUStatusAndRetrievManifestJob checkIUStatusAndRetrievManifestJob;

	@Autowired
	RetrieveIUBiosampleCatalogJob retrieveIUBiosampleCatalogjob;

	@Autowired
	DatasetDataDeletionJob datasetDataDeletionJob;

	@Autowired
	StuckDatasetStatusChangeJob stuckDatasetStatusChangeJob;

	@Autowired
	AdvancedViewCreateFileJob advancedViewCreateFileJob;

	@Autowired
	AccountEmailReportJob accountEmailReportJob;

	@Autowired
	protected ModulesConstants modulesConstants;

	@Autowired
	protected RdfGenConstants rdfGenConstants;

	@Autowired
	protected PublicSiteStudiesCreateFileJob publicSiteStudiesCreateFileJob;
	
	@Autowired
	UpdateAccountPrivilegesExpirationJob updateAccountPrivilegesExpirationSoonJob;
	
	@Autowired
	DownloadDatasetDeleteJob downloadDatasetDeleteJob;
	
	@Autowired
	DatasetStatusEmailJob datasetEmailStatusJob;
	
	@Autowired
	AccountReportingEmailJob accountReportingEmailJob;
	

	@Value("${common.portal.noOfDayToDeleteDatasets}")
	private String noOfDayToDeleteDatasets;
	
	@Value("${common.email.renewPassword.enabled}")
	private boolean emailRenewPasswordEnabled;
	
	@Value("${common.rdf.generate.enabled}")
	private boolean rdfGenerateEnabled;
	
	@Value("${common.dataElement.updateStatus.enabled}")
	private boolean dataElementUpdateStatusEnabled;
	
	@Value("${common.account.removeUnused.enabled}")
	private boolean accountRemoveUnusedEnabled;
	
	@Value("${common.portal.checkIUStatus.enabled}")
	private boolean portalCheckIUStatusEnabled;
	
	@Value("${common.portal.retrieveIUCatalog.enabled}")
	private boolean portalRetrieveIUCatalogEnabled;
	
	@Value("${common.dataset.data.deletion.enabled}")
	private boolean datasetDataDeletionEnabled;

	@Value("${common.portal.datasetDelete.enabled}")
	private boolean datasetDeletionEnabled;
	
	@Value("${common.dataset.stuck.statusChange.enabled}")
	private boolean datasetStuckStatusChangeEnabled;
	
	@Value("${common.advanced.view.enabled}")
	private boolean advancedViewEnabled;
	
	@Value("${common.publicSite.studyMetrics.enabled}")
	private boolean publicSiteStudyMetricsEnabled;
	
	@Value("${common.account.emailReport.enabled}")
	private boolean accountEmailReportEnabled;
	
	@Value("${common.account.updatePrivilegesExpirationSoon.enabled}")
	private boolean accountUpdatePrivilegesExpirationSoonEnabled;
	
	@Value("${common.dataset.downloadDatasetDelete.enabled}")
	private boolean datasetDownloadDatasetDeleteEnabled;
	
	@Value("${common.dataset.datasetEmailStatus.enabled}")
	private boolean datasetEmailStatusEnabled;
	
	@Value("${common.account.accountReporting.enabled}")
	private boolean accountReportingEmailEnabled;
	
	/*
	 * @Autowired ThreadPoolTaskScheduler scheduler;
	 */

	/**
	 * This method will run the email job every day at midnight
	 */
	@Scheduled(cron = "${common.email.renewPassword.cronExpression}")
	public void scheduleEmailJob() {

		log.info("executing scheduled email job");
		if(emailRenewPasswordEnabled) {
			passwordExpirationEmailJob.doJob();
			accountExpirationEmailJob.doJob();
		}
	}

	@Scheduled(cron = "${common.rdf.generate.cronExpression}")
	// @Scheduled(fixedDelay = 5000)
	// @Scheduled(fixedRate = 5000)
	public void scheduleRDFGeneration() {

		log.info("executing RDF Generation Job!");
		if(rdfGenerateEnabled) {
			rdfJobs.generateAndUploadRDF();
		}
	}

	//donont forget to all prop to local/prop folder, added for this job
	@Scheduled(cron = "${common.portal.datasetDelete.cronExpression}")
	public void scheduleDatasetDeleteStatus() {

		log.info("executing Dataset delete Status Update Job");
		if(datasetDeletionEnabled) {
			datasetDeletionEnabled = false;
			deleteDataset.deleteDatasetJob(noOfDayToDeleteDatasets);
		}
	}
	
	@Scheduled(cron = "${common.dataElement.updateStatus.cronExpression}")
	public void scheduleDataElementUpdateStatus() {

		log.info("executing Data Element Status Update Job");
		if(dataElementUpdateStatusEnabled) {
			dataElementStatusUpdateJob.updateDataElementStatus();
		}
	}

	@Scheduled(cron = "${common.account.removeUnused.cronExpression}")
	public void scheduleRmoveUnusedAccountDeletion() {
		log.info("executing isClientPd flag:\t" + modulesConstants.getEraseUnsusedAccounts());
		if (modulesConstants.getEraseUnsusedAccounts() && accountRemoveUnusedEnabled) {
			deleteLoginPendingApprovedAccountJob.deleteLoginPendingAccountOlderThanTwoBusinessDays();
		}
	}

	@Scheduled(cron = "${common.portal.checkIUStatus.cronExpression}")
	public void checkIUStatusAndRetrieveManifest() {
		log.info("executing Check IU Status and Retrieve Manifest Job");
		if(portalCheckIUStatusEnabled) {
			checkIUStatusAndRetrievManifestJob.checkIUStatusAndRetrieveManifest();
		}
	}

	@Scheduled(cron = "${common.portal.retrieveIUCatalog.cronExpression}")
	public void retrieveIUBiosampleCatalog() {
		log.info("Executing retrieveIUBiosampleCatalog from scheduler");
		if(portalRetrieveIUCatalogEnabled) {
			retrieveIUBiosampleCatalogjob.retrieveIUBiosampleCatalog();
		}
		log.info("Ending retrieveIUBiosampleCatalog from scheduler");
	}

	@Scheduled(cron = "${common.dataset.data.deletion.cronExpression}")
	public void deleteDatasetData() {
		log.info("Executing datasetDataDeletionJob from scheduler");
		if(datasetDataDeletionEnabled) {
			this.datasetDataDeletionJob.doJob();
		}
		log.info("Ending datasetDataDeletionJob from scheduler");
	}

	@Scheduled(cron = "${common.dataset.stuck.statusChange.cronExpression}")
	public void stuckDatasetStatusChange() {
		log.info("Executing stuckDatasetStatusChangeJob from scheduler");
		if(datasetStuckStatusChangeEnabled) {
			stuckDatasetStatusChangeJob.stuckDatasetStatusChange();
		}
		log.info("Ending stuckDatasetStatusChangeJob from scheduler");
	}

	@Scheduled(cron = "${common.advanced.view.cronExpression}")
	public void scheduleAdvancedViewCreateFile() {

		log.info("executing Advanced View Create File Job!");
		if (advancedViewEnabled) {
			advancedViewCreateFileJob.storeJson();
		}
	}

	@Scheduled(cron = "${common.publicSite.studyMetrics.cronExpression}")
	public void createPublicSiteStudyFile() {
		log.info("Executing publicSiteStudiesCreateFileJob from scheduler");
		if(publicSiteStudyMetricsEnabled){
			publicSiteStudiesCreateFileJob.storeJson();			
		}
		log.info("Ending publicSiteStudiesCreateFileJob from scheduler");
	}

	@Scheduled(cron = "${common.account.emailReport.cronExpression}")
	public void scheduleAccountEmailReport() {

		log.info("executing Account Email Report Job!");
		if(accountEmailReportEnabled) {
			accountEmailReportJob.doJob();
		}
	}
	
	
	@Scheduled(cron = "${common.account.updatePrivilegesExpirationSoon.cronExpression}")
	public void updateAccountPrivilegesExpirationSoon() {
		log.info("Executing updateAccountPrivilegesExpirationJob from scheduler");
		if(accountUpdatePrivilegesExpirationSoonEnabled) {
			updateAccountPrivilegesExpirationSoonJob.doJob();
		}
		log.info("Ending updateAccountPrivilegesExpirationJob from scheduler");
	}
	
	@Scheduled(cron = "${common.dataset.downloadDatasetDelete.cronExpression}")
	public void downloadDatasetDelete(){
		log.info("Executing downloadDatasetDeleteJob from scheduler");
		if(datasetDownloadDatasetDeleteEnabled) {
			downloadDatasetDeleteJob.doJob();
		}
		log.info("Ending downloadDatasetDeleteJob from scheduler");
		
	}
	
	@Scheduled(cron = "${common.dataset.datasetEmailStatus.cronExpression}")
	public void datasetEmailStatus(){
		log.info("Executing datasetEmailStatus from scheduler");
		if(datasetEmailStatusEnabled) {
			datasetEmailStatusJob.doJob();
		}
		log.info("Ending datasetEmailStatusJob from scheduler");
		
	}
	
	@Scheduled(cron = "${common.account.accountReporting.cronExpression}")
	public void accountReportingEmail(){
		log.info("Executing accountReportingEmailJob from scheduler");
		if(accountReportingEmailEnabled) {
			accountReportingEmailJob.doJob();
		}
		log.info("Ending accountReportingEmailJob from scheduler");
		
	}
	
	
	

	/********************
	 * 
	 * Getters and Setters
	 * 
	 *********************/
	public PasswordExpirationEmailJob getPasswordExpirationEmailJob() {
		return passwordExpirationEmailJob;
	}

	public void setPasswordExpirationEmailJob(PasswordExpirationEmailJob passwordExpirationEmailJob) {
		this.passwordExpirationEmailJob = passwordExpirationEmailJob;
	}
}

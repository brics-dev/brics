package gov.nih.tbi.repository.portal;


import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.QueryParameter;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.BadParameterException;
import gov.nih.tbi.commons.service.RepositoryManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.commons.service.WebServiceManager;
import gov.nih.tbi.commons.service.util.MailEngine;
import gov.nih.tbi.commons.util.PaginationData;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.ws.RestDictionaryProvider;
import gov.nih.tbi.idt.ws.IdtColumnDescriptor;
import gov.nih.tbi.idt.ws.IdtFilterDescription;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.IdtRequest;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;
import gov.nih.tbi.portal.PortalUtils;
import gov.nih.tbi.repository.model.SessionDataset;
import gov.nih.tbi.repository.model.hibernate.BasicDataset;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetDataStructure;
import gov.nih.tbi.repository.model.hibernate.DatasetFile;
import gov.nih.tbi.repository.model.hibernate.DownloadFileDataset;
import gov.nih.tbi.repository.model.hibernate.DownloadPackage;
import gov.nih.tbi.repository.model.hibernate.EventLog;
import gov.nih.tbi.repository.model.hibernate.EventLogDocumentation;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.service.exception.DataLoaderException;
import gov.nih.tbi.taglib.datatableDecorators.DatasetIdtListDecorator;
import gov.nih.tbi.taglib.datatableDecorators.EventLogListIdtDecorator;



/**
 * Action class for Datasets
 * 
 * @author Francis Chen
 */
public class DatasetAction extends BaseRepositoryAction {

	private static final long serialVersionUID = -1775293419068905185L;
	static Logger logger = Logger.getLogger(DatasetAction.class);

	/*****************************************************************/
	@Autowired
	WebServiceManager webServiceManager;

	@Autowired
	RepositoryManager repositoryManager;

	@Autowired
	MailEngine mailEngine;

	@Autowired
	ModulesConstants modulesConstants;

	@Autowired
	protected AccountManager accountManager;

	// Pagination Constants
	private Integer numSearchResults;
	private Integer page;
	private Integer pageSize;
	private Boolean ascending;
	private String sort;
	private String key;
	private Long filterId;
	private Long ownerId;

	List<Dataset> datasetList;
	private Map<Long, EntityMap> permissionList;

	Dataset currentDataset;

	Set<DatasetFile> datasetFileSet;

	EventLog currentDatasetLatestEvent;

	String statusReason;

	private String delComment;

	private Set<EventLog> eventLogList;

	private String selectedDatasets;

	private String datasetStatusSelect;

	private DatasetStatus currentDatasetStatus;

	private DatasetStatus requestedDatasetStatus;

	private boolean isBulkRequest;

	private boolean isRequestApprove;

	private DatasetStatus newStatus;

	private String statusChangeComment;

	private Study currentLazyStudy;


	/*****************************************************************/
	public String getCurrentDatasetStatusView() {
		return currentDataset.getDatasetStatus().getName();
	}

	public String getDatasetSelectStatusName() {

		for (DatasetStatus status : DatasetStatus.values()) {
			if (datasetStatusSelect.equals(status.getVerb())) {
				newStatus = status;
				break;
			}
		}
		return newStatus.getName();
	}

	public Boolean getIsNoChangeStatusOptions() {

		DatasetStatus datasetStatus = getSessionDataset().getDataset().getDatasetStatus();

		if (DatasetStatus.ARCHIVED.equals(datasetStatus) || DatasetStatus.DELETED.equals(datasetStatus)
				|| DatasetStatus.LOADING.equals(datasetStatus))
			return true;

		return false;
	}

	public boolean getUploading() {
		return DatasetStatus.UPLOADING.equals(getSessionDataset().getDataset().getDatasetStatus());
	}

	public Boolean getHasPermissionDownload() {
		return accountManager.getDatasetAccess(getAccount(), getSessionDataset().getDataset(), PermissionType.READ);
	}

	public boolean getIsDownloadable() {
		switch (getCurrentDataset().getDatasetStatus()) {
			case ARCHIVED: // if dataset is archived, only admins can download
				return this.accountManager.getDatasetAccess(getAccount(), getCurrentDataset(), PermissionType.ADMIN);
			case DELETED: // if dataset is deleted, uploading, loading, or errored, no one can download
			case UPLOADING:
			case ERROR:
			case LOADING:
				return false;
			default: // default check for all other statuses
				return this.accountManager.getDatasetAccess(getAccount(), getCurrentDataset(), PermissionType.READ);
		}
	}

	public boolean getIsRequestApprove() {
		return isRequestApprove;
	}

	public void setRequestApprove(boolean isRequestApprove) {
		this.isRequestApprove = isRequestApprove;
	}

	public String getStatusChangeComment() {
		return statusChangeComment;
	}

	public void setStatusChangeComment(String statusChangeComment) {
		this.statusChangeComment = statusChangeComment;
	}

	public DatasetStatus getNewStatus() {
		return newStatus;
	}

	public void setNewStatus(DatasetStatus newStatus) {
		this.newStatus = newStatus;
	}

	public boolean getIsBulkRequest() {

		boolean isBulkRequest = false;

		List<Dataset> datasets = getSessionDatasetList().getDatasets();

		if (datasets.get(0).getDatasetRequestStatus() != null)
			isBulkRequest = true;

		return isBulkRequest;
	}

	public boolean getIsDatasetRequestDelete() {

		List<Dataset> datasets = getSessionDatasetList().getDatasets();
		Boolean deleteRequest = (Boolean) getSession().getAttribute("deleteDatasetRequest");
		datasetStatusSelect = (String) getSession().getAttribute("datasetStatusSelect");

		if (datasets.get(0).getDatasetRequestStatus() != null) {
			if ((datasets.get(0).getDatasetRequestStatus() == DatasetStatus.DELETED
					&& datasetStatusSelect.equals(PortalConstants.DATASET_REQUEST_APPROVE))) {
				getSession().setAttribute("deleteDatasetRequest", true);
				return true;
			}
		}

		if (datasetStatusSelect.equals(DatasetStatus.DELETED.getVerb())) {
			getSession().setAttribute("deleteDatasetRequest", true);
			return true;
		}

		return deleteRequest;
	}

	public HashMap<Long, String> getAccessRecord() {

		HashMap<Long, String> accesRecord = new HashMap<Long, String>();

		List<Dataset> datasets = getSessionDatasetList().getDatasets();

		for (Dataset dataset : datasets) {
			if (repositoryManager.countAccessRecords(dataset.getId()) > 0)
				accesRecord.put(dataset.getId(), PortalConstants.ACCESSRECORD_YES);
			else
				accesRecord.put(dataset.getId(), PortalConstants.ACCESSRECORD_NO);
		}
		return accesRecord;
	}

	
	public String getEventLogListOutput() {
		try {
			if (eventLogList == null) {
				eventLogList = repositoryManager.getEventLogs(getCurrentDataset().getId(), EntityType.DATASET);
			}
			
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<EventLog> outputList = new ArrayList<EventLog>(eventLogList);
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new EventLogListIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}
	
	public void setBulkRequest(boolean isBulkRequest) {
		this.isBulkRequest = isBulkRequest;
	}

	public DatasetStatus getCurrentDatasetStatus() {
		return currentDatasetStatus;
	}

	public void setCurrentDatasetStatus(DatasetStatus currentDatasetStatus) {
		this.currentDatasetStatus = currentDatasetStatus;
	}

	public DatasetStatus getRequestedDatasetStatus() {
		return requestedDatasetStatus;
	}

	public void setRequestedDatasetStatus(DatasetStatus requestedDatasetStatus) {
		this.requestedDatasetStatus = requestedDatasetStatus;
	}

	public String getDatasetStatusSelect() {
		return datasetStatusSelect;
	}

	public void setDatasetStatusSelect(String datasetStatusSelect) {
		this.datasetStatusSelect = datasetStatusSelect;
	}

	public String getSelectedDatasets() {
		return selectedDatasets;
	}

	public void setSelectedDatasets(String selectedDatasets) {
		this.selectedDatasets = selectedDatasets;
	}

	public Set<EventLog> getEventLogList() {
		return eventLogList;
	}

	public void setEventLogList(Set<EventLog> eventLogList) {
		this.eventLogList = eventLogList;
	}

	public Long getFilterId() {

		return filterId;
	}

	public void setFilterId(Long filterId) {

		this.filterId = filterId;
	}

	public Long getOwnerId() {

		return ownerId;
	}

	public void setOwnerId(Long ownerId) {

		this.ownerId = ownerId;
	}

	public String getMethod() {

		return getCurrentMethod();
	}

	public List<Dataset> getDatasetList() {

		if (datasetList == null) {
			datasetList = new ArrayList<Dataset>();
		}
		return datasetList;
	}

	public void setDatasetList(List<Dataset> datasetList) {

		this.datasetList = datasetList;
	}

	public Integer getNumSearchResults() {

		return numSearchResults;
	}

	public void setNumSearchResults(Integer numSearchResults) {

		this.numSearchResults = numSearchResults;
	}

	public Integer getPage() {

		return page;
	}

	public void setPage(Integer page) {

		this.page = page;
	}

	public Integer getPageSize() {

		return pageSize;
	}

	public void setPageSize(Integer pageSize) {

		this.pageSize = pageSize;
	}

	public Boolean getAscending() {

		return ascending;
	}

	public void setAscending(Boolean ascending) {

		this.ascending = ascending;
	}

	public String getSort() {

		return sort;
	}

	public void setSort(String sort) {

		this.sort = sort;
	}

	public String getKey() {

		return key;
	}

	public void setKey(String key) {

		this.key = key;
	}

	public Dataset getCurrentDataset() {

		return getSessionDataset().getDataset();
	}

	public Set<DatasetFile> getDatasetFileSet() {

		return datasetFileSet;
	}


	public String getStatusReason() {

		return statusReason;
	}

	public void setStatusReason(String statusReason) {

		this.statusReason = statusReason;
	}

	public String getDeletionComment() {

		return this.delComment;
	}

	public void setDeletionComment(String _val) {

		this.delComment = _val;
	}

	public void setCurrentDataset(Dataset currentDataset) {

		this.currentDataset = currentDataset;
	}

	public Study getCurrentStudy() {

		return repositoryManager.getStudy(getSessionDataset().getDataset().getStudy().getId());
	}

	public Study getCurrentLazyStudy() {

		if (currentLazyStudy == null) {
			this.currentLazyStudy =
					repositoryManager.getStudyExcludingDatasets(getSessionDataset().getDataset().getStudy().getId());
		}
		return currentLazyStudy;
	}

	public List<String> getAssociatedDataStructure() throws MalformedURLException, UnsupportedEncodingException {

		currentDataset = getSessionDataset().getDataset();

		List<Long> dsIdList = new ArrayList<Long>();

		for (DatasetDataStructure datasetDataStructure : currentDataset.getDatasetDataStructure()) {
			dsIdList.add(datasetDataStructure.getDataStructureId());
		}

		// The Long -1L provides the default URL. In this case there is only 1 dictionary to connect
		// to.
		String dictionaryUrl = modulesConstants.getModulesDDTURL(ServiceConstants.DEFAULT_PROVIDER);
		RestDictionaryProvider restProvider =
				new RestDictionaryProvider(dictionaryUrl, PortalUtils.getProxyTicket(dictionaryUrl));
		List<String> dsNameList = restProvider.getFormStructureNamesByIds(dsIdList);

		return dsNameList;
	}

	public boolean getIsReady() {

		return !DatasetStatus.UPLOADING.equals(getSessionDataset().getDataset().getDatasetStatus())
				&& !DatasetStatus.LOADING.equals(getSessionDataset().getDataset().getDatasetStatus())
				&& !DatasetStatus.ERROR.equals(getSessionDataset().getDataset().getDatasetStatus());
	}

	public boolean getIsDeletable() {

		DatasetStatus currentStatus = this.currentDataset.getDatasetStatus();

		boolean result = currentStatus.getIsDeletable();

		return result;
	}

	public boolean getErrorLoading() {

		return DatasetStatus.ERROR.equals(getSessionDataset().getDataset().getDatasetStatus());
	}

	/**
	 * Checks if the dataset is in the user's download queue.
	 * 
	 * @return true if dataset is in queue, false otherwise.
	 */
	public Boolean getInDownloadQueue() {

		DownloadPackage downloadPackage = repositoryManager
				.getDownloadPackageByDatasetAndUser(new DownloadFileDataset(getCurrentDataset()), getUser());

		// returns true if the list of ids contains the current
		return downloadPackage != null;
	}

	/******************************************************************/

	/**
	 * A helper function to serve the total number of results pages to the jsp
	 * 
	 * @return
	 */
	public Integer getNumPages() {

		return (int) Math.ceil(((double) numSearchResults) / ((double) pageSize));
	}

	/**
	 * Gets the owner of the study
	 * 
	 * @return
	 */
	public User getStudyOwner() {

		return (accountManager.getEntityOwnerAccount(getCurrentStudy().getId(), EntityType.STUDY)).getUser();
	}

	/**
	 * If the request status is not null, then theres a request pending
	 * 
	 * @return
	 */
	public boolean getIsRequest() {

		return getCurrentDataset().getDatasetRequestStatus() != null;
	}

	/**
	 * Returns true if user is in admin or studyAdmin namespace
	 * 
	 * @return
	 */
	public boolean getInAdmin() {

		boolean out = (PortalConstants.NAMESPACE_ADMIN.equals(getNameSpace())
				|| PortalConstants.NAMESPACE_STUDYADMIN.equals(getNameSpace()));
		return out;
	}

	/**
	 * Return the number of access_records for this dataset
	 * 
	 * @return
	 */
	public int getAccessRecordCount() {
		return repositoryManager.countAccessRecords(getSessionDataset().getDataset().getId());
	}

	/******************************************************************/

	/**
	 * Get the dataset ID from the URL parameter datasetId
	 * 
	 * @return
	 * @throws UserPermissionException
	 */
	public Dataset getDatasetFromParam() throws UserPermissionException, BadParameterException {

		String prefixedId = super.getRequestParameter(PortalConstants.PREFIXED_ID);

		if (prefixedId == null) {
			throw new BadParameterException(QueryParameter.DATASET_PREFIXED_ID, null);
		}

		Account account = super.getAccount();

		Dataset result = repositoryManager.getDatasetByPrefixedIdWithStudyInfo(prefixedId, account);

		this.datasetFileSet = new HashSet<DatasetFile>();
		datasetFileSet.addAll(repositoryManager.getDatasetFiles(result.getId()));
		return result;
	}

	public Dataset getDatasetFromId() {

		String datasetId = getRequest().getParameter(PortalConstants.DATASET_ID);

		try {
			return repositoryManager.getDataset(Long.valueOf(datasetId));
		} catch (NumberFormatException e) {
			throw new NumberFormatException("Illegal dataset ID");
		} catch (NullPointerException e) {
			throw new RuntimeException("Study with ID = " + Long.valueOf(datasetId) + " not found.");
		}
	}

	public String delete() throws UserPermissionException, BadParameterException {

		if (currentDataset == null) {
			currentDataset = getDatasetFromParam();
		}

		// Set current study to prevent nullpointer on redirect after delete
		if (currentDataset != null && currentDataset.getStudy() != null && currentDataset.getStudy().getId() != null) {
			Study currentStudy = repositoryManager.getStudyExcludingDatasets(currentDataset.getStudy().getId());
			if (currentStudy != null) {
				getSessionStudy().setStudy(currentStudy);
			}
		}

		Account account = super.getAccount();

		accountManager.checkEntityAccess(account, EntityType.DATASET, currentDataset.getId(), PermissionType.WRITE);

		User user = account.getUser();
		EventLog eventLog = new EventLog();

		try {
			repositoryManager.deleteDataset(currentDataset, user, this.delComment, eventLog);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		return PortalConstants.REDIRECT;
	}

	/**
	 * This method handles viewing of a dataset
	 * 
	 * @return
	 * @throws UserPermissionException
	 */
	public String view() throws UserPermissionException, BadParameterException {

		String result = null;

		try {
			this.currentDataset = this.getDatasetFromParam();

			eventLogList = repositoryManager.getEventLogs(currentDataset.getId(), EntityType.DATASET);

			SessionDataset currentSessionDataset = super.getSessionDataset();

			currentSessionDataset.setDataset(this.currentDataset);
			getSessionStudy().setStudy(this.currentDataset.getStudy());
			getSessionSupportDocList().clear();

			result = PortalConstants.ACTION_VIEW;
		} catch (UserPermissionException permEx) {
			result = PortalConstants.EXCEPTION;
		} catch (BadParameterException paramEx) {
			result = PortalConstants.EXCEPTION;
		}

		return result;
	}

	/**
	 * This method handles the approval of a dataset status
	 * 
	 * @return
	 * @throws MessagingException
	 * @throws UserPermissionException
	 * @throws SQLException
	 */
	public String approve() throws MessagingException, UserPermissionException, BadParameterException, SQLException {

		currentDataset = getCurrentDataset();

		// Get word for requested status as a verb
		String requestStatusVerb = currentDataset.getDatasetRequestStatus().getVerb().toLowerCase();

		Set<EventLogDocumentation> currentDocSet = getSessionSupportDocList().getSupportingDocumentation();

		EventLog eventLog = new EventLog();

		if (currentDocSet != null && !currentDocSet.isEmpty()) {
			for (EventLogDocumentation sd : currentDocSet) {
				if (sd.getUserFile() != null) {
					sd.setUserFile(repositoryManager.saveUserFile(sd.getUserFile()));
				}
				sd.setEventLog(eventLog);
			}
		}
		eventLog.setSupportingDocumentationSet(currentDocSet);

		// save eventLog
		repositoryManager.saveDatasetEventLog(getAccount(), currentDataset, eventLog, statusChangeComment);

		// TODO: (08/07/2018) We have two places where we approve status change, here and RepositoryManagerImpl.approveDatasetStatus.
		// They should be merged in the future.
		// copy the requested status over to current status and set request to
		// null
		if (currentDataset.getDatasetRequestStatus() != null) {
			currentDataset.setDatasetStatus(currentDataset.getDatasetRequestStatus());

			// since we are sharing this dataset, set a new share date
			if (DatasetStatus.SHARED.equals(currentDataset.getDatasetRequestStatus())) {
				currentDataset.setShareDate(new Date());
			}

			currentDataset.setDatasetRequestStatus(null);
		}

		// save dataset
		currentDataset = repositoryManager.saveDataset(getAccount(), currentDataset);

		boolean isLocalEnv = this.modulesConstants.getEnvIsLocal();

		// construct subject line and message text to send through email
		if (!isLocalEnv) {
			String subject =
					this.getText(PortalConstants.MAIL_RESOURCE_ACCEPTED_DATASET + PortalConstants.MAIL_RESOURCE_SUBJECT,
							Arrays.asList(currentDataset.getPrefixedId(), requestStatusVerb));

			String messageText =
					this.getText(PortalConstants.MAIL_RESOURCE_COMMON + PortalConstants.MAIL_RESOURCE_HEADER,
							Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
									modulesConstants.getModulesOrgEmail(getDiseaseId()),
									currentDataset.getSubmitter().getFullName(), statusReason,
									currentDataset.getPrefixedId(), requestStatusVerb))
							+ this.getText(
									PortalConstants.MAIL_RESOURCE_ACCEPTED_DATASET + PortalConstants.MAIL_RESOURCE_BODY,
									Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
											modulesConstants.getModulesOrgEmail(getDiseaseId()),
											currentDataset.getSubmitter().getFullName(), statusReason,
											currentDataset.getPrefixedId(), requestStatusVerb))
							+ this.getText(PortalConstants.MAIL_RESOURCE_COMMON + PortalConstants.MAIL_RESOURCE_FOOTER,
									Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
											modulesConstants.getModulesOrgEmail(getDiseaseId())));


			mailEngine.sendMail(subject, messageText, null, currentDataset.getSubmitter().getEmail());
		} else {
			logger.info("[LOCAL ENV ONLY MESSAGE] Criteria met to send an email for the approval of dataset: "
					+ currentDataset.getName());
		}

		if (DatasetStatus.DELETED.equals(currentDataset.getDatasetStatus())) {
			return delete();
		}

		return PortalConstants.ACTION_VIEW;
	}

	/**
	 * This method handles the rejection of dataset status
	 * 
	 * @return
	 * @throws MessagingException
	 * @throws SQLException
	 */
	public String reject() throws MessagingException, SQLException {

		currentDataset = getCurrentDataset();

		// Get word for requested status as a verb
		String requestStatusVerb = currentDataset.getDatasetRequestStatus().getVerb().toLowerCase();

		Set<EventLogDocumentation> currentDocSet = getSessionSupportDocList().getSupportingDocumentation();

		EventLog eventLog = new EventLog();

		if (currentDocSet != null && !currentDocSet.isEmpty()) {
			for (EventLogDocumentation sd : currentDocSet) {
				if (sd.getUserFile() != null) {
					sd.setUserFile(repositoryManager.saveUserFile(sd.getUserFile()));
				}
				sd.setEventLog(eventLog);
			}
		}
		eventLog.setSupportingDocumentationSet(currentDocSet);

		// save eventLog
		repositoryManager.saveDatasetEventLog(getAccount(), currentDataset, eventLog, statusChangeComment);

		// remove status request
		currentDataset.setDatasetRequestStatus(null);

		currentDataset = repositoryManager.saveDataset(getAccount(), currentDataset);

		boolean isLocalEnv = this.modulesConstants.getEnvIsLocal();

		// send the email to the user with reason for rejection
		if (!isLocalEnv) {
			String subject =
					this.getText(PortalConstants.MAIL_RESOURCE_REJECTED_DATASET + PortalConstants.MAIL_RESOURCE_SUBJECT,
							Arrays.asList(currentDataset.getPrefixedId(), requestStatusVerb));

			String messageText =
					this.getText(PortalConstants.MAIL_RESOURCE_COMMON + PortalConstants.MAIL_RESOURCE_HEADER,
							Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
									modulesConstants.getModulesOrgPhone(getDiseaseId()),
									currentDataset.getSubmitter().getFullName(), statusReason,
									currentDataset.getPrefixedId(), requestStatusVerb))
							+ this.getText(
									PortalConstants.MAIL_RESOURCE_REJECTED_DATASET + PortalConstants.MAIL_RESOURCE_BODY,
									Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
											modulesConstants.getModulesOrgPhone(getDiseaseId()),
											currentDataset.getSubmitter().getFullName(), statusReason,
											currentDataset.getPrefixedId(), requestStatusVerb))
							+ this.getText(PortalConstants.MAIL_RESOURCE_COMMON + PortalConstants.MAIL_RESOURCE_FOOTER,
									Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
											modulesConstants.getModulesOrgEmail(getDiseaseId())));

			mailEngine.sendMail(subject, messageText, null, currentDataset.getSubmitter().getEmail());
		} else {
			logger.info("[LOCAL ENV ONLY MESSAGE] Criteria met to send an email for the denial of dataset: "
					+ currentDataset.getName());
		}

		return PortalConstants.ACTION_VIEW;
	}

	/**
	 * Direct the user to the list user page if they are in the admin namespace
	 * 
	 * @return
	 */
	public String list() {

		if (!getInAdmin()) {
			return PortalConstants.ACTION_ERROR;
		}
		return PortalConstants.ACTION_LIST;
	}

	/**
	 * Get a single page of datasets based on the page and filter data.
	 * 
	 * @return
	 */
	public String search() {
		ownerId = null;

		PaginationData pageData = new PaginationData();
		datasetList = repositoryManager.searchDatasets(key, getAccount(), filterId, ownerId, pageData);
		numSearchResults = pageData.getNumSearchResults();

		List<Long> ids = new ArrayList<Long>();

		// because datasetList is ACTUALLY a list of BasicDataset but let's handle both
		for (Object o : datasetList) {
			if (o instanceof BasicDataset) {
				ids.add(((BasicDataset) o).getId());
			} else if (o instanceof Dataset) {
				ids.add(((Dataset) o).getId());
			}
		}

		List<EntityMap> ownerEntityList = accountManager.getOwnerEntityMaps(ids, EntityType.DATASET);
		setPermissionList(ownerEntityList);

		return PortalConstants.ACTION_SEARCH;
	}

	public String searchIdt() {
		ownerId = null;
		try {
			IdtInterface idt = new Struts2IdtInterface();
			IdtRequest request = idt.getRequest();

			updateDatasetOrder(request.getOrderColumn());

			Long currentDatasetStatus = null;
			Long requestDatasetStatus = null;
			for (IdtFilterDescription idtFilter : request.getFilters()) {
				if (idtFilter.getName().equals("Status: All")) {
					String value = idtFilter.getValue();
					if (value.equals("private")) {
						currentDatasetStatus = 0L;
					} else if (value.equals("requestedDeletion")) {
						currentDatasetStatus = 0L;
						requestDatasetStatus = 3L;
					} else if (value.equals("requestedSharing")) {
						currentDatasetStatus = 0L;
						requestDatasetStatus = 1L;
					} else if (value.equals("requestedArchive")) {
						currentDatasetStatus = 0L;
						requestDatasetStatus = 2L;
					} else if (value.equals("shared")) {
						currentDatasetStatus = 1L;
					} else if (value.equals("sharedRequestedArchive")) {
						currentDatasetStatus = 1L;
						requestDatasetStatus = 2L;
					} else if (value.equals("archived")) {
						currentDatasetStatus = 2L;
					} else if (value.equals("deleted")) {
						currentDatasetStatus = 3L;
					} else if (value.equals("errors")) {
						currentDatasetStatus = 6L;
					} else if (value.equals("uploading")) {
						currentDatasetStatus = 4L;
					} else if (value.equals("loadingData")) {
						currentDatasetStatus = 5L;
					}
				} else if (idtFilter.getName().equals("Ownership: all")) {
					if (idtFilter.getValue().equals("mine")) {
						ownerId = 0L;
					}
				}
			}

			// TODO: translate filters here to go into the dao
			String searchKey = request.getSearchVal();
			
			List<String> searchColumn = new ArrayList<String>();
			for (IdtColumnDescriptor icd : request.getColumnDescriptions()) {
				if (icd.isSearchable()) {
					searchColumn.add(icd.getData());
				}
			}

			PaginationData pageData = new PaginationData(request.getPageNumber(), request.getLength(), ascending, sort);
			datasetList = repositoryManager.searchDatasets(searchKey, searchColumn, getAccount(), currentDatasetStatus,
					requestDatasetStatus, ownerId, pageData);
			numSearchResults = pageData.getNumSearchResults();
			idt.setTotalRecordCount(pageData.getNumSearchResults());
			idt.setFilteredRecordCount(pageData.getNumFilteredResults());

			List<Long> ids = new ArrayList<Long>();

			// because datasetList is ACTUALLY a list of BasicDataset but let's handle both
			for (Dataset o : datasetList) {
				ids.add(o.getId());
			}

			List<EntityMap> ownerEntityList = accountManager.getOwnerEntityMaps(ids, EntityType.DATASET);
			setPermissionList(ownerEntityList);

			idt.setList(new ArrayList<Dataset>(datasetList));
			idt.decorate(new DatasetIdtListDecorator());

			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	private void updateDatasetOrder(IdtColumnDescriptor orderColumn) {
		sort = "name";
		if (orderColumn != null) {
			String orderData = orderColumn.getData();
			sort = orderData;
			// the sql injection protection and mapping happens in the DAO
			ascending = orderColumn.getOrderDirection().equals(IdtRequest.ORDER_DESCENDING);
		}
	}

	/**
	 * Loads dataset and display as lightbox
	 * 
	 * @return
	 * @throws UserPermissionException
	 */
	public String viewLightbox() throws UserPermissionException, BadParameterException {
		logger.info("viewLightbox");
		currentDataset = getDatasetFromParam();

		// if we dont throw exception here, it won't fail in an obvious way.
		if (currentDataset == null) {
			throw new NullPointerException();
		}

		eventLogList = repositoryManager.getEventLogs(currentDataset.getId(), EntityType.DATASET);
		
		getSessionDataset().setDataset(currentDataset);
		return PortalConstants.ACTION_LIGHTBOX;
	}

	/**
	 * Reloads the data submitted to this dataset. Only datasets with status ERROR can be reloaded this way.
	 * @throws DataLoaderException 
	 */
	public void reloadData() throws DataLoaderException {

		currentDataset = getSessionDataset().getDataset();
		if (!DatasetStatus.ERROR.equals(currentDataset.getDatasetStatus())) {
			throw new UnsupportedOperationException("Only dataset with error during upload can reload the data");
		}

		repositoryManager.loadDataFromDataset(getAccount(), currentDataset.getId(), null);
	}

	/**
	 * Adds the current dataset to the user's download queue
	 * 
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws MalformedURLException
	 */
	public String addToDownloadQueue() throws MalformedURLException, UnsupportedEncodingException {
		if (!getInDownloadQueue()) {
			currentDataset = getCurrentDataset();
			List<Long> dsIdList = new ArrayList<Long>();

			for (DatasetDataStructure datasetDataStructure : currentDataset.getDatasetDataStructure()) {
				dsIdList.add(datasetDataStructure.getDataStructureId());
			}

			String dictionaryUrl = modulesConstants.getModulesDDTURL(ServiceConstants.DEFAULT_PROVIDER);
			RestDictionaryProvider restProvider =
					new RestDictionaryProvider(dictionaryUrl, PortalUtils.getProxyTicket(dictionaryUrl));
			List<FormStructure> fsList = restProvider.getDataStructureDetailsByIds(dsIdList);
			logger.info("Dataset Added to Queue\n" + "Name: " + currentDataset.getName() + "\n" + "     Study: "
					+ currentDataset.getStudy().getTitle() + "\n" + "   User: " + getUsername());
			repositoryManager.addDatasetToDownloadQueue(currentDataset, getAccount(), fsList, true);
		}
		return PortalConstants.ACTION_LIGHTBOX;
	}



	public Map<Long, EntityMap> getPermissionList() {

		return permissionList;
	}

	public void setPermissionList(List<EntityMap> permissionList) {

		HashMap<Long, EntityMap> mapList = new HashMap<Long, EntityMap>();

		for (EntityMap entity : permissionList) {
			mapList.put(entity.getEntityId(), entity);
		}
		setPermissionList(mapList);
	}

	public void setPermissionList(Map<Long, EntityMap> permissionList) {

		this.permissionList = permissionList;
	}

	public boolean getCanDelete() {

		int accessRecCount = this.getAccessRecordCount();

		boolean inAdmin = this.getInAdmin();

		boolean result = (accessRecCount == 0) && inAdmin;

		return result;
	}

	public boolean getCantDelete() {
		return !this.getCanDelete();
	}

	public boolean getIsCurrentDatasetDeleted() {

		boolean result = this.currentDataset.getDatasetStatus().equals(DatasetStatus.DELETED);

		return result;
	}

	public EventLog getCurrentDatasetLatestEvent() {

		if (this.currentDatasetLatestEvent == null) {
			this.currentDatasetLatestEvent = this.repositoryManager.getDatasetLatestEvent(this.currentDataset);
		}

		return this.currentDatasetLatestEvent;
	}

	public String manageBulkDatasets() {

		String selectedDatasets = super.getRequestParameter("selectedDatasets");
		String datasetStatusSelect = super.getRequestParameter("datasetStatusSelect");

		// if redirected from document upload page
		if (selectedDatasets == null) {
			selectedDatasets = (String) getSession().getAttribute("selectedDatasets");
			datasetStatusSelect = (String) getSession().getAttribute("datasetStatusSelect");
		}
		// else clear the session for the datasetList and supportDocList
		else {
			getSessionDatasetList().clear();
			getSessionSupportDocList().clear();
		}

		// get selected datasets by serializing dataset ids
		String[] datasetIds = selectedDatasets.split(",");


		for (DatasetStatus status : DatasetStatus.values()) {
			if (datasetStatusSelect.equals(status.getVerb())) {
				newStatus = status;
				break;
			}
		}

		if (datasetStatusSelect.equals(PortalConstants.DATASET_REQUEST_APPROVE))
			isRequestApprove = true;

		Set<Long> setDatasetIds = new HashSet<Long>();

		for (String datasetId : datasetIds) {
			Long lDatasetId = Long.valueOf(datasetId);
			setDatasetIds.add(lDatasetId);
		}

		List<Dataset> datasets = repositoryManager.getDatasets(setDatasetIds);

		for (Dataset dataset : datasets) {

			currentDatasetStatus = dataset.getDatasetStatus();
			requestedDatasetStatus = dataset.getDatasetRequestStatus();

			if (requestedDatasetStatus != null) {
				isBulkRequest = true;
				newStatus = requestedDatasetStatus;
			}

			if (newStatus == null)
				newStatus = currentDatasetStatus;
			this.getDatasetList().add(dataset);
		}

		List<Dataset> datasetSession = getSessionDatasetList().getDatasets();

		// fetch datasets from the database and set to session
		if (datasetSession.isEmpty())
			getSessionDatasetList().setDatasets(datasets);
		else
			getSessionDatasetList().setDatasets(datasetSession);

		// set attributes to retrieve it in the forwarded action
		getSession().setAttribute("selectedDatasets", selectedDatasets);
		getSession().setAttribute("datasetStatusSelect", datasetStatusSelect);
		getSession().setAttribute("newStatus", newStatus);
		getSession().setAttribute("deleteDatasetRequest", null);

		return PortalConstants.STATUS_CHANGE;
	}

	public String bulkDatasetLists() throws SQLException, MessagingException {

		List<Dataset> datasets = getSessionDatasetList().getDatasets();

		Set<EventLogDocumentation> currentDocSet = getSessionSupportDocList().getSupportingDocumentation();

		newStatus = (DatasetStatus) getSession().getAttribute("newStatus");

		EventLog eventLog = new EventLog();

		if (currentDocSet != null && !currentDocSet.isEmpty()) {
			for (EventLogDocumentation sd : currentDocSet) {
				if (sd.getUserFile() != null) {
					sd.setUserFile(repositoryManager.saveUserFile(sd.getUserFile()));
				}
				sd.setEventLog(eventLog);
			}
		}
		eventLog.setSupportingDocumentationSet(currentDocSet);


		HashMap<User, List<Dataset>> bulkDatasets = new HashMap<User, List<Dataset>>();

		for (Dataset dataset : datasets) {

			if (dataset.getDatasetRequestStatus() == null) {

				dataset = repositoryManager.changeDatasetStatus(dataset, newStatus, getAccount(), statusChangeComment,
						eventLog);

				if (!bulkDatasets.containsKey(dataset.getSubmitter())) {
					List<Dataset> listDatasets = new ArrayList<Dataset>();
					listDatasets.add(dataset);

					bulkDatasets.put(dataset.getSubmitter(), listDatasets);
				} else {
					bulkDatasets.get(dataset.getSubmitter()).add(dataset);
				}

			}

			getDatasetList().add(dataset);
		}


		// To concatenate datasets name for including it in the email
		Iterator<Entry<User, List<Dataset>>> bulkDatasetsIt = bulkDatasets.entrySet().iterator();
		String newstatusVerb = newStatus.getVerb().toLowerCase();

		while (bulkDatasetsIt.hasNext()) {

			User user = bulkDatasetsIt.next().getKey();
			List<Dataset> bulkdatasets = bulkDatasets.get(user);


			Iterator<Dataset> bulkDatasetIt = bulkdatasets.iterator();
			String datasetsName = "";
			while (bulkDatasetIt.hasNext()) {
				Dataset dataset = bulkDatasetIt.next();
				if (!bulkDatasetIt.hasNext())
					datasetsName += dataset.getPrefixedId();
				else
					datasetsName += dataset.getPrefixedId() + ", ";

			}

			boolean isLocalEnv = this.modulesConstants.getEnvIsLocal();

			// construct subject line and message text to send through email
			if (!isLocalEnv) {
				String subject = this.getText(PortalConstants.MAIL_RESOURCE_CHANGE_STATUS_BULK_DATASETS
						+ PortalConstants.MAIL_RESOURCE_SUBJECT, Arrays.asList(newstatusVerb));

				String messageText =
						this.getText(PortalConstants.MAIL_RESOURCE_COMMON + PortalConstants.MAIL_RESOURCE_HEADER,
								Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
										modulesConstants.getModulesOrgEmail(getDiseaseId()), user.getFullName()))
								+ this.getText(
										PortalConstants.MAIL_RESOURCE_CHANGE_STATUS_BULK_DATASETS
												+ PortalConstants.MAIL_RESOURCE_BODY,
										Arrays.asList(modulesConstants.getModulesOrgEmail(getDiseaseId()), datasetsName,
												newstatusVerb))
								+ this.getText(
										PortalConstants.MAIL_RESOURCE_COMMON + PortalConstants.MAIL_RESOURCE_FOOTER,
										Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),""));


				mailEngine.sendMail(subject, messageText, null, user.getEmail());
			} else {
				logger.info(
						"[LOCAL ENV ONLY MESSAGE] Criteria met to send an email for the approval of dataset(s) for user: "
								+ user.getFullName() + " datasetName: " + datasetsName);
			}
		}

		return PortalConstants.REDIRECT;
	}

	public String approveBulkDatasetStatus() throws SQLException, MessagingException {

		List<Dataset> datasets = getSessionDatasetList().getDatasets();

		Set<EventLogDocumentation> currentDocSet = getSessionSupportDocList().getSupportingDocumentation();

		EventLog eventLog = new EventLog();

		if (currentDocSet != null && !currentDocSet.isEmpty()) {
			for (EventLogDocumentation sd : currentDocSet) {
				if (sd.getUserFile() != null)
					sd.setUserFile(repositoryManager.saveUserFile(sd.getUserFile()));

				sd.setEventLog(eventLog);
			}
		}
		eventLog.setSupportingDocumentationSet(currentDocSet);


		HashMap<User, List<Dataset>> bulkDatasets = new HashMap<User, List<Dataset>>();

		for (Dataset dataset : datasets) {

			if (dataset.getDatasetRequestStatus() != null)
				dataset = repositoryManager.approveDatasetStatus(dataset, getAccount(), statusChangeComment, eventLog);

			if (!bulkDatasets.containsKey(dataset.getSubmitter())) {
				List<Dataset> listDatasets = new ArrayList<Dataset>();
				listDatasets.add(dataset);

				bulkDatasets.put(dataset.getSubmitter(), listDatasets);
			} else {
				bulkDatasets.get(dataset.getSubmitter()).add(dataset);
			}

			getDatasetList().add(dataset);
		}

		newStatus = (DatasetStatus) getSession().getAttribute("newStatus");


		// To concatenate datasets name for including it in the email
		Iterator<Entry<User, List<Dataset>>> bulkDatasetsIt = bulkDatasets.entrySet().iterator();
		String newstatusVerb = newStatus.getVerb().toLowerCase();

		while (bulkDatasetsIt.hasNext()) {

			User user = bulkDatasetsIt.next().getKey();
			List<Dataset> bulkdatasets = bulkDatasets.get(user);


			Iterator<Dataset> bulkDatasetIt = bulkdatasets.iterator();
			String datasetsName = "";
			while (bulkDatasetIt.hasNext()) {
				Dataset dataset = bulkDatasetIt.next();
				if (!bulkDatasetIt.hasNext())
					datasetsName += dataset.getPrefixedId();
				else
					datasetsName += dataset.getPrefixedId() + ", ";

			}

			boolean isLocalEnv = this.modulesConstants.getEnvIsLocal();

			// construct subject line and message text to send through email
			if (!isLocalEnv) {
				String subject = this.getText(
						PortalConstants.MAIL_RESOURCE_ACCEPTED_BULK_DATASETS + PortalConstants.MAIL_RESOURCE_SUBJECT,
						Arrays.asList(newstatusVerb));

				String messageText =
						this.getText(PortalConstants.MAIL_RESOURCE_COMMON + PortalConstants.MAIL_RESOURCE_HEADER,
								Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
										modulesConstants.getModulesOrgPhone(getDiseaseId()), user.getFullName()))
								+ this.getText(
										PortalConstants.MAIL_RESOURCE_ACCEPTED_BULK_DATASETS
												+ PortalConstants.MAIL_RESOURCE_BODY,
										Arrays.asList(modulesConstants.getModulesOrgPhone(getDiseaseId()), datasetsName,
												newstatusVerb, statusChangeComment))
								+ this.getText(
										PortalConstants.MAIL_RESOURCE_COMMON + PortalConstants.MAIL_RESOURCE_FOOTER,
										Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),""));

				mailEngine.sendMail(subject, messageText, null, user.getEmail());
			} else {
				logger.info(
						"[LOCAL ENV ONLY MESSAGE] Criteria met to send an email for the approval of dataset(s) for user: "
								+ user.getFullName() + " datasetName: " + datasetsName);

			}
		}


		return PortalConstants.REDIRECT;
	}

	public String rejectBulkDatasetStatus() throws MessagingException {

		List<Dataset> datasets = getSessionDatasetList().getDatasets();

		Set<EventLogDocumentation> currentDocSet = getSessionSupportDocList().getSupportingDocumentation();

		EventLog eventLog = new EventLog();

		if (currentDocSet != null && !currentDocSet.isEmpty()) {
			for (EventLogDocumentation sd : currentDocSet) {
				if (sd.getUserFile() != null)
					sd.setUserFile(repositoryManager.saveUserFile(sd.getUserFile()));

				sd.setEventLog(eventLog);
			}
		}
		eventLog.setSupportingDocumentationSet(currentDocSet);

		HashMap<User, List<Dataset>> bulkDatasets = new HashMap<User, List<Dataset>>();
		String requestStatus = "";

		for (Dataset dataset : datasets) {

			if (dataset.getDatasetRequestStatus() != null) {
				requestStatus = dataset.getDatasetRequestStatus().getVerb().toLowerCase();
				dataset = repositoryManager.rejectDatasetStatus(dataset, getAccount(), statusChangeComment, eventLog);

				if (!bulkDatasets.containsKey(dataset.getSubmitter())) {
					List<Dataset> listDatasets = new ArrayList<Dataset>();
					listDatasets.add(dataset);

					bulkDatasets.put(dataset.getSubmitter(), listDatasets);
				} else {
					bulkDatasets.get(dataset.getSubmitter()).add(dataset);
				}

				newStatus = dataset.getDatasetStatus();
				getDatasetList().add(dataset);
			}
		}

		// To concatenate datasets name for including it in the email
		Iterator<Entry<User, List<Dataset>>> bulkDatasetsIt = bulkDatasets.entrySet().iterator();

		while (bulkDatasetsIt.hasNext()) {

			User user = bulkDatasetsIt.next().getKey();
			List<Dataset> bulkdatasets = bulkDatasets.get(user);


			Iterator<Dataset> bulkDatasetIt = bulkdatasets.iterator();
			String datasetsName = "";
			while (bulkDatasetIt.hasNext()) {
				Dataset dataset = bulkDatasetIt.next();
				if (!bulkDatasetIt.hasNext())
					datasetsName += dataset.getPrefixedId();
				else
					datasetsName += dataset.getPrefixedId() + ", ";

			}

			boolean isLocalEnv = this.modulesConstants.getEnvIsLocal();

			// construct subject line and message text to send through email
			if (!isLocalEnv) {
				String subject = this.getText(
						PortalConstants.MAIL_RESOURCE_REJECTED_BULK_DATASETS + PortalConstants.MAIL_RESOURCE_SUBJECT,
						Arrays.asList(requestStatus));

				String messageText =
						this.getText(PortalConstants.MAIL_RESOURCE_COMMON + PortalConstants.MAIL_RESOURCE_HEADER,
								Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
										modulesConstants.getModulesOrgPhone(getDiseaseId()), user.getFullName()))
								+ this.getText(
										PortalConstants.MAIL_RESOURCE_REJECTED_BULK_DATASETS
												+ PortalConstants.MAIL_RESOURCE_BODY,
										Arrays.asList(modulesConstants.getModulesOrgPhone(getDiseaseId()), datasetsName,
												requestStatus, statusChangeComment))
								+ this.getText(
										PortalConstants.MAIL_RESOURCE_COMMON + PortalConstants.MAIL_RESOURCE_FOOTER,
										Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),""));

				mailEngine.sendMail(subject, messageText, null, user.getEmail());
			}

			else {
				logger.info(
						"[LOCAL ENV ONLY MESSAGE] Criteria met to send an email for the approval of dataset(s) for user: "
								+ user.getFullName() + " datasetName: " + datasetsName);
			}
		}


		return PortalConstants.REDIRECT;

	}

	public String bulkList() {

		List<Dataset> datasets = getSessionDatasetList().getDatasets();

		newStatus = (DatasetStatus) getSession().getAttribute("newStatus");
		datasetStatusSelect = (String) getSession().getAttribute("datasetStatusSelect");

		// To set the value status in the bulkDatasetLists
		if (datasetStatusSelect.equals(PortalConstants.DATASET_REQUEST_REJECT))
			newStatus = datasets.get(0).getDatasetStatus();

		setDatasetList(datasets);

		return PortalConstants.STATUS_CHANGE_LISTS;
	}

	public String manageDataset() {

		for (DatasetStatus status : DatasetStatus.values()) {
			if (datasetStatusSelect.equals(status.getVerb())) {
				newStatus = status;
				break;
			}
		}
		getSession().setAttribute("newStatus", newStatus);
		getSessionSupportDocList().clear();

		return PortalConstants.MANAGE_DATASET;
	}

	public String changeStatus() throws SQLException, MessagingException {

		currentDataset = getCurrentDataset();

		Set<EventLogDocumentation> currentDocSet = getSessionSupportDocList().getSupportingDocumentation();

		newStatus = (DatasetStatus) getSession().getAttribute("newStatus");

		EventLog eventLog = new EventLog();

		if (currentDocSet != null && !currentDocSet.isEmpty()) {
			for (EventLogDocumentation sd : currentDocSet) {
				if (sd.getUserFile() != null) {
					sd.setUserFile(repositoryManager.saveUserFile(sd.getUserFile()));
				}
				sd.setEventLog(eventLog);
			}
		}
		eventLog.setSupportingDocumentationSet(currentDocSet);

		currentDataset =
				repositoryManager.changeDatasetStatus(currentDataset, newStatus, getAccount(), statusReason, eventLog);

		String newstatusVerb = newStatus.getVerb().toLowerCase();

		boolean isLocalEnv = this.modulesConstants.getEnvIsLocal();

		// construct subject line and message text to send through email
		if (!isLocalEnv) {
			String subject = this.getText(
					PortalConstants.MAIL_RESOURCE_CHANGE_STATUS_DATASET + PortalConstants.MAIL_RESOURCE_SUBJECT,
					Arrays.asList(currentDataset.getPrefixedId(), newstatusVerb));

			String messageText =
					this.getText(PortalConstants.MAIL_RESOURCE_COMMON + PortalConstants.MAIL_RESOURCE_HEADER,
							Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
									modulesConstants.getModulesOrgPhone(getDiseaseId()),
									currentDataset.getSubmitter().getFullName()))
							+ this.getText(
									PortalConstants.MAIL_RESOURCE_CHANGE_STATUS_DATASET
											+ PortalConstants.MAIL_RESOURCE_BODY,
									Arrays.asList(modulesConstants.getModulesOrgPhone(getDiseaseId()),
											currentDataset.getPrefixedId(), newstatusVerb))
							+ this.getText(PortalConstants.MAIL_RESOURCE_COMMON + PortalConstants.MAIL_RESOURCE_FOOTER,
									Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),""));

			mailEngine.sendMail(subject, messageText, null, currentDataset.getSubmitter().getEmail());
		} else {
			logger.info("[LOCAL ENV ONLY MESSAGE] Criteria met to send an email for the approval of dataset: "
					+ currentDataset.getName());
		}


		return PortalConstants.ACTION_REDIRECT_TO_VIEW;
	}

	/*------DatasetDocumentUpload, 
	 * kept this part here because document upload is in the same page as the dataset status change----*/
	public int getSupportDocListSize() {

		Set<EventLogDocumentation> supportDocListSession = getSessionSupportDocList().getSupportingDocumentation();

		int size = 0;
		if (supportDocListSession != null)
			size = supportDocListSession.size();

		return size;

	}

	public Boolean getIsDocumentLimitNR() {
		if (getSupportDocListSize() < getDocumentationLimit())
			return true;
		return false;
	}

	public int getDocumentationLimit() {
		return PortalConstants.MAXIMUM_DOCUMENT_UPLOAD;
	}

	public String getActionName() {
		return "eventLogDocumentationAction";
	}

	public Set<EventLogDocumentation> getSupportDocList() {

		Set<EventLogDocumentation> supportDocListSession = getSessionSupportDocList().getSupportingDocumentation();

		return supportDocListSession;

	}
}

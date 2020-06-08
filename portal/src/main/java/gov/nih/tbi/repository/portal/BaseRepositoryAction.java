package gov.nih.tbi.repository.portal;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.WebApplicationException;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonArray;
import com.google.gson.JsonParseException;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.common.util.ProformsWsProvider;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.StudyStatus;
import gov.nih.tbi.commons.portal.BaseAction;
import gov.nih.tbi.commons.service.StaticReferenceManager;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.repository.model.SessionDataset;
import gov.nih.tbi.repository.model.SessionDatasetList;
import gov.nih.tbi.repository.model.SessionStudy;
import gov.nih.tbi.repository.model.SessionSupportDocList;
import gov.nih.tbi.repository.model.SessionUploadFile;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.Study;

public class BaseRepositoryAction extends BaseAction {

	private static final long serialVersionUID = 6725279032180853872L;
	private static final Logger logger = Logger.getLogger(BaseRepositoryAction.class);

	@Autowired
	protected SessionStudy sessionStudy;

	@Autowired
	protected SessionDataset sessionDataset;

	@Autowired
	protected StaticReferenceManager staticManager;

	@Autowired
	protected SessionDatasetList sessionDatasetList;

	@Autowired
	protected SessionSupportDocList sessionSupportDocList;

	@Autowired
	protected SessionUploadFile sessionUploadFile;

	@Autowired
	protected ProformsWsProvider proformsWsProvider;


	public SessionStudy getSessionStudy() {

		if (sessionStudy == null) {
			sessionStudy = new SessionStudy();
		}
		return sessionStudy;
	}

	public SessionDataset getSessionDataset() {

		if (sessionDataset == null) {
			sessionDataset = new SessionDataset();
		}
		return sessionDataset;
	}

	public SessionDatasetList getSessionDatasetList() {

		if (sessionDatasetList == null) {
			sessionDatasetList = new SessionDatasetList();
		}
		return sessionDatasetList;
	}

	public SessionSupportDocList getSessionSupportDocList() {

		if (sessionSupportDocList == null) {
			sessionSupportDocList = new SessionSupportDocList();
		}
		return sessionSupportDocList;
	}

	public SessionUploadFile getSessionUploadFile() {

		if (sessionUploadFile == null) {
			sessionUploadFile = new SessionUploadFile();
		}
		return sessionUploadFile;
	}

	/************************ Getters/Setters *****************************/

	@Override
	public boolean getIsAdmin() {
		Study study = getSessionStudy().getStudy();

		if (study != null && study.getId() != null) {
			if (accountManager.getAccess(getAccount(), EntityType.STUDY, study.getId(), PermissionType.ADMIN)) {
				return true;
			}
		}
		return getIsStudyAdmin();
	}

	/**
	 * This method returns true if the login user is a study admin.
	 * 
	 * @return : true if user has ROLE_STUDY_ADMIN (implicit or explicit)
	 */
	public boolean getIsStudyAdmin() {
		return accountManager.hasRole(getAccount(), RoleType.ROLE_STUDY_ADMIN);
	}
	
	/**
	 * This method returns true if the login user is a Global or Repository Admin.
	 * 
	 * @return : true if User has ROLE_REPOSITORY_ADMIN or ROLE_ADMIN
	 */
	public boolean getIsRepositoryAdmin() {
		return accountManager.hasRole(getAccount(), RoleType.ROLE_REPOSITORY_ADMIN) || accountManager.hasRole(getAccount(), RoleType.ROLE_ADMIN);
	}
	
	public boolean getHasWriteAccess() {

		// If there is no user or we are not in a session then there is no write access to be had
		if (getAccount() == null || getSessionStudy().getStudy() == null) {
			return false;
		}
		return accountManager.getAccess(getAccount(), EntityType.STUDY, getSessionStudy().getStudy().getId(),
				PermissionType.WRITE);
	}

	public boolean getHasAdminAccess() {
		// If there is no user or we are not in a session then there is no admin access to be had
		if (getAccount() == null || getSessionStudy().getStudy() == null) {
			return false;
		}
		return accountManager.getAccess(getAccount(), EntityType.STUDY, getSessionStudy().getStudy().getId(),
				PermissionType.ADMIN);
	}

	public boolean getHasOwnerAccess() {
		// If there is no user or we are not in a session then there is no admin access to be had
		if (getAccount() == null || getSessionStudy().getStudy() == null) {
			return false;
		}
		return accountManager.getAccess(getAccount(), EntityType.STUDY, getSessionStudy().getStudy().getId(),
				PermissionType.OWNER);

	}

	/**
	 * Returns true if user is in admin or studyAdmin namespace
	 * 
	 * @return
	 */
	public boolean getInAdmin() {

		return (PortalConstants.NAMESPACE_ADMIN.equals(getNameSpace())
				|| PortalConstants.NAMESPACE_STUDYADMIN.equals(getNameSpace()));
	}


	/**
	 * Gets the current study from parameter studyId
	 * 
	 * @throws UserPermissionException
	 */
	public Study getStudyFromParameter() throws UserPermissionException {

		String studyId = getRequest().getParameter(PortalConstants.STUDY_ID);

		try {
			return repositoryManager.getStudyByPrefixedId(getAccount(), studyId);
		} catch (NumberFormatException e) {
			throw new NumberFormatException("Illegal study ID");
		} catch (NullPointerException e) {
			throw new RuntimeException("Study with ID = " + Long.valueOf(studyId) + " not found.");
		}
	}

	/**
	 * Gets the current study from parameter studyId, excluding the dataset records
	 * 
	 * @return Study
	 * @throws UserPermissionException
	 */
	public Study getStudyExcludingDataset() throws UserPermissionException {

		String studyId = getRequest().getParameter(PortalConstants.STUDY_ID);
		Study study = null;

		if (StringUtils.isNumeric(studyId)) {
			// studyId is just the ID, not prefix ID
			study = repositoryManager.getStudy(getAccount(), Long.decode(studyId));
		} else {
			study = repositoryManager.getStudyByPrefixedIdExcludingDataset(getAccount(), studyId);
		}

		getDatasetCount(study);

		return study;
	}

	public void getDatasetCount(Study study) {
		if (study != null && study.getId() != null) {
			Long studyid = study.getId();
			Set<Long> studyIds = new HashSet<Long>();
			studyIds.add(studyid);

			List<Dataset> datasets = repositoryManager.getStudyDataset(studyIds);
			if (datasets != null) {
				study.setDatasetCount(new Long(datasets.size()));
			} else {
				study.setDatasetCount(new Long(0));
			}
		}
	}

	/**
	 * Gets list of Dataset records
	 * 
	 * @param studyIds, gets datasets from study ids passed
	 * @return List<Dataset
	 */
	public List<Dataset> getDatasetForStudy(Set<Long> studyIds) {
		List<Dataset> datasets = repositoryManager.getStudyDataset(studyIds);
		return datasets;
	}


	/**
	 * Gets list of Dataset records
	 * 
	 * @param studyIds, gets datasets from study ids passed
	 * @return List<Dataset
	 */
	public List<Dataset> getDatasetByIds(Set<Long> datasetIds) {
		List<Dataset> datasets = repositoryManager.getDatasetById(datasetIds);
		return datasets;
	}

	public boolean updateDataset(Set<Dataset> dataset) {
		repositoryManager.updateDatasets(dataset);
		return true;
	}

	public boolean canShowCreateDoiUi() {
		Study currStudy = getSessionStudy().getStudy();

		// Verify if there is a study stored in session.
		if (currStudy == null) {
			logger.warn("The session study is null. Can't determine DOI eligibility at this time.");

			return false;
		}

		String doi = currStudy.getDoi();
		StudyStatus studyStatus = currStudy.getStudyStatus();

		return (doi == null || doi.isEmpty())
				&& (studyStatus != null && studyStatus != StudyStatus.REQUESTED && studyStatus != StudyStatus.REJECTED)
				&& getIsDoiEnabled() && getIsAdmin();
	}

	public JsonArray getStudyAssoPfProtocols(String studyPrefixId) {
		JsonArray rtnJsonArr = null;

		// Check if ProFoRMS is enabled on this instance.
		if (modulesConstants.getModulesPFEnabled()) {
			try {
				String proformsWsUrl = modulesConstants.getModulesPFURL(getDiseaseId());
				rtnJsonArr = proformsWsProvider.getStudyAssoPfProtocols(proformsWsUrl, studyPrefixId);
			} catch (JsonParseException e) {
				logger.error(
						"Error while contructing the JSON array from the returned JSON string from the web service.",
						e);
				rtnJsonArr = new JsonArray();
			} catch (WebApplicationException e) {
				logger.error("Error occurred while communicating with the ProFoRMS web service.", e);
				rtnJsonArr = new JsonArray();
			} catch (IOException e) {
				logger.error("Error while generating the proxy ticket.", e);
				rtnJsonArr = new JsonArray();
			}

			getSessionStudy().setAssociatedPFProtocols(rtnJsonArr.toString());
		} else {
			// Send empty array if ProFoRMS is disabled.
			rtnJsonArr = new JsonArray();
		}

		return rtnJsonArr;
	}
}

package gov.nih.tbi.repository.portal;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.apache.struts2.result.StreamResult;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.StudyStatus;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;
import gov.nih.tbi.repository.dao.StudyDao;
import gov.nih.tbi.repository.model.alzped.ModelName;
import gov.nih.tbi.repository.model.alzped.ModelType;
import gov.nih.tbi.repository.model.alzped.StudyModelName;
import gov.nih.tbi.repository.model.alzped.StudyModelType;
import gov.nih.tbi.repository.model.alzped.StudyTherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.StudyTherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.StudyTherapyType;
import gov.nih.tbi.repository.model.alzped.TherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.TherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.TherapyType;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.taglib.datatableDecorators.StudyPfProtocolIdtListDecorator;

public class ViewStudyAction extends BaseRepositoryAction {

	private static final long serialVersionUID = 4668296443874317981L;
	
	private static Logger logger = Logger.getLogger(ViewStudyAction.class);

	private String visibility;
	private String reason;

	private Boolean assoPfProtoVisible = false;
	
	@Autowired
	private StudyDao studyDao;
	
	private Set<StudyTherapeuticAgent> therapeuticAgentSet;
	private Set<StudyTherapeuticTarget> therapeuticTargetSet;
	private Set<StudyTherapyType> therapyTypeSet;
	private Set<StudyModelName> modelNameSet;
	private Set<StudyModelType> modelTypeSet;
	
	/**
	 * Action for view studies
	 * 
	 * @return
	 * @throws UserPermissionException
	 */
	public String view() throws UserPermissionException {

		Study currentStudy = getStudyExcludingDataset();
		getSessionStudy().setStudy(currentStudy);
		
		getAssoPfProtoVisible();
		return PortalConstants.ACTION_VIEW;
	}

	public String lightbox() throws UserPermissionException {

		Study currentStudy = getStudyExcludingDataset();
		getSessionStudy().setStudy(currentStudy);

		getAssoPfProtoVisible();
		return PortalConstants.ACTION_LIGHTBOX;
	}

	
	/**
	 * Deletes the study with id of parameter studyId
	 * 
	 * @return
	 * @throws UserPermissionException
	 */
	public String delete() throws UserPermissionException {

		Study currentStudy = getStudyExcludingDataset();

		accountManager.checkEntityAccess(getAccount(), EntityType.STUDY, currentStudy.getId(), PermissionType.ADMIN);

		// non-admin user should not be able to delete a non-private study
		if (!StudyStatus.PRIVATE.equals(currentStudy.getStudyStatus())
				|| !accountManager.hasRole(getAccount(), RoleType.ROLE_STUDY_ADMIN)) {
			throw new UnsupportedOperationException();
		}	
		try{
			accountManager.unregisterEntity(accountManager.listEntityAccess(currentStudy.getId(), EntityType.STUDY));
		}
		catch(Exception e){
			e.printStackTrace();
			logger.error("Error occurred when deleting access permission records for the study with ID: " + currentStudy.getId());
		}
		repositoryManager.deleteStudy(getAccount(), currentStudy, getIsAdmin());
		getSessionStudy().clear();
		return PortalConstants.ACTION_LIST;

	}


	/**
	 * Sets the visibility of the study and saves the change
	 */
	public StreamResult changeVisibility() {

		Study currentStudy = getSessionStudy().getStudy();

		if (visibility != null && currentStudy != null && currentStudy.getStudyStatus() != null) {
			if (PortalConstants.PUBLIC.equals(visibility)
					&& !StudyStatus.PUBLIC.equals(currentStudy.getStudyStatus())) {
				currentStudy.setStudyStatus(StudyStatus.PUBLIC);
				accountManager.registerEntity(accountManager.getPermissionGroup(ServiceConstants.PUBLIC_STUDY),
						EntityType.STUDY, currentStudy.getId(), PermissionType.READ);
				currentStudy = repositoryManager.saveStudy(getAccount(), getSessionStudy().getStudy());
			}

			if (PortalConstants.PRIVATE.equals(visibility)
					&& !StudyStatus.PRIVATE.equals(currentStudy.getStudyStatus())) {
				currentStudy.setStudyStatus(StudyStatus.PRIVATE);
				currentStudy = repositoryManager.saveStudy(getAccount(), getSessionStudy().getStudy());
			}

			getSessionStudy().setStudy(currentStudy);

			return new StreamResult(new ByteArrayInputStream(currentStudy.getStudyStatus().getName().getBytes()));
		} else {
			return null;
		}
	}


	/**
	 * Approves a requested study and makes its status public. This function depends on the study being approved being
	 * in session (which it should be when user goes to the view page).
	 * 
	 * @throws MessagingException
	 * @return
	 */
	public String approve() throws MessagingException {

		// This action can only be performed in an admin namespace
		if (!getInAdmin()) {
			return PortalConstants.ACTION_ERROR;
		}

		// change study state study
		Study currentStudy = getSessionStudy().getStudy();
		currentStudy.setStudyStatus(StudyStatus.PUBLIC);

		// Register this study with public study permissions group
		accountManager.registerEntity(accountManager.getPermissionGroup(ServiceConstants.PUBLIC_STUDY),
				EntityType.STUDY, currentStudy.getId(), PermissionType.READ);

		currentStudy = repositoryManager.saveStudy(getAccount(), currentStudy);
		getSessionStudy().setStudy(currentStudy);

		boolean isLocalEnv = this.modulesConstants.getEnvIsLocal();
		
		// send email to user
		if (!isLocalEnv) {
			String subject =
					this.getText(PortalConstants.MAIL_RESOURCE_ACCEPTED_STUDY + PortalConstants.MAIL_RESOURCE_SUBJECT,
							Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId())));
			String messageText =
					this.getText(PortalConstants.MAIL_RESOURCE_COMMON + PortalConstants.MAIL_RESOURCE_HEADER,
							Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
									modulesConstants.getModulesOrgPhone(getDiseaseId()),
									accountManager.getEntityOwnerAccount(currentStudy.getId(), EntityType.STUDY)
											.getUser().getFullName(), reason))
							+ this.getText(
									PortalConstants.MAIL_RESOURCE_ACCEPTED_STUDY + PortalConstants.MAIL_RESOURCE_BODY,
									Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
											modulesConstants.getModulesOrgPhone(getDiseaseId()),
											accountManager.getEntityOwnerAccount(currentStudy.getId(), EntityType.STUDY)
													.getUser().getFullName(), reason))
							+ this.getText(PortalConstants.MAIL_RESOURCE_COMMON + PortalConstants.MAIL_RESOURCE_FOOTER,
									Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
											modulesConstants.getModulesOrgPhone(getDiseaseId()), accountManager
													.getEntityOwnerAccount(currentStudy.getId(), EntityType.STUDY)
													.getUser().getFullName(), reason));
			accountManager.sendEmail(accountManager.getEntityOwnerAccount(currentStudy.getId(), EntityType.STUDY),
					subject, messageText);
		} else {
			logger.info("[LOCAL ENV ONLY MESSAGE] Criteria met to send an email for the approval of study: "
					+ currentStudy.getTitle());
		}

		return PortalConstants.ACTION_VIEW;
	}

	/**
	 * Denies a requested study and makes its status public. This function depends on the study being approved being in
	 * session (which it should be when user goes to the view page).
	 * 
	 * @throws MessagingException
	 * @return
	 */
	public String deny() throws MessagingException {

		// This action can only be performed in an admin namespace
		if (!getInAdmin()) {
			return PortalConstants.ACTION_ERROR;
		}

		// change study state study
		Study currentStudy = getSessionStudy().getStudy();
		currentStudy.setStudyStatus(StudyStatus.REJECTED);
		currentStudy = repositoryManager.saveStudy(getAccount(), currentStudy);
		getSessionStudy().setStudy(currentStudy);

		boolean isLocalEnv = this.modulesConstants.getEnvIsLocal();
		
		// send email to user
		if (!isLocalEnv) {
			String subject =
					this.getText(PortalConstants.MAIL_RESOURCE_REJECTED_STUDY + PortalConstants.MAIL_RESOURCE_SUBJECT,
							Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId())));
			String messageText =
					this.getText(PortalConstants.MAIL_RESOURCE_COMMON + PortalConstants.MAIL_RESOURCE_HEADER,
							Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
									modulesConstants.getModulesOrgPhone(getDiseaseId()),
									accountManager.getEntityOwnerAccount(currentStudy.getId(), EntityType.STUDY)
											.getUser().getFullName(), reason))
							+ this.getText(
									PortalConstants.MAIL_RESOURCE_REJECTED_STUDY + PortalConstants.MAIL_RESOURCE_BODY,
									Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
											modulesConstants.getModulesOrgPhone(getDiseaseId()),
											accountManager.getEntityOwnerAccount(currentStudy.getId(), EntityType.STUDY)
													.getUser().getFullName(), reason))
							+ this.getText(PortalConstants.MAIL_RESOURCE_COMMON + PortalConstants.MAIL_RESOURCE_FOOTER,
									Arrays.asList(modulesConstants.getModulesOrgName(getDiseaseId()),
											modulesConstants.getModulesOrgPhone(getDiseaseId()), accountManager
													.getEntityOwnerAccount(currentStudy.getId(), EntityType.STUDY)
													.getUser().getFullName(), reason));
			accountManager.sendEmail(accountManager.getEntityOwnerAccount(currentStudy.getId(), EntityType.STUDY),
					subject, messageText);
		} else {
			logger.info("[LOCAL ENV ONLY MESSAGE] Criteria met to send an email for the denial of study: "
					+ currentStudy.getTitle());
		}

		return PortalConstants.ACTION_VIEW;
	}
	
	
	/**
	 * Returns a boolean determining if the currentStudy is a request
	 * 
	 * @return true if the currentStudy is a request
	 */
	public boolean getIsStudyRequest() {
		Study currentStudy = getSessionStudy().getStudy();

		if (currentStudy == null) {
			return true;
		}
		return (currentStudy.getStudyStatus().equals(StudyStatus.REQUESTED));
	}

	
	/**
	 * Gets the owner of the current study
	 * @return
	 */
	public User getStudyOwner() {
		return (accountManager.getEntityOwnerAccount(getCurrentStudy().getId(), EntityType.STUDY)).getUser();
	}

	public Study getCurrentStudy() {
		if (getSessionStudy() != null) {
			return getSessionStudy().getStudy();
		} else {
			return null;
		}
	}

	
	// url: http://fitbir-portal-local.cit.nih.gov:8080/portal/study/studyAction!getStudySiteSet.action
	public String getStudyPfProtocolList() {

		String outJsonArrStr = getSessionStudy().getAssociatedPFProtocols();
		JsonArray outJsonArr = new JsonArray();
		JsonParser jsonParser = new JsonParser();
		outJsonArr = jsonParser.parse(outJsonArrStr).getAsJsonArray();

		ArrayList<String> outputList = new ArrayList<String>();
		for (int i=0; i< outJsonArr.size();i++){ 
			outputList.add(outJsonArr.get(i).toString());
		   } 
		try {
			IdtInterface idt = new Struts2IdtInterface();
			idt.setList(outputList);
			idt.decorate(new StudyPfProtocolIdtListDecorator());
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}


	public String getVisibility() {
		return visibility;
	}

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
	
	public String getDocumentsActionName() {
		return "studyDocumentationAction";
	}

	public Boolean getAssoPfProtoVisible() {
		JsonArray pFProtoJsonArr = getStudyAssoPfProtocols(getCurrentStudy().getPrefixedId());
		
		for(int i = 0; i < pFProtoJsonArr.size(); i++) {
			JsonObject pFProtoObject = pFProtoJsonArr.get(i).getAsJsonObject();
			if(!pFProtoObject.get("closingBricsUserId").isJsonNull()) {
				this.assoPfProtoVisible = true;
			}
		}
		return this.assoPfProtoVisible;
	}
	
	public void setAssoPfProtoVisible(Boolean assoPfProtoVisible) {
		this.assoPfProtoVisible = assoPfProtoVisible;
	}

	public List<TherapeuticAgent> getTherapeuticAgentSet() {
		return studyDao.getStudyTherapeuticAgents(getSessionStudy().getStudy().getId().toString());
	}

	public List<TherapeuticTarget> getTherapeuticTargetSet() {
		return studyDao.getStudyTherapeuticTargets(getSessionStudy().getStudy().getId().toString());
	}

	public List<TherapyType> getTherapyTypeSet() {
		return studyDao.getStudyTherapyTypes(getSessionStudy().getStudy().getId().toString());
	}

	public List<ModelName> getModelNameSet() {
		return studyDao.getStudyModelNames(getSessionStudy().getStudy().getId().toString());
	}

	public List<ModelType> getModelTypeSet() {
		return studyDao.getStudyModelTypes(getSessionStudy().getStudy().getId().toString());
	}

}

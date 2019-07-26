package gov.nih.nichd.ctdb.response.manager;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbManager;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.common.PaginationData;
import gov.nih.nichd.ctdb.form.domain.DataCollectionExport;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.domain.Section;
import gov.nih.nichd.ctdb.form.manager.FormManager;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.protocol.domain.Interval;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.question.domain.Answer;
import gov.nih.nichd.ctdb.question.domain.CalculationType;
import gov.nih.nichd.ctdb.question.domain.ImageMapQuestion;
import gov.nih.nichd.ctdb.question.domain.PatientCalendarQuestion;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.domain.QuestionType;
import gov.nih.nichd.ctdb.question.manager.QuestionManager;
import gov.nih.nichd.ctdb.response.common.DuplicateDataEntriesException;
import gov.nih.nichd.ctdb.response.common.InputHandler;
import gov.nih.nichd.ctdb.response.common.VisitDateMismatchException;
import gov.nih.nichd.ctdb.response.dao.ResponseManagerDao;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;
import gov.nih.nichd.ctdb.response.domain.CalendarResponse;
import gov.nih.nichd.ctdb.response.domain.DataEntryDraft;
import gov.nih.nichd.ctdb.response.domain.EditAnswerDisplay;
import gov.nih.nichd.ctdb.response.domain.EditAssignment;
import gov.nih.nichd.ctdb.response.domain.FormCollectionDisplay;
import gov.nih.nichd.ctdb.response.domain.FormInterval;
import gov.nih.nichd.ctdb.response.domain.PatientCalendarCellResponse;
import gov.nih.nichd.ctdb.response.domain.Response;
import gov.nih.nichd.ctdb.response.form.DataCollectionLandingForm;
import gov.nih.nichd.ctdb.response.form.DataEntrySetupForm;
import gov.nih.nichd.ctdb.response.util.AdminFormMetaDataEdit;
import gov.nih.nichd.ctdb.security.domain.User;

/**
 * ResponseManager is a business layer object which interacts with the ResponseManagerDao. The role of the
 * ResponseManager is to enforce business rule logic and delegate data layer manipulation to the ResponseManagerDao.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class ResponseManager extends CtdbManager {
	Logger logger = Logger.getLogger(ResponseManager.class.getName());

	/**
	 * Creates the administered form. If the form requires double key data entry, it checks if two data entries have
	 * been done and throws DuplicateDataEntriesException if so. It also updates the form status to "In Progress" to
	 * indicate the form is in the data entry progress
	 *
	 * @param administeredForm The Administered form object to create
	 * @param user The user object to be used for saving the data entry user info
	 * @throws DuplicateDataEntriesException Thrown if two data entries exist already
	 * @throws DuplicateObjectException Thrown if the Administered form exists in the system based on the unique
	 *         constraints
	 * @throws CtdbException Thrown if any other errors occur while processing
	 */
	public void create(AdministeredForm administeredForm, User user)
			throws DuplicateDataEntriesException, DuplicateObjectException, VisitDateMismatchException, CtdbException {
		Connection conn = null;
		logger.info("ResponseManager->create(AdminForm)");
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			dao.create(administeredForm);
			dao.createDataEntryDraft(administeredForm, user);
			conn.commit();
		} catch (SQLException e) {
			this.rollback(conn);
			throw new CtdbException("Unable to create the administered form : " + e.getMessage(), e);
		} catch (DuplicateObjectException doe) {
			this.rollback(conn);
			throw doe;
		} catch (CtdbException ce) {
			this.rollback(conn);
			throw ce;
		} catch (Exception et) {
			this.rollback(conn);
			throw new CtdbException("Unknown error occurred while creating the administered form: " + et.getMessage(),
					et);
		} finally {
			this.close(conn);
		}
	}



	/**
	 * Preforms the save progress work, may be called from create administered form due to data spring data.
	 * 
	 * @param administeredForm
	 * @param user
	 * @param dao
	 * @throws ObjectNotFoundException
	 * @throws DuplicateObjectException
	 * @throws CtdbException
	 */
	public void saveProgress(AdministeredForm administeredForm, User loggedInUser)
			throws ObjectNotFoundException, DuplicateObjectException, CtdbException {

		long start = System.currentTimeMillis();

		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);

			List responses = administeredForm.getResponses();
			Response response;
			AdministeredForm admForm = new AdministeredForm();
			admForm.setId(administeredForm.getId());
			admForm.setDataEntryDraftId(administeredForm.getDataEntryDraftId());
			admForm.setMarkAsCompleted(administeredForm.isMarkAsCompleted());
			admForm.setEntryOneStatus(administeredForm.getEntryOneStatus());
			admForm.setEntryTwoStatus(administeredForm.getEntryTwoStatus());
			// logger.info("ResponseManager->saveProgress: \n Aform ID:\t"+administeredForm.getId()+"\n Form
			// Name:\t"+administeredForm.getForm().getName()+"\n User:\t"+user.getUsername()+"Mark As completed
			// Flag:\t"+admForm.isMarkAsCompleted());
			// logger.info("ResponseManager->saveProgress: \n Autosaver Flag
			// status:\t"+administeredForm.isComingFromAutoSaver());
			for (Iterator it = responses.iterator(); it.hasNext();) {

				response = (Response) it.next();
				response.setAdministeredForm(admForm);
				// logger.info("\t Response Id:"+response.getId());
				if (response.getId() == Integer.MIN_VALUE) {
					// logger.info("Create Response Draft:");
					// TODO Aggregate
					dao.createResponseDraft(response, loggedInUser);
					if (!response.getAnswers().isEmpty()) {
						// logger.info("Create Response Draft answers (delete first and then create answers):");

						// TODO Aggregate
						dao.createResponseDraftAnswers(response, false);
					}
					// this.createResponseDraft(response, user);
				} else {
					// logger.info("Update Response Draft:");
					// TODO Aggregate
					dao.updateResponseDraft(response, loggedInUser);
					if (!response.getAnswers().isEmpty()) {
						// logger.info("Create Response Draft answers (delete first and then create answers):");
						// TODO Aggregate
						dao.createResponseDraftAnswers(response, false);
					} else {
						/*
						 * Ching Heng Since the file question don't have default value, the response for file question
						 * will always come here even it had a file
						 */

						if (response.getQuestion().getType().getValue() != QuestionType.File.getValue()) {
							// logger.info("Delete Draft answers:");
							// TODO Aggregate
							dao.deleteDraftAnswers(response);
						}
					}

					// this.updateResponseDraft(response, user);
				}
			}
			if (conn != null) {
				conn.commit();
			}
		} catch (SQLException e) {
			this.rollback(conn);
			throw new CtdbException("Unable to create response draft: " + e.getMessage(), e);
		} catch (DuplicateObjectException doe) {
			this.rollback(conn);
			throw doe;
		} catch (CtdbException ce) {
			this.rollback(conn);
			throw ce;
		} finally {
			this.rollback(conn);
			this.close(conn);

			long end = System.currentTimeMillis();
			System.out.println("PERF DEBUG: saveProgress took " + (end - start) + " ms to complete.");
		}
	}

	/**
	 * An alternate method to saveProgress(). Has been optimized for performance by aggregating individual sql queries
	 * for each response, into single queries executed across all responses.
	 */
	public void saveProgress2(AdministeredForm administeredForm, User loggedInUser)
			throws ObjectNotFoundException, DuplicateObjectException, CtdbException {

		long start = System.currentTimeMillis();

		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);

			List<Response> responses = administeredForm.getResponses();
			List<Response> oldResponses = new ArrayList<Response>();
			List<Response> newResponses = new ArrayList<Response>();
			List<Response> responsesHasAnswers = new ArrayList<Response>();
			List<Response> responseAnswersToDelete = new ArrayList<Response>();
			List answers = null; // new ArrayList();
			List editAnswers = null; // new ArrayList();
			List previousAnswers = null; // new ArrayList();
			List latestAnswerInProgress = null; // new ArrayList();

			for (Response response : responses) {

				if (response.getId() == Integer.MIN_VALUE) {
					newResponses.add(response);
					if (!response.getAnswers().isEmpty()) {
						responsesHasAnswers.add(response);
					}
				} else {
					oldResponses.add(response);
					previousAnswers = dao.getDraftCompletedAnswers(response.getId());

					if (!response.getAnswers().isEmpty()) {
						responsesHasAnswers.add(response);
					} else {
						if (response.getQuestion().getType().getValue() != QuestionType.File.getValue()) {
							responseAnswersToDelete.add(response);
						}
					}
				}

				answers = response.getAnswers();
				editAnswers = response.getEditAnswers();
				latestAnswerInProgress = dao.getLatestAnswerInProgress(administeredForm, response);

				if (editAnswers.isEmpty()) {
					editAnswers = dao.getEditAnswers(administeredForm, response);
				}
				response.setAdministeredForm(administeredForm);

				if (!answers.isEmpty()) {
					// archive the final answers to PATIENTADMINRESPONSE table
					response.setEditedBy(loggedInUser.getId());
					if (!answers.equals(previousAnswers)) {
						// dao.deletePreviousAuditLogInProgress(administeredForm, response);
						dao.createEditAnswersArchiveInProgress(administeredForm.getId(), response, false);
					}
				}

			}

			if (newResponses.size() > 0) {
				dao.createResponsesDraft(newResponses, loggedInUser);
			}

			if (oldResponses.size() > 0) {
				dao.updateResponsesDraft(oldResponses, loggedInUser);
			}

			// delete draft answers somewhere
			// either within the createresponsesdraftanswers method
			// or as distinct step. thinking the latter.
			if (responses.size() > 0) {
				if (responsesHasAnswers.size() > 0) {
					dao.createResponsesDraftAnswers(responsesHasAnswers, false);
				}
				if (responseAnswersToDelete.size() > 0) {
					dao.deleteDraftAnswers(responseAnswersToDelete);
				}

			}

			if (conn != null) {
				conn.commit();
			}
		} catch (SQLException e) {
			this.rollback(conn);
			throw new CtdbException("Unable to create response draft: " + e.getMessage(), e);
		} catch (DuplicateObjectException doe) {
			this.rollback(conn);
			throw doe;
		} catch (CtdbException ce) {
			this.rollback(conn);
			throw ce;
		} finally {
			this.rollback(conn);
			this.close(conn);

			long end = System.currentTimeMillis();
			System.out.println("PERF DEBUG: saveProgress2 took " + (end - start) + " ms to complete.");

		}
	}

	/**
	 * Retrieves the number of questions from an administered form which have answers in the patientresponsedraft table.
	 *
	 * @param admFormId the administered form id
	 * @param user The user who is the data entry person for this admin form.
	 * @return int the number of questions with answers received.
	 * @throws CtdbException thrown if any errors occur while processing
	 */
	public int getNumQuestionsWithAnswers(int admFormId, User user) throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			conn.commit();
			return dao.getNumQuestionsWithAnswers(admFormId, user);
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException("Unable to get administered form for the ID: " + admFormId + " : " + e.getMessage(),
					e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}



	public void saveDiscrepancyProgress(AdministeredForm administeredForm, User user)
			throws CtdbException, SQLException, UnknownHostException, MessagingException {
		List responses = administeredForm.getResponses();
		Response response;
		AdministeredForm admForm;

		for (Iterator it = responses.iterator(); it.hasNext();) {

			response = (Response) it.next();

			admForm = new AdministeredForm();
			admForm.setId(administeredForm.getId());
			admForm.setDataEntryDraftId(administeredForm.getDataEntryDraftId());
			response.setAdministeredForm(admForm);

			if (response.getId() == Integer.MIN_VALUE) {
				this.createResponse(response, user);
			} else {
				this.updateResponse(response, user);
			}
		}

		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			dao.updateDiscrepancyFlag(administeredForm.getId(), false);
			conn.commit();

			administeredForm.setDiscrepancyFlag(false);

			// final lock
			administeredForm.setCertifiedBy(user.getId());
			administeredForm.setFinalLockBy(user.getId());
			updateAdministeredForm(administeredForm);
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException("saveDiscrepancyProgress->error:\t" + e.getMessage());
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}


	/**
	 * Saves the final edit answers for the administered form.
	 *
	 * @param administeredForm The AdministeredForm object
	 * @param user The User object contains the user id who edits the answers
	 * @throws CtdbException Thrown if any errors occur while processing
	 */
	public void saveFinalEditAnswers(AdministeredForm administeredForm, User user) throws CtdbException {

		// System.out.println("IN SAVE EDIT ANSWERS");
		// logger.info("ResponseManager->saveEditAnswers");
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);

			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);

			List responses = administeredForm.getResponses();
			Response response;
			List answers = null; // new ArrayList();
			for (Iterator it = responses.iterator(); it.hasNext();) {
				response = (Response) it.next();
				// logger.info("ResponseManager->saveEditAnswers->responseId:\t"+response.getId());
				answers = response.getEditAnswers();
				response.setAdministeredForm(administeredForm);
				if (answers.isEmpty()) {
					// logger.info("Answers is empty");
				}

				if (!answers.isEmpty()) {
					// archive the final answers to PATIENTADMINRESPONSE table
					response.setEditedBy(user.getId());
					dao.createEditAnswersArchive(administeredForm.getId(), response, true);
					// create the final answers using edit answers
					// if the response id = integer.MinValue user is editing a previously unanswered question
					if (response.getId() == Integer.MIN_VALUE) {
						response.setAdministeredForm(administeredForm);
						// logger.info("Creating Response");
						dao.createResponse(response, user);
					}
					if (((String) answers.get(0)).equals("^^DELETED^^")) {
						// logger.info("Deleting Response Answers");
						dao.deleteResponseAnswers(response);
						answers = new ArrayList();
					} else {
						// logger.info("createResponseAnswers");
						dao.createResponseAnswers(response, true);
					}
				}
			}
			conn.commit();
		} catch (SQLException e) {
			this.rollback(conn);
			throw new CtdbException("Unable to save edit answers as final answers: " + e.getMessage(), e);
		} catch (DuplicateObjectException doe) {
			this.rollback(conn);
			throw doe;
		} catch (CtdbException ce) {
			this.rollback(conn);
			throw ce;
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}



	/**
	 * Saves the completed edit answers for the administered form.
	 *
	 * @param administeredForm The AdministeredForm object
	 * @param user The User object contains the user id who edits the answers
	 * @throws CtdbException Thrown if any errors occur while processing
	 */
	public void saveCompletedEditAnswers(AdministeredForm administeredForm, User loggedInUser) throws CtdbException {

		// System.out.println("IN SAVE EDIT ANSWERS");
		// logger.info("ResponseManager->saveEditAnswers");
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);

			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);

			List responses = administeredForm.getResponses();
			Response response;
			List answers = null; // new ArrayList();
			for (Iterator it = responses.iterator(); it.hasNext();) {
				response = (Response) it.next();
				// logger.info("ResponseManager->saveEditAnswers->responseId:\t"+response.getId());
				answers = response.getEditAnswers();
				response.setAdministeredForm(administeredForm);
				if (answers.isEmpty()) {
					// logger.info("Answers is empty");
				}

				if (!answers.isEmpty()) {
					// archive the final answers to PATIENTADMINRESPONSE table
					response.setEditedBy(loggedInUser.getId());
					dao.createEditAnswersArchive(administeredForm.getId(), response, false);
					// create the final answers using edit answers
					// if the response id = integer.MinValue user is editing a previously unanswered question
					if (response.getId() == Integer.MIN_VALUE) {
						response.setAdministeredForm(administeredForm);
						// logger.info("Creating Response");
						dao.createResponseDraft(response, loggedInUser);
					}
					if (((String) answers.get(0)).equals("^^DELETED^^")) {
						// logger.info("Deleting Response Answers");
						dao.deleteDraftAnswers(response);
						answers = new ArrayList();
					} else {
						// logger.info("createResponseAnswers");
						dao.createResponseDraftAnswers(response, true);
					}
				}
			}
			conn.commit();
		} catch (SQLException e) {
			this.rollback(conn);
			throw new CtdbException("Unable to save edit answers as final answers: " + e.getMessage(), e);
		} catch (DuplicateObjectException doe) {
			this.rollback(conn);
			throw doe;
		} catch (CtdbException ce) {
			this.rollback(conn);
			throw ce;
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}



	/**
	 * gets the locked by and date info for a given administered form domain object.
	 *
	 * @param admForm the administered form domain object where lockedBy, locked2By, lockedDate, and locked2Date will be
	 *        set.
	 * @throws CtdbException thrown if any errors occur while processing
	 */
	public void getLockedByAndDate(AdministeredForm admForm) throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			dao.getLockedByAndDate(admForm);
			conn.commit();
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException(
					"Unable to get administered form for the ID: " + admForm.getId() + " : " + e.getMessage(), e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}


	public List<Response> getResponsesLocked(int admForm) throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			List<Response> aformResponses = dao.getResponsesLocked(admForm);
			return aformResponses;
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException("Unable to get administered form for the ID: " + admForm + " : " + e.getMessage(),
					e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	/**
	 * Method to get admin form passing the form id by yogi
	 * 
	 * @param formId
	 * @return
	 * @throws ObjectNotFoundException
	 * @throws CtdbException
	 */
	public List getPatientViewDataForLandingPage(DataCollectionLandingForm pvList, int protocolId,
			Map<String, String> searchOptions) throws ObjectNotFoundException, CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			List pvDataList = dao.getPatientViewDataForLandingPage(pvList, protocolId, searchOptions);
			conn.commit();
			return pvDataList;
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException("Unable to get  form for the ID: " + pvList.getPvPatientId() + e.getMessage(), e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	public void saveVisibleRepeatableSection(AdministeredForm aform, DataEntrySetupForm desf, User usr)
			throws ObjectNotFoundException, CtdbException, SQLException {
		// logger.info("ResponseManager->saveVisibleRepeatableSection");
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			dao.saveVisibleRepeatableSection(aform, desf, usr);
			conn.commit();
		} catch (Exception e) {
			this.rollback(conn);
			e.printStackTrace();
			throw new CtdbException(
					"Unable to save section ids for admin from:\t" + aform.getForm().getName() + e.getMessage());
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}



	/**
	 * Method to get AdministeredForm object for given formId,intervalId, userId, subjectId and studyId combination
	 * 
	 * @param formId
	 * @param intervalId
	 * @param userId
	 * @param subjectId
	 * @param studyId
	 * @return
	 * @throws ObjectNotFoundException
	 * @throws CtdbException
	 */
	public AdministeredForm getAdminFormForSubjectIntervalStudyForGivenForm(int formId, int intervalId, int subjectId,
			int studyId) throws ObjectNotFoundException, CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			AdministeredForm admForm =
					dao.getAdminFormForSubjectIntervalStudyForGivenForm(formId, intervalId, subjectId, studyId);
			conn.commit();
			return admForm;
		} catch (Exception e) {
			this.rollback(conn);
			e.printStackTrace();
			throw new CtdbException(
					"ResponseManager->getAdminFormForSubjectIntervalStudyUserForGivenForm:Unable to get AdministeredForm form for given parameters combination of  the IDs: "
							+ formId + ":" + intervalId + ":" + subjectId + ":" + studyId + ":" + e.getMessage(),
					e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}



	/**
	 * Retrieves the administered form based on the administered ID for Unadminister Form.
	 *
	 * @param administeredFormId The administered form ID for retrieving the AdministeredForm
	 * @return AdministeredForm domain object
	 * @throws ObjectNotFoundException Thrown if the administered form does not exist
	 * @throws CtdbException Thrown if any other errors occur while processing
	 */
	public AdministeredForm getAdministeredFormForUnadmin(int administeredFormId)
			throws ObjectNotFoundException, CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);

			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			AdministeredForm admForm = dao.getAdministeredForm(administeredFormId);
			FormManager fm = new FormManager();

			/*
			 * Form form = fm.getFormNoQuestions(admForm.getForm().getId());
			 */

			Form form = new Form();
			form.setId(admForm.getEformid());

			admForm.setForm(form);

			conn.commit();
			return admForm;
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException("Unable to get administered form for Unadminister Form for the ID: "
					+ administeredFormId + " : " + e.getMessage(), e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}


	/**
	 * Retrieves the administered form for View Audit based on the administered ID.
	 *
	 * @param administeredFormId The administered form ID for retrieving the AdministeredForm
	 * @return AdministeredForm domain object
	 * @throws ObjectNotFoundException Thrown if the administered form does not exist
	 * @throws CtdbException Thrown if any other errors occur while processing
	 */
	public AdministeredForm getAdministeredFormForViewAudit(int administeredFormId)
			throws ObjectNotFoundException, CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);

			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			AdministeredForm admForm = dao.getAdministeredForm(administeredFormId);
			FormManager fm = new FormManager();

			/*
			 * Form form = fm.getFormNoQuestionAttribues(admForm.getForm().getId());
			 */
			// admForm.setForm(form);

			conn.commit();
			return admForm;
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException(
					"Unable to get administered form for the ID: " + administeredFormId + " : " + e.getMessage(), e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	/**
	 * Retrieves the administered form's certified information based on the administered ID.
	 *
	 * @param administeredFormId The administered form ID for retrieving the AdministeredForm
	 * @return AdministeredForm domain object
	 * @throws ObjectNotFoundException Thrown if the administered form does not exist
	 * @throws CtdbException Thrown if any other errors occur while processing
	 */
	public AdministeredForm getCertifiedInfo(int administeredFormId) throws ObjectNotFoundException, CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			AdministeredForm admForm = dao.getCertifiedInfo(administeredFormId);
			conn.commit();
			return admForm;
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException("Unable to get administered form's certified information for the ID: "
					+ administeredFormId + " : " + e.getMessage(), e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	/**
	 * Retrieves the administered form's final locked information based on the administered ID.
	 *
	 * @param administeredFormId The administered form ID for retrieving the AdministeredForm
	 * @return AdministeredForm domain object
	 * @throws ObjectNotFoundException Thrown if the administered form does not exist
	 * @throws CtdbException Thrown if any other errors occur while processing
	 */
	public AdministeredForm getFinalLockedInfo(int administeredFormId) throws ObjectNotFoundException, CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			AdministeredForm admForm = dao.getFinalLockedInfo(administeredFormId);
			conn.commit();
			return admForm;
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException("Unable to get administered form's final lock information for the ID: "
					+ administeredFormId + " : " + e.getMessage(), e);
		} finally {
			rollback(conn);
			this.close(conn);
		}
	}

	/**
	 * Collect data serach method by yogi
	 * 
	 * @param dcrc,dataForm
	 * @return
	 * @throws ObjectNotFoundException
	 * @throws CtdbException
	 */

	public List<DataCollectionLandingForm> getCollectDataSearchResult(DataCollectionLandingForm dataForm,
			int protocolId, Map<String, String> searchOptions) throws ObjectNotFoundException, CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			List<DataCollectionLandingForm> dataFormList =
					dao.getCollectDataSearchResult(dataForm, protocolId, searchOptions);
			conn.commit();
			return dataFormList;
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException(
					"Unable to get data form for the ID: " + dataForm.getFormId() + " : " + e.getMessage(), e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	public List<DataCollectionLandingForm> getInProgressSearchResult(DataCollectionLandingForm dataForm, int protocolId)
			throws ObjectNotFoundException, CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			List<DataCollectionLandingForm> dataFormList = dao.getInProgressSearchResult(dataForm, protocolId);
			conn.commit();
			return dataFormList;
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException(
					"Unable to get data form for the ID: " + dataForm.getFormId() + " : " + e.getMessage(), e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	/**
	 * Retrieves the administered form based on the administered ID and user object.
	 *
	 * @param administeredFormId The administered form ID for retrieving the AdministeredForm
	 * @param user The user object
	 * @return AdministeredForm domain object
	 * @throws ObjectNotFoundException Thrown if the administered form does not exist
	 * @throws CtdbException Thrown if any other errors occur while processing
	 */
	public AdministeredForm getAdministeredForm(int administeredFormId, User user)
			throws ObjectNotFoundException, CtdbException {
		Connection conn = null;
		logger.info("ResponseManager->getAdministeredForm->aformId:\t" + administeredFormId + "User Name:\t"
				+ user.getUsername());
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);

			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			AdministeredForm admForm = dao.getAdministeredForm(administeredFormId, user);

			commit(conn);
			return admForm;
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}


	/**
	 * Retrieves the administered form based on the administered ID and user object. It contains the list of final
	 * answers and edit answers for the administered form. It also contains the list of answers for the questions for
	 * data entry 1 and data entry 2.
	 *
	 * @param responseId The response ID for retrieving the infomation
	 * @return AdministeredForm domain object
	 * @throws ObjectNotFoundException Thrown if the administered form does not exist
	 * @throws CtdbException Thrown if any other errors occur while processing
	 */
	public PatientCalendarCellResponse getCalendarCellResponse(int responseId)
			throws ObjectNotFoundException, CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);

			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);

			PatientCalendarCellResponse responseFinal = dao.getFinalCalendarResponse(responseId);
			if (responseFinal.getResponse1() != null) {
				responseFinal.setResponse1(dao.getDraftCalendarResponse(responseFinal.getResponse1().getId()));
			}

			if (responseFinal.getResponse2() != null) {
				responseFinal.setResponse2(dao.getDraftCalendarResponse(responseFinal.getResponse2().getId()));
			}
			conn.commit();
			return responseFinal;
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException("Unable to get the response for the id : " + responseId + " : " + e.getMessage(),
					e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}


	public void completeGetAdministeredForm(AdministeredForm admForm, boolean onlyDiscrepancy)
			throws CtdbException, SQLException {
		completeGetAdministeredForm(admForm, onlyDiscrepancy, true);
	}

	public void completeGetAdministeredForm(AdministeredForm admForm, boolean onlyDiscrepancy, boolean fillAnswers)
			throws CtdbException, SQLException {
		logger.info("ResponseManager->completeGetAdministeredForm#--------------------------");
		// get all questions for form
		List<Question> allFormQuestions = new ArrayList<Question>();
		List<Question> calQuestions = new ArrayList<Question>();

		for (List<Section> row : admForm.getForm().getRowList()) {
			for (Section section : row) {
				if (section != null) {
					for (Question question : section.getQuestionList()) {
						if (question.getType() == QuestionType.PATIENT_CALENDAR) {
							question = (PatientCalendarQuestion) question;
						} else if (question.getType().equals(QuestionType.IMAGE_MAP)) {
							question = (ImageMapQuestion) question;
						}

						question.setParentSectionName(section.getName());
						question.setSectionId(section.getId());
						allFormQuestions.add(question);

						if (question.getFormQuestionAttributes().isCalculatedQuestion()) {
							calQuestions.add(question);
						}
					}
				}
			}
		}

		Response response1;
		Response response2;
		List<Response> responses = new ArrayList<Response>();

		for (Question question : allFormQuestions) {
			Response response = null;
			if (question.getType() == QuestionType.PATIENT_CALENDAR) {
				response = this.CreateCalendarResponse(admForm.getId(), question);
			} else {
				response = new Response();
				response.setQuestion(question);
				response1 = new Response();

				if (question instanceof PatientCalendarQuestion) {
					PatientCalendarQuestion pcq1 = (PatientCalendarQuestion) question;
					response1.setQuestion(pcq1);
				} else {
					response1.setQuestion(question);
				}

				response.setResponse1(response1);
				response2 = new Response();

				if (question instanceof PatientCalendarQuestion) {
					PatientCalendarQuestion pcq2 = (PatientCalendarQuestion) question;
					response2.setQuestion(pcq2);
				} else {
					response2.setQuestion(question);
				}

				response.setResponse2(response2);
			}

			responses.add(response);
		}

		admForm.setResponses(responses);

		List<String> answers = null; // new ArrayList();
		int responseId = Integer.MIN_VALUE;
		int response1Id = Integer.MIN_VALUE;
		String comment = "";

		// populate the final answers, edit answers and draft answers
		Connection conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);

		try {
			if (fillAnswers) {
				ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);

				int counter = 1;
				int responseLength = responses.size();
				for (Response response : responses) {
					logger.info("ResponseManager->completeGetAdministeredForm: filling answers " + counter + " of "
							+ responseLength);
					if (!(response instanceof CalendarResponse)) {
						if (admForm.getFinalLockDate() != null) {
							answers = dao.getFinalAnswers(admForm, response); // final answer
							response.setAnswers(answers);
							responseId = dao.getResponseId(admForm, response); // final ID
							response.setId(responseId);

							comment = dao.getResponseCommentById(responseId);
							response.setComment(comment);
						} else {
							// populate the draft answers for data entry 1
							response1 = response.getResponse1();
							List submitAnswers = new ArrayList();
							answers = dao.getDraftAnswers(admForm, response1, 1, submitAnswers); // draft collection 1
							response1.setAnswers(answers);
							response1.setSubmitAnswers(submitAnswers);

							response1Id = dao.getResponseDraftId(admForm, response1, 1); // response draft ID 1
							response1.setId(response1Id);
						}

						commit(conn);
					}
					counter++;
				}
			}
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
		logger.info("Finished ResponseManager->completeGetAdministeredForm#--------------------------");
	}


	private void getDescrepancies(AdministeredForm admForm, List calQuestions) {
		// List discrepancyResponses = new ArrayList();
		Map discrepancyMap = new HashMap();
		Map allMap = new HashMap();
		List parents = new ArrayList();
		List allParents = new ArrayList();
		List discrepancyList = new ArrayList();
		for (Iterator it = admForm.getResponses().iterator(); it.hasNext();) {
			Response response = (Response) it.next();
			allMap.put(Integer.toString(response.getQuestion().getId()), response);
			if (response.getQuestion().getFormQuestionAttributes().hasSkipRule())
				allParents.add(response);
			// no final answers are set for this response. Only
			// under this condition, discrepancy is possible.
			// Or no response object has been created in the database.

			if (response instanceof CalendarResponse) {
				if (((CalendarResponse) response).hasDataEntryDiscrepancy()) {
					discrepancyMap.put(Integer.toString(response.getQuestion().getId()), response);
					discrepancyList.add(response);
				}
			} else {
				// } else if (response.getId() == Integer.MIN_VALUE) {
				Response response1 = response.getResponse1();
				Response response2 = response.getResponse2();

				if (!response1.equals(response2)) {
					Question q = response.getQuestion();
					// discrepancyResponses.add(response);
					discrepancyMap.put(Integer.toString(q.getId()), response);
					discrepancyList.add(response);

					// Now check for skip rule.
					if (q.getFormQuestionAttributes().hasSkipRule())
						parents.add(response);
				}
			}
		}

		// Associate a parent response to its child responses whose
		// answers are in discrepancy.
		if (!allParents.isEmpty()) {
			for (Iterator it = allParents.iterator(); it.hasNext();) {
				Response r = (Response) it.next();
				Question q = r.getQuestion();
				List children = q.getFormQuestionAttributes().getQuestionsToSkip();
				for (Iterator itt = children.iterator(); itt.hasNext();) {
					Question qq = (Question) itt.next();
					Response childResponse = (Response) discrepancyMap.get(Integer.toString(qq.getId()));
					if (childResponse != null)
						childResponse.getVersionResponses().add(r);
				}
			}
		}

		// If at least one discrepancy question is the parent question
		// of a skip rule, then we will find out its dependent questions
		if (!parents.isEmpty()) {
			for (Iterator it = parents.iterator(); it.hasNext();) {
				Response r = (Response) it.next();
				Question q = r.getQuestion();
				List qFromParent = null;

				if (q.getFormQuestionAttributes().hasSkipRule())
					qFromParent = q.getFormQuestionAttributes().getQuestionsToSkip();

				List childList = new ArrayList();
				for (Iterator itt = qFromParent.iterator(); itt.hasNext();) {
					Question qq = (Question) itt.next();

					Response rr = (Response) discrepancyMap.get(Integer.toString(qq.getId()));
					// If the toSkip questions are not in the discrepancy map,
					// we get them from all response list. They will be used during
					// validation during resolution of the discrepancies.
					if (rr == null) {
						rr = (Response) allMap.get(Integer.toString(qq.getId()));
						// the child question responses also store parent question
						// response in its versionResponses list.

						if (rr != null) {
							rr.getVersionResponses().add(r);
							childList.add(rr);
						}
					}
				}

				// These child responses are stored in the versionResponses of the
				// response object.
				r.setVersionResponses(childList);
			}
		}

		// Now deal with calculated question. Attach parent questions
		// to child questions in case of calculation. Responses from Calculated
		// questions are always treated as equals by response1.equals(response2).
		if (!calQuestions.isEmpty()) {
			List childList = new ArrayList();
			for (Iterator it = calQuestions.iterator(); it.hasNext();) {
				Question calQ = (Question) it.next();
				List qToCalculated = (calQ.getCalculatedFormQuestionAttributes().getQuestionsToCalculate());
				for (Iterator it1 = qToCalculated.iterator(); it1.hasNext();) {
					Question q = (Question) it1.next();
					Response calDR = (Response) discrepancyMap.get(Integer.toString(q.getId()));
					Response calR = (Response) allMap.get((Integer.toString(q.getId())));
					childList.add(calR);
					if (calDR != null) {
						List vResps = calDR.getVersionResponses();
						/*
						 * if (vResps.isEmpty()) {
						 */
						vResps.add((Response) allMap.get(Integer.toString(calQ.getId())));
						/* } */
					}
				}
				// Attach child responses to parent response.
				Response parentR = (Response) allMap.get(Integer.toString(calQ.getId()));
				parentR.setVersionResponses(childList);
			}
		}

		// set only discrepancy responses
		admForm.setResponses(discrepancyList);
	}

	public PatientCalendarCellResponse getCalendarDataEntryDraft(PatientCalendarCellResponse pccr, int adminFormId)
			throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			PatientCalendarCellResponse pCal = dao.getCalendarDataEntryDraft(pccr, adminFormId);
			conn.commit();
			return pCal;
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException("Unable to get response draft and answers for the data entry draft ID: "
					+ adminFormId + " : " + e.getMessage(), e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}

	}

	private CalendarResponse CreateCalendarResponse(int administeredFormId, Question q) throws CtdbException {

		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			CalendarResponse cResponse = new CalendarResponse();
			cResponse.setAdminFormId(administeredFormId);
			PatientCalendarQuestion pcq = new PatientCalendarQuestion();
			pcq.setId(q.getId());
			pcq.setVersion(q.getVersion());
			cResponse.setQuestion(q);

			Response r1 = new Response();
			PatientCalendarQuestion q1 = new PatientCalendarQuestion();
			q1.setId(q.getId());
			q1.setVersion(q.getVersion());

			r1.setQuestion(q1);
			cResponse.setResponse1(r1);

			Response r2 = new Response();
			PatientCalendarQuestion q2 = new PatientCalendarQuestion();
			q2.setId(q.getId());
			q2.setVersion(q.getVersion());

			r2.setQuestion(q2);
			cResponse.setResponse2(r2);

			ResponseManagerDao.getInstance(conn).setResponseCalendarDataEntryDraft(administeredFormId, cResponse);
			ResponseManagerDao.getInstance(conn).setResponseCalendarDataEntryFinal(administeredFormId, cResponse);
			conn.commit();
			return cResponse;

		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException("Unable to build patient calendar response for admin form: " + administeredFormId
					+ " and qId : " + q.getId() + " : " + e.getMessage(), e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}



	/**
	 * Retrieves the responses and answers for the administered form
	 *
	 * @param dataEntryDraftId the administeredForm id
	 * @return The list of the responses for the administered form
	 * @throws CtdbException thrown if any errors occur while processing
	 */
	public List getResponsesDraft(int dataEntryDraftId) throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			List reponseDraftList = dao.getResponsesDraft(dataEntryDraftId);
			conn.commit();
			return reponseDraftList;
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException("Unable to get response draft and answers for the data entry draft ID: "
					+ dataEntryDraftId + " : " + e.getMessage(), e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}


	/**
	 * Retrieves the resolve information for the administered form's double key
	 *
	 * @param admForm the administeredForm object
	 * @return The list of the resolve info for the administered form
	 * @throws CtdbException thrown if any errors occur while processing
	 */
	public List getResolveInfo(AdministeredForm admForm) throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);

			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);

			List resolveResponses = new ArrayList();
			if (admForm.getForm().getSingleDoubleKeyFlag() == 1 || /*
																	 * (admForm.getForm().getSingleDoubleKeyFlag() == 2
																	 * && !dao.isDoubleEntryLocked(admForm.getId())) ||
																	 */ !dao.updateAndReturnDiscrepancyFlag(admForm)) {

				// do not proceed if its a single key form or if there are no discrepancies between de 1 and de 2
				return resolveResponses;
			}

			Map disparatQuestions = dao.getDiscrepancyQuestions(admForm.getId());

			for (Iterator i = disparatQuestions.keySet().iterator(); i.hasNext();) {
				String sId_qId = (String) i.next();
				String[] splits = sId_qId.split("_");
				String sId = splits[0];
				String qId = splits[1];
				Response r = dao.getResolveInfoForLog(admForm.getId(), Integer.parseInt(qId), Integer.parseInt(sId),
						Integer.parseInt((String) disparatQuestions.get(sId_qId)));
				resolveResponses.add(r);
			}
			conn.commit();
			return resolveResponses;
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException("Unable to get resolve information for the administered form ID: " + admForm.getId()
					+ " : " + e.getMessage(), e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	private boolean checkAnswersSame(Response responseDraft1, Response responseDraft2) throws CtdbException {
		boolean isSame = true;
		List answer1 = responseDraft1.getAnswers();
		List answer2 = responseDraft2.getAnswers();
		if (answer1 == null && answer2 != null) {
			isSame = false;
		} else if (answer1 != null && answer2 == null) {
			isSame = false;
		} else if (answer1.size() != answer2.size()) {
			isSame = false;
		} else {
			for (Iterator it = answer1.iterator(); it.hasNext();) {
				String answer = (String) it.next();
				if (!this.isAnswerInList(answer, answer2)) {
					isSame = false;
				}
			}
		}
		return isSame;
	}

	private boolean isAnswerInList(String answer, List answerList) {
		boolean found = false;
		for (Iterator it = answerList.iterator(); it.hasNext();) {
			String a = (String) it.next();

			if (a.equalsIgnoreCase(answer)) {
				found = true;
				break;
			}
		}
		return found;
	}

	/*
	 * retrieve completed forms for a given patient
	 */
	public List getCompletedPatientForms(Integer patientId, int protocolId) throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			List completedPatientForms = dao.getCompletedPatientForms(patientId, protocolId);
			conn.commit();
			return completedPatientForms;
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException(
					"Unable to get completed administered forms the patient ID: " + patientId + " : " + e.getMessage(),
					e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}



	/**
	 * Method to display getAllAdministeredFormInProgress and search on that list for IBIS new screen added by yogi
	 * 
	 * @param user
	 * @param protocolId
	 * @param afrc_pc
	 * @return
	 * @throws CtdbException
	 */
	public List<AdministeredForm> getAllAdministeredFormInProgress(User user, int protocolId) throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			List<AdministeredForm> aformList = dao.getAllAdministeredFormInProgress(user, protocolId);
			conn.commit();
			return aformList;
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException("Unable to get administered forms in progress for protocol Home: " + e.getMessage(),
					e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	/**
	 * Saves the response draft and its answer.
	 *
	 * @param response The Response object to create
	 * @param user The User object
	 * @throws DuplicateObjectException Thrown if the ResponseDraft exists in the system based on the unique constraints
	 * @throws CtdbException Thrown if any other errors occur while processing
	 */
	void createResponseDraft(Response response, User loggedInUser) throws DuplicateObjectException, CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			dao.createResponseDraft(response, loggedInUser);

			if (!response.getAnswers().isEmpty()) {
				dao.createResponseDraftAnswers(response, false);
			}

			conn.commit();
		} catch (SQLException e) {
			this.rollback(conn);
			throw new CtdbException("Unable to create response draft: " + e.getMessage(), e);
		} catch (DuplicateObjectException doe) {
			this.rollback(conn);
			throw doe;
		} catch (CtdbException ce) {
			this.rollback(conn);
			throw ce;
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}



	/**
	 * Executes express data entry, that is takes the steps of certifying and final locking a form This will halt if
	 * there are discrepancies on a double key form. May execute the bypass double key data entry option as well. This
	 * method is called from an express data entry thread.
	 *
	 * @param admForm
	 * @param u
	 * @param bypassDoubleKeyDataEntry
	 * @throws ObjectNotFoundException
	 * @throws CtdbException
	 */
	public void performExpressDataEntry(AdministeredForm admForm, User u, boolean bypassDoubleKeyDataEntry,
			User loggedInUser) throws ObjectNotFoundException, CtdbException {
		Connection conn = null;
		try {
			// System.out.println("in performExpressDataEntry");
			// logger.info("ResponseManager->performExpressDataEntry aForm
			// Id:\t"+admForm.getId()+"User:\t"+u.getUsername());
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);

			/*
			 * method updateDataEntryDraft
			 */


			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);



			boolean canContinuePastDoubleKeyLock = true;
			/*
			 * if (admForm.getForm().getSingleDoubleKeyFlag() == 2) { //double entry needs the data entry draft status
			 * updated right away dao.updateDataEntryDraft(admForm,loggedInUser);
			 * 
			 * if (dao.isDoubleEntryLocked(admForm.getId())) { canContinuePastDoubleKeyLock =
			 * !dao.updateAndReturnDiscrepancyFlag(admForm); conn.commit(); } else { canContinuePastDoubleKeyLock =
			 * false; } }
			 */
			// System.out.println(canContinuePastDoubleKeyLock);
			if (canContinuePastDoubleKeyLock) {
				// either the form is single key or double key and both entrys finished with no descrepancy
				// certify

				// This is an alternate copyDraftToResponse method to optimize the form locking performance
				// This method executes single queries on the list of responses, rather than
				// iterating through each response to execute an individual query for each.
				// Update by TT on 2/1/17
				long start7 = System.currentTimeMillis();
				dao.copyDraftToResponse2(admForm, u);
				long end7 = System.currentTimeMillis();
				logger.info("PERFORMANCE DEBUG :: copyDraftToResponse2 Timer == " + (end7 - start7));


				admForm.setCertifiedBy(u.getId());
				// dao.updateAdministeredForm(admForm);

				// final lock
				admForm.setFinalLockBy(loggedInUser.getId());
				dao.updateAdministeredForm(admForm);
				conn.commit();
			}
			// Update data entry draft table only if we don't have any issues in copying draft entry to final patient
			// response table...for single entry
			if (admForm.getForm().getSingleDoubleKeyFlag() == 1) {
				dao.updateDataEntryDraft(admForm, loggedInUser);
			}
			// conn.commit();
			conn.commit();

		} catch (DuplicateObjectException doe) {
			doe.printStackTrace();
			throw new DuplicateObjectException(
					"Duplicate data detected while copying from draft to response. Please consult with your vendor for data cleanup.");
		}

		catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException("Unknown error occurred while performExpressDataEntry : " + e.getMessage(), e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	public boolean checkGuid(int aFormid) throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			boolean guid = dao.checkGuid(aFormid);
			conn.commit();
			return guid;
		} catch (Exception e) {
			this.rollback(conn);
			e.printStackTrace();
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
		return true;
	}



	/**
	 * Updates the data entry draft lock date.
	 *
	 * @param admForm The AdministeredForm object
	 * @throws ObjectNotFoundException Thrown if the data entry draft does not exist
	 * @throws CtdbException Thrown if any other errors occur
	 */
	public void updateDataEntryDraft(AdministeredForm admForm) throws ObjectNotFoundException, CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			conn.commit();
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException("Unknown error occurred while updating the data entry draft : " + e.getMessage(),
					e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	/**
	 * Updates the administer form's information, such as certified info and final lock info.
	 *
	 * @param admForm The administered form object to update
	 * @throws ObjectNotFoundException Thrown if the administer form does not exist
	 * @throws CtdbException Thrown if any other errors occur
	 */
	public void updateAdministeredForm(AdministeredForm admForm) throws ObjectNotFoundException, CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			dao.updateAdministeredForm(admForm);
			conn.commit();
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException("Unknown error occurred while updating the administer form : " + e.getMessage(), e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}


	/**
	 * Updates the visit date for the admisistered form object
	 * 
	 * @param admForm
	 * @throws ObjectNotFoundException
	 * @throws CtdbException
	 */
	public void updateAdministeredFormVisitDate(AdministeredForm admForm)
			throws ObjectNotFoundException, CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			dao.updateAdministeredFormVisitDate(admForm);
			conn.commit();
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException("Unknown error occurred while updating the administer form : " + e.getMessage(), e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}


	/**
	 * Creates the final response and its answer.
	 *
	 * @param response The Response object to create
	 * @throws DuplicateObjectException Thrown if the Response exists in the system based on the unique constraints
	 * @throws CtdbException Thrown if any other errors occur while processing
	 * @throws UnknownHostException
	 * @throws MessagingException
	 */
	void createResponse(Response response, User user)
			throws DuplicateObjectException, CtdbException, UnknownHostException, MessagingException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);

			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			dao.createResponse(response, user);

			if (!response.getAnswers().isEmpty()) {
				dao.createResponseAnswers(response);
			}
			conn.commit();
		} catch (SQLException e) {
			this.rollback(conn);
			throw new CtdbException("Unable to create response: " + e.getMessage(), e);
		} catch (DuplicateObjectException doe) {
			this.rollback(conn);
			throw doe;
		} catch (CtdbException ce) {
			this.rollback(conn);
			throw ce;
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	/**
	 * Updates the final response and its answer.
	 *
	 * @param response The Response object to update
	 * @throws ObjectNotFoundException Thrown if the Response does not exist
	 * @throws CtdbException Thrown if any other errors occur while processing
	 */
	void updateResponse(Response response, User user) throws ObjectNotFoundException, CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			dao.updateResponse(response, user);
			if (!response.getAnswers().isEmpty()) {
				dao.createResponseAnswers(response);
			} else {
				dao.deleteResponseAnswers(response);
			}
			conn.commit();
		} catch (SQLException e) {
			this.rollback(conn);
			throw new CtdbException("Unable to update response and its answers: " + e.getMessage(), e);
		} catch (Exception et) {
			this.rollback(conn);
			throw new CtdbException(
					"Unknown error occurred while updating the response and its answers: " + et.getMessage(), et);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}



	/**
	 * Gets the list of AdministeredForm based on the administered form id and user object
	 *
	 * @return The list of administered form objects
	 * @throws CtdbException Thrown if any errors occur while processing
	 */
	public List getDataEntrySummary(Map searchOptions) throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			List summaryEntryList = dao.getDataEntrySummary(searchOptions);
			conn.commit();
			return summaryEntryList;
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException("Unable to get data entry summary for the form ID: " + searchOptions.size() + " : "
					+ e.getMessage(), e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	// Added by Yogi for IBIS project
	/**
	 * Method to get the patient previous collection data
	 * 
	 * @param afrc
	 * @param protocolId
	 * @param searchOptions
	 * @return
	 * @throws CtdbException
	 */
	public List<AdministeredForm> myCollectionsList(int protocolId, int userId, Map<String, String> searchOptions,
			PaginationData pageData, String key) throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			List<AdministeredForm> summaryList =
					dao.myCollectionsList(protocolId, userId, searchOptions, pageData, key);
			conn.commit();
			return summaryList;
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException("Unable to get data entry summary for the form ID: " + searchOptions.size() + " : "
					+ e.getMessage(), e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	public int countMyCollectionsList(int protocolId, int userId, Map<String, String> searchOptions, String key)
			throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			int countTotal = dao.countMyCollectionsList(protocolId, userId, searchOptions, key);
			conn.commit();
			return countTotal;
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException("Unable to get data entry summary for the form ID: " + searchOptions.size() + " : "
					+ e.getMessage(), e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}



	/**
	 * Copies a response draft to the final response table
	 *
	 * @param admForm The administered form object to copy drafts from/to
	 * @param user The user copying
	 * @throws CtdbException Thrown if any errors occur while processing
	 */
	public void copyDraftToResponse(AdministeredForm admForm, User user) throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			dao.copyDraftToResponse(admForm, user);
			conn.commit();
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException("Unknown error occurred while copying response draft to response for "
					+ "the administer form ID " + admForm.getId() + " : " + e.getMessage(), e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}



	/**
	 * When editing an adminstered form's meta data, the old and new values must be logged
	 *
	 * @param data
	 * @throws CtdbException
	 */
	public void recordAdminFormMetaDataEdit(AdminFormMetaDataEdit data) throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);

			dao.recordAdminFormMetaDataEdit(data);
			conn.commit();
		} catch (SQLException e) {
			this.rollback(conn);
			throw new CtdbException("failure recording meta data edit", e);
		} catch (CtdbException e) {
			this.rollback(conn);
			throw new CtdbException(e.getMessage());
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	public void updateClinicalTrialAdmFormVisitDate(AdministeredForm aform) throws CtdbException, SQLException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			dao.updateClinicalTrialAdmFormVisitDate(aform);
			conn.commit();
		} catch (CtdbException e) {
			this.rollback(conn);
			throw new CtdbException(e.getMessage());
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}



	public void deleteVisibleAdministeredSection(int sectionId) throws CtdbException, SQLException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			dao.deleteVisibleAdministeredSection(sectionId);
			conn.commit();
		} catch (CtdbException e) {
			this.rollback(conn);
			e.printStackTrace();
			throw new CtdbException(e.getMessage());
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}



	public void updateQaReviewStatus(int aformId, boolean reviewed, boolean qaLocked)
			throws CtdbException, SQLException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			dao.updateQaReviewStatus(aformId, reviewed, qaLocked);
			conn.commit();
		} catch (CtdbException e) {
			this.rollback(conn);
			throw new CtdbException(e.getMessage(), e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	/**
	 * Used to edit an administered form, not the responses, only the patient interval etc.
	 *
	 * @param af
	 * @param intervalId
	 * @param patientId
	 * @param isClinicalTrial
	 * @throws CtdbException
	 */
	public void updateAdministeredFormMetaData(AdministeredForm af, int intervalId, int patientId,
			boolean isClinicalTrial) throws CtdbException {

		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			dao.updateAdministeredFormMetaData(af, intervalId, patientId, isClinicalTrial);
			conn.commit();
		} catch (SQLException e) {
			this.rollback(conn);
			throw new CtdbException("failure recording meta data edit", e);
		} catch (CtdbException e) {
			this.rollback(conn);
			throw new CtdbException(e.getMessage());
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	/**
	 * check if the administered form edited by the user is already present in the system.
	 *
	 * @param adminForm the administered form that contains new meta data.
	 * @throws ObjectNotFoundException thrown if the administered form with new meta data is not found in the system.
	 * @throws CtdbException Thrown if any errors occur while processing
	 * @throws SQLException
	 */
	public boolean checkIsDuplicateAdminForm(AdministeredForm adminForm) throws CtdbException, SQLException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			boolean duplicateAdminForm = dao.checkIsDuplicateAdminForm(adminForm);
			conn.commit();
			return duplicateAdminForm;
		} catch (CtdbException e) {
			this.rollback(conn);
			e.printStackTrace();
			throw new CtdbException(e.getMessage());
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	public boolean isDuplicateAdminForm(User u, AdministeredForm af, int intervalId, int patientId)
			throws CtdbException, SQLException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			boolean isDuplicateAdminForm = dao.isDuplicateAdminForm(u, af, intervalId, patientId);
			conn.commit();
			return isDuplicateAdminForm;
		} catch (CtdbException e) {
			this.rollback(conn);
			e.printStackTrace();
			throw new CtdbException(e.getMessage());
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}



	public boolean areThereDraftAnswers(int aformid) throws CtdbException, SQLException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			boolean areThereDraftAnswers = dao.areThereDraftAnswers(aformid);
			conn.commit();
			return areThereDraftAnswers;
		} catch (CtdbException e) {
			this.rollback(conn);
			e.printStackTrace();
			throw new CtdbException(e.getMessage());
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	/**
	 * Generates a CSV extract for the administered form with all the questions and answers.
	 *
	 * @param administeredForm The form to generate
	 * @return A CSV string representation of the form for download and offline data entry
	 * @throws CtdbException Thrown if any errors occur while processing
	 */
	public String downloadFormInCSV(AdministeredForm administeredForm, boolean usePatientName) throws CtdbException {
		FormManager fm = new FormManager();
		ProtocolManager pm = new ProtocolManager();
		QuestionManager qm = new QuestionManager();
		Form form = fm.getForm(administeredForm.getForm().getId(),
				administeredForm.getForm().getVersion().getVersionNumber());
		Protocol protocol = pm.getProtocol(form.getProtocolId());
		Interval interval = pm.getInterval(administeredForm.getInterval().getId());
		Patient patient = this.getPatientForCollection(protocol.getId(), administeredForm.getPatient().getId());


		// Get the list of question ids for the form in order of their question orders.
		List<Integer> qOrders =
				fm.getQuestionOrderInForm(form.getId(), administeredForm.getForm().getVersion().getVersionNumber());

		StringBuffer csv = new StringBuffer(1000);

		csv.append("Protocol Name,");
		csv.append(protocol.getName().replace(',', ' '));
		csv.append("\n");
		csv.append("Protocol Number,");
		csv.append(protocol.getProtocolNumber().replace(',', ' '));
		csv.append("\n");
		csv.append("Form Name,");
		csv.append(form.getName().replace(',', ' '));
		csv.append("\n");
		csv.append("Form ID,");
		csv.append(form.getId());
		csv.append("\n");
		csv.append("Interval Name,");
		csv.append(interval.getName().replace(',', ' '));
		csv.append("\n");
		if (usePatientName) {
			csv.append("Patient Name,");
			csv.append(patient.getLastName() + "-" + patient.getFirstName());
		} else {
			csv.append("Patient ID,");
			csv.append(patient.getSubjectId());
		}
		csv.append("\n");
		csv.append("Questions\n");
		csv.append("Question Name,Question Text,Question ID,Answer\n");

		List<Response> responses = administeredForm.getResponses();

		// We need to rearrange these responses in order of question ids in the
		// form, so question order in the csv file matches the question order
		// in the form. First convert the list to a map.
		Map<Integer, Response> map = new HashMap<Integer, Response>();
		for (Iterator<Response> itr = responses.iterator(); itr.hasNext();) {
			Response response = itr.next();
			Question q = response.getQuestion();
			map.put(q.getId(), response);
		}
		// Then order the responses in order of question ids in the form.
		responses.clear();

		for (Iterator<Integer> itr = qOrders.iterator(); itr.hasNext();) {
			Integer qid = itr.next();
			Response resp = map.get(qid);

			if (resp != null)
				responses.add(resp);
		}
		map.clear();

		Question question;
		Response response;
		List<String> answers = new ArrayList<String>();
		for (Iterator<Response> itr = responses.iterator(); itr.hasNext();) {
			response = itr.next();
			question = qm.getQuestion(response.getQuestion().getId());

			csv.append(question.getName().replace(',', ' '));
			csv.append(",");
			csv.append(question.getText().replace(',', ' '));
			csv.append(",");
			csv.append(question.getId());
			csv.append(",");

			answers = response.getAnswers();

			String answer;
			for (Iterator<String> it = answers.iterator(); it.hasNext();) {
				answer = it.next();
				csv.append(answer.replace(',', ' '));
				csv.append(" ,");
			}

			csv.append("\n");
		}

		return csv.toString();
	}

	/**
	 * Retrieves the responses on an form from the RESPONSE table (as oppose to from the RESPONSEDRAFT table). It also
	 * gets the most updated answers after possible edit by administrator.
	 *
	 * @param administeredFormId The administered form ID for retrieving the AdministeredForm
	 * @param isGettingEdit If getting the edited answers
	 * @return AdministeredForm domain object which contains responses
	 * @throws ObjectNotFoundException Thrown if the administered form does not exist
	 * @throws CtdbException Thrown if any other errors occur while processing
	 */
	public AdministeredForm getUpdatedResponses(int administeredFormId, boolean isGettingEdit)
			throws ObjectNotFoundException, CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			AdministeredForm admForm;
			try {
				admForm = dao.getAdministeredFormMetaData(administeredFormId);
			} catch (ObjectNotFoundException onfe) { // try to get it without any responses
				admForm = dao.getAdministeredFormMetaDataNoResponses(administeredFormId);
			}
			FormManager fm = new FormManager();
			admForm.setForm(fm.getFormAndSetofQuestions(admForm.getForm().getId()));
			admForm.setResponses(dao.getUpdatedResponses(admForm, isGettingEdit));
			conn.commit();
			return admForm;
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException(
					"Unable to get administered form for the ID: " + administeredFormId + " : " + e.getMessage(), e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	/**
	 * Retrieves all data entries for the data entry 1 or 2. The list includes the reassign information and data entry
	 * lock information. This is for View Audit for who has the View Data Entry Audit Trail privilege.
	 *
	 * @param admFormId The administered form ID for retrieving the data entry.
	 * @param dataEntryFlag data entry 1 or 2
	 * @return List the list of data entry information including the reassign info and lock info.
	 * @throws CtdbException thrown if any errors occur
	 */
	public List<DataEntryDraft> getDataEntries(int admFormId, int dataEntryFlag) throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			List<DataEntryDraft> dataEntries = dao.getDataEntries(admFormId, dataEntryFlag);
			this.commit(conn);

			return dataEntries;
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	public int getDataEntryDraftId(int administeredFormId, int userId) throws CtdbException, SQLException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			int draftId = dao.getDataEntryDraftId(administeredFormId, userId);
			conn.commit();

			return draftId;
		} finally {
			this.rollback(conn);
			this.close(conn);
		}

	}

	public int getDataEntryFlag(int administeredFormId, int userId) throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			int entryFlag = dao.getDataEntryFlag(administeredFormId, userId);
			this.commit(conn);

			return entryFlag;
		} finally {
			this.rollback(conn);
			this.close(conn);
		}

	}

	public String getDataEntryUserName(int administeredFormId, int dataEntryFlag) throws CtdbException, SQLException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			String username = dao.getDataEntryUserName(administeredFormId, dataEntryFlag);

			return username;
		} finally {
			this.close(conn);
		}
	}

	/**
	 * Retrieves all edit answers and reasons.
	 *
	 * @param admFormId The administered form ID for retrieving the edit answers and reasons
	 * @return List the list of Response objects for the admFormId
	 * @throws CtdbException thrown if any errors occur
	 * @throws SQLException
	 */
	public List<EditAnswerDisplay> getEditArchives(int admFormId, boolean isFinalLocked)
			throws CtdbException, SQLException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			List<EditAnswerDisplay> editArchives = dao.getEditArchives(admFormId, isFinalLocked);
			conn.commit();

			return editArchives;
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	/**
	 * Retrieves all edit answers and reasons.
	 *
	 * @param admFormId The administered form ID for retrieving the edit answers and reasons
	 * @return List the list of Response objects for the admFormId
	 * @throws CtdbException thrown if any errors occur
	 * @throws SQLException
	 */
	public List<EditAnswerDisplay> getEditArchives(Form form, int admFormId, boolean isFinalLocked)
			throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			return dao.getEditArchives(form, admFormId, isFinalLocked);
		} finally {
			this.close(conn);
		}
	}

	/**
	 * Retrieves adminisered form meta data editing history.
	 *
	 * @param admFormId The administered form ID for retrieving the editing administered form meta data history.
	 * @return List the list of MetaDataHistory objects for the admFormId.
	 * @throws CtdbException thrown if any errors occur
	 */
	public List getMetaDataHistory(int admFormId, boolean finalLocked) throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			List metaData = dao.getMetaDataHistory(admFormId, finalLocked);
			conn.commit();
			return metaData;
		} catch (Exception e) {
			this.rollback(conn);
			e.printStackTrace();
			throw new CtdbException("Please see stack trace");
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	/**
	 * Updates the answers in the patientresponse table for all calculated type questions in a given administered form.
	 * <p/>
	 * This only needs to be called when the data in the patientresponse table has changed.
	 *
	 * @param administeredFormId The administered form ID for retrieving the AdministeredForm
	 * @throws CtdbException Thrown if any other errors occur while processing
	 */
	public void updateCalculatedResponses(int administeredFormId, User user) throws CtdbException {
		// need to make sure the order of answers retrieved from the patientresponse table
		// is the original order of answers in the question, for mult-answer question type.
		try {
			List<Response> responses = this.getUpdatedResponses(administeredFormId, false).getResponses();
			Map<Integer, List<String>> map = this.getMapQuestionIdAnswers(responses);

			Iterator<Response> iter = responses.iterator();
			while (iter.hasNext()) {
				Response response = iter.next();
				Question question = response.getQuestion();
				List<String> answers = new ArrayList<String>();

				if (question.getFormQuestionAttributes().isCalculatedQuestion()) {
					List<Question> questions = question.getCalculatedFormQuestionAttributes().getQuestionsToCalculate();
					boolean isFirst = true;
					boolean isFirstFound = false;

					Iterator<Question> iter1 = questions.iterator();
					while (iter1.hasNext()) {
						Question q = iter1.next();
						List<String> ans = map.get(q.getId());

						if (ans != null && !ans.isEmpty()) {
							// no need to check. validation is already done.
							answers.addAll(ans);

							if (isFirst) {
								isFirstFound = true;
							}
						}

						if (isFirst) {
							isFirst = false;
						}
					}

					// change the list object from String to Double
					List<Double> answersDouble = new ArrayList<Double>();
					try {
						Iterator<String> iter2 = answers.iterator();
						while (iter2.hasNext()) {
							String answer = iter2.next();
							Double answerDouble = Double.valueOf(answer);

							answersDouble.add(answerDouble);
						}

						if (!answersDouble.isEmpty()) {
							if (!isFirstFound && question.getCalculatedFormQuestionAttributes()
									.getCalculationType() == CalculationType.DIFFERENCE) {
								response.setAnswers(new ArrayList<String>());
							} else if (answersDouble.size() == 1 && question.getCalculatedFormQuestionAttributes()
									.getCalculationType() == CalculationType.DIVISION) {
								response.setAnswers(new ArrayList<String>());
							} else {
								double d = InputHandler.doCalculation(question, answersDouble);
								List<String> finalAnswers = new ArrayList<String>();
								finalAnswers.add(Double.toString(d));
								response.setAnswers(finalAnswers);
							}
						} else {
							response.setAnswers(new ArrayList<String>());
						}
					} catch (NumberFormatException nfe) {
						response.setAnswers(new ArrayList<String>());
					}

					this.updateResponse(response, user);
				}
			}
		} catch (CtdbException ce) {
			throw ce;
		} catch (Exception e) {
			throw new CtdbException(
					"Unable to update answers for calculated questions in the patientresponse table for the administered form ID: "
							+ administeredFormId,
					e);
		}
	}

	/**
	 * Get a hash map from question IDs to List of answers (Strings) based on a list of responses
	 *
	 * @param responses List of responses
	 * @return A map from question IDs to List of string answers
	 */
	private Map<Integer, List<String>> getMapQuestionIdAnswers(List<Response> responses) {
		Map<Integer, List<String>> map = new HashMap<Integer, List<String>>();

		for (Response response : responses) {
			map.put(response.getQuestion().getId(), response.getAnswers());
		}

		return map;
	}

	/**
	 * Verify the answer read in from the data transfer template against all possible options for that question.
	 *
	 * @param answers List of answer options for that question.
	 * @param templateAnswer the string answer read in from the data transfer template.
	 * @return true if match, false otherwise.
	 */
	private boolean verifyTemplateAnswer(List answers, String templateAnswer) {
		for (Iterator itr = answers.iterator(); itr.hasNext();) {
			Answer answer = (Answer) itr.next();

			if (answer.getDisplay().equalsIgnoreCase(templateAnswer)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Retrieves Data entry assignment archives.
	 *
	 * @param admFormId The administered form ID for retrieving the Data entry assignment history
	 * @return List the list of Data entry assignment objects for the admFormId
	 * @throws CtdbException thrown if any errors occur
	 * @throws SQLException
	 */
	public List getAssignArchives(int admFormId) throws CtdbException, SQLException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			List assignArchives = dao.getAssignArchives(admFormId);
			conn.commit();

			return assignArchives;
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	/**
	 * Retrieves sorted User ID and name map by protocol.
	 *
	 * @return List the map of User ID and name
	 * @throws CtdbException thrown if any errors occur
	 * @throws SQLException
	 */
	public Map<String, String> getProtocolUsers(int pId) throws CtdbException, SQLException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			// conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			return dao.getProtocolUsers(pId);

		} catch (Exception e) {
			e.printStackTrace();
			throw new CtdbException("Please see stack trace");
		} finally {
			this.close(conn);
		}
	}

	public List<FormCollectionDisplay> getCollectedPatientDataByFormName(long studyId, long subjectId, String formName)
			throws CtdbException {
		Connection conn = null;
		List<FormCollectionDisplay> collectedPatientList = null;

		try {
			conn = CtdbManager.getConnection();
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			collectedPatientList = dao.getCollectedPatientDataByFormName(studyId, subjectId, formName);
		} finally {
			this.close(conn);
		}

		return collectedPatientList;
	}

	/**
	 * Seems to not be used???
	 *
	 * @param formId
	 * @return
	 * @throws CtdbException
	 * @throws SQLException
	 */
	public Collection getDataCollectionSummary(int formId) throws CtdbException, SQLException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			Collection collSumm = dao.getDataCollectionFormInfo(formId);
			conn.commit();

			return collSumm;
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	/**
	 * Retrieves Current Data Entry Assignment info.
	 *
	 * @param admFormId the Adninisteredform id
	 * @param dataEntryFlag 1 indicates for the first data entry; 2 indicates for the second data entry
	 * @return EditAssignment the Object contains Current Data Entry Assignment info
	 * @throws CtdbException thrown if any errors occur
	 * @throws SQLException
	 */
	public EditAssignment getEditAssignment(int admFormId, int dataEntryFlag) throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);

			EditAssignment ea = dao.getEditAssignment(admFormId, dataEntryFlag);
			this.commit(conn);

			return ea;
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	/**
	 * Save Data Re-Assign infor
	 *
	 * @param editAssignment the Object contains Data Entry Assignment info to be saved
	 * @throws DuplicateObjectException thrown if the user assigned to has started another data entry
	 * @throws CtdbException thrown if any errors occur
	 */
	public void saveDataEntryReAssign(EditAssignment editAssignment) throws DuplicateObjectException, CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			dao.saveDataEntryReAssign(editAssignment);
			this.commit(conn);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}


	// ================ added end 505951 ====================

	/**
	 * Unadminister the form with "In Progress" status.
	 *
	 * @param administeredForm The form to be unadministered
	 * @param dataEntryFlag the flag indicates the data entry 1 or 2
	 * @throws CtdbException thrown if any errors occur when processing
	 */
	public void unadministerForm(AdministeredForm administeredForm, int dataEntryFlag) throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);

			dao.unadministerForm(administeredForm, dataEntryFlag);
			conn.commit();
		} catch (SQLException e) {
			this.rollback(conn);
			throw new CtdbException(
					"Unable to unadminister the form for ID " + administeredForm.getId() + ": " + e.getMessage(), e);
		} catch (Exception et) {
			this.rollback(conn);
			throw new CtdbException("Unknown error occurred while unadministering the form for ID "
					+ administeredForm.getId() + ": " + et.getMessage(), et);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	public int getAttachmentIdFromResponse(Response response, AdministeredForm adm) throws CtdbException, SQLException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);

			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			int attch = dao.getAttachmentIdFromResponse(response, adm);
			conn.commit();
			return attch;
		} catch (Exception e) {
			this.rollback(conn);
			e.printStackTrace();
			throw new CtdbException("Please see stack trace");
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	public String getAttachmentNameFromResponse(Response response, AdministeredForm adm)
			throws CtdbException, SQLException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			conn.commit();
			return ResponseManagerDao.getInstance(conn).getAttachmentNameFromResponse(response, adm);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	/**
	 * Method to get the section group for parent sectionId
	 * 
	 * @param parentSectionId
	 * @return
	 * @throws CtdbException
	 */
	public List<Integer> getVisibleRepeableSectionsList(int adminFormId, int loggedInUserId) throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			List<Integer> visibleList = dao.getVisibleRepeableSectionsList(adminFormId, loggedInUserId);

			return visibleList;
		} finally {
			this.close(conn);
		}
	}

	/**
	 * Gets the ElementIds of the hidden sections and hidden questions
	 * 
	 * @param protocolId
	 * @param eformId
	 * @return
	 * @throws CtdbException
	 */
	public List<String> getHiddenSectionsQuestionsElementIds(int protocolId, int eformId) throws CtdbException {
		Connection conn = null;
		List<String> hiddenSectionsQuestionsElementIdsList = new ArrayList<String>();


		try {
			conn = CtdbManager.getConnection();
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);

			List<Integer> hiddenSectionsList = dao.getHiddenSectionsList(protocolId, eformId);
			Iterator<Integer> iter1 = hiddenSectionsList.iterator();
			while (iter1.hasNext()) {
				Integer hiddenSection = iter1.next();
				String sectionElementId = "sectionContainer_" + hiddenSection;
				hiddenSectionsQuestionsElementIdsList.add(sectionElementId);
			}

			List<String> hiddenQuestionsList = dao.getHiddenQuestionsList(protocolId, eformId);
			Iterator<String> iter2 = hiddenQuestionsList.iterator();
			while (iter2.hasNext()) {
				String hiddenSectionQuestion = iter2.next();
				String questionElementId = "questionContainer_" + hiddenSectionQuestion;
				hiddenSectionsQuestionsElementIdsList.add(questionElementId);
			}

			return hiddenSectionsQuestionsElementIdsList;
		} finally {
			this.close(conn);
		}
	}

	/**
	 * method that gets the max repeated number of sections open for list of aforms for a particular repeatable group
	 * 
	 * @param adminFormIds
	 * @param formId
	 * @param repeatedSectionParentId
	 * @return
	 * @throws CtdbException
	 */
	public int getMaxRepeatedSectionsForAdminForms(Form form, int[] adminFormIds, int repeatedSectionParentId)
			throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			int max = dao.getMaxRepeatedSectionsForAdminForms(form, adminFormIds, repeatedSectionParentId);

			return max;
		} finally {
			this.close(conn);
		}
	}

	/**
	 * Method that gets the collection data for list of aformids
	 * 
	 * @throws CtdbException
	 */
	public HashMap<Integer, HashMap<String, DataCollectionExport>> getDataCollectionExport(int[] aformIds, Form form)
			throws CtdbException {
		Connection conn = null;
		HashMap<Integer, HashMap<String, DataCollectionExport>> aformIdsDCEMap =
				new HashMap<Integer, HashMap<String, DataCollectionExport>>();
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			int aformId = -1;
			HashMap<String, DataCollectionExport> dceMap = null;
			boolean isLocked = false;
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			for (int i = 0; i < aformIds.length; i++) {
				aformId = aformIds[i];

				isLocked = dao.isAdministeredFormInFinalLockStatus(aformId);

				if (isLocked) {
					dceMap = dao.getDataCollectionExport_LOCKED(aformId, form);
				} else {
					dceMap = dao.getDataCollectionExport_DRAFT(aformId, form);
				}

				aformIdsDCEMap.put(Integer.valueOf(aformId), dceMap);
			}
			return aformIdsDCEMap;
		} finally {
			this.close(conn);
		}
	}

	public Integer getDataEntryDraftIdForGivenResponse(int aformId, int questionId, int sectionId)
			throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			Integer draftId = dao.getDataEntryDraftIdForGivenResponse(aformId, questionId, sectionId);
			conn.commit();
			return draftId;
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException("Unable to get dataEntryDraftId" + e.getMessage(), e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}

	/**
	 * Method to get the list of admin forms for interval subject user and study ids combination
	 * 
	 * @param intervalId
	 * @param subjectId
	 * @param userId
	 * @param studyId
	 * @return
	 * @throws CtdbException
	 */
	public List<AdministeredForm> getAdmininisteredFormForSubjectInterval(String intervalId, String subjectId,
			int userId, int studyId) throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			List<AdministeredForm> aFormList =
					dao.getAdmininisteredFormForSubjectInterval(intervalId, subjectId, userId, studyId);
			conn.commit();
			return aFormList;
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException("Unable to get active form group list" + e.getMessage(), e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}

	}

	/**
	 * Method to get the list of admin forms for interval subject and study combination
	 * 
	 * @param intervalId
	 * @param subjectId
	 * @param studyId
	 * @return
	 * @throws CtdbException
	 */
	public List<AdministeredForm> getAdminFormForSubjectIntervalAndStudy(int intervalId, int subjectId, int studyId)
			throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			List<AdministeredForm> aFormList =
					dao.getAdminFormForSubjectIntervalAndStudy(intervalId, subjectId, studyId);
			conn.commit();
			return aFormList;
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException("Unable to get active form group list" + e.getMessage(), e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}



	public List<FormInterval> getActiveFormsListInInterval(int intervalId) throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			List<FormInterval> intList = dao.getActiveFormsListInInterval(intervalId);
			conn.commit();
			return intList;
		} catch (Exception e) {
			this.rollback(conn);
			throw new CtdbException("Unable to get active form group list" + e.getMessage(), e);
		} finally {
			this.rollback(conn);
			this.close(conn);
		}

	}

	public Map<String, Map<String, List<Integer>>> getPatientIdLockedAformIdsHashMap(int protocolId)
			throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			return ResponseManagerDao.getInstance(conn).getPatientIdLockedAformIdsHashMap(protocolId);
		} finally {
			this.close(conn);
		}
	}

	/**
	 * Retrieves formid of administerd form
	 *
	 * 
	 */
	public int getFormId(int admFormId) throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			return ResponseManagerDao.getInstance(conn).getFormId(admFormId);
		} finally {
			this.close(conn);
		}
	}

	/**
	 * 
	 *
	 * 
	 */
	public int getResponseId(int admFormId, int sectionId, int questionId) throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			return ResponseManagerDao.getInstance(conn).getResponseId(admFormId, sectionId, questionId);
		} finally {
			this.close(conn);
		}
	}

	/**
	 * 
	 * @param responseId
	 * @return
	 * @throws CtdbException
	 */
	public List<String> getFinalAnswers(int responseId) throws CtdbException {
		List<String> answerList = null;
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			answerList = ResponseManagerDao.getInstance(conn).getFinalAnswers(responseId);
		} finally {
			this.close(conn);
		}

		return answerList;
	}

	public Patient getPatientForCollection(int protocolId, int patientid) throws CtdbException {
		Patient patient = null;
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			patient = ResponseManagerDao.getInstance(conn).getPatientForCollection(protocolId, patientid);

		} finally {
			this.close(conn);
		}

		return patient;
	}

	/**
	 * 
	 * Method to get site for given subject and protocol should be only set to only one
	 * 
	 * @param patientId
	 * @param protocolId
	 * @return siteId
	 * @throws CtdbException
	 */
	public Integer getSubjectSiteForStudy(Integer patientId, Integer protocolId) throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			return ResponseManagerDao.getInstance(conn).getSubjectSiteForStudy(patientId, protocolId);
		} finally {
			this.close(conn);
		}
	}

	/**
	 * Gets the list of eform ids of collections that are in-progress/completed or locked for a particular visit type
	 * and patient
	 * 
	 * 
	 * @param visitTypeid
	 * @param patientId
	 * @return
	 * @throws CtdbException
	 */
	public List<Integer> getEformIdsOfCollections(int visitTypeid, int patientId) throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			return ResponseManagerDao.getInstance(conn).getEformIdsOfCollections(visitTypeid, patientId);
		} finally {
			this.close(conn);
		}
	}

	/**
	 * Retrieves all data entries for the data entry 1 or 2. The list includes the reassign information and data entry
	 * lock information. This is for View Audit for who has the View Data Entry Audit Trail privilege.
	 *
	 * @param admFormId The administered form ID for retrieving the data entry.
	 * @param dataEntryFlag data entry 1 or 2
	 * @return List the list of data entry information including the reassign info and lock info.
	 * @throws CtdbException thrown if any errors occur
	 */
	public List<EditAnswerDisplay> getInitialDataEntries(int admFormId, AdministeredForm admForm) throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			ResponseManagerDao dao = ResponseManagerDao.getInstance(conn);
			List<EditAnswerDisplay> dataEntries = dao.getInitialDataEntries(admFormId, admForm);
			this.commit(conn);
			return dataEntries;
		} finally {
			this.rollback(conn);
			this.close(conn);
		}
	}
}

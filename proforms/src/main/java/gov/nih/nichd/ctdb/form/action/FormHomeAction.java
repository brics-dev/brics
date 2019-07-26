package gov.nih.nichd.ctdb.form.action;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonObject;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbLookup;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.navigation.LeftNavController;
import gov.nih.nichd.ctdb.common.util.Utils;
import gov.nih.nichd.ctdb.form.common.FormConstants;
import gov.nih.nichd.ctdb.form.common.FormResultControl;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.manager.FormManager;
import gov.nih.nichd.ctdb.form.tag.DateFormatDecorator;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.question.action.QuestionImageAction;
import gov.nih.nichd.ctdb.question.domain.QuestionImage;
import gov.nih.nichd.ctdb.question.manager.QuestionManager;
import gov.nih.nichd.ctdb.util.common.LookupResultControl;
import gov.nih.nichd.ctdb.util.domain.LookupType;
import gov.nih.nichd.ctdb.util.manager.LookupManager;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;

/**
 * FormHomeAction will handle the retrieval of all forms assigned to a given protocol and search for forms that meet
 * search criteria.
 *
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class FormHomeAction extends BaseAction {
	private static final long serialVersionUID = 5968807325265410066L;
	private static Logger logger = Logger.getLogger(FormHomeAction.class);

	private String name = null;
	private String status = null;
	private String issued;
	private String updatedDate = null;
	private String sortBy = FormResultControl.SORT_BY_LASTUPDATED;
	private String sortedBy = FormResultControl.SORT_BY_LASTUPDATED;
	private String sortOrder = FormResultControl.SORT_DESC;
	private String clicked = "initial";
	private String protocolName = null;
	private String numResults = null;
	private String numResultsPerPage = null;
	private String searchFormType;
	private JsonObject formListJson;

	public String execute() throws Exception {
		buildLeftNav(LeftNavController.LEFTNAV_FORM_FORMS);
		try {
			FormManager fm = new FormManager();
			session.remove(CtdbConstants.FORM_XML_IMPORT);
			session.remove(CtdbConstants.QUESTIONS_XML_IMPORT);
			int formId = request.getParameter("formId") == null ? 0 : Integer.parseInt(request.getParameter("formId"));

			String cancelFromBuildForm = request.getParameter("cancelFromBuildForm");

			if (cancelFromBuildForm == null || !cancelFromBuildForm.equals("true")) {
				this.retrieveActionMessages(FormAction.ACTION_MESSAGES_KEY);
			} else { // cancelFromBuildForm--big "Cancel" button
				session.remove(FormAction.ACTION_MESSAGES_KEY); // "FormAction_ActionMessages";
				/*
				 * "Cancel" the edited images on the form.(other edited data are not saved on db until big "Save"
				 * button.) {"2496":"None", "2497":"None", "2498":["questionimage_13.jpg", "…….", …….], ……..}
				 */
				JSONObject graphicNamesOrigMappingJSONObj =
						new JSONObject(request.getParameter("graphicNamesOrigMappingJSON"));
				JSONObject graphicNamesMappingJSONObj = new JSONObject(request.getParameter("graphicNamesMappingJSON"));

				// List<Integer> questionsids = fm.getQuestionOrderInForm(formId);
				Iterator<String> keys = graphicNamesOrigMappingJSONObj.keys();
				if (keys != null) {
					while (keys.hasNext()) {
						String questionId = (String) keys.next();
						JSONArray origMappingJSONArr = null;
						JSONArray mappingJSONArr = null;

						try { // ["questionimage_13.jpg", "…….", …….]
							origMappingJSONArr = graphicNamesOrigMappingJSONObj.getJSONArray("" + questionId);
						} catch (JSONException e) {
							origMappingJSONArr = new JSONArray();
						}

						try { // ["questionimage_13.jpg", "…….", …….]
							mappingJSONArr = graphicNamesMappingJSONObj.getJSONArray("" + questionId);
						} catch (JSONException e) {
							mappingJSONArr = new JSONArray();
						}

						if ((origMappingJSONArr == null || origMappingJSONArr.length() == 0)
								&& (mappingJSONArr == null || mappingJSONArr.length() == 0)) {
							continue;
						}

						List<String> namesToDelete = new ArrayList<String>();
						List<String> namesToDeleteTemp = new ArrayList<String>();
						String mappingStr = "";
						for (int n = 0; n < mappingJSONArr.length(); n++) {
							mappingStr = mappingJSONArr.getString(n);
							namesToDelete.add(mappingStr);
							namesToDeleteTemp.add(mappingStr);
						}

						List<String> namesToInsert = new ArrayList<String>();
						List<String> namesToInsertTemp = new ArrayList<String>();
						String origMappingStr = "";
						for (int n = 0; n < origMappingJSONArr.length(); n++) {
							origMappingStr = origMappingJSONArr.getString(n);
							namesToInsert.add(origMappingStr);
							namesToInsertTemp.add(origMappingStr);
						}

						namesToInsert.removeAll(namesToDeleteTemp);
						namesToDelete.removeAll(namesToInsertTemp);

						if ((namesToInsert != null && namesToInsert.size() != 0)
								|| (namesToDelete != null && namesToDelete.size() != 0)) {
							// questionImageAction.saveQuestionImage();
							this.saveQuestionImage("" + questionId, namesToDelete, namesToInsert);
						}
					}
				}
			}

			this.retrieveActionMessages(FormBuildAction.ACTION_MESSAGES_KEY);
			this.retrieveActionMessages(ImportXmlFormAction.ACTION_MESSAGES_KEY);
			this.retrieveActionErrors(FormAction.ACTION_ERRORS_KEY);


			@SuppressWarnings("unchecked")
			List<CtdbLookup> formStatus = (List<CtdbLookup>) session.get(FormConstants.FORMSEARCHSTATUS);

			if (formStatus == null) {
				LookupManager lookUp = new LookupManager();

				formStatus = lookUp.getLookups(LookupType.FORM_STATUS, new LookupResultControl());
				CtdbLookup all = new CtdbLookup(0, "All");	// This is for all status value.
				formStatus.add(0, all);
				session.put(FormConstants.FORMSEARCHSTATUS, formStatus);
			}

			FormResultControl frc = new FormResultControl();

			Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);

			if (p == null) {
				return StrutsConstants.SELECTPROTOCOL;
			}

			// patient and non-patient
			updateResultControl(frc);

			frc.setSortBy(getSortBy());
			frc.setSortOrder(getSortOrder());
			frc.setFormType(" 10 ");
			frc.setSortBy(FormResultControl.SORT_BY_LASTUPDATED);
			frc.setSortOrder(FormResultControl.SORT_DESC);
			request.setAttribute(FormConstants.PROTOCOLFORMS, fm.getForms(p.getId(), frc));

			frc.setFormType(FormResultControl.NonPatientFormType);
			request.setAttribute("NonPatientForms", fm.getForms(p.getId(), frc));

			frc.setFormType("all");
			request.setAttribute("AllForms", fm.getForms(p.getId(), frc));

			String formTypeToDisplay = (String) session.get("formTypeToDisplay");
			session.remove("formTypeToDisplay");

			if (Utils.isBlank(formTypeToDisplay) || formTypeToDisplay.equals("Subject")) {
				request.setAttribute("formTypeToDisplay", "Subject");
			} else {
				request.setAttribute("formTypeToDisplay", "nonSubject");
			}

			session.remove("qustionMap");
			session.remove("nowCopyright");
			session.remove("orgCopyright");
		} catch (CtdbException ce) {
			logger.error("Could not setup the \"My Forms\" page.", ce);
			return StrutsConstants.FAILURE;
		}

		return SUCCESS;
	}

	public String getFormList() {
		FormManager fm = new FormManager();
		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		FormResultControl frc = new FormResultControl();

		updateResultControl(frc);
		try {
			frc.setSortBy(getSortBy());
			frc.setSortOrder(getSortOrder());
			frc.setSortBy(FormResultControl.SORT_BY_LASTUPDATED);
			frc.setSortOrder(FormResultControl.SORT_DESC);
			frc.setFormType("all");
			IdtInterface idt = new Struts2IdtInterface();
			ArrayList<Form> forms = new ArrayList(fm.getForms(p.getId(), frc));
			idt.setList(forms);
			idt.setTotalRecordCount(forms.size());
			idt.setFilteredRecordCount(forms.size());
			// idt.decorate(new SampleIdtDecorator());
			idt.decorate(new DateFormatDecorator("updatedDate"));
			idt.output();
			// HttpServletResponse response = ServletActionContext.getResponse();
			// response.setContentType("application/json");
			// response.getWriter().write(formListJson);
		} catch (Exception e) {
			logger.error("eee");
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * "Cancel" the new created form. model="create" ActionBarView.js
	 * url=baseUrl+'/form/deleteForm.action?mode=create&id=' + formId;
	 */
	public String deleteForm() {
		Protocol p = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		String mode = request.getParameter("mode");
		String formIds = request.getParameter(CtdbConstants.ID_REQUEST_ATTR);
		String[] formIdsArr = formIds.split(",");
		String formNamesDeleted = "";
		String formNamesCantDelete = "";
		Form form = null;
		int formId = -1;

		try {
			FormManager fm = new FormManager();
			QuestionManager qm = new QuestionManager();

			for (int i = 0; i < formIdsArr.length; i++) {
				formId = Integer.parseInt(formIdsArr[i]);
				form = fm.getFormAndSetofQuestions(formId);

				if (!form.isAdministered()) {

					// need to determine if any of the questions on the form exist on other forms
					// if so...we will not delete this form...because deleting forms now also includes
					// deleting questions
					List<Integer> questionsids = fm.getQuestionOrderInForm(formId);
					boolean isUsedInAnotherForm = false;
					Iterator iter = questionsids.iterator();
					while (iter.hasNext()) {
						int questionId = (Integer) iter.next();
						isUsedInAnotherForm = qm.isAttachedOnAnotherForm(questionId, formId);
						if (isUsedInAnotherForm) {
							break;
						}
					}

					if (isUsedInAnotherForm) {
						formNamesCantDelete = formNamesCantDelete + form.getName() + ", ";
					} else {
						formNamesDeleted = formNamesDeleted + form.getName() + ", ";

						if (form.getFormType() == 10) {
							session.put("formTypeToDisplay", "Subject");
						} else {
							session.put("formTypeToDisplay", "nonSubject");
						}

						fm.deleteForm(formId, form.isCopyRight());
						List<Form> forms = fm.getActiveForms(p.getId());
						p.setForms(forms);

						// delete all images under the new created form.
						if ("create".equalsIgnoreCase(mode)) {
							// List<Integer> questionsids = fm.getQuestionOrderInForm(formId);
							iter = questionsids.iterator();
							while (iter.hasNext()) {
								int questionId = (Integer) iter.next();
								// deleteImagesURL=baseUrl+'/question/deleteQuestionImage.action?qId='+qId+'&namesToDelete='+namesToDelete;
								this.deleteQuestionImage(questionId + "", CtdbConstants.DELETE_ALL_IMAGES);
								// questionImageAction.deleteQuestionImage();
							}
						}
					}

				}
			}

			// String mode = request.getParameter("mode");
			if (mode == null) {
				formNamesDeleted = formNamesDeleted.trim();
				formNamesCantDelete = formNamesCantDelete.trim();
				if (!formNamesDeleted.equals("")) {
					formNamesDeleted = formNamesDeleted.substring(0, formNamesDeleted.length() - 1);
				}
				if (!formNamesCantDelete.equals("")) {
					formNamesCantDelete = formNamesCantDelete.substring(0, formNamesCantDelete.length() - 1);
				}

				if (formIdsArr.length == 1) {
					if (!formNamesDeleted.equals("")) {
						addActionMessage(getText(StrutsConstants.SUCCESS_DELETE_KEY,
								new String[] {"form  " + formNamesDeleted}));
					} else {
						addActionError(getText(StrutsConstants.ERROR_DELETE_QUESTIONATTACHEDONOTHERFORMS,
								new String[] {"form " + formNamesCantDelete}));
					}
				} else {
					if (!formNamesDeleted.equals("")) {
						addActionMessage(getText(StrutsConstants.SUCCESS_DELETE_MULTI_KEY,
								new String[] {"The form(s)  " + formNamesDeleted}));
					}
					if (!formNamesCantDelete.equals("")) {
						addActionError(getText(StrutsConstants.ERROR_DELETE_QUESTIONATTACHEDONOTHERFORMS,
								new String[] {"form(s) " + formNamesCantDelete}));
					}
				}

				session.put(FormAction.ACTION_MESSAGES_KEY, getActionMessages());
			}
		} catch (NumberFormatException nfe) {
			logger.error("Invalid form ID given in parameter: " + formIds, nfe);
			addActionError(getText("errors.form.delete.invalidParam"));
		} catch (CtdbException ce) {
			logger.error("Database error occurred while deleting form " + formId + ".", ce);

			if (form != null) {
				addActionError(getText(StrutsConstants.ERROR_DELETE, new String[] {form.getName() + " "
						+ getText("form.forms.formInfo.FormDisplay").toLowerCase(request.getLocale())}));
			} else {
				addActionError(getText(StrutsConstants.ERROR_DELETE,
						new String[] {getText("form.forms.formInfo.FormDisplay").toLowerCase(request.getLocale())}));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Check for action errors
		if (hasActionErrors()) {
			session.put(FormAction.ACTION_ERRORS_KEY, getActionErrors());
		}

		return SUCCESS;
	}

	// helper method. happen on big "Cancel" the new created form.
	private String deleteQuestionImage(String questionId, String namesToDelete) throws Exception {
		try {
			QuestionManager qm = new QuestionManager();

			QuestionImage questionImage = new QuestionImage();
			questionImage.setId(Integer.parseInt(questionId));
			questionImage.setNamesToDelete(Arrays.asList(namesToDelete));
			String questionImagePrefix = getText("app.questionimageprefix");
			qm.deleteQuestionImagesOnDB(questionImage, questionImagePrefix);

			List<String> names = qm.getQuestionImages(questionImage.getId());
			session.put("questionImageNames", names);
		} catch (CtdbException ce) {
			return StrutsConstants.FAILURE;
		}

		session.put(QuestionImageAction.ACTION_MESSAGES_KEY, getActionMessages());
		return SUCCESS;
	}

	// helper method on big "Cancel"
	private String saveQuestionImage(String questionId, List<String> namesToDelete, List<String> namesToInsert)
			throws Exception {
		try {
			QuestionManager qm = new QuestionManager();

			QuestionImage questionImage = new QuestionImage();
			questionImage.setId(Integer.parseInt(questionId));
			questionImage.setNamesToDelete(namesToDelete);
			questionImage.setNames(namesToInsert);
			String questionImagePrefix = getText("app.questionimageprefix");
			qm.createQuestionImagesOnDB(questionImage, questionImagePrefix);
			qm.deleteQuestionImagesOnDB(questionImage, questionImagePrefix);

			List<String> names = qm.getQuestionImages(questionImage.getId());
			session.put("questionImageNames", names);
		} catch (CtdbException ce) {
			return StrutsConstants.FAILURE;
		}

		session.put(QuestionImageAction.ACTION_MESSAGES_KEY, getActionMessages());
		return SUCCESS;
	}


	/**
	 * Updates a FormResultControl object with the parameters specified in the searchForm
	 *
	 * @param frc The result control to update
	 */
	private void updateResultControl(FormResultControl frc) {
		if (getName() != null) {
			frc.setName(getName().trim());
		}

		if (getStatus() != null) {
			int formStatus = Integer.parseInt(getStatus());
			if (formStatus != 0) {
				CtdbLookup status = new CtdbLookup();
				status.setId(formStatus);
				frc.setStatus(status);
			}
		}

		if (getIssued() != null) {
			frc.setIsAdministed(Integer.parseInt(getIssued()));
		}

		if (getUpdatedDate() != null) {
			String dateString = getUpdatedDate().trim();

			if (dateString.length() > 0) {
				SimpleDateFormat parser = new SimpleDateFormat("yyy-MM-dd");
				Date date = parser.parse(dateString, new ParsePosition(0));
				frc.setUpdatedDate(date);
			}
		}
	}


	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getIssued() {
		return issued;
	}

	public void setIssued(String issued) {
		this.issued = issued;
	}

	public String getUpdatedDate() {
		return updatedDate;
	}

	public void setUpdatedDate(String updatedDate) {
		this.updatedDate = updatedDate;
	}

	public String getSortBy() {
		return sortBy;
	}

	public void setSortBy(String sortBy) {
		this.sortBy = sortBy;
	}

	public String getSortedBy() {
		return sortedBy;
	}

	public void setSortedBy(String sortedBy) {
		this.sortedBy = sortedBy;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}

	public String getClicked() {
		return clicked;
	}

	public void setClicked(String clicked) {
		this.clicked = clicked;
	}

	public String getProtocolName() {
		return protocolName;
	}

	public void setProtocolName(String protocolName) {
		this.protocolName = protocolName;
	}

	public String getNumResults() {
		return numResults;
	}

	public void setNumResults(String numResults) {
		this.numResults = numResults;
	}

	public String getNumResultsPerPage() {
		return numResultsPerPage;
	}

	public void setNumResultsPerPage(String numResultsPerPage) {
		this.numResultsPerPage = numResultsPerPage;
	}

	public String getSearchFormType() {
		return searchFormType;
	}

	public void setSearchFormType(String searchFormType) {
		this.searchFormType = searchFormType;
	}

	public JsonObject getFormListJson() {
		return formListJson;
	}

	public void setFormListJson(JsonObject formListJson) {
		this.formListJson = formListJson;
	}
}

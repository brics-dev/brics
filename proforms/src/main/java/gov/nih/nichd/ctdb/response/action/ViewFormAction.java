package gov.nih.nichd.ctdb.response.action;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.BaseAction;
import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.StrutsConstants;
import gov.nih.nichd.ctdb.common.TransformationException;
import gov.nih.nichd.ctdb.common.util.XslTransformer;
import gov.nih.nichd.ctdb.form.common.FormConstants;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.manager.FormManager;
import gov.nih.nichd.ctdb.form.util.FormDataStructureUtility;
import gov.nih.nichd.ctdb.form.util.ImportedRetrevialMaster;
import gov.nih.nichd.ctdb.patient.domain.Patient;
import gov.nih.nichd.ctdb.protocol.domain.Interval;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.protocol.manager.ProtocolManager;
import gov.nih.nichd.ctdb.response.common.ResponseConstants;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;
import gov.nih.nichd.ctdb.response.domain.DataEntryHeader;
import gov.nih.nichd.ctdb.response.domain.EditAssignment;
import gov.nih.nichd.ctdb.response.manager.ResponseManager;
import gov.nih.nichd.ctdb.response.util.DataCollectionUtils;
import gov.nih.nichd.ctdb.response.util.XMLManipulator;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.security.manager.SecurityManager;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

/**
 * The Struts Action class responsable for for displaying the form with answers.
 * 
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class ViewFormAction extends BaseAction {
	private static final long serialVersionUID = 1804095360305675606L;
	private static final Logger logger = Logger.getLogger(ViewFormAction.class.getName());

	String userEntryFlagStr = null;
	String action = null;

	public String execute() throws Exception {
		logger.info("ViewFormAction----------------->");
		session.remove(ResponseConstants.DATAENTRYHEADER_SESSION_KEY);
		
		String aformIdStr = null;
		User user = null;
		try {
			ResponseManager rm = new ResponseManager();
			AdministeredForm aform  = null;

			aformIdStr = request.getParameter(CtdbConstants.ID_REQUEST_ATTR); // required
			if (aformIdStr == null) {
	        	logger.error("Errror: Form Id is not defined");
	        	addActionError("Form Id is not fdefined");
				return StrutsConstants.FAILURE;
			}
			int aformId = Integer.parseInt(aformIdStr);
			
			
			if(aformId == Integer.MIN_VALUE) {
				//get aform id from session aform object
				aform = (AdministeredForm) session.get(ResponseConstants.AFORM_SESSION_KEY);
				aformId = aform.getId();	
			}

			userEntryFlagStr = request.getParameter("userEntryFlag");

			// TODO: this execute() function can be refactored into a few methods.
			if(userEntryFlagStr != null){ // action request coming from My collections-> view Entry 1/2 by an admin user.
				int userNum = Integer.parseInt(userEntryFlagStr); 
				EditAssignment ea = rm.getEditAssignment(aformId, userNum);
				SecurityManager sm = new SecurityManager();
				user = sm.getUser(ea.getCurrentBy());
			}
			else if (session.get("editUser") != null) { // action request coming from collection locking by an admin use
				int editUserNum = Integer.parseInt((String)session.get("editUser"));
				EditAssignment ea = rm.getEditAssignment(aformId, editUserNum);
				SecurityManager sm = new SecurityManager();
				user = sm.getUser(ea.getCurrentBy());
			} // reqular user to view the collection
			else{ // request from a regular user locking a data collection
				user = getUser();
			}
			FormManager fm = new FormManager();
			Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
			String shortName = fm.getEFormShortNameByAFormId(aformId);
			//if(aform == null) {
				aform = rm.getAdministeredForm(aformId, user);
			//}
			int eformid = aform.getEformid();
			FormDataStructureUtility fsUtil = new FormDataStructureUtility();
			Form form = fsUtil.getEformFromBrics(request, shortName);
			form.setId(eformid);
			form.setProtocolId(protocol.getId());
			aform.setForm(form);
			DataCollectionUtils.completeAform(aform);
					
			session.put(ResponseConstants.LOCKEDFORM_SESSION_KEY, aform);
			this.setHTML(aform, user.getId());
			this.setDataEntryHeader(aform);

		} catch (Exception ce) {
        	logger.error("Failed to retrieve the form entry for the given user.", ce);
        	addActionError(getText(StrutsConstants.ERROR_RESPONSE_LOADINGFORM));
        	return StrutsConstants.EXCEPTION;
		}

		return SUCCESS;
	}


	private void setDataEntryHeader(AdministeredForm aform) throws Exception {
		
		Protocol protocol = (Protocol) session.get(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		DataEntryHeader deh = new DataEntryHeader();
		deh.setFormDisplay(aform.getForm().getName());
		ProtocolManager pm = new ProtocolManager();
		
		if (aform.getInterval().getId() == -1) {
			deh.setIntervalDisplay("Other");
		} else {
			Interval i = pm.getInterval(aform.getInterval().getId());
			deh.setIntervalDisplay(i.getName());
		}
		
		deh.setStudyName(protocol.getName());
		deh.setStudyNum(protocol.getProtocolNumber());
		deh.setSingleDoubleKeyFlag(CtdbConstants.SINGLE_ENTRY_FORM);

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		
		if (aform.getVisitDate() != null) {
			String visitDateString = df.format(aform.getVisitDate());
			deh.setDateDisplay(visitDateString);
			String scheduledVisitDateDisplay = "";
			Date sd = aform.getScheduledVisitDate();
			if(sd != null) {
				scheduledVisitDateDisplay = df.format(sd);
			}
			deh.setScheduledVisitDateDisplay(scheduledVisitDateDisplay);
		}

		if (aform.getFinalLockDate() != null) {
			String finalLockString = df.format(aform.getFinalLockDate());
			deh.setFinalLockDate(finalLockString);
		}
		
		if (aform.getLockDate() != null) {
			String lockString = df.format(aform.getLockDate());
			deh.setLockDate(lockString);
		}

		ResponseManager rm = new ResponseManager();
		EditAssignment ea1 = rm.getEditAssignment(aform.getId(), 1);
		deh.setEntry(ea1.getCurrentByName());

		Patient p =rm.getPatientForCollection(protocol.getId(),aform.getPatient().getId());
		deh.setPatientDisplay(p.getDisplayLabel(
				protocol.getPatientDisplayType(), protocol.getId()));
		deh.setGuid(p.getGuid());
		
		session.put(ResponseConstants.AFORM_SESSION_KEY, aform);
		session.put(ResponseConstants.DATAENTRYHEADER_SESSION_KEY, deh);
	}


	/**
	 * Transforms the current administered form object into XML and tansforms
	 * into HTML using XSL
	 * 
	 * @param aform - The administered form object to transform
	 * @throws TransformationException - Thrown if any error occurs while transforming
	 * @throws CtdbException- Thrown if any other errors occur while processing
	 */
	private void setHTML(AdministeredForm aform, int dataEntryId) throws TransformationException, CtdbException {
		String formDetail = null;
		String importAnswersJs = "";

		if (aform.getForm().getImportedDate() == null) {
			String xsl;
			if (aform.getForm().isTabDisplay()) {
				xsl = SysPropUtil.getProperty("form.tab.xsl.tabAnswerdisplay");
			} else {
				if(aform.getForm().isCAT()) {
					xsl = SysPropUtil.getProperty("form.xsl.answerdisplayPROMIS");
				}else {
					xsl = SysPropUtil.getProperty("form.xsl.answerdisplay");					
				}
			}

			InputStream stream = request.getServletContext().getResourceAsStream(xsl);
			aform.setLoggedInUserId(dataEntryId);
			Document formDom = XMLManipulator.populateFormWithAnswers(aform, false, null);

			formDetail = XslTransformer.transform(formDom, stream,
					CtdbConstants.GLOBAL_XSL_PARAMETER_MAP);
		}
		else {
			formDetail = ImportedRetrevialMaster.getImportedHtml(
					request.getServletContext(), aform.getForm());
			importAnswersJs = ImportedRetrevialMaster
					.getImportedFormAnswerPopulatingJavascript(aform.getResponses());
		}

		request.setAttribute("dataEntry", "");
		
		ResponseManager rm = new ResponseManager();
		if (userEntryFlagStr != null) {
			EditAssignment ea1 = rm.getEditAssignment(aform.getId(), 1);
			String un1Str = String.valueOf(ea1.getCurrentBy());
			if (un1Str.equals(userEntryFlagStr)) {
				request.setAttribute("dataEntry", "1");
			}
			
			EditAssignment ea2 = rm.getEditAssignment(aform.getId(), 2);
			if (ea2 != null) {
				String un2Str = String.valueOf(ea2.getCurrentBy());
				if (un2Str.equals(userEntryFlagStr)) {
					request.setAttribute("dataEntry", "2");
				}
			}
		}

		request.setAttribute(FormConstants.FORMDETAIL, formDetail);
		request.setAttribute(StrutsConstants.IMPORTED_FORM_ANSWER_JS, importAnswersJs);
	}

	public String getUserIdStr() {
		return userEntryFlagStr;
	}

	public void setUserIdStr(String userIdStr) {
		this.userEntryFlagStr = userIdStr;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

}

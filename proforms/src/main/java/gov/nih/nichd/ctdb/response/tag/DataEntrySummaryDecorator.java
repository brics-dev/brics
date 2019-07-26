package gov.nih.nichd.ctdb.response.tag;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.jsp.JspException;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.Version;
import gov.nih.nichd.ctdb.common.tag.ActionDecorator;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;
import gov.nih.nichd.ctdb.security.domain.User;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

/**
 * DataEntrySummaryDecorator enables a table to have a column with Action links
 * This class works with the <code>display</code> tag library.
 * 
 * @author Booz Allen Hamilton
 * @version 1.0
 */
public class DataEntrySummaryDecorator extends ActionDecorator {

	/**
	 * Default Constructor
	 */
	public DataEntrySummaryDecorator() {
		super();
	}
	
	

	public String getFormVersion() throws JspException {
		AdministeredForm domainObject = (AdministeredForm) this.getObject();
		return new Version(domainObject.getFormVersion()).toString();
	}

	public String getPatientLabel() throws JspException {
		AdministeredForm domainObject = (AdministeredForm) this.getObject();
		Protocol proto = (Protocol) this.getPageContext().getSession()
				.getAttribute(CtdbConstants.CURRENT_PROTOCOL_SESSION_KEY);
		
		String title = domainObject.getPatient().getDisplayLabel(proto.getPatientDisplayType(), proto.getId());
		
		return title;
	}
	
	public String getGuid() throws JspException {
		AdministeredForm domainObject = (AdministeredForm) this.getObject();
		return domainObject.getPatient().getGuid();
	}
	
	public String getSubjectId() throws JspException{
		AdministeredForm domainObject = (AdministeredForm) this.getObject();
		return domainObject.getPatient().getSubjectId();
	}
	
	public String getMrn() throws JspException{
		AdministeredForm domainObject = (AdministeredForm) this.getObject();
		return domainObject.getPatient().getMrn();
	}
	
	public String getSubjectNo() throws JspException{
		AdministeredForm domainObject = (AdministeredForm) this.getObject();
		return domainObject.getPatientRecordNo();
	}
	
	public String getSubjectName() throws JspException{
		AdministeredForm domainObject = (AdministeredForm) this.getObject();
		return domainObject.getPatient().getFirstName()+" "+domainObject.getPatient().getLastName();
	}


	/**
	 * Retrieves the admined form's discrepancy flag and privileges. If flag is
	 * true, return String "resolve" otherwise return an null
	 * 
	 * @return HTML string displaying the resolve link on a row.
	 */
	public String getResolve() throws JspException {
		AdministeredForm adminedForm = (AdministeredForm) this.getObject();
		return getResolveLink(adminedForm);

	}

	private String getResolveLink(AdministeredForm adminedForm)
			throws JspException {
		String resolve = "";

		if (adminedForm.getDiscrepancyFlag()) {
			boolean flag2 = this.checkPrivilege("sysadmin")
					|| this.checkPrivilege("doublekeyresolution")
					|| this.checkPrivilege("dataentryoversight");

			boolean singleKey = (adminedForm.getForm().getSingleDoubleKeyFlag() == 1);
			if (flag2 && !singleKey) {
				String root = this.getWebRoot();
				return "<a href=\""
						+ root
						+ "/response/resolveDiscrepancies.do?action=add_form&id="
						+ adminedForm.getId() + "\">resolve</a>";
			} else
				return "";
		} else {
			return resolve;
		}
	}


	
	
	public String getStatus1() throws JspException {
		AdministeredForm adminedForm = (AdministeredForm) this.getObject();
		return adminedForm.getEntryOneStatus();
	}
	
	public String getStatus2() throws JspException {
		AdministeredForm adminedForm = (AdministeredForm) this.getObject();
		String status = "";
		if(adminedForm.getKeySingleDouble() == 1) {
			status = "-";
		}else {
			status = adminedForm.getEntryTwoStatus();
			
			if (status == null || status.equals("")) {
				status = "Not Started";
			}
			
		}
		
		
		return status;
	}
	
	public String getFinalLockDate() throws JspException {
		AdministeredForm adminedForm = (AdministeredForm) this.getObject();
		String finalLockDateString = "";
		
		Date finalLockDate = adminedForm.getFinalLockDate();
				
		if(finalLockDate != null) {
			SimpleDateFormat dateFormat = new SimpleDateFormat(SysPropUtil.getProperty("default.system.datetimeformat"));
			finalLockDateString = dateFormat.format(finalLockDate);
		}
		
		return finalLockDateString;
	}

	/**
	 * Returns the actions available for this row
	 * 
	 * @return The actions available
	 */
	public String getActions() throws JspException {
		AdministeredForm adminedForm = (AdministeredForm) this.getObject();
		Form form = adminedForm.getForm();

		boolean singleKey = (form.getSingleDoubleKeyFlag() == 1);

		String root = this.getWebRoot();

		// if user is not sysadmin and user is the same of one of the two
		// dataentry person
		// then the user should not be able to certify
		// get User object from session
		User user = (User) this.getPageContext().getSession()
				.getAttribute(CtdbConstants.USER_SESSION_KEY);
		boolean canCertify = this.checkPrivilege("sysadmin")
				|| (adminedForm.getLockedBy() != user.getId() && adminedForm
						.getLocked2By() != user.getId());

		String actions = "";
		if (adminedForm.getFinalLockDate() != null
				&& (this.checkPrivilege("editanswer"))
				&& !adminedForm.isQaLocked()) {
			actions = "<a href=\"" + root
					+ "/response/answersEdit.do?action=add_form&id="
					+ adminedForm.getId() + "\">edit&nbsp;answer</a>";
		}
		if (adminedForm.getFinalLockDate() != null
				&& this.checkPrivilege("dataentryoversight")
				&& !this.checkPrivilege("editanswer")
				&& !this.checkPrivilege("sysadmin")) {
			// if (!actions.equals("")) {
			// actions += "<br>";
			// }
			actions += "<a href=\""
					+ root
					+ "/response/editAdminFormMetaData.do?source=summary&action=edit_form&id="
					+ adminedForm.getId() + "\">edit</a>";
		}
		if (this.checkPrivilege("viewdataentryaudittrail")) {
			// if (!actions.equals("")) {
			// actions += "<br>";
			// }
			actions += "&nbsp;&nbsp;<a href=\"Javascript:popupWindow ('" + root
					+ "/response/viewEditedAnswer.do?id=" + adminedForm.getId()
					+ "');\">view&nbsp;audit</a>";
		}
		if ((adminedForm.getFinalLockDate() != null)
				|| (adminedForm.getFinalLockDate() == null && (adminedForm
						.getDiscrepancyFlag() && adminedForm
						.getResolveStarted()))) {
			if (this.checkPrivilege("unadministeraform"))
				actions += " <a href=\"javascript:confirmDelete ('"
						+ this.getWebRoot()
						+ "/response/dataEntryAdmin.do?action=process_delete&id="
						+ adminedForm.getId()
						+ "', 'Administered Form');\">unadminister</a> ";
		}
		if (adminedForm.getFinalLockDate() == null) {
			if (adminedForm.getCertifiedDate() != null
					&& adminedForm.getFinalLockDate() == null
					&& this.checkPrivilege("dataentryoversight")) {
				actions += "&nbsp;&nbsp;&nbsp;<a href=\"" + root
						+ "/response/finalLock.do?action=add_form&id="
						+ adminedForm.getId()
						+ "\">final&nbsp;lock</a>&nbsp;&nbsp;&nbsp;";
				if (this.checkPrivilege("unadministeraform")) {
					actions += "<br><a href=\"javascript:confirmDelete ('"
							+ this.getWebRoot()
							+ "/response/dataEntryAdmin.do?action=process_delete&id="
							+ adminedForm.getId()
							+ "', 'Administered Form');\">unadminister</a>";
				}
			} else if (singleKey
					&& adminedForm.getLockDate() != null
					&& adminedForm.getCertifiedDate() == null
					&& (this.checkPrivilege("doublekeyresolution") || this
							.checkPrivilege("dataentryoversight"))) {
				actions += "&nbsp;&nbsp;&nbsp;<a href=\"" + root
						+ "/response/finalCertification.do?action=add_form&id="
						+ adminedForm.getId()
						+ "\">certify</a>&nbsp;&nbsp;&nbsp;";
			} else if (!singleKey && adminedForm.getLockDate() != null
					&& !adminedForm.getDiscrepancyFlag()
					&& adminedForm.getCertifiedDate() == null
					&& adminedForm.getLock2Date() != null
					&& this.checkPrivilege("dataentryoversight")) {
				actions += "&nbsp;&nbsp;&nbsp;<a href=\"" + root
						+ "/response/finalCertification.do?action=add_form&id="
						+ adminedForm.getId()
						+ "\">certify</a>&nbsp;&nbsp;&nbsp;";
				if (this.checkPrivilege("unadministeraform")) {
					actions += "<br><a href=\"javascript:confirmDelete ('"
							+ this.getWebRoot()
							+ "/response/dataEntryAdmin.do?action=process_delete&id="
							+ adminedForm.getId()
							+ "', 'Administered Form');\">unadminister</a>";
				}
			} else if (this.getStatus1().indexOf("In Progress") > -1
					|| this.getStatus2().indexOf("In Progress") > -1) {
				actions += "&nbsp;&nbsp;&nbsp;<a href=\"Javascript:popupWindow ('"
						+ root
						+ "/response/viewAssignment.do?id="
						+ adminedForm.getId()
						+ "');\">view&nbsp;assignment</a>";
			} else {
				actions += "";
			}
		}
		actions += "&nbsp;&nbsp;&nbsp;" + this.getResolveLink(adminedForm);
		if (this.checkPrivilege("manageAttachments")
				&& adminedForm.getForm().isAttachFiles()) {
			actions += "<br><a href=\""
					+ root
					+ "/attachments/attachmentHome.do?hideNav=false&typeId=3&associatedId="
					+ adminedForm.getId() + "\">file&nbsp;attachments</a>";
		}
		return actions;
	}

	/**
	 * Returns current user name 1 for this row
	 * 
	 * @return The user name 1
	 */
	public String getUserName1() throws JspException {
		AdministeredForm adminedForm = (AdministeredForm) this.getObject();
		String returnValue = adminedForm.getUser1();

		

		if (returnValue == null || returnValue.equals("")) {
			returnValue = "N/A";
		}
		return returnValue;
	}
	
	
	public String getShortName() throws JspException {
		String shortName = "";
		AdministeredForm adminedForm = (AdministeredForm) this.getObject();
		shortName = adminedForm.getForm().getShortName();
		return shortName;
		
		
	}
	
	

	/**
	 * Returns current user name 2 for this row
	 * 
	 * @return The user name 2
	 */
	public String getUserName2() throws JspException {
		AdministeredForm adminedForm = (AdministeredForm) this.getObject();
		String returnValue = adminedForm.getUser2();

		if (returnValue == null || returnValue.equals("")) {
			returnValue = "--";
		}
		return returnValue;
	}
	
	
	
	/**
	 * Returns current user name 1 for this row
	 * 
	 * @return The user name 1
	 */
	public String getDataEntry1() throws JspException {
		AdministeredForm adminedForm = (AdministeredForm) this.getObject();

		
		String returnValue = adminedForm.getUser1LastNameFirstNameDisplay();

		if (returnValue == null || returnValue.equals("")) {
			returnValue = "";
		}
		return returnValue;
	}

	/**
	 * Returns current user name 2 for this row
	 * 
	 * @return The user name 2
	 */
	public String getDataEntry2() throws JspException {
		AdministeredForm adminedForm = (AdministeredForm) this.getObject();
		String returnValue = "";
		if(adminedForm.getKeySingleDouble() == 1) {
			 returnValue = "-";
		}else {
			returnValue = adminedForm.getUser2LastNameFirstNameDisplay();
			
			if (returnValue == null || returnValue.equals("")) {
				returnValue = "Not Started";
			}
			
		}
		
		

		
		return returnValue;
	}
	
	
	
	

	public String getTimePointDec() throws JspException {
		AdministeredForm adminedForm = (AdministeredForm) this.getObject();
		
		if(adminedForm.getInterval().getName()!=null && !adminedForm.getInterval().getName().isEmpty()){
			return adminedForm.getInterval().getName();
		}else{
			return "other";
		}
		
	}

	public String getClinicalTrialVisitDate() throws JspException {
		AdministeredForm adminedForm = (AdministeredForm) this.getObject();

		SimpleDateFormat df = new SimpleDateFormat(SysPropUtil.getProperty("default.system.datetimeformat"));
		if (adminedForm.getVisitDate() != null) {
			return df.format(adminedForm.getVisitDate());
		} else {
			return "N/A";
		}

	}

	/**
	 * getAdminFromId decorator returns the admin form Id to the jsp page
	 * 
	 * @return
	 * @throws JspException
	 */
	public int getAdminFromId() throws JspException {
		AdministeredForm adminedForm = (AdministeredForm) this.getObject();
		return adminedForm.getId();
	}
	
	public int getFormIdForAdminFromId() throws JspException {
		AdministeredForm adminedForm = (AdministeredForm) this.getObject();
		return adminedForm.getForm().getId();
	}

	public String getVisitDate() throws JspException {
		AdministeredForm adminedForm = (AdministeredForm) this.getObject();
		SimpleDateFormat df = new SimpleDateFormat(SysPropUtil.getProperty("default.system.datetimeformat"));
		if (adminedForm.getVisitDate() != null) {
			return df.format(adminedForm.getVisitDate());
		} else {
			return "N/A";
		}
	}

	/**
	 * getFormIdForAdminForm decorator returns the form Id for admin form Id to
	 * the jsp page
	 * 
	 * @return
	 * @throws JspException
	 */
	public String getFormNameForAdminForm() throws JspException {
		AdministeredForm adminedForm = (AdministeredForm) this.getObject();
		String name = adminedForm.getForm().getName();
		int formId = adminedForm.getForm().getId();
		String root = this.getWebRoot();
		
		String anchorTag = "<a href=\"Javascript:popupWindowWithMenu ('" + root
				+ "/form/viewFormDetail.action?source=popup&id=" + formId + "');\">"
				+ name + "</a>";

		return anchorTag;
	}
	
	
	
	public String getSingleDoubleEntryFlag() {
		AdministeredForm aForm = (AdministeredForm) this.getObject();
		
		return String.valueOf(aForm.getKeySingleDouble());
	}

	/**
	 * Get nonPatient Form Name
	 * 
	 * @return
	 * @throws JspException
	 */
	public String getName() throws JspException {
		Form formObj = (Form) this.getObject();
		return formObj.getName();
	}

	public String getDate() throws JspException {
		AdministeredForm adminedForm = (AdministeredForm) this.getObject();
		return adminedForm.getpVisitDate();
	}

	public String getAdminFormId() {
		AdministeredForm adminedForm = (AdministeredForm) this.getObject();

		int aFormId = adminedForm.getId();

		int singleDoubleEntryFlag = adminedForm.getKeySingleDouble();
		boolean dataEntry2NotStarted = adminedForm.getDataEntry2NotStarted();
		boolean dataEntry2Started = !dataEntry2NotStarted;
		Date lock2DateValue = adminedForm.getLock2Date();
		boolean lock2Date;
		if(lock2DateValue==null){
			lock2Date = false;
		}else{
			lock2Date = true;
		}
		return "<input type='checkbox' name='selectFormId' id=\"" + aFormId
				+ "\" value=\""
				+ singleDoubleEntryFlag + "_" + dataEntry2Started +"\"/>";
	}

}

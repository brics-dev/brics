package gov.nih.nichd.ctdb.emailtrigger.manager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbManager;
import gov.nih.nichd.ctdb.emailtrigger.dao.EmailTriggerDao;
import gov.nih.nichd.ctdb.emailtrigger.domain.EmailTrigger;
import gov.nih.nichd.ctdb.emailtrigger.domain.SentEmail;
import gov.nih.nichd.ctdb.question.domain.AnswerType;
import gov.nih.nichd.ctdb.question.domain.QuestionType;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;
import gov.nih.nichd.ctdb.response.domain.Response;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;
import gov.nih.tbi.commons.service.ServiceConstants;

public class EmailTriggerManager extends CtdbManager {
	private static final Logger LOGGER = Logger.getLogger(EmailTriggerManager.class);

	public void checkSendEmail(EmailTrigger et, Response r, List<String> triggeredValues, QuestionType qt,
			AnswerType at, String condStrWithVal) throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getConnection();
			conn.setAutoCommit(false);
			EmailTriggerDao dao = EmailTriggerDao.getInstance(conn);

			// send the email
			doSendEmail(et, r, triggeredValues, qt, at, condStrWithVal);
			dao.insertSentEmail(et.getId(), r.getAdministeredForm(), triggeredValues, qt, at);
			conn.commit();
		} catch (SQLException sqle) {
			throw new CtdbException("Failure deleting emailTrigger, sqle caught in manager " + sqle.getMessage(), sqle);
		} finally {
			this.close(conn);
		}
	}

	private void doSendEmail(EmailTrigger et, Response r, List<String> triggerValues, QuestionType qt, AnswerType at,
			String condStrWithVal) throws CtdbException {
		LOGGER.info("in doSendEmail");

		try {
			Properties props = System.getProperties();
			String smtpServer = SysPropUtil.getProperty("mail.smtp.host");
			props.put("mail.smtp.host", smtpServer);
			String port = SysPropUtil.getProperty("mail.smtp.port");
			props.put("mail.smtp.port", port);
			Session session = Session.getDefaultInstance(props, null);

			// Create a new message
			Message msg = new MimeMessage(session);

			// Set the FROM and TO fields
			String emailFromAddress = SysPropUtil.getProperty("mail.email.from.address");

			msg.setFrom(new InternetAddress(emailFromAddress));
			String toAddressStr = et.getToEmailAddress();
			InternetAddress[] toAddressArr = this.getAddressArray(toAddressStr);
			msg.setRecipients(Message.RecipientType.TO, toAddressArr);

			if (et.getCcEmailAddress() != null) {
				String ccAddressStr = et.getCcEmailAddress();
				InternetAddress[] ccAddressArr = this.getAddressArray(ccAddressStr);
				msg.setRecipients(Message.RecipientType.CC, ccAddressArr);
			}

			int patientDisplayType = r.getAdministeredForm().getForm().getProtocol().getPatientDisplayType();

			// Set the subject and body text
			msg.setSubject(et.getSubject());
			StringBuffer sb = new StringBuffer(50);
			sb.append(notNull(et.getBody())).append(" \n");
			sb.append(SysPropUtil.getProperty("email.trigger.text") + "\n");

			sb.append("   Form : ").append(r.getAdministeredForm().getForm().getName()).append("\n");

			if (patientDisplayType == CtdbConstants.PATIENT_DISPLAY_ID) {
				String subjId = "";
				if (r.getAdministeredForm().getPatient().getSubjectId() != null) {
					subjId = r.getAdministeredForm().getPatient().getSubjectId();
				}

				sb.append("   Subject ID: ").append(subjId).append("\n");
			} else if (patientDisplayType == CtdbConstants.PATIENT_DISPLAY_SUBJECT) {
				sb.append("   Subject Number: ").append(r.getAdministeredForm().getPatient().getSubjectNumber())
						.append("\n");
			} else {
				sb.append("   Subject GUID : ").append(r.getAdministeredForm().getPatient().getGuid()).append("\n");
			}

			if (qt == QuestionType.RADIO || qt == QuestionType.CHECKBOX || qt == QuestionType.SELECT
					|| qt == QuestionType.MULTI_SELECT) {
				sb.append("   Question : ").append(r.getQuestion().getText()).append("\n").append("   Response : ")
						.append(triggerValues.toString()).append("\n");
			} else if (qt == QuestionType.TEXTBOX && at == AnswerType.NUMERIC) {
				sb.append("   Question : ").append(r.getQuestion().getText()).append("\n")
						.append("   Response(s) meet the condition of [").append(condStrWithVal)
						.append("] and trigger the email.").append("\n");
			}

			msg.setText(sb.toString());
			msg.setSentDate(new Date());

			// Send the message
			Transport.send(msg);
		} catch (Exception e) {
			throw new CtdbException("Unable to send email for email trigger " + e.getMessage(), e);
		}
	}

	/**
	 * gets a list of sent email objects for display on administeredform audit page.
	 * 
	 * @return
	 * @throws CtdbException
	 */
	public List<SentEmail> getSentEmailAudit(AdministeredForm aform) throws CtdbException {
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection();
			return EmailTriggerDao.getInstance(conn).getSentEmailAudit(aform);
		} finally {
			this.close(conn);
		}
	}

	private final String notNull(String s) {
		if (s == null) {
			return ServiceConstants.EMPTY_STRING;
		} else {
			return s;
		}
	}

	private InternetAddress[] getAddressArray(String addresses) throws AddressException {
		InternetAddress[] addressArr = {};
		if (!addresses.trim().equals("")) {
			String[] recipientList = addresses.split("[ ;,]+");
			addressArr = new InternetAddress[recipientList.length];
			int counter = 0;

			for (String recipient : recipientList) {
				addressArr[counter] = new InternetAddress(recipient.trim());
				counter++;
			}
		}
		return addressArr;
	}
}

package gov.nih.nichd.ctdb.emailtrigger.manager;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.common.CtdbConstants;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbManager;
import gov.nih.nichd.ctdb.emailtrigger.dao.EmailTriggerDao;
import gov.nih.nichd.ctdb.emailtrigger.domain.EmailTrigger;
import gov.nih.nichd.ctdb.emailtrigger.domain.SentEmail;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;
import gov.nih.nichd.ctdb.response.domain.Response;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Mar 9, 2007
 * Time: 10:26:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class EmailTriggerManager extends CtdbManager {
	private static final Logger LOGGER = Logger.getLogger(EmailTriggerManager.class);
	
    /**
     * Creates an email trigger entry in db, the trigger parameter's id is  populated
     *
     * @param et
     * @throws CtdbException
     */
   /* public void createEmailTrigger(EmailTrigger et) throws CtdbException {

        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            conn.setAutoCommit(false);
            EmailTriggerDao.getInstance(conn).createEmailTrigger(et);
            conn.commit();
        } catch (SQLException sqle) {
            throw new CtdbException("Failure creating emailTrigger, sqle caught in manager " + sqle.getMessage(), sqle);
        } finally {
            this.close(conn);
        }
    }*/

    /**
     * Updates an existing email trigger
     *
     * @param et
     * @throws CtdbException
     */
    /*public void updateEmailTrigger(EmailTrigger et) throws CtdbException {

        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            conn.setAutoCommit(false);
            EmailTriggerDao.getInstance(conn).updateEmailTrigger(et);
            conn.commit();
        } catch (SQLException sqle) {
            throw new CtdbException("Failure updating emailTrigger, sqle caught in manager " + sqle.getMessage(), sqle);
        } finally {
            this.close(conn);
        }
    }*/

    /**
     * Gets an email trigger and returns it.
     *
     * @param id
     * @return
     * @throws CtdbException
     */
   /* public EmailTrigger getEmailTrigger(int id) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return EmailTriggerDao.getInstance(conn).getEmailTrigger(id);
        } finally {
            this.close(conn);
        }
    }*/

    /**
     * returns a
     * @param questionAttributesId
     * @return
     * @throws CtdbException
     */
    /*public List getEmailTriggerAudit(int questionAttributesId) throws CtdbException {
        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            return EmailTriggerDao.getInstance(conn).getEmailTriggerAudit(questionAttributesId);
        } finally {
            this.close(conn);
        }
    }*/

    /*public void deleteEmailTrigger(int id) throws CtdbException {

        Connection conn = null;
        try {
            conn = CtdbManager.getConnection();
            conn.setAutoCommit(false);
            EmailTriggerDao.getInstance(conn).deleteEmailTrigger(id);
            conn.commit();
        } catch (SQLException sqle) {
            throw new CtdbException("Failure deleting emailTrigger, sqle caught in manager " + sqle.getMessage(), sqle);
        } finally {
            this.close(conn);
        }
    }*/

    public void checkSendEmail(EmailTrigger et, Response r, List triggeredAnswers) throws CtdbException {
    	Connection conn = null;
    	
    	try {
    		conn = CtdbManager.getConnection();
    		conn.setAutoCommit(false);
    		EmailTriggerDao dao = EmailTriggerDao.getInstance(conn);
    		
    		// send the email
    		doSendEmail(et, r, triggeredAnswers);
    		dao.insertSentEmail(et.getId(), r.getAdministeredForm(), triggeredAnswers);
    		conn.commit();
    	}
    	catch (SQLException sqle) {
    		throw new CtdbException("Failure deleting emailTrigger, sqle caught in manager " + sqle.getMessage(), sqle);
    	}
    	finally {
    		this.close(conn);
    	}
    }


	private void doSendEmail(EmailTrigger et, Response r, List answers) throws CtdbException {
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
			String emailAddress = "";
			String globalAppName = SysPropUtil.getProperty("template.global.appName");
			String publicUrl = SysPropUtil.getProperty("brics.modules.home.url");
			
			/**
			 * FITBIR LOCAL|DEV|STAGE|UAT
			 */
			if (globalAppName.equals(CtdbConstants.GLOBAL_APPNAME_FITBIR)) {

				if ((publicUrl.contains(CtdbConstants.LOCAL_CIT_NIH_GOV))
						|| (publicUrl.equals(CtdbConstants.URL_HOST_FITBIR_DEV))
						|| (publicUrl.equals(CtdbConstants.URL_HOST_FITBIR_STAGE))
						|| (publicUrl.equals(CtdbConstants.URL_HOST_FITBIR_UAT))) {
					
					emailAddress = SysPropUtil.getProperty("admin.email.address.fitbir");

				} else { //DEMO|PROD 
					emailAddress = SysPropUtil.getProperty("admin.email.fitbir");
				}
			/**
			 * PDBP DEV|STAGE|UAT|DEMO|PROD 
			 */		
			} else if (globalAppName.equals(CtdbConstants.GLOBAL_APPNAME_PDBP)) {

				if ((publicUrl.contains(CtdbConstants.LOCAL_CIT_NIH_GOV))
						|| (publicUrl.equals(CtdbConstants.URL_HOST_PDBBP_DEV))
						|| (publicUrl.equals(CtdbConstants.URL_HOST_PDBP_STAGE))
						|| (publicUrl.equals(CtdbConstants.URL_HOST_PDBP_UAT))) {

					emailAddress = SysPropUtil.getProperty("admin.email.address.pdbp");
				
				} else {//DEMO|PROD
					emailAddress = SysPropUtil.getProperty("admin.email.pdbp");

				}
			/**
		    * CiTAR STAGE|DEMO|PROD 
			*/		
			} else if (globalAppName.equals(CtdbConstants.GLOBAL_APPNAME_CISTAR)) {
				if (publicUrl.contains(CtdbConstants.LOCAL_CIT_NIH_GOV)
						|| (publicUrl.equals(CtdbConstants.URL_HOST_CISTAR_STAGE))) {
					emailAddress = SysPropUtil.getProperty("admin.email.address.cistar");
				} else {//DEMO|PROD
					emailAddress = SysPropUtil.getProperty("admin.email.cistar");
				}
			/**
			 * CDRNS STAGE|DEMO|PROD
			 */	
			} else if (globalAppName.equals(CtdbConstants.GLOBAL_APPNAME_CDRNS)) {
				if (publicUrl.contains(CtdbConstants.LOCAL_CIT_NIH_GOV)
						|| (publicUrl.equals(CtdbConstants.URL_HOST_CDRNS_STAGE))) {
					emailAddress = SysPropUtil.getProperty("admin.email.address.cdrns");
				} else {// DEMO|PROD
					emailAddress = SysPropUtil.getProperty("admin.email.cdrns");
				}
			/**
			* NEI STAGE|DEMO|PROD 
			*/	
			} else if (globalAppName.equals(CtdbConstants.GLOBAL_APPNAME_NEI)) {
				if (publicUrl.contains(CtdbConstants.LOCAL_CIT_NIH_GOV)
						|| (publicUrl.equals(CtdbConstants.URL_HOST_NEI_STAGE))) {
					emailAddress = SysPropUtil.getProperty("admin.email.address.nei");
				} else {
					// DEMO|PROD
					emailAddress = SysPropUtil.getProperty("admin.email.nei");
				}
			/**
		    * EYEGENE DEMO|PROD
			*/
			} else if (globalAppName.equals(CtdbConstants.GLOBAL_APPNAME_EYEGENE)) {

				emailAddress = SysPropUtil.getProperty("admin.email.eyegene");

			} else {
				// DEMO|PROD
				emailAddress = "check the proforms.properties";
			}
			

			msg.setFrom(new InternetAddress(emailAddress));
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(et.getToEmailAddress(), false));
			
			if (et.getCcEmailAddress() != null) {
				msg.setRecipients(Message.RecipientType.CC,
						InternetAddress.parse(et.getCcEmailAddress(), false));
			}

			int patientDisplayType = r.getAdministeredForm().getForm().getProtocol().getPatientDisplayType();

			// Set the subject and body text
			msg.setSubject(et.getSubject());
			StringBuffer sb = new StringBuffer(50);
			sb.append(notNull(et.getBody())).append(" \n");

			if (globalAppName.equals(CtdbConstants.GLOBAL_APPNAME_PDBP) || globalAppName.equals(CtdbConstants.GLOBAL_APPNAME_FITBIR) || 
					globalAppName.equals(CtdbConstants.GLOBAL_APPNAME_NTI) ) {
				sb.append(SysPropUtil.getProperty("email.trigger.text.pdbp_fitbir") + "\n");
			}
			else {
				sb.append(SysPropUtil.getProperty("email.trigger.text.cistar") + "\n");
			}

			sb.append("   Form : ")
					.append(r.getAdministeredForm().getForm().getName())
					.append("\n");
			
			if (patientDisplayType == CtdbConstants.PATIENT_DISPLAY_ID) {
				String subjId  = "";
				if(r.getAdministeredForm().getPatient().getSubjectId() != null) {
					subjId = r.getAdministeredForm().getPatient().getSubjectId();	
				}
				
				sb.append("   Subject ID: ")
						.append(subjId).append("\n");
			}
			else if (patientDisplayType == CtdbConstants.PATIENT_DISPLAY_SUBJECT) {
				sb.append("   Subject Number: ")
						.append(r.getAdministeredForm().getPatient().getSubjectNumber()).append("\n");
			}
			else {
				sb.append("   Subject GUID : ")
						.append(r.getAdministeredForm().getPatient().getGuid())
						.append("\n");
			}
			
			sb.append("   Question : ")
					.append(r.getQuestion().getText())
					.append("\n")
					.append("   Response : ")
					.append(answers.toString())
					.append("\n");

			msg.setText(sb.toString());
			msg.setSentDate(new Date());

			// Send the message
			Transport.send(msg);
		}
		catch (Exception e) {
			throw new CtdbException("Unable to send email for email trigger " + e.getMessage(), e);
		}
	}

    /**
     * gets a list of sent email objects for display on administeredform audit page.
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


    private final String notNull (String s) {
        if (s == null) {
            return "";
        } else {
            return s;
        }
    }
}

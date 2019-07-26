package gov.nih.nichd.ctdb.emailtrigger.dao;

import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.Version;
import gov.nih.nichd.ctdb.emailtrigger.domain.EmailTrigger;
import gov.nih.nichd.ctdb.emailtrigger.domain.SentEmail;
import gov.nih.nichd.ctdb.form.domain.Form;
import gov.nih.nichd.ctdb.form.domain.Section;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: breymaim
 * Date: Mar 6, 2007
 * Time: 3:45:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class EmailTriggerDao extends CtdbDao {


	/**
	 * Private Constructor to hide the instance
	 * creation implementation of the EmailTriggerDao object
	 * in memory. This will provide a flexible architecture
	 * to use a different pattern in the future without
	 * refactoring the EmailTriggerDao.
	 */
	private EmailTriggerDao() {

	}

	/**
	 * Method to retrieve the instance of the EmailTriggerDao.
	 *
	 * @return EmailTriggerDao data object
	 */
	public static synchronized EmailTriggerDao getInstance() {
		return new EmailTriggerDao();
	}

	/**
	 * Method to retrieve the instance of the EmailTriggerDao. This method
	 * accepts a Database Connection to be used internally by the DAO. All
	 * transaction management will be handled at the BusinessManager level.
	 *
	 * @param conn Database connection to be used within this data object
	 * @return EmailTriggerDao data object
	 */
	public static synchronized EmailTriggerDao getInstance(Connection conn) {
		EmailTriggerDao dao = new EmailTriggerDao();
		dao.setConnection(conn);
		return dao;
	}

	public void createEmailTrigger(EmailTrigger et) throws CtdbException {
		PreparedStatement stmt = null;
		try {
			StringBuffer sb = new StringBuffer("insert into emailtrigger (emailtriggerid, version, toEmailAddress, ccemailaddress, subject, ");
			sb.append(" body, createdby, createddate, updatedby, updateddate ) values ( DEFAULT, 1,  ?,  ?, ?, ?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP ) ");
			stmt = this.conn.prepareStatement(sb.toString());
			//stmt.setInt(1, et.getId());
			stmt.setString(1, et.getToEmailAddress());
			stmt.setString(2, et.getCcEmailAddress());
			stmt.setString(3, et.getSubject());
			stmt.setString(4, et.getBody());
			stmt.setLong(5, Long.valueOf(et.getCreatedBy()));
			stmt.setLong(6, Long.valueOf(et.getUpdatedBy()));

			stmt.executeUpdate();
			et.setId(this.getInsertId(conn, "emailtrigger_seq"));

			for (String answer : et.getTriggerAnswers()) {
				sb = new StringBuffer("insert into emailtriggervalues (emailtriggerid, answer) values (?, ?) ");
				stmt = this.conn.prepareStatement(sb.toString());
				stmt.setLong(1, Long.valueOf(et.getId()));
				stmt.setString(2, answer);
				stmt.executeUpdate();
			}
		} catch (SQLException sqle) {
			throw new CtdbException("failure creating emailtrigger , orig msg " + sqle.getMessage(), sqle);
		} finally {
			this.close(stmt);
		}
	}

	public void updateEmailTrigger(EmailTrigger et) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sb = new StringBuffer("update emailtrigger set toEmailAddress= ?, ccemailaddress=?, subject = ?, body = ?,  ");
			sb.append(" updatedby = ?, updateddate = CURRENT_TIMESTAMP, version = ?  where emailtriggerid = ? ");
			stmt = this.conn.prepareStatement(sb.toString());
			stmt.setString(1, et.getToEmailAddress());
			stmt.setString(2, et.getCcEmailAddress());
			stmt.setString(3, et.getSubject());
			stmt.setString(4, et.getBody());
			stmt.setLong(5, Long.valueOf(et.getUpdatedBy()));
			stmt.setInt(6, et.getVersion().getVersionNumber() + 1);
			stmt.setLong(7, Long.valueOf(et.getId()));

			stmt.executeUpdate();
			stmt.close();

			sb = new StringBuffer("delete FROM emailtriggervalues where emailtriggerid = ? ");
			stmt = conn.prepareStatement(sb.toString());
			stmt.setLong(1, Long.valueOf(et.getId()));
			stmt.executeUpdate();

			for (String answer : et.getTriggerAnswers()) {
				sb = new StringBuffer("insert into emailtriggervalues (emailtriggerid, answer) values (?, ?) ");
				stmt = this.conn.prepareStatement(sb.toString());
				stmt.setLong(1, Long.valueOf(et.getId()));
				stmt.setString(2, answer);
				stmt.executeUpdate();
			}
		} catch (SQLException sqle) {
			throw new CtdbException("failure updating emailtrigger , orig msg " + sqle.getMessage(), sqle);
		} finally {
			this.close(stmt);
		}

	}

	public EmailTrigger getEmailTrigger(int id) throws CtdbException {

		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			StringBuffer sb = new StringBuffer("select * from emailTrigger et, emailtriggervalues etv ");
			sb.append(" where et.emailtriggerid = etv.emailtriggerid ");
			sb.append(" and et.emailTriggerid = ? ");
			stmt = this.conn.prepareStatement(sb.toString());
			stmt.setLong(1, Long.valueOf(id));
			rs = stmt.executeQuery();
			EmailTrigger et = null;
			for (int i = 0; rs.next(); i++) {
				if (i == 0) {
					et = rsToEmailTrigger(rs);
				}
				et.getTriggerAnswers().add(rs.getString("answer"));
			}
			return et;

		} catch (SQLException sqle) {
			throw new CtdbException("failure getting emailTrigger , orig msg " + sqle.getMessage(), sqle);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	public void deleteEmailTrigger(int id) throws CtdbException {

		PreparedStatement stmt = null;
		try {
			StringBuffer sb = new StringBuffer("delete FROM emailtriggervalues where emailtriggerid = ?");
			stmt = this.conn.prepareStatement(sb.toString());
			stmt.setLong(1, Long.valueOf(id));
			stmt.executeUpdate();
			stmt.close();

			sb = new StringBuffer("delete FROM emailtrigger where emailtriggerid = ? ");
			stmt = this.conn.prepareStatement(sb.toString());
			stmt.setLong(1, Long.valueOf(id));
			stmt.executeUpdate();
		} catch (SQLException sqle) {
			throw new CtdbException("failure deleting emailtrigger , orig msg " + sqle.getMessage(), sqle);
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * retreives the list of email trigger objects describing the ihsitory fo the trigger.
	 * @param questionAttributesId
	 * @return
	 * @throws CtdbException
	 */
	/*public List<EmailTrigger> getEmailTriggerAudit(int questionAttributesId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<EmailTrigger> al = new ArrayList<EmailTrigger>();

		try {
			StringBuffer sb = new StringBuffer("select eta.*, usr.username from emailTriggerArchive eta, usr where questionattributesid = ? and eta.updatedby = usr.usrid ");
			stmt = this.conn.prepareStatement(sb.toString());
			stmt.setLong(1, Long.valueOf(questionAttributesId));
			rs = stmt.executeQuery();

			while (rs.next()) {
				al.add(rsToEmailTriggerAudit(rs));
			}
		}
		catch (SQLException sqle) {
			throw new CtdbException("failure getting emailTriggerAudit , orig msg " + sqle.getMessage(), sqle);
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}

		return al;
	}*/

	/*private EmailTrigger rsToEmailTriggerAudit(ResultSet rs) throws SQLException {
		EmailTrigger et = rsToEmailTrigger(rs);
		et.setQuestionattributesid(rs.getInt("questionattributesid"));
		et.getTriggerAnswers().add(rs.getString("triggerValues"));
		et.setUpdatedByUsername(rs.getString("username"));
		
		return et;
	}*/

	private EmailTrigger rsToEmailTrigger(ResultSet rs) throws SQLException {
		EmailTrigger et = new SentEmail();
		et.setVersion(new Version(rs.getInt("version")));
		et.setToEmailAddress(rs.getString("toemailaddress"));
		et.setCcEmailAddress(rs.getString("ccemailaddress"));
		et.setId(rs.getInt("emailtriggerid"));
		et.setSubject(rs.getString("subject"));
		et.setBody(rs.getString("body"));
		et.setCreatedBy(rs.getInt("createdby"));
		et.setCreatedDate(rs.getDate("createddate"));
		et.setUpdatedBy(rs.getInt("updatedby"));
		et.setUpdatedDate(rs.getDate("updateddate"));

		return et;
	}


	/**
	 * Checks an email trigger / answer / administeredform to determine if the trigger has previously
	 * been triggered.
	 * @param admFormId
	 * @param emailtriggerId
	 * @param answer
	 * @return
	 * @throws CtdbException
	 */
	public boolean isEmailSent (int admFormId, int emailtriggerId, String answer) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			StringBuffer sb = new StringBuffer("select 1 from sentemail where emailtriggerid = ? and administeredformid = ? and answer = ? ");
			stmt = this.conn.prepareStatement(sb.toString());
			stmt.setLong(1, Long.valueOf(emailtriggerId));
			stmt.setLong(2, Long.valueOf(admFormId));
			stmt.setString(3, answer);
			rs = stmt.executeQuery();
			if (rs.next()) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException sqle) {
			throw new CtdbException("failure checking sent email , orig msg " + sqle.getMessage(), sqle);
		} finally {
			this.close(rs);
			this.close(stmt);
		}
	}

	/**
	 * Creates a record of an email being sent.
	 * @param aForm
	 * @param emailtriggerId
	 * @param answer
	 * @throws CtdbException
	 */
	public void createSentEmail (AdministeredForm aForm, int emailtriggerId, String answer) throws CtdbException {
		PreparedStatement stmt = null;
		try {
			StringBuffer sb = new StringBuffer("insert into sentemail (sentemailid, emailtriggerid, patientid, intervalid, eformid, visitdate, answer, datesent) ");
			sb.append(" values (DEFAULT, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP) ");
			stmt = this.conn.prepareStatement(sb.toString());
			//stmt.setInt(1, this.getSequenceValue(conn, "sentemail_seq"));
			stmt.setLong(1, Long.valueOf(emailtriggerId));
			stmt.setLong(2, Long.valueOf(aForm.getPatient().getId()));
			stmt.setLong(3, Long.valueOf(aForm.getInterval().getId()));
			stmt.setLong(4, Long.valueOf(aForm.getForm().getId()));
			stmt.setTimestamp(5, new Timestamp (aForm.getVisitDate().getTime()));
			stmt.setString(6,answer);
			stmt.executeUpdate();

		} catch (SQLException sqle) {
			throw new CtdbException("failure getting creating sent email , orig msg " + sqle.getMessage(), sqle);
		} finally {
			this.close(stmt);
		}
	}

	/**
	 * Removes answers for parameter answer if a record already exisits
	 * indicating that answer had triggered the email trigger on the same administeredofrm.
	 * @param emailTriggerId
	 * @param aForm
	 * @param answers
	 * @return
	 * @throws CtdbException
	 */
	public List<String> removePreviouslySent (int emailTriggerId, AdministeredForm aForm, List<String> answers) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<String> al = new ArrayList<String>();

		try {
			StringBuffer sb = new StringBuffer(" select 1 from sentemail where emailtriggerid = ? and patientid = ? and intervalid = ? ");
			sb.append(" and eformid = ? and visitdate = ? and answer = ? ");
			for (String answer : answers) {
				stmt = this.conn.prepareStatement(sb.toString());
				stmt.setLong(1, emailTriggerId);
				stmt.setLong(2, aForm.getPatient().getId());
				stmt.setLong(3, aForm.getInterval().getId());
				stmt.setLong(4, aForm.getForm().getId());
				stmt.setTimestamp(5, new Timestamp(aForm.getVisitDate().getTime()));
				stmt.setString(6, answer);
				rs = stmt.executeQuery();

				if (!rs.next()) {
					al.add(answer);
				}
			}
		}
		catch (SQLException sqle) {
			throw new CtdbException("Failure checkign sent email for email trigger id : " + emailTriggerId + sqle.getMessage());
		}
		finally {
			this.close(rs);
			this.close(stmt);
		}

		return al;
	}

	/**
	 *   Creates a sent email record recording an email being setn as a result of
	 * an email trigger for each answer in parameter answers.
	 * @param emailTriggerId
	 * @param aForm
	 * @param answers
	 * @throws CtdbException
	 */
	public void insertSentEmail (int emailTriggerId, AdministeredForm aForm, List<String> answers) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			StringBuffer sb = new StringBuffer("insert into sentemail (sentemailid, emailtriggerid, patientid, intervalid, eformid, visitdate, answer, datesent) ");
			sb.append(" values (DEFAULT, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP) ");
			stmt = this.conn.prepareStatement(sb.toString());
			for (String answer : answers) {
				stmt.setLong(1, Long.valueOf(emailTriggerId));
				stmt.setLong(2, Long.valueOf(aForm.getPatient().getId()));

				if (aForm.getInterval().getId() > 0) {
					stmt.setLong(3, Long.valueOf(aForm.getInterval().getId()));
				}
				else {
					stmt.setNull(3, Types.BIGINT);
				}

				stmt.setLong(4, Long.valueOf(aForm.getForm().getId()));
				stmt.setTimestamp(5, new Timestamp (aForm.getVisitDate().getTime()));
				stmt.setString(6, answer);
				stmt.addBatch();
			}

			stmt.executeUpdate();
		}
		catch (SQLException sqle) {
			throw new CtdbException("failure inserting setn email records " + sqle.getMessage(), sqle);
		}
		finally {
			this.close(stmt);
		}
	}


	/**
	 * gets emails sent for a given administeredform ,
	 * for adminsiterdform audit  log
	 * @param aform
	 * @return
	 * @throws CtdbException
	 */
	public List<SentEmail> getSentEmailAudit (AdministeredForm aform) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;
		ArrayList<SentEmail> al = new ArrayList<SentEmail>();

		try {
			if(aform.getInterval().getId() == -1) {
				StringBuffer sb = new StringBuffer(" select se.* from sentemail se where patientid = ? ");
				sb.append (" and intervalid is null and eformid = ? and visitdate = ?  order by  datesent");
				//sb.append( " and et.emailtriggerid = se.emailtriggerid ");
				stmt = this.conn.prepareStatement(sb.toString());
				stmt.setLong(1, Long.valueOf(aform.getPatient().getId()));
				stmt.setLong(2, Long.valueOf(aform.getForm().getId()));
				stmt.setTimestamp(3, new Timestamp(aform.getVisitDate().getTime()));
				rs = stmt.executeQuery();
			}else {
				StringBuffer sb = new StringBuffer(" select se.* from sentemail se where patientid = ? ");
				sb.append (" and intervalid = ? and eformid = ? and visitdate = ?  order by  datesent ");
				//sb.append( " and et.emailtriggerid = se.emailtriggerid ");
				stmt = this.conn.prepareStatement(sb.toString());
				stmt.setLong(1, Long.valueOf(aform.getPatient().getId()));
				stmt.setLong(2, Long.valueOf(aform.getInterval().getId()));
				stmt.setLong(3, Long.valueOf(aform.getForm().getId()));
				stmt.setTimestamp(4, new Timestamp(aform.getVisitDate().getTime()));
				rs = stmt.executeQuery();
				
			}
			
			
			
			

			while (rs.next()) {
				al.add (rsToSentEmail(aform, rs));
			}
		}
		catch (SQLException sqle) {
			throw new CtdbException("Failure obtaining sent email audit " + sqle.getMessage());
		}
		finally{
			this.close (rs);
			this.close (stmt);
		}

		return al;
	}

	private SentEmail rsToSentEmail (AdministeredForm aform, ResultSet rs) throws SQLException {
		EmailTrigger se = new SentEmail();
		int emailTriggerId = rs.getInt("emailtriggerid");

		//need to find the email trigger from the form object
		Form f = aform.getForm();
		EmailTrigger e = null;
		loop:		for (List<Section> sectionList : aform.getForm().getRowList()) {
						for (Section section : sectionList) {
							for (Question question : section.getQuestionList()) {
								 if ( question.getFormQuestionAttributes().getEmailTrigger().getId() == emailTriggerId) {
									 e = question.getFormQuestionAttributes().getEmailTrigger();
									 break loop;
								 }
							}
						}
					}
		
		
		
		
		
		((SentEmail)se).setVersion(e.getVersion());
		((SentEmail)se).setToEmailAddress(e.getToEmailAddress());
		((SentEmail)se).setCcEmailAddress(e.getCcEmailAddress());
		((SentEmail)se).setId(emailTriggerId);
		((SentEmail)se).setSubject(e.getSubject());
		((SentEmail)se).setBody(e.getBody());
		((SentEmail)se).setCreatedBy(e.getCreatedBy());
		((SentEmail)se).setCreatedDate(e.getCreatedDate());
		((SentEmail)se).setUpdatedBy(e.getUpdatedBy());
		((SentEmail)se).setUpdatedDate(e.getUpdatedDate());
		
		
		((SentEmail)se).setDateSent(rs.getTimestamp("datesent"));
		((SentEmail)se).setPatientId(rs.getInt ("patientid"));
		((SentEmail)se).setIntervalId(rs.getInt ("intervalid"));
		((SentEmail)se).setFormId(rs.getInt ("eformid"));
		((SentEmail)se).setPatientId(rs.getInt ("patientid"));
		((SentEmail)se).setVisitdate(rs.getDate("visitdate"));
		((SentEmail)se).setTriggeredAnswer(rs.getString("answer"));
		return (SentEmail)se;
	}
}

package gov.nih.nichd.ctdb.response.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.postgresql.util.PSQLException;

import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.form.domain.FormQuestionAttributes;
import gov.nih.nichd.ctdb.form.domain.Section;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.question.domain.Answer;
import gov.nih.nichd.ctdb.question.domain.AnswerType;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.domain.QuestionType;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;
import gov.nih.nichd.ctdb.response.domain.Response;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

public class DataSubmissionDao extends CtdbDao {

	private static Logger logger = Logger.getLogger(DataSubmissionDao.class);

	/**
	 * Private Constructor to hide the instance creation implementation of the DataSubmissionDao object in memory. This
	 * will provide a flexible architecture to use a different pattern in the future without refactoring the
	 * DataSubmissionManager.
	 */
	private DataSubmissionDao() {

	}

	/**
	 * Method to retrieve the instance of the DataSubmissionDao.
	 * 
	 * @return DataSubmissionDao data object
	 */
	public static synchronized DataSubmissionDao getInstance() {
		return new DataSubmissionDao();
	}

	/**
	 * Method to retrieve the instance of the DataSubmissionDao. This method accepts a Database Connection to be used
	 * internally by the DAO. All transaction management will be handled at the BusinessManager level.
	 * 
	 * @param conn Database connection to be used within this data object
	 * @return DataSubmissionDao data object
	 */
	public static synchronized DataSubmissionDao getInstance(Connection conn) {
		DataSubmissionDao dao = new DataSubmissionDao();
		dao.setConnection(conn);
		return dao;
	}


	public void insertLockedDataIntoDataSubmissionTable(AdministeredForm lockedForm, Protocol formProtocol)
			throws CtdbException {

		for (Response userResponse : lockedForm.getResponses()) {
			
			//do not use the submitted answer if the question isn't mapped to a data element.
			String dataElementName = userResponse.getQuestion().getFormQuestionAttributes().getDataElementName();
			if(dataElementName == null || dataElementName.trim().equals("") || dataElementName.trim().equalsIgnoreCase("none")){
				continue;
			}
			//if the user is editing a non submitted form we will load that data for submission to mirth instead
			//of the original answers
			if (userResponse.getEditAnswers().isEmpty()) {
                Question q = userResponse.getQuestion();
                if(q.getType() == QuestionType.CHECKBOX || q.getType() == QuestionType.MULTI_SELECT  || q.getType() == QuestionType.RADIO ||  q.getType() == QuestionType.SELECT) {
                	submitFormData(userResponse.getSubmitAnswers(), lockedForm, formProtocol, userResponse);
                }else {
                    submitFormData(userResponse.getAnswers(), lockedForm, formProtocol, userResponse);
                }
                
            } else {
                if(!userResponse.getEditAnswers().get(0).equals("^^DELETED^^")) {
                	Question q = userResponse.getQuestion();
                    if(q.getType() == QuestionType.CHECKBOX || q.getType() == QuestionType.MULTI_SELECT  || q.getType() == QuestionType.RADIO ||  q.getType() == QuestionType.SELECT) {
                    	submitFormData(userResponse.getSubmitAnswers(), lockedForm, formProtocol, userResponse);
                    }else {
                        submitFormData(userResponse.getEditAnswers(), lockedForm, formProtocol, userResponse);
                    }
                }
            }
		}
	}

	private void submitFormData(List<String> answers, AdministeredForm lockedForm, Protocol formProtocol, Response userResponse) throws CtdbException {
		PreparedStatement stmt = null;
		try {
			for (String userAnswer : answers) {
				StringBuffer sql = new StringBuffer(200);

				sql.append("INSERT INTO datasubmission(id, brics_studyid, administeredformid, ");
				sql.append("formid, finallockdate, data_structure_name, data_element_name, ");
				sql.append("group_name, isrepeatable, attachmentid, sectionid, ");
				sql.append("submitanswer_before_conversion, submitanswer, groupdataelement, multi_select_question) ");
				sql.append("values(DEFAULT,?,?,?,CURRENT_TIMESTAMP,?,?,?,?,?,?,?,?,?,?)");
				Section currentSection = lockedForm.getForm().getSectionMap().get(userResponse.getQuestion().getSectionId());
				String formattedQuestionId = "S_" + Integer.toString(currentSection.getId()) + "_Q_" + Integer.toString(userResponse.getQuestion().getId());
				Question currentQuestion = lockedForm.getForm().getQuestionMap().get(formattedQuestionId);

				stmt = conn.prepareStatement(sql.toString());
				stmt.setString(1, formProtocol.getBricsStudyId());
				stmt.setLong(2, lockedForm.getId());
				stmt.setLong(3, lockedForm.getForm().getId());
				stmt.setString(4, lockedForm.getForm().getDataStructureName());
				stmt.setString(5, currentQuestion.getFormQuestionAttributes().getDataElementName());
				if(lockedForm.getForm().isLegacy() && lockedForm.getForm().getDataStructureName().endsWith("UPDRS")) {
					//work around for ticket#
					stmt.setString(6, currentQuestion.getFormQuestionAttributes().getRepeatableGroupName());
				}else {
					stmt.setString(6, currentSection.getRepeatableGroupName());
				}
				
				stmt.setBoolean(7, currentSection.isRepeatable());

				Integer attachmentId = null;
				if (currentQuestion.getType().equals(QuestionType.File)) {			
					if(userAnswer.contains(":")){
						String attachmentIdString = userAnswer.substring(userAnswer.indexOf(":") + 1, userAnswer.length());
						attachmentId = Integer.valueOf(attachmentIdString);
						userAnswer = userAnswer.substring(0, userAnswer.indexOf(":"));
					} else if(attachmentId == null){
						//if this is an edit the attachment id isn't included in the answer string
						//need to make a database call to determine what the id is.
						attachmentId = getAttachementIdByResponseIdAndFileName(userAnswer, userResponse.getId());
					}
					stmt.setLong(8, attachmentId);
				} else {
					stmt.setNull(8, Types.INTEGER);
				}

				stmt.setLong(9, currentSection.getId());
				stmt.setString(10, userAnswer);
				stmt.setString(11, convertAnswerToBricsValue(userAnswer, currentQuestion, attachmentId));
				if(lockedForm.getForm().isLegacy() && lockedForm.getForm().getDataStructureName().endsWith("UPDRS")) {
					//work around for ticket#
					stmt.setString(12, currentQuestion.getFormQuestionAttributes().getRepeatableGroupName() + "."
							+ currentQuestion.getFormQuestionAttributes().getDataElementName());
				}else {
					stmt.setString(12, currentSection.getRepeatableGroupName() + "."
							+ currentQuestion.getFormQuestionAttributes().getDataElementName());

				}
				
				
				if (currentQuestion.getType().equals(QuestionType.CHECKBOX) || currentQuestion.getType().equals(QuestionType.MULTI_SELECT)) {
					stmt.setBoolean(13, true);
				} else {
					stmt.setBoolean(13, false);
				}

				stmt.executeUpdate();
			}
		} catch (PSQLException e) {
			if (e.getSQLState().equals(SysPropUtil.getProperty("postgres_database.unique_constraint_sqlstate"))) {
				throw new DuplicateObjectException("A administered form already exists: " + e.getMessage(), e);
			} else {
				throw new CtdbException("Unable to submit data for repository : " + e.getMessage(), e);
			}
		} catch (SQLException e) {
			// System.out.println("unique constraint erroe");
			if (e.getErrorCode() == Integer
					.parseInt(SysPropUtil.getProperty("database.unique_constraint_error.code"))) {
				throw new DuplicateObjectException("A administered form already exists: " + e.getMessage(), e);
			} else {
				throw new CtdbException("Unable to submit data for repository : " + e.getMessage(), e);
			}
		} finally {
			this.close(stmt);
		}
	}

	public void deletePendingSubmissionFormData(AdministeredForm af) throws CtdbException {
		PreparedStatement stmt = null;

		try {
			String sql = "DELETE FROM datasubmission WHERE administeredformid = ?";
			stmt = this.conn.prepareStatement(sql);
			stmt.setLong(1, af.getId());
			stmt.executeUpdate();
		} catch (SQLException e) {
			throw new CtdbException("Unable to delete the form " + af.getId(), e);
		} finally {
			this.close(stmt);
		}
	}

	private String convertAnswerToBricsValue(String answer, Question currentQuestion, Integer attachmentId)
			throws CtdbException {
		String formattedAnswer = answer;
		FormQuestionAttributes fqa = currentQuestion.getFormQuestionAttributes();

		if (fqa.getAnswerType().equals(AnswerType.DATE)) {
			// formattedAnswer = convertDate(answer, "yyyy-MMM-dd");
		} else if (fqa.getAnswerType().equals(AnswerType.DATETIME)) {
			// formattedAnswer = convertDate(answer, "yyyy-MMM-dd HH:MM");
		} else if (currentQuestion.getType().equals(QuestionType.File)) {
			formattedAnswer = retrieveFileWebServiceString(answer, attachmentId);
		} else if(currentQuestion != null && !currentQuestion.getAnswers().isEmpty()){
			formattedAnswer = convertDisplayAnswerToBricsValue(answer,currentQuestion.getAnswers());
		}
		
		if (currentQuestion.getType().equals(QuestionType.File)) {
			formattedAnswer = retrieveFileWebServiceString(answer, attachmentId);
		}

		return formattedAnswer;
	}
	
	private String convertDisplayAnswerToBricsValue(String answer, List<Answer> questionOptions){
		
		for(Answer questionOtption : questionOptions){
			if(answer.trim().equalsIgnoreCase(questionOtption.getDisplay())){
				if(questionOtption.getCodeValue() != null && !questionOtption.getCodeValue().isEmpty()){
					return questionOtption.getCodeValue();
				} else if(questionOtption.getSubmittedValue() != null && !questionOtption.getSubmittedValue().trim().equals("")) {
					return questionOtption.getSubmittedValue();
				} else {
					return questionOtption.getDisplay();
				}
			}
		}
		
		return answer;
	}

	private String convertDate(String date, String format) {
		DateTimeZone currentTimeZone = DateTimeZone.forTimeZone(Calendar.getInstance().getTimeZone());
		DateTimeFormatter standardDateTimeParser = ISODateTimeFormat.dateTimeParser().withZone(currentTimeZone);
		DateTimeFormatter readableDateTimeFormatter = DateTimeFormat.forPattern(format);
		Date unformattedDate = new Date(standardDateTimeParser.parseMillis(date));
		return readableDateTimeFormatter.print(unformattedDate.getTime());
	}

	private String retrieveFileWebServiceString(String fileName, Integer fileId) throws CtdbException {
		String webServicePath = SysPropUtil.getProperty("app.webroot").trim() + "/ws/submission/download/";
		webServicePath = webServicePath + fileId; // file id
		webServicePath = webServicePath + "?fileName=" + fileName;  // file name
		logger.info("This is the web service path to the file " + webServicePath);
		return webServicePath;

	}

	/**
	 * Retrieves the patient response attachment id by the response id and file name
	 */
	public Integer getAttachementIdByResponseIdAndFileName(String submitanswer, int responseId) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			StringBuffer sql = new StringBuffer(50);
			sql.append("select attachmentid from patientresponse ");
			sql.append("where responseid =  ? ");
			sql.append("and submitanswer =  ? ");

			stmt = this.conn.prepareStatement(sql.toString());
			stmt.setLong(1, responseId);
			stmt.setString(2, submitanswer);

			rs = stmt.executeQuery();

			while (rs.next()) {
				return rs.getInt("attachmentid");
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to get attachmentid for patientresponse with id " + responseId
					+ " and submittedanswer " + submitanswer + " : " + e.getMessage(), e);
		} finally {
			this.close(rs);
			this.close(stmt);
		}

		return null;
	}

}

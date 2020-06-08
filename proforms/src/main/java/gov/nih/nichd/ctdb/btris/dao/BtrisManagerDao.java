package gov.nih.nichd.ctdb.btris.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.btris.domain.BtrisObject;
import gov.nih.nichd.ctdb.btris.domain.BtrisSubject;
import gov.nih.nichd.ctdb.btris.domain.ProformsSubject;
import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.domain.QuestionType;
import gov.nih.nichd.ctdb.util.common.SysPropUtil;

public class BtrisManagerDao extends CtdbDao {

	public static final List<QuestionType> PREDEFINED_QUESTION_TYPE = Collections.unmodifiableList(
			Arrays.asList(QuestionType.CHECKBOX, QuestionType.RADIO, QuestionType.SELECT, QuestionType.MULTI_SELECT));

	private static final Logger logger = Logger.getLogger(BtrisManagerDao.class);
	
	public static synchronized BtrisManagerDao getInstance(Connection conn) {
		BtrisManagerDao dao = new BtrisManagerDao();
		dao.setConnection(conn);
		return dao;
	}

	public BtrisSubject getSubjectFromBtris(ProformsSubject ps) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			String sql_get_subject = "select * from subject where MRN=? and First_name=? and Last_name=?";

			stmt = this.conn.prepareStatement(sql_get_subject);
			stmt.setString(1, ps.getMrn());
			stmt.setString(2, ps.getFirstName());
			stmt.setString(3, ps.getLastName());
			rs = stmt.executeQuery();

			if (!rs.next()) {
				throw new ObjectNotFoundException("Subject could not be found in btris.");
			}

			BtrisSubject bs = new BtrisSubject();
			bs.setFirstName(rs.getString("First_name"));
			bs.setMiddleName(rs.getString("Middle_name"));
			bs.setLastName(rs.getString("Last_name"));
			
			bs.setDob(rs.getTimestamp("Date_of_birth"));
			bs.setBirthCity(rs.getString("Birth_city"));
			bs.setBirthCountry(rs.getString("Birth_country"));
			bs.setGender(rs.getString("Gender"));
			bs.setAddressLine1(rs.getString("Address_line1"));
			bs.setAddressLine2(rs.getString("Address_line2"));
			bs.setCity(rs.getString("City"));
			bs.setZip(rs.getString("Zip"));
			bs.setCountryDivisionCode(rs.getString("Country_division_code"));
			bs.setCountryCode(rs.getString("Country_code"));			
			bs.setPhoneNumber(rs.getString("Area_code") + "-" + rs.getString("Phone_number"));

			return bs;

		} catch (SQLException e) {
			throw new CtdbException("Unable to get subject from btris ", e);
		}

	}

	public HashMap<String, BtrisObject> getBtrisDataByQuestions(String mrn, HashMap<String, Question> questionMap)
			throws CtdbException {
		HashMap<String, BtrisObject> btrisObjectMap = new HashMap<String, BtrisObject>();
		for (HashMap.Entry<String, Question> qEntry : questionMap.entrySet()) {
			String sectionQuestionId = qEntry.getKey();
			Question q = qEntry.getValue();
			BtrisObject bo = q.getBtrisObject();
			QuestionType qt = q.getType();
			
			BtrisObject rtnBO = this.getBtrisDataByBtrisObject(bo, mrn, qt);
			btrisObjectMap.put(sectionQuestionId, rtnBO);
		}
		
		return btrisObjectMap;
	}

	public BtrisObject getBtrisDataByBtrisObject(BtrisObject bo, String mrn, QuestionType questionType)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		String tableName = bo.getBtrisTable();
		BtrisObject rtnBo = new BtrisObject();


		if (tableName.equalsIgnoreCase("Subject")) {
			rtnBo = getDataFromSubjectTable(bo, mrn, questionType);
		} else if (tableName.equalsIgnoreCase("Observation_Measurable")) {
			rtnBo = getDataFromObservationMeasurableTable(bo, mrn);
		}


		return rtnBo;
	}

	private BtrisObject getDataFromSubjectTable(BtrisObject bo, String mrn, QuestionType questionType)
			throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		String tableName = bo.getBtrisTable();
		BtrisObject rtnBo = new BtrisObject(bo);
		String columnName = bo.getBtrisObservationName();
		String conceptColumnName = columnName + "_CONCEPT";
		try {
			String sql = "";
			Boolean hasConceptCode = hasConceptCode(conceptColumnName, tableName);
			if (PREDEFINED_QUESTION_TYPE.contains(questionType) && hasConceptCode) {
				sql = "select distinct rc.Preferred_name as " + columnName + " from Subject s "
						+ " inner join RED_Concepts rc on s." + conceptColumnName + " = rc.Concept_code "
						+ " where mrn = ? ;";
			} else {
				sql = "select " + columnName + " from " + tableName + " where mrn = ? ;";
			}


			stmt = this.conn.prepareStatement(sql);
			stmt.setString(1, mrn);
			
			rs = stmt.executeQuery();

			if (!rs.next()) {
				throw new ObjectNotFoundException("No data of "+ columnName +"could not be found in btris for MRN - "+mrn );
			}
			String valueText = rs.getString(columnName);
			if (valueText != null) {
				rtnBo.setBtrisValueText(rs.getString(columnName));
			} else {
				rtnBo.setBtrisValueText("");
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to get data from btris for field " + columnName + " MRN " + mrn, e);
		}

		return rtnBo;
	}

	private BtrisObject getDataFromObservationMeasurableTable(BtrisObject bo, String mrn) throws CtdbException {
		PreparedStatement stmt = null;
		ResultSet rs = null;

		String tableName = bo.getBtrisTable();
		BtrisObject rtnBo = new BtrisObject(bo);
		String observationName = bo.getBtrisObservationName().toLowerCase();
		try {

			String redCode = bo.getBtrisRedCode();
			String specimenType = bo.getBtrisSpecimenType().toLowerCase();
			String sql = "SELECT TOP 1 cm.red_lab_test_concept_code, cm.red_lab_test_concept_name, cm.btris_cluster_id, " 
				  + " cm.btris_cluster_name, cm.btris_cluster_specimen_type, om.*, s.MRN "
				  + " FROM [BTRIS].[dbo].[Cluster_Mapping] cm " 
				  + " inner join [BTRIS].[dbo].[" + tableName +"] om on om.Observation_Name_CONCEPT = cm.red_lab_test_concept_code "
				  + " inner join [BTRIS].[dbo].[Subject] s on om.Subject_GUID = s.Subject_GUID "
				  + " WHERE s.mrn = ? "
				  + " and (lower(cm.btris_cluster_name) = ? or lower(om.Observation_Name) = ?) "
				  + " and lower(cm.btris_cluster_specimen_type) = ? ";
			if (!redCode.isEmpty()) {
				sql += " and Observation_Name_CONCEPT = ? ";
			}
			sql += " order by om.Primary_Date_Time desc ";
			stmt = this.conn.prepareStatement(sql);
			stmt.setString(1, mrn);
			stmt.setString(2, observationName);
			stmt.setString(3, observationName);
			stmt.setString(4, specimenType);
			if (!redCode.isEmpty()) {
				stmt.setString(5, redCode);
			} 
			
			rs = stmt.executeQuery();
	
			if (!rs.next()) {
				rtnBo.setBtrisValueText("");
				rtnBo.setBtrisValueNumeric("");
				rtnBo.setBtrisValueNameComment("");
				rtnBo.setBtrisRange("");
				rtnBo.setBtrisUnitOfMeasure("");
				rtnBo.setBtrisPrimaryDateTime(null);
				// throw new ObjectNotFoundException("No data of "+ observationName +"could not be found in btris for
				// MRN - "+mrn );
			} else {
				String valueText = rs.getString("Observation_Value_Text");
				if (valueText == null) {
					rtnBo.setBtrisValueText("");
				} else {
					rtnBo.setBtrisValueText(valueText);
				}

				String valueNumeric = rs.getString("Observation_Value_Numeric");
				if (valueNumeric == null) {
					rtnBo.setBtrisValueNumeric("");
				} else {
					rtnBo.setBtrisValueNumeric(valueNumeric);
				}
				
				String valueNameComment = rs.getString("Observation_Value_Name");
				if (valueNameComment == null) {
					rtnBo.setBtrisValueNameComment("");
				} else {
					rtnBo.setBtrisValueNameComment(valueNameComment);
				}
				
				String valueRange = rs.getString("Range");
				if (valueRange == null) {
					rtnBo.setBtrisRange("");
				} else {
					rtnBo.setBtrisRange(valueRange);
				}
				
				String valueUnit = rs.getString("Unit_of_Measure");
				if (valueUnit == null) {
					rtnBo.setBtrisUnitOfMeasure("");
				} else {
					rtnBo.setBtrisUnitOfMeasure(valueUnit);
				}
				
				String strValueOfBtrisDate = rs.getString("Primary_Date_Time");
		        SimpleDateFormat df = new SimpleDateFormat(SysPropUtil.getProperty("default.system.datetimeformat"));
                Date valueBtrisDate = null;
				try {
					valueBtrisDate = df.parse(strValueOfBtrisDate);
					
					if (valueBtrisDate == null) {
						rtnBo.setBtrisPrimaryDateTime(null);
					} else {
						rtnBo.setBtrisPrimaryDateTime(valueBtrisDate);
					}
				}      
	    		catch (Exception e) {
	    			logger.error("Error while validating the Btris Primary Date string: " + valueBtrisDate, e);
	            }
				
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to get data from btris for field " + observationName + " MRN " + mrn, e);
		}
		return rtnBo;
	}

	private Boolean hasConceptCode(String conceptColumnName, String tableName) throws CtdbException {
		Boolean hasConceptCode = false;
		PreparedStatement stmt = null;
		ResultSet rs = null;
		try {
			String sql = "select COLUMN_NAME from INFORMATION_SCHEMA.columns where table_name = ? "
					+ " and column_name = ?;";

			stmt = this.conn.prepareStatement(sql);
			stmt.setString(1, tableName);
			stmt.setString(2, conceptColumnName);

			rs = stmt.executeQuery();

			if (rs.next()) {
				hasConceptCode = true;
			}
		} catch (SQLException e) {
			throw new CtdbException("Unable to get data for " + conceptColumnName, e);
		}
		return hasConceptCode;
	}

}

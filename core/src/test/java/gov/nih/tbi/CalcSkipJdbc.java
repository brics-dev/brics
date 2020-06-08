package gov.nih.tbi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import au.com.bytecode.opencsv.CSVWriter;

public class CalcSkipJdbc {
	// static Logger log = Logger.getLogger(CalcSkipJdbc.class);
	public static final String colDelimiter = "|";

	public static void main(String[] args) {

		// PF DB connection
		String url_pf = "jdbc:postgresql://proforms-uat-db.cit.nih.gov:5432/pdbp_data";
		Properties props_pf = new Properties();
		props_pf.setProperty("user", "proforms_app_uat");
		props_pf.setProperty("password", "abcd");
		Connection conn = null;
		PreparedStatement preparedStatement1 = null;
		PreparedStatement preparedStatement2 = null;

		ResultSet rs1 = null;
		ResultSet rs2 = null;
		String studyQuery = "select protocolid from protocol order by protocolid";
		try {
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(url_pf, props_pf);

			preparedStatement1 = conn.prepareStatement(studyQuery);
			rs1 = preparedStatement1.executeQuery();

			while (rs1.next()) {
				int protocolid = rs1.getInt("protocolid");
				String home = System.getProperty("user.home");
				String fileName = "StudyNo_" + protocolid;
				File file = new File(home + "/Downloads/" + fileName + ".csv");

				// Header line for each file
				PrintWriter pw = null;
				try {
					pw = new PrintWriter(new FileOutputStream(file, true));
					pw.println(
							"Form Name,Data Element Name (that has skiprule),Skip Rule Operator Type,Skip Rul Equals,Skip Rule Type,Skipped DE(s)");
					// pw.println(
					// "Form Name,Data Element that calculation Ruleis
					// on,Calculation Rule (DE Name),Calculation Rule (Question
					// Name)");

				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} finally {
					if (pw != null) {
						pw.close();
					}
				}

				String eFormQuery = "select name,shortname from eform where protocolid =? order by name";
				preparedStatement2 = conn.prepareStatement(eFormQuery);
				preparedStatement2.setInt(1, protocolid);
				rs2 = preparedStatement2.executeQuery();
				while (rs2.next()) {
					String formName = rs2.getString("name");
					String shortName = rs2.getString("shortname");

					skipRule(formName, shortName, file);
					/// calcRule(formName, shortName, file);

				}

			}

		} catch (SQLException e) {
			e.printStackTrace();

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static void calcRule(String formName, String shortName, File file) {
		String url_dd = "jdbc:postgresql://ibis-db-uat.cit.nih.gov:5432/dictionary_uat";
		Properties props_dd = new Properties();
		props_dd.setProperty("user", "dictionaryuat");
		props_dd.setProperty("password", "abcd");
		Connection conn = null;

		PreparedStatement preparedStatement1 = null;
		PreparedStatement preparedStatement2 = null;
		PreparedStatement preparedStatement3 = null;

		ResultSet rs1 = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;

		String calcRuleStringSQForQTextReplacement = "", calcRuleStringSQForDEReplacement = "",
				deNameThatHasCalcRule = "";
		String sqListSql = "select calculation,section_id,question_id,qa.data_element_name  de,q.name from section_question sq,question q,question_attributes qa where q.question_attribute_id=qa.id and  (calculation <> '') IS NOT FALSE and sq.question_id=q.id and section_id in  ( select id from section where eform_id = (select id from eform where SHORT_NAME =?)) order by q.name";

		try {
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(url_dd, props_dd);

			preparedStatement1 = conn.prepareStatement(sqListSql);
			preparedStatement1.setString(1, shortName);
			rs1 = preparedStatement1.executeQuery();

			while (rs1.next()) {
				String calcRuleStringSQ = rs1.getString("calculation");
				String deNameThatHasCalcQuerySQL = rs1.getString("de");
				// Assign the final replaced de name and text initial S-Q
				calcRuleStringSQForDEReplacement = calcRuleStringSQForQTextReplacement = calcRuleStringSQ;
				calcRuleStringSQForDEReplacement = calcRuleStringSQForQTextReplacement = calcRuleStringSQ;
				String[] starr = calcRuleStringSQ.split("\\[");
				Pattern p = Pattern.compile("S_(\\d+)_Q_(\\d+)");
				for (String sqStr : starr) {
					// System.out.println("Str: " + sqStr);
					Matcher m = p.matcher(sqStr);
					if (m.find()) {
						int calcRuleSecId = Integer.valueOf(m.group(1));
						int calcRuleQId = Integer.valueOf(m.group(2));

						String calcRuleQuesTextQuery = "select name from question where id=" + calcRuleQId;
						preparedStatement2 = conn.prepareStatement(calcRuleQuesTextQuery);
						rs2 = preparedStatement2.executeQuery();
						while (rs2.next()) {
							String calcRuleQuText = rs2.getString("name");
							calcRuleStringSQForQTextReplacement = calcRuleStringSQForQTextReplacement
									.replace("S_" + calcRuleSecId + "_Q_" + calcRuleQId, calcRuleQuText);
							// log.info("calcRuleStringSQForQTextReplacement:\t"+calcRuleStringSQForQTextReplacement);
						}

						String deNameQuery = "select qa.data_element_name de from question q,question_attributes qa where q.question_attribute_id=qa.id and   q.id="
								+ calcRuleQId;
						preparedStatement3 = conn.prepareStatement(deNameQuery);
						rs3 = preparedStatement3.executeQuery();
						while (rs3.next()) {
							String deName = rs3.getString("de");
							calcRuleStringSQForDEReplacement = calcRuleStringSQForDEReplacement
									.replace("S_" + calcRuleSecId + "_Q_" + calcRuleQId, deName);
						}

					}

				}
				// Write to file
				CSVWriter writer = new CSVWriter(new FileWriter(file, true), ',');

				String[] calcVal = new String[5];
				calcVal[0] = formName;
				calcVal[1] = deNameThatHasCalcQuerySQL;
				calcVal[2] = calcRuleStringSQForDEReplacement;
				calcVal[3] = calcRuleStringSQForQTextReplacement;
				writer.writeNext(calcVal);
				writer.close();

			}

		} catch (SQLException e) {
			e.printStackTrace();

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public static void skipRule(String formName, String shortName, File file) {

		String url_dd = "jdbc:postgresql://ibis-db-uat.cit.nih.gov:5432/dictionary_uat";
		Properties props_dd = new Properties();
		props_dd.setProperty("user", "dictionaryuat");
		props_dd.setProperty("password", "abcd");
		Connection conn = null;

		PreparedStatement preparedStatement = null;
		PreparedStatement preparedStatement1 = null;
		PreparedStatement preparedStatement2 = null;
		PreparedStatement preparedStatement3 = null;
		PreparedStatement preparedStatement4 = null;

		ResultSet rs = null;
		ResultSet rs1 = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		ResultSet rs4 = null;

		//String skipSQL = "select distinct(e.short_name) shortName from eform e, section s,section_question sq,question q,question_attributes qa where e.id= s.eform_id and s.id=sq.section_id and sq.question_id = q.id and qa.id = q.question_attribute_id and skip_rule_flag is true and e.short_name='FamilyHistory_4'";
		try {
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(url_dd, props_dd);

			//preparedStatement1 = conn.prepareStatement(skipSQL);
			//preparedStatement1.setString(1, shortName);
			//rs1 = preparedStatement1.executeQuery();
			String secSQL = "select * from section where eform_id = (select id from eform where SHORT_NAME = ?) order by form_row,form_col";
			preparedStatement = conn.prepareStatement(secSQL);
			preparedStatement.setString(1, shortName);
			rs = preparedStatement.executeQuery();
			while (rs.next()) {

				boolean isRep = rs.getBoolean("isrepeatable");
				Integer repSecParentId = rs.getInt("repeated_section_parent_id"); // CHECK
																					// ON
																		// THIS
				int sectionId = rs.getInt("id");
				String secName = rs.getString("name");

				if (isRep && repSecParentId != 0) {

					continue;
				} else {

					//while (rs1.next()) {
						//String shortEform = rs1.getString("shortName");
						// log.info("shortEform:\t"+shortEform);
						String sqListSQL = "select question_id,section_id from section_question where section_id = "
								+ sectionId+" order by question_order, questionorder_col";
						preparedStatement2 = conn.prepareStatement(sqListSQL);
						//preparedStatement2.setString(1, shortEform);
						rs2 = preparedStatement2.executeQuery();
						while (rs2.next()) {
							int questionId = rs2.getInt("question_id");
							
						
							// int sectionId = rs2.getInt("section_id");
							// log.info("questionId:\t"+questionId);
							// log.info("sectionId:\t"+sectionId);
							String qaListQuery = "select data_element_name,skip_rule_equals,skip_rule_type,skip_rule_operator_type  from question_attributes where id = ( select question_attribute_id from question where id ="
									+ questionId + " and skip_rule_flag is true)";

							preparedStatement3 = conn.prepareStatement(qaListQuery);
							
							rs3 = preparedStatement3.executeQuery();
							while (rs3.next()) {
								String deThatHasSkipRule = rs3.getString("data_element_name");
								//System.out.println("secId:\t"+sectionId+" qId :\t"+questionId+" secName:\t"+secName+"  repSecParentId:\t"+repSecParentId+" deThatHasSkipRule:\t"+deThatHasSkipRule);
								String skip_rule_equals = rs3.getString("skip_rule_equals");
								String skip_rule_type = rs3.getString("skip_rule_type");
								if (skip_rule_type.equalsIgnoreCase("0")) {
									// log.info("skip_rule_type:\t" +
									// "Require");
									skip_rule_type = "Require";
								}
								if (skip_rule_type.equalsIgnoreCase("1")) {
									// log.info("skip_rule_type:\t" +
									// "Disable");
									skip_rule_type = "Disable";
								}

								String skip_rule_operator_type = rs3.getString("skip_rule_operator_type");
								if (skip_rule_operator_type.equalsIgnoreCase("0")) {
									// log.info("skip_rule_operator_type:\t" +
									// "Equals");
									skip_rule_operator_type = "Equals";

								}
								if (skip_rule_operator_type.equalsIgnoreCase("1")) {
									// log.info("skip_rule_operator_type:\t" +
									// "Is Blank");
									skip_rule_operator_type = "Is Blank";
								}
								if (skip_rule_operator_type.equalsIgnoreCase("2")) {
									// log.info("skip_rule_operator_type:\t" +
									// "Has Any Value");
									skip_rule_operator_type = "Has Any Value";
								}
								if (skip_rule_operator_type.equalsIgnoreCase("3")) {
									// log.info("skip_rule_operator_type:\t" +
									// "Contains");
									skip_rule_operator_type = "Contains";
								}
								String skipQuestionsSql = "select qa.data_element_name de from question q,question_attributes qa where q.question_attribute_id=qa.id and q.id in  (select  skip_question_id from  skip_rule_question where question_id = "
										+ questionId + " and  section_id=" + sectionId + ")";
								preparedStatement4 = conn.prepareStatement(skipQuestionsSql);
								rs4 = preparedStatement4.executeQuery();
								StringBuffer skippedDEs = new StringBuffer();
								while (rs4.next()) {
									String qName = rs4.getString("de");
									skippedDEs.append(qName).append(colDelimiter);
								}

								CSVWriter writer = new CSVWriter(new FileWriter(file, true), ',');

								String[] skipVal = new String[6];
								skipVal[0] = formName;
								skipVal[1] = deThatHasSkipRule;
								skipVal[2] = skip_rule_operator_type;
								skipVal[3] = skip_rule_equals;
								skipVal[4] = skip_rule_type;
								skipVal[5] = StringUtils.substring(skippedDEs.toString(), 0, -1);
								//System.out.println(skippedDEs.toString());
								writer.writeNext(skipVal);
								writer.close();

							}

						}

					//}

				}

			}

		} catch (SQLException e) {
			e.printStackTrace();

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}

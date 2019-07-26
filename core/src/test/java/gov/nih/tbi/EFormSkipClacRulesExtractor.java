package gov.nih.tbi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import gov.nih.tbi.dictionary.model.hibernate.eform.Eform;
import gov.nih.tbi.dictionary.model.hibernate.eform.Question;
import gov.nih.tbi.dictionary.model.hibernate.eform.QuestionAttribute;
import gov.nih.tbi.dictionary.model.hibernate.eform.SectionQuestion;

/**
 * Stand alone Class to extra the PF rules like(skip,calc) making connection to
 * db
 * JDBCTempplate
 * @author khanaly
 *
 */
public class EFormSkipClacRulesExtractor {
	static Logger log = Logger.getLogger(EFormSkipClacRulesExtractor.class);
	// public static final String SHORT_NAME_IN_QUERY = "AdverseEvents_15";
	public static final String SHORT_NAME_IN_QUERY = "Demographics_1";

	public static void main(String[] args) {
		BasicConfigurator.configure();
		DriverManagerDataSource dataSource_dd = new DriverManagerDataSource();
		dataSource_dd.setDriverClassName("org.postgresql.Driver");
		// dataSource.setUrl("jdbc:postgresql://ibis-db-stage.cit.nih.gov:5432/dictionary_stage");
		// dataSource.setUsername("REPLACED");
		// dataSource.setPassword("REPLACED");
		dataSource_dd.setUrl(
				"jdbc:postgresql://ibis-db-uat.cit.nih.gov:5432/dictionary_uat");
		dataSource_dd.setUsername("REPLACED");
		dataSource_dd.setPassword("REPLACED");
		JdbcTemplate jdbcTemplate_dd = new JdbcTemplate(dataSource_dd);



		String url = "jdbc:postgresql://proforms-uat-db.cit.nih.gov:5432/pdbp_data";
		Properties props = new Properties();
		props.setProperty("user", "REPLACED");
		props.setProperty("password", "REPLACED");
		Connection conn =null;
		PreparedStatement preparedStatement1 = null;
		PreparedStatement preparedStatement2 = null;
		
		ResultSet rs1 = null;
		ResultSet rs2 = null;
		BufferedWriter bw = null;
		
		
		String studyQuery = "select protocolid from protocol";
		try {
			Class.forName("org.postgresql.Driver");
			conn = DriverManager.getConnection(url, props);

			preparedStatement1 = conn.prepareStatement(studyQuery);
			rs1 = preparedStatement1.executeQuery();
			
			while (rs1.next()) {
				int protocolid = rs1.getInt("protocolid");
				String home = System.getProperty("user.home");
				String fileName = "StudyNo_"+protocolid;
				File file = new File(home+"/Downloads/" + fileName + ".csv"); 
				
				try {
					 bw = new BufferedWriter(new FileWriter(file, true));
				 String formQuery ="select distinct data_structure_name deName from form where protocolid=? order by data_structure_name";
				 preparedStatement2 = conn.prepareStatement(formQuery);
				 preparedStatement2.setInt(1, protocolid);
				 rs2 = preparedStatement2.executeQuery();
					bw = new BufferedWriter(new FileWriter(file,true));

					//bw.write("Form Name,Data Element that calculation Rule is on,Calculation Rule (DE Name),Calculation Rule (Question Name)");
					bw.write("Form Name,Data Element Name (that has skip rule),Skip Rule Operator Type,Skiprulequals,Skip Rule Type,Skipped DE(s)");
					bw.newLine();
					bw.flush();
				 while (rs2.next()) {
					 String formName = rs2.getString("deName");
					 
					 //calcRules(jdbcTemplate_dd,formName, bw);
					 skipRules(jdbcTemplate_dd,formName,bw);
					 
					 
						//bw.write(formName);
						//bw.newLine();
					 
				 }
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				 

				

			}
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				bw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}



	}

	/**
	 * Method to get calcRule from eForm
	 * 
	 * @param jdbcTemplate
	 */
	public static void calcRules(JdbcTemplate jdbcTemplate,String proformFormName,BufferedWriter bw) {
		
		
		String calcRuleStringSQForQTextReplacement = "",
				calcRuleStringSQForDEReplacement = "",
				deNameThatHasCalcRule = "";
		String eFormsHavingCalcList = "	select distinct(e.short_name) from eform e, section s,section_question sq where e.id= s.eform_id and s.id=sq.section_id and sq.section_id=s.id and sq.calculation is not null and  e.short_name like '"+proformFormName+"%'";
		List<Eform> eFormsList = jdbcTemplate.query(eFormsHavingCalcList,
				new BeanPropertyRowMapper(Eform.class));
		for (Eform ef : eFormsList) {
			String shortName = ef.getShortName();
			log.info("shortName:\t" + shortName);
			String sqListSql = "select * from section_question where (calculation <> '') IS NOT FALSE and section_id in  ( select id from section where eform_id = (select id from eform where SHORT_NAME ='"
					+ shortName + "'))";
			List<SectionQuestion> sqList = jdbcTemplate.query(sqListSql,
					new BeanPropertyRowMapper(SectionQuestion.class));

			for (SectionQuestion sq : sqList) {
				
				String calcRuleStringSQ =  sq.getCalculation();
				String deNameThatHasCalcQuerySQL = "select q.name from section_question sq,question q where sq.question_id=q.id and sq.id="
						+ sq.getId();
				String deNameThatHasCalcQuery = (String) jdbcTemplate
						.queryForObject(deNameThatHasCalcQuerySQL,
								String.class);
				log.info("deNameThatHasCalcQuery:\t" + deNameThatHasCalcQuery);
			
				// Assign the final replaced de name and text initial S-Q
				// pattern
				// value
				calcRuleStringSQForDEReplacement = calcRuleStringSQForQTextReplacement = calcRuleStringSQ;
				String[] starr = calcRuleStringSQ.split("\\[");
				Pattern p = Pattern.compile("S_(\\d+)_Q_(\\d+)");
				for (String sqStr : starr) {
					// System.out.println("Str: " + sqStr);
					Matcher m = p.matcher(sqStr);
					if (m.find()) {
						int calcRuleSecId = Integer.valueOf(m.group(1));
						int calcRuleQId = Integer.valueOf(m.group(2));
						// log.info("calcRule secId:\t " + calcRuleSecId +
						// "calcRule
						// qId" + calcRuleQId);
						String calcRuleQuesTextQuery = "select text from question where id="
								+ calcRuleQId;
						String deNameQuery = "select name from question where id="
								+ calcRuleQId;
						String calcRuleQuText = (String) jdbcTemplate
								.queryForObject(calcRuleQuesTextQuery,
										String.class);
						String deName = (String) jdbcTemplate
								.queryForObject(deNameQuery, String.class);
						calcRuleStringSQForDEReplacement = calcRuleStringSQForDEReplacement
								.replaceAll("S_" + calcRuleSecId + "_Q_"
										+ calcRuleQId, deName);
						calcRuleStringSQForQTextReplacement = calcRuleStringSQForQTextReplacement
								.replaceAll("S_" + calcRuleSecId + "_Q_"
										+ calcRuleQId, calcRuleQuText);
					}

				}
				try {
					bw.write(shortName);
					bw.append(",");
					bw.write(deNameThatHasCalcQuery);
					bw.append(",");
					bw.write(calcRuleStringSQForDEReplacement);
					bw.append(",");
					bw.write(calcRuleStringSQForQTextReplacement);
					bw.newLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
			}

		}


	}

	/**
	 * Method to get skipRules from Eform
	 * 
	 * @param jdbcTemplate
	 */
	public static void skipRules(JdbcTemplate jdbcTemplate,String proformFormName,BufferedWriter bw) {
		FileWriter fw = null;
		
		String eFormsHavingSkipRuleList = "select distinct(e.short_name) from eform e, section s,section_question sq,question q,question_attributes qa where e.id= s.eform_id and s.id=sq.section_id and sq.question_id = q.id and qa.id = q.question_attribute_id and skip_rule_flag is true and e.short_name like '"+proformFormName+"%'";
		List<Eform> eFormsList = jdbcTemplate.query(eFormsHavingSkipRuleList, new BeanPropertyRowMapper(Eform.class));
		for (Eform ef : eFormsList) {
			log.info("eForm shortName having skip Rule---> " + ef.getShortName());
			String sqListSql = "select * from section_question where section_id in  ( select id from section where eform_id = (select id from eform where SHORT_NAME ='"
					+ ef.getShortName() + "'))";

			List<SectionQuestion> sqList = jdbcTemplate.query(sqListSql,
					new BeanPropertyRowMapper(SectionQuestion.class));

			for (SectionQuestion sq : sqList) {
				String sqQuesetionSql = "select question_id from section_question where id = " + sq.getId();
				String sqQuestionId = (String) jdbcTemplate.queryForObject(sqQuesetionSql, String.class);

				String sqSectionSql = "select section_id from section_question where id = " + sq.getId();
				String sqSectionId = (String) jdbcTemplate.queryForObject(sqSectionSql, String.class);

				String qaListQuery = "select id from question_attributes where id = ( select question_attribute_id from question where id ="
						+ sqQuestionId + " and skip_rule_flag is true)";
				List<QuestionAttribute> qaList = jdbcTemplate.query(qaListQuery,
						new BeanPropertyRowMapper(QuestionAttribute.class));
			
				
				for (QuestionAttribute qa : qaList) {
					String deSkipRuleSql = "select data_element_name from question_attributes where id= " + qa.getId();
					String deThatHasSkipRule = (String) jdbcTemplate.queryForObject(deSkipRuleSql, String.class);
					log.info("deThatHasSkipRule:\t" + deThatHasSkipRule);

					String skipEqSql = "select skip_rule_equals from question_attributes where id= " + qa.getId();
					String skip_rule_equals = (String) jdbcTemplate.queryForObject(skipEqSql, String.class);
					log.info("skip_rule_equals:\t" + skip_rule_equals);
					String skip_rule_typeSQL = "select skip_rule_type from question_attributes where id= " + qa.getId();
					String skip_rule_type = (String) jdbcTemplate.queryForObject(skip_rule_typeSQL, String.class);

					if (skip_rule_type.equalsIgnoreCase("1")) {
						log.info("skip_rule_type:\t" + "Require");
						skip_rule_type="Require";
					}
					if (skip_rule_type.equalsIgnoreCase("2")) {
						log.info("skip_rule_type:\t" + "Disable");
						skip_rule_type="Disable";
					}

					String skip_rule_operator_typeSQL = "select skip_rule_operator_type from question_attributes where id= "
							+ qa.getId();
					String skip_rule_operator_type = (String) jdbcTemplate.queryForObject(skip_rule_operator_typeSQL,
							String.class);
					if (skip_rule_operator_type.equalsIgnoreCase("0")) {
						//log.info("skip_rule_operator_type:\t" + "Equals");
						skip_rule_operator_type="Equals";

					}
					if (skip_rule_operator_type.equalsIgnoreCase("1")) {
						//log.info("skip_rule_operator_type:\t" + "Is Blank");
						skip_rule_operator_type="Is Blank";
					}
					if (skip_rule_operator_type.equalsIgnoreCase("2")) {
						//log.info("skip_rule_operator_type:\t" + "Has Any Value");
						skip_rule_operator_type="Has Any Value";
					}
					if (skip_rule_operator_type.equalsIgnoreCase("3")) {
						//log.info("skip_rule_operator_type:\t" + "Contains");
						skip_rule_operator_type="Contains";
					}
					log.info("skip_rule_operator_type:\t" +skip_rule_operator_type);
					
					String skipQuestionsSql = "select name from question where id in  (select  skip_question_id from  skip_rule_question where question_id ="+sqQuestionId+"and  section_id="+sqSectionId+")";
					// String depenedemtDE = (String)
					// jdbcTemplate.queryForObject(skipQuestionsSql,
					// String.class);
					List<Question> qList = jdbcTemplate.query(skipQuestionsSql,
							new BeanPropertyRowMapper(Question.class));
					StringBuffer skippedDEs = new StringBuffer();
					for (Question q : qList) {
						
						skippedDEs.append(q.getName()).append(':');

					}
					log.info("skippedDEs:\t" + skippedDEs);
					log.info("******Working on following eFrom*****"+ef.getShortName()+"*******************");
					try {
						bw.write(ef.getShortName());
						bw.append(",");
						bw.write(deThatHasSkipRule);
						bw.append(",");
						bw.write(skip_rule_operator_type);
						bw.append(",");
						bw.write(skip_rule_equals);
						bw.append(",");
						bw.write(skip_rule_type);
						bw.append(",");
						bw.write(skippedDEs.toString());
						bw.newLine();
						bw.flush();
						

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
				
				
				 

				
				
				

			}



		}

	}

}

package gov.nih.ninds.proforms.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
/**
 * JDBC to connect to PF db and extract business logic
 * @author khanaly
 *
 */
public class JDBCformsRules {
	
	
	public static void main(String[] args) {
		
		String url = "jdbc:postgresql://db:5432/pdbp_data";
		Properties props = new Properties();
		props.setProperty("user","abcd");
		props.setProperty("password","abcd");
		Connection conn =null;
		PreparedStatement preparedStatement1 = null;
		PreparedStatement preparedStatement2 = null;
		PreparedStatement preparedStatement3 = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		try {
			Class.forName("org.postgresql.Driver");
			 conn = DriverManager.getConnection(url, props);
			 String selectSQL = " select qa.data_element_name  de,qa.skipruleoperatortype oper,qa.skipruletype t, qa.skipruleequals eq,s.sectionid secId, qa.questionid as qId from questionattributes qa,form f, section s,sectionquestion sq where "+  
					 			" f.formid = s.formid and sq.sectionid = s.sectionid and sq.questionattributesid = qa.questionattributesid "+ 
					 			" and f.name = 'CSF Collection Data Form' and skipruleflag = 't' and f.protocolid = 8; ";
			 preparedStatement1 = conn.prepareStatement(selectSQL);
			 rs = preparedStatement1.executeQuery();
			 if (!rs.next() ) {
				    System.out.println("No skip Rule");
				    return;
				}
			while (rs.next()) {				
					int sectionId = rs.getInt("secId");
					int questionId = rs.getInt("qId");
					 String deParent = rs.getString("de");
					 System.out.println("Parent DE:\t"+deParent);
					 String operator = rs.getString("oper");
					 if(operator.equalsIgnoreCase("1")){
						 System.out.println("operator:\t"+"Equals");
					 }
					 if(operator.equalsIgnoreCase("2")){
						 System.out.println("operator:\t"+"Is Blank");
					 }
					 if(operator.equalsIgnoreCase("3")){
						 System.out.println("operator:\t"+"Has Any Value");
					 }
					 if(operator.equalsIgnoreCase("4")){
						 System.out.println("operator:\t"+"Contains");
					 }
						
					 String type = rs.getString("t");
					 if(type.equalsIgnoreCase("1")){
						 System.out.println("TYPE:\t"+"Require");
					 }
					 if(type.equalsIgnoreCase("2")){
						 System.out.println("TYPE:\t"+"Disable");
					 }
					 String equals = rs.getString("eq");
					 System.out.println("equals:\t"+equals);
					//System.out.println("sectionId:\t"+sectionId+"questionId:\t :"+ questionId);
					 String skipSelect2 ="select skipsectionid as skId,skipquestionid as sqId from skiprulequestion where sectionid = ? and questionid = ?";
					 preparedStatement2 = conn.prepareStatement(skipSelect2);
					 preparedStatement2.setInt(1, sectionId);
					 preparedStatement2.setInt(2,questionId);
					 rs2 = preparedStatement2.executeQuery();
					 while (rs2.next()) {	
						 int skipSecId = rs2.getInt("skId");
						 int skipQid = rs2.getInt("sqId");
						
						// System.out.println("skipSecId:\t"+skipSecId+"skipQid:\t :"+ skipQid);
						 String sql3 ="select distinct qa.data_element_name as dename from questionattributes qa join sectionquestion sq on "+
						 	"qa.questionattributesid = sq.questionattributesid where sq.sectionid = ? and sq.questionid = ?";
						 preparedStatement3 = conn.prepareStatement(sql3);
						 preparedStatement3.setInt(1, skipSecId);
						 preparedStatement3.setInt(2,skipQid);
						 rs3 = preparedStatement3.executeQuery();
						 while (rs3.next()) {
							 String deName = rs3.getString("dename");
							 System.out.println("\t ChildDE:\t"+deName);
						 }
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
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
	}

}

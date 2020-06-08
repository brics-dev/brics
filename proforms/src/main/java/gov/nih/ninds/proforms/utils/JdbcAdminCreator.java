package gov.nih.ninds.proforms.utils;

import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import au.com.bytecode.opencsv.CSVReader;

public class JdbcAdminCreator {

	public static void main(String[] args) {
		String url = "jdbc:postgresql://proforms-dev-db.cit.nih.gov:5432/pdbp_data";
		Properties props = new Properties();
		props.setProperty("user","proforms_app_dev");
		props.setProperty("password","abcd");
		Connection conn =null;
		PreparedStatement preparedStatement = null;
		ResultSet rs = null;
		try {
			Class.forName("org.postgresql.Driver");
			 conn = DriverManager.getConnection(url, props);
				CSVReader reader = new CSVReader(new FileReader("/Users/khanaly/Downloads/old_aid_csv.csv"));
				 String [] nextLine;
					int y=1;
				while ((nextLine = reader.readNext()) != null) {
			 String selectSQL = "select sum(answer::int) as MDSUPDRS_TotalScore from administeredform af, response r,patientresponse pr,questionattributes qa,form f,section s,sectionquestion sq where af.administeredformid ="+
					 nextLine[2]+" and af.administeredformid = r.administeredformid and af.formid= f.formid and f.formid=201 and  f.formid= s.formid and sq.sectionid=s.sectionid and sq.questionattributesid= qa.questionattributesid and qa.questionid = r.questionid and pr.responseid = r.responseid and qa.data_element_name in('MDSUPDRS_PartIScore','MDSUPDRS_PartIIScore','MDSUPDRS_PartIIIScore','MDSUPDRS_PartIVScore')"; 
			 
			 preparedStatement = conn.prepareStatement(selectSQL);
				
			 	rs = preparedStatement.executeQuery();
			 
			 	if (rs.next() ) {
				 int MDSUPDRS_TotalScore = rs.getInt("MDSUPDRS_TotalScore");
				// System.out.println(y+")AformId\t"+aid);
				 System.out.println(MDSUPDRS_TotalScore);
				y++;
				}
				}
		
					
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
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

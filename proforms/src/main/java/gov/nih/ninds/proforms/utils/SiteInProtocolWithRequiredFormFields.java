package gov.nih.ninds.proforms.utils;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import au.com.bytecode.opencsv.CSVWriter;

/**
 * Program to find the list of efroms status for all GUID for a specific study and site
 * @author khanaly
 *
 */
public class SiteInProtocolWithRequiredFormFields {

	public static void main(String[] args) {
	
		 CSVWriter csvWriter = null;
		//using custom delimiter and quote character
		Map<String, ArrayList<String>> mapGuidCollection = new HashMap<String, ArrayList<String>>();
		List<String> headerList = new ArrayList<String>();
		ArrayList<String>  dataList= null;
		//First Column is GUID
		headerList.add("GUID");
		//you will get this for protocol you want to update
		int  protocolNo = 8;
		
		// TODO Auto-generated method stub
		String url = "jdbc:postgresql://db:5432/pdbp_data";
		Properties props = new Properties();
		props.setProperty("user","proforms_app_prod");
		props.setProperty("password","");
		Connection conn =null;
		PreparedStatement preparedStatement = null;
		PreparedStatement preparedStatement2 = null;
		PreparedStatement preparedStatement3 = null;
		PreparedStatement preparedStatement4 = null;
		ResultSet rs = null;
		ResultSet rs2 = null;
		ResultSet rs3 = null;
		ResultSet rs4 = null;
		try {
			Class.forName("org.postgresql.Driver");
			 conn = DriverManager.getConnection(url, props);

			 String guidSQL = "select guid,patientid from patient where patientid in (select patientid from patientprotocol where protocolid="+protocolNo+")";
			 preparedStatement = conn.prepareStatement(guidSQL);
			 rs = preparedStatement.executeQuery();
			 int y=0;
			while(rs.next() ) {
				 String guid = rs.getString("guid");
				 dataList = new ArrayList<String>();
				 System.out.println("Working on GUID...."+y+" "+guid);
 				 int patientId = rs.getInt("patientid");
 				dataList.add(guid);

				 String intevalQuery ="select name,intervalid from interval where protocolid="+protocolNo+" order by orderval";
				 preparedStatement2 = conn.prepareStatement(intevalQuery);
				 rs2 = preparedStatement2.executeQuery();
					while(rs2.next() ) {
						int intervalId = rs2.getInt("intervalid");
						String intervalName = rs2.getString("name");
						if(y==0) {
						headerList.add(intervalName);
						}
						dataList.add("");
						 String eFormQuery ="select ef.eformid eformid,ef.name efname,fi.mandatory mandatory from form_interval fi,eform ef where ef.eformid=fi.eformid and intervalid=? order by efname";
						
						 preparedStatement3 = conn.prepareStatement(eFormQuery);
						 preparedStatement3.setInt(1, intervalId);
						 rs3 = preparedStatement3.executeQuery();
						 while(rs3.next() ) {
							 int eFormId = rs3.getInt("eformid");
							 String eformName = rs3.getString("efname");
							 boolean mandatory = rs3.getBoolean("mandatory");
							 String isRequired = "";
							
								if (mandatory) {
									isRequired = "(REQUIRED)";
								}
								if(y==0) {
							 headerList.add(eformName+isRequired);
								}
							 String aFormQyert ="select coll_status  from administeredform af,dataentrydraft ded where af.administeredformid = ded.administeredformid and eformid=? and intervalid = ? and patientid =?";
							 
							 preparedStatement4 = conn.prepareStatement(aFormQyert);
							 preparedStatement4.setInt(1, eFormId);
							 preparedStatement4.setInt(2, intervalId);
							 preparedStatement4.setInt(3, patientId);
							 rs4 = preparedStatement4.executeQuery();
							 if(rs4.next()) {
								 String coll_status = rs4.getString("coll_status");
								 dataList.add(coll_status);								
							 }else {							
								 dataList.add("NOT STARTED");
							 }
							 
							 
						 }
						 
						 mapGuidCollection.put(guid,dataList);
						 
						
					}
					
				
					
					
				
				 y++;
				
			 }
			
			
			//Write header and data list to CSV file
			
			 final String ANDREAREPRTO_ARRAY_SAMPLE = "/Users/khanaly/Downloads/guidCollectionStatusReport_Ted_Dawson_133-01.csv";
			 Writer writer;
			
			 try {
			
				 writer = Files.newBufferedWriter(Paths.get(ANDREAREPRTO_ARRAY_SAMPLE));
				 csvWriter = new CSVWriter(writer, CSVWriter.DEFAULT_SEPARATOR,  CSVWriter.NO_QUOTE_CHARACTER);
				csvWriter.writeNext(headerList.toArray(new String[headerList.size()]));
				csvWriter.flush();
				
				for (String key : mapGuidCollection.keySet()) {
					
					///Guid key and arraylist as data values fo collection
					List<String> mapDataList = mapGuidCollection.get(key);
					csvWriter.writeNext(mapDataList.toArray(new String[mapDataList.size()]));
					csvWriter.flush();
				}
				
				
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		
		
			System.out.println("Done writing report to Excel(CSV)");
			 

		
					
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
	
		}finally{
			
			try {
				csvWriter.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			try {
				conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			 

	}

	

}

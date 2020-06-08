package gov.nih.nichd.ctdb.response.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import gov.nih.nichd.ctdb.common.CtdbDao;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.response.form.AdminSearchForm;


public class AdminDataSearchDao extends CtdbDao{
	
	private static final String GUID_LIST = "guidList";
	private static final String VISIT_LIST = "visitList";
	private static final String SHORT_NAME_LIST = "shortNameList";
	private static final String STUDY_PREFIX_ID = "studyPrefixId";
	private static final String PROTOCOL_NUMBER = "protocolNumber";
	private static final String ADMIN_FORM_ID = "adminFormId";
	private static final String GUID = "guid";
	private static final String VISIT_TYPE = "visitType";
	private static final String SHORT_NAME = "shortName";

	private static final String SQL_ADMIN_DATA_SEARCH = "select prot.brics_studyid, prot.protocolnumber, a.administeredformid, p.guid, f.shortname, i.name "+
			"from administeredform a, patient p, eform f, Protocol prot, interval i "+
			"where a.patientid = p.patientid "+
			"and a.eformid = f.eformid "+
			"and a.intervalid = i.intervalid "+
			"and f.protocolid = prot.protocolid "+
			"and a.finallockdate IS NOT NULL "+
			"and p.guid = ? "+
			"and i.name = ? "+
			"and f.data_structure_name = ? ";
	
	HashMap<String,Integer> valueMap = new HashMap<String,Integer>();
	

	/**
	 * Private Constructor to hide the instance creation implementation of the AdminDataSearchDao object in memory.
	 * This will provide a flexible architecture to use a different pattern in the future without refactoring the
	 * AdminDataSearchManager.
	 */
	private AdminDataSearchDao() {
		
	}
	
	/**
	 * Method to retrieve the instance of the AdminDataSearchDao.
	 * 
	 * @return AdminDataSearchDao data object
	 */
	public static synchronized AdminDataSearchDao getInstance() {
		return new AdminDataSearchDao();
	}
	
	/**
	 * Method to retrieve the instance of the DataSubmissionDao. This method accepts a Database Connection to be used
	 * internally by the DAO. All transaction management will be handled at the BusinessManager level.
	 * 
	 * @param conn Database connection to be used within this data object
	 * @return AdminDataSearchDao data object
	 */
	public static synchronized AdminDataSearchDao getInstance(Connection conn) {
		
		AdminDataSearchDao dao = new AdminDataSearchDao();
		dao.setConnection(conn);
		return dao;
	}
	
	public List<AdminSearchForm> getFormsFromSearch(String[] guidList, String[] shortnameList, String[] visitList) throws CtdbException{
		
		valueMap.put(GUID_LIST,1);
		valueMap.put(VISIT_LIST, 2);
		valueMap.put(SHORT_NAME_LIST, 3);
		valueMap.put(STUDY_PREFIX_ID,1);
		valueMap.put(PROTOCOL_NUMBER, 2);
		valueMap.put(ADMIN_FORM_ID, 3);
		valueMap.put(GUID,4);
		valueMap.put(VISIT_TYPE, 5);
		valueMap.put(SHORT_NAME, 6);
		
		PreparedStatement stmt = null;
		ResultSet rs = null;
		List<AdminSearchForm> administeredFormList = new ArrayList<AdminSearchForm>();
		try {
			for(int index = 0; index < guidList.length; index++) {
				stmt = conn.prepareStatement(SQL_ADMIN_DATA_SEARCH);
				stmt.setString(valueMap.get(GUID_LIST), guidList[index].replace("\"", ""));
				stmt.setString(valueMap.get(VISIT_LIST), visitList[index].replace("\"", ""));
				stmt.setString(valueMap.get(SHORT_NAME_LIST), shortnameList[index].replace("\"", ""));
				rs = stmt.executeQuery();
				while ( rs.next() ){
					AdminSearchForm afl = new AdminSearchForm();				
					afl.setStudyPrefixId(rs.getString(valueMap.get(STUDY_PREFIX_ID)));
					afl.setProtocolNum(rs.getString(valueMap.get(PROTOCOL_NUMBER)));
					afl.setAdminFormId(rs.getInt(valueMap.get(ADMIN_FORM_ID)));
					afl.setGuid(rs.getString(valueMap.get(GUID)));
					afl.setShortName(rs.getString(valueMap.get(VISIT_TYPE)));
					afl.setVisitType(rs.getString(valueMap.get(SHORT_NAME)));
					administeredFormList.add(afl);
				}
			}
		} catch(SQLException e) {
			
			throw new CtdbException("Unable to get administered form : " + e.getMessage(), e);
			
		} finally {
			this.close(stmt);
			this.close(rs);
		}

		return administeredFormList;
	}
	
}
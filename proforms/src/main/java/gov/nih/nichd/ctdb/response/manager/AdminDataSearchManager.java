package gov.nih.nichd.ctdb.response.manager;

import java.sql.Connection;
import java.util.List;

import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbManager;
import gov.nih.nichd.ctdb.response.dao.AdminDataSearchDao;
import gov.nih.nichd.ctdb.response.form.AdminSearchForm;

public class AdminDataSearchManager extends CtdbManager {
	
	public List<AdminSearchForm> searchForms(String[] guidList, String[] shortnameList, String[] visitList) throws CtdbException {
		
		Connection conn = null;
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			return AdminDataSearchDao.getInstance(conn).getFormsFromSearch(guidList, shortnameList, visitList);
		} finally {
			this.close(conn);
		}
	}
}
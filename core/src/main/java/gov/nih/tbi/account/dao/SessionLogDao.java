package gov.nih.tbi.account.dao;

import java.util.Date;
import java.util.List;

import gov.nih.tbi.account.model.hibernate.SessionLog;
import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.commons.util.PaginationData;

public interface SessionLogDao extends GenericDao<SessionLog, Long> {
	public List<SessionLog> search(PaginationData pageData, String serachVal, List<String> searchColumn, String filterStatus, Date filterStartDate, Date filterEndDate, boolean export);
	public int countUnfiltered();
	public List<SessionLog> getActiveSessions(String username);
	public List<SessionLog> getAllActiveSessions();
}

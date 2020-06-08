package gov.nih.tbi.commons.service;

import java.util.List;

import gov.nih.tbi.account.model.hibernate.SessionLog;
import gov.nih.tbi.commons.util.PaginationData;

public interface SessionLogManager extends BaseManager {
	public List<SessionLog> search(PaginationData pageData, String searchVal, List<String> searchColumns, String filterStatus, String filterStartDate, String filterEndDate, boolean export);
	public int countUnfiltered();
	public List<SessionLog> getActiveSessions();
	public List<SessionLog> getActiveSessions(String username);
	public void updateSession(SessionLog session);
	public void startSession(SessionLog session);
	public void updateSessions(List<SessionLog> sessions);
}

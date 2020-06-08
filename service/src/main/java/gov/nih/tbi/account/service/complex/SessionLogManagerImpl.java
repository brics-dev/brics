package gov.nih.tbi.account.service.complex;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import gov.nih.tbi.account.dao.SessionLogDao;
import gov.nih.tbi.account.model.hibernate.SessionLog;
import gov.nih.tbi.commons.service.SessionLogManager;
import gov.nih.tbi.commons.util.PaginationData;

@Service
@Scope("singleton")
public class SessionLogManagerImpl extends BaseManagerImpl implements SessionLogManager {
	
	private static final long serialVersionUID = -4609413661474976347L;
	
	@Autowired
	protected SessionLogDao sessionLogDao;
	
	/**
	 * Sets up the search query for SessionLogs. Translates String date values into
	 * Date objects based on the supplied format (yyyy-MM-dd HH:mm)
	 * 
	 * @param pageData PaginationData defining pagination and sorting
	 * @param searchVal search text entered by user
	 * @param searchColumns available search columns
	 * @param filterStatus status filter's value
	 * @param filterStartDate start date filter's value
	 * @param filterEndDate end date filter's value
	 * @return result of the search
	 */
	public List<SessionLog> search(PaginationData pageData, String searchVal, List<String> searchColumns, String filterStatus, String filterStartDate, String filterEndDate, boolean export) {
		// our dateTimePicker uses the format YYYY-MM-DD
		
		Date startDate = null;
		Date endDate = null;
		
		SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
		if (filterStartDate != null && !filterStartDate.equals("")) {
			try {
				startDate = dateformat.parse(filterStartDate);
			}
			catch(ParseException e) {
				// do nothing, keep the filter date null if there's a problem
			}
		}
		
		if (filterEndDate != null && !filterEndDate.equals("")) {
			try {
				// the end date needs its time to be set to 11:59:59 as per the comment in CRIT-10225 
				endDate = dateformat.parse(filterEndDate);
				Calendar cal = Calendar.getInstance();
				cal.setTime(endDate);
				cal.set(Calendar.HOUR_OF_DAY, cal.getMaximum(Calendar.HOUR_OF_DAY));
				cal.set(Calendar.MINUTE, cal.getMaximum(Calendar.MINUTE));
				cal.set(Calendar.SECOND, cal.getMaximum(Calendar.SECOND));
				cal.set(Calendar.MILLISECOND, cal.getMaximum(Calendar.MILLISECOND));
				endDate = cal.getTime();
			}
			catch(ParseException e) {
				// do nothing, keep the filter date null if there's a problem
			}
		}
		
		return sessionLogDao.search(pageData, searchVal, searchColumns, filterStatus, startDate, endDate, export);
	}
	
	/**
	 * Get the count of available records before any filters/searches/pagination is applied.
	 * 
	 * @return integer count of records
	 */
	public int countUnfiltered() {
		return sessionLogDao.countUnfiltered();
	}
	
	public List<SessionLog> getActiveSessions() {
		return sessionLogDao.getAllActiveSessions();
	}
	
	public List<SessionLog> getActiveSessions(String username) {
		return sessionLogDao.getActiveSessions(username);
	}
	
	public void updateSession(SessionLog session) {
		sessionLogDao.save(session);
	}
	
	public void startSession(SessionLog session) {
		sessionLogDao.save(session);
	}
	
	public void updateSessions(List<SessionLog> sessions) {
		sessionLogDao.batchSave(sessions.stream().collect(Collectors.toSet()));
	}
}

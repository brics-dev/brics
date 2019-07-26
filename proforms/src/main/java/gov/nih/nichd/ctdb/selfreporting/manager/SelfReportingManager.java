package gov.nih.nichd.ctdb.selfreporting.manager;

import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbManager;
import gov.nih.nichd.ctdb.selfreporting.dao.SelfReportingManagerDao;
import gov.nih.nichd.ctdb.selfreporting.form.SelfReportingLandingForm;
import gov.nih.nichd.ctdb.selfreporting.form.SelfReportingProperties;

public class SelfReportingManager extends CtdbManager {

	private static final Logger logger = Logger.getLogger(SelfReportingManager.class.getName());

	public List<SelfReportingLandingForm> getSelfReportingList(String token) throws CtdbException {

		logger.info("SelfReportingManager->getSelfReportingList with token " + token);

		Connection conn = null;
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			return SelfReportingManagerDao.getInstance(conn).getSelfReportingList(token);
			
		} finally {
			this.close(conn);
		}
	}
	
	public SelfReportingProperties getSelfReportingDates(String token) throws CtdbException {
		
		logger.info("SelfReportingManager->getSelfReportingDates with token " + token);

		Connection conn = null;
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			return SelfReportingManagerDao.getInstance(conn).getStartEndDates(token);
			
		} finally {
			this.close(conn);
		}
	}
	
public int getProtocolId(String token) throws CtdbException {
		
		logger.info("SelfReportingManager->getSelfReportingDates with token " + token);

		Connection conn = null;
		try {
			conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
			return SelfReportingManagerDao.getInstance(conn).getProtocolId(token);
			
		} finally {
			this.close(conn);
		}
	}

/**
 * gets the scheduled visit date
 * @param token
 * @return
 * @throws CtdbException
 */
public Date getScheduledVisitDate(String token) throws CtdbException {
	
	logger.info("SelfReportingManager->getSelfReportingDates with token " + token);

	Connection conn = null;
	try {
		conn = CtdbManager.getConnection(CtdbManager.AUTOCOMMIT_FALSE);
		return SelfReportingManagerDao.getInstance(conn).getScheduledVisitDate(token);
		
	} finally {
		this.close(conn);
	}
}
}

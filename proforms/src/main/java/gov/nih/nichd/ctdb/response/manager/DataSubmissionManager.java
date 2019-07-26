package gov.nih.nichd.ctdb.response.manager;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbManager;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;
import gov.nih.nichd.ctdb.response.dao.DataSubmissionDao;
import gov.nih.nichd.ctdb.response.domain.AdministeredForm;

public class DataSubmissionManager extends CtdbManager {
	Logger logger = Logger.getLogger(DataSubmissionManager.class.getName());
		
	public void submitDataForMirth(AdministeredForm lockedForm, Protocol formProtocol) throws CtdbException {
		 Connection conn = null;
	        logger.info("DataSubmissionManager->submitDataForMirth(AdministeredForm lockedForm, Protocol formProtocol)");
	        try {
	        	if(formProtocol.getBricsStudyId() != null && !formProtocol.getBricsStudyId().trim().equals("") /*&& lockedForm.getLockDate() != null*/){
		            conn = CtdbManager.getConnection();
		            conn.setAutoCommit(false);
		            DataSubmissionDao dao = DataSubmissionDao.getInstance(conn);
		            dao.insertLockedDataIntoDataSubmissionTable(lockedForm, formProtocol);
		            conn.commit();
	        	}
	        } catch (SQLException e) {
	           this.rollback(conn);
	            throw new CtdbException("Unable to submit data to data  : " + e.getMessage(), e);
	        } catch (DuplicateObjectException doe) {
	            this.rollback(conn);
	            throw doe;
	        } catch (CtdbException ce) {
	            this.rollback(conn);
	            throw ce;
	        } catch (Exception et) {
	           this.rollback(conn);
	            throw new CtdbException("Unknown error occurred while inserting to data submission table: " + et.getMessage(), et);
	        } finally {
	            this.close(conn);
	        }
	}
	
	public void deletePendingSubmissionData(AdministeredForm lockedForm) throws CtdbException {
		 Connection conn = null;
	        logger.info("DataSubmissionManager->deletePendingSubmissionData(AdministeredForm lockedForm)");
	        try {
	            conn = CtdbManager.getConnection();
	            conn.setAutoCommit(false);
	            DataSubmissionDao dao = DataSubmissionDao.getInstance(conn);
	            dao.deletePendingSubmissionFormData(lockedForm);
	            conn.commit();
	        } catch (SQLException e) {
	           this.rollback(conn);
	            throw new CtdbException("Unable to delete previously uploaded data from the data submission table : " + e.getMessage(), e);
	        } catch (DuplicateObjectException doe) {
	            this.rollback(conn);
	            throw doe;
	        } catch (CtdbException ce) {
	            this.rollback(conn);
	            throw ce;
	        } catch (Exception et) {
	           this.rollback(conn);
	            throw new CtdbException("Unable to delete previously uploaded data from the data submission table : " + et.getMessage(), et);
	        } finally {
	            this.close(conn);
	        }
	}
}

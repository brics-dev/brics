package gov.nih.nichd.ctdb.audit.manager;

import java.sql.Connection;

import gov.nih.nichd.ctdb.audit.dao.AuditDao;
import gov.nih.nichd.ctdb.audit.domain.Audit;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbManager;
import gov.nih.nichd.ctdb.common.DuplicateObjectException;
import gov.nih.nichd.ctdb.common.ObjectNotFoundException;
import gov.nih.nichd.ctdb.protocol.domain.Protocol;

public class AuditManager extends CtdbManager {


	public void createUpdateAudit(Protocol protocol, Connection conn)
			throws CtdbException, ObjectNotFoundException, DuplicateObjectException {
		AuditDao adao = AuditDao.getInstance(conn);
		Audit a = protocol.getAudit();
		Audit auditsDb = this.getAuditLast(protocol.getId()); // TODO: should also be by audit field--e.g. eSignReason.

		// Separate to be added and to be edited
		if (auditsDb == null) {
			a.setProtocolId(protocol.getId()); // This negative while adding a new protocol
			a.setUpdatedBy(protocol.getUpdatedBy());
			adao.createAudit(a);
		}
		else {
			/*
			 * to keep the record of "by who and when" the radio was turned off ("No").
			 */
			a.setId(auditsDb.getId());
			adao.updateAuditWithReason(a);
		}

	}

	public Audit getAuditLast(int protocolId) throws CtdbException {
		Connection conn = null;
		Audit audit = null;
		
		try {
			conn = CtdbManager.getConnection();
			audit = AuditDao.getInstance(conn).getAuditLast(protocolId);
		}
		finally {
			close(conn);
		}
		
		return audit;
	}
}

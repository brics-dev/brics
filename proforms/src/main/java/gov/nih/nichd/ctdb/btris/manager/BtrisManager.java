package gov.nih.nichd.ctdb.btris.manager;

import java.sql.Connection;
import java.util.HashMap;

import org.apache.log4j.Logger;

import gov.nih.nichd.ctdb.btris.dao.BtrisManagerDao;
import gov.nih.nichd.ctdb.btris.domain.BtrisObject;
import gov.nih.nichd.ctdb.btris.domain.BtrisSubject;
import gov.nih.nichd.ctdb.btris.domain.ProformsSubject;
import gov.nih.nichd.ctdb.common.CtdbException;
import gov.nih.nichd.ctdb.common.CtdbManager;
import gov.nih.nichd.ctdb.question.domain.Question;
import gov.nih.nichd.ctdb.question.domain.QuestionType;

public class BtrisManager extends CtdbManager {
	Logger logger = Logger.getLogger(BtrisManager.class.getName());

	public BtrisSubject getSubjectFromBtris(ProformsSubject ps) throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getBtrisConnection();
			BtrisManagerDao dao = BtrisManagerDao.getInstance(conn);
			BtrisSubject bs = dao.getSubjectFromBtris(ps);
			return bs;

		} finally {
			this.close(conn);
		}

	}

	public BtrisObject getBtrisDataByBtrisObject(BtrisObject bo, String mrn, QuestionType questionType)
			throws CtdbException {
		Connection conn = null;

		try {
			conn = CtdbManager.getBtrisConnection();
			BtrisManagerDao dao = BtrisManagerDao.getInstance(conn);
			BtrisObject rtnBo = dao.getBtrisDataByBtrisObject(bo, mrn, questionType);
			return rtnBo;

		} finally {
			this.close(conn);
		}
	}

	public HashMap<String, BtrisObject> getBtrisDataByQuestions(String mrn, HashMap<String, Question> questionMap)
			throws CtdbException {
		Connection conn = null;
		HashMap<String, BtrisObject> rtnBoMap = null;
		try {
			conn = CtdbManager.getBtrisConnection();
			BtrisManagerDao dao = BtrisManagerDao.getInstance(conn);
			rtnBoMap = dao.getBtrisDataByQuestions(mrn, questionMap);
			return rtnBoMap;
		} catch (Exception e) {
			logger.error("Unable to get BTRIS data for patient: " + mrn + " : " + e.getMessage(), e); 
			return rtnBoMap;
		} finally {
			this.close(conn);
		}
		
	}
}

package gov.nih.tbi.dictionary.dao;

import java.util.Set;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.dictionary.model.hibernate.DictionaryEventLog;


public interface DictionaryEventLogDao extends GenericDao <DictionaryEventLog, Long> {
	
	Set <DictionaryEventLog> searchDEEventLogs(Long entityID);
	
	Set <DictionaryEventLog> searchFSEventLogs(Long entityID);

}

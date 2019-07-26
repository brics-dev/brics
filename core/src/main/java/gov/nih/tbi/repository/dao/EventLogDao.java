package gov.nih.tbi.repository.dao;

import gov.nih.tbi.commons.dao.GenericDao;
import gov.nih.tbi.repository.model.hibernate.EventLog;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.EventType;

import java.util.List;
import java.util.Set;

/**
 * Created by amakar on 9/1/2016.
 */
public interface EventLogDao extends GenericDao<EventLog, Long> {

    List<EventLog> search(long entityID, EventType type);

    List<EventLog> search(long entityID);

    EventLog findLatestOfTypeForEntityID(long entityID, EventType type);
    
    Set <EventLog> search(long entityID, EntityType EntityType);
}

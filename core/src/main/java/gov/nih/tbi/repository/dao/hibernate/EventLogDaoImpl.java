package gov.nih.tbi.repository.dao.hibernate;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.EventType;
import gov.nih.tbi.repository.dao.EventLogDao;
import gov.nih.tbi.repository.model.hibernate.EventLog;

/**
 * Created by amakar on 9/1/2016.
 */

@Transactional("metaTransactionManager")
@Repository
public class EventLogDaoImpl extends GenericDaoImpl<EventLog, Long> implements EventLogDao {

    @Autowired
    public EventLogDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {

        super(EventLog.class, sessionFactory);
    }

    public List<EventLog> search(long entityID, EventType type) {

        List<EventLog> result = null;

        return result;
    }

    public List<EventLog> search(long entityID) {

        List<EventLog> result = null;

        return result;
    }

    public EventLog findLatestOfTypeForEntityID(long entityID, EventType type) {

        EventLog result = null;
        Query query = getSession().createNamedQuery("EventLog.searchByTypeForEntityID");
        query.setParameter("entityID", entityID);
        query.setParameter("typeStr", type.getId());

		List<EventLog> searchList = query.getResultList();

		int searchListSize = searchList.size();
        if (searchListSize > 0) {
            result = searchList.get(searchListSize - 1);
        }

        return result;
    }

	@Override
	public Set <EventLog> search(long entityID, EntityType entityType) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<EventLog> query = cb.createQuery(EventLog.class);

		Root<EventLog> root = query.from(persistentClass);
		query.where(cb.and(cb.equal(root.get("entityID"), entityID), cb.equal(root.get("type"), entityType)));
		query.orderBy(cb.desc(root.get("createTime")));

    	List<EventLog> eventLogs = createQuery(query).getResultList();
    	Set <EventLog> eventLogsSet = new LinkedHashSet<EventLog>();
    	eventLogsSet.addAll(eventLogs);

        return eventLogsSet;
	}
}

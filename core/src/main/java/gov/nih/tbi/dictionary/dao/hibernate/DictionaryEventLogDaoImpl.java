package gov.nih.tbi.dictionary.dao.hibernate;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDictDaoImpl;
import gov.nih.tbi.dictionary.dao.DictionaryEventLogDao;
import gov.nih.tbi.dictionary.model.hibernate.DictionaryEventLog;

@Transactional("dictionaryTransactionManager")
@Repository
public class DictionaryEventLogDaoImpl extends GenericDictDaoImpl<DictionaryEventLog, Long> implements DictionaryEventLogDao {

	@Autowired
	public DictionaryEventLogDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {

		super(DictionaryEventLog.class, sessionFactory);
	}

	@Override
	public Set<DictionaryEventLog> searchDEEventLogs(Long entityID) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<DictionaryEventLog> query = cb.createQuery(DictionaryEventLog.class);

		Root<DictionaryEventLog> root = query.from(DictionaryEventLog.class);
		query.where(cb.equal(root.get("dataElementID"), entityID));
		query.orderBy(cb.desc(root.get("createTime")));
		root.fetch("supportingDocumentationSet", JoinType.LEFT);

		List<DictionaryEventLog> eventLogs = createQuery(query).getResultList();
		Set<DictionaryEventLog> eventLogsSet = new LinkedHashSet<DictionaryEventLog>();
		eventLogsSet.addAll(eventLogs);

		return eventLogsSet;
	}

	@Override
	public Set<DictionaryEventLog> searchFSEventLogs(Long entityID) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<DictionaryEventLog> query = cb.createQuery(DictionaryEventLog.class);

		Root<DictionaryEventLog> root = query.from(DictionaryEventLog.class);
		query.where(cb.equal(root.get("formStructureID"), entityID));
		query.orderBy(cb.desc(root.get("createTime")));
		root.fetch("supportingDocumentationSet", JoinType.LEFT);

		List<DictionaryEventLog> eventLogs = createQuery(query).getResultList();
		Set<DictionaryEventLog> eventLogsSet = new LinkedHashSet<DictionaryEventLog>();
		eventLogsSet.addAll(eventLogs);

		return eventLogsSet;
	}

}

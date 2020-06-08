package gov.nih.tbi.dictionary.dao.hibernate;


import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDictDaoImpl;
import gov.nih.tbi.dictionary.dao.PublishedFormStructureDao;
import gov.nih.tbi.dictionary.model.hibernate.PublishedFormStructure;

@Transactional("dictionaryTransactionManager")
@Repository
public class PublishedFormStructureDaoImpl extends GenericDictDaoImpl<PublishedFormStructure, Long> implements PublishedFormStructureDao {

	public PublishedFormStructureDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {
		super(PublishedFormStructure.class, sessionFactory);
	}

	@Override
	public PublishedFormStructure getFormStructurePublished(Long formStructureId, Long diseaseId) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<PublishedFormStructure> query = cb.createQuery(PublishedFormStructure.class);
		
		Root<PublishedFormStructure> root = query.from(PublishedFormStructure.class);
		query.where(cb.equal(root.get("formStructureId"), formStructureId),cb.equal(root.get("diseaseId"), diseaseId));
		
		query.orderBy(cb.desc(root.get("publicationDate")));
		
		try {
			return createQuery(query).setMaxResults(1).getSingleResult();
		} catch (NoResultException e) {
		    return null;
		}

	}

}

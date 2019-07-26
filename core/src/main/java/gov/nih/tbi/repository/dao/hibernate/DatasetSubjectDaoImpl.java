
package gov.nih.tbi.repository.dao.hibernate;

import java.util.List;

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
import gov.nih.tbi.repository.dao.DatasetSubjectDao;
import gov.nih.tbi.repository.model.hibernate.DatasetSubject;

@Transactional("metaTransactionManager")
@Repository
public class DatasetSubjectDaoImpl extends GenericDaoImpl<DatasetSubject, Long> implements DatasetSubjectDao {

	@Autowired
	public DatasetSubjectDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {

		super(DatasetSubject.class, sessionFactory);
	}


	// TODO, combine these two methods, just pass guid as parameter
	public List<DatasetSubject> getDatasetSubjectListByGuid(String guid) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<DatasetSubject> query = cb.createQuery(DatasetSubject.class);
		Root<DatasetSubject> root = query.from(DatasetSubject.class);

		query.where(cb.equal(root.get("subjectGuid"), guid)).distinct(true);
		return createQuery(query).getResultList();
	}
}

package gov.nih.tbi.metastudy.dao.hibernate;

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
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.metastudy.dao.MetaStudyDataDao;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyData;

@Transactional("metaTransactionManager")
@Repository
public class MetaStudyDataDaoImpl extends GenericDaoImpl<MetaStudyData, Long> implements MetaStudyDataDao {

	@Autowired
	public MetaStudyDataDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {

		super(MetaStudyData.class, sessionFactory);
	}


	public boolean isMetaStudyDataTitleUnique(String fileName, long metaStudyId) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<MetaStudyData> root = query.from(MetaStudyData.class);

		query.where(cb.and(cb.equal(root.join("metaStudy", JoinType.LEFT).get("id"), metaStudyId),
				cb.equal(cb.upper(root.join("userFile", JoinType.LEFT).get("name")), fileName.toUpperCase())));

		query.select(cb.count(root));
		long count = createQuery(query).getSingleResult();
		return count == 0;

	}
}

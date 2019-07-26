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
import gov.nih.tbi.repository.dao.BasicDatasetFileDao;
import gov.nih.tbi.repository.model.hibernate.BasicDatasetFile;

@Transactional("metaTransactionManager")
@Repository
public class BasicDatasetFileDaoImpl extends GenericDaoImpl<BasicDatasetFile, Long> implements BasicDatasetFileDao {

	@Autowired
	public BasicDatasetFileDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {
		super(BasicDatasetFile.class, sessionFactory);
	}

	/**
	 * @inheritDoc
	 */
	public List<BasicDatasetFile> getUserFileByDatasetId(Long datasetId) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<BasicDatasetFile> query = cb.createQuery(BasicDatasetFile.class);
		Root<BasicDatasetFile> root = query.from(BasicDatasetFile.class);

		query.where(cb.equal(root.get("dataset"), datasetId)).distinct(true);
		return createQuery(query).getResultList();
	}
}

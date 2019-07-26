
package gov.nih.tbi.commons.dao.hibernate;

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
import gov.nih.tbi.commons.dao.FileTypeDao;
import gov.nih.tbi.commons.model.FileClassification;
import gov.nih.tbi.commons.model.hibernate.FileType;

@Transactional("metaTransactionManager")
@Repository
public class FileTypeDaoImpl extends GenericDaoImpl<FileType, Long> implements FileTypeDao {

	@Autowired
	public FileTypeDaoImpl(@Qualifier(CoreConstants.COMMONS_FACTORY) SessionFactory sessionFactory) {

		super(FileType.class, sessionFactory);
	}

	/**
	 * @inheritDoc
	 */
	public List<FileType> getFileTypeByClassification(FileClassification fileClassification) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<FileType> query = cb.createQuery(FileType.class);
		Root<FileType> root = query.from(persistentClass);

		query.where(cb.and(cb.equal(root.get("fileClassification"), fileClassification),
				cb.equal(root.get("isActive"), true)));
		query.orderBy(cb.asc(root.get(CoreConstants.NAME))).distinct(true);

		List<FileType> out = createQuery(query).getResultList();
		return out;
	}


	@Override
	public FileType get(String name) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<FileType> query = cb.createQuery(FileType.class);
		Root<FileType> root = query.from(persistentClass);

		query.where(cb.and(cb.equal(root.get(CoreConstants.NAME), name), cb.equal(root.get("isActive"), true)));

		return getUniqueResult(query.distinct(true));
	}

	@Override
	public FileType getFileTypeByNameAndClassification(String name, FileClassification fileClassification) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<FileType> query = cb.createQuery(FileType.class);
		Root<FileType> root = query.from(persistentClass);

		query.where(cb.and(cb.equal(root.get(CoreConstants.NAME), name), cb.equal(root.get("isActive"), true),
				cb.equal(root.get("fileClassification"), fileClassification)));

		return getUniqueResult(query.distinct(true));
	}
}

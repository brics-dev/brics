
package gov.nih.tbi.repository.dao.hibernate;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.commons.model.hibernate.FileType;
import gov.nih.tbi.repository.dao.UserFileDao;
import gov.nih.tbi.repository.model.hibernate.UserFile;

/**
 * Hibernate Implementation of UserFileDao
 * 
 * @author Andrew Johnson
 * 
 */
@Transactional("metaTransactionManager")
@Repository
public class UserFileDaoImpl extends GenericDaoImpl<UserFile, Long> implements UserFileDao {

	@Autowired
	public UserFileDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {

		super(UserFile.class, sessionFactory);
	}

	/**
	 * @inheritDoc
	 */
	public List<UserFile> getByUserId(Long userId) {

		if (userId == null) {
			throw new IllegalArgumentException("Cannot retrieve UserFiles without a userId");
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<UserFile> query = cb.createQuery(UserFile.class);

		Root<UserFile> root = query.from(UserFile.class);
		query.where(cb.equal(root.get("userId"), userId)).distinct(true);

		Query<UserFile> q = getSession().createQuery(query);
		return q.getResultList();
	}

	/**
     * @inheritDoc
     */
    @Override
    public List<UserFile> getByUserId(Long userId, FileType fileType)
    {

        if (userId == null)
        {
            throw new IllegalArgumentException("Cannot retrieve UserFiles without a userId");
        }

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<UserFile> query = cb.createQuery(UserFile.class);

		Root<UserFile> root = query.from(UserFile.class);
		query.where(cb.and(
				cb.equal(root.get("userId"), userId), cb.equal(root.get("fileType"), fileType)));
		query.distinct(true);

		Query<UserFile> q = getSession().createQuery(query);
		return q.getResultList();
    }

	/**
	 * @inheritDoc
	 */
	public List<UserFile> getById(List<Long> ids) {
		if (ids == null) {
			throw new IllegalArgumentException("Cannot retrieve UserFiles without ID values");
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<UserFile> query = cb.createQuery(UserFile.class);

		Root<UserFile> root = query.from(UserFile.class);
		query.where(root.get("id").in(ids)).distinct(true);

		Query<UserFile> q = createQuery(query);
		return q.getResultList();
	}

}

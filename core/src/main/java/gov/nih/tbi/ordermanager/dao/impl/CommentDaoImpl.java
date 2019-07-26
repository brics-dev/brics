
package gov.nih.tbi.ordermanager.dao.impl;

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
import gov.nih.tbi.ordermanager.dao.CommentDao;
import gov.nih.tbi.ordermanager.model.BiospecimenOrder;
import gov.nih.tbi.ordermanager.model.Comment;

@Transactional("metaTransactionManager")
@Repository
public class CommentDaoImpl extends GenericDaoImpl<Comment, Long> implements CommentDao {

	@Autowired
	public CommentDaoImpl(@Qualifier(CoreConstants.ACCOUNT_FACTORY) SessionFactory sessionFactory) {

		super(Comment.class, sessionFactory);
	}

	@Override
	public List<Comment> findByOrder(BiospecimenOrder order) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Comment> query = cb.createQuery(Comment.class);
		Root<Comment> root = query.from(Comment.class);
		query.where(cb.equal(root.get("biospecimenOrder"), order)).distinct(true);

		List<Comment> result = createQuery(query).getResultList();
		return result;
	}

}

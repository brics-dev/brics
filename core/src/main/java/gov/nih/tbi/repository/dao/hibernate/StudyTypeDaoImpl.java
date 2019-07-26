package gov.nih.tbi.repository.dao.hibernate;

import java.util.List;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.repository.dao.StudyTypeDao;
import gov.nih.tbi.repository.model.hibernate.StudyType;

@Repository
public class StudyTypeDaoImpl extends GenericDaoImpl<StudyType, Long> implements StudyTypeDao {

	@Autowired
	public StudyTypeDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {

		super(StudyType.class, sessionFactory);
	}

	@Override
	public StudyType getStudyTypeFromName(String name) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StudyType> query = cb.createQuery(StudyType.class);

		Root<StudyType> root = query.from(StudyType.class);
		query.where(cb.equal(root.get("name"), name));

		return getUniqueResult(query);
	}

	@Override
	public StudyType getStudyTypeById(Long id) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StudyType> query = cb.createQuery(StudyType.class);

		Root<StudyType> root = query.from(StudyType.class);
		query.where(cb.equal(root.get("id"), id));

		return getUniqueResult(query);
	}
}

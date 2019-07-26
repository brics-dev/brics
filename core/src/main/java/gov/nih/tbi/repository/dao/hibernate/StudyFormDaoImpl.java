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
import gov.nih.tbi.repository.dao.StudyFormDao;
import gov.nih.tbi.repository.model.hibernate.StudyForm;

@Transactional("metaTransactionManager")
@Repository
public class StudyFormDaoImpl extends GenericDaoImpl<StudyForm, Long> implements StudyFormDao {

	@Autowired
	public StudyFormDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {
		super(StudyForm.class, sessionFactory);
	}

	/**
	 * @inheritDoc
	 */
	public List<StudyForm> getByShortNameVersion(String shortname, String version) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StudyForm> query = cb.createQuery(StudyForm.class);

		Root<StudyForm> root = query.from(StudyForm.class);
		query.where(cb.and(cb.equal(root.get("shortName"), shortname), cb.equal(root.get("version"), version)));

		return createQuery(query).getResultList();
	}

	/**
	 * @inheritDoc
	 */
	public List<StudyForm> getByStudy(Long studyId) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StudyForm> query = cb.createQuery(StudyForm.class);

		Root<StudyForm> root = query.from(StudyForm.class);
		query.where(cb.equal(root.get("studyId"), studyId));

		return createQuery(query).getResultList();
	}

	/**
	 * @inheritDoc
	 */
	public StudyForm getSingle(Long studyId, String shortName, String version) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<StudyForm> query = cb.createQuery(StudyForm.class);

		Root<StudyForm> root = query.from(StudyForm.class);
		query.where(cb.and(cb.equal(root.get("studyId"), studyId), cb.equal(root.get("shortName"), shortName),
				cb.equal(root.get("version"), version)));

		return getUniqueResult(query);
	}
}

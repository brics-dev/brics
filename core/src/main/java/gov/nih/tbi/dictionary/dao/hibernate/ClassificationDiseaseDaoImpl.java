
package gov.nih.tbi.dictionary.dao.hibernate;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDictDaoImpl;
import gov.nih.tbi.dictionary.dao.ClassificationDiseaseDao;
import gov.nih.tbi.dictionary.model.hibernate.Classification;
import gov.nih.tbi.dictionary.model.hibernate.ClassificationDisease;
import gov.nih.tbi.dictionary.model.hibernate.Disease;

@Transactional("dictionaryTransactionManager")
@Repository
public class ClassificationDiseaseDaoImpl extends GenericDictDaoImpl<ClassificationDisease, Long> implements ClassificationDiseaseDao {

	@Autowired
	public ClassificationDiseaseDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {

		super(ClassificationDisease.class, sessionFactory);
	}

	/**
	 * @inheritDoc
	 */
	public List<Classification> getByDisease(Disease disease, boolean isAdmin) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Classification> query = cb.createQuery(Classification.class);
		Root<ClassificationDisease> root = query.from(ClassificationDisease.class);
		
		Path<Classification> classPath = root.get("classification");
		query.select(cb.construct(Classification.class, classPath.get("id"), classPath.get("name"),
				classPath.get("isActive"), classPath.get("canCreate"))).distinct(true);

		Join<ClassificationDisease, Disease> diseaseJoin = root.join("disease", JoinType.INNER);
		Predicate predicate = cb.equal(diseaseJoin.get("id"), disease.getId());

		if (!isAdmin) {
			predicate = cb.and(predicate, cb.equal(classPath.get("canCreate"), true));
		}

		query.where(predicate);
		query.orderBy(cb.asc(classPath.get("name")));
		return createQuery(query).getResultList();
	}
}


package gov.nih.tbi.dictionary.dao.hibernate;

import java.util.Collections;
import java.util.List;

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
import gov.nih.tbi.commons.dao.hibernate.GenericDictDaoImpl;
import gov.nih.tbi.dictionary.dao.SubgroupDiseaseDao;
import gov.nih.tbi.dictionary.model.hibernate.Disease;
import gov.nih.tbi.dictionary.model.hibernate.Subgroup;
import gov.nih.tbi.dictionary.model.hibernate.SubgroupDisease;

@Transactional("dictionaryTransactionManager")
@Repository
public class SubgroupDiseaseDaoImpl extends GenericDictDaoImpl<SubgroupDisease, Long> implements SubgroupDiseaseDao {

	@Autowired
	public SubgroupDiseaseDaoImpl(@Qualifier(CoreConstants.DICTIONARY_FACTORY) SessionFactory sessionFactory) {

		super(SubgroupDisease.class, sessionFactory);
	}

	/**
	 * @inheritDoc
	 */
	public List<Subgroup> getByDisease(Disease disease) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Subgroup> query = cb.createQuery(Subgroup.class);
		Root<SubgroupDisease> root = query.from(SubgroupDisease.class);

		query.select(root.get("subgroup"));
		query.where(cb.equal(root.join("disease", JoinType.LEFT).get("id"), disease.getId()));

		List<Subgroup> list = createQuery(query).getResultList();
		Collections.sort(list);

		return list;
	}

	@Override
	public Disease getDiseaseBySubGroup(Subgroup subgroup) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Disease> query = cb.createQuery(Disease.class);
		Root<SubgroupDisease> root = query.from(SubgroupDisease.class);

		query.select(root.get("disease"));
		query.where(cb.equal(root.get("subgroup"), subgroup));

		Disease disease = getUniqueResult(query);
		return disease;
	}
}

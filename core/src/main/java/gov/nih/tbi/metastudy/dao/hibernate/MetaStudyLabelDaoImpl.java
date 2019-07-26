package gov.nih.tbi.metastudy.dao.hibernate;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.metastudy.dao.MetaStudyLabelDao;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyLabel;

@Transactional("metaTransactionManager")
@Repository
public class MetaStudyLabelDaoImpl extends GenericDaoImpl<MetaStudyLabel, Long> implements MetaStudyLabelDao {

	@Autowired
	public MetaStudyLabelDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {

		super(MetaStudyLabel.class, sessionFactory);
	}

	public MetaStudyLabel getByName(String name) {

		if (name == null) {
			name = CoreConstants.EMPTY_STRING;
		}
		name = CoreConstants.WILDCARD + name + CoreConstants.WILDCARD;

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<MetaStudyLabel> query = cb.createQuery(MetaStudyLabel.class);
		Root<MetaStudyLabel> root = query.from(MetaStudyLabel.class);
		query.where(cb.like(cb.upper(root.get("label")), name.toUpperCase())).distinct(true);

		MetaStudyLabel label = getUniqueResult(query);
		return label;

	}

	public List<MetaStudyLabel> search(String searchKey) {

		if (searchKey == null) {
			searchKey = CoreConstants.EMPTY_STRING;
		}
		searchKey = CoreConstants.WILDCARD + searchKey + CoreConstants.WILDCARD;

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createTupleQuery();
		Root<MetaStudyLabel> root = query.from(MetaStudyLabel.class);

		Expression<String> labelExp = root.get("label");
		query.multiselect(labelExp.alias("label"), cb.count(labelExp).alias("count"));
		query.where(cb.like(cb.upper(labelExp), searchKey.toUpperCase()));
		query.groupBy(labelExp);

		List<Tuple> tuples = createQuery(query).getResultList();
		List<MetaStudyLabel> labels = new ArrayList<MetaStudyLabel>();

		for (Tuple t : tuples) {
			MetaStudyLabel msl = new MetaStudyLabel();
			msl.setLabel((String) t.get("label"));
			msl.setCount((Long) t.get("count"));
			labels.add(msl);
		}

		return labels;
	}

	public Long getCountByLabel(String label) {

		if (label == null) {
			label = CoreConstants.EMPTY_STRING;
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<MetaStudyLabel> root = query.from(MetaStudyLabel.class);

		query.where(cb.like(cb.upper(root.get("label")), label.toUpperCase()));
		query.select(cb.count(root));

		Long countValue = createQuery(query).getSingleResult();
		return countValue;
	}
}

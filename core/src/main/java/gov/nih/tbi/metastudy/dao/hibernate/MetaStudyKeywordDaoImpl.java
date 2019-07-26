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
import gov.nih.tbi.metastudy.dao.MetaStudyKeywordDao;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyKeyword;

@Transactional("metaTransactionManager")
@Repository
public class MetaStudyKeywordDaoImpl extends GenericDaoImpl<MetaStudyKeyword, Long> implements MetaStudyKeywordDao {

	@Autowired
	public MetaStudyKeywordDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {

		super(MetaStudyKeyword.class, sessionFactory);
	}

	public MetaStudyKeyword getByName(String name) {

		if (name == null) {
			name = CoreConstants.EMPTY_STRING;
		}
		name = CoreConstants.WILDCARD + name + CoreConstants.WILDCARD;

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<MetaStudyKeyword> query = cb.createQuery(MetaStudyKeyword.class);
		Root<MetaStudyKeyword> root = query.from(MetaStudyKeyword.class);
		query.where(cb.like(cb.upper(root.get("keyword")), name.toUpperCase())).distinct(true);

		MetaStudyKeyword keyword = getUniqueResult(query);
		return keyword;
	}

	public List<MetaStudyKeyword> search(String searchKey) {

		if (searchKey == null) {
			searchKey = CoreConstants.EMPTY_STRING;
		}
		searchKey = CoreConstants.WILDCARD + searchKey + CoreConstants.WILDCARD;

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Tuple> query = cb.createTupleQuery();
		Root<MetaStudyKeyword> root = query.from(MetaStudyKeyword.class);

		Expression<String> keywordExp = root.get("keyword");
		query.multiselect(keywordExp.alias("keyword"), cb.count(keywordExp));
		query.where(cb.like(cb.upper(keywordExp), searchKey.toUpperCase()));
		query.groupBy(keywordExp);

		List<Tuple> tuples = createQuery(query).getResultList();
		List<MetaStudyKeyword> keywords = new ArrayList<MetaStudyKeyword>();

		for (Tuple t : tuples) {
			MetaStudyKeyword msk = new MetaStudyKeyword();
			msk.setKeyword((String) t.get("keyword"));
			msk.setCount((Long) t.get("count"));
			keywords.add(msk);
		}

		return keywords;
	}

	public Long getCountByKeyword(String keyword) {

		if (keyword == null) {
			keyword = CoreConstants.EMPTY_STRING;
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<MetaStudyKeyword> root = query.from(MetaStudyKeyword.class);

		query.where(cb.like(cb.upper(root.get("keyword")), keyword.toUpperCase()));
		query.select(cb.count(root));

		Long countValue = createQuery(query).getSingleResult();
		return countValue;
	}

}

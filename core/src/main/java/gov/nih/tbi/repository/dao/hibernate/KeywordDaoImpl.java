package gov.nih.tbi.repository.dao.hibernate;

import java.util.ArrayList;
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
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyKeyword;
import gov.nih.tbi.repository.dao.KeywordDao;
import gov.nih.tbi.repository.model.hibernate.Keyword;
import gov.nih.tbi.repository.model.hibernate.StudyKeyword;

@Transactional("metaTransactionManager")
@Repository
public class KeywordDaoImpl extends GenericDaoImpl<Keyword, Long> implements KeywordDao {

	@Autowired
	public KeywordDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {

		super(Keyword.class, sessionFactory);
	}
	
	public List<? extends Keyword> getAllKeywords(Class<?> keywordClass) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Keyword> query = (CriteriaQuery<Keyword>) cb.createQuery(keywordClass);
		query.from(keywordClass);
		
		List<Keyword> keywords = createQuery(query).getResultList();
		if (keywords.size() < 1) {
			return new ArrayList<Keyword>();
		}
		return keywords;
	}
	
	public List<? extends Keyword> search(String searchKey, Class<?> keywordClass) {
    	
		if (searchKey == null) {
			searchKey = CoreConstants.EMPTY_STRING;
		}
		searchKey = CoreConstants.WILDCARD + searchKey + CoreConstants.WILDCARD;

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
		Root<?> root = query.from(keywordClass);
		
		query.multiselect(root.get("keyword"), cb.count(root));
		query.where(cb.like(cb.upper(root.get("keyword")), searchKey.toUpperCase()));
		query.groupBy(root.get("keyword"));
		
		List<Object[]> rows = createQuery(query).getResultList();
		List<Keyword> keywords = new ArrayList<Keyword>();
		
		for (Object[] row : rows) {
			Keyword k = null;
			
			if (keywordClass.equals(StudyKeyword.class)) {
				k = new StudyKeyword();
				((StudyKeyword) k).setKeyword((String) row[0]);
				((StudyKeyword) k).setCount((Long) row[1]);
			} else if (keywordClass.equals(MetaStudyKeyword.class)) {
				k = new MetaStudyKeyword();
				((MetaStudyKeyword) k).setKeyword((String) row[0]);
				((MetaStudyKeyword) k).setCount((Long) row[1]);
			}
			
			keywords.add(k);
		}
		
		if (keywords.size() < 1) {
			return new ArrayList<Keyword>();
		}
		
		return keywords;
    }
    
    public Long getCountByKeyword(String keyword, Class<?> keywordClass) {

		if (keyword == null) {
			keyword = CoreConstants.EMPTY_STRING;
		}
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<?> root = query.from(keywordClass);

		query.select(cb.count(root.get("keyword")));
		query.where(cb.like(cb.upper(root.get("keyword")), keyword.toUpperCase()));
		
		Long countValue = createQuery(query).getSingleResult();
		return countValue;
    }
}

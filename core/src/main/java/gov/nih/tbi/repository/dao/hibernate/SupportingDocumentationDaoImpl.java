package gov.nih.tbi.repository.dao.hibernate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.PersistanceSessionUtils;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.repository.dao.SupportingDocumentationDao;
import gov.nih.tbi.repository.model.hibernate.AccessRecord;
import gov.nih.tbi.repository.model.hibernate.SupportingDocumentation;

@Transactional("metaTransactionManager")
@Repository
public class SupportingDocumentationDaoImpl extends GenericDaoImpl<SupportingDocumentation, Long> implements SupportingDocumentationDao {

	@Autowired
	private PersistanceSessionUtils persistanceSessionUtils;
	
	@Autowired
	public SupportingDocumentationDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {
		super(SupportingDocumentation.class, sessionFactory);
	}

	public List<SupportingDocumentation> getStudyPublicationDocumentation(Long studyId) {

		String sqlString = "select id from supporting_documentation where study_id = ?";
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		idQuery.setParameter(1, studyId);
		List<Long> ids = idQuery.getResultList();

		List<Long> pubFileTypeIds = Arrays.asList(8l, 28l);

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<SupportingDocumentation> query = cb.createQuery(SupportingDocumentation.class);
		Root<SupportingDocumentation> root = query.from(SupportingDocumentation.class);

		if(ids.isEmpty()){
			return new ArrayList<SupportingDocumentation>();
		} else {
			query.where(cb.and(root.get("id").in(ids), root.join("fileType").get("id").in(pubFileTypeIds)));
		}
		query.distinct(true);

		return createQuery(query).getResultList();
	}

	public List<SupportingDocumentation> getMetastudyPublicationDocumentation(Long metaStudyId) {

		String sqlString = "select id from supporting_documentation where meta_study_id = ?";
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		idQuery.setParameter(1, metaStudyId);
		List<Long> ids = idQuery.getResultList();
		
		if(ids.isEmpty()){
			return new ArrayList<SupportingDocumentation>();
		} 

		List<Long> pubFileTypeIds = Arrays.asList(8l, 28l);

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<SupportingDocumentation> query = cb.createQuery(SupportingDocumentation.class);
		Root<SupportingDocumentation> root = query.from(SupportingDocumentation.class);

		query.where(cb.and(root.get("id").in(ids), root.join("fileType").get("id").in(pubFileTypeIds)));
		query.distinct(true);

		return createQuery(query).getResultList();
	}

	public SupportingDocumentation getPublicationDocumentation(Long supportingDocId) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<SupportingDocumentation> query = cb.createQuery(SupportingDocumentation.class);
		Root<SupportingDocumentation> root = query.from(SupportingDocumentation.class);

		query.where(cb.equal(root.get("id"), supportingDocId)).distinct(true);
		return getUniqueResult(query);
	}
	
	public SupportingDocumentation getPublicationDocumentation(Long studyId, Long publicationId){
		String sqlString = "select id from supporting_documentation where study_id = ?";
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		idQuery.setParameter(1, studyId);
		List<Long> ids = idQuery.getResultList();

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<SupportingDocumentation> query = cb.createQuery(SupportingDocumentation.class);
		Root<SupportingDocumentation> root = query.from(SupportingDocumentation.class);
		root.fetch("userFile", JoinType.LEFT);
		if(ids.isEmpty()){
			return null;
		} else {
			query.where(cb.and(root.get("id").in(ids), cb.equal(root.join("publication").get("id"), publicationId)));
		}
		
		query.distinct(true);
		
		return getUniqueResult(query);
	}

}

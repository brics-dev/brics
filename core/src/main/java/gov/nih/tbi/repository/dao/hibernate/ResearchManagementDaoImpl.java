
package gov.nih.tbi.repository.dao.hibernate;

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
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.commons.model.ResearchManagementRole;
import gov.nih.tbi.repository.dao.ResearchManagementDao;
import gov.nih.tbi.repository.model.hibernate.ResearchManagement;

@Transactional("metaTransactionManager")
@Repository
public class ResearchManagementDaoImpl extends GenericDaoImpl<ResearchManagement, Long> implements ResearchManagementDao {

	@Autowired
	public ResearchManagementDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {
		super(ResearchManagement.class, sessionFactory);
	}

	public ResearchManagement getStudyPrimaryInvestigator(Long studyId) {
		
		String sqlString = "select id from research_management where study_id = ? and role_id = ? ";
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		idQuery.setParameter(1, studyId);
		idQuery.setParameter(2, ResearchManagementRole.PRIMARY_PRINCIPAL_INVESTIGATOR.getId());
		Integer rmid = (Integer) idQuery.getSingleResult();

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<ResearchManagement> query = cb.createQuery(ResearchManagement.class);
		Root<ResearchManagement> root = query.from(persistentClass);
				
		root.fetch("pictureFile", JoinType.LEFT);
		query.where(cb.equal(root.get("id"), rmid));

		return getUniqueResult(query.distinct(true));
	}


	public ResearchManagement getStudyManagementImage(Long studyId, Long rmId) {

/*		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<ResearchManagement> query = cb.createQuery(ResearchManagement.class);

		Root<ResearchManagement> root = query.from(persistentClass);
		root.fetch("pictureFile", JoinType.LEFT);
		query.where(cb.and(cb.equal(root.get("study_id"), studyId), cb.equal(root.get("id"), rmId)));

		return getUniqueResult(query.distinct(true));*/
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<ResearchManagement> query = cb.createQuery(ResearchManagement.class);

		Root<ResearchManagement> root = query.from(persistentClass);
		root.fetch("pictureFile", JoinType.LEFT);
		query.select(root);
		query.where(cb.equal(root.get("id"), rmId)).distinct(true);
		
		ResearchManagement rtn = getUniqueResult(query);
		return rtn;
	}

	public ResearchManagement getMetastudyPrimaryInvestigator(Long metastudyId) {
		
		String sqlString = "select id from research_management where meta_study_id = ? and role_id = ? ";
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		idQuery.setParameter(1, metastudyId);
		idQuery.setParameter(2, ResearchManagementRole.PRIMARY_PRINCIPAL_INVESTIGATOR.getId());
		Integer rmid = (Integer) idQuery.getSingleResult();

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<ResearchManagement> query = cb.createQuery(ResearchManagement.class);
		Root<ResearchManagement> root = query.from(persistentClass);
				
		root.fetch("pictureFile", JoinType.LEFT);
		query.where(cb.equal(root.get("id"), rmid));

		return getUniqueResult(query.distinct(true));
	}
	
	public ResearchManagement getMetaStudyManagementImage(Long metastudyId, Long rmId) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<ResearchManagement> query = cb.createQuery(ResearchManagement.class);

		Root<ResearchManagement> root = query.from(persistentClass);
		root.fetch("pictureFile", JoinType.LEFT);
		query.select(root);
		query.where(cb.equal(root.get("id"), rmId)).distinct(true);
		
		ResearchManagement rtn = getUniqueResult(query);

		return rtn;
	}

}

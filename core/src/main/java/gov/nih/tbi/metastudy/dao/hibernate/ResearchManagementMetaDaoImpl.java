package gov.nih.tbi.metastudy.dao.hibernate;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
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
import gov.nih.tbi.metastudy.dao.ResearchManagementMetaDao;
import gov.nih.tbi.metastudy.model.hibernate.ResearchManagementMeta;
import gov.nih.tbi.repository.model.hibernate.UserFile;

@Transactional("metaTransactionManager")
@Repository
public class ResearchManagementMetaDaoImpl extends GenericDaoImpl<ResearchManagementMeta, Long> implements ResearchManagementMetaDao {

	@Autowired
	public ResearchManagementMetaDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {
		super(ResearchManagementMeta.class, sessionFactory);
	}

	public ResearchManagementMeta getMetastudyPrimaryInvestigator(Long metastudyId) {
		
		String sqlString = "select id from research_management where meta_study_id = ? and role_id = ? ";
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		idQuery.setParameter(1, metastudyId);
		idQuery.setParameter(2, ResearchManagementRole.PRIMARY_PRINCIPAL_INVESTIGATOR.getId());
		Integer rmid = (Integer) idQuery.getSingleResult();

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<ResearchManagementMeta> query = cb.createQuery(ResearchManagementMeta.class);
		Root<ResearchManagementMeta> root = query.from(persistentClass);
				
		root.fetch("pictureFile", JoinType.LEFT);
		query.where(cb.equal(root.get("id"), rmid));

		return getUniqueResult(query.distinct(true));
	}
	
	public ResearchManagementMeta getMetaStudyManagementImage(Long metastudyId, Long rmId) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<ResearchManagementMeta> query = cb.createQuery(ResearchManagementMeta.class);

		Root<ResearchManagementMeta> root = query.from(persistentClass);
		root.fetch("pictureFile", JoinType.LEFT);
		query.where(cb.equal(root.get("id"), rmId));
		ResearchManagementMeta rtn = getUniqueResult(query.distinct(true));
		
		CriteriaQuery<UserFile> queryUf = cb.createQuery(UserFile.class);
		Root<ResearchManagementMeta> rootRM= queryUf.from(ResearchManagementMeta.class);
		Join<ResearchManagementMeta, UserFile> picFile = rootRM.join("pictureFile", JoinType.LEFT);
		queryUf.select(picFile).where(cb.and(cb.equal(rootRM.get("id"), rmId)));
		UserFile pictureFile = getUniqueResult(queryUf.distinct(true));
		
		rtn.setPictureFile(pictureFile);
		return rtn;
	}


}


package gov.nih.tbi.repository.dao.hibernate;

import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.repository.dao.TherapeuticTargetDataDao;
import gov.nih.tbi.repository.model.alzped.MetaStudyTherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.MetaStudyTherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.StudyTherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.StudyTherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.TherapeuticTargetData;

@Transactional("metaTransactionManager")
@Repository
public class TherapeuticTargetDataDaoImpl extends GenericDaoImpl<TherapeuticTargetData, Long> implements TherapeuticTargetDataDao
{

    @Autowired
    public TherapeuticTargetDataDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory)
    {

        super(TherapeuticTargetData.class, sessionFactory);
    }
    
    
	public int saveTherapeuticTargetData(StudyTherapeuticTarget studyTherapeuticTarget, String studyId) {
		Long id = studyTherapeuticTarget.getTherapeuticTarget().getId();
		String sqlString = "insert into THERAPEUTIC_TARGET_DATA values (nextval('therapeutic_target_data_seq'), " + id + " , " + studyId + ", null) ";
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
	}
	
	public int deleteTherapeuticTargetData(StudyTherapeuticTarget s, String studyId) {
		Long id = s.getTherapeuticTarget().getId();
		String sqlString = "delete from THERAPEUTIC_target_DATA where THERAPEUTIC_target_id = " + id + " and study_id = " + studyId;
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
	}
	
	public int saveTherapeuticTargetData(MetaStudyTherapeuticTarget metStudyTherapeuticTarget, String metaStudyId) {
		Long id = metStudyTherapeuticTarget.getTherapeuticTarget().getId();
		String sqlString = "insert into THERAPEUTIC_TARGET_DATA values (nextval('therapeutic_target_data_seq'), " + id + " , " + metaStudyId + ", null) ";
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
	}
	
	public int deleteTherapeuticTargetData(MetaStudyTherapeuticTarget s, String metaStudyId) {
		Long id = s.getTherapeuticTarget().getId();
		String sqlString = "delete from THERAPEUTIC_target_DATA where THERAPEUTIC_target_id = " + id + " and meta_study_id = " + metaStudyId;
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
	}


	@SuppressWarnings("rawtypes")
	@Override
	public int saveTherapeuticTargetDataBulk(List<MetaStudyTherapeuticTarget> metaStudyTherapeuticTargets,
			String metaStudyId) {
		
		String sqlString = "";
		for(MetaStudyTherapeuticTarget studyTherapeuticAgent:metaStudyTherapeuticTargets) {
			Long id = studyTherapeuticAgent.getTherapeuticTarget().getId();
			sqlString+= "insert into THERAPEUTIC_TARGET_DATA values (nextval('therapeutic_target_data_seq'), " + id + " , " + metaStudyId + ", null); ";
		}
		
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public int deleteTherapeuticTargetDataBulk(List<MetaStudyTherapeuticTarget> metaStudyTherapeuticTargets,
			String metaStudyId) {
		
		String sqlString = "";
		for(MetaStudyTherapeuticTarget metaStudyTherapeuticTarget:metaStudyTherapeuticTargets) {
			Long id = metaStudyTherapeuticTarget.getTherapeuticTarget().getId();
			sqlString+= "delete from THERAPEUTIC_TARGET_DATA where THERAPEUTIC_target_id = " + id + " and meta_study_id = " + metaStudyId+ ";";
		}
		
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
	}
}

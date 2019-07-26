
package gov.nih.tbi.repository.dao.hibernate;

import java.math.BigInteger;
import java.util.List;

import org.hibernate.SessionFactory;
import org.hibernate.query.NativeQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.commons.model.DatasetFileStatus;
import gov.nih.tbi.repository.dao.TherapeuticAgentDataDao;
import gov.nih.tbi.repository.model.alzped.MetaStudyTherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.StudyTherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.TherapeuticAgentData;

@Transactional("metaTransactionManager")
@Repository
public class TherapeuticAgentDataDaoImpl extends GenericDaoImpl<TherapeuticAgentData, Long> implements TherapeuticAgentDataDao
{

    @Autowired
    public TherapeuticAgentDataDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory)
    {

        super(TherapeuticAgentData.class, sessionFactory);
    }
    
    
	public int saveTherapeuticAgentData(StudyTherapeuticAgent studyTherapeuticAgent, String studyId) {
		Long id = studyTherapeuticAgent.getTherapeuticAgent().getId();
		String sqlString = "insert into THERAPEUTIC_AGENT_DATA values (nextval('therapeutic_agent_data_seq'), " + id + " , " + studyId + ", null) ";
		@SuppressWarnings("rawtypes")
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
	}
	
	public int deleteTherapeuticAgentData(StudyTherapeuticAgent s, String studyId) {
		Long id = s.getTherapeuticAgent().getId();
		String sqlString = "delete from THERAPEUTIC_AGENT_DATA where THERAPEUTIC_AGENT_id = " + id + " and study_id = " + studyId;
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
	}
	public int saveTherapeuticAgentData(MetaStudyTherapeuticAgent studyTherapeuticAgent, String metaStudyId) {
		Long id = studyTherapeuticAgent.getTherapeuticAgent().getId();
		String sqlString = "insert into THERAPEUTIC_AGENT_DATA values (nextval('therapeutic_agent_data_seq'), " + id + " , " + metaStudyId + ", null) ";
		@SuppressWarnings("rawtypes")
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
	}
	
	public int deleteTherapeuticAgentData(MetaStudyTherapeuticAgent s, String metaStudyId) {
		Long id = s.getTherapeuticAgent().getId();
		String sqlString = "delete from THERAPEUTIC_AGENT_DATA where THERAPEUTIC_AGENT_id = " + id + " and meta_study_id = " + metaStudyId;
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
	}


	@Override
	public int saveTherapeuticAgentDataBulk(List<MetaStudyTherapeuticAgent> studyTherapeuticAgents, String studyId) {
		
		String sqlString = "";
		for(MetaStudyTherapeuticAgent studyTherapeuticAgent:studyTherapeuticAgents) {
			Long id = studyTherapeuticAgent.getTherapeuticAgent().getId();
			sqlString+= "insert into THERAPEUTIC_AGENT_DATA values (nextval('therapeutic_agent_data_seq'), " + id + " , " + studyId + ", null); ";
		}
		@SuppressWarnings("rawtypes")
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
		
	}


	@Override
	public int deleteTherapeuticAgentDataBulk(List<MetaStudyTherapeuticAgent> studyTherapeuticAgents, String studyId) {
		
		String sqlString = "";
		for(MetaStudyTherapeuticAgent studyTherapeuticAgent:studyTherapeuticAgents) {
			Long id = studyTherapeuticAgent.getTherapeuticAgent().getId();
			sqlString+= "delete from THERAPEUTIC_AGENT_DATA where THERAPEUTIC_AGENT_id = " + id + " and meta_study_id = " + studyId+ ";";
		}
		@SuppressWarnings("rawtypes")
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
		
	}
}

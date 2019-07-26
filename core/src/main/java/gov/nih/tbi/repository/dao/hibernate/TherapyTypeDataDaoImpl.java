
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
import gov.nih.tbi.repository.dao.TherapyTypeDataDao;
import gov.nih.tbi.repository.model.alzped.MetaStudyTherapeuticAgent;
import gov.nih.tbi.repository.model.alzped.MetaStudyTherapyType;
import gov.nih.tbi.repository.model.alzped.StudyTherapyType;
import gov.nih.tbi.repository.model.alzped.StudyTherapyType;
import gov.nih.tbi.repository.model.alzped.TherapyTypeData;

@Transactional("metaTransactionManager")
@Repository
public class TherapyTypeDataDaoImpl extends GenericDaoImpl<TherapyTypeData, Long> implements TherapyTypeDataDao
{

    @Autowired
    public TherapyTypeDataDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory)
    {

        super(TherapyTypeData.class, sessionFactory);
    }
    
	public int saveTherapyTypeData(StudyTherapyType studyTherapyType, String studyId) {
		Long id = studyTherapyType.getTherapyType().getId();
		String sqlString = "insert into therapy_type_data values (nextval('therapy_type_data_seq'), " + id + " , " + studyId + ", null) ";
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
	}
	
	public int deleteTherapyTypeData(StudyTherapyType s, String studyId) {
		Long id = s.getTherapyType().getId();
		String sqlString = "delete from therapy_type_DATA where therapy_type_id = " + id + " and study_id = " + studyId;
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
	}

	@Override
	public int saveTherapyTypeData(MetaStudyTherapyType metaStudyTherapyType, String metaStudyId) {
		Long id = metaStudyTherapyType.getTherapyType().getId();
		String sqlString = "insert into therapy_type_data values (nextval('therapy_type_data_seq'), " + id + " , " + metaStudyId + ", null) ";
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
	}

	@Override
	public int deleteTherapyTypeData(MetaStudyTherapyType s, String metaStudyId) {
		Long id = s.getTherapyType().getId();
		String sqlString = "delete from therapy_type_DATA where therapy_type_id = " + id + " and meta_study_id = " + metaStudyId;
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public int saveTherapyTypeDataBulk(List<MetaStudyTherapyType> metaStudyTherapyTypes, String metaStudyId) {
		
		String sqlString = "";
		for(MetaStudyTherapyType metaStudyTherapyType:metaStudyTherapyTypes) {
			Long id = metaStudyTherapyType.getTherapyType().getId();
			sqlString += "insert into therapy_type_data values (nextval('therapy_type_data_seq'), " + id + " , " + metaStudyId + ", null);";
		}
		
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public int deleteTherapyTypeDataBulk(List<MetaStudyTherapyType> metaStudyTherapyTypes, String metaStudyId) {
		
		String sqlString = "";
		for(MetaStudyTherapyType metaStudyTherapyType:metaStudyTherapyTypes) {
			Long id = metaStudyTherapyType.getTherapyType().getId();
			sqlString+= "delete from therapy_type_data where therapy_type_id = " + id + " and meta_study_id = " + metaStudyId+ ";";
		}

		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
		
	}
}

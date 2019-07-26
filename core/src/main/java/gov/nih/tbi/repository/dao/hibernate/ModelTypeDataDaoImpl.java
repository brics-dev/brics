
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
import gov.nih.tbi.repository.dao.ModelTypeDataDao;
import gov.nih.tbi.repository.model.alzped.MetaStudyModelType;
import gov.nih.tbi.repository.model.alzped.MetaStudyTherapeuticTarget;
import gov.nih.tbi.repository.model.alzped.ModelTypeData;
import gov.nih.tbi.repository.model.alzped.StudyModelType;
import gov.nih.tbi.repository.model.alzped.StudyModelType;

@Transactional("metaTransactionManager")
@Repository
public class ModelTypeDataDaoImpl extends GenericDaoImpl<ModelTypeData, Long> implements ModelTypeDataDao
{

    @Autowired
    public ModelTypeDataDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory)
    {

        super(ModelTypeData.class, sessionFactory);
    }
    
	public int saveModelTypeData(StudyModelType studyModelType, String studyId) {
		Long id = studyModelType.getModelType().getId();
		String sqlString = "insert into model_type_data values (nextval('model_type_data_seq'), " + id + " , " + studyId + ", null) ";
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
	}
	
	public int deleteModelTypeData(StudyModelType s, String studyId) {
		Long id = s.getModelType().getId();
		String sqlString = "delete from model_type_DATA where model_type_id = " + id + " and study_id = " + studyId;
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
	}
	public int saveModelTypeData(MetaStudyModelType metaStudyModelType, String metaStudyId) {
		Long id = metaStudyModelType.getModelType().getId();
		String sqlString = "insert into model_type_data values (nextval('model_type_data_seq'), " + id + " , " + metaStudyId + ", null) ";
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
	}
	
	public int deleteModelTypeData(MetaStudyModelType s, String metaStudyId) {
		Long id = s.getModelType().getId();
		String sqlString = "delete from model_type_DATA where model_type_id = " + id + " and meta_study_id = " + metaStudyId;
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public int saveModelTypeDataBulk(List<MetaStudyModelType> metaStudyModelTypes, String metaStudyId) {
		
		String sqlString = "";
		for(MetaStudyModelType metaStudyModelType:metaStudyModelTypes) {
			Long id = metaStudyModelType.getModelType().getId();
			sqlString+= "insert into model_type_data values (nextval('model_type_data_seq'), " + id + " , " + metaStudyId + ", null); ";
		}
		
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public int deleteModelTypeDataBulk(List<MetaStudyModelType> metaStudyModelTypes, String metaStudyId) {
		
		String sqlString = "";
		for(MetaStudyModelType metaStudyModelType:metaStudyModelTypes) {
			Long id = metaStudyModelType.getModelType().getId();
			sqlString+= "delete from model_type_data where model_type_id = " + id + " and meta_study_id = " + metaStudyId+ ";";
		}
		
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
	}
}

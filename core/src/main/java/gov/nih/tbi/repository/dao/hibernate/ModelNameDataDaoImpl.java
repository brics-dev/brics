
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
import gov.nih.tbi.repository.dao.ModelNameDataDao;
import gov.nih.tbi.repository.model.alzped.MetaStudyModelName;
import gov.nih.tbi.repository.model.alzped.ModelNameData;
import gov.nih.tbi.repository.model.alzped.StudyModelName;

@Transactional("metaTransactionManager")
@Repository
public class ModelNameDataDaoImpl extends GenericDaoImpl<ModelNameData, Long> implements ModelNameDataDao
{

    @Autowired
    public ModelNameDataDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory)
    {

        super(ModelNameData.class, sessionFactory);
    }
    
	public int saveModelNameData(StudyModelName studyModelName, String studyId) {
		Long id = studyModelName.getModelName().getId();
		String sqlString = "insert into model_name_data values (nextval('model_name_data_seq'), " + id + " , " + studyId + ", null) ";
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
	}
	
	public int deleteModelNameData(StudyModelName s, String studyId) {
		Long id = s.getModelName().getId();
		String sqlString = "delete from model_name_DATA where model_name_id = " + id + " and study_id = " + studyId;
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
	}

	@Override
	public int saveModelNameData(MetaStudyModelName studyModelName, String metaStudyId) {
		Long id = studyModelName.getModelName().getId();
		String sqlString = "insert into model_name_data values (nextval('model_name_data_seq'), " + id + " , " + metaStudyId + ", null) ";
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
	}

	@Override
	public int deleteModelNameData(MetaStudyModelName s, String metaStudyId) {
		Long id = s.getModelName().getId();
		String sqlString = "delete from model_name_DATA where model_name_id = " + id + " and meta_study_id = " + metaStudyId;
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public int saveModelNameDataBulk(List<MetaStudyModelName> studyModelNames, String metaStudyId) {
		
		String sqlString = "";
		for(MetaStudyModelName studyModelName:studyModelNames) {
			Long id = studyModelName.getModelName().getId();
			sqlString += "insert into model_name_data values (nextval('model_name_data_seq'), " + id + " , " + metaStudyId + ", null);";
		}
		
		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public int deleteModelNameDataBulk(List<MetaStudyModelName> studyModelNames, String metaStudyId) {
		
		String sqlString = "";
		for(MetaStudyModelName studyModelName:studyModelNames) {
			Long id = studyModelName.getModelName().getId();
			sqlString+= "delete from model_name_data where model_name_id = " + id + " and meta_study_id = " + metaStudyId+ ";";
		}

		NativeQuery idQuery = getSession().createNativeQuery(sqlString);
		return idQuery.executeUpdate();
	}
}

package gov.nih.tbi.repository.dao.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.hibernate.query.internal.NativeQueryImpl;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.repository.dao.MetaDataImportDao;


@Transactional("metaTransactionManager")
@Repository
public class MetaDataImportDaoImpl implements MetaDataImportDao{
	Logger logger = Logger.getLogger(MetaDataImportDaoImpl.class);
	
	private static final String DATASET_WITH_ERRORS_QUERY = "SELECT DISTINCT name, submit_date from dataset WHERE (submit_date > (CURRENT_TIMESTAMP -  interval '24 hours'))" 
			+ "and dataset_status_id in (5,6);";
	
	private SessionFactory sessionFactory;
	
	@Autowired
	public MetaDataImportDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory factory) {

		this.sessionFactory = factory;
	}
	
	private Session getSession() {
		return this.sessionFactory.getCurrentSession();
	}
	public List<String> getDatasetsInError(){
		
		List<String> datasetsAndDates  = new ArrayList();
		Query query = getSession().createNativeQuery(DATASET_WITH_ERRORS_QUERY);
		((NativeQueryImpl) query).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

		List<Map<String, Object>> results = query.list();
		
		for(Map<String, Object> result: results) {
			datasetsAndDates.add(result.get("name")+ "|" + result.get("submit_date"));
		}
		return datasetsAndDates;
	}
}

package gov.nih.tbi.repository.dao.jdbc;

import java.text.MessageFormat;
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
import gov.nih.tbi.repository.dao.ProformsDataImportDao;
import gov.nih.tbi.repository.dataimport.AdministeredFormProcessingInfo;
import gov.nih.tbi.repository.dataimport.DataImportDataElementData;

@Transactional("proformsTransactionManager")
@Repository
public class ProformsDataImportDaoImpl implements ProformsDataImportDao {

	Logger logger = Logger.getLogger(ProformsDataImportDaoImpl.class);

	private SessionFactory sessionFactory;

	private static final String DISTINCT_DS_STUDY_QUERY = "SELECT DISTINCT data_structure_name, brics_studyid, administeredformid, finallockdate "
			+ "FROM datasubmission_view WHERE brics_studyid IS NOT NULL ";

	private static final String FINAL_LOCK_BY_USER_QUERY = "SELECT finallockby from administeredform where administeredformid = {0}";

	private static final String GET_BRICS_USER_ID = "SELECT brics_userid from usr WHERE usrid = {0}";
	
	private static final String DEFAULT_BRICS_USER_ID = "-1";

	private static final String GET_ALL_GROUP_DATA_ELEMENTS_QUERY = "SELECT DISTINCT groupdataelement FROM datasubmission_view WHERE "
			+ "administeredFormId = {0}";

	private static final String GET_ALL_MULTI_SELECT_DATA_ELEMENTS_QUERY = "SELECT DISTINCT groupdataelement from datasubmission_view "
			+ "WHERE multi_select_question = ''t'' AND administeredFormId = {0}";

	private static final String DATA_SUBMISSION_VIEW_QUERY = "SELECT * FROM datasubmission_view WHERE administeredformid = {0}";

	private static final String UPDATE_ADMIN_FORM_STATUS_QUERY = "update administeredform set xsubmissionstatusid = {0} WHERE administeredformid = {1};";

	private static final String DELETE_ADMIN_FORM_FROM_DATA_SUBMISSION_TABLE_QUERY = "DELETE FROM datasubmission where administeredformid = {0};";

	private static final String GET_FINAL_LOCK_DATE = "SELECT finallockdate from administeredform where administeredformid = {0}";

	@Autowired
	public ProformsDataImportDaoImpl(@Qualifier(CoreConstants.PF_FACTORY) SessionFactory factory) {

		this.sessionFactory = factory;
	}

	private Session getSession() {
		return this.sessionFactory.getCurrentSession();
	}

	public List<AdministeredFormProcessingInfo> getAllAdminForms() {

		List<AdministeredFormProcessingInfo> adminForms = new ArrayList();
		Query query = getSession().createNativeQuery(DISTINCT_DS_STUDY_QUERY);
		((NativeQueryImpl) query).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

		List<Map<String, Object>> results = query.list();

		for (Map<String, Object> adminFormInfo : results) {
			adminForms.add(new AdministeredFormProcessingInfo(adminFormInfo));
		}
		return adminForms;
	}

	public String getFinalLockByUser(String adminFormId) {
		String lockByUser = "-1";
		Query query = getSession().createNativeQuery(MessageFormat.format(FINAL_LOCK_BY_USER_QUERY, adminFormId));
		((NativeQueryImpl) query).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

		List<Map<String, Object>> results = query.list();
		if (!results.isEmpty()) {
			if (results.size() > 1) {
				logger.error("Multiple lock by users found for admin form: " + adminFormId);
			} else {
				if (results.get(0).containsKey("finallockby")) {
					return getBricsUser(results.get(0).get("finallockby").toString());
				}
			}
		}
		return lockByUser;
	}

	public String getFinalLockDate(String adminFormId) {
		String lockDate = "";
		Query query = getSession().createNativeQuery(MessageFormat.format(GET_FINAL_LOCK_DATE, adminFormId));
		((NativeQueryImpl) query).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

		List<Map<String, Object>> results = query.list();
		for (Map<String, Object> obj : results) {
			lockDate = obj.get("finallockdate").toString();
		}

		return lockDate;
	}

	public String getBricsUser(String lockByUser) {
		String bricsUserId = "";
		Query query = getSession().createNativeQuery(MessageFormat.format(GET_BRICS_USER_ID, lockByUser));
		((NativeQueryImpl) query).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

		List<Map<String, Object>> results = query.list();
		for (Map<String, Object> obj : results) {
			if(obj.get("brics_userid") == null) {
				return DEFAULT_BRICS_USER_ID;
			}
			bricsUserId = obj.get("brics_userid").toString();
		}
		
		return bricsUserId;
	}

	public List<String> getGroupDataElements(String adminFormId) {
		List<String> groupDataElements = new ArrayList<>();
		Query query = getSession()
				.createNativeQuery(MessageFormat.format(GET_ALL_GROUP_DATA_ELEMENTS_QUERY, adminFormId));
		((NativeQueryImpl) query).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

		List<Map<String, Object>> results = query.list();
		for (Map<String, Object> groupDataElement : results) {
			groupDataElements.add(groupDataElement.get("groupdataelement").toString());
		}

		return groupDataElements;
	}

	public List<String> getMultiSelectGroupDataElements(String adminFormId) {
		List<String> multiSelectGroupDataElements = new ArrayList<>();
		Query query = getSession()
				.createNativeQuery(MessageFormat.format(GET_ALL_MULTI_SELECT_DATA_ELEMENTS_QUERY, adminFormId));
		((NativeQueryImpl) query).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

		List<Map<String, Object>> results = query.list();
		for (Map<String, Object> groupDataElement : results) {
			multiSelectGroupDataElements.add(groupDataElement.get("groupdataelement").toString());
		}

		return multiSelectGroupDataElements;
	}

	public List<DataImportDataElementData> getDataFromDataSubmissionView(String adminFormId) {
		List<DataImportDataElementData> list = new ArrayList<>();
		Query query = getSession().createNativeQuery(MessageFormat.format(DATA_SUBMISSION_VIEW_QUERY, adminFormId));
		((NativeQueryImpl) query).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

		List<Map<String, Object>> results = query.list();
		for (Map<String, Object> groupDataElement : results) {
			list.add(new DataImportDataElementData(groupDataElement));

		}
		return list;
	}

	public void updateAdminFormSubmissionStatus(int status, String adminFormId) {
		Query query = getSession()
				.createNativeQuery(MessageFormat.format(UPDATE_ADMIN_FORM_STATUS_QUERY, status, adminFormId));
		((NativeQueryImpl) query).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		query.executeUpdate();
	}

	public void deleteAdminFormFromDataSubmissionTable(String adminFormId) {
		Query query = getSession()
				.createNativeQuery(MessageFormat.format(DELETE_ADMIN_FORM_FROM_DATA_SUBMISSION_TABLE_QUERY, adminFormId));
		((NativeQueryImpl) query).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
		
		query.executeUpdate();
	}
}

package gov.nih.tbi.repository.dao.hibernate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

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
import gov.nih.tbi.repository.dao.RepositoryDao;
import gov.nih.tbi.repository.model.GenericTable;
import gov.nih.tbi.repository.model.GenericTableRow;
import gov.nih.tbi.repository.model.hibernate.DataStoreInfo;
import gov.nih.tbi.repository.model.hibernate.DataStoreTabularColumnInfo;
import gov.nih.tbi.repository.model.hibernate.DataStoreTabularInfo;
import gov.nih.tbi.repository.model.hibernate.SubmissionRecordJoin;

/**
 * Dao implementation to store general information about the data in the repository
 * 
 * 
 * 
 */
@Repository
@Transactional("repositoryTransactionManager")
public class RepositoryDaoImpl implements RepositoryDao {

	static Logger log = Logger.getLogger(RepositoryDaoImpl.class);
	private static final String COLUMN_NAME_SUBMISSION_RECORD_JOIN_ID = "submission_record_join_id";
	private static final String COLUMN_NAME_SUBMISSION_RECORD_JOIN = "submission_record_join";
	private static final String COLUMN_NAME_DATASETID = "dataset_id";
	private static final String TABLE_NAME_SUBMISSION_RECORD_JOIN = "submission_record_join";

	private SessionFactory sessionFactory;

	@Autowired
	public RepositoryDaoImpl(@Qualifier(CoreConstants.REPOS_FACTORY) SessionFactory factory) {

		this.sessionFactory = factory;
	}

	private Session getSession() {
		return this.sessionFactory.getCurrentSession();
	}

	public SubmissionRecordJoin getSubmissionRecordJoin(Long id) {

		CriteriaBuilder cb = getSession().getCriteriaBuilder();
		CriteriaQuery<SubmissionRecordJoin> query = cb.createQuery(SubmissionRecordJoin.class);

		Root<SubmissionRecordJoin> root = query.from(SubmissionRecordJoin.class);
		query.where(cb.equal(root.get("id"), id));

		Query<SubmissionRecordJoin> q = getSession().createQuery(query.distinct(true));

		try {
			return q.getSingleResult();
		} catch (NoResultException ex) {
			return null;
		}
	}


	public Map<String, String> generateDataStoreInfoQuery(DataStoreInfo dsInfo, Integer limit, Long datasetId) {

		Map<String, String> queries = new HashMap<String, String>();
		StringBuffer sb = new StringBuffer();

		for (DataStoreTabularInfo tabInfo : dsInfo.getDataStoreTabularInfos()) {
			sb.append("SELECT *\n").append("FROM ").append(tabInfo.getTableName());

			log.info(sb.toString());
			queries.put(tabInfo.getTableName(), sb.toString());
		}

		return queries;
	}

	/**
	 * {@inheritDoc}
	 */
	public String generateJoinDataStoreInfoQuery(DataStoreInfo dsInfo, Integer limit, Long datasetId) {

		String queryStringSelect = "";
		String queryStringFrom = " FROM ";
		String queryStringWhere = " WHERE ";
		Boolean isFirstTable = true;

		// Using " " for table names and column names because apparently they both may contain spaces
		// Also aliasing with the rg table name so that when we get the result set we know which column belongs to which
		// rg after the join
		// After aliasing table / column names casing matters so must lower case them all.
		for (DataStoreTabularInfo tabInfo : dsInfo.getDataStoreTabularInfos()) {

			String currentJoinTable = tabInfo.getTableName().toLowerCase();

			// Check to see if this is the first table join
			if (isFirstTable) {
				isFirstTable = false;

				queryStringFrom = queryStringFrom + "\"" + TABLE_NAME_SUBMISSION_RECORD_JOIN + "\" AS \""
						+ TABLE_NAME_SUBMISSION_RECORD_JOIN + "\"";

				queryStringWhere = queryStringWhere + "\"" + currentJoinTable + "\".\"" + "id" + "\" is not null ";

			} else {
				queryStringWhere = queryStringWhere + "OR \"" + currentJoinTable + "\".\"" + "id" + "\" is not null ";
			}

			// Build select portion of query
			for (DataStoreTabularColumnInfo column : tabInfo.getColumnInfos()) {
				if (queryStringSelect.isEmpty() || queryStringSelect.equals("")) {
					queryStringSelect = "SELECT \"" + currentJoinTable + "\".\"" + column.getColumnName().toLowerCase()
							+ "\" AS \"" + currentJoinTable + "." + column.getColumnName().toLowerCase() + "\" ";
				} else {
					queryStringSelect = queryStringSelect + ", \"" + currentJoinTable + "\".\""
							+ column.getColumnName().toLowerCase() + "\" AS \"" + currentJoinTable + "."
							+ column.getColumnName().toLowerCase() + "\" ";
				}
			}

			// Build query portion FROM on with joins
			queryStringFrom = queryStringFrom + " LEFT JOIN \"" + currentJoinTable + "\" AS \"" + currentJoinTable
					+ "\" ON \"" + TABLE_NAME_SUBMISSION_RECORD_JOIN + "\".\"" + "id" + "\" = \"" + currentJoinTable
					+ "\".\"" + COLUMN_NAME_SUBMISSION_RECORD_JOIN_ID + "\" ";

			if (datasetId != null) {
				queryStringFrom = queryStringFrom + " AND \"" + TABLE_NAME_SUBMISSION_RECORD_JOIN + "\".\""
						+ COLUMN_NAME_DATASETID + "\" = " + datasetId + " ";
			}
		}

		// if there are no de's in the form just make a correct sql statement
		if (queryStringSelect.isEmpty() || queryStringSelect.equals("")) {
			queryStringSelect = "SELECT * ";
		}

		// Add Dataset Id to query, join to submission_record_join table
		if (!isFirstTable) {

			queryStringSelect =
					queryStringSelect + ", \"" + TABLE_NAME_SUBMISSION_RECORD_JOIN + "\".\"" + COLUMN_NAME_DATASETID
							+ "\" AS \"" + COLUMN_NAME_DATASETID + "\" " + ", \"" + COLUMN_NAME_SUBMISSION_RECORD_JOIN
							+ "\"" + ".\"id\" AS \"" + COLUMN_NAME_SUBMISSION_RECORD_JOIN + "\"";
		}

		String limitString = " LIMIT " + limit;
		String queryString = queryStringSelect + queryStringFrom + queryStringWhere + limitString;
		log.debug("This is the Join Data Store Inf oQuery " + queryString);

		return queryString;
	}

	/**
	 * @inheritDoc
	 */
	/**
	 * @inheritDoc
	 */
	public Map<String, GenericTable> queryByDataStoreInfo(DataStoreInfo dsInfo) {

		Map<String, GenericTable> tableMap = new HashMap<String, GenericTable>();

		for (DataStoreTabularInfo tabInfo : dsInfo.getDataStoreTabularInfos()) {
			StringBuffer sb = new StringBuffer();
			GenericTable newTable = new GenericTable(tabInfo.getTableName());

			sb.append("SELECT ")
					.append(tabInfo.getTableName() + ".*, " + CoreConstants.RECORD_JOIN_TABLE + ".dataset_id FROM ")
					.append(tabInfo.getTableName()).append(" LEFT OUTER JOIN ").append(CoreConstants.RECORD_JOIN_TABLE)
					.append(" ON ").append(tabInfo.getTableName()).append(".submission_record_join_id = ")
					.append(CoreConstants.RECORD_JOIN_TABLE).append(".id");

			String queryString = sb.toString();

			Query query = getSession().createNativeQuery(queryString);
			((NativeQueryImpl) query).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

			List<Map<String, Object>> results = query.list();

			for (Map<String, Object> row : results) {
				GenericTableRow newRow = new GenericTableRow();
				newTable.addRow(newRow);

				for (String columnName : row.keySet()) {
					newRow.addCell(columnName, row.get(columnName));
				}
			}

			tableMap.put(tabInfo.getTableName(), newTable);
		}

		return tableMap;
	}

	public GenericTable queryByDataStoreTabInfo(DataStoreTabularInfo tabInfo, List<Long> datasetIds) {
		StringBuffer sb = new StringBuffer();
		String tableName = tabInfo.getTableName();
		GenericTable newTable = new GenericTable(tableName);

		// need to do left outer join to submission record join to get the dataset id's
		sb.append("SELECT ").append(tableName + ".*, " + CoreConstants.RECORD_JOIN_TABLE + ".dataset_id FROM ")
				.append(tableName).append(" LEFT OUTER JOIN ").append(CoreConstants.RECORD_JOIN_TABLE)
				.append(" ON ").append(tableName).append(".submission_record_join_id = ")
				.append(CoreConstants.RECORD_JOIN_TABLE).append(".id").append(" WHERE ")
				.append(CoreConstants.RECORD_JOIN_TABLE).append(".dataset_id IN (");

		for (Long datasetId : datasetIds) {
			sb.append(datasetId).append(",");
		}

		sb = sb.replace(sb.length() - 1, sb.length(), ")");

		sb.append(" ORDER BY (").append(tableName).append(".id)");

		// log.trace(sb.toString());
		String queryString = sb.toString();
		Query query = getSession().createNativeQuery(queryString);
		((NativeQueryImpl) query).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

		List<Map<String, Object>> results = query.list();
		log.info("Results Count for " + tableName + ": " + results.size());

		for (Map<String, Object> row : results) {
			GenericTableRow newRow = new GenericTableRow();
			newTable.addRow(newRow);

			for (String columnName : row.keySet()) {
				newRow.addCell(columnName, row.get(columnName));
			}
		}

		return newTable;
	}

	/**
	 * @inheritDoc
	 */
	public Map<String, GenericTable> queryByDataStoreInfo(DataStoreInfo dsInfo, List<Long> datasetIds) {

		Map<String, GenericTable> tableMap = new HashMap<String, GenericTable>();

		// return the empty table when there are no non-archived datasets in the DB
		if (datasetIds == null || datasetIds.isEmpty()) {
			return tableMap;
		}

		for (DataStoreTabularInfo tabInfo : dsInfo.getDataStoreTabularInfos()) {
		    GenericTable newTable = queryByDataStoreTabInfo(tabInfo, datasetIds);
			tableMap.put(tabInfo.getTableName(), newTable);
		}

		return tableMap;
	}

	@SuppressWarnings({"rawtypes"})
	public List joinDataStoreInfo(DataStoreInfo dsInfo, Integer limit) {

		String queryString = generateJoinDataStoreInfoQuery(dsInfo, limit, null);
		Query query = getSession().createNativeQuery(queryString);
		((NativeQueryImpl) query).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

		List results = query.list();
		log.debug("Results Count: " + results.size());
		return results;
	}

}

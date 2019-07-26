
package gov.nih.tbi.repository.dao.hibernate;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.repository.dao.DataStoreTabularInfoDao;
import gov.nih.tbi.repository.model.hibernate.DataStoreInfo;
import gov.nih.tbi.repository.model.hibernate.DataStoreTabularInfo;

/**
 * Dao implementation to perform basic operations on Data Store Table Information
 * 
 * @author dhollo
 * 
 */
@Transactional("metaTransactionManager")
@Repository
public class DataStoreTabularInfoDaoImpl extends GenericDaoImpl<DataStoreTabularInfo, Long> implements DataStoreTabularInfoDao {

	@Autowired
	public DataStoreTabularInfoDaoImpl(@Qualifier(CoreConstants.META_FACTORY) SessionFactory sessionFactory) {

		super(DataStoreTabularInfo.class, sessionFactory);
	}

	public DataStoreTabularInfo get(DataStoreInfo storeInfo, RepeatableGroup repeatableGroup) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<DataStoreTabularInfo> query = cb.createQuery(DataStoreTabularInfo.class);

		Root<DataStoreTabularInfo> root = query.from(persistentClass);
		query.where(cb.and(cb.equal(root.get("dataStoreInfo"), storeInfo),
				cb.equal(root.get("repeatableGroupId"), repeatableGroup.getId()))).distinct(true);

		return getUniqueResult(query);
	}

}

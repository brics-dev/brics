package gov.nih.tbi.account.dao.hibernate;

import java.util.List;

import javax.persistence.Query;

import org.hibernate.SessionFactory;
import org.hibernate.query.internal.NativeQueryImpl;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.account.model.hibernate.EntityMap;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.account.dao.VisualizationEntityMapDao;
import gov.nih.tbi.account.model.hibernate.VisualizationEntityMap;

@Transactional("metaTransactionManager")
@Repository
public class VisualizationEntityMapDaoImpl  extends GenericDaoImpl< VisualizationEntityMap, Long > implements  VisualizationEntityMapDao {

	@Autowired
	public VisualizationEntityMapDaoImpl(@Qualifier(CoreConstants.ACCOUNT_FACTORY) SessionFactory sessionFactory) {

		super(VisualizationEntityMap.class, sessionFactory);
	}
	
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<VisualizationEntityMap> getUserGrantedEntities(Long accountId, Long typeId) {
			String hql = "SELECT a.user_name as \"userName\", a.id as \"accountId\", e.type_id as \"typeId\", e.entity_id as \"entityId\", e.permission_type_id as \"permissionTypeId\", p.group_name as \"groupName\", m.permission_group_status_id as \"permissionGroupStatusId\", m.id as \"permissionGroupMemberId\" from account a " 
					+ "left join permission_group_member m on m.account_id = a.id "
					+ "left join permission_group p on p.id = m.permission_group_id " 
					+ "left join entity_map e on e.account_id = a.id "
					+ "where (a.id = ? and e.type_id = ?);";

		Query query = getSession().createNativeQuery(hql);
		query.setParameter(1, accountId);
		query.setParameter(2, typeId);
		((NativeQueryImpl) query).setResultTransformer(Transformers.aliasToBean(VisualizationEntityMap.class));
		List<VisualizationEntityMap> list = query.getResultList();

		return list;
		

	}
}

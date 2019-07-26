package gov.nih.tbi.account.dao.hibernate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.account.dao.AccountMessageTemplateDao;
import gov.nih.tbi.account.model.AccountMessageTemplateType;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountMessageTemplate;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.commons.model.AccountStatus;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.ordermanager.model.BiospecimenOrder;


@Transactional("metaTransactionManager")
@Repository
public class AccountMessageTemplateDaoImpl extends GenericDaoImpl<AccountMessageTemplate, Long> implements AccountMessageTemplateDao {

	
	public AccountMessageTemplateDaoImpl(@Qualifier(CoreConstants.ACCOUNT_FACTORY) SessionFactory sessionFactory) {
	     super(AccountMessageTemplate.class, sessionFactory);
	}

	@Override
	public List<AccountMessageTemplate> getAccountMessageTemplateListByType(AccountMessageTemplateType accountMessageTemplateType) {
	
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<AccountMessageTemplate> query = cb.createQuery(AccountMessageTemplate.class);
		Root<AccountMessageTemplate> root = query.from(AccountMessageTemplate.class);
		
		Predicate predicate = cb.conjunction();
		predicate = cb.and(predicate,cb.equal(root.get("accountMessageTemplateType"), accountMessageTemplateType));
		
		query.where(predicate).distinct(true);
		
		List<AccountMessageTemplate> accountMessageTemplates = createQuery(query).getResultList();;
		return accountMessageTemplates;
		
	}

	@Override
	public Map<Long, AccountMessageTemplate> getAccountMessageTemplateListByIds(List<Long> ids) {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<AccountMessageTemplate> query = cb.createQuery(AccountMessageTemplate.class);
		Root<AccountMessageTemplate> root = query.from(persistentClass);
		
		query.where(root.get("id").in(ids)).distinct(true);
		List<AccountMessageTemplate> accountMessageTemplates = createQuery(query).getResultList();
		
		Map<Long,AccountMessageTemplate> accountMessageTemplateMap = new HashMap<Long,AccountMessageTemplate>();
		
		for(AccountMessageTemplate accountMessageTemplate:accountMessageTemplates){
			accountMessageTemplateMap.put(accountMessageTemplate.getId(), accountMessageTemplate);
		}
		
		return accountMessageTemplateMap;
		
	}
	
	public List<AccountMessageTemplate> getgAccountMessageTemplateEmptyMsgListByType(AccountMessageTemplateType accountMessageTemplateType) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<AccountMessageTemplate> query = cb.createQuery(AccountMessageTemplate.class);
		Root<AccountMessageTemplate> root = query.from(AccountMessageTemplate.class);
		
		Predicate predicate = cb.conjunction();
		predicate = cb.and(predicate,cb.equal(root.get("accountMessageTemplateType"), accountMessageTemplateType));
		predicate = cb.and(predicate, cb.equal(root.get("message"), CoreConstants.EMPTY_STRING));
		
		query.where(predicate).distinct(true);
		
		List<AccountMessageTemplate> accountMessageTemplates = createQuery(query).getResultList();;
		return accountMessageTemplates;
	}

}

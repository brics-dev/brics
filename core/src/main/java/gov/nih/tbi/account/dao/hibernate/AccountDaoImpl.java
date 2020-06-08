
package gov.nih.tbi.account.dao.hibernate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.log4j.Logger;
import org.hibernate.Hibernate;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.tbi.CoreConstants;
import gov.nih.tbi.account.dao.AccountDao;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.BasicAccount;
import gov.nih.tbi.account.model.hibernate.LoginAudit;
import gov.nih.tbi.account.model.hibernate.PermissionGroupMember;
import gov.nih.tbi.commons.dao.hibernate.GenericDaoImpl;
import gov.nih.tbi.commons.model.AccountStatus;
import gov.nih.tbi.commons.model.RoleStatus;
import gov.nih.tbi.commons.model.RoleType;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.commons.util.PaginationData;

@Transactional("metaTransactionManager")
@Repository
public class AccountDaoImpl extends GenericDaoImpl<Account, Long> implements AccountDao {

	private static Logger logger = Logger.getLogger(AccountDaoImpl.class);

	@Autowired
	public AccountDaoImpl(@Qualifier(CoreConstants.ACCOUNT_FACTORY) SessionFactory sessionFactory) {
		super(Account.class, sessionFactory);
	}
	
	@Override
	public Account get(Long id) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Account> query = cb.createQuery(persistentClass);

		Root<Account> root = query.from(persistentClass);
		query.where(cb.equal(root.get("id"), id)).distinct(true);
		root.fetch("accountReportingLog", JoinType.LEFT);
		
		Account account = getUniqueResult(query);
		if (account != null) {
			account.getAccountHistory().size();
			
			for (PermissionGroupMember pgm : account.getPermissionGroupMemberList()) {
				pgm.getPermissionGroup().toString();
			}
		}

		return account;
	}

	
	public List<Account> getAccountList() {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Account> query = cb.createQuery(persistentClass);

		Root<Account> root = query.from(persistentClass);
		query.select(cb.construct(Account.class, root.get("id"), root.get("userName"), root.get("user"),
				root.get("accountStatus"), root.get("affiliatedInstitution"), root.get("requestSubmitDate"),
				root.get("lastUpdatedDate"), root.get("applicationDate")));
		
		Query<Account> q = createQuery(query.distinct(true));
		return q.getResultList();
	}
	
	/**
	 * @inheritDoc
	 */
	public List<Account> getAllActive() {
		List<Account> activeAccounts = new ArrayList<Account>();

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Account> query = cb.createQuery(persistentClass);

		Root<Account> root = query.from(persistentClass);
		
		// Do not return the "anonymous" account.
		query.where(cb.and(cb.equal(root.get("isActive"), true),
				cb.notEqual(root.get("userName"), CoreConstants.UNAUTHENTICATED_USER)));

		Query<Account> q = createQuery(query.distinct(true));
		for (Account a : q.getResultList()) {
			activeAccounts.add(a);
		}

		return activeAccounts;
	}

	/**
	 * @inheritDoc
	 */
	public Account changePassword(Long id, byte[] password) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Account> query = cb.createQuery(persistentClass);

		Root<Account> root = query.from(persistentClass);
		query.where(cb.equal(root.get("id"), id)).distinct(true);

		Account account = getUniqueResult(query);

		// set next password expiration
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_YEAR, CoreConstants.EXPIRE_ACCOUNT_AFTER_DAYS);
		logger.debug(account.getUserName() + "'s next password change must be before " + cal.getTime());

		// set the new password and save
		account.setPasswordExpirationDate(cal.getTime());
		account.setPassword(password);
		return save(account);
	}

	/**
	 * {@inheritDoc}
	 */
	public Account getByUserName(String userName) {
		
		userName = userName.replace(CoreConstants.UNDERSCORE, CoreConstants.SQL_ESCAPED_UNDERSCORE)
				.replace(CoreConstants.PERIOD, CoreConstants.SQL_ESCAPED_PERIOD);
		logger.debug("getByUserName: " + userName);

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Account> query = cb.createQuery(Account.class);

		Root<Account> root = query.from(persistentClass);
		query.where(cb.like(cb.upper(root.get("userName")), userName.toUpperCase())).distinct(true);

		Account account = getUniqueResult(query);
		if (account != null) {
			for (PermissionGroupMember pgm : account.getPermissionGroupMemberList()) {
				pgm.getPermissionGroup().toString();
			}
		}

		return account;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<Account> getByUserNameList(List<String> usernames) {
		List<Account> accounts = new ArrayList<Account>();

		// Ensure the usernames are trimmed of leading and trailing whitespace characters.
		for (ListIterator<String> it = usernames.listIterator(); it.hasNext();) {
			String userName = it.next();
			userName = userName.trim();
			it.set(userName);
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Account> query = cb.createQuery(Account.class);

		Root<Account> root = query.from(persistentClass);
		query.where(root.get("userName").in(usernames)).distinct(true);

		Query<Account> q = createQuery(query);

		// Perform some transformations and populate the final account list.
		for (Account a : q.getResultList()) {
			for (PermissionGroupMember pgm : a.getPermissionGroupMemberList()) {
				pgm.getPermissionGroup().toString();
			}
			accounts.add(a);
		}

		return accounts;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Account getByUserUPN(String userUPN) {

		logger.debug("getByUserUPN: " + userUPN);

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Account> query = cb.createQuery(Account.class);

		Root<Account> root = query.from(persistentClass);
		query.where(cb.equal(cb.upper(root.get("eraId")), userUPN.toUpperCase())).distinct(true);

		Account account = getUniqueResult(query);
		if (account != null) {
			for (PermissionGroupMember pgm : account.getPermissionGroupMemberList()) {
				pgm.getPermissionGroup().toString();
			}
		}

		return account;
	}

	/**
	 * @inheritDoc
	 */
	public Account getByUser(User user) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Account> query = cb.createQuery(Account.class);

		Root<Account> root = query.from(persistentClass);
		query.where(cb.equal(root.get("user"), user)).distinct(true);

		Account account = getUniqueResult(query);
		if (account != null) {
			for (PermissionGroupMember pgm : account.getPermissionGroupMemberList()) {
				pgm.getPermissionGroup().toString();
			}
		}

		return account;
	}

	/**
	 * @inheritDoc
	 */
	public Account getByEmail(String email) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Account> query = cb.createQuery(Account.class);

		Root<Account> root = query.from(persistentClass);
		Join<Account, User> accUser = root.join("user", JoinType.LEFT);

		query.where(cb.equal(cb.upper(accUser.get("email")), email.toUpperCase()));
		query.distinct(true);

		Account account = getUniqueResult(query);
		if (account != null) {
			for (PermissionGroupMember pgm : account.getPermissionGroupMemberList()) {
				pgm.getPermissionGroup().toString();
			}
		}

		return account;
	}

	/**
	 * @inheritDoc
	 */
	public List<String> getAllUserNames() {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<String> query = cb.createQuery(String.class);

		Root<Account> root = query.from(Account.class);
		query.select(root.get(CoreConstants.USERNAME));
		query.where(cb.notEqual(root.get("userName"), CoreConstants.UNAUTHENTICATED_USER));

		List<String> out = createQuery(query).getResultList();
		return out;
	}

	/**
	 * @inheritDoc
	 */
	public List<BasicAccount> search(String key, AccountStatus status, PaginationData pageData) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<BasicAccount> query = cb.createQuery(BasicAccount.class);
		Root<BasicAccount> root = query.from(BasicAccount.class);

		Subquery<Long> subquery = query.subquery(Long.class);
		Root<Account> accRoot = subquery.from(Account.class);
		Join<Account, User> userJoin = accRoot.join("user", JoinType.LEFT);
		Predicate accPredicate = cb.conjunction();

		if (key != null) {
			key = CoreConstants.WILDCARD + key.toUpperCase() + CoreConstants.WILDCARD;
			Predicate keyPredicate =
					cb.or(cb.like(cb.upper(accRoot.get("interestInTbi")), key),
							cb.like(cb.upper(userJoin.get("firstName")), key),
							cb.like(cb.upper(userJoin.get("lastName")), key),
							cb.like(cb.upper(userJoin.get("email")), key),
							cb.like(cb.upper(accRoot.get("adminNote")), key),
							cb.like(cb.upper(accRoot.get("affiliatedInstitution")), key),
							cb.like(cb.upper(accRoot.get("userName")), key));

			accPredicate = cb.and(accPredicate, keyPredicate);
		}

		// Add the status filter
		if (status != null) {
			Predicate statusPredicate = null;

			// If active, then filter by active and pending
			if (status.equals(AccountStatus.ACTIVE)) {
				statusPredicate = cb.or(cb.equal(accRoot.get("accountStatus"), AccountStatus.ACTIVE),
						cb.equal(accRoot.get("accountStatus"), AccountStatus.CHANGE_REQUESTED));
			} else {
				statusPredicate = cb.equal(accRoot.get("accountStatus"), status);
			}

			accPredicate = cb.and(accPredicate, statusPredicate);
		}

		subquery.select(accRoot.get("id")).distinct(true);
		subquery.where(accPredicate);

		query.where(cb.and(root.get("id").in(subquery),
				cb.notEqual(root.get("userName"), CoreConstants.UNAUTHENTICATED_USER)));

		// Pagination block
		if (pageData != null) {
			// get the count of distinct IDs only to compute the total results before pagination
			CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
			Root<BasicAccount> countRoot = countQuery.from(BasicAccount.class);
			countQuery.select(cb.countDistinct(countRoot.get("id")));
			countQuery.where(countRoot.get("id").in(subquery));

			long count = getUniqueResult(countQuery);
			pageData.setNumSearchResults((int) count);

			// Add Sorting
			if (pageData.getSort() != null && !pageData.getSort().equals("null")) {
				Path<Object> orderPath = root.get(pageData.getSort());
				query.orderBy(pageData.getAscending() ? cb.asc(orderPath) : cb.desc(orderPath));
				
			} else {
				query.orderBy(cb.asc(root.get("userName")));
			}
		}

		Query<BasicAccount> q = createQuery(query.distinct(true));

		// Add Pagination
		if (pageData != null && pageData.getPage() != null) {
			q.setMaxResults(pageData.getPageSize()).setFirstResult(pageData.getPageSize() * (pageData.getPage() - 1));
		}

		List<BasicAccount> list = q.getResultList();
		return list;
	}

	/**
	 * @inheritDoc
	 */
	public Long getStatusCount(AccountStatus status) {

		if (status == null) {
			return null;
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);

		Root<BasicAccount> root = query.from(BasicAccount.class);
		query.select(cb.countDistinct(root.get("id")));

		query.where(cb.and(cb.equal(root.get("accountStatus"), status),
				cb.notEqual(root.get("userName"), CoreConstants.UNAUTHENTICATED_USER)));

		return getUniqueResult(query);
	}

	/**
	 * @inheritDoc
	 */
	public List<String> getAffiliatedInstitutions() {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<String> query = cb.createQuery(String.class);

		Root<BasicAccount> root = query.from(BasicAccount.class);
		query.select(root.get("affiliatedInstitution"));

		Query<String> q = createQuery(query.distinct(true));
		return q.getResultList();
	}

	/**
	 * @inheritDoc
	 */
	@Override
	public List<BasicAccount> getAccountsByEraId(String eraId) {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<BasicAccount> query = cb.createQuery(BasicAccount.class);
		Root<BasicAccount> root = query.from(BasicAccount.class);

		query.where(cb.equal(cb.upper(root.get("eraId")), eraId.toUpperCase())).distinct(true);
		List<BasicAccount> out = createQuery(query).getResultList();

		return out;
	}

	@Override
	public List<Account> getAccountsWithRoles(Set<RoleType> roles, boolean onlyActive) {

		// convert the roles into their ids
		Set<Long> roleIds = new HashSet<Long>();
		for (RoleType r : roles) {
			roleIds.add(r.getId());
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Account> query = cb.createQuery(Account.class);
		Root<Account> root = query.from(Account.class);

		Predicate predicate = cb.conjunction();
		if (onlyActive == true) {
			predicate = cb.and(predicate, cb.equal(root.get("isActive"), true));
		}

		// Do not return the "anonymous" account.
		predicate = cb.and(predicate, cb.notEqual(root.get("userName"), CoreConstants.UNAUTHENTICATED_USER));

		predicate = cb.and(predicate, root.join("accountRoleList", JoinType.LEFT).get("roleType").in(roles));

		query.where(predicate).distinct(true);
		List<Account> out = createQuery(query).getResultList();
		return out;
	}

	@Override
	public void auditSuccessfulLogin(Long accountId, Date eventDate) {

		LoginAudit loginAudit = new LoginAudit(accountId, eventDate);
		getSession().persist(loginAudit);
	}

	@Override
	public List<Account> getApprovedAndPendignAccounts() {

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Account> query = cb.createQuery(Account.class);
		Root<Account> root = query.from(Account.class);

		// add filter null lastSuccessfulLogin to return only limited accounts that we are interested to delete
		query.where(cb.and(root.get("accountStatus").in(AccountStatus.ACTIVE, AccountStatus.CHANGE_REQUESTED),
				cb.notEqual(root.get("userName"), CoreConstants.UNAUTHENTICATED_USER),
				cb.isNull(root.get("lastSuccessfulLogin"))));

		List<Account> activeAndPendingAccounts = createQuery(query.distinct(true)).getResultList();
		return activeAndPendingAccounts;
	}

	@Override
	public List<Account> getAccountsUpForRenewal() {
		
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Account> query = cb.createQuery(Account.class);
		Root<Account> root = query.from(Account.class);
		
		root.fetch("accountHistory", JoinType.LEFT);
		
		Predicate predicate = cb.conjunction();
		
		predicate = cb.and(predicate,cb.equal(root.get("isActive"), true));
		predicate = cb.and(predicate,cb.and(root.get("accountStatus").in(AccountStatus.ACTIVE, AccountStatus.CHANGE_REQUESTED)));
		// Do not return the "anonymous" account.
		predicate = cb.and(predicate, cb.notEqual(root.get("userName"), CoreConstants.UNAUTHENTICATED_USER));
		
		predicate = cb.and(predicate, cb.and(root.join("accountRoleList", JoinType.LEFT).get("roleStatus").in(RoleStatus.EXPIRING_SOON,RoleStatus.EXPIRED)));
		query.where(predicate).distinct(true);
		List<Account> out = createQuery(query).getResultList();
		
		for(Account account:out) {
			Hibernate.initialize(account.getAccountHistory());
		}
		
		return out;

	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<Account> getByStatuses(AccountStatus...accountStatuses) {
		
		if(accountStatuses.length == 0) {
			return new ArrayList<Account> ();
		}

		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Account> query = cb.createQuery(persistentClass);

		Root<Account> root = query.from(persistentClass);
		
		root.fetch("accountHistory", JoinType.LEFT);
		
		query.where(root.get("accountStatus").in(accountStatuses)).distinct(true);

		List<Account> out = createQuery(query).getResultList();
		
		for(Account account:out) {
			Hibernate.initialize(account.getAccountHistory());
		}
		
		return out;
	}
	
	@Override
	public Account getAccountWithEmailReport(Long accountId) {
		CriteriaBuilder cb = getCriteriaBuilder();
		CriteriaQuery<Account> query = cb.createQuery(Account.class);
		Root<Account> root = query.from(Account.class);
		
		root.fetch("accountEmailReportSettings", JoinType.LEFT);
		
		query.where(cb.equal(root.get("id"), accountId)).distinct(true);

		Account account = getUniqueResult(query);
		
		Hibernate.initialize(account.getAccountEmailReportSettings());
		
		return account;
	}
}

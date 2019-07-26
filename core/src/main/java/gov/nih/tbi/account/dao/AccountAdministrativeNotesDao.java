package gov.nih.tbi.account.dao;

import java.util.List;
import java.util.Map;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.account.model.hibernate.AccountAdministrativeNote;
import gov.nih.tbi.commons.dao.GenericDao;

public interface AccountAdministrativeNotesDao extends GenericDao<AccountAdministrativeNote, Long>{
	
	public List<AccountAdministrativeNote> getAccountAdministrativeNotes (Account account);
	
	/**
	 * Returns a map of the latest adminNotes for the given account list, key: accountId, value: latest adminNote 
	 * @param accounts - a HashMap, key: accountId, value: the latest adminNotes 
	 * @return
	 */
	public Map<Long, String> getLatestAdminNoteForAccounts(List<Account> accounts);
	
}

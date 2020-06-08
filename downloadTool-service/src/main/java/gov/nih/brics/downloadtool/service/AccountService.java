package gov.nih.brics.downloadtool.service;

import gov.nih.tbi.account.model.hibernate.Account;

public interface AccountService {
	public Account findByUserName(String username);
}

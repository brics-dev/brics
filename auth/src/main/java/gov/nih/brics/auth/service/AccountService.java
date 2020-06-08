package gov.nih.brics.auth.service;

import org.springframework.security.core.AuthenticationException;

import gov.nih.brics.auth.data.entity.AccountUserDetails;
import gov.nih.tbi.account.model.hibernate.Account;

public interface AccountService {
	public Account findByUserName(String username);
	public Account findByUserId(Long id);
	public void validateAccount(Account account, AccountUserDetails userDetails, String plainTxtPwd) throws AuthenticationException;
	public void validateAccountFromBRICS(Account account, String password);
	public Long getDiseaseKey();
}

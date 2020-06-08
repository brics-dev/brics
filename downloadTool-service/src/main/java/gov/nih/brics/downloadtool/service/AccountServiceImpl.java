package gov.nih.brics.downloadtool.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.brics.downloadtool.data.repository.AccountRepository;
import gov.nih.tbi.account.model.hibernate.Account;

@Service
public class AccountServiceImpl implements AccountService {
	
	@Autowired
	AccountRepository accountRepository;

	@Transactional(readOnly = true)
	public Account findByUserName(String username) {
		return accountRepository.findByUserName(username).orElse(null);
	}

}

package gov.nih.brics.auth.data.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import gov.nih.tbi.account.model.hibernate.Account;

public interface AccountRepository extends CrudRepository<Account, Long> {
	Optional<Account> findByUserName(String username);
}

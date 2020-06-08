package gov.nih.brics.downloadtool.data.repository;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import gov.nih.tbi.account.model.hibernate.Account;

/**
 * Provides access to the Account database tables
 * 
 * @author Joshua Park
 *
 */
public interface AccountRepository extends CrudRepository<Account, Long> {
	Optional<Account> findByUserName(String username);
}

package gov.nih.cit.brics.file.data.repository.meta;

import java.util.Optional;

import org.springframework.data.repository.CrudRepository;

import gov.nih.tbi.account.model.hibernate.Account;

public interface AccountRepository extends CrudRepository<Account, Long> {
	Optional<Account> findByUserName(String username);

	Optional<Account> findByUser_Id(Long id);
}

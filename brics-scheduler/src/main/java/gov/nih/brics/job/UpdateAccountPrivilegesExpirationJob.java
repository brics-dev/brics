package gov.nih.brics.job;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.commons.service.AccountManager;
import gov.nih.tbi.commons.service.HibernateManager;
import gov.nih.tbi.repository.ws.RestRepositoryProvider;


@Component
@Scope("singleton")
public class UpdateAccountPrivilegesExpirationJob {
	
	
	private static Logger log = Logger.getLogger(UpdateAccountPrivilegesExpirationJob.class);
	
	
	@Autowired
	private AccountManager accountManager;
	
	@Autowired
	private ModulesConstants modulesConstants;
	
	public void doJob() {
		log.log(Level.INFO, "---------Beginning updateAccountPrivilegesExpiration job");	
		accountManager.updateAccountPrivilegesExpiration();
		log.log(Level.INFO, "---------End updateAccountPrivilegesExpiration job");
	}
	

}

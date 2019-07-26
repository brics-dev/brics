package gov.nih.tbi.commons.service;

import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.repository.service.exception.DataLoaderException;

public interface DataLoaderManager {
	public void initializeDatasetFile(Account account, Long datasetId, String proxyTicket) throws DataLoaderException;
}

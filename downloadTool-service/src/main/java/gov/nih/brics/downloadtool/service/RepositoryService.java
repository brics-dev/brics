package gov.nih.brics.downloadtool.service;

import java.util.List;

import gov.nih.brics.downloadtool.exception.UnauthorizedActionException;
import gov.nih.brics.downloadtool.model.DownloadToolPackage;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.repository.model.hibernate.Dataset;

public interface RepositoryService {
	
	public void addToQueue(Account userAccount, Dataset dataset);
	public List<DownloadToolPackage> getDownloadPackageByUser(Account userAccount);
	public void removeFromQueue(Account userAccount, List<Long> datasetIds) throws UnauthorizedActionException;
	public void removePackageFromQueue(Account userAccount, Long packageId) throws UnauthorizedActionException;
	public boolean doesUserOwnThese(Account userAccount, List<Long> downloadableIds);
	public boolean doesUserOwnPackage(Account userAccount, Long packageId);
}

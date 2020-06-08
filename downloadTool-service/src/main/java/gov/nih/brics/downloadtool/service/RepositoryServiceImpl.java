package gov.nih.brics.downloadtool.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import gov.nih.brics.downloadtool.data.repository.DownloadPackageRepository;
import gov.nih.brics.downloadtool.data.repository.DownloadableRepository;
import gov.nih.brics.downloadtool.exception.UnauthorizedActionException;
import gov.nih.brics.downloadtool.model.DownloadToolPackage;
import gov.nih.brics.downloadtool.model.DownloadToolPackageDownloadables;
import gov.nih.brics.downloadtool.model.DownloadToolPackageUserFile;
import gov.nih.brics.downloadtool.security.jwt.ClaimsOnlyTokenProvider;
import gov.nih.tbi.account.model.hibernate.Account;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetDownloadFile;
import gov.nih.tbi.repository.model.hibernate.DownloadPackage;
import gov.nih.tbi.repository.model.hibernate.Downloadable;
import gov.nih.tbi.repository.model.hibernate.QueryToolDownloadFile;
import gov.nih.tbi.repository.model.hibernate.UserFile;

@Service
public class RepositoryServiceImpl implements RepositoryService {
	
	private final Logger logger = LoggerFactory.getLogger(RepositoryServiceImpl.class);
	
	@Autowired
	ClaimsOnlyTokenProvider tokenProvider;
	
	@Autowired
	DownloadPackageRepository downloadPackageRepository;
	
	@Autowired
	DownloadableRepository downloadableRepository;
	
	@Override
	public void addToQueue(Account userAccount, Dataset dataset) {
		// TODO Auto-generated method stub
		// see note in DownloadQueueController
	}
	
	@Override
	public List<DownloadToolPackage> getDownloadPackageByUser(Account userAccount) {
		// originally in repositoryManager.getDownloadPackageByUser()
		List<DownloadPackage> packages = downloadPackageRepository.findDistinctByUser(userAccount.getUser());
		List<DownloadToolPackage> outputPackages = new ArrayList<>();
		for (DownloadPackage pkg : packages) {
			outputPackages.add(toDownloadToolPackage(pkg));
		}
		return outputPackages;
	}

	@Transactional
	@Override
	public void removeFromQueue(Account userAccount, List<Long> downloadableIds) throws UnauthorizedActionException {
		//originally in repositoryManager.deleteDownloadableByIds()
		// which is called by RepositoryRestService.deleteDownloadablesByIds
		// which is called by download tool delete - which gets ids from the package object
		if (!doesUserOwnThese(userAccount, downloadableIds)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Remove from Queue - permissions check failed");
			}
			throw new UnauthorizedActionException();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Remove from Queue - permissions check passed");
		}
		downloadableRepository.deleteAllByIds(downloadableIds);
		List<DownloadPackage> emptyPackages = downloadPackageRepository.findByDownloadablesIsEmpty();
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Remove from Queue - number of empty packages to clear: %s", emptyPackages.size()));
		}
		List<Long> toDeleteIds = emptyPackages.stream().map(DownloadPackage::getId).collect(Collectors.toList());
		downloadPackageRepository.deleteByIds(toDeleteIds);
	}
	
	@Transactional
	@Override
	public void removePackageFromQueue(Account userAccount, Long packageId) throws UnauthorizedActionException {
		if (!doesUserOwnPackage(userAccount, packageId)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Remove Package from Queue - permissions check failed");
			}
			throw new UnauthorizedActionException();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("Remove Package from Queue - permissions check passed");
		}
		downloadableRepository.deleteByDownloadPackageId(packageId);
		List<DownloadPackage> emptyPackages = downloadPackageRepository.findByDownloadablesIsEmpty();
		if (logger.isDebugEnabled()) {
			logger.debug(String.format("Remove Package from Queue - number of empty packages to clear: %s", emptyPackages.size()));
		}
		List<Long> toDeleteIds = emptyPackages.stream().map(DownloadPackage::getId).collect(Collectors.toList());
		if (!toDeleteIds.isEmpty()) {
			downloadPackageRepository.deleteByIds(toDeleteIds);
		}
	}
	
	/**
	 * Determines if the given userAccount owns the package referenced by the passed
	 * in packageId.
	 * 
	 * @param userAccount
	 * @param packageId
	 * @return boolean true if user owns the package; otherwise false
	 */
	public boolean doesUserOwnPackage(Account userAccount, Long packageId) {
		List<DownloadToolPackage> packages = getDownloadPackageByUser(userAccount);
		for (DownloadToolPackage pkg : packages) {
			if (pkg.getId().equals(packageId)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean doesUserOwnThese(Account userAccount, List<Long> downloadableIds) {
		List<DownloadToolPackage> packages = getDownloadPackageByUser(userAccount);
		for (Long downloadableId : downloadableIds) {
			boolean found = false;
			for (DownloadToolPackage singlePackage : packages) {
				List<Long> packageIds = singlePackage.getDownloadables().stream().map(DownloadToolPackageDownloadables::getId).collect(Collectors.toList());
				if (packageIds.contains(downloadableId)) {
					found = true;
					break;
				}

			}
			if (!found) {
				return false;
			}
		}
		return true;
	}

	protected DownloadToolPackage toDownloadToolPackage(DownloadPackage dp) {
		DownloadToolPackage output = new DownloadToolPackage();
		output.setId(dp.getId());
		output.setName(dp.getName());
		output.setDateAdded(dp.getDateAdded().getTime());
		output.setOrigin(DownloadToolPackage.OriginEnum.fromValue(dp.getOrigin().name()));
		output.setDownloadables(dp.getDownloadables().stream().map(this::toDownloadables).collect(Collectors.toList()));
		return output;
	}
	
	protected DownloadToolPackageDownloadables toDownloadables(Downloadable dl) {
		DownloadToolPackageDownloadables output = new DownloadToolPackageDownloadables();
		output.setId(dl.getId());
		if (dl.getType() != null) {
			output.setType(dl.getType().name());
		}
		else {
			output.setType("none");
		}
		UserFile inputFile = dl.getUserFile();
		DownloadToolPackageUserFile file = new DownloadToolPackageUserFile();
		file.setId(inputFile.getId());
		file.setName(inputFile.getName());
		file.setDescription(inputFile.getDescription());
		file.setPath(inputFile.getPath());
		if (dl instanceof QueryToolDownloadFile)
			file.setStudy("Query");
		else {
			DatasetDownloadFile ddf = (DatasetDownloadFile) dl;
			file.setStudy(ddf.getDataset().getStudy().getTitle());
		}
		
		file.setSize(inputFile.getSize());
		output.setUserFile(file);
		return output;
	}
}

package gov.nih.brics.downloadtool.controller;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import gov.nih.brics.downloadtool.exception.UnauthorizedActionException;
import gov.nih.brics.downloadtool.model.DownloadToolPackage;
import gov.nih.brics.downloadtool.service.RepositoryService;

@RestController
public class DownloadQueueController extends BaseController implements QueueApi {
	
	private final Logger logger = LoggerFactory.getLogger(DownloadQueueController.class);
	
	@Autowired
	RepositoryService repositoryService;

	@Override
	public ResponseEntity<Void> addToQueue(Long datasetId) {
		// current process happens in DatasetAction.addToDownloadQueue
		// it's kindof complex:
		// 	* check if dataset is in download queue
		// 	* get list of form structures in dataset
		// 	* call RepositoryManager.addDatasetToDownloadQueue
		//		* build PV mapping files
		//		* create a new UserFile to hold the pv mapping file
		//		* upload that file with the pv mapping file content
		//		* build the DownloadPackage 
		//		* SAVE THE DOWNLOAD PACKAGE
		//		* optionally send an email about the download
		// 		* create an access record for the download/dataset
		// TODO: fill in once we can access dictionary with JWTs
		return ResponseEntity.ok().build();
	}
	
	@Override
	public ResponseEntity<List<DownloadToolPackage>> getAll() {
		logger.debug("user requests download packages: all");
		return ResponseEntity.ok(repositoryService.getDownloadPackageByUser(getAccount()));
	}

	@Override
	public ResponseEntity<Void> removeFromQueue(@NotNull @Valid List<Long> downloadableIds) {
		if (logger.isInfoEnabled()) {
			logger.info(String.format("Attempting to remove from queue %s",downloadableIds.toString()));
		}
		if (downloadableIds.isEmpty()) {
			return ResponseEntity.badRequest().build();
		}
		
		try {
			repositoryService.removeFromQueue(getAccount(), downloadableIds);
		}
		catch(UnauthorizedActionException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return ResponseEntity.ok().build();
	}

	@Override
	public ResponseEntity<Void> removePackageFromQueue(Long packageId) {
		if (logger.isInfoEnabled()) {
			logger.info(String.format("Attempting to remove package from queue %s", String.valueOf(packageId)));
		}
		if (packageId == null) {
			return ResponseEntity.badRequest().build();
		}
		
		try {
			repositoryService.removePackageFromQueue(getAccount(), packageId);
		}
		catch(UnauthorizedActionException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
		return ResponseEntity.ok().build();
	}
}

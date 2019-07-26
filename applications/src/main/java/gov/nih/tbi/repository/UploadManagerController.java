package gov.nih.tbi.repository;

import gov.nih.tbi.ApplicationsConstants;
import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.commons.AppConfig;
import gov.nih.tbi.commons.WebstartRestProviderException;
import gov.nih.tbi.commons.model.DatasetFileStatus;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.dictionary.validation.view.ValidationClient;
import gov.nih.tbi.repository.model.SubmissionDataFile;
import gov.nih.tbi.repository.model.SubmissionTicket;
import gov.nih.tbi.repository.model.hibernate.BasicStudy;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetFile;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.table.UploadTableModel;

import java.awt.EventQueue;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.ws.rs.InternalServerErrorException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;

/**
 * This class is the controller for the Upload Manager
 * 
 */
public class UploadManagerController {

	static Logger logger = Logger.getLogger(UploadManagerController.class);

	private static AppConfig config = AppConfig.getInstance();

	private static SubmissionTicket submissionTicket = new SubmissionTicket();
	private static Dataset myDataset = new Dataset();
	// stores the list of upload items loaded
	//private static List<UploadItem> uploadList = new ArrayList<UploadItem>();
	private static LinkedBlockingQueue <UploadItem> uploadList = new LinkedBlockingQueue<UploadItem>();

	// list of upload items still in queue
	private static Queue<UploadItem> uploadQueue = new LinkedList<UploadItem>();
	private static int tableIndex = 0;
	private static int uploadToken = ApplicationsConstants.MAXIMUM_CONCURRENT_UPLOAD;
	private static ThreadGroup uploadGroup = new ThreadGroup(ApplicationsConstants.UPLOAD_THREADGROUP_NAME);
	private static ConcurrentHashMap <UploadItem, Future<UploadStatus>> futureMap = new ConcurrentHashMap();	
	private static ExecutorService UploadExecutor;
	public static ExecutorService MonitorExecutor;
	public static int getUploadToken() {
		return uploadToken;
	}

	public static ExecutorService getUploadExecutor(){
		if (UploadExecutor == null || UploadExecutor.isShutdown()   || UploadExecutor.isTerminated() ){	
			UploadExecutor  = Executors.newFixedThreadPool(ApplicationsConstants.MAXIMUM_CONCURRENT_UPLOAD);
		}
		 return UploadExecutor;
	}
	
	public static ExecutorService getMonitorExecutor(){
		if (MonitorExecutor == null || MonitorExecutor.isShutdown()   || MonitorExecutor.isTerminated() ){	
			MonitorExecutor  = Executors.newFixedThreadPool(ApplicationsConstants.MAXIMUM_CONCURRENT_UPLOAD);
		}
		 return MonitorExecutor;
	}
	


	/**
	 * Initiate the queue by asking the user whether or not to load submissions from previous session
	 * 
	 * @throws InterruptedException
	 */
	public static void initQueue() {

		List<Dataset> uploadingSets = UploadManagerController.getUploadingDataset();

		if (!uploadingSets.isEmpty()) {
			// Display confirm dialog
			int confirmed =
					JOptionPane.showConfirmDialog(null,
							"Would you like to load the submissions from the previous session?", "Load Session",
							JOptionPane.YES_NO_OPTION);

			// load session if user confirmed
			if (confirmed == JOptionPane.YES_OPTION) {
				UploadManagerController.addToQueue(uploadingSets);
				doUpload();
			}
		}
	}

	public static void doUpload() {
		while (uploadToken > 0 && !UploadManagerController.getUploadQueue().isEmpty()) {
			nextUpload();
		}		
	}

	/**
	 * Upload just finished, so increment upload token and initialize the next upload
	 */
	public static void singleItemUploadDone() {

		uploadToken++;
		nextUpload();
	}

	/**
	 * Loads the upload queue
	 * 
	 * @param dataset
	 * @throws InterruptedException
	 */
	public static void loadUploadQueue(Dataset dataset) {

		UploadManagerController.addToQueue(dataset);

		doUpload();
	}

	/**
	 * Polls the next upload from the upload queue and initiates a new thread to upload it
	 */
	public static void nextUpload() {
			if (!UploadManagerController.getUploadQueue().isEmpty()) {
				UploadItem nextFile = UploadManagerController.getUploadQueue().poll();
				System.out.println("Before invoking thread, the upload queue status is " + nextFile.getUploadStatus());
				UploadManagerController.uploadFile(nextFile);
				System.out.println("This child thread has ben invoked. before reduced by one, now the uploadToken=" + uploadToken);
				uploadToken--;
			}
	}

	public static Queue<UploadItem> getUploadQueue() {

		return uploadQueue;
	}
	
	public static LinkedBlockingQueue<UploadItem> getUploadList() {

		return uploadList;
	}

	/**
	 * Gets the UploadItem with the specified table index.
	 * 
	 * @param index - Index of the UploadItem to return
	 * @return UploadItem with the specified table index.
	 */
	public static UploadItem getUploadItem(int index) {

		for (UploadItem uploadItem : uploadList) {
			if (uploadItem.getTableIndex() == index) {
				return uploadItem;
			}
		}
		return null;
	}

	/**
	 * Gets all the UploadItems with the specified status
	 * 
	 * @param status - Upload Status
	 * @return all the UploadItems with the specified status
	 */
	public static List<UploadItem> getUploadItemsOfStatus(UploadStatus status) {

		List<UploadItem> out = new ArrayList<UploadItem>();

		for (UploadItem uploadItem : uploadList) {
			if (status.equals(uploadItem.getUploadStatus())) {
				out.add(uploadItem);
			}
		}

		return out;
	}

	/**
	 * Gets string X/Y where X is the number of completed datasetfiles and Y is the total number of datasetfiles
	 * 
	 * @param name - name of the dataset in question
	 * @return
	 */
	public static String getNumOfCompletedFiles(UploadItem item) {

		String numberOfComplete = null;

		try {
			numberOfComplete = UploadManager.provider.getNumOfCompletedFiles(item.getDataset().getPrefixedId());
		} catch (Exception e) {
			e.printStackTrace();
		}

		return numberOfComplete;
	}

	/**
	 * Sets the UploadItem to complete.
	 * 
	 * @param uploadItem
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @throws WebstartRestProviderException
	 * @throws InternalServerErrorException
	 */
	public static void setUploadToComplete(UploadItem uploadItem) throws MalformedURLException,
			UnsupportedEncodingException, WebstartRestProviderException {

		logger.debug("enter setUploadToComplete");
		UploadManagerController.setDatasetFileToComplete(uploadItem.getDatasetFile());

	}

	/**
	 * Loads the icon to go on the frame
	 * 
	 * @param path
	 * @param description
	 * @return
	 */
	public static ImageIcon createImageIcon(String path, String description) {

		java.net.URL imgURL = ValidationClient.class.getClassLoader().getResource(path);
		if (imgURL != null) {
			ImageIcon imageIcon = new ImageIcon(imgURL, description);
			return imageIcon;
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

	/**
	 * Sends the submission ticket to the webservice and return the Dataset that gets created
	 * 
	 * @param submissionTicket
	 * @return
	 * @throws IOException
	 * @throws JAXBException
	 */
	public static Dataset processSubmissionTicket(SubmissionTicket submissionTicket) throws IOException, JAXBException {

		String sftpBaseDir = config.getProperty("SFTP_BASEDIR");

		StringBuffer serverPathBuffer = new StringBuffer(sftpBaseDir);

		String studyPrefixId = UploadManager.getSelectedStudy().getPrefixedId();
		serverPathBuffer.append(studyPrefixId);
		//correct spelling would be nice
		serverPathBuffer.append(ServiceConstants.FILE_SEPARATER);
		String datasetName = UploadManager.getDatasetName();
		serverPathBuffer.append(datasetName);
		serverPathBuffer.append(ServiceConstants.FILE_SEPARATER);

		String serverPath = serverPathBuffer.toString();

		Dataset result = UploadManager.provider.processSubmissionTicket(submissionTicket, getDirectoryPath(), serverPath,
				UploadManager.getSelectedStudy().getTitle(), UploadManager.getDatasetName());

		return result;
	}

	/**
	 * Determines if the dataset name is unique in the study
	 * 
	 * @return true if dataset name is unique, false otherwise.
	 */
	public static boolean isDatasetUnique() {

		Set<Dataset> datasets = null;
		try {
			datasets = UploadManager.provider.getDatasets(UploadManager.getSelectedStudy().getTitle());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (datasets != null && !datasets.isEmpty()) {
			for (Dataset dataset : datasets) {
				if (dataset != null && UploadManager.getDatasetName().equals(dataset.getName())) {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Unmarshals the submission ticket into submission ticket object
	 * 
	 * @param fileName
	 * @return
	 */
	public static SubmissionTicket unmarshalSubmissionTicket(String fileName) {

		SubmissionTicket submissionPackage = null;

		try {
			File uploadFile = new File(fileName);
			FileInputStream fis = new FileInputStream(uploadFile);
			ClassLoader cl = gov.nih.tbi.commons.model.ObjectFactory.class.getClassLoader();
			JAXBContext jc = JAXBContext.newInstance("gov.nih.tbi.repository.model", cl);
			Unmarshaller um = jc.createUnmarshaller();
			submissionPackage = (SubmissionTicket) um.unmarshal(fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}

		return submissionPackage;
	}

	/**
	 * Returns the path to the directory the submission ticket is in
	 * 
	 * @return
	 */
	public static String getDirectoryPath() {

		File file = new File(UploadManager.getFilePath());

		return file.getParent() + File.separator;
	}

	/**
	 * Validate if the file's CRC is the same a specified CRC using MD5
	 * 
	 * @param filePath - Full path of the file we want to validate
	 * @param crc - CRC we want to validate against
	 * @return boolean true if file's CRC is valid, false otherwise
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	public static boolean validateFileCrc(String filePath, String crc) throws IOException, NoSuchAlgorithmException {

		String md5String = "";
		MessageDigest md5 = MessageDigest.getInstance("MD5");
		FileReader fr;

		fr = new FileReader(filePath);
		int bytesRead = 0;
		while (fr.ready() && bytesRead < ModelConstants.MD5_HASH_SIZE) {
			int input = fr.read();
			md5.update((byte) input);
			bytesRead++;
		}

		md5String += new BigInteger(1, md5.digest()).toString(16);

		return crc.equals(md5String);
	}

	/**
	 * checks the CRCHASH. CRCHash should be the same as the md5 of the submission xml file
	 * 
	 * @param submissionTicket
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	public static boolean validateTicketCrc(SubmissionTicket submissionTicket) throws NoSuchAlgorithmException,
			IOException {

		try {
			// validate submission ticket CRC
			if (!validateFileCrc(getDirectoryPath() + submissionTicket.getSubmissionPackage().getName() + ".xml",
					submissionTicket.getSubmissionPackage().getCrcHash())) {
				return false;
			}

			List<SubmissionDataFile> datasetFiles = submissionTicket.getSubmissionPackage().getDatasets();
			List<SubmissionDataFile> associatedFiles = submissionTicket.getSubmissionPackage().getAssociatedFiles();

			// validate dataset files
			for (SubmissionDataFile datasetFile : datasetFiles) {
				if (!validateFileCrc(datasetFile.getPath(), datasetFile.getCrcHash())) {
					return false;
				}
			}

			// validate associated files
			for (SubmissionDataFile associatedFile : associatedFiles) {
				if (!validateFileCrc(associatedFile.getPath(), associatedFile.getCrcHash())) {
					return false;
				}
			}

			return true;
		} catch (FileNotFoundException e) {
			throw new FileNotFoundException(e.getMessage());
		} catch (NoSuchAlgorithmException e) {
			throw new NoSuchAlgorithmException(e.getMessage());
		} catch (IOException e) {
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * Returns all the Datasets that has Uploading as its status
	 * 
	 * @return All the Datasets of the user that has 'Uploading' as its status.
	 */
	public static List<Dataset> getUploadingDataset() {

		List<Dataset> datasetList = null;
		try {
			datasetList = UploadManager.provider.getUploadingDataset();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		if (uploadList != null && !uploadList.isEmpty()) {
			Iterator<Dataset> datasetIterator = datasetList.iterator();

			while (datasetIterator.hasNext()) {
				if (isDatasetInList(datasetIterator.next().getName())) {
					datasetIterator.remove();
				}
			}
		}

		return datasetList;
	}

	public static boolean isDatasetInList(String name) {
		for (UploadItem item : uploadList) {
			if (item.getDatasetName().equals(name)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if the Dataset of specified name is in the upload queue
	 * 
	 * @param name - name of the dataset to check for
	 * @return boolean true if dataset with the name exists, otherwise false
	 */
	public static boolean isDatasetQueued(String name) {

		for (UploadItem item : uploadQueue) {
			if (item.getDatasetName().equals(name)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Checks if the Dataset of specified name is pending
	 * 
	 * @param name - name of the dataset to check for
	 * @return boolean true if dataset with the name exists, otherwise false
	 */
	public static boolean isDatasetPending(String name) {

		for (UploadItem item : uploadList) {
			if (item.getDatasetName().equals(name)
					&& (UploadStatus.UPLOADING.equals(item.getUploadStatus()) || UploadStatus.QUEUED.equals(item
							.getUploadStatus()))) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Add the DatasetFiles from the Dataset into the upload queue
	 * 
	 * @param dataset - Dataset to be added into the upload queue.
	 */
	public static void addToQueue(Dataset dataset) {

		try {
			for (DatasetFile datasetFile : UploadManager.provider.getDatasetFiles(dataset.getId())) {
				if (DatasetFileStatus.PENDING.equals(datasetFile.getDatasetFileStatus())) {
					UploadItem item =
							new UploadItem(tableIndex, dataset, datasetFile, dataset.getStudy().getTitle(),
									UploadStatus.QUEUED, dataset.getStudy().getPrefixedId());
					uploadQueue.add(item);
					uploadList.add(item);
					//UploadManagerController.threadMessage("Confirming UMC.addToQueue: should be in EDT");
					((UploadTableModel) UploadManager.getTable().getModel()).addNewRow(item);
					tableIndex++;
				}
			}
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Adds the dataset files to the upload queue
	 * 
	 * @param datasets
	 */
	public static void addToQueue(List<Dataset> datasets) {

		for (Dataset dataset : datasets) {
			for (DatasetFile datasetFile : dataset.getDatasetFileSet()) {
				if (DatasetFileStatus.PENDING.equals(datasetFile.getDatasetFileStatus())) {
					UploadItem item =
							new UploadItem(tableIndex, dataset, datasetFile, dataset.getStudy().getTitle(),
									UploadStatus.QUEUED, dataset.getStudy().getPrefixedId());
					uploadQueue.add(item);
					uploadList.add(item);
					((UploadTableModel) UploadManager.getTable().getModel()).addNewRow(item);
					// increment table index for the next item
					tableIndex++;
				}
			}
		}
	}

	/**
	 * start uploading the file
	 * 
	 * @param datasetFile
	 */
	public static void uploadFile(UploadItem uploadItem) {
		// start new thread to upload the file
			UploadChannel uploadChannel = new UploadChannel(uploadItem);
//			Thread childThread = new Thread(uploadGroup, uploadChannel);
//			Thread childThread = new Thread(uploadChannel);
//			childThread.start();
			uploadItem.setUploadChannel(uploadChannel);
			if (UploadManagerController.UploadExecutor == null){
				System.out.println("Executorservice is null!");
			}	
			else if ( UploadManagerController.UploadExecutor.isShutdown() || UploadManagerController.UploadExecutor.isTerminated()	){
				System.out.println("Executor is shutdown now with status of  " +  
						UploadManagerController.UploadExecutor.isShutdown());
			}	
			uploadItem.setUploadStatus(UploadStatus.UPLOADING);
			UploadManagerController.UploadExecutor.submit(uploadChannel);
			// add the thread into upload map that uses the unique dataset name as a key
			uploadItem.setUploadChannel(uploadChannel);
		}


	/**
	 * Returns a list of all the active studies the user has access to
	 * 
	 * @return
	 */
	public static List<Study> getStudyList() {

		List<Study> studies = null;
		try {
			studies = new ArrayList<Study>(UploadManager.provider.getStudies());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		Collections.sort(studies, new Comparator<Study>() {

			@Override
			public int compare(Study o1, Study o2) {
				return o1.getTitle().compareTo(o2.getTitle());
			}

		});

		return studies;
	}

	/**
	 * Gets the study based on the dataset file
	 * 
	 * @param id
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static BasicStudy getStudyFromDatasetFile(Long id) throws UnsupportedEncodingException {

		return UploadManager.provider.getStudyFromDatasetFile(id);
	}

	/**
	 * Gets a list of pending dataset files to be completed
	 * 
	 * @param datasetFile
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @throws WebstartRestProviderException
	 * @throws InternalServerErrorException
	 */
	public static void setDatasetFileToComplete(DatasetFile datasetFile) throws MalformedURLException,
			UnsupportedEncodingException, WebstartRestProviderException {
		//System.out.println("Webservice: setting DatasetStatus:"  + datasetFile.getDatasetFileStatus().name());
		UploadManager.provider.setDatasetFileToComplete(datasetFile.getId());
	}

	/**
	 * Gets a list of pending dataset files to be completed
	 * 
	 * @param id
	 * @throws MalformedURLException
	 * @throws UnsupportedEncodingException
	 * @throws WebstartRestProviderException
	 * @throws InternalServerErrorException
	 */
	public static void setDatasetFileToComplete(Long id) throws MalformedURLException, UnsupportedEncodingException,
			WebstartRestProviderException {

		UploadManager.provider.setDatasetFileToComplete(id);
	}

	/**
	 * Remove all the items from the table regardless of the status.
	 */
	public static void clearTable() {

		for (UploadItem upload : uploadList) {
			((UploadTableModel) UploadManager.getTable().getModel()).removeRow(upload.getTableIndex());
			tableIndex--;
		}
		uploadList = new LinkedBlockingQueue<UploadItem>();
	}

	/**
	 * Remove items from the table by their status.
	 */
	public static void clearByStatus(UploadStatus status) {

		List<UploadItem> toBeRemoved = new ArrayList<UploadItem>();
		
		int rowCount = UploadManager.getTable().getModel().getRowCount();
		
		List<UploadItem> uploads = new ArrayList<UploadItem>(uploadList);

		for (int row = 0; row < rowCount; row++) {
			UploadItem upload = uploads.get(row);
			UploadTableModel tableModel = UploadManager.getModel();
			String columnStatus = (String) tableModel.getValueAt(row, UploadTableModel.STATUS_COLUMN);
			if(status.getName().equals(columnStatus)){
				toBeRemoved.add(upload);					
			}
		}

		for (UploadItem upload : toBeRemoved) {
			((UploadTableModel) UploadManager.getTable().getModel()).removeRow(upload.getTableIndex());
		}
	}

	public synchronized static void uploadCompleteAction(UploadItem uploadItem) {
		try {
			UploadManagerController.setUploadToComplete(uploadItem);
			uploadItem.setUploadStatus(UploadStatus.COMPLETED);
			
			// code below has been taken care at higher level
			// displays the upload completion message if the dataset upload is complete
			/* if (!UploadManagerController.isDatasetPending(uploadItem.getDatasetName())) {
				JOptionPane.showMessageDialog(null, UploadManagerController.getNumOfCompletedFiles(uploadItem)
						+ " Upload of Dataset " + uploadItem.getDatasetName() + " has been completed.",
						"Upload Complete", JOptionPane.INFORMATION_MESSAGE);
			}
			*/
		} catch (MalformedURLException e) {
			logger.info("MalformedURLException: " + e.getMessage() );
			UploadManagerController.cancelSingleItemUpload(uploadItem);
			JOptionPane.showMessageDialog(null, "A: An error has occured while trying to contact the server.");
		} catch (UnsupportedEncodingException e) {
			logger.info("UnsupportedEncodingException: " + e.getMessage() );
			UploadManagerController.cancelSingleItemUpload(uploadItem);
			JOptionPane.showMessageDialog(null, "B: An error has occured while trying to contact the server.");
		} catch (WebstartRestProviderException e) {
			logger.info("WebstartRestProviderException: " + e.getMessage() );
			UploadManagerController.cancelSingleItemUpload(uploadItem);
			JOptionPane.showMessageDialog(null, "C: An error has occured while trying to contact the server.");
		}
	}

	public static void retryUpload(int rowIndex) {

		UploadItem item = getUploadItem(rowIndex);
		uploadFile(item);
		uploadToken--;
	}
	
	/**
	 * Cancels the upload
	 * 
	 * @param uploadItem
	 */
	public static void cancelSingleItemUpload(UploadItem uploadItem) {
		uploadItem.getUploadChannel().cancelUpload();
/*		if (UploadStatus.QUEUED.equals(uploadItem.getUploadStatus())) {
			//uploadQueue.remove(uploadItem);
			//uploadList.remove(uploadItem);
			//((UploadTableModel) UploadManager.getTable().getModel()).removeRow(uploadItem.getTableIndex());
		} else {
			uploadItem.getUploadChannel().cancelUpload();
		}
*/
		}

	public static void cancelUpload(UploadItem uploadItem) {
		if (UploadStatus.QUEUED.equals(uploadItem.getUploadStatus())) {
			uploadQueue.remove(uploadItem);
			uploadList.remove(uploadItem);
			((UploadTableModel) UploadManager.getTable().getModel()).removeRow(uploadItem.getTableIndex());
		} else {
			uploadItem.getUploadChannel().cancelUpload();
		}
	}

	public static void shutdownAndAwaitTermination() {
		try {
			UploadExecutor.shutdown(); // Disable new tasks from being submitted
			// Wait a while for existing tasks to terminate
			if (!UploadExecutor.awaitTermination(3, TimeUnit.SECONDS)) {
				UploadExecutor.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!UploadExecutor.awaitTermination(3, TimeUnit.SECONDS))
					System.err.println("Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			System.err.println("In InterruptedException: force shutdown");
			// (Re-)Cancel if current thread also interrupted
			UploadExecutor.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
		return;
	}
	 
	
	public static void threadMessage(String msg) {

		if (EventQueue.isDispatchThread()) {
			System.out.println(msg + " is EDT thread:  " + Thread.currentThread().getId());
		} else {
			System.out.println("~~~~ NON-EDT thread:" + msg + " with name " + Thread.currentThread().getName());
		}
	
	}

	//check status
	
	public static UploadStatus invokeUpload() throws InterruptedException, ExecutionException {
		//UploadManager.setUploadStatusReady(false);
		UploadManagerController.addToQueue(myDataset);
		UploadStatus status = UploadStatus.QUEUED;
		UploadExecutor  = getUploadExecutor();
		ExecutorCompletionService completionService = new ExecutorCompletionService<String >(UploadExecutor); 
		futureMap = new ConcurrentHashMap<UploadItem, Future<UploadStatus>>();

		while (!uploadQueue.isEmpty()){
			UploadItem uploadItem = uploadQueue.poll();
			UploadChannel uploadChannel = new UploadChannel(uploadItem);

			// add the thread into upload map that uses the unique dataset name as a key
			uploadItem.setUploadChannel(uploadChannel);
			uploadItem.setUploadStatus(UploadStatus.QUEUED);
			futureMap.put(uploadItem, completionService.submit(uploadChannel) );
		}
		int completedTasks = 0;
		for (int i = 0; i <  futureMap.size(); i++){
			UploadStatus itemStatus= (UploadStatus) completionService.take().get();
			if(itemStatus.equals(UploadStatus.COMPLETED) ){
				completedTasks++;
			}
		}
		if(futureMap.size() == completedTasks){
			UploadManager.ISUPLOADING.set(false);
			status= UploadStatus.COMPLETED;
		}
		else{
			status= UploadStatus.CANCELLED;
		}
		return status;
	}		

	public static void cancelAllUpload() throws InterruptedException{
		try{
			for (Map.Entry<UploadItem, Future<UploadStatus>> entry : futureMap.entrySet()) {
				UploadItem uploadItem = entry.getKey();
				uploadItem.setUploadStatus(UploadStatus.CANCELLED);
				Future <UploadStatus> future = entry.getValue();
				if(!future.isDone() && !future.isCancelled()){
					future.cancel(true);
				}
			}
		}
		catch(Exception e){
			logger.error("Error message caught from upload cancelation: " + e.getMessage());
		}
		UploadExecutor.shutdownNow();
		Thread.sleep(2000);
		logger.info("cancelling submission for Study Name: " + UploadManager.getFinalStudyName()+ " with datasetName:" + UploadManager.getFinalDatasetName());
		deleteDatasetFromServer(UploadManager.getFinalStudyName(), UploadManager.getFinalDatasetName());
	}
		
	/*
	 * Parameter: xml ticket file name. This method must be called before invoking the 
	 * submission.
	 */
	public static SubmissionQCStatus  getSubmissionQCStatus(String ticketFileName, String selectedStudyName,
			String datasetName, String version, String serverLocation) throws Exception {
		if (selectedStudyName == null) {
			return SubmissionQCStatus.EMPTY_STUDY;
		} 
		else if (ticketFileName == null || ServiceConstants.EMPTY_STRING.equals(ticketFileName) ) {
			return SubmissionQCStatus.EMPTY_TICKET_NAME;
		}
		else if (!ticketFileName.isEmpty() && !ticketFileName.contains("submissionTicket")) {
			return SubmissionQCStatus.INVALID_TICKET_NAME;
		}
		
		submissionTicket = UploadManagerController.unmarshalSubmissionTicket(ticketFileName);
		if (submissionTicket == null) {
			return SubmissionQCStatus.NULL_TICKET;
		}
		else if (!UploadManagerController.validateTicketCrc(submissionTicket)) {
			return SubmissionQCStatus.ILLEGAL_MODIFICATION;
		}
		// Check if the environment and version are consistant with those of the ticket
		else if (submissionTicket.getEnvironment().indexOf(serverLocation) > 0) {
			return SubmissionQCStatus.INVALID_ENV;
		}
		else if (!version.equalsIgnoreCase(submissionTicket.getVersion())) {
			return SubmissionQCStatus.INVALID_VERSION;
		}
		else if (datasetName == null || datasetName.equals(ServiceConstants.EMPTY_STRING)) {
			return SubmissionQCStatus.EMPTY_DATASET;
		} 
		else if (datasetName.length() > ApplicationsConstants.MAX_DATASET_NAME_LENGTH){
			return SubmissionQCStatus.INVALID_DATASET_LENGTH;
		}
		else if (!UploadManagerController.isDatasetUnique()) {
			return SubmissionQCStatus.DUPLICATE_DATASET;
		}			
		else{
			myDataset = UploadManagerController.processSubmissionTicket(submissionTicket);
			if (myDataset == null || myDataset.getDatasetFileSet().isEmpty() ){
				return SubmissionQCStatus.INVALID_TICKET_FORMAT;
			}
		}
		return SubmissionQCStatus.PASSED;
	}
	
	/*
	 * Convert the submission ticket XML doc to dataset object
	 */
	public static boolean convertTicketToDataset() throws Exception {
		myDataset = UploadManagerController.processSubmissionTicket(submissionTicket);
		if (myDataset == null || myDataset.getDatasetFileSet().isEmpty() ){
			return false; // Message for display: SubmissionQCStatus.INVALID_TICKET_FORMAT;
		}	
		return true;
	}	
/*	
	private class UploadPropertyChangeListener implements  PropertyChangeListener {
		public UploadPropertyChangeListener() {
			// TODO Auto-generated constructor stub
		}
		public void propertyChange(PropertyChangeEvent event) {
		    String property = event.getPropertyName();
		    if ("progress".equals(property)) {
		    }	
		}
	}
	*/
	public static void deleteDatasetFromServer(String studyTitle, String datasetName){
		logger.debug("Start to delete files from the server for dataset name: "  + datasetName);
		try {
			UploadManager.provider.deleteDatasetCascadeByName(studyTitle, datasetName);
		} catch (HttpException e1) {
			logger.error("HttpException Error for dataset deletion. dataset name: " + datasetName + " for study: " + studyTitle);
			e1.printStackTrace();
		} catch (IOException e1) {
			logger.error("IOException Error for dataset deletion: dataset name: " + datasetName + " for study: " + studyTitle);
			e1.printStackTrace();
		}
		System.out.println("Complete deleting files from the server for dataset name: "  + datasetName);
	}

}
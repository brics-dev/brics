package gov.nih.brics.job;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.mail.MessagingException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import gov.nih.tbi.repository.dao.DatasetDao;
import gov.nih.tbi.repository.dao.DownloadPackageDao;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.commons.model.DatasetStatus;

@Component
@Scope("singleton")
public class DatasetStatusEmailJob extends EmailJobs {

	private static Logger log = Logger.getLogger(DatasetStatusEmailJob.class);

	@Autowired
	private DatasetDao datasetDao;

	private static final String emailSubject = " Dataset Status Report";

	private static final String emailTable = "<!DOCTYPE html> <html> <head> <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n <style> table {   border-collapse: collapse;   border-spacing: 0;   width: 100%;   border: 1px solid #ddd; } \n  th, td {   text-align: left;   padding: 8px; } \n tr:nth-child(even){background-color: #f2f2f2} </style> </head> \n <body>";
	private static final String tableOpen = "<div style=\"overflow-x:scroll;max-height:250px;width:750px\"><table>\n";
	private static final String tableClose = "</table> </div></br></br>";
	private static final String docClose = "</body> </html>";
	private static final String tableHeader = " <tr><th>id</th><th>Dataset Name</th><th>Study Name</th><th>Submitted Date</th></tr>\n";
	private static final String br = "<br>";
	
	private static final String archivedHeader = "1. Number of requests for datasets to be archived = ";
	private static final String deletedHeader = "2. Number of requests for datasets to be deleted = ";
	private static final String sharedHeader = "3. Number of requests for datasets to be shared = ";
	private static final String errorHeader = "4. Number of datasets in 'Error During Load' = ";
	private static final String uploadedHeader = "5. Number of datasets in 'Uploading' = ";

	private StringBuilder reportBody = new StringBuilder();

	@Async
	public void doJob() {
		log.log(Level.INFO, "---------Beginning DatasetStatusEmailJob job");
		reportBody.append(emailTable);
		addArchiveDatasetsToReportBody();
		addDeleteDatasetsToReportBody();
		addShareDatasetsToReportBody();
		addErrorDatasetsReportBody();
		addUploadingDatasetsReportBody();
		reportBody.append(docClose);

		try {
			sendEmail(modulesConstants.getModulesOrgName() + emailSubject, reportBody.toString(),
					modulesConstants.getModulesOrgEmail());
			log.log(Level.INFO, "email sent to " + modulesConstants.getModulesOrgEmail());
		} catch (MessagingException e) {
			e.printStackTrace();
		}
		
		
		log.log(Level.INFO, "---------End DatasetStatusEmailJob job");
	}

	private void addArchiveDatasetsToReportBody() {
		StringBuilder body = new StringBuilder();
		StringBuilder data = new StringBuilder();
		Set<DatasetStatus> statuses = new HashSet<>();
		statuses.add(DatasetStatus.PRIVATE);

		List<Dataset> datasetList = datasetDao.getDatasetByStatuses(statuses);

		int rowCount = 0;
		for (Dataset d : datasetList) {
			if (d.getDatasetRequestStatus() != null && d.getDatasetRequestStatus().equals(DatasetStatus.ARCHIVED)) {
				++rowCount;
				data.append(parseLine(rowCount, d));
			}

		}
		
		body.append(archivedHeader + (rowCount) + br);
		
		if (rowCount != 0) {
			body.append(tableOpen);
			body.append(tableHeader);
			body.append(data);
			body.append(tableClose);
		}

		reportBody.append(body);
	}

	private void addDeleteDatasetsToReportBody() {
		StringBuilder body = new StringBuilder();
		StringBuilder data = new StringBuilder();
		Set<DatasetStatus> statuses = new HashSet<>();
		statuses.add(DatasetStatus.PRIVATE);

		List<Dataset> datasetList = datasetDao.getDatasetByStatuses(statuses);

		int rowCount = 0;
		for (Dataset d : datasetList) {
			if (d.getDatasetRequestStatus() != null && d.getDatasetRequestStatus().equals(DatasetStatus.DELETED)) {
				++rowCount;
				data.append(parseLine(rowCount, d));
			}

		}
		
		body.append(deletedHeader + (rowCount) + br);
		
		if (rowCount != 0) {
			body.append(tableOpen);
			body.append(tableHeader);
			body.append(data);
			body.append(tableClose);
		}

		reportBody.append(body);

	}

	private void addShareDatasetsToReportBody() {
		StringBuilder body = new StringBuilder();
		StringBuilder data = new StringBuilder();
		Set<DatasetStatus> statuses = new HashSet<>();
		statuses.add(DatasetStatus.PRIVATE);

		List<Dataset> datasetList = datasetDao.getDatasetByStatuses(statuses);

		int rowCount = 0;
		for (Dataset d : datasetList) {
			if (d.getDatasetRequestStatus() != null && d.getDatasetRequestStatus().equals(DatasetStatus.SHARED)) {
				++rowCount;
				data.append(parseLine(rowCount, d));
			}

		}
		
		body.append(sharedHeader + (rowCount) + br);
		
		if (rowCount != 0) {
			body.append(tableOpen);
			body.append(tableHeader);
			body.append(data);
			body.append(tableClose);
		}

		reportBody.append(body);

	}

	private void addUploadingDatasetsReportBody() {
		StringBuilder body = new StringBuilder();
		StringBuilder data = new StringBuilder();
		Set<DatasetStatus> statuses = new HashSet<>();
		statuses.add(DatasetStatus.UPLOADING);

		List<Dataset> datasetList = datasetDao.getDatasetByStatuses(statuses);

		int rowCount = 0;
		for (Dataset d : datasetList) {
			++rowCount;
			data.append(parseLine(rowCount, d));
		}

		body.append(uploadedHeader + (rowCount) + br);

		if (rowCount != 0) {
			body.append(tableOpen);
			body.append(tableHeader);
			body.append(data);
			body.append(tableClose);
		}

		reportBody.append(body);

	}

	private void addErrorDatasetsReportBody() {
		StringBuilder body = new StringBuilder();
		StringBuilder data = new StringBuilder();
		Set<DatasetStatus> statuses = new HashSet<>();
		statuses.add(DatasetStatus.ERROR);

		List<Dataset> datasetList = datasetDao.getDatasetByStatuses(statuses);

		int rowCount = 0;
		for (Dataset d : datasetList) {
			++rowCount;
			data.append(parseLine(rowCount, d));
		}

		body.append(errorHeader + (rowCount) + br);

		if (rowCount != 0) {
			body.append(tableOpen);
			body.append(tableHeader);
			body.append(data);
			body.append(tableClose);
		}

		reportBody.append(body);

	}

	private static String parseLine(int i, Dataset d) {

		return "<tr>" + surroundWithTd(Integer.toString(i))
				+ surroundWithTd(d.getName() != null ? d.getName().toString() : "")
				+ surroundWithTd(d.getStudy().getTitle() != null ? d.getStudy().getTitle().toString() : "")
				+ surroundWithTd(d.getSubmissionDate() != null ? d.getSubmissionDate().toString() : "") + "</tr>\n";
	}

	private static String surroundWithTd(String s) {
		return "<td>" + s + "</td>";
	}

}

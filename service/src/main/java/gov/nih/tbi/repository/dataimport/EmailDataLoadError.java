package gov.nih.tbi.repository.dataimport;


import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import gov.nih.tbi.repository.dao.MetaDataImportDao;

@Service
public class EmailDataLoadError {
	private static final Logger logger = Logger.getLogger(EmailDataLoadError.class);
	private static final String EMAIL_HEADER = "Below are the current datasets that have failed the data load process: \n\n" + "<table border=\"1\"><tr><th>Dataset Name </th><th>Date Submitted</th></tr>";
	
	@Autowired
	MetaDataImportDao metaDataImportDao;
		
	public String getEmailDataLoadErrorBody() {
		String emailBody = EMAIL_HEADER;
		List<String> datasetsInError = metaDataImportDao.getDatasetsInError();
		if(datasetsInError.isEmpty()) {
				return "";
			}
		for(String datasetAndDate: datasetsInError) {
				String[] arr = datasetAndDate.split("|", 2);
				emailBody = emailBody.concat("<tr><td>"+arr[0]+"</td><td>"+arr[1]+"</td></tr>");
			}
		emailBody = emailBody.concat("</table>");
		return emailBody;
	}

}

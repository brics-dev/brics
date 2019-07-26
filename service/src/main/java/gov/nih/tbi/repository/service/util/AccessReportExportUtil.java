package gov.nih.tbi.repository.service.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVWriter;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.metastudy.model.hibernate.MetaStudyAccessRecord;
import gov.nih.tbi.repository.model.hibernate.AccessRecord;

public class AccessReportExportUtil {

	static Logger logger = Logger.getLogger(AccessReportExportUtil.class);

	// Headers
	private static final String DATASET_ID = "Dataset ID";
	private static final String DATASET_NAME = "Dataset Name";
	private static final String DATASET_STATUS = "Dataset Status";
	private static final String USER = "Username";
	private static final String QUEUED_DATE = "Download Date";
	private static final String NUM_RECORDS = "Number of Records";
	private static final String LOCATION = "Download Location";
	private static final String STUDY_PREFIXED_ID = "Study ID";
	private static final String PI_NAME = "Principal Investigator";

	ByteArrayOutputStream baos;
	CSVWriter writer;
	boolean incStudyColumns;

	public AccessReportExportUtil(boolean incStudyColumns) {
		this.incStudyColumns = incStudyColumns;
		String[] studyHeaders = {STUDY_PREFIXED_ID, PI_NAME};
		String[] accessRecordheaders =
				{DATASET_ID, DATASET_NAME, DATASET_STATUS, USER, QUEUED_DATE, NUM_RECORDS, LOCATION};
		String[] headers;
		if (incStudyColumns) {
			headers = (String[]) ArrayUtils.addAll(studyHeaders, accessRecordheaders);
		} else {
			headers = accessRecordheaders;
		}

		// start a new writer and print the headers.
		baos = new ByteArrayOutputStream();
		writer = new CSVWriter(new OutputStreamWriter(baos));
		writer.writeNext(headers);

	}
	
	public AccessReportExportUtil(String[] columnHeaders) {

		// start a new writer and print the headers.
		baos = new ByteArrayOutputStream();
		writer = new CSVWriter(new OutputStreamWriter(baos));
		writer.writeNext(columnHeaders);

	}

	public void writeData(List<AccessRecord> records) {
		// Create a string ArrayList of a single accessRecord and then write it before moving on to
		// the next one.

		for (AccessRecord ar : records) {
			List<String> currentRow = new ArrayList<String>();
			if (incStudyColumns) {
				currentRow.add(ar.getDataset().getStudy().getPrefixedId());
				currentRow.add(ar.getDataset().getStudy().getPrincipalInvestigator());
			}
			currentRow.add(ar.getDataset().getId().toString());
			currentRow.add(ar.getDataset().getName());
			currentRow.add(ar.getDataset().getDatasetStatus().getName());
			currentRow.add(ar.getAccount().getUserName());
			currentRow.add(BRICSTimeDateUtil.formatDate(ar.getQueueDate()));
			currentRow.add(ar.getRecordCount().toString());
			currentRow.add(ar.getDataSource().getName());

			// java recommended way to convert from List<String> to String[]
			writer.writeNext(currentRow.toArray(new String[0]));
		}
	}
	
	public void writeMetaStudyAccessData(List<MetaStudyAccessRecord> metaStudyAccessRecordList) {
		for(MetaStudyAccessRecord msar : metaStudyAccessRecordList){
			List<String> currentRow = new ArrayList<String>();
			
			if(msar.getMetaStudyData() != null){
				currentRow.add(msar.getMetaStudyData().getDoi());
				currentRow.add(msar.getMetaStudyData().getUserFile().getName());
				currentRow.add(msar.getAccount().getUserName());
				currentRow.add(BRICSTimeDateUtil.formatDate(msar.getDateCreated()));
			} else {
				currentRow.add(msar.getSupportingDocumentation().getDoi());
				currentRow.add(msar.getSupportingDocumentation().getUserFile().getName());
				currentRow.add(msar.getAccount().getUserName());
				currentRow.add(BRICSTimeDateUtil.formatDate(msar.getDateCreated()));
			}
			writer.writeNext(currentRow.toArray(new String[0]));
		}
	}

	public ByteArrayOutputStream getOutputStream() {
		try {
			writer.close();
		} catch (IOException e) {
			logger.error(
					"CSV Writer output stream failed to close when exporting AccessRecord. This is not normal operation.",
					e);
		}
		return baos;
	}


	/**
	 * Converts a list of access reports into a byteArrayStream that contains a CSV export.
	 * 
	 * @param records
	 * @param incStudyColumns : iff true, a study and PI column will be included in the CSV
	 * @return
	 */
	public static ByteArrayOutputStream exportAccessReportToCSV(List<AccessRecord> records, boolean incStudyColumns) {
		AccessReportExportUtil util = new AccessReportExportUtil(incStudyColumns);
		util.writeData(records);
		return util.getOutputStream();

	}

	public static ByteArrayOutputStream exportMetaStudyAccessReportToCSV(List<MetaStudyAccessRecord> records, boolean incStudyColumns) {
		AccessReportExportUtil util = new AccessReportExportUtil(incStudyColumns);
		util.writeMetaStudyAccessData(records);
		return util.getOutputStream();

	}
}

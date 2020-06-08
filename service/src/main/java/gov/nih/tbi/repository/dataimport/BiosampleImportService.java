package gov.nih.tbi.repository.dataimport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.jcraft.jsch.JSchException;
import com.sun.mail.iap.ProtocolException;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.repository.model.hibernate.DatafileEndpointInfo;
import gov.nih.tbi.repository.service.io.SftpClient;
import gov.nih.tbi.repository.service.io.SftpClientManager;

@Service
@Scope("singleton")
public class BiosampleImportService {
	
	private static final Logger logger = Logger.getLogger(BiosampleImportService.class);
			
	private SftpClient sftp = null;
	
	private static final String REPOSITORY = "NINDS";
	private static final String DATA_STRUCTURE_SHORT_NAME = "BiosampleCatalogV6";
	private static final String DATA_STRUCTURE_VERSION = "1";
	private static final String STUDY_ID = "PDBP-STUDY0000209";
	private static final String BIOSAMPLE_URL = "?isBiosample=true&repository=";
	private static final String FAILED = "Failed";
	private static final String SUCCESS = "Success";
	private static final String NOT_CONFIGURED = "Not configured";
	private static final String CSV_HEADER = "record,Subject Information.RepositoryName,Subject Information.PDBPStudyId,Subject Information.GUID,Subject Information.VisitTypPDBP,Subject Information.DaysSinceBaseline,Sample Information.OrderableBiosampleID,Sample Information.SampCollTyp,Sample Information.SampleUnitNum,Sample Information.SampleUnitMeasurement,Quality Control.SampleAvgHemoglobinVal,Quality Control.SampleAvgHemoglobinUnits,Quality Control.SampleAvgHemoglobinResult,Quality Control.SampleAvgHemoglobinResultDesc,Quality Control.SampleClottingInd,Quality Control.SampleHemolysisScale,Quality Control.SampleTurbidityScale,Quality Control.ConcentrationDNA_RNA,Quality Control.ConcentrationDNA_RNAUnits,Quality Control.Sample260_280Ratio,Quality Control.Sample260_230Ratio,Quality Control.RNAQualityScale,Quality Control.RNAIntegrityNum,Quality Control.rRNARatio,Inventory.Inventory_BiorepositoryCount,Inventory.InventoryAdditStockAvailabInd,Inventory.Inventory_BiorepositoryDate\n";
	
	
	@Autowired
	ModulesConstants modulesConstants;
	
	

	public synchronized String processBiosampleSubmission() {
		SimpleDateFormat f = new SimpleDateFormat("yyyyMMddHHmmssS");
		String currentDateTime = f.format(new Date());
		String validationMessage = "";
		
		String fileName = modulesConstants.getBiosampleFilename();
		try {
			String biosampleFile = getBiosampleFile(fileName);
			//If the env isn't configured for Biosample, end the import process immediately
			if(biosampleFile.contentEquals(NOT_CONFIGURED)) {
				return NOT_CONFIGURED;
			}
			List<BiosampleCSVData> biosamplePojos = parseFileToPojo(biosampleFile);
			List<BiosampleCSVData> biosampleRecords = updateBiosampleRecords(biosamplePojos);
			List<String> dataProcessingInfo = new ArrayList<>();
			List<String> messages = new ArrayList<>();
			String dataProcessingHeader = "";
			String header = "";
			dataProcessingInfo.add(modulesConstants.getModulesOrgEmail());
			dataProcessingInfo.add(DATA_STRUCTURE_SHORT_NAME + "_" + currentDateTime);
			dataProcessingInfo.add(STUDY_ID);
			for (String s : dataProcessingInfo) {
				dataProcessingHeader = dataProcessingHeader.concat(s + ",");
			}
			dataProcessingHeader = dataProcessingHeader.substring(0, dataProcessingHeader.length() - 1) + "\n";
			header = dataProcessingHeader.concat(DATA_STRUCTURE_SHORT_NAME + "," + DATA_STRUCTURE_VERSION + ",\n");
			logger.info("header: " + header);
			String csv = "";
			csv = csv.concat(getCsvHeader());
			for(BiosampleCSVData row: biosampleRecords) {
				csv = csv.concat(row.printCSVRecord());
			}
			logger.info("CSV: " + csv);
			messages.add(header + csv);
			
			validationMessage = submitDataToImportRestful(messages);
			logger.info("Response is: " + validationMessage);
			
		} catch (JSchException | ProtocolException | IOException e) {
			logger.error("Error retrieving Biosample file " + fileName, e);
			return FAILED;
		}
		
		return validationMessage;

	}
	/**
	 * Connects to the SFTP server and grabs the Biosmaple raw file from IU
	 * @param fileName
	 * @return
	 * @throws JSchException
	 */

	private String getBiosampleFile(String fileName) throws JSchException {

		String username = modulesConstants.getBiosampleUsername();
		String url = modulesConstants.getBiosampleServer();
		String password = modulesConstants.getBiosamplePassword();
		
		
		//This is in case any of the non Biosample instances accidentally run the job. Just return out
		if(username == null || url == null || password == null) {
			return NOT_CONFIGURED;
		}
		
		StringBuffer sb = new StringBuffer();
		
		DatafileEndpointInfo info = new DatafileEndpointInfo();
		info.setUrl(url);
		info.setUserName(username);
		info.setPassword(password);
		info.setPort(22);
		
		sftp = SftpClientManager.getClient(info);
		
		
		
		try {
			InputStream stream = sftp.getFileStream("", fileName);
			InputStreamReader isReader = new InputStreamReader(stream);
		    BufferedReader reader = new BufferedReader(isReader);
		    String line;
		    reader.readLine(); //we don't care about the header, so just skip it.
		    while((line = reader.readLine())!= null){
		         sb.append(line + "\n");
		        
		    }
		} catch (Exception e) {
			logger.error("Unable to download file " + fileName, e);
			return FAILED;
		}
		
		return sb.toString();
		
	}
	
	/**
	 * Parses a CSV to a list of BiosampleCSVData objects
	 * @param s
	 * @return a list of BiosampleCSVData objects
	 */
	
	protected List<BiosampleCSVData> parseFileToPojo(String s){
		StringReader reader = new StringReader(s);
		CsvMapper mapper = new CsvMapper();
		CsvSchema schema = mapper.schemaFor(BiosampleCSVData.class).withoutHeader().withLineSeparator("\n").withColumnSeparator(',');
		
		List<BiosampleCSVData> list = new ArrayList<>();
		try {
			MappingIterator<BiosampleCSVData> r = mapper.readerFor(BiosampleCSVData.class).with(schema).readValues(reader);
			
			while(r.hasNext()) {
				BiosampleCSVData record = r.nextValue();
				list.add(record);
			}
			
		} catch (IOException e) {
		    e.printStackTrace();
		} 
		return list;
	}
	
	private String getCsvHeader() {
		 return CSV_HEADER;
	}
	
	/**
	 * Updater method to remove all UDALL and STEADYPDIII samples and update the repository to NINDS for the rest
	 * @param data
	 * @return The updated list
	 */
	protected List<BiosampleCSVData> updateBiosampleRecords(List<BiosampleCSVData> data){

		for(BiosampleCSVData record: data) {
			record.setRepositoryName(REPOSITORY);
		}
		return data;
		
	}
	
	/**
	 * Returns a string representation of a list with the opening/closing braces
	 * replaced with brackets
	 * 
	 * @param list
	 * @return The formatted string
	 */
	protected String formattedListToString(List<String> list) {
		String result = list.toString();
		result = result.replace("[", "");
		result = result.replace("]", "");
		return result;
	}
	
	
	protected String submitDataToImportRestful(List<String> messages)
			throws MalformedURLException, ProtocolException, IOException {
		BufferedReader resp = null;

		try {
			String data = formattedListToString(messages);
			String importRestfulUrl = modulesConstants.getImportRestfulUrl() + BIOSAMPLE_URL + REPOSITORY;
			URL url = new URL(importRestfulUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setDoOutput(true);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "text/plain");
			conn.setRequestProperty("charset", "utf-8");
			conn.setUseCaches(false);
			conn.setRequestProperty("Content-Length", String.valueOf(messages.toString().getBytes().length));

			OutputStream outStream = conn.getOutputStream();
			OutputStreamWriter outWriter = new OutputStreamWriter(outStream, "UTF-8");
			outWriter.write(data);
			outWriter.flush();
			outWriter.close();

			InputStream inputStream = conn.getInputStream();
			InputStreamReader streamRead = new InputStreamReader(inputStream);
			resp = new BufferedReader(streamRead);

			StringBuffer rData = new StringBuffer();
			String rDataLine = null;

			while ((rDataLine = resp.readLine()) != null) {
				rData.append(rDataLine);
				logger.info("response is: " + rDataLine);
			}

			logger.info("Web service to import-restful has been completed. Waiting for response.");
			
			if(rData.toString().equals(("Success"))) {
				return SUCCESS;
			}

			return FAILED;
		} finally {
			resp.close();
		}

	}
	

}

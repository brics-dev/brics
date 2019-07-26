package go.nih.tbi.client;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.Test;

import com.google.common.io.Files;

import gov.nih.tbi.RboxConstants;
import gov.nih.tbi.query.model.RboxRequest;
import gov.nih.tbi.query.model.RboxResponse;

public class RboxClientTest {
	
	
	@Test
	public void executeTest() throws IOException{
		/*
			In actual QT Client class, we will want to retrieve the datatable view results 
			via the CSVGenerator.generateCSV() method to retrieve the csv byte[], as we already
			have the data parsing logic available to us.
			Then pass the data via new String(bytes) in xml. (or just as byte[]? jaxb should encode as base64Binary by default)
			May need to encode/encrypt for security. Then, on server side,
			retrieve and decode as string, then import into R script.
			In this case, local files are being used for testing purposes.
		*/
		
		long clientStart = System.currentTimeMillis();
		long clientPrepStart = System.currentTimeMillis();
		
		String scriptFilePath = "C:/Users/rtromb/r_testing/RScript_3.txt";
		String dataFilePath = "C:/Users/rtromb/testdata2/biosampleCSVFlat_2016-05-05T14-52-48/query_result_BiosampleCatalogV3_2016-05-05T14-52-48.csv";
		
		File scriptFile = new File(scriptFilePath);
		File dataFile = new File(dataFilePath);
		
		//Read text from script file into String object, to be passed to the restful service
		StringBuilder scriptsb = new StringBuilder();
		BufferedReader reader = new BufferedReader(new FileReader(scriptFile));
    	String line = null;
    	while ((line = reader.readLine()) != null) {
    		scriptsb.append(" " + line + "\n");
    	}
    	
    	reader.close();
    	
        String scriptString = scriptsb.toString();
        String dataString = Files.toByteArray(dataFile).toString();
        
        //Populate jaxb request object with script and data Strings
        RboxRequest request = new RboxRequest();
        request.setScript(scriptString);
        request.setDataBytes(dataString.replaceAll("'", "`"));
        
        long clientPrepStop = System.currentTimeMillis();
        
        //Execute client post request and retrieve jaxb response object from server
        WebClient client = WebClient.create(RboxConstants.LOCAL_HOST + RboxConstants.RBOX_SERVICE_URL + "process");
		RboxResponse response = client.post(request, RboxResponse.class);
		
		long clientStop = System.currentTimeMillis();
		
		long clientTime = clientStop - clientStart;
		long clientPrepTime = clientPrepStop - clientPrepStart;
		
		System.out.println("Full client execution time (s): " + clientTime/1000);
		System.out.println("Client preparation execution time (s): " + clientPrepTime/1000);
		
		System.out.println(response.getConsoleOutput());
		System.out.println(response.getGraphImage());
	}

}
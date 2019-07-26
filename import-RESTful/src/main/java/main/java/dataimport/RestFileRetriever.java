package main.java.dataimport;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import gov.nih.tbi.commons.util.ValUtil;

public class RestFileRetriever {
	private static final Logger logger = Logger.getLogger(RestFileRetriever.class);

	private URL url;
	private File rootDir;
	private String filename;
	
	
	public RestFileRetriever(String urlString, String localDirectoryPath) throws IOException {
		// Initialize URL
		logger.info("Initializing URL: " + urlString);
		initializeSourceURL(urlString);
		
		// Initialize Destination File
		logger.info("Initializing FilePath: " + localDirectoryPath);
		initializeDestinationFile(localDirectoryPath);
	}


	private void initializeSourceURL(String urlString) throws MalformedURLException, IOException {
		url = new URL(urlString);
		
		String fileQuery = url.getQuery();
		
		if (ValUtil.isBlank(fileQuery) || !fileQuery.contains(".")
				|| !fileQuery.contains("=") && !filename.endsWith("=")) {
			
			logger.error("  URL is not well formed");
			throw new IOException("Cannot initialize url because filename pattern was not specified:" + urlString);
		} else {
			this.filename = fileQuery.substring(fileQuery.indexOf("=") + 1, fileQuery.length());
		}
		
		logger.info("  URL is well formed");
	}
	
	

	private void initializeDestinationFile(String localDirectoryPath) throws IOException {
		rootDir = new File(localDirectoryPath);

		if (!rootDir.isDirectory()) {
			logger.info(
					"  Cannot initialize destination because file specified is not a directory: " + localDirectoryPath);
			throw new IOException("Cannot initialize destination because file specified is not a directory: " + localDirectoryPath);
		}
	}
	
	
	public String getDestinationFileName() {
		return filename;
	}
	
	public void setDestinationFileName(String filename) {
		this.filename = filename;
	}
	
	public boolean copyFile() {
		boolean success = false;
		File file = new File(rootDir, filename);
		String fileName = url.toString().substring(url.toString().lastIndexOf('=') + 1);
		String encodedFileName = "";
		try {
			 encodedFileName  = URLEncoder.encode(fileName, "UTF-8");
			
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String urlWithEncodedFileName= url.getProtocol()+"://"+url.getHost()+url.getPath()+"?fileName="+encodedFileName;
		
		try {
			FileUtils.copyURLToFile(new URL(urlWithEncodedFileName), file);
			logger.info("  File retrieved, saved to: " + file.getAbsolutePath());
			success = true;
		} catch (IOException e) {
			logger.error("  Error Retrieving or Writing File" + file.getAbsolutePath(), e);
		}

		return success;
	}
	
	public static void main (String[] args) throws IOException {
        String urlString = "https://pdbp-stage.cit.nih.gov/proforms/ws/submission/download/250?fileName=1900B.PNG";
		String rootPath = "C:/temp";
		RestFileRetriever retriever = new RestFileRetriever(urlString, rootPath);
		
		retriever.copyFile();
	}
}

package gov.nih.tbi.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public interface TriplanarManager {

	public boolean isTriplanarReady(String studyName, String datasetName, String triplanarName)
			throws UnsupportedEncodingException, FileNotFoundException;

	/**
	 * This method checks if 4D info file exists in the output directory and returns the number of time slices if
	 * exists, otherwise returns 0.
	 * 
	 * @param studyName - Name of the study
	 * @param datasetName - name of the dataSet
	 * @param triplanarName - Original triplanar file name
	 * @return number of time slices for 4D image, 0 if not 4D.
	 * @throws NumberFormatException If the time slice value is not a number.
	 * @throws IOException If there is an error while reading from the triplanar image.
	 */
	public int check4DImage(String studyName, String datasetName, String triplanarName)
			throws NumberFormatException, IOException;


	/**
	 * Call the web service provider to generate the triplanar result zip file.
	 * 
	 * @param originalPath
	 * @param originalFile
	 * @param newPath
	 * @param newFile
	 * @return if the web service call is successful
	 */
	public boolean generateTriplanarImages(String originalPath, String originalFile, String newPath, String newFile);

	public String getOutputFilePath(String studyName, String datasetName, String triplanarName);

}

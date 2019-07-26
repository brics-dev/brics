package main.java.dataimport;

import gov.nih.tbi.ordermanager.model.DerivedBiosampleConfigurations;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import main.java.dataimport.exception.BiosampleRepositoryNotFound;
import main.java.dataimport.utils.DerivedDataUtils;

import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

/**
 * This class is used to write a new copy of the submission csv with dummy column headers, so we can use it to validate
 * the catalog before deriving data.
 * 
 * @author fchen
 *
 */
public class CatalogDummyDataService {
	private String submissionLocation;
	private int COLUMN_HEADER_LINE = 1;
	private static final Logger logger = Logger.getLogger(CatalogDummyDataService.class);
	private DerivedBiosampleConfigurations configurations;
	private String repositoryName;

	public CatalogDummyDataService(DerivedBiosampleConfigurations configurations, String submissionLocation,
			String repositoryName) {
		super();
		this.submissionLocation = submissionLocation;
		this.configurations = configurations;
		this.repositoryName = repositoryName;
	}

	/**
	 * Write the dummy catalog file to <working directory>/dummy/...
	 * 
	 * @return File object representing the dummy file that is written
	 * @throws BiosampleRepositoryNotFound
	 */
	public File writeDummyCatalog() throws BiosampleRepositoryNotFound {
		File file = new File(submissionLocation);
		logger.info("Original submission path: " + file.getAbsolutePath());
		String dummyFileLocation = file.getParent() + File.separator + "dummy" + File.separator + file.getName();
		File dummyFile = new File(dummyFileLocation);
		logger.info("Dummy file path: " + dummyFile.getAbsolutePath());
		CSVReader reader = null;
		CSVWriter dummyWriter = null;

		try {
			if (!dummyFile.exists()) { // create directory and new file
				if (dummyFile.getParentFile() != null) {
					dummyFile.getParentFile().mkdirs();
				}
				dummyFile.createNewFile();
			}

			reader = new CSVReader(new BufferedReader(new FileReader(file)));
			dummyWriter =
					new CSVWriter(new FileWriter(dummyFile), CSVWriter.DEFAULT_SEPARATOR, CSVWriter.NO_QUOTE_CHARACTER,
							CSVWriter.NO_ESCAPE_CHARACTER);
			int currentLineCount = 0;
			for (String[] line; (line = reader.readNext()) != null;) {
				if (currentLineCount == COLUMN_HEADER_LINE) { // reading column headers
					List<String> newColumns = null;

					newColumns = DerivedDataUtils.getDerivedDataColumns(configurations, repositoryName); // get the
																											// list
																											// of
																											// dummy
																											// columns
																											// to
																											// write


					logger.info("New Columns: " + newColumns);

					// use arraylist here because we are appending more items to an existing array. easier to read code
					// this way.
					List<String> newColumnHeaders = new ArrayList<String>();

					for (int i = 0; i < line.length; i++) {
						String currentColumnHeader = line[i];

						// i found that sometimes the column headers would have a trailing empty header. check it before
						// adding it to our column header list
						if (!currentColumnHeader.isEmpty()) {
							newColumnHeaders.add(currentColumnHeader);
						}
					}

					newColumnHeaders.addAll(newColumns);

					dummyWriter.writeNext(DerivedDataUtils.escapeLine(newColumnHeaders
							.toArray(new String[newColumnHeaders.size()])));

				} else { // if current line is not the column header line, just copy the line over
					dummyWriter.writeNext(DerivedDataUtils.escapeLine(line));
				}

				currentLineCount++;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (dummyWriter != null) {
					dummyWriter.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return dummyFile;
	}
}

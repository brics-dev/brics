package gov.nih.tbi.export.csv;

import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.exceptions.CSVGenerationException;
import gov.nih.tbi.pojo.DownloadPVMappingRow;
import gov.nih.tbi.pojo.InstancedDataTable;
import gov.nih.tbi.util.InstancedDataUtil;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import au.com.bytecode.opencsv.CSVWriter;

public class CSVGenerator {

	private static final Logger log = LogManager.getLogger(CSVGenerator.class.getName());

	// the CSV files we could be writing are quite large. With default buffer used by the writer being 8192 bytes, we
	// must increase the buffer size in order to reduce slow I/O calls.

	// this is in bytes. equates to ~100MB.
	final static public int WRITER_BUFFER_SIZE = 104857600;

	/**
	 * Convenience method to serialize a CSV using a given serializer
	 * 
	 * @param fileName
	 * @param instancedDataTable
	 * @param recordSerializer
	 * @return
	 */
	public static File generateCsvAux(String fileName, InstancedDataTable instancedDataTable,
			CsvRecordSerializer recordSerializer) {

		File tempFile = null;
		BufferedWriter writer = null;

		try {
			tempFile = File.createTempFile(fileName, ".csv");

			log.info("Writing data CSV to..." + tempFile.getAbsolutePath());
			writer = new BufferedWriter(new FileWriter(tempFile, true), WRITER_BUFFER_SIZE);

			// first write the CSV header
			writer.append(recordSerializer.serializeHeader());

			// then write all the records individually
			while (recordSerializer.hasNext()) {
				writer.append(recordSerializer.serializeNext());
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		} finally {
			log.info("Done!");
			try {
				if (writer != null) {
					writer.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return tempFile;
	}

	public static File generateCSV(String fileName, InstancedDataTable instancedDataTable, boolean showAgeRange)
			throws CSVGenerationException {
		NormalCsvRecordSerializer recordSerializer = new NormalCsvRecordSerializer(instancedDataTable, showAgeRange);

		return generateCsvAux(fileName, instancedDataTable, recordSerializer);
	}

	public static File generateFlattenedCSV(String fileName, InstancedDataTable instancedDataTable, boolean showAgeRange)
			throws CSVGenerationException {
		FlattenCsvRecordSerializer recordSerializer = new FlattenCsvRecordSerializer(instancedDataTable, showAgeRange);

		return generateCsvAux(fileName, instancedDataTable, recordSerializer);
	}

	/**
	 * Writes the data array into the CSV file using CSVWriter. This should properly close the writer stream as well.
	 * 
	 * @param file
	 * @param dataToWrite
	 * @throws CSVGenerationException
	 * @throws IOException
	 */
	private static byte[] writeCSVDataToByte(List<List<String>> dataToWrite) throws CSVGenerationException {

		CSVWriter csvWriter = null;
		List<String[]> dataToWritePrimitive = new ArrayList<String[]>();

		if (dataToWrite != null && !dataToWrite.isEmpty()) {
			for (List<String> rowToWrite : dataToWrite) {
				dataToWritePrimitive.add(rowToWrite.toArray(new String[rowToWrite.size()]));
			}
		}

		// use opencsv's CSVWriter!
		ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
		csvWriter = new CSVWriter(new PrintWriter(byteOutputStream));
		csvWriter.writeAll(dataToWritePrimitive);

		try {
			csvWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new CSVGenerationException();
		}

		byte[] bytes = byteOutputStream.toByteArray();

		try {
			byteOutputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new CSVGenerationException();
		}

		return bytes;
	}


	/**
	 * This method generates byte array for mapping file downloading.
	 * 
	 * @param mappings - list of DownloadPVMappingRow objects
	 * @return - byte array for mapping file downloading
	 * @throws CSVGenerationException
	 */
	public static byte[] generateMappingCSV(List<DownloadPVMappingRow> mappings, String displayOption)
			throws CSVGenerationException {

		boolean displaySchema = InstancedDataUtil.isDisplaySchema(displayOption);

		List<List<String>> table = new ArrayList<List<String>>();

		List<String> header = new ArrayList<String>();
		header.add(QueryToolConstants.NAME_READABLE);
		header.add(QueryToolConstants.TITLE_READABLE);
		header.add(QueryToolConstants.ELEMENT_TYPE_READABLE);
		header.add(QueryToolConstants.VERSION_READABLE);
		header.add(QueryToolConstants.DEFINITION_READABLE);
		header.add(QueryToolConstants.SHORT_DESCRIPTION_READABLE);
		header.add(QueryToolConstants.DATA_TYPE_READABLE);
		header.add(QueryToolConstants.MAX_CHAR_QUANTITY_READBLE);
		header.add(QueryToolConstants.INPUT_RESTRICTION_READABLE);
		header.add(QueryToolConstants.MINIMUM_VALUE_READABLE);
		header.add(QueryToolConstants.MAXIMUM_VALUE_READABLE);
		header.add(QueryToolConstants.PERMISSIBLE_VALUES_READABLE);
		header.add(QueryToolConstants.PERMISSIBLE_VALUES_DESCRIPTION_READABLE);
		header.add(QueryToolConstants.PERMISSIBLE_VALUES_OUTPUT_CODES_READABLE);
		header.add(QueryToolConstants.ITEM_RESPONSE_OID_READABLE);
		header.add(QueryToolConstants.ELEMENT_OID_READABLE);
		header.add(QueryToolConstants.UNIT_OF_MEASURE_READABLE);
		header.add(QueryToolConstants.GUIDELINES_READABLE);
		header.add(QueryToolConstants.NOTES_READABLE);
		header.add(QueryToolConstants.PREFERRED_QUESTION_TEXT_READABLE);
		header.add(QueryToolConstants.KEYWORDS_READABLE);
		header.add(QueryToolConstants.REFERENCES_READABLE);
		header.add(QueryToolConstants.POPULATION_ALL_READABLE);
		header.add(QueryToolConstants.HISTORICAL_NOTES_READABLE);
		header.add(QueryToolConstants.LABELS_READABLE);
		header.add(QueryToolConstants.SEE_ALSO_READABLE);
		header.add(QueryToolConstants.SUBMITTING_ORG_NAME_READABLE);
		header.add(QueryToolConstants.SUBMITTING_CONTACT_NAME_READABLE);
		header.add(QueryToolConstants.SUBMITTING_CONTACT_INFO_READABLE);
		header.add(QueryToolConstants.EFFECTIVE_DATE_READABLE);
		header.add(QueryToolConstants.UNTIL_DATE_READABLE);
		header.add(QueryToolConstants.KEYWORDS_READABLE);
		header.add(QueryToolConstants.STEWARD_ORG_NAME_READABLE);
		header.add(QueryToolConstants.STEWARD_CONTACT_NAME_READABLE);
		header.add(QueryToolConstants.STEWARD_CONTACT_INFO_READABLE);

		if (displaySchema) {
			header.add(displayOption + " ID");
			header.add(displayOption + " values");
		}

		table.add(header);

		for (DownloadPVMappingRow mapping : mappings) {
			List<String> row = new ArrayList<String>();
			row.add(mapping.getDeName());
			row.add(mapping.getDeTitle());
			row.add(mapping.getElementType());
			row.add(mapping.getVersion());
			row.add(mapping.getDefinition());
			row.add(mapping.getShortDescription());
			row.add(mapping.getDataType());
			row.add(mapping.getMaxCharQuantity());
			row.add(mapping.getInputRestriction());
			row.add(mapping.getMinVal());
			row.add(mapping.getMaxVal());
			row.add(mapping.getPvValue());
			row.add(mapping.getPvDesciption());
			row.add(mapping.getPvCode());
			row.add(mapping.getItemResponseOID());
			row.add(mapping.getElementOID());
			row.add(mapping.getUnitOfMeasurement());
			row.add(mapping.getGuidelines());
			row.add(mapping.getNotes());
			row.add(mapping.getPreferredQuestionText());
			row.add(mapping.getKeywords());
			row.add(mapping.getReferences());
			row.add(mapping.getPopulationAll());
			row.add(mapping.getHistoricalNotes());
			row.add(mapping.getLabels());
			row.add(mapping.getSeeAlso());
			row.add(mapping.getSubmittingOrgName());
			row.add(mapping.getSubmittingContactName());
			row.add(mapping.getSubmittingContactInformation());
			row.add(mapping.getEffectiveDate());
			row.add(mapping.getUntilDate());
			row.add(mapping.getStewardOrgName());
			row.add(mapping.getStewardContactName());
			row.add(mapping.getStewardContactInfo());
			

			if (displaySchema) {
				row.add(mapping.getSchemaDeId());
				row.add(mapping.getSchemaValue());
			}

			table.add(row);
		}

		return writeCSVDataToByte(table);
	}
}

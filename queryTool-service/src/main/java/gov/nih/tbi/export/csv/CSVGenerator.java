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
	// must increase the buffer size in order to reduce slow I/O calls. Trying out 512Kb here to see if it improves
	// performance.

	// this is in bytes. equates to 512Kb.
	final static public int WRITER_BUFFER_SIZE = 524288;

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
			writer = new BufferedWriter(new FileWriter(tempFile), WRITER_BUFFER_SIZE);

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

	public static File generateCSV(String fileName, InstancedDataTable instancedDataTable)
			throws CSVGenerationException {
		NormalCsvRecordSerializer recordSerializer = new NormalCsvRecordSerializer(instancedDataTable);

		return generateCsvAux(fileName, instancedDataTable, recordSerializer);
	}

	public static File generateFlattenedCSV(String fileName, InstancedDataTable instancedDataTable)
			throws CSVGenerationException {
		FlattenCsvRecordSerializer recordSerializer = new FlattenCsvRecordSerializer(instancedDataTable);

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
		header.add(QueryToolConstants.SHORT_DESCRIPTION_READABLE);
		header.add(QueryToolConstants.PERMISSIBLE_VALUES_READABLE);
		header.add(QueryToolConstants.PERMISSIBLE_VALUES_DESCRIPTION_READABLE);
		header.add(QueryToolConstants.PERMISSIBLE_VALUES_OUTPUT_CODES_READABLE);

		if (displaySchema) {
			header.add(displayOption + " ID");
			header.add(displayOption + " values");
		}

		table.add(header);

		for (DownloadPVMappingRow mapping : mappings) {
			List<String> row = new ArrayList<String>();
			row.add(mapping.getDeName());
			row.add(mapping.getDeTitle());
			row.add(mapping.getDeDescription());
			row.add(mapping.getPvValue());
			row.add(mapping.getPvDesciption());
			row.add(mapping.getPvCode());

			if (displaySchema) {
				row.add(mapping.getSchemaDeId());
				row.add(mapping.getSchemaValue());
			}

			table.add(row);
		}

		return writeCSVDataToByte(table);
	}
}

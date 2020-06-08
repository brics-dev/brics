package gov.nih.tbi.export.json;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import gov.nih.tbi.pojo.InstancedDataTable;

/**
 * This class is responsible for serializing and generating the JSON file using
 * an InstancedDataTable object. Generating the JSON streams the JSON file to
 * temporary storage using a buffered writer, this ensures the memory usage
 * remain constant regardless of the size of the file being generated.
 * 
 * @author Francis Chen
 *
 */
public class JsonGenerator {
	// the JSON files we could be writing are quite large. With default buffer used
	// by the writer being 8192 bytes, we
	// must increase the buffer size in order to reduce slow I/O calls. Trying out
	// 512Kb here to see if it improves
	// performance.

	// this is in bytes. equates to 512Kb.
	final static public int WRITER_BUFFER_SIZE = 524288;

	private static final String JSON_ARRAY_START = "[\n";
	private static final String JSON_ARRAY_END = "\n]";
	private static final Logger log = LogManager.getLogger(JsonGenerator.class.getName());

	private static File generateJsonAux(String fileName, InstancedDataTable instancedDataTable,
			JsonRecordSerializer recordSerializer) {
		File tempFile = null;
		BufferedWriter writer = null;
		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		try {
			tempFile = File.createTempFile(fileName, ".json");

			log.info("Writing data JSON to..." + tempFile.getAbsolutePath());
			writer = new BufferedWriter(new FileWriter(tempFile), WRITER_BUFFER_SIZE);

			// first write the JSON array start
			writer.append(JSON_ARRAY_START);

			// then write all the records individually
			while (recordSerializer.hasNext()) {
				writer.append(gson.toJson(recordSerializer.serializeNext()));

				if (recordSerializer.hasNext()) {
					writer.append(",\n");
				}
			}

			// first write the JSON array end
			writer.append(JSON_ARRAY_END);
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

	public static File generateJson(String fileName, InstancedDataTable instancedDataTable) {
		SimpleJsonRecordSerializer serializer = new SimpleJsonRecordSerializer(instancedDataTable);
		return generateJsonAux(fileName, instancedDataTable, serializer);
	}
}

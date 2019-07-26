package gov.nih.tbi.repository.rdf;

import gov.nih.tbi.commons.model.BRICSTimeDateUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Date;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;

public class RDFFileWriteUtil {
	public static final String RDF_FORMAT = "Turtle";
	static Logger log = Logger.getLogger(RDFFileWriteUtil.class);

	private String directory;
	private Date timestamp;

	public RDFFileWriteUtil(String directory, Date timestamp) {
		super();
		this.directory = directory;
		this.timestamp = timestamp;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	public synchronized void writeToFile(Model m) throws IOException {
		if(m == null || m.isEmpty() || m.isClosed()) {
			return;
		}
		
		String filePath = generateFilePath(directory, timestamp);
		Writer fout = null;
		try {

			File file = new File(filePath);
			if (file.exists() == false) {
				if (file.getParentFile() != null) {
					file.getParentFile().mkdirs();
				}

				file.createNewFile();
			}
			fout = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true), "utf-8"));
			m.write(fout, RDFFileWriteUtil.RDF_FORMAT);
		} catch (IOException e) {
			log.error("Exception caught" + e.getMessage());
			e.printStackTrace();
		} finally {
			if (fout != null) {
				fout.close();
			}

			m.close();
		}

		log.info("******************************");
		log.info("File Written :" + filePath);
		log.info("******************************");
	}

	public static String generateFilePath(String directory, Date timestamp) {
		String timestampString = BRICSTimeDateUtil.formatTimeStamp(timestamp);
		return directory + RDFConstants.FILE_NAME_ALL + "_" + timestampString;
	}
}

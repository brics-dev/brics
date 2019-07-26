package gov.nih.tbi.commons.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.log4j.Logger;

public class BRICSZipUtil {
	private static final Logger log = Logger.getLogger(BRICSZipUtil.class);

	private static int DEFAULT_WRITER_BUFFER = 4092;

	/**
	 * Writes the files from fileEntries to the given zip file. Uses 4092 bytes as the default.
	 * 
	 * @param fileEntries
	 * @param zipFile
	 */
	public static void writeZipFile(List<File> fileEntries, File zipFile) {
		writeZipFile(fileEntries, zipFile, DEFAULT_WRITER_BUFFER);
	}

	/**
	 * Writes the files from fileEntries to the given zip file. Must also provide a buffer size.
	 * 
	 * @param fileEntries
	 * @param zipFile
	 * @param bufferSize
	 */
	public static void writeZipFile(List<File> fileEntries, File zipFile, int bufferSize) {

		log.info("Writing zip: " + zipFile.getAbsolutePath() + "...");

		ZipOutputStream zos = null;

		try {
			zos = new ZipOutputStream(new FileOutputStream(zipFile));

			// add data files to zip
			for (File fileEntry : fileEntries) {
				log.info("Adding " + fileEntry.getAbsolutePath() + " to the zip...");
				ZipEntry entry = new ZipEntry(fileEntry.getName());
				zos.putNextEntry(entry);

				FileInputStream in = new FileInputStream(fileEntry);

				// write the data file to the zip entry
				try {
					byte[] buffer = new byte[bufferSize];

					int len = 0;
					while ((len = in.read(buffer)) > 0) {
						zos.write(buffer, 0, len);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					in.close();
					zos.closeEntry();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (zos != null) {
				try {
					zos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}
}

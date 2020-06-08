package gov.nih.tbi.commons.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.lang3.RandomStringUtils;

public class BRICSFilesUtils {
	private static final int RANDOM_STR_LENGTH = 16;

	/**
	 * Takes a file name or path and either appends a file extension or replaces the existing file extension
	 * 
	 * @param fileName
	 * @param extension
	 * @return
	 */
	public static String modifyFileExtension(String fileName, String extension) {
		String fileNameSansExtension;
		int periodIndex = fileName.lastIndexOf(".");

		if (periodIndex >= 0) {
			fileNameSansExtension = fileName.substring(0, periodIndex);
		} else {
			fileNameSansExtension = fileName;
		}

		return fileNameSansExtension + extension;
	}

	/**
	 * Reads the file in the specified path into a string and return
	 * 
	 * @param path
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return new String(encoded, encoding);
	}

	/**
	 * Reads the file in the specified path into a string and return
	 * 
	 * @param file
	 * @param encoding
	 * @return
	 * @throws IOException
	 */
	public static String readFile(File file, Charset encoding) throws IOException {
		return BRICSFilesUtils.readFile(file.getAbsolutePath(), encoding);
	}

	/**
	 * Helper method that copies the contents of the source file into a new temporary file. Please delete the temporary
	 * file if it is no longer being used.
	 * 
	 * @param source - The source file to read from.
	 * @return A copy of the source file with a generic auto generated name.
	 * @throws IOException When there is an error copying the file.
	 */
	public static synchronized File copyFile(File source) throws IOException {
		if ((source == null) || !source.isFile()) {
			return null;
		}

		String randomStr = RandomStringUtils.randomAlphanumeric(BRICSFilesUtils.RANDOM_STR_LENGTH);
		File newFile = File.createTempFile("uploadedFile-" + randomStr, ".tmp");
		FileInputStream inFile = new FileInputStream(source);
		FileOutputStream outFile = new FileOutputStream(newFile);

		// Copy the source file.
		try {
			FileChannel inChan = inFile.getChannel();
			FileChannel outChan = outFile.getChannel();

			outChan.transferFrom(inChan, 0, source.length());
		} finally {
			inFile.close();
			outFile.close();
		}

		// Since this is a temp file, setting it to be deleted when the JVM exists. Just
		// in case someone forgets to delete it after it's no longer being used.
		newFile.deleteOnExit();

		return newFile;
	}

	public static String getFileExtension(String filename) {

		int lastIndexOf = filename.lastIndexOf(".");
		if (lastIndexOf == -1) {
			return ""; // empty extension
		}
		return filename.substring(lastIndexOf + 1);
	}
}

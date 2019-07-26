package gov.nih.tbi.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

public class StreamingFileOutput implements StreamingOutput {

	private File file;

	/**
	 * Constructor that will store a reference to a Java file handler, which will be used later to send the file data to
	 * the client browser or server.
	 * 
	 * @param inFile - The Java file handler to be used later for uploading the file's data.
	 * @throws WebApplicationException When there is a validation error with the passed in file object.
	 */
	public StreamingFileOutput(File inFile) throws WebApplicationException {
		// Check if the given file is null.
		if (inFile != null) {
			// Verify if the given file exists.
			if (inFile.exists()) {
				setFile(inFile);
			} else {
				throw new InternalServerErrorException("The passed in file (" + inFile.getName() + ") doesn't exist.");
			}
		} else {
			throw new InternalServerErrorException("The passed in file is null.");
		}
	}

	/**
	 * Writes the data from the stored file object out to the provided response output stream.
	 * 
	 * @param out - The response output stream (Note: CFX will close this output stream on its own)
	 * @throws IOException When an access error occurs from the server's file system.
	 * @throws WebApplicationException When any other error occurs.
	 */
	@Override
	public void write(OutputStream out) throws IOException, WebApplicationException {
		FileInputStream in = new FileInputStream(file);
		byte[] buffer = new byte[1024];
		int bytesRead = 0;

		try {
			// Stream the file out to the user's browser
			bytesRead = in.read(buffer);

			while (bytesRead > 0) {
				out.write(buffer, 0, bytesRead);
				out.flush();
				bytesRead = in.read(buffer);
			}
		} finally {
			in.close();
		}
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

}

package gov.nih.cit.brics.file.mvc.model;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.StandardOpenOption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import gov.nih.cit.brics.file.util.FileRepositoryConstants;

public class FileStreamingResponseBody implements StreamingResponseBody {
	private static final Logger logger = LoggerFactory.getLogger(FileStreamingResponseBody.class);

	private File data;

	public FileStreamingResponseBody() {}

	public FileStreamingResponseBody(File file) {
		this.data = file;
	}

	@Override
	public void writeTo(OutputStream outputStream) throws IOException {
		FileChannel inChannel = FileChannel.open(data.toPath(), StandardOpenOption.READ);
		WritableByteChannel outChannel = Channels.newChannel(outputStream);

		logger.info("Sending file data for \"{}\"...", data.getName());

		// Send the file contents through the response.
		try {
			long bytesTransferred = 0L;

			// Check if the file size exceeds the download chunk size limit.
			if (data.length() > FileRepositoryConstants.DOWNLOAD_CHUNK_SIZE) {
				long bytesRemaining = data.length();

				// Send file data to through the response in chunks.
				while (bytesRemaining > 0) {
					long bytesSent = inChannel.transferTo(bytesTransferred,
							FileRepositoryConstants.DOWNLOAD_CHUNK_SIZE, outChannel);

					// Update byte counters.
					bytesTransferred += bytesSent;
					bytesRemaining -= bytesSent;
				}
			} else {
				bytesTransferred = inChannel.transferTo(0L, data.length(), outChannel);
			}

			if (bytesTransferred != data.length()) {
				logger.warn("Some bytes of \"{}\" didn't get sent. Out of {} bytes. Only {} bytes were sent.",
						new Object[] {data.getName(), data.length(), bytesTransferred});
			}
		} finally {
			inChannel.close();
			outChannel.close();
		}
	}

	public File getData() {
		return data;
	}

	public void setData(File data) {
		this.data = data;
	}

}

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

import gov.nih.cit.brics.file.exception.HttpRangeOutOfBoundsException;
import gov.nih.cit.brics.file.util.FileRepositoryConstants;

public class PartialFileStreamingResponseBody implements StreamingResponseBody {
	private static final Logger logger = LoggerFactory.getLogger(PartialFileStreamingResponseBody.class);

	private File data;
	private long rangeStart;
	private long rangeEnd;

	public PartialFileStreamingResponseBody() {
		this.rangeStart = 0L;
		this.rangeEnd = 0L;
	}

	/**
	 * Constructor that sets the ranges and file. The given ranges will also be validated.
	 * 
	 * @param file - A handle to the file that will be streamed to a response.
	 * @param rangeStart - The starting position of the file to be streamed.
	 * @param rangeEnd - The ending position of the file to be streamed.
	 * @throws HttpRangeOutOfBoundsException - If the given range is not valid or outside of the bounds of the file.
	 */
	public PartialFileStreamingResponseBody(File file, long rangeStart, long rangeEnd)
			throws HttpRangeOutOfBoundsException {
		this.data = file;
		this.rangeStart = rangeStart;
		this.rangeEnd = rangeEnd;

		// Validate the ranges.
		if (!validateRanges()) {
			throw new HttpRangeOutOfBoundsException(
					String.format("The range (%1$d, %2$d) is outside of the bounds of the file (%3$s).", rangeStart,
							rangeEnd, file.getName()));
		}
	}

	@Override
	public void writeTo(OutputStream outputStream) throws IOException {
		// Check ranges before beginning.
		if (!validateRanges()) {
			throw new IOException(String.format("The range (%1$d, %2$d) is outside of the bounds of the file (%3$s).",
					rangeStart, rangeEnd, data.getName()));
		}

		FileChannel inChannel = FileChannel.open(data.toPath(), StandardOpenOption.READ);
		WritableByteChannel outChannel = Channels.newChannel(outputStream);
		long bytesToTransfer = rangeEnd - rangeStart + 1L;

		logger.info("Sending data for \"{}\" from {} to {} with a total of {} bytes to be sent.",
				new Object[] {data.getName(), rangeStart, rangeEnd, bytesToTransfer});

		// Send the file contents through the response.
		try {
			long bytesTransferred = 0L;

			// Check if the file size exceeds the download chunk size limit.
			if (bytesToTransfer > FileRepositoryConstants.DOWNLOAD_CHUNK_SIZE) {
				long bytesRemaining = bytesToTransfer;
				long filePosition = rangeStart;

				// Send file data to through the response in chunks.
				while (bytesRemaining > 0) {
					long bytesSent =
							inChannel.transferTo(filePosition, FileRepositoryConstants.DOWNLOAD_CHUNK_SIZE, outChannel);

					// Update byte counters.
					bytesTransferred += bytesSent;
					filePosition += bytesSent;
					bytesRemaining -= bytesSent;
				}
			} else {
				bytesTransferred = inChannel.transferTo(rangeStart, bytesToTransfer, outChannel);
			}

			// Check if all of the requested bytes were transferred.
			if ((bytesTransferred != bytesToTransfer) && logger.isWarnEnabled()) {
				logger.warn("Some bytes of \"{}\" didn't get sent. Out of {} bytes. Only {} bytes were sent.",
						new Object[] {data.getName(), bytesToTransfer, bytesTransferred});
			}
		} finally {
			inChannel.close();
			outChannel.close();
		}
	}

	/**
	 * Checks and validates the ranges assigned to this object, and also factors in the file size as well.
	 * 
	 * @return True if and only if the assigned ranges are valid and within the bounds of the assigned file.
	 */
	public boolean validateRanges() {
		long fileSize = data.length();

		if ((rangeStart >= 0L) && (rangeStart <= rangeEnd) && (rangeStart <= fileSize) && (rangeEnd <= fileSize)) {
			return true;
		}

		return false;
	}

	public File getData() {
		return data;
	}

	public void setData(File data) {
		this.data = data;
	}

	public long getRangeStart() {
		return rangeStart;
	}

	public void setRangeStart(long rangeStart) {
		this.rangeStart = rangeStart;
	}

	public long getRangeEnd() {
		return rangeEnd;
	}

	public void setRangeEnd(long rangeEnd) {
		this.rangeEnd = rangeEnd;
	}

}

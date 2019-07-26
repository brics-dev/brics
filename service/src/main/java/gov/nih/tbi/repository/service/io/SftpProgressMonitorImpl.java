package gov.nih.tbi.repository.service.io;

import org.apache.log4j.Logger;

import com.jcraft.jsch.SftpProgressMonitor;

/**
 * Progress tracker for SFTP transfers over using the JSCH library.
 * 
 */
public class SftpProgressMonitorImpl implements SftpProgressMonitor {

	static Logger log = Logger.getLogger(SftpProgressMonitorImpl.class);

	private long totalSize;
	private long transferred = 0;
	private boolean countReturn = true;

	/**
	 * Sets the total size of the file being transferred because init() does not always get called.
	 * 
	 * @param totalSize
	 */

	public SftpProgressMonitorImpl(long totalSize) {

		this.totalSize = totalSize;
	}

	public void cancel() {

		countReturn = false;
	}

	/*
	 * (non-Javadoc) This updates the transferred by the count since the count is not cumulative.
	 * 
	 * @see com.jcraft.jsch.SftpProgressMonitor#count(long)
	 */
	public boolean count(long count) {

		transferred += count;
		log.debug(transferred + " / " + totalSize);
		return (countReturn);
	}

	public void end() {

		log.debug("Transfer Complete");
	}

	/**
	 * Returns the percentage of the transfer completed so far as an integer.
	 * 
	 * @return
	 */
	public int getPercentComplete() {

		float pct = (float) transferred / totalSize * 100;
		return countReturn ? (int) pct : 0;
	}

	public float getTransferred() // This was a float
	{

		return transferred;
	}

	/*
	 * (non-Javadoc) This doesn't get called all the time!
	 * 
	 * @see com.jcraft.jsch.SftpProgressMonitor#init(int, java.lang.String, java.lang.String, long)
	 */
	public void init(int op, String src, String dest, long max) {

		/*
		 * This doesn't get called for every put function in JSCH so be careful!
		 * 
		 * Use the constructor to pass in the size at initialization instead.
		 */
	}
}

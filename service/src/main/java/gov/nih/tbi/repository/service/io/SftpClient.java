
package gov.nih.tbi.repository.service.io;

import java.awt.Container;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.util.Iterator;
import java.util.Vector;
import java.util.regex.Pattern;

import org.apache.cxf.helpers.IOUtils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelSftp.LsEntry;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

import gov.nih.tbi.commons.service.ServiceConstants;

/**
 * Wrapper class for JSCH
 * 
 * @author Andrew Johnson
 * 
 */
public class SftpClient {

	public static final String FILE_EXTENSION_SEPARATOR = ".";
	public static final String TMP_FILE_SUFFIX = ".tmp";
	public static final String ROOT_DIR = "/";
	public static final String HOME_ROOT_DIR = "/home/";

	String userName;
	String password;
	String host;
	String passphrase;
	Integer port;

	Session session;

	// array list of all of the open channels for this client. this is primarily so we can properly close all of the
	// channels if close is called on this client.
	// private List<ChannelSftp> openChannels = new ArrayList<ChannelSftp>();
	private Vector<ChannelSftp> openChannels = new Vector<ChannelSftp>();

	/**
	 * Protected to ensure only the SftpClientManager can create new clients
	 * 
	 * @param userName
	 * @param password
	 * @param host
	 * @param port
	 * @throws JSchException
	 * @throws InterruptedException
	 */
	protected SftpClient(String userName, String password, String host, Integer port) throws JSchException {

		this.userName = userName;
		this.password = password;
		this.host = host;
		this.port = port;

		// Connect to host
		try {
			JSch jsch = new JSch();

			session = jsch.getSession(userName, host, port);
			session.setConfig(ServiceConstants.STRICT_HOST_KEY_CHECK, ServiceConstants.NO);
			session.setPassword(password);
			session.connect();
		} catch (JSchException e) {
			throw e;
		}
	}

	/**
	 * Creates a connection using a passphrase and a
	 * 
	 * @param userName
	 * @param passphrase
	 * @param privateKey
	 * @param host
	 * @throws JSchException
	 */
	protected SftpClient(String userName, String passphrase, String privateKey, String host, Integer port)
			throws JSchException {

		this.userName = userName;
		this.passphrase = passphrase;
		this.host = host;
		this.port = port;

		// Connect to host

		try {
			JSch jsch = new JSch();
			jsch.addIdentity(privateKey);
			session = jsch.getSession(userName, host, port);
			UserInfo ui = new SFTPUserInfo();
			session.setUserInfo(ui);
			session.connect();
		} catch (JSchException e) {
			throw e;
		}

	}

	/**
	 * Download the file (title) at the specified location (filepath) and store at a specified location (dest)
	 * 
	 * @param title
	 * @param filePath
	 * @param dest
	 * @throws Exception
	 */
	public void download(String title, String filePath, String dest) throws Exception {

		ChannelSftp sftpChannel = openSftpChannel();
		try {
			File localDirFile = new File(dest);

			if (!localDirFile.exists()) {
				localDirFile.mkdirs();
			}
			navigateToFolder(sftpChannel, filePath);
			sftpChannel.get(title, dest);
		} catch (Exception e) {
			throw e;
		} finally {
			// Exit the channel
			closeSftpChannel(sftpChannel);
		}
	}

	public void download(String title, String filePath, String dest, SimpleProgressMonitor simple) throws Exception {

		ChannelSftp sftpChannel = openSftpChannel();
		try {
			File localDirFile = new File(dest);

			if (!localDirFile.exists()) {
				localDirFile.mkdirs();
			}

			File dup = new File(dest + File.separator + title);

			if (dup.exists()) {
				navigateToFolder(sftpChannel, filePath);
				String dupFileName = dupIndexConcat(dest, title);
				sftpChannel.get(title, dest + File.separator + dupFileName, simple, ChannelSftp.OVERWRITE);
			} else {
				navigateToFolder(sftpChannel, filePath);
				sftpChannel.get(title, dest, simple, ChannelSftp.OVERWRITE);
			}

		} catch (Exception e) {
			throw e;
		} finally {
			// Exit the channel
			closeSftpChannel(sftpChannel);
		}
	}

	public void download(String title, String filePath, String dest, SftpProgressMonitorImpl progress)
			throws SftpException, JSchException {

		ChannelSftp sftpChannel = null;

		try {
			sftpChannel = openSftpChannel();

			File localDirFile = new File(dest);

			if (!localDirFile.exists()) {
				localDirFile.mkdirs();
			}
			File dup = new File(dest + File.separator + title);
			if (dup.exists()) {
				navigateToFolder(sftpChannel, filePath);
				String dupFileName = dupIndexConcat(dest, title);
				sftpChannel.get(title, dest + File.separator + dupFileName, progress, ChannelSftp.OVERWRITE);
			} else {
				navigateToFolder(sftpChannel, filePath);
				sftpChannel.get(title, dest, progress, ChannelSftp.OVERWRITE);
			}

		} catch (SftpException e) {
			throw e;
		} catch (JSchException e) {
			throw e;
		} finally {
			// Exit the channel
			closeSftpChannel(sftpChannel);
		}
	}

	/**
	 * Downloads the specified file from the sftp. This method directly overwrites files with the same name instead of
	 * renaming the new file.
	 * 
	 * @param title
	 * @param filePath
	 * @param dest
	 * @param progress
	 * @throws SftpException
	 * @throws JSchException
	 */
	public void downloadOverwrite(String title, String filePath, String dest, SftpProgressMonitorImpl progress)
			throws SftpException, InterruptedException, JSchException {

		ChannelSftp sftpChannel = null;

		try {
			// sftpChannel = openSftpChannel();
			sftpChannel = openSftpChannel(ServiceConstants.RETRY_CHANNEL_CONNECTION);

			File localDirFile = new File(dest);

			if (!localDirFile.exists()) {
				localDirFile.mkdirs();
			}

			navigateToFolder(sftpChannel, filePath);
			sftpChannel.get(title, dest, progress, ChannelSftp.OVERWRITE);

		} catch (SftpException e) {
			throw e;
		} catch (InterruptedException e) {
			throw e;
		} catch (JSchException e) {
			throw e;
		} finally {
			// Exit the channel
			closeSftpChannel(sftpChannel);
		}
	}

	public InputStream download(String title, String filePath) throws Exception {

		ChannelSftp sftpChannel = openSftpChannel();
		InputStream toReturn = null;
		try {
			navigateToFolder(sftpChannel, filePath);
			toReturn = sftpChannel.get(title);
		} catch (Exception e) {
			throw e;
		} finally {
			closeSftpChannel(sftpChannel);
		}
		return toReturn;
	}

	public byte[] downloadBytes(String title, String filePath) throws Exception {

		ChannelSftp sftpChannel = openSftpChannel();
		InputStream fileInputStream;
		try {
			navigateToFolder(sftpChannel, filePath);
			fileInputStream = sftpChannel.get(title);
			byte[] data = null;
			data = IOUtils.readBytesFromStream(fileInputStream);
			return data;
		} catch (Exception e) {
			throw e;
		} finally {
			closeSftpChannel(sftpChannel);
		}

	}

	/**
	 * Downloads a file form the SFTP site to a temporary file generated and returned by this method. The temporary file
	 * is stored in the JVM's default temporary directory. See {@link java.io.File#createTempFile(String, String)} for
	 * further details. Since what is returned is a temporary copy of the file that is stored in the SFTP site, the
	 * returned file should be deleted once its use has expired. The returned file has also been marked to be deleted
	 * once the JVM exits. This auto deletion is not guaranteed, and should not be relied upon.
	 * 
	 * @param title - The file's name, which should include the extension.
	 * @param filePath - The path to the file. Including a trailing slash.
	 * @return A temporary file, which is a copy of referenced file from the SFTP site.
	 * @throws IOException When there is an error creating or writing to the temporary file.
	 * @throws JSchException When there is an error with opening a connection to the SFTP site.
	 * @throws SftpException When there is an error with reading the referenced file from the SFTP site.
	 */
	public File downloadFile(String title, String filePath) throws IOException, JSchException, SftpException {
		int extensionIndx = title.lastIndexOf(SftpClient.FILE_EXTENSION_SEPARATOR);
		File df = null;

		// Check if the title or file name has a file extension associated with it.
		if (extensionIndx >= 0) {
			df = File.createTempFile(title.substring(0, extensionIndx), title.substring(extensionIndx));
		} else {
			df = File.createTempFile(title, SftpClient.TMP_FILE_SUFFIX);
		}

		// Download file from SFTP site.
		ChannelSftp sftpChannel = openSftpChannel();
		ReadableByteChannel sftpInChan = null;
		FileOutputStream fileOut = new FileOutputStream(df);
		FileChannel fileOutChan = fileOut.getChannel();

		try {
			SftpATTRS sftpFileAttrs = sftpChannel.lstat(filePath + title);
			long sftpFileSize = sftpFileAttrs.getSize();

			navigateToFolder(sftpChannel, filePath);
			sftpInChan = Channels.newChannel(new BufferedInputStream(sftpChannel.get(title)));
			fileOutChan.transferFrom(sftpInChan, 0, sftpFileSize);
		} finally {
			closeFileChannel(sftpInChan);
			closeFileChannel(fileOutChan);
			fileOut.close();
			closeSftpChannel(sftpChannel);
		}

		// Since this is a temp file, setting it to be deleted when the JVM exists. Just
		// in case someone forgets to delete it after it's no longer being used.
		df.deleteOnExit();

		return df;
	}

	/**
	 * upload the file
	 * 
	 * @param upload
	 * @param filePath
	 * @param fileName
	 * @throws JSchException 
	 * @throws IOException 
	 * @throws SftpException 
	 * @throws Exception
	 */
	public void upload(File upload, String filePath, String fileName) throws JSchException, IOException, SftpException {

		ChannelSftp sftpChannel = openSftpChannel();
		FileInputStream in = null;

		try {
			navigateToFolder(sftpChannel, filePath);
			in = new FileInputStream(upload);
			sftpChannel.put(in, fileName);
		} finally {
			closeSftpChannel(sftpChannel);

			if (in != null) {
				in.close();
			}
		}
	}

	/**
	 * Uploads a file and provides updates to the SftpProgressMonitorImpl.
	 * 
	 * @param upload
	 * @param filePath
	 * @param fileName
	 * @param progress
	 * @throws Exception
	 */
	public void upload(File upload, String filePath, String fileName, SftpProgressMonitorImpl progress)
			throws Exception {

		ChannelSftp sftpChannel = openSftpChannel();

		try {
			navigateToFolder(sftpChannel, filePath);

			sftpChannel.put(new FileInputStream(upload), fileName, progress, ChannelSftp.OVERWRITE);
		} catch (Exception e) {
			throw e;
		} finally {
			closeSftpChannel(sftpChannel);
		}
	}

	/*
	 * a generic upload call where different progress monitor implementation can be used
	 */
	public synchronized void upload(File upload, String filePath, String fileName, SftpProgressMonitor progress)
			throws Exception {

		ChannelSftp sftpChannel = openSftpChannel();

		try {
			navigateToFolder(sftpChannel, filePath);

			sftpChannel.put(new FileInputStream(upload), fileName, progress, ChannelSftp.OVERWRITE);
		} catch (Exception e) {
			throw e;
		} finally {
			closeSftpChannel(sftpChannel);
		}
	}

	public void createFolder(String filePath) throws JSchException, SftpException {

		ChannelSftp sftpChannel = openSftpChannel();

		navigateToFolder(sftpChannel, filePath + ServiceConstants.FILE_SEPARATER + "hack");
	}

	/**
	 * upload the file
	 * 
	 * @param upload
	 * @param filePath
	 * @param fileName
	 * @throws SftpException 
	 * @throws Exception
	 */
	public Boolean upload(byte[] upload, String filePath, String fileName) throws SftpException {

		ChannelSftp sftpChannel = null;
		try {
			sftpChannel = openSftpChannel();
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		try {
			navigateToFolder(sftpChannel, filePath);
			sftpChannel.put(new ByteArrayInputStream(upload), fileName);

		} catch (SftpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} finally {
			closeSftpChannel(sftpChannel);
		}
		return true;

	}

	public void upload(InputStream upload, String filePath, String fileName) throws Exception {

		ChannelSftp sftpChannel = openSftpChannel();

		try {
			navigateToFolder(sftpChannel, filePath);

			sftpChannel.put(upload, fileName);
		} catch (Exception e) {
			throw e;
		} finally {
			closeSftpChannel(sftpChannel);
		}
	}

	public boolean delete(String filePath, String fileName) throws Exception {

		ChannelSftp sftpChannel = null;
		try {
			sftpChannel = openSftpChannel();
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		try {
			navigateToFolder(sftpChannel, filePath);
			sftpChannel.rm(fileName);

		} catch (SftpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} finally {
			closeSftpChannel(sftpChannel);
		}

		return true;
	}

	// full path must be given, and the last subfolder should be the dataset name
	public boolean deleteDatasetFolder(String datasetPath, String studyDir, String datasetName) throws Exception {

		ChannelSftp sftpChannel = null;
		try {
			sftpChannel = openSftpChannel();
			sftpChannel.cd(datasetPath); // Change Directory on SFTP Server
			sftpChannel.cd(studyDir);
			sftpChannel.cd(datasetName);

		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		// List source directory structure.
		Vector<ChannelSftp.LsEntry> fileAndFolderList = sftpChannel.ls(".");

		// Iterate objects in the list to get file/folder names.
		for (ChannelSftp.LsEntry entry : fileAndFolderList) {
			if (!(entry.getFilename().equals(".") || entry.getFilename().equals(".."))) {
				if (entry.getAttrs().isDir()) // Normally should not go to this recursive call due to strict upload file
												 // structure
				{
					deleteDatasetFolder(".", ".", entry.getFilename());
				} else {
					sftpChannel.rm(entry.getFilename());
				}
			}
		}
		sftpChannel.cd("..");
		sftpChannel.rmdir(datasetName);
		return true;
	}

	public boolean copyToNewLocation(String filePath, String fileName, String destinationPath) throws Exception {

		ChannelSftp sftpChannel = null;
		try {
			sftpChannel = openSftpChannel();
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		try {
			// sftpChannel.mkdir(destinationPath);
			navigateToFolder(sftpChannel, filePath);
			InputStream in = sftpChannel.get(fileName);
			upload(in, destinationPath, fileName);

		} catch (SftpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} finally {
			closeSftpChannel(sftpChannel);
		}

		return true;
	}



	public boolean copyAsNewFileName(String filePath, String fileName, String newFileName) throws Exception {

		ChannelSftp sftpChannel = null;
		try {
			sftpChannel = openSftpChannel();
		} catch (JSchException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

		try {
			// sftpChannel.mkdir(destinationPath);
			navigateToFolder(sftpChannel, filePath);
			InputStream in = sftpChannel.get(fileName);
			upload(in, filePath, newFileName);

		} catch (SftpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} finally {
			closeSftpChannel(sftpChannel);
		}

		return true;
	}



	public OutputStream upload(String filePath, String fileName) throws Exception {

		ChannelSftp sftpChannel = openSftpChannel();
		OutputStream os;
		try {
			navigateToFolder(sftpChannel, filePath);

			os = sftpChannel.put(fileName);
		} finally {
			closeSftpChannel(sftpChannel);
		}

		return os;
	}

	/**
	 * This is used to get the input stream from the SFTP site.
	 * 
	 * @param filePath - The relative path to the file's directory.
	 * @param fileName - The name of the target file.
	 * @return An input stream to the given file on the server's file system.
	 * @throws JSchException When there is an error with establishing the SFTP connection.
	 * @throws SftpException When there is an error with creating the input stream.
	 */
	public InputStream getFileStream(String filePath, String fileName) throws JSchException, SftpException {

		ChannelSftp sftpChannel = openSftpChannel();

		InputStream is = sftpChannel.get(filePath + fileName);

		return is;
	}

	/**
	 * Retrieves the size of a file stored in the SFTP site. The size will be given in bytes.
	 * 
	 * @param filePath - The relative path to the file's directory.
	 * @param fileName - The name of the target file.
	 * @return The size of the target file in bytes.
	 * @throws JSchException When there is an error with establishing a SFTP connection to the SFTP site.
	 * @throws SftpException When there is an error with getting the file's size.
	 */
	public Long getFileSize(String filePath, String fileName) throws JSchException, SftpException {

		Long fileSize = null;
		ChannelSftp sftpChannel = openSftpChannel();

		try {
			SftpATTRS fileAttrs = sftpChannel.lstat(filePath + fileName);
			fileSize = new Long(fileAttrs.getSize());
		} finally {
			closeSftpChannel(sftpChannel);
		}

		return fileSize;
	}

	/**
	 * 
	 * This is used to detect and rename files that already exist on the server.
	 * 
	 * @param dest
	 * @param title
	 * @return
	 */
	public String dupIndexConcat(String dest, String title) {

		File dup = new File(dest + File.separator + title);
		String newTitle = "";
		int index = 1;
		if (dup.exists()) {
			while (dup.exists()) {
				newTitle = title.split("\\.")[0] + "(" + index + ")." + title.split("\\.")[1];
				dup = new File(dest + File.separator + newTitle);
				index++;
			}
			return newTitle;
		}
		return title;
	}

	/**
	 * Navigate and create path to where the file is expected to be saved.
	 * 
	 * @param sftpChannel
	 * @param filePath
	 * @throws SftpException
	 */
	private void navigateToFolder(ChannelSftp sftpChannel, String filePath) throws SftpException {

		if (filePath.length() > 0 && filePath.indexOf(ServiceConstants.FILE_SEPARATER) != -1) {
			String firstFolder = filePath.substring(0, filePath.indexOf(ServiceConstants.FILE_SEPARATER));
			if (firstFolder.isEmpty()) {
				firstFolder = "/";
			}

			for (Object obj : sftpChannel.ls(sftpChannel.pwd())) {
				LsEntry info = (LsEntry) obj;

				if (info.getFilename().equals(firstFolder)) {
					sftpChannel.cd(firstFolder);
					navigateToFolder(sftpChannel,
							filePath.substring(filePath.indexOf(ServiceConstants.FILE_SEPARATER) + 1));
					return;
				}

			}

			try {
				sftpChannel.mkdir(firstFolder);
				sftpChannel.cd(firstFolder);
			} catch (Exception e) {
				if (e.toString().substring(0, 1).equals("4")) {
					sftpChannel.cd(firstFolder);
				} else {
					e.printStackTrace();
				}
			}
			navigateToFolder(sftpChannel, filePath.substring(filePath.indexOf(ServiceConstants.FILE_SEPARATER) + 1));
		}
	}

	protected synchronized void close() {

		Iterator<ChannelSftp> channelSftpIterator = openChannels.iterator();

		while (channelSftpIterator.hasNext()) {
			ChannelSftp currentChannel = channelSftpIterator.next();
			if (currentChannel != null) {
				currentChannel.exit();
				channelSftpIterator.remove();
			}
		}

		session.disconnect();
	}

	/**
	 * Protected to ensure only the SftpClientManager can open itself
	 * 
	 * @param userName
	 * @param password
	 * @param host
	 * @param port
	 * @throws JSchException
	 * @throws InterruptedException
	 */
	protected void open() throws JSchException {

		try {
			JSch jsch = new JSch();

			session = jsch.getSession(this.userName, this.host, this.port);
			session.setConfig(ServiceConstants.STRICT_HOST_KEY_CHECK, ServiceConstants.NO);
			session.setPassword(password);
			session.connect();
		} catch (JSchException e) {
			throw e;
		}
	}

	private synchronized ChannelSftp openSftpChannel() throws JSchException, SftpException {

		Channel channel = null;

		/*
		 * The code block blow was added recently, but caused the packet corrupt error, shouldn't be used: // connect
		 * the session if it's not already connected if (!session.isConnected()) { session.connect(); }
		 */

		try {
			channel = session.openChannel(ServiceConstants.SFTP_NAME);
			// default timeout time is 0, i.e. wait for as long as needed to establish the connection
			channel.connect();
		} catch (JSchException e) {
			e.printStackTrace();
			// channel status:1_false_true_true
			System.out.println("channel status:" + channel.getExitStatus() + "_" + channel.isConnected() + "_"
					+ channel.isEOF() + "_" + channel.isClosed());

			session.disconnect();

			try {
				session = (new JSch()).getSession(userName, host, port);

				session.setConfig(ServiceConstants.STRICT_HOST_KEY_CHECK, ServiceConstants.NO);
				session.setPassword(password);
				session.connect();

			} catch (JSchException e1) {
				throw e1;
			}

			return openSftpChannel();
		}

		ChannelSftp channelSftp = (ChannelSftp) channel;
		openChannels.add(channelSftp);
		/*
		 * if(channelSftp.pwd() != null && channelSftp.pwd().equals(ROOT_DIR)) { channelSftp.cd( HOME_ROOT_DIR +
		 * userName); }
		 */
		return channelSftp;
	}

	/*
	 * Jeff Liu: use a retry method, without re-create session
	 */
	private ChannelSftp openSftpChannel(int retry) throws JSchException, InterruptedException {

		Channel channel = null;

		/*
		 * The code block blow was added recently, but caused the packet corrupt error, shouldn't be used: // connect
		 * the session if it's not already connected if (!session.isConnected()) { session.connect(); }
		 */
		while (retry > 0 && (channel == null || !channel.isConnected())) {
			retry--;
			try {
				channel = session.openChannel(ServiceConstants.SFTP_NAME);
				// default timeout time is 0, i.e. wait for as long as needed to establish the connection
				channel.connect();
			} catch (JSchException e) {
				// will update this into logger.info() later
				System.out.println("Retry mode for thread with ID=" + Thread.currentThread().getId() + ": retry="
						+ retry + "_" + channel.getExitStatus() + "_" + channel.isConnected() + "_" + channel.isEOF()
						+ "_" + channel.isClosed() + "_" + session.isConnected());
				// e.printStackTrace();
				try {
					Thread.sleep(2000);
				} catch (InterruptedException ie) {
					// ie.printStackTrace();
					Thread.currentThread().interrupt();
				}
			}
		}
		// check the final status of Channel creation
		if (channel == null || !channel.isConnected()) {
			throw new JSchException(Thread.currentThread().getId() + " Fail to establish an SFTP channel after retry "
					+ retry + ": " + channel + "_" + channel.isConnected() + "_" + session.isConnected());
		} else {
			openChannels.add((ChannelSftp) channel);
			return (ChannelSftp) channel;
		}
	}

	/**
	 * Closes the given SFTP channel, and performs other clean up operations.
	 * 
	 * @param channel - The SFTP channel to close.
	 */
	private void closeSftpChannel(ChannelSftp channel) {

		if (channel != null) {
			channel.exit();
			openChannels.remove(channel);
		}
	}

	/**
	 * Close a file channel, if it is not null.
	 * 
	 * @param channel - The channel to close.
	 * @throws IOException When there is an error with closing the channel.
	 */
	private void closeFileChannel(java.nio.channels.Channel channel) throws IOException {
		if (channel != null) {
			channel.close();
		}
	}

	public String getPassphrase() {

		return passphrase;
	}

	public void setPassphrase(String passphrase) {

		this.passphrase = passphrase;
	}

	/**
	 * An implementation of the UserInfo in order to pass in the passphrase.
	 * 
	 * @author mgree1
	 * 
	 */
	public class SFTPUserInfo implements UserInfo, UIKeyboardInteractive {

		public String getPassword() {

			return null;
		}

		public boolean promptYesNo(String str) {

			return true;
		}

		public String getPassphrase() {

			return SftpClient.this.getPassphrase();
		}

		public boolean promptPassphrase(String message) {

			return true;
		}

		public boolean promptPassword(String message) {

			return true;
		}

		public void showMessage(String message) {

		}

		private Container panel;

		public String[] promptKeyboardInteractive(String destination, String name, String instruction, String[] prompt,
				boolean[] echo) {

			return null; // cancel
		}

	}

	public void createSubfolders(ChannelSftp sftp, String path) throws Exception {

		String[] folders = path.split(Pattern.quote(File.separator));
		for (String folder : folders) {
			try {
				sftp.cd(folder);
			} catch (SftpException e) {
				sftp.mkdir(folder);
				sftp.cd(folder);
			}
		}
	}
}

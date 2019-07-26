package gov.nih.tbi.service.io;

import gov.nih.tbi.constants.QueryToolConstants;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

/**
 * Wrapper class for JSCH
 * 
 * @author Andrew Johnson
 * 
 */
public class SftpClient {

	String userName;
	String password;
	String host;
	String passphrase;
	Integer port;

	Session session;

	// array list of all of the open channels for this client. this is primarily so we can properly close all of the
	// channels if close is called on this client.
	private List<ChannelSftp> openChannels = new ArrayList<ChannelSftp>();

	/**
	 * Protected to ensure only the SftpClientManager can create new clients
	 * 
	 * @param userName
	 * @param password
	 * @param host
	 * @param port
	 * @throws JSchException
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
			session.setConfig(QueryToolConstants.STRICT_HOST_KEY_CHECK, QueryToolConstants.NO);
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
	 * upload the file
	 * 
	 * @param upload
	 * @param filePath
	 * @param fileName
	 */
	public void upload(File upload, String filePath, String fileName) {

		ChannelSftp sftpChannel = null;

		try {
			sftpChannel = openSftpChannel();
			navigateToFolder(sftpChannel, filePath);
			sftpChannel.put(new FileInputStream(upload), fileName);
		} catch (JSchException e) {
			e.printStackTrace();
		} catch (SftpException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (sftpChannel != null) {
				closeSftpChannel(sftpChannel);
			}
		}
	}

	public void createFolder(String filePath) throws JSchException, SftpException {

		ChannelSftp sftpChannel = openSftpChannel();

		navigateToFolder(sftpChannel, filePath + QueryToolConstants.FILE_SEPARATER + "hack");
	}

	/**
	 * upload the file
	 * 
	 * @param upload
	 * @param filePath
	 * @param fileName
	 * @throws Exception
	 */
	public Boolean upload(byte[] upload, String filePath, String fileName) {

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

		if (filePath.length() > 0 && filePath.indexOf(QueryToolConstants.FILE_SEPARATER) != -1) {
			String firstFolder = filePath.substring(0, filePath.indexOf(QueryToolConstants.FILE_SEPARATER));

			for (Object obj : sftpChannel.ls(sftpChannel.pwd())) {
				LsEntry info = (LsEntry) obj;

				if (info.getFilename().equals(firstFolder)) {
					sftpChannel.cd(firstFolder);
					navigateToFolder(sftpChannel,
							filePath.substring(filePath.indexOf(QueryToolConstants.FILE_SEPARATER) + 1));
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
			navigateToFolder(sftpChannel, filePath.substring(filePath.indexOf(QueryToolConstants.FILE_SEPARATER) + 1));
		}
	}

	protected void close() {
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

	private synchronized ChannelSftp openSftpChannel() throws JSchException {

		Channel channel = null;

		/*
		 * The code block blow was added recently, but caused the packet corrupt error, shouldn't be used: // connect
		 * the session if it's not already connected if (!session.isConnected()) { session.connect(); }
		 */

		try {
			channel = session.openChannel(QueryToolConstants.SFTP_NAME);
			channel.connect(30000);
		} catch (JSchException e) {
			e.printStackTrace();
			session.disconnect();

			try {
				session = (new JSch()).getSession(userName, host, port);

				session.setConfig(QueryToolConstants.STRICT_HOST_KEY_CHECK, QueryToolConstants.NO);
				session.setPassword(password);
				session.connect();

			} catch (JSchException e1) {
				throw e1;
			}

			return openSftpChannel();
		}

		ChannelSftp channelSftp = (ChannelSftp) channel;
		openChannels.add(channelSftp);

		return channelSftp;
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

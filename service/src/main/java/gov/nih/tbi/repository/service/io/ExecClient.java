package gov.nih.tbi.repository.service.io;

import java.awt.Container;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

import gov.nih.tbi.commons.service.ServiceConstants;

public class ExecClient {
	private static final Logger logger = Logger.getLogger(ExecClient.class);
	String userName;
	String password;
	String host;
	String passphrase;
	Integer port;
	
	Session session;
	
	// array list of all of the open channels for this client. this is primarily so we can properly close all of the
	// channels if close is called on this client.
	private List<ChannelExec> openChannels = new ArrayList<ChannelExec>();
		
	/**
	 * 
	 * @param userName
	 * @param password
	 * @param host
	 * @param port
	 * @throws JSchException
	 */
	public ExecClient(String userName, String password, String host, Integer port) throws JSchException {

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
	public ExecClient(String userName, String passphrase, String privateKey, String host, Integer port)
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
			UserInfo ui = new ExecUserInfo();
			session.setUserInfo(ui);
			session.connect();
		} catch (JSchException e) {
			throw e;
		}

	}
	
	public void appendToFile(String fileName, String content) throws Exception {
		execCommand("echo '" + content + "' >> " + fileName);
	}
	
	public void copyOnRemote(String sourcePath, String destinationPath) throws Exception {
		execCommand("cp -f " + sourcePath + " " + destinationPath);
	}
	
	public void runNodeCommand(String dir, String command) throws Exception {
		execCommand("cd " + dir + "; " + "node " + command);
	}
	
	public boolean doesFileExist(String fileName) throws Exception {
		String output = execCommand("[ ! -e " + fileName + " ]; echo $?");
		logger.info("Output for file exist: " + output + " ");
		return output.trim().replace("\n", "").replace("\r", "").equals("1");
	}
	
	public String execCommand(String command) throws Exception {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ChannelExec channel = openExecChannel();
		channel.setOutputStream(baos);
		String output = "";
		try {
			channel.setCommand(command);
			channel.connect();
			output = new String(baos.toByteArray());
		}
		catch (Exception e) {
			
		}
		finally {
			closeExecChannel(channel);
		}
		return output;
	}
	
	/**
	 * Navigate and create path to where the file is expected to be saved.
	 * 
	 * @param sftpChannel
	 * @param filePath
	 * @throws SftpException
	 */
	private void navigateToFolder(ChannelExec channel, String filePath) {

		channel.setCommand("cd " + filePath);
	}
	
	public void navigateToFolder(String filePath) throws Exception {
		execCommand("cd " + filePath);
	}
	
	private ChannelExec openExecChannel() throws JSchException {
		return (ChannelExec) session.openChannel("exec");
	}
	
	private void closeExecChannel(ChannelExec channel) {
		if (channel != null) {
			channel.disconnect();
			openChannels.remove(channel);
		}
	}
	
	public void disconnectSession(){
		 session.disconnect();
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPassphrase() {
		return passphrase;
	}

	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}

	public Integer getPort() {
		return port;
	}

	public void setPort(Integer port) {
		this.port = port;
	}

	public Session getSession() {
		return session;
	}

	public void setSession(Session session) {
		this.session = session;
	}
	
	/**
	 * An implementation of the UserInfo in order to pass in the passphrase.
	 * 
	 * @author mgree1
	 * 
	 */
	public class ExecUserInfo implements UserInfo, UIKeyboardInteractive {

		public String getPassword() {

			return null;
		}

		public boolean promptYesNo(String str) {

			return true;
		}

		public String getPassphrase() {

			return ExecClient.this.getPassphrase();
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
}
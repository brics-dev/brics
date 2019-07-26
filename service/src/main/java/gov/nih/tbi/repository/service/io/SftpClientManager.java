package gov.nih.tbi.repository.service.io;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import com.jcraft.jsch.JSchException;

import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.repository.model.hibernate.DatafileEndpointInfo;

/**
 * This class manages the SFTP Connections. It is a singleton so connections are only opened once.
 * 
 * @author Andrew Johnson
 * 
 */
public class SftpClientManager {

	private static Logger logger = Logger.getLogger(SftpClientManager.class);
	
	private static SftpClientManager _singleton;

	private ConcurrentMap <SFTPConnectionClient, SftpClient> clientMap;

	private SftpClientManager() {

		clientMap = new ConcurrentHashMap<SFTPConnectionClient, SftpClient>();
	}

	/**
	 * [STATIC] get instance. This helps protect the singleton.
	 * 
	 * @return
	 */
	public static SftpClientManager getInstance() {

		if (_singleton == null) {
			_singleton = new SftpClientManager();
		}

		return _singleton;
	}

	/**
	 * [STATIC] get client by DatafileEndpointInfo
	 * 
	 * @param info
	 * @return
	 * @throws JSchException
	 */
	public static SftpClient getClient(DatafileEndpointInfo info) throws JSchException {
		SFTPConnectionClient clientKey = new SFTPConnectionClient(info.getUserName() , info.getUrl());
		return getClient(clientKey, info.getUserName(), info.getPassword(), info.getUrl(), info.getPort());
	}

	/**
	 * [STATIC] Closes the given client
	 * @param client
	 */
	public static void closeClient(SftpClient client) {
		client.close();
	}

	/**
	 * [STATIC] Get client by everything
	 * 
	 * @param userName
	 * @param password
	 * @param host
	 * @param port
	 * @return
	 * @throws JSchException
	 */
	public static SftpClient getClient(SFTPConnectionClient clientKey, String userName, String password, String host, Integer port)
			throws JSchException {
		
		SftpClient client = getInstance().getClient(clientKey);
		if (client == null) {
			getInstance().putClient(userName, password, host, port);
		} else {
			if(!client.session.isConnected()) {
				client.open();
				return client;
			}
		}

		return getInstance().getClient(clientKey);
	}

	public static SftpClient getClient(SFTPConnectionClient clientKey ,String userName, String passphrase, String privateKey, String host, Integer port)
			throws JSchException {

		if (getInstance().getClient(clientKey) == null) {
			getInstance().putClient(userName, passphrase, privateKey, host, port);
		}
		return getInstance().getClient(clientKey);
	}

	/**
	 * Get client by host name
	 * 
	 * @param host
	 * @return
	 */
	public SftpClient getClient(SFTPConnectionClient clientKey) {

		return clientMap.get(clientKey);
	}

	/**
	 * Creates a new client
	 * 
	 * @param userName
	 * @param password
	 * @param host
	 * @param port
	 * @throws JSchException
	 */
	public void putClient(String userName, String password, String host, Integer port) throws JSchException{
		int retry = ServiceConstants.RETRY_SFTP_CLIENT_CONNECTION;
		SftpClient client = null;
		try {
			client = new SftpClient(userName, password, host, port);
		}
		catch(JSchException e){
			try{ 
				while (retry > 0 &&  (client== null  || client.session == null || !client.session.isConnected() )){
					retry--;
					Thread.sleep(ServiceConstants.THREAD_SLEEP_TIME_SFTP);
					client = new SftpClient(userName, password, host, port);
					logger.info("Retry the socket connection: " + retry);
				}
			} catch(InterruptedException ie) {
				ie.printStackTrace();
				Thread.currentThread().interrupt();
			}
		}		
		
		if(client== null  || client.session == null || !client.session.isConnected() ){
			throw new JSchException("Session can't be established after 10 retries");
		}
		else{
			SFTPConnectionClient clientKey = new SFTPConnectionClient(userName, host);
			clientMap.put(clientKey, client);
		}
	}


	public void putClient(String userName, String passphrase, String privateKey, String host, Integer port)
			throws JSchException {

		SftpClient client = new SftpClient(userName, passphrase, privateKey, host, port);
		SFTPConnectionClient clientKey = new SFTPConnectionClient(userName , host);
		clientMap.put(clientKey, client);
	}

	/**
	 * Closes all connections in the map
	 */
	public static void closeAll() {

		for (SFTPConnectionClient key : getInstance().clientMap.keySet()) {
			getInstance().clientMap.get(key).close();
		}

		getInstance().clientMap.clear();
	}
	
	public static void showAllClients() {
		Map<SFTPConnectionClient, SftpClient> clients = getInstance().clientMap;
		Iterator<SFTPConnectionClient> keys = clients.keySet().iterator();
		logger.info("sftp clients: ");
		while (keys.hasNext()) {
			SftpClient client = clients.get(keys.next());
			logger.info(client.host + ", " + client.userName);
		}
	}
}

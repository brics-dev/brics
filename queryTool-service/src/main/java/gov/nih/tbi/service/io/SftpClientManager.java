package gov.nih.tbi.service.io;

import gov.nih.tbi.repository.model.hibernate.DatafileEndpointInfo;

import java.util.HashMap;
import java.util.Map;

import com.jcraft.jsch.JSchException;

/**
 * This class manages the SFTP Connections. It is a singleton so connections are only opened once.
 * 
 * @author Andrew Johnson
 * 
 */
public class SftpClientManager {

	private static SftpClientManager _singleton;

	private Map<String, SftpClient> clientMap;

	private SftpClientManager() {

		clientMap = new HashMap<String, SftpClient>();
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

		return getClient(info.getUserName(), info.getPassword(), info.getUrl(), info.getPort());
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
	public static SftpClient getClient(String userName, String password, String host, Integer port)
			throws JSchException {

		if (getInstance().getClient(host) == null) {
			getInstance().putClient(userName, password, host, port);
		}

		return getInstance().getClient(host);
	}

	public static SftpClient getClient(String userName, String passphrase, String privateKey, String host, Integer port)
			throws JSchException {

		if (getInstance().getClient(host) == null) {
			getInstance().putClient(userName, passphrase, privateKey, host, port);
		}
		return getInstance().getClient(host);
	}

	/**
	 * Get client by host name
	 * 
	 * @param host
	 * @return
	 */
	public SftpClient getClient(String host) {

		return clientMap.get(host);
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
	public void putClient(String userName, String password, String host, Integer port) throws JSchException {

		SftpClient client = new SftpClient(userName, password, host, port);

		clientMap.put(host, client);
	}


	public void putClient(String userName, String passphrase, String privateKey, String host, Integer port)
			throws JSchException {

		SftpClient client = new SftpClient(userName, passphrase, privateKey, host, port);

		clientMap.put(host, client);
	}

	/**
	 * Closes all connections in the map
	 */
	public static void closeAll() {

		for (String key : getInstance().clientMap.keySet()) {
			getInstance().clientMap.get(key).close();
		}

		getInstance().clientMap.clear();
	}
}

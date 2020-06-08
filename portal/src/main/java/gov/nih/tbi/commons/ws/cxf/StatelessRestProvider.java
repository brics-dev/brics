package gov.nih.tbi.commons.ws.cxf;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.cxf.jaxrs.client.WebClient;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;

import gov.nih.tbi.commons.service.ServiceConstants;

public abstract class StatelessRestProvider {
	private String serverUrl;
	private String userName;
	private String password;

	protected final int RECEIVE_TIMEOUT_MS = 0;
	protected final int CONNECTION_TIMEOUT_MS = 0;
	protected final String USER_NAME = "userName";
	protected final String PASS = "pass";
	
	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
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
	
	public StatelessRestProvider(String serverUrl, String userName, String password) {
		this.serverUrl = serverUrl;
		this.userName = userName;
		this.password = password;
	}
	
	protected WebClient buildWebClient(String url) {
		WebClient client = WebClient.create(url);
		HTTPConduit conduit = WebClient.getConfig(client).getHttpConduit();
		HTTPClientPolicy policy = new HTTPClientPolicy();
		policy.setReceiveTimeout(RECEIVE_TIMEOUT_MS);
		policy.setConnectionTimeout(CONNECTION_TIMEOUT_MS);
		conduit.setClient(policy);

		return client;
	}

	protected static String encodeUrlParam(String param) throws UnsupportedEncodingException {

		if (param == null || ServiceConstants.EMPTY_STRING.equals(param)) {
			return param;
		}

		String encoded = URLEncoder.encode(param, "UTF-8");
		return encoded;
	}
}

package gov.nih.tbi.recaptcha.service;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

import javax.net.ssl.HttpsURLConnection;

import org.apache.commons.httpclient.HttpException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import gov.nih.tbi.recaptcha.model.ReCaptchaResponse;

@Service
@Scope("singleton")
public class RecaptchaImpl implements ReCaptcha {
	
	public static final String PROPERTY_THEME = "theme";
	public static final String PROPERTY_TABINDEX = "tabindex";
	
	public static final String HTTP_SERVER = "http://api.recaptcha.net";
	public static final String HTTPS_SERVER = "https://api-secure.recaptcha.net";
	public static final String VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
	
	private String secret;
	private String publicKey;
	private String recaptchaServer = HTTP_SERVER;
	private boolean includeNoscript = false;
	
	public void setSecret(String secret) {
		this.secret = secret;
	}
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}
	public void setRecaptchaServer(String recaptchaServer) {
		this.recaptchaServer = recaptchaServer;
	}
	public void setIncludeNoscript(boolean includeNoscript) {
		this.includeNoscript = includeNoscript;
	}
	

	public ReCaptchaResponse checkAnswer(String response, String remoteip) throws IOException {

		String postParameters = "secret=" + URLEncoder.encode(secret) + "&remoteip=" + URLEncoder.encode(remoteip) +
			"&response=" + URLEncoder.encode(response == null ? "" : response);

		
		
		//WebClient client = WebClient.create(requestUrl);
		//WebClient.getConfig(client).getHttpConduit().getClient().setReceiveTimeout(6000000); 
		//String response = "[]";
		
		DataOutputStream writer = null;
		HttpURLConnection connection = null;
		InputStream input = null;
		OutputStream out = null;
		
		byte[] postData = postParameters.getBytes(Charset.forName("UTF-8"));
		int postDataLength = postData.length;
		
		try {

			URL url = new URL(VERIFY_URL);

			if (url.getProtocol().equals("https") == true) {
				connection = (HttpsURLConnection) url.openConnection();
			} else {
				connection = (HttpURLConnection) url.openConnection();
			}


			
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			//connection.addRequestProperty("Content-Type", MediaType.APPLICATION_FORM_URLENCODED);
			//connection.addRequestProperty("charset", "utf-8");
			//connection.addRequestProperty("Content-Length", Integer.toString(postDataLength));
			connection.connect();
			
			writer = new DataOutputStream(connection.getOutputStream());
			writer.write(postData);
			writer.flush();
			System.out.println("Response Code: " + connection.getResponseCode());
			if (connection.getResponseCode() != 200) {
				System.out.println("Content: " + connection.getContent());
				System.out.println("HTTP toString: " + connection.toString());
				System.out.println("Error Stream: " + connection.getErrorStream() != null ? connection
						.getErrorStream().toString() : "Nothing");
				throw new HttpException(
						"Response code: " + connection.getResponseCode() + "\nContent: " + connection.getContent());
			} else {
				
				input = connection.getInputStream();
				
				ByteArrayOutputStream bout = new ByteArrayOutputStream();
				byte[] buf = new byte[1024];
				while (true) {
					int rc = input.read(buf);
					if (rc <= 0)
						break;
					else
						bout.write(buf, 0, rc);
				}

				writer.close();
				input.close();
				System.out.println("HTTP toString: " + connection.toString());
				System.out.println("Content: " + connection.getContent());
				System.out.println("Message: " + bout.toString());
				if (bout.toString() == null) {
					return new ReCaptchaResponse(false, "Null read from server.");
				}
				
				String[] a = bout.toString().split("\r?\n");
				if (a.length < 1) {
					return new ReCaptchaResponse(false, "No answer returned from recaptcha: " + bout.toString());
				}
				
				boolean valid = true;
				String errorMessage = bout.toString();
				
				return new ReCaptchaResponse(valid, errorMessage);
				
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			connection.disconnect();
		}

		/*try {
			logger.debug("Request URL: " + requestUrl);
			response = client.accept("application/json").get(String.class);
		}
		catch(Exception e) {
			e.printStackTrace();
			logger.error("error retrieving summary data for study " + studyName);
		}*/

	}

}
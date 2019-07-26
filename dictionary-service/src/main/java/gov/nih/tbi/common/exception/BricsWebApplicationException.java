package gov.nih.tbi.common.exception;

import java.io.StringWriter;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The exception should be thrown for any kind of error in the RESTFUL API. Currently only allows
 * the reader to specify a string to be included in the repsonse's XML body. Retains the
 * functionality of WebApplicationException (with a more limited set of constructors), but allows
 * for a body.
 * 
 * A unique key is generated for each exception thrown. That key is included in the response body
 * and is outputted in the logs (along with the parent stack trace if there is one).
 * 
 * This exception should support a more flexible response body. Solutions subclasses, for specific
 * exceptions types, or allowing custom body's in the form of a string writer.
 * 
 * If an exception occurs generating the XML response (this should never happen in normal
 * operation), then the behavior reverts to the behavior of WebApplicationException, where the
 * response body will be empty.
 * 
 * @author mvalei
 */
public class BricsWebApplicationException extends WebApplicationException {

	private static final long serialVersionUID = 652894116044812965L;
	static Logger logger = Logger.getLogger(BricsWebApplicationException.class);


	public BricsWebApplicationException(final Response.Status status) {
		this((Throwable) null, status);
	}

	public BricsWebApplicationException(final Response.Status status, String errorMessage) {
		this((Throwable) null, status, errorMessage);
	}

	public BricsWebApplicationException(final Throwable cause, final Response.Status status) {
		this(cause, status, null);
	}

	public BricsWebApplicationException(final Throwable cause, final Response.Status status, String errorMessage) {
		super(cause, buildResponse(cause, status, errorMessage));
	}

	public BricsWebApplicationException(final String message, final Response.Status status) {
		this(message, null, status);
	}

	public BricsWebApplicationException(final String message, final Response.Status status, String errorMessage) {
		this(message, null, status, errorMessage);
	}

	public BricsWebApplicationException(final String message, final Throwable cause, final Response.Status status) {
		this(message, cause, status, null);
	}

	public BricsWebApplicationException(final String message, final Throwable cause, final Response.Status status,
			String errorMessage) {
		super(cause, buildResponse(cause, status, errorMessage));
	}

	private static Response buildResponse(final Throwable cause, final Response.Status status,
			final String errorMessage) {

		Response.Status s;
		if (status == null) {
			s = Response.Status.INTERNAL_SERVER_ERROR;
		}
		else {
			s = status;
		}
		
		// Logging
		String key = RandomStringUtils.random(16);
		logger.error("BricsWebApplicationException thrown. Status code: " + s + ". Key: " + key);
		if (cause != null) {
			cause.printStackTrace();
		}

		// Document building
		Document doc;
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			doc = docBuilder.newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return generic500Error();
		}
		
		Element root = doc.createElement("error");
		doc.appendChild(root);
		
		Element errorTag = doc.createElement("status");
		errorTag.setTextContent("HTTP" + s.getStatusCode() + ' ' + s.getReasonPhrase());
		root.appendChild(errorTag);

		if (errorMessage != null) {
			Element messageTag = doc.createElement("reason");
			messageTag.setTextContent(errorMessage);
			root.appendChild(messageTag);
		}

		Element keyTag = doc.createElement("key");
		keyTag.setTextContent(key);
		root.appendChild(keyTag);

		try {
			return Response.status(status).entity(transformDocument(doc).toString())
					.type(MediaType.APPLICATION_XML_TYPE).build();
		} catch (TransformerException e) {
			e.printStackTrace();
			return generic500Error();
		}
	}

	private static StringWriter transformDocument(Document doc) throws TransformerException {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer t = tf.newTransformer();
		StringWriter sw = new StringWriter();
		t.transform(new DOMSource(doc), new StreamResult(sw));
		return sw;
	}

	private static Response generic500Error() {
		return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
	}



}

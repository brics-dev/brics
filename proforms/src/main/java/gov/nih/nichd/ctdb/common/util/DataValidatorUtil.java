package gov.nih.nichd.ctdb.common.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

public class DataValidatorUtil extends DefaultHandler{

	private List<String> quietErrors;
	private List<String> errors;
	private Hashtable<String, String> elementTable;
	private StringBuffer guidBuffer;
	
	// Properties of the XMLReader that need to get set in order to validate documents with the W3C
	// schema definition
	static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
	
	// Host strings for service calls
	static final String DICTIONARY_HOST = "http://pdbp-dd-local.cit.nih.gov:8081";
	static final String PORTAL_HOST = "http://pdbp-portal-local.cit.nih.gov:8080";

	/**
	 * @param args
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException 
	 */
	public static void validate(String FormStructureName, String XmlDoc) throws ParserConfigurationException, SAXException, IOException {


		// Get the Form Structure XSD
		String fsName = FormStructureName;
		InputStream fsStream = null;
		try {
			URI uri =
					new URI(DICTIONARY_HOST + "/portal/ws/ddt/dictionary/Schema/FormStructure/"
							+ fsName);
			URL service = new URL(uri.toASCIIString());
			fsStream = service.openStream();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		if (fsStream == null) {
			System.out.println("Failed to open inputStream for form structure.");
			return;
		}

		// XML Validation
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		spf.setValidating(true);
		// spf.setSchema(schema);
		SAXParser saxParser = spf.newSAXParser();

		saxParser.setProperty(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA); // Tell the validator to use
																		// W3C parser.
		saxParser.setProperty(JAXP_SCHEMA_SOURCE, fsStream);

		XMLReader xmlReader = saxParser.getXMLReader();
		DefaultHandler handler = new DataValidatorUtil();
		xmlReader.setContentHandler(handler);
		xmlReader.setErrorHandler(handler);
		xmlReader.setEntityResolver(handler);
		xmlReader.parse(new InputSource(new StringReader(XmlDoc)));
		

	}

	/*
	public static void save(String studyInput, String dsInput, String XmlDoc) throws NumberFormatException, UnsupportedEncodingException{
		// Submission
		int statusCode = 0;
		String postMethodResult = null;
		String endpoint = PORTAL_HOST + "/portal/ws/repository/repository/xml/submit";
		PostMethod post = new PostMethod(endpoint);
		StringRequestEntity entity = new StringRequestEntity(XmlDoc, "text/xml","UTF-8");
		post.setRequestEntity(entity);

		try {
			HttpClient httpClient = new HttpClient();
			httpClient.executeMethod(post);
			postMethodResult = post.getResponseBodyAsString();
		} catch (IOException e) {
			e.printStackTrace();
		}

		statusCode = post.getStatusCode();
		post.releaseConnection();

		// Dataset creation
		String getMethodResult = null;
		String datasetEndpoint = PORTAL_HOST + 
				"/portal/ws/repository/repository/xml/dataset?fileId="
						+ Long.parseLong(postMethodResult) + "&studyName=" + URLEncoder.encode(studyInput, "UTF-8")
						+ "&dsName=" + URLEncoder.encode(dsInput, "UTF-8");
		GetMethod get = new GetMethod(datasetEndpoint);
		try {
			HttpClient httpClient = new HttpClient();
			httpClient.executeMethod(get);
			getMethodResult = get.getResponseBodyAsString();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(getMethodResult);

	}	
	*/

	@Override
	public void startDocument() throws SAXException {
		errors = new ArrayList<String>();
		quietErrors = new ArrayList<String>();
		elementTable = new Hashtable<String, String>();

	}

	@Override
	public void endDocument() throws SAXException {
		System.out.println("");
		String quietCount = "(" + quietErrors.size() + " errors hidden)";
		if (errors.isEmpty()) {
			System.out.println("No errors. " + quietCount);
		} else {
			System.out.println("Errors " + quietCount + ":");
			for (String err : errors) {
				System.out.println(err);
			}
		}
	}

	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		if (elementTable.get(localName) != null) {
			System.out.println("Found " + elementTable.get(localName) + " type data element during XML validation.");
		}
		if (localName.equals("GUID")) {
			guidBuffer = new StringBuffer();
		}

	}

	@Override
	public void characters(char[] ch, int start, int length) {
		if (guidBuffer != null) {
			guidBuffer.append(ch, start, length);
		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		if (guidBuffer != null) {
			System.out.println("GUID Identified: " + guidBuffer.toString());
			guidBuffer = null;
		}
	}

	// Throwing an exception (including the one passed as the argument) here will terminate
	// validation. Otherwise it will continue.
	@Override
	public void warning(SAXParseException spe) throws SAXException {
		errors.add("Warning: " + getParseExceptionInfo(spe));
	}

	@Override
	// Occurs when schema validation fails.
	public void error(SAXParseException spe) throws SAXException {
		String message = "Error: " + getParseExceptionInfo(spe);
		if (message.contains("Attribute 'deType' cannot")) {
			quietErrors.add(message);
		} else {
		errors.add(message);
		}
		// throw new SAXException(message);
	}

	@Override
	// Occurs when XML is not well formed.
	public void fatalError(SAXParseException spe) throws SAXException {
		String message = "Fatal Error: " + getParseExceptionInfo(spe);
		errors.add(message);
		throw new SAXException(message);
	}

	private String getParseExceptionInfo(SAXParseException spe) {
		String systemId = spe.getSystemId();

		if (systemId == null) {
			systemId = "null";
		}

		String info = "URI=" + systemId + " Line=" + spe.getLineNumber() + ": " + spe.getMessage();

		return info;
	}

	@Override
	public InputSource resolveEntity(String publicId, String systemId) {
		byte[] bytes = null;
		String deName = systemIdToDeName(systemId);
		System.out.print("Validating " + deName + ".xsd... ");
		try {
			URI uri =
					new URI(DICTIONARY_HOST + "/portal/ws/ddt/dictionary/Schema/DataElement/"
							+ deName);
			URL service = new URL(uri.toASCIIString());
			bytes = IOUtils.toByteArray(service.openStream());
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		System.out.println("Done!");
		return new InputSource(new ByteArrayInputStream(bytes));
	}

	private String systemIdToDeName(String systemId) {
		String[] cutSystemId = systemId.split("/");
		return cutSystemId[cutSystemId.length - 1].split("\\.")[0];
	}
	
}


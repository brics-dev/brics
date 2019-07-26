package gov.nih.tbi.repository.xml.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.log4j.Logger;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import gov.nih.tbi.repository.xml.exception.SAXMalformedXMLException;
import gov.nih.tbi.repository.xml.exception.XmlValidationException;
import gov.nih.tbi.repository.xml.model.XmlValidationResponse;

public class DataParser extends DefaultHandler {

	private static Logger logger = Logger.getLogger(DataParser.class);

	static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";

	private byte[] dataBytes;
	private byte[] schemaBytes;
	private List<String> errors;

	// Variables that are tracked for error reporting (tag names of different levels)
	Map<String, Integer> formInst;
	Map<String, Integer> groupInst;
	String form;
	String group;
	String element;
	int level;

	public DataParser(byte[] dataBytes, byte[] schemaBytes) {
		this.dataBytes = dataBytes;
		this.schemaBytes = schemaBytes;
	}

	public List<String> getErrors() {
		// TODO: MV- throw exception if null.
		return errors;
	}

	public XmlValidationResponse validate() throws XmlValidationException {
		errors = new ArrayList<String>();

		if (dataBytes == null) {
			// TODO: MV-Throw exception.
		}
		if (schemaBytes == null) {
			// TODO: MV-Throw exception.
		}



		// XML Validation
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);
		spf.setValidating(false); // Should only be set true for DTD validation (aka never).
		spf.setSchema(getSchema(schemaBytes));

		try {
			SAXParser saxParser = spf.newSAXParser();

			XMLReader xmlReader = saxParser.getXMLReader();
			// xmlReader.setContentHandler(this); // Only needed for extra java validation
			xmlReader.setErrorHandler(this);
			xmlReader.setContentHandler(this);
			xmlReader.parse(getInput(dataBytes));
		} catch (SAXMalformedXMLException e) {
			// Data file is malformed so parsing was terminated. Nothing to be done.
		} catch (SAXException | ParserConfigurationException | IOException e) {
			logger.error("There was a system error parsing this file. This error should not occur.", e);
			throw new XmlValidationException("There was an internal error. Please contanct the help desk.");
		}


		// Create repsonse
		XmlValidationResponse returnObject = new XmlValidationResponse();
		returnObject.setErrors(errors);
		boolean valid = false;
		if (errors.size() == 0) {
			valid = true;
		} else {
			returnObject.setReason("Errors were found during data validation.");
		}
		returnObject.setValid(valid);
		return returnObject;
	}

	@Override
	public void startDocument() throws SAXException {
		formInst = new HashMap<String, Integer>();
		groupInst = new HashMap<String, Integer>();
		level = -1;
	};

	@Override
	public void startElement(String uri, String localName, String qName, org.xml.sax.Attributes atts)
			throws SAXException {
		level++;
		
		// currently level == 0 means we just started the document (root node)
		if (level == 0) {
			return;
		}
		
		if (form == null) {
			form = localName;
			
			// formInst.put(form, (formInst.get(form) == null) ? 0 : formInst.get(form) + 1); // to
			// hard to read
			if (formInst.get(form) == null)
			{
				formInst.put(form, 0);
			}
			else {
				formInst.put(form, formInst.get(form) + 1);
			}
		}
		else if (group == null) {
			group = localName;

			if (groupInst.get(group) == null) {
				groupInst.put(group, 0);
			} else {
				groupInst.put(group, groupInst.get(group) + 1);
			}
		}
		else if (element == null) {
			element = localName;
		}
	};

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		level--;

		if (element == localName) {
			element = null;
		} else if (group == localName) {
			group = null;
		} else if (form == localName) {
			form = null;
			groupInst = new HashMap<String, Integer>();
		}
	};

	/**
	 * What kind of error in parsing throws a warning? Throw an exception in this function to halt
	 * parsing immediately.
	 */
	@Override
	public void warning(SAXParseException spe) throws SAXException {
		logger.debug("Warning: " + getParseExceptionInfo(spe));
		errors.add(parseErrorMessage(spe.getLineNumber(), spe.getMessage()));
	}

	/**
	 * Schema validation errors trigger this function. Throw an exception in this function to halt
	 * parsing immediately.
	 */
	@Override
	public void error(SAXParseException spe) throws SAXException {
		logger.debug("Error: " + getParseExceptionInfo(spe));
		errors.add(parseErrorMessage(spe.getLineNumber(), spe.getMessage()));
	}

	/**
	 * This function is triggered when XML is not well formed. An exception is thrown to halt
	 * parsing immediately.
	 */
	@Override
	public void fatalError(SAXParseException spe) throws SAXException {
		String message = "Fatal Error: " + getParseExceptionInfo(spe);
		errors.add(message);
		throw new SAXMalformedXMLException(message);
	}

	private Schema getSchema(byte[] bytes) {
		Schema schema = null;
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			schema = sf.newSchema(new StreamSource(new ByteArrayInputStream(bytes)));
		} catch (SAXException saxe) {
			throw new Error("Error while getting schema", saxe);
		}
		return schema;
	}

	private InputSource getInput(byte[] bytes) {
		InputSource source = null;
		source = new InputSource(new ByteArrayInputStream(bytes));
		return source;
	}

	private String getParseExceptionInfo(SAXParseException spe) {
		String systemId = spe.getSystemId();

		if (systemId == null) {
			systemId = "null";
		}

		String info = "URI=" + systemId + " Line=" + spe.getLineNumber() + ": " + spe.getMessage();

		return info;
	}

	/**
	 * Create an error string (currently formatted in xml). This function assumes that parser is
	 * currently on the same line the error was thrown (uses
	 * 
	 * @param lineNumber : should come from a SAXParseException
	 * @param message : : should come from a SAXParseException
	 * @return
	 */
	private String parseErrorMessage(int lineNumber, String message) {
		String error = "<line>" + lineNumber + "</line>\n";

		if (form != null) {
			error = error.concat("<formStructure>\n<name>" + form + "</name>\n");
			error = error.concat("<instance>" + formInst.get(form) + "</instance>\n</formStructure>\n");
		}
		if (group != null) {
			error = error.concat("<group>\n<name>" + group + "</name>\n");
			error = error.concat("<instance>" + groupInst.get(group) + "</instance>\n</group>\n");
		}
		if (element != null) {
			error = error.concat("<element>" + element + "</element>\n");
		}

		error = error.concat("<message>" + message + "</message>");
		return error;
	}
}

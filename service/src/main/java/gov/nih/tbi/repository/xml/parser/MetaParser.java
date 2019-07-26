package gov.nih.tbi.repository.xml.parser;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import gov.nih.tbi.repository.xml.exception.SAXMalformedXMLException;
import gov.nih.tbi.repository.xml.exception.SAXParsingCompleteException;
import gov.nih.tbi.repository.xml.exception.XmlValidationException;
import gov.nih.tbi.repository.xml.model.XmlInstanceMetaData;

public class MetaParser extends DefaultHandler {

	private static Logger logger = Logger.getLogger(MetaParser.class);

	private byte[] dataBytes;
	private String formStructureName;
	private int depth = -1;

	public MetaParser(byte[] dataBytes) {
		this.dataBytes = dataBytes;
	}

	public XmlInstanceMetaData parse() throws XmlValidationException {

		if (dataBytes == null) {
			// TODO: MV-Throw exception.
		}

		// XML Validation
		SAXParserFactory spf = SAXParserFactory.newInstance();
		spf.setNamespaceAware(true);

		try {
			SAXParser saxParser = spf.newSAXParser();

			XMLReader xmlReader = saxParser.getXMLReader();
			xmlReader.setContentHandler(this);
			xmlReader.parse(getInput(dataBytes));
		} catch (SAXMalformedXMLException e) {
			throw new XmlValidationException(e.getMessage());

		} catch (SAXParsingCompleteException e) {
			// Parsing was terminated after all meta data was found. Nothing to do.

		} catch (ParserConfigurationException | IOException | SAXException e) {
			logger.error("There was a system error parsing this file. This error should not occur.", e);
			throw new XmlValidationException("There was an internal error. Please contanct the help desk.");
		}

		// Create response
		XmlInstanceMetaData returnObject = new XmlInstanceMetaData();
		returnObject.setFsName(formStructureName);
		return returnObject;


	}

	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		depth++;
		if (depth == 1) {
			formStructureName = localName;
			throw new SAXParsingCompleteException("Parsing terminated after all meta-data found.");
		}

	}

	@Override
	public void endElement(String uri, String localName, String qName) {
		depth--;

	}

	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		String message = "An XML Syntax error was found on line " + e.getLineNumber() + ": \"" + e.getMessage() + "\"";
		throw new SAXMalformedXMLException(message);
	}

	private InputSource getInput(byte[] bytes) {
		InputSource source = null;
		source = new InputSource(new ByteArrayInputStream(bytes));
		return source;
	}

}

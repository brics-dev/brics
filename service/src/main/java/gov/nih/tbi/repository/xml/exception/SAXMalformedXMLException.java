package gov.nih.tbi.repository.xml.exception;

import org.xml.sax.SAXException;

/**
 * An exception that is thrown when SAX parsing is ready to be terminated regularly.
 * 
 * @author mvalei
 *
 */
public class SAXMalformedXMLException extends SAXException {

	private static final long serialVersionUID = 7145647591006470827L;

	public SAXMalformedXMLException(String string) {
		super(string);
	}

}
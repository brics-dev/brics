package gov.nih.tbi.repository.xml.exception;

import org.xml.sax.SAXException;

/**
 * An exception that is thrown when SAX parsing is ready to be terminated regularly.
 * @author mvalei
 *
 */
public class SAXParsingCompleteException extends SAXException {

	private static final long serialVersionUID = -312647986846467933L;

	public SAXParsingCompleteException(String string) {
		super(string);
	}

}

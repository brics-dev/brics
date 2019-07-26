package gov.nih.tbi.commons.service.util;

import java.io.OutputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.apache.log4j.Logger;

import com.sun.xml.txw2.output.IndentingXMLStreamWriter;

/**
 * Marshaller utility that can marshal individual elements separately to avoid running out of heap space.  
 * This is a slightly modified code that I found on stackoverflow ;)
 * @author fchen
 *
 * @param <T>
 */
public class MarshalStreamer<T> {
	private static Logger log = Logger.getLogger(MarshalStreamer.class);

	private Class<T> type;
	private Marshaller marshaller;
	private XMLStreamWriter xmlOut;
	
	/**
	 * Give the constructor an outputstream, class object of T, and the name of the root element
	 * @param outputStream
	 * @param type
	 * @param rootElement
	 */
	public MarshalStreamer(OutputStream outputStream, Class<T> type, String rootElement) {
		this.type = type;
		
		try {
			JAXBContext context = JAXBContext.newInstance(type);
			marshaller = context.createMarshaller();
			marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE); //need this to fragment our marshals
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		
		
		//need to use indentingXMLStreamWriter because Jaxb pretty print does not work wheny ou have fragmenting property turned on
		try {
			XMLOutputFactory xmlof = XMLOutputFactory.newInstance();
			xmlOut = new IndentingXMLStreamWriter(xmlof.createXMLStreamWriter(outputStream));
			xmlOut.writeStartDocument();
			xmlOut.writeStartElement(rootElement);
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Marshal the given element and write to output stream
	 * @param element
	 */
	public void writeElement(T element) {
		try {
			JAXBElement<T> jaxbElement = new JAXBElement<T>(QName.valueOf(type.getSimpleName()), type, element);
			marshaller.marshal(jaxbElement, xmlOut);
		} catch (JAXBException e) {
			log.error("Error while marshalling: " + element.toString());
			e.printStackTrace();
		}
	}
	
	/**
	 * Call this to close the root tag and the output stream.  Do this in a finally block in case something bad happens
	 */
	public void close() {
		try {
			xmlOut.writeEndDocument();
			xmlOut.close();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
	}
}

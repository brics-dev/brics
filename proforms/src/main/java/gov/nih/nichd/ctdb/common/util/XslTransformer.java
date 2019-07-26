package gov.nih.nichd.ctdb.common.util;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.StringWriter;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Document;

import gov.nih.nichd.ctdb.common.TransformationException;

public class XslTransformer implements Serializable {
	private static final long serialVersionUID = -3233793121571461584L;
	
	public static String transform(Document paramDocument, String paramString) throws TransformationException {
		try {
			StreamSource localStreamSource = new StreamSource(new File(paramString));
			DOMSource localDOMSource = new DOMSource(paramDocument);
			StringWriter localStringWriter = new StringWriter();
			StreamResult localStreamResult = new StreamResult(localStringWriter);
			TransformerFactory.newInstance().newTransformer(localStreamSource).transform(localDOMSource,
					localStreamResult);
			return localStringWriter.toString();
		} catch (Exception localException) {
			throw new TransformationException(localException.getMessage());
		}
	}

	public static String transform(Document paramDocument, InputStream paramInputStream)
			throws TransformationException {
		try {
			StreamSource localStreamSource = new StreamSource(paramInputStream);
			DOMSource localDOMSource = new DOMSource(paramDocument);
			StringWriter localStringWriter = new StringWriter();
			StreamResult localStreamResult = new StreamResult(localStringWriter);
			TransformerFactory.newInstance().newTransformer(localStreamSource).transform(localDOMSource,
					localStreamResult);
			return localStringWriter.toString();
		} catch (Exception localException) {
			throw new TransformationException(localException.getMessage());
		}
	}

	public static void transform(Document paramDocument, String paramString, OutputStream paramOutputStream)
			throws TransformationException {
		try {
			StreamSource localStreamSource = new StreamSource(new File(paramString));
			DOMSource localDOMSource = new DOMSource(paramDocument);
			TransformerFactory.newInstance().newTransformer(localStreamSource).transform(localDOMSource,
					new StreamResult(paramOutputStream));
		} catch (Exception localException) {
			throw new TransformationException(localException.getMessage());
		}
	}

	public static void transform(Document paramDocument, InputStream paramInputStream, OutputStream paramOutputStream)
			throws TransformationException {
		try {
			StreamSource localStreamSource = new StreamSource(paramInputStream);
			DOMSource localDOMSource = new DOMSource(paramDocument);
			TransformerFactory.newInstance().newTransformer(localStreamSource).transform(localDOMSource,
					new StreamResult(paramOutputStream));
		} catch (Exception localException) {
			throw new TransformationException(localException.getMessage());
		}
	}

	public static String transform(Document paramDocument, String paramString, Map<String, String> paramMap)
			throws TransformationException {
		StringWriter localStringWriter = new StringWriter();
		
		try {
			StreamSource localStreamSource = new StreamSource(new File(paramString));
			DOMSource localDOMSource = new DOMSource(paramDocument);
			StreamResult localStreamResult = new StreamResult(localStringWriter);
			Transformer localTransformer = TransformerFactory.newInstance().newTransformer(localStreamSource);
			
			for ( Entry<String, String> localEntry : paramMap.entrySet()) {
				localTransformer.setParameter(localEntry.getKey(), localEntry.getValue());
			}
			
			localTransformer.transform(localDOMSource, localStreamResult);
		} catch (Exception localException) {
			throw new TransformationException(localException.getMessage());
		}
		
		return localStringWriter.toString();
	}

	public static String transform(Document paramDocument, InputStream paramInputStream, Map<String, String> paramMap) throws TransformationException {
		StringWriter localStringWriter = new StringWriter();
		
		try {
			StreamSource localStreamSource = new StreamSource(paramInputStream);
			DOMSource localDOMSource = new DOMSource(paramDocument);
			StreamResult localStreamResult = new StreamResult(localStringWriter);
			Transformer localTransformer = TransformerFactory.newInstance().newTransformer(localStreamSource);
			
			for ( Entry<String, String> localEntry : paramMap.entrySet() ) {
				localTransformer.setParameter(localEntry.getKey(), localEntry.getValue());
			}
			
			localTransformer.transform(localDOMSource, localStreamResult);
			
		} catch (Exception localException) {
			throw new TransformationException(localException.getMessage());
		}
		
		return localStringWriter.toString();
	}

	public static void transform(Document paramDocument, String paramString, Map<String, String> paramMap,
			OutputStream paramOutputStream) throws TransformationException {
		try {
			StreamSource localStreamSource = new StreamSource(new File(paramString));
			DOMSource localDOMSource = new DOMSource(paramDocument);
			Transformer localTransformer = TransformerFactory.newInstance().newTransformer(localStreamSource);
			
			for ( Entry<String, String> localEntry : paramMap.entrySet() ) {
				localTransformer.setParameter(localEntry.getKey(), localEntry.getValue());
			}
			
			localTransformer.transform(localDOMSource, new StreamResult(paramOutputStream));
		} catch (Exception localException) {
			throw new TransformationException(localException.getMessage());
		}
	}

	public static void transform(Document paramDocument, InputStream paramInputStream, Map<String, String> paramMap,
			OutputStream paramOutputStream) throws TransformationException {
		try {
			StreamSource localStreamSource = new StreamSource(paramInputStream);
			DOMSource localDOMSource = new DOMSource(paramDocument);
			Transformer localTransformer = TransformerFactory.newInstance().newTransformer(localStreamSource);
			
			for ( Entry<String, String> localEntry : paramMap.entrySet() ) {
				localTransformer.setParameter(localEntry.getKey(), localEntry.getValue());
			}
			
			localTransformer.transform(localDOMSource, new StreamResult(paramOutputStream));
		} catch (Exception localException) {
			throw new TransformationException(localException.getMessage());
		}
	}
}

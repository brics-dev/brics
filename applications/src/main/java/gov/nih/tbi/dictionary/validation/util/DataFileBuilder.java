
package gov.nih.tbi.dictionary.validation.util;

import gov.nih.tbi.ApplicationsConstants;
import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.dictionary.model.Translations;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.dictionary.validation.model.DataStructureTable;
import gov.nih.tbi.dictionary.validation.model.DataSubmission;
import gov.nih.tbi.dictionary.validation.model.FileNode;
import gov.nih.tbi.dictionary.validation.model.FileNode.FileType;
import gov.nih.tbi.dictionary.ws.validation.Normalizer;
import gov.nih.tbi.dictionary.validation.model.RepeatableGroupTable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JTextField;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A package for constructing a data XML file and writing the file
 * 
 * @author mvalei
 * 
 */
public class DataFileBuilder {

	/**
	 * The submission contains the data that will be written to the XML data file.
	 */
	private DataSubmission submission;

	/**
	 * Any error messages that are encountered during the build or write process are
	 * recorded here.
	 */
	private String errorMessage;

	/**
	 * The path where the data file is written.
	 */
	private String path;

	/**
	 * The path of the root folder for the submission
	 */
	private String rootPath;

	private Translations translations;

	public DataFileBuilder() {

		submission = null;
		setPath(null);
		setRootPath(null);
		errorMessage = "";
	}

	public DataFileBuilder(DataSubmission submission, String rootPath) throws JAXBException, IOException {

		this.submission = submission;
		this.rootPath = rootPath;
		setPath(null);
		errorMessage = "";
		for (FileNode node : submission.getDataNodes()) {
			if (FileType.TRANSLATION_RULE.equals(node.getType()) && node.isIncluded()) {
				FileInputStream in = null;
				try {
					in = new FileInputStream(new File(node.getConicalPath()));
					ClassLoader cl = gov.nih.tbi.dictionary.model.TranslationObjectFactory.class.getClassLoader();
					JAXBContext jc = JAXBContext.newInstance(Translations.class);
					Unmarshaller um = jc.createUnmarshaller();
					this.translations = (Translations) um.unmarshal(in);
				} finally {
					in.close();
				}
			}
		}
	}

	public DataSubmission getSubmission() {

		return submission;
	}

	public void setSubmission(DataSubmission submission) {

		this.submission = submission;
	}

	public String getErrorMessage() {

		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {

		this.errorMessage = errorMessage;
	}

	public String getPath() {

		return path;
	}

	public void setPath(String path) {

		this.path = path;
	}

	public String getRootPath() {

		return rootPath;
	}

	public void setRootPath(String rootPath) {

		this.rootPath = rootPath;
	}

	/**
	 * Builds document from the datasets contained in submissionPackage On failure,
	 * the reason is written in errorMessageField
	 * 
	 * @return boolean: true on success, false on failure
	 */
	public boolean build(HashMap<FileNode, JTextField> mapFieldToNode, boolean isNonToolSubmission, String proformsDatasetName) {

		// normalizer for the translation rules
		Normalizer norm = new Normalizer(submission.getDictionary(), translations);

		for (StructuralFormStructure currentDataStructure : submission.getDictionary()) {

			String shortName = currentDataStructure.getShortName();
			String version = currentDataStructure.getVersion().toString();
			String baseName;

			Vector<DataStructureTable> referenceTables = submission.getReferencedTables(shortName);
			// This prevents the adding of data structure tags that have nothing in them.
			if (referenceTables.size() > 0) {

				for (DataStructureTable d : referenceTables) {
					FileNode n = getKey(submission.getFileData(), d);
					if(!isNonToolSubmission) {
						baseName = mapFieldToNode.get(n).getText() + "_" + shortName;
					}else if(proformsDatasetName == null){
						baseName = shortName + "_" + n.getName().substring(0,n.getName().lastIndexOf("."));
					}else {
						baseName = proformsDatasetName;
					}
					
					File file = new File(rootPath + File.separator + baseName + ".xml");
					String hash = "";
					while (file.exists()) {
						hash = "_" + baseName.concat(hash).hashCode();
						file = new File(rootPath + File.separator + baseName + hash + ".xml");
					}
					path = file.getAbsolutePath();
					Path usedPath = Paths.get(path);
					OutputStream outStream;
					XMLStreamWriter writer = null;
					try {
						outStream = Files.newOutputStream(usedPath);
						XMLOutputFactory outputFactory = XMLOutputFactory.newFactory();
						writer = outputFactory.createXMLStreamWriter(outStream);
						writer.writeStartDocument("UTF-8", "1.0");
						writer.writeStartElement(ApplicationsConstants.DATA_FILE);
						writer.writeStartElement(ApplicationsConstants.DATA_STRUCTURE);
						writer.writeAttribute(ApplicationsConstants.SHORT_NAME, shortName);
						writer.writeAttribute(ApplicationsConstants.VERSION, version);

						// i = an iteration over the rows in the data structure (a single record)
						for (int i = 0; i < d.getRowCount(); i++) {
							writer.writeStartElement(ModelConstants.RECORD_STRING);
							// x = an iteration over the repeatable groups
							for (int x = 0; x < d.getColumnCount(); x++) {
								RepeatableGroupTable rgTable = d.getRepeatableGroupTable(x);
								writer.writeStartElement(ApplicationsConstants.REPEATABLE_GROUP);
								writer.writeAttribute(ApplicationsConstants.NAME,
										rgTable.getRepeatableGroup().getName().toLowerCase());
								for (Integer rgRow : d.getAllReferences(i, x, null)) {
									writer.writeStartElement(ApplicationsConstants.GROUP);
									// y = iterating over the elements in a repeatable group (rgTable)
									for (int y = 0; y < rgTable.getSize(); y++) {
										if (rgTable.getValueAt(rgRow, y) != null) {
											writer.writeStartElement(ApplicationsConstants.DATA);
											writer.writeAttribute(ApplicationsConstants.NAME,
													rgTable.getColumnName(y).toLowerCase());
											writer.writeAttribute(ApplicationsConstants.ALIAS,
													d.getColumnName(rgTable.getDataFilePositionMapping(y) - 1)
															.split("\\.")[1]);
											writer.writeAttribute(ApplicationsConstants.VALUE,
													prepareValue(norm, currentDataStructure, rgTable, rgRow, y));
											writer.writeEndElement();
										}
									}
									writer.writeEndElement();
								}
								writer.writeEndElement();
							}
							writer.writeEndElement();
						}
						writer.writeEndElement();
						writer.writeEndElement();
						writer.writeEndDocument();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (XMLStreamException e) {
						e.printStackTrace();
					} finally {
						if (writer != null) {
							try {
								writer.close();
							} catch (XMLStreamException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
		}

		return true;

	}

	private String prepareValue(Normalizer norm, StructuralFormStructure currentDataStructure,
			RepeatableGroupTable rgTable, Integer rgRow, int y) {

		String stringData = norm.normalize(currentDataStructure,
				rgTable.getElementMapping(y).getStructuralDataElement(), (String) rgTable.getValueAt(rgRow, y));

		// Mess around with the slashes in the file path
		if ((DataType.FILE.equals(rgTable.getElementMapping(y).getStructuralDataElement().getType())
				|| DataType.THUMBNAIL.equals(rgTable.getElementMapping(y).getStructuralDataElement().getType())
				|| DataType.TRIPLANAR.equals(rgTable.getElementMapping(y).getStructuralDataElement().getType()))
				&& stringData != null && !stringData.isEmpty()) {

			// make sure to remove the absolute path
			if (stringData.toLowerCase().startsWith(rootPath.toLowerCase())) {
				stringData = stringData.substring(rootPath.length());
			}

			if (!stringData.startsWith("\\") && !stringData.startsWith("/")) {
				stringData = "/" + stringData;
			}
			stringData = stringData.replace("\\", "/");

		}
		return stringData;
	}
	
	private static FileNode getKey(ConcurrentHashMap<FileNode,DataStructureTable> map, DataStructureTable dst) {
		return map.entrySet().stream().filter(entry -> dst.equals(entry.getValue())).map(Map.Entry::getKey).findFirst()
				.get();
	}

}

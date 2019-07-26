
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
import java.util.Set;
import java.util.Vector;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
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
public class DataFileBuilder
{

    /**
     * The submission contains the data that will be written to the XML data file.
     */
    private DataSubmission submission;

    /**
     * The document file defines the structure of the xml file. This file is built in the build function and used to
     * write the XML file in the write function.
     */
    private Document document;

    /**
     * Any error messages that are encountered during the build or write process are recorded here.
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

    public DataFileBuilder()
    {

        submission = null;
        document = null;
        setPath(null);
        setRootPath(null);
        errorMessage = "";
    }

    public DataFileBuilder(DataSubmission submission, String rootPath) throws JAXBException, IOException
    {

        this.submission = submission;
        this.rootPath = rootPath;
        document = null;
        setPath(null);
        errorMessage = "";
        for (FileNode node : submission.getDataNodes())
        {
            if (FileType.TRANSLATION_RULE.equals(node.getType()) && node.isIncluded())
            {
                FileInputStream in = new FileInputStream(new File(node.getConicalPath()));
                ClassLoader cl = gov.nih.tbi.dictionary.model.TranslationObjectFactory.class.getClassLoader();
                JAXBContext jc = JAXBContext.newInstance(Translations.class);
                Unmarshaller um = jc.createUnmarshaller();
                this.translations = (Translations) um.unmarshal(in);
                in.close();
            }
        }
    }

    public DataSubmission getSubmission()
    {

        return submission;
    }

    public void setSubmission(DataSubmission submission)
    {

        this.submission = submission;
    }

    public String getErrorMessage()
    {

        return errorMessage;
    }

    public void setErrorMessage(String errorMessage)
    {

        this.errorMessage = errorMessage;
    }

    public Document getDocument()
    {

        return document;
    }

    public void setDocument(Document document)
    {

        this.document = document;
    }

    public String getPath()
    {

        return path;
    }

    public void setPath(String path)
    {

        this.path = path;
    }

    public String getRootPath()
    {

        return rootPath;
    }

    public void setRootPath(String rootPath)
    {

        this.rootPath = rootPath;
    }

    /**
     * Builds document from the datasets contained in submissionPackage On failure, the reason is written in
     * errorMessageField
     * 
     * @return boolean: true on success, false on failure
     */
    public boolean build()
    {

        // normalizer for the translation rules
        Normalizer norm = new Normalizer(submission.getDictionary(), translations);

        // Make sure that submission is not null
        if (submission == null)
        {
            errorMessage = "DataFileBuilder could not read from the submission. [submission is null]";
            return false;
        }

        try
        {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // root elements
            Document doc = docBuilder.newDocument();
            Element rootElement = doc.createElement(ApplicationsConstants.DATA_FILE);
            doc.appendChild(rootElement);

            Set<String> shortNameSet = submission.getStructureData().keySet();

            for (StructuralFormStructure currentDataStructure : submission.getDictionary())
            {
                String shortName = currentDataStructure.getShortName();
                String version = currentDataStructure.getVersion().toString();

                Vector<DataStructureTable> referenceTables = submission.getReferencedTables(shortName);

                // This prevents the adding of data structure tags that have nothing in them.
                if (referenceTables.size() > 0)
                {
                    Element dataStructure = doc.createElement(ApplicationsConstants.DATA_STRUCTURE);
                    dataStructure.setAttribute(ApplicationsConstants.SHORT_NAME, shortName);
                    dataStructure.setAttribute(ApplicationsConstants.VERSION, version);
                    rootElement.appendChild(dataStructure);

                    for (DataStructureTable d : referenceTables)
                    {
                        // i = an iteration over the rows in the data structure (a single record)
                        for (int i = 0; i < d.getRowCount(); i++)
                        {
                            Element record = doc.createElement(ModelConstants.RECORD_STRING);
                            // x = an iteration over the repeatable groups
                            for (int x = 0; x < d.getColumnCount(); x++)
                            {
                                RepeatableGroupTable rgTable = d.getRepeatableGroupTable(x);
                                Element rgElement = doc.createElement(ApplicationsConstants.REPEATABLE_GROUP);
                                rgElement.setAttribute(ApplicationsConstants.NAME, rgTable.getRepeatableGroup()
                                        .getName().toLowerCase());

                                for (Integer rgRow : d.getAllReferences(i, x, null))
                                {
                                    Element groupElement = doc.createElement(ApplicationsConstants.GROUP);
                                    // y = iterating over the elements in a repeatbale group (rgTable)
                                    for (int y = 0; y < rgTable.getSize(); y++)
                                    {
                                        if (rgTable.getValueAt(rgRow, y) != null)
                                        {
                                            Element data = doc.createElement(ApplicationsConstants.DATA);
                                            data.setAttribute(ApplicationsConstants.NAME, rgTable.getColumnName(y)
                                                    .toLowerCase());
                                            data.setAttribute(
                                                    ApplicationsConstants.ALIAS,
                                                    d.getColumnName(rgTable.getDataFilePositionMapping(y) - 1).split(
                                                            "\\.")[1]);
                                            data.setAttribute(ApplicationsConstants.VALUE,
                                                    prepareValue(norm, currentDataStructure, rgTable, rgRow, y));
                                            groupElement.appendChild(data);
                                        }
                                    }
                                    rgElement.appendChild(groupElement);

                                }
                                record.appendChild(rgElement);
                            }
                            dataStructure.appendChild(record);
                        }
                    }
                }
            }

            this.document = doc;
            return true;
        }
        catch (ParserConfigurationException e)
        {
            errorMessage = "A parserConfigurationException has occured in the build process of the document file.";
            e.printStackTrace();
            return false;
        }
    }

    private String prepareValue(Normalizer norm, StructuralFormStructure currentDataStructure,
            RepeatableGroupTable rgTable, Integer rgRow, int y)
    {

        String stringData = norm.normalize(currentDataStructure, rgTable.getElementMapping(y)
                .getStructuralDataElement(), (String) rgTable.getValueAt(rgRow, y));

        // Mess around with the slashes in the file path
        if ((DataType.FILE.equals(rgTable.getElementMapping(y).getStructuralDataElement().getType()) || DataType.THUMBNAIL
                .equals(rgTable.getElementMapping(y).getStructuralDataElement().getType()) || DataType.TRIPLANAR
                .equals(rgTable.getElementMapping(y).getStructuralDataElement().getType()))
                && stringData != null
                && !stringData.isEmpty())
        {

            // make sure to remove the absolute path
            if (stringData.toLowerCase().startsWith(rootPath.toLowerCase()))
            {
                stringData = stringData.substring(rootPath.length());
            }

            if (!stringData.startsWith("\\") && !stringData.startsWith("/"))
            {
                stringData = "/" + stringData;
            }
            stringData = stringData.replace("\\", "/");

        }
        return stringData;
    }

    /**
     * Uses document to write an XML file containing all the data in this data submission.
     * 
     * @param fileName
     *            : the name of the file to write
     * @return
     */
    public boolean write(String fileName)
    {

        if (document == null)
        {
            errorMessage = "Cannot write XML: document is not constructed.";
            return false;
        }

        try
        {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);

            File file = new File(rootPath + fileName);
            path = file.getAbsolutePath();
            StreamResult result = new StreamResult(file.toString().replaceAll("%20", " "));

            transformer.transform(source, result);
            return true;
        }
        catch (TransformerException e)
        {
            errorMessage = "DataFileBuilder could not constrcut the XML tree. [TransformerException]";
            e.printStackTrace();
            return false;
        }
    }

}

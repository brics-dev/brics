
package gov.nih.tbi.dictionary.validation.parser;

import gov.nih.tbi.ApplicationsConstants;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.dictionary.validation.model.DataStructureTable;
import gov.nih.tbi.dictionary.validation.model.FileNode;
import gov.nih.tbi.dictionary.validation.model.FileNode.FileType;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.SwingWorker;

import au.com.bytecode.opencsv.CSVReader;

public class TableBuilder implements Callable<BufferedReader>
{

    FileNode node;
    BufferedReader reader;
    StructuralFormStructure structure;
    ConcurrentHashMap<FileNode, DataStructureTable> dataMap;
    String delim;
    char delimChar;
    SwingWorker worker;

    public TableBuilder(FileNode node, BufferedReader reader, StructuralFormStructure structure,
            ConcurrentHashMap<FileNode, DataStructureTable> dataMap, SwingWorker worker)
    {

        this.worker = worker;
        this.node = node;
        this.reader = reader;
        this.structure = structure;
        this.dataMap = dataMap;
        if (node.getType() == FileType.CSV)
        {
            delim = "[,]";
            delimChar = ',';
        }
        else
            if (node.getType() == FileType.TAB)
            {
                delim = "[\t]";
                delimChar = '\t';
            }
    }

    public BufferedReader getReader()
    {

        return reader;
    }

    public BufferedReader call() throws Exception
    {

        try
        {
            String[] columns = readNext();
            ArrayList<String[]> data = new ArrayList<String[]>(readAll());

            DataStructureTable table = new DataStructureTable(structure, columns, data, worker);

            dataMap.put(node, table);

            node.setErrorNum(table.getErrorCount());
            node.setWarnNum(table.getWarningCount());

            return reader;

        }
        catch (Exception e)
        {
            // Clean up
            node.setType(FileType.UNKNOWN);
            node.setStructureName("");
            throw e;
		} finally {
			reader.close();
        }
    }

    private String[] readNext() throws IOException
    {

        String line = reader.readLine();
        if (line != null)
        {

            String[] temp = line.split(delim);
            String[] output = new String[temp.length];
            for (int i = 0; i < temp.length; i++)
            {
                output[i] = temp[i].trim();
            }
            return output;
        }
        return null;
    }

    private ArrayList<String[]> readAll() throws IOException
    {

        ArrayList<String[]> output = new ArrayList<String[]>();
        CSVReader csvReader = new CSVReader(reader, delimChar, ApplicationsConstants.QUOTE_CHAR,
                ApplicationsConstants.ESCAPE_CHAR);
        String line[] = csvReader.readNext();
        while (line != null)
        {
            if (line.length > 0)
            {
                output.add(line);
            }
            line = csvReader.readNext();
        }
        return output;
    }

}

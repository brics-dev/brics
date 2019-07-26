
package gov.nih.tbi.dictionary.validation.model;

import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.repository.model.SubmissionType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class DataSubmission
{

    // Tree of all SubmissionFiles
    private final FileNode root;
    private final ConcurrentHashMap<FileNode, DataStructureTable> fileData;
    private final ConcurrentHashMap<String, Vector<FileNode>> structureData; // shortName to DataTables of the struct
                                                                             // type
    private List<StructuralFormStructure> dictionary;
    private boolean drafts;

    public DataSubmission(FileNode root, ConcurrentHashMap<FileNode, DataStructureTable> dataMap,
            ConcurrentHashMap<String, Vector<FileNode>> structMap, List<StructuralFormStructure> dictionary)
    {

        this.root = root;
        fileData = dataMap;
        structureData = structMap;
        this.dictionary = dictionary;
    }

    public FileNode getRoot()
    {

        return root;
    }

    public Set<FileNode> getDataNodes()
    {

        return fileData.keySet();
    }

    public ConcurrentHashMap<String, Vector<FileNode>> getStructureData()
    {

        return structureData;
    }

    public boolean isDataNode(FileNode node)
    {

        return getDataNodes().contains(node);
    }

    public DataStructureTable getFileData(FileNode node)
    {

        return fileData.get(node);
    }

    public ConcurrentHashMap<FileNode, DataStructureTable> getFileData()
    {

        return fileData;
    }

    public List<StructuralFormStructure> getDictionary()
    {

        return dictionary;
    }

    public SubmissionType getSubmissionTypeByName(String name)
    {

        for (StructuralFormStructure dataStructure : dictionary)
        {
            if (dataStructure.getShortName().equalsIgnoreCase(name))
            {
                return dataStructure.getFileType();
            }
        }

        return null;
    }

    public boolean hasDrafts()
    {

        return drafts;
    }

    public void setDrafts(boolean drafts)
    {

        this.drafts = drafts;
    }

    public Vector<DataStructureTable> getReferencedTables(String shortName)
    {

        Vector<DataStructureTable> output = new Vector<DataStructureTable>();
        for (FileNode node : structureData.get(shortName))
        {
            if (node.isIncluded())
            {
                output.add(fileData.get(node));
            }
        }
        return output;
    }

    // TODO: Michael - Referenced data is for conditional submissions
    // If the structure or column is not in the submission it will not be included in the map, I can throw more specific
    // errors later
    public HashMap<String, HashSet<String>> getReferencedData(DataTable table)
    {

        // HashMap<String, HashSet<String>> data = new HashMap<String, HashSet<String>>();
        // for(String shortName : table.getReferencedStructs()){
        // if (structureData.keySet().contains(shortName)){
        // for (String elementName : table.getReferencedElements(shortName)){
        // HashSet<String> values = new HashSet<String>();
        // for (DataTable refTable : getReferencedTables(shortName)){
        // values.addAll(refTable.getColumnValues(elementName));
        // }
        // if (!values.isEmpty()){
        // data.put(shortName + ValidationConstants.VALUE_REFERENCE_DIVIDER + elementName, values);
        // }
        // }
        // }
        // }
        //
        // return data;
        return null;
    }

    public void reset()
    {

        reset(root);
    }

    private void reset(FileNode node)
    {

        node.setValidated(false);
        DataTable table = fileData.get(node);
        if (table != null)
        {
            table.clearOutputs();
            node.setErrorNum(table.getErrorCount());
            node.setWarnNum(table.getWarningCount());
        }
        for (int i = 0; i < node.getChildCount(); i++)
        {
            reset((FileNode) node.getChildAt(i));
        }

    }

}

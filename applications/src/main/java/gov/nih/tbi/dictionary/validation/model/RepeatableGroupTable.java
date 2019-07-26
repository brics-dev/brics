
package gov.nih.tbi.dictionary.validation.model;

import gov.nih.tbi.commons.model.RequiredType;
import gov.nih.tbi.dictionary.model.hibernate.Alias;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.dictionary.model.hibernate.StructuralDataElement;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.dictionary.validation.model.ValidationOutput.OutputType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;


public class RepeatableGroupTable extends DataTable
{

    private static final long serialVersionUID = 1L;
    private final RepeatableGroup repeatableGroup;
    private final StructuralFormStructure dataStructure; // This is in repeatable group, but a reference is also stored
                                                         // here to
    // avoid casting it every time.
    private String[] columnNames;
    private ArrayList<String[]> data;
    private int size;
    private HashMap<String, String> aliasMap = new HashMap<String, String>(); // elements names to column name
                                                                              // (structure centeric)
    private HashMap<String, Integer> locationMap = new HashMap<String, Integer>();// element name or alias to location
                                                                                  // in columns
    private HashMap<Integer, Integer> parentMap = new HashMap<Integer, Integer>(); // row on the repeatable group table
                                                                                   // to the row the group instance
                                                                                   // belongs to on the parent table.
    private HashMap<Integer, Integer> dataFilePositionMap = new HashMap<Integer, Integer>(); // column in this table to
                                                                                             // the column on the raw
                                                                                             // data file
    // private HashMap<String, HashMap<AstTree, String>> conditionalMap = new HashMap<String, HashMap<AstTree,
    // String>>(); //element name to Conditional(Constraint AstTree, value)
	private HashMap<String, ArrayList<String>> referencedStructs = new HashMap<String, ArrayList<String>>(); // references
																												// map
                                                                                                       // of structures
                                                                                                       // (by short
                                                                                                       // names) to list
                                                                                                       // of columns
                                                                                                       // needed for
                                                                                                       // validation
    private HashMap<Integer, MapElement> elementMap = new HashMap<Integer, MapElement>(); // column position in the
                                                                                          // table to the map element of
                                                                                          // that repeatable group
    private boolean modified = false;

    private TreeSet<ValidationOutput> loadErrors = new TreeSet<ValidationOutput>();
    private TreeSet<ValidationOutput> loadWarnings = new TreeSet<ValidationOutput>();
    private TreeSet<ValidationOutput> validationErrors = new TreeSet<ValidationOutput>();
    private TreeSet<ValidationOutput> validationWarnings = new TreeSet<ValidationOutput>();

    public RepeatableGroupTable(RepeatableGroup rg, String[] columns)
    {

        this.repeatableGroup = rg;
        this.dataStructure = rg.getDataStructure();
        this.columnNames = columns;
        this.size = columns.length;
        data = new ArrayList<String[]>();
        buildLocationMap();
        buildElementMap();
    }

    /**
     * Adds a key value pair to the dataFilePositionMap
     * 
     * @param key
     * @param value
     */
    public void putDataFilePositionMapping(Integer key, Integer value)
    {

        dataFilePositionMap.put(key, value);
    }

    /**
     * Retrieves a value from the dataFilePositionMap based on a key
     * 
     * @param key
     * @return
     */
    public Integer getDataFilePositionMapping(Integer key)
    {

        return dataFilePositionMap.get(key);
    }

    /**
     * A getter for the 'size' member of this object
     * 
     * @return
     */
    public int getSize()
    {

        return size;
    }

    /**
     * Builds the elementMap hash map maps column positions to the map elements in repeatable group
     */
    private void buildElementMap()
    {

        for (int i = 0; i < size; i++)
        {
            for (MapElement me : repeatableGroup.getMapElements())
            {
                if (me.getStructuralDataElement().getName().equalsIgnoreCase(columnNames[i]))
                {
                    elementMap.put(i, me);
                    break;
                }
            }
        }
    }

    /**
     * Returns a value from elementMap given a key
     * 
     * @param key
     *            : the key for the hash map
     * @return
     */
    public MapElement getElementMapping(Integer key)
    {

        return elementMap.get(key);
    }

    /**
     * Returns the row on this table that is referenced by the parent data structure table and desired row (argument).
     * If such a row does not exist or it is specified that we want a new row, then one is created and its position is
     * returned.
     * 
     * @param parentRow
     * @param newGroup
     * @return
     */
    public Integer getRowIndex(int parentRow, boolean newGroup)
    {

        Integer row = parentMap.get(parentRow);
        if (row == null || newGroup)
        {
            data.add(new String[size]);
            parentMap.put(parentRow, data.size() - 1);
            row = data.size() - 1;
        }
        return row;
    }

    private void buildLocationMap()
    {

        for (int i = 0; i < columnNames.length; i++)
        {
            locationMap.put(columnNames[i].toLowerCase(), i);
        }
        // Add aliases
        for (MapElement me : repeatableGroup.getMapElements())
        {
            Integer position = locationMap.get(me.getStructuralDataElement().getName().toLowerCase());
            for (Alias a : me.getStructuralDataElement().getAliasList())
            {
                locationMap.put(a.getName().toLowerCase(), position);
            }
        }
    }

    public void populateTable(ArrayList<String[]> data)
    {

        this.data = fitData(data);
    }

    @Override
    public int getRowCount()
    {

        return data.size();
    }

    @Override
    public int getColumnCount()
    {

        return size;
    }

    @Override
    public String getColumnName(int column)
    {

        return columnNames[column];
    }

    public int getColumnIndex(String columnName)
    {

        for (int i = 0; i < columnNames.length; i++)
        {
            if (columnName.equalsIgnoreCase(columnNames[i]))
            {
                return i;
            }
        }

        return -1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex)
    {

        return data.get(rowIndex)[columnIndex];
    }

    public void putValueAt(String value, int rowIndex, int columnIndex)
    {

        String[] row = data.get(rowIndex);
        row[columnIndex] = value;
        data.add(rowIndex, row);
        data.remove(rowIndex + 1);
    }

    private ArrayList<String[]> fitData(ArrayList<String[]> data)
    {

        for (int i = 0; i < data.size(); i++)
        {
            String[] row = data.get(i);
            if (row.length < size)
            {
                String[] replace = new String[size];
                for (int j = 0; j < size; j++)
                {
                    if (j < row.length)
                    {
                        replace[j] = row[j];
                    }
                    else
                        replace[j] = "";
                }
                int numMissing = size - row.length;
                String message = "Row " + (i + 3) + " of data structure " + getStructureName()
                        + " and repeatable element group " + getRepeatableGroupName() + " is missing " + numMissing
                        + " data entrie(s). The row has been padded at the end to table size.";
                loadWarnings.add(new ValidationOutput(this, OutputType.WARNING, i, -1, message));
                data.remove(i);
                data.add(i, replace);
            }
            else
                if (row.length > size)
                {
                    String[] replace = new String[size];
                    for (int j = 0; j < size; j++)
                    {
                        replace[j] = row[j];
                    }
                    ArrayList<String> extra = new ArrayList<String>();
                    for (int j = size; j < row.length; j++)
                    {
                        extra.add(row[j]);
                    }
                    String message = "Row " + (i + 3) + " of data structure " + getStructureName()
                            + " and repeatable element group " + getRepeatableGroupName() + " contains " + extra.size()
                            + " additional data entries - " + extra + "The additional entries will be ignored.";
                    loadWarnings.add(new ValidationOutput(this, OutputType.WARNING, i, -1, message));
                    data.remove(i);
                    data.add(i, replace);
                }
        }
        return data;
    }

    /**
     * This
     * 
     * @param group
     * @param columns
     */
    private void popluateAliasMap(RepeatableGroup group, String[] columns)
    {

        for (MapElement me : group.getMapElements())
        {
            String name = me.getStructuralDataElement().getName();
            for (int j = 0; j < columns.length; j++)
            {
                String col = columns[j];
                if (name.equalsIgnoreCase(col) || inAliases(me.getStructuralDataElement(), col))
                {
                    if (!aliasMap.containsKey(name))
                    {
                        aliasMap.put(name, col);
                        locationMap.put(name, j);
                    }
                    else
                    {
                        String message = "In data structure " + getStructureName() + " and repeatable element group "
                                + getRepeatableGroupName() + " two or more columns reference the element " + name
                                + ", already matched to " + aliasMap.get(name);
                        loadErrors.add(new ValidationOutput(this, OutputType.ERROR, -1, j, message));
                    }
                }
            }
            if (!aliasMap.containsKey(name))
            {
                if (me.getRequiredType().equals(RequiredType.REQUIRED))
                {
                    String message = "Data structure " + getStructureName() + " and repeatable element group "
                            + getRepeatableGroupName() + " identifies element " + name
                            + " as being required. No match was found.";
                    loadErrors.add(new ValidationOutput(this, OutputType.ERROR, -1, -1, message));
                }
                else
                    if (me.getRequiredType().equals(RequiredType.RECOMMENDED))
                    {
                        String message = "Data structure " + getStructureName() + " and repeatable element group "
                                + getRepeatableGroupName() + " identifies element " + name
                                + " as being recommended. No match was found.";
                        loadWarnings.add(new ValidationOutput(this, OutputType.WARNING, -1, -1, message));
                    }
                    else
                        if (me.getRequiredType().equals(RequiredType.OPTIONAL))
                        {
                            String message = "Data structure " + getStructureName() + " and repeatable element group "
                                    + getRepeatableGroupName() + " identifies element " + name
                                    + " as being conditional. No match was found.";
                            loadWarnings.add(new ValidationOutput(this, OutputType.WARNING, -1, -1, message));
                        }
            }
        }
        // Catch the empty, not included columns
        for (int j = 0; j < columns.length; j++)
        {
            String col = columns[j];
            if (col.equals(""))
            {
                String message = "In data structure " + getStructureName() + " and repeatable element group "
                        + getRepeatableGroupName() + ", column " + (j + 1)
                        + " has a empty header. It will be ignored on upload.";
                loadWarnings.add(new ValidationOutput(this, OutputType.WARNING, -1, j, message));
            }
            else
                if (!aliasMap.containsValue(col))
                {
                    String message = "In data structure " + getStructureName() + " and repeatable element group "
                            + getRepeatableGroupName() + ", column " + (j + 1) + ", " + col
                            + " does not match any of the elements call out in structure "
                            + dataStructure.getShortName() + ". It will be ignored on upload.";
                    loadWarnings.add(new ValidationOutput(this, OutputType.WARNING, -1, j, message));
                }
        }

    }

    private boolean inAliases(StructuralDataElement element, String s)
    {

        for (Alias alias : element.getAliasList())
        {
            if (alias.getName().equalsIgnoreCase(s))
            {
                return true;
            }
        }
        return false;
    }

    public String getStructureName()
    {

        return dataStructure.getShortName();
    }

    public String getRepeatableGroupName()
    {

        return repeatableGroup.getName();
    }

    public String getAlias(String elementName)
    {

        return aliasMap.get(elementName);
    }

    @Override
    public TreeSet<ValidationOutput> getErrors()
    {

        TreeSet<ValidationOutput> errors = new TreeSet<ValidationOutput>();
        errors.addAll(loadErrors);
        errors.addAll(validationErrors);
        return errors;
    }

    @Override
    public TreeSet<ValidationOutput> getWarnings()
    {

        TreeSet<ValidationOutput> warnings = new TreeSet<ValidationOutput>();
        warnings.addAll(loadWarnings);
        warnings.addAll(validationWarnings);
        return warnings;
    }

    // @Override
    // public DataStructure getStructure() {
    // return dataStructure;
    // }

    public RepeatableGroup getRepeatableGroup()
    {

        return repeatableGroup;
    }

    @Override
    public Set<String> getElementNames()
    {

        return locationMap.keySet();
    }

    public String[] getRow(int index)
    {

        return data.get(index);
    }

    /**
     * Returns the location of the position of the data element in the table given a name. Can handle alias names
     * because alias names are in the location map. Returns null if the element cannot be found.
     * 
     * @param name
     *            : the data element name or alias you want the location for
     * @return : the column position of the requested data element
     */
    public Integer getLocation(String name)
    {

        return locationMap.get(name.toLowerCase());
    }

    public HashSet<String> getColumnValues(String elementName)
    {

        HashSet<String> values = new HashSet<String>();
        Integer index = locationMap.get(elementName);
        if (index != null)
        {
            for (String[] row : data)
            {
                String s = row[index];
                if (!s.isEmpty())
                {
                    values.add(s);
                }
            }
        }
        return values;
    }

    @Override
    public void addOutput(ValidationOutput output)
    {

        if (output.getType() == OutputType.ERROR)
        {
            validationErrors.add(output);
        }
        else
            if (output.getType() == OutputType.WARNING)
            {
                validationWarnings.add(output);
            }
    }

    @Override
    public void clearOutputs()
    {

        validationWarnings.clear();
        validationErrors.clear();
    }

    @Override
    public boolean isModified()
    {

        return modified;
    }

    @Override
    public void save()
    {

        modified = false;
    }

    @Override
    public int getErrorCount()
    {

        return (loadErrors.size() + validationErrors.size());
    }

    @Override
    public int getWarningCount()
    {

        return (loadWarnings.size() + validationWarnings.size());
    }

}

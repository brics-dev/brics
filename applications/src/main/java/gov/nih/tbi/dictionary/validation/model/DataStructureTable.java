package gov.nih.tbi.dictionary.validation.model;

// import gov.nih.ndar.util.validation.AstTree;
import gov.nih.tbi.ApplicationsConstants;
import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.commons.model.RequiredType;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.dictionary.validation.model.ValidationOutput.OutputType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.SwingWorker;

public class DataStructureTable extends DataTable {

	private static final long serialVersionUID = 1L;
	private final StructuralFormStructure structure;
	SwingWorker worker;

	/**
	 * The map element names as they appear in the data file in the order they appear in the data file.
	 */
	private String[] columnNames;

	/**
	 * References to the repeatable group tables. These references correspond to the columns in the DataStructureTable
	 */
	private RepeatableGroupTable[] repeatableGroupTables;
	/**
	 * The number of columns in this table
	 */
	private int size;
	/**
	 * An array of booleans which determine which raw data columns are excluded. The 0th position of the array
	 * (referencing the new record column) is undefined. true = column is included in dataStructure false = column is
	 * being ignored
	 */
	private boolean[] includedColumns;
	private ArrayList<String[]> data; // The data in this table is a comma delimited list of values that references rows
										// on a RepeatableGroupTable
	private HashMap<String, Integer> locationMap = new HashMap<String, Integer>();// the name of the repeatable group to
																					// location in rows
	private HashMap<String, Vector<String>> referencedStructs = new HashMap<String, Vector<String>>(); // references map
																										// of structures
																										// (by short
																										// names) to
																										// list
																										// of columns
																										// needed for
																										// validation
	/**
	 * A mapping of a column from the data file to a 2-diamensional array containing the column position of its
	 * repeatbale group and the column position of the data element within the repeatbale group.
	 */
	private HashMap<String, Integer[]> elementMap = new HashMap<String, Integer[]>();
	private boolean modified = false;

	private TreeSet<ValidationOutput> loadErrors = new TreeSet<ValidationOutput>();
	private TreeSet<ValidationOutput> loadWarnings = new TreeSet<ValidationOutput>();
	private TreeSet<ValidationOutput> validationErrors = new TreeSet<ValidationOutput>();
	private TreeSet<ValidationOutput> validationWarnings = new TreeSet<ValidationOutput>();

	public DataStructureTable(StructuralFormStructure struct, String[] columns, ArrayList<String[]> data,
			SwingWorker worker) {

		this.worker = worker;
		this.structure = struct;
		this.columnNames = columns;
		createRepeatableGroupTables();
		this.size = repeatableGroupTables.length;

		Integer recordLocation = null;

		// Populate the included columns array
		includedColumns = new boolean[columns.length];
		for (int i = 0; i < includedColumns.length; i++) {
			includedColumns[i] = true;
			if (columnNames[i].equals(ModelConstants.RECORD_STRING)) {
				recordLocation = i;
			}
		}
		// includedColumns[0] = false;

		if (recordLocation != null && recordLocation == 0) {
			includedColumns[recordLocation] = false;

			populateElementMap();
			this.data = fitData(data);
			populateConditionalMap(struct);
		} else if (recordLocation == null) {
			String message = String.format(ApplicationsConstants.LOC_FILE, structure.getShortName());
			message += String.format(ApplicationsConstants.ERR_RECORD_MISSING);

			loadErrors.add(new ValidationOutput(this, OutputType.ERROR, -1, -1, message));
		} else {
			String message = String.format(ApplicationsConstants.LOC_FILE, structure.getShortName());
			message += String.format(ApplicationsConstants.ERR_RECORD_LOCATION);

			loadErrors.add(new ValidationOutput(this, OutputType.ERROR, -1, -1, message));
		}

	}

	public DataStructureTable() {

		structure = null;
	}

	/**
	 * This function creates a mapping from column names in the data file to a array[2] containing the [index of
	 * repeatable group, index of the column in that repeatable group where the map element is located] The function
	 * also checks for extra/missing columns.
	 */
	private void populateElementMap() {

		int numDuplicates = 0;
		// iterate over columns, looking for matches
		for (int i = 0; i < columnNames.length; i++) {
			if (includedColumns[i] == true) {
				Integer[] value = new Integer[2];

				// If no repeatable group is listed then assume it belongs in the group 'main'
				if (!columnNames[i].contains(".")) {
					columnNames[i] = "main." + columnNames[i];
				}
				columnNames[i] = columnNames[i].toLowerCase();
				String rgName = columnNames[i].split("\\.")[0];
				String deName = columnNames[i].split("\\.")[1];

				// Find the repeatableGroup
				value[0] = locationMap.get(rgName);
				// If the repeatableGroup is not found, then throw an error
				if (value[0] == null) {
					String message =
							String.format(
									"Could not identify '%s' at column %d as a valid repeatable Group. "
											+ "Please reference this form structure in the data dictionary and verify that the group name matches exactly.",
									rgName, i);
					loadErrors.add(new ValidationOutput(this, OutputType.ERROR, -1, i, message));
					includedColumns[i] = false;
					continue;
				}
				RepeatableGroupTable rgTable = repeatableGroupTables[value[0]];
				// Iterate through the repeatableGroup columns and look for a match to the name or an alias
				value[1] = rgTable.getLocation(deName);
				if (value[1] == null) {
					String message =
							String.format(
									"Could not identify '%s' in repeatable group '%s' at column %d as a valid data element. "
											+ "Please reference this form structure in data dictionary and verify that the element name matches exactly.",
									deName, rgName, i);
					loadErrors.add(new ValidationOutput(this, OutputType.ERROR, -1, i, message));
					includedColumns[i] = false;
					continue;
				}

				// Check to see if this column is a duplicate
				for (Integer[] array : elementMap.values()) {
					if (array[0].equals(value[0]) && array[1].equals(value[1])) {
						// This element is a duplicate
						String message =
								"The column, " + columnNames[i] + " located at position " + i
										+ " has been identified as a duplicate. This column will be ignored.";
						loadWarnings.add(new ValidationOutput(this, OutputType.WARNING, -1, i, message));
						includedColumns[i] = false;
						numDuplicates++;
						break;
					}
				}

				elementMap.put(columnNames[i], value);
				// Add to the dataFilePositionMap which maps a column position in a rgTable to the columns position in
				// the data file
				rgTable.putDataFilePositionMapping(value[1], i + 1);
			}
		}

		if (elementMap.size() + numDuplicates < structure.getDataElements().size()) {
			// There is at least one column missing
			for (int i = 0; i < size; i++) {
				for (int x = 0; x < repeatableGroupTables[i].getSize(); x++) {
					boolean found = false;
					for (Integer[] array : elementMap.values()) {
						if (array[0].equals(i) && array[1].equals(x)) {
							found = true;
							break;
						}
					}
					if (!found) {
						MapElement me = repeatableGroupTables[i].getElementMapping(x);

						if (RequiredType.REQUIRED.equals(me.getRequiredType())) {
							String message =
									String.format(ApplicationsConstants.LOC_NAME_GROUP, me.getStructuralDataElement()
											.getName(), repeatableGroupTables[i].getRepeatableGroup().getName());

							message +=
									String.format(ApplicationsConstants.ERR_MISSING_REQUIRED, structure.getShortName());

							loadErrors.add(new ValidationOutput(this, OutputType.ERROR, -1, -1, message));
						} else if (RequiredType.RECOMMENDED.equals(me.getRequiredType())) {
							String message =
									String.format(ApplicationsConstants.LOC_NAME_GROUP, me.getStructuralDataElement()
											.getName(), repeatableGroupTables[i].getRepeatableGroup().getName());

							message +=
									String.format(ApplicationsConstants.ERR_MISSING_RECOMMENDED,
											structure.getShortName());

							loadWarnings.add(new ValidationOutput(this, OutputType.WARNING, -1, -1, message));
						}
					}
				}
			}
		}
	}

	/**
	 * Returns the array of repeatableGroupTable row references stored in the indicated cell. Returns an empty array
	 * list if there are no references in this location. The data param is optional. If one is passed then the
	 * references are retrived from the specified data table. If data is null then the data property for this structure
	 * is used.
	 * 
	 * @param rowIndex
	 * @param columnIndex
	 * @param data
	 * @return
	 */
	public ArrayList<Integer> getAllReferences(int rowIndex, int columnIndex, ArrayList<String[]> data) {

		// If a data table is not passed in, then we use the one for this structure.
		if (data == null) {
			data = this.data;
		}

		ArrayList<Integer> values = new ArrayList<Integer>();
		String cellContents = data.get(rowIndex)[columnIndex];
		if (cellContents != null) {
			String[] string = cellContents.trim().split(",");
			for (String s : string) {
				values.add(Integer.parseInt(s));
			}
		}
		return values;
	}

	private Integer getLastReference(int rowIndex, int columnIndex, ArrayList<String[]> data) {

		// If a data table is not passed in, then we use the one for this structure.
		if (data == null) {
			data = this.data;
		}

		ArrayList<Integer> values = getAllReferences(rowIndex, columnIndex, data);
		return values.get(values.size() - 1);
	}

	/**
	 * Returns the specific row referenced in a repeatbaleGroupTable and stored in the indicated cell. Returns null if
	 * the zIndex is out of bounds. The data arg is optional. If one is specified then the reference from that table is
	 * used, if left null, then the data property of this structure is used.
	 * 
	 * @param zIndex
	 * @param rowIndex
	 * @param columnIndex
	 * @param data
	 * @return
	 */
	private Integer getReference(Integer zIndex, int rowIndex, int columnIndex, ArrayList<String[]> data) {

		// If a data table is not passed in, then we use the one for this structure.
		if (data == null) {
			data = this.data;
		}

		ArrayList<Integer> values = getAllReferences(rowIndex, columnIndex, data);
		try {
			return values.get(zIndex);
		} catch (IndexOutOfBoundsException e) {
			return null;
		}
	}

	/**
	 * Adds a references to the indicated cell. This function does not check for duplicates.
	 * 
	 * @param value
	 * @param rowIndex
	 * @param columnIndex
	 * @param data
	 */
	private void addReference(Integer value, int rowIndex, int columnIndex, ArrayList<String[]> data) {

		// If a data table is not passed in, then we use the one for this structure.
		if (data == null) {
			data = this.data;
		}

		String contents = data.get(rowIndex)[columnIndex];
		if (contents == null) {
			contents = value.toString();
		} else {
			contents = contents + "," + value.toString();
		}
		modified = true;
		data.get(rowIndex)[columnIndex] = contents;
	}

	/**
	 * Creates an empty (no data) RepeatableGroupTable for each repeatable group in the data structure. Groups are
	 * created based on the structure not the data in the datafile. The locationMap is also created which maps a
	 * repeatableGroup table to its column in the table.
	 */
	private void createRepeatableGroupTables() {

		repeatableGroupTables = new RepeatableGroupTable[structure.getRepeatableGroups().size()];
		int i = 0;
		for (RepeatableGroup rg : structure.getRepeatableGroups()) {
			// Due to lazy loading, the repeatable groups do not have their structure field populated
			// so we do it here
			rg.setDataStructure(structure);

			// Create a list of the elements in the repeatable group
			String[] columns = new String[rg.getMapElements().size()];
			int y = 0;
			for (MapElement me : rg.getMapElements()) {
				columns[y] = me.getStructuralDataElement().getName().toLowerCase();
				y++;
			}
			repeatableGroupTables[i] = new RepeatableGroupTable(rg, columns);

			locationMap.put(rg.getName().toLowerCase(), i);
			i++;
		}
	}

	private ArrayList<String[]> fitData(ArrayList<String[]> dataIn) {

		ArrayList<String[]> dataOut = new ArrayList<String[]>();
		String[] currentRecord;

		// Loop through every row in the data file
		for (int i = 0; i < dataIn.size(); i++) {
			String[] row = dataIn.get(i);
			worker.firePropertyChange("progress", 0, 1);

			// check to make sure there is an identifier in the A3			
			if(i == 0){
				if (row[0] == null || row[0].trim().isEmpty()) {
					loadErrors.add(new ValidationOutput(this, OutputType.ERROR, i + 3, 1, String.format(
							ApplicationsConstants.WARN_INVALID_DATA_FORMAT, i + 3, this.getStructureName())));
				}
			}
			
			// If there is data in the first position then create a new record
			if (row[0] != null && !row[0].trim().isEmpty()) {
				currentRecord = new String[size];
				dataOut.add(currentRecord);
			}

			// For every row in the datafile (corresponding with repeatable groups), we need a way to track
			// if a repeatable group has been created for the row in the raw data file.
			boolean rgEntryCreated[] = new boolean[size];

			// Loop through the columns in the record and create a add the data to the new table
			for (int x = 1; x < row.length; x++) {

				// BL: ensure data cell content is not in a column without a header (note x is column cursor and x
				// begins at index 1)
				if (x >= includedColumns.length) {

					// BL: only output for cells with content in it
					if (dataIn.get(i)[x] != null && !dataIn.get(i)[x].trim().isEmpty()) {

						loadErrors.add(new ValidationOutput(this, OutputType.ERROR, i + 2, x,
								ApplicationsConstants.ERR_CELL_WITH_NO_HEADER));
					}

					continue;
				}

				// Make sure that this column is not excluded and this cell is not blank (or null)
				if (!includedColumns[x] || dataIn.get(i)[x] == null || dataIn.get(i)[x].trim().isEmpty()) {
					continue;
				}

				String rawName = columnNames[x];
				Integer[] location = elementMap.get(rawName);

				// The getRowIndex function will return the index of the row we need to add to in the repeatable group
				// table. If the second argument
				// is true, which it should be when we are inserting the first element of a repeatable group, then a new
				// row will be created.
				Integer repeatableGroupRow =
						repeatableGroupTables[location[0]]
								.getRowIndex(dataOut.size() - 1, !rgEntryCreated[location[0]]);
				rgEntryCreated[location[0]] = true;

				// If the reference to the repeatable group row is not yet in the DS table then add it there.
				if (!getAllReferences(dataOut.size() - 1, location[0], dataOut).contains(repeatableGroupRow)) {
					addReference(repeatableGroupRow, dataOut.size() - 1, location[0], dataOut);
				}
				// Add to the table
				repeatableGroupTables[location[0]].putValueAt(dataIn.get(i)[x], repeatableGroupRow, location[1]);

			}
		}

		return dataOut;
	}

	//
	// private ArrayList<String[]> fitDataOld(ArrayList<String[]> data){
	// for (int i = 0; i < data.size(); i++){
	// String[] row = data.get(i);
	// if (row.length < size){
	// String[] replace = new String[size];
	// for(int j = 0; j < size; j++){
	// if(j < row.length){
	// replace[j] = row[j];
	// }else
	// replace[j] = "";
	// }
	// int numMissing = size - row.length;
	// String message = "Row " + (i+3) + " of data structure " + getStructureName() + " is missing " + numMissing +
	// " data entrie(s). The row has been padded at the end to table size." ;
	// loadWarnings.add(new ValidationOutput(this, OutputType.WARNING, i, -1, message));
	// data.remove(i);
	// data.add(i, replace);
	// }else if(row.length > size){
	// String[] replace = new String[size];
	// for(int j = 0; j < size; j++){
	// replace[j] = row[j];
	// }
	// ArrayList<String> extra = new ArrayList<String>();
	// for(int j = size; j< row.length; j++){
	// extra.add(row[j]);
	// }
	// String message = "Row " + (i+3) + " of data structure " + getStructureName() + " contains " + extra.size() +
	// " additional data entries - " + extra + "The additional entries will be ignored." ;
	// loadWarnings.add(new ValidationOutput(this, OutputType.WARNING, i, -1, message));
	// data.remove(i);
	// data.add(i, replace);
	// }
	// }
	// return data;
	// }

	// XXX: Since we don't currently support conditional logic this has just been commented out
	private void populateConditionalMap(StructuralFormStructure struct) {

		// for (AbstractDataElement element : struct.getDataElements()){
		// HashMap<AstTree, String> valueMap = new HashMap<AstTree, String>();
		// for (IConditional cond : element.getConditionals()){
		// Vector<String> tokens = ValidationUtil.tokenizeConstraint(cond.getConstraint());
		// try{
		// AstTree tree = new AstTree(struct.getShortName(), tokens);
		// valueMap.put(tree, cond.getValueRange());
		// HashSet<String> refs = new HashSet<String>(tree.getColumnRefs());
		// for (String s : ValidationUtil.tokenizeRange(cond.getValueRange())){
		// if (ValidationUtil.isRowRef(s)){
		// refs.add(getStructureName() + ValidationConstants.VALUE_REFERENCE_DIVIDER + s.substring(1));
		// }else if(ValidationUtil.isColRef(s)){
		// if (!s.contains(ValidationConstants.VALUE_REFERENCE_DIVIDER)){
		// refs.add(getStructureName() + ValidationConstants.VALUE_REFERENCE_DIVIDER + s.substring(1));
		// }else{
		// refs.add(s.substring(1));
		// }
		// }
		// }
		// for (String s : refs){
		// String[] fullName = s.split("\\" + ValidationConstants.VALUE_REFERENCE_DIVIDER);
		// String shortName = fullName[0];
		// String columnName = fullName[1];
		// if (!referencedStructs.keySet().contains(shortName)){
		// referencedStructs.put(shortName, new Vector<String>());
		// }
		// referencedStructs.get(shortName).add(columnName);
		// }
		// }catch (ParseException e){
		// //Theoretically these should all be valid
		// e.printStackTrace();
		// }
		// }
		// if (!valueMap.isEmpty()){
		// conditionalMap.put(element.getName(), valueMap);
		// }
		// }
	}

	public String getStructureName() {

		return structure.getShortName();
	}

	// Swing Table Interface
	public int getColumnCount() {

		return size;
	}

	public int getRowCount() {

		return data.size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {

		return data.get(rowIndex)[columnIndex];
	}

	public Class<?> getColumnClass(int columnIndex) {

		return String.class;
	}

	public String getColumnName(int columnIndex) {

		return columnNames[columnIndex];
	}

	public RepeatableGroupTable getRepeatableGroupTable(int columnIndex) {

		return repeatableGroupTables[columnIndex];
	}

	// Or it is possible that the errors from the repeatable groups need to be added to the loadErrors and
	// validationErrors
	// line elsewhere.
	public TreeSet<ValidationOutput> getErrors() {

		TreeSet<ValidationOutput> errors = new TreeSet<ValidationOutput>();
		errors.addAll(loadErrors);
		errors.addAll(validationErrors);
		return errors;
	}

	public TreeSet<ValidationOutput> getWarnings() {

		TreeSet<ValidationOutput> warnings = new TreeSet<ValidationOutput>();
		warnings.addAll(loadWarnings);
		warnings.addAll(validationWarnings);
		return warnings;
	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {

		return true;
	}

	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {

		modified = true;
		data.get(rowIndex)[columnIndex] = (String) aValue;
	}

	//

	public StructuralFormStructure getStructure() {

		return structure;
	}

	// Names of the elements that are actually included
	public Set<String> getElementNames() {

		return locationMap.keySet();
	}

	public String[] getRow(int index) {

		return data.get(index);
	}

	public Integer getLocation(String name) {

		return locationMap.get(name);
	}

	// TODO: Michael - Aliases are no longer supported
	public String getAlias(String elementName) {

		// return aliasMap.get(elementName);
		return null;
	}

	// public HashMap<AstTree, String> getConditionals(String elementName){
	// return conditionalMap.get(elementName);
	// }

	public HashSet<String> getColumnValues(String elementName) {

		throw new UnsupportedOperationException();
	}

	public Set<String> getReferencedStructs() {

		return referencedStructs.keySet();
	}

	public Vector<String> getReferencedElements(String shortName) {

		return referencedStructs.get(shortName);
	}

	public void addOutput(ValidationOutput output) {

		if (output.getType() == OutputType.ERROR) {
			validationErrors.add(output);
		} else if (output.getType() == OutputType.WARNING) {
			validationWarnings.add(output);
		}
	}

	public void clearOutputs() {

		validationWarnings.clear();
		validationErrors.clear();
	}

	public boolean isModified() {

		return modified;
	}

	public void save() {

		modified = false;
	}

	public int getErrorCount() {

		return (loadErrors.size() + validationErrors.size());
	}

	public int getWarningCount() {

		return (loadWarnings.size() + validationWarnings.size());
	}

}

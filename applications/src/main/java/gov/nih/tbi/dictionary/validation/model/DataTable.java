
package gov.nih.tbi.dictionary.validation.model;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.table.AbstractTableModel;

public abstract class DataTable extends AbstractTableModel
{

    private static final long serialVersionUID = 8860785717096139375L;

    public abstract String getStructureName();

    public abstract TreeSet<ValidationOutput> getErrors();

    public abstract TreeSet<ValidationOutput> getWarnings();;

    // Names of the elements that are actually included
    public abstract Set<String> getElementNames();

    public abstract String[] getRow(int index);

    public abstract Integer getLocation(String name);

    public abstract HashSet<String> getColumnValues(String elementName);

    // public Set<String> getReferencedStructs() {
    // return referencedStructs.keySet();
    // }

    // public Vector<String> getReferencedElements(String shortName){
    // return referencedStructs.get(shortName);
    // }

    public abstract void addOutput(ValidationOutput output);

    /**
     * Clears all of the validation warnings and errors.
     */
    public abstract void clearOutputs();

    public abstract boolean isModified();

    public abstract void save();

    public abstract int getErrorCount();

    public abstract int getWarningCount();

}

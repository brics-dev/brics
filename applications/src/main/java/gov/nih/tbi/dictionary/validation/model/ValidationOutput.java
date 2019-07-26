
package gov.nih.tbi.dictionary.validation.model;

public class ValidationOutput implements Comparable<ValidationOutput>
{

    // Types listed first will show up first in the output; orders must match
    public static final String[] TYPES = new String[] { "Error", "Warning" };

    public enum OutputType
    {
        ERROR, WARNING
    };

    private final DataTable table;
    private final OutputType type;
    // -1 is being used to signal that the error is not associated with a specific row or column
    private int column = -1;
    private int row = -1;
    private String message;
    private String messagePrefix;

    public ValidationOutput(DataTable table, OutputType type, int row, int col, String message)
    {

        this.table = table;
        this.type = type;
        this.row = row;
        this.column = col;
        this.message = message;
        messagePrefix = "";
    }

    public String getStructureName()
    {

        return table.getStructureName();
    }

    public String getElementName()
    {

        if (column >= 0)
        {
            try
            {
                return table.getColumnName(column);
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String getTypeString()
    {

        return TYPES[type.ordinal()];
    }

    public OutputType getType()
    {

        return type;
    }

    public int getColumn()
    {

        return column;
    }

    public int getRow()
    {

        return row;
    }

    public String getMessage()
    {

        return messagePrefix + message;
    }
    
    public void setMessagePrefix(String newMessage){
        messagePrefix = newMessage;
    }

    public int hashCode()
    {

        return getStructureName().hashCode() + getElementName().hashCode() + column + row + type.ordinal()
                + message.hashCode();
    }

    public boolean equals(Object obj)
    {

        if (obj == this)
        {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass())
        {
            return false;
        }

        ValidationOutput output = (ValidationOutput) obj;
        return (table == output.table && type == output.type && row == output.row && column == output.column && message
                .equalsIgnoreCase(output.message));
    }

    public int compareTo(ValidationOutput output)
    {

        int result = row - output.row;
        if (result == 0)
        {
            result = column - output.column;
        }

        if (result == 0)
        {
            result = message.compareTo(output.message);
        }

        return result;
    }

    public String toString()
    {

        return messagePrefix + message;
    }

}


package gov.nih.tbi.dictionary.model;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * This class is a model that can hold sample data for validation, it has two methods that can directly turn this object
 * into something that the validation tool can use.
 * 
 * @author Andrew Johnson
 */
public class DictionaryData implements Serializable
{

    /**
	 * 
	 */
    private static final long serialVersionUID = -4854982967013857106L;

    /*************************************************************************************/

    ArrayList<String> header;
    ArrayList<ArrayList<String>> dataList;

    /*************************************************************************************/

    private ArrayList<String> getHeader()
    {

        if (header == null)
        {
            header = new ArrayList<String>();
        }

        return header;
    }

    private ArrayList<ArrayList<String>> getDataList()
    {

        if (dataList == null)
        {
            dataList = new ArrayList<ArrayList<String>>();
        }

        return dataList;
    }

    public int getRowCount()
    {

        return getDataList().size();
    }

    /*************************************************************************************/

    /**
     * Adds a new column to the table, or returns the index of the existing column
     * 
     * @param column
     *            (String) - name of the column
     * 
     * @return index of the column
     */
    public int addColumn(String column)
    {

        int col = getColumn(column);

        if (col == -1)
        {
            getHeader().add(column);

            for (ArrayList<String> row : getDataList())
            {
                row.add(null);
            }

            col = getColumn(column);
        }

        return col;
    }

    /**
     * Gets the index of the column with the parameter name
     * 
     * @param column
     *            (String) - name of the column to find
     * 
     * @return index of the column
     */
    private int getColumn(String column)
    {

        return getHeader().indexOf(column);
    }

    /**
     * Adds a row to the table and returns the row number
     * 
     * @return row index
     */
    public int addRow()
    {

        int nextRow = getDataList().size() + 1;

        getDataList().add(new ArrayList<String>());

        for (String col : getHeader())
        {
            this.set(nextRow, col, null);
        }

        return nextRow;
    }

    /**
     * Sets the value at row, column to value
     * 
     * @param row
     *            (int) - row index
     * @param column
     *            (int) - column index
     * @param value
     *            (String) - value to insert
     */
    public void set(int row, String column, String value)
    {

        int col = getColumn(column);

        if (col == -1)
        {
            throw new IllegalArgumentException(column);
        }

        getDataList().get(row).set(col, value);
    }

    /*************************************************************************************/

    /**
     * Convenience method for turning this table into DataStructureTable for validation tool
     * 
     * @return
     */
    public String[] getColumnNames()
    {

        return header.toArray(new String[0]);
    }

    /**
     * Convenience method for turning this table into DataStructureTable for validation tool
     * 
     * @return
     */
    public ArrayList<String[]> getData()
    {

        ArrayList<String[]> out = new ArrayList<String[]>();

        for (ArrayList<String> row : getDataList())
        {
            out.add(row.toArray(new String[0]));
        }

        return out;
    }

    /**
     * Outputs to a two dimensional String array
     * 
     * @return String[][]
     */
    public String[][] getMatrix()
    {

        String[][] out = new String[getRowCount() + 1][getHeader().size()];

        int rowCount = 0;
        int colCount = 0;

        for (colCount = 0; colCount < getHeader().size(); colCount++)
        {
            out[rowCount][colCount] = getHeader().get(colCount);
        }

        for (rowCount = 1; rowCount < (getRowCount() + 1); rowCount++)
        {
            for (colCount = 0; colCount < getHeader().size(); colCount++)
            {
                out[rowCount][colCount] = getDataList().get(rowCount - 1).get(colCount);
            }
        }

        return out;
    }

    /*************************************************************************************/

}

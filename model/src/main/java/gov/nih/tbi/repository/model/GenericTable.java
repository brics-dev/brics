
package gov.nih.tbi.repository.model;

import java.util.ArrayList;
import java.util.List;

public class GenericTable
{

    private String name;
    private List<GenericTableRow> rows = new ArrayList<GenericTableRow>();
    
    public GenericTable(String name)
    {
        this.name = name;
    }
    
    public String getName()
    {

        return name;
    }

    public void setName(String name)
    {

        this.name = name;
    }

    public List<GenericTableRow> getRows()
    {

        return rows;
    }

    public void setRows(List<GenericTableRow> rows)
    {

        this.rows = rows;
    }

    public void addRow(GenericTableRow newRow)
    {

        rows.add(newRow);
    }
}

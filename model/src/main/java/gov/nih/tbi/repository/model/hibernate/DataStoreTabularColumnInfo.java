
package gov.nih.tbi.repository.model.hibernate;

import gov.nih.tbi.dictionary.model.hibernate.StructuralDataElement;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Model for mapping data elements in repeatable groups to the columns they are stored in upon submission.
 * 
 * @author dhollo
 * 
 */
@Entity
@Table(name = "DATASTORE_TABULAR_COLUMN_INFO")
public class DataStoreTabularColumnInfo implements Serializable
{

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DATASTORE_TABULAR_COLUMN_INFO_SEQ")
    @SequenceGenerator(name = "DATASTORE_TABULAR_COLUMN_INFO_SEQ", sequenceName = "DATASTORE_TABULAR_COLUMN_INFO_SEQ", allocationSize = 1)
    private long id;

    @OneToOne
    @JoinColumn(name = "DATASTORE_TABULAR_INFO_ID")
    private DataStoreTabularInfo tableInfo;

    // The db column may be named data element but we are storing map element, do not be alarmed.
    @Column(name = "DATA_ELEMENT_ID")
    private Long mapElementId;

    @Column(name = "COLUMN_NAME")
    private String columnName;

    @Column(name = "COLUMN_TYPE")
    private String columnType;

    // This can not be hibernate managed because it will be in another DB
    // Must make webservice call to populate this...
    @Transient
    private StructuralDataElement dataElement;

    public DataStoreTabularColumnInfo()
    {

    } // Default constructor

    public DataStoreTabularColumnInfo(DataStoreTabularInfo tableInfo, Long dataElementId, String columnName,
            String columnType)
    {

        this.tableInfo = tableInfo;
        this.mapElementId = dataElementId;
        this.columnName = columnName;
        this.columnType = columnType;
    }

    public long getId()
    {

        return id;
    }

    public void setId(long id)
    {

        this.id = id;
    }

    public DataStoreTabularInfo getTableInfo()
    {

        return tableInfo;
    }

    public void setTableInfo(DataStoreTabularInfo tableInfo)
    {

        this.tableInfo = tableInfo;
    }

    public Long getMapElementId()
    {

        return mapElementId;
    }

    public void setMapElementId(Long mapElementId)
    {

        this.mapElementId = mapElementId;
    }

    public String getColumnName()
    {

        return columnName;
    }

    public void setColumnName(String columnName)
    {

        this.columnName = columnName;
    }

    public String getColumnType()
    {

        return columnType;
    }

    public void setColumnType(String columnType)
    {

        this.columnType = columnType;
    }

    public StructuralDataElement getDataElement()
    {

        return dataElement;
    }

    public void setDataElement(StructuralDataElement dataElement)
    {

        this.dataElement = dataElement;
    }

}

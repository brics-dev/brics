
package gov.nih.tbi.repository.model.hibernate;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Model for mapping repeatable groups to the tables submission data is stored in.
 * 
 * @author dhollo
 * 
 */
@Entity
@Table(name = "DATASTORE_TABULAR_INFO")
public class DataStoreTabularInfo implements Serializable
{

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DATASTORE_TABULAR_INFO_SEQ")
    @SequenceGenerator(name = "DATASTORE_TABULAR_INFO_SEQ", sequenceName = "DATASTORE_TABULAR_INFO_SEQ", allocationSize = 1)
    private long id;

    @OneToOne
    @JoinColumn(name = "DATASTORE_INFO_ID")
    private DataStoreInfo dataStoreInfo;

    @Column(name = "REPEATABLE_GROUP_ID")
    private Long repeatableGroupId;

    @Column(name = "TABLE_NAME")
    private String tableName;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "tableInfo", targetEntity = DataStoreTabularColumnInfo.class, orphanRemoval = true)
    private Set<DataStoreTabularColumnInfo> columnInfos;

    public DataStoreTabularInfo(DataStoreInfo dataStoreInfo, Long repeatableGroupId, String tableName)
    {

        this.dataStoreInfo = dataStoreInfo;
        this.repeatableGroupId = repeatableGroupId;
        this.tableName = tableName;
    }

    public DataStoreTabularInfo()
    {

    } // Default Constructor

    public long getId()
    {

        return id;
    }

    public void setId(long id)
    {

        this.id = id;
    }

    public DataStoreInfo getDataStoreInfo()
    {

        return dataStoreInfo;
    }

    public void setDataStoreInfo(DataStoreInfo dataStoreInfo)
    {

        this.dataStoreInfo = dataStoreInfo;
    }

    public Long getRepeatableGroupId()
    {

        return repeatableGroupId;
    }

    public void setRepeatableGroup(Long repeatableGroupId)
    {

        this.repeatableGroupId = repeatableGroupId;
    }

    public String getTableName()
    {

        return tableName;
    }

    public void setTableName(String tableName)
    {

        this.tableName = tableName;
    }

    public Set<DataStoreTabularColumnInfo> getColumnInfos()
    {

        if (columnInfos == null)
        {
            return new HashSet<DataStoreTabularColumnInfo>();
        }
        return columnInfos;
    }

    public void setColumnInfos(Set<DataStoreTabularColumnInfo> columnInfos)
    {

        this.columnInfos = columnInfos;
    }
}
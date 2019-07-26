
package gov.nih.tbi.repository.model.hibernate;

import gov.nih.tbi.repository.model.AbstractDataStoreInfo;

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
 * Model for general information about a datastore.
 * 
 * @author dhollo
 * 
 */
@Entity
@Table(name = "DATASTORE_INFO")
public class DataStoreInfo extends AbstractDataStoreInfo implements Serializable
{

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DATASTORE_INFO_SEQ")
    @SequenceGenerator(name = "DATASTORE_INFO_SEQ", sequenceName = "DATASTORE_INFO_SEQ", allocationSize = 1)
    private long id;

    @Column(name = "DATA_STRUCTURE_ID")
    private long dataStructureId;

    // @OneToOne(cascade = { CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST })
    // @JoinColumn(name = "DATA_STRUCTURE_ID")
    // private DataStructure dataStructure;

    @Column(name = "TABULAR")
    private boolean tabular;

    @Column(name = "FEDERATED")
    private boolean federated;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "dataStoreInfo", targetEntity = DataStoreTabularInfo.class)
    private Set<DataStoreTabularInfo> dataStoreTabularInfos;

    @OneToOne
    @JoinColumn(name = "DATASTORE_BINARY_INFO_ID")
    private DataStoreBinaryInfo binaryDataStore;

    @Column(name = "ARCHIVED")
    private boolean archived;

    public DataStoreInfo()
    {

        archived = false;
    }

    public DataStoreInfo(Long dataStructureId, boolean tabular, boolean federated)
    {

        this.tabular = tabular;
        this.federated = federated;
        this.binaryDataStore = null;
        this.dataStructureId = dataStructureId;
        archived = false;
    }

    public DataStoreInfo(DataStoreBinaryInfo binaryDataStore, boolean tabular, boolean federated)
    {

        this.tabular = tabular;
        this.federated = federated;
        this.binaryDataStore = binaryDataStore;
        archived = false;
    }

    public Long getId()
    {

        return this.id;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    // public AbstractDataStructure getDataStructure()
    // {
    //
    // return dataStructure;
    // }
    //
    // public void setDataStructure(AbstractDataStructure dataStructure)
    // {
    //
    // if (dataStructure instanceof DataStructure)
    // {
    // this.dataStructure = (DataStructure) dataStructure;
    // }
    // else
    // {
    // throw new ClassCastException("Cannot cast BasicDataStructure to DataStructure.");
    // }
    // }

    public long getDataStructureId()
    {

        return dataStructureId;
    }

    public void setDataStructureId(long dataStructureId)
    {

        this.dataStructureId = dataStructureId;
    }

    public boolean isTabular()
    {

        return this.tabular;
    }

    public void setTabular(boolean tabular)
    {

        this.tabular = tabular;
    }

    public boolean isFederated()
    {

        return this.federated;
    }

    public void setFederated(boolean federated)
    {

        this.federated = federated;
    }

    public Set<DataStoreTabularInfo> getDataStoreTabularInfos()
    {

        if (dataStoreTabularInfos == null)
        {
            dataStoreTabularInfos = new HashSet<DataStoreTabularInfo>();
        }
        return dataStoreTabularInfos;
    }

    public void setDataStoreTabularInfos(Set<DataStoreTabularInfo> dataStoreTabularInfos)
    {

        this.dataStoreTabularInfos = dataStoreTabularInfos;
    }

    public DataStoreBinaryInfo getBinaryDataStore()
    {

        return binaryDataStore;
    }

    public void setBinaryDataStore(DataStoreBinaryInfo binaryDataStore)
    {

        this.binaryDataStore = binaryDataStore;
    }

    public boolean isArchived()
    {

        return archived;
    }

    public void setArchived(boolean archived)
    {

        this.archived = archived;
    }
}

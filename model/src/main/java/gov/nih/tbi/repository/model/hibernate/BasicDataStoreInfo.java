
package gov.nih.tbi.repository.model.hibernate;

import gov.nih.tbi.repository.model.AbstractDataStoreInfo;

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

/**
 * A Basic version of the datastore model for listing datastores
 * 
 * @author mvalei
 * 
 */
@Entity
@Table(name = "DATASTORE_INFO")
public class BasicDataStoreInfo extends AbstractDataStoreInfo implements Serializable
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
    // private BasicDataStructure dataStructure;

    @Column(name = "TABULAR")
    private boolean tabular;

    @Column(name = "FEDERATED")
    private boolean federated;

    @OneToOne
    @JoinColumn(name = "DATASTORE_BINARY_INFO_ID")
    private DataStoreBinaryInfo binaryDataStore;

    @Column(name = "ARCHIVED")
    private boolean archived;

    public BasicDataStoreInfo()
    {

        archived = false;
    }
    
    public BasicDataStoreInfo(DataStoreInfo dsi) {
    	this.id = dsi.getId();
    	this.dataStructureId = dsi.getDataStructureId();
    	this.tabular = dsi.isTabular();
    	this.federated = dsi.isFederated();
    	this.binaryDataStore = dsi.getBinaryDataStore();
    	this.archived = dsi.isArchived();
    }

    public BasicDataStoreInfo(Long dataStructureId, boolean tabular, boolean federated)
    {

        this.dataStructureId = dataStructureId;
        this.tabular = tabular;
        this.federated = federated;
        this.binaryDataStore = null;
        archived = false;
    }

    public BasicDataStoreInfo(DataStoreBinaryInfo binaryDataStore, boolean tabular, boolean federated)
    {

        // this.dataStructure = null;
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

    public long getDataStructureId()
    {

        return dataStructureId;
    }

    public void setDataStructureId(long dataStructureId)
    {

        this.dataStructureId = dataStructureId;
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
    // if (dataStructure instanceof BasicDataStructure)
    // {
    // this.dataStructure = (BasicDataStructure) dataStructure;
    // }
    // else
    // {
    // throw new ClassCastException("Cannot cast DataStructure to BasicDataStructure.");
    // }
    // }

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

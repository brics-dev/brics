
package gov.nih.tbi.repository.model.hibernate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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
 * Model of information about binary data stores stored in the database.
 * 
 * @author dhollo
 * 
 */
@Entity
@Table(name = "DATASTORE_BINARY_INFO")
public class DataStoreBinaryInfo implements Serializable
{

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DATASTORE_BINARY_INFO_SEQ")
    @SequenceGenerator(name = "DATASTORE_BINARY_INFO_SEQ", sequenceName = "DATASTORE_BINARY_INFO_SEQ", allocationSize = 1)
    private long id;

    @OneToOne
    @JoinColumn(name = "DATAFILE_ENDPOINT_INFO_ID")
    private DatafileEndpointInfo datafileEndpointInfo;

    @Column(name = "PATH")
    private String path;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "dataStoreBinaryInfo", targetEntity = UserFile.class, orphanRemoval = true)
    private List<UserFile> files;

    public DataStoreBinaryInfo()
    {

    } // Default Constructor

    public DataStoreBinaryInfo(DatafileEndpointInfo datafileEndpointInfo, String path)
    {

        this.datafileEndpointInfo = datafileEndpointInfo;
        this.path = path;
    }

    public long getId()
    {

        return id;
    }

    public void setId(long id)
    {

        this.id = id;
    }

    public DatafileEndpointInfo getDatafileEndpointInfo()
    {

        return datafileEndpointInfo;
    }

    public void setDatafileEndpointInfo(DatafileEndpointInfo datafileEndpointInfo)
    {

        this.datafileEndpointInfo = datafileEndpointInfo;
    }

    public String getPath()
    {

        return path;
    }

    public void setPath(String path)
    {

        this.path = path;
    }

    public List<UserFile> getFiles()
    {

        if (files == null)
        {
            files = new ArrayList<UserFile>();
        }
        return files;
    }

    public void setFiles(List<UserFile> files)
    {

        this.files = files;
    }
}

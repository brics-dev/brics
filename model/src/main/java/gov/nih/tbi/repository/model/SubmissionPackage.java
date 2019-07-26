
package gov.nih.tbi.repository.model;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class SubmissionPackage implements Serializable
{

    private static final long serialVersionUID = 1L;

    @XmlAttribute
    private String name;

    @XmlAttribute
    private long bytes;

    @XmlAttribute
    private long dataFileBytes;

    @XmlAttribute
    private String crcHash;

    @XmlAttribute
    @XmlList
    private Set<SubmissionType> types;

    @XmlElementWrapper(name = "datasets")
    @XmlElement(name = "dataset")
    private List<SubmissionDataFile> datasets;

    @XmlElementWrapper(name = "associatedFiles")
    @XmlElement(name = "associatedFile")
    private List<SubmissionDataFile> associatedFiles;

    public String getName()
    {

        return name;
    }

    public void setName(String name)
    {

        this.name = name;
    }

    public long getBytes()
    {

        return bytes;
    }

    public void setBytes(long bytes)
    {

        this.bytes = bytes;
    }

    public long getDataFileBytes()
    {

        return dataFileBytes;
    }

    public void setDataFileBytes(long dataFileBytes)
    {

        this.dataFileBytes = dataFileBytes;
    }

    public String getCrcHash()
    {

        return crcHash;
    }

    public void setCrcHash(String crcHash)
    {

        this.crcHash = crcHash;
    }

    public List<SubmissionDataFile> getDatasets()
    {

        return datasets;
    }

    public void setDatasets(List<SubmissionDataFile> datasets)
    {

        this.datasets = datasets;
    }

    public Set<SubmissionType> getTypes()
    {

        return types;
    }

    public void setTypes(Set<SubmissionType> types)
    {

        this.types = types;
    }

    public List<SubmissionDataFile> getAssociatedFiles()
    {

        return associatedFiles;
    }

    public void setAssociatedFiles(List<SubmissionDataFile> associatedFiles)
    {

        this.associatedFiles = associatedFiles;
    }
}

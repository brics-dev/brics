
package gov.nih.tbi.repository.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class SubmissionDataFile implements Serializable
{

    private static final long serialVersionUID = 1L;

    @XmlAttribute
    private String name;

    @XmlAttribute
    private String path;

    @XmlAttribute
    private long bytes;

    @XmlAttribute
    private String crcHash;

    @XmlAttribute
    private SubmissionType type;

    public String getName()
    {

        return name;
    }

    public void setName(String name)
    {

        this.name = name;
    }

    public String getPath()
    {

        return path;
    }

    public void setPath(String path)
    {

        this.path = path;
    }

    public long getBytes()
    {

        return bytes;
    }

    public void setBytes(long bytes)
    {

        this.bytes = bytes;
    }

    public String getCrcHash()
    {

        return crcHash;
    }

    public void setCrcHash(String crcHash)
    {

        this.crcHash = crcHash;
    }

    public SubmissionType getType()
    {

        return type;
    }

    public void setType(SubmissionType type)
    {

        this.type = type;
    }
}

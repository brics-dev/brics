
package gov.nih.tbi.repository.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "instancedRowMap")
@XmlAccessorType(XmlAccessType.FIELD)
public class InstancedRecordMap implements Serializable
{

    private static final long serialVersionUID = 2319698394382196715L;

    @XmlElement(name = "instancedRecords", type = InstancedRecord.class)
    private List<InstancedRecord> instancedRecords;

    @XmlAttribute
    private String userName;

    @XmlAttribute
    private String selectedFormName;

    @XmlAttribute
    private String joinFormName;

    @XmlElement(name = "headers")
    private ArrayList<String> headers;

    @XmlAttribute
    private int hardCodedColumns;

    @XmlAttribute
    private int selectedFormColumnLength;

    @XmlAttribute
    private int joinFormColumnLength;

    public int getSelectedFormColumnLength()
    {

        return selectedFormColumnLength;
    }

    public void setSelectedFormColumnLength(int selectedFormColumnLength)
    {

        this.selectedFormColumnLength = selectedFormColumnLength;
    }

    public int getJoinFormColumnLength()
    {

        return joinFormColumnLength;
    }

    public void setJoinFormColumnLength(int joinFormColumnLength)
    {

        this.joinFormColumnLength = joinFormColumnLength;
    }

    public InstancedRecordMap()
    {

    }

    public String getUserName()
    {

        return userName;
    }

    public void setUserName(String userName)
    {

        this.userName = userName;
    }

    public String getSelectedFormName()
    {

        return selectedFormName;
    }

    public void setSelectedFormName(String selectedFormName)
    {

        this.selectedFormName = selectedFormName;
    }

    public String getJoinFormName()
    {

        return joinFormName;
    }

    public void setJoinFormName(String joinFormName)
    {

        this.joinFormName = joinFormName;
    }

    public List<InstancedRecord> getInstancedRecords()
    {

        return instancedRecords;
    }

    public void setInstancedRecords(List<InstancedRecord> instancedRecords)
    {

        this.instancedRecords = instancedRecords;
    }

    public ArrayList<String> getHeaders()
    {

        return headers;
    }

    public void setHeaders(ArrayList<String> headers)
    {

        this.headers = headers;
    }

    public int getHardCodedColumns()
    {

        return hardCodedColumns;
    }

    public void setHardCodedColumns(int hardCodedColumns)
    {

        this.hardCodedColumns = hardCodedColumns;
    }
}

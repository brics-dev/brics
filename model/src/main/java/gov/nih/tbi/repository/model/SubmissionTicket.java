
package gov.nih.tbi.repository.model;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class SubmissionTicket implements Serializable
{

    private static final long serialVersionUID = 1L;

    @XmlAttribute
    private String version;

    @XmlAttribute
    private String environment;

    @XmlElement
    public String localPath;

    @XmlElement
    public String serverPath;

    @XmlElement
    public String studyName;

    @XmlElement
    public String datasetName;
    
    @XmlElementWrapper(name = "submissionPackages")
    @XmlElement(name = "submissionPackage", required = true)
    private List<SubmissionPackage> submissionPackages;

    public String getVersion()
    {

        return version;
    }

    public void setVersion(String version)
    {

        this.version = version;
    }

    public String getEnvironment()
    {

        return environment;
    }

    public void setEnvironment(String environment)
    {

        this.environment = environment;
    }

    public String getLocalPath()
    {

        return localPath;
    }

    public void setLocalPath(String localPath)
    {

        this.localPath = localPath;
    }

    public String getServerPath()
    {

        return serverPath;
    }

    public void setServerPath(String serverPath)
    {

        this.serverPath = serverPath;
    }

    public String getStudyName()
    {

        return studyName;
    }

    public void setStudyName(String studyName)
    {

        this.studyName = studyName;
    }

    public String getDatasetName()
    {

        return datasetName;
    }

    public void setDatasetName(String datasetName)
    {

        this.datasetName = datasetName;
    }

    public List<SubmissionPackage> getSubmissionPackages()
    {

        return submissionPackages;
    }

    public void setSubmissionPackages(List<SubmissionPackage> submissionPackages)
    {

        this.submissionPackages = submissionPackages;
    }
}

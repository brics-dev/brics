
package gov.nih.tbi.repository.model;

import gov.nih.tbi.commons.model.Data;
import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.commons.model.hibernate.User;
import gov.nih.tbi.repository.model.hibernate.Study;

import java.util.Date;

import javax.persistence.MappedSuperclass;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * An abstract class with the minimum fields required for a Dataset
 * 
 * @author mvalei
 * 
 */
@MappedSuperclass
@XmlType(namespace = "http://tbi.nih.gov/RepositorySchema")
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractDataset implements Data
{

    /**********************************************************************/

    public abstract Long getId();

    public abstract void setId(Long id);

    public abstract String getName();

    public abstract void setName(String name);

    public abstract User getSubmitter();

    public abstract void setSubmitter(User submitter);

    public abstract Date getSubmitDate();

    public abstract void setSubmitDate(Date submitDate);

    public abstract DatasetStatus getDatasetStatus();

    public abstract void setDatasetStatus(DatasetStatus datasetStatus);

    public abstract DatasetStatus getDatasetRequestStatus();

    public abstract void setDatasetRequestStatus(DatasetStatus datasetRequestStatus);

    public abstract String getPrefixedId();

    public abstract void setPrefixedId(String prefixedId);

    public abstract Study getStudy();

    public abstract void setStudy(Study study);

    public abstract User getReviewer();

    public abstract void setReviewer(User reviewer);

    public abstract User getVerifier();

    public abstract void setVerifier(User verifier);

    public abstract Date getPublicationDate();

    public abstract void setPublicationDate(Date publicationDate);
    
    public String getDataId()
    {

        return getPrefixedId();
    }

    public String getDataName()
    {

        return getName();
    }

    public abstract Date getSubmissionDate();

    public String getContainerName()
    {

        return getStudy().getTitle();
    }
}

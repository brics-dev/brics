
package gov.nih.tbi.repository.model.hibernate;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "SUBMISSION_RECORD_JOIN")
public class SubmissionRecordJoin implements Serializable
{

    private static final long serialVersionUID = 6363132257498748031L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "SUBMISSION_RECORD_JOIN_SEQ")
    @SequenceGenerator(name = "SUBMISSION_RECORD_JOIN_SEQ", sequenceName = "SUBMISSION_RECORD_JOIN_SEQ", allocationSize = 1)
    private Long id;

    @Column(name = "DATASET_ID")
    private Long datasetId;

    public Long getId()
    {

        return id;
    }

    public Long getDatasetId()
    {

        return datasetId;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    public void setDatasetId(Long datasetId)
    {

        this.datasetId = datasetId;
    }
}

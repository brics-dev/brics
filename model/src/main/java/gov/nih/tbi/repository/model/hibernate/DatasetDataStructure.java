
package gov.nih.tbi.repository.model.hibernate;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "DATASET_DATA_STRUCTURE")
public class DatasetDataStructure implements Serializable
{

    private static final long serialVersionUID = -8549338709769396030L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "DATASET_DATA_STRUCTURE_SEQ")
    @SequenceGenerator(name = "DATASET_DATA_STRUCTURE_SEQ", sequenceName = "DATASET_DATA_STRUCTURE_SEQ", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "DATASET_ID")
    private Dataset dataset;

    @Column(name = "DATA_STRUCTURE_ID")
    private long dataStructureId;

    public Long getId()
    {

        return id;
    }

    public void setId(Long id)
    {

        this.id = id;
    }

    public Dataset getDataset()
    {

        return dataset;
    }

    public void setDataset(Dataset dataset)
    {

        this.dataset = dataset;
    }

    public long getDataStructureId()
    {

        return dataStructureId;
    }

    public void setDataStructureId(long dataStructureId)
    {

        this.dataStructureId = dataStructureId;
    }
}

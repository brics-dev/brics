
package gov.nih.tbi.repository.model;

import gov.nih.tbi.repository.model.hibernate.Dataset;

import java.io.Serializable;

public class SessionDataset implements Serializable
{

    private static final long serialVersionUID = -1328255503886725301L;

    private Dataset dataset;

    public Dataset getDataset()
    {

        return dataset;
    }

    public void setDataset(Dataset dataset)
    {

        this.dataset = dataset;
    }
}

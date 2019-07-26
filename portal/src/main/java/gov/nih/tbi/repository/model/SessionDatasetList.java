package gov.nih.tbi.repository.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.nih.tbi.repository.model.hibernate.Dataset;

public class SessionDatasetList implements Serializable {

	private static final long serialVersionUID = 7329220862862266027L;
	
	private List <Dataset> datasets;
	
	
	public void clear(){
		
		datasets=null;
	}
	public List<Dataset> getDatasets()
    {

        if (datasets == null)
        {
        	datasets = new ArrayList<Dataset>();
        }

        return datasets;
    }
	
	public void setDatasets(List<Dataset> datasets)
    {

        this.datasets = datasets;
    }
	
}

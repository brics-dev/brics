
package gov.nih.tbi.util;

import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.pojo.BeanField;
import gov.nih.tbi.pojo.DataElement;

import java.util.List;


public class ResultSetToDataElement extends ResultSetToBean<DataElement>
{

	private static final long serialVersionUID = -8001932852703314505L;

	public ResultSetToDataElement()
    {

        setType(DataElement.class);
    }
    
    // first field must be able to be used as a primary key
    @Override
    public List<BeanField> getFields()
    {

        return QueryToolConstants.ELEMENT_FIELDS;
    }
}

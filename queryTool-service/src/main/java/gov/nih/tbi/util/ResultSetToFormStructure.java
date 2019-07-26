
package gov.nih.tbi.util;

import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.pojo.BeanField;
import gov.nih.tbi.pojo.FormResult;

import java.util.List;

public class ResultSetToFormStructure extends ResultSetToBean<FormResult>
{
	private static final long serialVersionUID = 4425485218745522739L;

	public ResultSetToFormStructure()
    {
        setType(FormResult.class);
    }
    
    // first field must be able to be used as a primary key
    @Override
    public List<BeanField> getFields()
    {
        return QueryToolConstants.FORM_FIELDS;
    }
}

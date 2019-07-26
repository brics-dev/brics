
package gov.nih.tbi.util;

import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.pojo.BeanField;
import gov.nih.tbi.pojo.StudyResult;

import java.util.List;

public class ResultSetToStudy extends ResultSetToBean<StudyResult> {
	
	private static final long serialVersionUID = 4070606674107868654L;

	public ResultSetToStudy() {
        setType(StudyResult.class);
    }

    // first field must be able to be used as a primary key
    @Override
    public List<BeanField> getFields()
    {

        return QueryToolConstants.STUDY_FIELDS;
    }
}

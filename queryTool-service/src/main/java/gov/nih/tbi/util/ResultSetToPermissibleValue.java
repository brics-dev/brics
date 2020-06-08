package gov.nih.tbi.util;

import java.util.List;

import gov.nih.tbi.constants.QueryToolConstants;
import gov.nih.tbi.pojo.BeanField;
import gov.nih.tbi.pojo.PermissibleValue;

public class ResultSetToPermissibleValue extends ResultSetToBean<PermissibleValue> {
	private static final long serialVersionUID = 476385592768114614L;

	public ResultSetToPermissibleValue() {
		setType(PermissibleValue.class);
	}

	@Override
	public List<BeanField> getFields() {
		return QueryToolConstants.PV_FIELDS;
	}
}

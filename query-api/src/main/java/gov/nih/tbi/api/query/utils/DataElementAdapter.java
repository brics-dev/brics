package gov.nih.tbi.api.query.utils;

import java.util.List;
import java.util.stream.Collectors;

import gov.nih.tbi.api.query.model.DataElement;
import gov.nih.tbi.api.query.model.DataElement.DataTypeEnum;
import gov.nih.tbi.api.query.model.DataElement.InputRestrictionEnum;
import gov.nih.tbi.api.query.model.DataElement.RequiredTypeEnum;

public class DataElementAdapter implements ObjectAdapter<DataElement> {

	private gov.nih.tbi.pojo.DataElement de;

	public DataElementAdapter(gov.nih.tbi.pojo.DataElement de) {
		this.de = de;
	}

	@Override
	public DataElement adapt() {
		DataElement apiDe = new DataElement();
		apiDe.setId(de.getId());
		if (de.getType() != null) {
			apiDe.setDataType(DataTypeEnum.fromValue(de.getType().getValue()));
		}
		apiDe.setDescription(de.getDescription());
		if (de.getInputRestrictions() != null) {
			apiDe.setInputRestriction(InputRestrictionEnum.fromValue(de.getInputRestrictions().getValue()));
		}

		if (de.getRequiredType() != null) {
			apiDe.setRequiredType(RequiredTypeEnum.fromValue(de.getRequiredType().getValue()));
		}

		apiDe.setMaximumValue(de.getMaximumValue());
		apiDe.setMinimumValue(de.getMinimumValue());
		apiDe.setName(de.getName());

		if (de.getPermissibleValues() != null && !de.getPermissibleValues().isEmpty()) {
			List<String> permissibleValues =
					de.getPermissibleValues().stream().map(pv -> pv.getValueLiteral()).collect(Collectors.toList());
			apiDe.setPermissibleValue(permissibleValues);
		}

		apiDe.setTitle(de.getTitle());
		apiDe.setPosition(de.getPosition());
		return apiDe;
	}
}

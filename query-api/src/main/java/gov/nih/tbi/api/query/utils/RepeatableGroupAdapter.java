package gov.nih.tbi.api.query.utils;

import java.util.stream.Collectors;

import gov.nih.tbi.api.query.model.RepeatableGroup.TypeEnum;

public class RepeatableGroupAdapter implements ObjectAdapter<gov.nih.tbi.api.query.model.RepeatableGroup> {
	
	private gov.nih.tbi.pojo.RepeatableGroup rg;
	
	public RepeatableGroupAdapter(gov.nih.tbi.pojo.RepeatableGroup rg) {
		this.rg = rg;
	}
	
	@Override
	public gov.nih.tbi.api.query.model.RepeatableGroup adapt() {
		gov.nih.tbi.api.query.model.RepeatableGroup apiRg = new gov.nih.tbi.api.query.model.RepeatableGroup();
		apiRg.setName(rg.getName());
		apiRg.setPosition(rg.getPosition());
		apiRg.setThreshold(rg.getThreshold());
		apiRg.setType(TypeEnum.fromValue(rg.getType()));
		apiRg.setDataElements(rg.getDataElements().stream().map(de -> new DataElementAdapter(de).adapt()).collect(Collectors.toList()));
		return apiRg;
	}

}

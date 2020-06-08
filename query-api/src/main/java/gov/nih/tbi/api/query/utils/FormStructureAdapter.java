package gov.nih.tbi.api.query.utils;

import gov.nih.tbi.api.query.model.FormStructure;
import gov.nih.tbi.pojo.FormResult;

public class FormStructureAdapter implements ObjectAdapter<FormStructure> {

	private FormResult formResult;

	public FormStructureAdapter(FormResult formResult) {
		this.formResult = formResult;
	}

	public FormStructure adapt() {
		FormStructure fs = new FormStructure();
		fs.setId(formResult.getId());
		fs.setShortName(formResult.getShortName());
		fs.setTitle(formResult.getTitle());
		fs.setVersion(formResult.getVersion());
		
		return fs;
	}

}

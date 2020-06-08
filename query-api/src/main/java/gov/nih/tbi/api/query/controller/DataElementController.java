package gov.nih.tbi.api.query.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import gov.nih.tbi.api.query.exception.ApiEntityNotFoundException;
import gov.nih.tbi.api.query.model.RepeatableGroup;
import gov.nih.tbi.api.query.utils.RepeatableGroupAdapter;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.service.InstancedDataManager;
import gov.nih.tbi.service.ResultManager;
import gov.nih.tbi.service.model.PermissionModel;
import io.swagger.annotations.Api;

@Api(tags = {"Data Element"}, value = "dataElement", description = "the dataElement API")
@RestController
public class DataElementController extends BaseController implements DataElementApi {

	@Autowired
	private ResultManager resultManager;

	@Autowired
	private InstancedDataManager instancedDataManager;

	@Override
	public ResponseEntity<List<RepeatableGroup>> getDataElementsByFormName(String formName) {

		PermissionModel permissionModel = getPermissionModel();
		if (permissionModel != null && !permissionModel.getFormResultPermissions().containsKey(formName)) {
			throw new ApiEntityNotFoundException("Form structure not found: " + formName);
		}

		FormResult form = resultManager.getFormByShortName(formName);

		if (form == null) {
			throw new ApiEntityNotFoundException("Form structure not found: " + formName);
		}

		instancedDataManager.seedFormDataElements(form);

		List<RepeatableGroup> apiRgs = form.getRepeatableGroups().stream()
				.map(rg -> new RepeatableGroupAdapter(rg).adapt()).collect(Collectors.toList());

		return ResponseEntity.ok(apiRgs);
	}
}

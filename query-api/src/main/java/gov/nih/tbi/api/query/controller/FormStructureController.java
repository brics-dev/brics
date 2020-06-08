package gov.nih.tbi.api.query.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Multimap;

import gov.nih.tbi.api.query.exception.ApiEntityNotFoundException;
import gov.nih.tbi.api.query.model.StudyForm;
import gov.nih.tbi.api.query.utils.FormStructureAdapter;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.StudyResult;
import gov.nih.tbi.semantic.model.QueryPermissions.StudyResultPermission;
import gov.nih.tbi.service.ResultManager;
import gov.nih.tbi.service.model.PermissionModel;
import io.swagger.annotations.Api;

@Api(tags = {"Form Structure"}, value = "form", description = "the form API")
@RestController
public class FormStructureController extends BaseController implements FormApi {

	@Autowired
	private ResultManager resultManager;

	@Override
	public ResponseEntity<List<StudyForm>> getFormByStudyPrefixedId(List<String> prefixedIds) {

		List<StudyResult> studyResults = resultManager.getStudyByPrefixedIds(prefixedIds);

		Map<String, String> studyPrefixIdToUriMap = new HashMap<String, String>();
		for (StudyResult studyResult : studyResults) {
			studyPrefixIdToUriMap.put(studyResult.getPrefixedId(), studyResult.getUri());
		}

		PermissionModel permissionModel = getPermissionModel();

		// Remove prefixedId from the list if user doesn't have access to this study.
		if (permissionModel != null) {
			Map<String, StudyResultPermission> studyPermissionMap = permissionModel.getStudyResultPermissions();

			for (Iterator<String> it = prefixedIds.iterator(); it.hasNext();) {
				String prefixedId = it.next();
				String studyUri = studyPrefixIdToUriMap.get(prefixedId);
				if (!studyPermissionMap.containsKey(studyUri)) {
					it.remove();
				}
			}
		}

		Multimap<String, FormResult> studyToFormMultimap =
				resultManager.searchFormsByStudyPrefixedIds(prefixedIds);

		if (studyToFormMultimap == null || studyToFormMultimap.isEmpty()) {
			throw new ApiEntityNotFoundException("No form structure was found to be associated with the given prefixed ID(s)");
		}

		List<StudyForm> apiOutput = new ArrayList<>();

		for (Entry<String, Collection<FormResult>> mapEntry : studyToFormMultimap.asMap().entrySet()) {
			String prefixedId = mapEntry.getKey();
			Collection<FormResult> currentStudyResults = mapEntry.getValue();
			StudyForm studyForm = new StudyForm();
			studyForm.setStudyId(prefixedId);
			studyForm.setForms(currentStudyResults.stream().map((s) -> new FormStructureAdapter(s).adapt())
					.collect(Collectors.toList()));
			apiOutput.add(studyForm);
		}

		return ResponseEntity.ok(apiOutput);
	}

}

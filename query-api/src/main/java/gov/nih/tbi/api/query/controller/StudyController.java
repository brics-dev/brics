package gov.nih.tbi.api.query.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

import gov.nih.tbi.api.query.exception.ApiEntityNotFoundException;
import gov.nih.tbi.api.query.model.FormStudy;
import gov.nih.tbi.api.query.model.Study;
import gov.nih.tbi.api.query.utils.StudyAdapter;
import gov.nih.tbi.exceptions.ResultSetTranslationException;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.StudyResult;
import gov.nih.tbi.service.QueryAccountManager;
import gov.nih.tbi.service.ResultManager;
import gov.nih.tbi.service.model.PermissionModel;
import gov.nih.tbi.util.SearchResultUtil;
import io.swagger.annotations.Api;

@Api(tags = {"Study"}, value = "study", description = "the study API")
@RestController
public class StudyController extends BaseController implements StudyApi {
	final Logger logger = LoggerFactory.getLogger(StudyController.class);

	@Autowired
	private ResultManager resultManager;

	@Autowired
	QueryAccountManager queryAccountManager;


	protected List<Study> getAllStudies() {
		List<StudyResult> studyResults = null;
		try {
			studyResults = resultManager.runStudyQueryForCaching();
		} catch (ResultSetTranslationException e) {
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
					"Error occurred when attempting to get studies.", e);
		}

		List<Study> studies =
				studyResults.stream().map((s) -> new StudyAdapter(s).adapt()).collect(Collectors.toList());
		return studies;
	}

	@Override
	public ResponseEntity<List<Study>> getStudies(@Valid List<String> prefixedId) {
		if (prefixedId == null || prefixedId.isEmpty()) {
			return ResponseEntity.ok(getAllStudies());
		} else {
			logger.debug("Request - getStudyByPrefixedID, Prefixed ID: " + prefixedId);
			List<StudyResult> studyResults = resultManager.getStudyByPrefixedIds(prefixedId);

			if (studyResults.isEmpty()) {
				throw new ApiEntityNotFoundException("No study was found");
			}

			List<Study> apiStudies =
					studyResults.stream().map((s) -> new StudyAdapter(s).adapt()).collect(Collectors.toList());
			return ResponseEntity.ok(apiStudies);
		}
	}

	@Override
	public ResponseEntity<List<FormStudy>> getStudyByFormName(List<String> formNames) {
		List<StudyResult> studyResults = resultManager.searchStudyByFormNames(formNames);
		SearchResultUtil.insertFormDetails(resultManager, studyResults);

		if (studyResults == null || studyResults.isEmpty()) {
			throw new ApiEntityNotFoundException("No study was found to be associated with the form structure(s)");
		}

		// Remove private studies that non-admin user do not have access to
		PermissionModel permissionModel = getPermissionModel();
		if (permissionModel != null) {
			queryAccountManager.hidePrivateStudyToNonAdmin(studyResults, permissionModel);
		}

		// We need to convert the raw StudyResults from query tool into the API
		// object(s) to be returned.

		// Since the output is a JSON of form names to its associated studies, creating
		// a mapping association here will make it easier to build the API objects
		// (FormStudy).
		ListMultimap<String, StudyResult> formToStudyMultimap = ArrayListMultimap.create();

		for (StudyResult studyResult : studyResults) {
			for (FormResult formResult : studyResult.getForms()) {
				String currentFormName = formResult.getShortName();
				if (formNames.contains(currentFormName)) {
					formToStudyMultimap.put(currentFormName, studyResult);
				}
			}
		}

		List<FormStudy> apiOutput = new ArrayList<>();

		for (Entry<String, Collection<StudyResult>> mapEntry : formToStudyMultimap.asMap().entrySet()) {
			String formName = mapEntry.getKey();
			Collection<StudyResult> currentStudyResults = mapEntry.getValue();
			FormStudy formStudy = new FormStudy();
			formStudy.setForm(formName);
			formStudy.setStudies(
					currentStudyResults.stream().map((s) -> new StudyAdapter(s).adapt()).collect(Collectors.toList()));
			apiOutput.add(formStudy);
		}

		return ResponseEntity.ok(apiOutput);
	}
}

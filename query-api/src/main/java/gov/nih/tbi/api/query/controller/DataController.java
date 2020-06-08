package gov.nih.tbi.api.query.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.InternalServerErrorException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import gov.nih.tbi.api.query.model.BasicFormStudy;
import gov.nih.tbi.api.query.model.FormDataParam;
import gov.nih.tbi.api.query.model.SimpleDataParam;
import gov.nih.tbi.api.query.model.StudyDataParam;
import gov.nih.tbi.api.query.service.DataService;
import gov.nih.tbi.exceptions.CSVGenerationException;
import gov.nih.tbi.exceptions.FilterEvaluationException;
import gov.nih.tbi.exceptions.FilterEvaluatorException;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.service.model.PermissionModel;
import io.swagger.annotations.Api;

@Api(tags = {"Data"}, value = "data", description = "the data API")
@RestController
public class DataController extends BaseController implements DataApi {
	private static final Logger logger = Logger.getLogger(DataController.class);

	@Autowired
	DataService dataService;

	private static final String DISPOSITION_FORMAT = "attachment;filename=\"%s\"";

	@Override
	public ResponseEntity<Resource> bulkSingleForm(@Valid SimpleDataParam body) {

		PermissionModel pm = getPermissionModel();
		boolean isJsonFormat = (body.getOutputFormat() == SimpleDataParam.OutputFormatEnum.JSON);

		logger.info("Endpoint: /bulk/form/study");
		List<FormResult> formResults = new ArrayList<>();

		for (BasicFormStudy basicFormStudy : body.getFormStudies()) {
			List<BasicFormStudy> dataList = new ArrayList<BasicFormStudy>();
			dataList.add(basicFormStudy);

			// There is actually only one form in the return list
			List<FormResult> oneForm = dataService.basicFormStudyToFormResults(dataList);

			if (oneForm != null && !oneForm.isEmpty()) {
				formResults.add(oneForm.get(0));
			}
		}

		if (formResults == null || formResults.isEmpty()) {
			logger.info("No matching forms found for the given input parameters.");
			return ResponseEntity.notFound().build();
		}


		File zipFile = dataService.generateBulkZip(formResults, isJsonFormat, body.isFlattened(), pm);

		if (zipFile != null) {
			Resource resource = null;

			try {
				resource = new InputStreamResource(new FileInputStream(zipFile));
			} catch (FileNotFoundException e) {
				throw new InternalServerErrorException(e);
			}

			return ResponseEntity.ok().headers(generateFileDownloadHeader(zipFile.getName()))
					.contentType(MediaType.parseMediaType("application/zip")).body(resource);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@Override
	public ResponseEntity<Resource> bulkStudySingleForm(@Valid StudyDataParam body) {

		PermissionModel pm = getPermissionModel();
		boolean isJsonFormat = (body.getOutputFormat() == StudyDataParam.OutputFormatEnum.JSON);

		logger.info("Endpoint: /bulk/study/form");
		List<FormResult> formResults = dataService.basicStudyFormToFormResults(body.getStudyForms());

		if (formResults == null || formResults.isEmpty()) {
			logger.info("No matching forms found for the given input parameters.");
			return ResponseEntity.notFound().build();
		}


		File zipFile = dataService.generateBulkZip(formResults, isJsonFormat, body.isFlattened(), pm);

		if (zipFile != null) {
			Resource resource = null;

			try {
				resource = new InputStreamResource(new FileInputStream(zipFile));
			} catch (FileNotFoundException e) {
				throw new InternalServerErrorException(e);
			}

			return ResponseEntity.ok().headers(generateFileDownloadHeader(zipFile.getName()))
					.contentType(MediaType.parseMediaType("application/zip")).body(resource);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@Override
	public ResponseEntity<Resource> getInstancedDataCsv(@Valid FormDataParam body) {
		if (logger.isDebugEnabled()) {
			logger.debug("Getting the permission model");
		}

		PermissionModel pm = super.getPermissionModel();

		File dataFile = null;

		try {
			List<FormResult> formResults = dataService.basicFormStudyToFormResults(body.getFormStudy());
			dataFile = dataService.getCsvData(formResults, body.getFilter(), body.isFlattened(), pm);
		} catch (CSVGenerationException e) {
			throw new InternalServerErrorException(e);
		} catch (FilterEvaluatorException e) {
			throw new BadRequestException(e);
		} catch (FilterEvaluationException e) {
			throw new BadRequestException(e);
		}

		if (dataFile != null) {
			Resource resource = null;

			try {
				resource = new InputStreamResource(new FileInputStream(dataFile));
			} catch (FileNotFoundException e) {
				throw new InternalServerErrorException(e);
			}

			return ResponseEntity.ok().headers(generateFileDownloadHeader(dataFile.getName()))
					.contentLength(dataFile.length()).contentType(MediaType.parseMediaType("application/octet-stream"))
					.body(resource);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	@Override
	public ResponseEntity<Resource> getInstancedDataJson(@Valid FormDataParam body) {
		PermissionModel pm = super.getPermissionModel();

		File dataFile = null;

		try {
			List<FormResult> formResults = dataService.basicFormStudyToFormResults(body.getFormStudy());
			dataFile = dataService.getJsonData(formResults, body.getFilter(), pm);
		} catch (FilterEvaluatorException e) {
			throw new BadRequestException(e);
		} catch (FilterEvaluationException e) {
			throw new BadRequestException(e);
		}

		if (dataFile != null) {
			Resource resource = null;

			try {
				resource = new InputStreamResource(new FileInputStream(dataFile));
			} catch (FileNotFoundException e) {
				throw new InternalServerErrorException(e);
			}

			return ResponseEntity.ok().headers(generateFileDownloadHeader(dataFile.getName()))
					.contentLength(dataFile.length()).contentType(MediaType.parseMediaType("application/octet-stream"))
					.body(resource);
		} else {
			return ResponseEntity.notFound().build();
		}
	}

	/**
	 * Generates the headers to define the response as a file download
	 * 
	 * @param fileName
	 * @return
	 */
	private HttpHeaders generateFileDownloadHeader(String fileName) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
		headers.add("Pragma", "no-cache");
		headers.add("Expires", "0");

		String contentDisposition = String.format(DISPOSITION_FORMAT, fileName);
		headers.add("Content-Disposition", contentDisposition);

		return headers;
	}
}

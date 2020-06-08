package gov.nih.tbi.api.query.service;

import java.io.File;
import java.util.List;

import gov.nih.tbi.api.query.model.BasicFormStudy;
import gov.nih.tbi.api.query.model.BasicStudyForm;
import gov.nih.tbi.api.query.model.Filter;
import gov.nih.tbi.exceptions.CSVGenerationException;
import gov.nih.tbi.exceptions.FilterEvaluatorException;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.service.model.PermissionModel;

public interface DataService {
	/**
	 * Returns the CSV file that represents running the query with the given form results and filters. This method will
	 * join the forms in formResults.
	 * 
	 * @param formResults
	 * @param filters
	 * @param isFlattened
	 * @param pm
	 * @return
	 * @throws FilterEvaluatorException
	 * @throws CSVGenerationException
	 */
	public File getCsvData(List<FormResult> formResults, List<Filter> filters, boolean isFlattened, PermissionModel pm)
			throws FilterEvaluatorException, CSVGenerationException;

	/**
	 * Returns the JSON file that represents running the query with the given form results and filters. This method will
	 * join the forms in formResults.
	 * 
	 * @param formResults
	 * @param filters
	 * @param isFlattened
	 * @param pm
	 * @return
	 * @throws FilterEvaluatorException
	 * @throws CSVGenerationException
	 */
	public File getJsonData(List<FormResult> formResults, List<Filter> filters, PermissionModel pm)
			throws FilterEvaluatorException;

	/**
	 * Parses the API parameter for BasicFormStudy into query tool FormResults.
	 * 
	 * @param formStudies
	 * @return
	 */
	public List<FormResult> basicFormStudyToFormResults(List<BasicFormStudy> formStudies);

	/**
	 * Parses the API parameter for BasicStudyForm into query tool FormResults.
	 * 
	 * @param formStudies
	 * @return
	 */
	public List<FormResult> basicStudyFormToFormResults(List<BasicStudyForm> studyForms);

	/**
	 * Returns the ZIP archive that includes the requested data files.
	 * 
	 * @param formResults
	 * @param isJsonFormat
	 * @param isFlattened
	 * @param pm
	 * @return
	 */
	public File generateBulkZip(List<FormResult> formResults, boolean isJsonFormat, boolean isFlattened,
			PermissionModel pm);
}

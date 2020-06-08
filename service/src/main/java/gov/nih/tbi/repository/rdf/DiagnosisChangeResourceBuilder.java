package gov.nih.tbi.repository.rdf;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.repository.model.GenericTable;
import gov.nih.tbi.repository.model.GenericTableRow;
import gov.nih.tbi.semantic.model.GuidRDF;
import gov.nih.tbi.semantic.model.SubjectVisit;

public class DiagnosisChangeResourceBuilder implements RDFGenResourceBuilder {
	static Logger log = Logger.getLogger(DiagnosisChangeResourceBuilder.class);
	private Map<String, GenericTable> repeatableTableResultMap;

	private static final String REQUIRED_RG = "Required";
	private static final String DIAGNOSIS_RG = "Diagnosis";

	private static final String SRJ_COLUMN = "submission_record_join_id";
	private static final String GUID_COLUMN = "guid";
	private static final String VISIT_DATE_COLUMN = "visitdate";
	private static final String DIAGNOSIS_CHANGE_COLUMN = "diagnoschangeind";

	public DiagnosisChangeResourceBuilder() {

	}

	public DiagnosisChangeResourceBuilder(Map<String, GenericTable> repeatableTableResultMap) {
		this.repeatableTableResultMap = repeatableTableResultMap;
	}

	public void putTableResult(String rgName, GenericTable tableResult) {
		if (repeatableTableResultMap == null) {
			repeatableTableResultMap = new HashMap<String, GenericTable>();
		}
		log.info("Put " + rgName);
		repeatableTableResultMap.put(rgName, tableResult);
	}

	public Model buildModel() {
		Model model = ModelFactory.createDefaultModel();
		Map<String, SubjectVisit> guidToSubjectVisit = new HashMap<String, SubjectVisit>();
		Map<String, String> submissionIdToGuid = new HashMap<String, String>();

		GenericTable requiredResult = repeatableTableResultMap.get(REQUIRED_RG);

		log.info(requiredResult.getRows().size() + " rows of Required group data...");

		for (GenericTableRow row : requiredResult.getRows()) {
			String submissionId = row.getValueByColumnName(SRJ_COLUMN).toString();

			String guid = row.getStringByColumnName(GUID_COLUMN);
			String visitDateString = row.getStringByColumnName(VISIT_DATE_COLUMN);
			Date visitDate = visitDateString != null ? BRICSTimeDateUtil.parseRepositoryDate(visitDateString) : null;

			if (guid != null && visitDate != null && submissionId != null) {
				submissionIdToGuid.put(submissionId, guid);
				SubjectVisit existingSubjectVisit = guidToSubjectVisit.get(guid);

				if (existingSubjectVisit == null) {
					guidToSubjectVisit.put(guid, new SubjectVisit(guid, visitDate, submissionId));
				} else if (existingSubjectVisit.getVisitDate().compareTo(visitDate) < 0) {
					guidToSubjectVisit.put(guid, new SubjectVisit(guid, visitDate, submissionId));
				}
			}
		}


		GenericTable diagnosisResult = repeatableTableResultMap.get(DIAGNOSIS_RG);
		log.info(diagnosisResult.getRows().size() + " rows of Diagnosis group data...");

		for (GenericTableRow row : diagnosisResult.getRows()) {
			String submissionId = row.getStringByColumnName(SRJ_COLUMN);
			String diagnosis = row.getStringByColumnName(DIAGNOSIS_CHANGE_COLUMN);
			String guid = submissionIdToGuid.get(submissionId);

			if (guid != null && diagnosis != null) {
				if (diagnosis.equals("Yes")) {
					SubjectVisit currentSubjectVisit = guidToSubjectVisit.get(guid);
					// checks if this diagnosis change is true for the latest visit date for
					// this subject
					if (currentSubjectVisit != null && submissionId.equals(currentSubjectVisit.getSubmissionId())) {
						model.add(GuidRDF.createGuidResource(guid), GuidRDF.DO_HIGHLIGHT_PROP,
								ResourceFactory.createPlainLiteral("true"));
						log.info("Adding highlight property to " + guid);
					}
				}
			}
		}

		return model;
	}
}

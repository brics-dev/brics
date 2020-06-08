package gov.nih.tbi.repository.rdf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.mail.MessagingException;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import gov.nih.tbi.ModulesConstants;
import gov.nih.tbi.RdfGenConstants;
import gov.nih.tbi.account.dao.EntityMapDao;
import gov.nih.tbi.commons.model.BRICSTimeDateUtil;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.model.DatasetStatus;
import gov.nih.tbi.commons.model.StatusType;
import gov.nih.tbi.commons.model.StudyStatus;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.util.MailEngine;
import gov.nih.tbi.commons.util.BRICSFilesUtils;
import gov.nih.tbi.dictionary.dao.DataElementDao;
import gov.nih.tbi.dictionary.dao.FormStructureDao;
import gov.nih.tbi.dictionary.dao.RepeatableGroupDao;
import gov.nih.tbi.dictionary.dao.StructuralDataElementDao;
import gov.nih.tbi.dictionary.model.hibernate.ClassificationElement;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.DiseaseStructure;
import gov.nih.tbi.dictionary.model.hibernate.ExternalId;
import gov.nih.tbi.dictionary.model.hibernate.FormStructure;
import gov.nih.tbi.dictionary.model.hibernate.Keyword;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.dictionary.model.hibernate.SubDomainElement;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.repository.dao.BasicDatasetDao;
import gov.nih.tbi.repository.dao.DataStoreInfoDao;
import gov.nih.tbi.repository.dao.DataStoreTabularInfoDao;
import gov.nih.tbi.repository.dao.DatasetDataStructureDao;
import gov.nih.tbi.repository.dao.RepositoryDao;
import gov.nih.tbi.repository.dao.StudyDao;
import gov.nih.tbi.repository.model.GenericTable;
import gov.nih.tbi.repository.model.GenericTableRow;
import gov.nih.tbi.repository.model.hibernate.BasicDataset;
import gov.nih.tbi.repository.model.hibernate.DataStoreInfo;
import gov.nih.tbi.repository.model.hibernate.DataStoreTabularColumnInfo;
import gov.nih.tbi.repository.model.hibernate.DataStoreTabularInfo;
import gov.nih.tbi.repository.model.hibernate.Dataset;
import gov.nih.tbi.repository.model.hibernate.DatasetDataStructure;
import gov.nih.tbi.repository.model.hibernate.Study;
import gov.nih.tbi.repository.rdf.exceptions.RDFGenException;
import gov.nih.tbi.semantic.model.DataElementRDF;
import gov.nih.tbi.semantic.model.DatasetRDF;
import gov.nih.tbi.semantic.model.FormStructureRDF;
import gov.nih.tbi.semantic.model.GuidRDF;
import gov.nih.tbi.semantic.model.RepeatableGroupRDF;
import gov.nih.tbi.semantic.model.StudyRDF;

@Service
@Scope("singleton")
public class RDFGeneratorManagerImpl {

	static Logger log = Logger.getLogger(RDFGeneratorManagerImpl.class);

	// Dao's
	@Autowired
	DataElementDao deDao;

	@Autowired
	FormStructureDao formStructureDao;

	@Autowired
	RepeatableGroupDao rgDao;

	@Autowired
	DatasetDataStructureDao dsdsDao;

	@Autowired
	StudyDao studyDao;

	@Autowired
	BasicDatasetDao basicDatasetDao;

	@Autowired
	EntityMapDao emDao;

	@Autowired
	DataStoreInfoDao dsInfoDao;

	@Autowired
	DataStoreTabularInfoDao dsTabInfoDao;

	@Autowired
	RepositoryDao repoDao;

	@Autowired
	MailEngine mailEngine;

	@Autowired
	MessageSource messageSource;

	@Autowired
	RdfGenConstants rdfGenConstants;

	@Autowired
	StructuralDataElementDao structuralDataElementDao;

	@Autowired
	ModulesConstants modulesConstants;

	private static final String CHANGE_DIAGNOSIS_FORM = "PDBPChangeDiagnosis";
	private static final String LOG_START_FORMAT = "----------  RDF Gen Started at %s ----------\n";
	private static final String LOG_SUCCESS_FORMAT = "----------  RDF Gen ended Successfully at %s ----------\n";
	private static final String LOG_FAIL_FORMAT = "----------  RDF Gen ended with Errors at %s ----------\n";
	private static final String COLUMN_NAME_DATASETID = "dataset_id";
	private static final String COLUMN_NAME_SJI = "submission_record_join_id";
	private static final int LARGE_REPEATABLE_GROUP_THRESHOLD = 65;
	private static final String INFINITE_REPEAT_THRESHOLD = "0";

	private Calendar startTime = Calendar.getInstance();

	private Map<Long, DataElement> deCache = new HashMap<Long, DataElement>();
	private Map<Long, FormStructure> fsCache = new HashMap<Long, FormStructure>();
	private Map<Long, RepeatableGroup> rgCache = new HashMap<Long, RepeatableGroup>();
	private Map<Long, Dataset> dsCache = new HashMap<Long, Dataset>();
	private Map<Long, Study> studyCache = new HashMap<Long, Study>();
	private Multimap<Long, Long> datasetDataStructureCache = ArrayListMultimap.create();
	private Multimap<Long, Long> formStructureDatasetCache = ArrayListMultimap.create();

	private RDFFileWriteUtil rdfFileWriter;
	private PrintStream logStream;
	private boolean isRunning = false;
	private boolean hasError;

	private final int INSTANCED_ROW_CHUNK_SIZE = 5000;

	private void cacheObjects() {
		log.info("Caching form structure and data elements...");
		fsCache = formStructureDao.getPublishedAndArchivedIntoMap();

		for (FormStructure fs : fsCache.values()) {
			for (DataElement de : fs.getDataElements().values()) {
				deCache.put(de.getId(), de);
			}
		}

		for (FormStructure fs : fsCache.values()) {
			for (RepeatableGroup rg : fs.getRepeatableGroups()) {
				rgCache.put(rg.getId(), rg);
			}
		}

		log.info("Done!");

		log.info("Caching study and datasets...");
		List<Study> privPubDatasetStudies = studyDao.getStudiesWithPrivateSharedDatasets();

		for (Study study : privPubDatasetStudies) {
			studyCache.put(study.getId(), study);
			for (Dataset dataset : study.getDatasetSet()) {
				dsCache.put(dataset.getId(), dataset);
			}
		}
		log.info("Done!");

		log.info("Caching dataset to form structure mapping...");
		List<DatasetDataStructure> datasetDataStructures = dsdsDao.getAll();
		for (DatasetDataStructure datasetDataStructure : datasetDataStructures) {
			datasetDataStructureCache.put(datasetDataStructure.getDataset().getId(),
					datasetDataStructure.getDataStructureId());
			formStructureDatasetCache.put(datasetDataStructure.getDataStructureId(),
					datasetDataStructure.getDataset().getId());
		}
		log.info("Done!");
	}

	private File initializeLogPrinter() {
		String filePath = rdfGenConstants.getLogPath();
		File logFile = new File(filePath);

		try {
			if (!logFile.exists()) {
				if (logFile.getParentFile() != null) {
					logFile.getParentFile().mkdirs();
				}
				logFile.createNewFile();
			}

			logStream = new PrintStream(logFile);
			String logStartString = String.format(LOG_START_FORMAT, BRICSTimeDateUtil.getCurrentReadableTimeString());
			logStream.println(logStartString);
			return logFile;
		} catch (FileNotFoundException e) {
			log.error("Could not find the log file", e);
		} catch (IOException e) {
			log.error("Error occurred when attempting to create log file", e);
		}

		return null;
	}

	private void clearCache() {
		deCache.clear();
		fsCache.clear();
		rgCache.clear();
		dsCache.clear();
		datasetDataStructureCache.clear();
		formStructureDatasetCache.clear();
	}

	public void generateAllRDF() {
		if (!isRunning) {
			isRunning = true;
			clearCache();
			File logFile = initializeLogPrinter();
			hasError = false;

			// set the start time of the current RDF gen job.
			// this is used to get the ttl file timestamp
			startTime = Calendar.getInstance();
			long myStartTime = System.currentTimeMillis();
			// if the generate all modules is set to true, generate the repository triples
			// as well
			try {
				if (rdfFileWriter == null) {
					rdfFileWriter = new RDFFileWriteUtil(rdfGenConstants.getTempExportDirectory(), startTime.getTime());
				} else {
					rdfFileWriter.setTimestamp(startTime.getTime());
				}

				log.info("Caching dictionary objects...");
				cacheObjects();
				log.info("Finished caching...");

				createDERDF();
				createFormRDF();
				createStudyRDF();
				createDatasetRDF();
				createRelationships();
				createSchema();

				if (rdfGenConstants.getGenerateAll()) {
					createRepositoryRDFDSInfo();
				}

				copyTempFileToRdfExportFolder();
			} catch (Exception e) {
				log.error("Error occurred during RDF Gen.", e);
				e.printStackTrace(logStream);
				hasError = true;
			} finally {
				isRunning = false;
				String logEndString = null;

				if (hasError) { // rdf gen ended with errors
					log.info("Error occurred while running RDF Gen, sending failure email to infrastructure...");
					logEndString = String.format(LOG_FAIL_FORMAT, BRICSTimeDateUtil.getCurrentReadableTimeString());
					sendFailureEmail(logFile);
				} else { // rdf gen ran successfully
					logEndString = String.format(LOG_SUCCESS_FORMAT, BRICSTimeDateUtil.getCurrentReadableTimeString());
				}

				logStream.println(logEndString);
				logStream.close();
			}

			long endTime = System.currentTimeMillis();

			log.debug("Total time: " + (endTime - myStartTime) + "ms");
		} else {
			log.info(
					"A user or scheduler tried to initiate another RDF gen.  Ignoring the command because a RDF gen process is already running.");
		}
	}

	/**
	 * Copies the temporary RDF file to the real RDF-Exports folder. Do this after a gen has been completed
	 * 
	 * @throws IOException
	 */
	private void copyTempFileToRdfExportFolder() throws IOException {
		File tempFile = new File(
				RDFFileWriteUtil.generateFilePath(rdfGenConstants.getTempExportDirectory(), startTime.getTime()));
		File newFile =
				new File(RDFFileWriteUtil.generateFilePath(rdfGenConstants.getExportPath(), startTime.getTime()));

		if (tempFile.renameTo(newFile)) {
			log.info("Successfully moved " + tempFile.getAbsolutePath() + " to " + newFile.getAbsolutePath());
		} else {
			throw new IOException("Failed to move " + tempFile.getAbsolutePath() + " to " + newFile.getAbsolutePath());
		}
	}

	/**
	 * Send an email to infrastructure to inform of RDF gen failure
	 */
	private void sendFailureEmail(File logFile) {
		// here, we are reading the rdflog file and adding it into the email to be sent
		// to infrastructure.
		String rdfLogContent = null;

		try {
			rdfLogContent = BRICSFilesUtils.readFile(logFile, StandardCharsets.UTF_8);
			rdfLogContent = rdfLogContent.replace("\n", "<br>");
		} catch (IOException e) {
			rdfLogContent = "Error occurred while trying to read rdfgen.log content to email";
			log.error(rdfLogContent, e);
		} finally {
			// we still want to send the email even if reading the log file caused an error
			String emailAddress = modulesConstants.getModulesInfraEmail();
			String mailSubject = messageSource.getMessage(ServiceConstants.RDF_GEN_FAILED_SUBJECT, null, null);

			Object[] bodyArgs = new Object[] {modulesConstants.getModulesAccountURL(), rdfLogContent};
			String mailBody = messageSource.getMessage(ServiceConstants.RDF_GEN_FAILED_BODY, bodyArgs, null);

			try {
				mailEngine.sendMail(mailSubject, mailBody, null, emailAddress);
			} catch (MessagingException e) {
				log.error("Error occured while trying to email infrastructure about failed RDF gen", e);
			}
		}
	}

	public void createDERDF() {
		Model model = ModelFactory.createDefaultModel();
		List<DataElement> elementList;

		elementList = new ArrayList<DataElement>(deCache.values());
		log.info("Creating Elements RDF for" + elementList.size() + " des");

		// Iterate through list
		for (DataElement de : elementList) {
			if (DataElementStatus.PUBLISHED.equals(de.getStatus())
					|| DataElementStatus.DEPRECATED.equals(de.getStatus())
					|| DataElementStatus.RETIRED.equals(de.getStatus())) {
				model = addRDFForDataElement(model, de);
			}

		}

		writeModelToTempFile(model);
	}

	public void writeModelToTempFile(Model model) {
		try {
			rdfFileWriter.writeToFile(model);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Model addRDFForDataElement(Model model, DataElement de) {

		// If Published
		Resource deResource = DataElementRDF.createDEResource(de.getStructuralObject());

		// If CDE
		if (de.isCommonDataElement()) {
			// model.add(deResource, RDF.type, DataElementRDF.RESOURCE_CDE);
			model.add(deResource, RDFS.subClassOf,
					ResourceFactory.createResource(DataElementRDF.RESOURCE_CDE.getURI()));

		} else {
			model.add(deResource, RDFS.subClassOf, ResourceFactory.createResource(DataElementRDF.RESOURCE_DE.getURI()));

		}
		// Added this for when model is not using a reasoner
		model.add(deResource, RDFS.subClassOf,
				ResourceFactory.createResource(DataElementRDF.RESOURCE_ELEMENT.getURI()));
		model.add(deResource, RDF.type, RDFS.Class);

		model.add(deResource, DataElementRDF.PROPERTY_ID, ResourceFactory.createPlainLiteral(de.getId().toString()));

		model.add(deResource, DataElementRDF.PROPERTY_ELEMENT_NAME, ResourceFactory.createPlainLiteral(de.getName()));

		model.add(deResource, DataElementRDF.PROPERTY_TITLE, ResourceFactory.createPlainLiteral(de.getTitle()));

		model.add(deResource, RDFS.label, ResourceFactory.createPlainLiteral(de.getName()));
		model.add(deResource, RDFS.isDefinedBy, DataElementRDF.createDEResource(de.getStructuralObject()));

		model.add(deResource, DataElementRDF.PROPERTY_ELEMENT_TYPE,
				ResourceFactory.createPlainLiteral(de.getType().getValue()));

		model.add(deResource, DataElementRDF.PROPERTY_INPUT_RESTRICTION,
				ResourceFactory.createPlainLiteral(de.getRestrictions().getValue()));

		model.add(deResource, DataElementRDF.PROPERTY_ELEMENT_CATEGORY,
				ResourceFactory.createPlainLiteral(de.getCategory().getShortName()));

		if (de.getSize() != null) {
			model.add(deResource, DataElementRDF.PROPERTY_ELEMENT_SIZE,
					ResourceFactory.createTypedLiteral(de.getSize()));
		}

		if (de.getSubDomainElementList() != null) {
			Set<String> alreadyProcessed = new HashSet<String>();
			for (SubDomainElement se : de.getSubDomainElementList()) // there should only be one disease here
			{
				if (se.getDisease().getName() != null && !alreadyProcessed.contains(se.getDisease().getName())) {
					model.add(deResource, DataElementRDF.PROPERTY_ELEMENT_DISEASE,
							ResourceFactory.createPlainLiteral(se.getDisease().getName()));

					alreadyProcessed.add(se.getDisease().getName());
				}
			}
		}

		if (de.getDescription() != null) {
			model.add(deResource, DataElementRDF.PROPERTY_DECRIPTION,
					ResourceFactory.createPlainLiteral(de.getDescription()));
		}

		if (de.getShortDescription() != null) {
			model.add(deResource, DataElementRDF.PROPERTY_SHORT_DESCRIPTION,
					ResourceFactory.createPlainLiteral(de.getShortDescription()));
		}

		if (de.getNotes() != null) {
			model.add(deResource, DataElementRDF.PROPERTY_NOTES, ResourceFactory.createPlainLiteral(de.getNotes()));
		}

		if (de.getGuidelines() != null) {
			model.add(deResource, DataElementRDF.PROPERTY_GUIDELINES,
					ResourceFactory.createPlainLiteral(de.getGuidelines()));
		}

		if (de.getHistoricalNotes() != null) {
			model.add(deResource, DataElementRDF.PROPERTY_HISTORICAL_NOTES,
					ResourceFactory.createPlainLiteral(de.getHistoricalNotes()));
		}

		if (de.getReferences() != null) {
			model.add(deResource, DataElementRDF.PROPERTY_REFERENCE,
					ResourceFactory.createPlainLiteral(de.getReferences()));
		}

		if (de.getMaximumValue() != null) {
			model.add(deResource, DataElementRDF.PROPERTY_MAXIMUM_VALUE,
					ResourceFactory.createPlainLiteral(de.getMaximumValue().toString()));
		}

		if (de.getMinimumValue() != null) {
			model.add(deResource, DataElementRDF.PROPERTY_MINIMUM_VALUE,
					ResourceFactory.createPlainLiteral(de.getMinimumValue().toString()));
		}

		if (de.getPopulation() != null && de.getPopulation().getName() != null) {
			model.add(deResource, DataElementRDF.PROPERTY_POPULATION,
					ResourceFactory.createPlainLiteral(de.getPopulation().getName()));
		}

		if (de.getKeywords() != null) {
			for (Keyword k : de.getKeywords()) {
				if (k.getKeyword() != null) {
					model.add(deResource, DataElementRDF.PROPERTY_KEYWORD,
							ResourceFactory.createPlainLiteral(k.getKeyword()));
				}
			}
		}

		if (de.getExternalIdSet() != null) {
			for (ExternalId externalId : de.getExternalIdSet()) {
				if (externalId.getValue() != null) {
					model.add(deResource, DataElementRDF.PROPERTY_EXTERNAL_ID,
							ResourceFactory.createPlainLiteral(externalId.getValue()));
				}
			}
		}

		// This was added by MV to mirror the keyword logic. I'm not sure if this should
		// actually be here. (Also
		// I created the PROPERTY_LABEL constant).
		if (de.getLabels() != null) {
			for (Keyword l : de.getLabels()) {
				if (l.getKeyword() != null) {
					model.add(deResource, DataElementRDF.PROPERTY_LABEL,
							ResourceFactory.createPlainLiteral(l.getKeyword()));
				}
			}
		}

		if (de.getSeeAlso() != null) {
			model.add(deResource, DataElementRDF.PROPERTY_SEE_ALSO,
					ResourceFactory.createPlainLiteral(de.getSeeAlso()));
		}

		if (de.getValueRangeList() != null && !de.getValueRangeList().isEmpty()) {
			for (ValueRange valueRange : de.getValueRangeList()) {
				if (valueRange.getValueRange() != null) {
					// **** Remove this once the code change for QT get merged back in with the
					// release brach
					model.add(deResource, DataElementRDF.PROPERTY_PERMISSIBLE_VALUE,
							ResourceFactory.createPlainLiteral(valueRange.getValueRange()));
					// ****

					Resource pvResource = DataElementRDF.createPermissibleValueResource(de.getName(), valueRange);
					model.add(pvResource, DataElementRDF.PROPERTY_PERMISSIBLE_VALUE_VALUE,
							ResourceFactory.createPlainLiteral(valueRange.getValueRange()));
					model.add(pvResource, RDFS.subClassOf, DataElementRDF.PERMISSIBLE_VALUE_CLASS);
					model.add(pvResource, DataElementRDF.PROPERTY_PERMISSIBLE_VALUE_DESCRIPTION,
							valueRange.getDescription());
					model.add(deResource, DataElementRDF.PROPERTY_PERMISSIBLE_VALUES, pvResource);
				}
			}
		}

		if (de.getClassificationElementList() != null)
			for (ClassificationElement ce : de.getClassificationElementList()) {
				if (ce.getClassification().getName() != null) {
					model.add(deResource, DataElementRDF.PROPERTY_CLASSIFICATION,
							ResourceFactory.createPlainLiteral(ce.getClassification().getName()));
				}
			}

		return model;

	}

	public void createFormRDF() {
		Model model = ModelFactory.createDefaultModel();
		List<FormStructure> formList;

		// Get Forms
		formList = new ArrayList<FormStructure>(fsCache.values());

		log.info("Creating RDF Forms for " + formList.size() + " forms");

		// Iterate through list
		for (FormStructure form : formList) {
			if (form.getStatus().equals(StatusType.PUBLISHED) || form.getStatus().equals(StatusType.ARCHIVED)) {
				model = addRDFForForm(model, form);

			}
		}

		writeModelToTempFile(model);
	}

	public Model addRDFForForm(Model model, FormStructure form) {

		// Only create form triples if they are in a dataset
		if (dsdsDao.isFormInAnyDataset(form.getId())) {

			Resource formResource = FormStructureRDF.createFormResource(form);

			// model.add(formResource, RDF.type, FormStructureRDF.RESOURCE_FORM_STRUCTURE);
			model.add(formResource, RDFS.subClassOf,
					ResourceFactory.createResource(FormStructureRDF.RESOURCE_FORM_STRUCTURE.getURI()));

			// Added this for when reasoner is off
			model.add(formResource, RDF.type, RDFS.Class);

			model.add(formResource, RDFS.label,
					ResourceFactory.createPlainLiteral(FormStructureRDF.generateName(form)));

			model.add(formResource, RDFS.isDefinedBy, formResource);

			model.add(formResource, FormStructureRDF.PROPERTY_ID,
					ResourceFactory.createPlainLiteral(form.getId().toString()));

			model.add(formResource, FormStructureRDF.PROPERTY_TITLE,
					ResourceFactory.createPlainLiteral(form.getTitle()));

			if (form.getShortName() != null) {
				model.add(formResource, FormStructureRDF.PROPERTY_SHORT_NAME,
						ResourceFactory.createPlainLiteral(form.getShortName()));
			}

			if (form.getDescription() != null) {
				model.add(formResource, FormStructureRDF.PROPERTY_DESCRIPTION,
						ResourceFactory.createPlainLiteral(form.getDescription()));
			}

			if (form.getVersion() != null) {
				model.add(formResource, FormStructureRDF.PROPERTY_VERSION,
						ResourceFactory.createPlainLiteral(form.getVersion()));
			}

			if (form.getOrganization() != null) {
				model.add(formResource, FormStructureRDF.PROPERTY_ORGANIZATION,
						ResourceFactory.createPlainLiteral(form.getOrganization()));
			}
			if (form.getPublicationDate() != null) {
				model.add(formResource, FormStructureRDF.PROPERTY_PUBLICATION_DATE,
						ResourceFactory.createTypedLiteral(form.getPublicationDate()));

			}

			if (form.getDiseaseList() != null) {
				for (DiseaseStructure ds : form.getDiseaseList()) {
					model.add(formResource, FormStructureRDF.PROPERTY_DISEASE,
							ResourceFactory.createPlainLiteral(ds.getDisease().getName()));
				}
			}

			// Create RDF for Repeatable Groups
			for (RepeatableGroup rg : form.getRepeatableGroups()) {
				int dataElementCount = rg.getDataElements().size();

				Resource rgResource = RepeatableGroupRDF.createRGResource(rg);
				model.add(rgResource, RDFS.subClassOf,
						ResourceFactory.createResource(RepeatableGroupRDF.RESOURCE_RG.getURI()));

				// Added this for when reasoner is off
				model.add(rgResource, RDF.type, RDFS.Class);

				model.add(rgResource, RepeatableGroupRDF.PROPERTY_POSITION,
						ResourceFactory.createPlainLiteral(rg.getPosition().toString()));

				model.add(rgResource, RepeatableGroupRDF.PROPERTY_REPEAT_TYPE, rg.getType().toString());

				// If the number of data elements is larger than the data element count
				// threshold, then we need to have
				// this repeatable group set to repeat in order to get around a problem in the
				// query tool when there are
				// too many data elements in a single non-repeating repeatable group
				if (dataElementCount > LARGE_REPEATABLE_GROUP_THRESHOLD) {
					model.add(rgResource, RepeatableGroupRDF.PROPERTY_THRESHOLD, INFINITE_REPEAT_THRESHOLD);
				} else {
					model.add(rgResource, RepeatableGroupRDF.PROPERTY_THRESHOLD, rg.getThreshold().toString());
				}

				model.add(rgResource, RDFS.label,
						ResourceFactory.createPlainLiteral(RepeatableGroupRDF.generateName(rg)));

				model.add(rgResource, RDFS.isDefinedBy, rgResource);

				model.add(rgResource, RepeatableGroupRDF.PROPERTY_NAME,
						ResourceFactory.createPlainLiteral(rg.getName()));

				// Relationships for repeatable group
				// Form --> RG
				model.add(formResource, FormStructureRDF.RELATION_PROPERTY_HAS_REPEATABLE_GROUP, rgResource);

				// RG --> Form
				model.add(rgResource, RepeatableGroupRDF.RELATION_PROPERTY_HAS_FORM, formResource);

				for (MapElement me : rg.getMapElements()) {
					DataElement de = deCache.get(me.getStructuralDataElement().getId());

					if (de == null) {
						de = deDao.get(me.getStructuralDataElement().getId());
					}

					Resource deResource = DataElementRDF.createDEResource(de.getStructuralObject());

					// RG --> DE
					model.add(rgResource, RepeatableGroupRDF.RELATION_PROPERTY_HAS_DATA_ELEMENT, deResource);

					if (me.getPosition() != null) {
						model.add(rgResource, DataElementRDF.createDEProperty(de.getStructuralObject()),
								ResourceFactory.createPlainLiteral(me.getPosition().toString()));
					}

					if (me.getRequiredType() != null) {
						Resource requiredTypeResource =
								DataElementRDF.createRequiredTypeResource(me);
						model.add(requiredTypeResource, RDF.type, DataElementRDF.RESOURCE_REQUIRED_TYPE);
						model.add(requiredTypeResource, DataElementRDF.PROPERTY_ELEMENT_NAME,
								ResourceFactory.createPlainLiteral(de.getName()));
						model.add(requiredTypeResource, RDFS.subClassOf, deResource);
						model.add(requiredTypeResource, RDFS.label,
								ResourceFactory.createPlainLiteral(me.getRequiredType().name()));
						model.add(rgResource, RepeatableGroupRDF.RELATION_PROPERTY_HAS_REQUIRED_TYPE,
								requiredTypeResource);
					}

					// DE --> RG
					model.add(deResource, DataElementRDF.RELATION_PROPERTY_HAS_REPEATABLE_GROUP, rgResource);
				}
			}

		}

		return model;

	}

	public void createStudyRDF() {
		Model model = ModelFactory.createDefaultModel();

		log.info("Creating RDF Studies for " + studyCache.size() + " studies");
		// Iterate through list
		for (Study study : studyCache.values()) {

			model = addRDFForStudy(model, study);
		}
		writeModelToTempFile(model);
	}

	public Model addRDFForStudy(Model model, Study study) {

		// Only create triples if Private / Public study AND the study contains at least
		// one dataset that is private or
		// shared
		if ((StudyStatus.PUBLIC.equals(study.getStudyStatus()) || StudyStatus.PRIVATE.equals(study.getStudyStatus()))) {
			Resource studyResource = StudyRDF.createStudyResource(study);
			// model.add(studyResource, RDF.type, StudyRDF.RESOURCE_STUDY);
			model.add(studyResource, RDFS.subClassOf, ResourceFactory.createResource(StudyRDF.RESOURCE_STUDY.getURI()));
			model.add(studyResource, StudyRDF.PROPERTY_TITLE, ResourceFactory.createPlainLiteral(study.getTitle()));

			// Added this for when reasoner is off
			model.add(studyResource, RDF.type, RDFS.Class);

			model.add(studyResource, RDFS.label, ResourceFactory.createPlainLiteral(study.getTitle()));

			model.add(studyResource, RDFS.isDefinedBy, StudyRDF.createStudyResource(study));
			model.add(studyResource, StudyRDF.PROPERTY_ID,
					ResourceFactory.createPlainLiteral(study.getId().toString()));

			if (study.getAbstractText() != null) {
				model.add(studyResource, StudyRDF.PROPERTY_ABSTRACT,
						ResourceFactory.createPlainLiteral(study.getAbstractText()));
			}

			if (study.getDateCreated() != null) {
				model.add(studyResource, StudyRDF.PROPERTY_DATE_CREATED,
						ResourceFactory.createTypedLiteral(study.getDateCreated()));
			}

			if (study.getPrincipalInvestigator() != null) {
				model.add(studyResource, StudyRDF.PROPERTY_PI,
						ResourceFactory.createPlainLiteral(study.getPrincipalInvestigator()));
			}

			if (study.getStudyStatus() != null) {
				model.add(studyResource, StudyRDF.PROPERTY_STATUS,
						ResourceFactory.createPlainLiteral(study.getStudyStatus().getName()));
			}

			if (study.getPrefixedId() != null) {
				model.add(studyResource, StudyRDF.PROPERTY_PREFIXED_ID,
						ResourceFactory.createPlainLiteral(study.getPrefixedId()));
			}
		}

		return model;

	}

	public void createDatasetRDF() {
		Model model = ModelFactory.createDefaultModel();
		List<BasicDataset> datasetList;

		// Get Forms
		datasetList = basicDatasetDao.getAll();

		log.info("Creating RDF Datasets for " + datasetList.size() + " datasets");

		// Iterate through list
		for (BasicDataset dataset : datasetList) {

			model = addRDFForDataset(model, dataset);
		}

		writeModelToTempFile(model);
	}

	public Model addRDFForDataset(Model model, BasicDataset dataset) {

		// Only create triples for nonarchived datasets (Private / Shared) for now
		if (dataset.getDatasetStatus().equals(DatasetStatus.PRIVATE)
				|| dataset.getDatasetStatus().equals(DatasetStatus.SHARED)) {

			Resource datasetResource = DatasetRDF.createDatasetResource(dataset);
			// model.add(datasetResource, RDF.type, DatasetRDF.RESOURCE_DATASET);
			model.add(datasetResource, RDFS.subClassOf,
					ResourceFactory.createResource(DatasetRDF.RESOURCE_DATASET.getURI()));

			// Added this for when reasoner is off
			model.add(datasetResource, RDF.type, RDFS.Class);

			model.add(datasetResource, RDFS.label, ResourceFactory.createPlainLiteral(dataset.getName()));

			model.add(datasetResource, RDFS.isDefinedBy, DatasetRDF.createDatasetResource(dataset));

			model.add(datasetResource, DatasetRDF.PROPERTY_ID,
					ResourceFactory.createPlainLiteral(dataset.getId().toString()));

			model.add(datasetResource, DatasetRDF.PROPERTY_IS_DERIVED, ResourceFactory.createPlainLiteral(
					String.valueOf(dataset.getIsDerived() != null ? dataset.getIsDerived() : Boolean.FALSE)));

			if (dataset.getPublicationDate() != null) {
				model.add(datasetResource, DatasetRDF.PROPERTY_PUBLICATION_DATE,
						ResourceFactory.createTypedLiteral(dataset.getPublicationDate()));
			}

			if (dataset.getSubmitDate() != null) {

				model.add(datasetResource, DatasetRDF.PROPERTY_SUBMIT_DATE,
						ResourceFactory.createTypedLiteral(dataset.getSubmitDate()));
			}

			if (dataset.getSubmitter() != null && dataset.getSubmitter().getFullName() != null) {
				model.add(datasetResource, DatasetRDF.PROPERTY_SUBMITTER_NAME,
						ResourceFactory.createPlainLiteral(dataset.getSubmitter().getFullName()));
			}

			if (dataset.getStudy() != null && dataset.getStudy().getId() != null) {
				model.add(datasetResource, StudyRDF.PROPERTY_ID,
						ResourceFactory.createTypedLiteral(dataset.getStudy().getId()));
			}

			if (dataset.getDatasetStatus() != null) {
				model.add(datasetResource, DatasetRDF.PROPERTY_STATUS,
						ResourceFactory.createPlainLiteral(dataset.getDatasetStatus().getName()));
			}
		}

		return model;
	}

	public void createRelationships() {
		Model model = ModelFactory.createDefaultModel();

		if (log.isDebugEnabled()) {
			log.debug("Creating RDF Relationships: " + studyCache.size() + " studies");
		}

		// Iterate through list
		for (Study study : studyCache.values()) {

			// Only create triples for Public / Private studies
			if (study.getStudyStatus().equals(StudyStatus.PUBLIC)
					|| study.getStudyStatus().equals(StudyStatus.PRIVATE)) {

				// refactoring that creates a separate public method for calling each object in
				// the loop.
				// This mimics the behavior for the other methods in this class.
				model = createRDFForStudyRelationship(model, study);
			}
		}

		writeModelToTempFile(model);
	}

	/**
	 * This method processes each study independently. The form is passed through for further processing unless it is
	 * null. Otherwise it looks up the form from the DAO (this was the original process - but if the form is already
	 * known, there is no need to look it up again).
	 */
	public Model createRDFForStudyRelationship(Model model, Study study) {

		Resource studyResource = StudyRDF.createStudyResource(study);
		// Property studyProperty = StudyRDF.createStudyProperty(study);

		// Study --> Study
		model.add(studyResource, StudyRDF.RELATION_PROPERTY_FACETED_STUDY, studyResource);

		for (Dataset ds : study.getDatasetSet()) {
			// Only create triples for nonarchived datasets (Private / Shared) for now
			if (ds.getDatasetStatus().equals(DatasetStatus.PRIVATE)
					|| ds.getDatasetStatus().equals(DatasetStatus.SHARED)) {
				log.info("Creating relationships for dataset: " + ds.getPrefixedId());
				Resource datasetResource = DatasetRDF.createDatasetResource(ds);

				// Dataset --> Study
				model.add(datasetResource, StudyRDF.RELATION_PROPERTY_FACETED_STUDY, studyResource);

				// Study --> Dataset
				model.add(studyResource, StudyRDF.RELATION_PROPERTY_FACETED_DATASET, datasetResource);

				// set of form structure IDs that are associated with the dataset
				Collection<Long> formStructureIds = datasetDataStructureCache.get(ds.getId());

				if (formStructureIds == null) {
					log.error("Dataset, " + ds.getPrefixedId()
							+ ", is missing entries from the dataset_data_structure table.");
					throw new RDFGenException("Dataset, " + ds.getPrefixedId()
							+ ", is missing entries from the dataset_data_structure table.");
				}

				for (Long formStructureId : formStructureIds) {
					FormStructure currentFormStructure = fsCache.get(formStructureId);

					if (currentFormStructure != null) {
						model = createRDFForDataElementsRelationship(model, studyResource, datasetResource,
								currentFormStructure);
					} else {
						log.error("Form structure with ID, " + formStructureId
								+ " does not exist in the form structure cache");
						throw new RDFGenException("Form structure with ID, " + formStructureId
								+ " does not exist in the form structure cache");
					}
				}
			}
		}
		return model;
	}

	/**
	 * This method contains the logic originally in the createReleationships() method. it has been broken out so that it
	 * can be called on the objects themselves rather than having it query all the DAOs required along the way.
	 */
	public Model createRDFForDataElementsRelationship(Model model, Resource studyResource, Resource datasetResource,
			FormStructure form) {

		Resource formResource = FormStructureRDF.createFormResource(form);
		// Property formProperty = FormStructureRDF.createFormProperty(form);

		// Study --> Form
		model.add(studyResource, StudyRDF.RELATION_PROPERTY_FACETED_FORM, formResource);

		// Form --> Form
		model.add(formResource, StudyRDF.RELATION_PROPERTY_FACETED_FORM, formResource);

		// Form --> Study
		model.add(formResource, StudyRDF.RELATION_PROPERTY_FACETED_STUDY, studyResource);

		// Form --> Dataset
		model.add(formResource, StudyRDF.RELATION_PROPERTY_FACETED_DATASET, datasetResource);

		// Dataset --> Form
		model.add(datasetResource, StudyRDF.RELATION_PROPERTY_FACETED_FORM, formResource);

		for (DataElement me : form.getDataElements().values()) {
			Resource deResource = DataElementRDF.createDEResource(me.getStructuralObject());
			// Property deProperty = DataElementRDF.createDEProperty(me.getDataElement());

			// Form --> DE
			model.add(formResource, StudyRDF.RELATION_PROPERTY_FACETED_DE, deResource);
			// Study --> DE
			model.add(studyResource, StudyRDF.RELATION_PROPERTY_FACETED_DE, deResource);
			// DE --> DE
			model.add(deResource, StudyRDF.RELATION_PROPERTY_FACETED_DE, deResource);

			// DE --> Form
			model.add(deResource, StudyRDF.RELATION_PROPERTY_FACETED_FORM, formResource);
			// DE --> Study
			model.add(deResource, StudyRDF.RELATION_PROPERTY_FACETED_STUDY, studyResource);

		}

		return model;
	}

	public void createSchema() {
		Model model = ModelFactory.createDefaultModel();
		Resource elementResource = DataElementRDF.RESOURCE_ELEMENT;
		model.add(elementResource, RDF.type, RDFS.Class);
		// model.add(elementResource, RDFS.subClassOf, RDFS.Class);
		model.add(elementResource, RDFS.label, ResourceFactory.createPlainLiteral("Element"));
		model.add(elementResource, RDFS.comment, ResourceFactory.createPlainLiteral("This is a comment for Elements"));

		Resource cdeResource = DataElementRDF.RESOURCE_CDE;
		model.add(cdeResource, RDF.type, RDFS.Class);
		model.add(cdeResource, RDFS.label, ResourceFactory.createPlainLiteral("CommonDataElement"));
		model.add(cdeResource, RDFS.subClassOf, ResourceFactory.createResource(elementResource.getURI()));
		model.add(cdeResource, RDFS.comment,
				ResourceFactory.createPlainLiteral("This is a comment for CommonDataElements"));

		Resource deResource = DataElementRDF.RESOURCE_DE;
		model.add(deResource, RDF.type, RDFS.Class);
		model.add(deResource, RDFS.label, ResourceFactory.createPlainLiteral("DataElement"));
		model.add(deResource, RDFS.subClassOf, ResourceFactory.createResource(elementResource.getURI()));
		model.add(deResource, RDFS.comment, ResourceFactory.createPlainLiteral("This is a comment for DataElements"));

		Resource formResource = FormStructureRDF.RESOURCE_FORM_STRUCTURE;
		model.add(formResource, RDF.type, RDFS.Class);
		// model.add(formResource, RDFS.subClassOf, RDFS.Class);
		model.add(formResource, RDFS.label, ResourceFactory.createPlainLiteral("FormStructure"));
		model.add(formResource, RDFS.comment,
				ResourceFactory.createPlainLiteral("This is a comment for FormStructures"));

		Resource rgResource = RepeatableGroupRDF.RESOURCE_RG;
		model.add(rgResource, RDF.type, RDFS.Class);
		model.add(rgResource, RDFS.label, ResourceFactory.createPlainLiteral("RepeatableGroup"));
		model.add(rgResource, RDFS.comment,
				ResourceFactory.createPlainLiteral("This is a comment for RepeatableGroup"));

		Resource studyResource = StudyRDF.RESOURCE_STUDY;
		model.add(studyResource, RDF.type, RDFS.Class);
		// model.add(studyResource, RDFS.subClassOf, RDFS.Class);
		model.add(studyResource, RDFS.label, ResourceFactory.createPlainLiteral("Study"));
		model.add(studyResource, RDFS.comment, ResourceFactory.createPlainLiteral("This is a comment for Studies"));

		Resource datasetResoure = DatasetRDF.RESOURCE_DATASET;
		model.add(datasetResoure, RDF.type, RDFS.Class);
		// model.add(datasetResoure, RDFS.subClassOf, RDFS.Class);
		model.add(datasetResoure, RDFS.label, ResourceFactory.createPlainLiteral("Dataset"));
		model.add(datasetResoure, RDFS.comment, ResourceFactory.createPlainLiteral("This is a comment for Datasets"));

		writeModelToTempFile(model);
	}

	/**
	 * Returns true if the form structure associated with the datastore has at least one dataset submitted against it,
	 * false otherwise.
	 * 
	 * @param dsInfo
	 * @return boolean
	 */
	private boolean doesDataStoreHaveData(DataStoreInfo dsInfo) {
		Long formStructureId = dsInfo.getDataStructureId();
		Collection<Long> datasetIds = formStructureDatasetCache.get(formStructureId);

		if (datasetIds == null) {
			return false;
		} else {
			return !datasetIds.isEmpty();
		}
	}

	public void createRepositoryRDFDSInfo() {

		log.info("CREATING RDF REPOSITORY");

		// Get DataStoreInfo
		List<DataStoreInfo> dsInfos = dsInfoDao.getAll();
		List<Long> privateSharedDatasetIds = basicDatasetDao.getPrivateSharedDatasetIds();
		Set<String> guidsFound = new HashSet<String>();
		for (DataStoreInfo dsInfo : dsInfos) {
			if (doesDataStoreHaveData(dsInfo)) {
				createRDFForDSInfo(dsInfo, privateSharedDatasetIds, guidsFound);
			}
		}
	}

	private Map<Long, DataElement> constructMapElementCache(Collection<RepeatableGroup> repeatableGroups) {
		Map<Long, DataElement> mapElementIdToDataElementMap = new HashMap<Long, DataElement>();

		for (RepeatableGroup repeatableGroup : repeatableGroups) {
			for (MapElement mapElement : repeatableGroup.getMapElements()) {
				Long mapElementId = mapElement.getId();
				Long dataElementId = mapElement.getStructuralDataElement().getId();
				DataElement dataElement = deCache.get(dataElementId);

				mapElementIdToDataElementMap.put(mapElementId, dataElement);
			}
		}

		return mapElementIdToDataElementMap;
	}

	private void createRDFForRowMetaData(Model model, Object datasetId, Resource formInstance) {
		String columnValue = datasetId.toString();

		if (columnValue != null && !columnValue.isEmpty()) {

			model.add(formInstance, DatasetRDF.PROPERTY_ID,
					ResourceFactory.createTypedLiteral(columnValue, XSDDatatype.XSDlong));
		}

		Long datasetLong = Long.valueOf(columnValue);
		Dataset dataset = dsCache.get(datasetLong);

		if (dataset != null) {
			model.add(formInstance, FormStructureRDF.PROPERTY_DATASET, DatasetRDF.createDatasetResource(dataset));

			String study = dataset.getStudy().getTitle();
			model.add(formInstance, DatasetRDF.PROPERTY_STUDY,
					ResourceFactory.createTypedLiteral(study, XSDDatatype.XSDstring));

			String studyPrefixedId = dataset.getStudy().getPrefixedId();
			model.add(formInstance, StudyRDF.PROPERTY_PREFIXED_ID, ResourceFactory.createPlainLiteral(studyPrefixedId));

			String prefixedId = dataset.getPrefixedId();
			model.add(formInstance, DatasetRDF.PROPERTY_PREFIXED_ID, ResourceFactory.createPlainLiteral(prefixedId));

			String datasetName = dataset.getName();
			model.add(formInstance, DatasetRDF.PROPERTY_DATASET_NAME, ResourceFactory.createPlainLiteral(datasetName));

			String isDerived = String.valueOf(dataset.getIsDerived());
			model.add(formInstance, DatasetRDF.PROPERTY_IS_DERIVED, ResourceFactory.createPlainLiteral(isDerived));
		}
	}

	private void createRDFForInstancedRg(Model model, Resource formInstance, Resource rgInstance, RepeatableGroup rg) {
		// Create RG Instance
		model.add(rgInstance, RDF.type, RepeatableGroupRDF.createRGResource(rg));

		// FormInstance --> RGInstance
		model.add(formInstance, FormStructureRDF.RELATION_PROPERTY_HAS_REPEATABLE_GROUP_INSTANCE, rgInstance);

		// RGInstnace --> FormInstance
		model.add(rgInstance, FormStructureRDF.RELATION_PROPERTY_IS_OF_FORM_INSTANCE, formInstance);
	}

	private String getAvailableRgKey(String submissionString, RepeatableGroup rg) {
		return submissionString + "_" + rg.getId();
	}

	private void createRDFForRow(Model model, DataStoreTabularInfo tabInfo, GenericTableRow row, FormStructure form,
			Map<String, Resource> submissionToFormInstance, Map<String, Resource> availableRgs,
			Map<Long, DataElement> mapElementCache, int rowNum, Set<String> guidsFound) {
		boolean existingFormInstance = false;
		Object submission_record_join_id = row.getValueByColumnName(COLUMN_NAME_SJI);
		String submissionString = submission_record_join_id.toString();

		Resource formInstance = submissionToFormInstance.get(submissionString);

		if (formInstance != null) {
			existingFormInstance = true;
		}

		if (!existingFormInstance) {
			formInstance = FormStructureRDF.createFormResourceInstance(form, submissionString);
			submissionToFormInstance.put(submissionString, formInstance);
			model.add(formInstance, RDF.type, FormStructureRDF.createFormResource(form));

			// we need to add all of the repeatable group instance rows as soon as the form
			// instance is created.
			// if we don't, then instanced repeatable group nodes may be missing if the row
			// does not contain any data
			// for the entire repeatable group.
			for (RepeatableGroup rg : form.getRepeatableGroups()) {

				// to reuse all of the new nodes we are putting here, we need to keep track of
				// the available rg nodes
				Resource rgInstance = RepeatableGroupRDF.createRGResourceInstance(rg, rowNum);
				availableRgs.put(getAvailableRgKey(submissionString, rg), rgInstance);

				createRDFForInstancedRg(model, formInstance, rgInstance, rg);
			}
		}

		// Add dataset id to row number
		Object datasetId = row.getValueByColumnName(COLUMN_NAME_DATASETID);

		if (datasetId != null && !existingFormInstance) {
			createRDFForRowMetaData(model, datasetId, formInstance);
		}

		// add submission join id to the row number
		if (submission_record_join_id != null && !existingFormInstance) {
			String columnValue = submission_record_join_id.toString();

			if (columnValue != null && !columnValue.isEmpty()) {
				model.add(formInstance, DatasetRDF.PROPERTY_SJI,
						ResourceFactory.createTypedLiteral(columnValue, XSDDatatype.XSDlong));
			}
		}

		// Get RG
		RepeatableGroup rg = rgCache.get(tabInfo.getRepeatableGroupId());

		Resource rgInstance = availableRgs.get(getAvailableRgKey(submissionString, rg));

		// if rgInstance is available then we will use it
		if (rgInstance != null) {
			availableRgs.remove(getAvailableRgKey(submissionString, rg));
		} else { // otherwise, create a new rgInstance
			// Create RG Instance
			rgInstance = RepeatableGroupRDF.createRGResourceInstance(rg, rowNum);

			// connect form instance to the current rgInstance
			createRDFForInstancedRg(model, formInstance, rgInstance, rg);
		}

		// Iterate through columns and get instance data
		// Create RDF for rest of the columns
		for (DataStoreTabularColumnInfo columnInfo : tabInfo.getColumnInfos()) {
			createRDFForColumnInfo(model, columnInfo, row, rg, formInstance, rgInstance, mapElementCache, guidsFound);
		}
	}

	private void createRDFForColumnInfo(Model model, DataStoreTabularColumnInfo columnInfo, GenericTableRow row,
			RepeatableGroup rg, Resource formInstance, Resource rgInstance, Map<Long, DataElement> mapElementCache,
			Set<String> guidsFound) {
		// Table names are created lower cased. Since we are
		// using alias to reference
		// because of
		// potential spaces casing must match
		// Add the table name in the select statement to
		// distinguish between the same de's
		// in
		// different rg's
		String columnName = columnInfo.getColumnName().toLowerCase();

		// have to get De for Column (in another db!) so
		// hopefully only do this once
		if (columnInfo.getDataElement() == null) {

			Long mapElementId = columnInfo.getMapElementId();
			DataElement columnDE = mapElementCache.get(mapElementId);

			if (columnDE == null) {
				log.error("ERROR: MapElement ID: " + mapElementId + " not found in group, " + rg.getName()
						+ ", skipping...");
				throw new RuntimeException("ERROR: MapElement ID: " + mapElementId + " not found in group, "
						+ rg.getName() + ", skipping...");
			}

			columnInfo.setDataElement(columnDE.getStructuralObject());
		}

		// Ignore srj and subject columns because those were
		// done earlier (srj wont be in
		// tabularInfo so
		// dont need to exclude)
		// if (columnName != submissionRecordJoinColumnName)
		// && columnName !=
		// subjectColumnName)
		if (columnInfo.getDataElement() != null) {
			Object column = row.getValueByColumnName(columnName);
			String columnValue;

			// determine type of data element to
			// figure out xsd data type
			XSDDatatype dType;

			if (column != null) {
				columnValue = column.toString();
				dType = DataElementRDF.determineDataType(columnInfo.getDataElement());
			} else {
				columnValue = ServiceConstants.EMPTY_STRING;
				dType = XSDDatatype.XSDstring;
			}

			// create node for GUID
			if (DataType.GUID.equals(columnInfo.getDataElement().getType()) && columnValue != null
					&& !columnValue.isEmpty()) {
				model.add(formInstance, FormStructureRDF.PROPERTY_GUID, GuidRDF.createGuidResource(columnValue));

				if (!guidsFound.contains(columnValue)) {
					guidsFound.add(columnValue);
					model.add(GuidRDF.createGuidResource(columnValue), RDFS.label,
							ResourceFactory.createPlainLiteral(columnValue));
					model.add(GuidRDF.createGuidResource(columnValue), RDFS.subClassOf, GuidRDF.RESOURCE_GUID);
				}
			}

			// Need to format Date Time
			if (dType.equals(XSDDatatype.XSDdateTime)) {
				XSDDateTime xsdDT = stringToDate(columnValue);
				model.add(rgInstance, DataElementRDF.createDEProperty(columnInfo.getDataElement()),
						ResourceFactory.createTypedLiteral(xsdDT));
			} else {
				if (XSDDatatype.XSDstring.equals(dType)) {
					model.add(rgInstance, DataElementRDF.createDEProperty(columnInfo.getDataElement()),
							ResourceFactory.createPlainLiteral(columnValue));

				} else {
					model.add(rgInstance, DataElementRDF.createDEProperty(columnInfo.getDataElement()),
							ResourceFactory.createTypedLiteral(columnValue, dType));
				}
			}
		}
	}

	/*
	 * This method will no longer be used as part of the export procedure. DataStoreInfo is now generated and submitted
	 * during form submission.
	 */
	@Transactional
	public void createRDFForDSInfo(DataStoreInfo dsInfo, List<Long> privateSharedDatasetIds, Set<String> guidsFound) {
		log.info("Adding RDF for dsInfoID: " + dsInfo.getId());
		// Model model = ModelFactory.createDefaultModel();

		// hash map from the submission record join ID to it's form instance node
		Map<String, Resource> submissionToFormInstance = new HashMap<String, Resource>();
		Map<String, Resource> availableRgs = new HashMap<>();

		try {
			FormStructure form = fsCache.get(dsInfo.getDataStructureId());

			if (form == null) {
				form = formStructureDao.get(dsInfo.getDataStructureId());
			}

			// get the form name and version. We will use this later for logging output
			String formNameAndVersion = form.getShortNameAndVersion();

			log.info("Current FS: " + formNameAndVersion);

			// cache of map element id's to the data element object
			Map<Long, DataElement> mapElementCache = constructMapElementCache(rgCache.values());
			Map<String, RDFGenResourceBuilder> resourceBuilders = new HashMap<>();

			int rowNum = 1;
			long startTime = System.currentTimeMillis();

			for (DataStoreTabularInfo tabInfo : dsInfo.getDataStoreTabularInfos()) {
				long tripleCount = 0;

				Model model = null;
				try {
					model = ModelFactory.createDefaultModel();
					log.info("Generating Triples for: " + tabInfo.getTableName());
					long tabularStartTime = System.currentTimeMillis();

					GenericTable tableResult = repoDao.queryByDataStoreTabInfo(tabInfo, privateSharedDatasetIds);

					if (tableResult != null) {
						int chunkIndex = 0;

						for (GenericTableRow row : tableResult.getRows()) {
							createRDFForRow(model, tabInfo, row, form, submissionToFormInstance, availableRgs,
									mapElementCache, rowNum, guidsFound);
							rowNum++;
							chunkIndex++;

							// we will stagger the writes to storage by chunks
							if (chunkIndex >= INSTANCED_ROW_CHUNK_SIZE) {
								log.info("Writing chunk of size " + INSTANCED_ROW_CHUNK_SIZE + " to storage...");
								tripleCount += model.size();
								rdfFileWriter.writeToFile(model); // the write automatically closes the model
								model = ModelFactory.createDefaultModel();
								chunkIndex = 0;
							}
						}

						if (CHANGE_DIAGNOSIS_FORM.equalsIgnoreCase(form.getShortName())) {

							RDFGenResourceBuilder diagnosisChangeBuilder = resourceBuilders.get(CHANGE_DIAGNOSIS_FORM);

							if (diagnosisChangeBuilder == null) {
								diagnosisChangeBuilder = new DiagnosisChangeResourceBuilder();
								resourceBuilders.put(CHANGE_DIAGNOSIS_FORM, diagnosisChangeBuilder);
							}

							RepeatableGroup rg = rgCache.get(tabInfo.getRepeatableGroupId());
							log.info("Storing data for PDBPChangeDiagnosis to create change in diagnosis flags for "
									+ rg.getName());
							diagnosisChangeBuilder.putTableResult(rg.getName(), tableResult);
						}

						if (rdfGenConstants.getMdsUpdrsXName().equalsIgnoreCase(form.getShortName())) {
							RDFGenResourceBuilder mdsUpdrsXBuilder =
									resourceBuilders.get(rdfGenConstants.getMdsUpdrsXName());

							if (mdsUpdrsXBuilder == null) {
								mdsUpdrsXBuilder = new MdsUpdrsXResourceBuilder();
								resourceBuilders.put(rdfGenConstants.getMdsUpdrsXName(), mdsUpdrsXBuilder);
							}

							RepeatableGroup rg = rgCache.get(tabInfo.getRepeatableGroupId());
							log.info(
									"Storing data for MDS UPDRS X to create subject mapping flags for " + rg.getName());
							mdsUpdrsXBuilder.putTableResult(rg.getName(), tableResult);
						}
					}

					long tabularEndTime = System.currentTimeMillis();

					if (!model.isEmpty() && !model.isClosed()) {
						tripleCount += model.size();
						rdfFileWriter.writeToFile(model);
					}

					if (!model.isClosed()) {
						model.close();
					}

					log.info("Table: Generated " + tripleCount + " triples: " + (tabularEndTime - tabularStartTime)
							+ "ms");
				} catch (Exception e) {
					log.error(e.getMessage());
					e.printStackTrace(logStream);
					hasError = true;

					if (!model.isClosed()) {
						model.close();
					}
				}
			}

			long endTime = System.currentTimeMillis();
			// long tripleNum = model.size();
			log.info("Finished Generating all triples: " + (endTime - startTime) + "ms");

			// write post processed triples to the triples file
			for (Entry<String, RDFGenResourceBuilder> resourceBuilderEntry : resourceBuilders.entrySet()) {
				String formName = resourceBuilderEntry.getKey();
				RDFGenResourceBuilder builder = resourceBuilderEntry.getValue();

				log.info("Generating triples for: " + formName);
				Model model = builder.buildModel();
				rdfFileWriter.writeToFile(model);
			}
		} catch (Exception e) {
			// instead of failing fast here, we will write the stack trace to the log file
			log.error(e.getMessage());
			e.printStackTrace(logStream);
			hasError = true;
		}
	}

	public static XSDDateTime stringToDate(String date) {
		Calendar cal = Calendar.getInstance();
		Date d = BRICSTimeDateUtil.parseRepositoryDate(date);
		cal.setTime(d);
		XSDDateTime xsdDT = new XSDDateTime(cal);
		return xsdDT;
	}

	public String generateFilePath(String filePath) {

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		return filePath + "_" + dateFormat.format(startTime.getTime());

	}

	public Calendar getStartTime() {
		return startTime;
	}

	public synchronized void writeToLog(String message) {
		try {
			logStream.write(message.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

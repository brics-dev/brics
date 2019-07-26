package gov.nih.tbi.dictionary.portal;

import gov.nih.tbi.PortalConstants;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.EntityType;
import gov.nih.tbi.commons.model.PermissionType;
import gov.nih.tbi.commons.model.SeverityLevel;
import gov.nih.tbi.commons.service.SchemaMappingManager;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.commons.service.StaticReferenceManager;
import gov.nih.tbi.commons.service.UserPermissionException;
import gov.nih.tbi.commons.service.WebServiceManager;
import gov.nih.tbi.dictionary.model.SeverityRecord;
import gov.nih.tbi.dictionary.model.hibernate.DataElement;
import gov.nih.tbi.dictionary.model.hibernate.DictionaryEventLog;
import gov.nih.tbi.dictionary.model.hibernate.Schema;
import gov.nih.tbi.dictionary.model.hibernate.SchemaPv;
import gov.nih.tbi.dictionary.model.hibernate.ValueRange;
import gov.nih.tbi.dictionary.model.hibernate.eform.BasicEform;
import gov.nih.tbi.dictionary.portal.util.SchemaMappingUtil;
import gov.nih.tbi.dictionary.service.rulesengine.model.RulesEngineException;
import gov.nih.tbi.idt.ws.IdtInterface;
import gov.nih.tbi.idt.ws.InvalidColumnException;
import gov.nih.tbi.idt.ws.Struts2IdtInterface;
import gov.nih.tbi.portal.PortalUtils;
import gov.nih.tbi.taglib.datatableDecorators.DictionaryEventLogListIdtDecorator;
import gov.nih.tbi.taglib.datatableDecorators.SchemaPvListIdtDecorator;
import gov.nih.tbi.taglib.datatableDecorators.SupportDocIdtListDecorator;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.bytecode.opencsv.CSVReader;

public class SchemaMappingAction extends BaseDataElementSearchAction {

	private static final long serialVersionUID = 5381872918271110943L;

	static Logger logger = Logger.getLogger(SchemaMappingAction.class);

	@Autowired
	protected SchemaMappingManager schemaMappingManager;

	@Autowired
	protected StaticReferenceManager staticReferenceManager;

	@Autowired
	WebServiceManager webServiceManager;

	public String deShortName;
	public DataElement de;

	List<SchemaPv> schemaPvs;

	private File upload;

	private String uploadContentType;

	public String viewSchemaMappingValues() {

		schemaPvs = new ArrayList<SchemaPv>();
		deShortName = getRequest().getParameter(PortalConstants.DATAELEMENT);
		if (deShortName != null) {
			de = schemaMappingManager.getLatestDeByName(deShortName);
			schemaPvs = schemaMappingManager.getAllMappings(de);
		}
		return PortalConstants.ACTION_VIEW;
	}

	public String adminDataElementSchemaImport() {
		return PortalConstants.ACTION_IMPORT_DATA_ELEMENT_SCHEMA;
	}
	

	// url: http://fitbir-portal-local.cit.nih.gov:8080/portal/dictionary/schemaMappingAction!getSchemaPvList.action
	public String getSchemaPvList() {
		try {
			IdtInterface idt = new Struts2IdtInterface();
			schemaPvs = new ArrayList<SchemaPv>();
			if (deShortName != null) {
				de = schemaMappingManager.getLatestDeByName(deShortName);
				schemaPvs = schemaMappingManager.getAllMappings(de);
			}
			ArrayList<SchemaPv> outputList =
					new ArrayList<SchemaPv>(getSchemaPvs());
			idt.setList(outputList);
			idt.setTotalRecordCount(outputList.size());
			idt.setFilteredRecordCount(outputList.size());
			idt.decorate(new SchemaPvListIdtDecorator());
			idt.output();
		} catch (InvalidColumnException e) {
			logger.error("invalid column: " + e);
			e.printStackTrace();
		}
		return null;
	}

	// TODO: Refactor this method so it no longer makes dao calls every row
	public String adminUploadSchemaMapping() throws Exception {
		if (ServiceConstants.CSV_FILE.equalsIgnoreCase(uploadContentType)
				|| "text/csv".equalsIgnoreCase(uploadContentType)
				|| "application/csv".equalsIgnoreCase(uploadContentType)) {
			// file itself is in File upload
			CSVReader reader = null;

			try {
				reader = new CSVReader(new FileReader(upload));
				List<String[]> lines = reader.readAll();

				if (lines.size() > 0) {
					String[] headers = lines.get(0);
					SchemaMappingUtil schemaMappingUtil = new SchemaMappingUtil(schemaMappingManager, headers);

					List<String> headerErrors = schemaMappingUtil.validateImportHeaderGetErrors(headers);
					if (!headerErrors.isEmpty()) {
						for (String headerError : headerErrors) {
							addActionError(headerError);
						}
						return PortalConstants.ACTION_IMPORT_DATA_ELEMENT_SCHEMA;
					}

					// the headers could be out of order but still valid, so
					// handle that
					Map<String, Integer> headersMap = new HashMap<String, Integer>();
					for (int i = 0; i < headers.length; i++) {
						// I do toLowerCase here so we don't have to worry with
						// case-sensitive matching later
						// when we look for headers by string name
						headersMap.put(headers[i].toLowerCase().trim(), i);
					}

					// validate all rows
					List<String> validationErrors = this.validateSchemaFile(schemaMappingUtil, lines);
					for (String error : validationErrors) {
						addActionError(error);
					}

					if (!hasActionErrors()) {
						// load the rows
						for (int i = 1; i < lines.size(); i++) {
							String[] line = lines.get(i);

							String lineDePv = schemaMappingUtil.getValueFromMappingByHeader(line,
									ServiceConstants.SCHEMA_MAPPING_BRICS_DE_PV);
							String lineDeName = schemaMappingUtil.getValueFromMappingByHeader(line,
									ServiceConstants.SCHEMA_MAPPING_BRICS_DE_SHORTNAME);
							String lineSchemaDeId = schemaMappingUtil.getValueFromMappingByHeader(line,
									ServiceConstants.SCHEMA_MAPPING_SCHEMA_DE_ID);
							String lineSchemaPvId = schemaMappingUtil.getValueFromMappingByHeader(line,
									ServiceConstants.SCHEMA_MAPPING_SCHEMA_PV_ID);
							String lineSchemaDeName = schemaMappingUtil.getValueFromMappingByHeader(line,
									ServiceConstants.SCHEMA_MAPPING_SCHEMA_DE_NAME);
							String lineSchemaName = schemaMappingUtil.getValueFromMappingByHeader(line,
									ServiceConstants.SCHEMA_MAPPING_SCHEMA_SYSTEM);
							String lineSchemaPv = schemaMappingUtil.getValueFromMappingByHeader(line,
									ServiceConstants.SCHEMA_MAPPING_SCHEMA_PV_VALUE);

							// the above fail gracefully to a null, so check here
							// where we can handle it with messaging
							if (lineDeName == null || lineDeName.equals("")) {
								addActionError(
										"The Data Element variable name header was missing.  Please check your input file and try again.");
							} else if (lineSchemaDeId == null || lineSchemaDeId.equals("")) {
								addActionError(
										"The Schema Data Element ID header was missing.  Please check your input file and try again.");
							}
							// if schema PV ID or schema PV are null, no problem
							else {
								// does this mapping already exist? If so, replace it
								DataElement de = getLatestDeByName(lineDeName);

								if (de != null) {

									// check if schema ID matches. If not, update the DE (including version update)
									// then get the new DE and run below
									de = updateDeSchemaId(de, lineSchemaName, lineSchemaDeId);

									SchemaPv pv = new SchemaPv();
									SchemaPv alreadyMappedSchemaPv =
											schemaMappingManager.getSchemaMapping(de, lineSchemaName, lineDePv);
									if (alreadyMappedSchemaPv != null) {
										pv = alreadyMappedSchemaPv;
									}

									pv.setDataElement(de.getStructuralObject());
									pv.setPermissibleValue(lineSchemaPv);
									pv.setSchemaDeId(lineSchemaDeId);
									pv.setSchemaPvId(lineSchemaPvId);
									pv.setSchemaDataElementName(lineSchemaDeName);

									// get and add schema value
									Schema schema = staticReferenceManager
											.getSchemaByName(schemaMappingUtil.getValueFromMappingByHeader(line,
													ServiceConstants.SCHEMA_MAPPING_SCHEMA_SYSTEM));

									ValueRange vr = schemaMappingManager.getValueRangeByDeAndPv(de, lineDePv);

									pv.setSchema(schema);
									pv.setValueRange(vr);
									pv = schemaMappingManager.saveSchemaPv(pv);
								} else {
									addActionError("Could not find the data element with name " + lineDeName);
								}
							}
						}
					}
				}
			} catch (Exception e) {
				logger.error("Username: " + getAccount().getUserName()
						+ ".  Exception occurred while performing a schema PV mapping");
				e.printStackTrace();
				addActionError(
						"Creating the mapping failed because of an error.  Please check your input file and try again.");
			} finally {
				try {
					if (reader != null)
						reader.close();
				} catch (IOException e) {
					logger.error("Username: " + getAccount().getUserName() + ".  Failed to close CSVReader");
					throw new IOException(e); // cant handle this, throw up
				}
			}
		}

		else {
			// File uploaded is not a CSV
			addActionError(
					"Invalid file type. If the file is open in another application please close it and try again.");
		}

		// if there are no errors, send up a success
		if (!this.hasActionErrors()) {
			this.addActionMessage("The Data Element Mapping has been uploaded successfully");
		}
		return PortalConstants.ACTION_IMPORT_DATA_ELEMENT_SCHEMA;
	}

	private List<String> validateSchemaFile(SchemaMappingUtil schemaMappingUtil, List<String[]> lines) {
		List<String> validationErrors = new ArrayList<String>();
		for (int i = 1; i < lines.size(); i++) {
			String[] line = lines.get(i);
			validationErrors.addAll(schemaMappingUtil.validateImportDataRow(line));
			if (validationErrors.size() > 20) {
				validationErrors.add(
						"More than 20 errors exist in your input document.  Please correct the above errors before continuing");
				break;
			}
		}
		return validationErrors;
	}

	private DataElement getLatestDeByName(String name) {
		de = schemaMappingManager.getLatestDeByName(name);
		return de;
	}

	private DataElement updateDeSchemaId(DataElement workingDe, String lineSchemaName, String lineSchemaDeId) {
		String schemaSystemId = schemaMappingManager.getDeSchemaSystemId(workingDe, lineSchemaName);
		// we don't want to do any of this if the IDs are equal.
		if (schemaSystemId == null || !schemaSystemId.equals(lineSchemaDeId)) {
			// the schema system IDs are not the same, so we have to run the "edit DE" process
			List<String> errors = new ArrayList<String>();
			List<String> warnings = new ArrayList<String>();

			String auditNote = "Updated automatically via Schema Mapping ID upload";

			String[] twoProxyTickets =
					PortalUtils.getMultipleProxyTickets(modulesConstants.getModulesAccountURL(getDiseaseId()), 2);

			// update de to contain new mapping. the update function here adds if not found
			Schema schema = staticManager.getSchemaByName(lineSchemaName);
			workingDe.updateExternalId(schema, lineSchemaDeId);

			SeverityLevel severityLevel = null;
			List<SeverityRecord> changeSeverities = new ArrayList<SeverityRecord>();

			DataElementStatus elementStatus = workingDe.getStatus();

			// check if published. If not, just save changes later
			// if awaiting publication, revert back to draft
			// if published, check rules engine (see below)
			if (DataElementStatus.DRAFT.equals(elementStatus)) {
				try {
					removeFromPublicGroup(workingDe);
				} catch (Exception e) {
					logger.error("Failed removing the data element from the public group");
					e.printStackTrace();
				}
				// it's draft, just save
				workingDe = dictionaryManager.saveDataElementUpdate(workingDe);
			} else if (DataElementStatus.AWAITING.equals(elementStatus)) {
				// it's awaiting, revert to draft then just save
				workingDe.setStatus(DataElementStatus.DRAFT);
				workingDe = dictionaryManager.saveDataElementUpdate(workingDe);
			} else {
				// it's published, deprecated, etc
				// get new copy of de (without change) from db
				DataElement originalDe = schemaMappingManager.getLatestDeByName(workingDe.getName());

				// compare using dictionaryService.evaluateDataElementChangeSeverity
				try {
					changeSeverities = dictionaryService.evaluateDataElementChangeSeverity(originalDe, workingDe);
					if (changeSeverities.size() > 0) {
						severityLevel = changeSeverities.get(0).getSeverityLevel();
					}

				} catch (RulesEngineException e) {
					logger.error("The rules engine failed to check the severity of the data element change");
					e.printStackTrace();
				}

				// save
				// only save if there was an actual change - it is possible for the DE to have an external mapping
				// but no pv schema mappings to search above but serverityLevel = null means no change
				if (severityLevel != null) {
					try {
						workingDe = dictionaryManager.saveDataElement(getAccount(), workingDe, errors, warnings,
								severityLevel, twoProxyTickets, null, false);

						// if the data element is not a draft update the map element references to point to the new DE
						// version
						dictionaryManager.updateFormStructuresWithLatestDataElement(workingDe.getName(),
								workingDe.getId());

						// If we just created a new de though versioning, then save it as public.
						if (DataElementStatus.PUBLISHED.equals(workingDe.getStatus())
								&& (SeverityLevel.MAJOR.equals(severityLevel)
										|| SeverityLevel.MINOR.equals(severityLevel))) {
							try {
								addToPublicGroup(workingDe);
							} catch (Exception e) {
								logger.error("Failed adding the new data element version to the public group");
								e.printStackTrace();
							}
						}

						// Save the audit note (if there is a note or not is controlled by jsp logic)
						if (auditNote != null && !auditNote.isEmpty()) {

							// save event
							DictionaryEventLog eventLog =
									new DictionaryEventLog(getDiseaseId(), getAccount().getUserId());
							Long originalEntityId =
									dictionaryService.getOriginalDataElementIdByName(workingDe.getName());
							eventLog.setDataElementID(originalEntityId);

							dictionaryService.saveChangeHistoryEventlog(eventLog, changeSeverities, severityLevel,
									EntityType.DATA_ELEMENT, originalDe.getId(), workingDe.getId(), auditNote);
						}
					} catch (Exception e) {
						logger.error("Failed saving the new version of the data element");
						e.printStackTrace();

						// since we are handling this here, the rest of the process will continue.
					}

					workingDe = getLatestDeByName(workingDe.getName());
				}
			}
		}

		return workingDe;
	}
	
	private void removeFromPublicGroup(DataElement dataElement) throws HttpException, IOException {

		webServiceManager.unregisterEntityToPermissionGroup(ServiceConstants.PUBLIC_DATA_ELEMENTS,
				EntityType.DATA_ELEMENT, dataElement.getId(), PermissionType.READ);
	}

	private void addToPublicGroup(DataElement dataElement) throws HttpException, IOException {

		webServiceManager.registerEntityToPermissionGroup(getAccount(), ServiceConstants.PUBLIC_DATA_ELEMENTS,
				EntityType.DATA_ELEMENT, dataElement.getId(), PermissionType.READ);
	}

	public String getDeShortName() {
		return deShortName;
	}

	public void setDeShortName(String deShortName) {
		this.deShortName = deShortName;
	}

	public List<SchemaPv> getSchemaPvs() {
		return schemaPvs;
	}

	public void setSchemaPvs(List<SchemaPv> schemaPvs) {
		this.schemaPvs = schemaPvs;
	}

	public DataElement getDe() {
		return de;
	}

	public void setDe(DataElement de) {
		this.de = de;
	}

	public File getUpload() {
		return upload;
	}

	public void setUpload(File upload) {
		this.upload = upload;
	}

	public String getUploadContentType() {
		return uploadContentType;
	}

	public void setUploadContentType(String uploadContentType) {
		this.uploadContentType = uploadContentType;
	}

}

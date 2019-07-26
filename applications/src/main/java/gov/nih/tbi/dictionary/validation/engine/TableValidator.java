package gov.nih.tbi.dictionary.validation.engine;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.xml.ws.WebServiceException;

import org.apache.log4j.Logger;

import gov.nih.tbi.ApplicationsConstants;
import gov.nih.tbi.ModelConstants;
import gov.nih.tbi.commons.model.DataElementStatus;
import gov.nih.tbi.commons.model.DataType;
import gov.nih.tbi.commons.model.InputRestrictions;
import gov.nih.tbi.commons.model.RepeatableType;
import gov.nih.tbi.commons.model.RequiredType;
import gov.nih.tbi.commons.service.ServiceConstants;
import gov.nih.tbi.dictionary.model.Translations;
import gov.nih.tbi.dictionary.model.hibernate.MapElement;
import gov.nih.tbi.dictionary.model.hibernate.RepeatableGroup;
import gov.nih.tbi.dictionary.model.hibernate.StructuralDataElement;
import gov.nih.tbi.dictionary.model.hibernate.StructuralFormStructure;
import gov.nih.tbi.dictionary.validation.model.DataStructureTable;
import gov.nih.tbi.dictionary.validation.model.FileNode;
import gov.nih.tbi.dictionary.validation.model.FileNode.FileType;
import gov.nih.tbi.dictionary.validation.model.RepeatableGroupTable;
import gov.nih.tbi.dictionary.validation.model.ValidationOutput;
import gov.nih.tbi.dictionary.validation.model.ValidationOutput.OutputType;
import gov.nih.tbi.dictionary.validation.view.ValidationClient;
import gov.nih.tbi.dictionary.ws.validation.ConditionalValidator;
import gov.nih.tbi.dictionary.ws.validation.DictionaryAccessor;
import gov.nih.tbi.dictionary.ws.validation.Normalizer;
import gov.nih.tbi.dictionary.ws.validation.RangeValidator;
import gov.nih.tbi.dictionary.ws.validation.TypeValidator;
import gov.nih.tbi.dictionary.ws.validation.ValidationConstants;
import gov.nih.tbi.dictionary.ws.validation.ValidationUtil;
import gov.nih.tbi.dictionary.ws.validation.XmlCharactersValidator;
import gov.nih.tbi.repository.ws.AccessionWebService;
import gov.nih.tbi.repository.ws.model.Accession;
import gov.nih.tbi.repository.ws.model.AccessionReturnType;

public class TableValidator extends DictionaryAccessor implements ConditionalValidator, Callable<FileNode> {

	private TypeValidator typer;
	private RangeValidator ranger;
	private Normalizer normer;
	private XmlCharactersValidator xmlValidator;
	private static Translations translations;
	private DataStructureTable table;
	private HashMap<String, HashSet<String>> references;
	private FileNode node;
	static Logger logger = Logger.getLogger(TableValidator.class);
	private AccessionWebService accClient;
	private final static int GUID_CHUNK_SIZE = 2500;
	private static HashMap<String, Boolean> extraValidation;
	

	public TableValidator(List<StructuralFormStructure> dictionary, DataStructureTable table,
			HashMap<String, HashSet<String>> references, FileNode node, AccessionWebService provider) {

		super.dictionary = dictionary;
		typer = new TypeValidator(dictionary);
		ranger = new RangeValidator(dictionary);
		xmlValidator = new XmlCharactersValidator();

		setTable(table, references, node);

		accClient = provider;
	}

	/**
	 * Sets the translation rule to use
	 * 
	 * @param translations
	 */
	public static void setTranslationRule(Translations translations) {

		TableValidator.translations = translations;
	}
	
	public static void setExtraValidation(HashMap<String, Boolean> extraValidation) {
		TableValidator.extraValidation = extraValidation;
	}
	
	
	/**
	 * Returns the translation rule currently being used to validate
	 * 
	 * @return
	 */
	public static Translations getTranslationRule() {

		return translations;
	}

	// These are theoretically reusable, single threaded approach would reuse one
	public void setTable(DataStructureTable table, HashMap<String, HashSet<String>> references, FileNode node) {

		this.table = table;
		this.references = references;
		this.node = node;
	}

	public String getConstraintType(String rowRef) throws RuntimeException {

		String[] split = rowRef.substring(1).split("\\" + ValidationConstants.VALUE_REFERENCE_DIVIDER);
		MapElement current = getElement(split[0], split[1]);

		return current.getStructuralDataElement().getType().name();
	}

	public void validate() {
		// This set will be used to validate duplicate file names if applicable.
		Set<String> fileNames = new HashSet<String>();

		normer = new Normalizer(dictionary, ranger, translations);

		if (FileType.TRANSLATION_RULE.equals(node.getType())) {
			if (normer.validate() != null) {
				table.addOutput(new ValidationOutput(table, OutputType.ERROR, -1, -1, "The form structure "
						+ normer.validate() + " referenced in the translation rule does not exist in your data."));
			}
		} else {
			try {

				StructuralFormStructure structure = table.getStructure();
				Vector<MapElement> elements = new Vector<MapElement>(); // populate with only the
																		// Elements that are present
																		// in this table

				HashMap<MapElement, List<Accession>> accessions = new HashMap<MapElement, List<Accession>>();// element
																												// to
																												// column
																												// data

				for (MapElement me : table.getStructure().getDataElements()) {
					// AbstractDataElement element = DictionaryAccessor.getDataElementByName( structure, elementName,
					// null );
					elements.add(me);
					if (DataType.GUID.equals(me.getStructuralDataElement().getType())) {
						accessions.put(me, new ArrayList<Accession>());
					}
				}

				// HashMap<String, String> elementValues = new HashMap<String, String>(); // element name to row data

				// Find the main repeatable group. This is important for conditionally required elements.
				RepeatableGroupTable mainTable = null;
				for (int search = 0; search < table.getColumnCount(); search++) {
					RepeatableGroupTable rgTable = table.getRepeatableGroupTable(search);

					if (ModelConstants.MAIN_STRING.equalsIgnoreCase(rgTable.getRepeatableGroupName())) {
						mainTable = rgTable;
					}
				}

				// subject_row_id = current row (a single subject); repeatable_group_id = current column( type of
				// repeatable repeatable group) in data structure table
				
				Vector<Vector<Vector<String>>> columnNameVector = new Vector<Vector<Vector<String>>>();
				Vector<Vector<Vector<String>>> dataVector = new Vector<Vector<Vector<String>>>();
				for (int subject_row_id = 0; subject_row_id < table.getRowCount(); subject_row_id++) {
					columnNameVector.clear();
					dataVector.clear();
					// for each repeatable group
					for (int repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
						columnNameVector.add(new Vector<Vector<String>>());
						dataVector.add(new Vector<Vector<String>>());
						RepeatableGroupTable rgTable = table.getRepeatableGroupTable(repeatable_group_id);
						RepeatableGroup rg = rgTable.getRepeatableGroup();
						ArrayList<Integer> rgEntries =
								table.getAllReferences(subject_row_id, repeatable_group_id, null);

						validateRepeatableGroupCount(subject_row_id, repeatable_group_id, rg, rgEntries);

						// group_row_index = iteration over all the rgs in a single cell on the DS table;
						// group_column_index = column position of the elements in the rg
						
						for (int group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
							columnNameVector.get(repeatable_group_id).add(new Vector<String>());
							dataVector.get(repeatable_group_id).add(new Vector<String>());
							for (int group_column_index = 0; group_column_index < rgTable.getColumnCount(); group_column_index++) {
								MapElement mapElement = rgTable.getElementMapping(group_column_index);

								if (mapElement.getStructuralDataElement().getStatus().equals(DataElementStatus.RETIRED)) {
									table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, rgEntries
											.get(group_row_index), group_column_index, structure.getShortName()
											+ ServiceConstants.RETIRED_DE_IN_FS_MSG
											+ mapElement.getStructuralDataElement().getName()));
								}

								// run the raw data through the translation rule
								/*
								 * String data = normer.normalize(structure, mapElement.getStructuralDataElement(),
								 * (String) rgTable.getValueAt(rgEntries.get(group_row_index),
								 * group_column_index)).trim();
								 */
								String data =
										normer.normalize(structure, mapElement.getStructuralDataElement(),
												(String) rgTable.getValueAt(rgEntries.get(group_row_index),
														group_column_index));
								if (data != null) {
									data = data.trim();
								}
				
								String currentColumnName = rgTable.getColumnName(group_column_index);
								columnNameVector.get(repeatable_group_id).get(group_row_index).add(currentColumnName);
								dataVector.get(repeatable_group_id).get(group_row_index).add(data);
						         
								

								DataType dataElementType = mapElement.getStructuralDataElement().getType();
								// Mess around with the slashes in the file path
								if ((DataType.FILE.equals(dataElementType)
										|| DataType.THUMBNAIL.equals(dataElementType) || DataType.TRIPLANAR
											.equals(dataElementType)) && data != null && !data.isEmpty()) {

									// make sure to remove the absolute path
									FileNode parent = (FileNode) node.getParent();
									if (data.toLowerCase().startsWith(parent.getConicalPath().toLowerCase())) {
										data = data.substring(parent.getConicalPath().length());
									}

									if (!data.startsWith("\\") && !data.startsWith("/")) {
										data = "/" + data;
									}
									data = data.replace("\\", "/");

								}

								// flag used by conditionally required to denote required status without having to
								// change map element states
								boolean required = false;

								// Check to make sure that the data isn't empty if it is required
								if ((required || RequiredType.REQUIRED.equals(mapElement.getRequiredType()))
										&& (data == null || data.isEmpty())) {
									String message =
											String.format(ApplicationsConstants.LOC_ROW_COLUMN_NAME,
													getRawDataRow(subject_row_id, group_row_index),
													rgTable.getDataFilePositionMapping(group_column_index),
													rgTable.getRepeatableGroupName(),
													rgTable.getColumnName(group_column_index));

									message =
											message
													+ String.format(ApplicationsConstants.ERR_COLUMN_REQUIRED,
															table.getStructureName());

									table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, rgEntries
											.get(group_row_index), group_column_index, message));
									required = false;
								}

								// if the data is empty, and it is recommended, add a warning
								if (RequiredType.RECOMMENDED.equals(mapElement.getRequiredType())
										&& (data == null || data.isEmpty())) {

									String message =
											String.format(ApplicationsConstants.LOC_ROW_COLUMN_NAME,
													getRawDataRow(subject_row_id, group_row_index),
													rgTable.getDataFilePositionMapping(group_column_index),
													rgTable.getRepeatableGroupName(),
													rgTable.getColumnName(group_column_index));

									message =
											message
													+ String.format(ApplicationsConstants.ERR_COLUMN_RECOMMENDED,
															table.getStructureName());

									table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, rgEntries
											.get(group_row_index), group_column_index, message));
								}

								// Validate contents of cell
								if (data != null && !data.isEmpty()) {

									// validate type
									if (!typer.validate(mapElement, data)) {
										String message =
												String.format(ApplicationsConstants.LOC_DATA_ROW_COLUMN_NAME, data,
														getRawDataRow(subject_row_id, group_row_index),
														rgTable.getDataFilePositionMapping(group_column_index),
														rgTable.getRepeatableGroupName(),
														rgTable.getColumnName(group_column_index));

										message =
												message
														+ String.format(ApplicationsConstants.ERR_TYPE_INCORRECT,
																mapElement.getStructuralDataElement().getType()
																		.getValue());

										table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, rgEntries
												.get(group_row_index), group_column_index, message));

										//validate if data in value range contains duplicates
									} else if (ranger.hasDuplicates(data).size() > 0){
										String message =
												String.format(ApplicationsConstants.LOC_DATA_ROW_COLUMN_NAME, data,
														getRawDataRow(subject_row_id, group_row_index),
														rgTable.getDataFilePositionMapping(group_column_index),
														rgTable.getRepeatableGroupName(),
														rgTable.getColumnName(group_column_index));
										message = message + String.format(ApplicationsConstants.ERR_DUPLICATE_RANGE_VALUE, 
																ranger.hasDuplicates(data).toString());
										
										table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, rgEntries
												.get(group_row_index), group_column_index, message));
										
										// validate range
									} else if (!ranger.validate(mapElement, data)) {
										String message =
												String.format(ApplicationsConstants.LOC_DATA_ROW_COLUMN_NAME, data,
														getRawDataRow(subject_row_id, group_row_index),
														rgTable.getDataFilePositionMapping(group_column_index),
														rgTable.getRepeatableGroupName(),
														rgTable.getColumnName(group_column_index));
										if (DataType.DATE.equals(dataElementType)) {
											message = message + String.format(ApplicationsConstants.ERR_RANGE_DATE);
										} else {
											message =
													message
															+ String.format(ApplicationsConstants.ERR_RANGE_INCORRECT,
																	mapElement.getStructuralDataElement()
																			.displayValueRange());
										}
										table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, rgEntries
												.get(group_row_index), group_column_index, message));

									} else if (!xmlValidator.validate(mapElement, data)) {
										String message =
												String.format(ApplicationsConstants.LOC_DATA_ROW_COLUMN_NAME, data,
														getRawDataRow(subject_row_id, group_row_index),
														rgTable.getDataFilePositionMapping(group_column_index),
														rgTable.getRepeatableGroupName(),
														rgTable.getColumnName(group_column_index));

										message = message + String.format(ApplicationsConstants.ERR_INVALID_CHAR);

										table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, rgEntries
												.get(group_row_index), group_column_index, message));

									} else {

										// evaluateConditional( elementValues,
										// name,
										// type,
										// data,
										// loc,
										// i );

										if (DataType.ALPHANUMERIC.equals(dataElementType)) {

											validateAlphanumeric(subject_row_id, rgTable, rgEntries, group_row_index,
													group_column_index, mapElement.getStructuralDataElement(), data);

										} else if (DataType.BIOSAMPLE.equals(dataElementType)) {
											validateBiosample(subject_row_id, rgTable, rgEntries, group_row_index,
													group_column_index, mapElement.getStructuralDataElement(), data);
										} else if (DataType.NUMERIC.equals(dataElementType)) {
											validateInputRestrictions(subject_row_id, rgTable, rgEntries,
													group_row_index, group_column_index,
													mapElement.getStructuralDataElement(), data);

										} else if (DataType.FILE.equals(dataElementType)
												|| DataType.THUMBNAIL.equals(dataElementType)
												|| DataType.TRIPLANAR.equals(dataElementType)) {
											validateFile(subject_row_id, rgTable, rgEntries, group_row_index,
													group_column_index, mapElement.getStructuralDataElement(), data,
													fileNames);
										}

										// Accession type - populate a list of accessions per column
										if (DataType.GUID.equals(mapElement.getStructuralDataElement().getType())
												&& (accClient != null)) {
											accessions.get(mapElement).add(new Accession(data));
										}
									}// end of valid type/range else statement
								}// end if(!data.isEmpty())
							}// end group_column_index
						}// end group_row_index
					}// end repeatable_group_id 
					validateCalculation(structure, columnNameVector, dataVector,
								subject_row_id, table);
				}// end subject_row_id

				// For each column that is an accession pass to the provider and find failures
				for (MapElement mapElement : accessions.keySet()) {
					// cast element as a ME to get the RG name and pass it along to the validation method
					String elementRGName = mapElement.getRepeatableGroup().getName();

					String name = mapElement.getStructuralDataElement().getName();
					String type = mapElement.getStructuralDataElement().getType().name();

					// logger.info("This is the name of the map element being processed " + name);

					if (!accessions.get(mapElement).isEmpty()) {
						validateAccessions(accessions, mapElement, name, type, elementRGName);

					}

				}
			}// end try
			catch (Exception e) {
				e.printStackTrace();
				String message =
						"An unknown exception has been encountered preventing validation from completing. Please submit a ticket regarding this file.";
				table.addOutput(new ValidationOutput(table, OutputType.ERROR, -1, -1, message));
			}
		}
	}
	
	private void validateCalculation(StructuralFormStructure structure, 
			Vector<Vector<Vector<String>>> columnNameVector, 
			Vector<Vector<Vector<String>>> dataVector,
			int subject_row_id, DataStructureTable table) throws Exception {
		// Temporarily comment out validateAccessions to disable GUID checking.
		// Note that the repeatable_group_id may not occur in the same
		// order as in the source file.  This must be taken into account
		// in validatePSQI where calculations require a visit date, in
		// validateCOWAT, and in validateAUDITC where the components and
		// sum are in different repeatable_group_id.
	    String shortName = structure.getShortName();
	    //System.out.println("shortName = " + shortName);
	    if ((extraValidation != null) && 
	    		(extraValidation.get(shortName) != null) && extraValidation.get(shortName)) {
	    	if (shortName.equalsIgnoreCase("AUDITC")) {
				validateAUDITC(structure, columnNameVector, dataVector, subject_row_id, 
						table);
			}
	    	else if (shortName.equalsIgnoreCase("AUDIT_FITBIR")) {
				validateAUDIT(structure, columnNameVector, dataVector, subject_row_id, 
						table);
			}
	    	else if (shortName.equalsIgnoreCase("BESS")) {
				validateBESS(structure, columnNameVector, dataVector, subject_row_id, 
						table);
			}
	    	else if (shortName.equalsIgnoreCase("BDI2")) {
				validateBDI2(structure, columnNameVector, dataVector, subject_row_id, 
						table);
			}
			else if (shortName.equalsIgnoreCase("BSI18")) {
				validateBSI18(structure, columnNameVector, dataVector, subject_row_id,
						table);
			}
			else if (shortName.equalsIgnoreCase("COWAT")) {
				validateCOWAT(structure, columnNameVector, dataVector, subject_row_id,
						table);
			}
			else if (shortName.equalsIgnoreCase("DHI")) {
				validateDHI(structure, columnNameVector, dataVector, subject_row_id,
						table);
			}
			else if (shortName.equalsIgnoreCase("FIM_Instrument")) {
				validateFIM(structure, columnNameVector, dataVector, subject_row_id,
						table);
			}
			else if (shortName.equalsIgnoreCase("GCS")) {
				validateGCS(structure, columnNameVector, dataVector, subject_row_id,
					    table);
			}
			else if (shortName.equalsIgnoreCase("GOSE_Standard")) {
				validateGOSE_Standard(structure, columnNameVector, dataVector, subject_row_id,
						table);
			}
			else if (shortName.equalsIgnoreCase("HIT6")) {
				validateHIT6(structure, columnNameVector, dataVector, subject_row_id,
						table);
			}
			else if (shortName.equalsIgnoreCase("MDS_UPDRS")) {
				validateMDS_UPDRS(structure, columnNameVector, dataVector, subject_row_id,
						table);
			}
			else if (shortName.equalsIgnoreCase("MoCA")) {
				validateMoCA(structure, columnNameVector, dataVector, subject_row_id,
						table);
			}
			else if (shortName.equalsIgnoreCase("NSI1")) {
				validateNSI1(structure, columnNameVector, dataVector, subject_row_id,
						table);
			}
			else if (shortName.equalsIgnoreCase("PHQ9")) {
				validatePHQ9(structure, columnNameVector, dataVector, subject_row_id,
						table);
			}
			else if (shortName.equalsIgnoreCase("PSQI")) {
				validatePSQI(structure, columnNameVector, dataVector, subject_row_id,
						table);
			}
			else if (shortName.equalsIgnoreCase("PCLC_Standard")) {
				validatePCLC_Standard(structure, columnNameVector, dataVector, subject_row_id,
						table);
			}
			else if (structure.getShortName().equalsIgnoreCase("Rivermead")) {
				validateRivermead(structure, columnNameVector, dataVector, subject_row_id,
						table);
			}
			else if (structure.getShortName().equalsIgnoreCase("SCAT3")) {
				validateSCAT3(structure, columnNameVector, dataVector, subject_row_id,
						table);
			}
			else if (structure.getShortName().equalsIgnoreCase("SF12")) {
				validateSF12(structure, columnNameVector, dataVector, subject_row_id,
						table);
			}
			else if (structure.getShortName().equalsIgnoreCase("SF36v2")) {
				validateSF36v2(structure, columnNameVector, dataVector, subject_row_id,
						table);
			}
			else if (structure.getShortName().equalsIgnoreCase("SWLS_CDISC_FITBIR")) {
				validateSWLS(structure, columnNameVector, dataVector, subject_row_id,
						table);
			}
			else if (structure.getShortName().equalsIgnoreCase("TMT_Standard")) {
				validateTMT(structure, columnNameVector, dataVector, subject_row_id,
						table);
			}
	    }
	}

	
	private void validateAUDITC(StructuralFormStructure structure,
			Vector<Vector<Vector<String>>> columnNameVector3, 
			Vector<Vector<Vector<String>>>dataVector3, int subject_row_id,
			DataStructureTable table) throws Exception {
		// table.getColumnCount = 4
		// repeatable_group_id = 0
		// rgEntries.size() = 1
		// group_row_index = 0
		// columnNameVector.size() = 9
		// group_column_index = 0 group_column_name = guid data = TBIMB932RL7
		// group_column_index = 1 group_column_name = subjectidnum data = 1234
		// group_column_index = 2 group_column_name = ageyrs data = 56
		// group_column_index = 3 group_column_name = vitstatus data = Alive
		// group_column_index = 4 group_column_name = visitdate data = 22-Apr-51
		// group_column_index = 5 group_column_name = sitename data = Gqgjrs
		// group_column_index = 6 group_column_name = dayssincebaseline data = 0
		// group_column_index = 7 group_column_name = casecontrlind data = Case
		// group_column_index = 8 group_column_name = generalnotestxt data = male
		// repeatable_group_id = 1
		// rgEntries.size() = 1
		// group_row_index = 0
		// columnNameVector.size() = 3
		// group_column_index = 0 group_column_name = auditdrnkcontainalcfreqscore data = 0
		// group_column_index = 1 group_column_name = auditalcdrnktypcldaynumscore data = 2
		// group_column_index = 2 group_column_name = auditmorethan6alcdrnkfreqscore data = 1
		// repeatable_group_id = 2
		// rgEntries.size() = 1
		// group_row_index = 0
		// columnNameVector.size() = 1
		// group_column_index = 0 group_column_name = auditctotalscore data = 3
		// repeatable_group_id = 3
		// rgEntries.size() = 1
		// group_row_index = 0
		// columnNameVector.size() = 4
		// group_column_index = 0 group_column_name = contexttype data = Last month
		// group_column_index = 1 group_column_name = contexttypeoth data = null
		// group_column_index = 2 group_column_name = datasource data = null
		// group_column_index = 3 group_column_name = datasourceoth data = null
				
		
		Vector<Vector<String>> columnNameVector2;
        Vector<Vector<String>> dataVector2;
        Vector<String> columnNameVector;
        Vector<String> dataVector;
        int i;
        String message;
        String data;
        Vector<String> bufColumnNameVector = new Vector<String>();
        Vector<String> bufDataVector = new Vector<String>();
        Vector<Integer> bufRepeatable_group_id = new Vector<Integer>();
        Vector<Integer> bufGroup_row_index = new Vector<Integer>();
        Vector<Integer> bufIndex = new Vector<Integer>();
        String guid = null;
        int group_row_index = -1;
        int repeatable_group_id;
        RepeatableGroupTable rgTable = null;
        ArrayList<Integer> rgEntries = null;
        int index = -1;
        
		 //System.out.println("table.getColumnCount = " + table.getColumnCount());
        	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
        		 
        		//System.out.println("repeatable_group_id = " + repeatable_group_id);
        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
        		dataVector2 = dataVector3.get(repeatable_group_id);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				//System.out.println("rgTable.getSize() = " + rgTable.getSize());
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				//System.out.println("rgEntries.size() = " + rgEntries.size());
				for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
        	 //System.out.println("group_row_index = " + group_row_index);
			columnNameVector = columnNameVector2.get(group_row_index);
			dataVector = dataVector2.get(group_row_index);
			 //System.out.println("columnNameVector.size() = " + columnNameVector.size());
			 //for (i = 0; i < columnNameVector.size(); i++) {
			    //System.out.println("group_column_index = " + i +
			    	//" group_column_name = " + columnNameVector.get(i) +
			    	//" data = " + dataVector.get(i)) ;	
			 //}
			for (i = 0; i < columnNameVector.size(); i++) {
				bufColumnNameVector.add(columnNameVector.get(i));
				bufDataVector.add(dataVector.get(i));
				bufRepeatable_group_id.add(repeatable_group_id);
				bufGroup_row_index.add(group_row_index);
				bufIndex.add(i);
			}
			} // for (int group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++)
            } // for (int repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++)
        for (i = 0; i < bufColumnNameVector.size(); i++) {
        	if (bufColumnNameVector.get(i).equalsIgnoreCase("guid")) {
				guid = bufDataVector.get(i);
				break;
			}	
        } // for (i = 0; i < bufColumnNameVector.size(); i++)
        
	    
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("ageyrs")) {
				String ageData = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((ageData == null) || (ageData.isEmpty()) || (ageData.trim() == null) ||
						(ageData.trim().isEmpty())) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
				}	
				else {
					double age = Double.valueOf(ageData).doubleValue();
					if (age < 18) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.WARNING_INCORRECT_AGE,
										  ageData, "18 or more years");
						table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
					}
				}
				break;
			  } // if (bufColumnNameVector.get(i).equalsIgnoreCase("ageyrs"))
			} // for (i = 0; i < bufColumnNameVector.size(); i++)
			
			int numberScoresMissing = 0;
		    Vector<Integer>missingIndices = new Vector<Integer>();
		    int missing_repeatable_group_id = -1;
		    int missing_group_row_index = -1;
            int calculatedSum = 0;
            
            int numberScoresNotInteger = 0;
            Vector<Integer>notIntegerIndices = new Vector<Integer>();
            int notInteger_repeatable_group_id = -1;
            int notInteger_group_row_index = -1;
            Vector<String>notIntegerData = new Vector<String>();
		    
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
				if ((bufColumnNameVector.get(i).equalsIgnoreCase("auditdrnkcontainalcfreqscore")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("auditalcdrnktypcldaynumscore")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("auditmorethan6alcdrnkfreqscore"))) {
					data = bufDataVector.get(i);
					if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
					    numberScoresMissing++;
					    missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
					}
					else {
						try {
					        calculatedSum += Integer.valueOf(data).intValue();
						}
						catch (NumberFormatException e) {
							numberScoresNotInteger++;
						    notIntegerIndices.add(bufIndex.get(i));
						    notInteger_repeatable_group_id = bufRepeatable_group_id.get(i);
							notInteger_group_row_index = bufGroup_row_index.get(i);
							notIntegerData.add(data);
						}
					}
				}
			}
		    
		    if (numberScoresMissing >= 1) {
		    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						missing_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresMissing; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, missing_group_row_index),
									rgTable.getDataFilePositionMapping(missingIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(missingIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index),
							rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
		    	}
		    } // if (numberScoresMissing >= 1)
		    
		    if (numberScoresNotInteger >= 1) {
		    	rgTable = table.getRepeatableGroupTable(notInteger_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						notInteger_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresNotInteger; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, notInteger_group_row_index),
									rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(notIntegerIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
											notIntegerData.get(i));
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, notInteger_group_row_index),
							rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)), message));
		    	}
		    } // if (numberScoresNotInteger >= 1)
		    
		    
		    
		    int actualSum = -1;
		    boolean haveTotal = false;
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
				if (bufColumnNameVector.get(i).equalsIgnoreCase("auditctotalscore")) {
					haveTotal = true;
					data = bufDataVector.get(i);
					repeatable_group_id = bufRepeatable_group_id.get(i);
					group_row_index = bufGroup_row_index.get(i);
					index = bufIndex.get(i);
					rgTable = table.getRepeatableGroupTable(repeatable_group_id);
					rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
					if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
					if ((numberScoresMissing == 0) && (numberScoresNotInteger == 0)) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));  
					}
					}
					else if ((numberScoresMissing > 0) || (numberScoresNotInteger > 0)) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));  	
					}
					else {
						try {
					        actualSum = Integer.valueOf(data).intValue();
						}
						catch (NumberFormatException e) {
							message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(index),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(index));

							message =
									message
											+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,data);
							table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index), message));    		
						}
					}
					break;
				}
		    }
		    
		    if ((!haveTotal) && (numberScoresMissing == 0) && (numberScoresNotInteger == 0)) {
		    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
		    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
		    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("AUDITC Scoring")) {
		    		    // rgTable.getColumnCount() = 1 for AUDITC Scoring
		    			message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, 0),
										rgTable.getDataFilePositionMapping(0),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(0));

						message =
								message
										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(0), message));
		    			break;
		    		}
		    	}
		    }
		    
		    if ((actualSum > -1) && (numberScoresMissing == 0) && (numberScoresNotInteger == 0)) {
		    	if (actualSum != calculatedSum) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
											String.valueOf(actualSum), String.valueOf(calculatedSum));
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
		    	}
		    }
		
	}
	
	private void validateAUDIT(StructuralFormStructure structure,
			Vector<Vector<Vector<String>>> columnNameVector3, 
			Vector<Vector<Vector<String>>>dataVector3, int subject_row_id,
			DataStructureTable table) throws Exception {
		
		    // table.getColumnCount = 2
		    // repeatable_group_id = 0
			// rgEntries.size() = 1
		    // group_row_index = 0
			// columnNameVector.size() = 8
		  	// group_column_index = 0 group_column_name = guid data = TBI_INVXN170PVJ
			// group_column_index = 1 group_column_name = subjectidnum data = 1BI_01
		    // group_column_index = 2 group_column_name = ageyrs data = null
			// group_column_index = 3 group_column_name = visitdate data = 2016-10-25T00:00:00Z
			// group_column_index = 4 group_column_name = sitename data = University of Miami
			// group_column_index = 5 group_column_name = dayssincebaseline data = 150
			// group_column_index = 6 group_column_name = casecontrlind data = control
			// group_column_index = 7 group_column_name = generalnotestxt data = null
			// repeatable_group_id = 1
			// rgEntries.size() = 1
			// group_row_index = 0
			// columnNameVector.size() = 11
			// group_column_index = 0 group_column_name = auditdrnkcontainalcfreqscore data = 1
			// group_column_index = 1 group_column_name = auditalcdrnktypcldaynumscore data = 0
			// group_column_index = 2 group_column_name = auditmorethan6alcdrnkfreqscore data = 1
			// group_column_index = 3 group_column_name = auditcantstopdrnkalcfreqscore data = 0
			// group_column_index = 4 group_column_name = auditfailnrmlactdrnkfreqscore data = 0
			// group_column_index = 5 group_column_name = auditmorndrnkaftdrnkfreqscore data = 0
			// group_column_index = 6 group_column_name = auditgltrmrseaftrdrnkfreqscore data = 0
			// group_column_index = 7 group_column_name = auditcantrmbrnghtbefrfreqscore data = 0
			// group_column_index = 8 group_column_name = auditdrnkinjindscore data = 0
			// group_column_index = 9 group_column_name = auditreltvfrnddrcncrnindscore data = 0
			// group_column_index = 10 group_column_name = audittotalscore data = 2
			Vector<Vector<String>> columnNameVector2;
	        Vector<Vector<String>> dataVector2;
	        Vector<String> columnNameVector;
	        Vector<String> dataVector;
	        int i;
	        String message;
	        String data;
	        int value = 0;
	        Vector<String> bufColumnNameVector = new Vector<String>();
	        Vector<String> bufDataVector = new Vector<String>();
	        Vector<Integer> bufRepeatable_group_id = new Vector<Integer>();
	        Vector<Integer> bufGroup_row_index = new Vector<Integer>();
	        Vector<Integer> bufIndex = new Vector<Integer>();
	        String guid = null;
	        int group_row_index = -1;
	        int repeatable_group_id;
	        RepeatableGroupTable rgTable = null;
	        ArrayList<Integer> rgEntries = null;
	        int index = -1;
	        
			 //System.out.println("table.getColumnCount = " + table.getColumnCount());
	        	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	        		 
	        		//System.out.println("repeatable_group_id = " + repeatable_group_id);
	        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
	        		dataVector2 = dataVector3.get(repeatable_group_id);
					rgTable = table.getRepeatableGroupTable(repeatable_group_id);
					//System.out.println("rgTable.getSize() = " + rgTable.getSize());
					rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
					//System.out.println("rgEntries.size() = " + rgEntries.size());
					for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
	        	 //System.out.println("group_row_index = " + group_row_index);
				columnNameVector = columnNameVector2.get(group_row_index);
				dataVector = dataVector2.get(group_row_index);
				 //System.out.println("columnNameVector.size() = " + columnNameVector.size());
				 //for (i = 0; i < columnNameVector.size(); i++) {
				    //System.out.println("group_column_index = " + i +
				    	//" group_column_name = " + columnNameVector.get(i) +
				    	//" data = " + dataVector.get(i)) ;	
				 //}
				for (i = 0; i < columnNameVector.size(); i++) {
					bufColumnNameVector.add(columnNameVector.get(i));
					bufDataVector.add(dataVector.get(i));
					bufRepeatable_group_id.add(repeatable_group_id);
					bufGroup_row_index.add(group_row_index);
					bufIndex.add(i);
				}
				} // for (int group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++)
	            } // for (int repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++)
	        for (i = 0; i < bufColumnNameVector.size(); i++) {
	        	if (bufColumnNameVector.get(i).equalsIgnoreCase("guid")) {
					guid = bufDataVector.get(i);
					break;
				}	
	        } // for (i = 0; i < bufColumnNameVector.size(); i++)
	        
		    
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
				if (bufColumnNameVector.get(i).equalsIgnoreCase("ageyrs")) {
					String ageData = bufDataVector.get(i);
					repeatable_group_id = bufRepeatable_group_id.get(i);
					group_row_index = bufGroup_row_index.get(i);
					index = bufIndex.get(i);
					rgTable = table.getRepeatableGroupTable(repeatable_group_id);
					rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
					if ((ageData == null) || (ageData.isEmpty()) || (ageData.trim() == null) ||
							(ageData.trim().isEmpty())) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
					}	
					else {
						double age = Double.valueOf(ageData).doubleValue();
						if (age < 18) {
							message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(index),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(index));

							message =
									message
											+ String.format(ApplicationsConstants.WARNING_INCORRECT_AGE,
											  ageData, "18 or more years");
							table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index), message));
						}
					}
					break;
				  } // if (bufColumnNameVector.get(i).equalsIgnoreCase("ageyrs"))
				} // for (i = 0; i < bufColumnNameVector.size(); i++)
				
				int numberScoresMissing = 0;
			    Vector<Integer>missingIndices = new Vector<Integer>();
			    int missing_repeatable_group_id = -1;
			    int missing_group_row_index = -1;
	            
	            int numberScoresNotInteger = 0;
	            Vector<Integer>notIntegerIndices = new Vector<Integer>();
	            int notInteger_repeatable_group_id = -1;
	            int notInteger_group_row_index = -1;
	            Vector<String>notIntegerData = new Vector<String>();
	            boolean notInteger;
			
			    int calculatedTotalScore = 0;
			    int actualTotalScore = -1;
			    int totalScoreIndex = -1;
			    boolean Q2Zero = false;
			    boolean Q3Zero = false;
			    boolean Q23Zero = false;
			    for (i = 0; i < bufColumnNameVector.size(); i++) {
			    	if (bufColumnNameVector.get(i).equalsIgnoreCase("auditdrnkcontainalcfreqscore") ||
			    		bufColumnNameVector.get(i).equalsIgnoreCase("auditalcdrnktypcldaynumscore") ||
			    		bufColumnNameVector.get(i).equalsIgnoreCase("auditmorethan6alcdrnkfreqscore") ||
			    		bufColumnNameVector.get(i).equalsIgnoreCase("auditdrnkinjindscore") ||	
			    		bufColumnNameVector.get(i).equalsIgnoreCase("auditreltvfrnddrcncrnindscore")) {
			    		data = bufDataVector.get(i);
			    		repeatable_group_id = bufRepeatable_group_id.get(i);
						group_row_index = bufGroup_row_index.get(i);
						index = bufIndex.get(i);
						rgTable = table.getRepeatableGroupTable(repeatable_group_id);
						rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				    	if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
								(data.trim().isEmpty())) {
				    		numberScoresMissing++;
						    missingIndices.add(index);
						    missing_repeatable_group_id = repeatable_group_id;
							missing_group_row_index = group_row_index;
				    	}
				    	else {
				    		notInteger = false;
				    		try {
					    	    value = Integer.valueOf(data).intValue();
				    		}
				    		catch (NumberFormatException e) {
				    		    notInteger = true;
				    		    numberScoresNotInteger++;
				    		    notIntegerIndices.add(index);
				    		    notInteger_repeatable_group_id = repeatable_group_id;
				    		    notInteger_group_row_index = group_row_index;
				    		    notIntegerData.add(data);
				    		}
				    		if (!notInteger) {
						    	calculatedTotalScore += value;	
						    	if (value == 0) {
						    		if (bufColumnNameVector.get(i).equalsIgnoreCase("auditalcdrnktypcldaynumscore")) {
						    		    Q2Zero = true;	
						    		}
						    		else if (bufColumnNameVector.get(i).equalsIgnoreCase("auditmorethan6alcdrnkfreqscore")) {
						    			Q3Zero = true;
						    		}
						    	}
				    		} // if (!notInteger)
				    	}
			    	}
			    	else if (bufColumnNameVector.get(i).equalsIgnoreCase("audittotalscore") ) {
			    		data = bufDataVector.get(i);
			    		repeatable_group_id = bufRepeatable_group_id.get(i);
						group_row_index = bufGroup_row_index.get(i);
						index = bufIndex.get(i);
						rgTable = table.getRepeatableGroupTable(repeatable_group_id);
						rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
			    		totalScoreIndex = index;
			    		if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
								(data.trim().isEmpty())) {
			    			message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(index),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(index));

							message =
									message
											+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
							table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index), message));	
			    		}
			    		else {
			    			try {  
			    			    actualTotalScore = Integer.valueOf(data).intValue();
			    			}
			    			catch (NumberFormatException e) {
			    				message =
										String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
												guid,
												getRawDataRow(subject_row_id, group_row_index),
												rgTable.getDataFilePositionMapping(index),
												rgTable.getRepeatableGroupName(),
												rgTable.getColumnName(index));

								message =
										message
												+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,data);
								table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index), message));		
			    			}
			    		}
			    	}
			    } // for (i = 0; i < bufColumnNameVector.size(); i++)
			    Q23Zero = Q2Zero && Q3Zero;
			    
			    if (!Q23Zero) {
			    	for (i = 0; i < bufColumnNameVector.size(); i++) {
				    	if (bufColumnNameVector.get(i).equalsIgnoreCase("auditcantstopdrnkalcfreqscore") ||
				    		bufColumnNameVector.get(i).equalsIgnoreCase("auditfailnrmlactdrnkfreqscore") ||
				    		bufColumnNameVector.get(i).equalsIgnoreCase("auditmorndrnkaftdrnkfreqscore") ||
				    		bufColumnNameVector.get(i).equalsIgnoreCase("auditgltrmrseaftrdrnkfreqscore") ||
				    		bufColumnNameVector.get(i).equalsIgnoreCase("auditcantrmbrnghtbefrfreqscore")) {
				    		data = bufDataVector.get(i);
					    	if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
									(data.trim().isEmpty())) {
					    		numberScoresMissing++;
					    		missingIndices.add(bufIndex.get(i));
							    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
								missing_group_row_index = bufGroup_row_index.get(i);
					    	}
					    	else {
					    		notInteger = false;
					    		try {
						    	    value = Integer.valueOf(data).intValue();
					    		}
					    		catch (NumberFormatException e) {
					    			notInteger = true;
					    			numberScoresNotInteger++;
						    		notIntegerIndices.add(bufIndex.get(i));
								    notInteger_repeatable_group_id = bufRepeatable_group_id.get(i);
									notInteger_group_row_index = bufGroup_row_index.get(i);	
									notIntegerData.add(data);
					    		}
					    		if (!notInteger) {
						    	    calculatedTotalScore += value;
					    		}
					    	}
				    	}
			    	}
			    } // if (!Q23Zero)
			    
			    if (numberScoresMissing >= 1) {
			    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
					rgEntries = table.getAllReferences(subject_row_id, 
							missing_repeatable_group_id, null);
			    	for (i = 0; i < numberScoresMissing; i++) {
			    		message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, missing_group_row_index),
										rgTable.getDataFilePositionMapping(missingIndices.get(i)),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(missingIndices.get(i)));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index),
								rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
			    	}
			    } // if (numberScoresMissing >= 1)
			    
			    if (numberScoresNotInteger >= 1) {
			    	rgTable = table.getRepeatableGroupTable(notInteger_repeatable_group_id);
					rgEntries = table.getAllReferences(subject_row_id, 
							notInteger_repeatable_group_id, null);
			    	for (i = 0; i < numberScoresNotInteger; i++) {
			    		message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, notInteger_group_row_index),
										rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(notIntegerIndices.get(i)));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
												notIntegerData.get(i));
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, notInteger_group_row_index),
								rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)), message));
			    	}	
			    } // if (numberScoresNotInteger >= 1)
			    
			    if ((numberScoresMissing >= 1) || (numberScoresNotInteger >= 1)) {
			    	if (actualTotalScore > -1) {
			    		message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(totalScoreIndex),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(totalScoreIndex));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,
												String.valueOf(actualTotalScore));
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(totalScoreIndex), message));	
			    	}
			    	return;
			    }
			    
			    if (actualTotalScore > -1) {
			    	if (actualTotalScore != calculatedTotalScore) {
			    		message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(totalScoreIndex),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(totalScoreIndex));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
												String.valueOf(actualTotalScore), String.valueOf(calculatedTotalScore));
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(totalScoreIndex), message));
			    	}
			    }
			   
					
		}

	private void validateGCS(StructuralFormStructure structure,
			Vector<Vector<Vector<String>>> columnNameVector3, 
			Vector<Vector<Vector<String>>> dataVector3, int subject_row_id, 
			DataStructureTable table) throws Exception {
		    // 3 possible sets of columnNameVectors and accompanying dataVectors:
		
		    // group_column_index 0 currentColumnName =  gcsconfoundertyp
			// group_column_index 1 currentColumnName =  gcseyerespnsscale
			// group_column_index 2 currentColumnName =  gcsmotorrespnsscale
	        // group_column_index 3 currentColumnName =  gcsverbalrspnsscale
		    // group_column_index 4 currentColumnName =  gcstotalscore
		    // group_column_index 5 currentColumnName =  pupilreactivitylghtlfteyereslt
		    // group_column_index 6 currentColumnName =  pupilreactivitylghtrteyereslt
		    // group_column_index 7 currentColumnName =  pupillfteyemeasr
			// group_column_index 8 currentColumnName =  pupilrteyemeasr
		    // group_column_index 9 currentColumnName =  pupilshapelfteyetyp
		    // group_column_index 10 currentColumnName =  pupilshaperteyetyp
		
		    // group_column_index 0 currentColumnName =  gcsconfoundertyp
		    // group_column_index 1 currentColumnName =  pgcseyerespnsscore
		    // group_column_index 2 currentColumnName =  pgcsmotorrespnsscore
			// group_column_index 3 currentColumnName =  pgcsverbalrespnsscore
		    // group_column_index 4 currentColumnName =  pgcstotalscore
		    // group_column_index 5 currentColumnName =  pupilreactivitylghtlfteyereslt
		    // group_column_index 6 currentColumnName =  pupilreactivitylghtrteyereslt
			// group_column_index 7 currentColumnName =  pupillfteyemeasr
			// group_column_index 8 currentColumnName =  pupilrteyemeasr
		    // group_column_index 9 currentColumnName =  pupilshapelfteyetyp
		    // group_column_index 10 currentColumnName =  pupilshaperteyetyp
  
		    // group_column_index 0 currentColumnName =  guid
		    // group_column_index 1 currentColumnName =  subjectidnum
		    // group_column_index 2 currentColumnName =  ageyrs
		    // group_column_index 3 currentColumnName =  vitstatus
			// group_column_index 4 currentColumnName =  visitdate
		    // group_column_index 5 currentColumnName =  sitename
		    // group_column_index 6 currentColumnName =  dayssincebaseline
		    // group_column_index 7 currentColumnName =  casecontrlind
			// group_column_index 8 currentColumnName =  generalnotestxt

		Vector<Vector<String>> columnNameVector2;
        Vector<Vector<String>> dataVector2;
        Vector<String> columnNameVector;
        Vector<String> dataVector;
        String message;
        int repeatable_group_id;
        RepeatableGroupTable rgTable;
        ArrayList<Integer> rgEntries;
        int group_row_index;
        int i;
        String guid = null;
        
        guidloop:
            for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
       		 
        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
        		dataVector2 = dataVector3.get(repeatable_group_id);
        		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
        		rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
    			for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
    		        columnNameVector = columnNameVector2.get(group_row_index);
    		        dataVector = dataVector2.get(group_row_index);
    		        for (i = 0; i  < columnNameVector.size(); i++) {
    					if (columnNameVector.get(i).equalsIgnoreCase("guid")) {
    					    guid = dataVector.get(i);
    					    break guidloop;
    					}
    		        }
    			}
            }
        loop1:
        	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
        		dataVector2 = dataVector3.get(repeatable_group_id);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries =
						table.getAllReferences(subject_row_id, repeatable_group_id, null);
        for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
			columnNameVector = columnNameVector2.get(group_row_index);
			dataVector = dataVector2.get(group_row_index);
		    int firstIndex = -1;
		    int totalScoreIndex = -1;
		    for (i = 0; i < columnNameVector.size() && (totalScoreIndex == -1); i++) {
		    	if (columnNameVector.get(i).endsWith("totalscore")) {
		    		totalScoreIndex = i;
		    	}
		    }
		    if (totalScoreIndex == -1) {
		    	// column name ending in totalscore not found
		    	continue loop1;
		    }
		    boolean scFound = true;
		    for (i = totalScoreIndex -1; i >= 0 && scFound; i--) {
		    	String columnName = columnNameVector.get(i);
		    	if ((columnName.endsWith("scale")) || (columnName.endsWith("score"))) {
		    		firstIndex = i;
		    	}
		    	else {
		    		scFound = false;
		    	}
		    }
		    if (firstIndex == -1) {
		    	// No column name ending with scale or score found before column name ending in totalscore
		    	return;
		    }
			String data = dataVector.get(totalScoreIndex);
			if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
					(data.trim().isEmpty())) {
			    data = "blank";
			}
	        boolean blankFound = false;
	        boolean unknownFound = false;
	        boolean untestableFound = false;
	        int sum = 0;
	        boolean goodSum = true;
	        for (i = firstIndex; i <= totalScoreIndex-1; i++) {
	            String scData = dataVector.get(i);
	            if ((scData == null) || (scData.isEmpty()) || (scData.trim() == null) ||
	            		(scData.trim().isEmpty())) {
	            	blankFound = true;
	            }
	            else if (scData.equalsIgnoreCase("Unknown")) {
	            	unknownFound = true;
	            }
	            else if (scData.equalsIgnoreCase("Untestable")) {
	            	untestableFound = true;
	            }
	            else { 
	            	try {
	            	    sum += Integer.valueOf(scData);
	            	}
	            	catch (NumberFormatException e) {
	            		goodSum = false;
	            		message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(i),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(i));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
												scData);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(i), message));
	            	}
	            }
	        } // for (int i = firstIndex; i <= totalScoreIndexIndex-1; i++)
	        int listedSum = 0;
	        boolean goodListedSum = true;
	        try {
	            listedSum = Integer.valueOf(data).intValue();
	        }
	        catch (NumberFormatException e) {
	        	goodListedSum = false;
	        	message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(totalScoreIndex),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(totalScoreIndex));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
										data);
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(totalScoreIndex), message));
	        }
	       
	        if (blankFound) { 
	            if  (!data.equalsIgnoreCase("blank")) {
	        
		        	message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(totalScoreIndex),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(totalScoreIndex));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,
											data);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(totalScoreIndex), message));
	            } // 
	        } // if (blankFound)
	        else if (unknownFound) {
	            if (!data.equalsIgnoreCase("Unknown")) {
	            	message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(totalScoreIndex),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(totalScoreIndex));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_UNKNOWN_REQUIRED,
											data);	
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(totalScoreIndex), message));
	            }
	        } // else if (unknownFound)
	        else if (untestableFound) {
	        	if (!data.equalsIgnoreCase("Untestable")) {
	            	message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(totalScoreIndex),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(totalScoreIndex));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_UNTESTABLE_REQUIRED,
											data);	
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(totalScoreIndex), message));
	            }	
	        } // else if (untestableFound)
	        else if ((goodSum && goodListedSum && (listedSum != sum)) ||
	        		(goodSum && (!goodListedSum))) {
	        	message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(totalScoreIndex),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(totalScoreIndex));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
										data, String.valueOf(sum));	
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(totalScoreIndex), message));
	        }
        } // for (int group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++)
        } // for (int repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) 	   
	}

	
	private void validateGOSE_Standard(StructuralFormStructure structure,
			Vector<Vector<Vector<String>>> columnNameVector3, 
			Vector<Vector<Vector<String>>> dataVector3,int subject_row_id,
			DataStructureTable table) throws Exception {
		// 2 possible sets of columnNameVectors and accompanying dataVectors:
		
		// group_column_index 0 group_columnName guid
		// group_column_index 1 group_columnName subjectidnum
		// group_column_index 2 group_columnName ageyrs
		// group_column_index 3 group_columnName visitdate
		// group_column_index 4 group_columnName sitename
		// group_column_index 5 group_columnName dayssincebaseline
		// group_column_index 6 group_columnName casecontrlind
		// group_column_index 7 group_columnName generalnotestxt
		
		// group_column_index 0 group_columnName goseresponse
		// group_column_index 1 group_columnName goseconsciousind
		// group_column_index 2 group_columnName goseasstneedind
		// group_column_index 3 group_columnName gosefreqnthlpneedind
		// group_column_index 4 group_columnName goseindpntpreinjryind
		// group_column_index 5 group_columnName goseshpwoutasstind
		// group_column_index 6 group_columnName goseshpwoutasstpreinjryind
		// group_column_index 7 group_columnName gosetrvlwoutasstind
		// group_column_index 8 group_columnName gosetrvlwoutasstpreinjryind
		// group_column_index 9 group_columnName gosecrrntwrkind
		// group_column_index 10 group_columnName gosewrkrestricttyp
		// group_column_index 11 group_columnName goselvlrestrictind
		// group_column_index 12 group_columnName gosesocleisoutind
		// group_column_index 13 group_columnName goseextntrestrctsocfreq
		// group_column_index 14 group_columnName goseextntrestrctsocpreinjrind
		// group_column_index 15 group_columnName gosefrindshpind
		// group_column_index 16 group_columnName goseextntfriendshpstrnfreq
		// group_column_index 17 group_columnName goselvlstrnpreinjryind
		// group_column_index 18 group_columnName gosecrrntprobind
		// group_column_index 19 group_columnName gosesimlrprobpreinjryind
		// group_column_index 20 group_columnName gosemstimptfctrtyp
		// group_column_index 21 group_columnName glasgowoutcomescalextscore
		
		Vector<Vector<String>> columnNameVector2;
        Vector<Vector<String>> dataVector2;
        Vector<String> columnNameVector;
        Vector<String> dataVector;
        String message;
        int repeatable_group_id;
        RepeatableGroupTable rgTable;
        ArrayList<Integer> rgEntries;
        int group_row_index;
        int i;
        String guid = null;
        
        guidloop:
            for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
       		 
        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
        		dataVector2 = dataVector3.get(repeatable_group_id);
        		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
        		rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
    			for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
    		        columnNameVector = columnNameVector2.get(group_row_index);
    		        dataVector = dataVector2.get(group_row_index);
    		        for (i = 0; i  < columnNameVector.size(); i++) {
    					if (columnNameVector.get(i).equalsIgnoreCase("guid")) {
    					    guid = dataVector.get(i);
    					    break guidloop;
    					}
    		        }
    			}
            }
        loop1:
        	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
        		dataVector2 = dataVector3.get(repeatable_group_id);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries =
						table.getAllReferences(subject_row_id, repeatable_group_id, null);

        for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
			columnNameVector = columnNameVector2.get(group_row_index);
			dataVector = dataVector2.get(group_row_index);
			int glasgowoutcomescalextscoreIndex = -1;
		for (i = 0; i < columnNameVector.size() && (glasgowoutcomescalextscoreIndex == -1); i++) {
			if (columnNameVector.get(i).equalsIgnoreCase("glasgowoutcomescalextscore")) {
				glasgowoutcomescalextscoreIndex = i;	
			}
		}
		
		if (glasgowoutcomescalextscoreIndex == -1) {
			// This is the columnNameVector with the guid.
			continue loop1;
		}
		
		String dataScore = dataVector.get(glasgowoutcomescalextscoreIndex);
		boolean blankDataScore = false;
		if ((dataScore == null) || (dataScore.isEmpty()) || (dataScore.trim() == null)
				|| (dataScore.trim().isEmpty())) {
		    blankDataScore = true;
		    dataScore = "blank";
		}
		int listedScore = 0;
		int calculatedScore = -1;
        boolean goodListedScore = true;
        try {
            listedScore = Integer.valueOf(dataScore).intValue();
            if (listedScore == 1) {
            	// Deceased - do not validate
            	return;
            }
        }
        catch (NumberFormatException e) {
        	goodListedScore = false;
        	message =
					String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
							guid,
							getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(glasgowoutcomescalextscoreIndex),
							rgTable.getRepeatableGroupName(),
							rgTable.getColumnName(glasgowoutcomescalextscoreIndex));

			message =
					message
							+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
									dataScore);	
			table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
					rgTable.getDataFilePositionMapping(glasgowoutcomescalextscoreIndex), message));	
        }
        
        String data;
        int goseconsciousindIndex = -1;
		for (i = 0; i < columnNameVector.size() && (goseconsciousindIndex == -1); i++) {
			if (columnNameVector.get(i).equalsIgnoreCase("goseconsciousind")) {
				goseconsciousindIndex = i;	
			}
		}
		
		if (goseconsciousindIndex != -1) {
			data = dataVector.get(goseconsciousindIndex);
		    if ((data != null) && (data.equalsIgnoreCase("No"))) {
		    	calculatedScore = 2;
		    }
		}
		
		if (calculatedScore == -1) {
			boolean Q2bNoResponse = false;
			boolean Q2bNo = false;
			boolean Q2bYes = false;
			boolean Q2cNoResponse = false;
			boolean Q2cNo = false;
			boolean Q2cYes = false;
			int gosefreqnthlpneedindIndex = -1;
			int goseindpntpreinjryindIndex = -1;
			for (i = 0; i < columnNameVector.size() && (gosefreqnthlpneedindIndex == -1); i++) {
				if (columnNameVector.get(i).equalsIgnoreCase("gosefreqnthlpneedind")) {
					gosefreqnthlpneedindIndex = i;	
				}
			}
			
			if (gosefreqnthlpneedindIndex != -1) {
				data = dataVector.get(gosefreqnthlpneedindIndex);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					Q2bNoResponse = true;
				}
			    else if (data.equalsIgnoreCase("No")) {
				    Q2bNo = true;	
				}
			    else if (data.equalsIgnoreCase("Yes")) {
				    Q2bYes = true;	
				}
			} // if (gosefreqnthlpneedindIndex != -1)
			
			for (i = 0; i < columnNameVector.size() && (goseindpntpreinjryindIndex == -1); i++) {
				if (columnNameVector.get(i).equalsIgnoreCase("goseindpntpreinjryind")) {
					goseindpntpreinjryindIndex = i;	
				}
			}
			
			if (goseindpntpreinjryindIndex != -1) {
				data = dataVector.get(goseindpntpreinjryindIndex);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					Q2cNoResponse = true;
				}
			    else if (data.equalsIgnoreCase("No")) {
				    Q2cNo = true;	
				}
			    else if (data.equalsIgnoreCase("Yes")) {
				    Q2cYes = true;	
				}	
			} // if (goseindpntpreinjryindIndex != -1)
			
			if (Q2bYes && Q2cYes) {
				calculatedScore = 3;
			}
			else if (Q2bNo && (Q2cNo || Q2cNoResponse)) {
				calculatedScore = 4;
			}
				
		} // if (calculatedScore == -1)
		
		if (calculatedScore == -1) {
		    boolean Q3aNoResponse = false;
		    boolean Q3aNo = false;
		    boolean Q3aYes = false;
		    boolean Q3bNoResponse = false;
		    boolean Q3bNo = false;
		    boolean Q3bYes = false;
		    int goseshpwoutasstindIndex = -1;
		    int goseshpwoutasstpreinjryindIndex = -1;
		    for (i = 0; i < columnNameVector.size() && (goseshpwoutasstindIndex == -1); i++) {
				if (columnNameVector.get(i).equalsIgnoreCase("goseshpwoutasstind")) {
					goseshpwoutasstindIndex = i;	
				}
			}
			
			if (goseshpwoutasstindIndex != -1) {
				data = dataVector.get(goseshpwoutasstindIndex);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					Q3aNoResponse = true;
				}
			    else if (data.equalsIgnoreCase("No")) {
				    Q3aNo = true;	
				}
			    else if (data.equalsIgnoreCase("Yes")) {
				    Q3aYes = true;	
				}
			} // if (goseshpwoutasstindIndex != -1)
			
			for (i = 0; i < columnNameVector.size() && (goseshpwoutasstpreinjryindIndex == -1); i++) {
				if (columnNameVector.get(i).equalsIgnoreCase("goseshpwoutasstpreinjryind")) {
					goseshpwoutasstpreinjryindIndex = i;	
				}
			}
			
			if (goseshpwoutasstpreinjryindIndex != -1) {
				data = dataVector.get(goseshpwoutasstpreinjryindIndex);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					Q3bNoResponse = true;
				}
			    else if (data.equalsIgnoreCase("No")) {
				    Q3bNo = true;	
				}
			    else if (data.equalsIgnoreCase("Yes")) {
				    Q3bYes = true;	
				}
			} // if (goseshpwoutasstpreinjryindIndex != -1
			
			if (Q3aNo && (Q3bYes || Q3bNoResponse)) {
			    calculatedScore = 4;	
			}
		} // if (calculatedScore == -1)
		
		if (calculatedScore == -1) {
			 boolean Q4aNoResponse = false;
			 boolean Q4aNo = false;
			 boolean Q4aYes = false;
			 boolean Q4bNoResponse = false;
			 boolean Q4bNo = false;
			 boolean Q4bYes = false;
			 int gosetrvlwoutasstindIndex = -1;
			 int gosetrvlwoutasstpreinjryindIndex = -1;
			 
			 for (i = 0; i < columnNameVector.size() && (gosetrvlwoutasstindIndex == -1); i++) {
					if (columnNameVector.get(i).equalsIgnoreCase("gosetrvlwoutasstind")) {
						gosetrvlwoutasstindIndex = i;	
					}
				}
				
				if (gosetrvlwoutasstindIndex != -1) {
					data = dataVector.get(gosetrvlwoutasstindIndex);
					if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
						Q4aNoResponse = true;
					}
				    else if (data.equalsIgnoreCase("No")) {
					    Q4aNo = true;	
					}
				    else if (data.equalsIgnoreCase("Yes")) {
					    Q4aYes = true;	
					}
				} // if (gosetrvlwoutasstindIndex != -1)
				
				for (i = 0; i < columnNameVector.size() && (gosetrvlwoutasstpreinjryindIndex == -1); i++) {
					if (columnNameVector.get(i).equalsIgnoreCase("gosetrvlwoutasstpreinjryind")) {
						gosetrvlwoutasstpreinjryindIndex = i;	
					}
				}
				
				if (gosetrvlwoutasstpreinjryindIndex != -1) {
					data = dataVector.get(gosetrvlwoutasstpreinjryindIndex);
					if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
						Q4bNoResponse = true;
					}
				    else if (data.equalsIgnoreCase("No")) {
					    Q4bNo = true;	
					}
				    else if (data.equalsIgnoreCase("Yes")) {
					    Q4bYes = true;	
					}
				} // if (gosetrvlwoutasstpreinjryindIndex != -1)
				
				if (Q4aNo && (Q4bYes || Q4bNoResponse)) {
					calculatedScore = 4;
				}
		} // if (calculatedScore == -1)
		
		boolean Q5bNoResponse = false;
		boolean Q5bSheltered = false;
		boolean Q5bReduced = false;
		boolean Q5cNoResponse = false;
		boolean Q5cNo = false;
		boolean Q5cYes = false;
		boolean Q6bNoResponse = false;
		boolean Q6bUnable = false;
		boolean Q6bMuchLess = false;
		boolean Q6bBitLess = false;
		boolean Q6cNoResponse = false;
		boolean Q6cNo = false;
		boolean Q6cYes = false;
		boolean Q7bNoResponse = false;
		boolean Q7bConstant = false;
		boolean Q7bFrequent = false;
		boolean Q7bOccasional = false;
		boolean Q7cNoResponse = false;
		boolean Q7cNo = false;
		boolean Q7cYes = false;
		
		if (calculatedScore == -1) {
		    int gosewrkrestricttypIndex = -1;
		    int goselvlrestrictindIndex = -1;
		    
		    for (i = 0; i < columnNameVector.size() && (gosewrkrestricttypIndex == -1); i++) {
				if (columnNameVector.get(i).equalsIgnoreCase("gosewrkrestricttyp")) {
					gosewrkrestricttypIndex = i;	
				}
			}
			
			if (gosewrkrestricttypIndex != -1) {
				data = dataVector.get(gosewrkrestricttypIndex);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					Q5bNoResponse = true;
				}
			    else if (data.contains("only in a sheltered workshop")) {
				    Q5bSheltered = true;	
				}
			    else if (data.contains("Reduced work capacity")) {
				    Q5bReduced = true;	
				}
			} // if (gosewrkrestricttypIndex != -1)
			
			for (i = 0; i < columnNameVector.size() && (goselvlrestrictindIndex == -1); i++) {
				if (columnNameVector.get(i).equalsIgnoreCase("goselvlrestrictind")) {
					goselvlrestrictindIndex = i;	
				}
			}
			
			if (goselvlrestrictindIndex != -1) {
				data = dataVector.get(goselvlrestrictindIndex);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					Q5cNoResponse = true;
				}
			    else if (data.equalsIgnoreCase("No")) {
				    Q5cNo = true;	
				}
			    else if (data.equalsIgnoreCase("Yes")) {
				    Q5cYes = true;	
				}
			} // if (goselvlrestrictindIndex != -1)
			
			if (Q5bSheltered && (Q5cYes || Q5cNoResponse)) {
				calculatedScore = 5;
			}
		} // if (calculatedScore == -1)
		
		if (calculatedScore == -1) {
		    int goseextntrestrctsocfreqIndex = -1;
		    int goseextntrestrctsocpreinjrindIndex = -1;
		    for (i = 0; i < columnNameVector.size() && (goseextntrestrctsocfreqIndex == -1); i++) {
				if (columnNameVector.get(i).equalsIgnoreCase("goseextntrestrctsocfreq")) {
					goseextntrestrctsocfreqIndex = i;	
				}
			}
			
			if (goseextntrestrctsocfreqIndex != -1) {
				data = dataVector.get(goseextntrestrctsocfreqIndex);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					Q6bNoResponse = true;
				}
			    else if (data.contains("Unable to participate")) {
				    Q6bUnable = true;	
				}
			    else if (data.contains("Participate much less")) {
				    Q6bMuchLess = true;	
				}
			    else if (data.contains("Participate a bit less")) {
			    	Q6bBitLess = true;
			    }
			} // if (goseextntrestrctsocfreqIndex != -1)
			
			for (i = 0; i < columnNameVector.size() && (goseextntrestrctsocpreinjrindIndex == -1); i++) {
				if (columnNameVector.get(i).equalsIgnoreCase("goseextntrestrctsocpreinjrind")) {
					goseextntrestrctsocpreinjrindIndex = i;	
				}
			}
			
			if (goseextntrestrctsocpreinjrindIndex != -1) {
				data = dataVector.get(goseextntrestrctsocpreinjrindIndex);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					Q6cNoResponse = true;
				}
			    else if (data.equalsIgnoreCase("No")) {
				    Q6cNo = true;	
				}
			    else if (data.equalsIgnoreCase("Yes")) {
				    Q6cYes = true;	
				}
			} // if (goseextntrestrctsocpreinjrindIndex != -1)
			
			if (Q6bUnable && (Q6cYes || Q6cNoResponse)) {
				calculatedScore = 5;
			}
		} // if (cacluatedScore == -1)
		
		if (calculatedScore == -1) {
		    int goseextntfriendshpstrnfreqIndex = -1;
		    int goselvlstrnpreinjryindIndex = -1;
		    for (i = 0; i < columnNameVector.size() && (goseextntfriendshpstrnfreqIndex == -1); i++) {
				if (columnNameVector.get(i).equalsIgnoreCase("goseextntfriendshpstrnfreq")) {
					goseextntfriendshpstrnfreqIndex = i;	
				}
			}
			
			if (goseextntfriendshpstrnfreqIndex != -1) {
				data = dataVector.get(goseextntfriendshpstrnfreqIndex);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					Q7bNoResponse = true;
				}
			    else if (data.contains("Constant")) {
				    Q7bConstant = true;	
				}
			    else if (data.contains("Frequent")) {
				    Q7bFrequent = true;	
				}
			    else if (data.contains("Occasional")) {
			    	Q7bOccasional = true;
			    }
			} // if (goseextntfriendshpstrnfreqIndex != -1)
			
			for (i = 0; i < columnNameVector.size() && (goselvlstrnpreinjryindIndex == -1); i++) {
				if (columnNameVector.get(i).equalsIgnoreCase("goselvlstrnpreinjryind")) {
					goselvlstrnpreinjryindIndex = i;	
				}
			}
			
			if (goselvlstrnpreinjryindIndex != -1) {
				data = dataVector.get(goselvlstrnpreinjryindIndex);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					Q7cNoResponse = true;
				}
			    else if (data.equalsIgnoreCase("No")) {
				    Q7cNo = true;	
				}
			    else if (data.equalsIgnoreCase("Yes")) {
				    Q7cYes = true;	
				}
			} // if (goselvlstrnpreinjryindIndex != -1)
			
			if (Q7bConstant && (Q7cNo || Q7cNoResponse)) {
				calculatedScore = 5;
			}
			else if (Q5bReduced && (Q5cYes || Q5cNoResponse)) {
				calculatedScore = 6;
			}
			else if (Q6bMuchLess && (Q6cYes || Q6cNoResponse)) {
				calculatedScore = 6;
			}
			else if (Q7bFrequent && (Q7cNo || Q7cNoResponse)) {
				calculatedScore = 6;
			}
			else if (Q6bBitLess && (Q6cYes || Q6cNoResponse)) {
				calculatedScore = 7;
			}
			else if (Q7bOccasional && (Q7cNo || Q7cNoResponse)) {
				calculatedScore = 7;
			}
		} // if (calculatedScore == -1)
		
		if (calculatedScore == -1) {
			boolean Q8aNoResponse = false;
			boolean Q8aNo = false;
			boolean Q8aYes = false;
		    boolean Q8bNoResponse = false;
		    boolean Q8bNo = false;
		    boolean Q8bYes = false;
		    int gosecrrntprobindIndex = -1;
		    int gosesimlrprobpreinjryindIndex = -1;
		    
		    for (i = 0; i < columnNameVector.size() && (gosecrrntprobindIndex == -1); i++) {
				if (columnNameVector.get(i).equalsIgnoreCase("gosecrrntprobind")) {
					gosecrrntprobindIndex = i;	
				}
			}
			
			if (gosecrrntprobindIndex != -1) {
				data = dataVector.get(gosecrrntprobindIndex);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					Q8aNoResponse = true;
				}
			    else if (data.equalsIgnoreCase("No")) {
				    Q8aNo = true;	
				}
			    else if (data.equalsIgnoreCase("Yes")) {
				    Q8aYes = true;	
				}
			} // if (gosecrrntprobindIndex != -1)
			
			for (i = 0; i < columnNameVector.size() && (gosesimlrprobpreinjryindIndex == -1); i++) {
				if (columnNameVector.get(i).equalsIgnoreCase("gosesimlrprobpreinjryind")) {
					gosesimlrprobpreinjryindIndex = i;	
				}
			}
			
			if (gosesimlrprobpreinjryindIndex != -1) {
				data = dataVector.get(gosesimlrprobpreinjryindIndex);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					Q8bNoResponse = true;
				}
			    else if (data.equalsIgnoreCase("No")) {
				    Q8bNo = true;	
				}
			    else if (data.equalsIgnoreCase("Yes")) {
				    Q8bYes = true;	
				}
			} // if (gosesimlrprobpreinjryindIndex != -1)
			
			if (Q8aYes && (Q8bNo || Q8bNoResponse)) {
				calculatedScore = 7;
			}
			else if (Q8aNo && (Q8bNo || Q8bNoResponse)) {
				calculatedScore = 8;
			}
		} // if (calculatedScore == -1)
		
		String calculatedString;
		if (calculatedScore == -1) {
			calculatedString = " ";
		}
		else {
			calculatedString = String.valueOf(calculatedScore);
		}
		
		if ((!blankDataScore) && (calculatedScore == -1)) {
			message =
					String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
							guid,
							getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(glasgowoutcomescalextscoreIndex),
							rgTable.getRepeatableGroupName(),
							rgTable.getColumnName(glasgowoutcomescalextscoreIndex));

			message =
					message
							+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,
									dataScore);
			table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
					rgTable.getDataFilePositionMapping(glasgowoutcomescalextscoreIndex), message));	
		}
		
		else if ((blankDataScore && (calculatedScore != -1)) || (goodListedScore && (listedScore != calculatedScore)) || 
				((!goodListedScore) && (!blankDataScore))) {
			message =
					String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
							guid,
							getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(glasgowoutcomescalextscoreIndex),
							rgTable.getRepeatableGroupName(),
							rgTable.getColumnName(glasgowoutcomescalextscoreIndex));

			message =
					message
							+ String.format(ApplicationsConstants.ERR_INCORRECT_MINIMUM,
									dataScore, calculatedString);	
			table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
					rgTable.getDataFilePositionMapping(glasgowoutcomescalextscoreIndex), message));	
		}
        } // for (int group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++)
    	} // for (int repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) 
	}

	
	private void validateSWLS(StructuralFormStructure structure,
			Vector<Vector<Vector<String>>> columnNameVector3, 
			Vector<Vector<Vector<String>>>dataVector3, int subject_row_id, 
		    DataStructureTable table) throws Exception {
	    // 2 possible sets of columnNameVectors and accompanying dataVectors:
	
	    // group_column_index 0 group_columnName guid
	    // group_column_index 1 group_columnName subjectidnum
	    // group_column_index 2 group_columnName ageyrs
	    // group_column_index 3 group_columnName visitdate
	    // group_column_index 4 group_columnName sitename
	    // group_column_index 5 group_columnName dayssincebaseline
	    // group_column_index 6 group_columnName casecontrlind
	    // group_column_index 7 group_columnName generalnotestxt

	    // group_column_index 0 group_columnName swlslifclosidlscore
	    // group_column_index 1 group_columnName swlslifcondexcllncscore
	    // group_column_index 2 group_columnName swlslifsatfctnscore
	    // group_column_index 3 group_columnName swlslifachvmntscore
	    // group_column_index 4 group_columnName swlslifchngscore
	    // group_column_index 5 group_columnName swlstotalscore
		
		Vector<Vector<String>> columnNameVector2;
        Vector<Vector<String>> dataVector2;
        Vector<String> columnNameVector;
        Vector<String> dataVector;
        String message;
        String guid = null;
        int repeatable_group_id;
        RepeatableGroupTable rgTable;
        ArrayList<Integer> rgEntries;
        int group_row_index;
        int i;
        
        guidloop:
        for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
   		 
    		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
    		dataVector2 = dataVector3.get(repeatable_group_id);
    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
    		rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
			for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
		        columnNameVector = columnNameVector2.get(group_row_index);
		        dataVector = dataVector2.get(group_row_index);
		        for (i = 0; i  < columnNameVector.size(); i++) {
					if (columnNameVector.get(i).equalsIgnoreCase("guid")) {
					    guid = dataVector.get(i);
					    break guidloop;
					}
		        }
			}
        }
        loop1:
        	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
        		dataVector2 = dataVector3.get(repeatable_group_id);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries =
						table.getAllReferences(subject_row_id, repeatable_group_id, null);
                

        for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
			columnNameVector = columnNameVector2.get(group_row_index);
			dataVector = dataVector2.get(group_row_index);
			int swlstotalscoreIndex = -1;
		for (i = 0; i < columnNameVector.size() && (swlstotalscoreIndex == -1); i++) {
			if (columnNameVector.get(i).equalsIgnoreCase("swlstotalscore")) {
				swlstotalscoreIndex = i;	
			}
		}
		
		if (swlstotalscoreIndex == -1) {
			// This is the columnNameVector with the guid.
			continue loop1;
		}
		
		String data = dataVector.get(swlstotalscoreIndex);
		if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
				(data.trim().isEmpty())) {
		    data = "blank";
		}
		int sum = 0;
        boolean goodSum = true;
        boolean blankFound = false;
		for (i = 0; i <= swlstotalscoreIndex-1; i++) {
		    if ((columnNameVector.get(i).equalsIgnoreCase("swlslifclosidlscore")) ||
		    	(columnNameVector.get(i).equalsIgnoreCase("swlslifcondexcllncscore")) ||
		    	(columnNameVector.get(i).equalsIgnoreCase("swlslifsatfctnscore")) ||
		    	(columnNameVector.get(i).equalsIgnoreCase("swlslifachvmntscore")) ||
		    	(columnNameVector.get(i).equalsIgnoreCase("swlslifchngscore"))) {
		    	String swlsData = dataVector.get(i);
		    	if ((swlsData == null) || (swlsData.isEmpty()) || (swlsData.trim() == null) ||
		    			(swlsData.trim().isEmpty())) {
		            blankFound = true;
		        }
		    	else {
			    	try {
	            	    sum += Integer.valueOf(swlsData);
	            	}
	            	catch (NumberFormatException e) {
	            		goodSum = false;
	            		message =
	    						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
	    								guid,
	    								getRawDataRow(subject_row_id, group_row_index),
	    								rgTable.getDataFilePositionMapping(i),
	    								rgTable.getRepeatableGroupName(),
	    								rgTable.getColumnName(i));

	    				message =
	    						message
	    								+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
	    										swlsData);
	    				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
	    						rgTable.getDataFilePositionMapping(i), message));
	            	}
		    	}
		    }
		} // for (int i = 0; i <= swlstotalscoreIndex-1; i++) 
		
		int listedSum = 0;
        boolean goodListedSum = true;
        try {
            listedSum = Integer.valueOf(data).intValue();
        }
        catch (NumberFormatException e) {
        	goodListedSum = false;
        	message =
					String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
							guid,
							getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(swlstotalscoreIndex),
							rgTable.getRepeatableGroupName(),
							rgTable.getColumnName(swlstotalscoreIndex));

			message =
					message
							+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
									data);
			table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
					rgTable.getDataFilePositionMapping(swlstotalscoreIndex), message));
        }
        
        if (blankFound) { 
            if  (!data.equalsIgnoreCase("blank")) {
        
	        	message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(swlstotalscoreIndex),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(swlstotalscoreIndex));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,
										data);
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(swlstotalscoreIndex), message));
            } // if  (!data.equalsIgnoreCase("blank"))
        } // if (blankFound)
        else if ((goodSum && goodListedSum && (listedSum != sum)) ||
        		(goodSum && (!goodListedSum))) {
        	message =
					String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
							guid,
							getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(swlstotalscoreIndex),
							rgTable.getRepeatableGroupName(),
							rgTable.getColumnName(swlstotalscoreIndex));

			message =
					message
							+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
									data, String.valueOf(sum));	
			table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
					rgTable.getDataFilePositionMapping(swlstotalscoreIndex), message));
        }
        } // for (int group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++)
        } // for (int repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) 
		
	}
	
	private void validateDHI(StructuralFormStructure structure,
			Vector<Vector<Vector<String>>> columnNameVector3, 
			Vector<Vector<Vector<String>>> dataVector3, int subject_row_id, 
			DataStructureTable table) throws Exception {
		// Structure Name:	DHI
		// table.getColumnCount = 4
		// repeatable_group_id = 0
		// rgTable.getRepeatableGroupName() = Main
		// rgTable.getSize() = 9
		// rgEntries.size() = 1
		// group_row_index = 0
		// columnNameVector.size() = 9
		// group_column_index = 0 group_column_name = guid data = TBI_INVWJ892MMQ
		// group_column_index = 1 group_column_name = subjectidnum data = OP007
		// group_column_index = 2 group_column_name = ageyrs data = 37
		// group_column_index = 3 group_column_name = vitstatus data = null
		// group_column_index = 4 group_column_name = visitdate data = null
		// group_column_index = 5 group_column_name = sitename data = Mountain Home Veterans Affairs Medical Center
		// group_column_index = 6 group_column_name = dayssincebaseline data = 0
		// group_column_index = 7 group_column_name = casecontrlind data = Control
		// group_column_index = 8 group_column_name = generalnotestxt data = N=130 subjects who 'Completed' the study are contained in this final data. This subject is a Dizzy Control
		// repeatable_group_id = 1
		// rgTable.getRepeatableGroupName() = Scoring
		// rgTable.getSize() = 4
		// rgEntries.size() = 1
		// group_row_index = 0
		// columnNameVector.size() = 4
		// group_column_index = 0 group_column_name = dhiemotionalscr data = 24
		// group_column_index = 1 group_column_name = dhiphysicalscr data = 8
		// group_column_index = 2 group_column_name = dhifunctionalscr data = 20
		// group_column_index = 3 group_column_name = dhitotalscr data = 42
		// repeatable_group_id = 2
		// rgTable.getRepeatableGroupName() = DHI
		// rgTable.getSize() = 25
		// rgEntries.size() = 1
		// group_row_index = 0
		// columnNameVector.size() = 25
		// group_column_index = 0 group_column_name = dhilookupincrprobscl data = null
		// group_column_index = 1 group_column_name = dhifeelfrustratedscl data = null
		// group_column_index = 2 group_column_name = dhirestricttravelscl data = null
		// group_column_index = 3 group_column_name = dhiwalkdownaislesupermarketscl data = null
		// group_column_index = 4 group_column_name = dhidiffgettingoutofbedscl data = null
		// group_column_index = 5 group_column_name = dhirestrictsocialactivitiesscl data = null
		// group_column_index = 6 group_column_name = dhidifficultyreadingscl data = null
		// group_column_index = 7 group_column_name = dhiambitiousactincrprobscl data = null
		// group_column_index = 8 group_column_name = dhiafrdleavehomewoaccompanyscl data = 0
		// group_column_index = 9 group_column_name = dhiembarrassedinfrontofothscl data = 2
		// group_column_index = 10 group_column_name = dhiquickmoveofheadincrprobscl data = 4
		// group_column_index = 11 group_column_name = dhiavoidheightsscl data = 4
		// group_column_index = 12 group_column_name = dhiturningoverinbedincrprobscl data = 2
		// group_column_index = 13 group_column_name = dhidifficultstrenuousworkscl data = 4
		// group_column_index = 14 group_column_name = dhiafraidpplthinkintoxscl data = 0
		// group_column_index = 15 group_column_name = dhidiffwalkingbyyourselfscl data = 2
		// group_column_index = 16 group_column_name = dhiwalkdnsidewalkincrprobscl data = 4
		// group_column_index = 17 group_column_name = dhidifftoconcentratescl data = 2
		// group_column_index = 18 group_column_name = dhidifftowalkinhouseindarkscl data = 2
		// group_column_index = 19 group_column_name = dhiafraidtostayhomealonescl data = 4
		// group_column_index = 20 group_column_name = dhifeelhandicappedscl data = 2
		// group_column_index = 21 group_column_name = dhistressonrelationshipsscl data = 2
		// group_column_index = 22 group_column_name = dhidepressedscl data = 4
		// group_column_index = 23 group_column_name = dhiprobintrfrwrspnsbltsscl data = 2
		// group_column_index = 24 group_column_name = dhibendoverincrprobscl data = 2
		// repeatable_group_id = 3
		// rgTable.getRepeatableGroupName() = Form Administration
		// rgTable.getSize() = 4
		// rgEntries.size() = 1
		// group_row_index = 0
		// columnNameVector.size() = 4
		// group_column_index = 0 group_column_name = datasource data = Participant/subject
		// group_column_index = 1 group_column_name = datasourceoth data = null
		// group_column_index = 2 group_column_name = contexttype data = After injury
		// group_column_index = 3 group_column_name = contexttypeoth data = null
		Vector<Vector<String>> columnNameVector2;
        Vector<Vector<String>> dataVector2;
        Vector<String> columnNameVector;
        Vector<String> dataVector;
        int i;
        String message;
        String data;
        Vector<String> bufColumnNameVector = new Vector<String>();
        Vector<String> bufDataVector = new Vector<String>();
        Vector<Integer> bufRepeatable_group_id = new Vector<Integer>();
        Vector<Integer> bufGroup_row_index = new Vector<Integer>();
        Vector<Integer> bufIndex = new Vector<Integer>();
        Vector<String>  bufRepeatableGroupName = new Vector<String>();
        String guid = null;
        int group_row_index = -1;
        int repeatable_group_id;
        RepeatableGroupTable rgTable = null;
        ArrayList<Integer> rgEntries = null;
        int index = -1;
        boolean haveDHIGroupName = false;
        
		 //System.out.println("table.getColumnCount = " + table.getColumnCount());
        	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
        		 
        		//System.out.println("repeatable_group_id = " + repeatable_group_id);
        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
        		dataVector2 = dataVector3.get(repeatable_group_id);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		//System.out.println("rgTable.getRepeatableGroupName() = " + rgTable.getRepeatableGroupName());
				//System.out.println("rgTable.getSize() = " + rgTable.getSize());
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				//System.out.println("rgEntries.size() = " + rgEntries.size());
				for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
        	//System.out.println("group_row_index = " + group_row_index);
			columnNameVector = columnNameVector2.get(group_row_index);
			dataVector = dataVector2.get(group_row_index);
			//System.out.println("columnNameVector.size() = " + columnNameVector.size());
			//for (i = 0; i < columnNameVector.size(); i++) {
			    //System.out.println("group_column_index = " + i +
			    	//" group_column_name = " + columnNameVector.get(i) +
			    	//" data = " + dataVector.get(i)) ;	
			 //}
			for (i = 0; i < columnNameVector.size(); i++) {
				bufColumnNameVector.add(columnNameVector.get(i));
				bufDataVector.add(dataVector.get(i));
				bufRepeatable_group_id.add(repeatable_group_id);
				bufGroup_row_index.add(group_row_index);
				bufIndex.add(i);
				bufRepeatableGroupName.add(rgTable.getRepeatableGroupName());
			}
			} // for (int group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++)
            } // for (int repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++)
        for (i = 0; i < bufColumnNameVector.size(); i++) {
        	if (bufColumnNameVector.get(i).equalsIgnoreCase("guid")) {
				guid = bufDataVector.get(i);
				break;
			}	
        } // for (i = 0; i < bufColumnNameVector.size(); i++)
        
	    
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("ageyrs")) {
				String ageData = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((ageData == null) || (ageData.isEmpty()) || (ageData.trim() == null) ||
						(ageData.trim().isEmpty())) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
				}	
				else {
					double age = Double.valueOf(ageData).doubleValue();
					if (age < 18) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.WARNING_INCORRECT_AGE,
										  ageData, "18 or more years");
						table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
						return;
					}
				}
				break;
			  } // if (bufColumnNameVector.get(i).equalsIgnoreCase("ageyrs"))
			} // for (i = 0; i < bufColumnNameVector.size(); i++)
	    
	    int numberScoresMissing = 0;
	    int numberEmotionalScoresMissing = 0;
	    int numberPhysicalScoresMissing  = 0;
	    int numberFunctionalScoresMissing = 0;
	    Vector<Integer>missingIndices = new Vector<Integer>();
	    Vector<Integer> missing_repeatable_group_id = new Vector<Integer>();
	    Vector<Integer> missing_group_row_index = new Vector<Integer>();
	    int value;
	    int numberScoresNotInteger = 0;
	    int numberEmotionalScoresNotInteger = 0;
	    int numberPhysicalScoresNotInteger = 0;
	    int numberFunctionalScoresNotInteger = 0;
	    Vector<Integer>notIntegerIndices = new Vector<Integer>();
	    Vector<Integer> notInteger_repeatable_group_id = new Vector<Integer>();
	    Vector<Integer> notInteger_group_row_index = new Vector<Integer>();
	    Vector<String>notIntegerData = new Vector<String>();
	    int numberWrongIntegerScores = 0;
	    int numberWrongEmotionalIntegerScores = 0;
	    int numberWrongPhysicalIntegerScores = 0;
	    int numberWrongFunctionalIntegerScores = 0;
	    Vector<Integer>wrongIntegerIndices = new Vector<Integer>();
	    Vector<Integer> wrongInteger_repeatable_group_id = new Vector<Integer>();
	    Vector<Integer> wrongInteger_group_row_index = new Vector<Integer>();
	    Vector<String>wrongIntegerData = new Vector<String>();
	    String permissibleValueString = "0;2;4";
	    
	    int calculatedEmotionalScore = 0;
	    int calculatedPhysicalScore = 0;
	    int calculatedFunctionalScore = 0;
	    int calculatedTotalScore = 0;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
	    	if (bufRepeatableGroupName.get(i).equalsIgnoreCase("DHI")) {
	    	haveDHIGroupName = true;
			if ((bufColumnNameVector.get(i).equalsIgnoreCase("dhiafraidtostayhomealonescl")) ||
			   (bufColumnNameVector.get(i).equalsIgnoreCase("dhifeelfrustratedscl")) ||	
			   (bufColumnNameVector.get(i).equalsIgnoreCase("dhidifftoconcentratescl")) ||
			   (bufColumnNameVector.get(i).equalsIgnoreCase("dhifeelhandicappedscl")) ||
			   (bufColumnNameVector.get(i).equalsIgnoreCase("dhidepressedscl")) ||
			   (bufColumnNameVector.get(i).equalsIgnoreCase("dhiafrdleavehomewoaccompanyscl")) ||
			   (bufColumnNameVector.get(i).equalsIgnoreCase("dhiafraidpplthinkintoxscl")) ||
			   (bufColumnNameVector.get(i).equalsIgnoreCase("dhiembarrassedinfrontofothscl")) ||
			   (bufColumnNameVector.get(i).equalsIgnoreCase("dhistressonrelationshipsscl"))) {
				data = bufDataVector.get(i);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
				    numberScoresMissing++;
				    numberEmotionalScoresMissing++;
				    missingIndices.add(bufIndex.get(i));
				    missing_repeatable_group_id.add(bufRepeatable_group_id.get(i));
					missing_group_row_index.add(bufGroup_row_index.get(i));
				}
				else {
					try {
				        value = Integer.valueOf(data).intValue();
				        if ((value == 0) || (value == 2) || (value == 4)) {
				            calculatedEmotionalScore += value;
				            calculatedTotalScore += value;
				        }
				        else {
				        	numberWrongIntegerScores++;
				        	numberWrongEmotionalIntegerScores++;
				        	wrongIntegerIndices.add(bufIndex.get(i));
						    wrongInteger_repeatable_group_id.add(bufRepeatable_group_id.get(i));
							wrongInteger_group_row_index.add(bufGroup_row_index.get(i));
	                        wrongIntegerData.add(data);
				        }
					}
					catch (NumberFormatException e) {
						numberScoresNotInteger++;
						numberEmotionalScoresNotInteger++;
					    notIntegerIndices.add(bufIndex.get(i));
					    notInteger_repeatable_group_id.add(bufRepeatable_group_id.get(i));
						notInteger_group_row_index.add(bufGroup_row_index.get(i));
                        notIntegerData.add(data);
					}
				}
			}
			else if ((bufColumnNameVector.get(i).equalsIgnoreCase("dhiwalkdownaislesupermarketscl")) ||
					   (bufColumnNameVector.get(i).equalsIgnoreCase("dhiambitiousactincrprobscl")) ||	
					   (bufColumnNameVector.get(i).equalsIgnoreCase("dhiwalkdnsidewalkincrprobscl")) ||
					   (bufColumnNameVector.get(i).equalsIgnoreCase("dhiturningoverinbedincrprobscl")) ||
					   (bufColumnNameVector.get(i).equalsIgnoreCase("dhilookupincrprobscl")) ||
					   (bufColumnNameVector.get(i).equalsIgnoreCase("dhiquickmoveofheadincrprobscl")) ||
					   (bufColumnNameVector.get(i).equalsIgnoreCase("dhibendoverincrprobscl"))) {
						data = bufDataVector.get(i);
						if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
								(data.trim().isEmpty())) {
						    numberScoresMissing++;
						    numberPhysicalScoresMissing++;
						    missingIndices.add(bufIndex.get(i));
						    missing_repeatable_group_id.add(bufRepeatable_group_id.get(i));
							missing_group_row_index.add(bufGroup_row_index.get(i));
						}
						else {
							try {
						        value = Integer.valueOf(data).intValue();
						        if ((value == 0) || (value == 2) || (value == 4)) {
						            calculatedPhysicalScore += value;
						            calculatedTotalScore += value;
						        }
						        else {
						        	numberWrongIntegerScores++;
						        	numberWrongPhysicalIntegerScores++;
						        	wrongIntegerIndices.add(bufIndex.get(i));
								    wrongInteger_repeatable_group_id.add(bufRepeatable_group_id.get(i));
									wrongInteger_group_row_index.add(bufGroup_row_index.get(i));
			                        wrongIntegerData.add(data);
						        }
							}
							catch (NumberFormatException e) {
								numberScoresNotInteger++;
								numberPhysicalScoresNotInteger++;
							    notIntegerIndices.add(bufIndex.get(i));
							    notInteger_repeatable_group_id.add(bufRepeatable_group_id.get(i));
								notInteger_group_row_index.add(bufGroup_row_index.get(i));
		                        notIntegerData.add(data);
							}
						}
					}
			else if ((bufColumnNameVector.get(i).equalsIgnoreCase("dhidiffgettingoutofbedscl")) ||
					   (bufColumnNameVector.get(i).equalsIgnoreCase("dhirestricttravelscl")) ||	
					   (bufColumnNameVector.get(i).equalsIgnoreCase("dhidifficultstrenuousworkscl")) ||
					   (bufColumnNameVector.get(i).equalsIgnoreCase("dhirestrictsocialactivitiesscl")) ||
					   (bufColumnNameVector.get(i).equalsIgnoreCase("dhiavoidheightsscl")) ||
					   (bufColumnNameVector.get(i).equalsIgnoreCase("dhiprobintrfrwrspnsbltsscl")) ||
					   (bufColumnNameVector.get(i).equalsIgnoreCase("dhidiffwalkingbyyourselfscl")) ||
					   (bufColumnNameVector.get(i).equalsIgnoreCase("dhidifftowalkinhouseindarkscl")) ||
					   (bufColumnNameVector.get(i).equalsIgnoreCase("dhidifficultyreadingscl"))) {
						data = bufDataVector.get(i);
						if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
								(data.trim().isEmpty())) {
						    numberScoresMissing++;
						    numberFunctionalScoresMissing++;
						    missingIndices.add(bufIndex.get(i));
						    missing_repeatable_group_id.add(bufRepeatable_group_id.get(i));
							missing_group_row_index.add(bufGroup_row_index.get(i));
						}
						else {
							try {
						        value = Integer.valueOf(data).intValue();
						        if ((value == 0) || (value == 2) || (value == 4)) {
						            calculatedFunctionalScore += value;
						            calculatedTotalScore += value;
						        }
						        else {
						        	numberWrongIntegerScores++;
						        	numberWrongFunctionalIntegerScores++;
						        	wrongIntegerIndices.add(bufIndex.get(i));
								    wrongInteger_repeatable_group_id.add(bufRepeatable_group_id.get(i));
									wrongInteger_group_row_index.add(bufGroup_row_index.get(i));
			                        wrongIntegerData.add(data);
						        }
							}
							catch (NumberFormatException e) {
								numberScoresNotInteger++;
								numberFunctionalScoresNotInteger++;
							    notIntegerIndices.add(bufIndex.get(i));
							    notInteger_repeatable_group_id.add(bufRepeatable_group_id.get(i));
								notInteger_group_row_index.add(bufGroup_row_index.get(i));
		                        notIntegerData.add(data);
							}
						}
					}
	    	}
	    }
	    
	    if (numberScoresMissing >= 1) {
	    	for (i = 0; i < numberScoresMissing; i++) {
	    		rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id.get(i));
				rgEntries = table.getAllReferences(subject_row_id, 
						missing_repeatable_group_id.get(i), null);
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, missing_group_row_index.get(i)),
								rgTable.getDataFilePositionMapping(missingIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(missingIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index.get(i)),
						rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
	    	}	
	    } // if (numberScoresMissing >= 1)
	    
	    if (numberScoresNotInteger >= 1) {
	    	for (i = 0; i < numberScoresNotInteger; i++) {
	    		rgTable = table.getRepeatableGroupTable(notInteger_repeatable_group_id.get(i));
				rgEntries = table.getAllReferences(subject_row_id, 
						notInteger_repeatable_group_id.get(i), null);
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, notInteger_group_row_index.get(i)),
								rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(notIntegerIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
										notIntegerData.get(i));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, notInteger_group_row_index.get(i)),
						rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)), message));
	    	}	
	    } // if (numberScoresNotInteger >= 1)
	    
	    if (numberWrongIntegerScores >= 1) {
	    	for (i = 0; i < numberWrongIntegerScores; i++) {
	    		rgTable = table.getRepeatableGroupTable(wrongInteger_repeatable_group_id.get(i));
				rgEntries = table.getAllReferences(subject_row_id, 
						wrongInteger_repeatable_group_id.get(i), null);
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, wrongInteger_group_row_index.get(i)),
								rgTable.getDataFilePositionMapping(wrongIntegerIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(wrongIntegerIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_NOT_PERMISSIBLE_VALUE,
										wrongIntegerData.get(i), permissibleValueString);
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, wrongInteger_group_row_index.get(i)),
						rgTable.getDataFilePositionMapping(wrongIntegerIndices.get(i)), message));
	    	}	
	    } // if (numberWrongIntegerScores >= 1)
	    
	    int actualEmotionalScore = -1;
	    int actualPhysicalScore = -1;
	    int actualFunctionalScore = -1;
	    int actualTotalScore = -1;
	    boolean haveScoring = false;
	    
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
	    	if (bufRepeatableGroupName.get(i).equalsIgnoreCase("Scoring")) {
	    	haveScoring = true;
			if (bufColumnNameVector.get(i).equalsIgnoreCase("dhiemotionalscr")) {
				data = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
			    if ((numberEmotionalScoresMissing == 0) && (numberEmotionalScoresNotInteger == 0) &&
			    		(numberWrongEmotionalIntegerScores == 0)) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message)); 
			    }
				}
				else if ((numberEmotionalScoresMissing > 0) || (numberEmotionalScoresNotInteger > 0) ||
				    		(numberWrongEmotionalIntegerScores > 0) || (!haveDHIGroupName)) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED, data);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message)); 
				}
				else {
					try {
				        actualEmotionalScore = Integer.valueOf(data).intValue();	
					}
				    catch (NumberFormatException e) {
				    	message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
												data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
				    }
				}
				break;
			}
	    	}
	    }
	    
	    if ((numberEmotionalScoresMissing == 0) && (numberEmotionalScoresNotInteger == 0) &&
	    		(numberWrongEmotionalIntegerScores == 0) && (actualEmotionalScore > -1)) {
	    	if (actualEmotionalScore != calculatedEmotionalScore) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(index));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
										String.valueOf(actualEmotionalScore), String.valueOf(calculatedEmotionalScore));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(index), message));
	    	}
	    }
	    
	    if ((numberEmotionalScoresMissing == 0) && (numberEmotionalScoresNotInteger == 0) &&
	    		(numberWrongEmotionalIntegerScores == 0) && (!haveScoring)) {
	    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Scoring")) {
	    			message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, 0),
									rgTable.getDataFilePositionMapping(0),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(0));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(0), message));
	    			break;
	    		}
	    	}

	    }
	    
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
	    	if (bufRepeatableGroupName.get(i).equalsIgnoreCase("Scoring")) {
	    	haveScoring = true;
			if (bufColumnNameVector.get(i).equalsIgnoreCase("dhiphysicalscr")) {
				data = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
			    if ((numberPhysicalScoresMissing == 0) && (numberPhysicalScoresNotInteger == 0) &&
			    		(numberWrongPhysicalIntegerScores == 0)) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message)); 
			    }
				}
				else if ((numberPhysicalScoresMissing > 0) || (numberPhysicalScoresNotInteger > 0) ||
				    		(numberWrongPhysicalIntegerScores > 0) || (!haveDHIGroupName)) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED, data);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message)); 
				}
				else {
					try {
				        actualPhysicalScore = Integer.valueOf(data).intValue();	
					}
				    catch (NumberFormatException e) {
				    	message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
												data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
				    }
				}
				break;
			}
	    	}
	    }
	    
	    if ((numberPhysicalScoresMissing == 0) && (numberPhysicalScoresNotInteger == 0) &&
	    		(numberWrongPhysicalIntegerScores == 0) && (actualPhysicalScore > -1)) {
	    	if (actualPhysicalScore != calculatedPhysicalScore) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(index));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
										String.valueOf(actualPhysicalScore), String.valueOf(calculatedPhysicalScore));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(index), message));
	    	}
	    }
	    
	    if ((numberPhysicalScoresMissing == 0) && (numberPhysicalScoresNotInteger == 0) &&
	    		(numberWrongPhysicalIntegerScores == 0) && (!haveScoring)) {
	    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Scoring")) {
	    			message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, 0),
									rgTable.getDataFilePositionMapping(1),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(1));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(1), message));
	    			break;
	    		}
	    	}

	    }

	    for (i = 0; i < bufColumnNameVector.size(); i++) {
	    	if (bufRepeatableGroupName.get(i).equalsIgnoreCase("Scoring")) {
	    	haveScoring = true;
			if (bufColumnNameVector.get(i).equalsIgnoreCase("dhifunctionalscr")) {
				data = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
			    if ((numberFunctionalScoresMissing == 0) && (numberFunctionalScoresNotInteger == 0) &&
			    		(numberWrongFunctionalIntegerScores == 0)) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message)); 
			    }
				}
				else if ((numberFunctionalScoresMissing > 0) || (numberFunctionalScoresNotInteger > 0) ||
				    		(numberWrongFunctionalIntegerScores > 0) || (!haveDHIGroupName)) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED, data);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message)); 
				}
				else {
					try {
				        actualFunctionalScore = Integer.valueOf(data).intValue();	
					}
				    catch (NumberFormatException e) {
				    	message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
												data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
				    }
				}
				break;
			}
	    	}
	    }
	    
	    if ((numberFunctionalScoresMissing == 0) && (numberFunctionalScoresNotInteger == 0) &&
	    		(numberWrongFunctionalIntegerScores == 0) && (actualFunctionalScore > -1)) {
	    	if (actualFunctionalScore != calculatedFunctionalScore) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(index));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
										String.valueOf(actualFunctionalScore), String.valueOf(calculatedFunctionalScore));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(index), message));
	    	}
	    }
	    
	    if ((numberFunctionalScoresMissing == 0) && (numberFunctionalScoresNotInteger == 0) &&
	    		(numberWrongFunctionalIntegerScores == 0) && (!haveScoring)) {
	    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Scoring")) {
	    			message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, 0),
									rgTable.getDataFilePositionMapping(2),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(2));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(2), message));
	    			break;
	    		}
	    	}

	    }
	    
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
	    	if (bufRepeatableGroupName.get(i).equalsIgnoreCase("Scoring")) {
	    	haveScoring = true;
			if (bufColumnNameVector.get(i).equalsIgnoreCase("dhitotalscr")) {
				data = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
			    if ((numberScoresMissing == 0) && (numberScoresNotInteger == 0) &&
			    		(numberWrongIntegerScores == 0)) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message)); 
			    }
				}
				else if ((numberScoresMissing > 0) || (numberScoresNotInteger > 0) ||
				    		(numberWrongIntegerScores > 0) || (!haveDHIGroupName)) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED, data);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message)); 
				}
				else {
					try {
				        actualTotalScore = Integer.valueOf(data).intValue();	
					}
				    catch (NumberFormatException e) {
				    	message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
												data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
				    }
				}
				break;
			}
	    	}
	    }
	    
	    if ((numberScoresMissing == 0) && (numberScoresNotInteger == 0) &&
	    		(numberWrongIntegerScores == 0) && (actualTotalScore > -1)) {
	    	if (actualTotalScore != calculatedTotalScore) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(index));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
										String.valueOf(actualTotalScore), String.valueOf(calculatedTotalScore));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(index), message));
	    	}
	    }
	    
	    if ((numberScoresMissing == 0) && (numberScoresNotInteger == 0) &&
	    		(numberWrongIntegerScores == 0) && (!haveScoring)) {
	    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Scoring")) {
	    			message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, 0),
									rgTable.getDataFilePositionMapping(3),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(3));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(3), message));
	    			break;
	    		}
	    	}

	    }

	}
	
	private void validateHIT6(StructuralFormStructure structure,
			Vector<Vector<Vector<String>>> columnNameVector3, 
			Vector<Vector<Vector<String>>> dataVector3, int subject_row_id, 
			DataStructureTable table) throws Exception {
		//Structure Name:	HIT6
		//table.getColumnCount = 4
		//repeatable_group_id = 0
		//rgTable.getRepeatableGroupName() = HIT-6
		//rgTable.getSize() = 6
		//rgEntries.size() = 1
		//group_row_index = 0
		//columnNameVector.size() = 6
		//group_column_index = 0 group_column_name = hit6headachseverpainfreqscl data = Rarely
		//group_column_index = 1 group_column_name = hit6headachlimitdlyactfreqscl data = Never
		//group_column_index = 2 group_column_name = hit6headachwishliedownfreqscl data = Rarely
		//group_column_index = 3 group_column_name = hit6headachtireddlyactfreqscl data = Never
		//group_column_index = 4 group_column_name = hit6headachfedupirritafreqscl data = Never
		//group_column_index = 5 group_column_name = hit6headachlimconcentrfreqscl data = Rarely
		//repeatable_group_id = 1
		//rgTable.getRepeatableGroupName() = Main
		//rgTable.getSize() = 9
		//rgEntries.size() = 1
		//group_row_index = 0
		//columnNameVector.size() = 9
		//group_column_index = 0 group_column_name = guid data = TBILD409APN
		//group_column_index = 1 group_column_name = subjectidnum data = 1001
		//group_column_index = 2 group_column_name = ageyrs data = null
		//group_column_index = 3 group_column_name = vitstatus data = null
		//group_column_index = 4 group_column_name = visitdate data = null
		//group_column_index = 5 group_column_name = sitename data = null
		//group_column_index = 6 group_column_name = dayssincebaseline data = null
		//group_column_index = 7 group_column_name = casecontrlind data = null
		//group_column_index = 8 group_column_name = generalnotestxt data = age missing
		//repeatable_group_id = 2
		//rgTable.getRepeatableGroupName() = Total Score
		//rgTable.getSize() = 1
		//rgEntries.size() = 1
		//group_row_index = 0
		//columnNameVector.size() = 1
		//group_column_index = 0 group_column_name = hit6ttlscore data = 42
		//repeatable_group_id = 3
		//rgTable.getSize() = 4
		//rgEntries.size() = 0
		

		Vector<Vector<String>> columnNameVector2;
        Vector<Vector<String>> dataVector2;
        Vector<String> columnNameVector;
        Vector<String> dataVector;
        int i;
        String message;
        String data;
        Vector<String> bufColumnNameVector = new Vector<String>();
        Vector<String> bufDataVector = new Vector<String>();
        Vector<Integer> bufRepeatable_group_id = new Vector<Integer>();
        Vector<Integer> bufGroup_row_index = new Vector<Integer>();
        Vector<Integer> bufIndex = new Vector<Integer>();
        Vector<String>  bufRepeatableGroupName = new Vector<String>();
        String guid = null;
        int group_row_index = -1;
        int repeatable_group_id;
        RepeatableGroupTable rgTable = null;
        ArrayList<Integer> rgEntries = null;
        int index = -1;
        
		 //System.out.println("table.getColumnCount = " + table.getColumnCount());
        	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
        		 
        		//System.out.println("repeatable_group_id = " + repeatable_group_id);
        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
        		dataVector2 = dataVector3.get(repeatable_group_id);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		//System.out.println("rgTable.getRepeatableGroupName() = " + rgTable.getRepeatableGroupName());
				//System.out.println("rgTable.getSize() = " + rgTable.getSize());
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				//System.out.println("rgEntries.size() = " + rgEntries.size());
				for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
        	//System.out.println("group_row_index = " + group_row_index);
			columnNameVector = columnNameVector2.get(group_row_index);
			dataVector = dataVector2.get(group_row_index);
			//System.out.println("columnNameVector.size() = " + columnNameVector.size());
			//for (i = 0; i < columnNameVector.size(); i++) {
			    //System.out.println("group_column_index = " + i +
			    	//" group_column_name = " + columnNameVector.get(i) +
			    	//" data = " + dataVector.get(i)) ;	
			 //}
			for (i = 0; i < columnNameVector.size(); i++) {
				bufColumnNameVector.add(columnNameVector.get(i));
				bufDataVector.add(dataVector.get(i));
				bufRepeatable_group_id.add(repeatable_group_id);
				bufGroup_row_index.add(group_row_index);
				bufIndex.add(i);
				bufRepeatableGroupName.add(rgTable.getRepeatableGroupName());
			}
			} // for (int group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++)
            } // for (int repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++)
        for (i = 0; i < bufColumnNameVector.size(); i++) {
        	if (bufColumnNameVector.get(i).equalsIgnoreCase("guid")) {
				guid = bufDataVector.get(i);
				break;
			}	
        } // for (i = 0; i < bufColumnNameVector.size(); i++)
        
	    
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("ageyrs")) {
				String ageData = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((ageData == null) || (ageData.isEmpty()) || (ageData.trim() == null) ||
						(ageData.trim().isEmpty())) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
				}	
				else {
					double age = Double.valueOf(ageData).doubleValue();
					if (age < 18) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.WARNING_INCORRECT_AGE,
										  ageData, "18 or more years");
						table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
						return;
					}
				}
				break;
			  } // if (bufColumnNameVector.get(i).equalsIgnoreCase("ageyrs"))
			} // for (i = 0; i < bufColumnNameVector.size(); i++)
	    
	    int numberScoresMissing = 0;
	    Vector<Integer>missingIndices = new Vector<Integer>();
	    Vector<Integer> missing_repeatable_group_id = new Vector<Integer>();
	    Vector<Integer> missing_group_row_index = new Vector<Integer>();
	    int value;
	    int numberHitStringNotCorrect = 0;
	    Vector<Integer>incorrectHitStringIndices = new Vector<Integer>();
	    Vector<Integer> incorrectHitString_repeatable_group_id = new Vector<Integer>();
	    Vector<Integer> incorrectHitString_group_row_index = new Vector<Integer>();
	    Vector<String>incorrectHitStringData = new Vector<String>();
	    String permissibleValueString = "Always;Never;Rarely;Sometimes;Very often";
	    int calculatedTotalScore = 0;
	    
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
	    	if (bufRepeatableGroupName.get(i).equalsIgnoreCase("HIT-6")) {
			if ((bufColumnNameVector.get(i).equalsIgnoreCase("hit6headachseverpainfreqscl")) ||
			   (bufColumnNameVector.get(i).equalsIgnoreCase("hit6headachlimitdlyactfreqscl")) ||	
			   (bufColumnNameVector.get(i).equalsIgnoreCase("hit6headachwishliedownfreqscl")) ||
			   (bufColumnNameVector.get(i).equalsIgnoreCase("hit6headachtireddlyactfreqscl")) ||
			   (bufColumnNameVector.get(i).equalsIgnoreCase("hit6headachfedupirritafreqscl")) ||
			   (bufColumnNameVector.get(i).equalsIgnoreCase("hit6headachlimconcentrfreqscl"))) {
				data = bufDataVector.get(i);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
				    numberScoresMissing++;
				    missingIndices.add(bufIndex.get(i));
				    missing_repeatable_group_id.add(bufRepeatable_group_id.get(i));
					missing_group_row_index.add(bufGroup_row_index.get(i));
				}
				else if (!((data.equalsIgnoreCase("Always")) ||
							(data.equalsIgnoreCase("Never"))||
							(data.equalsIgnoreCase("Rarely"))||
							(data.equalsIgnoreCase("Sometimes"))||
							(data.equalsIgnoreCase("Very often")))) {
					numberHitStringNotCorrect++;
					incorrectHitStringIndices.add(bufIndex.get(i));
					incorrectHitString_repeatable_group_id.add(bufRepeatable_group_id.get(i));
					incorrectHitString_group_row_index.add(bufGroup_row_index.get(i));
					incorrectHitStringData.add(data);
				}
				else if (data.equalsIgnoreCase("Always")) {
				    calculatedTotalScore += 13;
				}
				else if (data.equalsIgnoreCase("Never")) {
				    calculatedTotalScore += 6;
				}
				else if (data.equalsIgnoreCase("Rarely")) {
				    calculatedTotalScore += 8;
				}
				else if (data.equalsIgnoreCase("Sometimes")) {
				    calculatedTotalScore += 10;
				}
				else if (data.equalsIgnoreCase("Very often")) {
				    calculatedTotalScore += 11;
				}
			}
	    }
	    }
	    
	    if (numberScoresMissing >= 1) {
	    	for (i = 0; i < numberScoresMissing; i++) {
	    		rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id.get(i));
				rgEntries = table.getAllReferences(subject_row_id, 
						missing_repeatable_group_id.get(i), null);
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, missing_group_row_index.get(i)),
								rgTable.getDataFilePositionMapping(missingIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(missingIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index.get(i)),
						rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
	    	}	
	    } // if (numberScoresMissing >= 1)
	    
	    if (numberHitStringNotCorrect >= 1) {
	    	for (i = 0; i < numberHitStringNotCorrect; i++) {
	    		rgTable = table.getRepeatableGroupTable(incorrectHitString_repeatable_group_id.get(i));
				rgEntries = table.getAllReferences(subject_row_id, 
						incorrectHitString_repeatable_group_id.get(i), null);
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, incorrectHitString_group_row_index.get(i)),
								rgTable.getDataFilePositionMapping(incorrectHitStringIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(incorrectHitStringIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_NOT_PERMISSIBLE_VALUE,
										incorrectHitStringData.get(i), permissibleValueString);
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, incorrectHitString_group_row_index.get(i)),
						rgTable.getDataFilePositionMapping(incorrectHitStringIndices.get(i)), message));
	    	}	
	    } // if (numberHitStringNotCorrect >= 1)
	    
	    int actualTotalScore = -1;
	    boolean haveTotalScoreGroup = false;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
	    	if (bufRepeatableGroupName.get(i).equalsIgnoreCase("Total Score")) {
	        haveTotalScoreGroup = true; 
			if (bufColumnNameVector.get(i).equalsIgnoreCase("hit6ttlscore")) {
				data = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
				if ((numberScoresMissing == 0) && (numberHitStringNotCorrect == 0)) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));    	
				}
				}
				else if ((numberScoresMissing > 0) || (numberHitStringNotCorrect > 0)) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED, data);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));    		
				}
				else {
					try {
				        actualTotalScore = Integer.valueOf(data).intValue();	
					}
				    catch (NumberFormatException e) {
				    	message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
												data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
				    }
				}
				break;
			}
	    	}
	    }
	    
	    if ((numberScoresMissing >= 1) || (numberHitStringNotCorrect >= 1)) {
	    	return;
	    }
	    
        if (!haveTotalScoreGroup) {
	    	
	    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Total Score")) {
	    			message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, 0),
									rgTable.getDataFilePositionMapping(0),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(0));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(0), message));
	    			break;
	    		}
	    	}
	    }

		if (actualTotalScore > -1) {
			if (actualTotalScore != calculatedTotalScore) {
				message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(index));
		
				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
										String.valueOf(actualTotalScore), String.valueOf(calculatedTotalScore));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(index), message));
			}
		}
	    
	}
	
	private void validateFIM(StructuralFormStructure structure,
			Vector<Vector<Vector<String>>> columnNameVector3, 
			Vector<Vector<Vector<String>>> dataVector3, int subject_row_id, 
			DataStructureTable table) throws Exception {
		// Structure Name:	FIM_Instrument
		// table.getColumnCount = 7
		// repeatable_group_id = 0
		// rgTable.getRepeatableGroupName() = Form Administration
		// rgTable.getSize() = 6
		// rgEntries.size() = 0
		// repeatable_group_id = 1
		// rgTable.getRepeatableGroupName() = Expression
		// rgTable.getSize() = 3
		// rgEntries.size() = 1
		// group_row_index = 0
		// columnNameVector.size() = 3
		// group_column_index = 0 group_column_name = visittyp data = Follow-Up
		// group_column_index = 1 group_column_name = fimcognexpressionscl data = 7
		// group_column_index = 2 group_column_name = fimcogncompehensexpressmodetyp data = null
		// repeatable_group_id = 2
		// rgTable.getRepeatableGroupName() = Comprehension
		// rgTable.getSize() = 3
		// rgEntries.size() = 1
		// group_row_index = 0
		// columnNameVector.size() = 3
		// group_column_index = 0 group_column_name = visittyp data = Follow-Up
		// group_column_index = 1 group_column_name = fimcogncompehensscl data = 7
		// group_column_index = 2 group_column_name = fimcogncompehensexpressmodetyp data = null
		// repeatable_group_id = 3
		// rgTable.getRepeatableGroupName() = Social Cognition
		//mrgTable.getSize() = 4
		// rgEntries.size() = 1
		// group_row_index = 0
		// columnNameVector.size() = 4
		// group_column_index = 0 group_column_name = visittyp data = Follow-Up
		// group_column_index = 1 group_column_name = fimcognsoclinteractscl data = 6
		// group_column_index = 2 group_column_name = fimcognprobsolvscl data = 5
		// group_column_index = 3 group_column_name = fimcognmemoryscl data = 7
		// repeatable_group_id = 4
		// rgTable.getRepeatableGroupName() = Motor subscale
		// rgTable.getSize() = 15
		// rgEntries.size() = 1
		// group_row_index = 0
		// columnNameVector.size() = 15
		// group_column_index = 0 group_column_name = visittyp data = Follow-Up
		// group_column_index = 1 group_column_name = fimmotorselfcareeatingscl data = 7
		// group_column_index = 2 group_column_name = fimmotorselfcaregroomingscl data = 7
		// group_column_index = 3 group_column_name = fimmotorselfcarebathingscl data = 7
		// group_column_index = 4 group_column_name = fimmotorselfcaredreslowbodyscl data = 7
		// group_column_index = 5 group_column_name = fimmotorselfcaredresuppbodyscl data = 7
		// group_column_index = 6 group_column_name = fimmotorselfcaretoiletingscl data = 7
		// group_column_index = 7 group_column_name = fimmotorsphcontrbladdermgmtscl data = 6
		// group_column_index = 8 group_column_name = fimmotorsphcontrbowelmgmtscl data = 7
		// group_column_index = 9 group_column_name = fimmotortransfbedchairwchscl data = 7
		// group_column_index = 10 group_column_name = fimmotortransftoiletscl data = 7
		// group_column_index = 11 group_column_name = fimmotortransftubshowerscl data = 7
		// group_column_index = 12 group_column_name = fimmotorlocomotionwalkwcscl data = 6
		// group_column_index = 13 group_column_name = fimmotorlocomotionmodetyp data = W
		// group_column_index = 14 group_column_name = fimmotorlocomotionstairsscl data = 6
		// repeatable_group_id = 5
		// rgTable.getRepeatableGroupName() = Main
		// rgTable.getSize() = 9
		// rgEntries.size() = 1
		// group_row_index = 0
		// columnNameVector.size() = 9
		// group_column_index = 0 group_column_name = guid data = TBI_INVAA005PE4
		// group_column_index = 1 group_column_name = subjectidnum data = null
		// group_column_index = 2 group_column_name = ageyrs data = 13
		// group_column_index = 3 group_column_name = vitstatus data = null
		// group_column_index = 4 group_column_name = visitdate data = null
		// group_column_index = 5 group_column_name = sitename data = TBIMS NDSC
		// group_column_index = 6 group_column_name = dayssincebaseline data = 772
		// group_column_index = 7 group_column_name = casecontrlind data = Age is out of range
		// group_column_index = 8 group_column_name = generalnotestxt data = null
		// repeatable_group_id = 6
		// rgTable.getRepeatableGroupName() = Scores
		// rgTable.getSize() = 4
		// rgEntries.size() = 1
		// group_row_index = 0
		// columnNameVector.size() = 4
		// group_column_index = 0 group_column_name = visittyp data = Follow-Up
		// group_column_index = 1 group_column_name = fiminstrmotorsubscore data = 88
		// group_column_index = 2 group_column_name = fiminstrcognfunctsubscore data = 32
		// group_column_index = 3 group_column_name = fiminstrtotalscore data = 120
		
		Vector<Vector<String>> columnNameVector2;
        Vector<Vector<String>> dataVector2;
        Vector<String> columnNameVector;
        Vector<String> dataVector;
        int i;
        String message;
        String data;
        Vector<String> bufColumnNameVector = new Vector<String>();
        Vector<String> bufDataVector = new Vector<String>();
        Vector<Integer> bufRepeatable_group_id = new Vector<Integer>();
        Vector<Integer> bufGroup_row_index = new Vector<Integer>();
        Vector<Integer> bufIndex = new Vector<Integer>();
        Vector<String>  bufRepeatableGroupName = new Vector<String>();
        String guid = null;
        int group_row_index = -1;
        int repeatable_group_id;
        RepeatableGroupTable rgTable = null;
        ArrayList<Integer> rgEntries = null;
        int index = -1;
        
		 //System.out.println("table.getColumnCount = " + table.getColumnCount());
        	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
        		 
        		//System.out.println("repeatable_group_id = " + repeatable_group_id);
        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
        		dataVector2 = dataVector3.get(repeatable_group_id);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		//System.out.println("rgTable.getRepeatableGroupName() = " + rgTable.getRepeatableGroupName());
				//System.out.println("rgTable.getSize() = " + rgTable.getSize());
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				//System.out.println("rgEntries.size() = " + rgEntries.size());
				for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
        	 //System.out.println("group_row_index = " + group_row_index);
			columnNameVector = columnNameVector2.get(group_row_index);
			dataVector = dataVector2.get(group_row_index);
			 //System.out.println("columnNameVector.size() = " + columnNameVector.size());
			 //for (i = 0; i < columnNameVector.size(); i++) {
			    //System.out.println("group_column_index = " + i +
			    	//" group_column_name = " + columnNameVector.get(i) +
			    	//" data = " + dataVector.get(i)) ;	
			 //}
			for (i = 0; i < columnNameVector.size(); i++) {
				bufColumnNameVector.add(columnNameVector.get(i));
				bufDataVector.add(dataVector.get(i));
				bufRepeatable_group_id.add(repeatable_group_id);
				bufGroup_row_index.add(group_row_index);
				bufIndex.add(i);
				bufRepeatableGroupName.add(rgTable.getRepeatableGroupName());
			}
			} // for (int group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++)
            } // for (int repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++)
        for (i = 0; i < bufColumnNameVector.size(); i++) {
        	if (bufColumnNameVector.get(i).equalsIgnoreCase("guid")) {
				guid = bufDataVector.get(i);
				break;
			}	
        } // for (i = 0; i < bufColumnNameVector.size(); i++)
        
	    
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("ageyrs")) {
				String ageData = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((ageData == null) || (ageData.isEmpty()) || (ageData.trim() == null) ||
						(ageData.trim().isEmpty())) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
				}	
				else {
					double age = Double.valueOf(ageData).doubleValue();
					if (age < 18) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.WARNING_INCORRECT_AGE,
										  ageData, "18 or more years");
						table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
						return;
					}
				}
				break;
			  } // if (bufColumnNameVector.get(i).equalsIgnoreCase("ageyrs"))
			} // for (i = 0; i < bufColumnNameVector.size(); i++)
	    
	    int numberScoresMissing = 0;
	    Vector<Integer>missingIndices = new Vector<Integer>();
	    Vector<Integer> missing_repeatable_group_id = new Vector<Integer>();
	    Vector<Integer> missing_group_row_index = new Vector<Integer>();
	    int value;
	    int numberScoresNotInteger = 0;
	    Vector<Integer>notIntegerIndices = new Vector<Integer>();
	    Vector<Integer> notInteger_repeatable_group_id = new Vector<Integer>();
	    Vector<Integer> notInteger_group_row_index = new Vector<Integer>();
	    Vector<String>notIntegerData = new Vector<String>();
	    int numberVisitTypNotCorrect = 0;
	    Vector<Integer>incorrectVisitIndices = new Vector<Integer>();
	    Vector<Integer> incorrectVisit_repeatable_group_id = new Vector<Integer>();
	    Vector<Integer> incorrectVisit_group_row_index = new Vector<Integer>();
	    Vector<String>incorrectVisitData = new Vector<String>();
	    String permissibleValueString = "Follow-Up;Inpatient admission;Discharge";
	    int calculatedMotorSubscore = 0;
	    int calculatedCognFunctSubscore = 0;
	    int calculatedTotalScore = 0;
	    int numberMotorScoresMissing = 0;
	    int numberMotorScoresNotInteger = 0;
	    int numberMotorVisitTypNotCorrect = 0;
	    int numberCognitionScoresMissing = 0;
	    int numberCognitionScoresNotInteger = 0;
	    int numberCognitionVisitTypNotCorrect = 0;
	    
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
	    	if (bufRepeatableGroupName.get(i).equalsIgnoreCase("Motor subscale")) {
			if ((bufColumnNameVector.get(i).equalsIgnoreCase("VisitTyp")) ||
			   (bufColumnNameVector.get(i).equalsIgnoreCase("FIMMotorSelfCareEatingScl")) ||	
			   (bufColumnNameVector.get(i).equalsIgnoreCase("FIMMotorSelfCareGroomingScl")) ||
			   (bufColumnNameVector.get(i).equalsIgnoreCase("FIMMotorSelfCareBathingScl")) ||
			   (bufColumnNameVector.get(i).equalsIgnoreCase("FIMMotorSelfCareDresLowBodyScl")) ||
			   (bufColumnNameVector.get(i).equalsIgnoreCase("FIMMotorSelfCareDresUppBodyScl")) ||
			   (bufColumnNameVector.get(i).equalsIgnoreCase("FIMMotorSelfCareToiletingScl")) ||
			   (bufColumnNameVector.get(i).equalsIgnoreCase("FIMMotorSphContrBladderMgmtScl")) ||
			   (bufColumnNameVector.get(i).equalsIgnoreCase("FIMMotorSphContrBowelMgmtScl")) ||
			   (bufColumnNameVector.get(i).equalsIgnoreCase("FIMMotorTransfBedChairWChScl")) ||
			   (bufColumnNameVector.get(i).equalsIgnoreCase("FIMMotorTransfToiletScl")) ||
			   (bufColumnNameVector.get(i).equalsIgnoreCase("FIMMotorTransfTubShowerScl")) ||
			   (bufColumnNameVector.get(i).equalsIgnoreCase("FIMMotorLocomotionWalkWCScl")) ||
			   (bufColumnNameVector.get(i).equalsIgnoreCase("FIMMotorLocomotionModeTyp")) ||
			   (bufColumnNameVector.get(i).equalsIgnoreCase("FIMMotorLocomotionStairsScl"))) {
				data = bufDataVector.get(i);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
				    numberScoresMissing++;
				    numberMotorScoresMissing++;
				    missingIndices.add(bufIndex.get(i));
				    missing_repeatable_group_id.add(bufRepeatable_group_id.get(i));
					missing_group_row_index.add(bufGroup_row_index.get(i));
				}
				else if (!((bufColumnNameVector.get(i).equalsIgnoreCase("VisitTyp")) ||
						   (bufColumnNameVector.get(i).equalsIgnoreCase("FIMMotorLocomotionModeTyp")))) {
					try {
				        value = Integer.valueOf(data).intValue();
				        calculatedMotorSubscore += value;
					}
					catch (NumberFormatException e) {
						numberScoresNotInteger++;
						numberMotorScoresNotInteger++;
					    notIntegerIndices.add(bufIndex.get(i));
					    notInteger_repeatable_group_id.add(bufRepeatable_group_id.get(i));
						notInteger_group_row_index.add(bufGroup_row_index.get(i));
                        notIntegerData.add(data);
					}
				}
				else if (bufColumnNameVector.get(i).equalsIgnoreCase("VisitTyp")) {
					if (!((data.equalsIgnoreCase("Follow-Up")) ||
							(data.equalsIgnoreCase("Inpatient admission"))||
							(data.equalsIgnoreCase("Discharge")))) {
						numberVisitTypNotCorrect++;
						numberMotorVisitTypNotCorrect++;
					    incorrectVisitIndices.add(bufIndex.get(i));
					    incorrectVisit_repeatable_group_id.add(bufRepeatable_group_id.get(i));
						incorrectVisit_group_row_index.add(bufGroup_row_index.get(i));
                        incorrectVisitData.add(data);
					}	
				}
			}
	    	} // if (bufRepeatableGroupName.get(i).equalsIgnoreCase("Motor subscale"))
	    	else if (bufRepeatableGroupName.get(i).equalsIgnoreCase("Comprehension")) {
	    		if ((bufColumnNameVector.get(i).equalsIgnoreCase("VisitTyp")) ||
	    				   (bufColumnNameVector.get(i).equalsIgnoreCase("FIMCognCompehensScl")) ||
	    				   (bufColumnNameVector.get(i).equalsIgnoreCase("FIMCognCompehensExpressModeTyp"))) {
	    						data = bufDataVector.get(i);
	    						if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
	    								(data.trim().isEmpty())) {
	    						    numberScoresMissing++;
	    						    missingIndices.add(bufIndex.get(i));
	    						    missing_repeatable_group_id.add(bufRepeatable_group_id.get(i));
	    						    missing_group_row_index.add(bufGroup_row_index.get(i));
	    						}
	    						else if (!((bufColumnNameVector.get(i).equalsIgnoreCase("VisitTyp")) ||
	    								   (bufColumnNameVector.get(i).equalsIgnoreCase("FIMCognCompehensExpressModeTyp")))) {
	    							try {
	    						        value = Integer.valueOf(data).intValue();
	    						        calculatedCognFunctSubscore += value;
	    							}
	    							catch (NumberFormatException e) {
	    								numberScoresNotInteger++;
	    							    notIntegerIndices.add(bufIndex.get(i));
	    							    notInteger_repeatable_group_id.add(bufRepeatable_group_id.get(i));
	    							    notInteger_group_row_index.add(bufGroup_row_index.get(i));
	    		                        notIntegerData.add(data);
	    							}
	    						}
	    						else if (bufColumnNameVector.get(i).equalsIgnoreCase("VisitTyp")) {
	    							if (!((data.equalsIgnoreCase("Follow-Up")) ||
	    									(data.equalsIgnoreCase("Inpatient admission"))||
	    									(data.equalsIgnoreCase("Discharge")))) {
	    								numberVisitTypNotCorrect++;
	    							    incorrectVisitIndices.add(bufIndex.get(i));
	    							    incorrectVisit_repeatable_group_id.add(bufRepeatable_group_id.get(i));
	    								incorrectVisit_group_row_index.add(bufGroup_row_index.get(i));
	    		                        incorrectVisitData.add(data);
	    							}	
	    						}
	    					}
	    	} // else if (bufRepeatableGroupName.get(i).equalsIgnoreCase("Comprehension"))
	    	else if (bufRepeatableGroupName.get(i).equalsIgnoreCase("Expression")) {
	    		if ((bufColumnNameVector.get(i).equalsIgnoreCase("VisitTyp")) ||
	    				   (bufColumnNameVector.get(i).equalsIgnoreCase("FIMCognExpressionScl")) ||
	    				   (bufColumnNameVector.get(i).equalsIgnoreCase("FIMCognCompehensExpressModeTyp"))) {
	    						data = bufDataVector.get(i);
	    						if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
	    								(data.trim().isEmpty())) {
	    						    numberScoresMissing++;
	    						    missingIndices.add(bufIndex.get(i));
	    						    missing_repeatable_group_id.add(bufRepeatable_group_id.get(i));
	    						    missing_group_row_index.add(bufGroup_row_index.get(i));
	    						}
	    						else if (!((bufColumnNameVector.get(i).equalsIgnoreCase("VisitTyp")) ||
	    								   (bufColumnNameVector.get(i).equalsIgnoreCase("FIMCognCompehensExpressModeTyp")))) {
	    							try {
	    						        value = Integer.valueOf(data).intValue();
	    						        calculatedCognFunctSubscore += value;
	    							}
	    							catch (NumberFormatException e) {
	    								numberScoresNotInteger++;
	    							    notIntegerIndices.add(bufIndex.get(i));
	    							    notInteger_repeatable_group_id.add(bufRepeatable_group_id.get(i));
	    							    notInteger_group_row_index.add(bufGroup_row_index.get(i));
	    		                        notIntegerData.add(data);
	    							}
	    						}
	    						else if (bufColumnNameVector.get(i).equalsIgnoreCase("VisitTyp")) {
	    							if (!((data.equalsIgnoreCase("Follow-Up")) ||
	    									(data.equalsIgnoreCase("Inpatient admission"))||
	    									(data.equalsIgnoreCase("Discharge")))) {
	    								numberVisitTypNotCorrect++;
	    							    incorrectVisitIndices.add(bufIndex.get(i));
	    							    incorrectVisit_repeatable_group_id.add(bufRepeatable_group_id.get(i));
	    								incorrectVisit_group_row_index.add(bufGroup_row_index.get(i));
	    		                        incorrectVisitData.add(data);
	    							}	
	    						}
	    					}
	    	} // else if (bufRepeatableGroupName.get(i).equalsIgnoreCase("Expression")) 
	    	else if (bufRepeatableGroupName.get(i).equalsIgnoreCase("Social Cognition")) {
	    		if ((bufColumnNameVector.get(i).equalsIgnoreCase("VisitTyp")) ||
	    				   (bufColumnNameVector.get(i).equalsIgnoreCase("FIMCognSocialInteractScl")) ||
	    				   (bufColumnNameVector.get(i).equalsIgnoreCase("FIMCognProbSolvScl")) ||
	    				   (bufColumnNameVector.get(i).equalsIgnoreCase("FIMCognMemoryScl"))) {
	    						data = bufDataVector.get(i);
	    						if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
	    								(data.trim().isEmpty())) {
	    						    numberScoresMissing++;
	    						    numberCognitionScoresMissing++;
	    						    missingIndices.add(bufIndex.get(i));
	    						    missing_repeatable_group_id.add(bufRepeatable_group_id.get(i));
	    						    missing_group_row_index.add(bufGroup_row_index.get(i));
	    						}
	    						else if (!(bufColumnNameVector.get(i).equalsIgnoreCase("VisitTyp"))) {
	    							try {
	    						        value = Integer.valueOf(data).intValue();
	    						        calculatedCognFunctSubscore += value;
	    							}
	    							catch (NumberFormatException e) {
	    								numberScoresNotInteger++;
	    								numberCognitionScoresNotInteger++;
	    							    notIntegerIndices.add(bufIndex.get(i));
	    							    notInteger_repeatable_group_id.add(bufRepeatable_group_id.get(i));
	    							    notInteger_group_row_index.add(bufGroup_row_index.get(i));
	    		                        notIntegerData.add(data);
	    							}
	    						}
	    						else if (bufColumnNameVector.get(i).equalsIgnoreCase("VisitTyp")) {
	    							if (!((data.equalsIgnoreCase("Follow-Up")) ||
	    									(data.equalsIgnoreCase("Inpatient admission"))||
	    									(data.equalsIgnoreCase("Discharge")))) {
	    								numberVisitTypNotCorrect++;
	    								numberCognitionVisitTypNotCorrect++;
	    							    incorrectVisitIndices.add(bufIndex.get(i));
	    							    incorrectVisit_repeatable_group_id.add(bufRepeatable_group_id.get(i));
	    								incorrectVisit_group_row_index.add(bufGroup_row_index.get(i));
	    		                        incorrectVisitData.add(data);
	    							}	
	    						}
	    					}
	    	} // else if (bufRepeatableGroupName.get(i).equalsIgnoreCase("Social Cognition"))
	    	else if (bufRepeatableGroupName.get(i).equalsIgnoreCase("Scores")) {
	    		if (bufColumnNameVector.get(i).equalsIgnoreCase("VisitTyp")) {
	    			data = bufDataVector.get(i);
					if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
					    numberScoresMissing++;
					    missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id.add(bufRepeatable_group_id.get(i));
					    missing_group_row_index.add(bufGroup_row_index.get(i));
					}
					else if (!((data.equalsIgnoreCase("Follow-Up")) ||
							(data.equalsIgnoreCase("Inpatient admission"))||
							(data.equalsIgnoreCase("Discharge")))) {
						numberVisitTypNotCorrect++;
					    incorrectVisitIndices.add(bufIndex.get(i));
					    incorrectVisit_repeatable_group_id.add(bufRepeatable_group_id.get(i));
						incorrectVisit_group_row_index.add(bufGroup_row_index.get(i));
                        incorrectVisitData.add(data);
					}
	    		} // if (bufColumnNameVector.get(i).equalsIgnoreCase("VisitTyp"))
	    	} // else if (bufRepeatableGroupName.get(i).equalsIgnoreCase("Scores"))
		} // for (i = 0; i < bufColumnNameVector.size(); i++)
	    
	    if (numberScoresMissing >= 1) {
	    	for (i = 0; i < numberScoresMissing; i++) {
	    		rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id.get(i));
				rgEntries = table.getAllReferences(subject_row_id, 
						missing_repeatable_group_id.get(i), null);
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, missing_group_row_index.get(i)),
								rgTable.getDataFilePositionMapping(missingIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(missingIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index.get(i)),
						rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
	    	}	
	    } // if (numberScoresMissing >= 1)
	    
	    if (numberScoresNotInteger >= 1) {
	    	for (i = 0; i < numberScoresNotInteger; i++) {
	    		rgTable = table.getRepeatableGroupTable(notInteger_repeatable_group_id.get(i));
				rgEntries = table.getAllReferences(subject_row_id, 
						notInteger_repeatable_group_id.get(i), null);
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, notInteger_group_row_index.get(i)),
								rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(notIntegerIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
										notIntegerData.get(i));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, notInteger_group_row_index.get(i)),
						rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)), message));
	    	}	
	    } // if (numberScoresNotInteger >= 1)
	    
	    if (numberVisitTypNotCorrect >= 1) {
	    	for (i = 0; i < numberVisitTypNotCorrect; i++) {
	    		rgTable = table.getRepeatableGroupTable(incorrectVisit_repeatable_group_id.get(i));
				rgEntries = table.getAllReferences(subject_row_id, 
						incorrectVisit_repeatable_group_id.get(i), null);
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, incorrectVisit_group_row_index.get(i)),
								rgTable.getDataFilePositionMapping(incorrectVisitIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(incorrectVisitIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_NOT_PERMISSIBLE_VALUE,
										incorrectVisitData.get(i), permissibleValueString);
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, incorrectVisit_group_row_index.get(i)),
						rgTable.getDataFilePositionMapping(incorrectVisitIndices.get(i)), message));
	    	}	
	    } // if (numberVisitTypNotCorrect >= 1)
	    
	    if ((numberScoresMissing >= 1) || (numberScoresNotInteger >= 1) ||
	    		(numberVisitTypNotCorrect >= 1)) {
	    	// keep going
	    }
	    
	    calculatedTotalScore = calculatedMotorSubscore + calculatedCognFunctSubscore;
	    
	    int actualMotorSubscore = -1;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("fiminstrmotorsubscore")) {
				data = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
				if ((numberMotorScoresMissing == 0) && (numberMotorScoresNotInteger == 0) &&
						(numberMotorVisitTypNotCorrect == 0)) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message)); 
				}
				}
				else if ((numberMotorScoresMissing > 0) || (numberMotorScoresNotInteger > 0) ||
						(numberMotorVisitTypNotCorrect > 0)) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,data);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message)); 	
				}
				else {
					try {
				        actualMotorSubscore = Integer.valueOf(data).intValue();	
					}
				    catch (NumberFormatException e) {
				    	message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
												data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
				    }
				}
				break;
			}
	    }
	    
	    if ((actualMotorSubscore > -1) && (numberMotorScoresMissing == 0) &&
	    		(numberMotorScoresNotInteger == 0) && (numberMotorVisitTypNotCorrect == 0)) {
	    	if (actualMotorSubscore != calculatedMotorSubscore) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(index));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
										String.valueOf(actualMotorSubscore), String.valueOf(calculatedMotorSubscore));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(index), message));
	    	}
	    }
	    
	    int actualCognFunctSubscore = -1;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("fiminstrcognfunctsubscore")) {
				data = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					if ((numberCognitionScoresMissing == 0) && (numberCognitionScoresNotInteger == 0) &&
							(numberCognitionVisitTypNotCorrect == 0)) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
					}
				}
				else if ((numberCognitionScoresMissing > 0) || (numberCognitionScoresNotInteger > 0) ||
						(numberCognitionVisitTypNotCorrect > 0)) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED, data);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));	
				}
				else {
					try {
				        actualCognFunctSubscore = Integer.valueOf(data).intValue();	
					}
				    catch (NumberFormatException e) {
				    	message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
												data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
				    }
				}
				break;
			}
	    }
	    
	    if ((actualCognFunctSubscore > -1) && (numberCognitionScoresMissing == 0) &&
	    		(numberCognitionScoresNotInteger == 0) && (numberCognitionVisitTypNotCorrect == 0)) {
	    	if (actualCognFunctSubscore != calculatedCognFunctSubscore) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(index));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
										String.valueOf(actualCognFunctSubscore), String.valueOf(calculatedCognFunctSubscore));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(index), message));
	    	}
	    }
	    
	    int actualTotalScore = -1;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("fiminstrtotalscore")) {
				data = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					if ((numberScoresMissing == 0) && (numberScoresNotInteger == 0) &&
							(numberVisitTypNotCorrect == 0)) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));    	
				}
				}
				else if ((numberScoresMissing > 0) || (numberScoresNotInteger > 0) ||
						(numberVisitTypNotCorrect > 0)) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,data);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));    		
				}
				else {
					try {
				        actualTotalScore = Integer.valueOf(data).intValue();	
					}
				    catch (NumberFormatException e) {
				    	message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
												data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
				    }
				}
				break;
			}
	    }
	    
	    if ((actualTotalScore > -1) && (numberScoresMissing == 0) &&
	    		(numberScoresNotInteger == 0) && (numberVisitTypNotCorrect == 0)) {
	    	if (actualTotalScore != calculatedTotalScore) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(index));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
										String.valueOf(actualTotalScore), String.valueOf(calculatedTotalScore));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(index), message));
	    	}
	    }

	}
	
	private void validateBESS(StructuralFormStructure structure,
			Vector<Vector<Vector<String>>> columnNameVector3, 
			Vector<Vector<Vector<String>>> dataVector3, int subject_row_id, 
			DataStructureTable table) throws Exception {
		//table.getColumnCount = 3
		//repeatable_group_id = 0
		//rgTable.getSize() = 8
		//rgEntries.size() = 1
		//group_row_index = 0
		//columnNameVector.size() = 8
		//group_column_index = 0 group_column_name = guid data = TBICD238VCK
		//group_column_index = 1 group_column_name = subjectidnum data = 1198
		//group_column_index = 2 group_column_name = ageyrs data = 22
		//group_column_index = 3 group_column_name = visitdate data = 2014-11-25T00:00:00Z
		//group_column_index = 4 group_column_name = sitename data = null
		//group_column_index = 5 group_column_name = dayssincebaseline data = null
		//group_column_index = 6 group_column_name = casecontrlind data = null
		//group_column_index = 7 group_column_name = generalnotestxt data = null
		//repeatable_group_id = 1
		//rgTable.getSize() = 4
		//rgEntries.size() = 1
		//group_row_index = 0
		//columnNameVector.size() = 4
		//group_column_index = 0 group_column_name = contexttype data = Other, specify
		//group_column_index = 1 group_column_name = contexttypeoth data = Baseline
		//group_column_index = 2 group_column_name = datasource data = null
		//group_column_index = 3 group_column_name = datasourceoth data = null
		//repeatable_group_id = 2
		//rgTable.getSize() = 10
		//rgEntries.size() = 1
		//group_row_index = 0
		//columnNameVector.size() = 10
		//group_column_index = 0 group_column_name = bessdbllegfirmerrorct data = 0
		//group_column_index = 1 group_column_name = besssgllegfirmerrorct data = 0
		//group_column_index = 2 group_column_name = besstandemfirmerrorct data = 1
		//group_column_index = 3 group_column_name = besstotalfirmerrorct data = 1
		//group_column_index = 4 group_column_name = bessdbllegfoamerrorct data = 0
		//group_column_index = 5 group_column_name = besssgllegfoamerrorct data = 8
		//group_column_index = 6 group_column_name = besstandemstncfoamsrfcerrorct data = 0
		//group_column_index = 7 group_column_name = besstotalfoamerrorct data = 8
		//group_column_index = 8 group_column_name = besstotalerrorct data = 9
		//group_column_index = 9 group_column_name = lattyp data = Left
		Vector<Vector<String>> columnNameVector2;
        Vector<Vector<String>> dataVector2;
        Vector<String> columnNameVector;
        Vector<String> dataVector;
        int i;
        String message;
        String data;
        Vector<String> bufColumnNameVector = new Vector<String>();
        Vector<String> bufDataVector = new Vector<String>();
        Vector<Integer> bufRepeatable_group_id = new Vector<Integer>();
        Vector<Integer> bufGroup_row_index = new Vector<Integer>();
        Vector<Integer> bufIndex = new Vector<Integer>();
        String guid = null;
        int group_row_index = -1;
        int repeatable_group_id;
        RepeatableGroupTable rgTable = null;
        ArrayList<Integer> rgEntries = null;
        int index = -1;
        
		 //System.out.println("table.getColumnCount = " + table.getColumnCount());
        	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
        		 
        		//System.out.println("repeatable_group_id = " + repeatable_group_id);
        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
        		dataVector2 = dataVector3.get(repeatable_group_id);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				//System.out.println("rgTable.getSize() = " + rgTable.getSize());
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				//System.out.println("rgEntries.size() = " + rgEntries.size());
				for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
        	 //System.out.println("group_row_index = " + group_row_index);
			columnNameVector = columnNameVector2.get(group_row_index);
			dataVector = dataVector2.get(group_row_index);
			 //System.out.println("columnNameVector.size() = " + columnNameVector.size());
			 //for (i = 0; i < columnNameVector.size(); i++) {
			    //System.out.println("group_column_index = " + i +
			    	//" group_column_name = " + columnNameVector.get(i) +
			    	//" data = " + dataVector.get(i)) ;	
			 //}
			for (i = 0; i < columnNameVector.size(); i++) {
				bufColumnNameVector.add(columnNameVector.get(i));
				bufDataVector.add(dataVector.get(i));
				bufRepeatable_group_id.add(repeatable_group_id);
				bufGroup_row_index.add(group_row_index);
				bufIndex.add(i);
			}
			} // for (int group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++)
            } // for (int repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++)
        for (i = 0; i < bufColumnNameVector.size(); i++) {
        	if (bufColumnNameVector.get(i).equalsIgnoreCase("guid")) {
				guid = bufDataVector.get(i);
				break;
			}	
        } // for (i = 0; i < bufColumnNameVector.size(); i++)
        
	    
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("ageyrs")) {
				String ageData = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((ageData == null) || (ageData.isEmpty()) || (ageData.trim() == null) ||
						(ageData.trim().isEmpty())) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
				}	
				else {
					double age = Double.valueOf(ageData).doubleValue();
					if (age < 8) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.WARNING_INCORRECT_AGE,
										  ageData, "8 or more years");
						table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
					}
				}
				break;
			  } // if (bufColumnNameVector.get(i).equalsIgnoreCase("ageyrs"))
			} // for (i = 0; i < bufColumnNameVector.size(); i++)
		
		    int numberScoresMissing = 0;
		    Vector<Integer>missingIndices = new Vector<Integer>();
		    int missing_repeatable_group_id = -1;
		    int missing_group_row_index = -1;
		    int rValue = 0;
		    int calculatedFirmSum = 0;
		    boolean goodFirmSum = true;
		    int actualFirmSum = -1;
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
		    	if (bufColumnNameVector.get(i).equalsIgnoreCase("bessdbllegfirmerrorct") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("besssgllegfirmerrorct") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("besstandemfirmerrorct")) {
		    		data = bufDataVector.get(i);
			    	if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
			    		numberScoresMissing++;
			    		missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
			    	}
			    	else {
				    	try {
				    		rValue = Integer.valueOf(data);
		            	    calculatedFirmSum += rValue;
		            	}
		            	catch (Exception e) {
		            		goodFirmSum = false;
		            	}
			    	}
		        }
		    }
		    
		    int numberFirmScoresMissing = numberScoresMissing;
		    
			    for (i = 0; i < bufColumnNameVector.size(); i++) {
					if (bufColumnNameVector.get(i).equalsIgnoreCase("besstotalfirmerrorct")) {
						data = bufDataVector.get(i);
						repeatable_group_id = bufRepeatable_group_id.get(i);
						group_row_index = bufGroup_row_index.get(i);
						index = bufIndex.get(i);
						rgTable = table.getRepeatableGroupTable(repeatable_group_id);
						rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
						if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
								(data.trim().isEmpty())) {
							if (goodFirmSum & (numberFirmScoresMissing == 0)) {
							message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(index),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(index));

							message =
									message
											+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
							table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index), message));    	
						}
						}
						else if ((!goodFirmSum) || (numberFirmScoresMissing > 0)) {
							message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(index),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(index));

							message =
									message
											+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,data);
							table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index), message));    	
						}	
						else {
						    actualFirmSum = Integer.valueOf(data).intValue();	
						}
						break;
					}
			    }
			    
			    if ((actualFirmSum > -1) &&  goodFirmSum & (numberFirmScoresMissing == 0)){
			    	if (actualFirmSum != calculatedFirmSum) {
			    		message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
												String.valueOf(actualFirmSum), String.valueOf(calculatedFirmSum));
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
			    	}
			    }
		    
		    int calculatedFoamSum = 0;
		    boolean goodFoamSum = true;
		    int actualFoamSum = -1;
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
		    	if ((bufColumnNameVector.get(i).equalsIgnoreCase("bessdbllegfoamerrorct")) ||
				    (bufColumnNameVector.get(i).equalsIgnoreCase("besssgllegfoamerrorct")) ||
				    (bufColumnNameVector.get(i).equalsIgnoreCase("besstandemstncfoamsrfcerrorct"))) {
		    		data = bufDataVector.get(i);
			    	if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
			    		numberScoresMissing++;
			    		missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
			    	}
			    	else {
				    	try {
				    		rValue = Integer.valueOf(data);
		            	    calculatedFoamSum += rValue;
		            	}
		            	catch (Exception e) {
		            		goodFoamSum = false;
		            	}
			    	}
		        }
		    }
		    
		    int numberFoamScoresMissing = numberScoresMissing - numberFirmScoresMissing;
			    for (i = 0; i < bufColumnNameVector.size(); i++) {
					if (bufColumnNameVector.get(i).equalsIgnoreCase("besstotalfoamerrorct")) {
						data = bufDataVector.get(i);
						repeatable_group_id = bufRepeatable_group_id.get(i);
						group_row_index = bufGroup_row_index.get(i);
						index = bufIndex.get(i);
						rgTable = table.getRepeatableGroupTable(repeatable_group_id);
						rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
						if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
								(data.trim().isEmpty())) {
							if (goodFoamSum & (numberFoamScoresMissing == 0)) {
							message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(index),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(index));

							message =
									message
											+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
							table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index), message));    	
						}
						}
						else if ((!goodFoamSum) || (numberFoamScoresMissing > 0)) {
							message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(index),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(index));

							message =
									message
											+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED, data);
							table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index), message));    	
						}	
						else {
						    actualFoamSum = Integer.valueOf(data).intValue();	
						}
						break;
					}
			    }
			    
			    if ((actualFoamSum > -1) && goodFoamSum && (numberFoamScoresMissing == 0)) {
			    	if (actualFoamSum != calculatedFoamSum) {
			    		message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
												String.valueOf(actualFoamSum), String.valueOf(calculatedFoamSum));
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
			    	}
			    }
		    
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
		    	if (bufColumnNameVector.get(i).equalsIgnoreCase("lattyp")) {
		    		data = bufDataVector.get(i);
			    	if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
			    		numberScoresMissing++;
			    		missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
			    	}
			    	break;
		        }
		    }
		    
		    if (numberScoresMissing >= 1) {
		    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						missing_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresMissing; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, missing_group_row_index),
									rgTable.getDataFilePositionMapping(missingIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(missingIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index),
							rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
		    	}
		    	return;	
		    } // if (numberScoresMissing >= 1)
		    
		    int calculatedSum = calculatedFirmSum + calculatedFoamSum;
		    boolean goodSum = goodFirmSum && goodFoamSum;
		    int actualSum = -1;
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
				if (bufColumnNameVector.get(i).equalsIgnoreCase("besstotalerrorct")) {
					data = bufDataVector.get(i);
					repeatable_group_id = bufRepeatable_group_id.get(i);
					group_row_index = bufGroup_row_index.get(i);
					index = bufIndex.get(i);
					rgTable = table.getRepeatableGroupTable(repeatable_group_id);
					rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
					if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
					if (goodSum && (numberScoresMissing == 0)) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));    	
					}
					}
					else if ((!goodSum) || (numberScoresMissing > 0)) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));    		
					}
					else {
					    actualSum = Integer.valueOf(data).intValue();	
					}
					break;
				}
		    }
		    
		    if ((actualSum > -1) && goodSum && (numberScoresMissing == 0)) {
		    	if (actualSum != calculatedSum) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
											String.valueOf(actualSum), String.valueOf(calculatedSum));
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
		    	}
		    }
	}
	
	private void validateMDS_UPDRS(StructuralFormStructure structure,
			Vector<Vector<Vector<String>>> columnNameVector3, 
			Vector<Vector<Vector<String>>> dataVector3, int subject_row_id, 
			DataStructureTable table) throws Exception {
		//Structure Name:	MDS_UPDRS
		//table.getColumnCount = 7
		//repeatable_group_id = 0
		//rgTable.getSize() = 0
		//rgEntries.size() = 0
		//repeatable_group_id = 1
		//rgTable.getSize() = 16
		//rgEntries.size() = 1
		//group_row_index = 0
		//columnNameVector.size() = 16
		//group_column_index = 0 group_column_name = mdsupdrsprimrysrcinfotyp data = Patient
		//group_column_index = 1 group_column_name = mdsupdrsrcntcogimprmntscore data = 0
		//group_column_index = 2 group_column_name = mdsupdrshallucpsychosscore data = 0
		//group_column_index = 3 group_column_name = mdsupdrsdrpssmoodscore data = 0
		//group_column_index = 4 group_column_name = mdsupdrsanxsmoodscore data = 0
		//group_column_index = 5 group_column_name = mdsupdrsapathyscore data = 0
		//group_column_index = 6 group_column_name = mdsupdrsdopmndysregsyndscore data = 0
		//group_column_index = 7 group_column_name = mdsupdrsqstnnreinfoprovdrtyp data = Patient
		//group_column_index = 8 group_column_name = mdsupdrssleepprobscore data = 0
		//group_column_index = 9 group_column_name = mdsupdrsdaytmsleepscore data = 0
		//group_column_index = 10 group_column_name = mdsupdrspainothrsensscore data = 0
		//group_column_index = 11 group_column_name = mdsupdrsurnryprobscore data = 0
		//group_column_index = 12 group_column_name = mdsupdrsconstipprobscore data = 0
		//group_column_index = 13 group_column_name = mdsupdrsliteheadstndngscore data = 0
		//group_column_index = 14 group_column_name = mdsupdrsfatiguescore data = 0
		//group_column_index = 15 group_column_name = mdsupdrs_partiscore data = 0
		//repeatable_group_id = 2
		//rgTable.getSize() = 1
		//rgEntries.size() = 1
		//group_row_index = 0
		//columnNameVector.size() = 1
		//group_column_index = 0 group_column_name = mdsupdrs_totalscore data = 0
		//repeatable_group_id = 3
		//rgTable.getSize() = 41
		//rgEntries.size() = 1
		//group_row_index = 0
		//columnNameVector.size() = 41
		//group_column_index = 0 group_column_name = mdsupdrsptntprknsnmedind data = No
		//group_column_index = 1 group_column_name = mdsupdrsptclinstateprknsnmdind data = null
		//group_column_index = 2 group_column_name = mdsupdrsptntuseldopaind data = No
		//group_column_index = 3 group_column_name = mdsupdrslstldopadosetm data = null
		//group_column_index = 4 group_column_name = mdsupdrsfreeflowspeechscore data = 0
		//group_column_index = 5 group_column_name = mdsupdrsfacialexprscore data = 0
		//group_column_index = 6 group_column_name = mdsupdrsneckrigidscore data = 0
		//group_column_index = 7 group_column_name = mdsupdrsruerigidscore data = 0
		//group_column_index = 8 group_column_name = mdsupdrsluerigidscore data = 0
		//group_column_index = 9 group_column_name = mdsupdrsrlerigidscore data = 0
		//group_column_index = 10 group_column_name = mdsupdrsllerigidscore data = 0
		//group_column_index = 11 group_column_name = mdsupdrsfingertppngrtehndscore data = 0
		//group_column_index = 12 group_column_name = mdsupdrsfingertppnglfthndscore data = 0
		//group_column_index = 13 group_column_name = mdsupdrsrtehndscore data = 0
		//group_column_index = 14 group_column_name = mdsupdrslfthndscore data = 0
		//group_column_index = 15 group_column_name = mdsupdrsprontsupnrthndmvmtscr data = 0
		//group_column_index = 16 group_column_name = pronatsupinlfthndmvmntscore data = 0
		//group_column_index = 17 group_column_name = rtefttoetppngscore data = 0
		//group_column_index = 18 group_column_name = mdsupdrslftfttoetppngscore data = 0
		//group_column_index = 19 group_column_name = mdsupdrslegagiltyrtelegscore data = 0
		//group_column_index = 20 group_column_name = mdsupdrslegagiltylftlegscore data = 0
		//group_column_index = 21 group_column_name = mdsupdrsarisingfrmchrscore data = 0
		//group_column_index = 22 group_column_name = mdsupdrsgaitscore data = 0
		//group_column_index = 23 group_column_name = mdsupdrsfreezinggaitscore data = 0
		//group_column_index = 24 group_column_name = mdsupdrspostrlstabltyscore data = 0
		//group_column_index = 25 group_column_name = mdsupdrsposturescore data = 0
		//group_column_index = 26 group_column_name = mdsupdrsglblspontntymvmntscore data = 0
		//group_column_index = 27 group_column_name = mdsupdrspostrltremorrthndscore data = 0
		//group_column_index = 28 group_column_name = mdsupdrspostrltremrlfthndscore data = 0
		//group_column_index = 29 group_column_name = mdsupdrskinetictremrrthndscore data = 0
		//group_column_index = 30 group_column_name = mdsupdrskinetictremrlfthndscr data = 0
		//group_column_index = 31 group_column_name = mdsupdrsresttremorampruescore data = 0
		//group_column_index = 32 group_column_name = mdsupdrsresttremorampluescore data = 0
		//group_column_index = 33 group_column_name = mdsupdrsresttremoramprlescore data = 0
		//group_column_index = 34 group_column_name = mdsupdrsresttremorampllescore data = 0
		//group_column_index = 35 group_column_name = mdsupdrsresttremramplipjawscr data = 0
		//group_column_index = 36 group_column_name = mdsupdrsconstncyresttremrscore data = 0
		//group_column_index = 37 group_column_name = mdsupdrsdyskchreadystnaprsscr data = No
		//group_column_index = 38 group_column_name = mdsupdrsmvmntintrfrncescore data = null
		//group_column_index = 39 group_column_name = mdsupdrshoehnyahrstagescore data = 0
		//group_column_index = 40 group_column_name = mdsupdrs_partiiiscore data = 0
		//repeatable_group_id = 4
		//rgTable.getSize() = 16
		//rgEntries.size() = 1
		//group_row_index = 0
		//columnNameVector.size() = 16
		//group_column_index = 0 group_column_name = mdsupdrstmspntdyskscore data = 0
		//group_column_index = 1 group_column_name = mdsupdrsttlhrawkdysknum data = null
		//group_column_index = 2 group_column_name = mdsupdrsttlhrdysknum data = null
		//group_column_index = 3 group_column_name = mdsupdrsprcntdyskval data = null
		//group_column_index = 4 group_column_name = mdsupdrsfuncimpactdyskscore data = 0
		//group_column_index = 5 group_column_name = mdsupdrsttlhrawkoffstatenum data = null
		//group_column_index = 6 group_column_name = mdsupdrsttlhroffnum data = null
		//group_column_index = 7 group_column_name = mdsupdrsprcntoffval data = null
		//group_column_index = 8 group_column_name = mdsupdrstmspntoffstatescore data = 0
		//group_column_index = 9 group_column_name = mdsupdrsfuncimpactfluctscore data = 0
		//group_column_index = 10 group_column_name = mdsupdrscomplxtymtrfluctscore data = 0
		//group_column_index = 11 group_column_name = mdsupdrspainfloffstatdystnascr data = 0
		//group_column_index = 12 group_column_name = mdsupdrsttlhroffdemndystnianum data = null
		//group_column_index = 13 group_column_name = mdsupdrsttlhroffwdystnianum data = null
		// = 14 group_column_name = mdsupdrsprcntoffdystniaval data = null
		//group_column_index = 15 group_column_name = mdsupdrs_partivscore data = 0
		//repeatable_group_id = 5
		////rgTable.getSize() = 7
		//rgEntries.size() = 1
		//group_row_index = 0
		//columnNameVector.size() = 7
		//group_column_index = 0 group_column_name = sitename data = Brigham and Women's
		//group_column_index = 1 group_column_name = visittyppdbp data = 6 months
		//group_column_index = 2 group_column_name = visitdate data = 2015-11-19T00:00:00Z
		//group_column_index = 3 group_column_name = guid data = PDTT264VT6
		//group_column_index = 4 group_column_name = ageyrs data = 12
		//group_column_index = 5 group_column_name = ageremaindrmonths data = 2
		//group_column_index = 6 group_column_name = ageval data = 770
		//repeatable_group_id = 6
		//rgTable.getSize() = 14
		//rgEntries.size() = 1
		//group_row_index = 0
		//columnNameVector.size() = 14
		//group_column_index = 0 group_column_name = mdsupdrsspeechscore data = 0
		//group_column_index = 1 group_column_name = mdsupdrsslivadroolscore data = 0
		//group_column_index = 2 group_column_name = mdsupdrschwngswllwngscore data = 0
		//group_column_index = 3 group_column_name = mdsupdrseatingtskscore data = 0
		//group_column_index = 4 group_column_name = mdsupdrsdressingscore data = 0
		//group_column_index = 5 group_column_name = mdsupdrshygienescore data = 0
		//group_column_index = 6 group_column_name = mdsupdrshandwritingscore data = 0
		//group_column_index = 7 group_column_name = mdsupdrshobbieothractscore data = 0
		//group_column_index = 8 group_column_name = mdsupdrsturngbedscore data = 0
		//group_column_index = 9 group_column_name = mdsupdrstremorscore data = 0
		//group_column_index = 10 group_column_name = mdsupdrsgttngoutbedscore data = 0
		//group_column_index = 11 group_column_name = mdsupdrswlkngbalancescore data = 0
		//group_column_index = 12 group_column_name = mdsupdrsfreezingscore data = 0
		//group_column_index = 13 group_column_name = mdsupdrs_partiiscore data = 0
		Vector<Vector<String>> columnNameVector2;
        Vector<Vector<String>> dataVector2;
        Vector<String> columnNameVector;
        Vector<String> dataVector;
        int i;
        String message;
        String data;
        Vector<String> bufColumnNameVector = new Vector<String>();
        Vector<String> bufDataVector = new Vector<String>();
        Vector<Integer> bufRepeatable_group_id = new Vector<Integer>();
        Vector<Integer> bufGroup_row_index = new Vector<Integer>();
        Vector<Integer> bufIndex = new Vector<Integer>();
        String guid = null;
        int group_row_index = -1;
        int repeatable_group_id;
        RepeatableGroupTable rgTable = null;
        ArrayList<Integer> rgEntries = null;
        int index = -1;
        
		//System.out.println("table.getColumnCount = " + table.getColumnCount());
        	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
        		 
        		//System.out.println("repeatable_group_id = " + repeatable_group_id);
        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
        		dataVector2 = dataVector3.get(repeatable_group_id);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				//System.out.println("rgTable.getSize() = " + rgTable.getSize());
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				//System.out.println("rgEntries.size() = " + rgEntries.size());
				for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
        	 //System.out.println("group_row_index = " + group_row_index);
			columnNameVector = columnNameVector2.get(group_row_index);
			dataVector = dataVector2.get(group_row_index);
			 //System.out.println("columnNameVector.size() = " + columnNameVector.size());
			 //for (i = 0; i < columnNameVector.size(); i++) {
			    //System.out.println("group_column_index = " + i +
			    	//" group_column_name = " + columnNameVector.get(i) +
			    	//" data = " + dataVector.get(i)) ;	
			 //}
			for (i = 0; i < columnNameVector.size(); i++) {
				bufColumnNameVector.add(columnNameVector.get(i));
				bufDataVector.add(dataVector.get(i));
				bufRepeatable_group_id.add(repeatable_group_id);
				bufGroup_row_index.add(group_row_index);
				bufIndex.add(i);
			}
			} // for (int group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++)
            } // for (int repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++)
        for (i = 0; i < bufColumnNameVector.size(); i++) {
        	if (bufColumnNameVector.get(i).equalsIgnoreCase("guid")) {
				guid = bufDataVector.get(i);
				break;
			}	
        } // for (i = 0; i < bufColumnNameVector.size(); i++)
        
	    
	    int ageYears = -1;
        for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("ageyrs")) {
				String ageData = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((ageData == null) || (ageData.isEmpty()) || (ageData.trim() == null) ||
						(ageData.trim().isEmpty())) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
				}	
				else {
					ageYears = Integer.valueOf(ageData).intValue();
					if (ageYears < 18) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.WARNING_INCORRECT_AGE,
										  ageData, "18 or more years");
						table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
					}
				}
				break;
			  } // if (bufColumnNameVector.get(i).equalsIgnoreCase("ageyrs"))
			} // for (i = 0; i < bufColumnNameVector.size(); i++)
        
        int ageRemainderMonths = -1;
        for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("ageremaindrmonths")) {
				String ageData = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((ageData == null) || (ageData.isEmpty()) || (ageData.trim() == null) ||
						(ageData.trim().isEmpty())) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
				}	
				else {
					ageRemainderMonths = Integer.valueOf(ageData).intValue();
					if ((ageRemainderMonths < 0) || (ageRemainderMonths > 11)) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.WARNING_INCORRECT_AGE,
										  ageData, "0 to 11 remainder months");
						table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
					}
				}
				break;
			  } // if (bufColumnNameVector.get(i).equalsIgnoreCase("ageremaindrmonths"))
			} // for (i = 0; i < bufColumnNameVector.size(); i++)
		
		    int calculatedMonths = -1;
		    if ((ageYears >= 0) && (ageRemainderMonths >= 0) && (ageRemainderMonths <= 11)) {
		    	calculatedMonths = ageYears * 12 + ageRemainderMonths;
		    }
		    
		    int ageMonths = -1;
	        for (i = 0; i < bufColumnNameVector.size(); i++) {
				if (bufColumnNameVector.get(i).equalsIgnoreCase("ageval")) {
					String ageData = bufDataVector.get(i);
					repeatable_group_id = bufRepeatable_group_id.get(i);
					group_row_index = bufGroup_row_index.get(i);
					index = bufIndex.get(i);
					rgTable = table.getRepeatableGroupTable(repeatable_group_id);
					rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
					if ((ageData == null) || (ageData.isEmpty()) || (ageData.trim() == null) ||
							(ageData.trim().isEmpty())) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
					}	
					else {
						ageMonths = Integer.valueOf(ageData).intValue();
						if ((calculatedMonths >= 0) && (ageMonths != calculatedMonths)) {
							message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(index),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(index));

							message =
									message
											+ String.format(ApplicationsConstants.WARNING_INCORRECT_CALCULATION,
													String.valueOf(ageMonths), 
													String.valueOf(calculatedMonths));
							
							table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index), message));
						}
					}
					break;
				  } // if (bufColumnNameVector.get(i).equalsIgnoreCase("ageval"))
				} // for (i = 0; i < bufColumnNameVector.size(); i++)
		    int numberScoresMissing = 0;
		    Vector<Integer>missingIndices = new Vector<Integer>();
		    Vector<String>variables555 = new Vector<String>();
		    int missing_repeatable_group_id = -1;
		    int missing_group_row_index = -1;
		    int rValue = 0;
		    int calculatedPart1Sum = 0;
		    int values555 = 0;
		    int requiredValuesMissing = 0;
		    boolean goodPart1Sum = true;
		    int actualPart1Sum = -1;
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
		    	if (bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsrcntcogimprmntscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrshallucpsychosscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsdrpssmoodscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsanxsmoodscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsapathyscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsdopmndysregsyndscore")) {
		    		data = bufDataVector.get(i);
			    	if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
			    		numberScoresMissing++;
			    		missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
						goodPart1Sum = false;
						requiredValuesMissing++;
			    	}
			    	else {
				    	try {
				    		rValue = Integer.valueOf(data);
				    		if (rValue != 555) {
		            	        calculatedPart1Sum += rValue;
		 
				    		}
				    		else {
				    			goodPart1Sum = false;
				    			values555++;
				    			variables555.add(bufColumnNameVector.get(i));
				    		}
		            	}
		            	catch (Exception e) {
		            		goodPart1Sum = false;
		            	}
			    	}
		        }
		    }
		    
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
		    	if (bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrssleepprobscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsdaytmsleepscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrspainothrsensscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsurnryprobscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsconstipprobscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsliteheadstndngscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsfatiguescore")) {
		    		data = bufDataVector.get(i);
			    	if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
			    		numberScoresMissing++;
			    		missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
						goodPart1Sum = false;
						requiredValuesMissing++;
			    	}
			    	else {
				    	try {
				    		rValue = Integer.valueOf(data);
		            	    calculatedPart1Sum += rValue;
		            	}
		            	catch (Exception e) {
		            		goodPart1Sum = false;
		            	}
			    	}
		        }
		    }
		    
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
		    	if (bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsprimrysrcinfotyp") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsqstnnreinfoprovdrtyp")) {
		    		data = bufDataVector.get(i);
			    	if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
			    		numberScoresMissing++;
			    		missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
			    	}
		        }
		    }
		    
		    if (goodPart1Sum) {
			    for (i = 0; i < bufColumnNameVector.size(); i++) {
					if (bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrs_partiscore")) {
						data = bufDataVector.get(i);
						repeatable_group_id = bufRepeatable_group_id.get(i);
						group_row_index = bufGroup_row_index.get(i);
						index = bufIndex.get(i);
						rgTable = table.getRepeatableGroupTable(repeatable_group_id);
						rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
						if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
								(data.trim().isEmpty())) {
							message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(index),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(index));

							message =
									message
											+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
							table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index), message));    	
						}
						else {
						    actualPart1Sum = Integer.valueOf(data).intValue();	
						}
						break;
					}
			    }
			    
			    if (actualPart1Sum > -1) {
			    	if (actualPart1Sum != calculatedPart1Sum) {
			    		message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
												String.valueOf(actualPart1Sum), String.valueOf(calculatedPart1Sum));
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
			    	}
			    }
		    } // if (goodPart1Sum)
		    else {
		    	for (i = 0; i < bufColumnNameVector.size(); i++) {
					if (bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrs_partiscore")) {
						data = bufDataVector.get(i);
						repeatable_group_id = bufRepeatable_group_id.get(i);
						group_row_index = bufGroup_row_index.get(i);
						index = bufIndex.get(i);
						rgTable = table.getRepeatableGroupTable(repeatable_group_id);
						rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
						if (!((data == null) || (data.isEmpty()) || (data.trim() == null) ||
								(data.trim().isEmpty()))) {
							message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(index),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(index));

							message =
									message
											+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,data);
							if ((values555 > 0) && (requiredValuesMissing > 0)) {
								message = message + " Blank required by 555 in:";
								for (i = 0; i < values555; i++) {
									message = message + " " + variables555.get(i);
									if (i < values555-1) {
										message = message + ",";
									}
								}
								message = message + " and " + String.valueOf(requiredValuesMissing);
							    message = message + " missing required value";
								if (requiredValuesMissing >= 2) {
									message = message + "s";
								}
							}
							else if (values555 > 0) {
								message = message + " Blank required by 555 in:";
								for (i = 0; i < values555; i++) {
									message = message + " " + variables555.get(i);
									if (i < values555-1) {
										message = message + ",";
									}
								}	
							}
							else if (requiredValuesMissing > 0) {
								message = message + String.format(" Blank required by " + requiredValuesMissing +
										" missing required value");
								if (requiredValuesMissing >= 2) {
									message = message + "s";
								}
							}
							table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index), message));    	
						}
						
						break;
					}
			    }
			    
			    
		    }

		    
		    numberScoresMissing = 0;
		    missingIndices.clear();
		    missing_repeatable_group_id = -1;
		    missing_group_row_index = -1;
		    rValue = 0;
		    int calculatedPart2Sum = 0;
		    boolean goodPart2Sum = true;
		    int actualPart2Sum = -1;
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
		    	if (bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsspeechscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsslivadroolscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrschwngswllwngscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrseatingtskscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsdressingscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrshygienescore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrshandwritingscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrshobbieothractscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsturngbedscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrstremorscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsgttngoutbedscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrswlkngbalancescore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsfreezingscore")) {
		    		data = bufDataVector.get(i);
			    	if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
			    		numberScoresMissing++;
			    		missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
						goodPart2Sum = false;
			    	}
			    	else {
				    	try {
				    		rValue = Integer.valueOf(data);
		            	    calculatedPart2Sum += rValue;
		            	}
		            	catch (Exception e) {
		            		goodPart2Sum = false;
		            	}
			    	}
		        }
		    }
		    
		    if (numberScoresMissing >= 1) {
		    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						missing_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresMissing; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, missing_group_row_index),
									rgTable.getDataFilePositionMapping(missingIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(missingIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index),
							rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
		    	}	
		    } // if (numberScoresMissing >= 1)
		    
		    if (goodPart2Sum) {
			    for (i = 0; i < bufColumnNameVector.size(); i++) {
					if (bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrs_partiiscore")) {
						data = bufDataVector.get(i);
						repeatable_group_id = bufRepeatable_group_id.get(i);
						group_row_index = bufGroup_row_index.get(i);
						index = bufIndex.get(i);
						rgTable = table.getRepeatableGroupTable(repeatable_group_id);
						rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
						if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
								(data.trim().isEmpty())) {
							message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(index),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(index));

							message =
									message
											+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
							table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index), message));    	
						}
						else {
						    actualPart2Sum = Integer.valueOf(data).intValue();	
						}
						break;
					}
			    }
			    
			    if (actualPart2Sum > -1) {
			    	if (actualPart2Sum != calculatedPart2Sum) {
			    		message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
												String.valueOf(actualPart2Sum), String.valueOf(calculatedPart2Sum));
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
			    	}
			    }
		    } // if (goodPart2Sum)
		    
		    numberScoresMissing = 0;
		    missingIndices.clear();
		    variables555.clear();
		    missing_repeatable_group_id = -1;
		    missing_group_row_index = -1;
		    rValue = 0;
		    int calculatedPart3Sum = 0;
		    values555 = 0;
		    boolean goodPart3Sum = true;
		    int actualPart3Sum = -1;
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
		    	if (bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsfreeflowspeechscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsfacialexprscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsneckrigidscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsruerigidscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsluerigidscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsrlerigidscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsllerigidscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsfingertppngrtehndscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsfingertppnglfthndscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsrtehndscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrslfthndscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsprontsupnrthndmvmtscr") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("pronatsupinlfthndmvmntscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("rtefttoetppngscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrslftfttoetppngscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrslegagiltyrtelegscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrslegagiltylftlegscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsarisingfrmchrscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsgaitscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsfreezinggaitscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrspostrlstabltyscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsposturescore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsglblspontntymvmntscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrspostrltremorrthndscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrspostrltremrlfthndscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrskinetictremrrthndscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrskinetictremrlfthndscr") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsresttremorampruescore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsresttremorampluescore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsresttremoramprlescore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsresttremorampllescore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsresttremramplipjawscr") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsconstncyresttremrscore")) {
		    		data = bufDataVector.get(i);
			    	if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
			    		numberScoresMissing++;
			    		missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
						goodPart3Sum = false;
			    	}
			    	else {
				    	try {
				    		rValue = Integer.valueOf(data);
				    		if (rValue != 555) {
		            	        calculatedPart3Sum += rValue;
				    		}
				    		else {
				    			goodPart3Sum = false;
				    			values555++;
				    			variables555.add(bufColumnNameVector.get(i));
				    		}
		            	}
		            	catch (Exception e) {
		            		goodPart3Sum = false;
		            	}
			    	}
		        }
		    }
		    
		    if (numberScoresMissing >= 1) {
		    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						missing_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresMissing; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, missing_group_row_index),
									rgTable.getDataFilePositionMapping(missingIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(missingIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index),
							rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
		    	}	
		    } // if (numberScoresMissing >= 1)
		    
		    if (goodPart3Sum) {
			    for (i = 0; i < bufColumnNameVector.size(); i++) {
					if (bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrs_partiiiscore")) {
						data = bufDataVector.get(i);
						repeatable_group_id = bufRepeatable_group_id.get(i);
						group_row_index = bufGroup_row_index.get(i);
						index = bufIndex.get(i);
						rgTable = table.getRepeatableGroupTable(repeatable_group_id);
						rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
						if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
								(data.trim().isEmpty())) {
							message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(index),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(index));

							message =
									message
											+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
							table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index), message));    	
						}
						else {
						    actualPart3Sum = Integer.valueOf(data).intValue();	
						}
						break;
					}
			    }
			    
			    if (actualPart3Sum > -1) {
			    	if (actualPart3Sum != calculatedPart3Sum) {
			    		message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
												String.valueOf(actualPart3Sum), String.valueOf(calculatedPart3Sum));
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
			    	}
			    }
		    } // if (goodPart3Sum)
		    else {
		    	for (i = 0; i < bufColumnNameVector.size(); i++) {
					if (bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrs_partiiiscore")) {
						data = bufDataVector.get(i);
						repeatable_group_id = bufRepeatable_group_id.get(i);
						group_row_index = bufGroup_row_index.get(i);
						index = bufIndex.get(i);
						rgTable = table.getRepeatableGroupTable(repeatable_group_id);
						rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
						if (!((data == null) || (data.isEmpty()) || (data.trim() == null) ||
								(data.trim().isEmpty()))) {
							message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(index),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(index));

							message =
									message
											+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,data);
							if ((values555 > 0) && (numberScoresMissing > 0)) {
								message = message + " Blank required by 555 in:";
								for (i = 0; i < values555; i++) {
									message = message + " " + variables555.get(i);
									if (i < values555-1) {
										message = message + ",";
									}
								}
								message = message + " and " + String.valueOf(numberScoresMissing);
								message = message + " missing required value";
								if (numberScoresMissing >= 2) {
									message = message + "s";
								}
							}
							else if (values555 > 0) {
								message = message + " Blank required by 555 in:";
								for (i = 0; i < values555; i++) {
									message = message + " " + variables555.get(i);
									if (i < values555-1) {
										message = message + ",";
									}
								}	
							}
							else if (numberScoresMissing > 0) {
								message = message + String.format(" Blank required by " + numberScoresMissing +
										" missing required value");
								if (numberScoresMissing >= 2) {
									message = message + "s";
								}
							}
							
							table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index), message));    	
						}
						
						break;
					}
			    }
			    
			    
		    }
		    
		    numberScoresMissing = 0;
		    missingIndices.clear();
		    variables555.clear();
		    missing_repeatable_group_id = -1;
		    missing_group_row_index = -1;
		    rValue = 0;
		    int calculatedPart4Sum = 0;
		    values555 = 0;
		    boolean goodPart4Sum = true;
		    int actualPart4Sum = -1;
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
		    	if (bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrstmspntdyskscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsfuncimpactdyskscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrstmspntoffstatescore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrsfuncimpactfluctscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrscomplxtymtrfluctscore") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrspainfloffstatdystnascr")) {
		    		data = bufDataVector.get(i);
			    	if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
			    		numberScoresMissing++;
			    		missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
						goodPart4Sum = false;
			    	}
			    	else {
				    	try {
				    		rValue = Integer.valueOf(data);
				    		if (rValue != 555) {
		            	        calculatedPart4Sum += rValue;
				    		}
				    		else {
				    			goodPart4Sum = false;
				    			values555++;
				    			variables555.add(bufColumnNameVector.get(i));
				    		}
		            	}
		            	catch (Exception e) {
		            		goodPart4Sum = false;
		            	}
			    	}
		        }
		    }
		    
		    if (numberScoresMissing >= 1) {
		    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						missing_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresMissing; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, missing_group_row_index),
									rgTable.getDataFilePositionMapping(missingIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(missingIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index),
							rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
		    	}	
		    } // if (numberScoresMissing >= 1)
		    
		    if (goodPart4Sum) {
			    for (i = 0; i < bufColumnNameVector.size(); i++) {
					if (bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrs_partivscore")) {
						data = bufDataVector.get(i);
						repeatable_group_id = bufRepeatable_group_id.get(i);
						group_row_index = bufGroup_row_index.get(i);
						index = bufIndex.get(i);
						rgTable = table.getRepeatableGroupTable(repeatable_group_id);
						rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
						if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
								(data.trim().isEmpty())) {
							message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(index),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(index));

							message =
									message
											+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
							table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index), message));    	
						}
						else {
						    actualPart4Sum = Integer.valueOf(data).intValue();	
						}
						break;
					}
			    }
			    
			    if (actualPart4Sum > -1) {
			    	if (actualPart4Sum != calculatedPart4Sum) {
			    		message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
												String.valueOf(actualPart4Sum), String.valueOf(calculatedPart4Sum));
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
			    	}
			    }
		    } // if (goodPart4Sum)
		    else {
		    	for (i = 0; i < bufColumnNameVector.size(); i++) {
					if (bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrs_partivscore")) {
						data = bufDataVector.get(i);
						repeatable_group_id = bufRepeatable_group_id.get(i);
						group_row_index = bufGroup_row_index.get(i);
						index = bufIndex.get(i);
						rgTable = table.getRepeatableGroupTable(repeatable_group_id);
						rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
						if (!((data == null) || (data.isEmpty()) || (data.trim() == null) ||
								(data.trim().isEmpty()))) {
							message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(index),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(index));

							message =
									message
											+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,data);
							if ((values555 > 0) && (numberScoresMissing > 0)) {
								message = message + " Blank required by 555 in:";
								for (i = 0; i < values555; i++) {
									message = message + " " + variables555.get(i);
									if (i < values555-1) {
										message = message + ",";
									}
								}
								message = message + " and " + String.valueOf(numberScoresMissing);
								message = message + " missing required value";
								if (numberScoresMissing >= 2) {
									message = message + "s";
								}
							}
							else if (values555 > 0) {
								message = message + " Blank required by 555 in:";
								for (i = 0; i < values555; i++) {
									message = message + " " + variables555.get(i);
									if (i < values555-1) {
										message = message + ",";
									}
								}	
							}
							else if (numberScoresMissing > 0) {
								message = message + String.format(" Blank required by " + numberScoresMissing +
										" missing required value");
								if (numberScoresMissing >= 2) {
									message = message + "s";
								}
							}
							
							table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index), message));    	
						}
						
						break;
					}
			    }
			    
			    
		    }
		    
		    int calculatedSum = -1;
		    boolean goodSum = false;
		    
		    if (goodPart1Sum && goodPart2Sum && goodPart3Sum && goodPart4Sum) {
		        calculatedSum = calculatedPart1Sum + calculatedPart2Sum +
		        		calculatedPart3Sum + calculatedPart4Sum;
		        goodSum = true;
		    }
		    int actualSum = -1;
		    if (goodSum) {
			    for (i = 0; i < bufColumnNameVector.size(); i++) {
					if (bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrs_totalscore")) {
						data = bufDataVector.get(i);
						repeatable_group_id = bufRepeatable_group_id.get(i);
						group_row_index = bufGroup_row_index.get(i);
						index = bufIndex.get(i);
						rgTable = table.getRepeatableGroupTable(repeatable_group_id);
						rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
						if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
								(data.trim().isEmpty())) {
							message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(index),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(index));
	
							message =
									message
											+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
							table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index), message));    	
						}
						else {
						    actualSum = Integer.valueOf(data).intValue();	
						}
						break;
					}
			    }
			    
			    if (actualSum > -1) {
			    	if (actualSum != calculatedSum) {
			    		message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));
	
						message =
								message
										+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
												String.valueOf(actualSum), String.valueOf(calculatedSum));
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
			    	}
			    }
		    } // if (goodSum)
		    else {
		    	boolean haveTotalScore = false;
		    	for (i = 0; i < bufColumnNameVector.size(); i++) {
					if (bufColumnNameVector.get(i).equalsIgnoreCase("mdsupdrs_totalscore")) {
						haveTotalScore = true;
						data = bufDataVector.get(i);
						repeatable_group_id = bufRepeatable_group_id.get(i);
						group_row_index = bufGroup_row_index.get(i);
						index = bufIndex.get(i);
						rgTable = table.getRepeatableGroupTable(repeatable_group_id);
						rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
						if (!((data == null) || (data.isEmpty()) || (data.trim() == null) ||
								(data.trim().isEmpty()))) {
							message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(index),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(index));

							message =
									message
											+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,data);
							table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index), message));    	
						}
						
						break;
					}
			    }
		    	
		    	if (!haveTotalScore) {
			    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
			    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
			    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Total Score")) {
			    		    // rgTable.getColumnCount() = 1 for Total Score
			    			message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, 0),
											rgTable.getDataFilePositionMapping(0),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(0));

							message =
									message
											+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
							table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(0), message));
			    			break;
			    		}
			    	}
			    }
		    }

	}

	private void validateRivermead(StructuralFormStructure structure,
			Vector<Vector<Vector<String>>> columnNameVector3, 
			Vector<Vector<Vector<String>>> dataVector3, int subject_row_id, 
			DataStructureTable table) throws Exception {
		// 0 = Not experienced
		// 1 = No more a problem
		// 4 = A severe problem
		// Values of 1 are excluded from sums forming
		// rpq3score, rpq13score, and rpqtotalscore
		
		// 2 possible sets of columnNameVectors and accompanying dataVectors:
		
		// group_column_index = 0 group_columnName = guid
	    // group_column_index = 1 group_columnName = subjectidnum
	    // group_column_index = 2 group_columnName = ageyrs
	    // group_column_index = 3 group_columnName = visitdate
		// group_column_index = 4 group_columnName = sitename
		// group_column_index = 5 group_columnName = dayssincebaseline
		// group_column_index = 6 group_columnName = casecontrlind
		// group_column_index = 7 group_columnName = generalnotestxt
		
		// group_column_index = 0 group_columnName = rpqheadachesscale
		// group_column_index = 1 group_columnName = rpqdizzinessscale
	    // group_column_index = 2 group_columnName = rpqnauseascale
	    // group_column_index = 3 group_columnName = rpqnoisesensscale
		// group_column_index = 4 group_columnName = rpqsleepdistscale
		// group_column_index = 5 group_columnName = rpqfatiguescale
	    // group_column_index = 6 group_columnName = rpqirritablescale
	    // group_column_index = 7 group_columnName = rpqdepressedscale
		// group_column_index = 8 group_columnName = rpqfrustratedscale
	    // group_column_index = 9 group_columnName = rpqforgetfulscale
	    // group_column_index = 10 group_columnName = rpqpoorconcscale
		// group_column_index = 11 group_columnName = rpqlongtothinkscale
		// group_column_index = 12 group_columnName = rpqblurredvisionscale
		// group_column_index = 13 group_columnName = rpqlightsensscale
	    // group_column_index = 14 group_columnName = rpqdblvisionscale
	    // group_column_index = 15 group_columnName = rpqrestlessscale
	    // group_column_index = 16 group_columnName = rpqothr1diffcltyscale
		// group_column_index = 17 group_columnName = rpqothr1diffcltytxt
	    // group_column_index = 18 group_columnName = rpqothr2diffcltyscale
	    // group_column_index = 19 group_columnName = rpqothr2diffcltytxt
	    // group_column_index = 20 group_columnName = rpq3score
	    // group_column_index = 21 group_columnName = rpq13score
	    // group_column_index = 22 group_columnName = rpqtotalscore
		
		Vector<Vector<String>> columnNameVector2;
        Vector<Vector<String>> dataVector2;
        Vector<String> columnNameVector;
        Vector<String> dataVector;
        int i;
        String message;
        String data;
        Vector<String> bufColumnNameVector = new Vector<String>();
        Vector<String> bufDataVector = new Vector<String>();
        Vector<Integer> bufRepeatable_group_id = new Vector<Integer>();
        Vector<Integer> bufGroup_row_index = new Vector<Integer>();
        Vector<Integer> bufIndex = new Vector<Integer>();
        String guid = null;
        int group_row_index = -1;
        int repeatable_group_id;
        RepeatableGroupTable rgTable = null;
        ArrayList<Integer> rgEntries = null;
        int index = -1;
        
		 //System.out.println("table.getColumnCount = " + table.getColumnCount());
        	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
        		 
        		//System.out.println("repeatable_group_id = " + repeatable_group_id);
        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
        		dataVector2 = dataVector3.get(repeatable_group_id);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				//System.out.println("rgTable.getSize() = " + rgTable.getSize());
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				//System.out.println("rgEntries.size() = " + rgEntries.size());
				for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
        	 //System.out.println("group_row_index = " + group_row_index);
			columnNameVector = columnNameVector2.get(group_row_index);
			dataVector = dataVector2.get(group_row_index);
			 //System.out.println("columnNameVector.size() = " + columnNameVector.size());
			 //for (i = 0; i < columnNameVector.size(); i++) {
			    //System.out.println("group_column_index = " + i +
			    	//" group_column_name = " + columnNameVector.get(i) +
			    	//" data = " + dataVector.get(i)) ;	
			 //}
			for (i = 0; i < columnNameVector.size(); i++) {
				bufColumnNameVector.add(columnNameVector.get(i));
				bufDataVector.add(dataVector.get(i));
				bufRepeatable_group_id.add(repeatable_group_id);
				bufGroup_row_index.add(group_row_index);
				bufIndex.add(i);
			}
			} // for (int group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++)
            } // for (int repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++)
        for (i = 0; i < bufColumnNameVector.size(); i++) {
        	if (bufColumnNameVector.get(i).equalsIgnoreCase("guid")) {
				guid = bufDataVector.get(i);
				break;
			}	
        } // for (i = 0; i < bufColumnNameVector.size(); i++)
        
	    
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("ageyrs")) {
				String ageData = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((ageData == null) || (ageData.isEmpty()) || (ageData.trim() == null) ||
						(ageData.trim().isEmpty())) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
				}	
				else {
					double age = Double.valueOf(ageData).doubleValue();
					if (age < 18) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.WARNING_INCORRECT_AGE,
										  ageData, "18 or more years");
						table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
					}
				}
				break;
			  } // if (bufColumnNameVector.get(i).equalsIgnoreCase("ageyrs"))
			} // for (i = 0; i < bufColumnNameVector.size(); i++)
		
		    int numberScoresMissing = 0;
		    Vector<Integer>missingIndices = new Vector<Integer>();
		    int missing_repeatable_group_id = -1;
		    int missing_group_row_index = -1;
		    int numberScoresNotInteger = 0;
		    Vector<Integer>notIntegerIndices = new Vector<Integer>();
		    int notInteger_repeatable_group_id = -1;
		    int notInteger_group_row_index = -1;
		    Vector<String>notIntegerData = new Vector<String>();
		    int rValue = 0;
		    int calculated3Sum = 0;
		    int actual3Sum = -1;
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
		    	if (bufColumnNameVector.get(i).equalsIgnoreCase("rpqheadachesscale") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("rpqdizzinessscale") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("rpqnauseascale")) {
		    		data = bufDataVector.get(i);
			    	if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
			    		numberScoresMissing++;
			    		missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
			    	}
			    	else {
				    	try {
				    		rValue = Integer.valueOf(data);
				    		if (rValue != 1) {
		            	        calculated3Sum += rValue;
				    		}
		            	}
		            	catch (NumberFormatException e) {
		            		numberScoresNotInteger++;
				    		notIntegerIndices.add(bufIndex.get(i));
						    notInteger_repeatable_group_id = bufRepeatable_group_id.get(i);
							notInteger_group_row_index = bufGroup_row_index.get(i);
							notIntegerData.add(data);
		            	}
			    	}
		        }
		    }
		    
		    int number3ScoresMissing = numberScoresMissing;
		    int number3ScoresNotInteger = numberScoresNotInteger;
			    for (i = 0; i < bufColumnNameVector.size(); i++) {
					if (bufColumnNameVector.get(i).equalsIgnoreCase("rpq3score")) {
						data = bufDataVector.get(i);
						repeatable_group_id = bufRepeatable_group_id.get(i);
						group_row_index = bufGroup_row_index.get(i);
						index = bufIndex.get(i);
						rgTable = table.getRepeatableGroupTable(repeatable_group_id);
						rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
						if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
								(data.trim().isEmpty())) {
							if ((number3ScoresNotInteger == 0) && (number3ScoresMissing == 0)) {
							message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(index),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(index));

							message =
									message
											+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
							table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index), message));  
							}
						}
						else if ((number3ScoresNotInteger > 0) || (number3ScoresMissing > 0)) {
							message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(index),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(index));

							message =
									message
											+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,data);
							table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index), message));  
						}
						else {
							try {
						        actual3Sum = Integer.valueOf(data).intValue();
							}
							catch (NumberFormatException e) {
								message =
										String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
												guid,
												getRawDataRow(subject_row_id, group_row_index),
												rgTable.getDataFilePositionMapping(index),
												rgTable.getRepeatableGroupName(),
												rgTable.getColumnName(index));

								message =
										message
												+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,data);
								table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index), message)); 	
							}
						}
						break;
					}
			    }
			    
			    if ((actual3Sum > -1) && (number3ScoresNotInteger == 0) && (number3ScoresMissing == 0)) {
			    	if (actual3Sum != calculated3Sum) {
			    		message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
												String.valueOf(actual3Sum), String.valueOf(calculated3Sum));
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
			    	}
			    }
		    
		    int calculated13Sum = 0;
		    int actual13Sum = -1;
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
		    	if ((bufColumnNameVector.get(i).equalsIgnoreCase("rpqnoisesensscale")) ||
				    (bufColumnNameVector.get(i).equalsIgnoreCase("rpqsleepdistscale")) ||
				    (bufColumnNameVector.get(i).equalsIgnoreCase("rpqfatiguescale")) ||
				    (bufColumnNameVector.get(i).equalsIgnoreCase("rpqirritablescale")) ||
				    (bufColumnNameVector.get(i).equalsIgnoreCase("rpqdepressedscale")) ||
				    (bufColumnNameVector.get(i).equalsIgnoreCase("rpqfrustratedscale")) ||
				    (bufColumnNameVector.get(i).equalsIgnoreCase("rpqforgetfulscale")) ||
				    (bufColumnNameVector.get(i).equalsIgnoreCase("rpqpoorconcscale")) ||
				    (bufColumnNameVector.get(i).equalsIgnoreCase("rpqlongtothinkscale")) ||
				    (bufColumnNameVector.get(i).equalsIgnoreCase("rpqblurredvisionscale")) ||
				    (bufColumnNameVector.get(i).equalsIgnoreCase("rpqlightsensscale")) ||
				    (bufColumnNameVector.get(i).equalsIgnoreCase("rpqdblvisionscale")) ||
				    (bufColumnNameVector.get(i).equalsIgnoreCase("rpqrestlessscale"))) {
		    		data = bufDataVector.get(i);
			    	if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
			    		numberScoresMissing++;
			    		missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
			    	}
			    	else {
				    	try {
				    		rValue = Integer.valueOf(data);
				    		if (rValue != 1) {
		            	        calculated13Sum += rValue;
				    		}
		            	}
		            	catch (NumberFormatException e) {
		            		numberScoresNotInteger++;
				    		notIntegerIndices.add(bufIndex.get(i));
						    notInteger_repeatable_group_id = bufRepeatable_group_id.get(i);
							notInteger_group_row_index = bufGroup_row_index.get(i);
							notIntegerData.add(data);
		            	}
			    	}
		        }
		    }
		    
		    int number13ScoresMissing = numberScoresMissing - number3ScoresMissing;
		    int number13ScoresNotInteger = numberScoresNotInteger -
		    		number3ScoresNotInteger;
			    for (i = 0; i < bufColumnNameVector.size(); i++) {
					if (bufColumnNameVector.get(i).equalsIgnoreCase("rpq13score")) {
						data = bufDataVector.get(i);
						repeatable_group_id = bufRepeatable_group_id.get(i);
						group_row_index = bufGroup_row_index.get(i);
						index = bufIndex.get(i);
						rgTable = table.getRepeatableGroupTable(repeatable_group_id);
						rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
						if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
								(data.trim().isEmpty())) {
							if ((number13ScoresNotInteger == 0) && (number13ScoresMissing == 0)) {
							message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(index),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(index));

							message =
									message
											+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
							table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index), message)); 
							}
						}
						else if ((number13ScoresNotInteger > 0) || (number13ScoresMissing > 0)) {
							message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(index),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(index));

							message =
									message
											+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,data);
							table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index), message)); 
						}
						else {
							try {
						        actual13Sum = Integer.valueOf(data).intValue();	
							}
							catch (NumberFormatException e) {
								message =
										String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
												guid,
												getRawDataRow(subject_row_id, group_row_index),
												rgTable.getDataFilePositionMapping(index),
												rgTable.getRepeatableGroupName(),
												rgTable.getColumnName(index));

								message =
										message
												+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,data);
								table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index), message));    		
							}
						}
						break;
					}
			    }
			    
			    if ((actual13Sum > -1) && (number13ScoresNotInteger == 0) && (number13ScoresMissing == 0)) {
			    	if (actual13Sum != calculated13Sum) {
			    		message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
												String.valueOf(actual13Sum), String.valueOf(calculated13Sum));
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
			    	}
			    }
		    
		    if (numberScoresMissing >= 1) {
		    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						missing_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresMissing; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, missing_group_row_index),
									rgTable.getDataFilePositionMapping(missingIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(missingIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index),
							rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
		    	}
		    } // if (numberScoresMissing >= 1)
		    
		    if (numberScoresNotInteger >= 1) {
		    	rgTable = table.getRepeatableGroupTable(notInteger_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						notInteger_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresNotInteger; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, notInteger_group_row_index),
									rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(notIntegerIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
											notIntegerData.get(i));
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, notInteger_group_row_index),
							rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)), message));
		    	}
		    } // if (numberScoresNotInteger >= 1)
		    
		    int calculatedSum = calculated3Sum + calculated13Sum;
		    int actualSum = -1;
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
				if (bufColumnNameVector.get(i).equalsIgnoreCase("rpqtotalscore")) {
					data = bufDataVector.get(i);
					repeatable_group_id = bufRepeatable_group_id.get(i);
					group_row_index = bufGroup_row_index.get(i);
					index = bufIndex.get(i);
					rgTable = table.getRepeatableGroupTable(repeatable_group_id);
					rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
					if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
					if ((numberScoresMissing == 0) && (numberScoresNotInteger == 0)) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));   
					}
					}
					else if ((numberScoresMissing > 0) || (numberScoresNotInteger > 0)) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));   	
					}
					else {
						try {
					        actualSum = Integer.valueOf(data).intValue();
						}
						catch (NumberFormatException e) {
							message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(index),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(index));

							message =
									message
											+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,data);
							table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index), message));    		
						}
					}
					break;
				}
		    }
		    
		    if ((numberScoresMissing >= 1) || (numberScoresNotInteger >= 1)) {
		    	return;
		    }
		    
		    if (actualSum > -1) {
		    	if (actualSum != calculatedSum) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
											String.valueOf(actualSum), String.valueOf(calculatedSum));
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
		    	}
		    }
	}
	
	

	private void validateBSI18(StructuralFormStructure structure,
			Vector<Vector<Vector<String>>> columnNameVector3, 
			Vector<Vector<Vector<String>>> dataVector3,  int subject_row_id,
			DataStructureTable table) throws Exception {
		    
		
		// 2 possible sets of columnNameVectors and accompanying dataVectors:
		
		// group_column_index = 0 group_columnName = guid
	    // group_column_index = 1 group_columnName = subjectidnum
		// group_column_index = 2 group_columnName = ageyrs
		// group_column_index = 3 group_columnName = visitdate
		// group_column_index = 4 group_columnName = sitename
		// group_column_index = 5 group_columnName = dayssincebaseline
		// group_column_index = 6 group_columnName = casecontrlind
		// group_column_index = 7 group_columnName = generalnotestxt
		
		// group_column_index = 0 group_columnName = bsi18faintscale
		// group_column_index = 1 group_columnName = bsi18nointerestscale
		// group_column_index = 2 group_columnName = bsinervousscale
		// group_column_index = 3 group_columnName = bsi18chestpainscale
		// group_column_index = 4 group_columnName = bsi18feellonelyscale
		// group_column_index = 5 group_columnName = bsi18feeltensescale
		// group_column_index = 6 group_columnName = bsi18nauseascale
		// group_column_index = 7 group_columnName = bsi18feelbluescale
	    // group_column_index = 8 group_columnName = bsi18scaredscale
		// group_column_index = 9 group_columnName = bsi18trblbreathscale
		// group_column_index = 10 group_columnName = bsi18feelworthlessscale
		// group_column_index = 11 group_columnName = bsi18terrororpanicscale
		// group_column_index = 12 group_columnName = bsi18numbscale
		// group_column_index = 13 group_columnName = bsi18feelhopelessscale
		// group_column_index = 14 group_columnName = bsi18feelrestlessscale
		// group_column_index = 15 group_columnName = bsi18feelweakscale
		// group_column_index = 16 group_columnName = bsi18thoughtsendinglifescale
		// group_column_index = 17 group_columnName = bsi18feelfearfulscale
		// group_column_index = 18 group_columnName = bsi18somscoreraw
		// group_column_index = 19 group_columnName = bsi18somscoret
		// group_column_index = 20 group_columnName = bsi18deprscoreraw
		// group_column_index = 21 group_columnName = bsi18deprscoret
	    // group_column_index = 22 group_columnName = bsi18anxscoreraw
		// group_column_index = 23 group_columnName = bsi18anxscoret
		// group_column_index = 24 group_columnName = bsi18gsiscoreraw
		// group_column_index = 25 group_columnName = bsi18gsiscoret

		int i;
		Vector<Vector<String>> columnNameVector2;
        Vector<Vector<String>> dataVector2;
        Vector<String> columnNameVector;
        Vector<String> dataVector;
        String message;
        int repeatable_group_id;
        RepeatableGroupTable rgTable;
        ArrayList<Integer> rgEntries;
        int group_row_index;
        String guid = null;
        
        guidloop:
            for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
       		 
        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
        		dataVector2 = dataVector3.get(repeatable_group_id);
        		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
        		rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
    			for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
    		        columnNameVector = columnNameVector2.get(group_row_index);
    		        dataVector = dataVector2.get(group_row_index);
    		        for (i = 0; i  < columnNameVector.size(); i++) {
    					if (columnNameVector.get(i).equalsIgnoreCase("guid")) {
    					    guid = dataVector.get(i);
    					    break guidloop;
    					}
    		        }
    			}
            }
        loop1:
        	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
        		dataVector2 = dataVector3.get(repeatable_group_id);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries =
						table.getAllReferences(subject_row_id, repeatable_group_id, null);
                
        for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
			columnNameVector = columnNameVector2.get(group_row_index);
			dataVector = dataVector2.get(group_row_index);
			int bsi18somscorerawIndex = -1;
		for (i = 0; i < columnNameVector.size() && (bsi18somscorerawIndex == -1); i++) {
			if (columnNameVector.get(i).equalsIgnoreCase("bsi18somscoreraw")) {
				bsi18somscorerawIndex = i;	
			}
		}
		
		if (bsi18somscorerawIndex == -1) {
			// This is the columnNameVector with the guid.
			continue loop1;
		}
		
		String data = dataVector.get(bsi18somscorerawIndex);
		if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
				(data.trim().isEmpty())) {
		    data = "blank";
		}
		int somSum = 0;
        boolean goodSomSum = true;
        boolean blankSomFound = false;
		for (i = 0; i <= bsi18somscorerawIndex-1; i++) {
		    if ((columnNameVector.get(i).equalsIgnoreCase("bsi18faintscale")) ||
		    	(columnNameVector.get(i).equalsIgnoreCase("bsi18chestpainscale")) ||
		    	(columnNameVector.get(i).equalsIgnoreCase("bsi18nauseascale")) ||
		    	(columnNameVector.get(i).equalsIgnoreCase("bsi18trblbreathscale")) ||
		    	(columnNameVector.get(i).equalsIgnoreCase("bsi18numbscale")) ||
		    	(columnNameVector.get(i).equalsIgnoreCase("bsi18feelweakscale"))) {
		    	String bsData = dataVector.get(i);
		    	if ((bsData == null) || (bsData.isEmpty()) || (bsData.trim() == null) ||
		    			(bsData.trim().isEmpty())) {
		            blankSomFound = true;
		        }
		    	else {
			    	try {
	            	    somSum += Integer.valueOf(bsData);
	            	}
	            	catch (NumberFormatException e) {
	            		goodSomSum = false;
	            		message =
	    						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
	    								guid,
	    								getRawDataRow(subject_row_id, group_row_index),
	    								rgTable.getDataFilePositionMapping(i),
	    								rgTable.getRepeatableGroupName(),
	    								rgTable.getColumnName(i));

	    				message =
	    						message
	    								+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
	    										bsData);
	    				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
	    						rgTable.getDataFilePositionMapping(i), message));
	            	}
		    	}
		    }
		} // for (i = 0; i <= bsi18somscorerawIndex-1; i++) 
		
		int listedSomSum = 0;
        boolean goodListedSomSum = true;
        try {
            listedSomSum = Integer.valueOf(data).intValue();
        }
        catch (NumberFormatException e) {
        	goodListedSomSum = false;
        	message =
					String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
							guid,
							getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(bsi18somscorerawIndex),
							rgTable.getRepeatableGroupName(),
							rgTable.getColumnName(bsi18somscorerawIndex));

			message =
					message
							+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
									data);
			if  (!data.equalsIgnoreCase("blank")) {
			    table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
					rgTable.getDataFilePositionMapping(bsi18somscorerawIndex), message));
			}
			else {
				table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(bsi18somscorerawIndex), message));	
			}
        }
        
        if (blankSomFound) { 
            if  (!data.equalsIgnoreCase("blank")) {
        
	        	message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(bsi18somscorerawIndex),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(bsi18somscorerawIndex));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,
										data);
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(bsi18somscorerawIndex), message));
            } //  if  (!data.equalsIgnoreCase("blank"))
        } // if (blankSomFound)
        else if ((goodSomSum && goodListedSomSum && (listedSomSum != somSum)) ||
        		(goodSomSum && (!goodListedSomSum))) {
        	message =
					String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
							guid,
							getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(bsi18somscorerawIndex),
							rgTable.getRepeatableGroupName(),
							rgTable.getColumnName(bsi18somscorerawIndex));

			message =
					message
							+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
									data, String.valueOf(somSum));
			if  (!data.equalsIgnoreCase("blank")) {
			    table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
					rgTable.getDataFilePositionMapping(bsi18somscorerawIndex), message));
			}
			else {
				table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(bsi18somscorerawIndex), message));	
			}
        }
        
        int bsi18deprscorerawIndex = -1;
		for (i = 0; i < columnNameVector.size() && (bsi18deprscorerawIndex == -1); i++) {
			if (columnNameVector.get(i).equalsIgnoreCase("bsi18deprscoreraw")) {
				bsi18deprscorerawIndex = i;	
			}
		}
		
		data = dataVector.get(bsi18deprscorerawIndex);
		if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
				(data.trim().isEmpty())) {
		    data = "blank";
		}
		int deprSum = 0;
        boolean goodDeprSum = true;
        boolean blankDeprFound = false;
		for (i = 0; i <= bsi18deprscorerawIndex-1; i++) {
		    if ((columnNameVector.get(i).equalsIgnoreCase("bsi18nointerestscale")) ||
		    	(columnNameVector.get(i).equalsIgnoreCase("bsi18feellonelyscale")) ||
		    	(columnNameVector.get(i).equalsIgnoreCase("bsi18feelbluescale")) ||
		    	(columnNameVector.get(i).equalsIgnoreCase("bsi18feelworthlessscale")) ||
		    	(columnNameVector.get(i).equalsIgnoreCase("bsi18feelhopelessscale")) ||
		    	(columnNameVector.get(i).equalsIgnoreCase("bsi18thoughtsendinglifescale"))) {
		    	String bsData = dataVector.get(i);
		    	if ((bsData == null) || (bsData.isEmpty()) || (bsData.trim() == null) ||
		    			(bsData.trim().isEmpty())) {
		            blankDeprFound = true;
		        }
		    	else {
			    	try {
	            	    deprSum += Integer.valueOf(bsData);
	            	}
	            	catch (NumberFormatException e) {
	            		goodDeprSum = false;
	            		message =
	    						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
	    								guid,
	    								getRawDataRow(subject_row_id, group_row_index),
	    								rgTable.getDataFilePositionMapping(i),
	    								rgTable.getRepeatableGroupName(),
	    								rgTable.getColumnName(i));

	    				message =
	    						message
	    								+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
	    										bsData);
	    				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
	    						rgTable.getDataFilePositionMapping(i), message));
	            	}
		    	}
		    }
		} // for (i = 0; i <= bsi18deprscorerawIndex-1; i++) 
		
		int listedDeprSum = 0;
        boolean goodListedDeprSum = true;
        try {
            listedDeprSum = Integer.valueOf(data).intValue();
        }
        catch (NumberFormatException e) {
        	goodListedDeprSum = false;
        	message =
					String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
							guid,
							getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(bsi18deprscorerawIndex),
							rgTable.getRepeatableGroupName(),
							rgTable.getColumnName(bsi18deprscorerawIndex));

			message =
					message
							+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
									data);
			if  (!data.equalsIgnoreCase("blank")) {
			    table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
					rgTable.getDataFilePositionMapping(bsi18deprscorerawIndex), message));
			}
			else {
				table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(bsi18deprscorerawIndex), message));	
			}
        }
        
        if (blankDeprFound) { 
            if  (!data.equalsIgnoreCase("blank")) {
        
	        	message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(bsi18deprscorerawIndex),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(bsi18deprscorerawIndex));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,
										data);
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(bsi18deprscorerawIndex), message));
            } //  if  (!data.equalsIgnoreCase("blank"))
        } // if (blankDeprFound)
        else if ((goodDeprSum && goodListedDeprSum && (listedDeprSum != deprSum)) ||
        		(goodDeprSum && (!goodListedDeprSum))) {
        	message =
					String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
							guid,
							getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(bsi18deprscorerawIndex),
							rgTable.getRepeatableGroupName(),
							rgTable.getColumnName(bsi18deprscorerawIndex));

			message =
					message
							+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
									data, String.valueOf(deprSum));	
			if  (!data.equalsIgnoreCase("blank")) {
			    table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
					rgTable.getDataFilePositionMapping(bsi18deprscorerawIndex), message));
			}
			else {
				table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(bsi18deprscorerawIndex), message));	
			}
        }
        
        int bsi18anxscorerawIndex = -1;
		for (i = 0; i < columnNameVector.size() && (bsi18anxscorerawIndex == -1); i++) {
			if (columnNameVector.get(i).equalsIgnoreCase("bsi18anxscoreraw")) {
				bsi18anxscorerawIndex = i;	
			}
		}
		
		data = dataVector.get(bsi18anxscorerawIndex);
		if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
				(data.trim().isEmpty())) {
		    data = "blank";
		}
		int anxSum = 0;
        boolean goodAnxSum = true;
        boolean blankAnxFound = false;
		for (i = 0; i <= bsi18anxscorerawIndex-1; i++) {
		    if ((columnNameVector.get(i).equalsIgnoreCase("bsinervousscale")) ||
		    	(columnNameVector.get(i).equalsIgnoreCase("bsi18feeltensescale")) ||
		    	(columnNameVector.get(i).equalsIgnoreCase("bsi18scaredscale")) ||
		    	(columnNameVector.get(i).equalsIgnoreCase("bsi18terrororpanicscale")) ||
		    	(columnNameVector.get(i).equalsIgnoreCase("bsi18feelrestlessscale")) ||
		    	(columnNameVector.get(i).equalsIgnoreCase("bsi18feelfearfulscale"))) {
		    	String bsData = dataVector.get(i);
		    	if ((bsData == null) || (bsData.isEmpty()) || (bsData.trim() == null)
		    			|| (bsData.trim().isEmpty())) {
		            blankAnxFound = true;
		        }
		    	else {
			    	try {
	            	    anxSum += Integer.valueOf(bsData);
	            	}
	            	catch (NumberFormatException e) {
	            		goodAnxSum = false;
	            		message =
	    						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
	    								guid,
	    								getRawDataRow(subject_row_id, group_row_index),
	    								rgTable.getDataFilePositionMapping(i),
	    								rgTable.getRepeatableGroupName(),
	    								rgTable.getColumnName(i));

	    				message =
	    						message
	    								+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
	    										bsData);
	    				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
	    						rgTable.getDataFilePositionMapping(i), message));
	            	}
		    	}
		    }
		} // for (i = 0; i <= bsi18anxscorerawIndex-1; i++) 
		
		int listedAnxSum = 0;
        boolean goodListedAnxSum = true;
        try {
            listedAnxSum = Integer.valueOf(data).intValue();
        }
        catch (NumberFormatException e) {
        	goodListedAnxSum = false;
        	message =
					String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
							guid,
							getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(bsi18anxscorerawIndex),
							rgTable.getRepeatableGroupName(),
							rgTable.getColumnName(bsi18anxscorerawIndex));

			message =
					message
							+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
									data);
			if  (!data.equalsIgnoreCase("blank")) {
			    table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
					rgTable.getDataFilePositionMapping(bsi18anxscorerawIndex), message));
			}
			else {
				 table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(bsi18anxscorerawIndex), message));	
			}
        }
        
        if (blankAnxFound) { 
            if  (!data.equalsIgnoreCase("blank")) {
        
	        	message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(bsi18anxscorerawIndex),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(bsi18anxscorerawIndex));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,
										data);
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(bsi18anxscorerawIndex), message));
            } // if  (!data.equalsIgnoreCase("blank"))
        } // if (blankAnxFound)
        else if ((goodAnxSum && goodListedAnxSum && (listedAnxSum != anxSum)) ||
        		(goodAnxSum && (!goodListedAnxSum))) {
        	message =
					String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
							guid,
							getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(bsi18anxscorerawIndex),
							rgTable.getRepeatableGroupName(),
							rgTable.getColumnName(bsi18anxscorerawIndex));

			message =
					message
							+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
									data, String.valueOf(anxSum));	
			if  (!data.equalsIgnoreCase("blank")) {
			    table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
					rgTable.getDataFilePositionMapping(bsi18anxscorerawIndex), message));
			}
			else {
				table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(bsi18anxscorerawIndex), message));	
			}
        }
        
        int bsi18gsiscorerawIndex = -1;
		for (i = 0; i < columnNameVector.size() && (bsi18gsiscorerawIndex == -1); i++) {
			if (columnNameVector.get(i).equalsIgnoreCase("bsi18gsiscoreraw")) {
				bsi18gsiscorerawIndex = i;	
			}
		}
		
		data = dataVector.get(bsi18gsiscorerawIndex);
		if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
				(data.trim().isEmpty())) {
		    data = "blank";
		}
		
		int sum = 0;
        boolean goodSum = true;
        boolean blankFound = false;
        if (blankSomFound || blankDeprFound || blankAnxFound) {
        	blankFound = true;
        }
        try {
        	sum = somSum + deprSum + anxSum;
        }
        catch (Exception e) {
    		goodSum = false;
    	}
        
        int listedSum = 0;
        boolean goodListedSum = true;
        try {
            listedSum = Integer.valueOf(data).intValue();
        }
        catch (NumberFormatException e) {
        	goodListedSum = false;
        	message =
					String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
							guid,
							getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(bsi18gsiscorerawIndex),
							rgTable.getRepeatableGroupName(),
							rgTable.getColumnName(bsi18gsiscorerawIndex));

			message =
					message
							+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
									data);
			if  (!data.equalsIgnoreCase("blank")) {
			    table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
					rgTable.getDataFilePositionMapping(bsi18gsiscorerawIndex), message));
			}
			else {
				table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(bsi18gsiscorerawIndex), message));	
			}
        }
        
        if (blankFound) { 
            if  (!data.equalsIgnoreCase("blank")) {
        
	        	message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(bsi18gsiscorerawIndex),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(bsi18gsiscorerawIndex));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,
										data);
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(bsi18gsiscorerawIndex), message));
            } // if  (!data.equalsIgnoreCase("blank"))
        } // if (blankFound)
        else if ((goodSum && goodListedSum && (listedSum != sum)) ||
        		(goodSum && (!goodListedSum))) {
        	message =
					String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
							guid,
							getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(bsi18gsiscorerawIndex),
							rgTable.getRepeatableGroupName(),
							rgTable.getColumnName(bsi18gsiscorerawIndex));

			message =
					message
							+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
									data, String.valueOf(sum));	
			if  (!data.equalsIgnoreCase("blank")) {
			    table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
					rgTable.getDataFilePositionMapping(bsi18gsiscorerawIndex), message));
			}
			else {
				table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(bsi18gsiscorerawIndex), message));
				}	
        }
        } // for (int group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++)
        } // for (int repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) 
	}

	private void validateBDI2(StructuralFormStructure structure,
			Vector<Vector<Vector<String>>> columnNameVector3, 
			Vector<Vector<Vector<String>>> dataVector3, int subject_row_id, 
			DataStructureTable table) throws Exception {
		    		    
		    // 3 possible sets of columnNameVectors and accompanying dataVectors:
		    
		    // group_column_index = 0 group_columnName = contexttype
		    // group_column_index = 1 group_columnName = contexttypeoth	
		    // group_column_index = 2 group_columnName = datasource
		    // group_column_index = 3 group_columnName = datasourceoth
		    
		    // group_column_index = 0 group_columnName = guid
		    // group_column_index = 1 group_columnName = subjectidnum
		    // group_column_index = 2 group_columnName = ageyrs
		    // group_column_index = 3 group_columnName = visitdate
		    // group_column_index = 4 group_columnName = sitename
		    // group_column_index = 5 group_columnName = dayssincebaseline
		    // group_column_index = 6 group_columnName = casecontrlind
		    // group_column_index = 7 group_columnName = generalnotestxt
		    
		    // group_column_index = 0 group_columnName = bdiiisadnessscale
		    // group_column_index = 1 group_columnName = bdiiipessimismscale
		    // group_column_index = 2 group_columnName = bdiiipastfailurescale
		    // group_column_index = 3 group_columnName = bdiiilossofpleasurescale
		    // group_column_index = 4 group_columnName = bdiiiguiltyfeelingsscale
		    // group_column_index = 5 group_columnName = bdiiipunishmentfeelingsscale
		    // group_column_index = 6 group_columnName = bdiiiselfdislikescale
		    // group_column_index = 7 group_columnName = bdiiiselfcriticalnessscale
		    // group_column_index = 8 group_columnName = bdiiisuicidalscale
		    // group_column_index = 9 group_columnName = bdiiicryingscale
		    // group_column_index = 10 group_columnName = bdiiiagitationscale
		    // group_column_index = 11 group_columnName = bdiiilossofinterestscale
		    // group_column_index = 12 group_columnName = bdiiiindecisivnessscale
		    // group_column_index = 13 group_columnName = bdiiiworthlessnessscale
		    // group_column_index = 14 group_columnName = bdiiilossofenergyscale
		    // group_column_index = 15 group_columnName = bdiiisleeppatternscale
		    // group_column_index = 16 group_columnName = bdiiiirritabilityscale
		    // group_column_index = 17 group_columnName = bdiiiappetitescale
		    // group_column_index = 18 group_columnName = bdiiiconcentrationscale
		    // group_column_index = 19 group_columnName = bdiiitirednessscale
		    // group_column_index = 20 group_columnName = bdiiilossinterestsexscale
		    // group_column_index = 21 group_columnName = bdiiitotalscore
		
		    int i;
		    String message;
	        Vector<Vector<String>> columnNameVector2;
	        Vector<Vector<String>> dataVector2;
	        Vector<String> columnNameVector;
	        Vector<String> dataVector;
	        int repeatable_group_id;
	        RepeatableGroupTable rgTable;
	        ArrayList<Integer> rgEntries;
	        int group_row_index;
	        String guid = null;
	        guidloop:
	            for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	       		 
	        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
	        		dataVector2 = dataVector3.get(repeatable_group_id);
	        		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	        		rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
	    			for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
	    		        columnNameVector = columnNameVector2.get(group_row_index);
	    		        dataVector = dataVector2.get(group_row_index);
	    		        for (i = 0; i  < columnNameVector.size(); i++) {
	    					if (columnNameVector.get(i).equalsIgnoreCase("guid")) {
	    					    guid = dataVector.get(i);
	    					    break guidloop;
	    					}
	    		        }
	    			}
	            }
	        loop1:
	        	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
	        		dataVector2 = dataVector3.get(repeatable_group_id);
					rgTable = table.getRepeatableGroupTable(repeatable_group_id);
					rgEntries =
							table.getAllReferences(subject_row_id, repeatable_group_id, null);

	        for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
				columnNameVector = columnNameVector2.get(group_row_index);
				dataVector = dataVector2.get(group_row_index);
		    int contextIndex = -1;
		    for (i = 0; i < columnNameVector.size() && (contextIndex == -1); i++) {
				if (columnNameVector.get(i).equalsIgnoreCase("contexttype")) {
					contextIndex = i;	
				}
			}
		    
		    if (contextIndex >= 0) {
		    	// No extra validation performed on vector with contexttype
		    	continue loop1;
		    }
		    
		    int ageIndex = -1;
		    
		    for (i = 0; i < columnNameVector.size() && (ageIndex == -1); i++) {
				if (columnNameVector.get(i).equalsIgnoreCase("ageyrs")) {
					ageIndex = i;	
				}
			}
		    
		    if (ageIndex >= 0) {
		    	String ageData = dataVector.get(ageIndex);
				if ((ageData == null) || (ageData.isEmpty()) || (ageData.trim() == null) ||
						(ageData.trim().isEmpty())) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(ageIndex),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(ageIndex));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(ageIndex), message));
					continue loop1;
				}	
				else {
					double age = Double.valueOf(ageData).doubleValue();
					if (age < 13) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(ageIndex),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(ageIndex));

						message =
								message
										+ String.format(ApplicationsConstants.WARNING_INCORRECT_AGE,
										  ageData, "13 or more years");
						table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(ageIndex), message));
					}
					continue loop1;
				}
		    } // if (ageIndex >= 0)
		    
		    String bdi2Data;
		    int numberScoresMissing = 0;
		    Vector<Integer>missingIndices = new Vector<Integer>();
		    int numberScoresNotInteger = 0;
		    Vector<Integer>notIntegerIndices = new Vector<Integer>();
		    Vector<String>notIntegerData = new Vector<String>();
		    int bdi2TotalScore = -1;
		    int calculatedTotalScore = 0;
		    int totalScoreIndex = -1;
		    int bdi2Scores[] = new int[columnNameVector.size()-1];
		    for (i = 0; i < columnNameVector.size(); i++) {
		    	bdi2Data = dataVector.get(i);
				if ((bdi2Data == null) || (bdi2Data.isEmpty()) || (bdi2Data.trim() == null) ||
						(bdi2Data.trim().isEmpty())) {
					if (!columnNameVector.get(i).equalsIgnoreCase("bdiiitotalscore")) {
					    numberScoresMissing++;
					    missingIndices.add(i);
					    bdi2Scores[i] = 0;
					}
					else {
						// Total score value was missing
						totalScoreIndex = i;
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(i),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(i));

						message =
								message
										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(i), message));	
					}
				}
				else {
					if (columnNameVector.get(i).equalsIgnoreCase("bdiiitotalscore")) {
						try {
						    bdi2TotalScore = Integer.valueOf(bdi2Data).intValue();
						}
						catch (NumberFormatException e) {
							message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(i),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(i));

							message =
									message
											+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,bdi2Data);
							table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(i), message));	
						}
						totalScoreIndex = i;
					}
					else if (columnNameVector.get(i).equalsIgnoreCase("bdiiisleeppatternscale") ||
							(columnNameVector.get(i).equalsIgnoreCase("bdiiiappetitescale"))) {
						if (bdi2Data.equalsIgnoreCase("1")) {
							bdi2Scores[i] = 0;
						}
						else if (bdi2Data.equalsIgnoreCase("1a") || bdi2Data.equalsIgnoreCase("1b")) {
							bdi2Scores[i] = 1;
						}
						else if (bdi2Data.equalsIgnoreCase("2a") || bdi2Data.equalsIgnoreCase("2b")) {
							bdi2Scores[i] = 2;
						}
						else if (bdi2Data.equalsIgnoreCase("3a") || bdi2Data.equalsIgnoreCase("3b")) {
							bdi2Scores[i] = 3;
						}
						calculatedTotalScore += bdi2Scores[i];
					}
					else {
						try {
						    bdi2Scores[i] = Integer.valueOf(bdi2Data).intValue();
						}
						catch (NumberFormatException e) {
							numberScoresNotInteger++;
						    notIntegerIndices.add(i);
						    notIntegerData.add(bdi2Data);
						    bdi2Scores[i] = 0;	
						}
						calculatedTotalScore += bdi2Scores[i];
					}
				}
			} // for (i = 0; i < columnNameVector.size(); i++)
		    if (numberScoresMissing > 1) {
		    	for (i = 0; i < numberScoresMissing; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(missingIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(missingIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
		    	}
		    
		    } // if (numberScoresMissing > 1)
		    
		    if (numberScoresNotInteger >= 1) {
		    	for (i = 0; i < numberScoresNotInteger; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(notIntegerIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,notIntegerData.get(i));
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)), message));
		    	}	
		    } // if (numberScoresNotInteger >= 1)
		    
		    if ((numberScoresMissing > 1) || (numberScoresNotInteger >= 1)) {
		    	if (bdi2TotalScore > -1) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(totalScoreIndex),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(totalScoreIndex));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,
											String.valueOf(bdi2TotalScore));	
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(totalScoreIndex), message));    	
		    	}
		    	return;
		    }
		    
		    if (bdi2TotalScore > -1) {
		    	if (bdi2TotalScore != calculatedTotalScore) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(totalScoreIndex),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(totalScoreIndex));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
											String.valueOf(bdi2TotalScore), String.valueOf(calculatedTotalScore));	
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(totalScoreIndex), message));
		    	}
		    }
	        } // for (int group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++)
	    	} // for (int repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) 
	}

	
	private void validateCOWAT(StructuralFormStructure structure,
			Vector<Vector<Vector<String>>> columnNameVector3, 
			Vector<Vector<Vector<String>>>dataVector3, int subject_row_id,
			DataStructureTable table) throws Exception {
		
		// table.getColumnCount = 5
		// repeatable_group_id = 0
	    // rgEntries.size() = 1
	    // group_row_index = 0
		// columnNameVector.size() = 8
		// group_column_index = 0 group_column_name = guid data = TBI_INVCB298TTM
		// group_column_index = 1 group_column_name = subjectidnum data = 1234
		// group_column_index = 2 group_column_name = ageyrs data = 25
		// group_column_index = 3 group_column_name = visitdate data = 2017-04-11T00:00:00Z
		// group_column_index = 4 group_column_name = sitename data = University of Miami
		// group_column_index = 5 group_column_name = dayssincebaseline data = 60
		// group_column_index = 6 group_column_name = casecontrlind data = case
		// group_column_index = 7 group_column_name = generalnotestxt data = Phonemic test.COWATCategCorrectWordsNum= Totals.COWATCategCorrectWordsNum= Totals.COWATTtlCorrecWordsNum= Totals.COWATRawTotalScore
		// repeatable_group_id = 1
	    // rgEntries.size() = 3
		// group_row_index = 0
		// columnNameVector.size() = 9
		// group_column_index = 0 group_column_name = trialnum data = Trial 1
		// group_column_index = 1 group_column_name = cowatlettername data = F
		// group_column_index = 2 group_column_name = cowatwordslistindivlettrcount data = 16
		// group_column_index = 3 group_column_name = cowatindivdullettrcorrectnum data = 16
		// group_column_index = 4 group_column_name = cowatindivdullettrerrorsnum data = 8
		// group_column_index = 5 group_column_name = cowatindivdullettrrepetnum data = 0
		// group_column_index = 6 group_column_name = cowatcategcorrectwordsnum data = null
		// group_column_index = 7 group_column_name = cowatcategerrorsnum data = null
		// group_column_index = 8 group_column_name = cowatcategrepetlnum data = null
		// group_row_index = 1
		// columnNameVector.size() = 9
		// group_column_index = 0 group_column_name = trialnum data = Trial 2
		// group_column_index = 1 group_column_name = cowatlettername data = A
		// group_column_index = 2 group_column_name = cowatwordslistindivlettrcount data = 18
		// group_column_index = 3 group_column_name = cowatindivdullettrcorrectnum data = 10
		// group_column_index = 4 group_column_name = cowatindivdullettrerrorsnum data = 6
		// group_column_index = 5 group_column_name = cowatindivdullettrrepetnum data = 8
		// group_column_index = 6 group_column_name = cowatcategcorrectwordsnum data = null
		// group_column_index = 7 group_column_name = cowatcategerrorsnum data = null
		// group_column_index = 8 group_column_name = cowatcategrepetlnum data = null
		// group_row_index = 2
		// columnNameVector.size() = 9
		// group_column_index = 0 group_column_name = trialnum data = Trial 3
		// group_column_index = 1 group_column_name = cowatlettername data = S
		// group_column_index = 2 group_column_name = cowatwordslistindivlettrcount data = 20
		// group_column_index = 3 group_column_name = cowatindivdullettrcorrectnum data = 12
		// group_column_index = 4 group_column_name = cowatindivdullettrerrorsnum data = 6
		// group_column_index = 5 group_column_name = cowatindivdullettrrepetnum data = 6
		// group_column_index = 6 group_column_name = cowatcategcorrectwordsnum data = 38
		// group_column_index = 7 group_column_name = cowatcategerrorsnum data = 20
		// group_column_index = 8 group_column_name = cowatcategrepetlnum data = 14
		// repeatable_group_id = 2
	    // rgEntries.size() = 1
		// group_row_index = 0
		// columnNameVector.size() = 4
		// group_column_index = 0 group_column_name = contexttype data = null
		// group_column_index = 1 group_column_name = contexttypeoth data = null
		// group_column_index = 2 group_column_name = datasource data = participant/subject
		// group_column_index = 3 group_column_name = datasourceoth data = null
		// repeatable_group_id = 3
		// rgEntries.size() = 3
		// group_row_index = 0
		// columnNameVector.size() = 7
	    // group_column_index = 0 group_column_name = trialnum data = Trial 1
		// group_column_index = 1 group_column_name = cowattestcatnam data = Semantic
		// group_column_index = 2 group_column_name = cowatsemantcatnam data = Animals
		// group_column_index = 3 group_column_name = cowatsemantcatwordcount data = 16
		// group_column_index = 4 group_column_name = cowatcategcorrectwordsnum data = 16
		// group_column_index = 5 group_column_name = cowatcategrepetlnum data = 0
		// group_column_index = 6 group_column_name = cowatcategerrorsnum data = 8
		// group_row_index = 1
		// columnNameVector.size() = 7
		// group_column_index = 0 group_column_name = trialnum data = Trial 2
		// group_column_index = 1 group_column_name = cowattestcatnam data = Semantic
		// group_column_index = 2 group_column_name = cowatsemantcatnam data = Furniture
		// group_column_index = 3 group_column_name = cowatsemantcatwordcount data = 15
		// group_column_index = 4 group_column_name = cowatcategcorrectwordsnum data = 14
		// group_column_index = 5 group_column_name = cowatcategrepetlnum data = 1
		// group_column_index = 6 group_column_name = cowatcategerrorsnum data = 7
		// group_row_index = 2
		// columnNameVector.size() = 7
	    // group_column_index = 0 group_column_name = trialnum data = Trial 3
		// group_column_index = 1 group_column_name = cowattestcatnam data = Semantic
		// group_column_index = 2 group_column_name = cowatsemantcatnam data = Transport
	    // group_column_index = 3 group_column_name = cowatsemantcatwordcount data = 18
		// group_column_index = 4 group_column_name = cowatcategcorrectwordsnum data = 15
		// group_column_index = 5 group_column_name = cowatcategrepetlnum data = 0
		// group_column_index = 6 group_column_name = cowatcategerrorsnum data = 7
		// repeatable_group_id = 4
		// rgEntries.size() = 2
		// group_row_index = 0
		// columnNameVector.size() = 9
		// group_column_index = 0 group_column_name = cowattestcatnam data = Semantic
		// group_column_index = 1 group_column_name = cowatcategcorrectwordsnum data = 45
		// group_column_index = 2 group_column_name = cowatcategerrorsnum data = 22
		// group_column_index = 3 group_column_name = cowatcategrepetlnum data = 1
		// group_column_index = 4 group_column_name = cowatttlcorrecwordsnum data = 45
		// group_column_index = 5 group_column_name = cowatttlerrorslnum data = 42
		// group_column_index = 6 group_column_name = cowatttlrepetnum data = 15
		// group_column_index = 7 group_column_name = cowatrawtotalscore data = 45
		// group_column_index = 8 group_column_name = cowatadjustedtotalscore data = 43
		// group_row_index = 1
		// columnNameVector.size() = 9
		// group_column_index = 0 group_column_name = cowattestcatnam data = Phonemic
		// group_column_index = 1 group_column_name = cowatcategcorrectwordsnum data = 37
		// group_column_index = 2 group_column_name = cowatcategerrorsnum data = 20
		// group_column_index = 3 group_column_name = cowatcategrepetlnum data = 14
		// group_column_index = 4 group_column_name = cowatttlcorrecwordsnum data = 36
		// group_column_index = 5 group_column_name = cowatttlerrorslnum data = null
		// group_column_index = 6 group_column_name = cowatttlrepetnum data = null
		// group_column_index = 7 group_column_name = cowatrawtotalscore data = 39
		// group_column_index = 8 group_column_name = cowatadjustedtotalscore data = 36
				
		int i;
		String message;
		
		int ageIndex = -1;
		boolean haveAgeIndex = false;
		int cowatrawtotalscoreIndex = -1;
		int cowatCategCorrectWordsNumIndex = -1;
		int cowatTtlCorrecWordsNumIndex = -1;
		int calculatedTotalRawPhonemicScore = 0;
		int actualTotalRawPhonemicScore = -1;
		int cowatPhonemicCategCorrectWordsNum = -1;
		int cowatPhonemicTtlCorrecWordsNum = -1;
		int cowatPhonemicrawtotalscoreIndex = -1;
		int cowatPhonemicCategCorrectWordsNumIndex = -1;
		int cowatPhonemicTtlCorrecWordsNumIndex = -1;
		int phonemic_group_row_index = -1;
		int phonemic_repeatable_group_id = -1;
		RepeatableGroupTable phonemic_rgTable = null;
		ArrayList<Integer> phonemic_rgEntries = null;
		int calculatedTotalRawSemanticScore = 0;
		int actualTotalRawSemanticScore = -1;
		int cowatSemanticCategCorrectWordsNum = -1;
		int cowatSemanticTtlCorrecWordsNum = -1;
		int cowatSemanticrawtotalscoreIndex = -1;
		int cowatSemanticCategCorrectWordsNumIndex = -1;
		int cowatSemanticTtlCorrecWordsNumIndex = -1;
		int semantic_group_row_index = -1;
		int semantic_repeatable_group_id = -1;
		RepeatableGroupTable semantic_rgTable = null;
		ArrayList<Integer> semantic_rgEntries = null;
		
		Vector<Vector<String>> columnNameVector2;
        Vector<Vector<String>> dataVector2;
        Vector<String> columnNameVector;
        Vector<String> dataVector;
        String guid = null;
        int repeatable_group_id;
        RepeatableGroupTable rgTable;
        ArrayList<Integer> rgEntries;
        int group_row_index;
        
        guidloop:
        for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
   		 
    		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
    		dataVector2 = dataVector3.get(repeatable_group_id);
    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
    		rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
			for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
		        columnNameVector = columnNameVector2.get(group_row_index);
		        dataVector = dataVector2.get(group_row_index);
		        for (i = 0; i  < columnNameVector.size(); i++) {
					if (columnNameVector.get(i).equalsIgnoreCase("guid")) {
					    guid = dataVector.get(i);
					    break guidloop;
					}
		        }
			}
        }
		 
        //System.out.println("table.getColumnCount = " + table.getColumnCount());
        loop1:
        	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
        		//System.out.println("repeatable_group_id = " + repeatable_group_id);
        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
        		dataVector2 = dataVector3.get(repeatable_group_id);
			    rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries =
						table.getAllReferences(subject_row_id, repeatable_group_id, null);
				//System.out.println("rgEntries.size() = " + rgEntries.size());

        loop2:
				for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
        	//System.out.println("group_row_index = " + group_row_index);
			columnNameVector = columnNameVector2.get(group_row_index);
			dataVector = dataVector2.get(group_row_index);
			//System.out.println("columnNameVector.size() = " + columnNameVector.size());
			//for (i = 0; i < columnNameVector.size(); i++) {
			    //System.out.println("group_column_index = " + i +
			    	//" group_column_name = " + columnNameVector.get(i) +
			    	//" data = " + dataVector.get(i)) ;	
			//}
			
			for (i = 0; i  < columnNameVector.size(); i++) {
				if (columnNameVector.get(i).equalsIgnoreCase("contexttype")) {
					continue loop1;	
				}	
			}
			
			for (i = 0; i < columnNameVector.size() && (ageIndex == -1); i++) {
				if (columnNameVector.get(i).equalsIgnoreCase("ageyrs")) {
					ageIndex = i;	
				}
			}
		    
		    if ((!haveAgeIndex) && (ageIndex >= 0)) {
		    	haveAgeIndex = true;
		    	String ageData = dataVector.get(ageIndex);
				if ((ageData == null) || (ageData.isEmpty()) || (ageData.trim() == null) ||
						(ageData.trim().isEmpty())) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(ageIndex),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(ageIndex));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(ageIndex), message));
					continue loop1;
				}	
				else {
				    double age = Double.valueOf(ageData).doubleValue();
					if ((age < 6) || (age > 69)) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(ageIndex),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(ageIndex));

						message =
								message
										+ String.format(ApplicationsConstants.WARNING_INCORRECT_AGE,
										  ageData, "6-69 years");
						table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(ageIndex), message));
					}
					continue loop1;
				}
		    } // if ((!haveAgeIndex) && (ageIndex >= 0))
		    
		    int trialnumIndex = -1;
		    int cowatletternameIndex = -1;
		    int cowatindivdullettrcorrectnumIndex = -1;
		    for (i = 0; i  < columnNameVector.size(); i++) {
		    	if (columnNameVector.get(i).equalsIgnoreCase("trialnum")) {	
		    	    trialnumIndex = i;	
		    	}
		    	else if (columnNameVector.get(i).equalsIgnoreCase("cowatlettername")) {	
		    	    cowatletternameIndex = i;	
		    	}
		    	else if (columnNameVector.get(i).equalsIgnoreCase("cowatindivdullettrcorrectnum")) {	
		    		cowatindivdullettrcorrectnumIndex = i;	
		    	}
		    }
		    
		    String data;
		    int indexArray[] = new int[] {trialnumIndex, cowatletternameIndex, 
		    		cowatindivdullettrcorrectnumIndex};
		    if ((trialnumIndex >= 0) && (cowatletternameIndex >= 0)) {
		    	// Phonemic test
		    	if ((group_row_index == 0) && (rgEntries.size() < 3)) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(trialnumIndex),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(trialnumIndex));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_FEWER3_PHONEMIC,
									  String.valueOf(rgEntries.size()));
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(trialnumIndex), message));	
		    	} // if ((group_row_index == 0) && (rgEntries.size() < 3))
		    	for (i = 0; i < indexArray.length; i++) {
			    	data = dataVector.get(indexArray[i]);
					if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(indexArray[i]),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(indexArray[i]));
	
						message =
								message
										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(indexArray[i]), message));
					}
					else if (indexArray[i] == cowatindivdullettrcorrectnumIndex) {
						try {
					        calculatedTotalRawPhonemicScore += Integer.valueOf(data).intValue();
						}
						catch (NumberFormatException e) {
							message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(indexArray[i]),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(indexArray[i]));
		
							message =
									message
											+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,data);
							table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(indexArray[i]), message));	
						}
					}
				} // for (i = 0; i < indexArray.length; i++)
		    } // if ((trialnumIndex >= 0) && (cowatletternameIndex >= 0)) 
		    
		    boolean havePhonemic = false;
		    cowatrawtotalscoreIndex = -1;
		    cowatCategCorrectWordsNumIndex = -1;
			cowatTtlCorrecWordsNumIndex = -1;
		    for (i = 0; i  < columnNameVector.size(); i++) {
		    	if (columnNameVector.get(i).equalsIgnoreCase("cowattestcatnam")) {	
		    		data = dataVector.get(i);
					if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(i),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(i));
	
						message =
								message
										+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(i), message));
						return;
					}
					if (data.equalsIgnoreCase("Phonemic")) {
						havePhonemic = true;
					}
		    	}
		    	else if (columnNameVector.get(i).equalsIgnoreCase("cowatrawtotalscore")) {	
		    		cowatrawtotalscoreIndex = i;	
		    	}
		    	else if (columnNameVector.get(i).equalsIgnoreCase("cowatcategcorrectwordsnum")) {	
		    		cowatCategCorrectWordsNumIndex = i;	
		    	}
		    	else if (columnNameVector.get(i).equalsIgnoreCase("cowatttlcorrecwordsnum")) {	
		    		cowatTtlCorrecWordsNumIndex = i;	
		    	}
		    } // for (i = 0; i  < columnNameVector.size(); i++)
		    
		    if (havePhonemic && (cowatrawtotalscoreIndex >= 0)) {
		    	data = dataVector.get(cowatrawtotalscoreIndex);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(cowatrawtotalscoreIndex),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(cowatrawtotalscoreIndex));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(cowatrawtotalscoreIndex), message));	
				}
				else {
					try {
					    actualTotalRawPhonemicScore = Integer.valueOf(data).intValue();
					}
					catch (NumberFormatException e) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(cowatrawtotalscoreIndex),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(cowatrawtotalscoreIndex));


						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(cowatrawtotalscoreIndex), message));		
					}
					cowatPhonemicrawtotalscoreIndex = cowatrawtotalscoreIndex;
			    	phonemic_group_row_index = group_row_index;
			    	phonemic_repeatable_group_id = repeatable_group_id;
				}
				data = dataVector.get(cowatCategCorrectWordsNumIndex);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(cowatCategCorrectWordsNumIndex),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(cowatCategCorrectWordsNumIndex));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(cowatCategCorrectWordsNumIndex), message));	
				}
				else {
					try {
					    cowatPhonemicCategCorrectWordsNum = Integer.valueOf(data).intValue();
					}
					catch (NumberFormatException e) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(cowatCategCorrectWordsNumIndex),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(cowatCategCorrectWordsNumIndex));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER, data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(cowatCategCorrectWordsNumIndex), message));		
					}
					cowatPhonemicCategCorrectWordsNumIndex = cowatCategCorrectWordsNumIndex;
				}
				data = dataVector.get(cowatTtlCorrecWordsNumIndex);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(cowatTtlCorrecWordsNumIndex),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(cowatTtlCorrecWordsNumIndex));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(cowatTtlCorrecWordsNumIndex), message));	
				}
				else {
					try {
					    cowatPhonemicTtlCorrecWordsNum = Integer.valueOf(data).intValue();
					}
					catch (NumberFormatException e) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(cowatTtlCorrecWordsNumIndex),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(cowatTtlCorrecWordsNumIndex));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(cowatTtlCorrecWordsNumIndex), message));		
					}
					cowatPhonemicTtlCorrecWordsNumIndex = cowatTtlCorrecWordsNumIndex;
				}
				continue loop2;
		    } // if (havePhonemic && (cowatrawtotalscoreIndex >= 0))
		    
		    trialnumIndex = -1;
		    int cowatsemantcatnamIndex = -1;
		    int cowatcategcorrectwordsnumIndex = -1;
		    for (i = 0; i  < columnNameVector.size(); i++) {
		    	if (columnNameVector.get(i).equalsIgnoreCase("trialnum")) {	
		    	    trialnumIndex = i;	
		    	}
		    	else if (columnNameVector.get(i).equalsIgnoreCase("cowatsemantcatnam")) {	
		    		cowatsemantcatnamIndex = i;	
		    	}
		    	else if (columnNameVector.get(i).equalsIgnoreCase("cowatcategcorrectwordsnum")) {	
		    		cowatcategcorrectwordsnumIndex = i;	
		    	}
		    }
		    
		    indexArray = new int[] {trialnumIndex, cowatsemantcatnamIndex, 
		    		cowatcategcorrectwordsnumIndex};
		    if ((trialnumIndex >= 0) && (cowatsemantcatnamIndex >= 0)) {
		    	// Semantic test
		    	if ((group_row_index == 0) && (rgEntries.size() < 3)) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(trialnumIndex),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(trialnumIndex));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_FEWER3_SEMANTIC,
									  String.valueOf(rgEntries.size()));
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(trialnumIndex), message));	
		    	} // if ((group_row_index == 0) && (rgEntries.size() < 3))
		    	for (i = 0; i < indexArray.length; i++) {
			    	data = dataVector.get(indexArray[i]);
					if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(indexArray[i]),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(indexArray[i]));
	
						message =
								message
										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(indexArray[i]), message));
					}
					else if (indexArray[i] == cowatcategcorrectwordsnumIndex) {
						try {
					        calculatedTotalRawSemanticScore += Integer.valueOf(data).intValue();
						}
						catch (NumberFormatException e) {
							message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(indexArray[i]),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(indexArray[i]));
		
							message =
									message
											+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,data);
							table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(indexArray[i]), message));	
						}
					}
				} // for (i = 0; i < indexArray.length; i++)
		    } // if ((trialnumIndex >= 0) && (cowatsemantcatnamIndex >= 0)) 
		    
		    boolean haveSemantic = false;
		    cowatrawtotalscoreIndex = -1;
		    cowatCategCorrectWordsNumIndex = -1;
			cowatTtlCorrecWordsNumIndex = -1;
		    for (i = 0; i  < columnNameVector.size(); i++) {
		    	if (columnNameVector.get(i).equalsIgnoreCase("cowattestcatnam")) {	
		    		data = dataVector.get(i);
					if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(i),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(i));
	
						message =
								message
										+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(i), message));
						return;
					}
					if (data.equalsIgnoreCase("Semantic")) {
						haveSemantic = true;
					}
		    	}
		    	else if (columnNameVector.get(i).equalsIgnoreCase("cowatrawtotalscore")) {	
		    		cowatrawtotalscoreIndex = i;	
		    	}
		    	else if (columnNameVector.get(i).equalsIgnoreCase("cowatcategcorrectwordsnum")) {	
		    		cowatCategCorrectWordsNumIndex = i;	
		    	}
		    	else if (columnNameVector.get(i).equalsIgnoreCase("cowatttlcorrecwordsnum")) {	
		    		cowatTtlCorrecWordsNumIndex = i;	
		    	}
		    } // for (i = 0; i  < columnNameVector.size(); i++)
		    
		    if (haveSemantic && (cowatrawtotalscoreIndex >= 0)) {
		    	data = dataVector.get(cowatrawtotalscoreIndex);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(cowatrawtotalscoreIndex),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(cowatrawtotalscoreIndex));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(cowatrawtotalscoreIndex), message));	
				}
				else {
					try {
					    actualTotalRawSemanticScore = Integer.valueOf(data).intValue();
					}
					catch (NumberFormatException e) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(cowatrawtotalscoreIndex),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(cowatrawtotalscoreIndex));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(cowatrawtotalscoreIndex), message));		
					}
					cowatSemanticrawtotalscoreIndex = cowatrawtotalscoreIndex;
			    	semantic_group_row_index = group_row_index;
			    	semantic_repeatable_group_id = repeatable_group_id;
				}
				data = dataVector.get(cowatCategCorrectWordsNumIndex);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(cowatCategCorrectWordsNumIndex),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(cowatCategCorrectWordsNumIndex));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(cowatCategCorrectWordsNumIndex), message));	
				}
				else {
					try {
					    cowatSemanticCategCorrectWordsNum = Integer.valueOf(data).intValue();
					}
					catch (NumberFormatException e) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(cowatCategCorrectWordsNumIndex),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(cowatCategCorrectWordsNumIndex));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(cowatCategCorrectWordsNumIndex), message));		
					}
					cowatSemanticCategCorrectWordsNumIndex = cowatCategCorrectWordsNumIndex;
				}
				data = dataVector.get(cowatTtlCorrecWordsNumIndex);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(cowatTtlCorrecWordsNumIndex),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(cowatTtlCorrecWordsNumIndex));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(cowatTtlCorrecWordsNumIndex), message));	
				}
				else {
					try {
					    cowatSemanticTtlCorrecWordsNum = Integer.valueOf(data).intValue();
					}
					catch (NumberFormatException e) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(cowatTtlCorrecWordsNumIndex),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(cowatTtlCorrecWordsNumIndex));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(cowatTtlCorrecWordsNumIndex), message));		
					}
					cowatSemanticTtlCorrecWordsNumIndex = cowatTtlCorrecWordsNumIndex;
				}
				continue loop2;
		    } // if (haveSemantic && (cowatrawtotalscoreIndex >= 0))
        } // for (int group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++)
	    } // for (int repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) 
        
        if (actualTotalRawPhonemicScore >= 0) {
        	phonemic_rgTable = table.getRepeatableGroupTable(phonemic_repeatable_group_id);
			phonemic_rgEntries =
					table.getAllReferences(subject_row_id, phonemic_repeatable_group_id, null);
			
            if (actualTotalRawPhonemicScore != calculatedTotalRawPhonemicScore) {
				message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, phonemic_group_row_index),
								phonemic_rgTable.getDataFilePositionMapping(cowatPhonemicrawtotalscoreIndex),
								phonemic_rgTable.getRepeatableGroupName(),
								phonemic_rgTable.getColumnName(cowatPhonemicrawtotalscoreIndex));
	
				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
										String.valueOf(actualTotalRawPhonemicScore), String.valueOf(calculatedTotalRawPhonemicScore));
				table.addOutput(new ValidationOutput(phonemic_rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, phonemic_group_row_index),
						phonemic_rgTable.getDataFilePositionMapping(cowatPhonemicrawtotalscoreIndex), message));	
            } // if (actualTotalRawPhonemicScore != calculatedTotalRawPhonemicScore)
            
            if ((cowatPhonemicCategCorrectWordsNum >= 0) &&
            	(actualTotalRawPhonemicScore != cowatPhonemicCategCorrectWordsNum)) {
            	message =
    					String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
    							guid,
    							getRawDataRow(subject_row_id, phonemic_group_row_index),
    							phonemic_rgTable.getDataFilePositionMapping(cowatPhonemicCategCorrectWordsNumIndex),
    							phonemic_rgTable.getRepeatableGroupName(),
    							phonemic_rgTable.getColumnName(cowatPhonemicCategCorrectWordsNumIndex));

    			message =
    					message
    							+ String.format(ApplicationsConstants.ERR_TWO_NOT_EQUAL,
    									"Phonemic cowatcategcorrectwordsnum", String.valueOf(cowatPhonemicCategCorrectWordsNum),
    									"Phonemic cowatrawtotalscore", String.valueOf(actualTotalRawPhonemicScore));
    			table.addOutput(new ValidationOutput(phonemic_rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, phonemic_group_row_index),
    					phonemic_rgTable.getDataFilePositionMapping(cowatPhonemicCategCorrectWordsNumIndex), message));		
            } // if ((cowatPhonemicCategCorrectWordsNum >= 0) &&
            
            if ((cowatPhonemicTtlCorrecWordsNum >= 0) &&
               (actualTotalRawPhonemicScore != cowatPhonemicTtlCorrecWordsNum)) {
            	message =
    					String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
    							guid,
    							getRawDataRow(subject_row_id, phonemic_group_row_index),
    							phonemic_rgTable.getDataFilePositionMapping(cowatPhonemicTtlCorrecWordsNumIndex),
    							phonemic_rgTable.getRepeatableGroupName(),
    							phonemic_rgTable.getColumnName(cowatPhonemicTtlCorrecWordsNumIndex));

    			message =
    					message
    							+ String.format(ApplicationsConstants.ERR_TWO_NOT_EQUAL,
    									"Phonemic cowatttlcorrecwordsnum", String.valueOf(cowatPhonemicTtlCorrecWordsNum),
    									"Phonemic cowatrawtotalscore", String.valueOf(actualTotalRawPhonemicScore));
    			table.addOutput(new ValidationOutput(phonemic_rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, phonemic_group_row_index),
    					phonemic_rgTable.getDataFilePositionMapping(cowatPhonemicTtlCorrecWordsNumIndex), message));			   
            } // if (cowatPhonemicTtlCorrecWordsNum >= 0) &&
		} // if (actualTotalRawPhonemicScore >= 0)
        
        if (actualTotalRawSemanticScore >= 0) { 
        	semantic_rgTable = table.getRepeatableGroupTable(semantic_repeatable_group_id);
			semantic_rgEntries =
					table.getAllReferences(subject_row_id, semantic_repeatable_group_id, null);
			
            if (actualTotalRawSemanticScore != calculatedTotalRawSemanticScore) {
				message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, semantic_group_row_index),
								semantic_rgTable.getDataFilePositionMapping(cowatSemanticrawtotalscoreIndex),
								semantic_rgTable.getRepeatableGroupName(),
								semantic_rgTable.getColumnName(cowatSemanticrawtotalscoreIndex));
	
				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
										String.valueOf(actualTotalRawSemanticScore), String.valueOf(calculatedTotalRawSemanticScore));
				table.addOutput(new ValidationOutput(semantic_rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, semantic_group_row_index),
						semantic_rgTable.getDataFilePositionMapping(cowatSemanticrawtotalscoreIndex), message));	
            } // if (actualTotalRawSemanticScore != calculatedTotalRawSemanticScore)
            
            if ((cowatSemanticCategCorrectWordsNum >= 0) &&
                	(actualTotalRawSemanticScore != cowatSemanticCategCorrectWordsNum)) {
            	message =
    					String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
    							guid,
    							getRawDataRow(subject_row_id, semantic_group_row_index),
    							semantic_rgTable.getDataFilePositionMapping(cowatSemanticCategCorrectWordsNumIndex),
    							semantic_rgTable.getRepeatableGroupName(),
    							semantic_rgTable.getColumnName(cowatSemanticCategCorrectWordsNumIndex));

    			message =
    					message
    							+ String.format(ApplicationsConstants.ERR_TWO_NOT_EQUAL,
    									"Semantic cowatcategcorrectwordsnum", String.valueOf(cowatSemanticCategCorrectWordsNum),
    									"Semantic cowatrawtotalscore", String.valueOf(actualTotalRawSemanticScore));
    			table.addOutput(new ValidationOutput(semantic_rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, semantic_group_row_index),
    					semantic_rgTable.getDataFilePositionMapping(cowatSemanticCategCorrectWordsNumIndex), message));		    	
            } // if ((cowatSemanticCategCorrectWordsNum >= 0) &&
            
            if ((cowatSemanticTtlCorrecWordsNum >= 0) &&
               (actualTotalRawSemanticScore != cowatSemanticTtlCorrecWordsNum)) {
            	message =
    					String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
    							guid,
    							getRawDataRow(subject_row_id, semantic_group_row_index),
    							semantic_rgTable.getDataFilePositionMapping(cowatSemanticTtlCorrecWordsNumIndex),
    							semantic_rgTable.getRepeatableGroupName(),
    							semantic_rgTable.getColumnName(cowatSemanticTtlCorrecWordsNumIndex));

    			message =
    					message
    							+ String.format(ApplicationsConstants.ERR_TWO_NOT_EQUAL,
    									"Semantic cowatttlcorrecwordsnum", String.valueOf(cowatSemanticTtlCorrecWordsNum),
    									"Semantic cowatrawtotalscore", String.valueOf(actualTotalRawSemanticScore));
    			table.addOutput(new ValidationOutput(semantic_rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, semantic_group_row_index),
    					semantic_rgTable.getDataFilePositionMapping(cowatSemanticTtlCorrecWordsNumIndex), message));			   	   
            } // if (cowatSemanticTtlCorrecWordsNum >= 0) &&
		} // if (actualTotalRawSemanticScore >= 0)
	}

	private void validateNSI1(StructuralFormStructure structure,
			Vector<Vector<Vector<String>>> columnNameVector3, 
			Vector<Vector<Vector<String>>>dataVector3, int subject_row_id,
			DataStructureTable table) throws Exception {
		// 2 possible sets of columnNameVectors and accompanying dataVectors:
		 
		// group_column_index = 0 group_columnName = guid
		// group_column_index = 1 group_columnName = subjectidnum
		// group_column_index = 2 group_columnName = ageyrs
		// group_column_index = 3 group_columnName = visitdate
		// group_column_index = 4 group_columnName = sitename
		// group_column_index = 5 group_columnName = dayssincebaseline
		// group_column_index = 6 group_columnName = casecontrlind
		// group_column_index = 7 group_columnName = generalnotestxt
		
		// group_column_index = 0 group_columnName = nsidizscore
		// group_column_index = 1 group_columnName = nsibalncimprmntscore
		// group_column_index = 2 group_columnName = nsicoordntnimprmntscore
		// group_column_index = 3 group_columnName = nsihdchescore
		// group_column_index = 4 group_columnName = nsinausscore
		// group_column_index = 5 group_columnName = nsivisnimprmntscore
		// group_column_index = 6 group_columnName = nsisenstvlgtscore
		// group_column_index = 7 group_columnName = nsihearngimprmntscore
		// group_column_index = 8 group_columnName = nsisenstvnoisscore
		// group_column_index = 9 group_columnName = nsinumbnssimprmntscore
		// group_column_index = 10 group_columnName = nsitastimprmntscore
		// group_column_index = 11 group_columnName = nsiapptitimprmntscore
		// group_column_index = 12 group_columnName = nsiconctrtnimprmntscore
		// group_column_index = 13 group_columnName = nsimmryimprmntscore
		// group_column_index = 14 group_columnName = nsidecsnimprmntscore
		// group_column_index = 15 group_columnName = nsislwthnkngimprmntscore
		// group_column_index = 16 group_columnName = nsifatigimprmntscore
		// group_column_index = 17 group_columnName = nsislpimprmntscore
		// group_column_index = 18 group_columnName = nsianxtyimprmntscore
		// group_column_index = 19 group_columnName = nsideprsnimprmntscore
		// group_column_index = 20 group_columnName = nsiirrabltyimprmntscore
		// group_column_index = 21 group_columnName = nsifrustrtnimprmntscore
		// group_column_index = 22 group_columnName = nsitotalscore
		// group_column_index = 23 group_columnName = nsifrmcompltdate
		
		int i;
		String message;
		
		int ageIndex = -1;
		boolean haveAgeIndex = false;
		
		Vector<Vector<String>> columnNameVector2;
        Vector<Vector<String>> dataVector2;
        Vector<String> columnNameVector;
        Vector<String> dataVector;
        int repeatable_group_id;
        RepeatableGroupTable rgTable;
        ArrayList<Integer> rgEntries;
        int group_row_index;
        String guid = null;
        
        guidloop:
            for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
       		 
        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
        		dataVector2 = dataVector3.get(repeatable_group_id);
        		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
        		rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
    			for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
    		        columnNameVector = columnNameVector2.get(group_row_index);
    		        dataVector = dataVector2.get(group_row_index);
    		        for (i = 0; i  < columnNameVector.size(); i++) {
    					if (columnNameVector.get(i).equalsIgnoreCase("guid")) {
    					    guid = dataVector.get(i);
    					    break guidloop;
    					}
    		        }
    			}
            }
        
        loop1:
        	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
        		dataVector2 = dataVector3.get(repeatable_group_id);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries =
						table.getAllReferences(subject_row_id, repeatable_group_id, null);

        for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
			columnNameVector = columnNameVector2.get(group_row_index);
			dataVector = dataVector2.get(group_row_index);
	    
	    for (i = 0; i < columnNameVector.size() && (ageIndex == -1); i++) {
			if (columnNameVector.get(i).equalsIgnoreCase("ageyrs")) {
				ageIndex = i;	
			}
		}
	    
	    if ((!haveAgeIndex) && (ageIndex >= 0)) {
	    	haveAgeIndex = true;
	    	String ageData = dataVector.get(ageIndex);
			if ((ageData == null) || (ageData.isEmpty()) || (ageData.trim() == null) ||
					(ageData.trim().isEmpty())) {
				message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(ageIndex),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(ageIndex));

				message =
						message
								+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
				table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(ageIndex), message));
				continue loop1;
			}	
			else {
				double age = Double.valueOf(ageData).doubleValue();
				if (age < 18) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(ageIndex),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(ageIndex));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_INCORRECT_AGE,
									  ageData, "18 or more years");
					table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(ageIndex), message));
				}
				continue loop1;
			}
	    } // if (ageIndex >= 0)
	    
	    String nsi1Data;
	    int numberScoresMissing = 0;
	    Vector<Integer>missingIndices = new Vector<Integer>();
	    int numberScoresNotInteger = 0;
	    Vector<Integer>notIntegerIndices = new Vector<Integer>();
	    Vector<String>notIntegerData = new Vector<String>();
	    int nsi1TotalScore = -1;
	    int calculatedTotalScore = 0;
	    int totalScoreIndex = -1;
	    int nsi1Scores[] = new int[columnNameVector.size()-2];
	    for (i = 0; i < columnNameVector.size(); i++) {
	    	nsi1Data = dataVector.get(i);
			if ((nsi1Data == null) || (nsi1Data.isEmpty()) || (nsi1Data.trim() == null) ||
					(nsi1Data.trim().isEmpty())) {
				if ((!columnNameVector.get(i).equalsIgnoreCase("nsitotalscore")) &&
				    (!columnNameVector.get(i).equalsIgnoreCase("nsifrmcompltdate"))) {
				    numberScoresMissing++;
				    missingIndices.add(i);
				    nsi1Scores[i] = 0;
				}
				else if (columnNameVector.get(i).equalsIgnoreCase("nsitotalscore")){
					// Total score value was missing
					totalScoreIndex = i;
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(i),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(i));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(i), message));	
				}
			}
			else {
				if (columnNameVector.get(i).equalsIgnoreCase("nsitotalscore")) {
					try {
					    nsi1TotalScore = Integer.valueOf(nsi1Data).intValue();
					}
					catch (NumberFormatException e) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(i),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(i));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,nsi1Data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(i), message));		
					}
					totalScoreIndex = i;
				}
				
				else {
					try {
					    nsi1Scores[i] = Integer.valueOf(nsi1Data).intValue();
					}
					catch (NumberFormatException e) {
						 numberScoresNotInteger++;
						 notIntegerIndices.add(i);
						 nsi1Scores[i] = 0;	
						 notIntegerData.add(nsi1Data);
					}
					calculatedTotalScore += nsi1Scores[i];
				}
			}
		} // for (i = 0; i < columnNameVector.size(); i++)
	    if (numberScoresMissing >= 1) {
	    	for (i = 0; i < numberScoresMissing; i++) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(missingIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(missingIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
	    	}
	    } // if (numberScoresMissing >= 1)
	    
	    if (numberScoresNotInteger >= 1) {
	    	for (i = 0; i < numberScoresNotInteger; i++) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(notIntegerIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
										notIntegerData.get(i));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)), message));
	    	}
	    } // if (numberScoresNotInteger >= 1)
	    
	    if ((numberScoresMissing >= 1) || (numberScoresNotInteger >= 1)) {
	    	if (nsi1TotalScore > -1) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(totalScoreIndex),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(totalScoreIndex));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,
										String.valueOf(nsi1TotalScore));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),	
						rgTable.getDataFilePositionMapping(totalScoreIndex), message));
	    	}
	    	return;
	    }
	    
	    if (nsi1TotalScore > -1) {
	    	if (nsi1TotalScore != calculatedTotalScore) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(totalScoreIndex),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(totalScoreIndex));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
										String.valueOf(nsi1TotalScore), String.valueOf(calculatedTotalScore));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(totalScoreIndex), message));
	    	}
	    }
	    } // for (int group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++)
	    } // for (int repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) 
	}


	private void validatePSQI(StructuralFormStructure structure,
			Vector<Vector<Vector<String>>> columnNameVector3, 
			Vector<Vector<Vector<String>>> dataVector3, int subject_row_id,
			DataStructureTable table) throws Exception {
		// 3 possible sets of columnNameVectors and accompanying dataVectors:
		
		// group_column_index = 0 group_columnName = guid
		// group_column_index = 1 group_columnName = subjectidnum
		// group_column_index = 2 group_columnName = ageyrs
	    // group_column_index = 3 group_columnName = visitdate
		// group_column_index = 4 group_columnName = sitename
	    // group_column_index = 5 group_columnName = casecontrlind
		// group_column_index = 6 group_columnName = dayssincebaseline
		// group_column_index = 7 group_columnName = generalnotestxt	
		
		// visitdate appears as:
		// 2016-05-23T00:00:00Z
	    // 2016-01-25T00:00:00Z

		// group_column_index = 0 group_columnName = psqibedpastmonthtime
		// Typical value = 2:00
	    // group_column_index = 1 group_columnName = psqifallasleeppastmonthscl
		// Note that this is 0;1;2;3
		// The original time to fall asleep in minutes has been
		// irreversibly lost.
		// group_column_index = 2 group_columnName = psqiwakeuppastmonthtime
		// Typical value = 6:30
		// group_column_index = 3 group_columnName = psqiactsleeppastmonthscl
		// Note that this is 0;1;2;3
		// The original actual sleeping time in hours has been irreversibly 
		// lost.  This means that the psqiSleepEfficiencyScl calculation
		// which uses the actual sleeping time in hours cannot be
		// double checked.
	    // group_column_index = 4 group_columnName = psqinosleepin30minpastmonthscl
		// group_column_index = 5 group_columnName = psqiwakeupnightpastmonthscl
		// group_column_index = 6 group_columnName = psqiwakeupbathroompastmonthscl
		// group_column_index = 7 group_columnName = psqicannotbreathepastmonthscl
	    // group_column_index = 8 group_columnName = psqicoughsnoreloudpastmonthscl
		// group_column_index = 9 group_columnName = psqifeelcoldpastmonthscl
		// group_column_index = 10 group_columnName = psqifeelhotpastmonthscl
		// group_column_index = 11 group_columnName = psqihadbaddreamspastmonthscl
	    // group_column_index = 12 group_columnName = psqipainpastmonthscl
		// group_column_index = 13 group_columnName = psqiotherrsnpastmonthscloth
	    // group_column_index = 14 group_columnName = psqiotherrsnpastmonthscl
		// group_column_index = 15 group_columnName = psqisleepqualitypastmonthscl
		// group_column_index = 16 group_columnName = psqisleepmedpastmonthscl
		// group_column_index = 17 group_columnName = psqitroublestayawakepastmoscl
		// group_column_index = 18 group_columnName = psqiproblemethusiasmpastmoscl
		// group_column_index = 19 group_columnName = psqibedpartnerscl
		// group_column_index = 20 group_columnName = psqiprtnrrprtloudsnorepstmoscl
		// group_column_index = 21 group_columnName = psqiprtnrrprtpsebreathpstmoscl
		// group_column_index = 22 group_columnName = psqiprtnrrprttwitchpstmonthscl
		// group_column_index = 23 group_columnName = psqiprtnrrprtdisorientpstmoscl
		// group_column_index = 24 group_columnName = psqiptnrrprtothrestpstmoscloth
		// group_column_index = 25 group_columnName = psqiprtnrrprtothrestpastmoscl
		// group_column_index = 26 group_columnName = psqisleepdistbscl
		// group_column_index = 27 group_columnName = psqisleeplatencyscl
		// group_column_index = 28 group_columnName = psqidaydysfunctionsleepscl
		// group_column_index = 29 group_columnName = psqisleepefficiencyscl
		// group_column_index = 30 group_columnName = psqitotalscore
				
		// group_column_index = 0 group_columnName = contexttype
		// group_column_index = 1 group_columnName = contexttypeoth
	    // group_column_index = 2 group_columnName = datasource
	    // group_column_index = 3 group_columnName = datasourceoth
		int i;
		String message;
        int ageIndex = -1;
        boolean haveAgeIndex = false;
        boolean Q5jNeeded = false;
        Vector<Vector<String>> columnNameVector2;
        Vector<Vector<String>> dataVector2;
        Vector<String> columnNameVector;
        Vector<String> dataVector;
        String visitDate = null;
        
        String psqiData;
	    int numberScoresMissing = 0; // does not include Q5j
	    int numberQ5jScoresMissing = 0;
	    int numberScoresNotInteger = 0;
	    int numberQ5jScoresNotInteger = 0;
	    Vector<Integer>missingIndices = new Vector<Integer>();
	    Vector<Integer>Q5jIndices = new Vector<Integer>();
	    int psqiActSleepPastMonthScl = -1; 
	    int psqiSleepMedPastMonthScl = -1;
	    int psqiSleepQualityPastMonthScl = -1;
	    int psqiTroubleStayAwakePastMoScl = -1;
	    int psqiProblemEnthusiasmPastMoScl = -1;
	    int psqiDayDysunctionSleepScl = -1;
	    int calculatedpsqiDayDysfunctionSleepScl = 0;
	    int dayDysfunctionSleepSclIndex = -1;
	    int psqiWakeupNightPastMonthScl = 0;
	    int psqiWakeupBathroomPastMonthScl = 0;
	    int psqiCannotBreathePastMonthScl = 0;
	    int psqiCoughSnoreLoudPastMonthScl = 0;
	    int psqiFeelColdPastMonthScl = 0;
	    int psqiFeelHotPastMonthScl = 0;
	    int psqiHadBadDreamsPastMonthScl = 0;
	    int psqiPainPastMonthScl = 0;
	    int psqiOtherRsnPastMonthScl = 0;
	    String psqiOtherRsnPastMonthSclOth = null;
	    int psqiSleepDistbScl = -1;
	    int calculatedpsqiSleepDistbScl = 0;
	    int sleepDistbSclIndex = -1;
	    int psqiSleepEfficiencyScl = -1;
	    int psqiFallAsleepPastMonthScl  = 0;
	    int psqiNoSleepIn30minPastMonthScl = 0;
	    int psqiSleepLatencyScl = -1;
	    int calculatedpsqiSleepLatencyScl = 0;
	    int sleepLatencySclIndex = -1;
	    int psqiTotalScore = -1;
	    int calculatedTotalScore = 0;
	    int totalScoreIndex = -1;
	    String name;
	    
	    int psqi_group_row_index = -1;
		int psqi_repeatable_group_id = -1;
		RepeatableGroupTable psqi_rgTable = null;
		ArrayList<Integer> psqi_rgEntries = null;
		int repeatable_group_id;
		RepeatableGroupTable rgTable;
		ArrayList<Integer> rgEntries;
		int group_row_index;
		String guid = null;
		
		guidloop:
	        for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	   		 
	    		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
	    		dataVector2 = dataVector3.get(repeatable_group_id);
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
			        columnNameVector = columnNameVector2.get(group_row_index);
			        dataVector = dataVector2.get(group_row_index);
			        for (i = 0; i  < columnNameVector.size(); i++) {
						if (columnNameVector.get(i).equalsIgnoreCase("guid")) {
						    guid = dataVector.get(i);
						    break guidloop;
						}
			        }
				}
	        }
		
        loop1:
        	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
        		dataVector2 = dataVector3.get(repeatable_group_id);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries =
						table.getAllReferences(subject_row_id, repeatable_group_id, null);

        for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
			columnNameVector = columnNameVector2.get(group_row_index);
			dataVector = dataVector2.get(group_row_index);
        
        for (i = 0; i < columnNameVector.size(); i++) {
        	if (columnNameVector.get(i).equalsIgnoreCase("contexttype")) {
        		continue loop1;
        	}
        }
	    
	    for (i = 0; i < columnNameVector.size(); i++) {
			if (columnNameVector.get(i).equalsIgnoreCase("ageyrs")) {
				ageIndex = i;	
			}
			else if (columnNameVector.get(i).equalsIgnoreCase("visitdate")) {
				visitDate = dataVector.get(i);
			}
		}
	    
	    if ((!haveAgeIndex) && (ageIndex >= 0)) {
	    	if (visitDate != null) {
		        int comparison = compareDates(visitDate, "2005-05-20");
		        Q5jNeeded = (comparison == -1);
		    }
		    else {
		    	Q5jNeeded = false;
		    }
	    	haveAgeIndex = true;
	    	String ageData = dataVector.get(ageIndex);
			if ((ageData == null) || (ageData.isEmpty()) || (ageData.trim() == null) ||
					(ageData.trim().isEmpty())) {
				message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(ageIndex),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(ageIndex));

				message =
						message
								+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
				table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(ageIndex), message));
				continue loop1;
			}	
			else {
				double age = Double.valueOf(ageData).doubleValue();
				if (age < 18) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(ageIndex),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(ageIndex));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_INCORRECT_AGE,
									  ageData, "18 or more years");
					table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(ageIndex), message));
				}
				continue loop1;
			}
	    } // if ((!haveAgeIndex) && (ageIndex >= 0))
	   
	    psqi_group_row_index = group_row_index;
	    psqi_repeatable_group_id = repeatable_group_id;
	    
	    for (i = 0; i < columnNameVector.size(); i++) {
	    	name = columnNameVector.get(i);
	    	psqiData = dataVector.get(i);
			if ((psqiData == null) || (psqiData.isEmpty()) || (psqiData.trim() == null) ||
					(psqiData.trim().isEmpty())) {
				if (name.equalsIgnoreCase("psqiotherrsnpastmonthscloth")
						|| name.equalsIgnoreCase("psqiotherrsnpastmonthscl")) {
				    numberQ5jScoresMissing++;
				    Q5jIndices.add(i);
				}
				else if ((!name.equalsIgnoreCase("psqisleepdistbscl")) &&
				    (!name.equalsIgnoreCase("psqisleeplatencyscl")) &&
				    (!name.equalsIgnoreCase("psqidaydysfunctionsleepscl")) &&
				    (!name.equalsIgnoreCase("psqibedpastmonthtime")) &&
				    (!name.equalsIgnoreCase("psqiwakeuppastmonthtime")) &&
				    (!name.equalsIgnoreCase("psqitotalscore"))  &&
				    (!name.equalsIgnoreCase("psqiotherrsnpastmonthscloth")) &&
				    (!name.equalsIgnoreCase("psqiotherrsnpastmonthscl")) &&
				    (!name.equalsIgnoreCase("psqibedpartnerscl")) &&
				    (!name.equalsIgnoreCase("psqiprtnrrprtloudsnorepstmoscl")) &&
				    (!name.equalsIgnoreCase("psqiprtnrrprtpsebreathpstmoscl")) &&
				    (!name.equalsIgnoreCase("psqiprtnrrprttwitchpstmonthscl")) &&
				    (!name.equalsIgnoreCase("psqiprtnrrprtdisorientpstmoscl")) &&
				    (!name.equalsIgnoreCase("psqiptnrrprtothrestpstmoscloth")) &&
				    (!name.equalsIgnoreCase("psqiprtnrrprtothrestpastmoscl"))) { 
				    numberScoresMissing++;
				    missingIndices.add(i);
				}
				else if (name.equalsIgnoreCase("psqisleepdistbscl")){
					// PSQISleepDistbScl value was missing
					sleepDistbSclIndex = i;
					message = 
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(i),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(i));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(i), message));	
				}
				else if (name.equalsIgnoreCase("psqisleeplatencyscl")){
					// PSQISleepLatencyScl  value was missing
					sleepLatencySclIndex = i;
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(i),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(i));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(i), message));	
				}
				else if (name.equalsIgnoreCase("psqidaydysfunctionsleepscl")){
					// PSQIDayDysfunctionSleepScl  value was missing
					dayDysfunctionSleepSclIndex = i;
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(i),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(i));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(i), message));	
				}
				else if (name.equalsIgnoreCase("psqitotalscore")){
					// PSQITotalScore  value was missing
					totalScoreIndex  = i;
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(i),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(i));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(i), message));	
				}
				else if ((name.equalsIgnoreCase("psqibedpastmonthtime")) ||
			    (name.equalsIgnoreCase("psqiwakeuppastmonthtime"))) {
					// Values required but not used in score calculation
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(i),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(i));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(i), message));		
				}
			}
			else {
				try {
					if (name.equalsIgnoreCase("psqiactsleeppastmonthscl")) {
						psqiActSleepPastMonthScl = Integer.valueOf(psqiData).intValue();
					}
					else if (name.equalsIgnoreCase("psqitroublestayawakepastmoscl")) {
						psqiTroubleStayAwakePastMoScl = Integer.valueOf(psqiData).intValue();	
					}
					else if (name.equalsIgnoreCase("psqiproblemethusiasmpastmoscl")) {
						psqiProblemEnthusiasmPastMoScl = Integer.valueOf(psqiData).intValue();
					}
					else if (name.equalsIgnoreCase("psqidaydysfunctionsleepscl")) {
						 psqiDayDysunctionSleepScl = Integer.valueOf(psqiData).intValue();
						 dayDysfunctionSleepSclIndex = i;
					} 
					else if (name.equalsIgnoreCase("psqiwakeupnightpastmonthscl")) {
						psqiWakeupNightPastMonthScl = Integer.valueOf(psqiData).intValue();
					}
					else if (name.equalsIgnoreCase("psqiwakeupbathroompastmonthscl")) {
						psqiWakeupBathroomPastMonthScl = Integer.valueOf(psqiData).intValue();
					}
					else if (name.equalsIgnoreCase("psqicannotbreathepastmonthscl")) {
						psqiCannotBreathePastMonthScl = Integer.valueOf(psqiData).intValue();
					}
					else if (name.equalsIgnoreCase("psqicoughsnoreloudpastmonthscl")) {
						psqiCoughSnoreLoudPastMonthScl = Integer.valueOf(psqiData).intValue();
					}
					else if (name.equalsIgnoreCase("psqifeelcoldpastmonthscl")) {
						psqiFeelColdPastMonthScl = Integer.valueOf(psqiData).intValue();
					}
					else if (name.equalsIgnoreCase("psqifeelhotpastmonthscl")) {
						psqiFeelHotPastMonthScl = Integer.valueOf(psqiData).intValue();
					}
					else if (name.equalsIgnoreCase("psqihadbaddreamspastmonthscl")) {
						psqiHadBadDreamsPastMonthScl = Integer.valueOf(psqiData).intValue();
					}
					else if (name.equalsIgnoreCase("psqipainpastmonthscl")) {
						psqiPainPastMonthScl = Integer.valueOf(psqiData).intValue();
					}
					else if (name.equalsIgnoreCase("psqiotherrsnpastmonthscl")) {
						psqiOtherRsnPastMonthScl = Integer.valueOf(psqiData).intValue();
					}
					else if (name.equalsIgnoreCase("psqiotherrsnpastmonthscloth")) {
						psqiOtherRsnPastMonthSclOth = psqiData;
					}
					else if (name.equalsIgnoreCase("psqisleepdistbscl")) {
						psqiSleepDistbScl = Integer.valueOf(psqiData).intValue();
						sleepDistbSclIndex = i;
					}
					else if (name.equalsIgnoreCase("psqisleepefficiencyscl")) {
						 psqiSleepEfficiencyScl = Integer.valueOf(psqiData).intValue();
					}
					else if (name.equalsIgnoreCase("psqifallasleeppastmonthscl")) {
						psqiFallAsleepPastMonthScl = Integer.valueOf(psqiData).intValue();
					}
					else if (name.equalsIgnoreCase("psqinosleepin30minpastmonthscl")) {
						psqiNoSleepIn30minPastMonthScl = Integer.valueOf(psqiData).intValue();
					}
					else if (name.equalsIgnoreCase("psqisleeplatencyscl")) {
						psqiSleepLatencyScl = Integer.valueOf(psqiData).intValue();	
						sleepLatencySclIndex = i;
					}
					else if (name.equalsIgnoreCase("psqisleepmedpastmonthscl")) {
						psqiSleepMedPastMonthScl = Integer.valueOf(psqiData).intValue();
					}
					else if (name.equalsIgnoreCase("psqisleepqualitypastmonthscl")) {
						psqiSleepQualityPastMonthScl = Integer.valueOf(psqiData).intValue();
					}
					else if (name.equalsIgnoreCase("psqitotalscore")) {
							psqiTotalScore = Integer.valueOf(psqiData).intValue();
							totalScoreIndex = i;
					}
				}
				catch (NumberFormatException e) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(i),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(i));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
											psqiData);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(i), message));
					if ((name.equalsIgnoreCase("psqiactsleeppastmonthscl")) ||
					    (name.equalsIgnoreCase("psqitroublestayawakepastmoscl")) ||
					    (name.equalsIgnoreCase("psqiproblemethusiasmpastmoscl")) ||
					    (name.equalsIgnoreCase("psqiwakeupnightpastmonthscl")) ||
					    (name.equalsIgnoreCase("psqiwakeupbathroompastmonthscl")) ||
					    (name.equalsIgnoreCase("psqicannotbreathepastmonthscl"))||
					    (name.equalsIgnoreCase("psqicoughsnoreloudpastmonthscl")) ||
					    (name.equalsIgnoreCase("psqifeelcoldpastmonthscl")) ||
					    (name.equalsIgnoreCase("psqifeelhotpastmonthscl"))||
					    (name.equalsIgnoreCase("psqihadbaddreamspastmonthscl")) ||
					    (name.equalsIgnoreCase("psqipainpastmonthscl")) ||
					    (name.equalsIgnoreCase("psqisleepefficiencyscl")) ||
					    (name.equalsIgnoreCase("psqifallasleeppastmonthscl")) ||
					    (name.equalsIgnoreCase("psqinosleepin30minpastmonthscl"))|| 
					    (name.equalsIgnoreCase("psqisleepmedpastmonthscl")) ||
					    (name.equalsIgnoreCase("psqisleepqualitypastmonthscl"))) {
						numberScoresNotInteger++;
					}
					if (name.equalsIgnoreCase("psqiotherrsnpastmonthscloth")
							|| name.equalsIgnoreCase("psqiotherrsnpastmonthscl")) {
					    numberQ5jScoresNotInteger++;
					}
				}
			}
		} // for (i = 0; i < columnNameVector.size(); i++)
	    
        } // for (int group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++)
        } // for (int repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) 
		psqi_rgTable = table.getRepeatableGroupTable(psqi_repeatable_group_id);
		psqi_rgEntries =
				table.getAllReferences(subject_row_id, psqi_repeatable_group_id, null);
		if (Q5jNeeded) {
			numberScoresMissing = numberScoresMissing + numberQ5jScoresMissing;
			numberScoresNotInteger = numberScoresNotInteger +
					numberQ5jScoresNotInteger;
			for (i = 0; i < Q5jIndices.size(); i++) {
				missingIndices.add(Q5jIndices.get(i));
			}
		}
        if (numberScoresMissing >= 1) {
	    	for (i = 0; i < numberScoresMissing; i++) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, psqi_group_row_index),
								psqi_rgTable.getDataFilePositionMapping(missingIndices.get(i)),
								psqi_rgTable.getRepeatableGroupName(),
								psqi_rgTable.getColumnName(missingIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
				table.addOutput(new ValidationOutput(psqi_rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, psqi_group_row_index),
						psqi_rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
	    	}
	    	if (psqiTotalScore > -1) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, psqi_group_row_index),
								psqi_rgTable.getDataFilePositionMapping(totalScoreIndex),
								psqi_rgTable.getRepeatableGroupName(),
								psqi_rgTable.getColumnName(totalScoreIndex));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,
										String.valueOf(psqiTotalScore));
				table.addOutput(new ValidationOutput(psqi_rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, psqi_group_row_index),
						psqi_rgTable.getDataFilePositionMapping(totalScoreIndex), message));
	    	}
	    	return;
	    } // if (numberScoresMissing >= 1)
        
        if (numberScoresNotInteger >= 1) {
        	if (psqiTotalScore > -1) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, psqi_group_row_index),
								psqi_rgTable.getDataFilePositionMapping(totalScoreIndex),
								psqi_rgTable.getRepeatableGroupName(),
								psqi_rgTable.getColumnName(totalScoreIndex));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,
										String.valueOf(psqiTotalScore));
				table.addOutput(new ValidationOutput(psqi_rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, psqi_group_row_index),
						psqi_rgTable.getDataFilePositionMapping(totalScoreIndex), message));
	    	}
        	return;
        }
	    
	    int q89Sum = psqiTroubleStayAwakePastMoScl + psqiProblemEnthusiasmPastMoScl;
	    if (q89Sum == 0) {
	    	calculatedpsqiDayDysfunctionSleepScl = 0;	
	    }
	    else if ((q89Sum >= 1) && (q89Sum <= 2)) {
	    	calculatedpsqiDayDysfunctionSleepScl = 1;	
	    }
	    else if ((q89Sum >= 3) && (q89Sum <= 4)) {
	    	calculatedpsqiDayDysfunctionSleepScl = 2;	
	    }
	    else if ((q89Sum >= 5) && (q89Sum <= 6)) {
	    	calculatedpsqiDayDysfunctionSleepScl = 3;	
	    }
	    
	    if (psqiDayDysunctionSleepScl > -1) {
	    	if (psqiDayDysunctionSleepScl != calculatedpsqiDayDysfunctionSleepScl) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, psqi_group_row_index),
								psqi_rgTable.getDataFilePositionMapping(dayDysfunctionSleepSclIndex),
								psqi_rgTable.getRepeatableGroupName(),
								psqi_rgTable.getColumnName( dayDysfunctionSleepSclIndex));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_CALCULATION,
										String.valueOf(psqiDayDysunctionSleepScl), 
										String.valueOf(calculatedpsqiDayDysfunctionSleepScl));
				table.addOutput(new ValidationOutput(psqi_rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, psqi_group_row_index),
						psqi_rgTable.getDataFilePositionMapping(dayDysfunctionSleepSclIndex), message));	
	    	}
	    }
	    
	    if (psqiOtherRsnPastMonthSclOth == null) {
	    	psqiOtherRsnPastMonthScl = 0;
	    }
	    
	    int q5bjSum = psqiWakeupNightPastMonthScl +
	    		psqiWakeupBathroomPastMonthScl +
	    		psqiCannotBreathePastMonthScl +
	    		psqiCoughSnoreLoudPastMonthScl +
	    		psqiFeelColdPastMonthScl +
	    		psqiFeelHotPastMonthScl +
	    		psqiHadBadDreamsPastMonthScl +
	    		psqiPainPastMonthScl +
	    		psqiOtherRsnPastMonthScl;
	    
	    if (q5bjSum == 0) {
	    	calculatedpsqiSleepDistbScl = 0;	
	    }
	    else if ((q5bjSum >= 1) && (q5bjSum <= 9)) {
	    	calculatedpsqiSleepDistbScl = 1;	
	    }
	    else if ((q5bjSum >= 10) && (q5bjSum <= 18)) {
	    	calculatedpsqiSleepDistbScl = 2;	
	    }
	    else if (q5bjSum >= 19) {
	    	calculatedpsqiSleepDistbScl = 3;	
	    }
	    
	    if (psqiSleepDistbScl > -1) {
	    	if (psqiSleepDistbScl != calculatedpsqiSleepDistbScl) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, psqi_group_row_index),
								psqi_rgTable.getDataFilePositionMapping(sleepDistbSclIndex),
								psqi_rgTable.getRepeatableGroupName(),
								psqi_rgTable.getColumnName(sleepDistbSclIndex));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_CALCULATION,
										String.valueOf(psqiSleepDistbScl), 
										String.valueOf(calculatedpsqiSleepDistbScl));
				table.addOutput(new ValidationOutput(psqi_rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, psqi_group_row_index),
						psqi_rgTable.getDataFilePositionMapping(sleepDistbSclIndex), message));	
	    	}
	    }
	    
	    
	    int q25a = psqiFallAsleepPastMonthScl + psqiNoSleepIn30minPastMonthScl;
	    if (q25a == 0) {
	    	calculatedpsqiSleepLatencyScl = 0;	
	    }
	    else if ((q25a >= 1) && (q25a <= 2)) {
	    	calculatedpsqiSleepLatencyScl = 1;		
	    }
	    else if ((q25a >= 3) && (q25a <= 4)) {
	    	calculatedpsqiSleepLatencyScl = 2;		
	    }
	    else if ((q25a >= 5) && (q25a <= 6)) {
	    	calculatedpsqiSleepLatencyScl = 3;		
	    }
	    
	    if (psqiSleepLatencyScl > -1) {
	    	if (psqiSleepLatencyScl != calculatedpsqiSleepLatencyScl) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, psqi_group_row_index),
								psqi_rgTable.getDataFilePositionMapping(sleepLatencySclIndex),
								psqi_rgTable.getRepeatableGroupName(),
								psqi_rgTable.getColumnName(sleepLatencySclIndex));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_CALCULATION,
										String.valueOf(psqiSleepLatencyScl), 
										String.valueOf(calculatedpsqiSleepLatencyScl));
				table.addOutput(new ValidationOutput(psqi_rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, psqi_group_row_index),
						psqi_rgTable.getDataFilePositionMapping(sleepLatencySclIndex), message));	
	    	}
	    }
	    
	    calculatedTotalScore = psqiActSleepPastMonthScl + 
	    		calculatedpsqiDayDysfunctionSleepScl + 
	    		calculatedpsqiSleepDistbScl +
	    		psqiSleepEfficiencyScl +
	    		calculatedpsqiSleepLatencyScl +
	    		psqiSleepMedPastMonthScl + psqiSleepQualityPastMonthScl;
	    
	    if (psqiTotalScore > -1) {
	    	if (psqiTotalScore != calculatedTotalScore) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, psqi_group_row_index),
								psqi_rgTable.getDataFilePositionMapping(totalScoreIndex),
								psqi_rgTable.getRepeatableGroupName(),
								psqi_rgTable.getColumnName(totalScoreIndex));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
										String.valueOf(psqiTotalScore), String.valueOf(calculatedTotalScore));
				table.addOutput(new ValidationOutput(psqi_rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, psqi_group_row_index),
						psqi_rgTable.getDataFilePositionMapping(totalScoreIndex), message));
	    	}
	    }
	}

	
	private int compareDates(String actualDate, String referenceDate) {
		// = -1 if actualDate < referenceDate
		// = 0 if actualDate = referenceDate
		// = 1 if actualDate > referenceDate
		// visitdate is of form 2016-05-23T00:00:00Z
		int actualYear;
		int referenceYear;
		int actualMonth;
		int referenceMonth;
		int actualDay;
		int referenceDay;
		actualYear = Integer.valueOf(actualDate.substring(0,4)).intValue();
		referenceYear = Integer.valueOf(referenceDate.substring(0,4)).intValue();
		if (actualYear < referenceYear) {
			return -1;
		}
		else if (actualYear > referenceYear) {
			return 1;
		}
		else {
			actualMonth = Integer.valueOf(actualDate.substring(5,7)).intValue();
			referenceMonth = Integer.valueOf(referenceDate.substring(5,7)).intValue();
			if (actualMonth < referenceMonth) {
				return -1;
			}
			else if (actualMonth > referenceMonth) {
				return 1;
			}
			else {
				actualDay = Integer.valueOf(actualDate.substring(8,10)).intValue();
				referenceDay = Integer.valueOf(referenceDate.substring(8,10)).intValue();
				if (actualDay < referenceDay) {
					return -1;
				}
				else if (actualDay > referenceDay) {
					return 1;
				}
				else {
					return 0;
				}
			}
		}
	}

	private void validatePCLC_Standard(StructuralFormStructure structure,
			Vector<Vector<Vector<String>>> columnNameVector3, 
			Vector<Vector<Vector<String>>> dataVector3, int subject_row_id,
			DataStructureTable table) throws Exception {
		// 2 possible sets of columnNameVectors and accompanying dataVectors:
		
		// group_column_index = 0 group_columnName = guid
		// group_column_index = 1 group_columnName = subjectidnum
		// group_column_index = 2 group_columnName = ageyrs
		// group_column_index = 3 group_columnName = visitdate
		// group_column_index = 4 group_columnName = sitename
		// group_column_index = 5 group_columnName = dayssincebaseline
		// group_column_index = 6 group_columnName = casecontrlind
		// group_column_index = 7 group_columnName = generalnotestxt
			
		// group_column_index = 0 group_columnName = pclsmemoriesind
		// group_column_index = 1 group_columnName = pclsdreamsind
		// group_column_index = 2 group_columnName = pclshappenagainind
		// group_column_index = 3 group_columnName = pclsveryupsetind
		// group_column_index = 4 group_columnName = pclsphysicalreactionsind
	    // group_column_index = 5 group_columnName = pclsavoidhavingfeelingind
	    // group_column_index = 6 group_columnName = pclsavoidsituationind
	    // group_column_index = 7 group_columnName = pclsrememberimportantind
		// group_column_index = 8 group_columnName = pclslossinterestind
		// group_column_index = 9 group_columnName = pclsfeeldistantind
		// group_column_index = 10 group_columnName = pclsemotionallynumbind
		// group_column_index = 11 group_columnName = pclsshortfutureind
		// group_column_index = 12 group_columnName = pclsfallstayasleepind
		// group_column_index = 13 group_columnName = pclsangryoutburstind
		// group_column_index = 14 group_columnName = pclsdifficultyconcentratingind
		// group_column_index = 15 group_columnName = pclssuperalertind
		// group_column_index = 16 group_columnName = pclsjumpyind
		// group_column_index = 17 group_columnName = pclctotlscore
				
		int i;
        String message;
		
		int ageIndex = -1;
		boolean haveAgeIndex = false;
        Vector<Vector<String>> columnNameVector2;
        Vector<Vector<String>> dataVector2;
        Vector<String> columnNameVector;
        Vector<String> dataVector;
        int repeatable_group_id;
        RepeatableGroupTable rgTable;
        ArrayList<Integer> rgEntries;
        int group_row_index;
        String guid = null;
        
        guidloop:
            for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
       		 
        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
        		dataVector2 = dataVector3.get(repeatable_group_id);
        		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
        		rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
    			for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
    		        columnNameVector = columnNameVector2.get(group_row_index);
    		        dataVector = dataVector2.get(group_row_index);
    		        for (i = 0; i  < columnNameVector.size(); i++) {
    					if (columnNameVector.get(i).equalsIgnoreCase("guid")) {
    					    guid = dataVector.get(i);
    					    break guidloop;
    					}
    		        }
    			}
            }
        
        loop1:
        	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
        		dataVector2 = dataVector3.get(repeatable_group_id);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries =
						table.getAllReferences(subject_row_id, repeatable_group_id, null);

        for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
			columnNameVector = columnNameVector2.get(group_row_index);
			dataVector = dataVector2.get(group_row_index);
	    
	    for (i = 0; i < columnNameVector.size() && (ageIndex == -1); i++) {
			if (columnNameVector.get(i).equalsIgnoreCase("ageyrs")) {
				ageIndex = i;	
			}
		}
	    
	    if ((!haveAgeIndex) && (ageIndex >= 0)) {
	    	haveAgeIndex = true;
	    	String ageData = dataVector.get(ageIndex);
			if ((ageData == null) || (ageData.isEmpty()) || (ageData.trim() == null) ||
					(ageData.trim().isEmpty())) {
				message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(ageIndex),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(ageIndex));

				message =
						message
								+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
				table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(ageIndex), message));
				continue loop1;
			}	
			else {
				double age = Double.valueOf(ageData).doubleValue();
				if (age < 18) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(ageIndex),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(ageIndex));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_INCORRECT_AGE,
									  ageData, "18 or more years");
					table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(ageIndex), message));
				}
			}
			continue loop1;
	    } // if (ageIndex >= 0)
	    
	    String pclData;
	    int numberScoresMissing = 0;
	    Vector<Integer>missingIndices = new Vector<Integer>();
	    int numberScoresNotInteger = 0;
	    Vector<Integer>notIntegerIndices = new Vector<Integer>();
	    Vector<String>notIntegerData = new Vector<String>();
	    int pclTotalScore = -1;
	    int calculatedTotalScore = 0;
	    int totalScoreIndex = -1;
	    int pclScores[] = new int[columnNameVector.size()-1];
	    for (i = 0; i < columnNameVector.size(); i++) {
	    	pclData = dataVector.get(i);
			if ((pclData == null) || (pclData.isEmpty()) || (pclData.trim() == null) ||
					(pclData.trim().isEmpty())) {
				if (!columnNameVector.get(i).equalsIgnoreCase("pclctotlscore")) {
				    numberScoresMissing++;
				    missingIndices.add(i);
				}
				else if (columnNameVector.get(i).equalsIgnoreCase("pclctotlscore")){
					// Total score value was missing
					totalScoreIndex = i;
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(i),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(i));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(i), message));	
				}
			}
			else {
				if (columnNameVector.get(i).equalsIgnoreCase("pclctotlscore")) {
					try {
					    pclTotalScore = Integer.valueOf(pclData).intValue();
					}
					catch (NumberFormatException e) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(i),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(i));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,pclData);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(i), message));	
					}
					totalScoreIndex = i;
				}
				
				else {
					try {
					    pclScores[i] = Integer.valueOf(pclData).intValue();
					}
					catch (NumberFormatException e) {
						numberScoresNotInteger++;
					    notIntegerIndices.add(i);
					    notIntegerData.add(pclData);
					}
					calculatedTotalScore += pclScores[i];
				}
			}
		} // for (i = 0; i < columnNameVector.size(); i++)
	    if (numberScoresMissing >= 1) {
	    	for (i = 0; i < numberScoresMissing; i++) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(missingIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(missingIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
	    	}
	    } // if (numberScoresMissing >= 1)
	    
	    if (numberScoresNotInteger >= 1) {
	    	for (i = 0; i < numberScoresNotInteger; i++) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(notIntegerIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
										notIntegerData.get(i));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)), message));
	    	}
	    } // if (numberScoresNotInteger >= 1)
	    
	    if ((numberScoresMissing >= 1) || (numberScoresNotInteger >= 1)) {
	    	if (pclTotalScore > -1) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(totalScoreIndex),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(totalScoreIndex));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,
										String.valueOf(pclTotalScore));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(totalScoreIndex), message));	
	    	}
	    	return;
	    }
	    
	    if (pclTotalScore > -1) {
	    	if (pclTotalScore != calculatedTotalScore) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(totalScoreIndex),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(totalScoreIndex));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
										String.valueOf(pclTotalScore), String.valueOf(calculatedTotalScore));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(totalScoreIndex), message));
	    	}
	    }
        } // for (int group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++)
        } // for (int repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) 
	}
	
	private void validatePHQ9(StructuralFormStructure structure,
			Vector<Vector<Vector<String>>> columnNameVector3, 
			Vector<Vector<Vector<String>>>dataVector3, int subject_row_id,
			DataStructureTable table) throws Exception {
		    // table.getColumnCount = 2
		    // repeatable_group_id = 0
		    // rgTable.getSize() = 8
			// rgEntries.size() = 1
			// group_row_index = 0
			// columnNameVector.size() = 8
			// group_column_index = 0 group_column_name = guid data = TBIWF313YTE
			// group_column_index = 1 group_column_name = subjectidnum data = 125
			// group_column_index = 2 group_column_name = ageyrs data = null
			// group_column_index = 3 group_column_name = visitdate data = 2017-01-30T00:00:00Z
			// group_column_index = 4 group_column_name = sitename data = University of Arizona
			// group_column_index = 5 group_column_name = dayssincebaseline data = 47
			// group_column_index = 6 group_column_name = casecontrlind data = Case
			// group_column_index = 7 group_column_name = generalnotestxt data = age not submitted
			// repeatable_group_id = 1
			// rgTable.getSize() = 11
			// rgEntries.size() = 1
			// group_row_index = 0
			// columnNameVector.size() = 11
			// group_column_index = 0 group_column_name = phq9intrstpleasractscore data = 2
			// group_column_index = 1 group_column_name = phq9dwndeprssnhopelssscore data = 0
			// group_column_index = 2 group_column_name = phq9sleepimpairscore data = 3
			// group_column_index = 3 group_column_name = phq9tirdlittleenrgyscore data = 3
			// group_column_index = 4 group_column_name = phq9abnrmldietscore data = 2
			// group_column_index = 5 group_column_name = phq9flngfailrscore data = 0
			// group_column_index = 6 group_column_name = phq9concntrtnimprmntscore data = 2
			// group_column_index = 7 group_column_name = phq9movmntspchimprmntscore data = 2
			// group_column_index = 8 group_column_name = phq9bttrddthghtscore data = 0
			// group_column_index = 9 group_column_name = phq9totalscore data = 14
			// group_column_index = 10 group_column_name = phq9diffcltyperfactscre data = Very difficult
		Vector<Vector<String>> columnNameVector2;
        Vector<Vector<String>> dataVector2;
        Vector<String> columnNameVector;
        Vector<String> dataVector;
        int i;
        String message;
        Vector<String> bufColumnNameVector = new Vector<String>();
        Vector<String> bufDataVector = new Vector<String>();
        Vector<Integer> bufRepeatable_group_id = new Vector<Integer>();
        Vector<Integer> bufGroup_row_index = new Vector<Integer>();
        Vector<Integer> bufIndex = new Vector<Integer>();
        String guid = null;
        int group_row_index = -1;
        int repeatable_group_id;
        RepeatableGroupTable rgTable = null;
        ArrayList<Integer> rgEntries = null;
        int index = -1;
        String data;
        
		//System.out.println("table.getColumnCount = " + table.getColumnCount());
        	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
        		 
        		//System.out.println("repeatable_group_id = " + repeatable_group_id);
        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
        		dataVector2 = dataVector3.get(repeatable_group_id);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				//System.out.println("rgTable.getSize() = " + rgTable.getSize());
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				// System.out.println("rgEntries.size() = " + rgEntries.size());
				for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
        	        // System.out.println("group_row_index = " + group_row_index);
			        columnNameVector = columnNameVector2.get(group_row_index);
			        dataVector = dataVector2.get(group_row_index);
			        // System.out.println("columnNameVector.size() = " + columnNameVector.size());
			 // for (i = 0; i < columnNameVector.size(); i++) {
			    // System.out.println("group_column_index = " + i +
			    	// " group_column_name = " + columnNameVector.get(i) +
			    	// " data = " + dataVector.get(i)) ;	
			 // }
			for (i = 0; i < columnNameVector.size(); i++) {
				bufColumnNameVector.add(columnNameVector.get(i));
				bufDataVector.add(dataVector.get(i));
				bufRepeatable_group_id.add(repeatable_group_id);
				bufGroup_row_index.add(group_row_index);
				bufIndex.add(i);
			}
			} // for (int group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++)
            } // for (int repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++)
        for (i = 0; i < bufColumnNameVector.size(); i++) {
        	if (bufColumnNameVector.get(i).equalsIgnoreCase("guid")) {
				guid = bufDataVector.get(i);
				break;
			}	
        } // for (i = 0; i < bufColumnNameVector.size(); i++)
        
	    
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("ageyrs")) {
				String ageData = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((ageData == null) || (ageData.isEmpty()) || (ageData.trim() == null) ||
						(ageData.trim().isEmpty())) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
				}	
				else {
					double age = Double.valueOf(ageData).doubleValue();
					if (age < 18) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.WARNING_INCORRECT_AGE,
										  ageData, "18 or more years");
						table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
					}
				}
				break;
			  } // if (bufColumnNameVector.get(i).equalsIgnoreCase("ageyrs"))
			} // for (i = 0; i < bufColumnNameVector.size(); i++)
			
			int numberScoresMissing = 0;
		    Vector<Integer>missingIndices = new Vector<Integer>();
		    int missing_repeatable_group_id = -1;
		    int missing_group_row_index = -1;
		    int numberScoresNotInteger = 0;
		    Vector<Integer>notIntegerIndices = new Vector<Integer>();
		    int notInteger_repeatable_group_id = -1;
		    int notInteger_group_row_index = -1;
		    Vector<String>notIntegerData = new Vector<String>();
            int calculatedSum = 0;
		    
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
				if ((bufColumnNameVector.get(i).equalsIgnoreCase("phq9intrstpleasractscore")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("phq9dwndeprssnhopelssscore")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("phq9sleepimpairscore")) ||	
				   (bufColumnNameVector.get(i).equalsIgnoreCase("phq9tirdlittleenrgyscore")) ||	
				   (bufColumnNameVector.get(i).equalsIgnoreCase("phq9abnrmldietscore")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("phq9flngfailrscore")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("phq9concntrtnimprmntscore")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("phq9movmntspchimprmntscore")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("phq9bttrddthghtscore"))) {
					data = bufDataVector.get(i);
					if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
					    numberScoresMissing++;
					    missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
					}
					else {
						try {
					        calculatedSum += Integer.valueOf(data).intValue();
						}
						catch (NumberFormatException e) {
							numberScoresNotInteger++;
						    notIntegerIndices.add(bufIndex.get(i));
						    notInteger_repeatable_group_id = bufRepeatable_group_id.get(i);
							notInteger_group_row_index = bufGroup_row_index.get(i);
							notIntegerData.add(data);
						}
					}
				}
			}
		    
		    if (numberScoresMissing >= 1) {
		    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						missing_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresMissing; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, missing_group_row_index),
									rgTable.getDataFilePositionMapping(missingIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(missingIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index),
							rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
		    	}
		    } // if (numberScoresMissing >= 1)
		    
		    if (numberScoresNotInteger >= 1) {
		    	rgTable = table.getRepeatableGroupTable(notInteger_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						notInteger_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresNotInteger; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, notInteger_group_row_index),
									rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(notIntegerIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
											notIntegerData.get(i));
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, notInteger_group_row_index),
							rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)), message));
		    	}
		    } // if (numberScoresNotInteger >= 1)
		    
		    int actualSum = -1;
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
				if (bufColumnNameVector.get(i).equalsIgnoreCase("phq9totalscore")) {
					data = bufDataVector.get(i);
					repeatable_group_id = bufRepeatable_group_id.get(i);
					group_row_index = bufGroup_row_index.get(i);
					index = bufIndex.get(i);
					rgTable = table.getRepeatableGroupTable(repeatable_group_id);
					rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
					if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
						if ((numberScoresMissing == 0) && (numberScoresNotInteger == 0)) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
						}
					}
					else if ((numberScoresMissing > 0) || (numberScoresNotInteger > 0)) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));	
					}
					else {
						try {
					        actualSum = Integer.valueOf(data).intValue();
						}
						catch (NumberFormatException e) {
							message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(index),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(index));

							message =
									message
											+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
													data);
							table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index), message));   	
						}
					}
					break;
				}
		    }
		    
		    if ((numberScoresMissing >= 1) || (numberScoresNotInteger >= 1)) {
		    	return;
		    }
		    
		    if (actualSum > -1) {
		    	if (actualSum != calculatedSum) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
											String.valueOf(actualSum), String.valueOf(calculatedSum));
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
		    	}
		    }
		
	}
	
	private void validateSCAT3(StructuralFormStructure structure,
			Vector<Vector<Vector<String>>> columnNameVector3, 
			Vector<Vector<Vector<String>>>dataVector3, int subject_row_id,
			DataStructureTable table) throws Exception {
		// Structure Name:	SCAT3
		// table.getColumnCount = 19
		// repeatable_group_id = 0
		// rgTable.getRepeatableGroupName() = Glasgow Coma Scale Adult
		// rgTable.getSize() = 4
		// rgEntries.size() = 0
		// repeatable_group_id = 1
		// rgTable.getRepeatableGroupName() = Glasgow Coma Scale Pediatric
		// rgTable.getSize() = 4
		// rgEntries.size() = 0
		// repeatable_group_id = 2
		// rgTable.getRepeatableGroupName() = Maddocks Score
		// rgTable.getSize() = 6
		// rgEntries.size() = 0
		// repeatable_group_id = 3
		// rgTable.getRepeatableGroupName() = SAC Cognitive Assessment - Concentration
		// rgTable.getSize() = 7
		// rgEntries.size() = 0
		// repeatable_group_id = 4
		// rgTable.getRepeatableGroupName() = Indications for Emergency Management
		// rgTable.getSize() = 1
		// rgEntries.size() = 0
		// repeatable_group_id = 5
		// rgTable.getRepeatableGroupName() = Main
		// rgTable.getSize() = 9
		// rgEntries.size() = 1
		// group_row_index = 0
		// columnNameVector.size() = 9
		// group_column_index = 0 group_column_name = guid data = TBI_INVTJ452XKB
		// group_column_index = 1 group_column_name = subjectidnum data = 7010071
		// group_column_index = 2 group_column_name = ageyrs data = null
		// group_column_index = 3 group_column_name = vitstatus data = null
		// group_column_index = 4 group_column_name = visitdate data = 2016-08-03T00:00:00Z
		// group_column_index = 5 group_column_name = sitename data = null
		// group_column_index = 6 group_column_name = dayssincebaseline data = 0
		// group_column_index = 7 group_column_name = casecontrlind data = null
		// group_column_index = 8 group_column_name = generalnotestxt data = Age not submitted
		// repeatable_group_id = 6
		// rgTable.getRepeatableGroupName() = Symptom Evaluation
		// rgTable.getSize() = 26
		// rgEntries.size() = 1
		// group_row_index = 0
		// columnNameVector.size() = 26
		// group_column_index = 0 group_column_name = scat3headache data = 0
		// group_column_index = 1 group_column_name = scat3pressureinhead data = 0
		// group_column_index = 2 group_column_name = scat3neckpain data = 0
		// group_column_index = 3 group_column_name = scat3nauseavomiting data = 0
		// group_column_index = 4 group_column_name = scat3dizziness data = 0
		// group_column_index = 5 group_column_name = scat3blurryvision data = 0
		// group_column_index = 6 group_column_name = scat3balanceproblem data = 0
		// group_column_index = 7 group_column_name = scat3senssivitylight data = 0
		// group_column_index = 8 group_column_name = scat3sensitivitynoise data = 0
		// group_column_index = 9 group_column_name = scat3feelslowdown data = 0
		// group_column_index = 10 group_column_name = scat3feelfog data = 0
		// group_column_index = 11 group_column_name = scat3dontfeelright data = 0
		// group_column_index = 12 group_column_name = scat3difficultyconcent data = 0
		// group_column_index = 13 group_column_name = scat3difficultyremembering data = 0
		// group_column_index = 14 group_column_name = scat3fatglowenergy data = 0
		// group_column_index = 15 group_column_name = scat3confusion data = 0
		// group_column_index = 16 group_column_name = scat3drowsiness data = 0
		// group_column_index = 17 group_column_name = scat3troublfallasleep data = 0
		// group_column_index = 18 group_column_name = scat3moreemotional data = 0
		// group_column_index = 19 group_column_name = scat3irritable data = 0
		// group_column_index = 20 group_column_name = scat3sadness data = 0
		// group_column_index = 21 group_column_name = scat3nervousanxious data = 0
		// group_column_index = 22 group_column_name = scat3sympphysactvty data = null
		// group_column_index = 23 group_column_name = scat3sympmentactvty data = null
		// group_column_index = 24 group_column_name = scat3datasourcetyp data = null
		// group_column_index = 25 group_column_name = scat3differathleteactscore data = null
		// repeatable_group_id = 7
		// rgTable.getRepeatableGroupName() = Potential signs of concussion
		// rgTable.getSize() = 9
		// rgEntries.size() = 0
		// repeatable_group_id = 8
		// rgTable.getRepeatableGroupName() = Background
		// rgTable.getSize() = 6
		// rgEntries.size() = 0
		// repeatable_group_id = 9
		// rgTable.getRepeatableGroupName() = Mechanism of Injury
		// rgTable.getSize() = 1
		// rgEntries.size() = 0
		// repeatable_group_id = 10
		// rgTable.getRepeatableGroupName() = SAC Cognitive Assessment- Orientation
		// rgTable.getSize() = 5
		// rgEntries.size() = 0
		// repeatable_group_id = 11
		// rgTable.getRepeatableGroupName() = Balance examination (BESS)
		// rgTable.getSize() = 13
		// rgEntries.size() = 0
		// repeatable_group_id = 12
		// rgTable.getRepeatableGroupName() = Background Most Recent Concussion
		// rgTable.getSize() = 2
		// rgEntries.size() = 0
		// repeatable_group_id = 13
		// rgTable.getRepeatableGroupName() = Coordination Examination Upper Limb
		// rgTable.getSize() = 2
		// rgEntries.size() = 0
		// repeatable_group_id = 14
		// rgTable.getRepeatableGroupName() = Neck Examination
		// rgTable.getSize() = 4
		// rgEntries.size() = 0
		// repeatable_group_id = 15
		// rgTable.getRepeatableGroupName() = SAC Cognitive Assessment - Immediate Memory
		// rgTable.getSize() = 7
		// rgEntries.size() = 0
		// repeatable_group_id = 16
		// rgTable.getRepeatableGroupName() = Form Administration
		// rgTable.getSize() = 4
		// rgEntries.size() = 1
		// group_row_index = 0
		// columnNameVector.size() = 4
		// group_column_index = 0 group_column_name = contexttype data = Before injury
		// group_column_index = 1 group_column_name = contexttypeoth data = Baseline
		// group_column_index = 2 group_column_name = datasource data = null
		// group_column_index = 3 group_column_name = datasourceoth data = null
		// repeatable_group_id = 17
		// rgTable.getRepeatableGroupName() = Background Medical History
		// rgTable.getSize() = 7
		// rgEntries.size() = 0
		// repeatable_group_id = 18
		// rgTable.getRepeatableGroupName() = Scoring Summary
		// rgTable.getSize() = 12
		// rgEntries.size() = 1
		// group_row_index = 0
		// columnNameVector.size() = 12
		// group_column_index = 0 group_column_name = scat3totalsymptoms data = 0
		// group_column_index = 1 group_column_name = scat3totsympscore data = 0
		// group_column_index = 2 group_column_name = sacorientationsubsetscore data = null
		// group_column_index = 3 group_column_name = sacimmdmemorysubsetscore data = null
		// group_column_index = 4 group_column_name = sacimmdmemorytrialscore data = null
		// group_column_index = 5 group_column_name = sacconcentationsubsetscore data = null
		// group_column_index = 6 group_column_name = sacdelayedrecallsubsetscore data = null
		// group_column_index = 7 group_column_name = sactotalscore data = null
		// group_column_index = 8 group_column_name = besstotalerrorct data = null
		// group_column_index = 9 group_column_name = fulltandemstanddur data = null
		// group_column_index = 10 group_column_name = scat3upperlimbcoordinatscore data = null
		// group_column_index = 11 group_column_name = generalnotestxt data = null
		Vector<Vector<String>> columnNameVector2;
        Vector<Vector<String>> dataVector2;
        Vector<String> columnNameVector;
        Vector<String> dataVector;
        int i, j;
        String message;
        Vector<String> bufColumnNameVector = new Vector<String>();
        Vector<String> bufDataVector = new Vector<String>();
        Vector<Integer> bufRepeatable_group_id = new Vector<Integer>();
        Vector<Integer> bufGroup_row_index = new Vector<Integer>();
        Vector<Integer> bufIndex = new Vector<Integer>();
        Vector<String> bufRepeatableGroupName = new Vector<String>();
        String guid = null;
        int group_row_index = -1;
        int repeatable_group_id;
        RepeatableGroupTable rgTable = null;
        ArrayList<Integer> rgEntries = null;
        int index = -1;
        String data;
        
		 //System.out.println("table.getColumnCount = " + table.getColumnCount());
        	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
        		 
        		//System.out.println("repeatable_group_id = " + repeatable_group_id);
        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
        		dataVector2 = dataVector3.get(repeatable_group_id);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				//System.out.println("rgTable.getRepeatableGroupName() = " + rgTable.getRepeatableGroupName());
				//System.out.println("rgTable.getSize() = " + rgTable.getSize());
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				//System.out.println("rgEntries.size() = " + rgEntries.size());
				for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
        	 //System.out.println("group_row_index = " + group_row_index);
			columnNameVector = columnNameVector2.get(group_row_index);
			dataVector = dataVector2.get(group_row_index);
			 //System.out.println("columnNameVector.size() = " + columnNameVector.size());
			 //for (i = 0; i < columnNameVector.size(); i++) {
			    //System.out.println("group_column_index = " + i +
			    	//" group_column_name = " + columnNameVector.get(i) +
			    	//" data = " + dataVector.get(i)) ;	
			 //}
			for (i = 0; i < columnNameVector.size(); i++) {
				bufColumnNameVector.add(columnNameVector.get(i));
				bufDataVector.add(dataVector.get(i));
				bufRepeatable_group_id.add(repeatable_group_id);
				bufGroup_row_index.add(group_row_index);
				bufIndex.add(i);
				bufRepeatableGroupName.add(rgTable.getRepeatableGroupName());
			}
			} // for (int group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++)
            } // for (int repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++)
        for (i = 0; i < bufColumnNameVector.size(); i++) {
        	if (bufColumnNameVector.get(i).equalsIgnoreCase("guid")) {
				guid = bufDataVector.get(i);
				break;
			}	
        } // for (i = 0; i < bufColumnNameVector.size(); i++)
        
	    
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("ageyrs")) {
				String ageData = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((ageData == null) || (ageData.isEmpty()) || (ageData.trim() == null) ||
						(ageData.trim().isEmpty())) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
				}	
				else {
					double age = Double.valueOf(ageData).doubleValue();
					if (age < 13) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.WARNING_INCORRECT_AGE,
										  ageData, "13 or more years");
						table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
					}
				}
				break;
			  } // if (bufColumnNameVector.get(i).equalsIgnoreCase("ageyrs"))
			} // for (i = 0; i < bufColumnNameVector.size(); i++)
	    
	    int numberScoresMissing = 0;
	    Vector<Integer>missingIndices = new Vector<Integer>();
	    int missing_repeatable_group_id = -1;
	    int missing_group_row_index = -1;
	    boolean haveConcussionScore = false;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
	    	if (bufColumnNameVector.get(i).equalsIgnoreCase("LOCInd") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("BalanceIssueInd") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("SCAT3DisorientConfInd") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("PstTraumtcAmnsInd") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("PstTraumAmnsDurRang") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("AmnsType") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("SCAT3BlankVacantLookInd") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("SCAT3FacialInjuryInd")) {
	    		haveConcussionScore = true;
	    		data = bufDataVector.get(i);
		    	if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
		    		numberScoresMissing++;
		    		missingIndices.add(bufIndex.get(i));
				    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
					missing_group_row_index = bufGroup_row_index.get(i);
		    	}
	        }
	    }
	    
	    if (numberScoresMissing >= 1) {
	    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
			rgEntries = table.getAllReferences(subject_row_id, 
					missing_repeatable_group_id, null);
	    	for (i = 0; i < numberScoresMissing; i++) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, missing_group_row_index),
								rgTable.getDataFilePositionMapping(missingIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(missingIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index),
						rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
	    	}
	    } // if (numberScoresMissing >= 1)
	    
	    if (!haveConcussionScore) {
	    	
	    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Potential signs of concussion")) {
	    			for (i = 1; i <= 8; i++) {
		    			message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, 0),
										rgTable.getDataFilePositionMapping(i),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(i));
	
						message =
								message
										+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(i), message));
	    			} // for (i = 1; i <= 8; i++)
	    			break;
	    		}
	    	}
	    }
	    
	    numberScoresMissing = 0;
	    missingIndices.clear();
	    missing_repeatable_group_id = -1;
	    missing_group_row_index = -1;
	    boolean haveGlasgowComaScore = false;
	    int numberScoresNotInteger = 0;
	    Vector<Integer>notIntegerIndices = new Vector<Integer>();
	    int notInteger_repeatable_group_id = -1;
	    int notInteger_group_row_index = -1;
	    Vector<String>notIntegerData = new Vector<String>();
	    int calculatedGCSSum = 0;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
	    	if (bufColumnNameVector.get(i).equalsIgnoreCase("GCSEyeRespnsScale") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("GCSMotorRespnsScale") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("GCSVerbalRspnsScale")) {
	    		haveGlasgowComaScore = true;
	    		data = bufDataVector.get(i);
		    	if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
		    		numberScoresMissing++;
		    		missingIndices.add(bufIndex.get(i));
				    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
					missing_group_row_index = bufGroup_row_index.get(i);
		    	}
		    	else {
					try {
				        calculatedGCSSum += Integer.valueOf(data).intValue();
					}
					catch (NumberFormatException e) {
						numberScoresNotInteger++;
					    notIntegerIndices.add(bufIndex.get(i));
					    notInteger_repeatable_group_id = bufRepeatable_group_id.get(i);
						notInteger_group_row_index = bufGroup_row_index.get(i);
						notIntegerData.add(data);
					}
				}
	        }
	    }
	    
	    if (numberScoresMissing >= 1) {
	    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
			rgEntries = table.getAllReferences(subject_row_id, 
					missing_repeatable_group_id, null);
	    	for (i = 0; i < numberScoresMissing; i++) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, missing_group_row_index),
								rgTable.getDataFilePositionMapping(missingIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(missingIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index),
						rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
	    	}
	    } // if (numberScoresMissing >= 1)
	    
	    if (numberScoresNotInteger >= 1) {
	    	rgTable = table.getRepeatableGroupTable(notInteger_repeatable_group_id);
			rgEntries = table.getAllReferences(subject_row_id, 
					notInteger_repeatable_group_id, null);
	    	for (i = 0; i < numberScoresNotInteger; i++) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, notInteger_group_row_index),
								rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(notIntegerIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
										notIntegerData.get(i));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, notInteger_group_row_index),
						rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)), message));
	    	}
	    } // if (numberScoresNotInteger >= 1)
	    
	    if (!haveGlasgowComaScore) {
	    	
	    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Glasgow Coma Scale Adult")) {
	    			for (i = 0; i <= 2; i++) {
		    			message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, 0),
										rgTable.getDataFilePositionMapping(i),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(i));
	
						message =
								message
										+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(i), message));
	    			} // for (i = 0; i <= 2; i++)
	    			break;
	    		}
	    	}
	    }
	    
	    int actualGCSSum = -1;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("GCSTotalScore")) {
				data = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					if ((numberScoresMissing == 0) && (numberScoresNotInteger == 0)) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));
	
						message =
								message
										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message)); 
				    }
				}
			    else if ((numberScoresMissing >= 1) || (numberScoresNotInteger >= 1)) {
			    	message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,
											data);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
			    }
				else {
					try {
				        actualGCSSum = Integer.valueOf(data).intValue();
					}
					catch (NumberFormatException e) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
												data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index),message));   	
					}
				}
				break;
			}
	    }
	    
	    if (actualGCSSum > -1) {
	    	if (actualGCSSum != calculatedGCSSum) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(index));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
										String.valueOf(actualGCSSum), String.valueOf(calculatedGCSSum));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(index), message));
	    	}
	    }
	    
	    numberScoresMissing = 0;
	    missingIndices.clear();
	    missing_repeatable_group_id = -1;
	    missing_group_row_index = -1;
	    boolean haveMaddocksScore = false;
	    numberScoresNotInteger = 0;
	    notIntegerIndices.clear();
	    notInteger_repeatable_group_id = -1;
	    notInteger_group_row_index = -1;
	    notIntegerData.clear();
	    int calculatedMaddocksSum = 0;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
	    	if (bufColumnNameVector.get(i).equalsIgnoreCase("MaddocksScoreCorrVenueInd") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("MaddocksScoreCorrQuarterInd") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("MaddocksScoreCorrTeamScoreInd") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("MaddocksScoreCorrTeamPlayInd") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("MaddocksScoreCorrTeamWonInd")) {
	    		haveMaddocksScore = true;
	    		data = bufDataVector.get(i);
		    	if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
		    		numberScoresMissing++;
		    		missingIndices.add(bufIndex.get(i));
				    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
					missing_group_row_index = bufGroup_row_index.get(i);
		    	}
		    	else {
					try {
				        calculatedMaddocksSum += Integer.valueOf(data).intValue();
					}
					catch (NumberFormatException e) {
						numberScoresNotInteger++;
					    notIntegerIndices.add(bufIndex.get(i));
					    notInteger_repeatable_group_id = bufRepeatable_group_id.get(i);
						notInteger_group_row_index = bufGroup_row_index.get(i);
						notIntegerData.add(data);
					}
				}
	        }
	    }
	    
	    if (numberScoresMissing >= 1) {
	    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
			rgEntries = table.getAllReferences(subject_row_id, 
					missing_repeatable_group_id, null);
	    	for (i = 0; i < numberScoresMissing; i++) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, missing_group_row_index),
								rgTable.getDataFilePositionMapping(missingIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(missingIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index),
						rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
	    	}
	    } // if (numberScoresMissing >= 1)
	    
	    if (numberScoresNotInteger >= 1) {
	    	rgTable = table.getRepeatableGroupTable(notInteger_repeatable_group_id);
			rgEntries = table.getAllReferences(subject_row_id, 
					notInteger_repeatable_group_id, null);
	    	for (i = 0; i < numberScoresNotInteger; i++) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, notInteger_group_row_index),
								rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(notIntegerIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
										notIntegerData.get(i));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, notInteger_group_row_index),
						rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)), message));
	    	}
	    } // if (numberScoresNotInteger >= 1)
	    
	    if (!haveMaddocksScore) {
	    	
	    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Maddocks Score")) {
	    			for (i = 0; i <= 4; i++) {
		    			message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, 0),
										rgTable.getDataFilePositionMapping(i),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(i));
	
						message =
								message
										+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(i), message));
	    			} // for (i = 0; i <= 4; i++)
	    			break;
	    		}
	    	}
	    }
	    
	    int actualMaddocksSum = -1;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("MaddocksScoreTotalScore")) {
				data = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					if ((numberScoresMissing == 0) && (numberScoresNotInteger == 0)) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));
	
						message =
								message
										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));  
					}
				}
				else if ((numberScoresMissing >= 1) || (numberScoresNotInteger >= 1)) {
			    	message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,
											data);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
			    }
				else {
					try {
				        actualMaddocksSum = Integer.valueOf(data).intValue();
					}
					catch (NumberFormatException e) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
												data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index), 
								rgTable.getDataFilePositionMapping(index), message));   	
					}
				}
				break;
			}
	    }
	    
	    if (actualMaddocksSum > -1) {
	    	if (actualMaddocksSum != calculatedMaddocksSum) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(index));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
										String.valueOf(actualMaddocksSum), String.valueOf(calculatedMaddocksSum));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(index), message));
	    	}
	    }
	    
	    boolean haveMedclHist = false;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("MedclHistCondTxt")) {
				haveMedclHist = true;
				data = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
				}	
				
				break;
			  } // if (bufColumnNameVector.get(i).equalsIgnoreCase("MedclHistCondTxt"))
			} // for (i = 0; i < bufColumnNameVector.size(); i++)
	    
    if (!haveMedclHist) {
	    	
	    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Mechanism of Injury")) {
	    			message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, 0),
									rgTable.getDataFilePositionMapping(0),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(0));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(0), message));
	    			break;
	    		}
	    	}
	    }

	numberScoresMissing = 0;
	missingIndices.clear();
	missing_repeatable_group_id = -1;
	missing_group_row_index = -1;
	boolean haveBackground = false;
	for (i = 0; i < bufColumnNameVector.size(); i++) {
		if (bufColumnNameVector.get(i).equalsIgnoreCase("EduYrCt") ||
			bufColumnNameVector.get(i).equalsIgnoreCase("HandPrefTyp") ||
			bufColumnNameVector.get(i).equalsIgnoreCase("ConcussionPriorNum") ||
			bufColumnNameVector.get(i).equalsIgnoreCase("SportTeamParticipationTyp") ||
			bufColumnNameVector.get(i).equalsIgnoreCase("SportTeamParticipationTypOth") ||
			bufColumnNameVector.get(i).equalsIgnoreCase("SportsTeamCat")) {
			haveBackground = true;
			data = bufDataVector.get(i);
	    	if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
					(data.trim().isEmpty())) {
	    		numberScoresMissing++;
	    		missingIndices.add(bufIndex.get(i));
			    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
				missing_group_row_index = bufGroup_row_index.get(i);
	    	}
	    }
}

    if (numberScoresMissing >= 1) {
		rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
		rgEntries = table.getAllReferences(subject_row_id, 
				missing_repeatable_group_id, null);
		for (i = 0; i < numberScoresMissing; i++) {
			message =
					String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
							guid,
							getRawDataRow(subject_row_id, missing_group_row_index),
							rgTable.getDataFilePositionMapping(missingIndices.get(i)),
							rgTable.getRepeatableGroupName(),
							rgTable.getColumnName(missingIndices.get(i)));
	
			message =
					message
							+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
			table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, missing_group_row_index),
					rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
		}
	} // if (numberScoresMissing >= 1)
    
	    if (!haveBackground) {
	    	
	    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Background")) {
	    			for (i = 0; i <= 5; i++) {
		    			message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, 0),
										rgTable.getDataFilePositionMapping(i),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(i));
	
						message =
								message
										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(i), message));
	    			} // for (i = 0; i <= 5; i++)
	    			break;
	    		}
	    	}
	    }
	    
	    numberScoresMissing = 0;
		missingIndices.clear();
		missing_repeatable_group_id = -1;
		missing_group_row_index = -1;
		boolean haveBackgroundConcussion = false;
		for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("ConcussionEventDate") ||
				bufColumnNameVector.get(i).equalsIgnoreCase("ConcussionSymptomDurDays")) {
				haveBackgroundConcussion = true;
				data = bufDataVector.get(i);
		    	if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
		    		numberScoresMissing++;
		    		missingIndices.add(bufIndex.get(i));
				    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
					missing_group_row_index = bufGroup_row_index.get(i);
		    	}
		    }
	}

	    if (numberScoresMissing >= 1) {
			rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
			rgEntries = table.getAllReferences(subject_row_id, 
					missing_repeatable_group_id, null);
			for (i = 0; i < numberScoresMissing; i++) {
				message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, missing_group_row_index),
								rgTable.getDataFilePositionMapping(missingIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(missingIndices.get(i)));
		
				message =
						message
								+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
				table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, missing_group_row_index),
						rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
			}
		} // if (numberScoresMissing >= 1)
	    
	    if (!haveBackgroundConcussion) {
	    	
	    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Background Most Recent Concussion")) {
	    			for (i = 0; i <= 1; i++) {
		    			message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, 0),
										rgTable.getDataFilePositionMapping(i),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(i));
	
						message =
								message
										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(i), message));
	    			} // for (i = 0; i <= 1; i++)
	    			break;
	    		}
	    	}
	    }
		    
		    numberScoresMissing = 0;
			missingIndices.clear();
			missing_repeatable_group_id = -1;
			missing_group_row_index = -1;
			boolean haveBackgroundMedicalHistory = false;
			for (i = 0; i < bufColumnNameVector.size(); i++) {
				if (bufColumnNameVector.get(i).equalsIgnoreCase("SCAT3HospitalImgHeadInjInd") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("HeadachMigranDiagnsInd") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("LearnDisableADHDDiagnosInd") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("SCAT3AnxDeprPsychDxInd") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("SCAT3AnxDeprPsychFamHistInd") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("MedctPrConcomOngoingInd") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("MedicationsText")) {
					haveBackgroundMedicalHistory = true;
					data = bufDataVector.get(i);
			    	if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
			    		numberScoresMissing++;
			    		missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
			    	}
			    }
		}

	    if (numberScoresMissing >= 1) {
			rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
			rgEntries = table.getAllReferences(subject_row_id, 
					missing_repeatable_group_id, null);
			for (i = 0; i < numberScoresMissing; i++) {
				message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, missing_group_row_index),
								rgTable.getDataFilePositionMapping(missingIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(missingIndices.get(i)));
		
				message =
						message
								+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
				table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, missing_group_row_index),
						rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
			}
		} // if (numberScoresMissing >= 1)
	    
		if (!haveBackgroundMedicalHistory) {
		    	
	    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Background Medical History")) {
	    			for (i = 0; i <= 6; i++) {
		    			message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, 0),
										rgTable.getDataFilePositionMapping(i),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(i));
	
						message =
								message
										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(i), message));
	    			} // for (i = 0; i <= 6; i++)
	    			break;
	    		}
	    	}
	    }
		    
		    numberScoresMissing = 0;
			missingIndices.clear();
			missing_repeatable_group_id = -1;
			missing_group_row_index = -1;
			numberScoresNotInteger = 0;
		    notIntegerIndices.clear();
		    notInteger_repeatable_group_id = -1;
		    notInteger_group_row_index = -1;
		    notIntegerData.clear();
			boolean haveSymptomEvaluation = false;
			int calculatedScat3TotalSymptoms = 0;
			int calculatedScat3TotSympScore = 0;
			for (i = 0; i < bufColumnNameVector.size(); i++) {
				if (bufColumnNameVector.get(i).equalsIgnoreCase("Scat3Headache") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("Scat3Pressureinhead") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("Scat3Neckpain") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("Scat3Nauseavomiting") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("Scat3Dizziness") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("Scat3BlurryVision") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("Scat3BalanceProblem") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("Scat3senssivitylight") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("Scat3SensitivityNoise") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("Scat3FeelSlowDown") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("Scat3FeelFog") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("Scat3DontFeelRight") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("Scat3DifficultyConcent") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("Scat3DifficultyRemembering") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("Scat3FatgLowEnergy") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("Scat3Confusion") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("Scat3Drowsiness") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("Scat3TroublFallAsleep") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("Scat3MoreEmotional") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("Scat3Irritable") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("Scat3Sadness") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("Scat3NervousAnxious") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("Scat3SympPhysActvty") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("Scat3SympMentActvty") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("SCAT3DataSourceTyp") ||
					bufColumnNameVector.get(i).equalsIgnoreCase("SCAT3DifferAthleteActScore")) {
					haveSymptomEvaluation = true;
					data = bufDataVector.get(i);
			    	if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
			    		numberScoresMissing++;
			    		missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
			    	}
			    	else if (!(bufColumnNameVector.get(i).equalsIgnoreCase("Scat3SympPhysActvty") ||
							bufColumnNameVector.get(i).equalsIgnoreCase("Scat3SympMentActvty") ||
							bufColumnNameVector.get(i).equalsIgnoreCase("SCAT3DataSourceTyp") ||
							bufColumnNameVector.get(i).equalsIgnoreCase("SCAT3DifferAthleteActScore"))) {
			    		try {
					        calculatedScat3TotSympScore += Integer.valueOf(data).intValue();
					        calculatedScat3TotalSymptoms++;
						}
						catch (NumberFormatException e) {
							numberScoresNotInteger++;
						    notIntegerIndices.add(bufIndex.get(i));
						    notInteger_repeatable_group_id = bufRepeatable_group_id.get(i);
							notInteger_group_row_index = bufGroup_row_index.get(i);
							notIntegerData.add(data);
						}	
			    	}
			    }
		}

	    if (numberScoresMissing >= 1) {
			rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
			rgEntries = table.getAllReferences(subject_row_id, 
					missing_repeatable_group_id, null);
			for (i = 0; i < numberScoresMissing; i++) {
				message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, missing_group_row_index),
								rgTable.getDataFilePositionMapping(missingIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(missingIndices.get(i)));
		
				message =
						message
								+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index),
						rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
			}
		} // if (numberScoresMissing >= 1)
		    
	    if (numberScoresNotInteger >= 1) {
	    	rgTable = table.getRepeatableGroupTable(notInteger_repeatable_group_id);
			rgEntries = table.getAllReferences(subject_row_id, 
					notInteger_repeatable_group_id, null);
	    	for (i = 0; i < numberScoresNotInteger; i++) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, notInteger_group_row_index),
								rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(notIntegerIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
										notIntegerData.get(i));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, notInteger_group_row_index),
						rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)), message));
	    	}
	    } // if (numberScoresNotInteger >= 1)
		    
	    if (!haveSymptomEvaluation) {
	    	
	    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Symptom Evaluation")) {
	    			for (i = 0; i <= 25; i++) {
		    			message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, 0),
										rgTable.getDataFilePositionMapping(i),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(i));
	
						message =
								message
										+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(i), message));
	    			} // for (i = 0; i <= 25; i++)
	    			break;
	    		}
	    	}
	    }
	    
	    int actualScat3TotalSymptoms = -1;
	    boolean haveScoringSummary = false;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("Scat3TotalSymptoms")) {
				haveScoringSummary = true;
				data = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));
	
						message =
								message
										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message)); 
				}
				else {
					try {
				        actualScat3TotalSymptoms = Integer.valueOf(data).intValue();
					}
					catch (NumberFormatException e) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
												data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));   	
					}
				}
				break;
			}
	    }
	    
    if (!haveScoringSummary) {
	    	
	    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Scoring Summary")) {
	  
	    			message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, 0),
									rgTable.getDataFilePositionMapping(0),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(0));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(0), message));
	    			break;
	    		}
	    	}
	    }
	    
	    if (actualScat3TotalSymptoms > -1) {
	    	if (actualScat3TotalSymptoms != calculatedScat3TotalSymptoms) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(index));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
										String.valueOf(actualScat3TotalSymptoms), String.valueOf(calculatedScat3TotalSymptoms));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(index), message));
	    	}
	    }
	    
	    int actualScat3TotSympScore = -1;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("Scat3TotSympScore")) {
				data = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					if (calculatedScat3TotalSymptoms == 22) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));
	
						message =
								message
										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));  
					}
				}
				else if (calculatedScat3TotalSymptoms < 22) {
			    	message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,
											data);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
			    }
				else {
					try {
				        actualScat3TotSympScore = Integer.valueOf(data).intValue();
					}
					catch (NumberFormatException e) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
												data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));   	
					}
				}
				break;
			}
	    }
	    
    if ((!haveScoringSummary) && (numberScoresMissing == 0) && (numberScoresNotInteger == 0)) {
	    	
	    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Scoring Summary")) {
	  
	    			message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, 0),
									rgTable.getDataFilePositionMapping(1),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(1));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(1), message));
	    			break;
	    		}
	    	}
	    }
	    
	    if (actualScat3TotSympScore > -1) {
	    	if (actualScat3TotSympScore != calculatedScat3TotSympScore) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(index));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
										String.valueOf(actualScat3TotSympScore), String.valueOf(calculatedScat3TotSympScore));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(index), message));
	    	}
	    }
	    
	    numberScoresMissing = 0;
	    missingIndices.clear();
	    missing_repeatable_group_id = -1;
	    missing_group_row_index = -1;
	    boolean haveSACOrientationScore = false;
	    numberScoresNotInteger = 0;
	    notIntegerIndices.clear();
	    notInteger_repeatable_group_id = -1;
	    notInteger_group_row_index = -1;
	    notIntegerData.clear();
	    int calculatedSACOrientationSubsetScore = 0;
	    int numberCognitiveScoresMissing = 0;
	    int numberCognitiveScoresNotInteger = 0;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
	    	if (bufColumnNameVector.get(i).equalsIgnoreCase("SACOrientationCurrMonthScore") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("SACOrientationCurrDateScore") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("SACOrientationCurrDayWeekScore") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("SACOrientationCurrYearScore") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("SACOrientationCurrTimeScore")) {
	    		haveSACOrientationScore = true;
	    		data = bufDataVector.get(i);
		    	if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
		    		numberScoresMissing++;
		    		missingIndices.add(bufIndex.get(i));
				    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
					missing_group_row_index = bufGroup_row_index.get(i);
		    	}
		    	else {
					try {
				        calculatedSACOrientationSubsetScore += Integer.valueOf(data).intValue();
					}
					catch (NumberFormatException e) {
						numberScoresNotInteger++;
					    notIntegerIndices.add(bufIndex.get(i));
					    notInteger_repeatable_group_id = bufRepeatable_group_id.get(i);
						notInteger_group_row_index = bufGroup_row_index.get(i);
						notIntegerData.add(data);
					}
				}
	        }
	    }
	    numberCognitiveScoresMissing = numberScoresMissing;
	    numberCognitiveScoresNotInteger = numberScoresNotInteger;
	    
	    if (numberScoresMissing >= 1) {
	    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
			rgEntries = table.getAllReferences(subject_row_id, 
					missing_repeatable_group_id, null);
	    	for (i = 0; i < numberScoresMissing; i++) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, missing_group_row_index),
								rgTable.getDataFilePositionMapping(missingIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(missingIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index),
						rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
	    	}
	    } // if (numberScoresMissing >= 1)
	    
	    if (numberScoresNotInteger >= 1) {
	    	rgTable = table.getRepeatableGroupTable(notInteger_repeatable_group_id);
			rgEntries = table.getAllReferences(subject_row_id, 
					notInteger_repeatable_group_id, null);
	    	for (i = 0; i < numberScoresNotInteger; i++) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, notInteger_group_row_index),
								rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(notIntegerIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
										notIntegerData.get(i));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, notInteger_group_row_index),
						rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)), message));
	    	}
	    } // if (numberScoresNotInteger >= 1)
	    
	    if (!haveSACOrientationScore) {
	    	
	    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("SAC Cognitive Assessment- Orientation")) {
	    			for (i = 0; i <= 4; i++) {
		    			message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, 0),
										rgTable.getDataFilePositionMapping(i),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(i));
	
						message =
								message
										+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(i), message));
	    			} // for (i = 0; i <= 4; i++)
	    			break;
	    		}
	    	}
	    }
	    
	    int actualSACOrientationSubsetScore = -1;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("SACOrientationSubsetScore")) {
				data = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					if ((numberScoresMissing == 0) && (numberScoresNotInteger == 0)) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));
	
						message =
								message
										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));  
					}
				}
				else if ((numberScoresMissing >= 1) || (numberScoresNotInteger >= 1)) {
			    	message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,
											data);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
			    }
				else {
					try {
				        actualSACOrientationSubsetScore = Integer.valueOf(data).intValue();
					}
					catch (NumberFormatException e) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
												data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));   	
					}
				}
				break;
			}
	    }
	    
    if ((!haveScoringSummary) && (numberScoresMissing == 0) && (numberScoresNotInteger == 0)) {
	    	
	    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Scoring Summary")) {
	  
	    			message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, 0),
									rgTable.getDataFilePositionMapping(2),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(2));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(2), message));
	    			break;
	    		}
	    	}
	    }
	    
	    if (actualSACOrientationSubsetScore > -1) {
	    	if (actualSACOrientationSubsetScore != calculatedSACOrientationSubsetScore) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(index));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
										String.valueOf(actualSACOrientationSubsetScore), String.valueOf(calculatedSACOrientationSubsetScore));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(index), message));
	    	}
	    }
	    
	    numberScoresMissing = 0;
	    missingIndices.clear();
	    missing_repeatable_group_id = -1;
	    Vector<Integer> missingGroupRowIndices = new Vector<Integer>();
	    boolean haveSACImmediateMemoryScore = false;
	    numberScoresNotInteger = 0;
	    notIntegerIndices.clear();
	    notInteger_repeatable_group_id = -1;
	    Vector<Integer> notIntegerGroupRowIndices = new Vector<Integer>();
	    notIntegerData.clear();
	    int calculatedSACImmdMemorySubsetScore = 0;
	    int trialNumber = 1;
	    boolean haveTrial1 = false;
	    boolean haveTrial2 = false;
	    boolean haveTrial3 = false;
	    int numberTrials = 0;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
	    	if (bufRepeatableGroupName.get(i).equalsIgnoreCase("SAC Cognitive Assessment - Immediate Memory")) {
	    	if (bufColumnNameVector.get(i).equalsIgnoreCase("TrialNumber") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("SACListTyp") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("SACImmediateMemoryWord1Score") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("SACImmediateMemoryWord2Score") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("SACImmediateMemoryWord3Score") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("SACImmediateMemoryWord4Score") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("SACImmediateMemoryWord5Score")) {
	    		haveSACImmediateMemoryScore = true;
	    		data = bufDataVector.get(i);
		    	if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
		    		numberScoresMissing++;
		    		missingIndices.add(bufIndex.get(i));
				    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
					missingGroupRowIndices.add(bufGroup_row_index.get(i));
					if (!(bufColumnNameVector.get(i).equalsIgnoreCase("TrialNumber") ||
				    		bufColumnNameVector.get(i).equalsIgnoreCase("SACListTyp"))){
						numberCognitiveScoresMissing++;
					}
		    	}
		    	else {
		    		if (!(bufColumnNameVector.get(i).equalsIgnoreCase("TrialNumber") ||
	    		    bufColumnNameVector.get(i).equalsIgnoreCase("SACListTyp"))){
					try {
				        calculatedSACImmdMemorySubsetScore += Integer.valueOf(data).intValue();
					}
					catch (NumberFormatException e) {
						numberScoresNotInteger++;
						numberCognitiveScoresNotInteger++;
					    notIntegerIndices.add(bufIndex.get(i));
					    notInteger_repeatable_group_id = bufRepeatable_group_id.get(i);
						notIntegerGroupRowIndices.add(bufGroup_row_index.get(i));
						notIntegerData.add(data);
					}
		    		}
					else if (bufColumnNameVector.get(i).equalsIgnoreCase("TrialNumber")) {
						try {
							trialNumber = Integer.valueOf(data).intValue();
							if (trialNumber == 1) {
								if (!haveTrial1) {
									numberTrials++;
								}
								haveTrial1 = true;
							}
							else if (trialNumber == 2) {
								if (!haveTrial2) {
									numberTrials++;
								}
								haveTrial2 = true;
							}
							else if (trialNumber == 3) {
								if (!haveTrial3) {
									numberTrials++;
								}
								haveTrial3 = true;
							}
						}
						catch (NumberFormatException e){ 
							numberScoresNotInteger++;
						    notIntegerIndices.add(bufIndex.get(i));
						    notInteger_repeatable_group_id = bufRepeatable_group_id.get(i);
							notIntegerGroupRowIndices.add(bufGroup_row_index.get(i));
							notIntegerData.add(data);
						}
					}
				} // else data not blank
	        } // if (bufColumnNameVector.get(i).equalsIgnoreCase("TrialNumber") ||
	    	} // if (bufRepeatableGroupName.get(i).equalsIgnoreCase("SAC Cognitive Assessment - Immediate Memory"))
	    } // for (i = 0; i < bufColumnNameVector.size(); i++)
	    
	    if (numberScoresMissing >= 1) {
	    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
			rgEntries = table.getAllReferences(subject_row_id, 
					missing_repeatable_group_id, null);
	    	for (i = 0; i < numberScoresMissing; i++) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, missingGroupRowIndices.get(i)),
								rgTable.getDataFilePositionMapping(missingIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(missingIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missingGroupRowIndices.get(i)),
						rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
	    	}
	    } // if (numberScoresMissing >= 1)
	    
	    if (numberScoresNotInteger >= 1) {
	    	rgTable = table.getRepeatableGroupTable(notInteger_repeatable_group_id);
			rgEntries = table.getAllReferences(subject_row_id, 
					notInteger_repeatable_group_id, null);
	    	for (i = 0; i < numberScoresNotInteger; i++) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, notIntegerGroupRowIndices.get(i)),
								rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(notIntegerIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
										notIntegerData.get(i));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, notIntegerGroupRowIndices.get(i)),
						rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)), message));
	    	}
	    } // if (numberScoresNotInteger >= 1)
	    
	    if (!haveSACImmediateMemoryScore) {
	    	
	    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("SAC Cognitive Assessment - Immediate Memory")) {
	    			for (j = 0; j <= 0; j++) {
	    			for (i = 0; i <= 6; i++) {
		    			message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, j),
										rgTable.getDataFilePositionMapping(i),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(i));
	
						message =
								message
										+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, j), rgTable.getDataFilePositionMapping(i), message));
	    			} // for (i = 0; i <= 6; i++)
	    			} // for (j = 0; j <= 0; j++)
	    			break;
	    		}
	    	}
	    }
	    
	    
	    int numberTimes = 0;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("SACImmdMemorySubsetScore")) {
				numberTimes++;
			}
	    }
	    int actualSACImmdMemorySubsetScore = -1;
	    int thisTime = 0;
	    boolean haveValue = false;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("SACImmdMemorySubsetScore")) {
				thisTime++;
				data = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					if ((numberCognitiveScoresMissing == 0) && (numberCognitiveScoresNotInteger == 0)
							&& (numberTimes == thisTime) && (!haveValue)) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));
	
						message =
								message
										+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
						if (numberTrials < 3) {
							message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(index),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(index));

							message =
									message
											+ String.format(ApplicationsConstants.ERR_TOO_FEW_TRIALS,
													trialNumber, 3);
							table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index), message)); 	
						}
					}
				}
				else if ((numberCognitiveScoresMissing >= 1) || (numberCognitiveScoresNotInteger >= 1)) {
					haveValue = true;
			    	message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,
											data);
					if ((numberCognitiveScoresMissing == 1) && (numberCognitiveScoresNotInteger == 0)) {
						message = message + " due to 1 missing value";			
					}
					else if  ((numberCognitiveScoresMissing > 1) && (numberCognitiveScoresNotInteger == 0)) {
						message = message + " due to " + String.valueOf(numberCognitiveScoresMissing) +
								" missing values";
					}
					else if  ((numberCognitiveScoresMissing == 0) && (numberCognitiveScoresNotInteger == 1)) {
						message = message + " due to 1 noninteger value";
					}
					else if  ((numberCognitiveScoresMissing == 0) && (numberCognitiveScoresNotInteger > 1)) {
						message = message + " due to " + String.valueOf(numberCognitiveScoresNotInteger) +
								" noninteger values";
					}
					else {
						int totalScores = numberCognitiveScoresMissing + numberCognitiveScoresNotInteger;
						message = message + " due to " + String.valueOf(totalScores) + " missing and noninteger values";
					    
					}
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
			    }
				else {
					haveValue = true;
					try {
				        actualSACImmdMemorySubsetScore = Integer.valueOf(data).intValue();
					}
					catch (NumberFormatException e) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
												data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));   	
					}
					if (trialNumber < 3) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_TOO_FEW_TRIALS,
												trialNumber, 3);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));   	
					} // if (trialNumber < 3)
				}
			}
	    }
	    
    if ((!haveScoringSummary) && (numberScoresMissing == 0) && (numberScoresNotInteger == 0)) {
	    	
	    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Scoring Summary")) {
	  
	    			message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, 0),
									rgTable.getDataFilePositionMapping(3),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(3));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(3), message));
	    			break;
	    		}
	    	}
	    }
	    
	    if (actualSACImmdMemorySubsetScore > -1) {
	    	if (actualSACImmdMemorySubsetScore != calculatedSACImmdMemorySubsetScore) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(index));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
										String.valueOf(actualSACImmdMemorySubsetScore), String.valueOf(calculatedSACImmdMemorySubsetScore));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(index), message));
	    	}
	    }
	    
	    numberScoresMissing = 0;
	    missingIndices.clear();
	    missing_repeatable_group_id = -1;
	    missingGroupRowIndices.clear();
	    boolean haveSACConcentrationScore = false;
	    numberScoresNotInteger = 0;
	    notIntegerIndices.clear();
	    notInteger_repeatable_group_id = -1;
	    notIntegerGroupRowIndices.clear();
	    notIntegerData.clear();
	    int calculatedSACConcentrationSubsetScore = 0;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
	    	if (bufRepeatableGroupName.get(i).equalsIgnoreCase("SAC Cognitive Assessment - Concentration")) {
	    	if (bufColumnNameVector.get(i).equalsIgnoreCase("TrialNumber") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("SACListTyp") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("SACConcDigitBackwrdsSet1Score") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("SACConcDigitBackwrdsSet2Score") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("SACConcDigitBackwrdsSet3Score") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("SACConcDigitBackwrdsSet4Score") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("SACConcDigitBackwrdsSet5Score")) {
	    		haveSACConcentrationScore = true;
	    		data = bufDataVector.get(i);
	    		if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
		    		numberScoresMissing++;
		    		missingIndices.add(bufIndex.get(i));
				    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
					missingGroupRowIndices.add(bufGroup_row_index.get(i));
					if (!(bufColumnNameVector.get(i).equalsIgnoreCase("TrialNumber") ||
				    		bufColumnNameVector.get(i).equalsIgnoreCase("SACListTyp"))){
						numberCognitiveScoresMissing++;
					}
		    	}
		    	else {
		    		if (!(bufColumnNameVector.get(i).equalsIgnoreCase("TrialNumber") ||
	    		    bufColumnNameVector.get(i).equalsIgnoreCase("SACListTyp"))){
					try {
				        calculatedSACConcentrationSubsetScore += Integer.valueOf(data).intValue();
					}
					catch (NumberFormatException e) {
						numberScoresNotInteger++;
						numberCognitiveScoresNotInteger++;
					    notIntegerIndices.add(bufIndex.get(i));
					    notInteger_repeatable_group_id = bufRepeatable_group_id.get(i);
						notIntegerGroupRowIndices.add(bufGroup_row_index.get(i));
						notIntegerData.add(data);
					}
		    		}
					else if (bufColumnNameVector.get(i).equalsIgnoreCase("TrialNumber")) {
						try {
							trialNumber = Integer.valueOf(data).intValue();
						}
						catch (NumberFormatException e){ 
							numberScoresNotInteger++;
						    notIntegerIndices.add(bufIndex.get(i));
						    notInteger_repeatable_group_id = bufRepeatable_group_id.get(i);
							notIntegerGroupRowIndices.add(bufGroup_row_index.get(i));
							notIntegerData.add(data);
						}
					}
				} // else data not blank
	        }
	    	}
	    }
	    
	    if (numberScoresMissing >= 1) {
	    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
			rgEntries = table.getAllReferences(subject_row_id, 
					missing_repeatable_group_id, null);
	    	for (i = 0; i < numberScoresMissing; i++) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, missingGroupRowIndices.get(i)),
								rgTable.getDataFilePositionMapping(missingIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(missingIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missingGroupRowIndices.get(i)),
						rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
	    	}
	    } // if (numberScoresMissing >= 1)
	    
	    if (numberScoresNotInteger >= 1) {
	    	rgTable = table.getRepeatableGroupTable(notInteger_repeatable_group_id);
			rgEntries = table.getAllReferences(subject_row_id, 
					notInteger_repeatable_group_id, null);
	    	for (i = 0; i < numberScoresNotInteger; i++) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, notIntegerGroupRowIndices.get(i)),
								rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(notIntegerIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
										notIntegerData.get(i));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, notIntegerGroupRowIndices.get(i)),
						rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)), message));
	    	}
	    } // if (numberScoresNotInteger >= 1)
	    
	    if (!haveSACConcentrationScore) {
	    	
	    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("SAC Cognitive Assessment - Concentration")) {
	    			for (j = 0; j <= 0; j++) {
	    			for (i = 0; i <= 6; i++) {
		    			message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, j),
										rgTable.getDataFilePositionMapping(i),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(i));
	
						message =
								message
										+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, j), rgTable.getDataFilePositionMapping(i), message));
	    			} // for (i = 0; i <= 6; i++)
	    			} // for (j = 0; j <= 0; j++)
	    			break;
	    		}
	    	}
	    }
	    
	    int actualSACConcentrationSubsetScore = -1;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("SACConcentationSubsetScore")) {
				data = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					if ((numberScoresMissing == 0) && (numberScoresNotInteger == 0)) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));
	
						message =
								message
										+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));  
					}
				}
				else if ((numberScoresMissing >= 1) || (numberScoresNotInteger >= 1)) {
			    	message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,
											data);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
			    }
				else {
					try {
				        actualSACConcentrationSubsetScore = Integer.valueOf(data).intValue();
					}
					catch (NumberFormatException e) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
												data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));   	
					}
				}
				break;
			}
	    }
	    
    if ((!haveScoringSummary) && (numberScoresMissing == 0) && (numberScoresNotInteger == 0)) {
	    	
	    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Scoring Summary")) {
	  
	    			message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, 0),
									rgTable.getDataFilePositionMapping(5),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(5));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(5), message));
	    			break;
	    		}
	    	}
	    }
	    
	    if (actualSACConcentrationSubsetScore > -1) {
	    	if (actualSACConcentrationSubsetScore != calculatedSACConcentrationSubsetScore) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(index));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
										String.valueOf(actualSACConcentrationSubsetScore), String.valueOf(calculatedSACConcentrationSubsetScore));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(index), message));
	    	}
	    }
	    
	    int SACDelayedRecallSubsetScore = -1;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("SACDelayedRecallSubsetScore")) {
				data = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					numberCognitiveScoresMissing++;
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));
	
						message =
								message
										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));  
				}
				else {
					try {
				        SACDelayedRecallSubsetScore = Integer.valueOf(data).intValue();
					}
					catch (NumberFormatException e) {
						numberCognitiveScoresNotInteger++;
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
												data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));   	
					}
				}
				break;
			}
	    }
	    
    if (!haveScoringSummary) {
	    	numberCognitiveScoresMissing++;
	    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Scoring Summary")) {
	                
	    			message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, 0),
									rgTable.getDataFilePositionMapping(6),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(6));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(6), message));
	    			break;
	    		}
	    	}
	    }
	    
	    int calculatedSACTotalScore = calculatedSACOrientationSubsetScore +
	    		calculatedSACImmdMemorySubsetScore + calculatedSACConcentrationSubsetScore +
	    		SACDelayedRecallSubsetScore;
	    
	    int actualSACTotalScore = -1;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("SACTotalScore")) {
				data = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					if ((numberCognitiveScoresMissing == 0) && (numberCognitiveScoresNotInteger == 0)) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));
	
						message =
								message
										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));  
					}
				}
				else if ((numberCognitiveScoresMissing >= 1) || (numberCognitiveScoresNotInteger >= 1)) {
			    	message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,
											data);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
			    }
				else {
					try {
				        actualSACTotalScore = Integer.valueOf(data).intValue();
					}
					catch (NumberFormatException e) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
												data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));   	
					}
				}
				break;
			}
	    }
	    
    if ((!haveScoringSummary) && (numberCognitiveScoresMissing == 0) && (numberCognitiveScoresNotInteger == 0)) {
	    	
	    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Scoring Summary")) {
	  
	    			message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, 0),
									rgTable.getDataFilePositionMapping(7),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(7));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(7), message));
	    			break;
	    		}
	    	}
	    }
	    
	    if (actualSACTotalScore > -1) {
	    	if (actualSACTotalScore != calculatedSACTotalScore) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(index));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
										String.valueOf(actualSACTotalScore), String.valueOf(calculatedSACTotalScore));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(index), message));
	    	}
	    }
	    
	    numberScoresMissing = 0;
	    missingIndices.clear();
	    missing_repeatable_group_id = -1;
	    missing_group_row_index = -1;
	    boolean haveNeckExamination = false;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
	    	if (bufColumnNameVector.get(i).equalsIgnoreCase("SCAT3NeckExamRangOfMotionTxt") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("SCAT3NeckExamTendernessTxt") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("SCAT3NeckExamLowerLimbSensTxt") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("SCAT3NeckExamUpperLimbSensTxt")) {
	    		haveNeckExamination = true;
	    		data = bufDataVector.get(i);
		    	if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
		    		numberScoresMissing++;
		    		missingIndices.add(bufIndex.get(i));
				    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
					missing_group_row_index = bufGroup_row_index.get(i);
		    	}
	        }
	    }
	    
	    if (numberScoresMissing >= 1) {
	    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
			rgEntries = table.getAllReferences(subject_row_id, 
					missing_repeatable_group_id, null);
	    	for (i = 0; i < numberScoresMissing; i++) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, missing_group_row_index),
								rgTable.getDataFilePositionMapping(missingIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(missingIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index),
						rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
	    	}
	    } // if (numberScoresMissing >= 1)
	    
	    if (!haveNeckExamination) {
	    	
	    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Neck Examination")) {
	    			for (i = 0; i <= 3; i++) {
		    			message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, 0),
										rgTable.getDataFilePositionMapping(i),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(i));
	
						message =
								message
										+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(i), message));
	    			} // for (i = 0; i <= 3; i++)
	    			break;
	    		}
	    	}
	    }
	    
	    numberScoresMissing = 0;
	    missingIndices.clear();
	    missing_repeatable_group_id = -1;
	    missing_group_row_index = -1;
	    boolean haveBESS = false;
	    numberScoresNotInteger = 0;
	    notIntegerIndices.clear();
	    notInteger_repeatable_group_id = -1;
	    notInteger_group_row_index = -1;
	    notIntegerData.clear();
	    int calculatedBESSDblLegTotalErrorCt = 0;
	    int calculatedBESSSglLegTotalErrorCt = 0;
	    int calculatedBESSTandemStncTotalErrorCt = 0;
	    int calculatedBESSTotalErrorCt = 0;
	    int numberBESSDblMissing = 0;
	    int numberBESSDblNotInteger = 0;
	    int numberBESSSglMissing = 0;
	    int numberBESSSglNotInteger = 0;
	    int numberBESSTandemMissing = 0;
	    int numberBESSTandemNotInteger = 0;
	    int numberBESSMissing = 0;
	    int numberBESSNotInteger = 0;
	    int BESSValue;
	    boolean isFirm = true;
	    
	    int BESSFullTandemStandDur = -1;
	    int FullTandemStandDur = -1;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
	    	if (bufColumnNameVector.get(i).equalsIgnoreCase("FootwearUseTyp") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("BESSFootTestAnatSite") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("GroundSurfTyp") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("BESSDblLegFirmErrorCt") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("BESSDblLegFoamErrorCt") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("BESSSglLegFirmErrorCt") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("BESSSglLegFoamErrorCt") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("BESSTandemFirmErrorCt") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("BESSTandemStncFoamSrfcErrorCt") ||
	    		// BESSFullTandemStandDur in BESS can be different from
	    		// FullTandemStandDur in Scoring Summary
	    		bufColumnNameVector.get(i).equalsIgnoreCase("BESSFullTandemStandDur")) {
	    		haveBESS = true;
	    		data = bufDataVector.get(i);
		    	if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					if ((isFirm && bufColumnNameVector.get(i).equalsIgnoreCase("BESSDblLegFirmErrorCt")) ||
	    		    ((!isFirm) && bufColumnNameVector.get(i).equalsIgnoreCase("BESSDblLegFoamErrorCt"))) {
						numberScoresMissing++;
			    		missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
						numberBESSDblMissing++;
					}
					else if ((isFirm && bufColumnNameVector.get(i).equalsIgnoreCase("BESSSglLegFirmErrorCt")) ||
	    		    ((!isFirm) && bufColumnNameVector.get(i).equalsIgnoreCase("BESSSglLegFoamErrorCt"))) {
						numberScoresMissing++;
			    		missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
						numberBESSSglMissing++;
					}
					else if ((isFirm && bufColumnNameVector.get(i).equalsIgnoreCase("BESSTandemFirmErrorCt")) ||
	    		    ((!isFirm) && bufColumnNameVector.get(i).equalsIgnoreCase("BESSTandemStncFoamSrfcErrorCt"))) {
						numberScoresMissing++;
			    		missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
					    numberBESSTandemMissing++;	
					}
					else if (bufColumnNameVector.get(i).equalsIgnoreCase("FootwearUseTyp") ||
				    		bufColumnNameVector.get(i).equalsIgnoreCase("BESSFootTestAnatSite") ||
				    		bufColumnNameVector.get(i).equalsIgnoreCase("GroundSurfTyp") ||
				    		bufColumnNameVector.get(i).equalsIgnoreCase("BESSFullTandemStandDur")) {
						numberScoresMissing++;
			    		missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);	
					}
		    	}
		    	else {
		    		if (((isFirm && bufColumnNameVector.get(i).equalsIgnoreCase("BESSDblLegFirmErrorCt")) ||
			    		    ((!isFirm) && bufColumnNameVector.get(i).equalsIgnoreCase("BESSDblLegFoamErrorCt"))) ||
		    				((isFirm && bufColumnNameVector.get(i).equalsIgnoreCase("BESSSglLegFirmErrorCt")) ||
		    		    		    ((!isFirm) && bufColumnNameVector.get(i).equalsIgnoreCase("BESSSglLegFoamErrorCt"))) ||
		    				((isFirm && bufColumnNameVector.get(i).equalsIgnoreCase("BESSTandemFirmErrorCt")) ||
		    		    		    ((!isFirm) && bufColumnNameVector.get(i).equalsIgnoreCase("BESSTandemStncFoamSrfcErrorCt"))) ||
		    				(bufColumnNameVector.get(i).equalsIgnoreCase("BESSFullTandemStandDur") )) {
					try {
				        BESSValue = Integer.valueOf(data).intValue();
				        if (bufColumnNameVector.get(i).equalsIgnoreCase("BESSDblLegFirmErrorCt") ||
				    		bufColumnNameVector.get(i).equalsIgnoreCase("BESSDblLegFoamErrorCt")) {
				        	calculatedBESSDblLegTotalErrorCt = BESSValue;	
						}
						else if (bufColumnNameVector.get(i).equalsIgnoreCase("BESSSglLegFirmErrorCt") ||
		    		    bufColumnNameVector.get(i).equalsIgnoreCase("BESSSglLegFoamErrorCt")) {
							calculatedBESSSglLegTotalErrorCt = BESSValue;
						}
						else if (bufColumnNameVector.get(i).equalsIgnoreCase("BESSTandemFirmErrorCt") ||
		    		    bufColumnNameVector.get(i).equalsIgnoreCase("BESSTandemStncFoamSrfcErrorCt")) {
							calculatedBESSTandemStncTotalErrorCt = BESSValue;    	
						}
						else if (bufColumnNameVector.get(i).equalsIgnoreCase("BESSFullTandemStandDur") ) {
							BESSFullTandemStandDur = BESSValue;
						}
					}
					catch (NumberFormatException e) {
						numberScoresNotInteger++;
					    notIntegerIndices.add(bufIndex.get(i));
					    notInteger_repeatable_group_id = bufRepeatable_group_id.get(i);
						notInteger_group_row_index = bufGroup_row_index.get(i);
						notIntegerData.add(data);
						if (bufColumnNameVector.get(i).equalsIgnoreCase("BESSDblLegFirmErrorCt") ||
		    		    bufColumnNameVector.get(i).equalsIgnoreCase("BESSDblLegFoamErrorCt")) {
							numberBESSDblNotInteger++;
						}
						else if (bufColumnNameVector.get(i).equalsIgnoreCase("BESSSglLegFirmErrorCt") ||
		    		    bufColumnNameVector.get(i).equalsIgnoreCase("BESSSglLegFoamErrorCt")) {
							numberBESSSglNotInteger++;
						}
						else if (bufColumnNameVector.get(i).equalsIgnoreCase("BESSTandemFirmErrorCt") ||
		    		    bufColumnNameVector.get(i).equalsIgnoreCase("BESSTandemStncFoamSrfcErrorCt")) {
						    numberBESSTandemNotInteger++;	
						}
					}
		    		}
		    		else if (bufColumnNameVector.get(i).equalsIgnoreCase("GroundSurfTyp")) {
						if (data.equalsIgnoreCase("Firm")) {
							isFirm = true;
						}
						else if (data.equalsIgnoreCase("Foam")) {
							isFirm = false;
						}
						else {
							repeatable_group_id = bufRepeatable_group_id.get(i);
							group_row_index = bufGroup_row_index.get(i);
							index = bufIndex.get(i);
							rgTable = table.getRepeatableGroupTable(repeatable_group_id);
							rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
							message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(index),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(index));

							message =
									message
											+ String.format(ApplicationsConstants.ERR_ILLEGAL_GROUNDSURFTYP, data);
							table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index), message));		
						}
					} // else if (bufColumnNameVector.get(i).equalsIgnoreCase("GroundSurfTyp"))
		    		else if ((((!isFirm) && bufColumnNameVector.get(i).equalsIgnoreCase("BESSDblLegFirmErrorCt")) ||
			    		    (isFirm && bufColumnNameVector.get(i).equalsIgnoreCase("BESSDblLegFoamErrorCt"))) ||
		    				(((!isFirm) && bufColumnNameVector.get(i).equalsIgnoreCase("BESSSglLegFirmErrorCt")) ||
		    		    		    (isFirm && bufColumnNameVector.get(i).equalsIgnoreCase("BESSSglLegFoamErrorCt"))) ||
		    				(((!isFirm) && bufColumnNameVector.get(i).equalsIgnoreCase("BESSTandemFirmErrorCt")) ||
		    		    		    (isFirm && bufColumnNameVector.get(i).equalsIgnoreCase("BESSTandemStncFoamSrfcErrorCt")))) {
		    			repeatable_group_id = bufRepeatable_group_id.get(i);
						group_row_index = bufGroup_row_index.get(i);
						index = bufIndex.get(i);
						rgTable = table.getRepeatableGroupTable(repeatable_group_id);
						rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED, data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));			
		    		}
				} // else data not blank
	        } // if (bufColumnNameVector.get(i).equalsIgnoreCase("FootwearUseTyp") ||
	    } // for (i = 0; i < bufColumnNameVector.size(); i++)
	    numberBESSMissing = numberBESSDblMissing + numberBESSSglMissing +
	    		            numberBESSTandemMissing;
	    numberBESSNotInteger = numberBESSDblNotInteger + numberBESSSglNotInteger +
	    		               numberBESSTandemNotInteger;
	    calculatedBESSTotalErrorCt = calculatedBESSDblLegTotalErrorCt +
	    		calculatedBESSSglLegTotalErrorCt +
	    		calculatedBESSTandemStncTotalErrorCt;
	    
	    if (numberScoresMissing >= 1) {
	    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
			rgEntries = table.getAllReferences(subject_row_id, 
					missing_repeatable_group_id, null);
	    	for (i = 0; i < numberScoresMissing; i++) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, missing_group_row_index),
								rgTable.getDataFilePositionMapping(missingIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(missingIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index),
						rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
	    	}
	    } // if (numberScoresMissing >= 1)
	    
	    if (numberScoresNotInteger >= 1) {
	    	rgTable = table.getRepeatableGroupTable(notInteger_repeatable_group_id);
			rgEntries = table.getAllReferences(subject_row_id, 
					notInteger_repeatable_group_id, null);
	    	for (i = 0; i < numberScoresNotInteger; i++) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, notInteger_group_row_index),
								rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(notIntegerIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
										notIntegerData.get(i));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, notInteger_group_row_index),
						rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)), message));
	    	}
	    } // if (numberScoresNotInteger >= 1)
	    
	    if (!haveBESS) {
	    	
	    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Balance examination (BESS)")) {
	    			for (i = 0; i <= 12; i++) {
		    			message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, 0),
										rgTable.getDataFilePositionMapping(i),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(i));
	
						message =
								message
										+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(i), message));
	    			} // for (i = 0; i <= 12; i++)
	    			break;
	    		}
	    	}
	    }
	    
	    int actualBESSDblLegTotalErrorCt = -1;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("BESSDblLegTotalErrorCt")) {
				data = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					if ((numberBESSDblMissing == 0) && (numberBESSDblNotInteger == 0)) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));
	
						message =
								message
										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message)); 
				    }
				}
			    else if ((numberBESSDblMissing >= 1) || (numberBESSDblNotInteger >= 1)) {
			    	message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,
											data);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
			    }
				else {
					try {
				        actualBESSDblLegTotalErrorCt = Integer.valueOf(data).intValue();
					}
					catch (NumberFormatException e) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
												data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));   	
					}
				}
				break;
			}
	    }
	    
	    if (actualBESSDblLegTotalErrorCt > -1) {
	    	if (actualBESSDblLegTotalErrorCt != calculatedBESSDblLegTotalErrorCt) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(index));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
										String.valueOf(actualBESSDblLegTotalErrorCt), String.valueOf(calculatedBESSDblLegTotalErrorCt));
				//table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						//rgTable.getDataFilePositionMapping(index), message));
	    	}
	    }
	    
	    int actualBESSSglLegTotalErrorCt = -1;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("BESSSglLegTotalErrorCt")) {
				data = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					if ((numberBESSSglMissing == 0) && (numberBESSSglNotInteger == 0)) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));
	
						message =
								message
										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message)); 
				    }
				}
			    else if ((numberBESSSglMissing >= 1) || (numberBESSSglNotInteger >= 1)) {
			    	message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,
											data);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
			    }
				else {
					try {
				        actualBESSSglLegTotalErrorCt = Integer.valueOf(data).intValue();
					}
					catch (NumberFormatException e) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
												data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));   	
					}
				}
				break;
			}
	    }
	    
	    if (actualBESSSglLegTotalErrorCt > -1) {
	    	if (actualBESSSglLegTotalErrorCt != calculatedBESSSglLegTotalErrorCt) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(index));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
										String.valueOf(actualBESSSglLegTotalErrorCt), String.valueOf(calculatedBESSSglLegTotalErrorCt));
				//table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						//rgTable.getDataFilePositionMapping(index), message));
	    	}
	    }
	    
	    int actualBESSTandemStncTotalErrorCt = -1;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("BESSTandemStncTotalErrorCt")) {
				data = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					if ((numberBESSTandemMissing == 0) && (numberBESSTandemNotInteger == 0)) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));
	
						message =
								message
										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message)); 
				    }
				}
			    else if ((numberBESSTandemMissing >= 1) || (numberBESSTandemNotInteger >= 1)) {
			    	message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,
											data);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
			    }
				else {
					try {
				        actualBESSTandemStncTotalErrorCt = Integer.valueOf(data).intValue();
					}
					catch (NumberFormatException e) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
												data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));   	
					}
				}
				break;
			}
	    }
	    
	    if (actualBESSTandemStncTotalErrorCt > -1) {
	    	if (actualBESSTandemStncTotalErrorCt != calculatedBESSTandemStncTotalErrorCt) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(index));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
										String.valueOf(actualBESSTandemStncTotalErrorCt), String.valueOf(calculatedBESSTandemStncTotalErrorCt));
				//table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						//rgTable.getDataFilePositionMapping(index), message));
	    	}
	    }
	    
    if ((!haveScoringSummary) && (numberBESSMissing == 0) && (numberBESSNotInteger == 0)) {
	    	
	    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Scoring Summary")) {
	  
	    			message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, 0),
									rgTable.getDataFilePositionMapping(8),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(8));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(8), message));
	    			break;
	    		}
	    	}
	    }
	    
	    int actualBESSTotalErrorCt = -1;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("BESSTotalErrorCt")) {
				data = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
					if ((numberBESSMissing == 0) && (numberBESSNotInteger == 0)) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));
	
						message =
								message
										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message)); 
				    }
				}
			    else if ((numberBESSMissing >= 1) || (numberBESSNotInteger >= 1)) {
			    	message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_REQUIRED,
											data);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
			    }
				else {
					try {
				        actualBESSTotalErrorCt = Integer.valueOf(data).intValue();
					}
					catch (NumberFormatException e) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
												data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));   	
					}
				}
				break;
			}
	    }
	    
	    if (actualBESSTotalErrorCt > -1) {
	    	if (actualBESSTotalErrorCt != calculatedBESSTotalErrorCt) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(index));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
										String.valueOf(actualBESSTotalErrorCt), String.valueOf(calculatedBESSTotalErrorCt));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
						rgTable.getDataFilePositionMapping(index), message));
	    	}
	    }
	    
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("FullTandemStandDur")) {
				data = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));
	
						message =
								message
										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message)); 
				}
				else {
					try {
				        FullTandemStandDur = Integer.valueOf(data).intValue();
					}
					catch (NumberFormatException e) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
												data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));   	
					}
				}
				break;
			}
	    }
	    
	    if (!haveScoringSummary) {
	    	// FullTandemStandDur is not present
	    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Scoring Summary")) {
	  
	    			message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, 0),
									rgTable.getDataFilePositionMapping(9),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(9));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(9), message));
	    			break;
	    		}
	    	}
	    }
	    
	    numberScoresMissing = 0;
	    missingIndices.clear();
	    missing_repeatable_group_id = -1;
	    missing_group_row_index = -1;
	    boolean haveCoordination = false;
	    numberScoresNotInteger = 0;
	    notIntegerIndices.clear();
	    notInteger_repeatable_group_id = -1;
	    notInteger_group_row_index = -1;
	    notIntegerData.clear();
	    // SCAT3UpperLimbCoordinatScore appears twice
	    // In Coordination Examination Upper Limb and in Scoring Summary
	    // The 2 values are allowed to be different.
	    int SCAT3UpperLimbCoordinatScore1 = -1;
	    int SCAT3UpperLimbCoordinatScore2 = -1;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
	    	if (bufRepeatableGroupName.get(i).equalsIgnoreCase("Coordination Examination Upper Limb")) {
	    	if (bufColumnNameVector.get(i).equalsIgnoreCase("LatTyp") ||
	    		bufColumnNameVector.get(i).equalsIgnoreCase("SCAT3UpperLimbCoordinatScore")) {
	    		haveCoordination = true;
	    		data = bufDataVector.get(i);
		    	if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
		    		numberScoresMissing++;
		    		missingIndices.add(bufIndex.get(i));
				    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
					missing_group_row_index = bufGroup_row_index.get(i);
		    	}
		    	else if (bufColumnNameVector.get(i).equalsIgnoreCase("SCAT3UpperLimbCoordinatScore")) {
					try {
						SCAT3UpperLimbCoordinatScore1 = Integer.valueOf(data).intValue();
					}
					catch (NumberFormatException e) {
						numberScoresNotInteger++;
					    notIntegerIndices.add(bufIndex.get(i));
					    notInteger_repeatable_group_id = bufRepeatable_group_id.get(i);
						notInteger_group_row_index = bufGroup_row_index.get(i);
						notIntegerData.add(data);
					}
				}
	        }
	    	}
	    }
	    
	    if (numberScoresMissing >= 1) {
	    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
			rgEntries = table.getAllReferences(subject_row_id, 
					missing_repeatable_group_id, null);
	    	for (i = 0; i < numberScoresMissing; i++) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, missing_group_row_index),
								rgTable.getDataFilePositionMapping(missingIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(missingIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index), 
						rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
	    	}
	    } // if (numberScoresMissing >= 1)
	    
	    if (numberScoresNotInteger >= 1) {
	    	rgTable = table.getRepeatableGroupTable(notInteger_repeatable_group_id);
			rgEntries = table.getAllReferences(subject_row_id, 
					notInteger_repeatable_group_id, null);
	    	for (i = 0; i < numberScoresNotInteger; i++) {
	    		message =
						String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
								guid,
								getRawDataRow(subject_row_id, notInteger_group_row_index),
								rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)),
								rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(notIntegerIndices.get(i)));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
										notIntegerData.get(i));
				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, notInteger_group_row_index),
						rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)), message));
	    	}
	    } // if (numberScoresNotInteger >= 1)
	    
	    if (!haveCoordination) {
	    	
	    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
	    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Coordination Examination Upper Limb")) {
	    			for (i = 0; i <= 1; i++) {
		    			message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, 0),
										rgTable.getDataFilePositionMapping(i),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(i));
	
						message =
								message
										+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(i), message));
	    			} // for (i = 0; i <= 1; i++)
	    			break;
	    		} // if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Coordination Examination Upper Limb"))
	    	} // for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++)
	    } // if (!haveCoordination)
	    	
    		for (i = 0; i < bufColumnNameVector.size(); i++) {
    			if (bufRepeatableGroupName.get(i).equalsIgnoreCase("Scoring Summary")) {
	    			if (bufColumnNameVector.get(i).equalsIgnoreCase("SCAT3UpperLimbCoordinatScore")) {
	    				data = bufDataVector.get(i);
	    				repeatable_group_id = bufRepeatable_group_id.get(i);
	    				group_row_index = bufGroup_row_index.get(i);
	    				index = bufIndex.get(i);
	    				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
	    				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
	    				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
	    						(data.trim().isEmpty())) {
	    						message =
	    								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
	    										guid,
	    										getRawDataRow(subject_row_id, group_row_index),
	    										rgTable.getDataFilePositionMapping(index),
	    										rgTable.getRepeatableGroupName(),
	    										rgTable.getColumnName(index));
	    	
	    						message =
	    								message
	    										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
	    						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
	    								rgTable.getDataFilePositionMapping(index), message)); 
	    				}
	    				else {
	    					try {
	    				        SCAT3UpperLimbCoordinatScore2 = Integer.valueOf(data).intValue();
	    					}
	    					catch (NumberFormatException e) {
	    						message =
	    								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
	    										guid,
	    										getRawDataRow(subject_row_id, group_row_index),
	    										rgTable.getDataFilePositionMapping(index),
	    										rgTable.getRepeatableGroupName(),
	    										rgTable.getColumnName(index));

	    						message =
	    								message
	    										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
	    												data);
	    						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
	    								rgTable.getDataFilePositionMapping(index), message));   	
	    					}
	    				}
	    				break;
	    	    } // if (bufColumnNameVector.get(i).equalsIgnoreCase("SCAT3UpperLimbCoordinatScore"))
	    	} // if (bufRepeatableGroupName.get(i).equalsIgnoreCase("Scoring Summary")) {
	    } // for (i = 0; i < bufColumnNameVector.size(); i++)
    
	    
	    
	    int SACImmdMemoryTrialScore= -1;
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufRepeatableGroupName.get(i).equalsIgnoreCase("Scoring Summary")) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("SACImmdMemoryTrialScore")) {
				data = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
						(data.trim().isEmpty())) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));
	
						message =
								message
										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message)); 
				}
				else {
					try {
				        SACImmdMemoryTrialScore = Integer.valueOf(data).intValue();
					}
					catch (NumberFormatException e) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
												data);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));   	
					}
				}
				break;
			}
			}
	    }
	}
	
	private void validateSF12(StructuralFormStructure structure,
			Vector<Vector<Vector<String>>> columnNameVector3, 
			Vector<Vector<Vector<String>>>dataVector3, int subject_row_id,
			DataStructureTable table) throws Exception {
		// table.getColumnCount = 3
		// repeatable_group_id = 0
		// rgTable.getSize() = 12
		// rgEntries.size() = 1
		// columnNameVector.size() = 12
		// group_column_index = 0 group_column_name = sf36genhlthscore data = 3
	    // group_column_index = 1 group_column_name = sf36hlthlmtmodactscore data = 2
		// group_column_index = 2 group_column_name = sf36hlthlmtclmbsvrlstairscore data = 3
		// group_column_index = 3 group_column_name = sf12lesaccompphyhlthscale data = 4
		// group_column_index = 4 group_column_name = sf12lmtwrkactphyhlthscale data = 3
		// group_column_index = 5 group_column_name = sf12lesaccompemotprobscale data = 5
		// group_column_index = 6 group_column_name = sf12wknotcarefulemotprobscale data = 5
		// group_column_index = 7 group_column_name = sf36paininterfwrkscore data = 1
		// group_column_index = 8 group_column_name = sf12timescalmpcfulscale data = 2
		// group_column_index = 9 group_column_name = sf12timesenrgyscale data = 2
		// group_column_index = 10 group_column_name = sf12timesdowndepressedscale data = 5
		// group_column_index = 11 group_column_name = sf12interfsocphyemotscale data = 3
		// repeatable_group_id = 1
		// rgTable.getSize() = 4
		// rgEntries.size() = 0
	    // repeatable_group_id = 2
	    // rgTable.getSize() = 9
		// rgEntries.size() = 1
		// columnNameVector.size() = 9
        // group_column_index = 0 group_column_name = guid data = TBIZH491VR0
		// group_column_index = 1 group_column_name = subjectidnum data = 02-1094
		// group_column_index = 2 group_column_name = ageyrs data = 26
		// group_column_index = 3 group_column_name = vitstatus data = null
		// group_column_index = 4 group_column_name = visitdate data = 2015-12-15T00:00:00Z
	    // group_column_index = 5 group_column_name = sitename data = 2
	    // group_column_index = 6 group_column_name = dayssincebaseline data = 186
	    // group_column_index = 7 group_column_name = casecontrlind data = null
		// group_column_index = 8 group_column_name = generalnotestxt data = null
		Vector<Vector<String>> columnNameVector2;
        Vector<Vector<String>> dataVector2;
        Vector<String> columnNameVector;
        Vector<String> dataVector;
        int i;
        String message;
        Vector<String> bufColumnNameVector = new Vector<String>();
        Vector<String> bufDataVector = new Vector<String>();
        Vector<Integer> bufRepeatable_group_id = new Vector<Integer>();
        Vector<Integer> bufGroup_row_index = new Vector<Integer>();
        Vector<Integer> bufIndex = new Vector<Integer>();
        String guid = null;
        int group_row_index;
        int repeatable_group_id;
        RepeatableGroupTable rgTable;
        ArrayList<Integer> rgEntries;
        int index = -1;
        String data;
        
		 //System.out.println("table.getColumnCount = " + table.getColumnCount());
        	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
        		 
        		//System.out.println("repeatable_group_id = " + repeatable_group_id);
        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
        		dataVector2 = dataVector3.get(repeatable_group_id);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				//System.out.println("rgTable.getSize() = " + rgTable.getSize());
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				//System.out.println("rgEntries.size() = " + rgEntries.size());
				for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
        	 //System.out.println("group_row_index = " + group_row_index);
			columnNameVector = columnNameVector2.get(group_row_index);
			dataVector = dataVector2.get(group_row_index);
			 //System.out.println("columnNameVector.size() = " + columnNameVector.size());
			 //for (i = 0; i < columnNameVector.size(); i++) {
			    //System.out.println("group_column_index = " + i +
			    	//" group_column_name = " + columnNameVector.get(i) +
			    	//" data = " + dataVector.get(i)) ;	
			 //}
			for (i = 0; i < columnNameVector.size(); i++) {
				bufColumnNameVector.add(columnNameVector.get(i));
				bufDataVector.add(dataVector.get(i));
				bufRepeatable_group_id.add(repeatable_group_id);
				bufGroup_row_index.add(group_row_index);
				bufIndex.add(i);
			}
			} // for (int group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++)
            } // for (int repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++)
        for (i = 0; i < bufColumnNameVector.size(); i++) {
        	if (bufColumnNameVector.get(i).equalsIgnoreCase("guid")) {
				guid = bufDataVector.get(i);
				break;
			}	
        } // for (i = 0; i < bufColumnNameVector.size(); i++)
        
	    
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("ageyrs")) {
				String ageData = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((ageData == null) || (ageData.isEmpty()) || (ageData.trim() == null) ||
						(ageData.trim().isEmpty())) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
				}	
				else {
					double age = Double.valueOf(ageData).doubleValue();
					if (age < 18) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.WARNING_INCORRECT_AGE,
										  ageData, "18 or more years");
						table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
					}
				}
				break;
			  } // if (bufColumnNameVector.get(i).equalsIgnoreCase("ageyrs"))
			} // for (i = 0; i < bufColumnNameVector.size(); i++)
			
			int numberScoresMissing = 0;
		    Vector<Integer>missingIndices = new Vector<Integer>();
		    int missing_repeatable_group_id = -1;
		    int missing_group_row_index = -1;
		    int value;
		    int numberScoresNotInteger = 0;
		    Vector<Integer>notIntegerIndices = new Vector<Integer>();
		    int notInteger_repeatable_group_id = -1;
		    int notInteger_group_row_index = -1;
		    Vector<String>notIntegerData = new Vector<String>();
		    
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
				if ((bufColumnNameVector.get(i).equalsIgnoreCase("sf36genhlthscore")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36hlthlmtmodactscore")) ||	
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36hlthlmtclmbsvrlstairscore")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf12lesaccompphyhlthscale")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf12lmtwrkactphyhlthscale")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36paininterfwrkscore"))) {
					data = bufDataVector.get(i);
					if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
					    numberScoresMissing++;
					    missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
					}
					else {
						try {
						    value = Integer.valueOf(data).intValue();
						}
						catch (NumberFormatException e) {
							numberScoresNotInteger++;
						    notIntegerIndices.add(bufIndex.get(i));
						    notInteger_repeatable_group_id = bufRepeatable_group_id.get(i);
							notInteger_group_row_index = bufGroup_row_index.get(i);	
							notIntegerData.add(data);
						}
					}
				}
			}
		    
		    if (numberScoresMissing >= 1) {
		    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						missing_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresMissing; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, missing_group_row_index),
									rgTable.getDataFilePositionMapping(missingIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(missingIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					message = message + " for the PHYSICAL HEALTH DOMAIN";
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index),
							rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
		    	}
		    } // if (numberScoresMissing >= 1)	
		    
		    if (numberScoresNotInteger >= 1) {
		    	rgTable = table.getRepeatableGroupTable(notInteger_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						notInteger_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresNotInteger; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, notInteger_group_row_index),
									rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(notIntegerIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
											notIntegerData.get(i));
					message = message + " for the PHYSICAL HEALTH DOMAIN";
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, notInteger_group_row_index),
							rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)), message));
		    	}	
		    } // if (numberScoresNotInteger >= 1)
		    
		    numberScoresMissing = 0;
		    missingIndices.clear();
		    numberScoresNotInteger = 0;
		    notIntegerIndices.clear();
		    notIntegerData.clear();
		    
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
				if ((bufColumnNameVector.get(i).equalsIgnoreCase("sf12lesaccompemotprobscale")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf12wknotcarefulemotprobscale")) ||	
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf12timescalmpcfulscale")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf12timesenrgyscale")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf12timesdowndepressedscale")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf12interfsocphyemotscale"))) {
					data = bufDataVector.get(i);
					if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
					    numberScoresMissing++;
					    missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
					}
					else {
						try {
						    value = Integer.valueOf(data).intValue();
						}
						catch (NumberFormatException e) {
							numberScoresNotInteger++;
						    notIntegerIndices.add(bufIndex.get(i));
						    notInteger_repeatable_group_id = bufRepeatable_group_id.get(i);
							notInteger_group_row_index = bufGroup_row_index.get(i);	
							notIntegerData.add(data);
						}
					}
				}
			}
		    
		    if ((numberScoresMissing + numberScoresNotInteger) >= 6) {
		    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						missing_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresMissing; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, missing_group_row_index),
									rgTable.getDataFilePositionMapping(missingIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(missingIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					message = message + " for the MENTAL HEALTH DOMAIN";
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index),
							rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
		    	}
		    } // if ((numberScoresMissing + numberScoresNotInteger) >= 6)
		    
		    if (numberScoresNotInteger >= 1) {
		    	rgTable = table.getRepeatableGroupTable(notInteger_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						notInteger_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresNotInteger; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, notInteger_group_row_index),
									rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(notIntegerIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
											notIntegerData.get(i));
					message = message + " for the MENTAL HEALTH DOMAIN";
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, notInteger_group_row_index),
							rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)), message));
		    	}	
		    } // if (numberScoresNotInteger >= 1)
	}
	
	private void validateSF36v2(StructuralFormStructure structure,
			Vector<Vector<Vector<String>>> columnNameVector3, 
			Vector<Vector<Vector<String>>>dataVector3, int subject_row_id,
			DataStructureTable table) throws Exception {
		// shortName = SF36v2
		// table.getColumnCount = 10
		// repeatable_group_id = 0
		// rgEntries.size() = 0
		// repeatable_group_id = 1
		// rgEntries.size() = 1
		// group_row_index = 0
		// columnNameVector.size() = 8
		// group_column_index = 0 group_column_name = sf36v2emotlwellbeingtscore data = 30.29580357
		// group_column_index = 1 group_column_name = sf36v2enrgyfatiguetscore data = 36.48094357
		// group_column_index = 2 group_column_name = sf36v2genhlthtscore data = 30.52946367
		// group_column_index = 3 group_column_name = sf36v2paintscore data = 37.18410852
		// group_column_index = 4 group_column_name = sf36v2physfuncttscore data = 27.57004869
		// group_column_index = 5 group_column_name = sf36v2rolelimitemotltscore data = 17.0065355
		// group_column_index = 6 group_column_name = sf36v2rolelimitphystscore data = 25.01608917
		// group_column_index = 7 group_column_name = sf36v2socialfuncttscore data = 29.57935723
		// repeatable_group_id = 2
		// rgEntries.size() = 0
		// repeatable_group_id = 3
		// rgEntries.size() = 1
		// group_row_index = 0
		// columnNameVector.size() = 6
		// group_column_index = 0 group_column_name = langcrfadministratisocode data = eng
		// group_column_index = 1 group_column_name = langcrfadministratisocodeoth data = null
		// group_column_index = 2 group_column_name = contexttype data = At time of assessment
		// group_column_index = 3 group_column_name = contexttypeoth data = null
		// group_column_index = 4 group_column_name = datasource data = Participant/subject
		// group_column_index = 5 group_column_name = datasourceoth data = null
		// repeatable_group_id = 4
		// rgEntries.size() = 1
		// group_row_index = 0
		// columnNameVector.size() = 36
		// group_column_index = 0 group_column_name = sf36genhlthscore data = 5
		// group_column_index = 1 group_column_name = sf36nowhlthscore data = 4
		// group_column_index = 2 group_column_name = sf36hlthlmtvigoractscore data = 1
		// group_column_index = 3 group_column_name = sf36hlthlmtmodactscore data = 2
		// group_column_index = 4 group_column_name = sf36hlthlmtcrrygrocryscore data = 2
		// group_column_index = 5 group_column_name = sf36hlthlmtclmbsvrlstairscore data = 1
		// group_column_index = 6 group_column_name = sf36hlthlmtclmb1stairscore data = 2
		// group_column_index = 7 group_column_name = sf36hlthlmtbndknlstpscore data = 1
		// group_column_index = 8 group_column_name = sf36hlthlmtwlksvrlmilscore data = 1
		// group_column_index = 9 group_column_name = sf36hlthlmtwlksvrlblckscore data = 2
		// group_column_index = 10 group_column_name = sf36hlthlmtwlk1blckscore data = 2
		// group_column_index = 11 group_column_name = sf36hlthlimtbthdrssscore data = 2
		// group_column_index = 12 group_column_name = sf36v2cuttimewrkactphyhlthscl data = 2
		// group_column_index = 13 group_column_name = sf36v2lesaccompphyhlthscl data = 3
		// group_column_index = 14 group_column_name = sf36v2lmtwrkactphyhlthscl data = 1
		// group_column_index = 15 group_column_name = sf36v2diffcltwrkactphyhlthscl data = 1
		// group_column_index = 16 group_column_name = sf36v2cuttmewrkactemotprobscl data = 2
		// group_column_index = 17 group_column_name = sf36v2lesaccompemotprobscl data = 2
		// group_column_index = 18 group_column_name = sf36v2wknotcarefulemotprobscl data = 1
		// group_column_index = 19 group_column_name = sf36interfsocphyemotscore data = 4
		// group_column_index = 20 group_column_name = sf36bodypainscore data = 4
		// group_column_index = 21 group_column_name = sf36paininterfwrkscore data = 3
		// group_column_index = 22 group_column_name = sf36v2timespepscl data = 4
		// group_column_index = 23 group_column_name = sf36v2timesnervsscl data = 3
		// group_column_index = 24 group_column_name = sf36v2timesdownscl data = 3
		// group_column_index = 25 group_column_name = sf36v2timescalmpcfulscl data = 4
		// group_column_index = 26 group_column_name = sf36v2timesenrgyscl data = 4
		// group_column_index = 27 group_column_name = sf36v2timesdownbluescl data = 2
		// group_column_index = 28 group_column_name = sf36v2timeswornoutscl data = 2
		// group_column_index = 29 group_column_name = sf36v2timeshppyscl data = 3
		// group_column_index = 30 group_column_name = sf36v2timestiredscl data = 3
		// group_column_index = 31 group_column_name = sf36v2timesintersocphyemtscl data = 3
		// group_column_index = 32 group_column_name = sf36sckeasierscore data = 4
		// group_column_index = 33 group_column_name = sf36hlthyasanybodyscore data = 5
		// group_column_index = 34 group_column_name = sf36expworshlthscore data = 3
		// group_column_index = 35 group_column_name = sf36excelhlthscore data = 4
		// repeatable_group_id = 5
		// rgEntries.size() = 1
		// group_row_index = 0
		// columnNameVector.size() = 9
		// group_column_index = 0 group_column_name = guid data = TBIDE982KEA
		// group_column_index = 1 group_column_name = subjectidnum data = 49
		// group_column_index = 2 group_column_name = ageyrs data = null
		// group_column_index = 3 group_column_name = vitstatus data = Alive
		// group_column_index = 4 group_column_name = visitdate data = 2013-04-03T00:00:00Z
		// group_column_index = 5 group_column_name = sitename data = University of Washington
		// group_column_index = 6 group_column_name = dayssincebaseline data = 30
		// group_column_index = 7 group_column_name = casecontrlind data = Control
		// group_column_index = 8 group_column_name = generalnotestxt data = age not submitted
		// repeatable_group_id = 6
		// rgEntries.size() = 1
		// group_row_index = 0
		// columnNameVector.size() = 9
		// group_column_index = 0 group_column_name = sf36v2physfuncttotalscore data = 30
		// group_column_index = 1 group_column_name = sf36v2rolelimitphystotalscore data = 18.75
		// group_column_index = 2 group_column_name = sf36v2paintotalscore data = 41
		// group_column_index = 3 group_column_name = sf36v2genhlthtotalscore data = 30
		// group_column_index = 4 group_column_name = sf36v2enrgyfatiguetotalscore data = 31.25
		// group_column_index = 5 group_column_name = sf36v2emotlwellbeingtotalscore data = 40
		// group_column_index = 6 group_column_name = sf36v2socialfuncttotalscore data = 37.5
		// group_column_index = 7 group_column_name = sf36v2rolelimitemotltotalscore data = 16.66666667
		// group_column_index = 8 group_column_name = sf36v2rephealthtranstotalscore data = null
		// repeatable_group_id = 7
		// rgEntries.size() = 1
		// group_row_index = 0
		// columnNameVector.size() = 8
		// group_column_index = 0 group_column_name = sf36v2emotlwellbeingzscore data = -1.970419643
		// group_column_index = 1 group_column_name = sf36v2enrgyfatiguezscore data = -1.351905643
		// group_column_index = 2 group_column_name = sf36v2genhlthzscore data = -1.947053633
		// group_column_index = 3 group_column_name = sf36v2painzscore data = -1.281589148
		// group_column_index = 4 group_column_name = sf36v2physfunctzscore data = -2.242995131
		// group_column_index = 5 group_column_name = sf36v2rolelimitemotlzscore data = -3.29934645
		// group_column_index = 6 group_column_name = sf36v2rolelimitphyszscore data = -2.498391083
		// group_column_index = 7 group_column_name = sf36v2socialfunctzscore data = -2.042064277
		// repeatable_group_id = 8
		// rgEntries.size() = 1
		// group_row_index = 0
		// columnNameVector.size() = 4
		// group_column_index = 0 group_column_name = sf36v2pfcaggregatedscore data = -1.676301533
		// group_column_index = 1 group_column_name = sf36v2mcsaggregatedscore data = -2.27718681
		// group_column_index = 2 group_column_name = sf36v2pfctscore data = null
		// group_column_index = 3 group_column_name = sf36v2mcstscore data = null
		// repeatable_group_id = 9
		// rgEntries.size() = 0
		
		Vector<Vector<String>> columnNameVector2;
        Vector<Vector<String>> dataVector2;
        Vector<String> columnNameVector;
        Vector<String> dataVector;
        int i;
        String message;
        Vector<String> bufColumnNameVector = new Vector<String>();
        Vector<String> bufDataVector = new Vector<String>();
        Vector<Integer> bufRepeatable_group_id = new Vector<Integer>();
        Vector<Integer> bufGroup_row_index = new Vector<Integer>();
        Vector<Integer> bufIndex = new Vector<Integer>();
        String guid = null;
        int group_row_index;
        int repeatable_group_id;
        RepeatableGroupTable rgTable;
        ArrayList<Integer> rgEntries;
        int index = -1;
        String data;
        
		 //System.out.println("table.getColumnCount = " + table.getColumnCount());
        	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
        		 
        		//System.out.println("repeatable_group_id = " + repeatable_group_id);
        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
        		dataVector2 = dataVector3.get(repeatable_group_id);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				//System.out.println("rgTable.getSize() = " + rgTable.getSize());
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				//System.out.println("rgEntries.size() = " + rgEntries.size());
				for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
        	 //System.out.println("group_row_index = " + group_row_index);
			columnNameVector = columnNameVector2.get(group_row_index);
			dataVector = dataVector2.get(group_row_index);
			 //System.out.println("columnNameVector.size() = " + columnNameVector.size());
			 //for (i = 0; i < columnNameVector.size(); i++) {
			    //System.out.println("group_column_index = " + i +
			    	//" group_column_name = " + columnNameVector.get(i) +
			    	//" data = " + dataVector.get(i)) ;	
			 //}
			for (i = 0; i < columnNameVector.size(); i++) {
				bufColumnNameVector.add(columnNameVector.get(i));
				bufDataVector.add(dataVector.get(i));
				bufRepeatable_group_id.add(repeatable_group_id);
				bufGroup_row_index.add(group_row_index);
				bufIndex.add(i);
			}
			} // for (int group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++)
            } // for (int repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++)
        for (i = 0; i < bufColumnNameVector.size(); i++) {
        	if (bufColumnNameVector.get(i).equalsIgnoreCase("guid")) {
				guid = bufDataVector.get(i);
				break;
			}	
        } // for (i = 0; i < bufColumnNameVector.size(); i++)
        
	    
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("ageyrs")) {
				String ageData = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((ageData == null) || (ageData.isEmpty()) || (ageData.trim() == null) ||
						(ageData.trim().isEmpty())) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
				}	
				else {
					double age = Double.valueOf(ageData).doubleValue();
					if (age < 18) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.WARNING_INCORRECT_AGE,
										  ageData, "18 or more years");
						table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
					}
				}
				break;
			  } // if (bufColumnNameVector.get(i).equalsIgnoreCase("ageyrs"))
			} // for (i = 0; i < bufColumnNameVector.size(); i++)
			
			int numberScoresMissing = 0;
		    Vector<Integer>missingIndices = new Vector<Integer>();
		    int missing_repeatable_group_id = -1;
		    int missing_group_row_index = -1;
		    int value;
		    int numberScoresNotInteger = 0;
		    Vector<Integer>notIntegerIndices = new Vector<Integer>();
		    int notInteger_repeatable_group_id = -1;
		    int notInteger_group_row_index = -1;
		    Vector<String>notIntegerData = new Vector<String>();
		    
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
				if ((bufColumnNameVector.get(i).equalsIgnoreCase("sf36hlthlmtvigoractscore")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36hlthlmtmodactscore")) ||	
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36hlthlmtcrrygrocryscore")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36hlthlmtclmbsvrlstairscore")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36hlthlmtclmb1stairscore")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36hlthlmtbndknlstpscore")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36hlthlmtwlksvrlmilscore")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36hlthlmtwlksvrlblckscore")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36hlthlmtwlk1blckscore"))) {
					data = bufDataVector.get(i);
					if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
					    numberScoresMissing++;
					    missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
					}
					else {
						try {
					        value = Integer.valueOf(data).intValue();
						}
						catch (NumberFormatException e) {
							numberScoresNotInteger++;
						    notIntegerIndices.add(bufIndex.get(i));
						    notInteger_repeatable_group_id = bufRepeatable_group_id.get(i);
							notInteger_group_row_index = bufGroup_row_index.get(i);
	                        notIntegerData.add(data);
						}
					}
				}
			}
		    
		    if (numberScoresMissing >= 1) {
		    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						missing_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresMissing; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, missing_group_row_index),
									rgTable.getDataFilePositionMapping(missingIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(missingIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index),
							rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
		    	}	
		    } // if (numberScoresMissing >= 1)
		    
		    if (numberScoresNotInteger >= 1) {
		    	rgTable = table.getRepeatableGroupTable(notInteger_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						notInteger_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresNotInteger; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, notInteger_group_row_index),
									rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(notIntegerIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
											notIntegerData.get(i));
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, notInteger_group_row_index),
							rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)), message));
		    	}	
		    } // if (numberScoresNotInteger >= 1)
		    
		    numberScoresMissing = 0;
		    missingIndices.clear();
		    missing_repeatable_group_id = -1;
		    missing_group_row_index = -1;
		    numberScoresNotInteger = 0;
		    notIntegerIndices.clear();
		    notInteger_repeatable_group_id = -1;
		    notInteger_group_row_index = -1;
		    notIntegerData.clear();
		    
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
				if ((bufColumnNameVector.get(i).equalsIgnoreCase("sf36genhlthscore")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36nowhlthscore")) ||	
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36sckeasierscore")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36hlthyasanybodyscore")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36expworshlthscore")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36excelhlthscore"))) {
					data = bufDataVector.get(i);
					if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
					    numberScoresMissing++;
					    missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
					}
					else {
						try {
					        value = Integer.valueOf(data).intValue();
						}
						catch (NumberFormatException e) {
							numberScoresNotInteger++;
						    notIntegerIndices.add(bufIndex.get(i));
						    notInteger_repeatable_group_id = bufRepeatable_group_id.get(i);
							notInteger_group_row_index = bufGroup_row_index.get(i);
	                        notIntegerData.add(data);
						}
					}
				}
			}
		    
		    if ((numberScoresMissing + numberScoresNotInteger) >= 6) {
		    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						missing_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresMissing; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, missing_group_row_index),
									rgTable.getDataFilePositionMapping(missingIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(missingIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND)
					+ " in the General Health scale";
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index),
							rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
		    	}
		    } // if ((numberScoresMissing + numberScoresNotInteger) >= 6)
		    
		    if (numberScoresNotInteger >= 1) {
		    	rgTable = table.getRepeatableGroupTable(notInteger_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						notInteger_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresNotInteger; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, notInteger_group_row_index),
									rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(notIntegerIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
											notIntegerData.get(i))
					+ " in the General Health scale";
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, notInteger_group_row_index),
							rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)), message));
		    	}	
		    } // if (numberScoresNotInteger >= 1)
		    
		    numberScoresMissing = 0;
		    missingIndices.clear();
		    missing_repeatable_group_id = -1;
		    missing_group_row_index = -1;
		    numberScoresNotInteger = 0;
		    notIntegerIndices.clear();
		    notInteger_repeatable_group_id = -1;
		    notInteger_group_row_index = -1;
		    notIntegerData.clear();
		    
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
				if ((bufColumnNameVector.get(i).equalsIgnoreCase("sf36v2cuttimewrkactphyhlthscl")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36v2lesaccompphyhlthscl")) ||	
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36v2lmtwrkactphyhlthscl")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36v2diffcltwrkactphyhlthscl"))) {
					data = bufDataVector.get(i);
					if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
					    numberScoresMissing++;
					    missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
					}
					else {
						try {
					        value = Integer.valueOf(data).intValue();
						}
						catch (NumberFormatException e) {
							numberScoresNotInteger++;
						    notIntegerIndices.add(bufIndex.get(i));
						    notInteger_repeatable_group_id = bufRepeatable_group_id.get(i);
							notInteger_group_row_index = bufGroup_row_index.get(i);
	                        notIntegerData.add(data);
						}
					}
				}
			}
		    
		    if ((numberScoresMissing + numberScoresNotInteger) >= 4) {
		    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						missing_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresMissing; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, missing_group_row_index),
									rgTable.getDataFilePositionMapping(missingIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(missingIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND)
					+ " in the Role-Physical (RP) scale";
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index),
							rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
		    	}
		    } // if ((numberScoresMissing + numberScoresNotInteger) >= 4)
		    
		    if (numberScoresNotInteger >= 1) {
		    	rgTable = table.getRepeatableGroupTable(notInteger_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						notInteger_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresNotInteger; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, notInteger_group_row_index),
									rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(notIntegerIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
											notIntegerData.get(i))
					+ " in the Role-Physical (RP) scale";
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, notInteger_group_row_index),
							rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)), message));
		    	}	
		    } // if (numberScoresNotInteger >= 1)
		    
		    numberScoresMissing = 0;
		    missingIndices.clear();
		    missing_repeatable_group_id = -1;
		    missing_group_row_index = -1;
		    numberScoresNotInteger = 0;
		    notIntegerIndices.clear();
		    notInteger_repeatable_group_id = -1;
		    notInteger_group_row_index = -1;
		    notIntegerData.clear();
		    
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
				if ((bufColumnNameVector.get(i).equalsIgnoreCase("sf36v2cuttmewrkactemotprobscl")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36v2lesaccompemotprobscl")) ||	
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36v2wknotcarefulemotprobscl"))) {
					data = bufDataVector.get(i);
					if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
					    numberScoresMissing++;
					    missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
					}
					else {
						try {
					        value = Integer.valueOf(data).intValue();
						}
						catch (NumberFormatException e) {
							numberScoresNotInteger++;
						    notIntegerIndices.add(bufIndex.get(i));
						    notInteger_repeatable_group_id = bufRepeatable_group_id.get(i);
							notInteger_group_row_index = bufGroup_row_index.get(i);
	                        notIntegerData.add(data);
						}
					}
				}
			}
		    
		    if ((numberScoresMissing + numberScoresNotInteger) >= 3) {
		    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						missing_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresMissing; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, missing_group_row_index),
									rgTable.getDataFilePositionMapping(missingIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(missingIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND)
					+ " in the Role-Emotional (RE) scale";
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index),
							rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
		    	}
		    } // if ((numberScoresMissing + numberScoresNotInteger) >= 3)
		    
		    if (numberScoresNotInteger >= 1) {
		    	rgTable = table.getRepeatableGroupTable(notInteger_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						notInteger_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresNotInteger; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, notInteger_group_row_index),
									rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(notIntegerIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
											notIntegerData.get(i))
					+ " in the Role-Emotional (RE) scale";
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, notInteger_group_row_index),
							rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)), message));
		    	}	
		    } // if (numberScoresNotInteger >= 1)
		    
		    numberScoresMissing = 0;
		    missingIndices.clear();
		    missing_repeatable_group_id = -1;
		    missing_group_row_index = -1;
		    numberScoresNotInteger = 0;
		    notIntegerIndices.clear();
		    notInteger_repeatable_group_id = -1;
		    notInteger_group_row_index = -1;
		    notIntegerData.clear();
		    
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
				if ((bufColumnNameVector.get(i).equalsIgnoreCase("sf36interfsocphyemotscore")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36v2timesintersocphyemtscl"))) {
					data = bufDataVector.get(i);
					if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
					    numberScoresMissing++;
					    missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
					}
					else {
						try {
					        value = Integer.valueOf(data).intValue();
						}
						catch (NumberFormatException e) {
							numberScoresNotInteger++;
						    notIntegerIndices.add(bufIndex.get(i));
						    notInteger_repeatable_group_id = bufRepeatable_group_id.get(i);
							notInteger_group_row_index = bufGroup_row_index.get(i);
	                        notIntegerData.add(data);
						}
					}
				}
			}
		    
		    if ((numberScoresMissing + numberScoresNotInteger) >= 2) {
		    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						missing_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresMissing; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, missing_group_row_index),
									rgTable.getDataFilePositionMapping(missingIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(missingIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND)
					+ " in the Social Functioning (SF) scale";
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index),
							rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
		    	}	
		    } // if ((numberScoresMissing + numberScoresNotInteger) >= 2)
		    
		    if (numberScoresNotInteger >= 1) {
		    	rgTable = table.getRepeatableGroupTable(notInteger_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						notInteger_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresNotInteger; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, notInteger_group_row_index),
									rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(notIntegerIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
											notIntegerData.get(i))
					+ " in the Social Functioning (SF) scale";
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, notInteger_group_row_index),
							rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)), message));
		    	}		
		    } // if (numberScoresNotInteger >= 1)
		    
		    numberScoresMissing = 0;
		    missingIndices.clear();
		    missing_repeatable_group_id = -1;
		    missing_group_row_index = -1;
		    numberScoresNotInteger = 0;
		    notIntegerIndices.clear();
		    notInteger_repeatable_group_id = -1;
		    notInteger_group_row_index = -1;
		    notIntegerData.clear();
		    
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
				if ((bufColumnNameVector.get(i).equalsIgnoreCase("sf36bodypainscore")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36paininterfwrkscore"))) {
					data = bufDataVector.get(i);
					if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
					    numberScoresMissing++;
					    missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
					}
					else {
						try {
					        value = Integer.valueOf(data).intValue();
						}
						catch (NumberFormatException e) {
							numberScoresNotInteger++;
						    notIntegerIndices.add(bufIndex.get(i));
						    notInteger_repeatable_group_id = bufRepeatable_group_id.get(i);
							notInteger_group_row_index = bufGroup_row_index.get(i);
	                        notIntegerData.add(data);
						}
					}
				}
			}
		    
		    if ((numberScoresMissing + numberScoresNotInteger) >= 2) {
		    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						missing_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresMissing; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, missing_group_row_index),
									rgTable.getDataFilePositionMapping(missingIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(missingIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND)
					+ " in the Pain scale";
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index),
							rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
		    	}
		    } // if ((numberScoresMissing + numberScoresNotInteger) >= 2)
		    
		    if (numberScoresNotInteger >= 1) {
		    	rgTable = table.getRepeatableGroupTable(notInteger_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						notInteger_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresNotInteger; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, notInteger_group_row_index),
									rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(notIntegerIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
											notIntegerData.get(i))
					+ " in the Pain scale";
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, notInteger_group_row_index),
							rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)), message));
		    	}	
		    } // if (numberScoresNotInteger >= 1)
		    
		    numberScoresMissing = 0;
		    missingIndices.clear();
		    missing_repeatable_group_id = -1;
		    missing_group_row_index = -1;
		    numberScoresNotInteger = 0;
		    notIntegerIndices.clear();
		    notInteger_repeatable_group_id = -1;
		    notInteger_group_row_index = -1;
		    notIntegerData.clear();
		    
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
				if ((bufColumnNameVector.get(i).equalsIgnoreCase("sf36v2timesnervsscl")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36v2timesdownscl")) ||	
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36v2timescalmpcfulscl")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36v2timesdownbluescl")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36v2timeshppyscl"))) {
					data = bufDataVector.get(i);
					if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
					    numberScoresMissing++;
					    missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
					}
					else {
						try {
					        value = Integer.valueOf(data).intValue();
						}
						catch (NumberFormatException e) {
							numberScoresNotInteger++;
						    notIntegerIndices.add(bufIndex.get(i));
						    notInteger_repeatable_group_id = bufRepeatable_group_id.get(i);
							notInteger_group_row_index = bufGroup_row_index.get(i);
	                        notIntegerData.add(data);
						}
					}
				}
			}
		    
		    if ((numberScoresMissing + numberScoresNotInteger) >= 5) {
		    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						missing_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresMissing; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, missing_group_row_index),
									rgTable.getDataFilePositionMapping(missingIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(missingIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND)
					+ " in Mental Health";
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index),
							rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
		    	}
		    } // if ((numberScoresMissing + numberScoresNotInteger) >= 5)
		    
		    if (numberScoresNotInteger >= 1) {
		    	rgTable = table.getRepeatableGroupTable(notInteger_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						notInteger_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresNotInteger; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, notInteger_group_row_index),
									rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(notIntegerIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
											notIntegerData.get(i))
					+ " in Mental Health";
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, notInteger_group_row_index),
							rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)), message));
		    	}	
		    } // if (numberScoresNotInteger >= 1)
		    
		    numberScoresMissing = 0;
		    missingIndices.clear();
		    missing_repeatable_group_id = -1;
		    missing_group_row_index = -1;
		    numberScoresNotInteger = 0;
		    notIntegerIndices.clear();
		    notInteger_repeatable_group_id = -1;
		    notInteger_group_row_index = -1;
		    notIntegerData.clear();
		    
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
				if ((bufColumnNameVector.get(i).equalsIgnoreCase("sf36v2timespepscl")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36v2timesenrgyscl")) ||	
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36v2timeswornoutscl")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("sf36v2timestiredscl"))) {
					data = bufDataVector.get(i);
					if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
					    numberScoresMissing++;
					    missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
					}
					else {
						try {
					        value = Integer.valueOf(data).intValue();
						}
						catch (NumberFormatException e) {
							numberScoresNotInteger++;
						    notIntegerIndices.add(bufIndex.get(i));
						    notInteger_repeatable_group_id = bufRepeatable_group_id.get(i);
							notInteger_group_row_index = bufGroup_row_index.get(i);
	                        notIntegerData.add(data);
						}
					}
				}
			}
		    
		    if ((numberScoresMissing + numberScoresNotInteger) >= 4) {
		    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						missing_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresMissing; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, missing_group_row_index),
									rgTable.getDataFilePositionMapping(missingIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(missingIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND)
					+ " in the Vitality (VT) Scale";
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index),
							rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
		    	}
		    } // if ((numberScoresMissing + numberScoresNotInteger) >= 4)
		    
		    if (numberScoresNotInteger >= 1) {
		    	rgTable = table.getRepeatableGroupTable(notInteger_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						notInteger_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresNotInteger; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, notInteger_group_row_index),
									rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(notIntegerIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_NOT_INTEGER,
											notIntegerData.get(i))
					+ " in the Vitality (VT) Scale";
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, notInteger_group_row_index),
							rgTable.getDataFilePositionMapping(notIntegerIndices.get(i)), message));
		    	}	
		    } // if (numberScoresNotInteger >= 1)
		    
		    boolean haveLicense = false;
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
				if (bufColumnNameVector.get(i).equalsIgnoreCase("sf36v2licencetxt")) {
					haveLicense = true;
					data = bufDataVector.get(i);
					repeatable_group_id = bufRepeatable_group_id.get(i);
					group_row_index = bufGroup_row_index.get(i);
					index = bufIndex.get(i);
					rgTable = table.getRepeatableGroupTable(repeatable_group_id);
					rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
					if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
					}		
				}
		    }
		    
		    if (!haveLicense) {
	
		    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
		    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
		    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Licence info")) {
		    		    // rgTable.getColumnCount() = 1 for Licence Info
		    			message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, 0),
										rgTable.getDataFilePositionMapping(0),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(0));

						message =
								message
										+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(0), message));
		    			break;
		    		}
		    	}
		    }
	}
	
	private void validateMoCA(StructuralFormStructure structure,
			Vector<Vector<Vector<String>>> columnNameVector3, 
			Vector<Vector<Vector<String>>>dataVector3, int subject_row_id,
			DataStructureTable table) throws Exception {
		    // table.getColumnCount = 3
			// repeatable_group_id = 0
			// rgTable.getSize() = 7
			// rgEntries.size() = 1
			// group_row_index = 0
			// columnNameVector.size() = 7
			// group_column_index = 0 group_column_name = sitename data = SiteName1
			// group_column_index = 1 group_column_name = visittyppdbp data = 24 months
			// group_column_index = 2 group_column_name = visitdate data = 3-Aug-20
			// group_column_index = 3 group_column_name = guid data = TBI_INVABC12XYZ
			// group_column_index = 4 group_column_name = ageyrs data = null
			// group_column_index = 5 group_column_name = ageremaindrmonths data = 6
			// group_column_index = 6 group_column_name = ageval data = #VALUE!
			// repeatable_group_id = 1
			// rgTable.getSize() = 0
			// rgEntries.size() = 0
			// repeatable_group_id = 2
			// rgTable.getSize() = 15
			// rgEntries.size() = 1
			// group_row_index = 0
			// columnNameVector.size() = 15
			// group_column_index = 0 group_column_name = moca_visuospatialexec data = 1
			// group_column_index = 1 group_column_name = moca_naming data = 2
			// group_column_index = 2 group_column_name = moca_digits data = 0
			// group_column_index = 3 group_column_name = moca_letters data = 0
			// group_column_index = 4 group_column_name = moca_serial7 data = 0
			// group_column_index = 5 group_column_name = moca_langrepeat data = 1
			// group_column_index = 6 group_column_name = moca_langfluency data = 0
			// group_column_index = 7 group_column_name = moca_abstraction data = 1
			// group_column_index = 8 group_column_name = moca_delydrecall data = 0
			// group_column_index = 9 group_column_name = moca_delydrecalloptnlcatcue data = null
			// group_column_index = 10 group_column_name = moca_delydrecaloptnlmultchoice data = null
			// group_column_index = 11 group_column_name = moca_orient data = 0
			// group_column_index = 12 group_column_name = moca_imageresponse data = null
			// group_column_index = 13 group_column_name = moca_eduind data = 1
			// group_column_index = 14 group_column_name = moca_total data = 6
		Vector<Vector<String>> columnNameVector2;
        Vector<Vector<String>> dataVector2;
        Vector<String> columnNameVector;
        Vector<String> dataVector;
        int i;
        String message;
        Vector<String> bufColumnNameVector = new Vector<String>();
        Vector<String> bufDataVector = new Vector<String>();
        Vector<Integer> bufRepeatable_group_id = new Vector<Integer>();
        Vector<Integer> bufGroup_row_index = new Vector<Integer>();
        Vector<Integer> bufIndex = new Vector<Integer>();
        String guid = null;
        int group_row_index = -1;
        int repeatable_group_id;
        RepeatableGroupTable rgTable = null;
        ArrayList<Integer> rgEntries = null;
        int index = -1;
        String data;
        
		 //System.out.println("table.getColumnCount = " + table.getColumnCount());
        	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
        		 
        		//System.out.println("repeatable_group_id = " + repeatable_group_id);
        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
        		dataVector2 = dataVector3.get(repeatable_group_id);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				//System.out.println("rgTable.getSize() = " + rgTable.getSize());
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				//System.out.println("rgEntries.size() = " + rgEntries.size());
				for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
        	 //System.out.println("group_row_index = " + group_row_index);
			columnNameVector = columnNameVector2.get(group_row_index);
			dataVector = dataVector2.get(group_row_index);
			 //System.out.println("columnNameVector.size() = " + columnNameVector.size());
			 //for (i = 0; i < columnNameVector.size(); i++) {
			    //System.out.println("group_column_index = " + i +
			    	//" group_column_name = " + columnNameVector.get(i) +
			    	//" data = " + dataVector.get(i)) ;	
			 //}
			for (i = 0; i < columnNameVector.size(); i++) {
				bufColumnNameVector.add(columnNameVector.get(i));
				bufDataVector.add(dataVector.get(i));
				bufRepeatable_group_id.add(repeatable_group_id);
				bufGroup_row_index.add(group_row_index);
				bufIndex.add(i);
			}
			} // for (int group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++)
            } // for (int repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++)
        for (i = 0; i < bufColumnNameVector.size(); i++) {
        	if (bufColumnNameVector.get(i).equalsIgnoreCase("guid")) {
				guid = bufDataVector.get(i);
				break;
			}	
        } // for (i = 0; i < bufColumnNameVector.size(); i++)
        
	    
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("ageyrs")) {
				String ageData = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((ageData == null) || (ageData.isEmpty()) || (ageData.trim() == null) ||
						(ageData.trim().isEmpty())) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
				}	
				else {
					double age = Double.valueOf(ageData).doubleValue();
					if (age < 18) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.WARNING_INCORRECT_AGE,
										  ageData, "18 or more years");
						table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
					}
				}
				break;
			  } // if (bufColumnNameVector.get(i).equalsIgnoreCase("ageyrs"))
			} // for (i = 0; i < bufColumnNameVector.size(); i++)
			
			int numberScoresMissing = 0;
		    Vector<Integer>missingIndices = new Vector<Integer>();
		    int missing_repeatable_group_id = -1;
		    int missing_group_row_index = -1;
		    int calculatedSum = 0;
		    
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
				if ((bufColumnNameVector.get(i).equalsIgnoreCase("moca_visuospatialexec")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("moca_naming")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("moca_digits")) ||	
				   (bufColumnNameVector.get(i).equalsIgnoreCase("moca_letters")) ||	
				   (bufColumnNameVector.get(i).equalsIgnoreCase("moca_serial7")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("moca_langrepeat")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("moca_langfluency")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("moca_abstraction")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("moca_delydrecall")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("moca_orient")) ||
				   (bufColumnNameVector.get(i).equalsIgnoreCase("moca_eduind"))) {
					data = bufDataVector.get(i);
					if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
					    numberScoresMissing++;
					    missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
					}
					else {
					    calculatedSum += Integer.valueOf(data).intValue();	
					}
				}
			}
		    
		    if (numberScoresMissing >= 1) {
		    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						missing_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresMissing; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, missing_group_row_index),
									rgTable.getDataFilePositionMapping(missingIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(missingIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index),
							rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
		    	}	
		    } // if (numberScoresMissing >= 1)
		    
		    int actualSum = -1;
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
				if (bufColumnNameVector.get(i).equalsIgnoreCase("moca_total")) {
					data = bufDataVector.get(i);
					repeatable_group_id = bufRepeatable_group_id.get(i);
					group_row_index = bufGroup_row_index.get(i);
					index = bufIndex.get(i);
					rgTable = table.getRepeatableGroupTable(repeatable_group_id);
					rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
					if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
						if (numberScoresMissing == 0) {
							message =
									String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
											guid,
											getRawDataRow(subject_row_id, group_row_index),
											rgTable.getDataFilePositionMapping(index),
											rgTable.getRepeatableGroupName(),
											rgTable.getColumnName(index));
	
							message =
									message
											+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
							table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index), message));  
						}
					}
					else {
					    actualSum = Integer.valueOf(data).intValue();	
					}
					break;
				}
		    }
		    
		    if (actualSum > -1) {
		    	if (actualSum != calculatedSum) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_INCORRECT_SUM,
											String.valueOf(actualSum), String.valueOf(calculatedSum));
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
		    	}
		    	else if (numberScoresMissing == 1) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_MISSING_REQUIRED_VALUE);
					table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));	
		    	}
		    	else if (numberScoresMissing > 1) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_MISSING_REQUIRED_VALUES,
											numberScoresMissing);
					table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));	
		    	}
		    }
	}

	private void validateTMT(StructuralFormStructure structure,
			Vector<Vector<Vector<String>>> columnNameVector3, 
			Vector<Vector<Vector<String>>>dataVector3, int subject_row_id,
			DataStructureTable table) throws Exception {
		// Always see 1 of 3 repeatable groups is empty   
		//table.getColumnCount = 3
		//repeatable_group_id = 0
		//rgEntries.size() = 1
		//group_row_index = 0
		//columnNameVector.size() = 8
		//group_column_index = 0 group_column_name = guid data = TBI_INVCE754KHJ
		//group_column_index = 1 group_column_name = subjectidnum data = MH13
		//group_column_index = 2 group_column_name = ageyrs data = 53
		//group_column_index = 3 group_column_name = visitdate data = 2016-03-29T00:00:00Z
		//group_column_index = 4 group_column_name = sitename data = null
		//group_column_index = 5 group_column_name = dayssincebaseline data = 0
		//group_column_index = 6 group_column_name = casecontrlind data = Control
		//group_column_index = 7 group_column_name = generalnotestxt data = Healthy Control
		//repeatable_group_id = 1
		//rgEntries.size() = 1
		//group_row_index = 0
		//columnNameVector.size() = 4
		//group_column_index = 0 group_column_name = tmtpartatime data = 16
		//group_column_index = 1 group_column_name = tmtpartaerrors data = 0
		//group_column_index = 2 group_column_name = tmtpartbtime data = 56
		//group_column_index = 3 group_column_name = tmtpartberrors data = 1
		//repeatable_group_id = 2
		//rgEntries.size() = 0
		Vector<Vector<String>> columnNameVector2;
        Vector<Vector<String>> dataVector2;
        Vector<String> columnNameVector;
        Vector<String> dataVector;
        int i;
        String message;
        String data;
        Vector<String> bufColumnNameVector = new Vector<String>();
        Vector<String> bufDataVector = new Vector<String>();
        Vector<Integer> bufRepeatable_group_id = new Vector<Integer>();
        Vector<Integer> bufGroup_row_index = new Vector<Integer>();
        Vector<Integer> bufIndex = new Vector<Integer>();
        String guid = null;
        int group_row_index = -1;
        int repeatable_group_id;
        RepeatableGroupTable rgTable = null;
        ArrayList<Integer> rgEntries = null;
        int index = -1;
        
		 //System.out.println("table.getColumnCount = " + table.getColumnCount());
        	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
        		 
        		//System.out.println("repeatable_group_id = " + repeatable_group_id);
        		columnNameVector2 = columnNameVector3.get(repeatable_group_id);
        		dataVector2 = dataVector3.get(repeatable_group_id);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				//System.out.println("rgTable.getSize() = " + rgTable.getSize());
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				//System.out.println("rgEntries.size() = " + rgEntries.size());
				for (group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++) {
        	 //System.out.println("group_row_index = " + group_row_index);
			columnNameVector = columnNameVector2.get(group_row_index);
			dataVector = dataVector2.get(group_row_index);
			 //System.out.println("columnNameVector.size() = " + columnNameVector.size());
			 //for (i = 0; i < columnNameVector.size(); i++) {
			    //System.out.println("group_column_index = " + i +
			    	//" group_column_name = " + columnNameVector.get(i) +
			    	//" data = " + dataVector.get(i)) ;	
			 //}
			for (i = 0; i < columnNameVector.size(); i++) {
				bufColumnNameVector.add(columnNameVector.get(i));
				bufDataVector.add(dataVector.get(i));
				bufRepeatable_group_id.add(repeatable_group_id);
				bufGroup_row_index.add(group_row_index);
				bufIndex.add(i);
			}
			} // for (int group_row_index = 0; group_row_index < rgEntries.size(); group_row_index++)
            } // for (int repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++)
        for (i = 0; i < bufColumnNameVector.size(); i++) {
        	if (bufColumnNameVector.get(i).equalsIgnoreCase("guid")) {
				guid = bufDataVector.get(i);
				break;
			}	
        } // for (i = 0; i < bufColumnNameVector.size(); i++)
        
	    
	    for (i = 0; i < bufColumnNameVector.size(); i++) {
			if (bufColumnNameVector.get(i).equalsIgnoreCase("ageyrs")) {
				String ageData = bufDataVector.get(i);
				repeatable_group_id = bufRepeatable_group_id.get(i);
				group_row_index = bufGroup_row_index.get(i);
				index = bufIndex.get(i);
				rgTable = table.getRepeatableGroupTable(repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, repeatable_group_id, null);
				if ((ageData == null) || (ageData.isEmpty()) || (ageData.trim() == null) ||
						(ageData.trim().isEmpty())) {
					message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, group_row_index),
									rgTable.getDataFilePositionMapping(index),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(index));

					message =
							message
									+ String.format(ApplicationsConstants.WARNING_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
							rgTable.getDataFilePositionMapping(index), message));
				}	
				else {
					double age = Double.valueOf(ageData).doubleValue();
					if ((age < 15) || (age > 89)) {
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, group_row_index),
										rgTable.getDataFilePositionMapping(index),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(index));

						message =
								message
										+ String.format(ApplicationsConstants.WARNING_INCORRECT_AGE,
										  ageData, "15-89 years");
						table.addOutput(new ValidationOutput(rgTable, OutputType.WARNING, getRawDataRow(subject_row_id, group_row_index),
								rgTable.getDataFilePositionMapping(index), message));
					}
				}
				break;
			  } // if (bufColumnNameVector.get(i).equalsIgnoreCase("ageyrs"))
			} // for (i = 0; i < bufColumnNameVector.size(); i++)
		
		    int numberScoresMissing = 0;
		    Vector<Integer>missingIndices = new Vector<Integer>();
		    int missing_repeatable_group_id = -1;
		    int missing_group_row_index = -1;
		    boolean havePartTime = false;
		    for (i = 0; i < bufColumnNameVector.size(); i++) {
		    	if (bufColumnNameVector.get(i).equalsIgnoreCase("TMTPartATime") ||
		    		bufColumnNameVector.get(i).equalsIgnoreCase("TMTPartBTime")) {
		    		havePartTime = true;
		    		data = bufDataVector.get(i);
			    	if ((data == null) || (data.isEmpty()) || (data.trim() == null) ||
							(data.trim().isEmpty())) {
			    		numberScoresMissing++;
			    		missingIndices.add(bufIndex.get(i));
					    missing_repeatable_group_id = bufRepeatable_group_id.get(i);
						missing_group_row_index = bufGroup_row_index.get(i);
			    	}
		        }
		    }
		    
		    if (numberScoresMissing >= 1) {
		    	rgTable = table.getRepeatableGroupTable(missing_repeatable_group_id);
				rgEntries = table.getAllReferences(subject_row_id, 
						missing_repeatable_group_id, null);
		    	for (i = 0; i < numberScoresMissing; i++) {
		    		message =
							String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
									guid,
									getRawDataRow(subject_row_id, missing_group_row_index),
									rgTable.getDataFilePositionMapping(missingIndices.get(i)),
									rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(missingIndices.get(i)));

					message =
							message
									+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
					table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, missing_group_row_index),
							rgTable.getDataFilePositionMapping(missingIndices.get(i)), message));
		    	}
		    	return;	
		    } // if (numberScoresMissing >= 1)
		    
		   
		    
		    if (!havePartTime) {
	
		    	for (repeatable_group_id = 0; repeatable_group_id < table.getColumnCount(); repeatable_group_id++) {
		    		rgTable = table.getRepeatableGroupTable(repeatable_group_id);
		    		if (rgTable.getRepeatableGroupName().equalsIgnoreCase("Scoring")) {
		    			message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, 0),
										rgTable.getDataFilePositionMapping(0),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(0));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(0), message));
						message =
								String.format(ApplicationsConstants.LOC_GUID_ROW_COLUMN_NAME,
										guid,
										getRawDataRow(subject_row_id, 0),
										rgTable.getDataFilePositionMapping(2),
										rgTable.getRepeatableGroupName(),
										rgTable.getColumnName(2));

						message =
								message
										+ String.format(ApplicationsConstants.ERR_BLANK_FOUND);
						table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, getRawDataRow(subject_row_id, 0), rgTable.getDataFilePositionMapping(2), message));
		    			break;
		    		}
		    	}
		    }
	}

	
	/**
	 * Validates the number of rows in the repeatable group to ensure that it meets the threshold requirements
	 * 
	 * @param subject_row_id (int) - relative row number for subject
	 * @param repeatable_group_id - column-wise position of repeatable group
	 * @param rg (RepeatableGroup) - repeatable group
	 * @param rgEntries (ArrayList) - list of rows in the repeatable group
	 */
	private void validateRepeatableGroupCount(int subject_row_id, int repeatable_group_id, RepeatableGroup rg,
			ArrayList<Integer> rgEntries) {

		// Validate the count of repeatable groups
		// If there are no data elements in this repeatable group, then we do not want to validate the number of
		// instances. Do NOT throw a warning.
		if (!rg.getThreshold().equals(0) && !rg.getSize().equals(0)) {

			if (RepeatableType.MORETHAN.equals(rg.getType())) {
				if (rgEntries.size() < rg.getThreshold()) {
					String message =
							String.format(ApplicationsConstants.LOC_ENTRY, getRawDataRow(subject_row_id, 0))
									+ String.format(ApplicationsConstants.ERR_MANY_RGS, rg.getThreshold(), rg.getName());
					table.addOutput(new ValidationOutput(table, OutputType.ERROR, subject_row_id, repeatable_group_id,
							message));
				}
			} else {
				if (RepeatableType.LESSTHAN.equals(rg.getType())) {
					if (rgEntries.size() > rg.getThreshold()) {
						String message =
								String.format(ApplicationsConstants.LOC_ENTRY, getRawDataRow(subject_row_id, 0))
										+ String.format(ApplicationsConstants.ERR_FEW_RGS, rg.getThreshold(),
												rg.getName());
						table.addOutput(new ValidationOutput(table, OutputType.ERROR, subject_row_id,
								repeatable_group_id, message));
					}
				} else {
					if (rgEntries.size() != rg.getThreshold().intValue()) {
						String message =
								String.format(ApplicationsConstants.LOC_ENTRY, getRawDataRow(subject_row_id, 0))
										+ String.format(ApplicationsConstants.ERR_EXACTLY_RGS, rg.getThreshold(),
												rg.getName());
						table.addOutput(new ValidationOutput(table, OutputType.ERROR, subject_row_id,
								repeatable_group_id, message));
					}
				}
			}
		}
	}

	/**
	 * This method checks accessions by splitting accessions into chunks of predetermined size. This is to prevent GUID
	 * server timeout errors when checking a large amount of accessions at once.
	 * 
	 * @param accessions
	 * @return
	 */
	private List<Accession> checkAccessions(List<Accession> accessions) {
		List<Accession> checkedAccessions = new ArrayList<Accession>();

		int startIndex = 0;
		int endIndex = Math.min(GUID_CHUNK_SIZE, accessions.size());

		while (startIndex < accessions.size()) {
			checkedAccessions.addAll(accClient.doAccessionsExist(accessions.subList(startIndex, endIndex)));
			startIndex += GUID_CHUNK_SIZE;
			endIndex = Math.min(endIndex += GUID_CHUNK_SIZE, accessions.size());
		}

		return checkedAccessions;
	}

	private void validateAccessions(HashMap<MapElement, List<Accession>> accessions, MapElement element, String name,
			String type, String rgName) {
	
		try {
			logger.debug("Checking accessions");
			List<Accession> accessionsList = checkAccessions(accessions.get(element));
	
			for (int i = 0; i < accessionsList.size(); i++) {
				String accessionValue = accessionsList.get(i).getValue();
				String accessionType = AccessionReturnType.ERROR.name();
				String accessionComment = accessionsList.get(i).getComment();
	
				if (accessionsList.get(i) != null && accessionsList.get(i).getReturnValue() != null) {
					accessionType = accessionsList.get(i).getReturnValue().name();
				}
	
				if (!accessionValue.isEmpty()) {
	
					// TODO: Michael - These errors messages still need to be calculated properly
					if (type.equalsIgnoreCase("GUID")
							&& accessionType.equalsIgnoreCase(AccessionReturnType.PSUEDO_GUID.name())) {
						String message =accessionValue+
								";;;"+
								"Entry is a pseudo-GUID for a subject who has now been assigned the real GUID "
										+ accessionComment + ".; ; ";
						table.addOutput(new ValidationOutput(table, OutputType.WARNING, i, i, message));
						// Translation to the GUID
						// String[] row = table.getRow(i);
						// row[loc] = found;
					} else if (accessionType.contains("ERROR")) {
						String message =accessionValue+
								";;;"+
								"An error occured while validating the GUID entry; ; ";
						table.addOutput(new ValidationOutput(table, OutputType.ERROR, i, i, message));
					} else if (!accessionType.equals("VALID")) {
						String message = accessionValue+
								";;;"+
								"GUID is invalid or doesn't exist, please check the value to ensure it is correct.; ; ";
						table.addOutput(new ValidationOutput(table, OutputType.ERROR, i, i, message));
					}
	
				}
			}
		} catch (WebServiceException e) {
			String message =
					"Element "
							+ name
							+ ", in column "
							+ rgName
							+ " could not be validated due to connectivity problems. Please check your connection and revalidate.";
			table.addOutput(new ValidationOutput(table, OutputType.ERROR, -1, -1, message));
		}
	}

	private void validateInputRestrictions(int i, RepeatableGroupTable rgTable, ArrayList<Integer> rgEntries, int x,
			int y, StructuralDataElement element, String data) {

		String[] dataArr = data.split(";");

		if (InputRestrictions.SINGLE.equals(element.getRestrictions()) && dataArr.length > 1) {
			/*
			 * String message = String.format(ApplicationsConstants.LOC_DATA_ROW_COLUMN_NAME, data, getRawDataRow(i, x),
			 * rgTable.getRepeatableGroupName(), rgTable.getDataFilePositionMapping(y), rgTable.getColumnName(y));
			 */



			String message =
					String.format(ApplicationsConstants.LOC_DATA_ROW_COLUMN_NAME, data, getRawDataRow(i, x),
							rgTable.getDataFilePositionMapping(y), rgTable.getRepeatableGroupName(),
							rgTable.getColumnName(y));
			// message = message + String.format(ApplicationsConstants.ERR_RESTRICT_ONLY_SINGLE);

			table.addOutput(new ValidationOutput(table, OutputType.ERROR, rgEntries.get(x), y, message));
		}
		
		if(InputRestrictions.FREE_FORM.equals(element.getRestrictions()) && data.indexOf(";")!=-1) {

			String message =
					String.format(ApplicationsConstants.LOC_DATA_ROW_COLUMN_NAME, data, getRawDataRow(i, x),
							rgTable.getDataFilePositionMapping(y), rgTable.getRepeatableGroupName(),
							rgTable.getColumnName(y));
			message =
					message
							+ String.format(ApplicationsConstants.ERR_TYPE_INCORRECT,
									rgTable.getElementMapping(y).getStructuralDataElement().getType()
											.getValue());

			table.addOutput(new ValidationOutput(table, OutputType.ERROR, rgEntries.get(x), y, message));
		}

	}

	private void validateBiosample(int i, RepeatableGroupTable rgTable, ArrayList<Integer> rgEntries, int x, int y,
			StructuralDataElement element, String data) {

		String[] dataArr = null;

		if (InputRestrictions.FREE_FORM.equals(element.getRestrictions())) {
			dataArr = new String[1];
			dataArr[0] = data;
		} else {
			dataArr = data.split(ApplicationsConstants.SEMI_COLON);

			// only need to validate if it can be split if it is not a free form.
			validateInputRestrictions(i, rgTable, rgEntries, x, y, element, data);
		}

		for (String value : dataArr) {

			/*
			 * If the Biosample is greater than 9, an error is returned. The length can be changed in the
			 * ServiceConstants.java file.
			 */
			if (value.length() > ServiceConstants.MAX_BIOSAMPLE_LENGTH) {
				String message =
						String.format(ApplicationsConstants.LOC_DATA_ROW_COLUMN_NAME, data, getRawDataRow(i, x),
								rgTable.getDataFilePositionMapping(y), rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(y));

				message =
						message
								+ String.format(ApplicationsConstants.ERR_TYPE_STRING_SIZE,
										ServiceConstants.MAX_BIOSAMPLE_LENGTH);

				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, rgEntries.get(x), y, message));
			}
		}
	}

	private void validateAlphanumeric(int i, RepeatableGroupTable rgTable, ArrayList<Integer> rgEntries, int x, int y,
			StructuralDataElement element, String data) {

		String[] dataArr = null;

		if (InputRestrictions.FREE_FORM.equals(element.getRestrictions())) {
			dataArr = new String[1];
			dataArr[0] = data;
		} else {
			dataArr = data.split(ApplicationsConstants.SEMI_COLON);

			// only need to validate if it can be split if it is not a free form.
			validateInputRestrictions(i, rgTable, rgEntries, x, y, element, data);
		}

		for (String value : dataArr) {

			if (element.getSize() != null && value.length() > element.getSize()) {
				String message =
						String.format(ApplicationsConstants.LOC_DATA_ROW_COLUMN_NAME, data, getRawDataRow(i, x),
								rgTable.getDataFilePositionMapping(y), rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(y));

				message = message + String.format(ApplicationsConstants.ERR_TYPE_STRING_SIZE, element.getSize());

				table.addOutput(new ValidationOutput(rgTable, OutputType.ERROR, rgEntries.get(x), y, message));
			}
		}
	}

	private void validateFile(int i, RepeatableGroupTable rgTable, ArrayList<Integer> rgEntries, int x, int y,
			StructuralDataElement element, String data, Set<String> fileNames) {

		// Attempt to find the fileNode using the relative path
		FileNode parent = (FileNode) node.getParent();
		String canonicalPath = parent.getConicalPath() + data;
		File file = new File(canonicalPath);
		FileNode fileNode = getFileNode(parent, file.getAbsolutePath(), false);

		// If the relative path isn't found, attempt to use the data as a whole path
		if (fileNode == null) {
			file = new File(data);
			fileNode = getFileNode(parent, file.getAbsolutePath(), false);
		}

		if (fileNode != null) {
			if (fileNode.isIncluded()) {
				if (file.canRead()) {
					String fileName = file.getAbsolutePath();
					fileNode.setStructureName(table.getStructureName());
					if (fileNames.contains(fileName)) {
						String message =
								String.format(ApplicationsConstants.LOC_DATA_ROW_COLUMN_NAME, data,
										getRawDataRow(i, x), rgTable.getDataFilePositionMapping(y),
										rgTable.getRepeatableGroupName(), rgTable.getColumnName(y));

						message = message + String.format(ApplicationsConstants.ERR_TYPE_FILE_NAME_DUPLICATE);
						table.addOutput(new ValidationOutput(table, OutputType.WARNING, rgEntries.get(x), y, message));

					} else {

						fileNames.add(fileName);

						if (DataType.FILE.equals(element.getType())) {
							if (fileNode.getType() == FileType.UNKNOWN) {
								fileNode.setType(FileType.ASSOCIATED);
							}
						} else if (DataType.TRIPLANAR.equals(element.getType())) {
							if (fileNode.getType() == FileType.UNKNOWN) {
								fileNode.setType(FileType.TRIPLANAR);
							}
						} else if (DataType.THUMBNAIL.equals(element.getType())) {
							if (loadImage(file) == null) {
								String message =
										String.format(ApplicationsConstants.LOC_DATA_ROW_COLUMN_NAME, data,
												getRawDataRow(i, x), rgTable.getDataFilePositionMapping(y),
												rgTable.getRepeatableGroupName(), rgTable.getColumnName(y));

								message = message + String.format(ApplicationsConstants.ERR_TYPE_THUMBNAIL);

								table.addOutput(new ValidationOutput(table, OutputType.ERROR, rgEntries.get(x), y,
										message));
							} else {
								if (fileNode.getType() == FileType.UNKNOWN) {
									fileNode.setType(FileType.THUMBNAIL);
								}
							}
						} else if (fileNode.getType().equals(FileType.TRANSLATION_RULE)) {
							fileNode.setType(FileType.ASSOCIATED);
						}
					}
				} else {

					String message =
							String.format(ApplicationsConstants.LOC_DATA_ROW_COLUMN_NAME, data, getRawDataRow(i, x),
									rgTable.getDataFilePositionMapping(y), rgTable.getRepeatableGroupName(),
									rgTable.getColumnName(y));

					message = message + String.format(ApplicationsConstants.ERR_TYPE_FILE_PERMISSION);

					table.addOutput(new ValidationOutput(table, OutputType.ERROR, rgEntries.get(x), y, message));
				}
			} else {

				String message =
						String.format(ApplicationsConstants.LOC_DATA_ROW_COLUMN_NAME, data, getRawDataRow(i, x),
								rgTable.getDataFilePositionMapping(y), rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(y));

				message = message + String.format(ApplicationsConstants.ERR_TYPE_FILE_EXCLUDED);

				table.addOutput(new ValidationOutput(table, OutputType.ERROR, rgEntries.get(x), y, message));
			}
		} else {
			FileNode fileNodeIgnoreCase = getFileNode(parent, canonicalPath, true);
			if (fileNodeIgnoreCase != null) {

				String message =
						String.format(ApplicationsConstants.LOC_DATA_ROW_COLUMN_NAME, data, getRawDataRow(i, x),
								rgTable.getDataFilePositionMapping(y), rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(y));

				message = message + String.format(ApplicationsConstants.ERR_TYPE_FILE_CASE_SENSE);

				table.addOutput(new ValidationOutput(table, OutputType.ERROR, rgEntries.get(x), y, message));
			} else {
				String message =
						String.format(ApplicationsConstants.LOC_DATA_ROW_COLUMN_NAME, data, getRawDataRow(i, x),
								rgTable.getDataFilePositionMapping(y), rgTable.getRepeatableGroupName(),
								rgTable.getColumnName(y));

				message = message + String.format(ApplicationsConstants.ERR_TYPE_FILE_MISSING);

				table.addOutput(new ValidationOutput(table, OutputType.ERROR, rgEntries.get(x), y, message));
			}
		}
	}

	/**
	 * This function calculates a row on the raw data table based on the current entry and repeatable group
	 * 
	 * @param entry
	 * @param rgIter
	 * @return
	 */
	private int getRawDataRow(int entry, int rgIter) {

		int row = ApplicationsConstants.DATA_START_LINE + rgIter;
		for (int i = 0; i < entry; i++) {
			String[] rowData = table.getRow(i);
			int high = 0;
			for (String s : rowData) {
				if (s == null) {
					continue;
				}
				int count = 1;
				for (char c : s.toCharArray()) {
					if (c == ',') {
						count++;
					}
				}
				if (count > high) {
					high = count;
				}
			}
			row = row + high;
		}
		return row;
	}

	// XXX: We don't have conditionals, so this part is commented out currently
	private void evaluateConditional(HashMap<String, String> elementValues, String name, String type, String data,
			int loc, int i) {

		/*
		 * HashMap<AstTree, String> conditionals = table.getConditionals(name);
		 * 
		 * if (conditionals != null){ for(AstTree tree : conditionals.keySet()){ HashSet<String> valueRange = new
		 * HashSet<String>(); try{ tree.setConstraintTypes(this); AstTree subTree =
		 * tree.rowValuesSubsitution(elementValues); if (subTree == null || subTree.evaluate(this)){ for(String value :
		 * ValidationUtil.tokenizeRange(conditionals.get(tree))){ if(ValidationUtil.isColRef(value)){ value =
		 * value.substring(1).toLowerCase(); if (!value.contains(ValidationConstants.VALUE_REFERENCE_DIVIDER)){ value =
		 * table.getStructureName() + ValidationConstants.VALUE_REFERENCE_DIVIDER + value; } HashSet<String> refValues =
		 * references.get(value); if (refValues != null){ valueRange.addAll(refValues); }else{ String message =
		 * "In column " + table.getColumnName(loc) + ", data element," + name +
		 * ", conditional value range contains column reference " + value +
		 * " - No such data structure element pairing found in this submission."; table.addOutput(new
		 * ValidationOutput(table, OutputType.ERROR, -1, loc, message)); } }else if(ValidationUtil.isRowRef(value)){
		 * String actValue = elementValues.get(value.substring(1).toLowerCase()); if (actValue != null){
		 * valueRange.add(actValue); }else{ String message = "In column " + table.getColumnName(loc) + ", data element,"
		 * + name + ", conditional value range contains row references " + value +
		 * " - No such element found in this structure."; table.addOutput(new ValidationOutput(table, OutputType.ERROR,
		 * -1, loc, message)); } }else{ valueRange.add(value.trim()); } } } }catch (RuntimeException e){
		 * //e.printStackTrace(); String message = "The constraint, " + tree.toString() +
		 * " associated with data element, " + name + ", in column " + (loc + 1) + " could not evaluated because " +
		 * e.getMessage(); table.addOutput(new ValidationOutput(table, OutputType.ERROR, -1, loc, message)); }
		 * 
		 * if(!valueRange.isEmpty()){ if(!ranger.inRange(data, valueRange, type)){ String message; if
		 * (!tree.isRootNull()){ message = "Entry, " + data + ", at row " + (i+3) + " in column " +
		 * table.getColumnName(loc) + " is not within element, " + name + ", conditional, " + tree.toString() +
		 * ", value range - " + conditionals.get(tree) + ". The conditional evaluated to true."; }else{ message =
		 * "Entry, " + data + ", at row " + (i+3) + " in column " + table.getColumnName(loc) +
		 * " is not within element, " + name + ", conditional value range - " + conditionals.get(tree) +
		 * ". The conditional evaluated to true."; } table.addOutput(new ValidationOutput(table, OutputType.ERROR, i,
		 * loc, message)); } } } }
		 */
	}

	// XXX: We dont have any rules for conditionally required, so this can be left out for now
	private void evaluateConditional(HashMap<String, String> elementValues, String name, String data, int loc, int i,
			MapElement element) {

		// String requiredConstraint = element.getRequiredCondition();
		// if (requiredConstraint != null && !requiredConstraint.isEmpty()){
		// try {
		// AstTree required = new AstTree(table.getStructureName(),
		// ValidationUtil.tokenizeConstraint(requiredConstraint));
		// required.setConstraintTypes(this);
		// AstTree tree = required.rowValuesSubsitution(elementValues);
		// if(tree.evaluate(this) && data.isEmpty()){
		// String message = "Row " + (i+3) + " in column " + table.getColumnName(loc) + " is empty. Data structure "
		// + table.getStructureName() + " identifiies element " + name + " as being conditionally required." ;
		// table.addOutput(new ValidationOutput(table, OutputType.ERROR, i, loc, message));
		// }
		// } catch (ParseException e) {
		// //Theoretically these should all be valid
		// e.printStackTrace();
		// } catch (RuntimeException e){
		// // throw e;
		// String message = "Element " + name + ", in column " + table.getColumnName(loc) + " is Conditional Required. "
		// +
		// "The provided required constraint could not be evaluated because " + e.getMessage();
		// table.addOutput(new ValidationOutput(table, OutputType.ERROR, -1, loc, message));
		// }
		// }
		//
		// String prohibitedConstraint = element.getProhibitedCondition();
		// if (prohibitedConstraint != null && !requiredConstraint.isEmpty()){
		// try {
		// AstTree prohibited = new AstTree(table.getStructureName(),
		// ValidationUtil.tokenizeConstraint(prohibitedConstraint));
		// prohibited.setConstraintTypes(this);
		// AstTree tree = prohibited.rowValuesSubsitution(elementValues);
		// if(tree.evaluate(this) && !data.isEmpty()){
		// String message = "Row " + (i+3) + " in column " + table.getColumnName(loc) +
		// " contains data. Data structure "
		// + table.getStructureName() + " identifiies element " + name + " as being conditionally prohibited." ;
		// table.addOutput(new ValidationOutput(table, OutputType.ERROR, i, loc, message));
		// }
		// } catch (ParseException e) {
		// //Theoretically these should all be valid
		// e.printStackTrace();
		// }catch (RuntimeException e){
		// String message = "Element " + name + ", in column " + table.getColumnName(loc) +
		// " is conditionally required. " +
		// "The provided prohibited constraint could not be evaluated because " + e.getMessage();
		// table.addOutput(new ValidationOutput(table, OutputType.ERROR, -1, loc, message));
		// }
		// }
	}

	private FileNode getFileNode(FileNode parent, String canonicalPath, boolean ignoreCase) {

		for (int i = 0; i < parent.getChildCount(); i++) {
			FileNode node = (FileNode) parent.getChildAt(i);
			String nodePath = node.getConicalPath().replaceAll("\\\\", "/");
			canonicalPath = canonicalPath.replaceAll("\\\\", "/");
			if (ignoreCase) {
				nodePath = nodePath.toLowerCase();
				canonicalPath = canonicalPath.toLowerCase();
			}
			if (canonicalPath.startsWith(nodePath)) {
				if (canonicalPath.equalsIgnoreCase(nodePath)) {
					return node;
				} else {
					return getFileNode(node, canonicalPath, ignoreCase);
				}
			}
		}
		return null;
	}

	public boolean validateConstraint(String operator, String rowRef, Vector<String> valueRange, String type,
			MapElement iElement) throws RuntimeException {

		if (ValidationUtil.isRangeOperator(operator)) {
			HashSet<String> values = new HashSet<String>();
			for (String s : valueRange) {
				if (ValidationUtil.isColRef(s)) {
					s = s.substring(1).toLowerCase();
					if (references.get(s) != null) {
						values.addAll(references.get(s));
					} else {
						throw new RuntimeException("Value subsitution could not occur because " + s
								+ " - No such data structure element pairing found in this submission.");
					}
				} else {
					values.add(s);
				}
			}

			if (operator.equalsIgnoreCase("~")) {
				return ranger.inRange(rowRef, iElement);
			} else { // operator.equalsIgnoreCase("!~")
				return ValidationUtil.not(ranger.inRange(rowRef, iElement));
			}

		} else {
			String value = valueRange.get(0);
			if (operator.equalsIgnoreCase("=")) {
				return ValidationUtil.equals(rowRef, value, type);
			} else if (operator.equalsIgnoreCase("!=")) {
				return ValidationUtil.not(ValidationUtil.equals(rowRef, value, type));
			} else if (operator.equalsIgnoreCase("<")) {
				return ValidationUtil.less(rowRef, value, type);
			} else if (operator.equalsIgnoreCase(">")) {
				return ValidationUtil.greater(rowRef, value, type);
			} else if (operator.equalsIgnoreCase("<=")) {
				return (ValidationUtil.less(rowRef, value, type) || ValidationUtil.equals(rowRef, value, type));
			} else if (operator.equalsIgnoreCase(">=")) {
				return (ValidationUtil.greater(rowRef, value, type) || ValidationUtil.equals(rowRef, value, type));
			}
		}

		return false;
	}

	public FileNode call() throws Exception {

		validate();
		node.setErrorNum(table.getErrorCount());
		node.setWarnNum(table.getWarningCount());
		return node;
	}

	/**
	 * Attempts to load the specified file as a BufferedImage
	 * 
	 * @param file - The file to load
	 * @return - A new BufferedImage or null if the specified file is not an image
	 */
	private BufferedImage loadImage(File file) {

		InputStream input = null;

		try {
			input = new FileInputStream(file);
			return ImageIO.read(input);
		} catch (IOException ex) {
			// we need to catch the exception in
			// case the specified file does not
			// point to a 'real' image
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException ex) {
				}
			}
		}

		return null;
	}

}

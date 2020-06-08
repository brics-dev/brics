package gov.nih.tbi.constants;

import gov.nih.tbi.pojo.BeanField;
import gov.nih.tbi.pojo.FormResult;
import gov.nih.tbi.pojo.PermissibleValue;
import gov.nih.tbi.pojo.StudyResult;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.core.Var;

public class QueryToolConstants {

	public static final String PDBP_ORG_NAME = "PDBP";
	/********************* Errors ****************************/
	public static final String BIOSAMPLE_SERVICE_ERROR =
			"An error has occured while trying to invoke biosample webservice.";
	public static final String DATA_TABLE_ERROR = "An error has occured while retrieving query results.";
	public static final String STUDY_SEARCH_RESULT_ERROR_ID =
			":wrapper-form:tabView:studies-results:study-search-error";
	public static final String FORM_SEARCH_RESULT_ERROR_ID = ":wrapper-form:tabView:forms-results:form-search-error";
	public static final String FORM_RESULT_ERROR = "An error has occured while retrieving your form results.";
	public static final String STUDY_RESULT_ERROR = "An error has occured while retrieving your study results.";
	public static final String RECACHING_ERROR = "An error has occured while trying to recache meta data";
	public static final String INITIATE_DOWNLOAD_ERROR =
			"An error has occured while adding query to the download queue.";
	/********************* Prefixes ********************************/

	public static final String NINDS_URI = "http://ninds.nih.gov/";
	public static final String DICTIONARY_URI = NINDS_URI + "dictionary/ibis/1.0/";
	public static final String REPOSITORY_URI = NINDS_URI + "repository/fitbir/1.0/";

	public static final String[][] PREFIXES_ARR = {{"rdfs", "http://www.w3.org/2000/01/rdf-schema#"},
			{"xsd", "http://www.w3.org/2001/XMLSchema#"}, {"fs", DICTIONARY_URI + "FormStructure/"},
			{"element", DICTIONARY_URI + "Element/"}, {"study", REPOSITORY_URI + "Study/"},
			{"rg", REPOSITORY_URI + "RepeatableGroup/"}, {"dictionary", DICTIONARY_URI}, {"repository", REPOSITORY_URI},
			{"dataset", REPOSITORY_URI + "Dataset/"}};

	public static String PREFIXES = QueryToolConstants.EMPTY_STRING;

	public static final String DATASET_CLASS_URI = "http://ninds.nih.gov/repository/fitbir/1.0/Dataset";
	/**************************************************************/
	public static final Node FORM_PROPERTY_TITLE =
			NodeFactory.createURI("http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/title");
	public static final Node FORM_SHORT_NAME_PROPERTY =
			NodeFactory.createURI("http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/shortName");
	public static final Var FORM_NAME_VAR = Var.alloc("fsName");
	public static final Var URI_VAR = Var.alloc("uri");
	public static final Var RG_VAR = Var.alloc("rg");
	public static final Var ROW_VAR = Var.alloc("row");
	public static final Var GUID_VAR = Var.alloc("guid");
	public static final Var GUID_LABEL_VAR = Var.alloc("guidLabel");
	public static final Var GUID_N_VAR = Var.alloc("guidN");
	public static final Var STUDY_VAR = Var.alloc("study");
	public static final String ROW_URI = "rowUri";
	public static final Var SUBMISSION_VAR = Var.alloc("submission");
	public static final Var SUBMISSION_NODE_VAR = Var.alloc("submissionNode");
	public static final Var DATASET_NAME_VAR = Var.alloc("datasetName");
	public static final Var STUDY_ID_VAR = Var.alloc("studyId");
	public static final Var STUDY_TITLE_VAR = Var.alloc("study");
	public static final Var PREFIXED_ID_VAR = Var.alloc("prefixedId");
	public static final String BIOSAMPLE_VAR_NAME = "biosampleId";
	public static final Var BIOSAMPLE_VAR = Var.alloc(BIOSAMPLE_VAR_NAME);
	public static final Node RG_CLASS_NODE =
			NodeFactory.createURI("http://ninds.nih.gov/repository/fitbir/1.0/RepeatableGroup");
	public static final Node DATA_ELEMENT_CLASS = NodeFactory.createURI(DICTIONARY_URI + "Element");
	public static final Node ROW_STUDY =
			NodeFactory.createURI("http://ninds.nih.gov/repository/fitbir/1.0/Dataset/studyTitle");
	public static final Node ROW_DATASET_NAME =
			NodeFactory.createURI("http://ninds.nih.gov/repository/fitbir/1.0/Dataset/datasetName");
	public static final Node ROW_PREFIX =
			NodeFactory.createURI("http://ninds.nih.gov/repository/fitbir/1.0/Dataset/prefixedId");
	public static final Node HAS_INSTANCED_REPEATABLE_GROUP =
			NodeFactory.createURI("http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/hasRepeatableGroupInstance");
	public static final Node HAS_DATA_ELEMENT =
			NodeFactory.createURI("http://ninds.nih.gov/repository/fitbir/1.0/RepeatableGroup/hasDataElement");
	public static final Node HAS_REQUIRED_TYPE =
			NodeFactory.createURI("http://ninds.nih.gov/repository/fitbir/1.0/RepeatableGroup/hasRequiredType");
	public static final Node REPEATABLE_GROUP_PROP_NAME_N =
			NodeFactory.createURI("http://ninds.nih.gov/repository/fitbir/1.0/RepeatableGroup/name");
	public static final Node ROW_SUBMISSION =
			NodeFactory.createURI("http://ninds.nih.gov/repository/fitbir/1.0/Dataset/submissionRecordJoinId");
	public static final Node ROW_DATASET =
			NodeFactory.createURI("http://ninds.nih.gov/repository/fitbir/1.0/Dataset/datasetId");
	public static final Node HAS_REPEATABLE_GROUP =
			NodeFactory.createURI("http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/hasRepeatableGroup");
	public static final Node RG_NAME_PROPERTY =
			NodeFactory.createURI("http://ninds.nih.gov/repository/fitbir/1.0/RepeatableGroup/name");
	public static final Node RG_POSITION_PROPERTY =
			NodeFactory.createURI("http://ninds.nih.gov/repository/fitbir/1.0/RepeatableGroup/position");
	public static final Node RG_TYPE_PROPERTY =
			NodeFactory.createURI("http://ninds.nih.gov/repository/fitbir/1.0/RepeatableGroup/type");
	public static final Node RG_THRESHOLD_PROPERTY =
			NodeFactory.createURI("http://ninds.nih.gov/repository/fitbir/1.0/RepeatableGroup/threshold");
	public static final Var DATASET_ID_VAR = Var.alloc("dsId");
	public static final Var DATASET_IDS_VAR = Var.alloc("dsIds");
	public static final Var STUDY_PREFIXED_ID_VAR = Var.alloc("studyPrefixedId");
	public static final Var DO_HIGHLIGHT_VAR = Var.alloc("doHighlight");
	public static final Node ROW_GUID =
			NodeFactory.createURI("http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/guid");
	public static final Node DATA_ELEMENT_PROP_NAME_N = NodeFactory.createURI(DATA_ELEMENT_CLASS + "/elementName");
	public static final String FORM_STRUCTURE_NAMESPACE = DICTIONARY_URI + "FormStructure/";
	public static final String HAS_REPEATABLE_GROUP_INSTANCE = FORM_STRUCTURE_NAMESPACE + "hasRepeatableGroupInstance";
	public static final Node HAS_REPEATABLE_GROUP_INSTANCE_N = NodeFactory.createURI(HAS_REPEATABLE_GROUP_INSTANCE);
	public static final Var REPEATABLE_GROUP_VARIABLE = Var.alloc("repeatableGroup");
	public static final Var REPEATABLE_GROUP_NAME_VARIABLE = Var.alloc("repeatableGroupName");
	public static final Var INSTANCED_REPEATABLE_GROUP_VARIABLE = Var.alloc("instancedRepeatableGroup");
	public static final Var DATA_ELEMENT_VARIABLE = Var.alloc("dataElement");
	public static final Var DATA_ELEMENT_NAME_VARIABLE = Var.alloc("dataElementName");
	public static final Var RG_NAME_VARIABLE = Var.alloc("rgName");
	public static final Var RG_POSITION_VARIABLE = Var.alloc("rgPosition");
	public static final Var RG_TYPE_VARIABLE = Var.alloc("rgType");
	public static final Var RG_THRESHOLD_VARIABLE = Var.alloc("rgThreshold");
	public static final Var DE_POSITION_VARIABLE = Var.alloc("dePosition");
	public static final Var REQUIRED_TYPE_N_VARIABLE = Var.alloc("requiredTypeN");
	public static final Var REQUIRED_TYPE_VARIABLE = Var.alloc("requiredType");
	public static final Var COUNT_VARIABLE = Var.alloc("count");
	public static final Var VALUE_VARIABLE = Var.alloc("value");
	public static final Var ROW_VARIABLE = Var.alloc("row");
	public static final Var DATASET_ID_VARIABLE = Var.alloc("datasetId");
	public static final String INSTANCED_ROW_PROPERTY_DATASET =
			"http://ninds.nih.gov/repository/fitbir/1.0/Dataset/datasetId";
	public static final Node INSTANCED_ROW_PROPERTY_DATASET_NODE =
			NodeFactory.createURI(INSTANCED_ROW_PROPERTY_DATASET);
	public static final int DEFAULT_ROWS_TO_DISPLAY = 10;
	public static final String SUBJECT_INFORMATION_PREFIX = "Subject Information";
	public static final String SAMPLE_INFORMATION_PREFIX = "Sample Information";
	public static final int NUM_OF_HARDCODED_COLUMNS = 2;
	public static final String DICTIONARY_CONSTRUCT =
			"construct { ?s ?p ?o } \nwhere {\n?s <http://www.w3.org/2000/01/rdf-schema#subClassOf> ?class .\n?s ?p ?o . \nFILTER(?class = <http://ninds.nih.gov/repository/fitbir/1.0/Study> || ?class = <http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure> || ?class = <http://ninds.nih.gov/dictionary/ibis/1.0/Element>)\n}";
	public static final String[] ADDITIONAL_COLUMNS = {"Study", "Dataset ID"};
	// hash maps of sparql variable names to proper readable names from ADDITIONAL_COLUMNS
	public static final String PREFIX = "PREFIX ";
	public static final String MULTI_SELECT_DELIMIT = ";";
	public static final DateFormat FILTER_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
	public static final String NL = System.getProperty("line.separator");
	public static final String WS = " ";
	public final static String EMPTY_STRING = "";
	public final static String FORM_STRUCTURE_URI = DICTIONARY_URI + "FormStructure";
	public final static String STUDY_URI = REPOSITORY_URI + "Study";
	public final static String DATAELEMENT_URI = "<" + DICTIONARY_URI + "Element>";
	// added to fix problems when putting this as a string into an rdf query that post-wraps with brackets
	public final static String DE_URI = DICTIONARY_URI + "Element";
	public final static Node DATA_ELEMENT_NODE = NodeFactory.createURI(DATAELEMENT_URI);
	public final static String FORM_PREFIX = "form_";
	public final static String SUBCLASSOF = "rdfs:subClassOf";
	public final static String MISC_FIELDS[] = {"Study", "Dataset", "Submission"};
	public final static List<BeanField> PV_FIELDS = new ArrayList<BeanField>();
	public final static List<BeanField> FORM_FIELDS = new ArrayList<BeanField>();
	public final static List<BeanField> ELEMENT_FIELDS = new ArrayList<BeanField>();
	public final static List<BeanField> STUDY_FIELDS = new ArrayList<BeanField>();
	public final static List<BeanField> REPEATABLE_GROUP_FIELDS = new ArrayList<BeanField>();
	public static final String TEXT_SEARCH_STUDY_PREDICATE_KEY = "textSearch.study.predicates";
	public static final String TEXT_SEARCH_FORM_PREDICATE_KEY = "textSearch.form.predicates";
	public static final String TEXT_SEARCH_STUDY_PREDICATE_VALUE =
			"http://www.w3.org/2000/01/rdf-schema#label^http://ninds.nih.gov/repository/fitbir/1.0/Study/title^http://ninds.nih.gov/repository/fitbir/1.0/Study/abstract^http://ninds.nih.gov/repository/fitbir/1.0/Study/principalInvestigator^http://ninds.nih.gov/repository/fitbir/1.0/Study/principalInvestigatorEmail^http://ninds.nih.gov/repository/fitbir/1.0/Study/clinicalTrial^http://ninds.nih.gov/repository/fitbir/1.0/Study/grant";
	public static final String TEXT_SEARCH_FORM_PREDICATE_VALUE =
			"http://www.w3.org/2000/01/rdf-schema#label^http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/title^http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/shortName^http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure/description";
	public static final String STRING_TYPE = "xsd:string";
	public static final String NUMERIC_TYPE = "xsd:decimal";
	public static final String OPTIONAL_START = "OPTIONAL { ";
	public static final String END_BRACKET = " }";
	public static final boolean IS_INSTANCED_COLUMN_LIMITED = true;
	public static final String RG_EXACTLY = "EXACTLY";
	public static final String RG_LESSTHAN = "LESSTHAN";
	public static final String RG_MORETHAN = "MORETHAN";
	public static final String SUCCESS = "success";
	public static final String ERROR = "error";
	public static final int INSTANCED_DATA_COLUMN_LIMIT = 30; // controls the maximum number of columns allowed in an
																 // instanced data view
	public static final int INSTANCED_JOINED_DATA_COLUMN_LIMIT = 15;
	public static final int DOWNLOAD_DIRECTORY_FORM_LIMIT = 10;
	public static final String JOIN_SEPARATOR = "_join_";
	public static final String STUDY_COLUMN_VAR = "?study";
	public static final String DOUBLE_QUOTE = "\"";
	public static final String DATASET_COLUMN_VAR = "?prefixedId";
	public static final String BIOSAMPLE_CELL_PREFIX = "tbiosample";
	public static final Node VISIT_TYPE_URI =
			NodeFactory.createURI("http://ninds.nih.gov/dictionary/ibis/1.0/Element/DataElement/VisitTypPDBP");
	public static final Var VISIT_TYPE_VAR = Var.alloc("visitType");

	public static final Node STUDY_PROPERTY_PREFIXED_ID =
			NodeFactory.createURI("http://ninds.nih.gov/repository/fitbir/1.0/Study/prefixedId");

	/********************* Queries *********************************/
	public static final String INSTANCED_DATA_QUERY = "SELECT ?row ?rgName ?deName ?v\n" + "WHERE {\n"
			+ "<%s> fs:hasRepeatableGroup ?rg .\n" + "?rg rg:name ?rgName .\n" + "?rg rg:hasDataElement ?de .\n"
			+ "?de element:elementName ?deName .\n" + "?row ?de ?v .\n";

	public static final String FORM_FILTER_PATTERN = "?row a ?uri .\n" + "?row fs:hasRepeatableGroupInstance ?rg .\n";
	public static final String STUDY_FILTER_PATTERN =
			"?uri study:facetedForm ?form .\n" + "?row a ?form .\n" + "?row fs:hasRepeatableGroupInstance ?rg .\n";

	public static final String ASCENDING = "ASC";
	public static final String DESCENDING = "DESC";
	public static final String GUID_COLUMN_VAR = "?guid";
	public static final String GREYED_FLAG = "%greydis%";
	public static final Var DATASET_VAR = Var.alloc("dataset");
	public static final Var FORM_VAR = Var.alloc("form");

	public final static String HEXES = "0123456789abcdef";
	public final static int EXPIRE_ACCOUNT_AFTER_DAYS = 60;
	public final static String LOGIN_FAILURE_DEFAULT = "/login.jsp?login_error=1";
	public final static String LOGIN_FAILURE_EXPIRED =
			"/publicAccounts/passwordRecoveryAction!input.action?login_error=1";

	public static final String RDF_CONNECTION = "rdfConnection";

	public static final String FACETED_DE_URI = "http://ninds.nih.gov/repository/fitbir/1.0/Study/facetedDE";
	public static final Node FACETED_DE_N =
			NodeFactory.createURI("http://ninds.nih.gov/repository/fitbir/1.0/Study/facetedDE");

	public static final String[] deFacetConfig =
			{"Data Elements", "<http://ninds.nih.gov/repository/fitbir/1.0/Study/facetedDE>",
					"<http://ninds.nih.gov/dictionary/ibis/1.0/FormStructure>"};

	static {
		PV_FIELDS.add(new BeanField("uri", "http://ninds.nih.gov/dictionary/ibis/1.0/Element", String.class, false));
		PV_FIELDS.add(new BeanField("valueLiteral",
				"http://ninds.nih.gov/dictionary/ibis/1.0/Element/permissibleValues#value", String.class, false));
		PV_FIELDS.add(new BeanField("valueDescription",
				"http://ninds.nih.gov/dictionary/ibis/1.0/Element/permissibleValues#description", String.class, false));

		FORM_FIELDS.add(new BeanField("uri", "dictionary:FormStructure", String.class, false));
		FORM_FIELDS.add(new BeanField("id", "fs:formStructureId", Long.class, false));
		FORM_FIELDS.add(new BeanField("shortName", "fs:shortName", String.class, false));
		FORM_FIELDS.add(new BeanField("title", "fs:title", String.class, false));
		FORM_FIELDS.add(new BeanField("version", "fs:version", String.class, false));
		FORM_FIELDS.add(new BeanField("studies", "study:facetedStudy", StudyResult.class, true));

		/** Repeatable Group Fields **/
		REPEATABLE_GROUP_FIELDS.add(new BeanField("group_uri", "repository:RepeatableGroup", String.class, false));
		REPEATABLE_GROUP_FIELDS.add(new BeanField("group_name", "rdfs:label", String.class, false));
		REPEATABLE_GROUP_FIELDS.add(new BeanField("group_position", "rg:position", Integer.class, false));

		/** Element Fields **/
		ELEMENT_FIELDS.add(new BeanField("uri", "dictionary:Element", String.class, false));
		ELEMENT_FIELDS.add(new BeanField("name", "element:elementName", String.class, false));
		ELEMENT_FIELDS.add(new BeanField("id", "element:dataElementId", Long.class, false));
		ELEMENT_FIELDS.add(new BeanField("title", "element:title", String.class, false));
		ELEMENT_FIELDS.add(new BeanField("description", "element:description", String.class, false));
		ELEMENT_FIELDS.add(new BeanField("inputRestrictions", "element:inputRestriction", String.class, false));
		ELEMENT_FIELDS.add(new BeanField("type", "element:elementType", String.class, false));
		ELEMENT_FIELDS.add(new BeanField("maximumValue", "element:maximumValue", Double.class, false));
		ELEMENT_FIELDS.add(new BeanField("minimumValue", "element:minimumValue", Double.class, false));
		ELEMENT_FIELDS
				.add(new BeanField("permissibleValues", "element:permissibleValues", PermissibleValue.class, true));
		ELEMENT_FIELDS.add(new BeanField("position", "element:position", Integer.class, false));

		/** Study Fields **/
		STUDY_FIELDS.add(new BeanField("uri", "repository:Study", String.class, false));
		STUDY_FIELDS.add(new BeanField("title", "rdfs:label", String.class, false));
		STUDY_FIELDS.add(new BeanField("id", "study:studyId", Long.class, false));
		STUDY_FIELDS.add(new BeanField("pi", "study:principalInvestigator", String.class, false));
		STUDY_FIELDS.add(new BeanField("status", "study:status", String.class, false));
		STUDY_FIELDS.add(new BeanField("forms", "study:facetedForm", FormResult.class, true));
		STUDY_FIELDS.add(new BeanField("abstractText", "study:abstract", String.class, false));
		STUDY_FIELDS.add(new BeanField("prefixedId", "study:prefixedId", String.class, false));

		/** RDF PREFIXES **/
		StringBuffer prefixSb = new StringBuffer();
		for (String[] prefix : PREFIXES_ARR) {
			prefixSb.append(PREFIX).append(prefix[0]).append(":<").append(prefix[1]).append(">").append(NL);
		}

		PREFIXES = prefixSb.toString();
	}

	/***** Derived Data ****/

	public static final Var VISIT_TYPE_RG_INSTANCE_VAR = Var.alloc("visitTypeRgInstance");
	public static final Var IS_DERIVED_VAR = Var.alloc("isDerived");
	public static final Node ROW_IS_DERIVED =
			NodeFactory.createURI("http://ninds.nih.gov/repository/fitbir/1.0/Dataset/isDerived");


	/***** Downloading cart, copied from ServiceConstants ****/

	public static final String NAME_READABLE = "variable name";
	public static final String TITLE_READABLE = "title";
	public static final String ELEMENT_TYPE_READABLE = "element type";
	public static final String VERSION_READABLE = "version";
	public static final String DEFINITION_READABLE = "definition";
	public static final String SHORT_DESCRIPTION_READABLE = "short description";
	public static final String DATA_TYPE_READABLE = "data type";
	public static final String MAX_CHAR_QUANTITY_READBLE = "maximum character quantity";
	public static final String INPUT_RESTRICTION_READABLE = "input restriction";
	public static final String MINIMUM_VALUE_READABLE = "minimum value";
	public static final String MAXIMUM_VALUE_READABLE = "maximum value";
	public static final String PERMISSIBLE_VALUES_READABLE = "permissible values";
	public static final String PERMISSIBLE_VALUES_DESCRIPTION_READABLE = "permissible value descriptions";
	public static final String PERMISSIBLE_VALUES_OUTPUT_CODES_READABLE = "permissible value output codes";
	public static final String ITEM_RESPONSE_OID_READABLE = "item response oid";
	public static final String ELEMENT_OID_READABLE = "element oid";
	public static final String UNIT_OF_MEASURE_READABLE = "unit of measure";
	public static final String GUIDELINES_READABLE = "guidelines";
	public static final String NOTES_READABLE = "notes";
	public static final String PREFERRED_QUESTION_TEXT_READABLE = "preferred question text";
	public static final String KEYWORDS_READABLE = "keywords";
	public static final String REFERENCES_READABLE = "references";
	public static final String POPULATION_ALL_READABLE = "population.all";
	public static final String HISTORICAL_NOTES_READABLE = "historical notes";
	public static final String LABELS_READABLE = "labels";
	public static final String SEE_ALSO_READABLE = "see also";
	public static final String SUBMITTING_ORG_NAME_READABLE = "submitting organization name";
	public static final String SUBMITTING_CONTACT_NAME_READABLE = "submitting contact name";
	public static final String SUBMITTING_CONTACT_INFO_READABLE = "submitting contact information";
	public static final String EFFECTIVE_DATE_READABLE = "effective date";
	public static final String UNTIL_DATE_READABLE = "until date";
	public static final String STEWARD_ORG_NAME_READABLE = "steward organization name";
	public static final String STEWARD_CONTACT_NAME_READABLE = "steward contact name";
	public static final String STEWARD_CONTACT_INFO_READABLE = "steward contact information";



	public static final String QT_PV_MAPPING_FILE = "data_element_details";
	public static final String META_STUDY_FILE_PATH = "metaStudy/";

	public static final String SFTP_NAME = "sftp";
	public static final String STRICT_HOST_KEY_CHECK = "StrictHostKeyChecking";
	public static final String NO = "no";
	public static final String FILE_SEPARATER = "/";
	public static final String HIGHLIGHTED_FLAG = "%highlightdis%";
	public static final String MDS_UPDRS_X_FLAG = "%colorDis%";
	public static final Var MDS_UPDRS_X_VAR = Var.alloc("mdsUpdrsX");
	public static final Var SEE_ALSO_VAR = Var.alloc("seeAlso");
	public static final Var FS_ID_VAR = Var.alloc("fsId");
	public static final Var FS_URI_VAR = Var.alloc("fsUri");
	public static final Var DE_TITLE_VARIABLE = Var.alloc("deTitle");
	public static final String FS_LINK_FORMAT = "%s/dictionary/dataStructureAction!view.action?dataStructureName=%s";
	public static final String DE_LINK_FROMAT = "%s/dictionary/dataElementAction!view.action?dataElementName=%s";
}
